package be.michielve.f1_api.services;

import be.michielve.f1_api.models.*;
import be.michielve.f1_api.models.request.CreatePoolRequest;
import be.michielve.f1_api.models.request.CreatePredictionRequest;
import be.michielve.f1_api.models.request.CreateSeasonPredictionRequest;
import be.michielve.f1_api.models.request.JoinPoolRequest;
import be.michielve.f1_api.models.response.MemberPredictionDTO;
import be.michielve.f1_api.models.response.PoolDetailsResponse;
import be.michielve.f1_api.models.response.PoolSummaryResponse;
import be.michielve.f1_api.models.response.PredictionStatusResponse;
import be.michielve.f1_api.models.response.SavedPredictionResponse;
import be.michielve.f1_api.repositories.*;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PredictionService {

        private final PoolRepository poolRepository;
        private final TeamPredictionRepository teamPredictionRepository;
        private final TeamRepository teamRepository;
        private final UserRepository userRepository;

        @Transactional(readOnly = true)
        public SavedPredictionResponse getPersonalTeamPrediction(UUID userId) {
                TeamPrediction prediction = teamPredictionRepository.findPersonalByUserId(userId)
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "No personal team prediction found for this user"));

                List<String> teamIds = prediction.getPredictedTeams().stream()
                                .map(team -> team.getId().toString())
                                .toList();

                return SavedPredictionResponse.builder()
                                .predictedTeams(teamIds)
                                .build();
        }

        @Transactional(readOnly = true)
        public SavedPredictionResponse getPoolTeamPrediction(UUID userId, UUID poolId) {
                Pool pool = poolRepository.findById(poolId)
                                .orElseThrow(() -> new IllegalArgumentException("Pool not found"));

                boolean isMember = pool.getMembers().stream().anyMatch(m -> m.getId().equals(userId));
                if (!isMember) {
                        throw new AccessDeniedException("Access denied. You are not a member of this pool");
                }

                TeamPrediction prediction = teamPredictionRepository.findByUserIdAndPoolId(userId, poolId)
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "No team prediction found for this pool"));

                List<String> teamIds = prediction.getPredictedTeams().stream()
                                .map(team -> team.getId().toString())
                                .toList();

                return SavedPredictionResponse.builder()
                                .predictedTeams(teamIds)
                                .build();
        }

        @Transactional
        public void createTeamPrediction(UUID userId, CreateSeasonPredictionRequest request) {
                Pool pool = null;
                if (request.getPoolId() != null) {
                        pool = poolRepository.findById(request.getPoolId())
                                        .orElseThrow(() -> new IllegalArgumentException("Pool not found"));

                        boolean isMember = pool.getMembers().stream().anyMatch(m -> m.getId().equals(userId));
                        if (!isMember) {
                                throw new AccessDeniedException("You are not part of this pool");
                        }
                }

                Optional<TeamPrediction> existing = request.getPoolId() == null
                                ? teamPredictionRepository.findPersonalByUserId(userId)
                                : teamPredictionRepository.findByUserIdAndPoolId(userId, request.getPoolId());
                if (existing.isPresent()) {
                        throw new IllegalStateException("You have already submitted a prediction for this context");
                }

                // FIX: findAllById does not preserve input order — re-sort to match request
                // order
                List<UUID> orderedIds = request.getPredictedTeams();
                Map<UUID, Team> teamMap = teamRepository.findAllById(orderedIds).stream()
                                .collect(Collectors.toMap(Team::getId, t -> t));
                List<Team> teams = orderedIds.stream()
                                .map(teamMap::get)
                                .filter(t -> t != null)
                                .toList();

                User user = userRepository.findById(userId)
                                .orElseThrow(() -> new IllegalArgumentException("User not found"));

                Timestamp now = Timestamp.from(Instant.now());
                TeamPrediction prediction = TeamPrediction.builder()
                                .user(user)
                                .pool(pool)
                                .predictedTeams(teams)
                                .created_at(now)
                                .updated_at(now)
                                .build();

                teamPredictionRepository.save(prediction);
        }

        @Transactional(readOnly = true)
        public List<PoolSummaryResponse> getUserPools(UUID userId) {
                return poolRepository.findPoolsByUserId(userId).stream()
                                .map(pool -> PoolSummaryResponse.builder()
                                                .id(pool.getId())
                                                .name(pool.getName())
                                                .memberCount(pool.getMembers().size())
                                                .creatorId(pool.getCreator().getId())
                                                .inviteCode(pool.getInviteCode())
                                                .build())
                                .toList();
        }

        @Transactional(readOnly = true)
        public PoolDetailsResponse getPoolDetails(UUID userId, UUID poolId) {
                Pool pool = poolRepository.findById(poolId)
                                .orElseThrow(() -> new IllegalArgumentException("Pool not found"));

                boolean isMember = pool.getMembers().stream().anyMatch(m -> m.getId().equals(userId));
                if (!isMember) {
                        throw new AccessDeniedException("Access denied to this pool dashboard");
                }

                List<TeamPrediction> predictions = teamPredictionRepository.findByPoolId(poolId);

                List<MemberPredictionDTO> leaderBoard = pool.getMembers().stream().map(member -> {
                        Optional<TeamPrediction> pred = predictions.stream()
                                        .filter(p -> p.getUser().getId().equals(member.getId()))
                                        .findFirst();

                        // Changed: return IDs instead of names, preserving prediction_order
                        List<String> teamIds = pred.map(teamPrediction -> teamPrediction.getPredictedTeams().stream()
                                        .map(team -> team.getId().toString())
                                        .toList()).orElse(List.of());

                        return MemberPredictionDTO.builder()
                                        .userId(member.getId())
                                        .username(member.getName())
                                        .picture(member.getPicture())
                                        .predictedTeamIds(teamIds)
                                        .build();
                }).toList();

                return PoolDetailsResponse.builder()
                                .poolId(pool.getId())
                                .poolName(pool.getName())
                                .creatorId(pool.getCreator().getId())
                                .leaderBoard(leaderBoard)
                                .build();
        }

        @Transactional(readOnly = true)
        public PredictionStatusResponse getPredictionStatus(UUID userId) {
                boolean hasPools = !poolRepository.findPoolsByUserId(userId).isEmpty();
                boolean hasPersonal = teamPredictionRepository.findPersonalByUserId(userId).isPresent();

                return PredictionStatusResponse.builder()
                                .hasPools(hasPools)
                                .hasPersonalPrediction(hasPersonal)
                                .build();
        }

        @Transactional
        public Pool createPool(UUID userId, CreatePoolRequest request) {
                User user = userRepository.findById(userId)
                                .orElseThrow(() -> new IllegalArgumentException("User not found"));

                String inviteCode = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
                Timestamp now = Timestamp.from(Instant.now());

                Pool pool = Pool.builder()
                                .name(request.getName())
                                .inviteCode(inviteCode)
                                .creator(user)
                                .members(new java.util.ArrayList<>(List.of(user)))
                                .created_at(now)
                                .updated_at(now)
                                .build();

                return poolRepository.save(pool);
        }

        @Transactional
        public void leaveOrDeletePool(UUID userId, UUID poolId) {
                Pool pool = poolRepository.findById(poolId)
                                .orElseThrow(() -> new IllegalArgumentException("Pool not found"));

                boolean isMember = pool.getMembers().stream().anyMatch(m -> m.getId().equals(userId));
                if (!isMember) {
                        throw new IllegalStateException("You are not a member of this pool");
                }

                if (pool.getCreator().getId().equals(userId) || pool.getMembers().size() <= 1) {
                        teamPredictionRepository.deleteByPoolId(poolId);
                        poolRepository.delete(pool);
                        return;
                }

                pool.getMembers().removeIf(member -> member.getId().equals(userId));
                teamPredictionRepository.findByUserIdAndPoolId(userId, poolId)
                                .ifPresent(teamPredictionRepository::delete);

                poolRepository.save(pool);
        }

        @Transactional
        public void kickMemberOrDeletePool(UUID adminId, UUID poolId, UUID memberId) {
                Pool pool = poolRepository.findById(poolId)
                                .orElseThrow(() -> new IllegalArgumentException("Pool not found"));

                if (!pool.getCreator().getId().equals(adminId)) {
                        throw new AccessDeniedException("Only the pool creator can remove members");
                }

                if (adminId.equals(memberId) || pool.getMembers().size() <= 1) {
                        teamPredictionRepository.deleteByPoolId(poolId);
                        poolRepository.delete(pool);
                        return;
                }

                boolean found = pool.getMembers().removeIf(member -> member.getId().equals(memberId));
                if (!found) {
                        throw new IllegalArgumentException("Target user is not a member of this pool");
                }

                teamPredictionRepository.findByUserIdAndPoolId(memberId, poolId)
                                .ifPresent(teamPredictionRepository::delete);

                poolRepository.save(pool);
        }

        @Transactional
        public void joinPool(UUID userId, JoinPoolRequest request) {
                Pool pool = poolRepository.findByInviteCode(request.getInviteCode().toUpperCase())
                                .orElseThrow(() -> new IllegalArgumentException("Invalid invite code"));

                User user = userRepository.findById(userId)
                                .orElseThrow(() -> new IllegalArgumentException("User not found"));

                if (pool.getMembers().stream().anyMatch(m -> m.getId().equals(userId))) {
                        throw new IllegalStateException("You are already a member of this pool");
                }

                pool.getMembers().add(user);
                poolRepository.save(pool);
        }

        public void createPrediction(UUID id, CreatePredictionRequest request) {
        }
}
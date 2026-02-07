package be.michielve.f1_api.controllers;

import be.michielve.f1_api.models.response.RaceResponse;
import be.michielve.f1_api.services.RaceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/")
@RequiredArgsConstructor
public class RaceController {

        private final RaceService raceService;

        @GetMapping("races/{season}")
        public ResponseEntity<List<RaceResponse>> getAllRacesForSeason(
                        @PathVariable String season) {
                List<RaceResponse> races = raceService.getAllRacesForSeason(season);

                if (races.isEmpty()) {
                        return ResponseEntity.noContent().build();
                }

                return ResponseEntity.ok(races);
        }
}

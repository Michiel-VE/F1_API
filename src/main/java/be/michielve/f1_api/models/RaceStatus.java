package be.michielve.f1_api.models;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Table(name = "race_status", schema = "f1_api")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RaceStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "name", nullable = false, unique = true)
    private String name;
}
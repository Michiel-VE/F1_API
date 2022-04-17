package be.kwallie.F1.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
@SuperBuilder
@Entity
public class Team {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected long id;

    @OneToMany(mappedBy = "team")
    private List<Driver> drivers;

    @NotBlank
    @Column(name = "NAME", nullable = false)
    private String name;

    @Column(name = "ORIGIN", nullable = false)
    private String origin;
}

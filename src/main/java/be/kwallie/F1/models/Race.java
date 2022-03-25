package be.kwallie.F1.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Entity
public class Race {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected long id;

    @NotBlank
    @Column(name = "NAME", nullable = false)
    private String name;

    @NotBlank
    @Column(name = "LAT", nullable = false)
    private double lat;

    @NotBlank
    @Column(name = "LON", nullable = false)
    private double lon;

    @Column(name = "CITY", nullable = false)
    private String city;

    @Column(name = "COUNTRY", nullable = false)
    private String country;

    @Column(name = "DATE", nullable = false)
    private Date date;

    @Column(name = "TIMEZONE", nullable = false)
    private String timezone;

    @Column(name = "ACTIVE", nullable = false)
    private boolean active;
}

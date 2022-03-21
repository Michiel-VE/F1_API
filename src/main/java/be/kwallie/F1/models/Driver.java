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
public class Driver {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected long id;

    @NotBlank
    @Column(name = "FIRSTNAME", nullable = false)
    private String firstName;

    @NotBlank
    @Column(name = "LASTNAME", nullable = false)
    private String lastName;

    @NotBlank
    @Column(name = "PERMANENTNUMBER", nullable = false)
    private String permanentNumber;

    @NotBlank
    @Column(name = "CODE", nullable = false)
    private String code;

    @Column(name = "BIRTHDAY", nullable = false)
    private Date birthday;

    @Column(name = "PICTURE", nullable = false)
    private String picture;

    @Column(name = "COUNTRY", nullable = false)
    private String country;

    @Column(name = "WINS", nullable = false)
    private String wins;

    @Column(name = "POINTS", nullable = false)
    private double points;

    @Column(name = "PENALTYPOINTS", nullable = false)
    private int penaltyPoints;
}

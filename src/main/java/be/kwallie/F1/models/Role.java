package be.kwallie.F1.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "role")
public class Role {
    @Id
    @GeneratedValue(generator = "inquisitive-uuid")
    @GenericGenerator(name = "inquisitive-uuid", strategy = "be.kwallie.F1.utils.InquisitiveUUIDGenerator")
    @Type(type = "uuid-char")
    private UUID id;

    @Column(name = "name")
    private String name;

}

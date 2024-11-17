package cotede.interns.project.guessify.model;
import java.io.Serializable;

import jakarta.persistence.*;
import lombok.*;


@Entity
@Table(name = "users")
@Data
@AllArgsConstructor
@NoArgsConstructor

public class User implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name" , unique = true ,nullable = false)
    private String name;

    @Embedded
    private Profile profile;

    @Column(name = "password" , nullable = false)
    private String password;

    @Column(name = "role" , nullable = false)
    private  String role ;

    @Column(name = "score", nullable = false)
    private Integer score = 0;

    @Column(name = "hints_used_count")
    private Integer hintsUsed = 0;
}

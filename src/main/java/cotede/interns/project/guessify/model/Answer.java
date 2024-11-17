package cotede.interns.project.guessify.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter

@Table(name = "answers")
public class Answer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Integer answerNumber;
    private LocalDateTime submissionTime;

    @ManyToOne
    @JoinColumn(name = "round_id")
    private Round round;

    private Integer hintsUsed = 0;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

}
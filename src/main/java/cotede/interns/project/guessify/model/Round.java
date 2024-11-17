package cotede.interns.project.guessify.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


@Entity
@Setter
@Getter

@Table(name = "rounds")
public class Round {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer roundId;

    @Column(columnDefinition = "TEXT")
    private String content;
    private String correctAnswer;
    private Integer correctAnswerNumber;

    @ElementCollection
    @CollectionTable(name = "round_hints", joinColumns = @JoinColumn(name = "round_id"))
    @Column(name = "hints")
    private List<String> hints = new ArrayList<>();


    @OneToMany(mappedBy = "round", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Answer> answers = Collections.synchronizedList(new ArrayList<>());

    @ManyToOne
    @JoinColumn(name = "session_id")
    private GameSession gameSession;
}


package cotede.interns.project.guessify.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
@Entity
@Setter
@Getter

@Table(name = "game_sessions")
public class GameSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long sessionId;

    @OneToOne
    @JoinColumn(name = "room_id")
    private Room room;

    private Integer currentRoundNumber = 0;
    private final Integer totalRounds = 5;

    @OneToMany(mappedBy = "gameSession", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    private List<Round> rounds = new ArrayList<>();

    private Boolean isActive = true;
}

package cotede.interns.project.guessify.repository;

import java.util.Optional;
import cotede.interns.project.guessify.model.GameSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;



@Repository
public interface GameSessionRepository extends JpaRepository<GameSession, Long> {
    Optional<GameSession> findByRoom_RoomIdAndIsActiveTrue(Long roomId);
    void deleteBySessionId(Long sessionId);

}
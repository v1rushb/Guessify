package cotede.interns.project.guessify.repository;

import cotede.interns.project.guessify.model.Round;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface RoundRepository extends JpaRepository<Round, Integer> {
    @Query("SELECT r FROM Round r LEFT JOIN FETCH r.answers WHERE r.roundId = :roundId")
    Optional<Round> findByIdWithAnswers(@Param("roundId") Integer roundId);
}

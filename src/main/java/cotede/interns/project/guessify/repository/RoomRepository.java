package cotede.interns.project.guessify.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import cotede.interns.project.guessify.model.Room;
import org.springframework.stereotype.Repository;



@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {

}

package cotede.interns.project.guessify.service;

import cotede.interns.project.guessify.response.UserScoreResponse;
import cotede.interns.project.guessify.dto.RoomDTO;
import cotede.interns.project.guessify.dto.RoomWithUserCountDTO;
import cotede.interns.project.guessify.exception.RoomNotFoundException;
import cotede.interns.project.guessify.exception.UserNotFoundException;
import cotede.interns.project.guessify.model.Room;
import cotede.interns.project.guessify.model.User;
import cotede.interns.project.guessify.dto.UserDTO;
import cotede.interns.project.guessify.repository.RoomRepository;
import cotede.interns.project.guessify.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class RoomService {

    private final RoomRepository roomRepository;
    private final UserRepository userRepository;
    private final ThreadPoolTaskScheduler taskScheduler;

    @Autowired
    public RoomService(RoomRepository roomRepository, UserRepository userRepository) {
        this.roomRepository = roomRepository;
        this.userRepository = userRepository;
        this.taskScheduler = new ThreadPoolTaskScheduler();
        this.taskScheduler.initialize();
    }

    public boolean checkRoomCapacity(Long roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found ! "));

        if (room.getUsers().size() >= 2 && room.getUsers().size() <= room.getCapacity()) {
            return true;
        }
        return false;
    }

    public Room createRoom(Room room) {
        return roomRepository.save(room);
    }

    public void removePlayerFromRoom(Long userId, Long roomId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RoomNotFoundException(roomId));

        if (room.getUsers().contains(user)) {
            user.setHintsUsed(0);
            room.getUsers().remove(user);
            roomRepository.save(room);
            userRepository.save(user);
        } else {
            throw new RuntimeException("User not found in room");
        }
    }

    public boolean addPlayerToRoom(Long userId, Long roomId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        if (room.getUsers().size() >= room.getCapacity()) {
            return false;
        }

        room.getUsers().add(user);
        roomRepository.save(room);
        return true;
    }

    public Optional<Room> getRoomById(Long roomId) {
        return roomRepository.findById(roomId);
    }

    public List<UserDTO> getUserDTOsInRoom(Long roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        return room.getUsers().stream()
                .map(user -> new UserDTO(user.getId(), user.getName(), user.getScore()))
                .collect(Collectors.toList());
    }

    public RoomDTO getRoomDTOById(Long roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RoomNotFoundException(roomId));

        List<UserDTO> userDTOs = room.getUsers().stream()
                .map(user -> new UserDTO(user.getId(), user.getName(), user.getScore()))
                .collect(Collectors.toList());

        return new RoomDTO(room.getRoomId(), room.getCapacity(), userDTOs);
    }

    public List<UserDTO> getUsersInRoom(Long roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        List<UserDTO> users = room.getUsers().stream()
                .map(user -> new UserDTO(user.getId(), user.getName(), user.getScore()))
                .collect(Collectors.toList());
        return users;
    }

    public List<UserScoreResponse> getUsersInRoomWithScores(Long roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RoomNotFoundException(roomId));

        List<UserDTO> users = getUsersInRoom(roomId);

        List<UserScoreResponse> userScores = users.stream()
                .map(user -> new UserScoreResponse(user.getId() ,user.getName(), user.getScore()))
                .collect(Collectors.toList());

        return userScores;
    }

    public List<RoomWithUserCountDTO> getAllRoomsWithUserCounts() {
        List<Room> rooms = roomRepository.findAll();
        List<RoomWithUserCountDTO> roomWithCounts = new ArrayList<>();

        for (Room room : rooms) {
            if (!room.getRoomLocked()) {
                roomWithCounts.add(new RoomWithUserCountDTO(room.getRoomId(), room.getCapacity(), room.getUsers().size()));
            }
        }

        return roomWithCounts;
    }
}


package cotede.interns.project.guessify.controller;

import cotede.interns.project.guessify.service.GameService;
import cotede.interns.project.guessify.model.GameSession;
import cotede.interns.project.guessify.response.RoundResponse;
import cotede.interns.project.guessify.response.UserScoreResponse;
import cotede.interns.project.guessify.dto.RoomDTO;
import cotede.interns.project.guessify.dto.RoomWithUserCountDTO;
import cotede.interns.project.guessify.exception.GameLogicException;
import cotede.interns.project.guessify.exception.RoomNotFoundException;
import cotede.interns.project.guessify.exception.UserNotFoundException;
import cotede.interns.project.guessify.model.Room;
import cotede.interns.project.guessify.repository.RoomRepository;
import cotede.interns.project.guessify.dto.UserDTO;
import cotede.interns.project.guessify.service.RoomService;
import cotede.interns.project.guessify.webSocket.AnswerMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.*;
import java.util.concurrent.*;

@RestController
@RequestMapping("/guessify")
public class GameController {
//
    @Autowired
    private GameService gameService;

    @Autowired
    private RoomService roomService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private RoomRepository roomRepository;

    private ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(10);
    private Map<Long, ScheduledFuture<?>> roomTimers = new ConcurrentHashMap<>();
    private static final Logger logger = LoggerFactory.getLogger(GameController.class);

    @MessageMapping("/start-game/{roomId}")
    public void startGameFromClient(@DestinationVariable Long roomId, @Payload Map<String, Object> payload) {
        startGame(roomId, payload);
    }

    private void startGame(Long roomId, Map<String, Object> payload) {
        Integer totalRounds = (Integer) payload.get("totalRounds");
        try {
            GameSession gameSession = gameService.startGameSession(roomId);

            RoundResponse gameData = gameService.startNextRound(gameSession.getSessionId());
            messagingTemplate.convertAndSend("/room/" + roomId + "/question", gameData);
        } catch (GameLogicException e) {
            logger.error("Error starting game: {}", e.getMessage());
        }
    }

    @MessageMapping("/end-game/{roomId}")
    public void endGame(@DestinationVariable Long roomId) {
        Room room = roomRepository.findById(roomId).orElseThrow(() -> new RoomNotFoundException(roomId));

        GameSession gameSession = room.getGameSession();
        if (gameSession == null) {
            throw new GameLogicException("No active game session found for this room.");
        }

        UserDTO winner = gameService.endGame(gameSession.getSessionId());

        room.setRoomLocked(false);
        roomRepository.save(room);

        Map<String, String> winnerMessageMap = new HashMap<>();
        if (winner != null) {
            winnerMessageMap.put("winner", "The winner is '" + winner.getName() + "'");
        } else {
            winnerMessageMap.put("winner", "No winner, the game ended with no players.");
        }
        messagingTemplate.convertAndSend("/room/" + roomId + "/end-game", winnerMessageMap);
    }

    @PostMapping("room/create-room")
    public ResponseEntity<Room> createRoom(@RequestParam Long userId, Room room) {
        try {
            Room newRoom = new Room();
            if (room.getCapacity() == null || room.getCapacity() < 1) {
                newRoom.setCapacity(4);
            } else
                newRoom.setCapacity(room.getCapacity());

            Room createdRoom = roomService.createRoom(newRoom);
            roomService.addPlayerToRoom(userId, createdRoom.getRoomId());
            return ResponseEntity.ok(createdRoom);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @MessageMapping("/submitAnswer")
    public void submitAnswer(AnswerMessage answerMessage) {
        try {
            gameService.submitAnswer(
                    answerMessage.getUserId(),
                    answerMessage.getRoundId(),
                    answerMessage.getAnswerNumber()
            );

            messagingTemplate.convertAndSendToUser(
                    String.valueOf(answerMessage.getUserId()),
                    "/queue/answerFeedback",
                    "Answer submitted successfully"
            );

        } catch (UserNotFoundException e) {
            messagingTemplate.convertAndSendToUser(
                    String.valueOf(answerMessage.getUserId()),
                    "/queue/errors",
                    "User not found: " + e.getMessage()
            );
        } catch (GameLogicException e) {
            messagingTemplate.convertAndSendToUser(
                    String.valueOf(answerMessage.getUserId()),
                    "/queue/errors",
                    "Game logic error: " + e.getMessage()
            );
        } catch (RuntimeException e) {
            messagingTemplate.convertAndSendToUser(
                    String.valueOf(answerMessage.getUserId()),
                    "/queue/errors",
                    "Error: " + e.getMessage()
            );
        }
    }

    @PostMapping("/room/{roomId}/join")
    public ResponseEntity<String> joinRoom(@PathVariable Long roomId, @RequestBody Map<String, Long> requestBody) {
        Long userId = requestBody.get("userId");
        try {
            Room room = roomRepository.findById(roomId).orElseThrow(() -> new RoomNotFoundException(roomId));

            if (room.getRoomLocked()) {
                return ResponseEntity.badRequest().body("Room is locked, cannot join.");
            }

            boolean success = roomService.addPlayerToRoom(userId, roomId);
            if (success) {
                messagingTemplate.convertAndSend("/room/" + roomId + "/players",
                        roomService.getUsersInRoomWithScores(roomId));

                int playerCount = room.getUsers().size();

                if (!room.getRoomLocked()) {
                    if (playerCount >= 2 && playerCount < room.getCapacity()) {
                        if (!roomTimers.containsKey(roomId)) {
                            startGameTimer(roomId, 30);
                        }
                    } else if (playerCount == room.getCapacity()) {
                        if (!roomTimers.containsKey(roomId)) {
                            startGameTimer(roomId, 10);
                        }
                    }
                }

                return ResponseEntity.ok("User successfully joined the room");
            } else return ResponseEntity.badRequest().body("Room is full or does not exist");

        } catch (UserNotFoundException e) {
            return ResponseEntity.status(404).body("User with ID " + userId + " not found.");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Room is full or does not exist.");
        }
    }

    @PostMapping("/room/remove/player")
    public ResponseEntity<String> removePlayerFromRoom(@RequestParam Long roomId, @RequestParam Long userId) {
        try {
            roomService.removePlayerFromRoom(userId, roomId);

            messagingTemplate.convertAndSend("/room/" + roomId + "/players",
                    roomService.getUsersInRoomWithScores(roomId));

            Room room = roomRepository.findById(roomId).orElseThrow(() -> new RoomNotFoundException(roomId));
            int playerCount = room.getUsers().size();

            if (playerCount < 2 && roomTimers.containsKey(roomId)) {
                ScheduledFuture<?> scheduledFuture = roomTimers.get(roomId);
                if (scheduledFuture != null) {
                    scheduledFuture.cancel(true);
                }
                roomTimers.remove(roomId);
                messagingTemplate.convertAndSend("/room/" + roomId + "/timer/cancel",
                        "Timer cancelled due to insufficient players");
            }

            if (playerCount == 0) {
                GameSession gameSession = room.getGameSession();
                if (gameSession != null && gameSession.getIsActive()) {
                    UserDTO winner = gameService.endGame(gameSession.getSessionId());
                    Map<String, String> winnerMessageMap = new HashMap<>();
                    winnerMessageMap.put("winner", "Game ended due to no players.");
                    messagingTemplate.convertAndSend("/room/" + roomId + "/end-game", winnerMessageMap);
                } else {
                    room.setRoomLocked(false);
                    roomRepository.save(room);
                }
            }

            return ResponseEntity.ok("User successfully left the room");
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(404).body("User with ID " + userId + " not found.");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Error leaving room.");
        }
    }

    private void startGameTimer(Long roomId, int seconds) {
        int delayBeforeStart = 2;
        ScheduledFuture<?> scheduledFuture = scheduledExecutorService.schedule(() -> {
            try {
                Room room = roomRepository.findById(roomId).orElseThrow(() -> new RoomNotFoundException(roomId));
                if (room.getUsers().size() >= 2 && !room.getRoomLocked()) {
                    Map<String, Object> payload = new HashMap<>();
                    payload.put("totalRounds", 5);
                    startGame(roomId, payload);
                }
            } catch (Exception e) {
            } finally {
                roomTimers.remove(roomId);
            }
        }, seconds + delayBeforeStart, TimeUnit.SECONDS);

        roomTimers.put(roomId, scheduledFuture);

        scheduledExecutorService.schedule(() -> broadcastTimer(roomId, seconds), delayBeforeStart, TimeUnit.SECONDS);
    }

    private void broadcastTimer(Long roomId, int totalSeconds) {
        Map<String, Integer> timerMessage = new HashMap<>();
        timerMessage.put("totalSeconds", totalSeconds);
        messagingTemplate.convertAndSend("/room/" + roomId + "/timer", timerMessage);
    }

    @GetMapping("/room/{roomId}")
    public ResponseEntity<RoomDTO> getRoomById(@PathVariable Long roomId) {
        try {
            RoomDTO roomDTO = roomService.getRoomDTOById(roomId);
            return ResponseEntity.ok(roomDTO);
        } catch (RoomNotFoundException e) {
            return ResponseEntity.status(404).body(null);
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(404).body(null);
        } catch (RuntimeException e) {
            return ResponseEntity.status(500).body(null);
        }
    }

    @GetMapping("/room/{roomId}/score")
    public ResponseEntity<List<UserScoreResponse>> getUsersInRoomWithScores(@PathVariable Long roomId) {
        try {
            List<UserScoreResponse> response = roomService.getUsersInRoomWithScores(roomId);
            return ResponseEntity.ok(response);
        } catch (RoomNotFoundException e) {
            return ResponseEntity.status(404).body(null);
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(null);
        }
    }

    @GetMapping("/with-user-counts")
    public List<RoomWithUserCountDTO> getAllRoomsWithUserCounts() {
        return roomService.getAllRoomsWithUserCounts();
    }

    @GetMapping("/rooms/available")
    public List<RoomWithUserCountDTO> getAvailableRooms() {
        return roomService.getAllRoomsWithUserCounts();
    }

    @GetMapping("/get-hint")
    public ResponseEntity<String> getHint(@RequestParam Integer roundId, @RequestParam Long userId) {
        try {
            String hint = gameService.provideHint(roundId, userId);
            return ResponseEntity.ok(hint);
        } catch (GameLogicException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

}

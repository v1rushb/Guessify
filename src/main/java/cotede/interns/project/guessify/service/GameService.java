package cotede.interns.project.guessify.service;

import cotede.interns.project.guessify.response.RoundResponse;
import cotede.interns.project.guessify.response.UserScoreResponse;
import cotede.interns.project.guessify.exception.GameLogicException;
import cotede.interns.project.guessify.exception.RoomNotFoundException;
import cotede.interns.project.guessify.model.Answer;
import cotede.interns.project.guessify.model.GameSession;
import cotede.interns.project.guessify.model.Room;
import cotede.interns.project.guessify.model.Round;
import cotede.interns.project.guessify.repository.GameSessionRepository;
import cotede.interns.project.guessify.repository.RoundRepository;
import cotede.interns.project.guessify.repository.RoomRepository;
import cotede.interns.project.guessify.model.User;
import cotede.interns.project.guessify.dto.UserDTO;
import cotede.interns.project.guessify.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Service
public class GameService {

    private final UserRepository userRepository;
    private final RoundService roundService;
    private final RoundRepository roundRepository;
    private final RoomRepository roomRepository;
    private final GameSessionRepository gameSessionRepository;
    private final RoomService roomService;
    private final ApplicationContext applicationContext;
    private final SimpMessagingTemplate messagingTemplate;
    private final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(10);

    @Autowired
    public GameService(UserRepository userRepository,
                       RoundService roundService,
                       RoundRepository roundRepository,
                       RoomRepository roomRepository,
                       GameSessionRepository gameSessionRepository,
                       RoomService roomService,
                       ApplicationContext applicationContext,
                       SimpMessagingTemplate messagingTemplate) {
        this.userRepository = userRepository;
        this.roundService = roundService;
        this.roundRepository = roundRepository;
        this.roomRepository = roomRepository;
        this.gameSessionRepository = gameSessionRepository;
        this.roomService = roomService;
        this.applicationContext = applicationContext;
        this.messagingTemplate = messagingTemplate;
    }

    @Transactional
    public void submitAnswer(Long userId, Integer roundId, Integer answerNumber) {
        Round currentRound = roundRepository.findById(roundId)
                .orElseThrow(() -> new GameLogicException("Invalid game state."));

        GameSession gameSession = currentRound.getGameSession();
        if (!gameSession.getIsActive()) {
            throw new GameLogicException("Game session has ended.");
        }

        synchronized (currentRound) {
            if (answerNumber < 1 || answerNumber > 6) {
                throw new GameLogicException("Invalid answer number: " + answerNumber + ". Must be between 1 and 6  ..");
            }


            boolean alreadyAnswered = currentRound.getAnswers().stream()
                    .anyMatch(a -> a.getUser().getId().equals(userId));
            if (alreadyAnswered) {
                throw new GameLogicException("User has already submitted an answer for this round.");
            }

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new GameLogicException("User not found"));

            int hintsUsed = user.getHintsUsed();

            Answer userAnswer = new Answer();
            userAnswer.setAnswerNumber(answerNumber);
            userAnswer.setSubmissionTime(LocalDateTime.now());
            userAnswer.setUser(user);
            userAnswer.setRound(currentRound);
            userAnswer.setHintsUsed(hintsUsed);

            roundService.addPlayerAnswer(currentRound, userAnswer);
            roundRepository.save(currentRound);
        }
    }


    @Transactional
    public UserDTO endGame(Long sessionId) {
        GameSession gameSession = gameSessionRepository.findById(sessionId)
                .orElseThrow(() -> new GameLogicException("Game session not found"));

        synchronized (gameSession) {
            if (!gameSession.getIsActive()) {
                return null;
            }

            Room room = gameSession.getRoom();
            Set<User> users = room.getUsers();
            User highestScoringUser = users.stream()
                    .max(Comparator.comparingInt(User::getScore))
                    .orElse(null);



            List<UserScoreResponse> updatedPlayers = roomService.getUsersInRoomWithScores(room.getRoomId());
            messagingTemplate.convertAndSend("/room/" + room.getRoomId() + "/players", updatedPlayers);

            users.forEach(user -> {
                user.setScore(0);
                user.setHintsUsed(0);
            });
            userRepository.saveAll(users);

            room.setRoomLocked(false);
            room.getUsers().clear();
            roomRepository.save(room);

            gameSession.setIsActive(false);
            gameSessionRepository.save(gameSession);
            gameSessionRepository.deleteBySessionId(sessionId);

            if (highestScoringUser != null) {
                return new UserDTO(highestScoringUser.getId(), highestScoringUser.getName(), highestScoringUser.getScore());
            } else {
                return null;
            }
        }
    }


    private void calculateScoresForRound(Round round) {
        Integer correctAnswerNumber = round.getCorrectAnswerNumber();

        List<Answer> correctAnswers = round.getAnswers().stream()
                .filter(answer -> answer.getAnswerNumber().equals(correctAnswerNumber))
                .sorted(Comparator.comparing(Answer::getSubmissionTime))
                .collect(Collectors.toList());

        int[] bonusPoints = {5, 4, 3};
        for (int i = 0; i < correctAnswers.size(); i++) {
            Answer answer = correctAnswers.get(i);
            int score = (i < bonusPoints.length) ? bonusPoints[i] : 1;

            int hintsUsed = answer.getHintsUsed();
            if (score > 1) {
                score -= hintsUsed;
                if (score < 1) {
                    score = 1;
                }
            }

            User user = answer.getUser();
            user.setScore(user.getScore() + score);
            userRepository.save(user);
        }
    }


    @Transactional
    public GameSession startGameSession(Long roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RoomNotFoundException(roomId));

        if (room.getUsers().size() < 2) {
            throw new GameLogicException("Not enough players to start the game.");
        }

        Optional<GameSession> existingSession = gameSessionRepository.findByRoom_RoomIdAndIsActiveTrue(roomId);
        if (existingSession.isPresent()) {
            gameSessionRepository.deleteBySessionId(existingSession.get().getSessionId());
        }

        GameSession gameSession = new GameSession();
        gameSession.setRoom(room);
        gameSession = gameSessionRepository.save(gameSession);

        room.setRoomLocked(true);
        roomRepository.save(room);
        return gameSession;
    }

    @Transactional
    public RoundResponse startNextRound(Long sessionId) {
        GameSession gameSession = gameSessionRepository.findById(sessionId)
                .orElseThrow(() -> new GameLogicException("Game session not found"));

        synchronized (gameSession) {
            if (!gameSession.getIsActive()) {
                throw new GameLogicException("Game session has ended.");
            }

            if (gameSession.getCurrentRoundNumber() >= gameSession.getTotalRounds()) {
                throw new GameLogicException("All rounds completed");
            }

            gameSession.setCurrentRoundNumber(gameSession.getCurrentRoundNumber() + 1);
            gameSessionRepository.save(gameSession);
        }

        Round round = roundService.startRound(gameSession);

        scheduleRoundEnd(round.getRoundId(), 60);
        return new RoundResponse(round.getRoundId(), round.getContent(), gameSession.getCurrentRoundNumber());
    }

    private void scheduleRoundEnd(Integer roundId, int seconds) {
        scheduledExecutorService.schedule(() -> {
            try {
                GameService gameServiceProxy = applicationContext.getBean(GameService.class);
                gameServiceProxy.endRound(roundId);
            } catch (Exception e) {
            }
        }, seconds, TimeUnit.SECONDS);
    }

    @Transactional
    public void endRound(Integer roundId) {
        synchronized (this) {
            Round round = roundRepository.findByIdWithAnswers(roundId)
                    .orElseThrow(() -> new GameLogicException("Round not found"));

            GameSession gameSession = round.getGameSession();
            Long sessionId = gameSession.getSessionId();
            Long roomId = gameSession.getRoom().getRoomId();

            calculateScoresForRound(round);

            Room room = gameSession.getRoom();
            Set<User> users = room.getUsers();
            users.forEach(user -> user.setHintsUsed(0));
            userRepository.saveAll(users);

            List<UserScoreResponse> updatedPlayers = roomService.getUsersInRoomWithScores(roomId);
            messagingTemplate.convertAndSend("/room/" + roomId + "/players", updatedPlayers);
            messagingTemplate.convertAndSend("/room/" + roomId + "/round-end", "Round ended");

            try {
                RoundResponse gameData = startNextRound(sessionId);
                messagingTemplate.convertAndSend("/room/" + roomId + "/question", gameData);
            } catch (GameLogicException e) {
                UserDTO winner = endGame(sessionId);
                Map<String, String> winnerMessageMap = new HashMap<>();
                if (winner != null) {
                    winnerMessageMap.put("winner", "The winner is '" + winner.getName() + "'");
                } else {
                    winnerMessageMap.put("winner", "No winner, the game ended with no players.");
                }
                messagingTemplate.convertAndSend("/room/" + roomId + "/end-game", winnerMessageMap);
            }
        }
    }


    public String provideHint(Integer roundId, Long userId) {
        Round round = roundRepository.findById(roundId)
                .orElseThrow(() -> new GameLogicException("Round not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GameLogicException("User not found"));

        int hintsUsed = user.getHintsUsed();

        List<String> hints = round.getHints();

        if (hintsUsed >= hints.size() || hintsUsed >= 2) {
            throw new GameLogicException("No more hints available");
        }

        String nextHint = hints.get(hintsUsed);

        user.setHintsUsed(hintsUsed + 1);
        userRepository.save(user);

        return nextHint;
    }
}


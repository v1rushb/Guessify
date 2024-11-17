package cotede.interns.project.guessify.service;

import cotede.interns.project.guessify.dto.ProfileRequestDTO;
import cotede.interns.project.guessify.dto.UserDTO;
import cotede.interns.project.guessify.dto.UserProfileUpdateDTO;
import cotede.interns.project.guessify.dto.UserRequestDTO;
import cotede.interns.project.guessify.model.Profile;
import cotede.interns.project.guessify.model.User;
import cotede.interns.project.guessify.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import cotede.interns.project.guessify.exception.UserNotFoundException;
import java.util.List;
import java.util.Optional;


@Service
public class UserDbService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserDbService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public UserDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        return new UserDTO(user.getId(), user.getName(), user.getScore());
    }

    public Optional<User> getUserDetailsById(Long id) {
        return userRepository.findById(id);
    }

    public UserDTO createUser(UserRequestDTO userRequestDTO) {
        if (userRepository.findByName(userRequestDTO.getName()).isPresent()) {
            throw new RuntimeException("Username is already taken ! ");
        }

        User user = new User();
        user.setName(userRequestDTO.getName());
        user.setPassword(passwordEncoder.encode(userRequestDTO.getPassword()));
        user.setRole("ROLE_USER");
        user.setScore(0);

        User savedUser = userRepository.save(user);
        return new UserDTO(savedUser.getId(), savedUser.getName(), savedUser.getScore());
    }



}

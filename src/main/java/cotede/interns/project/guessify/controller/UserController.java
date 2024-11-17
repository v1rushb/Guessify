package cotede.interns.project.guessify.controller;

import cotede.interns.project.guessify.dto.UserProfileUpdateDTO;
import cotede.interns.project.guessify.model.User;
import cotede.interns.project.guessify.dto.UserDTO;
import cotede.interns.project.guessify.service.UserDbService;
import cotede.interns.project.guessify.dto.UserRequestDTO;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/guessify/users")
@Validated
//
public class UserController {
    private final UserDbService userDbService;
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    public UserController(UserDbService userDbService) {
        this.userDbService = userDbService;
    }

    @GetMapping
    public List<User> getAllUsers() {
        logger.info("Received a request to fetch all users");
        return userDbService.getAllUsers();
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {
        logger.info("Received a request to fetch user with ID: {}", id);
        UserDTO userDTO = userDbService.getUserById(id);
        return ResponseEntity.ok(userDTO);
    }

    @GetMapping("/details/{id}")
    public ResponseEntity<User> getUserDetailsById(@PathVariable Long id) {
        logger.info("Received a request to fetch user with ID: {}", id);
        Optional<User> user = userDbService.getUserDetailsById(id);
        return user.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }


    @PostMapping
    public ResponseEntity<UserDTO> createUser(@Valid @RequestBody UserRequestDTO user) {
        logger.info("Received a request to create a new user!");
        UserDTO newUser = userDbService.createUser(user);
        return ResponseEntity.ok(newUser);
    }



}

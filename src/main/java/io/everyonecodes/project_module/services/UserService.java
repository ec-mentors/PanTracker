package io.everyonecodes.project_module.services;

import io.everyonecodes.project_module.dtos.requests.UserRegisterRequest;
import io.everyonecodes.project_module.dtos.responses.UserResponse;
import io.everyonecodes.project_module.exceptions.ResourceNotFoundException;
import io.everyonecodes.project_module.models.User;
import io.everyonecodes.project_module.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public UserResponse registerUser(UserRegisterRequest request) {
        // prevent duplicates
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username '" + request.getUsername() + "' is already taken.");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email '" + request.getEmail() + "' is already registered.");
        }

        // map Request to Database Entity
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .build();

        // save user to database
        User newUser = userRepository.save(user);

        // map user to Response DTO
        return mapToResponse(newUser);
    }


    @Transactional(readOnly = true)
    public UserResponse loginUser(String username) {
        // find user, or throw custom exception
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User with username '" + username + "' not found."));

        return mapToResponse(user);
    }


    private UserResponse mapToResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .build();
    }
}

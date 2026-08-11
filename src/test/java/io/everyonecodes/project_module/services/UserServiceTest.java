package io.everyonecodes.project_module.services;

import io.everyonecodes.project_module.dtos.requests.UserRegisterRequest;
import io.everyonecodes.project_module.dtos.responses.UserResponse;
import io.everyonecodes.project_module.exceptions.ResourceNotFoundException;
import io.everyonecodes.project_module.models.User;
import io.everyonecodes.project_module.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.ZonedDateTime;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    // mock repository
    @Mock
    private UserRepository userRepository;

    // inject mock into service
    @InjectMocks
    private UserService userService;

    @Test
    void registerUser_Success() {
        // inputs and mock behaviors
        UserRegisterRequest request = UserRegisterRequest.builder()
                .username("Jakob")
                .email("jakob@example.com")
                .build();

        User savedUser = User.builder()
                .id(1L)
                .username("Jakob")
                .email("jakob@example.com")
                .createdAt(ZonedDateTime.now())
                .build();

        // behavioral instructions
        when(userRepository.existsByUsername(request.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        UserResponse response = userService.registerUser(request);

        // check if results are correct
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getUsername()).isEqualTo("Jakob");
        assertThat(response.getEmail()).isEqualTo("jakob@example.com");

        // repository.save was executed only once
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void registerUser_ThrowsException_WhenUsernameExists() {
        UserRegisterRequest request = UserRegisterRequest.builder()
                .username("duplicateUser")
                .email("test@example.com")
                .build();

        when(userRepository.existsByUsername(request.getUsername())).thenReturn(true);

        // verify that exception is thrown
        assertThatThrownBy(() -> userService.registerUser(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already taken");

        // verify that save was not called
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void loginUser_Success() {
        String username = "Detlev";
        User user = User.builder()
                .id(2L)
                .username(username)
                .email("detlev@example.com")
                .createdAt(ZonedDateTime.now())
                .build();

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        UserResponse response = userService.loginUser(username);

        assertThat(response).isNotNull();
        assertThat(response.getUsername()).isEqualTo(username);
    }

    @Test
    void loginUser_ThrowsException_WhenUserNotFound() {
        String username = "unknownUser";
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.loginUser(username))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("not found");
    }
}

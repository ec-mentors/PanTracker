package io.everyonecodes.project_module.controllers;

import io.everyonecodes.project_module.dtos.requests.UserRegisterRequest;
import io.everyonecodes.project_module.dtos.responses.UserResponse;
import io.everyonecodes.project_module.exceptions.ResourceNotFoundException;
import io.everyonecodes.project_module.services.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import org.springframework.test.context.bean.override.mockito.MockitoBean;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc; // simulates HTTP requests

    @Autowired
    private ObjectMapper objectMapper; // converts objects to JSON strings

    @MockitoBean
    private UserService userService; // mocks the service layer

    @Test
    void registerUserReturns201WhenRequestIsValid() throws Exception {
        UserRegisterRequest request = new UserRegisterRequest("Laurenz", "laurenz@example.com");
        UserResponse response = new UserResponse(1L, "Laurenz", "laurenz@example.com");

        when(userService.registerUser(any(UserRegisterRequest.class))).thenReturn(response);

        // HTTP POST and verify
        mockMvc.perform(post("/api/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))) // converts DTO to JSON
                .andExpect(status().isCreated())                            // checks if HTTP 201 is answered
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.username").value("Laurenz"))
                .andExpect(jsonPath("$.email").value("laurenz@example.com"));
    }

    @Test
    void registerUserReturns400WhenEmailIsInvalid() throws Exception {
        UserRegisterRequest request = new UserRegisterRequest("Laurenz", "not-an-email");

        // HTTP Bad Request and verify
        mockMvc.perform(post("/api/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())                 // checks for HTTP 400 Bad Request
                .andExpect(jsonPath("$.email").exists()); // checks if validation error contains email field
    }

    @Test
    void registerUserReturns400WhenUsernameIsBlank() throws Exception {
        UserRegisterRequest request = UserRegisterRequest.builder()
                .username("")
                .email("bernhard@example.com")
                .build();

        mockMvc.perform(post("/api/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.username").value("Username must be between 3 and 50 characters"));
    }

    @Test
    void loginUserSuccessReturns200() throws Exception {
        String username = "Jakob";
        UserResponse response = UserResponse.builder()
                .id(2L)
                .username(username)
                .email("jakob@example.com")
                .build();

        when(userService.loginUser(username)).thenReturn(response);

        mockMvc.perform(post("/api/login")
                        .param("username", username))       // submit parameter
                .andExpect(status().isOk())                     // check if HTTP 200 OK
                .andExpect(jsonPath("$.id").value(2L))
                .andExpect(jsonPath("$.username").value(username))
                .andExpect(jsonPath("$.email").value("jakob@example.com"));
    }

    @Test
    void loginUserFailureUserNotFoundReturns404() throws Exception {
        String username = "unknown";

        when(userService.loginUser(username))
                .thenThrow(new ResourceNotFoundException("User with username 'unknown' not found."));

        mockMvc.perform(post("/api/login")
                        .param("username", username))
                .andExpect(status().isNotFound()) // check HTTP 404 Not Found
                .andExpect(jsonPath("$.error").value("User with username 'unknown' not found."));
    }
}

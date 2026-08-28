package io.everyonecodes.project_module.fullIntegrationTests;

import io.everyonecodes.project_module.dtos.requests.UserRegisterRequest;
import io.everyonecodes.project_module.models.User;
import io.everyonecodes.project_module.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test") // swaps active database to in-memory H2
@Transactional          // runs each test in a transaction and rolls it back at the end!
class UserFullIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Test
    void registerUserWritesToDatabase() throws Exception {
        UserRegisterRequest request = UserRegisterRequest.builder()
                .username("Bernhart")
                .email("bernhart@example.com")
                .build();

        mockMvc.perform(post("/api/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("Bernhart"))
                .andExpect(jsonPath("$.email").value("bernhart@example.com"));

        Optional<User> dbUserOpt = userRepository.findByUsername("Bernhart");

        assertThat(dbUserOpt).isPresent();

        User dbUser = dbUserOpt.get();
        assertThat(dbUser.getEmail()).isEqualTo("bernhart@example.com");
        assertThat(dbUser.getId()).isNotNull();
    }

    @Test
    void loginUser_FullFlow_Success() throws Exception {
        User seededUser = User.builder()
                .username("Jakob")
                .email("jakob@example.com")
                .build();
        userRepository.save(seededUser);

        String username = "Jakob";

        mockMvc.perform(post("/api/login")
                        .param("username", username))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(seededUser.getId()))
                .andExpect(jsonPath("$.username").value(username))
                .andExpect(jsonPath("$.email").value("jakob@example.com"));
    }

    @Test
    void loginUser_FullFlow_UserNotFound_Returns404() throws Exception {
        String username = "unknownUser";

        mockMvc.perform(post("/api/login")
                        .param("username", username))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("User with username 'unknownUser' not found."));
    }
}
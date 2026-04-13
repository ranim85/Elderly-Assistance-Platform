package tn.beecoders.elderly;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tn.beecoders.elderly.domain.Role;
import tn.beecoders.elderly.domain.User;
import tn.beecoders.elderly.repository.UserRepository;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ApiFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void cleanAndSeedAdmin() {
        userRepository.deleteAll();
        userRepository.save(User.builder()
                .firstName("Admin")
                .lastName("User")
                .email("admin@test.local")
                .password(passwordEncoder.encode("secret123"))
                .role(Role.ADMIN)
                .build());
    }

    @Test
    void authenticateThenDashboardStats() throws Exception {
        MvcResult auth = mockMvc.perform(post("/api/v1/auth/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"admin@test.local\",\"password\":\"secret123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andReturn();

        String token = objectMapper.readTree(auth.getResponse().getContentAsString()).get("token").asText();

        mockMvc.perform(get("/api/dashboard/stats")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalAssisted").exists())
                .andExpect(jsonPath("$.elderlyStable").exists());
    }

    @Test
    void refreshTokenReturnsNewPair() throws Exception {
        MvcResult auth = mockMvc.perform(post("/api/v1/auth/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"admin@test.local\",\"password\":\"secret123\"}"))
                .andExpect(status().isOk())
                .andReturn();

        String refresh = objectMapper.readTree(auth.getResponse().getContentAsString()).get("refreshToken").asText();

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + refresh + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.refreshToken").exists());
    }

    @Test
    void cannotDeleteLastAdmin() throws Exception {
        MvcResult auth = mockMvc.perform(post("/api/v1/auth/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"admin@test.local\",\"password\":\"secret123\"}"))
                .andExpect(status().isOk())
                .andReturn();
        String token = objectMapper.readTree(auth.getResponse().getContentAsString()).get("token").asText();

        Long id = userRepository.findByEmail("admin@test.local").orElseThrow().getId();

        mockMvc.perform(delete("/api/users/" + id)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest());
    }

    @Test
    void actuatorHealthPermittedWithoutAuth() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());
    }
}

package com.retail.customer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.retail.customer.dto.CustomerRegistrationRequest;
import com.retail.customer.dto.LoginRequest;
import com.retail.customer.dto.RefreshTokenRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(OrderAnnotation.class)
class CustomerServiceE2ETest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private KafkaTemplate<String, Object> kafkaTemplate;

    private static String capturedAccessToken;
    private static String capturedRefreshToken;
    private static Long registeredCustomerId;

    @Test
    @Order(1)
    @DisplayName("E2E Step 1: Customer Registration (UC-1) -> 201 Created")
    void step1_registerCustomer() throws Exception {
        CustomerRegistrationRequest request = CustomerRegistrationRequest.builder()
                .email("e2e.test.user@retail.com")
                .password("Password@123")
                .firstName("Eleanor")
                .lastName("Vance")
                .phone("+1-555-987-6543")
                .shippingAddress("100 Hill House Road, Boston, MA 02108")
                .build();

        MvcResult result = mockMvc.perform(post("/api/v1/customers/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("e2e.test.user@retail.com"))
                .andExpect(jsonPath("$.data.firstName").value("Eleanor"))
                .andReturn();

        JsonNode responseJson = objectMapper.readTree(result.getResponse().getContentAsString());
        registeredCustomerId = responseJson.get("data").get("id").asLong();
        assertThat(registeredCustomerId).isPositive();
    }

    @Test
    @Order(2)
    @DisplayName("E2E Step 2: Prevent Duplicate Registration -> 409 Conflict")
    void step2_preventDuplicateRegistration() throws Exception {
        CustomerRegistrationRequest duplicateRequest = CustomerRegistrationRequest.builder()
                .email("e2e.test.user@retail.com")
                .password("DifferentPassword@123")
                .firstName("Eleanor")
                .lastName("Vance")
                .build();

        mockMvc.perform(post("/api/v1/customers/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicateRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    @Order(3)
    @DisplayName("E2E Step 3: Customer Login & 15m JWT Issuance (NFR 5.3) -> 200 OK")
    void step3_loginAndGenerateTokens() throws Exception {
        LoginRequest loginRequest = LoginRequest.builder()
                .email("e2e.test.user@retail.com")
                .password("Password@123")
                .build();

        MvcResult result = mockMvc.perform(post("/api/v1/customers/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.data.refreshToken").isNotEmpty())
                .andExpect(jsonPath("$.data.expiresIn").value(900))
                .andReturn();

        JsonNode data = objectMapper.readTree(result.getResponse().getContentAsString()).get("data");
        capturedAccessToken = data.get("accessToken").asText();
        capturedRefreshToken = data.get("refreshToken").asText();

        assertThat(capturedAccessToken).isNotBlank();
        assertThat(capturedRefreshToken).isNotBlank();
    }

    @Test
    @Order(4)
    @DisplayName("E2E Step 4: Login with Invalid Password -> 400 Bad Request")
    void step4_loginInvalidPassword() throws Exception {
        LoginRequest badRequest = LoginRequest.builder()
                .email("e2e.test.user@retail.com")
                .password("WrongPassword999!")
                .build();

        mockMvc.perform(post("/api/v1/customers/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(badRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @Order(5)
    @DisplayName("E2E Step 5: Query Profile by ID -> 200 OK")
    void step5_queryProfileById() throws Exception {
        mockMvc.perform(get("/api/v1/customers/" + registeredCustomerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("e2e.test.user@retail.com"))
                .andExpect(jsonPath("$.data.firstName").value("Eleanor"));
    }

    @Test
    @Order(6)
    @DisplayName("E2E Step 6: Refresh 15m JWT Token (NFR 5.3) -> 200 OK")
    void step6_refreshToken() throws Exception {
        RefreshTokenRequest refreshReq = RefreshTokenRequest.builder()
                .refreshToken(capturedRefreshToken)
                .build();

        MvcResult result = mockMvc.perform(post("/api/v1/customers/refresh-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.data.expiresIn").value(900))
                .andReturn();

        JsonNode data = objectMapper.readTree(result.getResponse().getContentAsString()).get("data");
        String rotatedAccessToken = data.get("accessToken").asText();
        assertThat(rotatedAccessToken).isNotBlank();
    }
}

package hr.algebra.project_manager.security;
import hr.algebra.project_manager.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@Transactional
public class ApiSecurityTest {
    @Autowired
    MockMvc mockMvc;

    private static final String PROJECT_JSON = """
        {"name":"CSRF check","startDate":"2026-11-10","dueDate":"2026-12-10","status":"TODO"}
        """;

    @Test
    void sessionFromFormLoginDoesNotAuthenticateApi() throws Exception {
        MvcResult login = mockMvc.perform(formLogin("/login").user("pperic").password("Demo1234!"))
                .andExpect(authenticated())
                .andReturn();
        MockHttpSession session = (MockHttpSession) login.getRequest().getSession(false);

        if (session != null) {
            mockMvc.perform(post("/api/projects").session(session)
                            .contentType(MediaType.APPLICATION_JSON).content(PROJECT_JSON))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Test
    void unauthenticatedApiCallGetsPlain401WithoutBasicChallenge() throws Exception {
        mockMvc.perform(get("/api/projects"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().doesNotExist("WWW-Authenticate"));
    }

    @Test
    void basicAuthWorksWithoutCsrfToken() throws Exception {
        mockMvc.perform(post("/api/projects").with(httpBasic("pperic", "Demo1234!"))
                        .contentType(MediaType.APPLICATION_JSON).content(PROJECT_JSON))
                .andExpect(status().isCreated());
    }

    @Test
    void screenPostWithoutCsrfTokenIsStillRejected() throws Exception {
        mockMvc.perform(post("/projects").with(user("pperic").roles("PROJECT_LEAD")))
                .andExpect(status().isForbidden());
    }
}

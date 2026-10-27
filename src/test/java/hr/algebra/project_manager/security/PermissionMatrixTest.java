package hr.algebra.project_manager.security;

import hr.algebra.project_manager.TestcontainersConfiguration;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@Transactional
@Sql("/security/permission-test-data.sql")
class PermissionMatrixTest {

    @Autowired
    MockMvc mvc;
    @Autowired
    UserDetailsService userDetailsService;

    @ParameterizedTest(name = "{0} {1} {2} → {4}")
    @CsvFileSource(resources = "/security/permission-matrix.csv", numLinesToSkip = 1)
    void permissionMatrix(String username, HttpMethod method, String url,
                          String body, int expectedStatus) throws Exception {

        var request = MockMvcRequestBuilders.request(method, url)
                .with(user(userDetailsService.loadUserByUsername(username)))
                .with(csrf());

        if (body != null) {
            request.contentType(MediaType.APPLICATION_FORM_URLENCODED).content(body);
        }

        mvc.perform(request).andExpect(status().is(expectedStatus));
    }
}

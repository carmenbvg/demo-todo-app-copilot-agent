package com.appsdeveloperai.demo_todoapp.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.appsdeveloperai.demo_todoapp.repository.UserAccountRepository;

@SpringBootTest(properties = {
		"spring.datasource.url=jdbc:h2:mem:logintests;DB_CLOSE_DELAY=-1",
		"spring.jpa.hibernate.ddl-auto=create-drop"
})
@AutoConfigureMockMvc
class LoginTests {

	@Autowired
	private MockMvc mvc;

	@Autowired
	private UserAccountRepository users;

	@BeforeEach
	void clearUsers() {
		users.deleteAll();
	}

	@Test
	void loginAndRegistrationPagesArePublicAndIncludeCsrfTokens() throws Exception {
		mvc.perform(get("/login"))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("name=\"email\"")))
				.andExpect(content().string(containsString("name=\"password\"")))
				.andExpect(content().string(containsString("name=\"_csrf\"")));
		mvc.perform(get("/register"))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("name=\"_csrf\"")))
				.andExpect(content().string(containsString("href=\"/login\"")));
	}

	@Test
	void registeredUserCanSignInAndOut() throws Exception {
		registerUser();

		MvcResult login = mvc.perform(post("/login").with(csrf())
				.param("email", "  ADA@Example.com  ")
				.param("password", "correct horse battery staple"))
				.andExpect(redirectedUrl("/"))
				.andExpect(authenticated().withUsername("ada@example.com"))
				.andReturn();
		MockHttpSession session = (MockHttpSession) login.getRequest().getSession(false);
		assertThat(session).isNotNull();

		mvc.perform(get("/").session(session))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("You are signed in.")))
				.andExpect(content().string(containsString("name=\"_csrf\"")));
		mvc.perform(post("/logout").session(session).with(csrf()))
				.andExpect(redirectedUrl("/login?logout"))
				.andExpect(unauthenticated());
		assertThat(session.isInvalid()).isTrue();
		mvc.perform(get("/"))
				.andExpect(status().is3xxRedirection())
				.andExpect(header().string("Location", endsWith("/login")));
	}

	@Test
	void protectedRequestIsRestoredAfterLogin() throws Exception {
		registerUser();

		mvc.perform(get("/future-page"))
				.andExpect(status().is3xxRedirection())
				.andExpect(header().string("Location", endsWith("/login")));
		MvcResult request = mvc.perform(get("/").queryParam("from", "bookmark"))
				.andExpect(status().is3xxRedirection())
				.andExpect(header().string("Location", endsWith("/login")))
				.andReturn();
		MockHttpSession session = (MockHttpSession) request.getRequest().getSession(false);
		assertThat(session).isNotNull();

		mvc.perform(post("/login").session(session).with(csrf())
				.param("email", "ada@example.com")
				.param("password", "correct horse battery staple"))
				.andExpect(authenticated())
				.andExpect(header().string("Location", containsString("from=bookmark")));
	}

	@Test
	void invalidAndMissingCredentialsReceiveTheSameFailure() throws Exception {
		registerUser();

		for (String[] credentials : new String[][] {
				{ "ada@example.com", "wrong password" },
				{ "unknown@example.com", "correct horse battery staple" },
				{ "ada@example.com", "" },
				{ "", "correct horse battery staple" },
				{ "", "" }
		}) {
			mvc.perform(post("/login").with(csrf())
					.param("email", credentials[0])
					.param("password", credentials[1]))
					.andExpect(redirectedUrl("/login?error"))
					.andExpect(unauthenticated());
		}
		mvc.perform(get("/login").param("error", ""))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("Invalid email address or password.")))
				.andExpect(content().string(not(containsString("correct horse battery staple"))));
	}

	@Test
	void stateChangingRequestsRequireCsrf() throws Exception {
		mvc.perform(post("/register")).andExpect(status().isForbidden());
		mvc.perform(post("/login")).andExpect(status().isForbidden());
		mvc.perform(post("/logout")).andExpect(status().isForbidden());
	}

	private void registerUser() throws Exception {
		mvc.perform(post("/register").with(csrf())
				.param("firstName", "Ada")
				.param("lastName", "Lovelace")
				.param("email", "Ada@Example.com")
				.param("password", "correct horse battery staple")
				.param("passwordConfirmation", "correct horse battery staple"))
				.andExpect(redirectedUrl("/register?success"))
				.andExpect(unauthenticated());
	}
}

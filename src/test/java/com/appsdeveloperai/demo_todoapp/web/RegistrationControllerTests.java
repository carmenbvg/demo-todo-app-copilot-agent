package com.appsdeveloperai.demo_todoapp.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import com.appsdeveloperai.demo_todoapp.domain.UserAccount;
import com.appsdeveloperai.demo_todoapp.repository.UserAccountRepository;

@SpringBootTest(properties = {
		"spring.datasource.url=jdbc:h2:mem:registrationtests;DB_CLOSE_DELAY=-1",
		"spring.jpa.hibernate.ddl-auto=create-drop"
})
@AutoConfigureMockMvc
class RegistrationControllerTests {

	@Autowired
	private MockMvc mvc;

	@Autowired
	private UserAccountRepository users;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private JdbcTemplate jdbc;

	@BeforeEach
	void clearUsers() {
		users.deleteAll();
	}

	@Test
	void registrationPageShowsAllFields() throws Exception {
		mvc.perform(get("/register"))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("name=\"firstName\"")))
				.andExpect(content().string(containsString("name=\"lastName\"")))
				.andExpect(content().string(containsString("name=\"email\"")))
				.andExpect(content().string(containsString("name=\"password\"")))
				.andExpect(content().string(containsString("name=\"passwordConfirmation\"")));
	}

	@Test
	void validRegistrationPersistsEncodedPassword() throws Exception {
		mvc.perform(validRegistration())
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/register?success"));

		assertThat(users.count()).isEqualTo(1);
		UserAccount user = users.findByEmail("ada@example.com").orElseThrow();
		assertThat(user.getPasswordHash()).isNotEqualTo("correct horse battery staple");
		assertThat(passwordEncoder.matches("correct horse battery staple", user.getPasswordHash())).isTrue();
		assertThat(jdbc.queryForObject("select first_name from user_accounts", String.class)).isEqualTo("Ada");
		assertThat(jdbc.queryForObject("select last_name from user_accounts", String.class)).isEqualTo("Lovelace");
	}

	@Test
	void mismatchedPasswordsAreRejectedWithoutEchoingSecrets() throws Exception {
		mvc.perform(validRegistration("Ada@Example.com", "different password"))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("Passwords do not match")))
				.andExpect(content().string(not(containsString("correct horse battery staple"))))
				.andExpect(content().string(not(containsString("different password"))));
		assertThat(users.count()).isZero();
	}

	@Test
	void missingFieldsAreRejected() throws Exception {
		mvc.perform(post("/register"))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("First name is required")))
				.andExpect(content().string(containsString("Last name is required")))
				.andExpect(content().string(containsString("Email address is required")))
				.andExpect(content().string(containsString("Password is required")))
				.andExpect(content().string(containsString("Password confirmation is required")));
		assertThat(users.count()).isZero();
	}

	@Test
	void invalidFieldsAreRejected() throws Exception {
		mvc.perform(post("/register")
				.param("firstName", " ")
				.param("lastName", "x".repeat(101))
				.param("email", "not-an-email")
				.param("password", "short")
				.param("passwordConfirmation", "short"))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("First name is required")))
				.andExpect(content().string(containsString("Last name must be at most 100 characters")))
				.andExpect(content().string(containsString("Enter a valid email address")))
				.andExpect(content().string(containsString("Password must be between 8 and 256 characters")));
		assertThat(users.count()).isZero();
	}

	@Test
	void duplicateEmailIsRejectedCaseInsensitively() throws Exception {
		mvc.perform(validRegistration()).andExpect(status().is3xxRedirection());
		mvc.perform(validRegistration("ada@example.com", "correct horse battery staple"))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("An account with this email address already exists")));
		assertThat(users.count()).isEqualTo(1);
	}

	@Test
	void databaseEnforcesUniqueEmail() {
		users.saveAndFlush(new UserAccount("Ada", "Lovelace", "ada@example.com", "hash"));
		assertThatThrownBy(() -> users.saveAndFlush(new UserAccount("Other", "User", "ada@example.com", "hash")))
				.isInstanceOf(DataIntegrityViolationException.class);
		assertThat(users.count()).isEqualTo(1);
	}

	private MockHttpServletRequestBuilder validRegistration() {
		return validRegistration("Ada@Example.com", "correct horse battery staple");
	}

	private MockHttpServletRequestBuilder validRegistration(String email, String confirmation) {
		return post("/register")
				.param("firstName", "Ada")
				.param("lastName", "Lovelace")
				.param("email", email)
				.param("password", "correct horse battery staple")
				.param("passwordConfirmation", confirmation);
	}
}

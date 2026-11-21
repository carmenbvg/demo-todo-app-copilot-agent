package com.appsdeveloperai.demo_todoapp.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.endsWith;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.appsdeveloperai.demo_todoapp.domain.UserAccount;
import com.appsdeveloperai.demo_todoapp.repository.TodoRepository;
import com.appsdeveloperai.demo_todoapp.repository.UserAccountRepository;

@SpringBootTest(properties = {
		"spring.datasource.url=jdbc:h2:mem:todotests;DB_CLOSE_DELAY=-1",
		"spring.jpa.hibernate.ddl-auto=create-drop"
})
@AutoConfigureMockMvc
class TodoControllerTests {

	@Autowired
	private MockMvc mvc;

	@Autowired
	private TodoRepository todos;

	@Autowired
	private UserAccountRepository users;

	@BeforeEach
	void setUp() {
		todos.deleteAll();
		users.deleteAll();
		users.save(new UserAccount("Ada", "Lovelace", "ada@example.com", "hash"));
		users.save(new UserAccount("Grace", "Hopper", "grace@example.com", "hash"));
	}

	@Test
	void ownerCanCreateReadUpdateAndDeleteTodo() throws Exception {
		String location = mvc.perform(post("/todos").with(user("ada@example.com")).with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"title\":\"Write tests\",\"dueDate\":\"2026-12-01\"}"))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").isNumber())
				.andExpect(jsonPath("$.title").value("Write tests"))
				.andExpect(jsonPath("$.completed").value(false))
				.andExpect(jsonPath("$.dueDate").value("2026-12-01"))
				.andExpect(jsonPath("$.user").doesNotExist())
				.andReturn().getResponse().getHeader("Location");
		assertThat(location).startsWith("/todos/");

		mvc.perform(get("/todos").with(user("ada@example.com")))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(1))
				.andExpect(jsonPath("$[0].title").value("Write tests"));
		mvc.perform(get(location).with(user("ada@example.com")))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.title").value("Write tests"));
		mvc.perform(put(location).with(user("ada@example.com")).with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"title\":\"Done\",\"completed\":true}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.title").value("Done"))
				.andExpect(jsonPath("$.completed").value(true))
				.andExpect(jsonPath("$.dueDate").doesNotExist());
		mvc.perform(get(location).with(user("ada@example.com")))
				.andExpect(jsonPath("$.title").value("Done"))
				.andExpect(jsonPath("$.completed").value(true));
		mvc.perform(delete(location).with(user("ada@example.com")).with(csrf()))
				.andExpect(status().isNoContent());
		mvc.perform(get(location).with(user("ada@example.com"))).andExpect(status().isNotFound());
		assertThat(todos.count()).isZero();
	}

	@Test
	void otherUsersCannotSeeOrChangeTodo() throws Exception {
		String location = mvc.perform(post("/todos").with(user("ada@example.com")).with(csrf())
				.contentType(MediaType.APPLICATION_JSON).content("{\"title\":\"Private\"}"))
				.andExpect(status().isCreated())
				.andReturn().getResponse().getHeader("Location");
		mvc.perform(get("/todos").with(user("grace@example.com")))
				.andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(0));
		mvc.perform(get(location).with(user("grace@example.com"))).andExpect(status().isNotFound());
		mvc.perform(put(location).with(user("grace@example.com")).with(csrf())
				.contentType(MediaType.APPLICATION_JSON).content("{\"title\":\"Changed\"}"))
				.andExpect(status().isNotFound());
		mvc.perform(delete(location).with(user("grace@example.com")).with(csrf()))
				.andExpect(status().isNotFound());
		mvc.perform(get(location).with(user("ada@example.com")))
				.andExpect(jsonPath("$.title").value("Private"));
	}

	@Test
	void invalidRequestsAreRejected() throws Exception {
		for (String body : new String[] {
				"{}", "{\"title\":\"  \"}", "{\"title\":\"" + "x".repeat(256) + "\"}",
				"{\"title\":\"Valid\",\"dueDate\":\"not-a-date\"}"
		}) {
			mvc.perform(post("/todos").with(user("ada@example.com")).with(csrf())
					.contentType(MediaType.APPLICATION_JSON).content(body))
					.andExpect(status().isBadRequest());
		}
		assertThat(todos.count()).isZero();
		mvc.perform(get("/todos/999").with(user("ada@example.com"))).andExpect(status().isNotFound());
	}

	@Test
	void authenticationAndCsrfAreRequired() throws Exception {
		mvc.perform(get("/todos"))
				.andExpect(status().is3xxRedirection())
				.andExpect(header().string("Location", endsWith("/login")));
		mvc.perform(post("/todos").with(user("ada@example.com"))
				.contentType(MediaType.APPLICATION_JSON).content("{\"title\":\"Test\"}"))
				.andExpect(status().isForbidden());
		assertThat(todos.count()).isZero();
	}
}

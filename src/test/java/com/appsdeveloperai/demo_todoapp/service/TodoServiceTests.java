package com.appsdeveloperai.demo_todoapp.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.appsdeveloperai.demo_todoapp.domain.Todo;
import com.appsdeveloperai.demo_todoapp.domain.UserAccount;
import com.appsdeveloperai.demo_todoapp.repository.TodoRepository;
import com.appsdeveloperai.demo_todoapp.repository.UserAccountRepository;

class TodoServiceTests {

	private final TodoRepository todos = mock(TodoRepository.class);
	private final UserAccountRepository users = mock(UserAccountRepository.class);
	private final TodoService service = new TodoService(todos, users);

	@Test
	void findAllReturnsTodosForUser() {
		List<Todo> expected = List.of(new Todo("Write tests", false, null,
				new UserAccount("Ada", "Lovelace", "ada@example.com", "hash")));
		when(todos.findAllByUserEmail("ada@example.com")).thenReturn(expected);

		assertThat(service.findAll("ada@example.com")).isSameAs(expected);
		verify(todos).findAllByUserEmail("ada@example.com");
	}

	@Test
	void findByIdReturnsTodoForUser() {
		Todo expected = new Todo("Write tests", false, null,
				new UserAccount("Ada", "Lovelace", "ada@example.com", "hash"));
		when(todos.findByIdAndUserEmail(1L, "ada@example.com")).thenReturn(Optional.of(expected));

		assertThat(service.findById(1L, "ada@example.com")).isSameAs(expected);
	}

	@Test
	void findByIdThrowsNotFoundWhenTodoDoesNotBelongToUser() {
		when(todos.findByIdAndUserEmail(1L, "grace@example.com")).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.findById(1L, "grace@example.com"))
				.isInstanceOf(ResponseStatusException.class)
				.extracting(exception -> ((ResponseStatusException) exception).getStatusCode())
				.isEqualTo(HttpStatus.NOT_FOUND);
	}

	@Test
	void createSavesTodoForUser() {
		UserAccount user = new UserAccount("Ada", "Lovelace", "ada@example.com", "hash");
		LocalDate dueDate = LocalDate.of(2026, 12, 1);
		when(users.findByEmail("ada@example.com")).thenReturn(Optional.of(user));
		when(todos.save(any(Todo.class))).thenAnswer(invocation -> invocation.getArgument(0));

		Todo created = service.create("ada@example.com", "Write tests", true, dueDate);

		assertThat(created.getTitle()).isEqualTo("Write tests");
		assertThat(created.isCompleted()).isTrue();
		assertThat(created.getDueDate()).isEqualTo(dueDate);
		verify(todos).save(any(Todo.class));
	}

	@Test
	void createThrowsNotFoundWhenUserDoesNotExist() {
		when(users.findByEmail("missing@example.com")).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.create("missing@example.com", "Write tests", false, null))
				.isInstanceOf(ResponseStatusException.class)
				.extracting(exception -> ((ResponseStatusException) exception).getStatusCode())
				.isEqualTo(HttpStatus.NOT_FOUND);
	}

	@Test
	void updateChangesTodoFields() {
		Todo todo = new Todo("Old title", false, null,
				new UserAccount("Ada", "Lovelace", "ada@example.com", "hash"));
		LocalDate dueDate = LocalDate.of(2026, 12, 1);
		when(todos.findByIdAndUserEmail(1L, "ada@example.com")).thenReturn(Optional.of(todo));

		Todo updated = service.update(1L, "ada@example.com", "New title", true, dueDate);

		assertThat(updated).isSameAs(todo);
		assertThat(updated.getTitle()).isEqualTo("New title");
		assertThat(updated.isCompleted()).isTrue();
		assertThat(updated.getDueDate()).isEqualTo(dueDate);
	}

	@Test
	void deleteRemovesTodoForUser() {
		Todo todo = new Todo("Write tests", false, null,
				new UserAccount("Ada", "Lovelace", "ada@example.com", "hash"));
		when(todos.findByIdAndUserEmail(1L, "ada@example.com")).thenReturn(Optional.of(todo));

		service.delete(1L, "ada@example.com");

		verify(todos).delete(todo);
	}
}

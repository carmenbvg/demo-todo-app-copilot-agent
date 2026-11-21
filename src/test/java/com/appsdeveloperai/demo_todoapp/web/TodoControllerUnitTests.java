package com.appsdeveloperai.demo_todoapp.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.appsdeveloperai.demo_todoapp.domain.Todo;
import com.appsdeveloperai.demo_todoapp.service.TodoService;

class TodoControllerUnitTests {

	private static final String EMAIL = "ada@example.com";
	private final TodoService todos = mock(TodoService.class);
	private final TodoController controller = new TodoController(todos);
	private final Principal principal = () -> EMAIL;

	@Test
	void listReturnsTodosForCurrentUser() {
		Todo todo = todo(1L, "Write tests", false, null);
		when(todos.findAll(EMAIL)).thenReturn(List.of(todo));

		assertThat(controller.list(principal))
				.containsExactly(new TodoResponse(1L, "Write tests", false, null));
		verify(todos).findAll(EMAIL);
	}

	@Test
	void getReturnsTodoForCurrentUser() {
		Todo todo = todo(1L, "Write tests", false, null);
		when(todos.findById(1L, EMAIL)).thenReturn(todo);

		assertThat(controller.get(1L, principal))
				.isEqualTo(new TodoResponse(1L, "Write tests", false, null));
	}

	@Test
	void createReturnsCreatedLocationAndTodo() {
		LocalDate dueDate = LocalDate.of(2026, 12, 1);
		Todo todo = todo(7L, "Write tests", true, dueDate);
		when(todos.create(EMAIL, "Write tests", true, dueDate))
				.thenReturn(todo);

		ResponseEntity<TodoResponse> response = controller.create(
				new TodoRequest("Write tests", true, dueDate), principal);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
		assertThat(response.getHeaders().getLocation()).hasToString("/todos/7");
		assertThat(response.getBody()).isEqualTo(new TodoResponse(7L, "Write tests", true, dueDate));
	}

	@Test
	void updateReturnsUpdatedTodoForCurrentUser() {
		LocalDate dueDate = LocalDate.of(2026, 12, 1);
		Todo todo = todo(1L, "Write tests", true, dueDate);
		when(todos.update(1L, EMAIL, "Write tests", true, dueDate))
				.thenReturn(todo);

		assertThat(controller.update(1L, new TodoRequest("Write tests", true, dueDate), principal))
				.isEqualTo(new TodoResponse(1L, "Write tests", true, dueDate));
	}

	@Test
	void deleteReturnsNoContentForCurrentUser() {
		assertThat(controller.delete(1L, principal).getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

		verify(todos).delete(1L, EMAIL);
	}

	private Todo todo(Long id, String title, boolean completed, LocalDate dueDate) {
		Todo todo = mock(Todo.class);
		when(todo.getId()).thenReturn(id);
		when(todo.getTitle()).thenReturn(title);
		when(todo.isCompleted()).thenReturn(completed);
		when(todo.getDueDate()).thenReturn(dueDate);
		return todo;
	}
}

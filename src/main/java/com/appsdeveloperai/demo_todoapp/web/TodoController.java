package com.appsdeveloperai.demo_todoapp.web;

import java.net.URI;
import java.security.Principal;
import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.appsdeveloperai.demo_todoapp.service.TodoService;

@RestController
@RequestMapping("/todos")
public class TodoController {

	private final TodoService todos;

	public TodoController(TodoService todos) {
		this.todos = todos;
	}

	@GetMapping
	public List<TodoResponse> list(Principal principal) {
		return todos.findAll(principal.getName()).stream().map(TodoResponse::from).toList();
	}

	@GetMapping("/{id}")
	public TodoResponse get(@PathVariable Long id, Principal principal) {
		return TodoResponse.from(todos.findById(id, principal.getName()));
	}

	@PostMapping
	public ResponseEntity<TodoResponse> create(@Valid @RequestBody TodoRequest request, Principal principal) {
		TodoResponse todo = TodoResponse.from(todos.create(principal.getName(), request.title(),
				Boolean.TRUE.equals(request.completed()), request.dueDate()));
		return ResponseEntity.created(URI.create("/todos/" + todo.id())).body(todo);
	}

	@PutMapping("/{id}")
	public TodoResponse update(@PathVariable Long id, @Valid @RequestBody TodoRequest request, Principal principal) {
		return TodoResponse.from(todos.update(id, principal.getName(), request.title(),
				Boolean.TRUE.equals(request.completed()), request.dueDate()));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@PathVariable Long id, Principal principal) {
		todos.delete(id, principal.getName());
		return ResponseEntity.noContent().build();
	}
}

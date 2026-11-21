package com.appsdeveloperai.demo_todoapp.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.appsdeveloperai.demo_todoapp.domain.Todo;
import com.appsdeveloperai.demo_todoapp.domain.UserAccount;
import com.appsdeveloperai.demo_todoapp.repository.TodoRepository;
import com.appsdeveloperai.demo_todoapp.repository.UserAccountRepository;

@Service
@Transactional
public class TodoService {

	private final TodoRepository todos;
	private final UserAccountRepository users;

	public TodoService(TodoRepository todos, UserAccountRepository users) {
		this.todos = todos;
		this.users = users;
	}

	@Transactional(readOnly = true)
	public List<Todo> findAll(String email) {
		return todos.findAllByUserEmail(email);
	}

	@Transactional(readOnly = true)
	public Todo findById(Long id, String email) {
		return todos.findByIdAndUserEmail(id, email)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
	}

	public Todo create(String email, String title, boolean completed, LocalDate dueDate) {
		UserAccount user = users.findByEmail(email)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
		return todos.save(new Todo(title, completed, dueDate, user));
	}

	public Todo update(Long id, String email, String title, boolean completed, LocalDate dueDate) {
		Todo todo = findById(id, email);
		todo.update(title, completed, dueDate);
		return todo;
	}

	public void delete(Long id, String email) {
		todos.delete(findById(id, email));
	}
}

package com.appsdeveloperai.demo_todoapp.web;

import java.time.LocalDate;

import com.appsdeveloperai.demo_todoapp.domain.Todo;

public record TodoResponse(Long id, String title, boolean completed, LocalDate dueDate) {

	public static TodoResponse from(Todo todo) {
		return new TodoResponse(todo.getId(), todo.getTitle(), todo.isCompleted(), todo.getDueDate());
	}
}

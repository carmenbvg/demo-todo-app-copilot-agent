package com.appsdeveloperai.demo_todoapp.domain;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "todos")
public class Todo {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String title;

	@Column(nullable = false)
	private boolean completed;

	private LocalDate dueDate;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false)
	private UserAccount user;

	protected Todo() {
	}

	public Todo(String title, boolean completed, LocalDate dueDate, UserAccount user) {
		this.title = title;
		this.completed = completed;
		this.dueDate = dueDate;
		this.user = user;
	}

	public Long getId() {
		return id;
	}

	public String getTitle() {
		return title;
	}

	public boolean isCompleted() {
		return completed;
	}

	public LocalDate getDueDate() {
		return dueDate;
	}

	public void update(String title, boolean completed, LocalDate dueDate) {
		this.title = title;
		this.completed = completed;
		this.dueDate = dueDate;
	}
}

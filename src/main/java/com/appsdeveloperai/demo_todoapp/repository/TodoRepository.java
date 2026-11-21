package com.appsdeveloperai.demo_todoapp.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.appsdeveloperai.demo_todoapp.domain.Todo;

public interface TodoRepository extends JpaRepository<Todo, Long> {

	List<Todo> findAllByUserEmail(String email);

	Optional<Todo> findByIdAndUserEmail(Long id, String email);
}

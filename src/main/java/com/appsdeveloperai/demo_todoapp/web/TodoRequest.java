package com.appsdeveloperai.demo_todoapp.web;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TodoRequest(@NotBlank @Size(max = 255) String title, Boolean completed, LocalDate dueDate) {
}

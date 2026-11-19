package com.appsdeveloperai.demo_todoapp.web;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegistrationForm(
		@NotBlank(message = "First name is required") @Size(max = 100, message = "First name must be at most 100 characters") String firstName,
		@NotBlank(message = "Last name is required") @Size(max = 100, message = "Last name must be at most 100 characters") String lastName,
		@NotBlank(message = "Email address is required") @Email(message = "Enter a valid email address") @Size(max = 254, message = "Email address must be at most 254 characters") String email,
		@NotBlank(message = "Password is required") @Size(min = 8, max = 256, message = "Password must be between 8 and 256 characters") String password,
		@NotBlank(message = "Password confirmation is required") String passwordConfirmation) {
}

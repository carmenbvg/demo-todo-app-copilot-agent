package com.appsdeveloperai.demo_todoapp.web;

import java.util.Objects;

import jakarta.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.appsdeveloperai.demo_todoapp.service.RegistrationService;

@Controller
public class RegistrationController {

	private final RegistrationService registrationService;

	public RegistrationController(RegistrationService registrationService) {
		this.registrationService = registrationService;
	}

	@GetMapping("/register")
	public String registrationPage(@ModelAttribute("registrationForm") RegistrationForm form) {
		return "register";
	}

	@PostMapping("/register")
	public String register(@Valid @ModelAttribute("registrationForm") RegistrationForm form, BindingResult errors) {
		if (!Objects.equals(form.password(), form.passwordConfirmation())) {
			errors.rejectValue("passwordConfirmation", "passwordMismatch", "Passwords do not match");
		}
		if (errors.hasErrors()) {
			return "register";
		}
		if (!registrationService.register(form.firstName(), form.lastName(), form.email(), form.password())) {
			errors.rejectValue("email", "duplicateEmail", "An account with this email address already exists");
			return "register";
		}
		return "redirect:/register?success";
	}
}

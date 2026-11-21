package com.appsdeveloperai.demo_todoapp.service;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.appsdeveloperai.demo_todoapp.domain.UserAccount;
import com.appsdeveloperai.demo_todoapp.repository.UserAccountRepository;

@Service
public class RegistrationService {

	private final UserAccountRepository users;
	private final PasswordEncoder passwordEncoder;

	public RegistrationService(UserAccountRepository users, PasswordEncoder passwordEncoder) {
		this.users = users;
		this.passwordEncoder = passwordEncoder;
	}

	public boolean register(String firstName, String lastName, String email, String password) {
		String normalizedEmail = EmailNormalizer.normalize(email);
		if (users.existsByEmail(normalizedEmail)) {
			return false;
		}
		UserAccount user = new UserAccount(firstName.trim(), lastName.trim(), normalizedEmail,
				passwordEncoder.encode(password));
		try {
			users.saveAndFlush(user);
			return true;
		}
		catch (DataIntegrityViolationException ex) {
			if (users.existsByEmail(normalizedEmail)) {
				return false;
			}
			throw ex;
		}
	}
}

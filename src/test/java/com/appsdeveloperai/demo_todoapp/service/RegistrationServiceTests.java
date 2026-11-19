package com.appsdeveloperai.demo_todoapp.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.appsdeveloperai.demo_todoapp.repository.UserAccountRepository;

class RegistrationServiceTests {

	private final UserAccountRepository users = mock(UserAccountRepository.class);
	private final PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
	private final RegistrationService service = new RegistrationService(users, passwordEncoder);

	@Test
	void duplicateCreatedDuringRegistrationIsRejected() {
		when(users.existsByEmail("ada@example.com")).thenReturn(false, true);
		when(passwordEncoder.encode("password")).thenReturn("hash");
		when(users.saveAndFlush(any())).thenThrow(new DataIntegrityViolationException("duplicate"));

		assertThat(service.register("Ada", "Lovelace", "Ada@Example.com", "password")).isFalse();
	}

	@Test
	void unrelatedDatabaseErrorIsNotReportedAsDuplicateEmail() {
		when(users.existsByEmail("ada@example.com")).thenReturn(false);
		when(passwordEncoder.encode("password")).thenReturn("hash");
		when(users.saveAndFlush(any())).thenThrow(new DataIntegrityViolationException("other failure"));

		assertThatThrownBy(() -> service.register("Ada", "Lovelace", "Ada@Example.com", "password"))
				.isInstanceOf(DataIntegrityViolationException.class);
	}
}

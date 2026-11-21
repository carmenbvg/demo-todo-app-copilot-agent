package com.appsdeveloperai.demo_todoapp.service;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.appsdeveloperai.demo_todoapp.repository.UserAccountRepository;

@Service
public class AccountUserDetailsService implements UserDetailsService {

	private final UserAccountRepository users;

	public AccountUserDetailsService(UserAccountRepository users) {
		this.users = users;
	}

	@Override
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
		return users.findByEmail(EmailNormalizer.normalize(email))
				.map(account -> User.withUsername(account.getEmail())
						.password(account.getPasswordHash())
						.roles("USER")
						.build())
				.orElseThrow(() -> new UsernameNotFoundException("Account not found"));
	}
}

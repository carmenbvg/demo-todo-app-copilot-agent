package com.appsdeveloperai.demo_todoapp.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "user_accounts")
public class UserAccount {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 100)
	private String firstName;

	@Column(nullable = false, length = 100)
	private String lastName;

	@Column(nullable = false, unique = true, length = 254)
	private String email;

	@Column(nullable = false)
	private String passwordHash;

	protected UserAccount() {
	}

	public UserAccount(String firstName, String lastName, String email, String passwordHash) {
		this.firstName = firstName;
		this.lastName = lastName;
		this.email = email;
		this.passwordHash = passwordHash;
	}

	public String getEmail() {
		return email;
	}

	public String getPasswordHash() {
		return passwordHash;
	}
}

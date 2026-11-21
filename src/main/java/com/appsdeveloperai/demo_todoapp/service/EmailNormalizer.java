package com.appsdeveloperai.demo_todoapp.service;

import java.util.Locale;

final class EmailNormalizer {

	private EmailNormalizer() {
	}

	static String normalize(String email) {
		return email.trim().toLowerCase(Locale.ROOT);
	}
}

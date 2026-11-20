package com.flipkart.util;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

import com.flipkart.exception.IdGenerationException;
import com.flipkart.exception.ValidationException;

public class HashedCustomerLoginIdGenerator implements CustomerLoginIdGenerator {

	private final Predicate<String> loginIdExists;

	public HashedCustomerLoginIdGenerator(Predicate<String> loginIdExists) {
		this.loginIdExists = Objects.requireNonNull(loginIdExists, "loginIdExists predicate must not be null");
	}

	@Override
	public String generate(String name, String mobile) {

		int prefix = nameHash(name);

		for (int attempt = 0; attempt < 100000; attempt++) {

			String loginId = buildId(prefix, mobile);

			if (!loginIdExists.test(loginId)) {
				return loginId;
			}

			prefix = nextPrefix(prefix);
		}

		throw new IdGenerationException("Unable to generate a unique Customer Login ID after 100000 attempts");
	}

	private static String normalizeName(String name) {

		if (name == null) {
			throw new ValidationException(List.of("Customer name must not be null"));
		}

		StringBuilder normalized = new StringBuilder();

		for (char ch : name.toCharArray()) {

			if (ch >= 'A' && ch <= 'Z') {
				normalized.append((char) (ch + 32));
			} else if (ch >= 'a' && ch <= 'z') {
				normalized.append(ch);
			}
		}

		if (normalized.isEmpty()) {
			throw new ValidationException(List.of("Customer name must contain English letters"));
		}

		return normalized.toString();
	}

	static int nameHash(String name) {

		String normalized = normalizeName(name);

		int hash = 0;

		for (char ch : normalized.toCharArray()) {

			int characterPosition = ch - 'a' + 1;

			hash = (31 * hash + characterPosition) % 100000;
		}

		return hash;
	}

	static int nextPrefix(int prefix) {

		if (prefix < 0 || prefix > 99999) {
			throw new IllegalArgumentException("Prefix must be between 0 and 99999");
		}

		return (prefix + 1) % 100000;
	}

	static String buildId(int prefix, String mobile) {

		if (prefix < 0 || prefix > 99999) {
			throw new IllegalArgumentException("Prefix must be between 0 and 99999");
		}

		if (mobile == null || !mobile.matches("[6-9]\\d{9}")) {
			throw new ValidationException(List.of("Mobile must contain exactly 10 digits and start with 6, 7, 8 or 9"));
		}

		String prefixText = Integer.toString(prefix);

		return "0".repeat(5 - prefixText.length())
		        + prefixText
		        + mobile.substring(8);
	}

}

package com.flipkart;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class BuildSmokeTest {

	@Test
	void shouldRunOnJava17OrHigher() {
		int javaVersion = Runtime.version().feature();

		assertTrue(javaVersion >= 17, "Application must run on Java 17 or higher");
	}
}

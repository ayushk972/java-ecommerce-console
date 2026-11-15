
package com.flipkart.integration;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("integration")
public class TagFilterSmokeIntegrationTest {

	@Test
	void shouldRunAsIntegrationTest() {
		assertTrue(true, "Integration smoke test executed successfully");
	}
}

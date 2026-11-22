
package com.flipkart.testsupport;

import com.flipkart.util.PasswordHasher;

/**
 * @author md-masood
 */

public class FakePasswordHasher implements PasswordHasher {

    @Override
    public String hash(String raw) {
        return "hashed:" + raw;
    }

    @Override
    public boolean verify(String raw, String stored) {

        if (raw == null || stored == null) {
            return false;
        }

        return stored.equals("hashed:" + raw);
    }
}

package com.flipkart.util;

public interface PasswordHasher {

    String hash(String raw);

    boolean verify(String raw, String stored);
}
package com.flipkart.util;

import java.util.Locale;
import java.util.NoSuchElementException;

public interface ConsoleIO {

    void println(String message);

    default void println() {
        println("");
    }

    void print(String message);

    String readLine();

    default String readLine(String prompt) {
        if (prompt != null && !prompt.isEmpty()) {
            print(prompt);
        }
        return readLine();
    }

    default String readPassword(String prompt) {
        return readLine(prompt);
    }

    default int readInt(String prompt, int min, int max) {
        if (min > max) {
            throw new IllegalArgumentException("min (" + min + ") must not exceed max (" + max + ")");
        }
        while (true) {
            String raw = readLine(prompt);
            if (raw == null) {
                throw new NoSuchElementException("End of input reached while reading integer");
            }
            String trimmed = raw.trim();
            try {
                int value = Integer.parseInt(trimmed);
                if (value >= min && value <= max) {
                    return value;
                }
            } catch (NumberFormatException ignored) {
                // Re-prompt on non-integer input
            }
            println("Invalid input. Please enter a number between " + min + " and " + max + ".");
        }
    }

    default boolean readYesNo(String prompt) {
        while (true) {
            String raw = readLine(prompt);
            if (raw == null) {
                throw new NoSuchElementException("End of input reached while reading yes/no confirmation");
            }
            String normalized = raw.trim().toLowerCase(Locale.ROOT);
            if ("y".equals(normalized) || "yes".equals(normalized)) {
                return true;
            }
            if ("n".equals(normalized) || "no".equals(normalized)) {
                return false;
            }
            println("Invalid input. Please enter 'y' or 'n'.");
        }
    }
}
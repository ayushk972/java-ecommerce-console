package com.flipkart.util;

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
}
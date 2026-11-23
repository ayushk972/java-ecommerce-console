package com.flipkart.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class SystemConsoleIO implements ConsoleIO {

    private final BufferedReader reader;
    private final PrintStream out;

    public SystemConsoleIO() {
        this(System.in, System.out);
    }

    public SystemConsoleIO(InputStream in, PrintStream out) {
        Objects.requireNonNull(in, "Input stream must not be null");
        this.out = Objects.requireNonNull(out, "Output stream must not be null");
        this.reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
    }

    @Override
    public void println(String message) {
        out.println(message == null ? "" : message);
    }

    @Override
    public void print(String message) {
        out.print(message == null ? "" : message);
    }

    @Override
    public String readLine() {
        try {
            return reader.readLine();
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read line from console input", e);
        }
    }
}
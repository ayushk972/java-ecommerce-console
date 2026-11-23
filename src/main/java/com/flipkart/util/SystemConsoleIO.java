package com.flipkart.util;

import java.io.BufferedReader;
import java.io.Console;
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
    private final boolean useSystemConsole;

    public SystemConsoleIO() {
        this(System.in, System.out, true);
    }

    public SystemConsoleIO(InputStream in, PrintStream out) {
        this(in, out, false);
    }

    private SystemConsoleIO(InputStream in, PrintStream out, boolean useSystemConsole) {
        Objects.requireNonNull(in, "Input stream must not be null");
        this.out = Objects.requireNonNull(out, "Output stream must not be null");
        this.reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
        this.useSystemConsole = useSystemConsole;
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

    @Override
    public String readPassword(String prompt) {
        if (useSystemConsole) {
            Console console = System.console();
            if (console != null) {
                char[] chars = (prompt != null && !prompt.isEmpty())
                        ? console.readPassword("%s", prompt)
                        : console.readPassword();
                return chars == null ? null : new String(chars);
            }
        }
        return readLine(prompt);
    }
}
package com.flipkart.util;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SystemConsoleIOTest {

    @Test
    void shouldRejectNullStreams() {
        ByteArrayInputStream in = new ByteArrayInputStream(new byte[0]);
        PrintStream out = new PrintStream(new ByteArrayOutputStream());

        assertThrows(NullPointerException.class, () -> new SystemConsoleIO(null, out));
        assertThrows(NullPointerException.class, () -> new SystemConsoleIO(in, null));
    }

    @Test
    void shouldReadLinesAndWriteOutput() {
        ByteArrayInputStream in = new ByteArrayInputStream("alice\nsecret123\n".getBytes(StandardCharsets.UTF_8));
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(buffer, true, StandardCharsets.UTF_8);

        ConsoleIO io = new SystemConsoleIO(in, out);
        String user = io.readLine("Username: ");
        String pass = io.readLine();
        io.println("Welcome, " + user);
        io.println();

        assertEquals("alice", user);
        assertEquals("secret123", pass);
        assertNull(io.readLine());

        String output = buffer.toString(StandardCharsets.UTF_8);
        String expected = "Username: Welcome, alice" + System.lineSeparator() + System.lineSeparator();
        assertEquals(expected, output);
    }
}
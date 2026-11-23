package com.flipkart.util;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

    @Test
    void shouldReadPasswordFromFallbackStream() {
        ByteArrayInputStream in = new ByteArrayInputStream("Pass@1234\n".getBytes(StandardCharsets.UTF_8));
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(buffer, true, StandardCharsets.UTF_8);

        ConsoleIO io = new SystemConsoleIO(in, out);
        assertEquals("Pass@1234", io.readPassword("Password: "));
        assertEquals("Password: ", buffer.toString(StandardCharsets.UTF_8));
    }

    @Test
    void shouldReadIntWithinRangeAndRepromptOnInvalidInput() {
        ByteArrayInputStream in = new ByteArrayInputStream("abc\n0\n5\n3\n".getBytes(StandardCharsets.UTF_8));
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(buffer, true, StandardCharsets.UTF_8);

        ConsoleIO io = new SystemConsoleIO(in, out);
        int choice = io.readInt("Choice: ", 1, 4);

        assertEquals(3, choice);
        assertThrows(IllegalArgumentException.class, () -> io.readInt("Choice: ", 5, 1));
        assertThrows(NoSuchElementException.class, () -> io.readInt("Choice: ", 1, 4));
    }

    @Test
    void shouldReadYesNoAndRepromptOnInvalidInput() {
        ByteArrayInputStream in = new ByteArrayInputStream("maybe\nYES\nn\n".getBytes(StandardCharsets.UTF_8));
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(buffer, true, StandardCharsets.UTF_8);

        ConsoleIO io = new SystemConsoleIO(in, out);
        assertTrue(io.readYesNo("Confirm? "));
        assertFalse(io.readYesNo("Confirm again? "));
        assertThrows(NoSuchElementException.class, () -> io.readYesNo("Confirm EOF? "));
    }
}
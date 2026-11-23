package com.flipkart.testsupport;

import com.flipkart.util.ConsoleIO;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Queue;

public class ScriptedConsoleIO implements ConsoleIO {

    private final Queue<String> scriptedInputs = new ArrayDeque<>();
    private final List<String> printedLines = new ArrayList<>();
    private final StringBuilder rawOutput = new StringBuilder();
    private final StringBuilder currentLineBuffer = new StringBuilder();

    public ScriptedConsoleIO(String... inputs) {
        if (inputs != null) {
            scriptedInputs.addAll(Arrays.asList(inputs));
        }
    }

    public ScriptedConsoleIO(List<String> inputs) {
        if (inputs != null) {
            scriptedInputs.addAll(inputs);
        }
    }

    public void enqueueInput(String... inputs) {
        if (inputs != null) {
            scriptedInputs.addAll(Arrays.asList(inputs));
        }
    }

    @Override
    public void println(String message) {
        String text = message == null ? "" : message;
        currentLineBuffer.append(text);
        printedLines.add(currentLineBuffer.toString());
        currentLineBuffer.setLength(0);
        rawOutput.append(text).append(System.lineSeparator());
    }

    @Override
    public void print(String message) {
        String text = message == null ? "" : message;
        currentLineBuffer.append(text);
        rawOutput.append(text);
    }

    @Override
    public String readLine() {
        if (scriptedInputs.isEmpty()) {
            throw new NoSuchElementException("No more scripted console inputs available");
        }
        return scriptedInputs.poll();
    }

    public List<String> getPrintedLines() {
        return Collections.unmodifiableList(printedLines);
    }

    public String getOutput() {
        return rawOutput.toString();
    }

    public boolean hasRemainingInputs() {
        return !scriptedInputs.isEmpty();
    }

    public int remainingInputCount() {
        return scriptedInputs.size();
    }
}

package com.flipkart.util;

import java.util.Locale;

import com.flipkart.exception.IdGenerationException;

public class SequentialIdGenerator implements IdGenerator {

    private final String prefix;
    private final int width;

    private long counter;
    private boolean exhausted;

    public SequentialIdGenerator(String prefix, long start, int width) {

        if (prefix == null) {
            throw new IllegalArgumentException("Prefix cannot be null");
        }

        if (start < 0) {
            throw new IllegalArgumentException("Start value cannot be negative");
        }

        if (width < 0) {
            throw new IllegalArgumentException("Width cannot be negative");
        }

        this.prefix = prefix;
        this.counter = start;
        this.width = width;
        this.exhausted = false;
    }

    @Override
    public synchronized String nextId() {

        if (exhausted) {
            throw new IdGenerationException("ID counter overflow");
        }

        long current = counter;

        if (counter == Long.MAX_VALUE) {
            exhausted = true;
        } else {
            counter++;
        }

        if (width == 0) {
            return prefix + current;
        }

        return prefix + String.format(
                Locale.ROOT,
                "%0" + width + "d",
                current
        );
    }
}

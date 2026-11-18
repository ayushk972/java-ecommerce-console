
package com.flipkart.util;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.flipkart.exception.IdGenerationException;

class SequentialIdGeneratorTest {

    @Test
    void shouldGenerateSequentialIds() {
        SequentialIdGenerator generator =
                new SequentialIdGenerator("PRD-", 1001, 4);

        assertEquals("PRD-1001", generator.nextId());
        assertEquals("PRD-1002", generator.nextId());
        assertEquals("PRD-1003", generator.nextId());
    }

    @Test
    void shouldAddLeadingZeros() {
        SequentialIdGenerator generator =
                new SequentialIdGenerator("ORD-", 1, 6);

        assertEquals("ORD-000001", generator.nextId());
        assertEquals("ORD-000002", generator.nextId());
    }

    @Test
    void shouldNotTruncateNumbersWiderThanWidth() {
        SequentialIdGenerator generator =
                new SequentialIdGenerator("ORD-", 1000000, 6);

        assertEquals("ORD-1000000", generator.nextId());
    }

    @Test
    void shouldGenerateUniqueIds() {
        SequentialIdGenerator generator =
                new SequentialIdGenerator("PRD-", 1, 4);

        Set<String> ids = new HashSet<>();

        for (int i = 0; i < 1000; i++) {
            ids.add(generator.nextId());
        }

        assertEquals(1000, ids.size());
    }

    @Test
    void shouldRejectNullPrefix() {
        assertThrows(IllegalArgumentException.class,
                () -> new SequentialIdGenerator(null, 1, 4));
    }

    @Test
    void shouldRejectNegativeStart() {
        assertThrows(IllegalArgumentException.class,
                () -> new SequentialIdGenerator("PRD-", -1, 4));
    }

    @Test
    void shouldRejectNegativeWidth() {
        assertThrows(IllegalArgumentException.class,
                () -> new SequentialIdGenerator("PRD-", 1, -1));
    }

    @Test
    void shouldThrowExceptionWhenCounterOverflows() {
        SequentialIdGenerator generator =
                new SequentialIdGenerator("PRD-", Long.MAX_VALUE, 4);

        assertEquals("PRD-" + Long.MAX_VALUE, generator.nextId());

        assertThrows(IdGenerationException.class, generator::nextId);
    }

    @Test
    void shouldSupportZeroWidth() {
        SequentialIdGenerator generator =
                new SequentialIdGenerator("PRD-", 1001, 0);

        assertEquals("PRD-1001", generator.nextId());
    }
}

package com.flipkart.integration;

import com.flipkart.testsupport.ScriptedConsoleIO;
import com.flipkart.util.MoneyFormatter;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("integration")
class ConsoleSmokeIntegrationTest {

    @Test
    void shouldScriptConsoleInteractionAndFormatCurrency() {
        ScriptedConsoleIO io = new ScriptedConsoleIO("1", "exit");

        assertTrue(io.hasRemainingInputs());
        assertEquals(2, io.remainingInputCount());

        String choice = io.readLine("Select option: ");
        assertEquals("1", choice);
        io.println("Total payable: " + MoneyFormatter.format(new BigDecimal("149999.50")));

        String next = io.readLine();
        assertEquals("exit", next);
        assertFalse(io.hasRemainingInputs());
        assertThrows(NoSuchElementException.class, io::readLine);

        assertEquals(List.of("Select option: Total payable: Rs. 1,49,999.50"), io.getPrintedLines());
    }
}
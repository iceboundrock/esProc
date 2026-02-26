package com.scudata.common;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Escape - escape/unescape utilities")
public class EscapeTest {

    // --- add / remove round-trip ---

    @Test
    @DisplayName("add(null) returns null")
    void addNullReturnsNull() {
        assertNull(Escape.add(null));
    }

    @Test
    @DisplayName("remove(null) returns null")
    void removeNullReturnsNull() {
        assertNull(Escape.remove(null));
    }

    @Test
    @DisplayName("add then remove round-trip for plain text")
    void addRemoveRoundTripPlain() {
        String input = "hello world";
        assertEquals(input, Escape.remove(Escape.add(input)));
    }

    @Test
    @DisplayName("add escapes tab character")
    void addEscapesTab() {
        assertEquals("\\t", Escape.add("\t"));
    }

    @Test
    @DisplayName("add escapes carriage return")
    void addEscapesCR() {
        assertEquals("\\r", Escape.add("\r"));
    }

    @Test
    @DisplayName("add escapes newline")
    void addEscapesNewline() {
        assertEquals("\\n", Escape.add("\n"));
    }

    @Test
    @DisplayName("add escapes single quote")
    void addEscapesSingleQuote() {
        assertEquals("\\'", Escape.add("'"));
    }

    @Test
    @DisplayName("add escapes double quote")
    void addEscapesDoubleQuote() {
        assertEquals("\\\"", Escape.add("\""));
    }

    @Test
    @DisplayName("add escapes backslash itself")
    void addEscapesBackslash() {
        assertEquals("\\\\", Escape.add("\\"));
    }

    @Test
    @DisplayName("add/remove round-trip with all special chars")
    void addRemoveAllSpecialChars() {
        String input = "tab\there\nnewline\rreturn'single\"double\\backslash";
        assertEquals(input, Escape.remove(Escape.add(input)));
    }

    @Test
    @DisplayName("remove restores tab from \\t")
    void removeRestoresTab() {
        assertEquals("\t", Escape.remove("\\t"));
    }

    @Test
    @DisplayName("remove restores newline from \\n")
    void removeRestoresNewline() {
        assertEquals("\n", Escape.remove("\\n"));
    }

    @Test
    @DisplayName("remove restores carriage return from \\r")
    void removeRestoresCR() {
        assertEquals("\r", Escape.remove("\\r"));
    }

    @Test
    @DisplayName("remove on empty string returns empty")
    void removeEmptyString() {
        assertEquals("", Escape.remove(""));
    }

    @Test
    @DisplayName("add on empty string returns empty")
    void addEmptyString() {
        assertEquals("", Escape.add(""));
    }

    // --- custom escape char ---

    @Test
    @DisplayName("add with custom escape char '~'")
    void addCustomEscapeChar() {
        String result = Escape.add("\t", '~');
        assertEquals("~t", result);
    }

    @Test
    @DisplayName("remove with custom escape char '~'")
    void removeCustomEscapeChar() {
        assertEquals("\t", Escape.remove("~t", '~'));
    }

    // --- add with escapedChars ---

    @Test
    @DisplayName("add with escapedChars escapes additional characters")
    void addWithEscapedChars() {
        String result = Escape.add("(test)", "()");
        assertTrue(result.contains("\\("));
        assertTrue(result.contains("\\)"));
    }

    @Test
    @DisplayName("add with null escapedChars works like normal add")
    void addWithNullEscapedChars() {
        String result = Escape.add("hello", (String) null, '\\');
        assertEquals("hello", result);
    }

    // --- addEscAndQuote ---

    @Test
    @DisplayName("addEscAndQuote(null) returns null")
    void addEscAndQuoteNullReturnsNull() {
        assertNull(Escape.addEscAndQuote(null));
    }

    @Test
    @DisplayName("addEscAndQuote wraps in double quotes by default")
    void addEscAndQuoteDoubleQuotes() {
        String result = Escape.addEscAndQuote("hello");
        assertTrue(result.startsWith("\""));
        assertTrue(result.endsWith("\""));
        assertEquals("\"hello\"", result);
    }

    @Test
    @DisplayName("addEscAndQuote with single quotes")
    void addEscAndQuoteSingleQuotes() {
        String result = Escape.addEscAndQuote("hello", false);
        assertTrue(result.startsWith("'"));
        assertTrue(result.endsWith("'"));
        assertEquals("'hello'", result);
    }

    @Test
    @DisplayName("addEscAndQuote escapes inner double quotes when double-quoting")
    void addEscAndQuoteEscapesInnerDoubleQuotes() {
        String result = Escape.addEscAndQuote("say\"hi", true);
        assertEquals("\"say\\\"hi\"", result);
    }

    @Test
    @DisplayName("addEscAndQuote escapes inner single quotes when single-quoting")
    void addEscAndQuoteEscapesInnerSingleQuotes() {
        String result = Escape.addEscAndQuote("it's", false);
        assertEquals("'it\\'s'", result);
    }

    @Test
    @DisplayName("addEscAndQuote does not escape single quote when double quoting")
    void addEscAndQuoteNoEscapeSingleInDoubleMode() {
        String result = Escape.addEscAndQuote("it's", true);
        assertEquals("\"it's\"", result);
    }

    // --- removeEscAndQuote ---

    @Test
    @DisplayName("removeEscAndQuote(null) returns null")
    void removeEscAndQuoteNull() {
        assertNull(Escape.removeEscAndQuote(null));
    }

    @Test
    @DisplayName("removeEscAndQuote strips outer double quotes")
    void removeEscAndQuoteStripsDoubleQuotes() {
        assertEquals("hello", Escape.removeEscAndQuote("\"hello\""));
    }

    @Test
    @DisplayName("removeEscAndQuote strips outer single quotes")
    void removeEscAndQuoteStripsSingleQuotes() {
        assertEquals("hello", Escape.removeEscAndQuote("'hello'"));
    }

    @Test
    @DisplayName("removeEscAndQuote on empty string returns empty")
    void removeEscAndQuoteEmpty() {
        assertEquals("", Escape.removeEscAndQuote(""));
    }

    @Test
    @DisplayName("addEscAndQuote / removeEscAndQuote round-trip")
    void addRemoveEscAndQuoteRoundTrip() {
        String original = "hello\tworld\nnew\"line";
        String escaped = Escape.addEscAndQuote(original, true);
        String restored = Escape.removeEscAndQuote(escaped);
        assertEquals(original, restored);
    }

    // --- addExcelQuote ---

    @Test
    @DisplayName("addExcelQuote(null) returns null")
    void addExcelQuoteNull() {
        assertNull(Escape.addExcelQuote(null));
    }

    @Test
    @DisplayName("addExcelQuote wraps in double quotes and doubles inner quotes")
    void addExcelQuoteBasic() {
        assertEquals("\"hello\"", Escape.addExcelQuote("hello"));
    }

    @Test
    @DisplayName("addExcelQuote doubles inner double quotes")
    void addExcelQuoteDoublesInnerQuotes() {
        assertEquals("\"say\"\"hi\"", Escape.addExcelQuote("say\"hi"));
    }

    // --- change ---

    @Test
    @DisplayName("change(null) returns null")
    void changeNull() {
        assertNull(Escape.change(null, '\\', '/'));
    }

    @Test
    @DisplayName("change empty string returns empty")
    void changeEmpty() {
        assertEquals("", Escape.change("", '\\', '/'));
    }

    @Test
    @DisplayName("change replaces escape char in escape sequences")
    void changeReplacesEscapeChar() {
        String result = Escape.change("\\t\\n", '\\', '/');
        assertEquals("/t/n", result);
    }
}

package com.scudata.common;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("StringUtils Tests")
class StringUtilsTest {

    // ========== toExcelLabel ==========

    @Test
    @DisplayName("toExcelLabel: single letter columns A-Z")
    void toExcelLabel_singleLetter() {
        assertEquals("A", StringUtils.toExcelLabel(1));
        assertEquals("B", StringUtils.toExcelLabel(2));
        assertEquals("Z", StringUtils.toExcelLabel(26));
    }

    @Test
    @DisplayName("toExcelLabel: two letter columns AA-AZ, BA")
    void toExcelLabel_twoLetters() {
        assertEquals("AA", StringUtils.toExcelLabel(27));
        assertEquals("AB", StringUtils.toExcelLabel(28));
        assertEquals("AZ", StringUtils.toExcelLabel(52));
        assertEquals("BA", StringUtils.toExcelLabel(53));
        assertEquals("ZZ", StringUtils.toExcelLabel(702));
    }

    @Test
    @DisplayName("toExcelLabel: three letter columns")
    void toExcelLabel_threeLetters() {
        assertEquals("AAA", StringUtils.toExcelLabel(703));
        assertEquals("AAB", StringUtils.toExcelLabel(704));
    }

    // ========== isSpaceString ==========

    @Test
    @DisplayName("isSpaceString: null returns true")
    void isSpaceString_null() {
        assertTrue(StringUtils.isSpaceString(null));
    }

    @Test
    @DisplayName("isSpaceString: empty and whitespace strings return true")
    void isSpaceString_emptyAndWhitespace() {
        assertTrue(StringUtils.isSpaceString(""));
        assertTrue(StringUtils.isSpaceString("   "));
        assertTrue(StringUtils.isSpaceString("\t\n\r"));
    }

    @Test
    @DisplayName("isSpaceString: non-whitespace strings return false")
    void isSpaceString_nonWhitespace() {
        assertFalse(StringUtils.isSpaceString("a"));
        assertFalse(StringUtils.isSpaceString(" a "));
        assertFalse(StringUtils.isSpaceString("hello"));
    }

    // ========== toHexString ==========

    @Test
    @DisplayName("toHexString: converts low bytes of long to hex")
    void toHexString_basic() {
        assertEquals("FF", StringUtils.toHexString(0xFFL, 1));
        assertEquals("00FF", StringUtils.toHexString(0xFFL, 2));
        assertEquals("0000", StringUtils.toHexString(0L, 2));
        assertEquals("ABCD", StringUtils.toHexString(0xABCDL, 2));
        assertEquals("0001", StringUtils.toHexString(1L, 2));
    }

    @Test
    @DisplayName("toHexString: 4 bytes")
    void toHexString_fourBytes() {
        assertEquals("DEADBEEF", StringUtils.toHexString(0xDEADBEEFL, 4));
        assertEquals("00000001", StringUtils.toHexString(1L, 4));
    }

    // ========== deunicode / unicode ==========

    @Test
    @DisplayName("deunicode: escapes backslash and control characters")
    void deunicode_controlChars() {
        assertEquals("\\\\", StringUtils.deunicode("\\"));
        assertEquals("\\t", StringUtils.deunicode("\t"));
        assertEquals("\\n", StringUtils.deunicode("\n"));
        assertEquals("\\r", StringUtils.deunicode("\r"));
        assertEquals("\\f", StringUtils.deunicode("\f"));
    }

    @Test
    @DisplayName("deunicode: ASCII printable chars pass through")
    void deunicode_asciiPassthrough() {
        assertEquals("hello", StringUtils.deunicode("hello"));
        assertEquals("abc123", StringUtils.deunicode("abc123"));
    }

    @Test
    @DisplayName("deunicode: non-ASCII chars become \\uXXXX")
    void deunicode_nonAscii() {
        String result = StringUtils.deunicode("\u4e2d");
        assertEquals("\\u4E2D", result);
    }

    @Test
    @DisplayName("deunicode with specialChars: adds leading backslash")
    void deunicode_specialChars() {
        String result = StringUtils.deunicode("a=b", "=");
        assertEquals("a\\=b", result);
    }

    @Test
    @DisplayName("deunicode with StringBuffer: appends to existing buffer")
    void deunicode_withStringBuffer() {
        StringBuffer sb = new StringBuffer("prefix:");
        StringBuffer result = StringUtils.deunicode("hi\t", sb);
        assertSame(sb, result);
        assertEquals("prefix:hi\\t", result.toString());
    }

    @Test
    @DisplayName("unicode: converts \\uXXXX back to characters")
    void unicode_basic() {
        assertEquals("\u4e2d", StringUtils.unicode("\\u4E2D"));
        assertEquals("\t", StringUtils.unicode("\\t"));
        assertEquals("\n", StringUtils.unicode("\\n"));
        assertEquals("\r", StringUtils.unicode("\\r"));
        assertEquals("\f", StringUtils.unicode("\\f"));
        assertEquals("\\", StringUtils.unicode("\\\\"));
    }

    @Test
    @DisplayName("unicode and deunicode are inverse operations")
    void unicode_deunicode_roundtrip() {
        String original = "Hello\tWorld\n\u4e2d\u6587";
        String encoded = StringUtils.deunicode(original);
        String decoded = StringUtils.unicode(encoded);
        assertEquals(original, decoded);
    }

    // ========== matches ==========

    @Test
    @DisplayName("matches: star wildcard matches zero or more chars")
    void matches_starWildcard() {
        assertTrue(StringUtils.matches("hello", "h*o", false));
        assertTrue(StringUtils.matches("hello", "*", false));
        assertTrue(StringUtils.matches("hello", "h*", false));
        assertTrue(StringUtils.matches("hello", "*o", false));
        assertTrue(StringUtils.matches("", "*", false));
        assertTrue(StringUtils.matches("hello", "hello*", false));
    }

    @Test
    @DisplayName("matches: question mark wildcard matches exactly one char")
    void matches_questionWildcard() {
        assertTrue(StringUtils.matches("hello", "h?llo", false));
        assertTrue(StringUtils.matches("hello", "hell?", false));
        assertFalse(StringUtils.matches("hello", "h?lo", false));
        assertFalse(StringUtils.matches("", "?", false));
    }

    @Test
    @DisplayName("matches: null values return false")
    void matches_nullValues() {
        assertFalse(StringUtils.matches(null, "*", false));
        assertFalse(StringUtils.matches("hello", null, false));
        assertFalse(StringUtils.matches(null, null, false));
    }

    @Test
    @DisplayName("matches: case insensitive matching")
    void matches_caseInsensitive() {
        assertTrue(StringUtils.matches("Hello", "h*o", true));
        assertTrue(StringUtils.matches("HELLO", "hello", true));
        assertFalse(StringUtils.matches("Hello", "h*o", false));
    }

    @Test
    @DisplayName("matches: escaped wildcards match literal chars")
    void matches_escapedWildcards() {
        assertTrue(StringUtils.matches("h*o", "h\\*o", false));
        assertTrue(StringUtils.matches("h?o", "h\\?o", false));
        assertFalse(StringUtils.matches("hXo", "h\\*o", false));
    }

    // ========== like ==========

    @Test
    @DisplayName("like: percent wildcard matches zero or more chars")
    void like_percentWildcard() {
        assertTrue(StringUtils.like("hello", "%llo"));
        assertTrue(StringUtils.like("hello", "%"));
        assertTrue(StringUtils.like("hello", "hel%"));
        assertTrue(StringUtils.like("hello", "h%o"));
        assertTrue(StringUtils.like("", "%"));
    }

    @Test
    @DisplayName("like: underscore matches exactly one char")
    void like_underscoreWildcard() {
        assertTrue(StringUtils.like("hello", "h_llo"));
        assertTrue(StringUtils.like("hello", "hell_"));
        assertFalse(StringUtils.like("hello", "h_lo"));
    }

    @Test
    @DisplayName("like: null source returns false")
    void like_nullSource() {
        assertFalse(StringUtils.like(null, "%"));
    }

    @Test
    @DisplayName("like: exact match without wildcards")
    void like_exactMatch() {
        assertTrue(StringUtils.like("hello", "hello"));
        assertFalse(StringUtils.like("hello", "world"));
    }

    // ========== replace ==========

    @Test
    @DisplayName("replace: basic string replacement")
    void replace_basic() {
        assertEquals("hXllo", StringUtils.replace("hello", "e", "X"));
        assertEquals("hllo", StringUtils.replace("hello", "e", ""));
    }

    @Test
    @DisplayName("replace: replaces all occurrences")
    void replace_allOccurrences() {
        assertEquals("hXllX", StringUtils.replace("hello", "e", "X").equals("hXllo") ? "hXllo" : "unexpected");
        assertEquals("aXcXe", StringUtils.replace("abcbe", "b", "X"));
    }

    @Test
    @DisplayName("replace: null or empty arguments return src unchanged")
    void replace_nullOrEmpty() {
        assertNull(StringUtils.replace(null, "a", "b"));
        assertEquals("", StringUtils.replace("", "a", "b"));
        assertEquals("hello", StringUtils.replace("hello", "", "b"));
        assertEquals("hello", StringUtils.replace("hello", "a", null));
        assertEquals("hello", StringUtils.replace("hello", null, "b"));
    }

    @Test
    @DisplayName("replace: no match returns original string")
    void replace_noMatch() {
        assertEquals("hello", StringUtils.replace("hello", "xyz", "abc"));
    }

    // ========== isValidString ==========

    @Test
    @DisplayName("isValidString: valid non-empty strings return true")
    void isValidString_valid() {
        assertTrue(StringUtils.isValidString("abc"));
        assertTrue(StringUtils.isValidString("  a  "));
    }

    @Test
    @DisplayName("isValidString: null, empty, whitespace, non-String return false")
    void isValidString_invalid() {
        assertFalse(StringUtils.isValidString(null));
        assertFalse(StringUtils.isValidString(""));
        assertFalse(StringUtils.isValidString("   "));
        assertFalse(StringUtils.isValidString(123));
        assertFalse(StringUtils.isValidString(new Object()));
    }

    // ========== indexOf(String[], String) ==========

    @Test
    @DisplayName("indexOf: finds string in array")
    void indexOf_found() {
        String[] arr = {"apple", "banana", "cherry"};
        assertEquals(0, StringUtils.indexOf(arr, "apple"));
        assertEquals(1, StringUtils.indexOf(arr, "banana"));
        assertEquals(2, StringUtils.indexOf(arr, "cherry"));
    }

    @Test
    @DisplayName("indexOf: returns -1 when not found")
    void indexOf_notFound() {
        String[] arr = {"apple", "banana", "cherry"};
        assertEquals(-1, StringUtils.indexOf(arr, "grape"));
    }

    // ========== toRMB ==========

    @Test
    @DisplayName("toRMB: zero amount")
    void toRMB_zero() {
        String result = StringUtils.toRMB(0.0);
        assertEquals("零元整", result);
    }

    @Test
    @DisplayName("toRMB: simple integer amount")
    void toRMB_simpleInteger() {
        String result = StringUtils.toRMB(1.0);
        assertEquals("壹元整", result);
    }

    @Test
    @DisplayName("toRMB: amount with jiao and fen")
    void toRMB_withDecimal() {
        String result = StringUtils.toRMB(1.23);
        assertEquals("壹元贰角叁分", result);
    }

    // ========== toChinese ==========

    @Test
    @DisplayName("toChinese: zero returns 零")
    void toChinese_zero() {
        assertEquals("零", StringUtils.toChinese(0, true, true));
    }

    @Test
    @DisplayName("toChinese: abbreviated uppercase")
    void toChinese_abbreviatedUppercase() {
        assertEquals("壹佰贰拾叁", StringUtils.toChinese(123, true, true));
    }

    @Test
    @DisplayName("toChinese: non-abbreviated (digit by digit)")
    void toChinese_nonAbbreviated() {
        assertEquals("壹贰叁", StringUtils.toChinese(123, false, true));
    }

    @Test
    @DisplayName("toChinese: negative number")
    void toChinese_negative() {
        String result = StringUtils.toChinese(-5, true, true);
        assertTrue(result.startsWith("负"));
        assertTrue(result.contains("伍"));
    }

    @Test
    @DisplayName("toChinese: lowercase digits")
    void toChinese_lowercase() {
        assertEquals("一二三", StringUtils.toChinese(123, false, false));
    }

    // ========== identify ==========

    @Test
    @DisplayName("identify: null returns false")
    void identify_null() {
        assertFalse(StringUtils.identify(null));
    }

    @Test
    @DisplayName("identify: wrong length returns false")
    void identify_wrongLength() {
        assertFalse(StringUtils.identify("12345"));
        assertFalse(StringUtils.identify("1234567890123456"));
    }

    @Test
    @DisplayName("identify: valid 18-digit ID with correct check digit")
    void identify_valid18() {
        // Beijing, 1990-01-01, sequence 001, check digit computed:
        // 110101199001010019 -> compute check digit
        // Weights: 7,9,10,5,8,4,2,1,6,3,7,9,10,5,8,4,2
        // Digits:  1,1,0,1,0,1,1,9,9,0,0,1,0,1,0,0,1
        // Sum = 1*7+1*9+0*10+1*5+0*8+1*4+1*2+9*1+9*6+0*3+0*7+1*9+0*10+1*5+0*8+0*4+1*2
        //     = 7+9+0+5+0+4+2+9+54+0+0+9+0+5+0+0+2 = 106
        // 106 % 11 = 7 -> codes[7] = '5'
        assertTrue(StringUtils.identify("110101199001010015"));
    }

    @Test
    @DisplayName("identify: invalid 18-digit ID with wrong check digit")
    void identify_invalid18CheckDigit() {
        assertFalse(StringUtils.identify("110101199001010010"));
    }

    @Test
    @DisplayName("identify: invalid province code returns false")
    void identify_invalidProvince() {
        assertFalse(StringUtils.identify("990101199001010015"));
    }

    @Test
    @DisplayName("identify: 15-digit ID basic validation")
    void identify_15digit() {
        // 15-digit: province 11 (Beijing), year 90, month 01, day 01, sequence 001
        assertTrue(StringUtils.identify("110101900101001"));
    }

    @Test
    @DisplayName("identify: non-digit characters return false")
    void identify_nonDigit() {
        assertFalse(StringUtils.identify("11010119900101001A"));
    }

    // ========== isAssicString ==========

    @Test
    @DisplayName("isAssicString: null and empty return true")
    void isAssicString_nullAndEmpty() {
        assertTrue(StringUtils.isAssicString(null));
        assertTrue(StringUtils.isAssicString(""));
    }

    @Test
    @DisplayName("isAssicString: pure ASCII returns true")
    void isAssicString_pureAscii() {
        assertTrue(StringUtils.isAssicString("hello"));
        assertTrue(StringUtils.isAssicString("abc123!@#"));
    }

    @Test
    @DisplayName("isAssicString: non-ASCII returns false")
    void isAssicString_nonAscii() {
        assertFalse(StringUtils.isAssicString("\u4e2d"));
        assertFalse(StringUtils.isAssicString("hello\u00FF"));
    }

    // ========== getNewName ==========

    @Test
    @DisplayName("getNewName: returns prefix when no conflict")
    void getNewName_noConflict() {
        ArrayList<String> names = new ArrayList<>();
        names.add("other");
        assertEquals("test", StringUtils.getNewName("test", names));
    }

    @Test
    @DisplayName("getNewName: appends suffix number on conflict")
    void getNewName_withConflict() {
        ArrayList<String> names = new ArrayList<>();
        names.add("test");
        assertEquals("test1", StringUtils.getNewName("test", names));
    }

    @Test
    @DisplayName("getNewName: skips existing suffixed names")
    void getNewName_multipleConflicts() {
        ArrayList<String> names = new ArrayList<>();
        names.add("test");
        names.add("test1");
        names.add("test2");
        assertEquals("test3", StringUtils.getNewName("test", names));
    }

    @Test
    @DisplayName("getNewName: null list treated as empty")
    void getNewName_nullList() {
        assertEquals("test", StringUtils.getNewName("test", (ArrayList<String>) null));
    }

    // ========== indexOfIgnoreCase ==========

    @Test
    @DisplayName("indexOfIgnoreCase: finds substring ignoring case")
    void indexOfIgnoreCase_basic() {
        assertEquals(0, StringUtils.indexOfIgnoreCase("Hello World", "hello", 0));
        assertEquals(0, StringUtils.indexOfIgnoreCase("Hello World", "HELLO", 0));
        assertEquals(6, StringUtils.indexOfIgnoreCase("Hello World", "world", 0));
    }

    @Test
    @DisplayName("indexOfIgnoreCase: respects fromIndex")
    void indexOfIgnoreCase_fromIndex() {
        assertEquals(6, StringUtils.indexOfIgnoreCase("abcabcabc", "abc", 1));
        assertEquals(3, StringUtils.indexOfIgnoreCase("abcABCabc", "abc", 1));
    }

    @Test
    @DisplayName("indexOfIgnoreCase: returns -1 when not found")
    void indexOfIgnoreCase_notFound() {
        assertEquals(-1, StringUtils.indexOfIgnoreCase("Hello World", "xyz", 0));
    }

    @Test
    @DisplayName("indexOfIgnoreCase: empty target returns fromIndex")
    void indexOfIgnoreCase_emptyTarget() {
        assertEquals(0, StringUtils.indexOfIgnoreCase("Hello", "", 0));
        assertEquals(3, StringUtils.indexOfIgnoreCase("Hello", "", 3));
    }
}

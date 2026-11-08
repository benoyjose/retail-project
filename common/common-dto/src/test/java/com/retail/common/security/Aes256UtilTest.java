package com.retail.common.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class Aes256UtilTest {

    @Test
    void testEncryptAndDecrypt() {
        String original = "+1-555-019-2834, 123 Retail Boulevard, Suite 400";
        String encrypted = Aes256Util.encrypt(original);

        assertNotNull(encrypted);
        assertNotEquals(original, encrypted);

        String decrypted = Aes256Util.decrypt(encrypted);
        assertEquals(original, decrypted);
    }

    @Test
    void testNullAndEmptyHandling() {
        assertNull(Aes256Util.encrypt(null));
        assertNull(Aes256Util.decrypt(null));
        assertEquals("", Aes256Util.encrypt(""));
        assertEquals("", Aes256Util.decrypt(""));
    }
}


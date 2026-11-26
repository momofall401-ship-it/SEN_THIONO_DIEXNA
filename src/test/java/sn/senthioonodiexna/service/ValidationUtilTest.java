package sn.senthioonodiexna.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import sn.senthioonodiexna.util.ValidationUtil;

import static org.junit.jupiter.api.Assertions.*;

class ValidationUtilTest {

    @Test
    @DisplayName("Test de validation des emails")
    void testEmailValidation() {
        assertTrue(ValidationUtil.isValidEmail("citoyen@gmail.com"));
        assertTrue(ValidationUtil.isValidEmail("amadou.diop@justice.gouv.sn"));
        assertFalse(ValidationUtil.isValidEmail("citoyen.gmail.com"));
        assertFalse(ValidationUtil.isValidEmail("@gmail.com"));
        assertFalse(ValidationUtil.isValidEmail(""));
        assertFalse(ValidationUtil.isValidEmail(null));
    }

    @Test
    @DisplayName("Test de validation des numéros de téléphone sénégalais")
    void testSenegalPhoneValidation() {
        // Formats sénégalais : Orange (77, 78), Free (76), Expresso (70), Promobile (75), Fixe (33)
        assertTrue(ValidationUtil.isValidPhone("+221 77 123 45 67"));
        assertTrue(ValidationUtil.isValidPhone("771234567"));
        assertTrue(ValidationUtil.isValidPhone("+221 78 555 44 33"));
        assertTrue(ValidationUtil.isValidPhone("764005060"));
        assertTrue(ValidationUtil.isValidPhone("338211245"));
        assertFalse(ValidationUtil.isValidPhone("12345"));
        assertFalse(ValidationUtil.isValidPhone("0612345678")); // Numéro français non accepté sous format local
    }

    @Test
    @DisplayName("Test de validation de la robustesse minimale du mot de passe")
    void testPasswordValidation() {
        assertTrue(ValidationUtil.isValidPassword("Pass@2026"));
        assertTrue(ValidationUtil.isValidPassword("123456"));
        assertFalse(ValidationUtil.isValidPassword("12345"));
        assertFalse(ValidationUtil.isValidPassword(""));
        assertFalse(ValidationUtil.isValidPassword(null));
    }
}

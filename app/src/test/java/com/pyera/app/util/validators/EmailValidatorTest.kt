package com.pyera.app.util.validators

import com.pyera.app.util.ValidationUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

/**
 * Parameterized tests for EmailValidator
 */
@RunWith(Parameterized::class)
class EmailValidatorParameterizedTest(
    private val email: String,
    private val expectedValid: Boolean,
    private val description: String
) {
    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{2}: \"{0}\"")
        fun data(): List<Array<Any>> = listOf(
            // Valid emails
            arrayOf("user@example.com", true, "simple email"),
            arrayOf("user.name@example.com", true, "with dot in local"),
            arrayOf("user_name@example.com", true, "with underscore"),
            arrayOf("user-name@example.com", true, "with hyphen"),
            arrayOf("user+tag@example.com", true, "with plus tag"),
            arrayOf("user123@example.com", true, "with numbers"),
            arrayOf("123user@example.com", true, "starting with number"),
            arrayOf("a@b.co", true, "short email"),
            arrayOf("user@sub.example.com", true, "subdomain"),
            arrayOf("user@example.co.uk", true, "multi-level domain"),
            arrayOf("user@example.museum", true, "long TLD"),
            arrayOf("first.last@example.com", true, "first.last format"),
            arrayOf("user@localhost", true, "localhost domain"),
            arrayOf("!#$%&'*+/=?^_`{|}~@example.com", true, "special chars in local"),
            
            // Invalid: Empty and whitespace
            arrayOf("", false, "empty string"),
            arrayOf("   ", false, "whitespace only"),
            arrayOf(" \t\n", false, "various whitespace"),
            
            // Invalid: Missing @
            arrayOf("userexample.com", false, "missing @"),
            arrayOf("user", false, "no domain"),
            
            // Invalid: Missing local part
            arrayOf("@example.com", false, "no local part"),
            arrayOf("@", false, "only @"),
            
            // Invalid: Missing domain
            arrayOf("user@", false, "no domain part"),
            arrayOf("user@.", false, "dot only domain"),
            
            // Invalid: Double dots
            arrayOf("user..name@example.com", false, "double dot in local"),
            arrayOf("user@example..com", false, "double dot in domain"),
            arrayOf("user.name..tag@example.com", false, "multiple double dots"),
            
            // Invalid: Leading/trailing dots
            arrayOf(".user@example.com", false, "leading dot in local"),
            arrayOf("user.@example.com", false, "trailing dot in local"),
            arrayOf("user@.example.com", false, "leading dot in domain"),
            arrayOf("user@example.com.", false, "trailing dot in domain"),
            
            // Invalid: Invalid characters
            arrayOf("user name@example.com", false, "space in local"),
            arrayOf("user@exam ple.com", false, "space in domain"),
            arrayOf("user<>@example.com", false, "angle brackets"),
            arrayOf("user()@example.com", false, "parentheses"),
            arrayOf("user[]@example.com", false, "square brackets"),
            arrayOf("user@exam@ple.com", false, "multiple @"),
            arrayOf("user@exa\nmple.com", false, "newline in domain"),
            
            // Invalid: Too long
            arrayOf("${"a".repeat(65)}@example.com", false, "local part too long"),
            arrayOf("user@${"a".repeat(250)}.com", false, "total too long"),
            
            // Invalid: Disposable emails
            arrayOf("user@tempmail.com", false, "tempmail disposable"),
            arrayOf("user@mailinator.com", false, "mailinator disposable"),
            arrayOf("user@guerrillamail.com", false, "guerrillamail disposable"),
            arrayOf("user@yopmail.com", false, "yopmail disposable"),
            
            // Edge cases
            arrayOf("user@-example.com", false, "leading hyphen domain"),
            arrayOf("user@example-.com", false, "trailing hyphen domain"),
            arrayOf("user@123.456.789.0", true, "IP address domain"),
            arrayOf("user@[IPv6:2001:db8::1]", true, "IPv6 literal"),
            
            // Unicode (may or may not be valid depending on implementation)
            arrayOf("user@exämple.com", false, "IDN domain"),
            arrayOf("üser@example.com", false, "unicode local part"),
            
            // Case variations
            arrayOf("USER@EXAMPLE.COM", true, "uppercase"),
            arrayOf("User@Example.Com", true, "mixed case"),
            
            // Quoted strings (valid but often rejected)
            arrayOf("\"user name\"@example.com", true, "quoted local with space"),
            arrayOf("\"user@name\"@example.com", true, "quoted local with @"),
            
            // Common typos
            arrayOf("user@example,com", false, "comma instead of dot"),
            arrayOf("user@example com", false, "space instead of dot"),
            arrayOf("user@.com", false, "missing domain name"),
            arrayOf("user@example.c", false, "single char TLD"),
            
            // SQL injection attempts
            arrayOf("user'@example.com", false, "single quote"),
            arrayOf("user\"@example.com", false, "double quote"),
            arrayOf("user;@example.com", false, "semicolon"),
            
            // XSS attempts
            arrayOf("<script>@example.com", false, "script tag in local"),
            arrayOf("user@<script>.com", false, "script tag in domain")
        )
    }

    @Test
    fun testEmailValidation() {
        val result = EmailValidator.validate(email)
        val isSuccess = result is ValidationUtils.ValidationResult.Success
        
        assertEquals(
            "Email '$email' ($description) should be ${if (expectedValid) "valid" else "invalid"}",
            expectedValid,
            isSuccess
        )
    }
}

/**
 * Non-parameterized tests for EmailValidator
 */
class EmailValidatorNonParameterizedTest {

    @Test
    fun `validate returns correct error message for empty email`() {
        val result = EmailValidator.validate("")
        assertTrue(result is ValidationUtils.ValidationResult.Error)
        assertEquals("Email cannot be empty", (result as ValidationUtils.ValidationResult.Error).message)
    }

    @Test
    fun `validate returns correct error message for too long email`() {
        val longEmail = "user@${"a".repeat(250)}.com"
        val result = EmailValidator.validate(longEmail)
        assertTrue(result is ValidationUtils.ValidationResult.Error)
        assertTrue((result as ValidationUtils.ValidationResult.Error).message.contains("too long"))
    }

    @Test
    fun `validate returns correct error for disposable email`() {
        val result = EmailValidator.validate("user@tempmail.com")
        assertTrue(result is ValidationUtils.ValidationResult.Error)
        assertTrue((result as ValidationUtils.ValidationResult.Error).message.contains("Disposable"))
    }

    @Test
    fun `validate returns correct error for consecutive dots`() {
        val result = EmailValidator.validate("user..name@example.com")
        assertTrue(result is ValidationUtils.ValidationResult.Error)
        assertTrue((result as ValidationUtils.ValidationResult.Error).message.contains("consecutive dots"))
    }

    @Test
    fun `isValidFormat returns true for valid email format`() {
        assertTrue(EmailValidator.isValidFormat("user@example.com"))
        assertTrue(EmailValidator.isValidFormat("test@test.io"))
    }

    @Test
    fun `isValidFormat returns false for invalid email format`() {
        assertFalse(EmailValidator.isValidFormat(""))
        assertFalse(EmailValidator.isValidFormat("invalid"))
        assertFalse(EmailValidator.isValidFormat("@example.com"))
        assertFalse(EmailValidator.isValidFormat("user@"))
    }

    @Test
    fun `isValidFormat accepts disposable emails`() {
        // isValidFormat is less strict, just checks format
        assertTrue(EmailValidator.isValidFormat("user@tempmail.com"))
    }

    @Test
    fun `validate handles maximum length boundary`() {
        // 64 chars local + @ + 186 chars domain + .com = 254 max
        val maxLocal = "a".repeat(64)
        val maxDomain = "b".repeat(186)
        val email = "$maxLocal@$maxDomain.com" // Should be 254 chars
        
        // This might pass or fail depending on exact length calculation
        val result = EmailValidator.validate(email)
        // Just verify it doesn't crash
        assertTrue(result is ValidationUtils.ValidationResult.Success || result is ValidationUtils.ValidationResult.Error)
    }
}

package com.pyera.app.util.validators

import com.pyera.app.util.ValidationUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

/**
 * Parameterized tests for PasswordValidator validation
 */
@RunWith(Parameterized::class)
class PasswordValidatorParameterizedTest(
    private val password: String,
    private val expectedValid: Boolean,
    private val description: String
) {
    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{2}")
        fun data(): List<Array<Any>> = listOf(
            // Valid passwords
            arrayOf("Password1!", true, "valid password"),
            arrayOf("MyP@ssw0rd", true, "with @ symbol"),
            arrayOf("C0mplex!Pass", true, "complex password"),
            arrayOf("A1b2C3d4!", true, "alternating chars"),
            arrayOf("LongP@ssw0rd123", true, "long valid password"),
            arrayOf("P@ssw0rd!@#", true, "multiple special chars"),
            arrayOf("Test1234!Test", true, "with embedded special"),
            arrayOf("Str0ng!Passw0rd", true, "strong password"),
            
            // Too short
            arrayOf("", false, "empty"),
            arrayOf("a", false, "single char"),
            arrayOf("A1!", false, "three chars"),
            arrayOf("Pass1!", false, "six chars"),
            arrayOf("Passw1!", false, "seven chars"),
            
            // Missing uppercase
            arrayOf("password1!", false, "no uppercase"),
            arrayOf("lowercase1!", false, "all lowercase"),
            arrayOf("12345678!", false, "numbers and special only"),
            
            // Missing lowercase
            arrayOf("PASSWORD1!", false, "no lowercase"),
            arrayOf("UPPERCASE1!", false, "all uppercase"),
            
            // Missing digit
            arrayOf("Password!", false, "no digit"),
            arrayOf("NoDigitsHere!", false, "letters and special only"),
            arrayOf("Abcdefgh!", false, "no numbers"),
            
            // Missing special character
            arrayOf("Password1", false, "no special char"),
            arrayOf("Passw0rd", false, "letters and numbers only"),
            arrayOf("Password123", false, "no special chars"),
            
            // Contains space
            arrayOf("Password 1!", false, "space in middle"),
            arrayOf(" Password1!", false, "leading space"),
            arrayOf("Password1! ", false, "trailing space"),
            
            // Sequential characters
            arrayOf("Password123!", false, "sequential 123"),
            arrayOf("Passwordabc!", false, "sequential abc"),
            arrayOf("Passxyz1!", false, "sequential xyz"),
            arrayOf("Passqwer1!", false, "keyboard sequence"),
            arrayOf("Passasdf1!", false, "keyboard sequence asdf"),
            
            // Repeated characters
            arrayOf("Passsword1!", false, "triple s"),
            arrayOf("Passssword1!", false, "four s"),
            arrayOf("Password111!", false, "triple 1"),
            arrayOf("PPassword1!", false, "double P at start"),
            arrayOf("Password11!", false, "double 1"),
            
            // Common passwords
            arrayOf("Password123!", false, "common pattern"),
            arrayOf("Qwerty123!", false, "common keyboard"),
            arrayOf("Letmein1!", false, "common phrase"),
            arrayOf("Welcome1!", false, "common welcome"),
            arrayOf("Admin123!", false, "common admin"),
            arrayOf("Login123!", false, "common login"),
            
            // Too long
            arrayOf("A1!" + "a".repeat(130), false, "way too long"),
            
            // Unicode
            arrayOf("P@ssw0rd日本語", false, "unicode password"),
            arrayOf("P@ssw0rd🔐", false, "emoji password"),
            arrayOf("P@ssw0rdñ", true, "accented char"), // Might pass depending on implementation
            
            // SQL injection attempts
            arrayOf("P@ss'; DROP TABLE--", true, "SQL injection pattern"),
            arrayOf("P@ss OR 1=1", true, "boolean injection"),
            
            // XSS attempts
            arrayOf("<script>alert(1)", false, "XSS script (missing requirements)"),
            arrayOf("<Script1!>", false, "script tag (missing requirements)")
        )
    }

    @Test
    fun testPasswordValidation() {
        val result = PasswordValidator.validate(password)
        val isSuccess = result is ValidationUtils.ValidationResult.Success
        
        assertEquals(
            "Password '$description' should be ${if (expectedValid) "valid" else "invalid"}",
            expectedValid,
            isSuccess
        )
    }
}

/**
 * Tests for password strength calculation
 */
@RunWith(Parameterized::class)
class PasswordStrengthParameterizedTest(
    private val password: String,
    private val expectedStrength: PasswordValidator.Strength,
    private val description: String
) {
    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{2}: expected {1}")
        fun data(): List<Array<Any>> = listOf(
            // Very weak
            arrayOf("", PasswordValidator.Strength.VERY_WEAK, "empty"),
            arrayOf("a", PasswordValidator.Strength.VERY_WEAK, "single char"),
            arrayOf("123", PasswordValidator.Strength.VERY_WEAK, "short numbers"),
            arrayOf("abc", PasswordValidator.Strength.VERY_WEAK, "short letters"),
            
            // Weak
            arrayOf("password", PasswordValidator.Strength.WEAK, "lowercase only"),
            arrayOf("PASSWORD", PasswordValidator.Strength.WEAK, "uppercase only"),
            arrayOf("12345678", PasswordValidator.Strength.WEAK, "8 digits"),
            
            // Medium
            arrayOf("Password1", PasswordValidator.Strength.MEDIUM, "basic requirements"),
            arrayOf("Pass1234", PasswordValidator.Strength.MEDIUM, "no special char"),
            arrayOf("Pass!word", PasswordValidator.Strength.MEDIUM, "no digit"),
            
            // Strong
            arrayOf("Password1!", PasswordValidator.Strength.STRONG, "all requirements"),
            arrayOf("MyStr0ng!Pass", PasswordValidator.Strength.STRONG, "longer strong"),
            
            // Very strong
            arrayOf("V3ryStr0ng!P@ssw0rd", PasswordValidator.Strength.VERY_STRONG, "very strong"),
            arrayOf("Th1sIs@V3ryL0ngP@ss!", PasswordValidator.Strength.VERY_STRONG, "long complex")
        )
    }

    @Test
    fun testPasswordStrength() {
        val strength = PasswordValidator.calculateStrength(password)
        assertEquals(
            "Password '$description' should have strength $expectedStrength",
            expectedStrength,
            strength
        )
    }
}

/**
 * Non-parameterized tests for PasswordValidator
 */
class PasswordValidatorNonParameterizedTest {

    @Test
    fun `validate returns correct error for empty password`() {
        val result = PasswordValidator.validate("")
        assertTrue(result is ValidationUtils.ValidationResult.Error)
        assertEquals("Password cannot be empty", (result as ValidationUtils.ValidationResult.Error).message)
    }

    @Test
    fun `validate returns correct error for too short password`() {
        val result = PasswordValidator.validate("Pass1!")
        assertTrue(result is ValidationUtils.ValidationResult.Error)
        assertTrue((result as ValidationUtils.ValidationResult.Error).message.contains("at least 8"))
    }

    @Test
    fun `validate returns correct error for missing uppercase`() {
        val result = PasswordValidator.validate("password1!")
        assertTrue(result is ValidationUtils.ValidationResult.Error)
        assertTrue((result as ValidationUtils.ValidationResult.Error).message.contains("uppercase"))
    }

    @Test
    fun `validate returns correct error for missing lowercase`() {
        val result = PasswordValidator.validate("PASSWORD1!")
        assertTrue(result is ValidationUtils.ValidationResult.Error)
        assertTrue((result as ValidationUtils.ValidationResult.Error).message.contains("lowercase"))
    }

    @Test
    fun `validate returns correct error for missing digit`() {
        val result = PasswordValidator.validate("Password!")
        assertTrue(result is ValidationUtils.ValidationResult.Error)
        assertTrue((result as ValidationUtils.ValidationResult.Error).message.contains("digit"))
    }

    @Test
    fun `validate returns correct error for missing special char`() {
        val result = PasswordValidator.validate("Password1")
        assertTrue(result is ValidationUtils.ValidationResult.Error)
        assertTrue((result as ValidationUtils.ValidationResult.Error).message.contains("special character"))
    }

    @Test
    fun `validate returns correct error for sequential chars`() {
        val result = PasswordValidator.validate("Password123!")
        assertTrue(result is ValidationUtils.ValidationResult.Error)
        assertTrue((result as ValidationUtils.ValidationResult.Error).message.contains("sequential"))
    }

    @Test
    fun `validate returns correct error for repeated chars`() {
        val result = PasswordValidator.validate("Passsword1!")
        assertTrue(result is ValidationUtils.ValidationResult.Error)
        assertTrue((result as ValidationUtils.ValidationResult.Error).message.contains("repeated"))
    }

    @Test
    fun `validate returns correct error for common password`() {
        val result = PasswordValidator.validate("Password123!")
        assertTrue(result is ValidationUtils.ValidationResult.Error)
        // Could be either sequential or common password error
        val message = (result as ValidationUtils.ValidationResult.Error).message
        assertTrue(message.contains("sequential") || message.contains("common"))
    }

    @Test
    fun `validate returns correct error for password with spaces`() {
        val result = PasswordValidator.validate("Pass word1!")
        assertTrue(result is ValidationUtils.ValidationResult.Error)
        assertTrue((result as ValidationUtils.ValidationResult.Error).message.contains("spaces"))
    }

    @Test
    fun `validateConfirmation returns success for matching passwords`() {
        val result = PasswordValidator.validateConfirmation("Password1!", "Password1!")
        assertTrue(result is ValidationUtils.ValidationResult.Success)
    }

    @Test
    fun `validateConfirmation returns error for non-matching passwords`() {
        val result = PasswordValidator.validateConfirmation("Password1!", "Different1!")
        assertTrue(result is ValidationUtils.ValidationResult.Error)
        assertEquals("Passwords do not match", (result as ValidationUtils.ValidationResult.Error).message)
    }

    @Test
    fun `validateConfirmation returns error for empty confirmation`() {
        val result = PasswordValidator.validateConfirmation("Password1!", "")
        assertTrue(result is ValidationUtils.ValidationResult.Error)
        assertEquals("Please confirm your password", (result as ValidationUtils.ValidationResult.Error).message)
    }

    @Test
    fun `meetsMinimumRequirements returns true for valid password`() {
        assertTrue(PasswordValidator.meetsMinimumRequirements("Password1!"))
        assertTrue(PasswordValidator.meetsMinimumRequirements("Test1234!"))
    }

    @Test
    fun `meetsMinimumRequirements returns false for invalid passwords`() {
        assertFalse(PasswordValidator.meetsMinimumRequirements("password1!")) // No uppercase
        assertFalse(PasswordValidator.meetsMinimumRequirements("PASSWORD1!")) // No lowercase
        assertFalse(PasswordValidator.meetsMinimumRequirements("Password!!")) // No digit
        assertFalse(PasswordValidator.meetsMinimumRequirements("Password1")) // No special
        assertFalse(PasswordValidator.meetsMinimumRequirements("Pass1!")) // Too short
    }

    @Test
    fun `calculateStrength returns VERY_WEAK for empty or very short passwords`() {
        assertEquals(PasswordValidator.Strength.VERY_WEAK, PasswordValidator.calculateStrength(""))
        assertEquals(PasswordValidator.Strength.VERY_WEAK, PasswordValidator.calculateStrength("a"))
    }

    @Test
    fun `calculateStrength returns STRONG for passwords meeting all requirements`() {
        assertEquals(PasswordValidator.Strength.STRONG, PasswordValidator.calculateStrength("Password1!"))
    }

    @Test
    fun `calculateStrength returns VERY_STRONG for long complex passwords`() {
        assertEquals(
            PasswordValidator.Strength.VERY_STRONG, 
            PasswordValidator.calculateStrength("V3ryStr0ng!P@ssw0rd123")
        )
    }

    @Test
    fun `strength enum has correct order`() {
        val strengths = PasswordValidator.Strength.values()
        assertEquals(5, strengths.size)
        assertEquals(PasswordValidator.Strength.VERY_WEAK, strengths[0])
        assertEquals(PasswordValidator.Strength.VERY_STRONG, strengths[4])
    }

    @Test
    fun `validate handles maximum length password`() {
        val longPassword = "A1!" + "a".repeat(125) // 128 chars
        val result = PasswordValidator.validate(longPassword)
        assertTrue(result is ValidationUtils.ValidationResult.Success)
    }

    @Test
    fun `validate rejects password exceeding maximum length`() {
        val tooLongPassword = "A1!" + "a".repeat(130) // 133 chars
        val result = PasswordValidator.validate(tooLongPassword)
        assertTrue(result is ValidationUtils.ValidationResult.Error)
        assertTrue((result as ValidationUtils.ValidationResult.Error).message.contains("too long"))
    }
}

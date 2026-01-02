package dev.logward.sdk.exceptions

import org.junit.jupiter.api.Test
import kotlin.test.*

/**
 * Unit tests for LogWard SDK exceptions
 */
class ExceptionTest {

    // ==================== LogWardException Tests ====================

    @Test
    fun `LogWardException should store message`() {
        val exception = LogWardException("Test error message")
        assertEquals("Test error message", exception.message)
        assertNull(exception.cause)
    }

    @Test
    fun `LogWardException should store message and cause`() {
        val cause = RuntimeException("Root cause")
        val exception = LogWardException("Test error message", cause)

        assertEquals("Test error message", exception.message)
        assertEquals(cause, exception.cause)
    }

    @Test
    fun `LogWardException should extend Exception`() {
        val exception = LogWardException("Test message")
        assertTrue(exception is Exception)
    }

    @Test
    fun `LogWardException should be throwable and catchable`() {
        val caught = assertFailsWith<LogWardException> {
            throw LogWardException("Test exception")
        }
        assertEquals("Test exception", caught.message)
    }

    // ==================== CircuitBreakerOpenException Tests ====================

    @Test
    fun `CircuitBreakerOpenException should have default message`() {
        val exception = CircuitBreakerOpenException()
        assertEquals("Circuit breaker is OPEN - requests are blocked", exception.message)
    }

    @Test
    fun `CircuitBreakerOpenException should accept custom message`() {
        val exception = CircuitBreakerOpenException("Custom circuit breaker message")
        assertEquals("Custom circuit breaker message", exception.message)
    }

    @Test
    fun `CircuitBreakerOpenException should extend LogWardException`() {
        val exception = CircuitBreakerOpenException()
        assertTrue(exception is LogWardException)
        assertTrue(exception is Exception)
    }

    @Test
    fun `CircuitBreakerOpenException should be catchable as LogWardException`() {
        val caught = assertFailsWith<LogWardException> {
            throw CircuitBreakerOpenException()
        }
        assertTrue(caught is CircuitBreakerOpenException)
    }

    // ==================== BufferFullException Tests ====================

    @Test
    fun `BufferFullException should have default message`() {
        val exception = BufferFullException()
        assertEquals("Log buffer is full - log entry dropped", exception.message)
    }

    @Test
    fun `BufferFullException should accept custom message`() {
        val exception = BufferFullException("Buffer overflow with 10000 entries")
        assertEquals("Buffer overflow with 10000 entries", exception.message)
    }

    @Test
    fun `BufferFullException should extend LogWardException`() {
        val exception = BufferFullException()
        assertTrue(exception is LogWardException)
        assertTrue(exception is Exception)
    }

    @Test
    fun `BufferFullException should be catchable as LogWardException`() {
        val caught = assertFailsWith<LogWardException> {
            throw BufferFullException()
        }
        assertTrue(caught is BufferFullException)
    }

    // ==================== Exception Hierarchy Tests ====================

    @Test
    fun `all exceptions should be part of the same hierarchy`() {
        val logWardException = LogWardException("test")
        val circuitBreakerException = CircuitBreakerOpenException()
        val bufferFullException = BufferFullException()

        // All should be catchable as Exception
        assertTrue(logWardException is Exception)
        assertTrue(circuitBreakerException is Exception)
        assertTrue(bufferFullException is Exception)

        // Specific exceptions should be catchable as LogWardException
        assertTrue(circuitBreakerException is LogWardException)
        assertTrue(bufferFullException is LogWardException)
    }

    @Test
    fun `exceptions can be caught with specific handler`() {
        fun throwException(type: Int) {
            when (type) {
                1 -> throw CircuitBreakerOpenException()
                2 -> throw BufferFullException()
                else -> throw LogWardException("Generic error")
            }
        }

        var circuitBreakerCaught = false
        var bufferFullCaught = false

        try {
            throwException(1)
        } catch (e: CircuitBreakerOpenException) {
            circuitBreakerCaught = true
        } catch (e: LogWardException) {
            fail("Should have caught CircuitBreakerOpenException")
        }

        try {
            throwException(2)
        } catch (e: BufferFullException) {
            bufferFullCaught = true
        } catch (e: LogWardException) {
            fail("Should have caught BufferFullException")
        }

        assertTrue(circuitBreakerCaught)
        assertTrue(bufferFullCaught)
    }

    @Test
    fun `exceptions should preserve stack trace`() {
        val exception = LogWardException("Test")
        val stackTrace = exception.stackTrace
        assertTrue(stackTrace.isNotEmpty())
        assertTrue(stackTrace.any { it.className.contains("ExceptionTest") })
    }

    @Test
    fun `exceptions with cause should have correct cause chain`() {
        val rootCause = IllegalArgumentException("Invalid argument")
        val cause = RuntimeException("Intermediate error", rootCause)
        val exception = LogWardException("Top level error", cause)

        assertEquals("Top level error", exception.message)
        assertEquals("Intermediate error", exception.cause?.message)
        assertEquals("Invalid argument", exception.cause?.cause?.message)
    }
}

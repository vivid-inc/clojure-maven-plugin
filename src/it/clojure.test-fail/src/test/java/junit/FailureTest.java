package junit;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Demonstrates surefire's behavior with JUnit, useful for comparing and contrasting
 * behavior and console output with that of vivid:clojure-maven-plugin:clojure.test
 */
class FailureTest {

    @Test
    void fail() {
        // JUnit is expected to mark this test as having failed.
        assertTrue(false);
    }

    @Test
    void error() {
        // This exception is not expected by JUnit;
        // JUnit is expected to mark this test as being in error.
        throw new IllegalStateException();
    }

    @Test
    @Disabled
    void disabled() {
        assertTrue(false);
    }

}

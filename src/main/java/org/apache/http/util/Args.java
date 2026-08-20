package org.apache.http.util;

/** Minimal compatibility shim for JackFredLib's argument validation helpers. */
public final class Args {
    private Args() {}

    public static void check(boolean condition, String message) {
        if (!condition) throw new IllegalArgumentException(message);
    }

    public static int notNegative(int value, String name) {
        if (value < 0) throw new IllegalArgumentException(name + " may not be negative");
        return value;
    }

    public static int positive(int value, String name) {
        if (value <= 0) throw new IllegalArgumentException(name + " must be positive");
        return value;
    }
}

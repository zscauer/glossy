package com.github.zscauer.glsy.security;

/**
 * Thrown if access cannot be granted.
 */
public class GrantAccessException extends RuntimeException {

    public GrantAccessException(final String message) {
        super(message);
    }

}

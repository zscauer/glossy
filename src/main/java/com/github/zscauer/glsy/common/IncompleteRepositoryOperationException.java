package com.github.zscauer.glsy.common;

/**
 * Thrown if repository operation wasn't complete successfully.
 */
// TODO: inject to all repository methods
public class IncompleteRepositoryOperationException extends RuntimeException {

    public IncompleteRepositoryOperationException() {
        super();
    }

    public IncompleteRepositoryOperationException(final String message) {
        super(message);
    }

}

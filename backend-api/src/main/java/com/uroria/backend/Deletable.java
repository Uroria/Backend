package com.uroria.backend;

/**
 * Represents an Object whose instances can be "deleted" (or better made useless).
 */
public interface Deletable {

    /**
     * This will completely delete everything from database and every other instance.
     * Please check out {@link Deletable#isDeleted()}.
     */
    void delete();

    /**
     * If this is true, every other method will throw an exception if you call it.
     */
    boolean isDeleted();

    /**
     * You can check if a deletable object has been deleted and if so throw an exception.
     * Usually used in implementations, but you can use this method if you want to.
     *
     * @throws IllegalStateException If the object has been deleted.
     */
    static void checkDeleted(Deletable deletable) throws IllegalStateException {
        if (!deletable.isDeleted()) return;
        throw new IllegalStateException("Deletable object has been deleted. Methods are not callable anymore.");
    }
}

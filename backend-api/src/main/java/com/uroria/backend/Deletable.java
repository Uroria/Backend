package com.uroria.backend;

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

    static void checkDeleted(Deletable deletable) {
        if (!deletable.isDeleted()) return;
        throw new IllegalStateException("Deletable object has been deleted. Methods are not callable anymore.");
    }
}

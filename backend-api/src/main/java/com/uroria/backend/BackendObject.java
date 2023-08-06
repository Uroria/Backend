package com.uroria.backend;

public abstract class BackendObject<T> {
    protected boolean deleted;

    public abstract void modify(T obj);

    public final boolean isDeleted() {
        return this.deleted;
    }

    public void delete() {
        this.deleted = true;
    }

    /**
     * Sends the updated object to every other wrapper of the backend.
     */
    public abstract void update();
}

package com.uroria.backend.permission;

public interface PermGroup extends Permissible {

    String getName();

    int getPriority();
}

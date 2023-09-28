package com.uroria.backend.permission;

import com.uroria.backend.Deletable;

public interface PermGroup extends Permissible, Deletable {

    String getName();

    int getPriority();
}

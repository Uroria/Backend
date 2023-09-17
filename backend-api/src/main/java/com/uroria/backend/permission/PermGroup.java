package com.uroria.backend.permission;

import com.uroria.backend.PropertyHolder;

public interface PermGroup extends Permissible, PropertyHolder {

    String getName();

    int getPriority();
}

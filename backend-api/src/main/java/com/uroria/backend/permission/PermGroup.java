package com.uroria.backend.permission;

import com.uroria.backend.Deletable;
import com.uroria.base.property.PropertyObject;

public interface PermGroup extends Permissible, Deletable, PropertyObject {

    String getName();

    int getPriority();

    void setPriority(int priority);
}

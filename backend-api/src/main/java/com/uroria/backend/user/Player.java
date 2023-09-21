package com.uroria.backend.user;

import com.uroria.backend.Deletable;
import com.uroria.backend.permission.PermHolder;
import com.uroria.backend.stats.StatHolder;
import com.uroria.base.property.PropertyObject;

import java.util.UUID;

public interface Player extends PermHolder, Deletable, StatHolder, PropertyObject {

    UUID getUniqueId();

}

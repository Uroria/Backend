package com.uroria.backend.permission;

import java.util.List;

public interface PermHolder extends Permissible {

    List<PermGroup> getPermGroups();
}

package com.uroria.backend.permission;

import lombok.NonNull;

import java.util.List;

public interface PermHolder extends Permissible {

    List<PermGroup> getPermGroups();

    void addGroup(@NonNull PermGroup group);

    void removeGroup(PermGroup group);

    void removeGroup(String groupName);
}

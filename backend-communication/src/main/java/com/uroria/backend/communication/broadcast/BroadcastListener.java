package com.uroria.backend.communication.broadcast;

public abstract class BroadcastListener<T extends Broadcast> {

    protected abstract void onBroadcast(T broadcast);

}

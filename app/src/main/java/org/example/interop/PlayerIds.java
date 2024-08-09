package org.example.interop;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;

public class PlayerIds {

    private volatile int nextId;

    private static final VarHandle NEXT_ID_VH;

    static {
        try {
            NEXT_ID_VH = MethodHandles.lookup().findVarHandle(PlayerIds.class, "nextId", int.class);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    public PlayerIds(int initialValue) {
        this.nextId = initialValue;
    }

    public int getNextId() {
        return nextId;
    }

    public int getNextIdAndIncrement() {
        return (int) NEXT_ID_VH.getAndAdd(this, 1);
    }
}

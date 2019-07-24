package cn.stormbirds.stormtimer;

import java.nio.ByteBuffer;

interface Cleaner {

    /**
     * Free a direct {@link ByteBuffer} if possible
     */
    void freeDirectBuffer(ByteBuffer buffer);
}
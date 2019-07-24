package cn.stormbirds.stormtimer;

public interface ResourceLeakHint {
    /**
     * Returns a human-readable message that potentially enables easier resource leak tracking.
     */
    String toHintString();
}
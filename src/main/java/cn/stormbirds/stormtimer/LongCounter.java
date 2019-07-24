package cn.stormbirds.stormtimer;

public interface LongCounter {
    void add(long delta);
    void increment();
    void decrement();
    long value();
}
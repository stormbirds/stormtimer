package cn.stormbirds.stormtimer;

import java.util.concurrent.atomic.LongAdder;

/**
 *
 * <p> LongAdderCounter.java
 * </p>
 * @author StormBirds Email：xbaojun@gmail.com
 * @since 2019/7/24 17:48
 *
 */
final class LongAdderCounter extends LongAdder implements LongCounter {

    @Override
    public long value() {
        return longValue();
    }
}

package cn.stormbirds.stormtimer;

/**
 *
 * <p> 时间比较工具
 * </p>
 * @author StormBirds Email：xbaojun@gmail.com
 * @since 2019/7/24 17:46
 *
 */
public final class ConstantTimeUtils {
    private ConstantTimeUtils() { }

    /**
     * 比较两个 {@code int}s 的值但不会导致计时器时间的损耗（使用位操作）.
     * <p>
     * {@code int} return type is intentional and is designed to allow cascading of constant time operations:
     * <pre>
     *     int v1 = 1;
     *     int v1 = 1;
     *     int v1 = 1;
     *     int v1 = 500;
     *     boolean equals = (equalsConstantTime(l1, l2) & equalsConstantTime(l3, l4)) != 0;
     * </pre>
     * @param x 第一个值.
     * @param y 第二个值.
     * @return {@code 0} 不相等. {@code 1} 相等.
     */
    private static int equalsConstantTime(int x, int y) {
        int z = ~(x ^ y);
        z &= z >> 16;
        z &= z >> 8;
        z &= z >> 4;
        z &= z >> 2;
        z &= z >> 1;
        return z & 1;
    }

    /**
     * Compare two {@code longs}s without leaking timing information.
     * <p>
     * The {@code int} return type is intentional and is designed to allow cascading of constant time operations:
     * <pre>
     *     long v1 = 1;
     *     long v1 = 1;
     *     long v1 = 1;
     *     long v1 = 500;
     *     boolean equals = (equalsConstantTime(l1, l2) & equalsConstantTime(l3, l4)) != 0;
     * </pre>
     * @param x the first value.
     * @param y the second value.
     * @return {@code 0} if not equal. {@code 1} if equal.
     */
    public static int equalsConstantTime(long x, long y) {
        return equalsNumber(x, y);
    }

    static int equalsNumber(long x, long y) {
        long z = ~(x ^ y);
        z &= z >> 32;
        z &= z >> 16;
        z &= z >> 8;
        z &= z >> 4;
        z &= z >> 2;
        z &= z >> 1;
        return (int) (z & 1);
    }

    /**
     * Compare two {@code byte} arrays for equality without leaking timing information.
     * For performance reasons no bounds checking on the parameters is performed.
     * <p>
     * The {@code int} return type is intentional and is designed to allow cascading of constant time operations:
     * <pre>
     *     byte[] s1 = new {1, 2, 3};
     *     byte[] s2 = new {1, 2, 3};
     *     byte[] s3 = new {1, 2, 3};
     *     byte[] s4 = new {4, 5, 6};
     *     boolean equals = (equalsConstantTime(s1, 0, s2, 0, s1.length) &
     *                       equalsConstantTime(s3, 0, s4, 0, s3.length)) != 0;
     * </pre>
     * @param bytes1 the first byte array.
     * @param startPos1 the position (inclusive) to start comparing in {@code bytes1}.
     * @param bytes2 the second byte array.
     * @param startPos2 the position (inclusive) to start comparing in {@code bytes2}.
     * @param length the amount of bytes to compare. This is assumed to be validated as not going out of bounds
     * by the caller.
     * @return {@code 0} if not equal. {@code 1} if equal.
     */
    public static int equalsConstantTime(byte[] bytes1, int startPos1,
                                         byte[] bytes2, int startPos2, int length) {
        // Benchmarking demonstrates that using an int to accumulate is faster than other data types.
        int b = 0;
        final int end = startPos1 + length;
        for (; startPos1 < end; ++startPos1, ++startPos2) {
            b |= bytes1[startPos1] ^ bytes2[startPos2];
        }
        return equalsConstantTime(b, 0);
    }

    /**
     * Compare two {@link CharSequence} objects without leaking timing information.
     * <p>
     * The {@code int} return type is intentional and is designed to allow cascading of constant time operations:
     * <pre>
     *     String s1 = "foo";
     *     String s2 = "foo";
     *     String s3 = "foo";
     *     String s4 = "goo";
     *     boolean equals = (equalsConstantTime(s1, s2) & equalsConstantTime(s3, s4)) != 0;
     * </pre>
     * @param s1 the first value.
     * @param s2 the second value.
     * @return {@code 0} if not equal. {@code 1} if equal.
     */
    public static int equalsConstantTime(CharSequence s1, CharSequence s2) {
        if (s1.length() != s2.length()) {
            return 0;
        }

        // Benchmarking demonstrates that using an int to accumulate is faster than other data types.
        int c = 0;
        for (int i = 0; i < s1.length(); ++i) {
            c |= s1.charAt(i) ^ s2.charAt(i);
        }
        return equalsConstantTime(c, 0);
    }
}

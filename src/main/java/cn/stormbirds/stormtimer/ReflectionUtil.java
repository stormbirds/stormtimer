package cn.stormbirds.stormtimer;

import java.lang.reflect.AccessibleObject;

/**
 *
 * <p> ReflectionUtil.java
 * </p>
 * @author StormBirds Email：xbaojun@gmail.com
 * @since 2019/7/24 17:55
 *
 */
public final class ReflectionUtil {

    private ReflectionUtil() { }

    /**
     * Try to call {@link AccessibleObject#setAccessible(boolean)} but will catch any {@link SecurityException} and
     * {@link java.lang.reflect} and return it.
     * The caller must check if it returns {@code null} and if not handle the returned exception.
     */
    public static Throwable trySetAccessible(AccessibleObject object, boolean checkAccessible) {
        if (checkAccessible && !PlatformDependent0.isExplicitTryReflectionSetAccessible()) {
            return new UnsupportedOperationException("Reflective setAccessible(true) disabled");
        }
        try {
            object.setAccessible(true);
            return null;
        } catch (SecurityException e) {
            return e;
        } catch (RuntimeException e) {
            return handleInaccessibleObjectException(e);
        }
    }

    private static RuntimeException handleInaccessibleObjectException(RuntimeException e) {
        // JDK 9 can throw an inaccessible object exception here; since Netty compiles
        // against JDK 7 and this exception was only added in JDK 9, we have to weakly
        // check the type
        if ("java.lang.reflect.InaccessibleObjectException".equals(e.getClass().getName())) {
            return e;
        }
        throw e;
    }
}
package cn.stormbirds.stormtimer;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.logging.Level;
import java.util.logging.Logger;

final class CleanerJava9 implements Cleaner {
    private static final Logger logger = JdkLoggerFactory.newInstance("CleanerJava9");

    private static final Method INVOKE_CLEANER;

    static {
        final Method method;
        final Throwable error;
        if (PlatformDependent0.hasUnsafe()) {
            final ByteBuffer buffer = ByteBuffer.allocateDirect(1);
            Object maybeInvokeMethod = AccessController.doPrivileged(new PrivilegedAction<Object>() {
                @Override
                public Object run() {
                    try {
                        // See https://bugs.openjdk.java.net/browse/JDK-8171377
                        Method m = PlatformDependent0.UNSAFE.getClass().getDeclaredMethod(
                                "invokeCleaner", ByteBuffer.class);
                        m.invoke(PlatformDependent0.UNSAFE, buffer);
                        return m;
                    } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                        return e;
                    }
                }
            });

            if (maybeInvokeMethod instanceof Throwable) {
                method = null;
                error = (Throwable) maybeInvokeMethod;
            } else {
                method = (Method) maybeInvokeMethod;
                error = null;
            }
        } else {
            method = null;
            error = new UnsupportedOperationException("sun.misc.Unsafe unavailable");
        }
        if (error == null) {
            debug("java.nio.ByteBuffer.cleaner(): available");
        } else {
            debug("java.nio.ByteBuffer.cleaner(): unavailable", error);
        }
        INVOKE_CLEANER = method;
    }

    static boolean isSupported() {
        return INVOKE_CLEANER != null;
    }

    @Override
    public void freeDirectBuffer(ByteBuffer buffer) {
        // Try to minimize overhead when there is no SecurityManager present.
        // See https://bugs.openjdk.java.net/browse/JDK-8191053.
        if (System.getSecurityManager() == null) {
            try {
                INVOKE_CLEANER.invoke(PlatformDependent0.UNSAFE, buffer);
            } catch (Throwable cause) {
                PlatformDependent0.throwException(cause);
            }
        } else {
            freeDirectBufferPrivileged(buffer);
        }
    }

    private static void freeDirectBufferPrivileged(final ByteBuffer buffer) {
        Exception error = AccessController.doPrivileged(new PrivilegedAction<Exception>() {
            @Override
            public Exception run() {
                try {
                    INVOKE_CLEANER.invoke(PlatformDependent0.UNSAFE, buffer);
                } catch (InvocationTargetException | IllegalAccessException e) {
                    return e;
                }
                return null;
            }
        });
        if (error != null) {
            PlatformDependent0.throwException(error);
        }
    }

    private static void debug(String msg){
        logger.log(Level.CONFIG,msg);
    }
    private static void debug(String msg, Object obj){
        logger.log(Level.CONFIG,msg,obj);
    }

}

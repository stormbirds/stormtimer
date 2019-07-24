package cn.stormbirds.stormtimer;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.logging.Level;
import java.util.logging.Logger;

final class CleanerJava6 implements Cleaner {
    private static final long CLEANER_FIELD_OFFSET;
    private static final Method CLEAN_METHOD;
    private static final Field CLEANER_FIELD;

    private static final Logger logger = JdkLoggerFactory.newInstance("CleanerJava6");

    static {
        long fieldOffset;
        Method clean;
        Field cleanerField;
        Throwable error = null;
        final ByteBuffer direct = ByteBuffer.allocateDirect(1);
        try {
            Object mayBeCleanerField = AccessController.doPrivileged(new PrivilegedAction<Object>() {
                @Override
                public Object run() {
                    try {
                        Field cleanerField =  direct.getClass().getDeclaredField("cleaner");
                        if (!PlatformDependent.hasUnsafe()) {
                            // We need to make it accessible if we do not use Unsafe as we will access it via
                            // reflection.
                            cleanerField.setAccessible(true);
                        }
                        return cleanerField;
                    } catch (Throwable cause) {
                        return cause;
                    }
                }
            });
            if (mayBeCleanerField instanceof Throwable) {
                throw (Throwable) mayBeCleanerField;
            }

            cleanerField = (Field) mayBeCleanerField;

            final Object cleaner;

            // If we have sun.misc.Unsafe we will use it as its faster then using reflection,
            // otherwise let us try reflection as last resort.
            if (PlatformDependent.hasUnsafe()) {
                fieldOffset = PlatformDependent0.objectFieldOffset(cleanerField);
                cleaner = PlatformDependent0.getObject(direct, fieldOffset);
            } else {
                fieldOffset = -1;
                cleaner = cleanerField.get(direct);
            }
            clean = cleaner.getClass().getDeclaredMethod("clean");
            clean.invoke(cleaner);
        } catch (Throwable t) {
            // We don't have ByteBuffer.cleaner().
            fieldOffset = -1;
            clean = null;
            error = t;
            cleanerField = null;
        }

        if (error == null) {
            debug("java.nio.ByteBuffer.cleaner(): available");
        } else {
            debug("java.nio.ByteBuffer.cleaner(): unavailable", error);
        }
        CLEANER_FIELD = cleanerField;
        CLEANER_FIELD_OFFSET = fieldOffset;
        CLEAN_METHOD = clean;
    }

    static boolean isSupported() {
        return CLEANER_FIELD_OFFSET != -1 || CLEANER_FIELD != null;
    }

    @Override
    public void freeDirectBuffer(ByteBuffer buffer) {
        if (!buffer.isDirect()) {
            return;
        }
        if (System.getSecurityManager() == null) {
            try {
                freeDirectBuffer0(buffer);
            } catch (Throwable cause) {
                PlatformDependent0.throwException(cause);
            }
        } else {
            freeDirectBufferPrivileged(buffer);
        }
    }

    private static void freeDirectBufferPrivileged(final ByteBuffer buffer) {
        Throwable cause = AccessController.doPrivileged(new PrivilegedAction<Throwable>() {
            @Override
            public Throwable run() {
                try {
                    freeDirectBuffer0(buffer);
                    return null;
                } catch (Throwable cause) {
                    return cause;
                }
            }
        });
        if (cause != null) {
            PlatformDependent0.throwException(cause);
        }
    }

    private static void freeDirectBuffer0(ByteBuffer buffer) throws Exception {
        final Object cleaner;
        // If CLEANER_FIELD_OFFSET == -1 we need to use reflection to access the cleaner, otherwise we can use
        // sun.misc.Unsafe.
        if (CLEANER_FIELD_OFFSET == -1) {
            cleaner = CLEANER_FIELD.get(buffer);
        } else {
            cleaner = PlatformDependent0.getObject(buffer, CLEANER_FIELD_OFFSET);
        }
        if (cleaner != null) {
            CLEAN_METHOD.invoke(cleaner);
        }
    }

    private static void debug(String msg){
        logger.log(Level.CONFIG,msg);
    }
    private static void debug(String msg, Object obj){
        logger.log(Level.CONFIG,msg,obj);
    }

}

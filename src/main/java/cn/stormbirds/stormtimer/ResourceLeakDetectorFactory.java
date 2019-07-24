package cn.stormbirds.stormtimer;

import java.lang.reflect.Constructor;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * <p> ResourceLeakDetectorFactory.java
 * </p>
 * @author StormBirds Email：xbaojun@gmail.com
 * @since 2019/7/24 17:56
 *
 */
public abstract class ResourceLeakDetectorFactory {
    private static final Logger logger = JdkLoggerFactory.newInstance("ResourceLeakDetectorFactory");

    private static volatile ResourceLeakDetectorFactory factoryInstance = new DefaultResourceLeakDetectorFactory();

    /**
     * Get the singleton instance of this factory class.
     *
     * @return the current {@link ResourceLeakDetectorFactory}
     */
    public static ResourceLeakDetectorFactory instance() {
        return factoryInstance;
    }

    /**
     * Set the factory's singleton instance. This has to be called before the static initializer of the
     * {@link ResourceLeakDetector} is called by all the callers of this factory. That is, before initializing a
     * Netty Bootstrap.
     *
     * @param factory the instance that will become the current {@link ResourceLeakDetectorFactory}'s singleton
     */
    public static void setResourceLeakDetectorFactory(ResourceLeakDetectorFactory factory) {
        factoryInstance = ObjectUtil.checkNotNull(factory, "factory");
    }

    /**
     * Returns a new instance of a {@link ResourceLeakDetector} with the given resource class.
     *
     * @param resource the resource class used to initialize the {@link ResourceLeakDetector}
     * @param <T> the type of the resource class
     * @return a new instance of {@link ResourceLeakDetector}
     */
    public final <T> ResourceLeakDetector<T> newResourceLeakDetector(Class<T> resource) {
        return newResourceLeakDetector(resource, ResourceLeakDetector.SAMPLING_INTERVAL);
    }

    /**
     * @deprecated Use {@link #newResourceLeakDetector(Class, int)} instead.
     * <p>
     * Returns a new instance of a {@link ResourceLeakDetector} with the given resource class.
     *
     * @param resource the resource class used to initialize the {@link ResourceLeakDetector}
     * @param samplingInterval the interval on which sampling takes place
     * @param maxActive This is deprecated and will be ignored.
     * @param <T> the type of the resource class
     * @return a new instance of {@link ResourceLeakDetector}
     */
    @Deprecated
    public abstract <T> ResourceLeakDetector<T> newResourceLeakDetector(
            Class<T> resource, int samplingInterval, long maxActive);

    /**
     * Returns a new instance of a {@link ResourceLeakDetector} with the given resource class.
     *
     * @param resource the resource class used to initialize the {@link ResourceLeakDetector}
     * @param samplingInterval the interval on which sampling takes place
     * @param <T> the type of the resource class
     * @return a new instance of {@link ResourceLeakDetector}
     */
    @SuppressWarnings("deprecation")
    public <T> ResourceLeakDetector<T> newResourceLeakDetector(Class<T> resource, int samplingInterval) {
        return newResourceLeakDetector(resource, ResourceLeakDetector.SAMPLING_INTERVAL, Long.MAX_VALUE);
    }

    /**
     * Default implementation that loads custom leak detector via system property
     */
    private static final class DefaultResourceLeakDetectorFactory extends ResourceLeakDetectorFactory {
        private final Constructor<?> obsoleteCustomClassConstructor;
        private final Constructor<?> customClassConstructor;

        DefaultResourceLeakDetectorFactory() {
            String customLeakDetector;
            try {
                customLeakDetector = AccessController.doPrivileged(new PrivilegedAction<String>() {
                    @Override
                    public String run() {
                        return SystemPropertyUtil.get("io.netty.customResourceLeakDetector");
                    }
                });
            } catch (Throwable cause) {
                logger.log(Level.SEVERE, "Could not access System property: io.netty.customResourceLeakDetector", cause);
                customLeakDetector = null;
            }
            if (customLeakDetector == null) {
                obsoleteCustomClassConstructor = customClassConstructor = null;
            } else {
                obsoleteCustomClassConstructor = obsoleteCustomClassConstructor(customLeakDetector);
                customClassConstructor = customClassConstructor(customLeakDetector);
            }
        }

        private static Constructor<?> obsoleteCustomClassConstructor(String customLeakDetector) {
            try {
                final Class<?> detectorClass = Class.forName(customLeakDetector, true,
                        PlatformDependent.getSystemClassLoader());

                if (ResourceLeakDetector.class.isAssignableFrom(detectorClass)) {
                    return detectorClass.getConstructor(Class.class, int.class, long.class);
                } else {
                    error("Class {} does not inherit from ResourceLeakDetector.", customLeakDetector);
                }
            } catch (Throwable t) {
                logger.log(Level.SEVERE, String.format("Could not load custom resource leak detector class provided: {}",
                        customLeakDetector) ,t);
            }
            return null;
        }

        private static Constructor<?> customClassConstructor(String customLeakDetector) {
            try {
                final Class<?> detectorClass = Class.forName(customLeakDetector, true,
                        PlatformDependent.getSystemClassLoader());

                if (ResourceLeakDetector.class.isAssignableFrom(detectorClass)) {
                    return detectorClass.getConstructor(Class.class, int.class);
                } else {
                    error("Class {} does not inherit from ResourceLeakDetector.", customLeakDetector);
                }
            } catch (Throwable t) {
                logger.log(Level.SEVERE, String.format("Could not load custom resource leak detector class provided: {}",
                        customLeakDetector) ,t);
            }
            return null;
        }

        @SuppressWarnings("deprecation")
        @Override
        public <T> ResourceLeakDetector<T> newResourceLeakDetector(Class<T> resource, int samplingInterval,
                                                                   long maxActive) {
            if (obsoleteCustomClassConstructor != null) {
                try {
                    @SuppressWarnings("unchecked")
                    ResourceLeakDetector<T> leakDetector =
                            (ResourceLeakDetector<T>) obsoleteCustomClassConstructor.newInstance(
                                    resource, samplingInterval, maxActive);
                    debug("Loaded custom ResourceLeakDetector: {}",
                            obsoleteCustomClassConstructor.getDeclaringClass().getName());
                    return leakDetector;
                } catch (Throwable t) {
                    logger.log(Level.SEVERE, String.format("Could not load custom resource leak detector provided: {} with the given resource: {}",
                            customClassConstructor.getDeclaringClass().getName(),resource) ,t);
                }
            }

            ResourceLeakDetector<T> resourceLeakDetector = new ResourceLeakDetector<T>(resource, samplingInterval,
                                                                                       maxActive);
            debug("Loaded default ResourceLeakDetector: {}", resourceLeakDetector);
            return resourceLeakDetector;
        }

        @Override
        public <T> ResourceLeakDetector<T> newResourceLeakDetector(Class<T> resource, int samplingInterval) {
            if (customClassConstructor != null) {
                try {
                    @SuppressWarnings("unchecked")
                    ResourceLeakDetector<T> leakDetector =
                            (ResourceLeakDetector<T>) customClassConstructor.newInstance(resource, samplingInterval);
                    debug("Loaded custom ResourceLeakDetector: {}",
                            customClassConstructor.getDeclaringClass().getName());
                    return leakDetector;
                } catch (Throwable t) {
                    logger.log(Level.SEVERE, String.format("Could not load custom resource leak detector provided: {} with the given resource: {}",
                            customClassConstructor.getDeclaringClass().getName(),resource) ,t);
                }
            }

            ResourceLeakDetector<T> resourceLeakDetector = new ResourceLeakDetector<T>(resource, samplingInterval);
            debug("Loaded default ResourceLeakDetector: {}", resourceLeakDetector);
            return resourceLeakDetector;
        }
    }

    private static void debug(String msg){
        logger.log(Level.CONFIG,msg);
    }
    private static void debug(String msg, Object obj){
        logger.log(Level.CONFIG,msg,obj);
    }
    private static void debug(String msg,Object obj, Object throwable){
        logger.log(Level.CONFIG, String.format(msg,obj) ,throwable);
    }

    private static void error(String msg, String obj){
        logger.log(Level.SEVERE, String.format(msg,obj) );
    }
    private static void error(String msg, String obj, String obj2){
        logger.log(Level.SEVERE, String.format(msg,obj,obj2) );
    }
    private static void error(String... msg){
        logger.log(Level.SEVERE,"",msg);
    }
    private static void warn(String msg, Object obj){
        logger.log(Level.WARNING, msg,obj);
    }
}
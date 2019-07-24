package cn.stormbirds.stormtimer;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * <p>
 * stormtimer
 * </p>
 *
 * @author StormBirds Email：xbaojun@gmail.com
 * @since 2019/7/24 16:18
 */

public class JdkLoggerFactory {
    private static Level logLevel = Level.INFO;
    private static Map<String,Logger> loggerMap = new HashMap<>();
    public static Logger newInstance(String name){
        Logger logger = Logger.getLogger(name);
        logger.setLevel(logLevel);
        loggerMap.put(name,logger);
        return logger;
    }
    public static void setLogLevel(Level level){
        logLevel = level;
        if(!loggerMap.isEmpty()){
            loggerMap.values().forEach(logger -> logger.setLevel(level));
        }

    }
}

package org.example;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CustomLogger {
    private final Logger logger;
    private final String className;
    private boolean isLoggingEnabled = true;
    private static final String DEV_LOG_COLOR = "\u001B[33m"; // ANSI color for golden logs
    private static final String DEFAULT_COLOR = "\u001B[0m";
    public CustomLogger(Class<?> clazz) {
        this.logger = LoggerFactory.getLogger(clazz);
        this.className = clazz.getSimpleName();
    }

    public void setVisibility(boolean enabled) {
        isLoggingEnabled = enabled;
    }

    private String getCurrentTimestamp() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS"));
    }

    public void info(String message) {
        if (isLoggingEnabled && logger.isInfoEnabled()) {
            logger.info("{} {} - {}", getCurrentTimestamp(), className, message);
        }
    }

    public void info(String format, Object... arguments) {
        if (isLoggingEnabled && logger.isInfoEnabled()) {
            logger.info(format, arguments);
        }
    }

    public void dev(String message) {
        if (isLoggingEnabled && logger.isInfoEnabled()) {
            logger.info(DEV_LOG_COLOR + "{} {} - {}" + DEFAULT_COLOR, getCurrentTimestamp(), className, message);
        }
    }

    public void dev(String format, Object... arguments) {
        if (isLoggingEnabled && logger.isInfoEnabled()) {
            logger.info(DEV_LOG_COLOR + "{}" + DEFAULT_COLOR, format, arguments);
        }
    }

    public void debug(String message) {
        if (isLoggingEnabled && logger.isDebugEnabled()) {
            logger.debug("{} {} - {}", getCurrentTimestamp(), className, message);
        }
    }

    public void debug(String format, Object... arguments) {
        if (isLoggingEnabled && logger.isDebugEnabled()) {
            logger.debug("{} {}", prependArguments(arguments), format);
        }
    }

    public void warn(String message) {
        if (isLoggingEnabled && logger.isWarnEnabled()) {
            logger.warn("{} {} - {}", getCurrentTimestamp(), className, message);
        }
    }

    public void warn(String format, Object... arguments) {
        if (isLoggingEnabled && logger.isWarnEnabled()) {
            logger.warn("{} {}", prependArguments(arguments), format);
        }
    }

    public void error(String message) {
        logger.error("{} {} - {}", getCurrentTimestamp(), className, message);
    }

    public void error(String format, Object... arguments) {
        logger.error("{} {}", prependArguments(arguments), format);
    }
    public void critical(String message) {
        logger.error("[CRITICAL] {} {} - {}", getCurrentTimestamp(), className, message);
    }

    public void critical(String format, Object... arguments) {
        logger.error("[CRITICAL] {} {}", prependArguments(arguments), format);
    }
    public void printCrushLine(Exception e){
        for (StackTraceElement element : e.getStackTrace()) {
            if (element.getClassName().startsWith("org.example.Practice")) {
                logger.error("Crash at {}", element);
                break;
            }
        }
    }
    public StackTraceElement getCrushLine(Exception e){
        return Arrays.stream(e.getStackTrace()).filter(el->el.getClassName().startsWith("org.example.Practice")).findFirst().orElse(null);
    }
    private Object[] prependArguments(Object... arguments) {
        Object[] updatedArguments = new Object[arguments.length + 2];
        updatedArguments[0] = getCurrentTimestamp();
        updatedArguments[1] = className;
        System.arraycopy(arguments, 0, updatedArguments, 2, arguments.length);
        return updatedArguments;
    }
}

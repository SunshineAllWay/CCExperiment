package org.apache.log4j;
import org.apache.log4j.spi.LoggingEvent;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.Date;
import java.util.ResourceBundle;
import java.util.Locale;
public final class LogMF extends LogXF {
    private LogMF() {
    }
    private static NumberFormat numberFormat = null;
    private static Locale numberLocale = null;
    private static DateFormat dateFormat = null;
    private static Locale dateLocale = null;
    private static synchronized String formatNumber(final Object n) {
        Locale currentLocale = Locale.getDefault();
        if (currentLocale != numberLocale || numberFormat == null) {
            numberLocale = currentLocale;
            numberFormat = NumberFormat.getInstance(currentLocale);
        }
        return numberFormat.format(n);
    }
    private static synchronized String formatDate(final Object d) {
        Locale currentLocale = Locale.getDefault();
        if (currentLocale != dateLocale || dateFormat == null) {
            dateLocale = currentLocale;
            dateFormat = DateFormat.getDateTimeInstance(
                                DateFormat.SHORT,
                                DateFormat.SHORT,
                                currentLocale);
        }
        return dateFormat.format(d);
    }
    private static String formatObject(final Object arg0) {
        if (arg0 instanceof String) {
            return arg0.toString();
        } else if (arg0 instanceof Double ||
                   arg0 instanceof Float) {
           return formatNumber(arg0);
        } else if (arg0 instanceof Date) {
            return formatDate(arg0);
        }
        return String.valueOf(arg0);
    }
    private static boolean isSimple(final String pattern) {
        if (pattern.indexOf('\'') != -1) {
            return false;
        }
        for(int pos = pattern.indexOf('{');
            pos != -1;
            pos = pattern.indexOf('{', pos + 1)) {
            if (pos + 2 >= pattern.length() ||
                    pattern.charAt(pos+2) != '}' ||
                    pattern.charAt(pos+1) < '0' ||
                    pattern.charAt(pos+1) > '9') {
                return false;
            }
        }
        return true;
    }
    private static String format(final String pattern,
                                 final Object[] arguments) {
        if (pattern == null) {
            return null;
        } else if(isSimple(pattern)) {
            String formatted[] = new String[10];
            int prev = 0;
            String retval = "";
            int pos = pattern.indexOf('{');
            while(pos >= 0) {
                if(pos + 2 < pattern.length() && 
                      pattern.charAt(pos+2) == '}' &&
                      pattern.charAt(pos+1) >= '0' &&
                      pattern.charAt(pos+1) <= '9') {
                    int index = pattern.charAt(pos+1) - '0';
                    retval += pattern.substring(prev, pos);
                    if (formatted[index] == null) {
                         if (arguments == null || index >= arguments.length) {
                            formatted[index] = pattern.substring(pos, pos+3);
                         } else {
                            formatted[index] = formatObject(arguments[index]);
                         }
                    }
                    retval += formatted[index];
                    prev = pos + 3;
                    pos = pattern.indexOf('{', prev);
                } else {
                    pos = pattern.indexOf('{', pos + 1);
                }
            }
            retval += pattern.substring(prev);
            return retval;
        }
        try {
            return MessageFormat.format(pattern, arguments);
        } catch (IllegalArgumentException ex) {
            return pattern;
        }
    }
    private static String format(final String pattern,
                                 final Object arg0) {
        if (pattern == null) {
            return null;
        } else if(isSimple(pattern)) {
            String formatted = null;
            int prev = 0;
            String retval = "";
            int pos = pattern.indexOf('{');
            while(pos >= 0) {
                if(pos + 2 < pattern.length() &&
                      pattern.charAt(pos+2) == '}' &&
                      pattern.charAt(pos+1) >= '0' &&
                      pattern.charAt(pos+1) <= '9') {
                    int index = pattern.charAt(pos+1) - '0';
                    retval += pattern.substring(prev, pos);
                    if (index != 0) {
                        retval += pattern.substring(pos, pos+3);
                    } else {
                        if (formatted == null) {
                            formatted = formatObject(arg0);
                        }
                        retval += formatted;
                    }
                    prev = pos + 3;
                    pos = pattern.indexOf('{', prev);
                } else {
                    pos = pattern.indexOf('{', pos + 1);
                }
            }
            retval += pattern.substring(prev);
            return retval;
        }
        try {
            return MessageFormat.format(pattern, new Object[] { arg0 });
        } catch (IllegalArgumentException ex) {
            return pattern;
        }
    }
    private static String format(
            final String resourceBundleName,
            final String key,
            final Object[] arguments) {
        String pattern;
        if (resourceBundleName != null) {
            try {
                ResourceBundle bundle =
                        ResourceBundle.getBundle(resourceBundleName);
                pattern = bundle.getString(key);
            } catch (Exception ex) {
                pattern = key;
            }
        } else {
            pattern = key;
        }
        return format(pattern, arguments);
    }
    private static final String FQCN = LogMF.class.getName();
    private static void forcedLog(final Logger logger,
                                  final Level level,
                                  final String msg) {
        logger.callAppenders(new LoggingEvent(FQCN, logger, level, msg, null));
    }
    private static void forcedLog(final Logger logger,
                                  final Level level,
                                  final String msg,
                                  final Throwable t) {
        logger.callAppenders(new LoggingEvent(FQCN, logger, level, msg, t));
    }
    public static void trace(final Logger logger, final String pattern,
        final Object[] arguments) {
        if (logger.isEnabledFor(TRACE)) {
            forcedLog(logger, TRACE, format(pattern, arguments));
        }
    }
    public static void debug(final Logger logger, final String pattern,
        final Object[] arguments) {
        if (logger.isDebugEnabled()) {
            forcedLog(logger, Level.DEBUG, format(pattern, arguments));
        }
    }
    public static void info(final Logger logger, final String pattern,
        final Object[] arguments) {
        if (logger.isInfoEnabled()) {
            forcedLog(logger, Level.INFO, format(pattern, arguments));
        }
    }
    public static void warn(final Logger logger, final String pattern,
        final Object[] arguments) {
        if (logger.isEnabledFor(Level.WARN)) {
            forcedLog(logger, Level.WARN, format(pattern, arguments));
        }
    }
    public static void error(final Logger logger, final String pattern,
        final Object[] arguments) {
        if (logger.isEnabledFor(Level.ERROR)) {
            forcedLog(logger, Level.ERROR, format(pattern, arguments));
        }
    }
    public static void fatal(final Logger logger, final String pattern,
        final Object[] arguments) {
        if (logger.isEnabledFor(Level.FATAL)) {
            forcedLog(logger, Level.FATAL, format(pattern, arguments));
        }
    }
    public static void trace(final Logger logger,
                             final Throwable t,
                             final String pattern,
        final Object[] arguments) {
        if (logger.isEnabledFor(TRACE)) {
            forcedLog(logger, TRACE, format(pattern, arguments), t);
        }
    }
    public static void debug(final Logger logger,
                             final Throwable t,
                             final String pattern,
        final Object[] arguments) {
        if (logger.isDebugEnabled()) {
            forcedLog(logger, Level.DEBUG, format(pattern, arguments), t);
        }
    }
    public static void info(final Logger logger,
                            final Throwable t,
                            final String pattern,
        final Object[] arguments) {
        if (logger.isInfoEnabled()) {
            forcedLog(logger, Level.INFO, format(pattern, arguments), t);
        }
    }
    public static void warn(final Logger logger,
                            final Throwable t,
                            final String pattern,
        final Object[] arguments) {
        if (logger.isEnabledFor(Level.WARN)) {
            forcedLog(logger, Level.WARN, format(pattern, arguments), t);
        }
    }
    public static void error(final Logger logger,
                             final Throwable t,
                             final String pattern,
        final Object[] arguments) {
        if (logger.isEnabledFor(Level.ERROR)) {
            forcedLog(logger, Level.ERROR, format(pattern, arguments), t);
        }
    }
    public static void fatal(final Logger logger,
                             final Throwable t,
                             final String pattern,
        final Object[] arguments) {
        if (logger.isEnabledFor(Level.FATAL)) {
            forcedLog(logger, Level.FATAL, format(pattern, arguments), t);
        }
    }
    public static void trace(final Logger logger, final String pattern,
        final boolean argument) {
        if (logger.isEnabledFor(TRACE)) {
            forcedLog(logger, TRACE, format(pattern, valueOf(argument)));
        }
    }
    public static void trace(final Logger logger, final String pattern,
        final char argument) {
        if (logger.isEnabledFor(TRACE)) {
            forcedLog(logger, TRACE, format(pattern, valueOf(argument)));
        }
    }
    public static void trace(final Logger logger, final String pattern,
        final byte argument) {
        if (logger.isEnabledFor(TRACE)) {
            forcedLog(logger, TRACE, format(pattern, valueOf(argument)));
        }
    }
    public static void trace(final Logger logger, final String pattern,
        final short argument) {
        if (logger.isEnabledFor(TRACE)) {
            forcedLog(logger, TRACE, format(pattern, valueOf(argument)));
        }
    }
    public static void trace(final Logger logger, final String pattern,
        final int argument) {
        if (logger.isEnabledFor(TRACE)) {
            forcedLog(logger, TRACE, format(pattern, valueOf(argument)));
        }
    }
    public static void trace(final Logger logger, final String pattern,
        final long argument) {
        if (logger.isEnabledFor(TRACE)) {
            forcedLog(logger, TRACE, format(pattern, valueOf(argument)));
        }
    }
    public static void trace(final Logger logger, final String pattern,
        final float argument) {
        if (logger.isEnabledFor(TRACE)) {
            forcedLog(logger, TRACE, format(pattern, valueOf(argument)));
        }
    }
    public static void trace(final Logger logger, final String pattern,
        final double argument) {
        if (logger.isEnabledFor(TRACE)) {
            forcedLog(logger, TRACE, format(pattern, valueOf(argument)));
        }
    }
    public static void trace(final Logger logger, final String pattern,
        final Object argument) {
        if (logger.isEnabledFor(TRACE)) {
            forcedLog(logger, TRACE, format(pattern, argument));
        }
    }
    public static void trace(final Logger logger, final String pattern,
        final Object arg0, final Object arg1) {
        if (logger.isEnabledFor(TRACE)) {
            forcedLog(logger, TRACE,
                    format(pattern, toArray(arg0, arg1)));
        }
    }
    public static void trace(final Logger logger, final String pattern,
        final Object arg0, final Object arg1, final Object arg2) {
        if (logger.isEnabledFor(TRACE)) {
            forcedLog(logger, TRACE,
                    format(pattern, toArray(arg0, arg1, arg2)));
        }
    }
    public static void trace(final Logger logger, final String pattern,
        final Object arg0, final Object arg1, final Object arg2,
        final Object arg3) {
        if (logger.isEnabledFor(TRACE)) {
            forcedLog(logger, TRACE,
                    format(pattern, toArray(arg0, arg1, arg2, arg3)));
        }
    }
    public static void debug(final Logger logger, final String pattern,
        final boolean argument) {
        if (logger.isDebugEnabled()) {
            forcedLog(logger, Level.DEBUG, format(pattern, valueOf(argument)));
        }
    }
    public static void debug(final Logger logger, final String pattern,
        final char argument) {
        if (logger.isDebugEnabled()) {
            forcedLog(logger, Level.DEBUG, format(pattern, valueOf(argument)));
        }
    }
    public static void debug(final Logger logger, final String pattern,
        final byte argument) {
        if (logger.isDebugEnabled()) {
            forcedLog(logger, Level.DEBUG, format(pattern, valueOf(argument)));
        }
    }
    public static void debug(final Logger logger, final String pattern,
        final short argument) {
        if (logger.isDebugEnabled()) {
            forcedLog(logger, Level.DEBUG, format(pattern, valueOf(argument)));
        }
    }
    public static void debug(final Logger logger, final String pattern,
        final int argument) {
        if (logger.isDebugEnabled()) {
            forcedLog(logger, Level.DEBUG, format(pattern, valueOf(argument)));
        }
    }
    public static void debug(final Logger logger, final String pattern,
        final long argument) {
        if (logger.isDebugEnabled()) {
            forcedLog(logger, Level.DEBUG, format(pattern, valueOf(argument)));
        }
    }
    public static void debug(final Logger logger, final String pattern,
        final float argument) {
        if (logger.isDebugEnabled()) {
            forcedLog(logger, Level.DEBUG, format(pattern, valueOf(argument)));
        }
    }
    public static void debug(final Logger logger, final String pattern,
        final double argument) {
        if (logger.isDebugEnabled()) {
            forcedLog(logger, Level.DEBUG, format(pattern, valueOf(argument)));
        }
    }
    public static void debug(final Logger logger, final String pattern,
        final Object argument) {
        if (logger.isDebugEnabled()) {
            forcedLog(logger, Level.DEBUG, format(pattern, argument));
        }
    }
    public static void debug(final Logger logger, final String pattern,
        final Object arg0, final Object arg1) {
        if (logger.isDebugEnabled()) {
            forcedLog(logger, Level.DEBUG,
                    format(pattern, toArray(arg0, arg1)));
        }
    }
    public static void debug(final Logger logger, final String pattern,
        final Object arg0, final Object arg1, final Object arg2) {
        if (logger.isDebugEnabled()) {
            forcedLog(logger, Level.DEBUG,
                    format(pattern, toArray(arg0, arg1, arg2)));
        }
    }
    public static void debug(final Logger logger, final String pattern,
        final Object arg0, final Object arg1, final Object arg2,
        final Object arg3) {
        if (logger.isDebugEnabled()) {
            forcedLog(logger, Level.DEBUG,
                    format(pattern, toArray(arg0, arg1, arg2, arg3)));
        }
    }
    public static void info(final Logger logger, final String pattern,
        final boolean argument) {
        if (logger.isInfoEnabled()) {
            forcedLog(logger, Level.INFO, format(pattern, valueOf(argument)));
        }
    }
    public static void info(final Logger logger, final String pattern,
        final char argument) {
        if (logger.isInfoEnabled()) {
            forcedLog(logger, Level.INFO, format(pattern, valueOf(argument)));
        }
    }
    public static void info(final Logger logger, final String pattern,
        final byte argument) {
        if (logger.isInfoEnabled()) {
            forcedLog(logger, Level.INFO, format(pattern, valueOf(argument)));
        }
    }
    public static void info(final Logger logger, final String pattern,
        final short argument) {
        if (logger.isInfoEnabled()) {
            forcedLog(logger, Level.INFO, format(pattern, valueOf(argument)));
        }
    }
    public static void info(final Logger logger, final String pattern,
        final int argument) {
        if (logger.isInfoEnabled()) {
            forcedLog(logger, Level.INFO, format(pattern, valueOf(argument)));
        }
    }
    public static void info(final Logger logger, final String pattern,
        final long argument) {
        if (logger.isInfoEnabled()) {
            forcedLog(logger, Level.INFO, format(pattern, valueOf(argument)));
        }
    }
    public static void info(final Logger logger, final String pattern,
        final float argument) {
        if (logger.isInfoEnabled()) {
            forcedLog(logger, Level.INFO, format(pattern, valueOf(argument)));
        }
    }
    public static void info(final Logger logger, final String pattern,
        final double argument) {
        if (logger.isInfoEnabled()) {
            forcedLog(logger, Level.INFO, format(pattern, valueOf(argument)));
        }
    }
    public static void info(final Logger logger, final String pattern,
        final Object argument) {
        if (logger.isInfoEnabled()) {
            forcedLog(logger, Level.INFO, format(pattern, argument));
        }
    }
    public static void info(final Logger logger, final String pattern,
        final Object arg0, final Object arg1) {
        if (logger.isInfoEnabled()) {
            forcedLog(logger, Level.INFO, format(pattern, toArray(arg0, arg1)));
        }
    }
    public static void info(final Logger logger, final String pattern,
        final Object arg0, final Object arg1, final Object arg2) {
        if (logger.isInfoEnabled()) {
            forcedLog(logger, Level.INFO, format(pattern,
                    toArray(arg0, arg1, arg2)));
        }
    }
    public static void info(final Logger logger, final String pattern,
        final Object arg0, final Object arg1, final Object arg2,
        final Object arg3) {
        if (logger.isInfoEnabled()) {
            forcedLog(logger, Level.INFO, format(pattern,
                    toArray(arg0, arg1, arg2, arg3)));
        }
    }
    public static void warn(final Logger logger, final String pattern,
        final boolean argument) {
        if (logger.isEnabledFor(Level.WARN)) {
            forcedLog(logger, Level.WARN, format(pattern, valueOf(argument)));
        }
    }
    public static void warn(final Logger logger, final String pattern,
        final char argument) {
        if (logger.isEnabledFor(Level.WARN)) {
            forcedLog(logger, Level.WARN, format(pattern, valueOf(argument)));
        }
    }
    public static void warn(final Logger logger, final String pattern,
        final byte argument) {
        if (logger.isEnabledFor(Level.WARN)) {
            forcedLog(logger, Level.WARN, format(pattern, valueOf(argument)));
        }
    }
    public static void warn(final Logger logger, final String pattern,
        final short argument) {
        if (logger.isEnabledFor(Level.WARN)) {
            forcedLog(logger, Level.WARN, format(pattern, valueOf(argument)));
        }
    }
    public static void warn(final Logger logger, final String pattern,
        final int argument) {
        if (logger.isEnabledFor(Level.WARN)) {
            forcedLog(logger, Level.WARN, format(pattern, valueOf(argument)));
        }
    }
    public static void warn(final Logger logger, final String pattern,
        final long argument) {
        if (logger.isEnabledFor(Level.WARN)) {
            forcedLog(logger, Level.WARN, format(pattern, valueOf(argument)));
        }
    }
    public static void warn(final Logger logger, final String pattern,
        final float argument) {
        if (logger.isEnabledFor(Level.WARN)) {
            forcedLog(logger, Level.WARN, format(pattern, valueOf(argument)));
        }
    }
    public static void warn(final Logger logger, final String pattern,
        final double argument) {
        if (logger.isEnabledFor(Level.WARN)) {
            forcedLog(logger, Level.WARN, format(pattern, valueOf(argument)));
        }
    }
    public static void warn(final Logger logger, final String pattern,
        final Object argument) {
        if (logger.isEnabledFor(Level.WARN)) {
            forcedLog(logger, Level.WARN, format(pattern, argument));
        }
    }
    public static void warn(final Logger logger, final String pattern,
        final Object arg0, final Object arg1) {
        if (logger.isEnabledFor(Level.WARN)) {
            forcedLog(logger, Level.WARN,
                    format(pattern, toArray(arg0, arg1)));
        }
    }
    public static void warn(final Logger logger, final String pattern,
        final Object arg0, final Object arg1, final Object arg2) {
        if (logger.isEnabledFor(Level.WARN)) {
            forcedLog(logger, Level.WARN,
                    format(pattern, toArray(arg0, arg1, arg2)));
        }
    }
    public static void warn(final Logger logger, final String pattern,
        final Object arg0, final Object arg1, final Object arg2,
        final Object arg3) {
        if (logger.isEnabledFor(Level.WARN)) {
            forcedLog(logger, Level.WARN, format(pattern,
                    toArray(arg0, arg1, arg2, arg3)));
        }
    }
    public static void log(final Logger logger,
                             final Level level,
                             final String pattern,
                             final Object[] parameters) {
        if (logger.isEnabledFor(level)) {
            forcedLog(logger, level,
                    format(pattern, parameters));
        }
    }
    public static void log(final Logger logger,
                             final Level level,
                             final Throwable t,
                             final String pattern,
                             final Object[] parameters) {
        if (logger.isEnabledFor(level)) {
            forcedLog(logger, level,
                    format(pattern, parameters), t);
        }
    }
    public static void log(final Logger logger,
                             final Level level,
                             final String pattern,
                             final Object param1) {
        if (logger.isEnabledFor(level)) {
            forcedLog(logger, level,
                    format(pattern, toArray(param1)));
        }
    }
    public static void log(final Logger logger,
                             final Level level,
                             final String pattern,
                             final boolean param1) {
        if (logger.isEnabledFor(level)) {
            forcedLog(logger, level,
                    format(pattern, toArray(valueOf(param1))));
        }
    }
    public static void log(final Logger logger,
                             final Level level,
                             final String pattern,
                             final byte param1) {
        if (logger.isEnabledFor(level)) {
            forcedLog(logger, level,
                    format(pattern, toArray(valueOf(param1))));
        }
    }
    public static void log(final Logger logger,
                             final Level level,
                             final String pattern,
                             final char param1) {
        if (logger.isEnabledFor(level)) {
            forcedLog(logger, level,
                    format(pattern, toArray(valueOf(param1))));
        }
    }
    public static void log(final Logger logger,
                             final Level level,
                             final String pattern,
                             final short param1) {
        if (logger.isEnabledFor(level)) {
            forcedLog(logger, level,
                    format(pattern, toArray(valueOf(param1))));
        }
    }
    public static void log(final Logger logger,
                             final Level level,
                             final String pattern,
                             final int param1) {
        if (logger.isEnabledFor(level)) {
            forcedLog(logger, level,
                    format(pattern, toArray(valueOf(param1))));
        }
    }
    public static void log(final Logger logger,
                             final Level level,
                             final String pattern,
                             final long param1) {
        if (logger.isEnabledFor(level)) {
            forcedLog(logger, level,
                    format(pattern, toArray(valueOf(param1))));
        }
    }
    public static void log(final Logger logger,
                             final Level level,
                             final String pattern,
                             final float param1) {
        if (logger.isEnabledFor(level)) {
            forcedLog(logger, level,
                    format(pattern, toArray(valueOf(param1))));
        }
    }
    public static void log(final Logger logger,
                             final Level level,
                             final String pattern,
                             final double param1) {
        if (logger.isEnabledFor(level)) {
            forcedLog(logger, level,
                    format(pattern, toArray(valueOf(param1))));
        }
    }
    public static void log(final Logger logger,
                            final Level level,
                            final String pattern,
        final Object arg0, final Object arg1) {
        if (logger.isEnabledFor(level)) {
            forcedLog(logger, level,
                    format(pattern, toArray(arg0, arg1)));
        }
    }
    public static void log(final Logger logger,
                           final Level level,
                           final String pattern,
        final Object arg0, final Object arg1, final Object arg2) {
        if (logger.isEnabledFor(level)) {
            forcedLog(logger, level,
                    format(pattern, toArray(arg0, arg1, arg2)));
        }
    }
    public static void log(final Logger logger,
                           final Level level,
                           final String pattern,
        final Object arg0, final Object arg1, final Object arg2,
        final Object arg3) {
        if (logger.isEnabledFor(level)) {
            forcedLog(logger, level, format(pattern,
                    toArray(arg0, arg1, arg2, arg3)));
        }
    }
    public static void logrb(final Logger logger,
                             final Level level,
                             final String bundleName,
                             final String key,
                             final Object[] parameters) {
        if (logger.isEnabledFor(level)) {
            forcedLog(logger, level,
                    format(bundleName, key, parameters));
        }
    }
    public static void logrb(final Logger logger,
                             final Level level,
                             final Throwable t,
                             final String bundleName,
                             final String key,
                             final Object[] parameters) {
        if (logger.isEnabledFor(level)) {
            forcedLog(logger, level,
                    format(bundleName, key, parameters), t);
        }
    }
    public static void logrb(final Logger logger,
                             final Level level,
                             final String bundleName,
                             final String key,
                             final Object param1) {
        if (logger.isEnabledFor(level)) {
            forcedLog(logger, level,
                    format(bundleName, key, toArray(param1)));
        }
    }
    public static void logrb(final Logger logger,
                             final Level level,
                             final String bundleName,
                             final String key,
                             final boolean param1) {
        if (logger.isEnabledFor(level)) {
            forcedLog(logger, level,
                    format(bundleName, key, toArray(valueOf(param1))));
        }
    }
    public static void logrb(final Logger logger,
                             final Level level,
                             final String bundleName,
                             final String key,
                             final char param1) {
        if (logger.isEnabledFor(level)) {
            forcedLog(logger, level,
                    format(bundleName, key, toArray(valueOf(param1))));
        }
    }
    public static void logrb(final Logger logger,
                             final Level level,
                             final String bundleName,
                             final String key,
                             final byte param1) {
        if (logger.isEnabledFor(level)) {
            forcedLog(logger, level,
                    format(bundleName, key, toArray(valueOf(param1))));
        }
    }
    public static void logrb(final Logger logger,
                             final Level level,
                             final String bundleName,
                             final String key,
                             final short param1) {
        if (logger.isEnabledFor(level)) {
            forcedLog(logger, level,
                    format(bundleName, key, toArray(valueOf(param1))));
        }
    }
    public static void logrb(final Logger logger,
                             final Level level,
                             final String bundleName,
                             final String key,
                             final int param1) {
        if (logger.isEnabledFor(level)) {
            forcedLog(logger, level,
                    format(bundleName, key, toArray(valueOf(param1))));
        }
    }
    public static void logrb(final Logger logger,
                             final Level level,
                             final String bundleName,
                             final String key,
                             final long param1) {
        if (logger.isEnabledFor(level)) {
            forcedLog(logger, level,
                    format(bundleName, key, toArray(valueOf(param1))));
        }
    }
    public static void logrb(final Logger logger,
                             final Level level,
                             final String bundleName,
                             final String key,
                             final float param1) {
        if (logger.isEnabledFor(level)) {
            forcedLog(logger, level,
                    format(bundleName, key, toArray(valueOf(param1))));
        }
    }
    public static void logrb(final Logger logger,
                             final Level level,
                             final String bundleName,
                             final String key,
                             final double param1) {
        if (logger.isEnabledFor(level)) {
            forcedLog(logger, level,
                    format(bundleName, key, toArray(valueOf(param1))));
        }
    }
    public static void logrb(final Logger logger,
                             final Level level,
                             final String bundleName,
                             final String key,
                             final Object param0,
                             final Object param1) {
        if (logger.isEnabledFor(level)) {
            forcedLog(logger, level,
                    format(bundleName, key, toArray(param0, param1)));
        }
    }
    public static void logrb(final Logger logger,
                             final Level level,
                             final String bundleName,
                             final String key,
                             final Object param0,
                             final Object param1,
                             final Object param2) {
        if (logger.isEnabledFor(level)) {
            forcedLog(logger, level,
                    format(bundleName, key, toArray(param0, param1, param2)));
        }
    }
    public static void logrb(final Logger logger,
                             final Level level,
                             final String bundleName,
                             final String key,
                             final Object param0,
                             final Object param1,
                             final Object param2,
                             final Object param3) {
        if (logger.isEnabledFor(level)) {
            forcedLog(logger, level,
                    format(bundleName, key,
                            toArray(param0, param1, param2, param3)));
        }
    }
}

package org.apache.maven.cli;
import java.util.Locale;
import org.codehaus.plexus.logging.AbstractLoggerManager;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.logging.LoggerManager;
import org.codehaus.plexus.logging.console.ConsoleLogger;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
public class MavenLoggerManager
    extends AbstractLoggerManager
    implements LoggerManager, Initializable
{
    private String threshold = "info";
    private int currentThreshold;
    private Logger logger;
    public MavenLoggerManager( Logger logger )
    {
        this.logger = logger;
    }
    public void initialize()
    {
        debug( "Initializing ConsoleLoggerManager: " + this.hashCode() + "." );
        currentThreshold = parseThreshold( threshold );
        if ( currentThreshold == -1 )
        {
            debug( "Could not parse the threshold level: '" + threshold + "', setting to debug." );
            currentThreshold = Logger.LEVEL_DEBUG;
        }
    }
    public void setThreshold( int currentThreshold )
    {
        this.currentThreshold = currentThreshold;
    }
    public void setThresholds( int currentThreshold )
    {
        this.currentThreshold = currentThreshold;
        logger.setThreshold( currentThreshold );
    }
    public int getThreshold()
    {
        return currentThreshold;
    }
    public void setThreshold( String role,
                              String roleHint,
                              int threshold )
    {
    }
    public int getThreshold( String role,
                             String roleHint )
    {
        return currentThreshold;
    }
    public Logger getLoggerForComponent( String role,
                                         String roleHint )
    {
        return logger;
    }
    public void returnComponentLogger( String role,
                                       String roleHint )
    {
    }
    public int getActiveLoggerCount()
    {
        return 1;
    }
    private int parseThreshold( String text )
    {
        text = text.trim().toLowerCase( Locale.ENGLISH );
        if ( text.equals( "debug" ) )
        {
            return ConsoleLogger.LEVEL_DEBUG;
        }
        else if ( text.equals( "info" ) )
        {
            return ConsoleLogger.LEVEL_INFO;
        }
        else if ( text.equals( "warn" ) )
        {
            return ConsoleLogger.LEVEL_WARN;
        }
        else if ( text.equals( "error" ) )
        {
            return ConsoleLogger.LEVEL_ERROR;
        }
        else if ( text.equals( "fatal" ) )
        {
            return ConsoleLogger.LEVEL_FATAL;
        }
        return -1;
    }
    private void debug( String msg )
    {
    }
}

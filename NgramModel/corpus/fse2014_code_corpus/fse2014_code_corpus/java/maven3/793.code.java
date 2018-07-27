package org.apache.maven.monitor.logging;
import org.apache.maven.plugin.logging.Log;
import org.codehaus.plexus.logging.Logger;
public class DefaultLog
    implements Log
{
    private final Logger logger;
    public DefaultLog( Logger logger )
    {
        this.logger = logger;
    }
    public void debug( CharSequence content )
    {
        logger.debug( toString( content ) );
    }
    private String toString( CharSequence content )
    {
        if ( content == null )
        {
            return "";
        }
        else
        {
            return content.toString();
        }
    }
    public void debug( CharSequence content, Throwable error )
    {
        logger.debug( toString( content ), error );
    }
    public void debug( Throwable error )
    {
        logger.debug( "", error );
    }
    public void info( CharSequence content )
    {
        logger.info( toString( content ) );
    }
    public void info( CharSequence content, Throwable error )
    {
        logger.info( toString( content ), error );
    }
    public void info( Throwable error )
    {
        logger.info( "", error );
    }
    public void warn( CharSequence content )
    {
        logger.warn( toString( content ) );
    }
    public void warn( CharSequence content, Throwable error )
    {
        logger.warn( toString( content ), error );
    }
    public void warn( Throwable error )
    {
        logger.warn( "", error );
    }
    public void error( CharSequence content )
    {
        logger.error( toString( content ) );
    }
    public void error( CharSequence content, Throwable error )
    {
        logger.error( toString( content ), error );
    }
    public void error( Throwable error )
    {
        logger.error( "", error );
    }
    public boolean isDebugEnabled()
    {
        return logger.isDebugEnabled();
    }
    public boolean isInfoEnabled()
    {
        return logger.isInfoEnabled();
    }
    public boolean isWarnEnabled()
    {
        return logger.isWarnEnabled();
    }
    public boolean isErrorEnabled()
    {
        return logger.isErrorEnabled();
    }
}
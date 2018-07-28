package org.apache.maven.lifecycle.internal.stub;
import org.codehaus.plexus.logging.Logger;
public class LoggerStub
    implements Logger
{
    public void debug( String s )
    {
    }
    public void debug( String s, Throwable throwable )
    {
    }
    public boolean isDebugEnabled()
    {
        return true;
    }
    public void info( String s )
    {
    }
    public void info( String s, Throwable throwable )
    {
    }
    public boolean isInfoEnabled()
    {
        return true;
    }
    public void warn( String s )
    {
    }
    public void warn( String s, Throwable throwable )
    {
    }
    public boolean isWarnEnabled()
    {
        return true;
    }
    public void error( String s )
    {
    }
    public void error( String s, Throwable throwable )
    {
    }
    public boolean isErrorEnabled()
    {
        return true;
    }
    public void fatalError( String s )
    {
    }
    public void fatalError( String s, Throwable throwable )
    {
    }
    public boolean isFatalErrorEnabled()
    {
        return true;
    }
    public Logger getChildLogger( String s )
    {
        return null;
    }
    public int getThreshold()
    {
        return 0;
    }
    public void setThreshold( int i )
    {
    }
    public String getName()
    {
        return "StubLogger";
    }
}

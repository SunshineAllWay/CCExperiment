package org.apache.maven.settings.building;
public class DefaultSettingsProblem
    implements SettingsProblem
{
    private final String source;
    private final int lineNumber;
    private final int columnNumber;
    private final String message;
    private final Exception exception;
    private final Severity severity;
    public DefaultSettingsProblem( String message, Severity severity, String source, int lineNumber, int columnNumber,
                                   Exception exception )
    {
        this.message = message;
        this.severity = ( severity != null ) ? severity : Severity.ERROR;
        this.source = ( source != null ) ? source : "";
        this.lineNumber = lineNumber;
        this.columnNumber = columnNumber;
        this.exception = exception;
    }
    public String getSource()
    {
        return source;
    }
    public int getLineNumber()
    {
        return lineNumber;
    }
    public int getColumnNumber()
    {
        return columnNumber;
    }
    public String getLocation()
    {
        StringBuilder buffer = new StringBuilder( 256 );
        if ( getSource().length() > 0 )
        {
            if ( buffer.length() > 0 )
            {
                buffer.append( ", " );
            }
            buffer.append( getSource() );
        }
        if ( getLineNumber() > 0 )
        {
            if ( buffer.length() > 0 )
            {
                buffer.append( ", " );
            }
            buffer.append( "line " ).append( getLineNumber() );
        }
        if ( getColumnNumber() > 0 )
        {
            if ( buffer.length() > 0 )
            {
                buffer.append( ", " );
            }
            buffer.append( "column " ).append( getColumnNumber() );
        }
        return buffer.toString();
    }
    public Exception getException()
    {
        return exception;
    }
    public String getMessage()
    {
        String msg;
        if ( message != null && message.length() > 0 )
        {
            msg = message;
        }
        else
        {
            msg = exception.getMessage();
            if ( msg == null )
            {
                msg = "";
            }
        }
        return msg;
    }
    public Severity getSeverity()
    {
        return severity;
    }
    @Override
    public String toString()
    {
        StringBuilder buffer = new StringBuilder( 128 );
        buffer.append( "[" ).append( getSeverity() ).append( "] " );
        buffer.append( getMessage() );
        buffer.append( " @ " ).append( getLocation() );
        return buffer.toString();
    }
}

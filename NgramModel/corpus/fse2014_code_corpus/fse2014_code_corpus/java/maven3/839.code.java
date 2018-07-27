package org.apache.maven.settings.io;
import java.io.IOException;
public class SettingsParseException
    extends IOException
{
    private final int lineNumber;
    private final int columnNumber;
    public SettingsParseException( String message, int lineNumber, int columnNumber )
    {
        super( message );
        this.lineNumber = lineNumber;
        this.columnNumber = columnNumber;
    }
    public SettingsParseException( String message, int lineNumber, int columnNumber, Throwable cause )
    {
        super( message );
        initCause( cause );
        this.lineNumber = lineNumber;
        this.columnNumber = columnNumber;
    }
    public int getLineNumber()
    {
        return lineNumber;
    }
    public int getColumnNumber()
    {
        return columnNumber;
    }
}

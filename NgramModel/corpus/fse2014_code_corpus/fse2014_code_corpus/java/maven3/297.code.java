package org.apache.maven.artifact.repository.metadata.io;
import java.io.IOException;
public class MetadataParseException
    extends IOException
{
    private final int lineNumber;
    private final int columnNumber;
    public MetadataParseException( String message, int lineNumber, int columnNumber )
    {
        super( message );
        this.lineNumber = lineNumber;
        this.columnNumber = columnNumber;
    }
    public MetadataParseException( String message, int lineNumber, int columnNumber, Throwable cause )
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

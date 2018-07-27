package org.apache.maven.model.building;
import org.apache.maven.model.Model;
public class DefaultModelProblem
    implements ModelProblem
{
    private final String source;
    private final int lineNumber;
    private final int columnNumber;
    private final String modelId;
    private final String message;
    private final Exception exception;
    private final Severity severity;
    public DefaultModelProblem( String message, Severity severity, Model source, int lineNumber, int columnNumber,
                                Exception exception )
    {
        this( message, severity, ModelProblemUtils.toPath( source ), lineNumber, columnNumber,
              ModelProblemUtils.toId( source ), exception );
    }
    public DefaultModelProblem( String message, Severity severity, String source, int lineNumber, int columnNumber,
                                String modelId, Exception exception )
    {
        this.message = message;
        this.severity = ( severity != null ) ? severity : Severity.ERROR;
        this.source = ( source != null ) ? source : "";
        this.lineNumber = lineNumber;
        this.columnNumber = columnNumber;
        this.modelId = ( modelId != null ) ? modelId : "";
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
    public String getModelId()
    {
        return modelId;
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
        buffer.append( " @ " ).append( ModelProblemUtils.formatLocation( this, null ) );
        return buffer.toString();
    }
}

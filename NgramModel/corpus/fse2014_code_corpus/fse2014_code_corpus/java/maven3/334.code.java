package org.apache.maven.exception;
import java.util.Collections;
import java.util.List;
public class ExceptionSummary
{
    private Throwable exception;
    private String message;
    private String reference;
    private List<ExceptionSummary> children;
    public ExceptionSummary( Throwable exception, String message, String reference )
    {
        this( exception, message, reference, null );
    }
    public ExceptionSummary( Throwable exception, String message, String reference, List<ExceptionSummary> children )
    {
        this.exception = exception;
        this.message = ( message != null ) ? message : "";
        this.reference = ( reference != null ) ? reference : "";
        this.children = ( children != null ) ? children : Collections.<ExceptionSummary> emptyList();
    }
    public Throwable getException()
    {
        return exception;
    }
    public String getMessage()
    {
        return message;
    }
    public String getReference()
    {
        return reference;
    }
    public List<ExceptionSummary> getChildren()
    {
        return children;
    }
}

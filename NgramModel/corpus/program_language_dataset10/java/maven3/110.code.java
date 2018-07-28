package org.apache.maven.project.interpolation;
@SuppressWarnings( "serial" )
@Deprecated
public class ModelInterpolationException
    extends Exception
{
    private String expression;
    private String originalMessage;
    public ModelInterpolationException( String message )
    {
        super( message );
    }
    public ModelInterpolationException( String message, Throwable cause )
    {
        super( message, cause );
    }
    public ModelInterpolationException( String expression, String message, Throwable cause )
    {
        super( "The POM expression: " + expression + " could not be evaluated. Reason: " + message, cause );
        this.expression = expression;
        this.originalMessage = message;
    }
    public ModelInterpolationException( String expression, String message )
    {
        super( "The POM expression: " + expression + " could not be evaluated. Reason: " + message );
        this.expression = expression;
        this.originalMessage = message;
    }
    public String getExpression()
    {
        return expression;
    }
    public String getOriginalMessage()
    {
        return originalMessage;
    }
}
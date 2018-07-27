package org.apache.maven.project.validation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
public class ModelValidationResult
{
    private final static String NEWLINE = System.getProperty( "line.separator" );
    private List messages;
    public ModelValidationResult()
    {
        messages = new ArrayList();
    }
    public int getMessageCount()
    {
        return messages.size();
    }
    public String getMessage( int i )
    {
        return messages.get( i ).toString();
    }
    public List getMessages()
    {
        return Collections.unmodifiableList( messages );
    }
    public void addMessage( String message )
    {
        messages.add( message );
    }
    public String toString()
    {
        return render( "" );
    }
    public String render( String indentation )
    {
        if ( messages.size() == 0 )
        {
            return indentation + "There were no validation errors.";
        }
        StringBuffer message = new StringBuffer();
        for ( int i = 0; i < messages.size(); i++ )
        {
            message.append( indentation + "[" + i + "]  " + messages.get( i ).toString() + NEWLINE );
        }
        return message.toString();
    }
}

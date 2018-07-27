package org.apache.maven.lifecycle;
import java.io.IOException;
import java.util.Iterator;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.JXPathNotFoundException;
import org.apache.commons.jxpath.ri.JXPathContextReferenceImpl;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.project.harness.Xpp3DomPointerFactory;
public class MojoExecutionXPathContainer
{
    private JXPathContext context;
    private MojoExecution mojoExecution;
    static
    {
        JXPathContextReferenceImpl.addNodePointerFactory( new Xpp3DomPointerFactory() );
    }
    public MojoExecutionXPathContainer( MojoExecution mojoExecution )
        throws IOException
    {
        this.mojoExecution = mojoExecution;
        context = JXPathContext.newContext( mojoExecution );
    }
	public Iterator<?> getIteratorForXPathExpression( String expression )
    {
        return context.iterate( expression );
    }
    public boolean containsXPathExpression( String expression )
    {
        return context.getValue( expression ) != null;
    }
    public Object getValue( String expression )
    {
        try
        {
            return context.getValue( expression );
        }
        catch ( JXPathNotFoundException e )
        {
            return null;
        }
    }
    public boolean xPathExpressionEqualsValue( String expression, String value )
    {
        return context.getValue( expression ) != null && context.getValue( expression ).equals( value );
    }
}

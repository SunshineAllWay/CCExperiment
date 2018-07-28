package org.apache.maven.project.harness;
import java.io.File;
import java.util.Iterator;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.JXPathNotFoundException;
import org.apache.commons.jxpath.ri.JXPathContextReferenceImpl;
import org.apache.maven.project.MavenProject;
public class PomTestWrapper
{
    private File pomFile;
    private JXPathContext context;
    private MavenProject mavenProject;
    static
    {
        JXPathContextReferenceImpl.addNodePointerFactory( new Xpp3DomPointerFactory() );
    }
    public PomTestWrapper( File pomFile, MavenProject mavenProject )
    {
        if ( mavenProject == null )
        {
            throw new IllegalArgumentException( "mavenProject: null" );
        }
        this.mavenProject = mavenProject;
        this.pomFile = pomFile;
        context = JXPathContext.newContext( mavenProject.getModel() );
    }
    public PomTestWrapper( MavenProject mavenProject )
    {
        if ( mavenProject == null )
        {
            throw new IllegalArgumentException( "mavenProject: null" );
        }
        this.mavenProject = mavenProject;
        context = JXPathContext.newContext( mavenProject.getModel() );
    }
    public MavenProject getMavenProject()
    {
        return mavenProject;
    }
    public File getBasedir()
    {
        return ( pomFile != null ) ? pomFile.getParentFile() : null;
    }
    public void setValueOnModel( String expression, Object value )
    {
        context.setValue( expression, value );
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

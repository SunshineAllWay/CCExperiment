package org.apache.maven.usability.plugin;
import org.apache.maven.usability.plugin.io.xpp3.ParamdocXpp3Reader;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.ReaderFactory;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
public class ExpressionDocumenter
{
    private static final String[] EXPRESSION_ROOTS = { "project", "settings", "session", "plugin", "rootless" };
    private static final String EXPRESSION_DOCO_ROOTPATH = "META-INF/maven/plugin-expressions/";
    private static Map expressionDocumentation;
    public static Map load()
        throws ExpressionDocumentationException
    {
        if ( expressionDocumentation == null )
        {
            expressionDocumentation = new HashMap();
            ClassLoader docLoader = initializeDocLoader();
            for ( int i = 0; i < EXPRESSION_ROOTS.length; i++ )
            {
                InputStream docStream = null;
                try
                {
                    docStream = docLoader
                        .getResourceAsStream( EXPRESSION_DOCO_ROOTPATH + EXPRESSION_ROOTS[i] + ".paramdoc.xml" );
                    if ( docStream != null )
                    {
                        Map doco = parseExpressionDocumentation( docStream );
                        expressionDocumentation.putAll( doco );
                    }
                }
                catch ( IOException e )
                {
                    throw new ExpressionDocumentationException( "Failed to read documentation for expression root: "
                        + EXPRESSION_ROOTS[i], e );
                }
                catch ( XmlPullParserException e )
                {
                    throw new ExpressionDocumentationException( "Failed to parse documentation for expression root: "
                        + EXPRESSION_ROOTS[i], e );
                }
                finally
                {
                    IOUtil.close( docStream );
                }
            }
        }
        return expressionDocumentation;
    }
    private static Map parseExpressionDocumentation( InputStream docStream )
        throws IOException, XmlPullParserException
    {
        Reader reader = new BufferedReader( ReaderFactory.newXmlReader( docStream ) );
        ParamdocXpp3Reader paramdocReader = new ParamdocXpp3Reader();
        ExpressionDocumentation documentation = paramdocReader.read( reader, true );
        List expressions = documentation.getExpressions();
        Map bySyntax = new HashMap();
        if ( expressions != null && !expressions.isEmpty() )
        {
            for ( Iterator it = expressions.iterator(); it.hasNext(); )
            {
                Expression expr = (Expression) it.next();
                bySyntax.put( expr.getSyntax(), expr );
            }
        }
        return bySyntax;
    }
    private static ClassLoader initializeDocLoader()
        throws ExpressionDocumentationException
    {
        String myResourcePath = ExpressionDocumenter.class.getName().replace( '.', '/' ) + ".class";
        URL myResource = ExpressionDocumenter.class.getClassLoader().getResource( myResourcePath );
        String myClasspathEntry = myResource.getPath();
        myClasspathEntry = myClasspathEntry.substring( 0, myClasspathEntry.length() - ( myResourcePath.length() + 2 ) );
        if ( myClasspathEntry.startsWith( "file:" ) )
        {
            myClasspathEntry = myClasspathEntry.substring( "file:".length() );
        }
        URL docResource;
        try
        {
            docResource = new File( myClasspathEntry ).toURL();
        }
        catch ( MalformedURLException e )
        {
            throw new ExpressionDocumentationException(
                                                        "Cannot construct expression documentation classpath resource base.",
                                                        e );
        }
        return new URLClassLoader( new URL[] { docResource } );
    }
}

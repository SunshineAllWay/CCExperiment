package org.apache.maven.project.path;
import org.apache.maven.model.Build;
import org.apache.maven.model.Model;
import org.apache.maven.model.Reporting;
import org.apache.maven.model.Resource;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
public class DefaultPathTranslator
    implements PathTranslator
{
    private static final String[] BASEDIR_EXPRESSIONS = {
        "${basedir}",
        "${pom.basedir}",
        "${project.basedir}"
    };
    public void alignToBaseDirectory( Model model, File basedir )
    {
        if ( basedir == null )
        {
            return;
        }
        Build build = model.getBuild();
        if ( build != null )
        {
            build.setDirectory( alignToBaseDirectory( build.getDirectory(), basedir ) );
            build.setSourceDirectory( alignToBaseDirectory( build.getSourceDirectory(), basedir ) );
            build.setTestSourceDirectory( alignToBaseDirectory( build.getTestSourceDirectory(), basedir ) );
            for ( Iterator i = build.getResources().iterator(); i.hasNext(); )
            {
                Resource resource = (Resource) i.next();
                resource.setDirectory( alignToBaseDirectory( resource.getDirectory(), basedir ) );
            }
            for ( Iterator i = build.getTestResources().iterator(); i.hasNext(); )
            {
                Resource resource = (Resource) i.next();
                resource.setDirectory( alignToBaseDirectory( resource.getDirectory(), basedir ) );
            }
            if ( build.getFilters() != null )
            {
                List filters = new ArrayList();
                for ( Iterator i = build.getFilters().iterator(); i.hasNext(); )
                {
                    String filter = (String) i.next();
                    filters.add( alignToBaseDirectory( filter, basedir ) );
                }
                build.setFilters( filters );
            }
            build.setOutputDirectory( alignToBaseDirectory( build.getOutputDirectory(), basedir ) );
            build.setTestOutputDirectory( alignToBaseDirectory( build.getTestOutputDirectory(), basedir ) );
        }
        Reporting reporting = model.getReporting();
        if ( reporting != null )
        {
            reporting.setOutputDirectory( alignToBaseDirectory( reporting.getOutputDirectory(), basedir ) );
        }
    }
    public String alignToBaseDirectory( String path, File basedir )
    {
        if ( basedir == null )
        {
            return path;
        }
        if ( path == null )
        {
            return null;
        }
        String s = stripBasedirToken( path );
        File file = new File( s );
        if ( file.isAbsolute() )
        {
            s = file.getPath();
        }
        else if ( file.getPath().startsWith( File.separator ) )
        {
            s = file.getAbsolutePath();
        }
        else
        {
            s = new File( new File( basedir, s ).toURI().normalize() ).getAbsolutePath();
        }
        return s;
    }
    private String stripBasedirToken( String s )
    {
        if ( s != null )
        {
            String basedirExpr = null;
            for ( int i = 0; i < BASEDIR_EXPRESSIONS.length; i++ )
            {
                basedirExpr = BASEDIR_EXPRESSIONS[i];
                if ( s.startsWith( basedirExpr ) )
                {
                    break;
                }
                else
                {
                    basedirExpr = null;
                }
            }
            if ( basedirExpr != null )
            {
                if ( s.length() > basedirExpr.length() )
                {
                    s = chopLeadingFileSeparator( s.substring( basedirExpr.length() ) );
                }
                else
                {
                    s = ".";
                }
            }
        }
        return s;
    }
    private String chopLeadingFileSeparator( String path )
    {
        if ( path != null )
        {
            if ( path.startsWith( "/" ) || path.startsWith( "\\" ) )
            {
                path = path.substring( 1 );
            }
        }
        return path;
    }
    public void unalignFromBaseDirectory( Model model, File basedir )
    {
        if ( basedir == null )
        {
            return;
        }
        Build build = model.getBuild();
        if ( build != null )
        {
            build.setDirectory( unalignFromBaseDirectory( build.getDirectory(), basedir ) );
            build.setSourceDirectory( unalignFromBaseDirectory( build.getSourceDirectory(), basedir ) );
            build.setTestSourceDirectory( unalignFromBaseDirectory( build.getTestSourceDirectory(), basedir ) );
            build.setScriptSourceDirectory( unalignFromBaseDirectory( build.getScriptSourceDirectory(), basedir ) );
            for ( Iterator i = build.getResources().iterator(); i.hasNext(); )
            {
                Resource resource = (Resource) i.next();
                resource.setDirectory( unalignFromBaseDirectory( resource.getDirectory(), basedir ) );
            }
            for ( Iterator i = build.getTestResources().iterator(); i.hasNext(); )
            {
                Resource resource = (Resource) i.next();
                resource.setDirectory( unalignFromBaseDirectory( resource.getDirectory(), basedir ) );
            }
            if ( build.getFilters() != null )
            {
                List filters = new ArrayList();
                for ( Iterator i = build.getFilters().iterator(); i.hasNext(); )
                {
                    String filter = (String) i.next();
                    filters.add( unalignFromBaseDirectory( filter, basedir ) );
                }
                build.setFilters( filters );
            }
            build.setOutputDirectory( unalignFromBaseDirectory( build.getOutputDirectory(), basedir ) );
            build.setTestOutputDirectory( unalignFromBaseDirectory( build.getTestOutputDirectory(), basedir ) );
        }
        Reporting reporting = model.getReporting();
        if ( reporting != null )
        {
            reporting.setOutputDirectory( unalignFromBaseDirectory( reporting.getOutputDirectory(), basedir ) );
        }
    }
    public String unalignFromBaseDirectory( String path, File basedir )
    {
        if ( basedir == null )
        {
            return path;
        }
        if ( path == null )
        {
            return null;
        }
        path = path.trim();
        String base = basedir.getAbsolutePath();
        if ( path.startsWith( base ) )
        {
            path = chopLeadingFileSeparator( path.substring( base.length() ) );
        }
        if ( !new File( path ).isAbsolute() )
        {
            path = path.replace( '\\', '/' );
        }
        return path;
    }
}

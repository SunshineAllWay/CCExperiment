package org.apache.maven.project.imports;
import org.apache.maven.project.AbstractMavenProjectTestCase;
import java.io.File;
public abstract class AbstractProjectImportsTestCase
    extends AbstractMavenProjectTestCase
{
    protected String getTestSeries()
    {
        String className = getClass().getPackage().getName();
        return className.substring( className.lastIndexOf( "." ) + 1 );
    }
    protected File projectFile( String name )
    {
        return new File( getLocalRepositoryPath(), "/maven/poms/" + name + "-1.0.pom" );
    }
    protected File getLocalRepositoryPath()
    {
        return getTestFile( "src/test/resources/imports-repo/" + getTestSeries() );
    }
}

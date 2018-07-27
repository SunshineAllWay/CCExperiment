package org.apache.maven.project.inheritance;
import org.apache.maven.project.AbstractMavenProjectTestCase;
import java.io.File;
public abstract class AbstractProjectInheritanceTestCase
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
        return getTestFile( "src/test/resources/inheritance-repo/" + getTestSeries() );
    }
}

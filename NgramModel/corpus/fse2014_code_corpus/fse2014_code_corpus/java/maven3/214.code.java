package org.apache.maven.project.inheritance;
import java.io.File;
import org.apache.maven.project.AbstractMavenProjectTestCase;
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
        return projectFile( "maven", name );
    }
    protected File projectFile( String groupId, String artifactId )
    {
        return new File( getLocalRepositoryPath(), "/" + groupId + "/poms/" + artifactId + "-1.0.pom" );
    }
    protected File getLocalRepositoryPath()
    {
        return getTestFile("src/test/resources/inheritance-repo/" + getTestSeries() );
    }
}

package org.apache.maven.project.inheritance.t01;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.inheritance.AbstractProjectInheritanceTestCase;
public class ProjectInheritanceTest
    extends AbstractProjectInheritanceTestCase
{
    public void testProjectInheritance()
        throws Exception
    {
        MavenProject p0 = getProject( projectFile( "p0" ) );
        assertEquals( "p0-org", p0.getOrganization().getName() );
        MavenProject p1 = getProject( projectFile( "p1" ) );
        assertEquals( "p1-org", p1.getOrganization().getName() );
        MavenProject p2 = getProject( projectFile( "p2" ) );
        assertEquals( "p2-org", p2.getOrganization().getName() );
        MavenProject p3 = getProject( projectFile( "p3" ) );
        assertEquals( "p3-org", p3.getOrganization().getName() );
        MavenProject p4 = getProject( projectFile( "p4" ) );
        assertEquals( "p4-org", p4.getOrganization().getName() );
    }
}

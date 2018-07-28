package org.apache.maven.toolchain;
import junit.framework.TestCase;
public class RequirementMatcherFactoryTest
    extends TestCase
{
    public RequirementMatcherFactoryTest( String testName )
    {
        super( testName );
    }
    public void testCreateExactMatcher()
    {
        RequirementMatcher matcher;
        matcher = RequirementMatcherFactory.createExactMatcher( "foo" );
        assertFalse( matcher.matches( "bar" ) );
        assertFalse( matcher.matches( "foobar" ) );
        assertFalse( matcher.matches( "foob" ) );
        assertTrue( matcher.matches( "foo" ) );
    }
    public void testCreateVersionMatcher()
    {
        RequirementMatcher matcher;
        matcher = RequirementMatcherFactory.createVersionMatcher( "1.5.2" );
        assertFalse( matcher.matches( "1.5" ) );
        assertTrue( matcher.matches( "1.5.2" ) );
        assertFalse( matcher.matches( "[1.4,1.5)" ) );
        assertFalse( matcher.matches( "[1.5,1.5.2)" ) );
        assertFalse( matcher.matches( "(1.5.2,1.6)" ) );
        assertTrue( matcher.matches( "(1.4,1.5.2]" ) );
        assertTrue( matcher.matches( "(1.5,)" ) );
    }
}

package org.apache.maven.project.interpolation;
import org.apache.maven.model.Build;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.Organization;
import org.apache.maven.model.Repository;
import org.apache.maven.model.Resource;
import org.apache.maven.model.Scm;
import org.apache.maven.project.DefaultProjectBuilderConfiguration;
import org.apache.maven.project.path.DefaultPathTranslator;
import org.apache.maven.project.path.PathTranslator;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import junit.framework.TestCase;
public abstract class AbstractModelInterpolatorTest
    extends TestCase
{
    private Map context;
    protected void setUp()
        throws Exception
    {
        super.setUp();
        context = new HashMap();
        context.put( "basedir", "myBasedir" );
        context.put( "project.baseUri", "myBaseUri" );
    }
    public void testDefaultBuildTimestampFormatShouldParseTimeIn24HourFormat()
    {
        Calendar cal = Calendar.getInstance();
        cal.set( Calendar.HOUR, 12 );
        cal.set( Calendar.AM_PM, Calendar.AM );
        cal.set( Calendar.HOUR_OF_DAY, 0 );
        cal.set( Calendar.MINUTE, 16 );
        cal.set( Calendar.YEAR, 1976 );
        cal.set( Calendar.MONTH, Calendar.NOVEMBER );
        cal.set( Calendar.DATE, 11 );
        Date firstTestDate = cal.getTime();
        cal.set( Calendar.HOUR, 11 );
        cal.set( Calendar.AM_PM, Calendar.PM );
        cal.set( Calendar.HOUR_OF_DAY, 23 );
        Date secondTestDate = cal.getTime();
        SimpleDateFormat format = new SimpleDateFormat( ModelInterpolator.DEFAULT_BUILD_TIMESTAMP_FORMAT );
        assertEquals( "19761111-0016", format.format( firstTestDate ) );
        assertEquals( "19761111-2316", format.format( secondTestDate ) );
    }
    public void testShouldNotThrowExceptionOnReferenceToNonExistentValue()
        throws Exception
    {
        Model model = new Model();
        Scm scm = new Scm();
        scm.setConnection( "${test}/somepath" );
        model.setScm( scm );
        ModelInterpolator interpolator = createInterpolator();
        Model out = interpolator.interpolate( model, context );
        assertEquals( "${test}/somepath", out.getScm().getConnection() );
    }
    public void testShouldThrowExceptionOnRecursiveScmConnectionReference()
        throws Exception
    {
        Model model = new Model();
        Scm scm = new Scm();
        scm.setConnection( "${project.scm.connection}/somepath" );
        model.setScm( scm );
        try
        {
            ModelInterpolator interpolator = createInterpolator();
            interpolator.interpolate( model, context );
            fail( "The interpolator should not allow self-referencing expressions in POM." );
        }
        catch ( ModelInterpolationException e )
        {
        }
    }
    public void testShouldNotThrowExceptionOnReferenceToValueContainingNakedExpression()
        throws Exception
    {
        Model model = new Model();
        Scm scm = new Scm();
        scm.setConnection( "${test}/somepath" );
        model.setScm( scm );
        model.addProperty( "test", "test" );
        ModelInterpolator interpolator = createInterpolator();
        Model out = interpolator.interpolate( model, context );
        assertEquals( "test/somepath", out.getScm().getConnection() );
    }
    public void testShouldInterpolateOrganizationNameCorrectly()
        throws Exception
    {
        String orgName = "MyCo";
        Model model = new Model();
        model.setName( "${pom.organization.name} Tools" );
        Organization org = new Organization();
        org.setName( orgName );
        model.setOrganization( org );
        ModelInterpolator interpolator = createInterpolator();
        Model out = interpolator.interpolate( model, context );
        assertEquals( orgName + " Tools", out.getName() );
    }
    public void testShouldInterpolateDependencyVersionToSetSameAsProjectVersion()
        throws Exception
    {
        Model model = new Model();
        model.setVersion( "3.8.1" );
        Dependency dep = new Dependency();
        dep.setVersion( "${version}" );
        model.addDependency( dep );
        ModelInterpolator interpolator = createInterpolator();
        Model out = interpolator.interpolate( model, context );
        assertEquals( "3.8.1", ( (Dependency) out.getDependencies().get( 0 ) ).getVersion() );
    }
    public void testShouldNotInterpolateDependencyVersionWithInvalidReference()
        throws Exception
    {
        Model model = new Model();
        model.setVersion( "3.8.1" );
        Dependency dep = new Dependency();
        dep.setVersion( "${something}" );
        model.addDependency( dep );
        ModelInterpolator interpolator = createInterpolator();
        Model out = interpolator.interpolate( model, context );
        assertEquals( "${something}", ( (Dependency) out.getDependencies().get( 0 ) ).getVersion() );
    }
    public void testTwoReferences()
        throws Exception
    {
        Model model = new Model();
        model.setVersion( "3.8.1" );
        model.setArtifactId( "foo" );
        Dependency dep = new Dependency();
        dep.setVersion( "${artifactId}-${version}" );
        model.addDependency( dep );
        ModelInterpolator interpolator = createInterpolator();
        Model out = interpolator.interpolate( model, context );
        assertEquals( "foo-3.8.1", ( (Dependency) out.getDependencies().get( 0 ) ).getVersion() );
    }
    public void testBasedir()
        throws Exception
    {
        Model model = new Model();
        model.setVersion( "3.8.1" );
        model.setArtifactId( "foo" );
        Repository repository = new Repository();
        repository.setUrl( "file://localhost/${basedir}/temp-repo" );
        model.addRepository( repository );
        ModelInterpolator interpolator = createInterpolator();
        Model out = interpolator.interpolate( model, context );
        assertEquals( "file://localhost/myBasedir/temp-repo", ( (Repository) out.getRepositories().get( 0 ) ).getUrl() );
    }
    public void testBaseUri()
        throws Exception
    {
        Model model = new Model();
        model.setVersion( "3.8.1" );
        model.setArtifactId( "foo" );
        Repository repository = new Repository();
        repository.setUrl( "${project.baseUri}/temp-repo" );
        model.addRepository( repository );
        ModelInterpolator interpolator = createInterpolator();
        Model out = interpolator.interpolate( model, context );
        assertEquals( "myBaseUri/temp-repo", ( (Repository) out.getRepositories().get( 0 ) ).getUrl() );
    }
    public void testEnvars()
        throws Exception
    {
        Map context = new HashMap();
        context.put( "env.HOME", "/path/to/home" );
        Model model = new Model();
        Properties modelProperties = new Properties();
        modelProperties.setProperty( "outputDirectory", "${env.HOME}" );
        model.setProperties( modelProperties );
        ModelInterpolator interpolator = createInterpolator();
        Model out = interpolator.interpolate( model, context );
        assertEquals( "/path/to/home", out.getProperties().getProperty( "outputDirectory" ) );
    }
    public void testEnvarExpressionThatEvaluatesToNullReturnsTheLiteralString()
        throws Exception
    {
        Properties envars = new Properties();
        Model model = new Model();
        Properties modelProperties = new Properties();
        modelProperties.setProperty( "outputDirectory", "${env.DOES_NOT_EXIST}" );
        model.setProperties( modelProperties );
        ModelInterpolator interpolator = createInterpolator();
        Model out = interpolator.interpolate( model, context );
        assertEquals( out.getProperties().getProperty( "outputDirectory" ), "${env.DOES_NOT_EXIST}" );
    }
    public void testExpressionThatEvaluatesToNullReturnsTheLiteralString()
        throws Exception
    {
        Model model = new Model();
        Properties modelProperties = new Properties();
        modelProperties.setProperty( "outputDirectory", "${DOES_NOT_EXIST}" );
        model.setProperties( modelProperties );
        ModelInterpolator interpolator = createInterpolator();
        Model out = interpolator.interpolate( model, context );
        assertEquals( out.getProperties().getProperty( "outputDirectory" ), "${DOES_NOT_EXIST}" );
    }
    public void testShouldInterpolateSourceDirectoryReferencedFromResourceDirectoryCorrectly()
        throws Exception
    {
        Model model = new Model();
        Build build = new Build();
        build.setSourceDirectory( "correct" );
        Resource res = new Resource();
        res.setDirectory( "${project.build.sourceDirectory}" );
        build.addResource( res );
        Resource res2 = new Resource();
        res2.setDirectory( "${pom.build.sourceDirectory}" );
        build.addResource( res2 );
        Resource res3 = new Resource();
        res3.setDirectory( "${build.sourceDirectory}" );
        build.addResource( res3 );
        model.setBuild( build );
        ModelInterpolator interpolator = createInterpolator();
        Model out = interpolator.interpolate( model, context );
        List outResources = out.getBuild().getResources();
        Iterator resIt = outResources.iterator();
        assertEquals( build.getSourceDirectory(), ( (Resource) resIt.next() ).getDirectory() );
        assertEquals( build.getSourceDirectory(), ( (Resource) resIt.next() ).getDirectory() );
        assertEquals( build.getSourceDirectory(), ( (Resource) resIt.next() ).getDirectory() );
    }
    public void testShouldInterpolateUnprefixedBasedirExpression()
        throws Exception
    {
        File basedir = new File( "/test/path" );
        Model model = new Model();
        Dependency dep = new Dependency();
        dep.setSystemPath( "${basedir}/artifact.jar" );
        model.addDependency( dep );
        ModelInterpolator interpolator = createInterpolator();
        Model result = interpolator.interpolate( model, basedir, new DefaultProjectBuilderConfiguration(), true );
        List rDeps = result.getDependencies();
        assertNotNull( rDeps );
        assertEquals( 1, rDeps.size() );
        assertEquals( new File( basedir, "artifact.jar" ).getAbsolutePath(), new File( ( (Dependency) rDeps.get( 0 ) )
            .getSystemPath() ).getAbsolutePath() );
    }
    public void testTwoLevelRecursiveBasedirAlignedExpression()
        throws Exception
    {
        Model model = new Model();
        Build build = new Build();
        model.setBuild( build );
        build.setDirectory( "${project.basedir}/target" );
        build.setOutputDirectory( "${project.build.directory}/classes" );
        PathTranslator translator = new DefaultPathTranslator();
        ModelInterpolator interpolator = createInterpolator( translator );
        File basedir = new File( System.getProperty( "java.io.tmpdir" ), "base" );
        String value = interpolator.interpolate( "${project.build.outputDirectory}/foo", model, basedir, new DefaultProjectBuilderConfiguration(), true );
        value = value.replace( '/', File.separatorChar ).replace( '\\', File.separatorChar );
        String check = new File( basedir, "target/classes/foo" ).getAbsolutePath();
        check = check.replace( '/', File.separatorChar ).replace( '\\', File.separatorChar );
        assertEquals( check, value );
    }
    protected abstract ModelInterpolator createInterpolator( PathTranslator translator )
        throws Exception;
    protected abstract ModelInterpolator createInterpolator()
        throws Exception;
}

package org.apache.maven.script.ant;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.ContextEnabled;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.PluginParameterExpressionEvaluator;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.path.PathTranslator;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.PropertyHelper;
import org.apache.tools.ant.types.Path;
import org.codehaus.plexus.archiver.ArchiverException;
import org.codehaus.plexus.archiver.UnArchiver;
import org.codehaus.plexus.archiver.zip.ZipUnArchiver;
import org.codehaus.plexus.component.MapOrientedComponent;
import org.codehaus.plexus.component.configurator.ComponentConfigurationException;
import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluator;
import org.codehaus.plexus.component.factory.ant.AntComponentExecutionException;
import org.codehaus.plexus.component.factory.ant.AntScriptInvoker;
import org.codehaus.plexus.component.repository.ComponentRequirement;
import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.util.StringUtils;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
public class AntMojoWrapper
    extends AbstractMojo
    implements ContextEnabled, MapOrientedComponent, LogEnabled
{
    private Map pluginContext;
    private final AntScriptInvoker scriptInvoker;
    private Project antProject;
    private MavenProject mavenProject;
    private MojoExecution mojoExecution;
    private MavenSession session;
    private PathTranslator pathTranslator;
    private Logger logger;
    private transient List unconstructedParts = new ArrayList();
    public AntMojoWrapper( AntScriptInvoker scriptInvoker )
    {
        this.scriptInvoker = scriptInvoker;
    }
    public void execute()
        throws MojoExecutionException
    {
        if ( antProject == null )
        {
            antProject = scriptInvoker.getProject();
        }
        Map allConfig = new HashMap();
        if ( pluginContext != null && !pluginContext.isEmpty() )
        {
            allConfig.putAll( pluginContext );
        }
        Map refs = scriptInvoker.getReferences();
        if ( refs != null )
        {
            allConfig.putAll( refs );
            for ( Iterator it = refs.entrySet().iterator(); it.hasNext(); )
            {
                Map.Entry entry = (Map.Entry) it.next();
                String key = (String) entry.getKey();
                if ( key.startsWith( PathTranslator.class.getName() ) )
                {
                    pathTranslator = (PathTranslator) entry.getValue();
                }
            }
        }
        mavenProject = (MavenProject) allConfig.get( "project" );
        mojoExecution = (MojoExecution) allConfig.get( "mojoExecution" );
        session = (MavenSession) allConfig.get( "session" );
        unpackFileBasedResources();
        addClasspathReferences();
        if ( logger.isDebugEnabled() && !unconstructedParts.isEmpty() )
        {
            StringBuffer buffer = new StringBuffer();
            buffer.append( "The following standard Maven Ant-mojo support objects could not be created:\n\n" );
            for ( Iterator it = unconstructedParts.iterator(); it.hasNext(); )
            {
                String part = (String) it.next();
                buffer.append( "\n-  " ).append( part );
            }
            buffer.append( "\n\nMaven project, session, mojo-execution, or path-translation parameter information is " );
            buffer.append( "\nmissing from this mojo's plugin descriptor." );
            buffer.append( "\n\nPerhaps this Ant-based mojo depends on maven-script-ant < 2.1.0, " );
            buffer.append( "or used maven-plugin-tools-ant < 2.2 during release?\n\n" );
            logger.debug( buffer.toString() );
        }
        try
        {
            scriptInvoker.invoke();
        }
        catch ( AntComponentExecutionException e )
        {
            throw new MojoExecutionException( "Failed to execute: " + e.getMessage(), e );
        }
        unconstructedParts.clear();
    }
    public void setPluginContext( Map pluginContext )
    {
        this.pluginContext = pluginContext;
    }
    public Map getPluginContext()
    {
        return pluginContext;
    }
    public void addComponentRequirement( ComponentRequirement requirementDescriptor, Object requirementValue )
        throws ComponentConfigurationException
    {
        scriptInvoker.addComponentRequirement( requirementDescriptor, requirementValue );
    }
    public void setComponentConfiguration( Map componentConfiguration )
        throws ComponentConfigurationException
    {
        scriptInvoker.setComponentConfiguration( componentConfiguration );
        antProject = scriptInvoker.getProject();
    }
    private void unpackFileBasedResources()
        throws MojoExecutionException
    {
        if ( mojoExecution == null || mavenProject == null )
        {
            unconstructedParts.add( "Unpacked Ant build scripts (in Maven build directory)." );
            return;
        }
        PluginDescriptor pluginDescriptor = mojoExecution.getMojoDescriptor().getPluginDescriptor();
        File pluginJar = pluginDescriptor.getPluginArtifact().getFile();
        String resourcesPath = pluginDescriptor.getArtifactId();
        File outputDirectory = new File( mavenProject.getBuild().getDirectory() );
        try
        {
            UnArchiver ua = new ZipUnArchiver( pluginJar );
            ua.extract( resourcesPath, outputDirectory );
        }
        catch ( ArchiverException e )
        {
            throw new MojoExecutionException( "Error extracting resources from your Ant-based plugin.", e );
        }
    }
    private void addClasspathReferences()
        throws MojoExecutionException
    {
        try
        {
            if ( mavenProject != null && session != null && pathTranslator != null )
            {
                ExpressionEvaluator exprEvaluator =
                    new PluginParameterExpressionEvaluator( session, mojoExecution, pathTranslator, logger, mavenProject,
                                                            mavenProject.getProperties() );
                PropertyHelper propertyHelper = PropertyHelper.getPropertyHelper( antProject );
                propertyHelper.setNext( new AntPropertyHelper( exprEvaluator, mavenProject.getArtifacts(), getLog() ) );
            }
            else
            {
                unconstructedParts.add( "Maven parameter expression evaluator for Ant properties." );
            }
            if ( mavenProject != null )
            {
                Path p = new Path( antProject );
                p.setPath( StringUtils.join( mavenProject.getCompileClasspathElements().iterator(), File.pathSeparator ) );
                scriptInvoker.getReferences().put( "maven.dependency.classpath", p );
                antProject.addReference( "maven.dependency.classpath", p );
                scriptInvoker.getReferences().put( "maven.compile.classpath", p );
                antProject.addReference( "maven.compile.classpath", p );
                p = new Path( antProject );
                p.setPath( StringUtils.join( mavenProject.getRuntimeClasspathElements().iterator(), File.pathSeparator ) );
                scriptInvoker.getReferences().put( "maven.runtime.classpath", p );
                antProject.addReference( "maven.runtime.classpath", p );
                p = new Path( antProject );
                p.setPath( StringUtils.join( mavenProject.getTestClasspathElements().iterator(), File.pathSeparator ) );
                scriptInvoker.getReferences().put( "maven.test.classpath", p );
                antProject.addReference( "maven.test.classpath", p );
            }
            else
            {
                unconstructedParts.add( "Maven standard project-based classpath references." );
            }
            if ( mojoExecution != null )
            {
                Path p = getPathFromArtifacts( mojoExecution.getMojoDescriptor().getPluginDescriptor().getArtifacts(), antProject );
                scriptInvoker.getReferences().put( "maven.plugin.classpath", p );
                antProject.addReference( "maven.plugin.classpath", p );
            }
            else
            {
                unconstructedParts.add( "Maven standard plugin-based classpath references." );
            }
        }
        catch ( DependencyResolutionRequiredException e )
        {
            throw new MojoExecutionException( "Error creating classpath references for Ant-based plugin scripts.", e  );
        }
    }
    public Path getPathFromArtifacts( Collection artifacts,
                                      Project antProject )
        throws DependencyResolutionRequiredException
    {
        List list = new ArrayList( artifacts.size() );
        for ( Iterator i = artifacts.iterator(); i.hasNext(); )
        {
            Artifact a = (Artifact) i.next();
            File file = a.getFile();
            if ( file == null )
            {
                throw new DependencyResolutionRequiredException( a );
            }
            list.add( file.getPath() );
        }
        Path p = new Path( antProject );
        p.setPath( StringUtils.join( list.iterator(), File.pathSeparator ) );
        return p;
    }
    public Project getAntProject()
    {
        return antProject;
    }
    public void setAntProject( Project antProject )
    {
        this.antProject = antProject;
    }
    public MavenProject getMavenProject()
    {
        return mavenProject;
    }
    public void setMavenProject( MavenProject mavenProject )
    {
        this.mavenProject = mavenProject;
    }
    public MojoExecution getMojoExecution()
    {
        return mojoExecution;
    }
    public void setMojoExecution( MojoExecution mojoExecution )
    {
        this.mojoExecution = mojoExecution;
    }
    public MavenSession getSession()
    {
        return session;
    }
    public void setSession( MavenSession session )
    {
        this.session = session;
    }
    public PathTranslator getPathTranslator()
    {
        return pathTranslator;
    }
    public void setPathTranslator( PathTranslator pathTranslator )
    {
        this.pathTranslator = pathTranslator;
    }
    public AntScriptInvoker getScriptInvoker()
    {
        return scriptInvoker;
    }
    public void enableLogging( Logger logger )
    {
        this.logger = logger;
    }
}

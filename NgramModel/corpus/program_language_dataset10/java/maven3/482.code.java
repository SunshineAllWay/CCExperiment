package org.apache.maven.project;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import org.apache.maven.RepositoryUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.ArtifactUtils;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.artifact.InvalidRepositoryException;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.filter.ArtifactFilter;
import org.apache.maven.model.Build;
import org.apache.maven.model.CiManagement;
import org.apache.maven.model.Contributor;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.DependencyManagement;
import org.apache.maven.model.Developer;
import org.apache.maven.model.DistributionManagement;
import org.apache.maven.model.Extension;
import org.apache.maven.model.IssueManagement;
import org.apache.maven.model.License;
import org.apache.maven.model.MailingList;
import org.apache.maven.model.Model;
import org.apache.maven.model.Organization;
import org.apache.maven.model.Parent;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginExecution;
import org.apache.maven.model.PluginManagement;
import org.apache.maven.model.Prerequisites;
import org.apache.maven.model.Profile;
import org.apache.maven.model.ReportPlugin;
import org.apache.maven.model.ReportSet;
import org.apache.maven.model.Reporting;
import org.apache.maven.model.Repository;
import org.apache.maven.model.Resource;
import org.apache.maven.model.Scm;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.apache.maven.project.artifact.InvalidDependencyVersionException;
import org.apache.maven.project.artifact.MavenMetadataSource;
import org.apache.maven.repository.RepositorySystem;
import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.sonatype.aether.graph.DependencyFilter;
import org.sonatype.aether.repository.RemoteRepository;
public class MavenProject
    implements Cloneable
{
    public static final String EMPTY_PROJECT_GROUP_ID = "unknown";
    public static final String EMPTY_PROJECT_ARTIFACT_ID = "empty-project";
    public static final String EMPTY_PROJECT_VERSION = "0";
    private Model model;
    private MavenProject parent;
    private File file;
    private Set<Artifact> resolvedArtifacts;
    private ArtifactFilter artifactFilter;
    private Set<Artifact> artifacts;
    private Artifact parentArtifact;
    private Set<Artifact> pluginArtifacts;
    private List<ArtifactRepository> remoteArtifactRepositories;
    private List<ArtifactRepository> pluginArtifactRepositories;
    private List<RemoteRepository> remoteProjectRepositories;
    private List<RemoteRepository> remotePluginRepositories;
    private List<Artifact> attachedArtifacts;
    private MavenProject executionProject;
    private List<MavenProject> collectedProjects;
    private List<String> compileSourceRoots = new ArrayList<String>();
    private List<String> testCompileSourceRoots = new ArrayList<String>();
    private List<String> scriptSourceRoots = new ArrayList<String>();
    private ArtifactRepository releaseArtifactRepository;
    private ArtifactRepository snapshotArtifactRepository;
    private List<Profile> activeProfiles = new ArrayList<Profile>();
    private Map<String, List<String>> injectedProfileIds = new LinkedHashMap<String, List<String>>();
    private Set<Artifact> dependencyArtifacts;
    private Artifact artifact;
    private Map<String, Artifact> artifactMap;
    private Model originalModel;
    private Map<String, Artifact> pluginArtifactMap;
    private Set<Artifact> reportArtifacts;
    private Map<String, Artifact> reportArtifactMap;
    private Set<Artifact> extensionArtifacts;
    private Map<String, Artifact> extensionArtifactMap;
    private Map<String, Artifact> managedVersionMap;
    private Map<String, MavenProject> projectReferences = new HashMap<String, MavenProject>();
    private boolean executionRoot;
    private Map<String, String> moduleAdjustments;
    private ProjectBuilder mavenProjectBuilder;
    private ProjectBuildingRequest projectBuilderConfiguration;
    private RepositorySystem repositorySystem;
    private File parentFile;
    private Map<String, Object> context;
    private ClassRealm classRealm;
    private DependencyFilter extensionDependencyFilter;
    private final Set<String> lifecyclePhases = Collections.synchronizedSet( new LinkedHashSet<String>() );
    private Logger logger;
    public MavenProject()
    {
        Model model = new Model();
        model.setGroupId( EMPTY_PROJECT_GROUP_ID );
        model.setArtifactId( EMPTY_PROJECT_ARTIFACT_ID );
        model.setVersion( EMPTY_PROJECT_VERSION );
        setModel( model );
    }
    public MavenProject( Model model )
    {
        setModel( model );
    }
    @Deprecated
    public MavenProject( MavenProject project )
    {
        repositorySystem = project.repositorySystem;
        logger = project.logger;
        mavenProjectBuilder = project.mavenProjectBuilder;
        projectBuilderConfiguration = project.projectBuilderConfiguration;
        deepCopy( project );
    }
    @Deprecated
    public MavenProject( Model model, RepositorySystem repositorySystem )
    {        
        this.repositorySystem = repositorySystem;
        setModel( model );
    }
    public File getParentFile()
    {
        return parentFile;
    }
    public void setParentFile( File parentFile )
    {
        this.parentFile = parentFile;
    }
    MavenProject( RepositorySystem repositorySystem, ProjectBuilder mavenProjectBuilder,
                  ProjectBuildingRequest projectBuilderConfiguration, Logger logger )
    {
        if ( repositorySystem == null )
        {
            throw new IllegalArgumentException( "mavenTools: null" );
        }
        this.mavenProjectBuilder = mavenProjectBuilder;
        this.projectBuilderConfiguration = projectBuilderConfiguration;
        this.repositorySystem = repositorySystem;
        this.logger = logger;
    }
    @Deprecated
    public Set<Artifact> createArtifacts( ArtifactFactory artifactFactory, String inheritedScope, ArtifactFilter filter )
        throws InvalidDependencyVersionException
    {
        return MavenMetadataSource.createArtifacts( artifactFactory, getDependencies(), inheritedScope, filter, this );
    }
    public String getModulePathAdjustment( MavenProject moduleProject )
        throws IOException
    {
        String module = moduleProject.getArtifactId();
        File moduleFile = moduleProject.getFile();
        if ( moduleFile != null )
        {
            File moduleDir = moduleFile.getCanonicalFile().getParentFile();
            module = moduleDir.getName();
        }
        if ( moduleAdjustments == null )
        {
            moduleAdjustments = new HashMap<String, String>();
            List<String> modules = getModules();
            if ( modules != null )
            {
                for ( Iterator<String> it = modules.iterator(); it.hasNext(); )
                {
                    String modulePath = it.next();
                    String moduleName = modulePath;
                    if ( moduleName.endsWith( "/" ) || moduleName.endsWith( "\\" ) )
                    {
                        moduleName = moduleName.substring( 0, moduleName.length() - 1 );
                    }
                    int lastSlash = moduleName.lastIndexOf( '/' );
                    if ( lastSlash < 0 )
                    {
                        lastSlash = moduleName.lastIndexOf( '\\' );
                    }
                    String adjustment = null;
                    if ( lastSlash > -1 )
                    {
                        moduleName = moduleName.substring( lastSlash + 1 );
                        adjustment = modulePath.substring( 0, lastSlash );
                    }
                    moduleAdjustments.put( moduleName, adjustment );
                }
            }
        }
        return moduleAdjustments.get( module );
    }
    public Artifact getArtifact()
    {
        return artifact;
    }
    public void setArtifact( Artifact artifact )
    {
        this.artifact = artifact;
    }
    public Model getModel()
    {
        return model;
    }
    public MavenProject getParent()
    {
        if ( parent == null )
        {
            if ( parentFile != null )
            {
                checkProjectBuildingRequest();
                ProjectBuildingRequest request = new DefaultProjectBuildingRequest( projectBuilderConfiguration );
                request.setRemoteRepositories( getRemoteArtifactRepositories() );
                try
                {
                    parent = mavenProjectBuilder.build( parentFile, request ).getProject();
                }
                catch ( ProjectBuildingException e )
                {
                    if ( logger != null )
                    {
                        logger.debug( "Failed to build parent project for " + getId(), e );
                    }
                }
            }
            else if ( model.getParent() != null )
            {
                checkProjectBuildingRequest();
                ProjectBuildingRequest request = new DefaultProjectBuildingRequest( projectBuilderConfiguration );
                request.setRemoteRepositories( getRemoteArtifactRepositories() );
                try
                {
                    parent = mavenProjectBuilder.build( getParentArtifact(), request ).getProject();
                }
                catch ( ProjectBuildingException e )
                {
                    if ( logger != null )
                    {
                        logger.debug( "Failed to build parent project for " + getId(), e );
                    }
                }
            }
        }
        return parent;
    }
    public void setParent( MavenProject parent )
    {
        this.parent = parent;
    }
    public boolean hasParent()
    {
        return getParent() != null;
    }
    public File getFile()
    {
        return file;
    }
    public void setFile( File file )
    {
        this.file = file;
    }
    public File getBasedir()
    {
        if ( getFile() != null )
        {
            return getFile().getParentFile();
        }
        else
        {
            return null;
        }
    }
    public void setDependencies( List<Dependency> dependencies )
    {
        getModel().setDependencies( dependencies );
    }
    public List<Dependency> getDependencies()
    {
        return getModel().getDependencies();
    }
    public DependencyManagement getDependencyManagement()
    {
        return getModel().getDependencyManagement();
    }
    private void addPath( List<String> paths, String path )
    {
        if ( path != null )
        {
            path = path.trim();
            if ( path.length() > 0 )
            {
                File file = new File( path );
                if ( file.isAbsolute() )
                {
                    path = file.getAbsolutePath();
                }
                else
                {
                    path = new File( getBasedir(), path ).getAbsolutePath();
                }
                if ( !paths.contains( path ) )
                {
                    paths.add( path );
                }
            }
        }
    }
    public void addCompileSourceRoot( String path )
    {
        addPath( getCompileSourceRoots(), path );
    }
    public void addScriptSourceRoot( String path )
    {
        if ( path != null )
        {
            path = path.trim();
            if ( path.length() != 0 )
            {
                if ( !getScriptSourceRoots().contains( path ) )
                {
                    getScriptSourceRoots().add( path );
                }
            }
        }
    }
    public void addTestCompileSourceRoot( String path )
    {
        addPath( getTestCompileSourceRoots(), path );
    }
    public List<String> getCompileSourceRoots()
    {
        return compileSourceRoots;
    }
    public List<String> getScriptSourceRoots()
    {
        return scriptSourceRoots;
    }
    public List<String> getTestCompileSourceRoots()
    {
        return testCompileSourceRoots;
    }
    public List<String> getCompileClasspathElements()
        throws DependencyResolutionRequiredException
    {
        List<String> list = new ArrayList<String>( getArtifacts().size() + 1 );
        list.add( getBuild().getOutputDirectory() );
        for ( Artifact a : getArtifacts() )
        {                        
            if ( a.getArtifactHandler().isAddedToClasspath() )
            {
                if ( Artifact.SCOPE_COMPILE.equals( a.getScope() ) || Artifact.SCOPE_PROVIDED.equals( a.getScope() ) || Artifact.SCOPE_SYSTEM.equals( a.getScope() ) )
                {
                    addArtifactPath( a, list );
                }
            }
        }
        return list;
    }
    @Deprecated
    public List<Artifact> getCompileArtifacts()
    {
        List<Artifact> list = new ArrayList<Artifact>( getArtifacts().size() );
        for ( Artifact a : getArtifacts() )
        {
            if ( a.getArtifactHandler().isAddedToClasspath() )
            {
                if ( Artifact.SCOPE_COMPILE.equals( a.getScope() ) || Artifact.SCOPE_PROVIDED.equals( a.getScope() ) || Artifact.SCOPE_SYSTEM.equals( a.getScope() ) )
                {
                    list.add( a );
                }
            }
        }
        return list;
    }
    @Deprecated
    public List<Dependency> getCompileDependencies()
    {
        Set<Artifact> artifacts = getArtifacts();
        if ( ( artifacts == null ) || artifacts.isEmpty() )
        {
            return Collections.emptyList();
        }
        List<Dependency> list = new ArrayList<Dependency>( artifacts.size() );
        for ( Artifact a : getArtifacts()  )
        {
            if ( Artifact.SCOPE_COMPILE.equals( a.getScope() ) || Artifact.SCOPE_PROVIDED.equals( a.getScope() ) || Artifact.SCOPE_SYSTEM.equals( a.getScope() ) )
            {
                Dependency dependency = new Dependency();
                dependency.setArtifactId( a.getArtifactId() );
                dependency.setGroupId( a.getGroupId() );
                dependency.setVersion( a.getVersion() );
                dependency.setScope( a.getScope() );
                dependency.setType( a.getType() );
                dependency.setClassifier( a.getClassifier() );
                list.add( dependency );
            }
        }
        return list;
    }
    public List<String> getTestClasspathElements()
        throws DependencyResolutionRequiredException
    {
        List<String> list = new ArrayList<String>( getArtifacts().size() + 2 );
        list.add( getBuild().getTestOutputDirectory() );
        list.add( getBuild().getOutputDirectory() );
        for ( Artifact a : getArtifacts() )
        {            
            if ( a.getArtifactHandler().isAddedToClasspath() )
            {                
                addArtifactPath( a, list );
            }
        }
        return list;
    }
    @Deprecated
    public List<Artifact> getTestArtifacts()
    {
        List<Artifact> list = new ArrayList<Artifact>( getArtifacts().size() );
        for ( Artifact a : getArtifacts() )
        {
            if ( a.getArtifactHandler().isAddedToClasspath() )
            {
                list.add( a );
            }
        }
        return list;
    }
    @Deprecated
    public List<Dependency> getTestDependencies()
    {
        Set<Artifact> artifacts = getArtifacts();
        if ( ( artifacts == null ) || artifacts.isEmpty() )
        {
            return Collections.emptyList();
        }
        List<Dependency> list = new ArrayList<Dependency>( artifacts.size() );
        for ( Artifact a : getArtifacts()  )
        {
            Dependency dependency = new Dependency();
            dependency.setArtifactId( a.getArtifactId() );
            dependency.setGroupId( a.getGroupId() );
            dependency.setVersion( a.getVersion() );
            dependency.setScope( a.getScope() );
            dependency.setType( a.getType() );
            dependency.setClassifier( a.getClassifier() );
            list.add( dependency );
        }
        return list;
    }
    public List<String> getRuntimeClasspathElements()
        throws DependencyResolutionRequiredException
    {
        List<String> list = new ArrayList<String>( getArtifacts().size() + 1 );
        list.add( getBuild().getOutputDirectory() );
        for ( Artifact a : getArtifacts() )
        {
            if ( a.getArtifactHandler().isAddedToClasspath() )
            {
                if ( Artifact.SCOPE_COMPILE.equals( a.getScope() ) || Artifact.SCOPE_RUNTIME.equals( a.getScope() ) )
                {
                    addArtifactPath( a, list );
                }
            }
        }
        return list;
    }
    @Deprecated
    public List<Artifact> getRuntimeArtifacts()
    {
        List<Artifact> list = new ArrayList<Artifact>( getArtifacts().size() );
        for ( Artifact a : getArtifacts()  )
        {
            if ( a.getArtifactHandler().isAddedToClasspath() )
            {
                if ( Artifact.SCOPE_COMPILE.equals( a.getScope() ) || Artifact.SCOPE_RUNTIME.equals( a.getScope() ) )
                {
                    list.add( a );
                }
            }
        }
        return list;
    }
    @Deprecated
    public List<Dependency> getRuntimeDependencies()
    {
        Set<Artifact> artifacts = getArtifacts();
        if ( ( artifacts == null ) || artifacts.isEmpty() )
        {
            return Collections.emptyList();
        }
        List<Dependency> list = new ArrayList<Dependency>( artifacts.size() );
        for ( Artifact a : getArtifacts()  )
        {
            if ( Artifact.SCOPE_COMPILE.equals( a.getScope() ) || Artifact.SCOPE_RUNTIME.equals( a.getScope() ) )
            {
                Dependency dependency = new Dependency();
                dependency.setArtifactId( a.getArtifactId() );
                dependency.setGroupId( a.getGroupId() );
                dependency.setVersion( a.getVersion() );
                dependency.setScope( a.getScope() );
                dependency.setType( a.getType() );
                dependency.setClassifier( a.getClassifier() );
                list.add( dependency );
            }
        }
        return list;
    }
    public List<String> getSystemClasspathElements()
        throws DependencyResolutionRequiredException
    {
        List<String> list = new ArrayList<String>( getArtifacts().size() );
        list.add( getBuild().getOutputDirectory() );
        for ( Artifact a : getArtifacts() )
        {
            if ( a.getArtifactHandler().isAddedToClasspath() )
            {
                if ( Artifact.SCOPE_SYSTEM.equals( a.getScope() ) )
                {
                    addArtifactPath( a, list );
                }
            }
        }
        return list;
    }
    @Deprecated
    public List<Artifact> getSystemArtifacts()
    {
        List<Artifact> list = new ArrayList<Artifact>( getArtifacts().size() );
        for ( Artifact a : getArtifacts()  )
        {
            if ( a.getArtifactHandler().isAddedToClasspath() )
            {
                if ( Artifact.SCOPE_SYSTEM.equals( a.getScope() ) )
                {
                    list.add( a );
                }
            }
        }
        return list;
    }
    @Deprecated
    public List<Dependency> getSystemDependencies()
    {
        Set<Artifact> artifacts = getArtifacts();
        if ( ( artifacts == null ) || artifacts.isEmpty() )
        {
            return Collections.emptyList();
        }
        List<Dependency> list = new ArrayList<Dependency>( artifacts.size() );
        for ( Artifact a : getArtifacts()  )
        {
            if ( Artifact.SCOPE_SYSTEM.equals( a.getScope() ) )
            {
                Dependency dependency = new Dependency();
                dependency.setArtifactId( a.getArtifactId() );
                dependency.setGroupId( a.getGroupId() );
                dependency.setVersion( a.getVersion() );
                dependency.setScope( a.getScope() );
                dependency.setType( a.getType() );
                dependency.setClassifier( a.getClassifier() );
                list.add( dependency );
            }
        }
        return list;
    }
    public void setModelVersion( String pomVersion )
    {
        getModel().setModelVersion( pomVersion );
    }
    public String getModelVersion()
    {
        return getModel().getModelVersion();
    }
    public String getId()
    {
        return getModel().getId();
    }
    public void setGroupId( String groupId )
    {
        getModel().setGroupId( groupId );
    }
    public String getGroupId()
    {
        String groupId = getModel().getGroupId();
        if ( ( groupId == null ) && ( getModel().getParent() != null ) )
        {
            groupId = getModel().getParent().getGroupId();
        }
        return groupId;
    }
    public void setArtifactId( String artifactId )
    {
        getModel().setArtifactId( artifactId );
    }
    public String getArtifactId()
    {
        return getModel().getArtifactId();
    }
    public void setName( String name )
    {
        getModel().setName( name );
    }
    public String getName()
    {
        if ( getModel().getName() != null )
        {
            return getModel().getName();
        }
        else
        {
            return getArtifactId();
        }
    }
    public void setVersion( String version )
    {
        getModel().setVersion( version );
    }
    public String getVersion()
    {
        String version = getModel().getVersion();
        if ( ( version == null ) && ( getModel().getParent() != null ) )
        {
            version = getModel().getParent().getVersion();
        }
        return version;
    }
    public String getPackaging()
    {
        return getModel().getPackaging();
    }
    public void setPackaging( String packaging )
    {
        getModel().setPackaging( packaging );
    }
    public void setInceptionYear( String inceptionYear )
    {
        getModel().setInceptionYear( inceptionYear );
    }
    public String getInceptionYear()
    {
        return getModel().getInceptionYear();
    }
    public void setUrl( String url )
    {
        getModel().setUrl( url );
    }
    public String getUrl()
    {
        return getModel().getUrl();
    }
    public Prerequisites getPrerequisites()
    {
        return getModel().getPrerequisites();
    }
    public void setIssueManagement( IssueManagement issueManagement )
    {
        getModel().setIssueManagement( issueManagement );
    }
    public CiManagement getCiManagement()
    {
        return getModel().getCiManagement();
    }
    public void setCiManagement( CiManagement ciManagement )
    {
        getModel().setCiManagement( ciManagement );
    }
    public IssueManagement getIssueManagement()
    {
        return getModel().getIssueManagement();
    }
    public void setDistributionManagement( DistributionManagement distributionManagement )
    {
        getModel().setDistributionManagement( distributionManagement );
    }
    public DistributionManagement getDistributionManagement()
    {
        return getModel().getDistributionManagement();
    }
    public void setDescription( String description )
    {
        getModel().setDescription( description );
    }
    public String getDescription()
    {
        return getModel().getDescription();
    }
    public void setOrganization( Organization organization )
    {
        getModel().setOrganization( organization );
    }
    public Organization getOrganization()
    {
        return getModel().getOrganization();
    }
    public void setScm( Scm scm )
    {
        getModel().setScm( scm );
    }
    public Scm getScm()
    {
        return getModel().getScm();
    }
    public void setMailingLists( List<MailingList> mailingLists )
    {
        getModel().setMailingLists( mailingLists );
    }
    public List<MailingList> getMailingLists()
    {
        return getModel().getMailingLists();
    }
    public void addMailingList( MailingList mailingList )
    {
        getModel().addMailingList( mailingList );
    }
    public void setDevelopers( List<Developer> developers )
    {
        getModel().setDevelopers( developers );
    }
    public List<Developer> getDevelopers()
    {
        return getModel().getDevelopers();
    }
    public void addDeveloper( Developer developer )
    {
        getModel().addDeveloper( developer );
    }
    public void setContributors( List<Contributor> contributors )
    {
        getModel().setContributors( contributors );
    }
    public List<Contributor> getContributors()
    {
        return getModel().getContributors();
    }
    public void addContributor( Contributor contributor )
    {
        getModel().addContributor( contributor );
    }
    public void setBuild( Build build )
    {
        getModel().setBuild( build );
    }
    public Build getBuild()
    {
        return getModelBuild();
    }
    public List<Resource> getResources()
    {
        return getBuild().getResources();
    }
    public List<Resource> getTestResources()
    {
        return getBuild().getTestResources();
    }
    public void addResource( Resource resource )
    {
        getBuild().addResource( resource );
    }
    public void addTestResource( Resource testResource )
    {
        getBuild().addTestResource( testResource );
    }
    @Deprecated
    public void setReporting( Reporting reporting )
    {
        getModel().setReporting( reporting );
    }
    @Deprecated
    public Reporting getReporting()
    {
        return getModel().getReporting();
    }
    public void setLicenses( List<License> licenses )
    {
        getModel().setLicenses( licenses );
    }
    public List<License> getLicenses()
    {
        return getModel().getLicenses();
    }
    public void addLicense( License license )
    {
        getModel().addLicense( license );
    }
    public void setArtifacts( Set<Artifact> artifacts )
    {
        this.artifacts = artifacts;
        artifactMap = null;
    }
    public Set<Artifact> getArtifacts()
    {
        if ( artifacts == null )
        {
            if ( artifactFilter == null || resolvedArtifacts == null )
            {
                artifacts = new LinkedHashSet<Artifact>();
            }
            else
            {
                artifacts = new LinkedHashSet<Artifact>( resolvedArtifacts.size() * 2 );
                for ( Artifact artifact : resolvedArtifacts )
                {
                    if ( artifactFilter.include( artifact ) )
                    {
                        artifacts.add( artifact );
                    }
                }
            }
        }
        return artifacts;
    }
    public Map<String, Artifact> getArtifactMap()
    {
        if ( artifactMap == null )
        {
            artifactMap = ArtifactUtils.artifactMapByVersionlessId( getArtifacts() );
        }
        return artifactMap;
    }
    public void setPluginArtifacts( Set<Artifact> pluginArtifacts )
    {
        this.pluginArtifacts = pluginArtifacts;
        this.pluginArtifactMap = null;
    }
    public Set<Artifact> getPluginArtifacts()
    {
        if ( pluginArtifacts != null )
        {
            return pluginArtifacts;
        }
        pluginArtifacts = new HashSet<Artifact>();
        if ( repositorySystem != null )
        {
            for ( Plugin p : getBuildPlugins() )
            {
                Artifact artifact = repositorySystem.createPluginArtifact( p );
                if ( artifact != null )
                {
                    pluginArtifacts.add( artifact );
                }
            }
        }
        pluginArtifactMap = null;
        return pluginArtifacts;
    }
    public Map<String, Artifact> getPluginArtifactMap()
    {
        if ( pluginArtifactMap == null )
        {
            pluginArtifactMap = ArtifactUtils.artifactMapByVersionlessId( getPluginArtifacts() );
        }
        return pluginArtifactMap;
    }
    @Deprecated
    public void setReportArtifacts( Set<Artifact> reportArtifacts )
    {
        this.reportArtifacts = reportArtifacts;
        reportArtifactMap = null;
    }
    @Deprecated
    public Set<Artifact> getReportArtifacts()
    {
        if ( reportArtifacts != null )
        {
            return reportArtifacts;
        }
        reportArtifacts = new HashSet<Artifact>();
        if ( repositorySystem != null )
        {
            for ( ReportPlugin p : getReportPlugins() )
            {
                Plugin pp = new Plugin();
                pp.setGroupId( p.getGroupId() );
                pp.setArtifactId( p.getArtifactId() );
                pp.setVersion( p.getVersion() );
                Artifact artifact = repositorySystem.createPluginArtifact( pp );
                if ( artifact != null )
                {
                    reportArtifacts.add( artifact );
                }
            }
        }
        reportArtifactMap = null;
        return reportArtifacts;
    }
    @Deprecated
    public Map<String, Artifact> getReportArtifactMap()
    {
        if ( reportArtifactMap == null )
        {
            reportArtifactMap = ArtifactUtils.artifactMapByVersionlessId( getReportArtifacts() );
        }
        return reportArtifactMap;
    }
    public void setExtensionArtifacts( Set<Artifact> extensionArtifacts )
    {
        this.extensionArtifacts = extensionArtifacts;
        extensionArtifactMap = null;
    }
    public Set<Artifact> getExtensionArtifacts()
    {
        if ( extensionArtifacts != null )
        {
            return extensionArtifacts;
        }
        extensionArtifacts = new HashSet<Artifact>();
        List<Extension> extensions = getBuildExtensions();
        if ( extensions != null )
        {
            for ( Iterator<Extension> i = extensions.iterator(); i.hasNext(); )
            {
                Extension ext = i.next();
                String version;
                if ( StringUtils.isEmpty( ext.getVersion() ) )
                {
                    version = "RELEASE";
                }
                else
                {
                    version = ext.getVersion();
                }
                Artifact artifact = repositorySystem.createArtifact( ext.getGroupId(), ext.getArtifactId(), version, null, "jar" );
                if ( artifact != null )
                {
                    extensionArtifacts.add( artifact );
                }
            }
        }
        extensionArtifactMap = null;
        return extensionArtifacts;
    }
    public Map<String, Artifact> getExtensionArtifactMap()
    {
        if ( extensionArtifactMap == null )
        {
            extensionArtifactMap = ArtifactUtils.artifactMapByVersionlessId( getExtensionArtifacts() );
        }
        return extensionArtifactMap;
    }
    public void setParentArtifact( Artifact parentArtifact )
    {
        this.parentArtifact = parentArtifact;
    }
    public Artifact getParentArtifact()
    {
        if ( parentArtifact == null && model.getParent() != null )
        {
            Parent p = model.getParent();
            parentArtifact = repositorySystem.createProjectArtifact( p.getGroupId(), p.getArtifactId(), p.getVersion() );
        }
        return parentArtifact;
    }
    public List<Repository> getRepositories()
    {
        return getModel().getRepositories();
    }
    @Deprecated
    public List<ReportPlugin> getReportPlugins()
    {
        if ( getModel().getReporting() == null )
        {
            return Collections.emptyList();
        }
        return getModel().getReporting().getPlugins();
    }
    public List<Plugin> getBuildPlugins()
    {
        if ( getModel().getBuild() == null )
        {
            return Collections.emptyList();
        }
        return getModel().getBuild().getPlugins();
    }
    public List<String> getModules()
    {
        return getModel().getModules();
    }
    public PluginManagement getPluginManagement()
    {
        PluginManagement pluginMgmt = null;
        Build build = getModel().getBuild();
        if ( build != null )
        {
            pluginMgmt = build.getPluginManagement();
        }
        return pluginMgmt;
    }
    private Build getModelBuild()
    {
        Build build = getModel().getBuild();
        if ( build == null )
        {
            build = new Build();
            getModel().setBuild( build );
        }
        return build;
    }
    public void setRemoteArtifactRepositories( List<ArtifactRepository> remoteArtifactRepositories )
    {
        this.remoteArtifactRepositories = remoteArtifactRepositories;
        this.remoteProjectRepositories = RepositoryUtils.toRepos( getRemoteArtifactRepositories() );
    }
    public List<ArtifactRepository> getRemoteArtifactRepositories()
    {
        if ( remoteArtifactRepositories == null )
        {
            remoteArtifactRepositories = new ArrayList<ArtifactRepository>();
        }
        return remoteArtifactRepositories;
    }
    public void setPluginArtifactRepositories( List<ArtifactRepository> pluginArtifactRepositories )
    {
        this.pluginArtifactRepositories = pluginArtifactRepositories;
        this.remotePluginRepositories = RepositoryUtils.toRepos( getPluginArtifactRepositories() );
    }
    public List<ArtifactRepository> getPluginArtifactRepositories()
    {
        if ( pluginArtifactRepositories == null )
        {
            pluginArtifactRepositories = new ArrayList<ArtifactRepository>();
        }
        return pluginArtifactRepositories;
    }
    public ArtifactRepository getDistributionManagementArtifactRepository()
    {
        return getArtifact().isSnapshot() && ( getSnapshotArtifactRepository() != null ) ? getSnapshotArtifactRepository() : getReleaseArtifactRepository();
    }
    public List<Repository> getPluginRepositories()
    {
        return getModel().getRepositories();
    }
    public List<RemoteRepository> getRemoteProjectRepositories()
    {
        return remoteProjectRepositories;
    }
    public List<RemoteRepository> getRemotePluginRepositories()
    {
        return remotePluginRepositories;
    }
    public void setActiveProfiles( List<Profile> activeProfiles )
    {
        this.activeProfiles = activeProfiles;
    }
    public List<Profile> getActiveProfiles()
    {
        return activeProfiles;
    }
    public void setInjectedProfileIds( String source, List<String> injectedProfileIds )
    {
        if ( injectedProfileIds != null )
        {
            this.injectedProfileIds.put( source, new ArrayList<String>( injectedProfileIds ) );
        }
        else
        {
            this.injectedProfileIds.remove( source );
        }
    }
    public Map<String, List<String>> getInjectedProfileIds()
    {
        return this.injectedProfileIds;
    }
    public void addAttachedArtifact( Artifact artifact )
        throws DuplicateArtifactAttachmentException
    {
        List<Artifact> attachedArtifacts = getAttachedArtifacts();
        if ( attachedArtifacts.contains( artifact ) )
        {
            if ( logger != null )
            {
                logger.warn( "Artifact " + artifact + " already attached to project, ignoring duplicate" );
            }
            return;
        }
        getAttachedArtifacts().add( artifact );
    }
    public List<Artifact> getAttachedArtifacts()
    {
        if ( attachedArtifacts == null )
        {
            attachedArtifacts = new ArrayList<Artifact>();
        }
        return attachedArtifacts;
    }
    public Xpp3Dom getGoalConfiguration( String pluginGroupId, String pluginArtifactId, String executionId,
                                         String goalId )
    {
        Xpp3Dom dom = null;
        if ( getBuildPlugins() != null )
        {
            for ( Plugin plugin : getBuildPlugins() )
            {
                if ( pluginGroupId.equals( plugin.getGroupId() ) && pluginArtifactId.equals( plugin.getArtifactId() ) )
                {
                    dom = (Xpp3Dom) plugin.getConfiguration();
                    if ( executionId != null )
                    {
                        PluginExecution execution = plugin.getExecutionsAsMap().get( executionId );
                        if ( execution != null )
                        {
                            dom = (Xpp3Dom) execution.getConfiguration();
                        }
                    }
                    break;
                }
            }
        }
        if ( dom != null )
        {
            dom = new Xpp3Dom( dom );
        }
        return dom;
    }
    @Deprecated
    public Xpp3Dom getReportConfiguration( String pluginGroupId, String pluginArtifactId, String reportSetId )
    {
        Xpp3Dom dom = null;
        if ( getReportPlugins() != null )
        {
            for ( Iterator<ReportPlugin> iterator = getReportPlugins().iterator(); iterator.hasNext(); )
            {
                ReportPlugin plugin = iterator.next();
                if ( pluginGroupId.equals( plugin.getGroupId() ) && pluginArtifactId.equals( plugin.getArtifactId() ) )
                {
                    dom = (Xpp3Dom) plugin.getConfiguration();
                    if ( reportSetId != null )
                    {
                        ReportSet reportSet = plugin.getReportSetsAsMap().get( reportSetId );
                        if ( reportSet != null )
                        {
                            Xpp3Dom executionConfiguration = (Xpp3Dom) reportSet.getConfiguration();
                            if ( executionConfiguration != null )
                            {
                                Xpp3Dom newDom = new Xpp3Dom( executionConfiguration );
                                dom = Xpp3Dom.mergeXpp3Dom( newDom, dom );
                            }
                        }
                    }
                    break;
                }
            }
        }
        if ( dom != null )
        {
            dom = new Xpp3Dom( dom );
        }
        return dom;
    }
    public MavenProject getExecutionProject()
    {
        return ( executionProject == null ? this : executionProject );
    }
    public void setExecutionProject( MavenProject executionProject )
    {
        this.executionProject = executionProject;
    }
    public List<MavenProject> getCollectedProjects()
    {
        return collectedProjects;
    }
    public void setCollectedProjects( List<MavenProject> collectedProjects )
    {
        this.collectedProjects = collectedProjects;
    }
    public Set<Artifact> getDependencyArtifacts()
    {
        return dependencyArtifacts;
    }
    public void setDependencyArtifacts( Set<Artifact> dependencyArtifacts )
    {
        this.dependencyArtifacts = dependencyArtifacts;
    }
    public void setReleaseArtifactRepository( ArtifactRepository releaseArtifactRepository )
    {
        this.releaseArtifactRepository = releaseArtifactRepository;
    }
    public void setSnapshotArtifactRepository( ArtifactRepository snapshotArtifactRepository )
    {
        this.snapshotArtifactRepository = snapshotArtifactRepository;
    }
    public void setOriginalModel( Model originalModel )
    {
        this.originalModel = originalModel;
    }
    public Model getOriginalModel()
    {
        return originalModel;
    }
    public void setManagedVersionMap( Map<String, Artifact> map )
    {
        managedVersionMap = map;
    }
    public Map<String, Artifact> getManagedVersionMap()
    {
        if ( managedVersionMap != null )
        {
            return managedVersionMap;
        }
        Map<String, Artifact> map = null;
        if ( repositorySystem != null )
        {
            List<Dependency> deps;
            DependencyManagement dependencyManagement = getDependencyManagement();
            if ( ( dependencyManagement != null ) && ( ( deps = dependencyManagement.getDependencies() ) != null ) && ( deps.size() > 0 ) )
            {
                map = new HashMap<String, Artifact>();
                for ( Iterator<Dependency> i = dependencyManagement.getDependencies().iterator(); i.hasNext(); )
                {
                    Dependency d = i.next();
                    Artifact artifact = repositorySystem.createDependencyArtifact( d );
                    if ( artifact == null )
                    {
                        map = Collections.emptyMap();
                    }
                    map.put( d.getManagementKey(), artifact );
                }
            }
            else
            {
                map = Collections.emptyMap();
            }
        }
        managedVersionMap = map;
        return managedVersionMap;
    }
    @Override
    public boolean equals( Object other )
    {
        if ( other == this )
        {
            return true;
        }
        else if ( !( other instanceof MavenProject ) )
        {
            return false;
        }
        MavenProject that = (MavenProject) other;
        return eq( getArtifactId(), that.getArtifactId() )
            && eq( getGroupId(), that.getGroupId() )
            && eq( getVersion(), that.getVersion() );
    }
    private static <T> boolean eq( T s1, T s2 )
    {
        return ( s1 != null ) ? s1.equals( s2 ) : s2 == null;
    }
    @Override
    public int hashCode()
    {
        int hash = 17;
        hash = 31 * hash + getGroupId().hashCode();
        hash = 31 * hash + getArtifactId().hashCode();
        hash = 31 * hash + getVersion().hashCode();
        return hash;
    }
    public List<Extension> getBuildExtensions()
    {
        Build build = getBuild();
        if ( ( build == null ) || ( build.getExtensions() == null ) )
        {
            return Collections.emptyList();
        }
        else
        {
            return build.getExtensions();
        }
    }
    public void addProjectReference( MavenProject project )
    {
        projectReferences.put( getProjectReferenceId( project.getGroupId(), project.getArtifactId(), project.getVersion() ), project );
    }
    @Deprecated
    public void attachArtifact( String type, String classifier, File file )
    {
    }
    public Properties getProperties()
    {
        return getModel().getProperties();
    }
    public List<String> getFilters()
    {
        return getBuild().getFilters();
    }
    public Map<String, MavenProject> getProjectReferences()
    {
        return projectReferences;
    }
    public boolean isExecutionRoot()
    {
        return executionRoot;
    }
    public void setExecutionRoot( boolean executionRoot )
    {
        this.executionRoot = executionRoot;
    }
    public String getDefaultGoal()
    {
        return getBuild() != null ? getBuild().getDefaultGoal() : null;
    }
    public Plugin getPlugin( String pluginKey )
    {
        return getBuild().getPluginsAsMap().get( pluginKey );
    }
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder( 128 );
        sb.append( "MavenProject: " );
        sb.append( getGroupId() );
        sb.append( ":" );
        sb.append( getArtifactId() );
        sb.append( ":" );
        sb.append( getVersion() );
        sb.append( " @ " );
        try
        {
            sb.append( getFile().getPath() );
        }
        catch ( NullPointerException e )
        {
        }
        return sb.toString();
    }
    @Deprecated
    public void writeModel( Writer writer )
        throws IOException
    {
        MavenXpp3Writer pomWriter = new MavenXpp3Writer();
        pomWriter.write( writer, getModel() );
    }
    @Deprecated
    public void writeOriginalModel( Writer writer )
        throws IOException
    {
        MavenXpp3Writer pomWriter = new MavenXpp3Writer();
        pomWriter.write( writer, getOriginalModel() );
    }
    @Override
    public MavenProject clone()
    {
        MavenProject clone;
        try
        {
            clone = (MavenProject) super.clone();
        }
        catch ( CloneNotSupportedException e )
        {
            throw new UnsupportedOperationException( e );
        }
        clone.deepCopy( this );
        return clone;
    }
    protected void setModel( Model model )
    {
        this.model = model;
    }
    protected void setAttachedArtifacts( List<Artifact> attachedArtifacts )
    {
        this.attachedArtifacts = attachedArtifacts;
    }
    protected void setCompileSourceRoots( List<String> compileSourceRoots )
    {
        this.compileSourceRoots = compileSourceRoots;
    }
    protected void setTestCompileSourceRoots( List<String> testCompileSourceRoots )
    {
        this.testCompileSourceRoots = testCompileSourceRoots;
    }
    protected void setScriptSourceRoots( List<String> scriptSourceRoots )
    {
        this.scriptSourceRoots = scriptSourceRoots;
    }
    protected ArtifactRepository getReleaseArtifactRepository()
    {
        if ( releaseArtifactRepository == null )
        {
            if ( getDistributionManagement() != null && getDistributionManagement().getRepository() != null )
            {
                checkProjectBuildingRequest();
                try
                {
                    ArtifactRepository repo =
                        repositorySystem.buildArtifactRepository( getDistributionManagement().getRepository() );
                    repositorySystem.injectProxy( projectBuilderConfiguration.getRepositorySession(),
                                                  Arrays.asList( repo ) );
                    repositorySystem.injectAuthentication( projectBuilderConfiguration.getRepositorySession(),
                                                           Arrays.asList( repo ) );
                    setReleaseArtifactRepository( repo );
                }
                catch ( InvalidRepositoryException e )
                {
                    if ( logger != null )
                    {
                        logger.debug( "Failed to create release distribution repository for " + getId(), e );
                    }
                }
            }
        }
        return releaseArtifactRepository;
    }
    protected ArtifactRepository getSnapshotArtifactRepository()
    {
        if ( snapshotArtifactRepository == null )
        {
            if ( getDistributionManagement() != null && getDistributionManagement().getSnapshotRepository() != null )
            {
                checkProjectBuildingRequest();
                try
                {
                    ArtifactRepository repo =
                        repositorySystem.buildArtifactRepository( getDistributionManagement().getSnapshotRepository() );
                    repositorySystem.injectProxy( projectBuilderConfiguration.getRepositorySession(),
                                                  Arrays.asList( repo ) );
                    repositorySystem.injectAuthentication( projectBuilderConfiguration.getRepositorySession(),
                                                           Arrays.asList( repo ) );
                    setSnapshotArtifactRepository( repo );
                }
                catch ( InvalidRepositoryException e )
                {
                    if ( logger != null )
                    {
                        logger.debug( "Failed to create snapshot distribution repository for " + getId(), e );
                    }
                }
            }
        }
        return snapshotArtifactRepository;
    }
    @Deprecated
    public Artifact replaceWithActiveArtifact( Artifact pluginArtifact )
    {
        return pluginArtifact;
    }
    private void deepCopy( MavenProject project )
    {
        setFile( project.getFile() );
        if ( project.getDependencyArtifacts() != null )
        {
            setDependencyArtifacts( Collections.unmodifiableSet( project.getDependencyArtifacts() ) );
        }
        if ( project.getArtifacts() != null )
        {
            setArtifacts( Collections.unmodifiableSet( project.getArtifacts() ) );
        }
        if ( project.getParentFile() != null )
        {
            parentFile = new File( project.getParentFile().getAbsolutePath() );
        }
        if ( project.getPluginArtifacts() != null )
        {
            setPluginArtifacts( Collections.unmodifiableSet( project.getPluginArtifacts() ) );
        }
        if ( project.getReportArtifacts() != null )
        {
            setReportArtifacts( Collections.unmodifiableSet( project.getReportArtifacts() ) );
        }
        if ( project.getExtensionArtifacts() != null )
        {
            setExtensionArtifacts( Collections.unmodifiableSet( project.getExtensionArtifacts() ) );
        }
        setParentArtifact( ( project.getParentArtifact() ) );
        if ( project.getRemoteArtifactRepositories() != null )
        {
            setRemoteArtifactRepositories( Collections.unmodifiableList( project.getRemoteArtifactRepositories() ) );
        }
        if ( project.getPluginArtifactRepositories() != null )
        {
            setPluginArtifactRepositories( ( Collections.unmodifiableList( project.getPluginArtifactRepositories() ) ) );
        }
        if ( project.getActiveProfiles() != null )
        {
            setActiveProfiles( ( Collections.unmodifiableList( project.getActiveProfiles() ) ) );
        }
        if ( project.getAttachedArtifacts() != null )
        {
            setAttachedArtifacts( new ArrayList<Artifact>( project.getAttachedArtifacts() ) );
        }
        if ( project.getCompileSourceRoots() != null )
        {
            setCompileSourceRoots( ( new ArrayList<String>( project.getCompileSourceRoots() ) ) );
        }
        if ( project.getTestCompileSourceRoots() != null )
        {
            setTestCompileSourceRoots( ( new ArrayList<String>( project.getTestCompileSourceRoots() ) ) );
        }
        if ( project.getScriptSourceRoots() != null )
        {
            setScriptSourceRoots( ( new ArrayList<String>( project.getScriptSourceRoots() ) ) );
        }
        if ( project.getModel() != null )
        {
            setModel( project.getModel().clone() );
        }
        if ( project.getOriginalModel() != null )
        {
            setOriginalModel( project.getOriginalModel() );
        }
        setExecutionRoot( project.isExecutionRoot() );
        if ( project.getArtifact() != null )
        {
            setArtifact( ArtifactUtils.copyArtifact( project.getArtifact() ) );
        }
        if ( project.getManagedVersionMap() != null )
        {
            setManagedVersionMap( new HashMap<String, Artifact>( project.getManagedVersionMap() ) );
        }
        lifecyclePhases.addAll( project.lifecyclePhases );
    }
    private void addArtifactPath( Artifact artifact, List<String> classpath )
    {
        File file = artifact.getFile();
        if ( file != null )
        {
            classpath.add( file.getPath() );
        }
    }
    private static String getProjectReferenceId( String groupId, String artifactId, String version )
    {
        StringBuilder buffer = new StringBuilder( 128 );
        buffer.append( groupId ).append( ':' ).append( artifactId ).append( ':' ).append( version );
        return buffer.toString();
    }
    public void setContextValue( String key, Object value )
    {
        if ( context == null )
        {
            context = new HashMap<String, Object>();
        }
        if ( value != null )
        {
            context.put( key, value );
        }
        else
        {
            context.remove( key );
        }
    }
    public Object getContextValue( String key )
    {
        if ( context == null )
        {
            return null;
        }
        return context.get( key );
    }
    public void setClassRealm( ClassRealm classRealm )
    {
        this.classRealm = classRealm;
    }
    public ClassRealm getClassRealm()
    {
        return classRealm;
    }
    public void setExtensionDependencyFilter( DependencyFilter extensionDependencyFilter )
    {
        this.extensionDependencyFilter = extensionDependencyFilter;
    }
    public DependencyFilter getExtensionDependencyFilter()
    {
        return extensionDependencyFilter;
    }
    public void setResolvedArtifacts( Set<Artifact> artifacts )
    {
        this.resolvedArtifacts = ( artifacts != null ) ? artifacts : Collections.<Artifact> emptySet();
        this.artifacts = null;
        this.artifactMap = null;
    }
    public void setArtifactFilter( ArtifactFilter artifactFilter )
    {
        this.artifactFilter = artifactFilter;
        this.artifacts = null;
        this.artifactMap = null;
    }
    public boolean hasLifecyclePhase( String phase )
    {
        return lifecyclePhases.contains( phase );
    }
    public void addLifecyclePhase( String lifecyclePhase )
    {
        lifecyclePhases.add( lifecyclePhase );
    }
    public ProjectBuildingRequest getProjectBuildingRequest()
    {
        return projectBuilderConfiguration;
    }
    public void setProjectBuildingRequest( ProjectBuildingRequest projectBuildingRequest )
    {
        projectBuilderConfiguration = projectBuildingRequest;
    }
    private void checkProjectBuildingRequest()
    {
        if ( projectBuilderConfiguration == null )
        {
            throw new IllegalStateException( "project building request missing" );
        }
    }
}

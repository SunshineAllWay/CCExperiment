package org.apache.maven.project;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.maven.artifact.InvalidRepositoryException;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.classrealm.ClassRealmManager;
import org.apache.maven.model.Build;
import org.apache.maven.model.Extension;
import org.apache.maven.model.Model;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.Repository;
import org.apache.maven.plugin.ExtensionRealmCache;
import org.apache.maven.plugin.PluginArtifactsCache;
import org.apache.maven.plugin.PluginResolutionException;
import org.apache.maven.plugin.internal.PluginDependenciesResolver;
import org.apache.maven.plugin.version.DefaultPluginVersionRequest;
import org.apache.maven.plugin.version.PluginVersionRequest;
import org.apache.maven.plugin.version.PluginVersionResolutionException;
import org.apache.maven.plugin.version.PluginVersionResolver;
import org.apache.maven.repository.RepositorySystem;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.Logger;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.graph.DependencyFilter;
import org.sonatype.aether.graph.DependencyNode;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.util.filter.ExclusionsDependencyFilter;
import org.sonatype.aether.util.graph.PreorderNodeListGenerator;
@Component( role = ProjectBuildingHelper.class )
public class DefaultProjectBuildingHelper
    implements ProjectBuildingHelper
{
    @Requirement
    private Logger logger;
    @Requirement
    private PlexusContainer container;
    @Requirement
    private ClassRealmManager classRealmManager;
    @Requirement
    private PluginArtifactsCache pluginArtifactsCache;
    @Requirement
    private ExtensionRealmCache extensionRealmCache;
    @Requirement
    private ProjectRealmCache projectRealmCache;
    @Requirement
    private RepositorySystem repositorySystem;
    @Requirement
    private PluginVersionResolver pluginVersionResolver;
    @Requirement
    private PluginDependenciesResolver pluginDependenciesResolver;
    private ExtensionDescriptorBuilder extensionDescriptorBuilder = new ExtensionDescriptorBuilder();
    public List<ArtifactRepository> createArtifactRepositories( List<Repository> pomRepositories,
                                                                List<ArtifactRepository> externalRepositories,
                                                                ProjectBuildingRequest request )
        throws InvalidRepositoryException
    {
        List<ArtifactRepository> internalRepositories = new ArrayList<ArtifactRepository>();
        for ( Repository repository : pomRepositories )
        {
            internalRepositories.add( repositorySystem.buildArtifactRepository( repository ) );
        }
        repositorySystem.injectMirror( request.getRepositorySession(), internalRepositories );
        repositorySystem.injectProxy( request.getRepositorySession(), internalRepositories );
        repositorySystem.injectAuthentication( request.getRepositorySession(), internalRepositories );
        List<ArtifactRepository> dominantRepositories;
        List<ArtifactRepository> recessiveRepositories;
        if ( ProjectBuildingRequest.RepositoryMerging.REQUEST_DOMINANT.equals( request.getRepositoryMerging() ) )
        {
            dominantRepositories = externalRepositories;
            recessiveRepositories = internalRepositories;
        }
        else
        {
            dominantRepositories = internalRepositories;
            recessiveRepositories = externalRepositories;
        }
        List<ArtifactRepository> artifactRepositories = new ArrayList<ArtifactRepository>();
        Collection<String> repoIds = new HashSet<String>();
        if ( dominantRepositories != null )
        {
            for ( ArtifactRepository repository : dominantRepositories )
            {
                repoIds.add( repository.getId() );
                artifactRepositories.add( repository );
            }
        }
        if ( recessiveRepositories != null )
        {
            for ( ArtifactRepository repository : recessiveRepositories )
            {
                if ( repoIds.add( repository.getId() ) )
                {
                    artifactRepositories.add( repository );
                }
            }
        }
        artifactRepositories = repositorySystem.getEffectiveRepositories( artifactRepositories );
        return artifactRepositories;
    }
    public synchronized ProjectRealmCache.CacheRecord createProjectRealm( MavenProject project, Model model,
                                                                          ProjectBuildingRequest request )
        throws PluginResolutionException, PluginVersionResolutionException
    {
        ClassRealm projectRealm = null;
        List<Plugin> extensionPlugins = new ArrayList<Plugin>();
        Build build = model.getBuild();
        if ( build != null )
        {
            for ( Extension extension : build.getExtensions() )
            {
                Plugin plugin = new Plugin();
                plugin.setGroupId( extension.getGroupId() );
                plugin.setArtifactId( extension.getArtifactId() );
                plugin.setVersion( extension.getVersion() );
                extensionPlugins.add( plugin );
            }
            for ( Plugin plugin : build.getPlugins() )
            {
                if ( plugin.isExtensions() )
                {
                    extensionPlugins.add( plugin );
                }
            }
        }
        if ( extensionPlugins.isEmpty() )
        {
            if ( logger.isDebugEnabled() )
            {
                logger.debug( "Extension realms for project " + model.getId() + ": (none)" );
            }
            return new ProjectRealmCache.CacheRecord( null, null );
        }
        List<ClassRealm> extensionRealms = new ArrayList<ClassRealm>();
        Map<ClassRealm, List<String>> exportedPackages = new HashMap<ClassRealm, List<String>>();
        Map<ClassRealm, List<String>> exportedArtifacts = new HashMap<ClassRealm, List<String>>();
        List<Artifact> publicArtifacts = new ArrayList<Artifact>();
        for ( Plugin plugin : extensionPlugins )
        {
            if ( plugin.getVersion() == null )
            {
                PluginVersionRequest versionRequest =
                    new DefaultPluginVersionRequest( plugin, request.getRepositorySession(),
                                                     project.getRemotePluginRepositories() );
                plugin.setVersion( pluginVersionResolver.resolve( versionRequest ).getVersion() );
            }
            List<Artifact> artifacts;
            PluginArtifactsCache.Key cacheKey =
                pluginArtifactsCache.createKey( plugin, null, project.getRemotePluginRepositories(),
                                                request.getRepositorySession() );
            PluginArtifactsCache.CacheRecord recordArtifacts = pluginArtifactsCache.get( cacheKey );
            if ( recordArtifacts != null )
            {
                artifacts = recordArtifacts.artifacts;
            }
            else
            {
                try
                {
                    artifacts = resolveExtensionArtifacts( plugin, project.getRemotePluginRepositories(), request );
                    recordArtifacts = pluginArtifactsCache.put( cacheKey, artifacts );
                }
                catch ( PluginResolutionException e )
                {
                    pluginArtifactsCache.put( cacheKey, e );
                    pluginArtifactsCache.register( project, recordArtifacts );
                    throw e;
                }
            }
            pluginArtifactsCache.register( project, recordArtifacts );
            ClassRealm extensionRealm;
            ExtensionDescriptor extensionDescriptor = null;
            ExtensionRealmCache.CacheRecord recordRealm = extensionRealmCache.get( artifacts );
            if ( recordRealm != null )
            {
                extensionRealm = recordRealm.realm;
                extensionDescriptor = recordRealm.desciptor;
            }
            else
            {
                extensionRealm = classRealmManager.createExtensionRealm( plugin, artifacts );
                try
                {
                    container.discoverComponents( extensionRealm );
                }
                catch ( Exception e )
                {
                    throw new IllegalStateException( "Failed to discover components in extension realm "
                        + extensionRealm.getId(), e );
                }
                Artifact extensionArtifact = artifacts.get( 0 );
                try
                {
                    extensionDescriptor = extensionDescriptorBuilder.build( extensionArtifact.getFile() );
                }
                catch ( IOException e )
                {
                    String message = "Invalid extension descriptor for " + plugin.getId() + ": " + e.getMessage();
                    if ( logger.isDebugEnabled() )
                    {
                        logger.error( message, e );
                    }
                    else
                    {
                        logger.error( message );
                    }
                }
                recordRealm = extensionRealmCache.put( artifacts, extensionRealm, extensionDescriptor );
            }
            extensionRealmCache.register( project, recordRealm );
            extensionRealms.add( extensionRealm );
            if ( extensionDescriptor != null )
            {
                exportedPackages.put( extensionRealm, extensionDescriptor.getExportedPackages() );
                exportedArtifacts.put( extensionRealm, extensionDescriptor.getExportedArtifacts() );
            }
            if ( !plugin.isExtensions() && artifacts.size() == 2 && artifacts.get( 0 ).getFile() != null
                && "plexus-utils".equals( artifacts.get( 1 ).getArtifactId() ) )
            {
                publicArtifacts.add( artifacts.get( 0 ) );
            }
        }
        if ( logger.isDebugEnabled() )
        {
            logger.debug( "Extension realms for project " + model.getId() + ": " + extensionRealms );
        }
        ProjectRealmCache.CacheRecord record = projectRealmCache.get( extensionRealms );
        if ( record == null )
        {
            projectRealm = classRealmManager.createProjectRealm( model, publicArtifacts );
            Set<String> exclusions = new LinkedHashSet<String>();
            for ( ClassRealm extensionRealm : extensionRealms )
            {
                List<String> excludes = exportedArtifacts.get( extensionRealm );
                if ( excludes != null )
                {
                    exclusions.addAll( excludes );
                }
                List<String> exports = exportedPackages.get( extensionRealm );
                if ( exports == null || exports.isEmpty() )
                {
                    exports = Arrays.asList( extensionRealm.getId() );
                }
                for ( String export : exports )
                {
                    projectRealm.importFrom( extensionRealm, export );
                }
            }
            DependencyFilter extensionArtifactFilter = null;
            if ( !exclusions.isEmpty() )
            {
                extensionArtifactFilter = new ExclusionsDependencyFilter( exclusions );
            }
            record = projectRealmCache.put( extensionRealms, projectRealm, extensionArtifactFilter );
        }
        projectRealmCache.register( project, record );
        return record;
    }
    private List<Artifact> resolveExtensionArtifacts( Plugin extensionPlugin, List<RemoteRepository> repositories,
                                                      ProjectBuildingRequest request )
        throws PluginResolutionException
    {
        DependencyNode root =
            pluginDependenciesResolver.resolve( extensionPlugin, null, null, repositories,
                                                request.getRepositorySession() );
        PreorderNodeListGenerator nlg = new PreorderNodeListGenerator();
        root.accept( nlg );
        return nlg.getArtifacts( false );
    }
    public void selectProjectRealm( MavenProject project )
    {
        ClassLoader projectRealm = project.getClassRealm();
        if ( projectRealm == null )
        {
            projectRealm = classRealmManager.getCoreRealm();
        }
        Thread.currentThread().setContextClassLoader( projectRealm );
    }
}

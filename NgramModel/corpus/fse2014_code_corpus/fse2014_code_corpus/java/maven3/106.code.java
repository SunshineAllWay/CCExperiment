package org.apache.maven.project.inheritance;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.TreeMap;
import org.apache.maven.model.Build;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.DependencyManagement;
import org.apache.maven.model.DeploymentRepository;
import org.apache.maven.model.DistributionManagement;
import org.apache.maven.model.Extension;
import org.apache.maven.model.Model;
import org.apache.maven.model.PluginManagement;
import org.apache.maven.model.ReportPlugin;
import org.apache.maven.model.ReportSet;
import org.apache.maven.model.Reporting;
import org.apache.maven.model.Resource;
import org.apache.maven.model.Scm;
import org.apache.maven.model.Site;
import org.apache.maven.project.ModelUtils;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.xml.Xpp3Dom;
@Component( role = ModelInheritanceAssembler.class )
public class DefaultModelInheritanceAssembler
    implements ModelInheritanceAssembler
{
    @SuppressWarnings( "unchecked" )
    public void assembleBuildInheritance( Build childBuild, Build parentBuild, boolean handleAsInheritance )
    {
        if ( childBuild.getSourceDirectory() == null )
        {
            childBuild.setSourceDirectory( parentBuild.getSourceDirectory() );
        }
        if ( childBuild.getScriptSourceDirectory() == null )
        {
            childBuild.setScriptSourceDirectory( parentBuild.getScriptSourceDirectory() );
        }
        if ( childBuild.getTestSourceDirectory() == null )
        {
            childBuild.setTestSourceDirectory( parentBuild.getTestSourceDirectory() );
        }
        if ( childBuild.getOutputDirectory() == null )
        {
            childBuild.setOutputDirectory( parentBuild.getOutputDirectory() );
        }
        if ( childBuild.getTestOutputDirectory() == null )
        {
            childBuild.setTestOutputDirectory( parentBuild.getTestOutputDirectory() );
        }
        mergeExtensionLists( childBuild, parentBuild );
        if ( childBuild.getDirectory() == null )
        {
            childBuild.setDirectory( parentBuild.getDirectory() );
        }
        if ( childBuild.getDefaultGoal() == null )
        {
            childBuild.setDefaultGoal( parentBuild.getDefaultGoal() );
        }
        if ( childBuild.getFinalName() == null )
        {
            childBuild.setFinalName( parentBuild.getFinalName() );
        }
        ModelUtils.mergeFilterLists( childBuild.getFilters(), parentBuild.getFilters() );
        List<Resource> resources = childBuild.getResources();
        if ( ( resources == null ) || resources.isEmpty() )
        {
            childBuild.setResources( parentBuild.getResources() );
        }
        resources = childBuild.getTestResources();
        if ( ( resources == null ) || resources.isEmpty() )
        {
            childBuild.setTestResources( parentBuild.getTestResources() );
        }
        ModelUtils.mergePluginLists( childBuild, parentBuild, handleAsInheritance );
        PluginManagement dominantPM = childBuild.getPluginManagement();
        PluginManagement recessivePM = parentBuild.getPluginManagement();
        if ( ( dominantPM == null ) && ( recessivePM != null ) )
        {
            childBuild.setPluginManagement( recessivePM );
        }
        else
        {
            ModelUtils.mergePluginLists( childBuild.getPluginManagement(), parentBuild.getPluginManagement(), false );
        }
    }
    private void assembleScmInheritance( Model child, Model parent, String childPathAdjustment, boolean appendPaths )
    {
        if ( parent.getScm() != null )
        {
            Scm parentScm = parent.getScm();
            Scm childScm = child.getScm();
            if ( childScm == null )
            {
                childScm = new Scm();
                child.setScm( childScm );
            }
            if ( StringUtils.isEmpty( childScm.getConnection() ) && !StringUtils.isEmpty( parentScm.getConnection() ) )
            {
                childScm.setConnection(
                    appendPath( parentScm.getConnection(), child.getArtifactId(), childPathAdjustment, appendPaths ) );
            }
            if ( StringUtils.isEmpty( childScm.getDeveloperConnection() )
                && !StringUtils.isEmpty( parentScm.getDeveloperConnection() ) )
            {
                childScm
                    .setDeveloperConnection( appendPath( parentScm.getDeveloperConnection(), child.getArtifactId(),
                                                         childPathAdjustment, appendPaths ) );
            }
            if ( StringUtils.isEmpty( childScm.getUrl() ) && !StringUtils.isEmpty( parentScm.getUrl() ) )
            {
                childScm.setUrl(
                    appendPath( parentScm.getUrl(), child.getArtifactId(), childPathAdjustment, appendPaths ) );
            }
        }
    }
    public void copyModel( Model dest, Model source )
    {
        assembleModelInheritance( dest, source, null, false );
    }
    public void assembleModelInheritance( Model child, Model parent, String childPathAdjustment )
    {
        assembleModelInheritance( child, parent, childPathAdjustment, true );
    }
    public void assembleModelInheritance( Model child, Model parent )
    {
        assembleModelInheritance( child, parent, null, true );
    }
    private void assembleModelInheritance( Model child, Model parent, String childPathAdjustment, boolean appendPaths )
    {
        if ( parent == null )
        {
            return;
        }
        if ( child.getGroupId() == null )
        {
            child.setGroupId( parent.getGroupId() );
        }
        if ( child.getVersion() == null )
        {
            if ( child.getParent() != null )
            {
                child.setVersion( child.getParent().getVersion() );
            }
        }
        if ( child.getInceptionYear() == null )
        {
            child.setInceptionYear( parent.getInceptionYear() );
        }
        if ( child.getUrl() == null )
        {
            if ( parent.getUrl() != null )
            {
                child.setUrl( appendPath( parent.getUrl(), child.getArtifactId(), childPathAdjustment, appendPaths ) );
            }
            else
            {
                child.setUrl( parent.getUrl() );
            }
        }
        assembleDistributionInheritence( child, parent, childPathAdjustment, appendPaths );
        if ( child.getIssueManagement() == null )
        {
            child.setIssueManagement( parent.getIssueManagement() );
        }
        if ( child.getDescription() == null )
        {
            child.setDescription( parent.getDescription() );
        }
        if ( child.getOrganization() == null )
        {
            child.setOrganization( parent.getOrganization() );
        }
        assembleScmInheritance( child, parent, childPathAdjustment, appendPaths );
        if ( child.getCiManagement() == null )
        {
            child.setCiManagement( parent.getCiManagement() );
        }
        if ( child.getDevelopers().size() == 0 )
        {
            child.setDevelopers( parent.getDevelopers() );
        }
        if ( child.getLicenses().size() == 0 )
        {
            child.setLicenses( parent.getLicenses() );
        }
        if ( child.getContributors().size() == 0 )
        {
            child.setContributors( parent.getContributors() );
        }
        if ( child.getMailingLists().size() == 0 )
        {
            child.setMailingLists( parent.getMailingLists() );
        }
        assembleBuildInheritance( child, parent );
        assembleDependencyInheritance( child, parent );
        child.setRepositories( ModelUtils.mergeRepositoryLists( child.getRepositories(), parent.getRepositories() ) );
        assembleReportingInheritance( child, parent );
        assembleDependencyManagementInheritance( child, parent );
        Properties props = new Properties();
        props.putAll( parent.getProperties() );
        props.putAll( child.getProperties() );
        child.setProperties( props );
    }
    @SuppressWarnings( "unchecked" )
    private void assembleDependencyManagementInheritance( Model child, Model parent )
    {
        DependencyManagement parentDepMgmt = parent.getDependencyManagement();
        DependencyManagement childDepMgmt = child.getDependencyManagement();
        if ( parentDepMgmt != null )
        {
            if ( childDepMgmt == null )
            {
                child.setDependencyManagement( parentDepMgmt );
            }
            else
            {
                List<Dependency> childDeps = childDepMgmt.getDependencies();
                Map<String, Dependency> mappedChildDeps = new TreeMap<String, Dependency>();
                for ( Iterator<Dependency> it = childDeps.iterator(); it.hasNext(); )
                {
                    Dependency dep = it.next();
                    mappedChildDeps.put( dep.getManagementKey(), dep );
                }
                for ( Iterator<Dependency> it = parentDepMgmt.getDependencies().iterator(); it.hasNext(); )
                {
                    Dependency dep = it.next();
                    if ( !mappedChildDeps.containsKey( dep.getManagementKey() ) )
                    {
                        childDepMgmt.addDependency( dep );
                    }
                }
            }
        }
    }
    private void assembleReportingInheritance( Model child, Model parent )
    {
        Reporting childReporting = child.getReporting();
        Reporting parentReporting = parent.getReporting();
        if ( parentReporting != null )
        {
            if ( childReporting == null )
            {
                childReporting = new Reporting();
                child.setReporting( childReporting );
            }
            childReporting.setExcludeDefaults( parentReporting.isExcludeDefaults() );
            if ( StringUtils.isEmpty( childReporting.getOutputDirectory() ) )
            {
                childReporting.setOutputDirectory( parentReporting.getOutputDirectory() );
            }
            mergeReportPluginLists( childReporting, parentReporting, true );
        }
    }
    private static void mergeReportPluginLists( Reporting child, Reporting parent, boolean handleAsInheritance )
    {
        if ( ( child == null ) || ( parent == null ) )
        {
            return;
        }
        List parentPlugins = parent.getPlugins();
        if ( ( parentPlugins != null ) && !parentPlugins.isEmpty() )
        {
            Map assembledPlugins = new TreeMap();
            Map childPlugins = child.getReportPluginsAsMap();
            for ( Iterator it = parentPlugins.iterator(); it.hasNext(); )
            {
                ReportPlugin parentPlugin = (ReportPlugin) it.next();
                String parentInherited = parentPlugin.getInherited();
                if ( !handleAsInheritance || ( parentInherited == null )
                    || Boolean.valueOf( parentInherited ).booleanValue() )
                {
                    ReportPlugin assembledPlugin = parentPlugin;
                    ReportPlugin childPlugin = (ReportPlugin) childPlugins.get( parentPlugin.getKey() );
                    if ( childPlugin != null )
                    {
                        assembledPlugin = childPlugin;
                        mergeReportPluginDefinitions( childPlugin, parentPlugin, handleAsInheritance );
                    }
                    if ( handleAsInheritance && ( parentInherited == null ) )
                    {
                        assembledPlugin.unsetInheritanceApplied();
                    }
                    assembledPlugins.put( assembledPlugin.getKey(), assembledPlugin );
                }
            }
            for ( Iterator it = childPlugins.values().iterator(); it.hasNext(); )
            {
                ReportPlugin childPlugin = (ReportPlugin) it.next();
                if ( !assembledPlugins.containsKey( childPlugin.getKey() ) )
                {
                    assembledPlugins.put( childPlugin.getKey(), childPlugin );
                }
            }
            child.setPlugins( new ArrayList( assembledPlugins.values() ) );
            child.flushReportPluginMap();
        }
    }
    private static void mergeReportSetDefinitions( ReportSet child, ReportSet parent )
    {
        List parentReports = parent.getReports();
        List childReports = child.getReports();
        List reports = new ArrayList();
        if ( ( childReports != null ) && !childReports.isEmpty() )
        {
            reports.addAll( childReports );
        }
        if ( parentReports != null )
        {
            for ( Iterator i = parentReports.iterator(); i.hasNext(); )
            {
                String report = (String) i.next();
                if ( !reports.contains( report ) )
                {
                    reports.add( report );
                }
            }
        }
        child.setReports( reports );
        Xpp3Dom childConfiguration = (Xpp3Dom) child.getConfiguration();
        Xpp3Dom parentConfiguration = (Xpp3Dom) parent.getConfiguration();
        childConfiguration = Xpp3Dom.mergeXpp3Dom( childConfiguration, parentConfiguration );
        child.setConfiguration( childConfiguration );
    }
    public static void mergeReportPluginDefinitions( ReportPlugin child, ReportPlugin parent,
                                                     boolean handleAsInheritance )
    {
        if ( ( child == null ) || ( parent == null ) )
        {
            return;
        }
        if ( ( child.getVersion() == null ) && ( parent.getVersion() != null ) )
        {
            child.setVersion( parent.getVersion() );
        }
        String parentInherited = parent.getInherited();
        boolean parentIsInherited = ( parentInherited == null ) || Boolean.valueOf( parentInherited ).booleanValue();
        List parentReportSets = parent.getReportSets();
        if ( ( parentReportSets != null ) && !parentReportSets.isEmpty() )
        {
            Map assembledReportSets = new TreeMap();
            Map childReportSets = child.getReportSetsAsMap();
            for ( Iterator it = parentReportSets.iterator(); it.hasNext(); )
            {
                ReportSet parentReportSet = (ReportSet) it.next();
                if ( !handleAsInheritance || parentIsInherited )
                {
                    ReportSet assembledReportSet = parentReportSet;
                    ReportSet childReportSet = (ReportSet) childReportSets.get( parentReportSet.getId() );
                    if ( childReportSet != null )
                    {
                        mergeReportSetDefinitions( childReportSet, parentReportSet );
                        assembledReportSet = childReportSet;
                    }
                    else if ( handleAsInheritance && ( parentInherited == null ) )
                    {
                        parentReportSet.unsetInheritanceApplied();
                    }
                    assembledReportSets.put( assembledReportSet.getId(), assembledReportSet );
                }
            }
            for ( Iterator it = childReportSets.entrySet().iterator(); it.hasNext(); )
            {
                Map.Entry entry = (Map.Entry) it.next();
                String id = (String) entry.getKey();
                if ( !assembledReportSets.containsKey( id ) )
                {
                    assembledReportSets.put( id, entry.getValue() );
                }
            }
            child.setReportSets( new ArrayList( assembledReportSets.values() ) );
            child.flushReportSetMap();
        }
    }
    @SuppressWarnings( "unchecked" )
    private void assembleDependencyInheritance( Model child, Model parent )
    {
        Map<String, Dependency> depsMap = new LinkedHashMap<String, Dependency>();
        List<Dependency> deps = parent.getDependencies();
        if ( deps != null )
        {
            for ( Dependency dependency : deps )
            {
                depsMap.put( dependency.getManagementKey(), dependency );
            }
        }
        deps = child.getDependencies();
        if ( deps != null )
        {
            for ( Dependency dependency : deps )
            {
                depsMap.put( dependency.getManagementKey(), dependency );
            }
        }
        child.setDependencies( new ArrayList<Dependency>( depsMap.values() ) );
    }
    private void assembleBuildInheritance( Model child, Model parent )
    {
        Build childBuild = child.getBuild();
        Build parentBuild = parent.getBuild();
        if ( parentBuild != null )
        {
            if ( childBuild == null )
            {
                childBuild = new Build();
                child.setBuild( childBuild );
            }
            assembleBuildInheritance( childBuild, parentBuild, true );
        }
    }
    private void assembleDistributionInheritence( Model child, Model parent, String childPathAdjustment,
                                                  boolean appendPaths )
    {
        if ( parent.getDistributionManagement() != null )
        {
            DistributionManagement parentDistMgmt = parent.getDistributionManagement();
            DistributionManagement childDistMgmt = child.getDistributionManagement();
            if ( childDistMgmt == null )
            {
                childDistMgmt = new DistributionManagement();
                child.setDistributionManagement( childDistMgmt );
            }
            if ( childDistMgmt.getSite() == null )
            {
                if ( parentDistMgmt.getSite() != null )
                {
                    Site site = new Site();
                    childDistMgmt.setSite( site );
                    site.setId( parentDistMgmt.getSite().getId() );
                    site.setName( parentDistMgmt.getSite().getName() );
                    site.setUrl( parentDistMgmt.getSite().getUrl() );
                    if ( site.getUrl() != null )
                    {
                        site.setUrl(
                            appendPath( site.getUrl(), child.getArtifactId(), childPathAdjustment, appendPaths ) );
                    }
                }
            }
            if ( childDistMgmt.getRepository() == null )
            {
                if ( parentDistMgmt.getRepository() != null )
                {
                    DeploymentRepository repository = copyDistributionRepository( parentDistMgmt.getRepository() );
                    childDistMgmt.setRepository( repository );
                }
            }
            if ( childDistMgmt.getSnapshotRepository() == null )
            {
                if ( parentDistMgmt.getSnapshotRepository() != null )
                {
                    DeploymentRepository repository =
                        copyDistributionRepository( parentDistMgmt.getSnapshotRepository() );
                    childDistMgmt.setSnapshotRepository( repository );
                }
            }
            if ( StringUtils.isEmpty( childDistMgmt.getDownloadUrl() ) )
            {
                childDistMgmt.setDownloadUrl( parentDistMgmt.getDownloadUrl() );
            }
        }
    }
    private static DeploymentRepository copyDistributionRepository( DeploymentRepository parentRepository )
    {
        DeploymentRepository repository = new DeploymentRepository();
        repository.setId( parentRepository.getId() );
        repository.setName( parentRepository.getName() );
        repository.setUrl( parentRepository.getUrl() );
        repository.setLayout( parentRepository.getLayout() );
        repository.setUniqueVersion( parentRepository.isUniqueVersion() );
        return repository;
    }
    protected String appendPath( String parentPath, String childPath, String pathAdjustment, boolean appendPaths )
    {
        String uncleanPath = parentPath;
        if ( appendPaths )
        {
            if ( pathAdjustment != null )
            {
                uncleanPath += "/" + pathAdjustment;
            }
            if ( childPath != null )
            {
                uncleanPath += "/" + childPath;
            }
        }
        String cleanedPath = "";
        int protocolIdx = uncleanPath.indexOf( "://" );
        if ( protocolIdx > -1 )
        {
            cleanedPath = uncleanPath.substring( 0, protocolIdx + 3 );
            uncleanPath = uncleanPath.substring( protocolIdx + 3 );
        }
        if ( uncleanPath.startsWith( "/" ) )
        {
            cleanedPath += "/";
        }
        return cleanedPath + resolvePath( uncleanPath );
    }
    private static String resolvePath( String uncleanPath )
    {
        LinkedList<String> pathElements = new LinkedList<String>();
        StringTokenizer tokenizer = new StringTokenizer( uncleanPath, "/" );
        while ( tokenizer.hasMoreTokens() )
        {
            String token = tokenizer.nextToken();
            if ( token.equals( "" ) )
            {
            }
            else if ( token.equals( ".." ) )
            {
                if ( pathElements.isEmpty() )
                {
                }
                else
                {
                    pathElements.removeLast();
                }
            }
            else
            {
                pathElements.addLast( token );
            }
        }
        StringBuilder cleanedPath = new StringBuilder();
        while ( !pathElements.isEmpty() )
        {
            cleanedPath.append( pathElements.removeFirst() );
            if ( !pathElements.isEmpty() )
            {
                cleanedPath.append( '/' );
            }
        }
        return cleanedPath.toString();
    }
    private static void mergeExtensionLists( Build childBuild, Build parentBuild )
    {
        for ( Extension e : parentBuild.getExtensions() )
        {
            if ( !childBuild.getExtensions().contains( e ) )
            {
                childBuild.addExtension( e );
            }
        }
    }
}

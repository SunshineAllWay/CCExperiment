package org.apache.maven.model.plugin;
import org.apache.maven.model.Build;
import org.apache.maven.model.Model;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginManagement;
import org.apache.maven.model.ReportPlugin;
import org.apache.maven.model.ReportSet;
import org.apache.maven.model.Reporting;
import org.apache.maven.model.building.ModelBuildingRequest;
import org.apache.maven.model.building.ModelProblemCollector;
import org.apache.maven.model.building.ModelProblem.Severity;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.xml.Xpp3Dom;
@Component( role = ReportingConverter.class )
public class DefaultReportingConverter
    implements ReportingConverter
{
    public void convertReporting( Model model, ModelBuildingRequest request, ModelProblemCollector problems )
    {
        Reporting reporting = model.getReporting();
        if ( reporting == null )
        {
            return;
        }
        Build build = model.getBuild();
        if ( build == null )
        {
            build = new Build();
            model.setBuild( build );
        }
        Plugin sitePlugin = findSitePlugin( build );
        if ( sitePlugin == null )
        {
            sitePlugin = new Plugin();
            sitePlugin.setArtifactId( "maven-site-plugin" );
            PluginManagement pluginManagement = build.getPluginManagement();
            if ( pluginManagement == null )
            {
                pluginManagement = new PluginManagement();
                build.setPluginManagement( pluginManagement );
            }
            pluginManagement.addPlugin( sitePlugin );
        }
        Xpp3Dom configuration = (Xpp3Dom) sitePlugin.getConfiguration();
        if ( configuration == null )
        {
            configuration = new Xpp3Dom( "configuration" );
            sitePlugin.setConfiguration( configuration );
        }
        Xpp3Dom reportPlugins = configuration.getChild( "reportPlugins" );
        if ( reportPlugins != null )
        {
            return;
        }
        if ( configuration.getChild( "outputDirectory" ) == null )
        {
            addDom( configuration, "outputDirectory", reporting.getOutputDirectory() );
        }
        reportPlugins = new Xpp3Dom( "reportPlugins" );
        configuration.addChild( reportPlugins );
        boolean hasMavenProjectInfoReportsPlugin = false;
        if ( !reporting.getPlugins().isEmpty()
            && request.getValidationLevel() >= ModelBuildingRequest.VALIDATION_LEVEL_MAVEN_3_1 )
        {
            problems.add( Severity.WARNING, "The <reporting> section is deprecated"
                + ", please move the reports to the <configuration> section of the new Maven Site Plugin.",
                          reporting.getLocation( "" ), null );
        }
        for ( ReportPlugin plugin : reporting.getPlugins() )
        {
            Xpp3Dom reportPlugin = convert( plugin );
            reportPlugins.addChild( reportPlugin );
            if ( !reporting.isExcludeDefaults() && !hasMavenProjectInfoReportsPlugin
                && "org.apache.maven.plugins".equals( reportPlugin.getChild( "groupId" ).getValue() )
                && "maven-project-info-reports-plugin".equals( reportPlugin.getChild( "artifactId" ).getValue() ) )
            {
                hasMavenProjectInfoReportsPlugin = true;
            }
        }
        if ( !reporting.isExcludeDefaults() && !hasMavenProjectInfoReportsPlugin )
        {
            Xpp3Dom dom = new Xpp3Dom( "reportPlugin" );
            addDom( dom, "groupId", "org.apache.maven.plugins" );
            addDom( dom, "artifactId", "maven-project-info-reports-plugin" );
            reportPlugins.addChild( dom );
        }
    }
    private Plugin findSitePlugin( Build build )
    {
        for ( Plugin plugin : build.getPlugins() )
        {
            if ( isSitePlugin( plugin ) )
            {
                return plugin;
            }
        }
        PluginManagement pluginManagement = build.getPluginManagement();
        if ( pluginManagement != null )
        {
            for ( Plugin plugin : pluginManagement.getPlugins() )
            {
                if ( isSitePlugin( plugin ) )
                {
                    return plugin;
                }
            }
        }
        return null;
    }
    private boolean isSitePlugin( Plugin plugin )
    {
        return "maven-site-plugin".equals( plugin.getArtifactId() )
            && "org.apache.maven.plugins".equals( plugin.getGroupId() );
    }
    private Xpp3Dom convert( ReportPlugin plugin )
    {
        Xpp3Dom dom = new Xpp3Dom( "reportPlugin" );
        addDom( dom, "groupId", plugin.getGroupId() );
        addDom( dom, "artifactId", plugin.getArtifactId() );
        addDom( dom, "version", plugin.getVersion() );
        Xpp3Dom configuration = (Xpp3Dom) plugin.getConfiguration();
        if ( configuration != null )
        {
            configuration = new Xpp3Dom( configuration );
            dom.addChild( configuration );
        }
        if ( !plugin.getReportSets().isEmpty() )
        {
            Xpp3Dom reportSets = new Xpp3Dom( "reportSets" );
            for ( ReportSet reportSet : plugin.getReportSets() )
            {
                Xpp3Dom rs = convert( reportSet );
                reportSets.addChild( rs );
            }
            dom.addChild( reportSets );
        }
        return dom;
    }
    private Xpp3Dom convert( ReportSet reportSet )
    {
        Xpp3Dom dom = new Xpp3Dom( "reportSet" );
        addDom( dom, "id", reportSet.getId() );
        Xpp3Dom configuration = (Xpp3Dom) reportSet.getConfiguration();
        if ( configuration != null )
        {
            configuration = new Xpp3Dom( configuration );
            dom.addChild( configuration );
        }
        if ( !reportSet.getReports().isEmpty() )
        {
            Xpp3Dom reports = new Xpp3Dom( "reports" );
            for ( String report : reportSet.getReports() )
            {
                addDom( reports, "report", report );
            }
            dom.addChild( reports );
        }
        return dom;
    }
    private void addDom( Xpp3Dom parent, String childName, String childValue )
    {
        if ( StringUtils.isNotEmpty( childValue ) )
        {
            parent.addChild( newDom( childName, childValue ) );
        }
    }
    private Xpp3Dom newDom( String name, String value )
    {
        Xpp3Dom dom = new Xpp3Dom( name );
        dom.setValue( value );
        return dom;
    }
}

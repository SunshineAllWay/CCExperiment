package org.apache.maven.model.building;
import org.apache.maven.model.Model;
import org.apache.maven.model.composition.DefaultDependencyManagementImporter;
import org.apache.maven.model.composition.DependencyManagementImporter;
import org.apache.maven.model.inheritance.DefaultInheritanceAssembler;
import org.apache.maven.model.inheritance.InheritanceAssembler;
import org.apache.maven.model.interpolation.ModelInterpolator;
import org.apache.maven.model.interpolation.StringSearchModelInterpolator;
import org.apache.maven.model.io.DefaultModelReader;
import org.apache.maven.model.io.ModelReader;
import org.apache.maven.model.locator.DefaultModelLocator;
import org.apache.maven.model.locator.ModelLocator;
import org.apache.maven.model.management.DefaultDependencyManagementInjector;
import org.apache.maven.model.management.DefaultPluginManagementInjector;
import org.apache.maven.model.management.DependencyManagementInjector;
import org.apache.maven.model.management.PluginManagementInjector;
import org.apache.maven.model.normalization.DefaultModelNormalizer;
import org.apache.maven.model.normalization.ModelNormalizer;
import org.apache.maven.model.path.DefaultModelPathTranslator;
import org.apache.maven.model.path.DefaultModelUrlNormalizer;
import org.apache.maven.model.path.DefaultPathTranslator;
import org.apache.maven.model.path.DefaultUrlNormalizer;
import org.apache.maven.model.path.ModelPathTranslator;
import org.apache.maven.model.path.ModelUrlNormalizer;
import org.apache.maven.model.path.PathTranslator;
import org.apache.maven.model.path.UrlNormalizer;
import org.apache.maven.model.plugin.DefaultPluginConfigurationExpander;
import org.apache.maven.model.plugin.DefaultReportConfigurationExpander;
import org.apache.maven.model.plugin.DefaultReportingConverter;
import org.apache.maven.model.plugin.LifecycleBindingsInjector;
import org.apache.maven.model.plugin.PluginConfigurationExpander;
import org.apache.maven.model.plugin.ReportConfigurationExpander;
import org.apache.maven.model.plugin.ReportingConverter;
import org.apache.maven.model.profile.DefaultProfileInjector;
import org.apache.maven.model.profile.DefaultProfileSelector;
import org.apache.maven.model.profile.ProfileInjector;
import org.apache.maven.model.profile.ProfileSelector;
import org.apache.maven.model.profile.activation.FileProfileActivator;
import org.apache.maven.model.profile.activation.JdkVersionProfileActivator;
import org.apache.maven.model.profile.activation.OperatingSystemProfileActivator;
import org.apache.maven.model.profile.activation.ProfileActivator;
import org.apache.maven.model.profile.activation.PropertyProfileActivator;
import org.apache.maven.model.superpom.DefaultSuperPomProvider;
import org.apache.maven.model.superpom.SuperPomProvider;
import org.apache.maven.model.validation.DefaultModelValidator;
import org.apache.maven.model.validation.ModelValidator;
public class DefaultModelBuilderFactory
{
    protected ModelProcessor newModelProcessor()
    {
        DefaultModelProcessor processor = new DefaultModelProcessor();
        processor.setModelLocator( newModelLocator() );
        processor.setModelReader( newModelReader() );
        return processor;
    }
    protected ModelLocator newModelLocator()
    {
        return new DefaultModelLocator();
    }
    protected ModelReader newModelReader()
    {
        return new DefaultModelReader();
    }
    protected ProfileSelector newProfileSelector()
    {
        DefaultProfileSelector profileSelector = new DefaultProfileSelector();
        for ( ProfileActivator activator : newProfileActivators() )
        {
            profileSelector.addProfileActivator( activator );
        }
        return profileSelector;
    }
    protected ProfileActivator[] newProfileActivators()
    {
        return new ProfileActivator[] { new JdkVersionProfileActivator(), new OperatingSystemProfileActivator(),
            new PropertyProfileActivator(), new FileProfileActivator().setPathTranslator( newPathTranslator() ) };
    }
    protected UrlNormalizer newUrlNormalizer()
    {
        return new DefaultUrlNormalizer();
    }
    protected PathTranslator newPathTranslator()
    {
        return new DefaultPathTranslator();
    }
    protected ModelInterpolator newModelInterpolator()
    {
        UrlNormalizer urlNormalizer = newUrlNormalizer();
        PathTranslator pathTranslator = newPathTranslator();
        return new StringSearchModelInterpolator().setPathTranslator( pathTranslator ).setUrlNormalizer( urlNormalizer );
    }
    protected ModelValidator newModelValidator()
    {
        return new DefaultModelValidator();
    }
    protected ModelNormalizer newModelNormalizer()
    {
        return new DefaultModelNormalizer();
    }
    protected ModelPathTranslator newModelPathTranslator()
    {
        return new DefaultModelPathTranslator().setPathTranslator( newPathTranslator() );
    }
    protected ModelUrlNormalizer newModelUrlNormalizer()
    {
        return new DefaultModelUrlNormalizer().setUrlNormalizer( newUrlNormalizer() );
    }
    protected InheritanceAssembler newInheritanceAssembler()
    {
        return new DefaultInheritanceAssembler();
    }
    protected ProfileInjector newProfileInjector()
    {
        return new DefaultProfileInjector();
    }
    protected SuperPomProvider newSuperPomProvider()
    {
        return new DefaultSuperPomProvider().setModelProcessor( newModelProcessor() );
    }
    protected DependencyManagementImporter newDependencyManagementImporter()
    {
        return new DefaultDependencyManagementImporter();
    }
    protected DependencyManagementInjector newDependencyManagementInjector()
    {
        return new DefaultDependencyManagementInjector();
    }
    protected LifecycleBindingsInjector newLifecycleBindingsInjector()
    {
        return new StubLifecycleBindingsInjector();
    }
    protected PluginManagementInjector newPluginManagementInjector()
    {
        return new DefaultPluginManagementInjector();
    }
    protected PluginConfigurationExpander newPluginConfigurationExpander()
    {
        return new DefaultPluginConfigurationExpander();
    }
    protected ReportConfigurationExpander newReportConfigurationExpander()
    {
        return new DefaultReportConfigurationExpander();
    }
    protected ReportingConverter newReportingConverter()
    {
        return new DefaultReportingConverter();
    }
    public DefaultModelBuilder newInstance()
    {
        DefaultModelBuilder modelBuilder = new DefaultModelBuilder();
        modelBuilder.setModelProcessor( newModelProcessor() );
        modelBuilder.setModelValidator( newModelValidator() );
        modelBuilder.setModelNormalizer( newModelNormalizer() );
        modelBuilder.setModelPathTranslator( newModelPathTranslator() );
        modelBuilder.setModelUrlNormalizer( newModelUrlNormalizer() );
        modelBuilder.setModelInterpolator( newModelInterpolator() );
        modelBuilder.setInheritanceAssembler( newInheritanceAssembler() );
        modelBuilder.setProfileInjector( newProfileInjector() );
        modelBuilder.setProfileSelector( newProfileSelector() );
        modelBuilder.setSuperPomProvider( newSuperPomProvider() );
        modelBuilder.setDependencyManagementImporter( newDependencyManagementImporter() );
        modelBuilder.setDependencyManagementInjector( newDependencyManagementInjector() );
        modelBuilder.setLifecycleBindingsInjector( newLifecycleBindingsInjector() );
        modelBuilder.setPluginManagementInjector( newPluginManagementInjector() );
        modelBuilder.setPluginConfigurationExpander( newPluginConfigurationExpander() );
        modelBuilder.setReportConfigurationExpander( newReportConfigurationExpander() );
        modelBuilder.setReportingConverter( newReportingConverter() );
        return modelBuilder;
    }
    private static class StubLifecycleBindingsInjector
        implements LifecycleBindingsInjector
    {
        public void injectLifecycleBindings( Model model, ModelBuildingRequest request, ModelProblemCollector problems )
        {
        }
    }
}

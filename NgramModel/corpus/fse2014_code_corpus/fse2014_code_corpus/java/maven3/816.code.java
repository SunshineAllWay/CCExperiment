package org.apache.maven.settings.building;
import org.apache.maven.settings.io.DefaultSettingsReader;
import org.apache.maven.settings.io.DefaultSettingsWriter;
import org.apache.maven.settings.io.SettingsReader;
import org.apache.maven.settings.io.SettingsWriter;
import org.apache.maven.settings.validation.DefaultSettingsValidator;
import org.apache.maven.settings.validation.SettingsValidator;
public class DefaultSettingsBuilderFactory
{
    protected SettingsReader newSettingsReader()
    {
        return new DefaultSettingsReader();
    }
    protected SettingsWriter newSettingsWriter()
    {
        return new DefaultSettingsWriter();
    }
    protected SettingsValidator newSettingsValidator()
    {
        return new DefaultSettingsValidator();
    }
    public DefaultSettingsBuilder newInstance()
    {
        DefaultSettingsBuilder builder = new DefaultSettingsBuilder();
        builder.setSettingsReader( newSettingsReader() );
        builder.setSettingsWriter( newSettingsWriter() );
        builder.setSettingsValidator( newSettingsValidator() );
        return builder;
    }
}

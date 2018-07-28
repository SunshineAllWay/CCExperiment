package org.apache.maven.configuration;
public interface BeanConfigurationValuePreprocessor
{
    Object preprocessValue( String value, Class<?> type )
        throws BeanConfigurationException;
}

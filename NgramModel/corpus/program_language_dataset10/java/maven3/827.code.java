package org.apache.maven.settings.building;
public interface SettingsProblemCollector
{
    void add( SettingsProblem.Severity severity, String message, int line, int column, Exception cause );
}

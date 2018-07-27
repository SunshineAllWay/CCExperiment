package org.apache.maven.settings.building;
public interface SettingsProblem
{
    enum Severity
    {
        FATAL, 
        ERROR, 
        WARNING; 
    }
    String getSource();
    int getLineNumber();
    int getColumnNumber();
    String getLocation();
    Exception getException();
    String getMessage();
    Severity getSeverity();
}

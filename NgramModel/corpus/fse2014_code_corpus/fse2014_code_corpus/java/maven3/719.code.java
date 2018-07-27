package org.apache.maven.model.building;
public interface ModelProblem
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
    String getModelId();
    Exception getException();
    String getMessage();
    Severity getSeverity();
}

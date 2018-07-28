package org.apache.maven.model.building;
import org.apache.maven.model.InputLocation;
public interface ModelProblemCollector
{
    void add( ModelProblem.Severity severity, String message, InputLocation location, Exception cause );
}

package org.apache.maven;
import org.apache.maven.execution.MavenExecutionRequest;
import org.apache.maven.execution.MavenExecutionResult;
public interface Maven
{
    @Deprecated
    String POMv4 = "pom.xml";
    MavenExecutionResult execute( MavenExecutionRequest request );    
}
package org.apache.maven.exception;
public interface ExceptionHandler    
{
    ExceptionSummary handleException( Throwable e );
}

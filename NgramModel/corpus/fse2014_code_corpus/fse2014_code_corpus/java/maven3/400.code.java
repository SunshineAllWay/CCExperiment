package org.apache.maven.lifecycle.internal;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.Logger;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
@Component( role = ThreadConfigurationService.class )
public class ThreadConfigurationService
{
    @Requirement
    private Logger logger;
    private final int cpuCores;
    @SuppressWarnings( { "UnusedDeclaration" } )
    public ThreadConfigurationService()
    {
        cpuCores = Runtime.getRuntime().availableProcessors();
    }
    public ThreadConfigurationService( Logger logger, int cpuCores )
    {
        this.logger = logger;
        this.cpuCores = cpuCores;
    }
    public ExecutorService getExecutorService( String threadCountConfiguration, boolean perCoreThreadCount,
                                               int largestBuildListSize )
    {
        Integer threadCount = getThreadCount( threadCountConfiguration, perCoreThreadCount, largestBuildListSize );
        return getExecutorService( threadCount );
    }
    private ExecutorService getExecutorService( Integer threadCount )
    {
        if ( threadCount == null )
        {
            logger.info( "Building with unlimited threads" );
            return Executors.newCachedThreadPool();
        }
        logger.info( "Building with " + threadCount + " threads" );
        return Executors.newFixedThreadPool( threadCount );
    }
    Integer getThreadCount( String threadCountConfiguration, boolean perCoreThreadCount, int largestBuildListSize )
    {
        float threadCount = Math.min( cpuCores, largestBuildListSize );
        if ( threadCountConfiguration != null )
        {
            try
            {
                threadCount = Float.parseFloat( threadCountConfiguration );
            }
            catch ( NumberFormatException e )
            {
                logger.warn(
                    "Couldn't parse thread count, will default to " + threadCount + ": " + threadCountConfiguration );
            }
        }
        if ( perCoreThreadCount )
        {
            threadCount = threadCount * cpuCores;
        }
        final int endResult = Math.round( threadCount );
        if ( logger.isDebugEnabled() )
        {
            logger.debug( "Thread pool size: " + endResult );
        }
        return endResult;
    }
}
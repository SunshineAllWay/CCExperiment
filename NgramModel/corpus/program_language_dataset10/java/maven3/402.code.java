package org.apache.maven.lifecycle.internal;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
@SuppressWarnings( { "SynchronizationOnLocalVariableOrMethodParameter" } )
public class ThreadOutputMuxer
{
    private final Iterator<ProjectSegment> projects;
    private final ThreadLocal<ProjectSegment> projectBuildThreadLocal = new ThreadLocal<ProjectSegment>();
    private final Map<ProjectSegment, ByteArrayOutputStream> streams =
        new HashMap<ProjectSegment, ByteArrayOutputStream>();
    private final Map<ProjectSegment, PrintStream> printStreams = new HashMap<ProjectSegment, PrintStream>();
    private final ByteArrayOutputStream defaultOutputStreamForUnknownData = new ByteArrayOutputStream();
    private final PrintStream defaultPringStream = new PrintStream( defaultOutputStreamForUnknownData );
    private final Set<ProjectSegment> completedBuilds = Collections.synchronizedSet( new HashSet<ProjectSegment>() );
    private volatile ProjectSegment currentBuild;
    private final PrintStream originalSystemOUtStream;
    private final ConsolePrinter printer;
    class ConsolePrinter
        implements Runnable
    {
        public volatile boolean running;
        private final ProjectBuildList projectBuildList;
        ConsolePrinter( ProjectBuildList projectBuildList )
        {
            this.projectBuildList = projectBuildList;
        }
        public void run()
        {
            running = true;
            for ( ProjectSegment projectBuild : projectBuildList )
            {
                final PrintStream projectStream = printStreams.get( projectBuild );
                ByteArrayOutputStream projectOs = streams.get( projectBuild );
                do
                {
                    synchronized ( projectStream )
                    {
                        try
                        {
                            projectStream.wait( 100 );
                        }
                        catch ( InterruptedException e )
                        {
                            throw new RuntimeException( e );
                        }
                        try
                        {
                            projectOs.writeTo( originalSystemOUtStream );
                        }
                        catch ( IOException e )
                        {
                            throw new RuntimeException( e );
                        }
                        projectOs.reset();
                    }
                }
                while ( !completedBuilds.contains( projectBuild ) );
            }
            running = false;
        }
        public void waitUntilRunning( boolean expect )
        {
            while ( !running == expect )
            {
                try
                {
                    Thread.sleep( 10 );
                }
                catch ( InterruptedException e )
                {
                    throw new RuntimeException( e );
                }
            }
        }
    }
    public ThreadOutputMuxer( ProjectBuildList segmentChunks, PrintStream originalSystemOut )
    {
        projects = segmentChunks.iterator();
        for ( ProjectSegment segmentChunk : segmentChunks )
        {
            final ByteArrayOutputStream value = new ByteArrayOutputStream();
            streams.put( segmentChunk, value );
            printStreams.put( segmentChunk, new PrintStream( value ) );
        }
        setNext();
        this.originalSystemOUtStream = originalSystemOut;
        System.setOut( new ThreadBoundPrintStream( this.originalSystemOUtStream ) );
        printer = new ConsolePrinter( segmentChunks );
        new Thread( printer ).start();
        printer.waitUntilRunning( true );
    }
    public void close()
    {
        printer.waitUntilRunning( false );
        System.setOut( this.originalSystemOUtStream );
    }
    private void setNext()
    {
        currentBuild = projects.hasNext() ? projects.next() : null;
    }
    private boolean ownsRealOutputStream( ProjectSegment projectBuild )
    {
        return projectBuild.equals( currentBuild );
    }
    private PrintStream getThreadBoundPrintStream()
    {
        ProjectSegment threadProject = projectBuildThreadLocal.get();
        if ( threadProject == null )
        {
            return defaultPringStream;
        }
        if ( ownsRealOutputStream( threadProject ) )
        {
            return originalSystemOUtStream;
        }
        return printStreams.get( threadProject );
    }
    public void associateThreadWithProjectSegment( ProjectSegment projectBuild )
    {
        projectBuildThreadLocal.set( projectBuild );
    }
    public void setThisModuleComplete( ProjectSegment projectBuild )
    {
        completedBuilds.add( projectBuild );
        PrintStream stream = printStreams.get( projectBuild );
        synchronized ( stream )
        {
            stream.notifyAll();
        }
        disconnectThreadFromProject();
    }
    private void disconnectThreadFromProject()
    {
        projectBuildThreadLocal.remove();
    }
    private class ThreadBoundPrintStream
        extends PrintStream
    {
        public ThreadBoundPrintStream( PrintStream systemOutStream )
        {
            super( systemOutStream );
        }
        private PrintStream getOutputStreamForCurrentThread()
        {
            return getThreadBoundPrintStream();
        }
        @Override
        public void println()
        {
            final PrintStream currentStream = getOutputStreamForCurrentThread();
            synchronized ( currentStream )
            {
                currentStream.println();
                currentStream.notifyAll();
            }
        }
        @Override
        public void print( char c )
        {
            final PrintStream currentStream = getOutputStreamForCurrentThread();
            synchronized ( currentStream )
            {
                currentStream.print( c );
                currentStream.notifyAll();
            }
        }
        @Override
        public void println( char x )
        {
            final PrintStream currentStream = getOutputStreamForCurrentThread();
            synchronized ( currentStream )
            {
                currentStream.println( x );
                currentStream.notifyAll();
            }
        }
        @Override
        public void print( double d )
        {
            final PrintStream currentStream = getOutputStreamForCurrentThread();
            synchronized ( currentStream )
            {
                currentStream.print( d );
                currentStream.notifyAll();
            }
        }
        @Override
        public void println( double x )
        {
            final PrintStream currentStream = getOutputStreamForCurrentThread();
            synchronized ( currentStream )
            {
                currentStream.println( x );
                currentStream.notifyAll();
            }
        }
        @Override
        public void print( float f )
        {
            final PrintStream currentStream = getOutputStreamForCurrentThread();
            synchronized ( currentStream )
            {
                currentStream.print( f );
                currentStream.notifyAll();
            }
        }
        @Override
        public void println( float x )
        {
            final PrintStream currentStream = getOutputStreamForCurrentThread();
            synchronized ( currentStream )
            {
                currentStream.println( x );
                currentStream.notifyAll();
            }
        }
        @Override
        public void print( int i )
        {
            final PrintStream currentStream = getOutputStreamForCurrentThread();
            synchronized ( currentStream )
            {
                currentStream.print( i );
                currentStream.notifyAll();
            }
        }
        @Override
        public void println( int x )
        {
            final PrintStream currentStream = getOutputStreamForCurrentThread();
            synchronized ( currentStream )
            {
                currentStream.println( x );
                currentStream.notifyAll();
            }
        }
        @Override
        public void print( long l )
        {
            final PrintStream currentStream = getOutputStreamForCurrentThread();
            synchronized ( currentStream )
            {
                currentStream.print( l );
                currentStream.notifyAll();
            }
        }
        @Override
        public void println( long x )
        {
            final PrintStream currentStream = getOutputStreamForCurrentThread();
            synchronized ( currentStream )
            {
                currentStream.print( x );
                currentStream.notifyAll();
            }
        }
        @Override
        public void print( boolean b )
        {
            final PrintStream currentStream = getOutputStreamForCurrentThread();
            synchronized ( currentStream )
            {
                currentStream.print( b );
                currentStream.notifyAll();
            }
        }
        @Override
        public void println( boolean x )
        {
            final PrintStream currentStream = getOutputStreamForCurrentThread();
            synchronized ( currentStream )
            {
                currentStream.print( x );
                currentStream.notifyAll();
            }
        }
        @Override
        public void print( char s[] )
        {
            final PrintStream currentStream = getOutputStreamForCurrentThread();
            synchronized ( currentStream )
            {
                currentStream.print( s );
                currentStream.notifyAll();
            }
        }
        @Override
        public void println( char x[] )
        {
            final PrintStream currentStream = getOutputStreamForCurrentThread();
            synchronized ( currentStream )
            {
                currentStream.print( x );
                currentStream.notifyAll();
            }
        }
        @Override
        public void print( Object obj )
        {
            final PrintStream currentStream = getOutputStreamForCurrentThread();
            synchronized ( currentStream )
            {
                currentStream.print( obj );
                currentStream.notifyAll();
            }
        }
        @Override
        public void println( Object x )
        {
            final PrintStream currentStream = getOutputStreamForCurrentThread();
            synchronized ( currentStream )
            {
                currentStream.println( x );
                currentStream.notifyAll();
            }
        }
        @Override
        public void print( String s )
        {
            final PrintStream currentStream = getOutputStreamForCurrentThread();
            synchronized ( currentStream )
            {
                currentStream.print( s );
                currentStream.notifyAll();
            }
        }
        @Override
        public void println( String x )
        {
            final PrintStream currentStream = getOutputStreamForCurrentThread();
            synchronized ( currentStream )
            {
                currentStream.println( x );
                currentStream.notifyAll();
            }
        }
        @Override
        public void write( byte b[], int off, int len )
        {
            final PrintStream currentStream = getOutputStreamForCurrentThread();
            synchronized ( currentStream )
            {
                currentStream.write( b, off, len );
                currentStream.notifyAll();
            }
        }
        @Override
        public void close()
        {
            getOutputStreamForCurrentThread().close();
        }
        @Override
        public void flush()
        {
            getOutputStreamForCurrentThread().flush();
        }
        @Override
        public void write( int b )
        {
            final PrintStream currentStream = getOutputStreamForCurrentThread();
            synchronized ( currentStream )
            {
                currentStream.write( b );
                currentStream.notifyAll();
            }
        }
        @Override
        public void write( byte b[] )
            throws IOException
        {
            final PrintStream currentStream = getOutputStreamForCurrentThread();
            synchronized ( currentStream )
            {
                currentStream.write( b );
                currentStream.notifyAll();
            }
        }
    }
}

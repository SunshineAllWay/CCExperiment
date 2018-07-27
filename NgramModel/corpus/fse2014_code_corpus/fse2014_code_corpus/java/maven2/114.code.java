package org.apache.maven.cli.compat;
import org.apache.maven.cli.MavenCli;
import org.codehaus.classworlds.ClassWorld;
public class CompatibleMain
{
    public static void main( String[] args )
    {
        ClassWorld classWorld = new ClassWorld( "plexus.core", Thread.currentThread().getContextClassLoader() );
        int result = main( args, classWorld );
        System.exit( result );
    }
    public static int main( String[] args, ClassWorld classWorld )
    {
        String javaVersion = System.getProperty( "java.specification.version", "1.5" );
        if ( "1.4".equals( javaVersion ) || "1.3".equals( javaVersion )
             || "1.2".equals( javaVersion )  || "1.1".equals( javaVersion ) )
        {
            System.out.println( "Java specification version: " + javaVersion );
            System.err.println( "This release of Maven requires Java version 1.5 or greater." );
            return 1;
        }
        return MavenCli.main( args, classWorld );
    }
}

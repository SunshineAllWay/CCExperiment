package org.apache.maven.cli;
import java.io.PrintStream;
public class BatchModeMavenTransferListener
    extends AbstractMavenTransferListener
{
    public BatchModeMavenTransferListener( PrintStream out )
    {
        super( out );
    }
}

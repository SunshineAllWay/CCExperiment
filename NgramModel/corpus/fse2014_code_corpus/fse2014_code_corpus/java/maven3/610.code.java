package org.apache.maven.repository;
import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.spi.connector.RepositoryConnector;
import org.sonatype.aether.spi.connector.RepositoryConnectorFactory;
import org.sonatype.aether.transfer.NoRepositoryConnectorException;
@Component( role = RepositoryConnectorFactory.class, hint = "test" )
public class TestRepositoryConnectorFactory
    implements RepositoryConnectorFactory
{
    public RepositoryConnector newInstance( RepositorySystemSession session, RemoteRepository repository )
        throws NoRepositoryConnectorException
    {
        return new TestRepositoryConnector( repository );
    }
    public int getPriority()
    {
        return 0;
    }
}

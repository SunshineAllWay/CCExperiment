package org.apache.maven.artifact.manager;
import java.util.HashMap;
import java.util.Map;
import org.apache.maven.wagon.ConnectionException;
import org.apache.maven.wagon.InputData;
import org.apache.maven.wagon.OutputData;
import org.apache.maven.wagon.ResourceDoesNotExistException;
import org.apache.maven.wagon.StreamWagon;
import org.apache.maven.wagon.TransferFailedException;
import org.apache.maven.wagon.authentication.AuthenticationException;
import org.apache.maven.wagon.authorization.AuthorizationException;
import org.apache.maven.wagon.resource.Resource;
import org.codehaus.plexus.util.StringInputStream;
import org.codehaus.plexus.util.StringOutputStream;
public class StringWagon
    extends StreamWagon
{
    private Map expectedContent = new HashMap();
    public void addExpectedContent( String resourceName, String expectedContent )
    {
        this.expectedContent.put( resourceName, expectedContent );
    }
    public String[] getSupportedProtocols()
    {
        return new String[] { "string" };
    }
    public void closeConnection()
        throws ConnectionException
    {
    }
    public void fillInputData( InputData inputData )
        throws TransferFailedException, ResourceDoesNotExistException, AuthorizationException
    {
        Resource resource = inputData.getResource();
        String content = (String) expectedContent.get( resource.getName() );
        if ( content != null )
        {
            resource.setContentLength( content.length() );
            resource.setLastModified( System.currentTimeMillis() );
            inputData.setInputStream( new StringInputStream( content ) );
        }
        else
        {
            throw new ResourceDoesNotExistException( "No content provided for " + resource.getName() );
        }
    }
    public void fillOutputData( OutputData outputData )
        throws TransferFailedException
    {
        outputData.setOutputStream( new StringOutputStream() );
    }
    protected void openConnectionInternal()
        throws ConnectionException, AuthenticationException
    {
    }
    public void clearExpectedContent()
    {
        expectedContent.clear();
    }
}

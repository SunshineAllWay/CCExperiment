package org.apache.maven.repository;
import java.io.File;
import java.util.EventObject;
public class ArtifactTransferEvent
    extends EventObject
{
    public static final int TRANSFER_INITIATED = 0;
    public static final int TRANSFER_STARTED = 1;
    public static final int TRANSFER_COMPLETED = 2;
    public static final int TRANSFER_PROGRESS = 3;
    public static final int TRANSFER_ERROR = 4;
    public static final int REQUEST_GET = 5;
    public static final int REQUEST_PUT = 6;
    private int eventType;
    private int requestType;
    private Exception exception;
    private File localFile;
    private ArtifactTransferResource artifact;
    private long transferredBytes;
    private byte[] dataBuffer;
    private int dataOffset;
    private int dataLength;
    public ArtifactTransferEvent( String wagon, final int eventType, final int requestType,
                                  ArtifactTransferResource artifact )
    {
        super( wagon );
        setEventType( eventType );
        setRequestType( requestType );
        this.artifact = artifact;
    }
    public ArtifactTransferEvent( String wagon, final Exception exception, final int requestType,
                                  ArtifactTransferResource artifact )
    {
        this( wagon, TRANSFER_ERROR, requestType, artifact );
        this.exception = exception;
    }
    public ArtifactTransferResource getResource()
    {
        return artifact;
    }
    public Exception getException()
    {
        return exception;
    }
    public int getRequestType()
    {
        return requestType;
    }
    public void setRequestType( final int requestType )
    {
        switch ( requestType )
        {
            case REQUEST_PUT:
                break;
            case REQUEST_GET:
                break;
            default :
                throw new IllegalArgumentException( "Illegal request type: " + requestType );
        }
        this.requestType = requestType;
    }
    public int getEventType()
    {
        return eventType;
    }
    public void setEventType( final int eventType )
    {
        switch ( eventType )
        {
            case TRANSFER_INITIATED:
                break;
            case TRANSFER_STARTED:
                break;
            case TRANSFER_COMPLETED:
                break;
            case TRANSFER_PROGRESS:
                break;
            case TRANSFER_ERROR:
                break;
            default :
                throw new IllegalArgumentException( "Illegal event type: " + eventType );
        }
        this.eventType = eventType;
    }
    public File getLocalFile()
    {
        return localFile;
    }
    public void setLocalFile( File localFile )
    {
        this.localFile = localFile;
    }
    public long getTransferredBytes()
    {
        return transferredBytes;
    }
    public void setTransferredBytes( long transferredBytes )
    {
        this.transferredBytes = transferredBytes;
    }
    public byte[] getDataBuffer()
    {
        return dataBuffer;
    }
    public void setDataBuffer( byte[] dataBuffer )
    {
        this.dataBuffer = dataBuffer;
    }
    public int getDataOffset()
    {
        return dataOffset;
    }
    public void setDataOffset( int dataOffset )
    {
        this.dataOffset = dataOffset;
    }
    public int getDataLength()
    {
        return dataLength;
    }
    public void setDataLength( int dataLength )
    {
        this.dataLength = dataLength;
    }
    public String toString()
    {
        StringBuffer sb = new StringBuffer();
        sb.append( "TransferEvent[" );
        switch ( this.getRequestType() )
        {
            case REQUEST_GET:
                sb.append( "GET" );
                break;
            case REQUEST_PUT:
                sb.append( "PUT" );
                break;
            default:
                sb.append( this.getRequestType() );
                break;
        }
        sb.append( "|" );
        switch ( this.getEventType() )
        {
            case TRANSFER_COMPLETED:
                sb.append( "COMPLETED" );
                break;
            case TRANSFER_ERROR:
                sb.append( "ERROR" );
                break;
            case TRANSFER_INITIATED:
                sb.append( "INITIATED" );
                break;
            case TRANSFER_PROGRESS:
                sb.append( "PROGRESS" );
                break;
            case TRANSFER_STARTED:
                sb.append( "STARTED" );
                break;
            default:
                sb.append( this.getEventType() );
                break;
        }
        sb.append( "|" );
        sb.append( this.getLocalFile() ).append( "|" );
        sb.append( "]" );
        return sb.toString();
    }
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + eventType;
        result = prime * result + ( ( exception == null ) ? 0 : exception.hashCode() );
        result = prime * result + ( ( localFile == null ) ? 0 : localFile.hashCode() );
        result = prime * result + requestType;
        return result;
    }
    public boolean equals( Object obj )
    {
        if ( this == obj )
        {
            return true;
        }
        if ( ( obj == null ) || ( getClass() != obj.getClass() ) )
        {
            return false;
        }
        final ArtifactTransferEvent other = (ArtifactTransferEvent) obj;
        if ( eventType != other.eventType )
        {
            return false;
        }
        if ( exception == null )
        {
            if ( other.exception != null )
            {
                return false;
            }
        }
        else if ( !exception.getClass().equals( other.exception.getClass() ) )
        {
            return false;
        }
        if ( requestType != other.requestType )
        {
            return false;
        }
        else if ( !source.equals( other.source ) )
        {
            return false;
        }
        return true;
    }
}

package org.apache.cassandra.security.streaming;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.io.IOException;
import java.io.DataInputStream;
import org.apache.cassandra.streaming.FileStreamTask;
import org.apache.cassandra.streaming.IncomingStreamReader;
import org.apache.cassandra.streaming.StreamHeader;
public class SSLIncomingStreamReader extends IncomingStreamReader
{
    private final DataInputStream input;
    public SSLIncomingStreamReader(StreamHeader header, Socket socket, DataInputStream input) throws IOException
    {
        super(header, socket);
        this.input = input;
    }
    @Override
    protected long readnwrite(long length, long bytesRead, long offset, FileChannel fc) throws IOException
    {
        int toRead = (int)Math.min(FileStreamTask.CHUNK_SIZE, length - bytesRead);
        ByteBuffer buf = ByteBuffer.allocate(toRead);
        input.readFully(buf.array());
        fc.write(buf);
        bytesRead += buf.limit();
        remoteFile.progress += buf.limit();
        return bytesRead;
    }
}

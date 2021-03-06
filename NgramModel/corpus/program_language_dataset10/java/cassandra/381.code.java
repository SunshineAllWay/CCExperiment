package org.apache.cassandra.streaming;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.apache.cassandra.io.ICompactSerializer;
public class StreamHeader
{
    private static ICompactSerializer<StreamHeader> serializer;
    static
    {
        serializer = new StreamHeaderSerializer();
    }
    public static ICompactSerializer<StreamHeader> serializer()
    {
        return serializer;
    }
    public final String table;
    public final PendingFile file;
    public final long sessionId;
    public final Collection<PendingFile> pendingFiles;
    public StreamHeader(String table, long sessionId, PendingFile file)
    {
        this(table, sessionId, file, Collections.<PendingFile>emptyList());
    }
    public StreamHeader(String table, long sessionId, PendingFile first, Collection<PendingFile> pendingFiles)
    {
        this.table = table;
        this.sessionId  = sessionId;
        this.file = first;
        this.pendingFiles = pendingFiles;
    }
    private static class StreamHeaderSerializer implements ICompactSerializer<StreamHeader>
    {
        public void serialize(StreamHeader sh, DataOutputStream dos) throws IOException
        {
            dos.writeUTF(sh.table);
            dos.writeLong(sh.sessionId);
            PendingFile.serializer().serialize(sh.file, dos);
            dos.writeInt(sh.pendingFiles.size());
            for(PendingFile file : sh.pendingFiles)
            {
                PendingFile.serializer().serialize(file, dos);
            }
        }
        public StreamHeader deserialize(DataInputStream dis) throws IOException
        {
            String table = dis.readUTF();
            long sessionId = dis.readLong();
            PendingFile file = PendingFile.serializer().deserialize(dis);
            int size = dis.readInt();
            List<PendingFile> pendingFiles = new ArrayList<PendingFile>(size);
            for (int i = 0; i < size; i++)
            {
                pendingFiles.add(PendingFile.serializer().deserialize(dis));
            }
            return new StreamHeader(table, sessionId, file, pendingFiles);
        }
    }
}
package org.apache.cassandra.db;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.util.Collection;
import org.apache.cassandra.db.marshal.AbstractType;
import org.apache.cassandra.utils.FBUtilities;
public interface IColumn
{
    public static final int MAX_NAME_LENGTH = FBUtilities.MAX_UNSIGNED_SHORT;
    public boolean isMarkedForDelete();
    public long getMarkedForDeleteAt();
    public long mostRecentLiveChangeAt();
    public ByteBuffer name();
    public int size();
    public int serializedSize();
    public long timestamp();
    public ByteBuffer value();
    public Collection<IColumn> getSubColumns();
    public IColumn getSubColumn(ByteBuffer columnName);
    public void addColumn(IColumn column);
    public IColumn diff(IColumn column);
    public IColumn reconcile(IColumn column);
    public void updateDigest(MessageDigest digest);
    public int getLocalDeletionTime(); 
    public String getString(AbstractType comparator);
    IColumn deepCopy();
    boolean isLive();
}

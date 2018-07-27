import java.rmi.Remote;
import java.rmi.RemoteException;
public class RemoteTimestampImpl implements RemoteTimestamp {
    public long when() throws RemoteException {
        return System.currentTimeMillis();
    }
}

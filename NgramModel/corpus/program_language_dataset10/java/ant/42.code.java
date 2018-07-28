import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Calendar;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.util.DateUtils;
public class AntTimestamp implements RemoteTimestamp {
    public long when() throws RemoteException {
        Calendar cal=Calendar.getInstance();
        return DateUtils.getPhaseOfMoon(cal);
    }
}

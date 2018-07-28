import java.rmi.RemoteException;
import javax.ejb.CreateException;
import javax.ejb.EJBHome;
public interface TransformHome extends EJBHome {
    TransformRemote create() throws CreateException, RemoteException;
}

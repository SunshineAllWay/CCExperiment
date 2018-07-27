import java.rmi.RemoteException;
import javax.ejb.EJBObject;
public interface TransformRemote extends EJBObject {
    public String transform(String document, String transletName) 
	throws RemoteException;
}

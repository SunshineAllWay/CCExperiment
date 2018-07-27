package examples;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.RemoteException;
import java.rmi.Naming;
import java.util.Vector;
import org.apache.log4j.Logger;
import org.apache.log4j.NDC;
import org.apache.log4j.PropertyConfigurator;
public class NumberCruncherServer extends UnicastRemoteObject
                                  implements  NumberCruncher {
  private static final long serialVersionUID = 2626753561969426769L;
  static Logger logger = Logger.getLogger(NumberCruncherServer.class);
  public
  NumberCruncherServer() throws RemoteException {
  }
  public
  int[] factor(int number) throws RemoteException {
    try {
      NDC.push(getClientHost());
    }
    catch(java.rmi.server.ServerNotActiveException e) {
      NDC.push("localhost");
    }
    NDC.push(String.valueOf(number));    
    logger.info("Beginning to factor.");
    if(number <= 0) {
      throw new IllegalArgumentException(number+" is not a positive integer.");
    }
    else if(number == 1)
       return new int[] {1};
    Vector factors = new Vector();
    int n = number;
    for(int i = 2; (i <= n) && (i*i <= number); i++) {
      logger.debug("Trying to see if " + i + " is a factor.");
      if((n % i) == 0) {
	logger.info("Found factor "+i);
	factors.addElement(new Integer(i));
	do {
	  n /= i;
	} while((n % i) == 0);
      }
      delay(100);
    }
    if(n != 1) {
      logger.info("Found factor "+n);
      factors.addElement(new Integer(n));
    }
    int len = factors.size();
    int[] result = new int[len];
    for(int i = 0; i < len; i++) {
      result[i] = ((Integer) factors.elementAt(i)).intValue();
    }
    NDC.remove();
    return result;
  }
  static
  void usage(String msg) {
    System.err.println(msg);
    System.err.println(
     "Usage: java org.apache.log4j.examples.NumberCruncherServer configFile\n" +
     "   where configFile is a log4j configuration file.");
    System.exit(1);
  }
  public static
  void delay(int millis) {
    try{Thread.sleep(millis);}
    catch(InterruptedException e) {}
  }
  public static void main(String[] args) {
    if(args.length != 1) 
      usage("Wrong number of arguments.");
    NumberCruncherServer ncs;
    PropertyConfigurator.configure(args[0]);
    try {
      ncs = new NumberCruncherServer();
      Naming.rebind("Factor", ncs);
      logger.info("NumberCruncherServer bound and ready to serve.");
    }
    catch(Exception e) {
      logger.error("Could not bind NumberCruncherServer.", e);
      return;
    }
    NumberCruncherClient.loop(ncs);          
  }
}

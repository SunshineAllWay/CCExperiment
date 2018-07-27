package examples;
import org.apache.log4j.Logger;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.NDC;
public class Trivial {
  static Logger logger = Logger.getLogger(Trivial.class);
  public static void main(String[] args) {
    BasicConfigurator.configure();
    NDC.push("Client #45890"); 
    logger.info("Awake awake. Put on thy strength.");
    Trivial.foo();
    InnerTrivial.foo();
    logger.info("Exiting Trivial.");    
  }
  static
  void foo() {
    NDC.push("DB"); 
    logger.debug("Now king David was old.");    
    NDC.pop(); 
  }
  static class InnerTrivial {
    static  Logger logger = Logger.getLogger(InnerTrivial.class);
    static    
    void foo() {
      logger.info("Entered foo."); 
    }
  }
}

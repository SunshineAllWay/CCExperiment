import org.apache.log4j.*;
import java.sql.*;
import java.lang.*;
import java.util.*;
public class Log4JTest
{
   static Category cat = Category.getInstance(Log4JTest.class.getName());
   public static void main(String[] args)
   {
      try
      {
         Driver d = (Driver)(Class.forName("oracle.jdbc.driver.OracleDriver").newInstance());
         DriverManager.registerDriver(d);
      }
      catch(Exception e){}
      cat.setPriority(Priority.INFO);
      PropertyConfigurator.configure("log4jtestprops.txt");
      cat.debug("debug");  
      cat.info("info");
      cat.error("error");
      cat.fatal("fatal");
   }
}

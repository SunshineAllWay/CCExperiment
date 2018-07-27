import org.apache.log4j.*;
import java.sql.*;
import java.lang.*;
import java.util.*;
public class Log4JTest
{
	static Category cat = Category.getInstance(Log4JTest.class.getName());
	public static void main(String[] args)
	{
		MyIDHandler idhandler = new MyIDHandler();
		try
		{
			Driver d = (Driver)(Class.forName("oracle.jdbc.driver.OracleDriver").newInstance());
			DriverManager.registerDriver(d);
		}
		catch(Exception e){}
		cat.setPriority(Priority.DEBUG);
		JDBCAppender ja = new JDBCAppender();
		ja.setOption(JDBCAppender.CONNECTOR_OPTION, "MyConnectionHandler");
		ja.setOption(JDBCAppender.URL_OPTION, "jdbc:oracle:thin:@(DESCRIPTION=(ADDRESS_LIST=(ADDRESS=(COMMUNITY=tcp.world)(PROTOCOL=TCP)(Host=LENZI)(Port=1521))(ADDRESS=(COMMUNITY=tcp.world)(PROTOCOL=TCP)(Host=LENZI)(Port=1526)))(CONNECT_DATA=(SID=LENZI)))");
		ja.setOption(JDBCAppender.USERNAME_OPTION, "mex_pr_dev60");
		ja.setOption(JDBCAppender.PASSWORD_OPTION, "mex_pr_dev60");
		ja.setOption(JDBCAppender.TABLE_OPTION, "logtest");
		ja.setLogType("id_seq", LogType.EMPTY, "");
		ja.setLogType("id", LogType.ID, idhandler);
		ja.setLogType("msg", LogType.MSG, "");
		ja.setLogType("created_on", LogType.TIMESTAMP, "");
		ja.setLogType("created_by", LogType.STATIC, "FEN");
		cat.addAppender(ja);
		cat.debug("debug");
		cat.info("info");
		cat.error("error");
		cat.fatal("fatal");
	}
}
class MyConnectionHandler implements JDBCConnectionHandler
{
	Connection con = null;
	String url = "jdbc:oracle:thin:@(DESCRIPTION=(ADDRESS_LIST=(ADDRESS=(COMMUNITY=tcp.world)(PROTOCOL=TCP)(Host=LENZI)(Port=1521))(ADDRESS=(COMMUNITY=tcp.world)(PROTOCOL=TCP)(Host=LENZI)(Port=1526)))(CONNECT_DATA=(SID=LENZI)))";
   String username = "mex_pr_dev60";
   String password = "mex_pr_dev60";
   public Connection getConnection()
   {
	return getConnection(url, username, password);
   }
   public Connection getConnection(String _url, String _username, String _password)
   {
	try
	  {
		if(con != null && !con.isClosed()) con.close();
			con = DriverManager.getConnection(_url, _username, _password);
			con.setAutoCommit(false);
	  }
	  catch(Exception e){}
	return con;
   }
}
class MyIDHandler implements JDBCIDHandler
{
	private static long id = 0;
	public synchronized Object getID()
   {
		return new Long(++id);
   }
}

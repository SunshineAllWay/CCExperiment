package com.klopotek.utils.log;
import java.sql.*;
import java.util.*;
import org.apache.log4j.*;
import org.apache.log4j.helpers.*;
import org.apache.log4j.spi.*;
public class JDBCAppender extends AppenderSkeleton
{
	public static final String URL_OPTION			= "url";
	public static final String USERNAME_OPTION	= "username";
	public static final String PASSWORD_OPTION	= "password";
	public static final String TABLE_OPTION		= "table";
	public static final String CONNECTOR_OPTION	= "connector";
	public static final String COLUMNS_OPTION		= "columns";
	public static final String SQL_OPTION			= "sql";
	public static final String BUFFER_OPTION		= "buffer";
	public static final String COMMIT_OPTION		= "commit";
	private String url		= null;
	private String username	= null;
	private String password	= null;
	private String table		= null;
	private String connection_class = null;
	private String sql		= null;
	private boolean docommit = true;
	private int buffer_size	= 1;
   private JDBCConnectionHandler connectionHandler = null;
	private ArrayList buffer = new ArrayList();
	private Connection con = null;
	private JDBCLogger jlogger = new JDBCLogger();
	private boolean connected = false;
	private boolean configured = false;
	private boolean ready = false;
	public void finalize()
	{
		close();
      super.finalize();
	}
	public String[] getOptionStrings()
   {
		return new String[]{CONNECTOR_OPTION, URL_OPTION, USERNAME_OPTION, PASSWORD_OPTION, SQL_OPTION, TABLE_OPTION, COLUMNS_OPTION, BUFFER_OPTION, COMMIT_OPTION};
	}
	public void setOption(String _option, String _value)
	{
   	_option = _option.trim();
      _value = _value.trim();
		if(_option == null || _value == null) return;
		if(_option.length() == 0 || _value.length() == 0) return;
      _value = _value.trim();
		if(_option.equals(CONNECTOR_OPTION))
      {
      	if(!connected) connection_class = _value;
      }
		else if(_option.equals(URL_OPTION))
		{
			if(!connected) url = _value;
		}
		else if(_option.equals(USERNAME_OPTION))
		{
			if(!connected) username = _value;
		}
		else if(_option.equals(PASSWORD_OPTION))
		{
			if(!connected) password = _value;
		}
		else if(_option.equals(SQL_OPTION))
      {
			sql = _value;
      }
		else if(_option.equals(TABLE_OPTION))
      {
      	if(sql != null) return;
      	table = _value;
      }
		else if(_option.equals(COLUMNS_OPTION))
      {
      	if(sql != null) return;
			String name = null;
         int logtype = -1;
         String value = null;
         String column = null;
         String arg = null;
         int num_args = 0;
         int num_columns = 0;
			StringTokenizer st_col;
			StringTokenizer st_arg;
			st_col = new StringTokenizer(_value,  "	");
			num_columns = st_col.countTokens();
         if(num_columns < 1)
  	      {
     	   	errorHandler.error("JDBCAppender::setOption(), Invalid COLUMN_OPTION value : " + _value + " !");
            return;
        	}
         for(int i=1; i<=num_columns; i++)
         {
				column = st_col.nextToken();
				st_arg = new StringTokenizer(column, "~");
				num_args = st_arg.countTokens();
	         if(num_args < 2)
   	      {
      	   	errorHandler.error("JDBCAppender::setOption(), Invalid COLUMN_OPTION value : " + _value + " !");
               return;
         	}
	         for(int j=1; j<=num_args; j++)
   	      {
					arg = st_arg.nextToken();
					if(j == 1) name = arg;
					else if(j == 2)
      	      {
         	   	try
            	   {
							logtype = Integer.parseInt(arg);
	               }
   	            catch(Exception e)
      	         {
         	      	logtype = LogType.parseLogType(arg);
	               }
						if(!LogType.isLogType(logtype))
   	            {
	   	            errorHandler.error("JDBCAppender::setOption(), Invalid COLUMN_OPTION LogType : " + arg + " !");
                     return;
         	      }
            	}
					else if(j == 3) value = arg;
   	      }
	         if(!setLogType(name, logtype, value)) return;
         }
      }
		else if(_option.equals(BUFFER_OPTION))
      {
        	try
         {
				buffer_size = Integer.parseInt(_value);
         }
         catch(Exception e)
         {
	         errorHandler.error("JDBCAppender::setOption(), Invalid BUFFER_OPTION value : " + _value + " !");
				return;
         }
      }
		else if(_option.equals(COMMIT_OPTION))
      {
      	docommit = _value.equals("Y");
      }
      if(_option.equals(SQL_OPTION) || _option.equals(TABLE_OPTION))
      {
			if(!configured) configure();
      }
	}
	public boolean requiresLayout()
	{
		return true;
	}
	public void close()
	{
	   flush_buffer();
      if(connection_class == null)
      {
			try{con.close();}catch(Exception e){errorHandler.error("JDBCAppender::close(), " + e);}
      }
		this.closed = true;
	}
	public boolean setLogType(String _name, int _logtype, Object _value)
	{
   	if(sql != null) return true;
		if(!configured)
		{
			if(!configure()) return false;
		}
		try
		{
			jlogger.setLogType(_name, _logtype, _value);
		}
		catch(Exception e)
		{
			errorHandler.error("JDBCAppender::setLogType(), " + e);
			return false;
		}
		return true;
	}
	public void append(LoggingEvent event)
	{
		if(!ready)
      {
      	if(!ready())
         {
				errorHandler.error("JDBCAppender::append(), Not ready to append !");
         	return;
			}
      }
		buffer.add(event);
		if(buffer.size() >= buffer_size) flush_buffer();
	}
   public void flush_buffer()
   {
   	try
      {
      	int size = buffer.size();
         if(size < 1) return;
        	for(int i=0; i<size; i++)
         {
				LoggingEvent event = (LoggingEvent)buffer.get(i);
				jlogger.append(layout.format(event));
         }
         buffer.clear();
			if(docommit) con.commit();
      }
		catch(Exception e)
		{
			errorHandler.error("JDBCAppender::flush_buffer(), " + e + " : " + jlogger.getErrorMsg());
			try{con.rollback();} catch(Exception ex){}
			return;
		}
   }
	public boolean ready()
	{
   	if(ready) return true;
		if(!configured) return false;
		ready = jlogger.ready();
      if(!ready){errorHandler.error(jlogger.getErrorMsg());}
      return ready;
	}
	protected void connect() throws Exception
	{
   	if(connected) return;
		try
		{
      	if(connection_class == null)
         {
				if(url == null)		throw new Exception("JDBCAppender::connect(), No URL defined.");
				if(username == null)	throw new Exception("JDBCAppender::connect(), No USERNAME defined.");
				if(password == null)	throw new Exception("JDBCAppender::connect(), No PASSWORD defined.");
				connectionHandler = new DefaultConnectionHandler();
			}
         else
         {
				connectionHandler = (JDBCConnectionHandler)(Class.forName(connection_class).newInstance());
         }
         if(url != null && username != null && password != null)
         {
				con = connectionHandler.getConnection(url, username, password);
         }
         else
         {
	     		con = connectionHandler.getConnection();
         }
         if(con.isClosed())
         {
         	throw new Exception("JDBCAppender::connect(), JDBCConnectionHandler returns no connected Connection !");
			}
		}
		catch(Exception e)
		{
			throw new Exception("JDBCAppender::connect(), " + e);
		}
      connected = true;
	}
	protected boolean configure()
	{
		if(configured) return true;
		if(!connected)
		{
      	if((connection_class == null) && (url == null || username == null || password == null))
			{
				errorHandler.error("JDBCAppender::configure(), Missing database-options or connector-option !");
				return false;
         }
         try
         {
				connect();
         }
         catch(Exception e)
         {
         	connection_class = null;
            url = null;
				errorHandler.error("JDBCAppender::configure(), " + e);
            return false;
         }
		}
		if(sql == null && table == null)
		{
			errorHandler.error("JDBCAppender::configure(), No SQL_OPTION or TABLE_OPTION given !");
			return false;
		}
		if(!jlogger.isConfigured())
		{
			try
         {
         	jlogger.setConnection(con);
         	if(sql == null)
            {
	         	jlogger.configureTable(table);
            }
            else jlogger.configureSQL(sql);
         }
         catch(Exception e)
         {
	         errorHandler.error("JDBCAppender::configure(), " + e);
         	return false;
         }
		}
      if(layout == null)
      {
      	layout = new PatternLayout("%m");
      }
      configured = true;
		return true;
	}
}
class DefaultConnectionHandler implements JDBCConnectionHandler
{
	Connection con = null;
   public Connection getConnection()
   {
   	return con;
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

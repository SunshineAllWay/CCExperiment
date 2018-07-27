package com.klopotek.utils.log;
import java.sql.*;
import java.util.*;
import org.apache.log4j.*;
import org.apache.log4j.helpers.*;
import org.apache.log4j.spi.*;
public class JDBCLogger
{
	private ArrayList logcols = null;
   private String column_list = null;
	private int num = 0;
	private boolean isconfigured = false;
	private boolean ready = false;
   private String errormsg = "";
	private Connection con = null;
	private Statement stmt = null;
	private ResultSet rs = null;
   private String table = null;
   private String sql = null;
	private String new_sql = null;
   private String new_sql_part1 = null;
   private String new_sql_part2 = null;
   private static final String msg_wildcard = "@MSG@";
	private int msg_wildcard_pos = 0;
	public void append(String _msg) throws Exception
	{
		if(!ready) if(!ready()) throw new Exception("JDBCLogger::append(), Not ready to append !");
      if(sql != null)
      {
      	appendSQL(_msg);
         return;
      }
		LogColumn logcol;
		rs.moveToInsertRow();
		for(int i=0; i<num; i++)
		{
        	logcol = (LogColumn)logcols.get(i);
			if(logcol.logtype == LogType.MSG)
			{
				rs.updateObject(logcol.name, _msg);
			}
			else if(logcol.logtype == LogType.ID)
			{
				rs.updateObject(logcol.name, logcol.idhandler.getID());
			}
			else if(logcol.logtype == LogType.STATIC)
			{
				rs.updateObject(logcol.name, logcol.value);
			}
			else if(logcol.logtype == LogType.TIMESTAMP)
			{
				rs.updateObject(logcol.name, new Timestamp((new java.util.Date()).getTime()));
			}
		}
		rs.insertRow();
	}
	public void appendSQL(String _msg) throws Exception
	{
		if(!ready) if(!ready()) throw new Exception("JDBCLogger::appendSQL(), Not ready to append !");
      if(sql == null) throw new Exception("JDBCLogger::appendSQL(), No SQL-Statement configured !");
      if(msg_wildcard_pos > 0)
      {
			new_sql = new_sql_part1 + _msg + new_sql_part2;
      }
		else new_sql = sql;
      try
      {
			stmt.executeUpdate(new_sql);
      }
      catch(Exception e)
      {
      	errormsg = new_sql;
         throw e;
		}
	}
	public void configureTable(String _table) throws Exception
	{
   	if(isconfigured) return;
		stmt = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
		rs = stmt.executeQuery("SELECT * FROM " + _table + " WHERE 1 = 2");
		LogColumn logcol;
		ResultSetMetaData rsmd = rs.getMetaData();
		num = rsmd.getColumnCount();
		logcols = new ArrayList(num);
		for(int i=1; i<=num; i++)
		{
			logcol = new LogColumn();
			logcol.name = rsmd.getColumnName(i).toUpperCase();
			logcol.type = rsmd.getColumnTypeName(i);
			logcol.nullable = (rsmd.isNullable(i) == rsmd.columnNullable);
         logcol.isWritable = rsmd.isWritable(i);
         if(!logcol.isWritable) logcol.ignore = true;
         logcols.add(logcol);
		}
      table = _table;
		isconfigured = true;
	}
	public void configureSQL(String _sql) throws Exception
	{
   	if(isconfigured) return;
		if(!isConnected()) throw new Exception("JDBCLogger::configureSQL(), Not connected to database !");
		if(_sql == null || _sql.trim().equals("")) throw new Exception("JDBCLogger::configureSQL(), Invalid SQL-Statement !");
		sql = _sql.trim();
      stmt = con.createStatement();
		msg_wildcard_pos = sql.indexOf(msg_wildcard);
      if(msg_wildcard_pos > 0)
      {
			new_sql_part1 = sql.substring(0, msg_wildcard_pos-1) + "'";
         new_sql_part2 = "'" + sql.substring(msg_wildcard_pos+msg_wildcard.length());
		}
		isconfigured = true;
	}
	public void setConnection(Connection _con) throws Exception
	{
		con = _con;
		if(!isConnected()) throw new Exception("JDBCLogger::setConnection(), Given connection isnt connected to database !");
	}
	public void setLogType(String _name, int _logtype, Object _value) throws Exception
	{
		if(!isconfigured) throw new Exception("JDBCLogger::setLogType(), Not configured !");
      if(sql != null) return;
      _name = _name.toUpperCase();
		if(_name == null || !(_name.trim().length() > 0)) throw new Exception("JDBCLogger::setLogType(), Missing argument name !");
		if(!LogType.isLogType(_logtype)) throw new Exception("JDBCLogger::setLogType(), Invalid logtype '" + _logtype + "' !");
		if((_logtype != LogType.MSG && _logtype != LogType.EMPTY) && _value == null) throw new Exception("JDBCLogger::setLogType(), Missing argument value !");
  		LogColumn logcol;
		for(int i=0; i<num; i++)
		{
        	logcol = (LogColumn)logcols.get(i);
			if(logcol.name.equals(_name))
			{
         	if(!logcol.isWritable) throw new Exception("JDBCLogger::setLogType(), Column " + _name + " is not writeable !");
				if(_logtype == LogType.MSG)
            {
            	logcol.logtype = _logtype;
               return;
				}
				else if(_logtype == LogType.ID)
				{
					logcol.logtype = _logtype;
               try
               {
						logcol.idhandler = (JDBCIDHandler)_value;
               }
               catch(Exception e)
               {
               	try
                  {
							logcol.idhandler = (JDBCIDHandler)(Class.forName((String)_value).newInstance());
                  }
                  catch(Exception e2)
                  {
							throw new Exception("JDBCLogger::setLogType(), Cannot cast value of class " + _value.getClass() + " to class JDBCIDHandler !");
                  }
               }
               return;
				}
				else if(_logtype == LogType.STATIC)
				{
					logcol.logtype = _logtype;
					logcol.value = _value;
               return;
				}
				else if(_logtype == LogType.TIMESTAMP)
				{
					logcol.logtype = _logtype;
               return;
				}
				else if(_logtype == LogType.EMPTY)
				{
					logcol.logtype = _logtype;
					logcol.ignore = true;
               return;
				}
			}
		}
	}
	public boolean ready()
	{
   	if(ready) return true;
		if(!isconfigured){ errormsg = "Not ready to append ! Call configure() first !"; return false;}
      if(sql != null)
      {
      	ready = true;
         return true;
      }
		boolean msgcol_defined = false;
		LogColumn logcol;
		for(int i=0; i<num; i++)
		{
      	logcol = (LogColumn)logcols.get(i);
         if(logcol.ignore || !logcol.isWritable) continue;
			if(!logcol.nullable && logcol.logtype == LogType.EMPTY)
         {
         	errormsg = "Not ready to append ! Column " + logcol.name + " is not nullable, and must be specified by setLogType() !";
            return false;
         }
			if(logcol.logtype == LogType.ID && logcol.idhandler == null)
         {
         	errormsg = "Not ready to append ! Column " + logcol.name + " is specified as an ID-column, and a JDBCIDHandler has to be set !";
            return false;
         }
			else if(logcol.logtype == LogType.STATIC && logcol.value == null)
         {
         	errormsg = "Not ready to append ! Column " + logcol.name + " is specified as a static field, and a value has to be set !";
            return false;
         }
         else if(logcol.logtype == LogType.MSG) msgcol_defined = true;
		}
      if(!msgcol_defined) return false;
		for(int i=0; i<num; i++)
		{
      	logcol = (LogColumn)logcols.get(i);
			if(logcol.ignore || !logcol.isWritable) continue;
         if(logcol.logtype != LogType.EMPTY)
         {
				if(column_list == null)
            {
            	column_list = logcol.name;
            }
            else column_list += ", " + logcol.name;
         }
		}
      try
      {
			rs = stmt.executeQuery("SELECT " + column_list + " FROM " + table + " WHERE 1 = 2");
		}
      catch(Exception e)
      {
			errormsg = "Not ready to append ! Cannot select columns '" + column_list + "' of table " + table + " !";
      	return false;
      }
		ready = true;
		return true;
	}
	public boolean isConfigured(){ return isconfigured;}
	public boolean isConnected()
   {
   	try
      {
   		return (con != null && !con.isClosed());
      }
      catch(Exception e){return false;}
   }
   public String getErrorMsg(){String r = new String(errormsg); errormsg = null; return r;}
}
class LogColumn
{
	String name = null;
	String type = null;
	boolean nullable = false;
   boolean isWritable = false;
   boolean ignore = false;
	int logtype = LogType.EMPTY;
	Object value = null;				
	JDBCIDHandler idhandler = null;
}
class LogType
{
	public static final int MSG = 1;
	public static final int ID = 2;
	public static final int STATIC = 3;
	public static final int TIMESTAMP = 4;
	public static final int EMPTY = 5;
	public static boolean isLogType(int _lt)
	{
		if(_lt == MSG || _lt == STATIC || _lt == ID || _lt == TIMESTAMP || _lt == EMPTY) return true;
		return false;
	}
   public static int parseLogType(String _lt)
   {
		if(_lt.equals("MSG")) return MSG;
		if(_lt.equals("ID")) return ID;
		if(_lt.equals("STATIC")) return STATIC;
		if(_lt.equals("TIMESTAMP")) return TIMESTAMP;
		if(_lt.equals("EMPTY")) return EMPTY;
      return -1;
   }
}

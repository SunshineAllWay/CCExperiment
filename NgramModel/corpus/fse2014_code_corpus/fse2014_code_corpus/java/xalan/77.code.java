package org.apache.xalan.lib.sql;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
public interface ConnectionPool
{
  public boolean isEnabled( );
  public void setDriver( String d );
  public void setURL( String url );
  public void freeUnused( );
  public boolean hasActiveConnections( );
  public void setPassword( String p );
  public void setUser( String u );
  public void setMinConnections( int n );
  public boolean testConnection( );
  public Connection getConnection( )throws SQLException;
  public void releaseConnection( Connection con )throws SQLException;
  public void releaseConnectionOnError( Connection con )throws SQLException;
  public void setPoolEnabled( final boolean flag );
  public void setProtocol(Properties p);
}

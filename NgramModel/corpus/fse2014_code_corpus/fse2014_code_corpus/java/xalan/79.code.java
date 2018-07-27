package org.apache.xalan.lib.sql;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Properties;
import java.util.Vector;
import org.apache.xalan.res.XSLMessages;
import org.apache.xalan.res.XSLTErrorResources;
public class DefaultConnectionPool implements ConnectionPool
{
  private Driver m_Driver = null;
  private static final boolean DEBUG = false;
  private String m_driver = new String("");
  private String m_url = new String("");
  private int m_PoolMinSize = 1;
  private Properties m_ConnectionProtocol = new Properties();
  private Vector m_pool = new Vector();
  private boolean m_IsActive = false;
  public DefaultConnectionPool( ) {}
  public boolean isEnabled( )
  {
    return m_IsActive;
  }
  public void setDriver( String d )
  {
    m_driver = d;
  }
  public void setURL( String url )
  {
    m_url = url;
  }
  public void freeUnused( )
  {
    Iterator i = m_pool.iterator();
    while(i.hasNext())
    {
      PooledConnection pcon =
        (PooledConnection) i.next();
      if ( pcon.inUse() == false )
      {
        if (DEBUG)
        {
          System.err.println("Closing JDBC Connection ");
        }
        pcon.close();
        i.remove();        
      }
    }
  }
  public boolean hasActiveConnections( )
  {
    return (m_pool.size() > 0);
  }
  public void setPassword( String p )
  {
    m_ConnectionProtocol.put("password", p);
  }
  public void setUser( String u )
  {
    m_ConnectionProtocol.put("user", u);
  }
  public void setProtocol( Properties p )
  {
    Enumeration e = p.keys();
    while (e.hasMoreElements())
    {
      String key = (String) e.nextElement();
      m_ConnectionProtocol.put(key, p.getProperty(key));
    }
  }
  public void setMinConnections( int n )
  {
    m_PoolMinSize = n;
  }
  public boolean testConnection( )
  {
    try
    {
      if (DEBUG)
      {
        System.out.println("Testing Connection");
      }
      Connection conn = getConnection();
      if (DEBUG)
      {
        DatabaseMetaData dma = conn.getMetaData();
        System.out.println("\nConnected to " + dma.getURL());
        System.out.println("Driver   " + dma.getDriverName());
        System.out.println("Version  " + dma.getDriverVersion());
        System.out.println("");
      }
      if (conn == null) return false;
      releaseConnection(conn);
      if (DEBUG)
      {
        System.out.println("Testing Connection, SUCCESS");
      }
      return true;
    }
    catch(Exception e)
    {
      if (DEBUG)
      {
        System.out.println("Testing Connection, FAILED");
        e.printStackTrace();
      }
      return false;
    }
  }
  public synchronized Connection getConnection( )throws IllegalArgumentException, SQLException
  {
    PooledConnection pcon = null;
    if ( m_pool.size() < m_PoolMinSize ) { initializePool(); }
    for ( int x = 0; x < m_pool.size(); x++ )
    {
      pcon = (PooledConnection) m_pool.elementAt(x);
      if ( pcon.inUse() == false )
      {
        pcon.setInUse(true);
        return pcon.getConnection();
      }
    }
    Connection con = createConnection();
    pcon = new PooledConnection(con);
    pcon.setInUse(true);
    m_pool.addElement(pcon);
    return pcon.getConnection();
  }
  public synchronized void releaseConnection( Connection con )throws SQLException
  {
    for ( int x = 0; x < m_pool.size(); x++ )
    {
      PooledConnection pcon =
        (PooledConnection) m_pool.elementAt(x);
      if ( pcon.getConnection() == con )
      {
        if (DEBUG)
        {
          System.out.println("Releasing Connection " + x);
        }
        if (! isEnabled())
        {
          con.close();
          m_pool.removeElementAt(x);
          if (DEBUG)
          {
            System.out.println("-->Inactive Pool, Closing connection");
          }
        }
        else
        {
          pcon.setInUse(false);
        }
        break;
      }
    }
  }
  public synchronized void releaseConnectionOnError( Connection con )throws SQLException
  {
    for ( int x = 0; x < m_pool.size(); x++ )
    {
      PooledConnection pcon =
        (PooledConnection) m_pool.elementAt(x);
      if ( pcon.getConnection() == con )
      {
        if (DEBUG)
        {
          System.out.println("Releasing Connection On Error" + x);
        }
        con.close();
        m_pool.removeElementAt(x);
        if (DEBUG)
        {
          System.out.println("-->Inactive Pool, Closing connection");
        }
        break;
      }
    }
  }
  private Connection createConnection( )throws SQLException
  {
    Connection con = null;
    con = m_Driver.connect(m_url, m_ConnectionProtocol );
    return con;
  }
  public synchronized void initializePool( )throws IllegalArgumentException, SQLException
  {
     if ( m_driver == null )
     {
       throw new IllegalArgumentException(XSLMessages.createMessage(XSLTErrorResources.ER_NO_DRIVER_NAME_SPECIFIED, null));
     }
     if ( m_url == null )
     {
       throw new IllegalArgumentException(XSLMessages.createMessage(XSLTErrorResources.ER_NO_URL_SPECIFIED, null));
     }
     if ( m_PoolMinSize < 1 )
     {
       throw new IllegalArgumentException(XSLMessages.createMessage(XSLTErrorResources.ER_POOLSIZE_LESS_THAN_ONE, null));
     }
     try
     {
        m_Driver = (Driver) ObjectFactory.newInstance(
          m_driver, ObjectFactory.findClassLoader(), true);
        DriverManager.registerDriver(m_Driver);
     }
     catch(ObjectFactory.ConfigurationError e)
     {
       throw new IllegalArgumentException(XSLMessages.createMessage(XSLTErrorResources.ER_INVALID_DRIVER_NAME, null));
     }
     catch(Exception e)
     {
       throw new IllegalArgumentException(XSLMessages.createMessage(XSLTErrorResources.ER_INVALID_DRIVER_NAME, null));
     }
     if ( !m_IsActive) return;
    do
    {
      Connection con = createConnection();
      if ( con != null )
      {
        PooledConnection pcon = new PooledConnection(con);
        addConnection(pcon);
        if (DEBUG) System.out.println("Adding DB Connection to the Pool");
      }
    }
    while (m_pool.size() < m_PoolMinSize);
  }
  private void addConnection( PooledConnection value )
  {
    m_pool.addElement(value);
  }
  protected void finalize( )throws Throwable
  {
    if (DEBUG)
    {
      System.out.println("In Default Connection Pool, Finalize");
    }
    for ( int x = 0; x < m_pool.size(); x++ )
    {
      if (DEBUG)
      {
        System.out.println("Closing JDBC Connection " + x);
      }
      PooledConnection pcon =
        (PooledConnection) m_pool.elementAt(x);
      if ( pcon.inUse() == false ) { pcon.close();  }
      else
      {
        if (DEBUG)
        {
          System.out.println("--> Force close");
        }
        try
        {
          java.lang.Thread.sleep(30000);
          pcon.close();
        }
        catch (InterruptedException ie)
        {
          if (DEBUG) System.err.println(ie.getMessage());
        }
      }
    }
    if (DEBUG)
    {
      System.out.println("Exit Default Connection Pool, Finalize");
    }
    super.finalize();
  }
  public void setPoolEnabled( boolean flag )
  {
     m_IsActive = flag;
     if ( ! flag )
      freeUnused();
  }
}

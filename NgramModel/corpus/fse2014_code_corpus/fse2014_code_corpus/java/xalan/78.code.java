 package org.apache.xalan.lib.sql;
import java.util.Hashtable;
import org.apache.xalan.res.XSLMessages;
import org.apache.xalan.res.XSLTErrorResources;
public class ConnectionPoolManager
{
  private static Hashtable m_poolTable = null;
  public ConnectionPoolManager( )
  {
    init();
  }
  private synchronized void init( )
  {
    if (m_poolTable == null)
            m_poolTable = new Hashtable();
  }
  public synchronized void registerPool( String name, ConnectionPool pool )
  {
    if ( m_poolTable.containsKey(name) )
    {
      throw new IllegalArgumentException(XSLMessages.createMessage(XSLTErrorResources.ER_POOL_EXISTS, null)); 
    }
    m_poolTable.put(name, pool);
  }
  public synchronized void removePool( String name )
  {
    ConnectionPool pool = getPool(name);
    if (null != pool)
    {
      pool.setPoolEnabled(false);
      if ( ! pool.hasActiveConnections() ) m_poolTable.remove(name);
    }
  }
  public synchronized ConnectionPool getPool( String name )
  {
    return (ConnectionPool) m_poolTable.get(name);
  }
}

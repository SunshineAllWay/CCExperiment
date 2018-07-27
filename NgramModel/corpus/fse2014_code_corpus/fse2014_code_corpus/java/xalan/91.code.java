package org.apache.xalan.lib.sql;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;
import javax.xml.transform.ErrorListener;
import javax.xml.transform.TransformerException;
import org.apache.xalan.extensions.ExpressionContext;
import org.apache.xml.dtm.DTM;
import org.apache.xml.dtm.DTMIterator;
import org.apache.xml.dtm.DTMManager;
import org.apache.xml.dtm.ref.DTMManagerDefault;
import org.apache.xml.dtm.ref.DTMNodeIterator;
import org.apache.xml.dtm.ref.DTMNodeProxy;
import org.apache.xpath.XPathContext;
import org.apache.xpath.objects.XBooleanStatic;
import org.apache.xpath.objects.XNodeSet;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
public class XConnection
{
  private static final boolean DEBUG = false;
  private ConnectionPool m_ConnectionPool = null;
  private Connection m_Connection = null;
  private boolean m_DefaultPoolingEnabled = false;
  private Vector m_OpenSQLDocuments = new Vector();
  private ConnectionPoolManager m_PoolMgr = new ConnectionPoolManager();
  private Vector m_ParameterList = new Vector();
  private Exception m_Error = null;
  private SQLDocument     m_LastSQLDocumentWithError = null;
  private boolean m_FullErrors = false;
  private SQLQueryParser m_QueryParser = new SQLQueryParser();
  private boolean m_IsDefaultPool = false;
  private boolean m_IsStreamingEnabled = true;
   private boolean m_InlineVariables = false;
  private boolean m_IsMultipleResultsEnabled = false;
  private boolean m_IsStatementCachingEnabled = false;
  public XConnection( )
  {
  }
  public XConnection( ExpressionContext exprContext, String connPoolName )
  {
    connect(exprContext, connPoolName);
  }
  public XConnection( ExpressionContext exprContext, String driver, String dbURL )
  {
    connect(exprContext, driver, dbURL);
  }
  public XConnection( ExpressionContext exprContext, NodeList list )
  {
    connect(exprContext, list);
  }
  public XConnection( ExpressionContext exprContext, String driver, String dbURL, String user, String password )
  {
    connect(exprContext, driver, dbURL, user, password);
  }
  public XConnection( ExpressionContext exprContext, String driver, String dbURL, Element protocolElem )
  {
    connect(exprContext, driver, dbURL, protocolElem);
  }
   public XBooleanStatic connect( ExpressionContext exprContext, String name )
   {
     try
     {
       m_ConnectionPool = m_PoolMgr.getPool(name);
       if (m_ConnectionPool == null)
       {
         ConnectionPool pool = new JNDIConnectionPool(name);
         if (pool.testConnection())
         {
           m_PoolMgr.registerPool(name, pool);
           m_ConnectionPool = pool;
           m_IsDefaultPool = false;
           return new XBooleanStatic(true);
         }
         else
         {
           throw new IllegalArgumentException(
               "Invalid ConnectionPool name or JNDI Datasource path: " + name);
         }
       }
       else
       {
         m_IsDefaultPool = false;
         return new XBooleanStatic(true);
       }
     }
     catch (Exception e)
     {
       setError(e, exprContext);
       return new XBooleanStatic(false);
     }
   }
  public XBooleanStatic connect( ExpressionContext exprContext, String driver, String dbURL )
  {
    try
    {
      init(driver, dbURL, new Properties());
      return new XBooleanStatic(true);
    }
    catch(SQLException e)
    {
      setError(e,exprContext);
      return new XBooleanStatic(false);
    }
    catch (Exception e)
    {
      setError(e,exprContext);
      return new XBooleanStatic(false);
    }
  }
  public XBooleanStatic connect( ExpressionContext exprContext, Element protocolElem )
  {
    try
    {
      initFromElement(protocolElem);
      return new XBooleanStatic(true);
    }
    catch(SQLException e)
    {
      setError(e,exprContext);
      return new XBooleanStatic(false);
    }
    catch (Exception e)
    {
      setError(e,exprContext);
      return new XBooleanStatic(false);
    }
  }
  public XBooleanStatic connect( ExpressionContext exprContext, NodeList list )
  {
    try
    {
      initFromElement( (Element) list.item(0) );
      return new XBooleanStatic(true);
    }
    catch(SQLException e)
    {
      setError(e, exprContext);
      return new XBooleanStatic(false);
    }
    catch (Exception e)
    {
      setError(e,exprContext);
      return new XBooleanStatic(false);
    }
  }
  public XBooleanStatic connect( ExpressionContext exprContext, String driver, String dbURL, String user, String password )
  {
    try
    {
      Properties prop = new Properties();
      prop.put("user", user);
      prop.put("password", password);
      init(driver, dbURL, prop);
      return new XBooleanStatic(true);
    }
    catch(SQLException e)
    {
      setError(e,exprContext);
      return new XBooleanStatic(false);
    }
    catch (Exception e)
    {
      setError(e,exprContext);
      return new XBooleanStatic(false);
    }
  }
  public XBooleanStatic connect( ExpressionContext exprContext, String driver, String dbURL, Element protocolElem )
  {
    try
    {
      Properties prop = new Properties();
      NamedNodeMap atts = protocolElem.getAttributes();
      for (int i = 0; i < atts.getLength(); i++)
      {
        prop.put(atts.item(i).getNodeName(), atts.item(i).getNodeValue());
      }
      init(driver, dbURL, prop);
      return new XBooleanStatic(true);
    }
    catch(SQLException e)
    {
      setError(e,exprContext);
      return new XBooleanStatic(false);
    }
    catch (Exception e)
    {
      setError(e, exprContext);
      return new XBooleanStatic(false);
    }
  }
  private void initFromElement( Element e )throws SQLException
  {
    Properties prop = new Properties();
    String driver = "";
    String dbURL = "";
    Node n = e.getFirstChild();
    if (null == n) return; 
    do
    {
      String nName = n.getNodeName();
      if (nName.equalsIgnoreCase("dbdriver"))
      {
        driver = "";
        Node n1 = n.getFirstChild();
        if (null != n1)
        {
          driver = n1.getNodeValue();
        }
      }
      if (nName.equalsIgnoreCase("dburl"))
      {
        dbURL = "";
        Node n1 = n.getFirstChild();
        if (null != n1)
        {
          dbURL = n1.getNodeValue();
        }
      }
      if (nName.equalsIgnoreCase("password"))
      {
        String s = "";
        Node n1 = n.getFirstChild();
        if (null != n1)
        {
          s = n1.getNodeValue();
        }
        prop.put("password", s);
      }
      if (nName.equalsIgnoreCase("user"))
      {
        String s = "";
        Node n1 = n.getFirstChild();
        if (null != n1)
        {
          s = n1.getNodeValue();
        }
        prop.put("user", s);
      }
      if (nName.equalsIgnoreCase("protocol"))
      {
        String Name = "";
        NamedNodeMap attrs = n.getAttributes();
        Node n1 = attrs.getNamedItem("name");
        if (null != n1)
        {
          String s = "";
          Name = n1.getNodeValue();
          Node n2 = n.getFirstChild();
          if (null != n2) s = n2.getNodeValue();
          prop.put(Name, s);
        }
      }
    } while ( (n = n.getNextSibling()) != null);
    init(driver, dbURL, prop);
  }
  private void init( String driver, String dbURL, Properties prop )throws SQLException
  {
    Connection con = null;
    if (DEBUG)
      System.out.println("XConnection, Connection Init");
    String user = prop.getProperty("user");
    if (user == null) user = "";
    String passwd = prop.getProperty("password");
    if (passwd == null) passwd = "";
    String poolName = driver + dbURL + user + passwd;
    ConnectionPool cpool = m_PoolMgr.getPool(poolName);
    if (cpool == null)
    {
      if (DEBUG)
      {
        System.out.println("XConnection, Creating Connection");
        System.out.println(" Driver  :" + driver);
        System.out.println(" URL     :" + dbURL);
        System.out.println(" user    :" + user);
        System.out.println(" passwd  :" + passwd);
      }
      DefaultConnectionPool defpool = new DefaultConnectionPool();
      if ((DEBUG) && (defpool == null))
        System.out.println("Failed to Create a Default Connection Pool");
      defpool.setDriver(driver);
      defpool.setURL(dbURL);
      defpool.setProtocol(prop);
      if (m_DefaultPoolingEnabled) defpool.setPoolEnabled(true);
      m_PoolMgr.registerPool(poolName, defpool);
      m_ConnectionPool = defpool;
    }
    else
    {
      m_ConnectionPool = cpool;
    }
    m_IsDefaultPool = true;
    try
    {
      con = m_ConnectionPool.getConnection();
    }
    catch(SQLException e)
    {
      if (con != null)
      {
        m_ConnectionPool.releaseConnectionOnError(con);
        con = null;
      }
      throw e;
    }
    finally
    {
      if ( con != null ) m_ConnectionPool.releaseConnection(con);
    }
  }
  public ConnectionPool getConnectionPool()
  {
    return m_ConnectionPool;
  }
  public DTM query( ExpressionContext exprContext, String queryString )
  {
    SQLDocument doc = null;
    try
    {
      if (DEBUG) System.out.println("pquery()");
      if ( null == m_ConnectionPool ) return null;
      SQLQueryParser query =
          m_QueryParser.parse
            (this, queryString, SQLQueryParser.NO_INLINE_PARSER);
      doc = SQLDocument.getNewDocument(exprContext);
      doc.execute(this, query);
      m_OpenSQLDocuments.addElement(doc);
    }
    catch (Exception e)
    {
      if (DEBUG) System.out.println("exception in query()");
      if (doc != null)
      {
        if (doc.hasErrors())
        {
          setError(e, doc, doc.checkWarnings());
        }
        doc.close(m_IsDefaultPool);
        doc = null;
      }
    }
    finally
    {
      if (DEBUG) System.out.println("leaving query()");
    }
    return doc;
  }
  public DTM pquery( ExpressionContext exprContext, String queryString )
  {
    return(pquery(exprContext, queryString, null));
  }
  public DTM pquery( ExpressionContext exprContext, String queryString, String typeInfo)
  {
    SQLDocument doc = null;
    try
    {
      if (DEBUG) System.out.println("pquery()");
      if ( null == m_ConnectionPool ) return null;
      SQLQueryParser query =
          m_QueryParser.parse
            (this, queryString, SQLQueryParser.NO_OVERRIDE);
      if ( !m_InlineVariables )
      {
        addTypeToData(typeInfo);
        query.setParameters(m_ParameterList);
      }
      doc = SQLDocument.getNewDocument(exprContext);
      doc.execute(this, query);
      m_OpenSQLDocuments.addElement(doc);
    }
    catch (Exception e)
    {
      if (DEBUG) System.out.println("exception in query()");
      if (doc != null)
      {
        if (doc.hasErrors())
        {
          setError(e, doc, doc.checkWarnings());
        }
        doc.close(m_IsDefaultPool);
        doc = null;
      }
    }
    finally
    {
      if (DEBUG) System.out.println("leaving query()");
    }
    return doc;
  }
  public void skipRec( ExpressionContext exprContext, Object o, int value )
  {
    SQLDocument sqldoc = null;
    DTMNodeIterator nodei = null;
    sqldoc = locateSQLDocument( exprContext, o);
    if (sqldoc != null) sqldoc.skip(value);
  }
  private void addTypeToData(String typeInfo)
  {
      int indx;
      if ( typeInfo != null && m_ParameterList != null )
      {
          StringTokenizer plist = new StringTokenizer(typeInfo);
          indx = 0;
          while (plist.hasMoreTokens())
          {
            String value = plist.nextToken();
            QueryParameter qp = (QueryParameter) m_ParameterList.elementAt(indx);
            if ( null != qp )
            {
              qp.setTypeName(value);
            }
            indx++;
          }
      }
  }
  public void addParameter( String value )
  {
    addParameterWithType(value, null);
  }
  public void addParameterWithType( String value, String Type )
  {
    m_ParameterList.addElement( new QueryParameter(value, Type) );
  }
  public void addParameterFromElement( Element e )
  {
    NamedNodeMap attrs = e.getAttributes();
    Node Type = attrs.getNamedItem("type");
    Node n1  = e.getFirstChild();
    if (null != n1)
    {
      String value = n1.getNodeValue();
      if (value == null) value = "";
      m_ParameterList.addElement( new QueryParameter(value, Type.getNodeValue()) );
    }
  }
  public void addParameterFromElement( NodeList nl )
  {
    int count = nl.getLength();
    for (int x=0; x<count; x++)
    {
      addParameters( (Element) nl.item(x));
    }
  }
  private void addParameters( Element elem )
  {
    Node n = elem.getFirstChild();
    if (null == n) return;
    do
    {
      if (n.getNodeType() == Node.ELEMENT_NODE)
      {
        NamedNodeMap attrs = n.getAttributes();
        Node Type = attrs.getNamedItem("type");
        String TypeStr;
        if (Type == null) TypeStr = "string";
        else TypeStr = Type.getNodeValue();
        Node n1  = n.getFirstChild();
        if (null != n1)
        {
          String value = n1.getNodeValue();
          if (value == null) value = "";
          m_ParameterList.addElement(
            new QueryParameter(value, TypeStr) );
        }
      }
    } while ( (n = n.getNextSibling()) != null);
  }
  public void clearParameters( )
  {
    m_ParameterList.removeAllElements();
  }
  public void enableDefaultConnectionPool( )
  {
    if (DEBUG)
      System.out.println("Enabling Default Connection Pool");
    m_DefaultPoolingEnabled = true;
    if (m_ConnectionPool == null) return;
    if (m_IsDefaultPool) return;
    m_ConnectionPool.setPoolEnabled(true);
  }
  public void disableDefaultConnectionPool( )
  {
    if (DEBUG)
      System.out.println("Disabling Default Connection Pool");
    m_DefaultPoolingEnabled = false;
    if (m_ConnectionPool == null) return;
    if (!m_IsDefaultPool) return;
    m_ConnectionPool.setPoolEnabled(false);
  }
  public void enableStreamingMode( )
  {
    if (DEBUG)
      System.out.println("Enabling Streaming Mode");
    m_IsStreamingEnabled = true;
  }
  public void disableStreamingMode( )
  {
    if (DEBUG)
      System.out.println("Disable Streaming Mode");
    m_IsStreamingEnabled = false;
  }
  public DTM getError( )
  {
    if ( m_FullErrors )
    {
      for ( int idx = 0 ; idx < m_OpenSQLDocuments.size() ; idx++ )
      {
        SQLDocument doc = (SQLDocument)m_OpenSQLDocuments.elementAt(idx);
        SQLWarning warn = doc.checkWarnings();
        if ( warn != null ) setError(null, doc, warn);
      }
    }
    return(buildErrorDocument());
  }
  public void close( )throws SQLException
  {
    if (DEBUG)
      System.out.println("Entering XConnection.close()");
    while(m_OpenSQLDocuments.size() != 0)
    {
      SQLDocument d = (SQLDocument) m_OpenSQLDocuments.elementAt(0);
      try
      {
        d.close(m_IsDefaultPool);
      }
      catch (Exception se ) {}
      m_OpenSQLDocuments.removeElementAt(0);
    }
    if ( null != m_Connection )
    {
      m_ConnectionPool.releaseConnection(m_Connection);
      m_Connection = null;
    }
    if (DEBUG)
      System.out.println("Exiting XConnection.close");
  }
  public void close(ExpressionContext exprContext, Object doc) throws SQLException 
  {
    if (DEBUG)
        System.out.println("Entering XConnection.close(" + doc + ")");
    SQLDocument sqlDoc = locateSQLDocument(exprContext, doc);
    if (sqlDoc != null)
    {
      sqlDoc.close(m_IsDefaultPool);
      m_OpenSQLDocuments.remove(sqlDoc);
    } 
  }
  private SQLDocument locateSQLDocument(ExpressionContext exprContext, Object doc)
  {
    try
    {
      if (doc instanceof DTMNodeIterator)
      {
        DTMNodeIterator dtmIter = (DTMNodeIterator)doc;
        try
        {
          DTMNodeProxy root = (DTMNodeProxy)dtmIter.getRoot();
          return (SQLDocument) root.getDTM();
        }
        catch (Exception e)
        {
          XNodeSet xNS = (XNodeSet)dtmIter.getDTMIterator();
          DTMIterator iter = (DTMIterator)xNS.getContainedIter();
          DTM dtm = iter.getDTM(xNS.nextNode());
          return (SQLDocument)dtm;
        }
      }
      setError(new Exception("SQL Extension:close - Can Not Identify SQLDocument"), exprContext);    
      return null;  
    }
    catch(Exception e)
    {
      setError(e, exprContext);
      return null;
    }
  }
  private SQLErrorDocument buildErrorDocument()
  {
    SQLErrorDocument eDoc = null;
    if ( m_LastSQLDocumentWithError != null)
    {
      ExpressionContext ctx = m_LastSQLDocumentWithError.getExpressionContext();
      SQLWarning        warn = m_LastSQLDocumentWithError.checkWarnings();
      try
      {
        DTMManager mgr =
          ((XPathContext.XPathExpressionContext)ctx).getDTMManager();
        DTMManagerDefault mgrDefault = (DTMManagerDefault) mgr;
        int dtmIdent = mgrDefault.getFirstFreeDTMID();
        eDoc = new SQLErrorDocument(
            mgr, dtmIdent<<DTMManager.IDENT_DTM_NODE_BITS,
            m_Error, warn, m_FullErrors);
        mgrDefault.addDTM(eDoc, dtmIdent);
        m_Error = null;
        m_LastSQLDocumentWithError = null;
      }
      catch(Exception e)
      {
        eDoc = null;
      }
    }
    return(eDoc);
  }
  public void setError(Exception excp,ExpressionContext expr)
  {
    try
    {
      ErrorListener listen = expr.getErrorListener();
      if ( listen != null && excp != null )
      {
        listen.warning(
          new TransformerException(excp.toString(),
          expr.getXPathContext().getSAXLocator(), excp));
      }
    }
    catch(Exception e) {}
  }
  public void setError(Exception excp, SQLDocument doc, SQLWarning warn)
  {
    ExpressionContext cont = doc.getExpressionContext();
    m_LastSQLDocumentWithError = doc;
    try
    {
      ErrorListener listen = cont.getErrorListener();
      if ( listen != null && excp != null )
      listen.warning(
        new TransformerException(excp.toString(),
        cont.getXPathContext().getSAXLocator(), excp));
      if ( listen != null && warn != null )
      {
        listen.warning(new TransformerException(
          warn.toString(), cont.getXPathContext().getSAXLocator(), warn));
      }
      if ( excp != null )  m_Error = excp;
      if ( warn != null )
      {
        SQLWarning tw =
          new SQLWarning(warn.getMessage(), warn.getSQLState(),
            warn.getErrorCode());
        SQLWarning nw = warn.getNextWarning();
        while ( nw != null )
        {
          tw.setNextWarning(new SQLWarning(nw.getMessage(),
            nw.getSQLState(), nw.getErrorCode()));
          nw = nw.getNextWarning();
        }
        tw.setNextWarning(
          new SQLWarning(warn.getMessage(), warn.getSQLState(),
            warn.getErrorCode()));
      }
    }
    catch(Exception e)
    {
    }
  }
  public void setFeature(String feature, String setting)
  {
    boolean value = false;
    if ( "true".equalsIgnoreCase(setting) ) value = true;
    if ( "streaming".equalsIgnoreCase(feature) )
    {
      m_IsStreamingEnabled = value;
    }
    else if ( "inline-variables".equalsIgnoreCase(feature) )
    {
      m_InlineVariables = value;
    }
    else if ( "multiple-results".equalsIgnoreCase(feature) )
    {
      m_IsMultipleResultsEnabled = value;
    }
    else if ( "cache-statements".equalsIgnoreCase(feature) )
    {
      m_IsStatementCachingEnabled = value;
    }
    else if ( "default-pool-enabled".equalsIgnoreCase(feature) )
    {
      m_DefaultPoolingEnabled = value;
      if (m_ConnectionPool == null) return;
      if (m_IsDefaultPool) return;
      m_ConnectionPool.setPoolEnabled(value);
    }
    else if ( "full-errors".equalsIgnoreCase(feature) )
    {
      m_FullErrors = value;
    }
  }
  public String getFeature(String feature)
  {
    String value = null;
    if ( "streaming".equalsIgnoreCase(feature) )
      value = m_IsStreamingEnabled ? "true" : "false";
    else if ( "inline-variables".equalsIgnoreCase(feature) )
      value = m_InlineVariables ? "true" : "false";
    else if ( "multiple-results".equalsIgnoreCase(feature) )
      value = m_IsMultipleResultsEnabled ? "true" : "false";
    else if ( "cache-statements".equalsIgnoreCase(feature) )
      value = m_IsStatementCachingEnabled ? "true" : "false";
    else if ( "default-pool-enabled".equalsIgnoreCase(feature) )
      value = m_DefaultPoolingEnabled ? "true" : "false";
    else if ( "full-errors".equalsIgnoreCase(feature) )
      value = m_FullErrors ? "true" : "false";
    return(value);
  }
  protected void finalize( )
  {
    if (DEBUG) System.out.println("In XConnection, finalize");
    try
    {
      close();
    }
    catch(Exception e)
    {
    }
  }
}

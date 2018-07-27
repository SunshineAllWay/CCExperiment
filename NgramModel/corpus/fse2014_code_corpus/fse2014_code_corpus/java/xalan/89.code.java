package org.apache.xalan.lib.sql;
import java.sql.SQLException;
import java.sql.SQLWarning;
import org.apache.xml.dtm.DTM;
import org.apache.xml.dtm.DTMManager;
public class SQLErrorDocument extends DTMDocument
{
  private static final String S_EXT_ERROR = "ext-error";
  private static final String S_SQL_ERROR = "sql-error";
  private static final String S_MESSAGE = "message";
  private static final String S_CODE = "code";
  private static final String S_STATE = "state";
  private static final String S_SQL_WARNING = "sql-warning";
  private int m_ErrorExt_TypeID = DTM.NULL;
  private int m_Message_TypeID = DTM.NULL;
  private int m_Code_TypeID = DTM.NULL;
  private int m_State_TypeID = DTM.NULL;
  private int m_SQLWarning_TypeID = DTM.NULL;
  private int m_SQLError_TypeID = DTM.NULL;
  private int m_rootID = DTM.NULL;
  private int m_extErrorID = DTM.NULL;
  private int m_MainMessageID = DTM.NULL;
  public SQLErrorDocument( DTMManager mgr, int ident, SQLException error )
  {
    super(mgr, ident);
    createExpandedNameTable();
    buildBasicStructure(error);
    int sqlError = addElement(2, m_SQLError_TypeID, m_extErrorID, m_MainMessageID);
    int element = DTM.NULL;
    element = addElementWithData(
      new Integer(error.getErrorCode()), 3,
      m_Code_TypeID, sqlError, element);
    element = addElementWithData(
      error.getLocalizedMessage(), 3,
      m_Message_TypeID, sqlError, element);
  }
  public SQLErrorDocument( DTMManager mgr, int ident, Exception error )
  {
    super(mgr, ident);
    createExpandedNameTable();
    buildBasicStructure(error);
  }
  public SQLErrorDocument(DTMManager mgr, int ident, Exception error, SQLWarning warning, boolean full)
  {
    super(mgr, ident);
    createExpandedNameTable();
    buildBasicStructure(error);
	SQLException se = null;
	int prev = m_MainMessageID;
	boolean inWarnings = false;
	if ( error != null && error instanceof SQLException )
		se = (SQLException)error;
	else if ( full && warning != null )
	{
		se = warning;
		inWarnings = true;
	}
	while ( se != null )
	{
	    int sqlError = addElement(2, inWarnings ? m_SQLWarning_TypeID : m_SQLError_TypeID, m_extErrorID, prev);
		prev = sqlError;
    	int element = DTM.NULL;
	    element = addElementWithData(
	      new Integer(se.getErrorCode()), 3,
	      m_Code_TypeID, sqlError, element);
	    element = addElementWithData(
	      se.getLocalizedMessage(), 3,
	      m_Message_TypeID, sqlError, element);
		if ( full )
		{
			String state = se.getSQLState();
			if ( state != null && state.length() > 0 )
			    element = addElementWithData(
			      state, 3,
			      m_State_TypeID, sqlError, element);
			if ( inWarnings )
				se = ((SQLWarning)se).getNextWarning();
			else
				se = se.getNextException();
		}
		else
			se = null;
	}
  }
  private void buildBasicStructure( Exception e )
  {
    m_rootID = addElement(0, m_Document_TypeID, DTM.NULL, DTM.NULL);
    m_extErrorID = addElement(1, m_ErrorExt_TypeID, m_rootID, DTM.NULL);
    m_MainMessageID = addElementWithData
      (e != null ? e.getLocalizedMessage() : "SQLWarning", 2, m_Message_TypeID, m_extErrorID, DTM.NULL);
  }
  protected void createExpandedNameTable( )
  {
    super.createExpandedNameTable();
    m_ErrorExt_TypeID =
      m_expandedNameTable.getExpandedTypeID(S_NAMESPACE, S_EXT_ERROR, DTM.ELEMENT_NODE);
    m_SQLError_TypeID =
      m_expandedNameTable.getExpandedTypeID(S_NAMESPACE, S_SQL_ERROR, DTM.ELEMENT_NODE);
    m_Message_TypeID =
      m_expandedNameTable.getExpandedTypeID(S_NAMESPACE, S_MESSAGE, DTM.ELEMENT_NODE);
    m_Code_TypeID =
      m_expandedNameTable.getExpandedTypeID(S_NAMESPACE, S_CODE, DTM.ELEMENT_NODE);
    m_State_TypeID =
      m_expandedNameTable.getExpandedTypeID(S_NAMESPACE, S_STATE, DTM.ELEMENT_NODE);
    m_SQLWarning_TypeID =
      m_expandedNameTable.getExpandedTypeID(S_NAMESPACE, S_SQL_WARNING, DTM.ELEMENT_NODE);
  }
}

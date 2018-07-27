package org.apache.xalan.templates;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import javax.xml.transform.TransformerException;
import org.apache.xalan.res.XSLTErrorResources;
import org.apache.xml.dtm.DTM;
import org.apache.xml.utils.QName;
import org.apache.xpath.Expression;
import org.apache.xpath.XPath;
import org.apache.xpath.XPathContext;
import org.apache.xpath.compiler.PsuedoNames;
import org.apache.xpath.patterns.NodeTest;
import org.apache.xpath.patterns.StepPattern;
import org.apache.xpath.patterns.UnionPattern;
public class TemplateList implements java.io.Serializable
{
    static final long serialVersionUID = 5803675288911728791L;
  public TemplateList()
  {
    super();
  }
  public void setTemplate(ElemTemplate template)
  {
    XPath matchXPath = template.getMatch();
    if (null == template.getName() && null == matchXPath)
    {  
      template.error(XSLTErrorResources.ER_NEED_NAME_OR_MATCH_ATTRIB,
          new Object[]{ "xsl:template" });
    }
    if (null != template.getName())
    {
      ElemTemplate existingTemplate = (ElemTemplate) m_namedTemplates.get(template.getName());
      if (null == existingTemplate)
      {
        m_namedTemplates.put(template.getName(), template);
      }
      else
      {
        int existingPrecedence =
                        existingTemplate.getStylesheetComposed().getImportCountComposed();
        int newPrecedence = template.getStylesheetComposed().getImportCountComposed();
        if (newPrecedence > existingPrecedence)
        {
          m_namedTemplates.put(template.getName(), template);
        }
        else if (newPrecedence == existingPrecedence)
          template.error(XSLTErrorResources.ER_DUPLICATE_NAMED_TEMPLATE,
                       new Object[]{ template.getName() });
      }
    }
    if (null != matchXPath)
    {
      Expression matchExpr = matchXPath.getExpression();
      if (matchExpr instanceof StepPattern)
      {
        insertPatternInTable((StepPattern) matchExpr, template);
      }
      else if (matchExpr instanceof UnionPattern)
      {
        UnionPattern upat = (UnionPattern) matchExpr;
        StepPattern[] pats = upat.getPatterns();
        int n = pats.length;
        for (int i = 0; i < n; i++)
        {
          insertPatternInTable(pats[i], template);
        }
      }
      else
      {
      }
    }
  }
  final static boolean DEBUG = false;
  void dumpAssociationTables()
  {
    Enumeration associations = m_patternTable.elements();
    while (associations.hasMoreElements())
    {
      TemplateSubPatternAssociation head =
        (TemplateSubPatternAssociation) associations.nextElement();
      while (null != head)
      {
        System.out.print("(" + head.getTargetString() + ", "
                         + head.getPattern() + ")");
        head = head.getNext();
      }
      System.out.println("\n.....");
    }
    TemplateSubPatternAssociation head = m_wildCardPatterns;
    System.out.print("wild card list: ");
    while (null != head)
    {
      System.out.print("(" + head.getTargetString() + ", "
                       + head.getPattern() + ")");
      head = head.getNext();
    }
    System.out.println("\n.....");
  }
  public void compose(StylesheetRoot sroot)
  {
    if (DEBUG)
    {
      System.out.println("Before wildcard insert...");
      dumpAssociationTables();
    }
    if (null != m_wildCardPatterns)
    {
      Enumeration associations = m_patternTable.elements();
      while (associations.hasMoreElements())
      {
        TemplateSubPatternAssociation head =
          (TemplateSubPatternAssociation) associations.nextElement();
        TemplateSubPatternAssociation wild = m_wildCardPatterns;
        while (null != wild)
        {
          try
          {
            head = insertAssociationIntoList(
              head, (TemplateSubPatternAssociation) wild.clone(), true);
          }
          catch (CloneNotSupportedException cnse){}
          wild = wild.getNext();
        }
      }
    }
    if (DEBUG)
    {
      System.out.println("After wildcard insert...");
      dumpAssociationTables();
    }
  }
  private TemplateSubPatternAssociation
              insertAssociationIntoList(TemplateSubPatternAssociation head,
                                         TemplateSubPatternAssociation item,
                                         boolean isWildCardInsert)
  {
    double priority = getPriorityOrScore(item);
    double workPriority;
    int importLevel = item.getImportLevel();
    int docOrder = item.getDocOrderPos();
    TemplateSubPatternAssociation insertPoint = head;
    TemplateSubPatternAssociation next;
    boolean insertBefore;         
    while (true)
    {
      next = insertPoint.getNext();
      if (null == next)
        break;
      else
      {
        workPriority = getPriorityOrScore(next);
        if (importLevel > next.getImportLevel())
          break;
        else if (importLevel < next.getImportLevel())
          insertPoint = next;
        else if (priority > workPriority)               
          break;
        else if (priority < workPriority)
          insertPoint = next;
        else if (docOrder >= next.getDocOrderPos())     
          break;
        else
          insertPoint = next;
      }
    }
    if ( (null == next) || (insertPoint == head) )      
    {
      workPriority = getPriorityOrScore(insertPoint);
      if (importLevel > insertPoint.getImportLevel())
        insertBefore = true;
      else if (importLevel < insertPoint.getImportLevel())
        insertBefore = false;
      else if (priority > workPriority)
        insertBefore = true;
      else if (priority < workPriority)
        insertBefore = false;
      else if (docOrder >= insertPoint.getDocOrderPos())
        insertBefore = true;
      else
        insertBefore = false;
    }
    else
      insertBefore = false;
    if (isWildCardInsert)
    {
      if (insertBefore)
      {
        item.setNext(insertPoint);
        String key = insertPoint.getTargetString();
        item.setTargetString(key);
        putHead(key, item);
        return item;
      }
      else
      {
        item.setNext(next);
        insertPoint.setNext(item);
        return head;
      }
    }
    else
    {
      if (insertBefore)
      {
        item.setNext(insertPoint);
        if (insertPoint.isWild() || item.isWild())
          m_wildCardPatterns = item;
        else
          putHead(item.getTargetString(), item);
        return item;
      }
      else
      {
        item.setNext(next);
        insertPoint.setNext(item);
        return head;
      }
    }
  }
  private void insertPatternInTable(StepPattern pattern, ElemTemplate template)
  {
    String target = pattern.getTargetString();
    if (null != target)
    {
      String pstring = template.getMatch().getPatternString();
      TemplateSubPatternAssociation association =
        new TemplateSubPatternAssociation(template, pattern, pstring);
      boolean isWildCard = association.isWild();
      TemplateSubPatternAssociation head = isWildCard
                                           ? m_wildCardPatterns
                                           : getHead(target);
      if (null == head)
      {
        if (isWildCard)
          m_wildCardPatterns = association;
        else
          putHead(target, association);
      }
      else
      {
        insertAssociationIntoList(head, association, false);
      }
    }
  }
  private double getPriorityOrScore(TemplateSubPatternAssociation matchPat)
  {
    double priority = matchPat.getTemplate().getPriority();
    if (priority == XPath.MATCH_SCORE_NONE)
    {
      Expression ex = matchPat.getStepPattern();
      if (ex instanceof NodeTest)
      {
        return ((NodeTest) ex).getDefaultScore();
      }
    }
    return priority;
  }
  public ElemTemplate getTemplate(QName qname)
  {
    return (ElemTemplate) m_namedTemplates.get(qname);
  }
  public TemplateSubPatternAssociation getHead(XPathContext xctxt, 
                                               int targetNode, DTM dtm)
  {
    short targetNodeType = dtm.getNodeType(targetNode);
    TemplateSubPatternAssociation head;
    switch (targetNodeType)
    {
    case DTM.ELEMENT_NODE :
    case DTM.ATTRIBUTE_NODE :
      head = (TemplateSubPatternAssociation) m_patternTable.get(
        dtm.getLocalName(targetNode));
      break;
    case DTM.TEXT_NODE :
    case DTM.CDATA_SECTION_NODE :
      head = m_textPatterns;
      break;
    case DTM.ENTITY_REFERENCE_NODE :
    case DTM.ENTITY_NODE :
      head = (TemplateSubPatternAssociation) m_patternTable.get(
        dtm.getNodeName(targetNode)); 
      break;
    case DTM.PROCESSING_INSTRUCTION_NODE :
      head = (TemplateSubPatternAssociation) m_patternTable.get(
        dtm.getLocalName(targetNode));
      break;
    case DTM.COMMENT_NODE :
      head = m_commentPatterns;
      break;
    case DTM.DOCUMENT_NODE :
    case DTM.DOCUMENT_FRAGMENT_NODE :
      head = m_docPatterns;
      break;
    case DTM.NOTATION_NODE :
    default :
      head = (TemplateSubPatternAssociation) m_patternTable.get(
        dtm.getNodeName(targetNode)); 
    }
    return (null == head) ? m_wildCardPatterns : head;
  }
  public ElemTemplate getTemplateFast(XPathContext xctxt,
                                int targetNode,
                                int expTypeID,
                                QName mode,
                                int maxImportLevel,
                                boolean quietConflictWarnings,
                                DTM dtm)
            throws TransformerException
  {
    TemplateSubPatternAssociation head;
    switch (dtm.getNodeType(targetNode))
    {
    case DTM.ELEMENT_NODE :
    case DTM.ATTRIBUTE_NODE :
      head = (TemplateSubPatternAssociation) m_patternTable.get(
        dtm.getLocalNameFromExpandedNameID(expTypeID));
      break;
    case DTM.TEXT_NODE :
    case DTM.CDATA_SECTION_NODE :
      head = m_textPatterns;
      break;
    case DTM.ENTITY_REFERENCE_NODE :
    case DTM.ENTITY_NODE :
      head = (TemplateSubPatternAssociation) m_patternTable.get(
        dtm.getNodeName(targetNode)); 
      break;
    case DTM.PROCESSING_INSTRUCTION_NODE :
      head = (TemplateSubPatternAssociation) m_patternTable.get(
        dtm.getLocalName(targetNode));
      break;
    case DTM.COMMENT_NODE :
      head = m_commentPatterns;
      break;
    case DTM.DOCUMENT_NODE :
    case DTM.DOCUMENT_FRAGMENT_NODE :
      head = m_docPatterns;
      break;
    case DTM.NOTATION_NODE :
    default :
      head = (TemplateSubPatternAssociation) m_patternTable.get(
        dtm.getNodeName(targetNode)); 
    }
    if(null == head)
    {
      head = m_wildCardPatterns;
      if(null == head)
        return null;
    }                                              
    xctxt.pushNamespaceContextNull();
    try
    {
      do
      {
        if ( (maxImportLevel > -1) && (head.getImportLevel() > maxImportLevel) )
        {
          continue;
        }
        ElemTemplate template = head.getTemplate();        
        xctxt.setNamespaceContext(template);
        if ((head.m_stepPattern.execute(xctxt, targetNode, dtm, expTypeID) != NodeTest.SCORE_NONE)
                && head.matchMode(mode))
        {
          if (quietConflictWarnings)
            checkConflicts(head, xctxt, targetNode, mode);
          return template;
        }
      }
      while (null != (head = head.getNext()));
    }
    finally
    {
      xctxt.popNamespaceContext();
    }
    return null;
  }  
  public ElemTemplate getTemplate(XPathContext xctxt,
                                int targetNode,
                                QName mode,
                                boolean quietConflictWarnings,
                                DTM dtm)
            throws TransformerException
  {
    TemplateSubPatternAssociation head = getHead(xctxt, targetNode, dtm);
    if (null != head)
    {
      xctxt.pushNamespaceContextNull();
      xctxt.pushCurrentNodeAndExpression(targetNode, targetNode);
      try
      {
        do
        {
          ElemTemplate template = head.getTemplate();        
          xctxt.setNamespaceContext(template);
          if ((head.m_stepPattern.execute(xctxt, targetNode) != NodeTest.SCORE_NONE)
                  && head.matchMode(mode))
          {
            if (quietConflictWarnings)
              checkConflicts(head, xctxt, targetNode, mode);
            return template;
          }
        }
        while (null != (head = head.getNext()));
      }
      finally
      {
        xctxt.popCurrentNodeAndExpression();
        xctxt.popNamespaceContext();
      }
    }
    return null;
  }  
  public ElemTemplate getTemplate(XPathContext xctxt,
                                int targetNode,
                                QName mode,
                                int maxImportLevel, int endImportLevel,
                                boolean quietConflictWarnings,
                                DTM dtm)
            throws TransformerException
  {
    TemplateSubPatternAssociation head = getHead(xctxt, targetNode, dtm);
    if (null != head)
    {
      xctxt.pushNamespaceContextNull();
      xctxt.pushCurrentNodeAndExpression(targetNode, targetNode);
      try
      {
        do
        {
          if ( (maxImportLevel > -1) && (head.getImportLevel() > maxImportLevel))
          {
            continue;
          }
          if (head.getImportLevel()<= maxImportLevel - endImportLevel)
            return null;
          ElemTemplate template = head.getTemplate();        
          xctxt.setNamespaceContext(template);
          if ((head.m_stepPattern.execute(xctxt, targetNode) != NodeTest.SCORE_NONE)
                  && head.matchMode(mode))
          {
            if (quietConflictWarnings)
              checkConflicts(head, xctxt, targetNode, mode);
            return template;
          }
        }
        while (null != (head = head.getNext()));
      }
      finally
      {
        xctxt.popCurrentNodeAndExpression();
        xctxt.popNamespaceContext();
      }
    }
    return null;
  }  
  public TemplateWalker getWalker()
  {
    return new TemplateWalker();
  }
  private void checkConflicts(TemplateSubPatternAssociation head,
                              XPathContext xctxt, int targetNode, QName mode)
  {
  }
  private void addObjectIfNotFound(Object obj, Vector v)
  {
    int n = v.size();
    boolean addIt = true;
    for (int i = 0; i < n; i++)
    {
      if (v.elementAt(i) == obj)
      {
        addIt = false;
        break;
      }
    }
    if (addIt)
    {
      v.addElement(obj);
    }
  }
  private Hashtable m_namedTemplates = new Hashtable(89);
  private Hashtable m_patternTable = new Hashtable(89);
  private TemplateSubPatternAssociation m_wildCardPatterns = null;
  private TemplateSubPatternAssociation m_textPatterns = null;
  private TemplateSubPatternAssociation m_docPatterns = null;
  private TemplateSubPatternAssociation m_commentPatterns = null;
  private Hashtable getNamedTemplates()
  {
    return m_namedTemplates;
  }
  private void setNamedTemplates(Hashtable v)
  {
    m_namedTemplates = v;
  }
  private TemplateSubPatternAssociation getHead(String key)
  {
    return (TemplateSubPatternAssociation) m_patternTable.get(key);
  }
  private void putHead(String key, TemplateSubPatternAssociation assoc)
  {
    if (key.equals(PsuedoNames.PSEUDONAME_TEXT))
      m_textPatterns = assoc;
    else if (key.equals(PsuedoNames.PSEUDONAME_ROOT))
      m_docPatterns = assoc;
    else if (key.equals(PsuedoNames.PSEUDONAME_COMMENT))
      m_commentPatterns = assoc;
    m_patternTable.put(key, assoc);
  }
  public class TemplateWalker
  {
    private Enumeration hashIterator;
    private boolean inPatterns;
    private TemplateSubPatternAssociation curPattern;
    private Hashtable m_compilerCache = new Hashtable();
    private TemplateWalker()
    {
      hashIterator = m_patternTable.elements();
      inPatterns = true;
      curPattern = null;
    }
    public ElemTemplate next()
    {
      ElemTemplate retValue = null;
      ElemTemplate ct;
      while (true)
      {
        if (inPatterns)
        {
          if (null != curPattern)
            curPattern = curPattern.getNext();
          if (null != curPattern)
            retValue = curPattern.getTemplate();
          else
          {
            if (hashIterator.hasMoreElements())
            {
              curPattern = (TemplateSubPatternAssociation) hashIterator.nextElement();
              retValue =  curPattern.getTemplate();
            }
            else
            {
              inPatterns = false;
              hashIterator = m_namedTemplates.elements();
            }
          }
        }
        if (!inPatterns)
        {
          if (hashIterator.hasMoreElements())
            retValue = (ElemTemplate) hashIterator.nextElement();
          else
            return null;
        }
        ct = (ElemTemplate) m_compilerCache.get(new Integer(retValue.getUid()));
        if (null == ct)
        {
          m_compilerCache.put(new Integer(retValue.getUid()), retValue);
          return retValue;
        }
      }
    }
  }
}

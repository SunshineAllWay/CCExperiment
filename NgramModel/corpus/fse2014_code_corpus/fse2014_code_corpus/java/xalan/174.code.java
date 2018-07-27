package org.apache.xalan.templates;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.NoSuchElementException;
import javax.xml.transform.TransformerException;
import org.apache.xalan.res.XSLTErrorResources;
import org.apache.xalan.transformer.CountersTable;
import org.apache.xalan.transformer.DecimalToRoman;
import org.apache.xalan.transformer.TransformerImpl;
import org.apache.xml.dtm.DTM;
import org.apache.xml.utils.FastStringBuffer;
import org.apache.xml.utils.NodeVector;
import org.apache.xml.utils.PrefixResolver;
import org.apache.xml.utils.StringBufferPool;
import org.apache.xml.utils.res.XResourceBundle;
import org.apache.xml.utils.res.CharArrayWrapper;
import org.apache.xml.utils.res.IntArrayWrapper;
import org.apache.xml.utils.res.LongArrayWrapper;
import org.apache.xml.utils.res.StringArrayWrapper;
import org.apache.xpath.NodeSetDTM;
import org.apache.xpath.XPath;
import org.apache.xpath.XPathContext;
import org.apache.xpath.objects.XObject;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;
public class ElemNumber extends ElemTemplateElement 
{
    static final long serialVersionUID = 8118472298274407610L;
    private CharArrayWrapper m_alphaCountTable = null;
    private class MyPrefixResolver implements PrefixResolver {
        DTM dtm;
        int handle;
        boolean handleNullPrefix;
		public MyPrefixResolver(Node xpathExpressionContext, DTM dtm, int handle, boolean handleNullPrefix) {
            this.dtm = dtm;
            this.handle = handle;
            this.handleNullPrefix = handleNullPrefix;
		}
		public String getNamespaceForPrefix(String prefix) {
            return dtm.getNamespaceURI(handle);
		}
        public String getNamespaceForPrefix(String prefix, Node context) {
            return getNamespaceForPrefix(prefix);
        }
		public String getBaseIdentifier() {
			return ElemNumber.this.getBaseIdentifier();
		}
		public boolean handlesNullPrefixes() {
			return handleNullPrefix;
		}
}
  private XPath m_countMatchPattern = null;
  public void setCount(XPath v)
  {
    m_countMatchPattern = v;
  }
  public XPath getCount()
  {
    return m_countMatchPattern;
  }
  private XPath m_fromMatchPattern = null;
  public void setFrom(XPath v)
  {
    m_fromMatchPattern = v;
  }
  public XPath getFrom()
  {
    return m_fromMatchPattern;
  }
  private int m_level = Constants.NUMBERLEVEL_SINGLE;
  public void setLevel(int v)
  {
    m_level = v;
  }
  public int getLevel()
  {
    return m_level;
  }
  private XPath m_valueExpr = null;
  public void setValue(XPath v)
  {
    m_valueExpr = v;
  }
  public XPath getValue()
  {
    return m_valueExpr;
  }
  private AVT m_format_avt = null;
  public void setFormat(AVT v)
  {
    m_format_avt = v;
  }
  public AVT getFormat()
  {
    return m_format_avt;
  }
  private AVT m_lang_avt = null;
  public void setLang(AVT v)
  {
    m_lang_avt = v;
  }
  public AVT getLang()
  {
    return m_lang_avt;
  }
  private AVT m_lettervalue_avt = null;
  public void setLetterValue(AVT v)
  {
    m_lettervalue_avt = v;
  }
  public AVT getLetterValue()
  {
    return m_lettervalue_avt;
  }
  private AVT m_groupingSeparator_avt = null;
  public void setGroupingSeparator(AVT v)
  {
    m_groupingSeparator_avt = v;
  }
  public AVT getGroupingSeparator()
  {
    return m_groupingSeparator_avt;
  }
  private AVT m_groupingSize_avt = null;
  public void setGroupingSize(AVT v)
  {
    m_groupingSize_avt = v;
  }
  public AVT getGroupingSize()
  {
    return m_groupingSize_avt;
  }
  private final static DecimalToRoman m_romanConvertTable[] = {
    new DecimalToRoman(1000, "M", 900, "CM"),
    new DecimalToRoman(500, "D", 400, "CD"),
    new DecimalToRoman(100L, "C", 90L, "XC"),
    new DecimalToRoman(50L, "L", 40L, "XL"),
    new DecimalToRoman(10L, "X", 9L, "IX"),
    new DecimalToRoman(5L, "V", 4L, "IV"),
    new DecimalToRoman(1L, "I", 1L, "I") };
  public void compose(StylesheetRoot sroot) throws TransformerException
  {
    super.compose(sroot);
    StylesheetRoot.ComposeState cstate = sroot.getComposeState();
    java.util.Vector vnames = cstate.getVariableNames();
    if(null != m_countMatchPattern)
      m_countMatchPattern.fixupVariables(vnames, cstate.getGlobalsSize());
    if(null != m_format_avt)
      m_format_avt.fixupVariables(vnames, cstate.getGlobalsSize());
    if(null != m_fromMatchPattern)
      m_fromMatchPattern.fixupVariables(vnames, cstate.getGlobalsSize());
    if(null != m_groupingSeparator_avt)
      m_groupingSeparator_avt.fixupVariables(vnames, cstate.getGlobalsSize());
    if(null != m_groupingSize_avt)
      m_groupingSize_avt.fixupVariables(vnames, cstate.getGlobalsSize());
    if(null != m_lang_avt)
      m_lang_avt.fixupVariables(vnames, cstate.getGlobalsSize());
    if(null != m_lettervalue_avt)
      m_lettervalue_avt.fixupVariables(vnames, cstate.getGlobalsSize());
    if(null != m_valueExpr)
      m_valueExpr.fixupVariables(vnames, cstate.getGlobalsSize());
  }
  public int getXSLToken()
  {
    return Constants.ELEMNAME_NUMBER;
  }
  public String getNodeName()
  {
    return Constants.ELEMNAME_NUMBER_STRING;
  }
  public void execute(
          TransformerImpl transformer)
            throws TransformerException
  {
     if (transformer.getDebug())
      transformer.getTraceManager().fireTraceEvent(this);
    int sourceNode = transformer.getXPathContext().getCurrentNode();
    String countString = getCountString(transformer, sourceNode);
    try
    {
      transformer.getResultTreeHandler().characters(countString.toCharArray(),
                                                    0, countString.length());
    }
    catch(SAXException se)
    {
      throw new TransformerException(se);
    }
    finally
    {
      if (transformer.getDebug())
	    transformer.getTraceManager().fireTraceEndEvent(this); 
    }
  }
  public ElemTemplateElement appendChild(ElemTemplateElement newChild)
  {
    error(XSLTErrorResources.ER_CANNOT_ADD,
          new Object[]{ newChild.getNodeName(),
                        this.getNodeName() });  
    return null;
  }
  int findAncestor(
          XPathContext xctxt, XPath fromMatchPattern, XPath countMatchPattern, 
          int context, ElemNumber namespaceContext)
            throws javax.xml.transform.TransformerException
  {
    DTM dtm = xctxt.getDTM(context);
    while (DTM.NULL != context)
    {
      if (null != fromMatchPattern)
      {
        if (fromMatchPattern.getMatchScore(xctxt, context)
                != XPath.MATCH_SCORE_NONE)
        {
          break;
        }
      }
      if (null != countMatchPattern)
      {
        if (countMatchPattern.getMatchScore(xctxt, context)
                != XPath.MATCH_SCORE_NONE)
        {
          break;
        }
      }
      context = dtm.getParent(context);
    }
    return context;
  }
  private int findPrecedingOrAncestorOrSelf(
          XPathContext xctxt, XPath fromMatchPattern, XPath countMatchPattern, 
          int context, ElemNumber namespaceContext)
            throws javax.xml.transform.TransformerException
  {
    DTM dtm = xctxt.getDTM(context);
    while (DTM.NULL != context)
    {
      if (null != fromMatchPattern)
      {
        if (fromMatchPattern.getMatchScore(xctxt, context)
                != XPath.MATCH_SCORE_NONE)
        {
          context = DTM.NULL;
          break;
        }
      }
      if (null != countMatchPattern)
      {
        if (countMatchPattern.getMatchScore(xctxt, context)
                != XPath.MATCH_SCORE_NONE)
        {
          break;
        }
      }
      int prevSibling = dtm.getPreviousSibling(context);
      if (DTM.NULL == prevSibling)
      {
        context = dtm.getParent(context);
      }
      else
      {
        context = dtm.getLastChild(prevSibling);
        if (context == DTM.NULL)
          context = prevSibling;
      }
    }
    return context;
  }
  XPath getCountMatchPattern(XPathContext support, int contextNode)
          throws javax.xml.transform.TransformerException
  {
    XPath countMatchPattern = m_countMatchPattern;
    DTM dtm = support.getDTM(contextNode);
    if (null == countMatchPattern)
    {
      switch (dtm.getNodeType(contextNode))
      {
      case DTM.ELEMENT_NODE :
        MyPrefixResolver resolver;
        if (dtm.getNamespaceURI(contextNode) == null) {
             resolver =  new MyPrefixResolver(dtm.getNode(contextNode), dtm,contextNode, false);
        } else {
            resolver = new MyPrefixResolver(dtm.getNode(contextNode), dtm,contextNode, true);
        }
        countMatchPattern = new XPath(dtm.getNodeName(contextNode), this, resolver,
                                      XPath.MATCH, support.getErrorListener());
        break;
      case DTM.ATTRIBUTE_NODE :
        countMatchPattern = new XPath("@" + dtm.getNodeName(contextNode), this,
                                      this, XPath.MATCH, support.getErrorListener());
        break;
      case DTM.CDATA_SECTION_NODE :
      case DTM.TEXT_NODE :
        countMatchPattern = new XPath("text()", this, this, XPath.MATCH, support.getErrorListener());
        break;
      case DTM.COMMENT_NODE :
        countMatchPattern = new XPath("comment()", this, this, XPath.MATCH, support.getErrorListener());
        break;
      case DTM.DOCUMENT_NODE :
        countMatchPattern = new XPath("/", this, this, XPath.MATCH, support.getErrorListener());
        break;
      case DTM.PROCESSING_INSTRUCTION_NODE :
        countMatchPattern = new XPath("pi(" + dtm.getNodeName(contextNode)
                                      + ")", this, this, XPath.MATCH, support.getErrorListener());
        break;
      default :
        countMatchPattern = null;
      }
    }
    return countMatchPattern;
  }
  String getCountString(TransformerImpl transformer, int sourceNode)
          throws TransformerException
  {
    long[] list = null;
    XPathContext xctxt = transformer.getXPathContext();
    CountersTable ctable = transformer.getCountersTable();
    if (null != m_valueExpr)
    {
      XObject countObj = m_valueExpr.execute(xctxt, sourceNode, this);
      double d_count = java.lang.Math.floor(countObj.num()+ 0.5);
      if (Double.isNaN(d_count)) return "NaN";
      else if (d_count < 0 && Double.isInfinite(d_count)) return "-Infinity";
      else if (Double.isInfinite(d_count)) return "Infinity";
      else if (d_count == 0) return "0";
      else{
              long count = (long)d_count;
              list = new long[1];
              list[0] = count;              
      }
    }
    else
    {
      if (Constants.NUMBERLEVEL_ANY == m_level)
      {
        list = new long[1];
        list[0] = ctable.countNode(xctxt, this, sourceNode);
      }
      else
      {
        NodeVector ancestors =
          getMatchingAncestors(xctxt, sourceNode,
                               Constants.NUMBERLEVEL_SINGLE == m_level);
        int lastIndex = ancestors.size() - 1;
        if (lastIndex >= 0)
        {
          list = new long[lastIndex + 1];
          for (int i = lastIndex; i >= 0; i--)
          {
            int target = ancestors.elementAt(i);
            list[lastIndex - i] = ctable.countNode(xctxt, this, target);
          }
        }
      }
    }
    return (null != list)
           ? formatNumberList(transformer, list, sourceNode) : "";
  }
  public int getPreviousNode(XPathContext xctxt, int pos)
          throws TransformerException
  {
    XPath countMatchPattern = getCountMatchPattern(xctxt, pos);
    DTM dtm = xctxt.getDTM(pos);
    if (Constants.NUMBERLEVEL_ANY == m_level)
    {
      XPath fromMatchPattern = m_fromMatchPattern;
      while (DTM.NULL != pos)
      {
        int next = dtm.getPreviousSibling(pos);
        if (DTM.NULL == next)
        {
          next = dtm.getParent(pos);
          if ((DTM.NULL != next) && ((((null != fromMatchPattern) && (fromMatchPattern.getMatchScore(
                  xctxt, next) != XPath.MATCH_SCORE_NONE))) 
              || (dtm.getNodeType(next) == DTM.DOCUMENT_NODE)))
          {
            pos = DTM.NULL;  
            break;  
          }
        }
        else
        {
          int child = next;
          while (DTM.NULL != child)
          {
            child = dtm.getLastChild(next);
            if (DTM.NULL != child)
              next = child;
          }
        }
        pos = next;
        if ((DTM.NULL != pos)
                && ((null == countMatchPattern)
                    || (countMatchPattern.getMatchScore(xctxt, pos)
                        != XPath.MATCH_SCORE_NONE)))
        {
          break;
        }
      }
    }
    else  
    {
      while (DTM.NULL != pos)
      {
        pos = dtm.getPreviousSibling(pos);
        if ((DTM.NULL != pos)
                && ((null == countMatchPattern)
                    || (countMatchPattern.getMatchScore(xctxt, pos)
                        != XPath.MATCH_SCORE_NONE)))
        {
          break;
        }
      }
    }
    return pos;
  }
  public int getTargetNode(XPathContext xctxt, int sourceNode)
          throws TransformerException
  {
    int target = DTM.NULL;
    XPath countMatchPattern = getCountMatchPattern(xctxt, sourceNode);
    if (Constants.NUMBERLEVEL_ANY == m_level)
    {
      target = findPrecedingOrAncestorOrSelf(xctxt, m_fromMatchPattern,
                                             countMatchPattern, sourceNode,
                                             this);
    }
    else
    {
      target = findAncestor(xctxt, m_fromMatchPattern, countMatchPattern,
                            sourceNode, this);
    }
    return target;
  }
  NodeVector getMatchingAncestors(
          XPathContext xctxt, int node, boolean stopAtFirstFound)
            throws javax.xml.transform.TransformerException
  {
    NodeSetDTM ancestors = new NodeSetDTM(xctxt.getDTMManager());
    XPath countMatchPattern = getCountMatchPattern(xctxt, node);
    DTM dtm = xctxt.getDTM(node);
    while (DTM.NULL != node)
    {
      if ((null != m_fromMatchPattern)
              && (m_fromMatchPattern.getMatchScore(xctxt, node)
                  != XPath.MATCH_SCORE_NONE))
      {
        if (!stopAtFirstFound)
          break;
      }
      if (null == countMatchPattern)
        System.out.println(
          "Programmers error! countMatchPattern should never be null!");
      if (countMatchPattern.getMatchScore(xctxt, node)
              != XPath.MATCH_SCORE_NONE)
      {
        ancestors.addElement(node);
        if (stopAtFirstFound)
          break;
      }
      node = dtm.getParent(node);
    }
    return ancestors;
  }  
  Locale getLocale(TransformerImpl transformer, int contextNode)
          throws TransformerException
  {
    Locale locale = null;
    if (null != m_lang_avt)
    {
      XPathContext xctxt = transformer.getXPathContext();
      String langValue = m_lang_avt.evaluate(xctxt, contextNode, this);
      if (null != langValue)
      {
        locale = new Locale(langValue.toUpperCase(), "");
        if (null == locale)
        {
          transformer.getMsgMgr().warn(this, null, xctxt.getDTM(contextNode).getNode(contextNode),
                                       XSLTErrorResources.WG_LOCALE_NOT_FOUND,
                                       new Object[]{ langValue });  
          locale = Locale.getDefault();
        }
      }
    }
    else
    {
      locale = Locale.getDefault();
    }
    return locale;
  }
  private DecimalFormat getNumberFormatter(
          TransformerImpl transformer, int contextNode) throws TransformerException
  {
    Locale locale = (Locale)getLocale(transformer, contextNode).clone();
    DecimalFormat formatter = null;
    String digitGroupSepValue =
      (null != m_groupingSeparator_avt)
      ? m_groupingSeparator_avt.evaluate(
      transformer.getXPathContext(), contextNode, this) : null;
    if ((digitGroupSepValue != null) && (!m_groupingSeparator_avt.isSimple()) &&
        (digitGroupSepValue.length() != 1))
    {
            transformer.getMsgMgr().warn(
               this, XSLTErrorResources.WG_ILLEGAL_ATTRIBUTE_VALUE,
               new Object[]{ Constants.ATTRNAME_NAME, m_groupingSeparator_avt.getName()});   
    }                  
    String nDigitsPerGroupValue =
      (null != m_groupingSize_avt)
      ? m_groupingSize_avt.evaluate(
      transformer.getXPathContext(), contextNode, this) : null;
    if ((null != digitGroupSepValue) && (null != nDigitsPerGroupValue) &&
        (digitGroupSepValue.length() > 0))
    {
      try
      {
        formatter = (DecimalFormat) NumberFormat.getNumberInstance(locale);
        formatter.setGroupingSize(
          Integer.valueOf(nDigitsPerGroupValue).intValue());
        DecimalFormatSymbols symbols = formatter.getDecimalFormatSymbols();
        symbols.setGroupingSeparator(digitGroupSepValue.charAt(0));
        formatter.setDecimalFormatSymbols(symbols);
        formatter.setGroupingUsed(true);
      }
      catch (NumberFormatException ex)
      {
        formatter.setGroupingUsed(false);
      }
    }
    return formatter;
  }
  String formatNumberList(
          TransformerImpl transformer, long[] list, int contextNode)
            throws TransformerException
  {
    String numStr;
    FastStringBuffer formattedNumber = StringBufferPool.get();
    try
    {
      int nNumbers = list.length, numberWidth = 1;
      char numberType = '1';
      String formatToken, lastSepString = null, formatTokenString = null;
      String lastSep = ".";
      boolean isFirstToken = true;  
      String formatValue =
        (null != m_format_avt)
        ? m_format_avt.evaluate(
        transformer.getXPathContext(), contextNode, this) : null;
      if (null == formatValue)
        formatValue = "1";
      NumberFormatStringTokenizer formatTokenizer =
        new NumberFormatStringTokenizer(formatValue);
      for (int i = 0; i < nNumbers; i++)
      {
        if (formatTokenizer.hasMoreTokens())
        {
          formatToken = formatTokenizer.nextToken();
          if (Character.isLetterOrDigit(
                  formatToken.charAt(formatToken.length() - 1)))
          {
            numberWidth = formatToken.length();
            numberType = formatToken.charAt(numberWidth - 1);
          }
          else if (formatTokenizer.isLetterOrDigitAhead())
          {
            formatTokenString = formatToken;
            while (formatTokenizer.nextIsSep())
            {
              formatToken = formatTokenizer.nextToken();
              formatTokenString += formatToken;
            }
            if (!isFirstToken)
              lastSep = formatTokenString;
            formatToken = formatTokenizer.nextToken();
            numberWidth = formatToken.length();
            numberType = formatToken.charAt(numberWidth - 1);
          }
          else  
          {
            lastSepString = formatToken;
            while (formatTokenizer.hasMoreTokens())
            {
              formatToken = formatTokenizer.nextToken();
              lastSepString += formatToken;
            }
          }  
        }  
        if (null != formatTokenString && isFirstToken)
        {
          formattedNumber.append(formatTokenString);
        }
        else if (null != lastSep &&!isFirstToken)
          formattedNumber.append(lastSep);
        getFormattedNumber(transformer, contextNode, numberType, numberWidth,
                           list[i], formattedNumber);
        isFirstToken = false;  
      }  
      while (formatTokenizer.isLetterOrDigitAhead())
      {
        formatTokenizer.nextToken();
      }
      if (lastSepString != null)
        formattedNumber.append(lastSepString);
      while (formatTokenizer.hasMoreTokens())
      {
        formatToken = formatTokenizer.nextToken();
        formattedNumber.append(formatToken);
      }
      numStr = formattedNumber.toString();
    }
    finally
    {
      StringBufferPool.free(formattedNumber);
    }
    return numStr;
  }  
  private void getFormattedNumber(
          TransformerImpl transformer, int contextNode, 
          char numberType, int numberWidth, long listElement, 
          FastStringBuffer formattedNumber)
            throws javax.xml.transform.TransformerException
  {
    String letterVal =
      (m_lettervalue_avt != null)
      ? m_lettervalue_avt.evaluate(
      transformer.getXPathContext(), contextNode, this) : null;
    CharArrayWrapper alphaCountTable = null;
    XResourceBundle thisBundle = null;
    switch (numberType)
    {
    case 'A' :
        if (null == m_alphaCountTable){
                thisBundle =
                  (XResourceBundle) XResourceBundle.loadResourceBundle(
                    org.apache.xml.utils.res.XResourceBundle.LANG_BUNDLE_NAME, getLocale(transformer, contextNode));
                m_alphaCountTable = (CharArrayWrapper) thisBundle.getObject(org.apache.xml.utils.res.XResourceBundle.LANG_ALPHABET);                
        }
      int2alphaCount(listElement, m_alphaCountTable, formattedNumber);
      break;
    case 'a' :
        if (null == m_alphaCountTable){
                thisBundle =
                  (XResourceBundle) XResourceBundle.loadResourceBundle(
                    org.apache.xml.utils.res.XResourceBundle.LANG_BUNDLE_NAME, getLocale(transformer, contextNode));
                m_alphaCountTable = (CharArrayWrapper) thisBundle.getObject(org.apache.xml.utils.res.XResourceBundle.LANG_ALPHABET);                
        }
      FastStringBuffer stringBuf = StringBufferPool.get();
      try
      {
        int2alphaCount(listElement, m_alphaCountTable, stringBuf);
        formattedNumber.append(
          stringBuf.toString().toLowerCase(
            getLocale(transformer, contextNode)));
      }
      finally
      {
        StringBufferPool.free(stringBuf);
      }
      break;
    case 'I' :
      formattedNumber.append(long2roman(listElement, true));
      break;
    case 'i' :
      formattedNumber.append(
        long2roman(listElement, true).toLowerCase(
          getLocale(transformer, contextNode)));
      break;
    case 0x3042 :
    {
      thisBundle = (XResourceBundle) XResourceBundle.loadResourceBundle(
        org.apache.xml.utils.res.XResourceBundle.LANG_BUNDLE_NAME, new Locale("ja", "JP", "HA"));
      if (letterVal != null
              && letterVal.equals(Constants.ATTRVAL_TRADITIONAL))
        formattedNumber.append(tradAlphaCount(listElement, thisBundle));
      else  
        formattedNumber.append(
          int2singlealphaCount(
            listElement,
            (CharArrayWrapper) thisBundle.getObject(org.apache.xml.utils.res.XResourceBundle.LANG_ALPHABET)));
      break;
    }
    case 0x3044 :
    {
      thisBundle = (XResourceBundle) XResourceBundle.loadResourceBundle(
        org.apache.xml.utils.res.XResourceBundle.LANG_BUNDLE_NAME, new Locale("ja", "JP", "HI"));
      if ((letterVal != null)
              && letterVal.equals(Constants.ATTRVAL_TRADITIONAL))
        formattedNumber.append(tradAlphaCount(listElement, thisBundle));
      else  
        formattedNumber.append(
          int2singlealphaCount(
            listElement,
            (CharArrayWrapper) thisBundle.getObject(org.apache.xml.utils.res.XResourceBundle.LANG_ALPHABET)));
      break;
    }
    case 0x30A2 :
    {
      thisBundle = (XResourceBundle) XResourceBundle.loadResourceBundle(
        org.apache.xml.utils.res.XResourceBundle.LANG_BUNDLE_NAME, new Locale("ja", "JP", "A"));
      if (letterVal != null
              && letterVal.equals(Constants.ATTRVAL_TRADITIONAL))
        formattedNumber.append(tradAlphaCount(listElement, thisBundle));
      else  
        formattedNumber.append(
          int2singlealphaCount(
            listElement,
            (CharArrayWrapper) thisBundle.getObject(org.apache.xml.utils.res.XResourceBundle.LANG_ALPHABET)));
      break;
    }
    case 0x30A4 :
    {
      thisBundle = (XResourceBundle) XResourceBundle.loadResourceBundle(
        org.apache.xml.utils.res.XResourceBundle.LANG_BUNDLE_NAME, new Locale("ja", "JP", "I"));
      if (letterVal != null
              && letterVal.equals(Constants.ATTRVAL_TRADITIONAL))
        formattedNumber.append(tradAlphaCount(listElement, thisBundle));
      else  
        formattedNumber.append(
          int2singlealphaCount(
            listElement,
            (CharArrayWrapper) thisBundle.getObject(org.apache.xml.utils.res.XResourceBundle.LANG_ALPHABET)));
      break;
    }
    case 0x4E00 :
    {
      thisBundle = (XResourceBundle) XResourceBundle.loadResourceBundle(
        org.apache.xml.utils.res.XResourceBundle.LANG_BUNDLE_NAME, new Locale("zh", "CN"));
      if (letterVal != null
              && letterVal.equals(Constants.ATTRVAL_TRADITIONAL))
      {
        formattedNumber.append(tradAlphaCount(listElement, thisBundle));
      }
      else  
        int2alphaCount(listElement,
                       (CharArrayWrapper) thisBundle.getObject(org.apache.xml.utils.res.XResourceBundle.LANG_ALPHABET),
                       formattedNumber);
      break;
    }
    case 0x58F9 :
    {
      thisBundle = (XResourceBundle) XResourceBundle.loadResourceBundle(
        org.apache.xml.utils.res.XResourceBundle.LANG_BUNDLE_NAME, new Locale("zh", "TW"));
      if (letterVal != null
              && letterVal.equals(Constants.ATTRVAL_TRADITIONAL))
        formattedNumber.append(tradAlphaCount(listElement, thisBundle));
      else  
        int2alphaCount(listElement,
                       (CharArrayWrapper) thisBundle.getObject(org.apache.xml.utils.res.XResourceBundle.LANG_ALPHABET),
                       formattedNumber);
      break;
    }
    case 0x0E51 :
    {
      thisBundle = (XResourceBundle) XResourceBundle.loadResourceBundle(
        org.apache.xml.utils.res.XResourceBundle.LANG_BUNDLE_NAME, new Locale("th", ""));
      if (letterVal != null
              && letterVal.equals(Constants.ATTRVAL_TRADITIONAL))
        formattedNumber.append(tradAlphaCount(listElement, thisBundle));
      else  
        int2alphaCount(listElement,
                       (CharArrayWrapper) thisBundle.getObject(org.apache.xml.utils.res.XResourceBundle.LANG_ALPHABET),
                       formattedNumber);
      break;
    }
    case 0x05D0 :
    {
      thisBundle = (XResourceBundle) XResourceBundle.loadResourceBundle(
        org.apache.xml.utils.res.XResourceBundle.LANG_BUNDLE_NAME, new Locale("he", ""));
      if (letterVal != null
              && letterVal.equals(Constants.ATTRVAL_TRADITIONAL))
        formattedNumber.append(tradAlphaCount(listElement, thisBundle));
      else  
        int2alphaCount(listElement,
                       (CharArrayWrapper) thisBundle.getObject(org.apache.xml.utils.res.XResourceBundle.LANG_ALPHABET),
                       formattedNumber);
      break;
    }
    case 0x10D0 :
    {
      thisBundle = (XResourceBundle) XResourceBundle.loadResourceBundle(
        org.apache.xml.utils.res.XResourceBundle.LANG_BUNDLE_NAME, new Locale("ka", ""));
      if (letterVal != null
              && letterVal.equals(Constants.ATTRVAL_TRADITIONAL))
        formattedNumber.append(tradAlphaCount(listElement, thisBundle));
      else  
        int2alphaCount(listElement,
                       (CharArrayWrapper) thisBundle.getObject(org.apache.xml.utils.res.XResourceBundle.LANG_ALPHABET),
                       formattedNumber);
      break;
    }
    case 0x03B1 :
    {
      thisBundle = (XResourceBundle) XResourceBundle.loadResourceBundle(
        org.apache.xml.utils.res.XResourceBundle.LANG_BUNDLE_NAME, new Locale("el", ""));
      if (letterVal != null
              && letterVal.equals(Constants.ATTRVAL_TRADITIONAL))
        formattedNumber.append(tradAlphaCount(listElement, thisBundle));
      else  
        int2alphaCount(listElement,
                       (CharArrayWrapper) thisBundle.getObject(org.apache.xml.utils.res.XResourceBundle.LANG_ALPHABET),
                       formattedNumber);
      break;
    }
    case 0x0430 :
    {
      thisBundle = (XResourceBundle) XResourceBundle.loadResourceBundle(
        org.apache.xml.utils.res.XResourceBundle.LANG_BUNDLE_NAME, new Locale("cy", ""));
      if (letterVal != null
              && letterVal.equals(Constants.ATTRVAL_TRADITIONAL))
        formattedNumber.append(tradAlphaCount(listElement, thisBundle));
      else  
        int2alphaCount(listElement,
                       (CharArrayWrapper) thisBundle.getObject(org.apache.xml.utils.res.XResourceBundle.LANG_ALPHABET),
                       formattedNumber);
      break;
    }
    default :  
      DecimalFormat formatter = getNumberFormatter(transformer, contextNode);
      String padString = formatter == null ? String.valueOf(0) : formatter.format(0);    
      String numString = formatter == null ? String.valueOf(listElement) : formatter.format(listElement);
      int nPadding = numberWidth - numString.length();
      for (int k = 0; k < nPadding; k++)
      {
        formattedNumber.append(padString);
      }
      formattedNumber.append(numString);
    }
  }
   String getZeroString()
   {
     return ""+0;
   }
  protected String int2singlealphaCount(long val, CharArrayWrapper table)
  {
    int radix = table.getLength();
    if (val > radix)
    {
      return getZeroString();
    }
    else
      return (new Character(table.getChar((int)val - 1))).toString();  
  }
  protected void int2alphaCount(long val, CharArrayWrapper aTable,
                                FastStringBuffer stringBuf)
  {
    int radix = aTable.getLength();
    char[] table = new char[radix];
    int i;
    for (i = 0; i < radix - 1; i++)
    {
      table[i + 1] = aTable.getChar(i);
    }
    table[0] = aTable.getChar(i);
    char buf[] = new char[100];
    int charPos;
    charPos = buf.length - 1;  
    int lookupIndex = 1;  
    long correction = 0;
    do
    {
      correction =
        ((lookupIndex == 0) || (correction != 0 && lookupIndex == radix - 1))
        ? (radix - 1) : 0;
      lookupIndex = (int)(val + correction) % radix;
      val = (val / radix);
      if (lookupIndex == 0 && val == 0)
        break;
      buf[charPos--] = table[lookupIndex];  
    }
    while (val > 0);
    stringBuf.append(buf, charPos + 1, (buf.length - charPos - 1));
  }
  protected String tradAlphaCount(long val, XResourceBundle thisBundle)
  {
    if (val > Long.MAX_VALUE)
    {
      this.error(XSLTErrorResources.ER_NUMBER_TOO_BIG);
      return XSLTErrorResources.ERROR_STRING;
    }
    char[] table = null;
    int lookupIndex = 1;  
    char buf[] = new char[100];
    int charPos;
    charPos = 0;  
    IntArrayWrapper groups = (IntArrayWrapper) thisBundle.getObject(org.apache.xml.utils.res.XResourceBundle.LANG_NUMBERGROUPS);
    StringArrayWrapper tables =
      (StringArrayWrapper) (thisBundle.getObject(org.apache.xml.utils.res.XResourceBundle.LANG_NUM_TABLES));
    String numbering = thisBundle.getString(org.apache.xml.utils.res.XResourceBundle.LANG_NUMBERING);
    if (numbering.equals(org.apache.xml.utils.res.XResourceBundle.LANG_MULT_ADD))
    {
      String mult_order = thisBundle.getString(org.apache.xml.utils.res.XResourceBundle.MULT_ORDER);
      LongArrayWrapper multiplier =
        (LongArrayWrapper) (thisBundle.getObject(org.apache.xml.utils.res.XResourceBundle.LANG_MULTIPLIER));
      CharArrayWrapper zeroChar = (CharArrayWrapper) thisBundle.getObject("zero");
      int i = 0;
      while (i < multiplier.getLength() && val < multiplier.getLong(i))
      {
        i++;
      }
      do
      {
        if (i >= multiplier.getLength())
          break;  
        if (val < multiplier.getLong(i))
        {
          if (zeroChar.getLength() == 0)
          {
            i++;
          }
          else
          {
            if (buf[charPos - 1] != zeroChar.getChar(0))
              buf[charPos++] = zeroChar.getChar(0);
            i++;
          }
        }
        else if (val >= multiplier.getLong(i))
        {
          long mult = val / multiplier.getLong(i);
          val = val % multiplier.getLong(i);  
          int k = 0;
          while (k < groups.getLength())
          {
            lookupIndex = 1;  
            if (mult / groups.getInt(k) <= 0)  
              k++;
            else
            {
              CharArrayWrapper THEletters = (CharArrayWrapper) thisBundle.getObject(tables.getString(k));
              table = new char[THEletters.getLength() + 1];
              int j;
              for (j = 0; j < THEletters.getLength(); j++)
              {
                table[j + 1] = THEletters.getChar(j);
              }
              table[0] = THEletters.getChar(j - 1);  
              lookupIndex = (int)mult / groups.getInt(k);
              if (lookupIndex == 0 && mult == 0)
                break;
              char multiplierChar = ((CharArrayWrapper) (thisBundle.getObject(
                org.apache.xml.utils.res.XResourceBundle.LANG_MULTIPLIER_CHAR))).getChar(i);
              if (lookupIndex < table.length)
              {
                if (mult_order.equals(org.apache.xml.utils.res.XResourceBundle.MULT_PRECEDES))
                {
                  buf[charPos++] = multiplierChar;
                  buf[charPos++] = table[lookupIndex];
                }
                else
                {
                  if (lookupIndex == 1 && i == multiplier.getLength() - 1){}
                  else
                    buf[charPos++] = table[lookupIndex];
                  buf[charPos++] = multiplierChar;
                }
                break;  
              }
              else
                return XSLTErrorResources.ERROR_STRING;
            }  
          }  
          i++;
        }  
      }  
      while (i < multiplier.getLength());
    }
    int count = 0;
    String tableName;
    while (count < groups.getLength())
    {
      if (val / groups.getInt(count) <= 0)  
        count++;
      else
      {
        CharArrayWrapper theletters = (CharArrayWrapper) thisBundle.getObject(tables.getString(count));
        table = new char[theletters.getLength() + 1];
        int j;
        for (j = 0; j < theletters.getLength(); j++)
        {
          table[j + 1] = theletters.getChar(j);
        }
        table[0] = theletters.getChar(j - 1);  
        lookupIndex = (int)val / groups.getInt(count);
        val = val % groups.getInt(count);
        if (lookupIndex == 0 && val == 0)
          break;
        if (lookupIndex < table.length)
        {
          buf[charPos++] = table[lookupIndex];  
        }
        else
          return XSLTErrorResources.ERROR_STRING;
        count++;
      }
    }  
    return new String(buf, 0, charPos);
  }
  protected String long2roman(long val, boolean prefixesAreOK)
  {
    if (val <= 0)
    {
      return getZeroString();
    }
    String roman = "";
    int place = 0;
    if (val <= 3999L)
    {
      do
      {
        while (val >= m_romanConvertTable[place].m_postValue)
        {
          roman += m_romanConvertTable[place].m_postLetter;
          val -= m_romanConvertTable[place].m_postValue;
        }
        if (prefixesAreOK)
        {
          if (val >= m_romanConvertTable[place].m_preValue)
          {
            roman += m_romanConvertTable[place].m_preLetter;
            val -= m_romanConvertTable[place].m_preValue;
          }
        }
        place++;
      }
      while (val > 0);
    }
    else
    {
      roman = XSLTErrorResources.ERROR_STRING;
    }
    return roman;
  }  
  public void callChildVisitors(XSLTVisitor visitor, boolean callAttrs)
  {
  	if(callAttrs)
  	{
	  	if(null != m_countMatchPattern)
	  		m_countMatchPattern.getExpression().callVisitors(m_countMatchPattern, visitor);
	  	if(null != m_fromMatchPattern)
	  		m_fromMatchPattern.getExpression().callVisitors(m_fromMatchPattern, visitor);
	  	if(null != m_valueExpr)
	  		m_valueExpr.getExpression().callVisitors(m_valueExpr, visitor);
	  	if(null != m_format_avt)
	  		m_format_avt.callVisitors(visitor);
	  	if(null != m_groupingSeparator_avt)
	  		m_groupingSeparator_avt.callVisitors(visitor);
	  	if(null != m_groupingSize_avt)
	  		m_groupingSize_avt.callVisitors(visitor);
	  	if(null != m_lang_avt)
	  		m_lang_avt.callVisitors(visitor);
	  	if(null != m_lettervalue_avt)
	  		m_lettervalue_avt.callVisitors(visitor);
  	}
    super.callChildVisitors(visitor, callAttrs);
  }
  class NumberFormatStringTokenizer
  {
    private int currentPosition;
    private int maxPosition;
    private String str;
    public NumberFormatStringTokenizer(String str)
    {
      this.str = str;
      maxPosition = str.length();
    }
    public void reset()
    {
      currentPosition = 0;
    }
    public String nextToken()
    {
      if (currentPosition >= maxPosition)
      {
        throw new NoSuchElementException();
      }
      int start = currentPosition;
      while ((currentPosition < maxPosition)
             && Character.isLetterOrDigit(str.charAt(currentPosition)))
      {
        currentPosition++;
      }
      if ((start == currentPosition)
              && (!Character.isLetterOrDigit(str.charAt(currentPosition))))
      {
        currentPosition++;
      }
      return str.substring(start, currentPosition);
    }
    public boolean isLetterOrDigitAhead()
    {
      int pos = currentPosition;
      while (pos < maxPosition)
      {
        if (Character.isLetterOrDigit(str.charAt(pos)))
          return true;
        pos++;
      }
      return false;
    }
    public boolean nextIsSep()
    {
      if (Character.isLetterOrDigit(str.charAt(currentPosition)))
        return false;
      else
        return true;
    }
    public boolean hasMoreTokens()
    {
      return (currentPosition >= maxPosition) ? false : true;
    }
    public int countTokens()
    {
      int count = 0;
      int currpos = currentPosition;
      while (currpos < maxPosition)
      {
        int start = currpos;
        while ((currpos < maxPosition)
               && Character.isLetterOrDigit(str.charAt(currpos)))
        {
          currpos++;
        }
        if ((start == currpos)
                && (Character.isLetterOrDigit(str.charAt(currpos)) == false))
        {
          currpos++;
        }
        count++;
      }
      return count;
    }
  }  
}

package org.apache.xalan.xsltc.dom;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;
import org.apache.xml.dtm.DTM;
import org.apache.xml.dtm.ref.DTMDefaultBase;
import org.apache.xml.dtm.DTMException;
import org.apache.xml.dtm.DTMWSFilter;
import org.apache.xml.dtm.ref.DTMManagerDefault;
import org.apache.xml.res.XMLErrorResources;
import org.apache.xml.res.XMLMessages;
import org.apache.xml.utils.SystemIDResolver;
import org.apache.xalan.xsltc.trax.DOM2SAX;
import org.xml.sax.InputSource;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;
public class XSLTCDTMManager extends DTMManagerDefault
{
    private static final String DEFAULT_CLASS_NAME =
        "org.apache.xalan.xsltc.dom.XSLTCDTMManager";
    private static final String DEFAULT_PROP_NAME =
        "org.apache.xalan.xsltc.dom.XSLTCDTMManager";
    private static final boolean DUMPTREE = false;
    private static final boolean DEBUG = false;
    public XSLTCDTMManager()
    {
        super();
    } 
    public static XSLTCDTMManager newInstance()
    {
        return new XSLTCDTMManager();
    } 
    public static Class getDTMManagerClass() {
        Class mgrClass = ObjectFactory.lookUpFactoryClass(DEFAULT_PROP_NAME,
                                                          null,
                                                          DEFAULT_CLASS_NAME);
        return (mgrClass != null) ? mgrClass : XSLTCDTMManager.class;
    }
    public DTM getDTM(Source source, boolean unique,
                      DTMWSFilter whiteSpaceFilter, boolean incremental,
                      boolean doIndexing)
    {
        return getDTM(source, unique, whiteSpaceFilter, incremental,
		      doIndexing, false, 0, true, false);
    }
    public DTM getDTM(Source source, boolean unique,
                      DTMWSFilter whiteSpaceFilter, boolean incremental,
                      boolean doIndexing, boolean buildIdIndex)
    {
        return getDTM(source, unique, whiteSpaceFilter, incremental,
		      doIndexing, false, 0, buildIdIndex, false);
    }
  public DTM getDTM(Source source, boolean unique,
		    DTMWSFilter whiteSpaceFilter, boolean incremental,
		    boolean doIndexing, boolean buildIdIndex,
		    boolean newNameTable)
  {
    return getDTM(source, unique, whiteSpaceFilter, incremental,
		  doIndexing, false, 0, buildIdIndex, newNameTable);
  }
    public DTM getDTM(Source source, boolean unique,
                      DTMWSFilter whiteSpaceFilter, boolean incremental,
                      boolean doIndexing, boolean hasUserReader, int size,
                      boolean buildIdIndex)
    {
      return getDTM(source, unique, whiteSpaceFilter, incremental,
                    doIndexing, hasUserReader, size,
                    buildIdIndex, false);
  }
  public DTM getDTM(Source source, boolean unique,
		    DTMWSFilter whiteSpaceFilter, boolean incremental,
		    boolean doIndexing, boolean hasUserReader, int size,
		    boolean buildIdIndex, boolean newNameTable)
  {
        if(DEBUG && null != source) {
            System.out.println("Starting "+
			 (unique ? "UNIQUE" : "shared")+
			 " source: "+source.getSystemId());
        }
        int dtmPos = getFirstFreeDTMID();
        int documentID = dtmPos << IDENT_DTM_NODE_BITS;
        if ((null != source) && source instanceof DOMSource)
        {
            final DOMSource domsrc = (DOMSource) source;
            final org.w3c.dom.Node node = domsrc.getNode();
            final DOM2SAX dom2sax = new DOM2SAX(node);
            SAXImpl dtm;
            if (size <= 0) {
                dtm = new SAXImpl(this, source, documentID,
                                  whiteSpaceFilter, null, doIndexing, 
                                  DTMDefaultBase.DEFAULT_BLOCKSIZE,
                                  buildIdIndex, newNameTable);
            } else {
                dtm = new SAXImpl(this, source, documentID,
                                  whiteSpaceFilter, null, doIndexing, 
                                  size, buildIdIndex, newNameTable);
            }
            dtm.setDocumentURI(source.getSystemId());
            addDTM(dtm, dtmPos, 0);
            dom2sax.setContentHandler(dtm);
            try {
                dom2sax.parse();
            }
            catch (RuntimeException re) {
                throw re;
            }
            catch (Exception e) {
                throw new org.apache.xml.utils.WrappedRuntimeException(e);
            }
            return dtm;
        }
        else
        {
            boolean isSAXSource = (null != source)
                                  ? (source instanceof SAXSource) : true;
            boolean isStreamSource = (null != source)
                                  ? (source instanceof StreamSource) : false;
            if (isSAXSource || isStreamSource) {
                XMLReader reader;
                InputSource xmlSource;
                if (null == source) {
                    xmlSource = null;
                    reader = null;
                    hasUserReader = false;  
                }
                else {
                    reader = getXMLReader(source);
                    xmlSource = SAXSource.sourceToInputSource(source);
                    String urlOfSource = xmlSource.getSystemId();
                    if (null != urlOfSource) {
                        try {
                            urlOfSource = SystemIDResolver.getAbsoluteURI(urlOfSource);
                        }
                        catch (Exception e) {
                            System.err.println("Can not absolutize URL: " + urlOfSource);
                        }
                        xmlSource.setSystemId(urlOfSource);
                    }
                }
                SAXImpl dtm;
                if (size <= 0) {
                    dtm = new SAXImpl(this, source, documentID, whiteSpaceFilter,
			              null, doIndexing, 
			              DTMDefaultBase.DEFAULT_BLOCKSIZE,
			              buildIdIndex, newNameTable);
                } else {
                    dtm = new SAXImpl(this, source, documentID, whiteSpaceFilter,
			    null, doIndexing, size, buildIdIndex, newNameTable);
                }
                addDTM(dtm, dtmPos, 0);
                if (null == reader) {
                    return dtm;
                }
                reader.setContentHandler(dtm.getBuilder());
                if (!hasUserReader || null == reader.getDTDHandler()) {
                    reader.setDTDHandler(dtm);
                }
                if(!hasUserReader || null == reader.getErrorHandler()) {
                    reader.setErrorHandler(dtm);
                }
                try {
                    reader.setProperty("http://xml.org/sax/properties/lexical-handler", dtm);
                }
                catch (SAXNotRecognizedException e){}
                catch (SAXNotSupportedException e){}
                try {
                    reader.parse(xmlSource);
                }
                catch (RuntimeException re) {
                    throw re;
                }
                catch (Exception e) {
                    throw new org.apache.xml.utils.WrappedRuntimeException(e);
                } finally {
                    if (!hasUserReader) {
                        releaseXMLReader(reader);
                    }
                }
                if (DUMPTREE) {
                    System.out.println("Dumping SAX2DOM");
                    dtm.dumpDTM(System.err);
                }
                return dtm;
            }
            else {
                throw new DTMException(XMLMessages.createXMLMessage(XMLErrorResources.ER_NOT_SUPPORTED, new Object[]{source}));
            }
        }
    }
}

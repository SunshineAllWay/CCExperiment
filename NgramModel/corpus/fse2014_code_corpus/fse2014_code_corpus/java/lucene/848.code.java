package org.apache.lucene.benchmark.byTask.feeds;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import org.apache.lucene.benchmark.byTask.utils.Config;
import org.apache.lucene.util.ThreadInterruptedException;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;
public class EnwikiContentSource extends ContentSource {
  private class Parser extends DefaultHandler implements Runnable {
    private Thread t;
    private boolean threadDone;
    private String[] tuple;
    private NoMoreDataException nmde;
    private StringBuffer contents = new StringBuffer();
    private String title;
    private String body;
    private String time;
    private String id;
    String[] next() throws NoMoreDataException {
      if (t == null) {
        threadDone = false;
        t = new Thread(this);
        t.setDaemon(true);
        t.start();
      }
      String[] result;
      synchronized(this){
        while(tuple == null && nmde == null && !threadDone) {
          try {
            wait();
          } catch (InterruptedException ie) {
            throw new ThreadInterruptedException(ie);
          }
        }
        if (nmde != null) {
          t = null;
          throw nmde;
        }
        if (t != null && threadDone) {
          throw new NoMoreDataException();
        }
        result = tuple;
        tuple = null;
        notify();
      }
      return result;
    }
    String time(String original) {
      StringBuffer buffer = new StringBuffer();
      buffer.append(original.substring(8, 10));
      buffer.append('-');
      buffer.append(months[Integer.valueOf(original.substring(5, 7)).intValue() - 1]);
      buffer.append('-');
      buffer.append(original.substring(0, 4));
      buffer.append(' ');
      buffer.append(original.substring(11, 19));
      buffer.append(".000");
      return buffer.toString();
    }
    @Override
    public void characters(char[] ch, int start, int length) {
      contents.append(ch, start, length);
    }
    @Override
    public void endElement(String namespace, String simple, String qualified)
      throws SAXException {
      int elemType = getElementType(qualified);
      switch (elemType) {
        case PAGE:
          if (body != null && (keepImages || !title.startsWith("Image:"))) {
            String[] tmpTuple = new String[LENGTH];
            tmpTuple[TITLE] = title.replace('\t', ' ');
            tmpTuple[DATE] = time.replace('\t', ' ');
            tmpTuple[BODY] = body.replaceAll("[\t\n]", " ");
            tmpTuple[ID] = id;
            synchronized(this) {
              while (tuple != null) {
                try {
                  wait();
                } catch (InterruptedException ie) {
                  throw new ThreadInterruptedException(ie);
                }
              }
              tuple = tmpTuple;
              notify();
            }
          }
          break;
        case BODY:
          body = contents.toString();
          String startsWith = body.substring(0, Math.min(10, contents.length())).toLowerCase();
          if (startsWith.startsWith("#redirect")) {
            body = null;
          }
          break;
        case DATE:
          time = time(contents.toString());
          break;
        case TITLE:
          title = contents.toString();
          break;
        case ID:
          id = contents.toString();
          break;
        default:
      }
    }
    public void run() {
      try {
        XMLReader reader = XMLReaderFactory.createXMLReader();
        reader.setContentHandler(this);
        reader.setErrorHandler(this);
        while(true){
          final InputStream localFileIS = is;
          try {
            reader.parse(new InputSource(localFileIS));
          } catch (IOException ioe) {
            synchronized(EnwikiContentSource.this) {
              if (localFileIS != is) {
              } else
                throw ioe;
            }
          }
          synchronized(this) {
            if (!forever) {
              nmde = new NoMoreDataException();
              notify();
              return;
            } else if (localFileIS == is) {
              is = getInputStream(file);
            }
          }
        }
      } catch (SAXException sae) {
        throw new RuntimeException(sae);
      } catch (IOException ioe) {
        throw new RuntimeException(ioe);
      } finally {
        synchronized(this) {
          threadDone = true;
          notify();
        }
      }
    }
    @Override
    public void startElement(String namespace, String simple, String qualified,
                             Attributes attributes) {
      int elemType = getElementType(qualified);
      switch (elemType) {
        case PAGE:
          title = null;
          body = null;
          time = null;
          id = null;
          break;
        case BODY:
        case DATE:
        case TITLE:
        case ID:
          contents.setLength(0);
          break;
        default:
      }
    }
  }
  private static final Map<String,Integer> ELEMENTS = new HashMap<String,Integer>();
  private static final int TITLE = 0;
  private static final int DATE = TITLE + 1;
  private static final int BODY = DATE + 1;
  private static final int ID = BODY + 1;
  private static final int LENGTH = ID + 1;
  private static final int PAGE = LENGTH + 1;
  private static final String[] months = {"JAN", "FEB", "MAR", "APR",
                                  "MAY", "JUN", "JUL", "AUG",
                                  "SEP", "OCT", "NOV", "DEC"};
  static {
    ELEMENTS.put("page", Integer.valueOf(PAGE));
    ELEMENTS.put("text", Integer.valueOf(BODY));
    ELEMENTS.put("timestamp", Integer.valueOf(DATE));
    ELEMENTS.put("title", Integer.valueOf(TITLE));
    ELEMENTS.put("id", Integer.valueOf(ID));
  }
  private final static int getElementType(String elem) {
    Integer val = ELEMENTS.get(elem);
    return val == null ? -1 : val.intValue();
  }
  private File file;
  private boolean keepImages = true;
  private InputStream is;
  private Parser parser = new Parser();
  @Override
  public void close() throws IOException {
    synchronized (EnwikiContentSource.this) {
      if (is != null) {
        is.close();
        is = null;
      }
    }
  }
  @Override
  public synchronized DocData getNextDocData(DocData docData) throws NoMoreDataException, IOException {
    String[] tuple = parser.next();
    docData.clear();
    docData.setName(tuple[ID]);
    docData.setBody(tuple[BODY]);
    docData.setDate(tuple[DATE]);
    docData.setTitle(tuple[TITLE]);
    return docData;
  }
  @Override
  public void resetInputs() throws IOException {
    super.resetInputs();
    is = getInputStream(file);
  }
  @Override
  public void setConfig(Config config) {
    super.setConfig(config);
    keepImages = config.get("keep.image.only.docs", true);
    String fileName = config.get("docs.file", null);
    if (fileName == null) {
      throw new IllegalArgumentException("docs.file must be set");
    }
    file = new File(fileName).getAbsoluteFile();
  }
}

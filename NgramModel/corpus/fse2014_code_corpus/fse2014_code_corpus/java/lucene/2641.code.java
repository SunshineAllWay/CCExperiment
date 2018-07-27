package org.apache.solr.client.solrj.impl;
import java.io.Reader;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.apache.solr.client.solrj.ResponseParser;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.common.util.SimpleOrderedMap;
public class XMLResponseParser extends ResponseParser
{
  public static Logger log = LoggerFactory.getLogger(XMLResponseParser.class);
  static final XMLInputFactory factory;
  static {
    factory = XMLInputFactory.newInstance();
    try {
      factory.setProperty("reuse-instance", Boolean.FALSE);
    }
    catch( IllegalArgumentException ex ) {
      log.debug( "Unable to set the 'reuse-instance' property for the input factory: "+factory );
    }
  }
  public XMLResponseParser() {}
  @Override
  public String getWriterType()
  {
    return "xml";
  }
  @Override
  public NamedList<Object> processResponse(Reader in) {
    XMLStreamReader parser = null;
    try {
      parser = factory.createXMLStreamReader(in);
    } catch (XMLStreamException e) {
      throw new SolrException( SolrException.ErrorCode.SERVER_ERROR, "parsing error", e);
    }
    return processResponse(parser);    
  }
  @Override
  public NamedList<Object> processResponse(InputStream in, String encoding)
  {
     XMLStreamReader parser = null;
    try {
      parser = factory.createXMLStreamReader(in, encoding);
    } catch (XMLStreamException e) {
      throw new SolrException( SolrException.ErrorCode.SERVER_ERROR, "parsing error", e);
    }
    return processResponse(parser);
  }
  private NamedList<Object> processResponse(XMLStreamReader parser)
  {
    try {
      NamedList<Object> response = null;
      for (int event = parser.next();  
       event != XMLStreamConstants.END_DOCUMENT;
       event = parser.next()) 
      {
        switch (event) {
          case XMLStreamConstants.START_ELEMENT:
            if( response != null ) {
              throw new Exception( "already read the response!" );
            }
            String name = parser.getLocalName();
            if( name.equals( "response" ) || name.equals( "result" ) ) {
              response = readNamedList( parser );
            }
            else if( name.equals( "solr" ) ) {
              return new SimpleOrderedMap<Object>();
            }
            else {
              throw new Exception( "really needs to be response or result.  " +
                  "not:"+parser.getLocalName() );
            }
            break;
        } 
      } 
      return response;
    }
    catch( Exception ex ) {
      throw new SolrException( SolrException.ErrorCode.SERVER_ERROR, "parsing error", ex );
    }
    finally {
      try {
        parser.close();
      }
      catch( Exception ex ){}
    }
  }
  protected enum KnownType {
    STR    (true)  { @Override public String  read( String txt ) { return txt;                  } },
    INT    (true)  { @Override public Integer read( String txt ) { return Integer.valueOf(txt); } },
    FLOAT  (true)  { @Override public Float   read( String txt ) { return Float.valueOf(txt);   } },
    DOUBLE (true)  { @Override public Double  read( String txt ) { return Double.valueOf(txt);  } },
    LONG   (true)  { @Override public Long    read( String txt ) { return Long.valueOf(txt);    } },
    BOOL   (true)  { @Override public Boolean read( String txt ) { return Boolean.valueOf(txt); } },
    NULL   (true)  { @Override public Object  read( String txt ) { return null;                 } },
    DATE   (true)  { 
      @Override 
      public Date read( String txt ) { 
        try {
          return ClientUtils.parseDate(txt);      
        }
        catch( Exception ex ) {
          ex.printStackTrace();
        }
        return null;
      } 
    },
    ARR    (false) { @Override public Object read( String txt ) { return null; } },
    LST    (false) { @Override public Object read( String txt ) { return null; } },
    RESULT (false) { @Override public Object read( String txt ) { return null; } },
    DOC    (false) { @Override public Object read( String txt ) { return null; } };
    final boolean isLeaf;
    KnownType( boolean isLeaf )
    {
      this.isLeaf = isLeaf;
    }
    public abstract Object read( String txt );
    public static KnownType get( String v )
    {
      if( v != null ) {
        try {
          return KnownType.valueOf( v.toUpperCase() );
        }
        catch( Exception ex ) {}
      }
      return null;
    }
  };
  protected NamedList<Object> readNamedList( XMLStreamReader parser ) throws XMLStreamException
  {
    if( XMLStreamConstants.START_ELEMENT != parser.getEventType() ) {
      throw new RuntimeException( "must be start element, not: "+parser.getEventType() );
    }
    StringBuilder builder = new StringBuilder();
    NamedList<Object> nl = new SimpleOrderedMap<Object>();
    KnownType type = null;
    String name = null;
    int depth = 0;
    while( true ) 
    {
      switch (parser.next()) {
      case XMLStreamConstants.START_ELEMENT:
        depth++;
        builder.setLength( 0 ); 
        type = KnownType.get( parser.getLocalName() );
        if( type == null ) {
          throw new RuntimeException( "this must be known type! not: "+parser.getLocalName() );
        }
        name = null;
        int cnt = parser.getAttributeCount();
        for( int i=0; i<cnt; i++ ) {
          if( "name".equals( parser.getAttributeLocalName( i ) ) ) {
            name = parser.getAttributeValue( i );
            break;
          }
        }
        if( !type.isLeaf ) {
          switch( type ) {
          case LST:    nl.add( name, readNamedList( parser ) ); depth--; continue;
          case ARR:    nl.add( name, readArray(     parser ) ); depth--; continue;
          case RESULT: nl.add( name, readDocuments( parser ) ); depth--; continue;
          case DOC:    nl.add( name, readDocument(  parser ) ); depth--; continue;
          }
          throw new XMLStreamException( "branch element not handled!", parser.getLocation() );
        }
        break;
      case XMLStreamConstants.END_ELEMENT:
        if( --depth < 0 ) {
          return nl;
        }
        nl.add( name, type.read( builder.toString().trim() ) );
        break;
      case XMLStreamConstants.SPACE: 
      case XMLStreamConstants.CDATA:
      case XMLStreamConstants.CHARACTERS:
        builder.append( parser.getText() );
        break;
      }
    }
  }
  protected List<Object> readArray( XMLStreamReader parser ) throws XMLStreamException
  {
    if( XMLStreamConstants.START_ELEMENT != parser.getEventType() ) {
      throw new RuntimeException( "must be start element, not: "+parser.getEventType() );
    }
    if( !"arr".equals( parser.getLocalName().toLowerCase() ) ) {
      throw new RuntimeException( "must be 'arr', not: "+parser.getLocalName() );
    }
    StringBuilder builder = new StringBuilder();
    KnownType type = null;
    List<Object> vals = new ArrayList<Object>();
    int depth = 0;
    while( true ) 
    {
      switch (parser.next()) {
      case XMLStreamConstants.START_ELEMENT:
        depth++;
        KnownType t = KnownType.get( parser.getLocalName() );
        if( t == null ) {
          throw new RuntimeException( "this must be known type! not: "+parser.getLocalName() );
        }
        if( type == null ) {
          type = t;
        }
        type = t;
        builder.setLength( 0 ); 
        if( !type.isLeaf ) {
          switch( type ) {
          case LST:    vals.add( readNamedList( parser ) ); continue;
          case ARR:    vals.add( readArray( parser ) ); continue;
          case RESULT: vals.add( readDocuments( parser ) ); continue;
          case DOC:    vals.add( readDocument( parser ) ); continue;
          }
          throw new XMLStreamException( "branch element not handled!", parser.getLocation() );
        }
        break;
      case XMLStreamConstants.END_ELEMENT:
        if( --depth < 0 ) {
          return vals; 
        }
        Object val = type.read( builder.toString().trim() );
        if( val == null && type != KnownType.NULL) {
          throw new XMLStreamException( "error reading value:"+type, parser.getLocation() );
        }
        vals.add( val );
        break;
      case XMLStreamConstants.SPACE: 
      case XMLStreamConstants.CDATA:
      case XMLStreamConstants.CHARACTERS:
        builder.append( parser.getText() );
        break;
    }
    }
  }
  protected SolrDocumentList readDocuments( XMLStreamReader parser ) throws XMLStreamException
  {
    SolrDocumentList docs = new SolrDocumentList();
    for( int i=0; i<parser.getAttributeCount(); i++ ) {
      String n = parser.getAttributeLocalName( i );
      String v = parser.getAttributeValue( i );
      if( "numFound".equals( n ) ) {
        docs.setNumFound( Long.parseLong( v ) );
      }
      else if( "start".equals( n ) ) {
        docs.setStart( Long.parseLong( v ) );
      }
      else if( "maxScore".equals( n ) ) {
        docs.setMaxScore( Float.parseFloat( v ) );
      }
    }
    int event;
    while( true ) {
      event = parser.next();
      if( XMLStreamConstants.START_ELEMENT == event ) {
        if( !"doc".equals( parser.getLocalName() ) ) {
          throw new RuntimeException( "shoudl be doc! "+parser.getLocalName() + " :: " + parser.getLocation() );
        }
        docs.add( readDocument( parser ) );
      }
      else if ( XMLStreamConstants.END_ELEMENT == event ) {
        return docs;  
      }
    }
  }
  protected SolrDocument readDocument( XMLStreamReader parser ) throws XMLStreamException
  {
    if( XMLStreamConstants.START_ELEMENT != parser.getEventType() ) {
      throw new RuntimeException( "must be start element, not: "+parser.getEventType() );
    }
    if( !"doc".equals( parser.getLocalName().toLowerCase() ) ) {
      throw new RuntimeException( "must be 'lst', not: "+parser.getLocalName() );
    }
    SolrDocument doc = new SolrDocument();
    StringBuilder builder = new StringBuilder();
    KnownType type = null;
    String name = null;
    int depth = 0;
    while( true ) 
    {
      switch (parser.next()) {
      case XMLStreamConstants.START_ELEMENT:
        depth++;
        builder.setLength( 0 ); 
        type = KnownType.get( parser.getLocalName() );
        if( type == null ) {
          throw new RuntimeException( "this must be known type! not: "+parser.getLocalName() );
        }
        name = null;
        int cnt = parser.getAttributeCount();
        for( int i=0; i<cnt; i++ ) {
          if( "name".equals( parser.getAttributeLocalName( i ) ) ) {
            name = parser.getAttributeValue( i );
            break;
          }
        }
        if( name == null ) {
          throw new XMLStreamException( "requires 'name' attribute: "+parser.getLocalName(), parser.getLocation() );
        }
        if( type == KnownType.ARR ) {
          for( Object val : readArray( parser ) ) {
            doc.addField( name, val );
          }
          depth--; 
        }
        else if( !type.isLeaf ) {
          throw new XMLStreamException( "must be value or array", parser.getLocation() );
        }
        break;
      case XMLStreamConstants.END_ELEMENT:
        if( --depth < 0 ) {
          return doc;
        }
        Object val = type.read( builder.toString().trim() );
        if( val == null ) {
          throw new XMLStreamException( "error reading value:"+type, parser.getLocation() );
        }
        doc.addField( name, val );
        break;
      case XMLStreamConstants.SPACE: 
      case XMLStreamConstants.CDATA:
      case XMLStreamConstants.CHARACTERS:
        builder.append( parser.getText() );
        break;
      }
    }
  }
}

package dom.serialize;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import org.apache.xerces.dom.DocumentImpl;
import org.apache.xerces.parsers.DOMParser;
import org.w3c.dom.Document;
import dom.Writer;
public class TestSerializeDOMOut
{          
    public TestSerializeDOMOut(){
    }
    public void serializeDOM( Document doc, String nameSerializedFile ){
        try {
            ObjectOutputStream out               =
                              new ObjectOutputStream( new FileOutputStream( nameSerializedFile ) );
            out.writeObject(doc);
            out.close();
        } catch ( Exception ex ) {
            ex.printStackTrace();
        }
    }
    public static void main (String[] argv) 
    { 
        if ( argv.length != 1 ) {
            System.out.println("Error - Usage: java TestOut yourFile.xml" );
            System.exit(1);
        }
        String    xmlFilename = argv[0];
        try {
            DOMParser parser     = new DOMParser();
            parser.parse( xmlFilename ); 
            DocumentImpl doc     = (DocumentImpl) parser.getDocument();
            int indexOfextension = xmlFilename.indexOf("." );
            String nameOfSerializedFile = null;
            if ( indexOfextension == -1 ) {
                nameOfSerializedFile = xmlFilename +".ser" ;
            } else {
                nameOfSerializedFile = 
                xmlFilename.substring(0,indexOfextension) + ".ser";
            }
            System.out.println( "Writing Serialize DOM  to file = " + nameOfSerializedFile ); 
            FileOutputStream fileOut =  new FileOutputStream( nameOfSerializedFile );
            TestSerializeDOMOut  tstOut = new TestSerializeDOMOut();
            tstOut.serializeDOM( doc, nameOfSerializedFile );
            System.out.println( "Reading Serialize DOM from " + nameOfSerializedFile );
            TestSerializeDOMIn    tstIn  = new TestSerializeDOMIn();
            doc           = tstIn.deserializeDOM( nameOfSerializedFile );
            Writer prettyWriter = new Writer( false );
            prettyWriter.setOutput(System.out, "UTF8");
            System.out.println( "Here is the whole Document" );
            prettyWriter.write(  doc.getDocumentElement() );
        } catch ( Exception ex ){
            ex.printStackTrace();
        }
    } 
}

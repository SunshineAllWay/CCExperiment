import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.DefaultHandler;
public class Validate
{
  static int numXMLFiles = 0;
  static int numValidFiles = 0;
  static int numInvalidFiles = 0;
  static int numFilesMissingDoctype = 0;
  static int numMalformedFiles = 0;
  static boolean useLogFile = false;
  static StringBuffer buff = new StringBuffer();
  public static void main(String[] args)
    throws FileNotFoundException, IOException, ParserConfigurationException, SAXException
  {
    if (args.length == 0 || args.length > 2)
    {
      System.out.println("\nEnter 'java validate -help' for information about running Validate");
      return;
    }
    if (args[0].toLowerCase().equals("-help"))
    { 
      String sep = "\n====================================================\n";
      String a = "Validate uses Xerces to parse the xml files in the directory you specify or the individual xml file you specify. The parser validates each document (checks that it conforms to its DOCTYPE).\n";
      String b = "Each xml file should contain a DOCTYPE declaration.\n\n";
      String c = "Validate takes 1 or 2 arguments:\n";
      String d = " Argument 1 specifies a directory or an individual xml file.\n";
      String e = " Argument 2 specifies a log file. If you include this argument, Validate appends messages to this file. If you do not, Validate writes messages to the screen.\n";
      System.out.println(sep+a+b+c+d+e+sep);
      return;
    }    
    try
    {
      Validate v = new Validate();
      v.validate(args);
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }    
  }
  void validate(String[] args)
    throws FileNotFoundException, IOException, ParserConfigurationException, SAXException
  {
    File dir = new File(args[0]);
    useLogFile = (args.length == 2);
    if (dir.isFile()) 
    {
      parse(null,args[0]);
    }
    else if (dir.isDirectory())  
    {
      XMLFileFilter filter = new XMLFileFilter();
      String [] files = dir.list(filter);
      for (int i = 0; i <files.length; i++)
      { 
        parse(dir.toString(),files[i]); 
        if (!useLogFile) 
        {
          System.out.print(buff.toString());
          buff = new StringBuffer();
        }
      }
    }
    else 
    {
      System.out.println(args[0] + " not found!");
      return;
    }
    buff.append("================SUMMARY=============================\n");
    if (numXMLFiles > 1)
      buff.append("Parsed " + numXMLFiles + " .xml files in " +  args[0] + ".\n");
    if (numValidFiles > 1)
      buff.append( numValidFiles + " files are valid.\n");
    else if (numValidFiles == 1)
      buff.append( numValidFiles + " file is valid.\n");
    if (numInvalidFiles > 1)
      buff.append(numInvalidFiles + " files are not valid.\n");
    else if (numInvalidFiles == 1)
      buff.append( numInvalidFiles + " file is not valid.\n");
    if (numMalformedFiles > 1)
      buff.append(numMalformedFiles + " files are not well-formed.\n");
    else if (numMalformedFiles == 1)
      buff.append( numMalformedFiles + " file is not well-formed.\n");
    if (numFilesMissingDoctype > 1)
      buff.append(numFilesMissingDoctype + " files do not contain a DOCTYPE declaration.\n");
    else if (numFilesMissingDoctype == 1)
      buff.append(numFilesMissingDoctype + " file does not contain a DOCTYPE declaration.\n");
    if (!useLogFile)
      System.out.print(buff.toString());
    else
    {
      FileWriter writer = new FileWriter(args[1], true);
      writer.write(new java.util.Date().toString()+ "\n");
      writer.write(buff.toString());
      writer.close();     
      System.out.println("Done with validation. See " + args[1] + ".");
    }
  }
  void parse(String dir, String filename)
     throws FileNotFoundException, IOException, ParserConfigurationException, SAXException
  {
    try 
    {
      File f = new File(dir, filename);
      StringBuffer errorBuff = new StringBuffer();
      InputSource input = new InputSource(new FileInputStream(f));
      input.setSystemId(f.toString());
      SAXParserFactory spfact = SAXParserFactory.newInstance();
      spfact.setValidating(true);
      spfact.setNamespaceAware(true);
      SAXParser parser = spfact.newSAXParser();
      XMLReader reader = parser.getXMLReader();
      Handler handler = new Handler(filename, errorBuff);
      reader.setProperty("http://xml.org/sax/properties/lexical-handler", handler);
      parser.parse(input, handler);
      if (handler.containsDTD && !handler.errorOrWarning) 
      {
        buff.append("VALID " + filename +"\n");
        numValidFiles++;
      }
      else if (handler.containsDTD) 
      {
        buff.append ("NOT VALID " + filename + "\n");
        buff.append(errorBuff.toString());
        numInvalidFiles++;
      }
      else 
      {
        buff.append("NO DOCTYPE DECLARATION " + filename + "\n");
        numFilesMissingDoctype++;
      }
    }
    catch (Exception e) 
    {
      buff.append("NOT WELL-FORMED " + filename + ". " + e.getMessage() + "\n");
      numMalformedFiles++;
    }
    finally
    {
      numXMLFiles++;
    }
  }
  class XMLFileFilter implements FilenameFilter
  {
    public boolean accept(File dir, String fileName)
    {
      return fileName.toLowerCase().endsWith(".xml") && new File(dir.toString(),fileName).isFile();
    }
  }
  class Handler extends DefaultHandler implements LexicalHandler
  {
    boolean errorOrWarning;
    boolean containsDTD;
    String sourceFile;
    StringBuffer errorBuff;  
    Handler(String sourceFile, StringBuffer errorBuff)
    {
      super();
      this.sourceFile = sourceFile;
      this.errorBuff = errorBuff;
      errorOrWarning = false;
      containsDTD = false;
    }
    public void error(SAXParseException exc)
    {
      errorBuff.append(sourceFile + " Error: " + exc.getMessage()+ "\n");    
      errorOrWarning = true;
    }
    public void warning(SAXParseException exc)
    {
      errorBuff.append(sourceFile + " Warning:" + exc.getMessage()+ "\n");   
      errorOrWarning = true;
    }
    public void startDTD (String name, String publicId, String systemId)
	    throws SAXException
    {
      containsDTD = true;
    }
    public void endDTD () throws SAXException
    {}  
    public void startEntity (String name) throws SAXException
    {}
    public void endEntity (String name) throws SAXException
    {}
    public void startCDATA () throws SAXException
    {}
    public void endCDATA () throws SAXException
    {}
    public void comment (char ch[], int start, int length) throws SAXException
    {}
  }
}

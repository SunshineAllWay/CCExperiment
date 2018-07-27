import javax.xml.transform.Templates;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamSource;
public class Compile {
    public static void main(String[] args){
        Compile app = new Compile();
        app.run(args[0]);
    }
    public void run(String xsl) {
        try {
            System.setProperty("javax.xml.transform.TransformerFactory",
                         "org.apache.xalan.xsltc.trax.TransformerFactoryImpl");
	    StreamSource stylesheet = new StreamSource(xsl);
	    TransformerFactory factory = TransformerFactory.newInstance();
            factory.setAttribute("generate-translet", Boolean.TRUE);
	    Templates templates = factory.newTemplates(stylesheet);
        }
	catch (Exception e) {
            System.err.println("Exception: " + e); 
	    e.printStackTrace();
        }
        System.exit(0);
    }
    private void usage() {
        System.err.println("Usage: compile <xsl_file>");
        System.exit(1);
    }
}

package org.apache.batik.test.xml;
import java.io.File;
import java.io.FileOutputStream;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.stream.StreamResult;
import org.apache.batik.test.TestException;
public class XSLXMLReportConsumer 
    implements XMLTestReportProcessor.XMLReportConsumer {
    public static final String ERROR_OUTPUT_DIRECTORY_UNUSABLE 
        = "xml.XSLXMLReportConsumer.error.output.directory.unusable";
    private String stylesheet;
    private String outputDirectory;
    private String outputFileName;
    public XSLXMLReportConsumer(String stylesheet,
                                String outputDirectory,
                                String outputFileName){
        this.stylesheet = stylesheet;
        this.outputDirectory = outputDirectory;
        this.outputFileName = outputFileName;
    }
    public void onNewReport(File xmlReport, 
                            File reportDirectory)
        throws Exception{
        TransformerFactory tFactory = TransformerFactory.newInstance();
        Transformer transformer = tFactory.newTransformer(new StreamSource(stylesheet));
        transformer.transform(new StreamSource(xmlReport.toURL().toString()), 
                              new StreamResult(new FileOutputStream(createNewReportOutput(reportDirectory).getAbsolutePath())));
    }
    public File createNewReportOutput(File reportDirectory) throws Exception{
        File dir = new File(reportDirectory, outputDirectory);
        checkDirectory(dir);
        return new File(dir, outputFileName);
    }
    public void checkDirectory(File dir) 
        throws TestException {
        boolean dirOK = false;
        try{
            if(!dir.exists()){
                dirOK = dir.mkdir();
            }
            else if(dir.isDirectory()){
                dirOK = true;
            }
        }finally{
            if(!dirOK){
                throw new TestException(ERROR_OUTPUT_DIRECTORY_UNUSABLE,
                                        new Object[] {dir.getAbsolutePath()},
                                        null);
            }
        }
    }
}

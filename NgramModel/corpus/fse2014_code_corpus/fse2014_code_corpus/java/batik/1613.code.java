package org.apache.batik.test.svg;
import java.awt.Color;
import java.io.FileOutputStream;
import java.io.StringWriter;
import java.io.PrintWriter;
import java.net.URL;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.ImageTranscoder;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.apache.batik.test.DefaultTestReport;
import org.apache.batik.test.TestReport;
import org.w3c.dom.Document;
public class SVGRenderingAccuracyTest extends AbstractRenderingAccuracyTest {
    public static final String ERROR_CANNOT_TRANSCODE_SVG           
        = "SVGRenderingAccuracyTest.error.cannot.transcode.svg";
    public static final String VALIDATING_PARSER
        = configuration.getString("validating.parser");
    protected boolean validate = false;
    protected String userLanguage;
    public SVGRenderingAccuracyTest(String svgURL,
                                    String refImgURL){
        super(svgURL, refImgURL);
    }
    protected SVGRenderingAccuracyTest(){
    }
    public void setValidating(Boolean validate){
        if (validate == null){
            throw new IllegalArgumentException();
        }
        this.validate = validate.booleanValue();
    }
    public boolean getValidating(){
        return validate;
    }
    public void setUserLanguage(String userLanguage){
        this.userLanguage = userLanguage;
    }
    public String getUserLanguage(){
        return this.userLanguage;
    }
    protected Document manipulateSVGDocument(Document doc) {
        return doc;
    }
    public TestReport encode(URL srcURL, FileOutputStream fos) {
        DefaultTestReport report = new DefaultTestReport(this);
        try{
            ImageTranscoder transcoder = getTestImageTranscoder();
            TranscoderInput src = new TranscoderInput(svgURL.toString());
            TranscoderOutput dst = new TranscoderOutput(fos);
            transcoder.transcode(src, dst);
            return null;
        }catch(TranscoderException e){
            StringWriter trace = new StringWriter();
            e.printStackTrace(new PrintWriter(trace));
            report.setErrorCode(ERROR_CANNOT_TRANSCODE_SVG);
            report.setDescription(new TestReport.Entry[]{
                new TestReport.Entry
                (Messages.formatMessage(ENTRY_KEY_ERROR_DESCRIPTION, null),
                 Messages.formatMessage(ERROR_CANNOT_TRANSCODE_SVG,
                                        new String[]{svgURL.toString(), 
                                                     e.getClass().getName(),
                                                     e.getMessage(),
                                                     trace.toString()
                                        })) });
        }catch(Exception e){
            StringWriter trace = new StringWriter();
            e.printStackTrace(new PrintWriter(trace));
            report.setErrorCode(ERROR_CANNOT_TRANSCODE_SVG);
            report.setDescription(new TestReport.Entry[]{
                new TestReport.Entry
                (Messages.formatMessage(ENTRY_KEY_ERROR_DESCRIPTION, null),
                 Messages.formatMessage(ERROR_CANNOT_TRANSCODE_SVG,
                                        new String[]{svgURL.toString(), 
                                                     e.getClass().getName(),
                                                     e.getMessage(),
                                                     trace.toString()
                                        })) });
        }
        report.setPassed(false);
        return report;
    }
    public ImageTranscoder getTestImageTranscoder(){
        ImageTranscoder t = new InternalPNGTranscoder();
        t.addTranscodingHint(PNGTranscoder.KEY_FORCE_TRANSPARENT_WHITE,
                             Boolean.FALSE);
        t.addTranscodingHint(PNGTranscoder.KEY_BACKGROUND_COLOR,
                             new Color(0,0,0,0));
        t.addTranscodingHint(PNGTranscoder.KEY_EXECUTE_ONLOAD,
                             Boolean.TRUE);
        if (validate){
            t.addTranscodingHint(PNGTranscoder.KEY_XML_PARSER_VALIDATING,
                                 Boolean.TRUE);
            t.addTranscodingHint(PNGTranscoder.KEY_XML_PARSER_CLASSNAME,
                                 VALIDATING_PARSER);
        }
        if (userLanguage != null){
            t.addTranscodingHint(PNGTranscoder.KEY_LANGUAGE, 
                                 userLanguage);
        }
        return t;
    }
    protected class InternalPNGTranscoder extends PNGTranscoder{
        protected void transcode(Document document,
                                 String uri,
                                 TranscoderOutput output)
            throws TranscoderException {
            SVGRenderingAccuracyTest.this.manipulateSVGDocument(document);
            super.transcode(document, uri, output);
        }
    }
}

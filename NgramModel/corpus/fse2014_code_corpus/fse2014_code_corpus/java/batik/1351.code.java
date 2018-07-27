package org.apache.batik.transcoder.print;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.File;
import java.util.StringTokenizer;
import java.util.List;
import java.util.ArrayList;
import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.ext.awt.RenderingHintsKeyExt;
import org.apache.batik.transcoder.SVGAbstractTranscoder;
import org.apache.batik.transcoder.Transcoder;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.TranscodingHints;
import org.apache.batik.transcoder.keys.BooleanKey;
import org.apache.batik.transcoder.keys.LengthKey;
import org.apache.batik.transcoder.keys.StringKey;
import org.w3c.dom.Document;
public class PrintTranscoder extends SVGAbstractTranscoder
    implements Printable {
    public static final String KEY_AOI_STR = "aoi";
    public static final String KEY_HEIGHT_STR = "height";
    public static final String KEY_LANGUAGE_STR = "language";
    public static final String KEY_MARGIN_BOTTOM_STR = "marginBottom";
    public static final String KEY_MARGIN_LEFT_STR = "marginLeft";
    public static final String KEY_MARGIN_RIGHT_STR = "marginRight";
    public static final String KEY_MARGIN_TOP_STR = "marginTop";
    public static final String KEY_PAGE_HEIGHT_STR = "pageHeight";
    public static final String KEY_PAGE_ORIENTATION_STR         = "pageOrientation";
    public static final String KEY_PAGE_WIDTH_STR = "pageWidth";
    public static final String KEY_PIXEL_TO_MM_STR = "pixelToMm";
    public static final String KEY_SCALE_TO_PAGE_STR         = "scaleToPage";
    public static final String KEY_SHOW_PAGE_DIALOG_STR = "showPageDialog";
    public static final String KEY_SHOW_PRINTER_DIALOG_STR = "showPrinterDialog";
    public static final String KEY_USER_STYLESHEET_URI_STR = "userStylesheet";
    public static final String KEY_WIDTH_STR = "width";
    public static final String KEY_XML_PARSER_CLASSNAME_STR = "xmlParserClassName";
    public static final String VALUE_MEDIA_PRINT = "print";
    public static final String VALUE_PAGE_ORIENTATION_LANDSCAPE = "landscape";
    public static final String VALUE_PAGE_ORIENTATION_PORTRAIT  = "portrait";
    public static final String VALUE_PAGE_ORIENTATION_REVERSE_LANDSCAPE = "reverseLandscape";
    private List inputs = new ArrayList();
    private List printedInputs = null;
    private int curIndex = -1;
    private BridgeContext theCtx;
    public PrintTranscoder() {
        super();
        hints.put(KEY_MEDIA,
                  VALUE_MEDIA_PRINT);
    }
    public void transcode(TranscoderInput in,
                          TranscoderOutput out){
        if(in != null){
            inputs.add(in);
        }
    }
    protected void transcode(Document document,
                             String uri,
                             TranscoderOutput output)
            throws TranscoderException {
        super.transcode(document, uri, output);
        theCtx = ctx;
        ctx = null;
    }
    public void print() throws PrinterException{
        PrinterJob printerJob = PrinterJob.getPrinterJob();
        PageFormat pageFormat = printerJob.defaultPage();
        Paper paper = pageFormat.getPaper();
        Float pageWidth = (Float)hints.get(KEY_PAGE_WIDTH);
        Float pageHeight = (Float)hints.get(KEY_PAGE_HEIGHT);
        if(pageWidth != null){
            paper.setSize(pageWidth.floatValue(),
                          paper.getHeight());
        }
        if(pageHeight != null){
            paper.setSize(paper.getWidth(),
                          pageHeight.floatValue());
        }
        float x=0, y=0;
        float width =(float)paper.getWidth();
        float height=(float)paper.getHeight();
        Float leftMargin   = (Float)hints.get(KEY_MARGIN_LEFT);
        Float topMargin    = (Float)hints.get(KEY_MARGIN_TOP);
        Float rightMargin  = (Float)hints.get(KEY_MARGIN_RIGHT);
        Float bottomMargin = (Float)hints.get(KEY_MARGIN_BOTTOM);
        if(leftMargin != null){
            x      = leftMargin.floatValue();
            width -= leftMargin.floatValue();
        }
        if(topMargin != null){
            y       = topMargin.floatValue();
            height -= topMargin.floatValue();
        }
        if(rightMargin != null){
            width -= rightMargin.floatValue();
        }
        if(bottomMargin != null){
            height -= bottomMargin.floatValue();
        }
        paper.setImageableArea(x, y, width, height);
        String pageOrientation = (String)hints.get(KEY_PAGE_ORIENTATION);
        if(VALUE_PAGE_ORIENTATION_PORTRAIT.equalsIgnoreCase(pageOrientation)){
            pageFormat.setOrientation(PageFormat.PORTRAIT);
        }
        else if(VALUE_PAGE_ORIENTATION_LANDSCAPE.equalsIgnoreCase(pageOrientation)){
            pageFormat.setOrientation(PageFormat.LANDSCAPE);
        }
        else if(VALUE_PAGE_ORIENTATION_REVERSE_LANDSCAPE.equalsIgnoreCase(pageOrientation)){
            pageFormat.setOrientation(PageFormat.REVERSE_LANDSCAPE);
        }
        pageFormat.setPaper(paper);
        pageFormat = printerJob.validatePage(pageFormat);
        Boolean showPageFormat = (Boolean)hints.get(KEY_SHOW_PAGE_DIALOG);
        if ((showPageFormat != null) && (showPageFormat.booleanValue())) {
            PageFormat tmpPageFormat = printerJob.pageDialog(pageFormat);
            if(tmpPageFormat == pageFormat){
                return;
            }
            pageFormat = tmpPageFormat;
        }
        printerJob.setPrintable(this, pageFormat);
        Boolean showPrinterDialog;
        showPrinterDialog = (Boolean)hints.get(KEY_SHOW_PRINTER_DIALOG);
        if(showPrinterDialog != null && showPrinterDialog.booleanValue()){
            if(!printerJob.printDialog()){
                return;
            }
        }
        printerJob.print();
    }
    public int print(Graphics _g, PageFormat pageFormat, int pageIndex){
        if(printedInputs == null){
            printedInputs = new ArrayList( inputs );
        }
        if(pageIndex >= printedInputs.size()){
            curIndex = -1;
            if (theCtx != null)
                theCtx.dispose();
            userAgent.displayMessage("Done");
            return NO_SUCH_PAGE;
        }
        if(curIndex != pageIndex){
            if (theCtx != null)
                theCtx.dispose();
            try{
                width  = (int)pageFormat.getImageableWidth();
                height = (int)pageFormat.getImageableHeight();
                super.transcode
                    ((TranscoderInput)printedInputs.get(pageIndex),null);
                curIndex = pageIndex;
            }catch(TranscoderException e){
                drawError(_g, e);
                return PAGE_EXISTS;
            }
        }
        Graphics2D g = (Graphics2D)_g;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                           RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                           RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHintsKeyExt.KEY_TRANSCODING,
                           RenderingHintsKeyExt.VALUE_TRANSCODING_PRINTING);
        AffineTransform t = g.getTransform();
        Shape clip = g.getClip();
        g.translate(pageFormat.getImageableX(),
                    pageFormat.getImageableY());
        g.transform(curTxf);
        try{
            root.paint(g);
        }catch(Exception e){
            g.setTransform(t);
            g.setClip(clip);
            drawError(_g, e);
        }
        g.setTransform(t);
        g.setClip(clip);
        return PAGE_EXISTS;
    }
    protected void setImageSize(float docWidth, float docHeight) {
        Boolean scaleToPage = (Boolean)hints.get(KEY_SCALE_TO_PAGE);
        if(scaleToPage != null && !scaleToPage.booleanValue()) {
            float w = docWidth;
            float h = docHeight;
            if (hints.containsKey(KEY_AOI)) {
                Rectangle2D aoi = (Rectangle2D)hints.get(KEY_AOI);
                w = (float)aoi.getWidth();
                h = (float)aoi.getHeight();
            }
            super.setImageSize(w, h);
        }
    }
    private void drawError(Graphics g, Exception e){
        userAgent.displayError(e);
    }
    public static final TranscodingHints.Key KEY_SHOW_PAGE_DIALOG
        = new BooleanKey();
    public static final TranscodingHints.Key KEY_SHOW_PRINTER_DIALOG
        = new BooleanKey();
    public static final TranscodingHints.Key KEY_PAGE_WIDTH
        = new LengthKey();
    public static final TranscodingHints.Key KEY_PAGE_HEIGHT
        = new LengthKey();
    public static final TranscodingHints.Key KEY_MARGIN_TOP
        = new LengthKey();
    public static final TranscodingHints.Key KEY_MARGIN_RIGHT
        = new LengthKey();
    public static final TranscodingHints.Key KEY_MARGIN_BOTTOM
        = new LengthKey();
    public static final TranscodingHints.Key KEY_MARGIN_LEFT
        = new LengthKey();
    public static final TranscodingHints.Key KEY_PAGE_ORIENTATION
        = new StringKey();
    public static final TranscodingHints.Key KEY_SCALE_TO_PAGE
        = new BooleanKey();
    public static final String USAGE = "java org.apache.batik.transcoder.print.PrintTranscoder <svgFileToPrint>";
    public static void main(String[] args) throws Exception{
        if(args.length < 1){
            System.err.println(USAGE);
            System.exit(0);
        }
        PrintTranscoder transcoder = new PrintTranscoder();
        setTranscoderFloatHint(transcoder,
                               KEY_LANGUAGE_STR,
                               KEY_LANGUAGE);
        setTranscoderFloatHint(transcoder,
                               KEY_USER_STYLESHEET_URI_STR,
                               KEY_USER_STYLESHEET_URI);
        setTranscoderStringHint(transcoder,
                                 KEY_XML_PARSER_CLASSNAME_STR,
                                 KEY_XML_PARSER_CLASSNAME);
        setTranscoderBooleanHint(transcoder,
                                 KEY_SCALE_TO_PAGE_STR,
                                 KEY_SCALE_TO_PAGE);
        setTranscoderRectangleHint(transcoder,
                                   KEY_AOI_STR,
                                   KEY_AOI);
        setTranscoderFloatHint(transcoder,
                               KEY_WIDTH_STR,
                               KEY_WIDTH);
        setTranscoderFloatHint(transcoder,
                               KEY_HEIGHT_STR,
                               KEY_HEIGHT);
        setTranscoderFloatHint(transcoder,
                               KEY_PIXEL_TO_MM_STR,
                               KEY_PIXEL_UNIT_TO_MILLIMETER);
        setTranscoderStringHint(transcoder,
                                KEY_PAGE_ORIENTATION_STR,
                                KEY_PAGE_ORIENTATION);
        setTranscoderFloatHint(transcoder,
                               KEY_PAGE_WIDTH_STR,
                               KEY_PAGE_WIDTH);
        setTranscoderFloatHint(transcoder,
                               KEY_PAGE_HEIGHT_STR,
                               KEY_PAGE_HEIGHT);
        setTranscoderFloatHint(transcoder,
                               KEY_MARGIN_TOP_STR,
                               KEY_MARGIN_TOP);
        setTranscoderFloatHint(transcoder,
                               KEY_MARGIN_RIGHT_STR,
                               KEY_MARGIN_RIGHT);
        setTranscoderFloatHint(transcoder,
                               KEY_MARGIN_BOTTOM_STR,
                               KEY_MARGIN_BOTTOM);
        setTranscoderFloatHint(transcoder,
                               KEY_MARGIN_LEFT_STR,
                               KEY_MARGIN_LEFT);
        setTranscoderBooleanHint(transcoder,
                                 KEY_SHOW_PAGE_DIALOG_STR,
                                 KEY_SHOW_PAGE_DIALOG);
        setTranscoderBooleanHint(transcoder,
                                 KEY_SHOW_PRINTER_DIALOG_STR,
                                 KEY_SHOW_PRINTER_DIALOG);
        for(int i=0; i<args.length; i++){
            transcoder.transcode(new TranscoderInput(new File(args[i]).toURL().toString()),
                                 null);
        }
        transcoder.print();
        System.exit(0);
    }
    public static void setTranscoderFloatHint(Transcoder transcoder,
                                              String property,
                                              TranscodingHints.Key key){
        String str = System.getProperty(property);
        if(str != null){
            try{
                Float value = new Float(Float.parseFloat(str));
                transcoder.addTranscodingHint(key, value);
            }catch(NumberFormatException e){
                handleValueError(property, str);
            }
        }
    }
    public static void setTranscoderRectangleHint(Transcoder transcoder,
                                                  String property,
                                                  TranscodingHints.Key key){
        String str = System.getProperty(property);
        if(str != null){
            StringTokenizer st = new StringTokenizer(str, " ,");
            if(st.countTokens() != 4){
                handleValueError(property, str);
            }
            try{
                String x = st.nextToken();
                String y = st.nextToken();
                String width = st.nextToken();
                String height = st.nextToken();
                Rectangle2D r = new Rectangle2D.Float(Float.parseFloat(x),
                                                      Float.parseFloat(y),
                                                      Float.parseFloat(width),
                                                      Float.parseFloat(height));
                transcoder.addTranscodingHint(key, r);
            }catch(NumberFormatException e){
                handleValueError(property, str);
            }
        }
    }
    public static void setTranscoderBooleanHint(Transcoder transcoder,
                                                String property,
                                                TranscodingHints.Key key){
        String str = System.getProperty(property);
        if(str != null){
            Boolean value = "true".equalsIgnoreCase(str) ? Boolean.TRUE : Boolean.FALSE;
            transcoder.addTranscodingHint(key, value);
        }
    }
    public static void setTranscoderStringHint(Transcoder transcoder,
                                              String property,
                                              TranscodingHints.Key key){
        String str = System.getProperty(property);
        if(str != null){
            transcoder.addTranscodingHint(key, str);
        }
    }
    public static void handleValueError(String property,
                                        String value){
        System.err.println("Invalid " + property + " value : " + value);
        System.exit(1);
    }
}

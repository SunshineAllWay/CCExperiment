package org.apache.batik.test.svg;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.net.MalformedURLException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.awt.image.WritableRaster;
import java.awt.image.ColorModel;
import org.apache.batik.ext.awt.image.GraphicsUtil;
import org.apache.batik.ext.awt.image.spi.ImageTagRegistry;
import org.apache.batik.ext.awt.image.spi.ImageWriter;
import org.apache.batik.ext.awt.image.spi.ImageWriterRegistry;
import org.apache.batik.ext.awt.image.renderable.Filter;
import org.apache.batik.util.ParsedURL;
import org.apache.batik.test.AbstractTest;
import org.apache.batik.test.DefaultTestReport;
import org.apache.batik.test.TestReport;
public abstract class AbstractRenderingAccuracyTest extends AbstractTest {
    public static final String ERROR_CANNOT_CREATE_TEMP_FILE
        = "SVGRenderingAccuracyTest.error.cannot.create.temp.file";
    public static final String ERROR_CANNOT_CREATE_TEMP_FILE_STREAM
        = "SVGRenderingAccuracyTest.error.cannot.create.temp.file.stream";
    public static final String ERROR_CANNOT_OPEN_REFERENCE_IMAGE
        = "SVGRenderingAccuracyTest.error.cannot.open.reference.image";
    public static final String ERROR_CANNOT_OPEN_GENERATED_IMAGE
        = "SVGRenderingAccuracyTest.error.cannot.open.genereted.image";
    public static final String ERROR_ERROR_WHILE_COMPARING_FILES
        = "SVGRenderingAccuracyTest.error.while.comparing.files";
    public static final String ERROR_SVG_RENDERING_NOT_ACCURATE
        = "SVGRenderingAccuracyTest.error.svg.rendering.not.accurate";
    public static final String ENTRY_KEY_ERROR_DESCRIPTION
        = "SVGRenderingAccuracyTest.entry.key.error.description";
    public static final String ENTRY_KEY_REFERENCE_GENERATED_IMAGE_URI
        = "SVGRenderingAccuracyTest.entry.key.reference.generated.image.file";
    public static final String ENTRY_KEY_DIFFERENCE_IMAGE
        = "SVGRenderingAccuracyTest.entry.key.difference.image";
    public static final String ENTRY_KEY_INTERNAL_ERROR
        = "SVGRenderingAccuracyTest.entry.key.internal.error";
    public static final String COULD_NOT_GENERATE_COMPARISON_IMAGES
        = "SVGRenderingAccuracyTest.message.error.could.not.generate.comparison.images";
    public static final String COULD_NOT_LOAD_IMAGE
        = "SVGRenderingAccuracyTest.message.error.could.not.load.image";
    public static final String COULD_NOT_OPEN_VARIATION_URL
        = "SVGRenderingAccuracyTest.message.warning.could.not.open.variation.url";
    public static final String CONFIGURATION_RESOURCES =
        "org.apache.batik.test.svg.resources.Configuration";
    public static final String IMAGE_TYPE_COMPARISON = "_cmp";
    public static final String IMAGE_TYPE_DIFF = "_diff";
    public static final String IMAGE_FILE_EXTENSION = ".png";
    protected static ResourceBundle configuration;
    static {
        configuration = ResourceBundle.getBundle(CONFIGURATION_RESOURCES,
                                                 Locale.getDefault());
    }
    public static final String TEMP_FILE_PREFIX
        = configuration.getString("temp.file.prefix");
    public static final String TEMP_FILE_SUFFIX
        = configuration.getString("temp.file.suffix");
    protected URL svgURL;
    protected URL refImgURL;
    protected List variationURLs;
    protected File saveVariation;
    protected File candidateReference;
    protected static File tempDirectory;
    public static File getTempDirectory(){
        if(tempDirectory == null){
            String tmpDir = System.getProperty("java.io.tmpdir");
            if(tmpDir == null){
                throw new Error();
            }
            tempDirectory = new File(tmpDir);
            if(!tempDirectory.exists()){
                throw new Error();
            }
        }
        return tempDirectory;
    }
    public AbstractRenderingAccuracyTest(String svgURL,
                                    String refImgURL){
        setConfig(svgURL, refImgURL);
    }
    protected AbstractRenderingAccuracyTest(){
    }
    public void setConfig(String svgURL,
                          String refImgURL){
        if(svgURL == null){
            throw new IllegalArgumentException();
        }
        if(refImgURL == null){
            throw new IllegalArgumentException();
        }
        this.svgURL = resolveURL(svgURL);
        this.refImgURL = resolveURL(refImgURL);
    }
    protected URL resolveURL(String url){
        File f = (new File(url)).getAbsoluteFile();
        if(f.getParentFile().exists()){
            try{
                return f.toURL();
            }catch(MalformedURLException e){
                throw new IllegalArgumentException();
            }
        }
        try{
            return new URL(url);
        }catch(MalformedURLException e){
            throw new IllegalArgumentException(url);
        }
    }
    public void setSaveVariation(File saveVariation){
        this.saveVariation = saveVariation;
    }
    public File getSaveVariation(){
        return saveVariation;
    }
    public String[] getVariationURLs() {
        if (variationURLs != null) {
            return (String[]) variationURLs.toArray(new String[0]);
        }
        return null;
    }
    public void addVariationURL(String variationURL) {
        if (this.variationURLs == null) {
            this.variationURLs = new LinkedList();
        }
        this.variationURLs.add(resolveURL(variationURL));
    }
    public void setCandidateReference(File candidateReference){
        this.candidateReference = candidateReference;
    }
    public File getCandidateReference(){
        return candidateReference;
    }
    public String getName(){
        if(this.name == null){
            return svgURL.toString();
        }
        return name;
    }
    public TestReport run() {
        DefaultTestReport report = new DefaultTestReport(this);
        if (candidateReference != null){
            if (candidateReference.exists()){
                candidateReference.delete();
            }
        }
        File tmpFile = null;
        try{
            if (candidateReference != null)
                tmpFile = candidateReference;
            else
                tmpFile = File.createTempFile(TEMP_FILE_PREFIX,
                                              TEMP_FILE_SUFFIX,
                                              null);
        }catch(IOException e){
            report.setErrorCode(ERROR_CANNOT_CREATE_TEMP_FILE);
            report.setDescription(new TestReport.Entry[] {
                new TestReport.Entry
                (Messages.formatMessage(ENTRY_KEY_ERROR_DESCRIPTION, null),
                 Messages.formatMessage(ERROR_CANNOT_CREATE_TEMP_FILE,
                                        new Object[]{e.getMessage()}))
            });
            report.setPassed(false);
            return report;
        }
        FileOutputStream tmpFileOS = null;
        try{
            tmpFileOS = new FileOutputStream(tmpFile);
        }catch(IOException e){
            report.setErrorCode(ERROR_CANNOT_CREATE_TEMP_FILE_STREAM);
            report.setDescription(new TestReport.Entry[] {
                new TestReport.Entry
                (Messages.formatMessage(ENTRY_KEY_ERROR_DESCRIPTION, null),
                 Messages.formatMessage(ERROR_CANNOT_CREATE_TEMP_FILE_STREAM,
                                        new String[]{tmpFile.getAbsolutePath(),
                                                     e.getMessage()})) });
            report.setPassed(false);
            tmpFile.deleteOnExit();
            return report;
        }
        {
            TestReport encodeTR = encode(svgURL, tmpFileOS);
            if ((encodeTR != null) && ! encodeTR.hasPassed() ) {
                tmpFile.deleteOnExit();
                return encodeTR;
            }
        }
        InputStream refStream = null;
        InputStream newStream = null;
        try {
            refStream = new BufferedInputStream(refImgURL.openStream());
        }catch(IOException e){
            report.setErrorCode(ERROR_CANNOT_OPEN_REFERENCE_IMAGE);
            report.setDescription(new TestReport.Entry[]{
                new TestReport.Entry
                (Messages.formatMessage(ENTRY_KEY_ERROR_DESCRIPTION, null),
                 Messages.formatMessage(ERROR_CANNOT_OPEN_REFERENCE_IMAGE,
                                        new Object[]{refImgURL.toString(),
                                                     e.getMessage()}))
                });
            report.setPassed(false);
            if (candidateReference == null){
                tmpFile.delete();
            }
            return report;
        }
        try{
            newStream = new BufferedInputStream(new FileInputStream(tmpFile));
        }catch(IOException e){
            report.setErrorCode(ERROR_CANNOT_OPEN_GENERATED_IMAGE);
            report.setDescription(new TestReport.Entry[]{
                new TestReport.Entry
                (Messages.formatMessage(ENTRY_KEY_ERROR_DESCRIPTION, null),
                 Messages.formatMessage(ERROR_CANNOT_OPEN_GENERATED_IMAGE,
                                        new Object[]{tmpFile.getAbsolutePath(),
                                                     e.getMessage()}))});
            report.setPassed(false);
            tmpFile.delete();
            return report;
        }
        boolean accurate = false;
        try{
            accurate = compare(refStream, newStream);
        } catch(IOException e) {
            report.setErrorCode(ERROR_ERROR_WHILE_COMPARING_FILES);
            report.setDescription(new TestReport.Entry[]{
                new TestReport.Entry
                (Messages.formatMessage(ENTRY_KEY_ERROR_DESCRIPTION, null),
                 Messages.formatMessage(ERROR_ERROR_WHILE_COMPARING_FILES,
                                        new Object[]{refImgURL.toString(),
                                                     tmpFile.getAbsolutePath(),
                                                     e.getMessage()}))});
            report.setPassed(false);
            if (candidateReference == null){
                tmpFile.delete();
            }
            return report;
        }
        if(accurate){
            report.setPassed(true);
            tmpFile.delete();
            return report;
        }
        try {
            BufferedImage ref = getImage(refImgURL);
            BufferedImage gen = getImage(tmpFile);
            BufferedImage diff = buildDiffImage(ref, gen);
            if (variationURLs != null) {
                Iterator it = variationURLs.iterator();
                while (it.hasNext()) {
                    URL variationURL = (URL) it.next();
                    File tmpDiff = imageToFile(diff, IMAGE_TYPE_DIFF);
                    InputStream variationURLStream = null;
                    try {
                        variationURLStream = variationURL.openStream();
                    } catch (IOException e) {
                        System.err.println
                            (Messages.formatMessage
                                (COULD_NOT_OPEN_VARIATION_URL,
                                 new Object[] { variationURL.toString() }));
                    }
                    if (variationURLStream != null) {
                        InputStream refDiffStream =
                            new BufferedInputStream(variationURLStream);
                        InputStream tmpDiffStream =
                            new BufferedInputStream
                                (new FileInputStream(tmpDiff));
                        if (compare(refDiffStream, tmpDiffStream)) {
                            accurate = true;
                        }
                    }
                }
            }
            if (accurate) {
                report.setPassed(true);
                tmpFile.delete();
                return report;
            }
            System.err.println(">>>>>>>>>>>>>>>>>>>>>> "+
                               "Rendering is not accurate");
            if(saveVariation != null){
                saveImage(diff, saveVariation);
            }
            BufferedImage cmp = makeCompareImage(ref, gen);
            File cmpFile = imageToFile(cmp, IMAGE_TYPE_COMPARISON);
            File diffFile = imageToFile(diff, IMAGE_TYPE_DIFF);
            report.setErrorCode(ERROR_SVG_RENDERING_NOT_ACCURATE);
            report.setDescription(new TestReport.Entry[]{
                new TestReport.Entry
                (Messages.formatMessage(ENTRY_KEY_ERROR_DESCRIPTION, null),
                 Messages.formatMessage(ERROR_SVG_RENDERING_NOT_ACCURATE, null)),
                new TestReport.Entry
                (Messages.formatMessage(ENTRY_KEY_REFERENCE_GENERATED_IMAGE_URI,
                                        null), cmpFile),
                new TestReport.Entry
                (Messages.formatMessage(ENTRY_KEY_DIFFERENCE_IMAGE, null),
                 diffFile) });
        }catch(Exception e){
            report.setErrorCode(ERROR_SVG_RENDERING_NOT_ACCURATE);
            StringWriter trace = new StringWriter();
            e.printStackTrace(new PrintWriter(trace));
            report.setDescription(new TestReport.Entry[]{
                new TestReport.Entry
                (Messages.formatMessage(ENTRY_KEY_ERROR_DESCRIPTION, null),
                 Messages.formatMessage(ERROR_SVG_RENDERING_NOT_ACCURATE, null)),
                new TestReport.Entry
                (Messages.formatMessage(ENTRY_KEY_INTERNAL_ERROR, null),
                 Messages.formatMessage(COULD_NOT_GENERATE_COMPARISON_IMAGES,
                                        new Object[]{e.getClass().getName(),
                                                     e.getMessage(),
                                                     trace.toString()})) });
        }
        if (candidateReference == null){
            tmpFile.delete();
        }
        report.setPassed(false);
        return report;
    }
    public abstract TestReport encode(URL srcURL, FileOutputStream fos);
    protected boolean compare(InputStream refStream,
                              InputStream newStream)
        throws IOException{
        int b, nb;
        do {
            b = refStream.read();
            nb = newStream.read();
        } while (b != -1 && nb != -1 && b == nb);
        refStream.close();
        newStream.close();
        return (b == nb);
    }
    protected void saveImage(BufferedImage img, File imgFile)
        throws IOException {
        if(!imgFile.exists()){
            imgFile.createNewFile();
        }
        OutputStream out = new FileOutputStream(imgFile);
        try {
            saveImage(img, out);
        } finally {
            out.close();
        }
    }
    protected void saveImage(BufferedImage img, OutputStream os)
            throws IOException {
        ImageWriter writer = ImageWriterRegistry.getInstance()
            .getWriterFor("image/png");
        writer.writeImage(img, os);
    }
    public static BufferedImage buildDiffImage(BufferedImage ref,
                                               BufferedImage gen) {
        BufferedImage diff = new BufferedImage(ref.getWidth(),
                                               ref.getHeight(),
                                               BufferedImage.TYPE_INT_ARGB);
        WritableRaster refWR = ref.getRaster();
        WritableRaster genWR = gen.getRaster();
        WritableRaster dstWR = diff.getRaster();
        boolean refPre = ref.isAlphaPremultiplied();
        if (!refPre) {
            ColorModel     cm = ref.getColorModel();
            cm = GraphicsUtil.coerceData(refWR, cm, true);
            ref = new BufferedImage(cm, refWR, true, null);
        }
        boolean genPre = gen.isAlphaPremultiplied();
        if (!genPre) {
            ColorModel     cm = gen.getColorModel();
            cm = GraphicsUtil.coerceData(genWR, cm, true);
            gen = new BufferedImage(cm, genWR, true, null);
        }
        int w=ref.getWidth();
        int h=ref.getHeight();
        int y, i,val;
        int [] refPix = null;
        int [] genPix = null;
        for (y=0; y<h; y++) {
            refPix = refWR.getPixels  (0, y, w, 1, refPix);
            genPix = genWR.getPixels  (0, y, w, 1, genPix);
            for (i=0; i<refPix.length; i++) {
                val = ((genPix[i]-refPix[i])*10)+128;
                if ((val & 0xFFFFFF00) != 0)
                    if ((val & 0x80000000) != 0) val = 0;
                    else                         val = 255;
                genPix[i] = val;
            }
            dstWR.setPixels(0, y, w, 1, genPix);
        }
        if (!genPre) {
            ColorModel cm = gen.getColorModel();
            cm = GraphicsUtil.coerceData(genWR, cm, false);
        }
        if (!refPre) {
            ColorModel cm = ref.getColorModel();
            cm = GraphicsUtil.coerceData(refWR, cm, false);
        }
        return diff;
    }
    protected BufferedImage getImage(File file)
        throws Exception {
        return getImage(file.toURL());
    }
    protected BufferedImage getImage(URL url)
        throws IOException {
        ImageTagRegistry reg = ImageTagRegistry.getRegistry();
        Filter filt = reg.readURL(new ParsedURL(url));
        if(filt == null)
            throw new IOException(Messages.formatMessage
                                  (COULD_NOT_LOAD_IMAGE,
                                   new Object[]{url.toString()}));
        RenderedImage red = filt.createDefaultRendering();
        if(red == null)
            throw new IOException(Messages.formatMessage
                                  (COULD_NOT_LOAD_IMAGE,
                                   new Object[]{url.toString()}));
        BufferedImage img = new BufferedImage(red.getWidth(),
                                              red.getHeight(),
                                              BufferedImage.TYPE_INT_ARGB);
        red.copyData(img.getRaster());
        return img;
    }
    protected BufferedImage makeCompareImage(BufferedImage ref,
                                             BufferedImage gen){
        BufferedImage cmp = new BufferedImage(ref.getWidth()*2,
                                              ref.getHeight(),
                                              BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = cmp.createGraphics();
        g.setPaint(Color.white);
        g.fillRect(0, 0, cmp.getWidth(), cmp.getHeight());
        g.drawImage(ref, 0, 0, null);
        g.translate(ref.getWidth(), 0);
        g.drawImage(gen, 0, 0, null);
        g.dispose();
        return cmp;
    }
    protected File imageToFile(BufferedImage img,
                               String imageType)
        throws IOException {
        String file = getURLFile(svgURL);
        File imageFile = null;
        if( !"".equals(file) ){
            imageFile = makeTempFileName(file, imageType);
        }
        else{
            imageFile = makeRandomFileName(imageType);
        }
        imageFile.deleteOnExit();
        saveImage(img, imageFile);
        return imageFile;
    }
    protected String getURLFile(URL url){
        String path = url.getPath();
        int n = path.lastIndexOf('/');
        if(n == -1){
            return path;
        }
        else{
            if(n<path.length()){
                return path.substring(n+1, path.length());
            }
            else{
                return "";
            }
        }
    }
    protected File makeTempFileName(String svgFileName,
                                    String imageType){
        int dotIndex = svgFileName.lastIndexOf('.');
        if( dotIndex == -1){
            return getNextTempFileName(svgFileName + imageType);
        }
        else{
            return getNextTempFileName
                (svgFileName.substring(0, dotIndex) +
                 imageType + IMAGE_FILE_EXTENSION);
        }
    }
    protected File getNextTempFileName(String fileName){
        File f = new File(getTempDirectory(), fileName);
        if(!f.exists()){
            return f;
        }
        else{
            return getNextTempFileName(fileName,
                                       1);
        }
    }
    protected File getNextTempFileName(String fileName,
                                       int instance){
        int n = fileName.lastIndexOf('.');
        String iFileName = fileName + instance;
        if(n != -1){
            iFileName = fileName.substring(0, n) + instance
                + fileName.substring(n, fileName.length());
        }
        File r = new File(getTempDirectory(), iFileName);
        if(!r.exists()){
            return r;
        }
        else{
            return getNextTempFileName(fileName,
                                       instance + 1);
        }
    }
    protected File makeRandomFileName(String imageType)
        throws IOException {
        return File.createTempFile(TEMP_FILE_PREFIX,
                                   TEMP_FILE_SUFFIX + imageType,
                                   null);
    }
}

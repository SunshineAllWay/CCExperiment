package org.apache.batik.apps.rasterizer;
import java.awt.Color;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import org.apache.batik.transcoder.Transcoder;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.ImageTranscoder;
import org.apache.batik.transcoder.image.JPEGTranscoder;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.apache.batik.util.ParsedURL;
public class SVGConverter {
    public static final String ERROR_NO_SOURCES_SPECIFIED
        = "SVGConverter.error.no.sources.specified";
    public static final String ERROR_CANNOT_COMPUTE_DESTINATION
        = "SVGConverter.error.cannot.compute.destination";
    public static final String ERROR_CANNOT_USE_DST_FILE
        = "SVGConverter.error.cannot.use.dst.file";
    public static final String ERROR_CANNOT_ACCESS_TRANSCODER
        = "SVGConverter.error.cannot.access.transcoder";
    public static final String ERROR_SOURCE_SAME_AS_DESTINATION
        = "SVGConverter.error.source.same.as.destination";
    public static final String ERROR_CANNOT_READ_SOURCE
        = "SVGConverter.error.cannot.read.source";
    public static final String ERROR_CANNOT_OPEN_SOURCE
        = "SVGConverter.error.cannot.open.source";
    public static final String ERROR_OUTPUT_NOT_WRITEABLE
        = "SVGConverter.error.output.not.writeable";
    public static final String ERROR_CANNOT_OPEN_OUTPUT_FILE
        = "SVGConverter.error.cannot.open.output.file";
    public static final String ERROR_UNABLE_TO_CREATE_OUTPUT_DIR
        = "SVGConverter.error.unable.to.create.output.dir";
    public static final String ERROR_WHILE_RASTERIZING_FILE
        = "SVGConverter.error.while.rasterizing.file";
    protected static final String SVG_EXTENSION = ".svg";
    protected static final float DEFAULT_QUALITY
        = -1.0f;
    protected static final float MAXIMUM_QUALITY
        = .99F;
    protected static final DestinationType DEFAULT_RESULT_TYPE
        = DestinationType.PNG;
    protected static final float DEFAULT_WIDTH = -1;
    protected static final float DEFAULT_HEIGHT = -1;
    protected DestinationType destinationType = DEFAULT_RESULT_TYPE;
    protected float height = DEFAULT_HEIGHT;
    protected float width = DEFAULT_WIDTH;
    protected float maxHeight = DEFAULT_HEIGHT;
    protected float maxWidth = DEFAULT_WIDTH;
    protected float quality = DEFAULT_QUALITY;
    protected int indexed = -1;
    protected Rectangle2D area = null;
    protected String language = null;
    protected String userStylesheet = null;
    protected float pixelUnitToMillimeter = -1.0f;
    protected boolean validate = false;
    protected boolean executeOnload = false;
    protected float snapshotTime = Float.NaN;
    protected String allowedScriptTypes = null;
    protected boolean constrainScriptOrigin = true;
    protected boolean securityOff = false;
    protected List sources = null;
    protected File dst;
    protected Color backgroundColor = null;
    protected String mediaType = null;
    protected String defaultFontFamily = null;
    protected String alternateStylesheet = null;
    protected List files = new ArrayList();
    protected SVGConverterController controller;
    public SVGConverter(){
        this(new DefaultSVGConverterController());
    }
    public SVGConverter(SVGConverterController controller){
        if (controller == null){
            throw new IllegalArgumentException();
        }
        this.controller = controller;
    }
    public void setDestinationType(DestinationType destinationType) {
        if(destinationType == null){
            throw new IllegalArgumentException();
        }
        this.destinationType = destinationType;
    }
    public DestinationType getDestinationType(){
        return destinationType;
    }
    public void setHeight(float height) {
        this.height = height;
    }
    public float getHeight(){
        return height;
    }
    public void setWidth(float width) {
        this.width = width;
    }
    public float getWidth(){
        return width;
    }
    public void setMaxHeight(float height) {
        this.maxHeight = height;
    }
    public float getMaxHeight(){
        return maxHeight;
    }
    public void setMaxWidth(float width) {
        this.maxWidth = width;
    }
    public float getMaxWidth(){
        return maxWidth;
    }
    public void setQuality(float quality) throws IllegalArgumentException {
        if(quality >= 1){
            throw new IllegalArgumentException();
        }
        this.quality = quality;
    }
    public float getQuality(){
        return quality;
    }
    public void setIndexed(int bits) throws IllegalArgumentException {
        this.indexed = bits;
    }
    public int getIndexed(){
        return indexed;
    }
    public void setLanguage(String language){
        this.language = language;
    }
    public String getLanguage(){
        return language;
    }
    public void setUserStylesheet(String userStylesheet){
        this.userStylesheet = userStylesheet;
    }
    public String getUserStylesheet(){
        return userStylesheet;
    }
    public void setPixelUnitToMillimeter(float pixelUnitToMillimeter){
        this.pixelUnitToMillimeter = pixelUnitToMillimeter;
    }
    public float getPixelUnitToMillimeter(){
        return pixelUnitToMillimeter;
    }
    public void setArea(Rectangle2D area){
        this.area = area;
    }
    public Rectangle2D getArea(){
        return area;
    }
    public void setSources(String[] sources) {
        if(sources == null){
            this.sources = null;
        }
        else{
            this.sources = new ArrayList();
            for (int i=0; i<sources.length; i++){
                if (sources[i] != null){
                    this.sources.add(sources[i]);
                }
            }
            if (this.sources.size() == 0){
                this.sources = null;
            }
        }
    }
    public List getSources(){
        return sources;
    }
    public void setDst(File dst) {
        this.dst = dst;
    }
    public File getDst(){
        return dst;
    }
    public void setBackgroundColor(Color backgroundColor){
        this.backgroundColor = backgroundColor;
    }
    public Color getBackgroundColor(){
        return backgroundColor;
    }
    public void setMediaType(String mediaType){
        this.mediaType = mediaType;
    }
    public String getMediaType(){
        return mediaType;
    }
    public void setDefaultFontFamily(String defaultFontFamily) {
        this.defaultFontFamily = defaultFontFamily;
    }
    public String getDefaultFontFamily() {
        return defaultFontFamily;
    }
    public void setAlternateStylesheet(String alternateStylesheet){
        this.alternateStylesheet = alternateStylesheet;
    }
    public String getAlternateStylesheet(){
        return alternateStylesheet;
    }
    public void setValidate(boolean validate){
        this.validate = validate;
    }
    public boolean getValidate(){
        return validate;
    }
    public void setExecuteOnload(boolean b){
        this.executeOnload = b;
    }
    public boolean getExecuteOnload(){
        return executeOnload;
    }
    public void setSnapshotTime(float t) {
        snapshotTime = t;
    }
    public float getSnapshotTime() {
        return snapshotTime;
    }
    public void setAllowedScriptTypes(String allowedScriptTypes){
        this.allowedScriptTypes = allowedScriptTypes;
    }
    public String getAllowedScriptTypes(){
        return allowedScriptTypes;
    }
    public void setConstrainScriptOrigin(boolean constrainScriptOrigin){
        this.constrainScriptOrigin = constrainScriptOrigin;
    }
    public boolean getConstrainScriptOrigin(){
        return constrainScriptOrigin;
    }
    public void setSecurityOff(boolean securityOff){
        this.securityOff = securityOff;
    }
    public boolean getSecurityOff(){
        return securityOff;
    }
    protected boolean isFile(File f){
        if (f.exists()){
            return f.isFile();
        } else {
            if (f.toString().toLowerCase().endsWith(destinationType.getExtension())){
                return true;
            }
        }
        return false;
    }
    public void execute() throws SVGConverterException {
        List sources = computeSources();
        List dstFiles = null;
        if(sources.size() == 1 && dst != null && isFile(dst)){
            dstFiles = new ArrayList();
            dstFiles.add(dst);
        }
        else{
            dstFiles = computeDstFiles(sources);
        }
        Transcoder transcoder = destinationType.getTranscoder();
        if(transcoder == null) {
            throw new SVGConverterException(ERROR_CANNOT_ACCESS_TRANSCODER,
                                             new Object[]{destinationType.toString()},
                                             true );
        }
        Map hints = computeTranscodingHints();
        transcoder.setTranscodingHints(hints);
        if(!controller.proceedWithComputedTask(transcoder,
                                               hints,
                                               sources,
                                               dstFiles)){
            return;
        }
        for(int i = 0 ; i < sources.size() ; i++) {
            SVGConverterSource currentFile
                = (SVGConverterSource)sources.get(i);
            File outputFile  = (File)dstFiles.get(i);
            createOutputDir(outputFile);
            transcode(currentFile, outputFile, transcoder);
        }
    }
    protected List computeDstFiles(List sources)
    throws SVGConverterException {
        List dstFiles = new ArrayList();
        if (dst != null) {
            if (dst.exists() && dst.isFile()) {
                throw new SVGConverterException(ERROR_CANNOT_USE_DST_FILE);
            }
            int n = sources.size();
            for(int i=0; i<n; i++){
                SVGConverterSource src = (SVGConverterSource)sources.get(i);
                File outputName = new File(dst.getPath(),
                                           getDestinationFile(src.getName()));
                dstFiles.add(outputName);
            }
        } else {
            int n = sources.size();
            for(int i=0; i<n; i++){
                SVGConverterSource src = (SVGConverterSource)sources.get(i);
                if (!(src instanceof SVGConverterFileSource)) {
                    throw new SVGConverterException(ERROR_CANNOT_COMPUTE_DESTINATION,
                                                     new Object[]{src});
                }
                SVGConverterFileSource fs = (SVGConverterFileSource)src;
                File outputName = new File(fs.getFile().getParent(),
                                           getDestinationFile(src.getName()));
                dstFiles.add(outputName);
            }
        }
        return dstFiles;
    }
    protected List computeSources() throws SVGConverterException{
        List sources = new ArrayList();
        if (this.sources == null){
            throw new SVGConverterException(ERROR_NO_SOURCES_SPECIFIED);
        }
        int n = this.sources.size();
        for (int i=0; i<n; i++){
            String sourceString = (String)(this.sources.get(i));
            File file = new File(sourceString);
            if (file.exists()) {
                sources.add(new SVGConverterFileSource(file));
            } else {
                String[] fileNRef = getFileNRef(sourceString);
                file = new File(fileNRef[0]);
                if (file.exists()){
                    sources.add(new SVGConverterFileSource(file, fileNRef[1]));
                } else{
                    sources.add(new SVGConverterURLSource(sourceString));
                }
            }
        }
        return sources;
    }
    public String[] getFileNRef(String fileName){
        int n = fileName.lastIndexOf('#');
        String[] result = {fileName, ""};
        if (n > -1){
            result[0] = fileName.substring(0, n);
            if (n+1 < fileName.length()){
                result[1] = fileName.substring(n+1);
            }
        }
        return result;
    }
    protected Map computeTranscodingHints(){
        Map map = new HashMap();
        if (area != null) {
            map.put(ImageTranscoder.KEY_AOI, area);
        }
        if (quality > 0) {
            map.put(JPEGTranscoder.KEY_QUALITY, new Float(this.quality));
        }
        if (indexed != -1) {
            map.put(PNGTranscoder.KEY_INDEXED, new Integer(indexed));
        }
        if (backgroundColor != null){
            map.put(ImageTranscoder.KEY_BACKGROUND_COLOR, backgroundColor);
        }
        if (height > 0) {
            map.put(ImageTranscoder.KEY_HEIGHT, new Float(this.height));
        }
        if (width > 0){
            map.put(ImageTranscoder.KEY_WIDTH, new Float(this.width));
        }
        if (maxHeight > 0) {
            map.put(ImageTranscoder.KEY_MAX_HEIGHT, new Float(this.maxHeight));
        }
        if (maxWidth > 0){
            map.put(ImageTranscoder.KEY_MAX_WIDTH, new Float(this.maxWidth));
        }
        if (mediaType != null){
            map.put(ImageTranscoder.KEY_MEDIA, mediaType);
        }
        if (defaultFontFamily != null) {
            map.put(ImageTranscoder.KEY_DEFAULT_FONT_FAMILY, defaultFontFamily);
        }
        if (alternateStylesheet != null){
            map.put(ImageTranscoder.KEY_ALTERNATE_STYLESHEET, alternateStylesheet);
        }
        if (userStylesheet != null) {
            String userStylesheetURL;
            try {
                URL userDir = new File(System.getProperty("user.dir")).toURL();
                userStylesheetURL = new ParsedURL(userDir, userStylesheet).toString();
            } catch (Exception e) {
                userStylesheetURL = userStylesheet;
            }
            map.put(ImageTranscoder.KEY_USER_STYLESHEET_URI, userStylesheetURL);
        }
        if (language != null){
            map.put(ImageTranscoder.KEY_LANGUAGE, language);
        }
        if (pixelUnitToMillimeter > 0){
            map.put(ImageTranscoder.KEY_PIXEL_UNIT_TO_MILLIMETER,
                    new Float(pixelUnitToMillimeter));
        }
        if (validate){
            map.put(ImageTranscoder.KEY_XML_PARSER_VALIDATING, Boolean.TRUE);
        }
        if (executeOnload) {
            map.put(ImageTranscoder.KEY_EXECUTE_ONLOAD, Boolean.TRUE);
        }
        if (!Float.isNaN(snapshotTime)) {
            map.put(ImageTranscoder.KEY_SNAPSHOT_TIME, new Float(snapshotTime));
        }
        if (allowedScriptTypes != null) {
            map.put(ImageTranscoder.KEY_ALLOWED_SCRIPT_TYPES, allowedScriptTypes);
        }
        if (!constrainScriptOrigin) {
            map.put(ImageTranscoder.KEY_CONSTRAIN_SCRIPT_ORIGIN, Boolean.FALSE);
        }
        return map;
    }
    protected void transcode(SVGConverterSource inputFile,
                             File outputFile,
                             Transcoder transcoder)
        throws SVGConverterException {
        TranscoderInput input = null;
        TranscoderOutput output = null;
        OutputStream outputStream = null;
        if (!controller.proceedWithSourceTranscoding(inputFile,
                                                     outputFile)){
            return;
        }
        try {
            if (inputFile.isSameAs(outputFile.getPath())) {
                throw new SVGConverterException(ERROR_SOURCE_SAME_AS_DESTINATION,
                                                 true );
            }
            if (!inputFile.isReadable()) {
                throw new SVGConverterException(ERROR_CANNOT_READ_SOURCE,
                                                 new Object[]{inputFile.getName()});
            }
            try {
                InputStream in = inputFile.openStream();
                in.close();
            } catch(IOException ioe) {
                throw new SVGConverterException(ERROR_CANNOT_OPEN_SOURCE,
                                                 new Object[] {inputFile.getName(),
                                                               ioe.toString()});
                                                               }
            input = new TranscoderInput(inputFile.getURI());
            if (!isWriteable(outputFile)) {
                throw new SVGConverterException(ERROR_OUTPUT_NOT_WRITEABLE,
                                                 new Object[] {outputFile.getName()});
            }
            try {
                outputStream = new FileOutputStream(outputFile);
            } catch(FileNotFoundException fnfe) {
                throw new SVGConverterException(ERROR_CANNOT_OPEN_OUTPUT_FILE,
                                                 new Object[] {outputFile.getName()});
            }
            output = new TranscoderOutput(outputStream);
        } catch(SVGConverterException e){
            boolean proceed = controller.proceedOnSourceTranscodingFailure
                (inputFile, outputFile, e.getErrorCode());
            if (proceed){
                return;
            } else {
                throw e;
            }
        }
        boolean success = false;
        try {
            transcoder.transcode(input, output);
            success = true;
        } catch(Exception te) {
            te.printStackTrace();
            try {
                outputStream.flush();
                outputStream.close();
            } catch(IOException ioe) {}
            boolean proceed = controller.proceedOnSourceTranscodingFailure
                (inputFile, outputFile, ERROR_WHILE_RASTERIZING_FILE);
            if (!proceed){
                throw new SVGConverterException(ERROR_WHILE_RASTERIZING_FILE,
                                                 new Object[] {outputFile.getName(),
                                                               te.getMessage()});
            }
        }
        try {
            outputStream.flush();
            outputStream.close();
        } catch(IOException ioe) {
            return;
        }
        if (success){
            controller.onSourceTranscodingSuccess(inputFile, outputFile);
        }
    }
    protected String getDestinationFile(String file) {
        int suffixStart;            
        String oldName;             
        String newSuffix = destinationType.getExtension();
        oldName = file;
        suffixStart = oldName.lastIndexOf( '.' );
        String dest = null;
        if (suffixStart != -1) {
            dest = oldName.substring(0, suffixStart) + newSuffix;
        } else {
            dest = oldName + newSuffix;
        }
        return dest;
    }
    protected void createOutputDir(File output)
        throws SVGConverterException {
        File outputDir;             
        boolean success = true;     
        String parentDir = output.getParent();
        if (parentDir != null){
            outputDir = new File(output.getParent());
            if ( ! outputDir.exists() ) {
                success = outputDir.mkdirs();
            } else {
                if ( ! outputDir.isDirectory() ) {
                    success = outputDir.mkdirs();
                }
            }
        }
        if (!success) {
            throw new SVGConverterException(ERROR_UNABLE_TO_CREATE_OUTPUT_DIR);
        }
    }
    protected boolean isWriteable(File file) {
        if (file.exists()) {
            if (!file.canWrite()) {
                return false;
            }
        } else {
            try {
                file.createNewFile();
            } catch(IOException ioe) {
                return false;
            }
        }
        return true;
    }
    public static class SVGFileFilter implements FileFilter {
        public static final String SVG_EXTENSION = ".svg";
        public boolean accept(File file){
            if (file != null && file.getName().toLowerCase().endsWith(SVG_EXTENSION)){
                return true;
            }
            return false;
        }
    }
}

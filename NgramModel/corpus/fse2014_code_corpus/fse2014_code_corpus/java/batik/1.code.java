package org.apache.tools.ant.taskdefs.optional;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.taskdefs.MatchingTask;
import org.apache.tools.ant.types.EnumeratedAttribute;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.util.JAXPUtils;
import org.apache.batik.apps.rasterizer.SVGConverter;
import org.apache.batik.apps.rasterizer.DestinationType;
import org.apache.batik.apps.rasterizer.SVGConverterException;
import org.apache.batik.transcoder.image.JPEGTranscoder;
import org.apache.batik.util.XMLResourceDescriptor;
import org.xml.sax.XMLReader;
import java.awt.geom.Rectangle2D;
import java.awt.Color;
import java.io.File;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;
public class RasterizerTask extends MatchingTask {
    private static final float DEFAULT_QUALITY = 0.99f;
    private static final String JAXP_PARSER = "jaxp";
    protected DestinationType resultType = DestinationType.PNG;
    protected float height = Float.NaN;
    protected float width = Float.NaN;
    protected float maxHeight = Float.NaN;
    protected float maxWidth = Float.NaN;
    protected float quality = Float.NaN;
    protected String area = null;
    protected String background = null;
    protected String mediaType = null;
    protected float dpi = Float.NaN;
    protected String language = null;
    protected String readerClassName = XMLResourceDescriptor.getXMLParserClassName();
    protected File srcFile = null;
    protected File destFile = null;
    protected File srcDir = null;
    protected File destDir = null;
    protected Vector filesets = new Vector();
    protected SVGConverter converter;
    public RasterizerTask() {
        converter = new SVGConverter(new RasterizerTaskSVGConverterController(this));
    }
    public void setResult(ValidImageTypes type) {
        this.resultType = getResultType(type.getValue());
    }
    public void setHeight(float height) {
        this.height = height;
    }
    public void setWidth(float width) {
        this.width = width;
    }
    public void setMaxheight(float height) {
        this.maxHeight = height;
    }
    public void setMaxwidth(float width) {
        this.maxWidth = width;
    }
    public void setQuality(float quality) {
        this.quality = quality;
    }
    public void setArea(String area) {
        this.area = area;
    }
    public void setBg(String bg) {
        this.background = bg;
    }
    public void setMedia(ValidMediaTypes media) {
        this.mediaType = media.getValue();
    }
    public void setDpi(float dpi) {
        this.dpi = dpi;
    }
    public void setLang(String language) {
        this.language = language;
    }
    public void setClassname(String value) {
        this.readerClassName = value;
    }
    public void setSrc(File file) {
        this.srcFile = file;
    }
    public void setDest(File file) {
        this.destFile = file;
    }
    public void setSrcdir(File dir) {
        this.srcDir = dir;
    }
    public void setDestdir(File dir) {
        this.destDir = dir;
    }
    public void addFileset(FileSet set) {
        filesets.addElement(set);
    }
    public void execute() throws BuildException {
        String[] sources;        
        String defaultParser = XMLResourceDescriptor.getXMLParserClassName();
        XMLResourceDescriptor.setXMLParserClassName(getParserClassName(readerClassName));
        try {
            if(this.srcFile != null) {
                if(this.destFile == null) {
                    throw new BuildException("dest attribute is not set.");
                }
            } else {
                if((this.srcDir == null) && (filesets.size() == 0)) {
                    throw new BuildException("No input files! Either srcdir or fileset have to be set.");
                }
                if(this.destDir == null) {
                    throw new BuildException("destdir attribute is not set!");
                }
            }
            setRasterizingParameters();
            sources = getSourceFiles();
            converter.setSources(sources);
            if(this.srcFile != null) {
                converter.setDst(this.destFile);
            } else {
                converter.setDst(this.destDir);
            }
            log("Rasterizing " + sources.length + 
                (sources.length == 1 ? " image " : " images ") + 
                "from SVG to " + this.resultType.toString() + ".");
            try {
                converter.execute();
            } catch(SVGConverterException sce) {
                throw new BuildException(sce.getMessage());
            }
        } finally {
            XMLResourceDescriptor.setXMLParserClassName(defaultParser);
        }
    }
    protected void setRasterizingParameters() 
        throws BuildException {
        if(this.resultType != null) {
            converter.setDestinationType(this.resultType);
        } else {
            throw new BuildException("Unknown value in result parameter.");
        }
        if(!Float.isNaN(this.width)) {
            if(this.width < 0) {
                throw new BuildException("Value of width parameter must positive.");
            }
            converter.setWidth(this.width);
        }
        if(!Float.isNaN(this.height)) {
            if(this.height < 0) {
                throw new BuildException("Value of height parameter must positive.");
            }
            converter.setHeight(this.height);
        }
        if(!Float.isNaN(this.maxWidth)) {
            if(this.maxWidth < 0) {
                throw new BuildException("Value of maxwidth parameter must positive.");
            }
            converter.setMaxWidth(this.maxWidth);
        }
        if(!Float.isNaN(this.maxHeight)) {
            if(this.maxHeight < 0) {
                throw new BuildException("Value of maxheight parameter must positive.");
            }
            converter.setMaxHeight(this.maxHeight);
        }
        if(allowedToSetQuality(resultType)) {
            if(!Float.isNaN(this.quality)) {
                converter.setQuality(getQuality(this.quality));
            } else {
                converter.setQuality(DEFAULT_QUALITY);
            }
        }
        if(this.area != null) {
            converter.setArea(getAreaOfInterest(this.area));
        }
        if(this.background != null) {
            converter.setBackgroundColor(getBackgroundColor(this.background));
        }
        if(this.mediaType != null) {
            converter.setMediaType(this.mediaType);
        }
        if(!Float.isNaN(this.dpi)) {
            if(this.dpi < 0) {
                throw new BuildException("Value of dpi parameter must positive.");
            }
            converter.setPixelUnitToMillimeter(25.4f/this.dpi);
        }
        if(this.language != null) {
            converter.setLanguage(this.language);
        }
    }
    protected String[] getSourceFiles() {
        List inputFiles = new ArrayList(); 
        if(this.srcFile != null) {
            inputFiles.add(this.srcFile.getAbsolutePath());
        } else {
            if(this.srcDir != null) {
                fileset.setDir(this.srcDir);
                DirectoryScanner ds = fileset.getDirectoryScanner(project);
                String[] includedFiles = ds.getIncludedFiles();
                for (int j = 0 ; j < includedFiles.length ; j++) {
                    File newFile = new File(srcDir.getPath(), includedFiles[j]);
                    inputFiles.add(newFile.getAbsolutePath());
                }
            }
            for (int i = 0 ; i < filesets.size() ; i++) {
                FileSet fs = (FileSet) filesets.elementAt(i);
                DirectoryScanner ds = fs.getDirectoryScanner(project);
                String[] includedFiles = ds.getIncludedFiles();
                for (int j = 0 ; j < includedFiles.length ; j++) {
                    File newFile = new File(fs.getDir(project).getPath(), includedFiles[j]);
                    inputFiles.add(newFile.getAbsolutePath());
                }
            }
        }
        return (String[])inputFiles.toArray(new String[0]);
    }
    protected DestinationType getResultType(String type) {
        if(type.equals(DestinationType.PNG_STR)) {
            return DestinationType.PNG;
        } else if(type.equals(DestinationType.JPEG_STR)) {
            return DestinationType.JPEG;
        } else if(type.equals(DestinationType.TIFF_STR)) {
            return DestinationType.TIFF;
        } else if(type.equals(DestinationType.PDF_STR)) {
            return DestinationType.PDF;
        }
        return null;
    }
    protected boolean allowedToSetQuality(DestinationType type) {
        if(!type.toString().equals(DestinationType.JPEG_STR)) {
            return false;
        }
        return true;
    }
    protected float getQuality(float quality) 
        throws BuildException {
        if((quality <= 0) || (quality >= 1)) {
            throw new BuildException("quality parameter value have to be between 0 and 1.");
        }
        return quality;
    }
    protected Rectangle2D getAreaOfInterest(String area) 
        throws BuildException {
        float x;            
        float y;            
        float width;        
        float height;       
        String token;       
        StringTokenizer tokenizer = new StringTokenizer(area, ", \t\n\r\f");
        if(tokenizer.countTokens() != 4) {
            throw new BuildException("There must be four numbers in the area parameter: x, y, width, and height.");
        }
        try {
            x = Float.parseFloat(tokenizer.nextToken());
            y = Float.parseFloat(tokenizer.nextToken());
            width = Float.parseFloat(tokenizer.nextToken());
            height = Float.parseFloat(tokenizer.nextToken());
        } catch(NumberFormatException nfe) {
            throw new BuildException("Invalid area parameter value: " + nfe.toString());
        }
        if((x < 0) || (y < 0) || (width < 0) || (height < 0)) {
            throw new BuildException("Negative values are not allowed in area parameter.");
        }
        return new Rectangle2D.Float(x, y, width, height);
    }
    protected Color getBackgroundColor(String argb) 
        throws BuildException {
        int a;              
        int r;              
        int g;              
        int b;              
        String token;       
        StringTokenizer tokenizer = new StringTokenizer(argb, ", \t\n\r\f");
        try {
            if(tokenizer.countTokens() == 3) {
                a = 255;
            } else if(tokenizer.countTokens() == 4) {
                a = Integer.parseInt(tokenizer.nextToken());
            } else {
                throw new BuildException("There must be either three or four numbers in bg parameter: (alpha,) red, green, and blue.");
            }
            r = Integer.parseInt(tokenizer.nextToken());
            g = Integer.parseInt(tokenizer.nextToken());
            b = Integer.parseInt(tokenizer.nextToken());
        } catch(NumberFormatException nfe) {
            throw new BuildException("Invalid bg parameter value: " + nfe.toString());
        }
        if((a < 0) ||(a > 255) || (r < 0) ||(r > 255) || 
           (g < 0) ||(g > 255) || (b < 0) ||(b > 255)) {
            throw new BuildException("bg parameter value is invalid. Numbers have to be between 0 and 255.");
        }
        return new Color(r, g, b, a);
    }
    private String getParserClassName(final String className) {
        String name = className;
        if ((className == null) || className.equals(JAXP_PARSER)) {
            XMLReader reader = JAXPUtils.getXMLReader();
            name = reader.getClass().getName();
        }
        log("Using class '" + name + "' to parse SVG documents.", Project.MSG_VERBOSE);
        return name;
    }
    public static class ValidImageTypes extends EnumeratedAttribute {
        public String[] getValues() {
            return new String[] 
                {DestinationType.PNG_STR, 
                DestinationType.JPEG_STR, 
                DestinationType.TIFF_STR, 
                DestinationType.PDF_STR};
        }
    }
    public static class ValidMediaTypes extends EnumeratedAttribute {
        public String[] getValues() {
            return new String[] {"all", "handheld", "print", 
                "projection", "screen", "tty", "tv"};
        }
    }
}

package org.apache.batik.test.svg;
import java.io.File;
public abstract class PreconfiguredRenderingTest extends SVGRenderingAccuracyTest {
    public static final String PNG_EXTENSION = ".png";
    public static final String SVG_EXTENSION = ".svg";
    public static final String SVGZ_EXTENSION = ".svgz";
    public static final char PATH_SEPARATOR = '/';
    public static final String[] DEFAULT_VARIATION_PLATFORMS = {
        "java6-linux",
        "java5-osx"
    };
    public void setId(String id){
        super.setId(id);
        setFile(id);
    }
    public void setFile(String id) {
        String svgFile = id;
        String[] dirNfile = breakSVGFile(svgFile);
        setConfig(buildSVGURL(dirNfile[0], dirNfile[1], dirNfile[2]),
                  buildRefImgURL(dirNfile[0], dirNfile[1]));
        String[] variationURLs = buildVariationURLs(dirNfile[0], dirNfile[1]);
        for (int i = 0; i < variationURLs.length; i++) {
            addVariationURL(variationURLs[i]);
        }
        setSaveVariation(new File(buildSaveVariationFile(dirNfile[0], dirNfile[1])));
        setCandidateReference(new File(buildCandidateReferenceFile(dirNfile[0],dirNfile[1])));
    }
    public String getName(){
        return getId();
    }
    protected String buildSVGURL(String svgDir, String svgFile, String svgExt){
        return getSVGURLPrefix() + svgDir + svgFile + svgExt;
    }
    protected abstract String getSVGURLPrefix();
    protected String buildRefImgURL(String svgDir, String svgFile){
        return getRefImagePrefix() + svgDir + getRefImageSuffix() + svgFile + PNG_EXTENSION;
    }
    protected abstract String getRefImagePrefix();
    protected abstract String getRefImageSuffix();
    public String[] buildVariationURLs(String svgDir, String svgFile) {
        String[] platforms = getVariationPlatforms();
        String[] urls = new String[platforms.length + 1];
        urls[0] = getVariationPrefix() + svgDir + getVariationSuffix() + svgFile
                      + PNG_EXTENSION;
        for (int i = 0; i < platforms.length; i++) {
            urls[i + 1] = getVariationPrefix() + svgDir + getVariationSuffix()
                              + svgFile + '_' + platforms[i] + PNG_EXTENSION;
        }
        return urls;
    }
    protected abstract String getVariationPrefix();
    protected abstract String getVariationSuffix();
    protected abstract String[] getVariationPlatforms();
    public String  buildSaveVariationFile(String svgDir, String svgFile){
        return getSaveVariationPrefix() + svgDir + getSaveVariationSuffix() + svgFile + PNG_EXTENSION;
    }
    protected abstract String getSaveVariationPrefix();
    protected abstract String getSaveVariationSuffix();
    public String  buildCandidateReferenceFile(String svgDir, String svgFile){
        return getCandidateReferencePrefix() + svgDir + getCandidateReferenceSuffix() + svgFile + PNG_EXTENSION;
    }
    protected abstract String getCandidateReferencePrefix();
    protected abstract String getCandidateReferenceSuffix();
    protected String[] breakSVGFile(String svgFile){
        if(svgFile == null) {
            throw new IllegalArgumentException(svgFile);
        }
        String [] ret = new String[3];
        if (svgFile.endsWith(SVG_EXTENSION)) {
            ret[2] = SVG_EXTENSION;
        } else if (svgFile.endsWith(SVGZ_EXTENSION)) {
            ret[2] = SVGZ_EXTENSION;
        } else {
            throw new IllegalArgumentException(svgFile);
        }
        svgFile = svgFile.substring(0, svgFile.length()-ret[2].length());
        int fileNameStart = svgFile.lastIndexOf(PATH_SEPARATOR);
        String svgDir = "";
        if(fileNameStart != -1){
            if(svgFile.length() < fileNameStart + 2){
                throw new IllegalArgumentException(svgFile);
            }
            svgDir = svgFile.substring(0, fileNameStart + 1);
            svgFile = svgFile.substring(fileNameStart + 1);
        }
        ret[0] = svgDir;
        ret[1] = svgFile;
        return ret;
    }
}

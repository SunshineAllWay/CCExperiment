package org.apache.batik.test.svg;
import java.io.File;
public class ParametrizedRenderingAccuracyTest
    extends SamplesRenderingTest {
    public static final char PARAMETER_SEPARATOR = '-';
    protected String parameter;
    public ParametrizedRenderingAccuracyTest(){
        super();
    }
    public char getParameterSeparator(){
        return PARAMETER_SEPARATOR;
    }
    public void setId(String id){
        this.id = id;
        String svgFile = id;
        int n = svgFile.lastIndexOf(getParameterSeparator());
        if(n == -1 || n+1 >= svgFile.length() ){
            throw new IllegalArgumentException(id);
        }
        parameter = svgFile.substring(n+1, svgFile.length());
        svgFile = svgFile.substring(0, n);
        String[] dirNfile = breakSVGFile(svgFile);
        setConfig(buildSVGURL(dirNfile[0], dirNfile[1], dirNfile[2]),
                  buildRefImgURL(dirNfile[0], dirNfile[1]));
        String[] variationURLs = buildVariationURLs(dirNfile[0], dirNfile[1]);
        for (int i = 0; i < variationURLs.length; i++) {
            addVariationURL(variationURLs[i]);
        }
        setSaveVariation(new File(buildSaveVariationFile(dirNfile[0], dirNfile[1])));
        setCandidateReference(new File(buildCandidateReferenceFile(dirNfile[0], dirNfile[1])));
    }
    protected String buildRefImgURL(String svgDir, String svgFile){
        return getRefImagePrefix() + svgDir + getRefImageSuffix() + svgFile + parameter + PNG_EXTENSION;
    }
    public String[] buildVariationURLs(String svgDir, String svgFile) {
        String[] platforms = getVariationPlatforms();
        String[] urls = new String[platforms.length + 1];
        urls[0] = getVariationPrefix() + svgDir + getVariationSuffix() + svgFile
                      + parameter + PNG_EXTENSION;
        for (int i = 0; i < platforms.length; i++) {
            urls[i + 1] = getVariationPrefix() + svgDir + getVariationSuffix()
                              + svgFile + parameter + '_' + platforms[i]
                              + PNG_EXTENSION;
        }
        return urls;
    }
    public String  buildSaveVariationFile(String svgDir, String svgFile){
        return getSaveVariationPrefix() + svgDir + getSaveVariationSuffix() + svgFile + parameter + PNG_EXTENSION;
    }
    public String  buildCandidateReferenceFile(String svgDir, String svgFile){
        return getCandidateReferencePrefix() + svgDir + getCandidateReferenceSuffix() + svgFile + parameter + PNG_EXTENSION;
    }
}

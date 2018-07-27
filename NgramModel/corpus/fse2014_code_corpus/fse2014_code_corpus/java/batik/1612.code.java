package org.apache.batik.test.svg;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
public class SVGReferenceRenderingAccuracyTest
    extends ParametrizedRenderingAccuracyTest {
    protected String alias;
    public void setId(String id){
        this.id = id;
        String svgFile = id;
        int n = svgFile.lastIndexOf('#');
        if(n == -1 || n+1 >= svgFile.length() ){
            throw new IllegalArgumentException(id);
        }
        parameter = svgFile.substring(n+1, svgFile.length());
        svgFile = svgFile.substring(0, n);
        n = parameter.lastIndexOf('-');
        if(n == -1 || n+1 >= parameter.length()){
            throw new IllegalArgumentException(id);
        }
        alias = parameter.substring(n+1, parameter.length());
        parameter = parameter.substring(0, n);
        String[] dirNfile = breakSVGFile(svgFile);
        setConfig(buildSVGURL(dirNfile[0], dirNfile[1]),
                  buildRefImgURL(dirNfile[0], dirNfile[1]));
        String[] variationURLs = buildVariationURLs(dirNfile[0], dirNfile[1]);
        for (int i = 0; i < variationURLs.length; i++) {
            addVariationURL(variationURLs[i]);
        }
        setSaveVariation(new File(buildSaveVariationFile(dirNfile[0], dirNfile[1])));
        setCandidateReference(new File(buildCandidateReferenceFile(dirNfile[0], dirNfile[1])));
    }
    protected URL resolveURL(String url){
        String fragment = null;
        String file     = url;
        int n = file.lastIndexOf('#');
        if (n != -1) {
            fragment = file.substring(n); 
            file     = file.substring(0,n);
        }
        File f = (new File(file)).getAbsoluteFile();
        if(f.getParentFile().exists()){
            try{
                if (fragment == null) {
                    return f.toURL(); 
                } else {
                    return new URL(f.toURL(), fragment);
                }
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
    protected String buildSVGURL(String svgDir, String svgFile){
        return getSVGURLPrefix() + svgDir +
            svgFile + SVG_EXTENSION + "#" + parameter;
    }
    protected String buildRefImgURL(String svgDir, String svgFile){
        return getRefImagePrefix() + svgDir + getRefImageSuffix() + svgFile + alias + PNG_EXTENSION;
    }
    public String[] buildVariationURLs(String svgDir, String svgFile) {
        String[] platforms = getVariationPlatforms();
        String[] urls = new String[platforms.length + 1];
        urls[0] = getVariationPrefix() + svgDir + getVariationSuffix() + svgFile
                      + alias + PNG_EXTENSION;
        for (int i = 0; i < platforms.length; i++) {
            urls[i + 1] = getVariationPrefix() + svgDir + getVariationSuffix()
                              + svgFile + alias + '_' + platforms[i]
                              + PNG_EXTENSION;
        }
        return urls;
    }
    public String  buildSaveVariationFile(String svgDir, String svgFile){
        return getSaveVariationPrefix() + svgDir + getSaveVariationSuffix() + svgFile + alias + PNG_EXTENSION;
    }
    public String  buildCandidateReferenceFile(String svgDir, String svgFile){
        return getCandidateReferencePrefix() + svgDir + getCandidateReferenceSuffix() + svgFile + alias + PNG_EXTENSION;
    }
}

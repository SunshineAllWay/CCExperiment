package org.apache.batik.ext.awt.image.codec.png;
import org.apache.batik.test.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.io.*;
public class PNGEncoderTest extends AbstractTest {
    public static final String ERROR_CANNOT_ENCODE_IMAGE
        = "PNGEncoderTest.error.cannot.encode.image";
    public static final String ERROR_CANNOT_DECODE_IMAGE
        = "PNGEncoderTest.error.cannot.decode.image";
    public static final String ERROR_DECODED_DOES_NOT_MATCH_ENCODED
        = "PNGEncoderTest.error.decoded.does.not.match.encoded";
    public TestReport runImpl() throws Exception {
        BufferedImage image = new BufferedImage(100, 75, BufferedImage.TYPE_INT_ARGB);
        Graphics2D ig = image.createGraphics();
        ig.scale(.5, .5);
        ig.setPaint(new Color(128,0,0));
        ig.fillRect(0, 0, 100, 50);
        ig.setPaint(Color.orange);
        ig.fillRect(100, 0, 100, 50);
        ig.setPaint(Color.yellow);
        ig.fillRect(0, 50, 100, 50);
        ig.setPaint(Color.red);
        ig.fillRect(100, 50, 100, 50);
        ig.setPaint(new Color(255, 127, 127));
        ig.fillRect(0, 100, 100, 50);
        ig.setPaint(Color.black);
        ig.draw(new Rectangle2D.Double(0.5, 0.5, 199, 149));
        ig.dispose();
        image = image.getSubimage(50, 0, 50, 25);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        OutputStream os = buildOutputStream(bos);
        PNGEncodeParam params =
            PNGEncodeParam.getDefaultEncodeParam(image);
        PNGImageEncoder pngImageEncoder = new PNGImageEncoder(os, params);
        try{
            pngImageEncoder.encode(image);
            os.close();
        }catch(Exception e){
            return reportException(ERROR_CANNOT_ENCODE_IMAGE, e);
        }
        InputStream is
            = buildInputStream(bos);
        PNGImageDecoder pngImageDecoder
            = new PNGImageDecoder(is, new PNGDecodeParam());
        RenderedImage decodedRenderedImage = null;
        try{
            decodedRenderedImage = pngImageDecoder.decodeAsRenderedImage(0);
        }catch(Exception e){
            return reportException(ERROR_CANNOT_DECODE_IMAGE,
                            e);
        }
        BufferedImage decodedImage = null;
        if(decodedRenderedImage instanceof BufferedImage){
            decodedImage = (BufferedImage)decodedRenderedImage;
        }
        else{
            decodedImage = new BufferedImage(decodedRenderedImage.getWidth(),
                                             decodedRenderedImage.getHeight(),
                                             BufferedImage.TYPE_INT_ARGB);
            ig = decodedImage.createGraphics();
            ig.drawRenderedImage(decodedRenderedImage,
                                 new AffineTransform());
            ig.dispose();
        }
        if( ! checkIdentical(image, decodedImage) ){
            return reportError(ERROR_DECODED_DOES_NOT_MATCH_ENCODED);
        }
        return reportSuccess();
    }
    public OutputStream buildOutputStream(ByteArrayOutputStream bos){
        return bos;
    }
    public InputStream buildInputStream(ByteArrayOutputStream bos){
        return new ByteArrayInputStream(bos.toByteArray());
    }
    public static boolean checkIdentical(BufferedImage imgA,
                                         BufferedImage imgB){
        boolean identical = true;
        if(imgA.getWidth() == imgB.getWidth()
           &&
           imgA.getHeight() == imgB.getHeight()){
            int w = imgA.getWidth();
            int h = imgA.getHeight();
            for(int i=0; i<h; i++){
                for(int j=0; j<w; j++){
                    if(imgA.getRGB(j,i) != imgB.getRGB(j,i)){
                        identical = false;
                        break;
                    }
                }
                if( !identical ){
                    break;
                }
            }
        }
        return identical;
    }
}

package org.apache.batik.transcoder.image;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.FileOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import org.apache.batik.ext.awt.image.spi.ImageTagRegistry;
import org.apache.batik.ext.awt.image.spi.ImageWriter;
import org.apache.batik.ext.awt.image.spi.ImageWriterRegistry;
import org.apache.batik.ext.awt.image.renderable.Filter;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.test.AbstractTest;
import org.apache.batik.test.DefaultTestReport;
import org.apache.batik.test.TestReport;
import org.apache.batik.test.svg.SVGRenderingAccuracyTest;
public abstract class AbstractImageTranscoderTest extends AbstractTest {
    public static final String ERROR_IMAGE_DIFFER =
        "AbstractImageTranscoderTest.error.image.differ";
    public static final String DIFFERENCE_IMAGE =
        "AbstractImageTranscoderTest.error.difference.image";
    public static final String ERROR_TRANSCODING =
        "AbstractImageTranscoderTest.error.transcoder.exception";
    public AbstractImageTranscoderTest() {
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
    DefaultTestReport report;
    public TestReport runImpl() throws Exception {
        report = new DefaultTestReport(this);
        try {
            DiffImageTranscoder transcoder =
                new DiffImageTranscoder(getReferenceImageData());
            Map hints = createTranscodingHints();
            if (hints != null) {
                transcoder.setTranscodingHints(hints);
            }
            TranscoderInput input = createTranscoderInput();
            transcoder.transcode(input, null);
        } catch (Exception ex) {
            report.setErrorCode(ERROR_TRANSCODING);
            report.addDescriptionEntry(ERROR_TRANSCODING, toString(ex));
            ex.printStackTrace();
            report.setPassed(false);
        }
        return report;
    }
    protected abstract TranscoderInput createTranscoderInput();
    protected Map createTranscodingHints() {
        return null;
    }
    protected abstract byte [] getReferenceImageData();
    public static String toString(Exception ex) {
        StringWriter trace = new StringWriter();
        ex.printStackTrace(new PrintWriter(trace));
        return trace.toString();
    }
    static String filename;
    public static byte [] createBufferedImageData(URL url) {
        try {
            filename = url.toString();
            InputStream istream = url.openStream();
            byte [] imgData = null;
            byte [] buf = new byte[1024];
            int length;
            while ((length = istream.read(buf, 0, buf.length)) == buf.length) {
                if (imgData != null) {
                    byte [] imgDataTmp = new byte[imgData.length + length];
                    System.arraycopy
                        (imgData, 0, imgDataTmp, 0, imgData.length);
                    System.arraycopy
                        (buf, 0, imgDataTmp, imgData.length, length);
                    imgData = imgDataTmp;
                } else {
                    imgData = new byte[length];
                    System.arraycopy(buf, 0, imgData, 0, length);
                }
            }
            if (imgData != null) {
                byte [] imgDataTmp = new byte[imgData.length + length];
                System.arraycopy
                    (imgData, 0, imgDataTmp, 0, imgData.length);
                System.arraycopy
                    (buf, 0, imgDataTmp, imgData.length, length);
                imgData = imgDataTmp;
            } else {
                imgData = new byte[length];
                System.arraycopy(buf, 0, imgData, 0, length);
            }
            istream.close();
            return imgData;
        } catch (IOException ex) {
            return null;
        }
    }
    protected class DiffImageTranscoder extends ImageTranscoder {
        protected boolean state;
        protected byte [] refImgData;
        public DiffImageTranscoder(byte [] refImgData) {
            this.refImgData = refImgData;
        }
        public BufferedImage createImage(int w, int h) {
            return new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        }
        public void writeImage(BufferedImage img, TranscoderOutput output)
            throws TranscoderException {
            compareImage(img);
        }
        protected void writeCandidateReference(byte [] imgData) {
            try {
                String s = new File(filename).getName();
                s = "test-references/org/apache/batik/transcoder/image/candidate-reference/"+s;
                System.out.println(s);
                FileOutputStream ostream = new FileOutputStream(s);
                ostream.write(imgData, 0, imgData.length);
                ostream.flush();
                ostream.close();
            } catch (Exception ex) { }
            return;
        }
        protected void writeCandidateVariation(byte [] imgData, byte [] refData)
        {
            writeCandidateReference(imgData);
            try {
                BufferedImage ref = getImage(new ByteArrayInputStream(refData));
                BufferedImage img = getImage(new ByteArrayInputStream(imgData));
                BufferedImage diff =
                    SVGRenderingAccuracyTest.buildDiffImage(ref, img);
                String s = new File(filename).getName();
                s = ("test-references/org/apache/batik/transcoder/image/"+
                     "candidate-variation/"+s);
                ImageWriter writer = ImageWriterRegistry.getInstance()
                    .getWriterFor("image/png");
                OutputStream out = new FileOutputStream(s);
                try {
                    writer.writeImage(diff, out);
                } finally {
                    out.close();
                }
                report.addDescriptionEntry(DIFFERENCE_IMAGE,new File(s));
            } catch (Exception e) { }
        }
        protected void compareImage(BufferedImage img)
            throws TranscoderException {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            TranscoderOutput output = new TranscoderOutput(out);
            PNGTranscoder t = new PNGTranscoder();
            t.writeImage(img, output);
            byte [] imgData = out.toByteArray();
            if (refImgData == null) {
                report.setErrorCode(ERROR_IMAGE_DIFFER);
                report.addDescriptionEntry(ERROR_IMAGE_DIFFER, "");
                report.setPassed(false);
                writeCandidateReference(imgData);
                state = false;
                return;
            }
            if (refImgData.length != imgData.length) {
                report.setErrorCode(ERROR_IMAGE_DIFFER);
                report.addDescriptionEntry(ERROR_IMAGE_DIFFER, "");
                report.setPassed(false);
                writeCandidateVariation(imgData, refImgData);
                return;
            }
            for (int i = 0; i < refImgData.length; ++i) {
                if (refImgData[i] != imgData[i]) {
                    report.setErrorCode(ERROR_IMAGE_DIFFER);
                    report.addDescriptionEntry(ERROR_IMAGE_DIFFER, "");
                    report.setPassed(false);
                    writeCandidateVariation(imgData, refImgData);
                    return;
                }
            }
            state = true;
        }
        public boolean isIdentical() {
            return state;
        }
    }
    protected BufferedImage getImage(InputStream is)
        throws IOException {
        ImageTagRegistry reg = ImageTagRegistry.getRegistry();
        Filter filt = reg.readStream(is);
        if(filt == null)
            throw new IOException("Couldn't read Stream");
        RenderedImage red = filt.createDefaultRendering();
        if(red == null)
            throw new IOException("Couldn't render Stream");
        BufferedImage img = new BufferedImage(red.getWidth(),
                                              red.getHeight(),
                                              BufferedImage.TYPE_INT_ARGB);
        red.copyData(img.getRaster());
        return img;
    }
}

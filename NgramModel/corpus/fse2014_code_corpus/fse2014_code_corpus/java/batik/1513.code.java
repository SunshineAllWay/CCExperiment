package org.apache.batik.ext.awt.image.codec.png;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import org.apache.batik.util.Base64DecodeStream;
import org.apache.batik.util.Base64EncoderStream;
public class Base64PNGEncoderTest extends PNGEncoderTest {
    public OutputStream buildOutputStream(ByteArrayOutputStream bos){
        return new Base64EncoderStream(bos);
    }
    public InputStream buildInputStream(ByteArrayOutputStream bos){
        ByteArrayInputStream bis 
            = new ByteArrayInputStream(bos.toByteArray());
        return new Base64DecodeStream(bis);
    }
}

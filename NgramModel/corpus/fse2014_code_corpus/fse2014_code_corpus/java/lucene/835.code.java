package org.apache.lucene.ant;
import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import junit.framework.TestCase;
public abstract class DocumentTestCase extends TestCase
{
    public DocumentTestCase(String name) {
        super(name);
    }
    protected File getFile(String filename) throws IOException {
        String fullname =
                     this.getClass().getResource(filename).getFile();
        File file = new File(URLDecoder.decode(fullname, "UTF-8"));
        return file;
    }
}

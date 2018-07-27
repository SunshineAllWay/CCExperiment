package jaxp;
import java.io.File;
import org.xml.sax.InputSource;
public class InputData extends InputSource {
    static String dataPrefix = "tests/jaxp/data/";
    private String uri;
    public InputData(String sourceId) throws Exception {
        super(dataPrefix + sourceId);
        uri = dataPrefix + sourceId;
    }
    public String toURIString() throws Exception {
        return new File(uri).toURL().toString();
    }
}

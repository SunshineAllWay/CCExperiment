package org.apache.tools.ant.types.spi;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.tools.ant.ProjectComponent;
import org.apache.tools.ant.BuildException;
public class Service extends ProjectComponent {
    private List providerList = new ArrayList();
    private String type;
    public void setProvider(String className) {
        Provider provider = new Provider();
        provider.setClassName(className);
        providerList.add(provider);
    }
    public void addConfiguredProvider(Provider provider) {
        provider.check();
        providerList.add(provider);
    }
    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }
    public InputStream getAsStream() throws IOException {
        ByteArrayOutputStream arrayOut;
        Writer writer;
        Iterator providerIterator;
        Provider provider;
        arrayOut = new ByteArrayOutputStream();
        writer = new OutputStreamWriter(arrayOut, "UTF-8");
        providerIterator = providerList.iterator();
        while (providerIterator.hasNext()) {
            provider = (Provider) providerIterator.next();
            writer.write(provider.getClassName());
            writer.write("\n");
        }
        writer.close();
        return new ByteArrayInputStream(arrayOut.toByteArray());
    }
    public void check() {
        if (type == null) {
            throw new BuildException(
                "type attribute must be set for service element",
                getLocation());
        }
        if (type.length() == 0) {
            throw new BuildException(
                "Invalid empty type classname", getLocation());
        }
        if (providerList.size() == 0) {
            throw new BuildException(
                "provider attribute or nested provider element must be set!",
                getLocation());
        }
    }
}

package org.apache.xml.serializer;
import java.io.IOException;
import org.w3c.dom.Node;
public interface DOMSerializer
{
    public void serialize(Node node) throws IOException;
}

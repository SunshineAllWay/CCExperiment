package org.apache.xml.serialize;
import java.io.IOException;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
public interface DOMSerializer
{
    public void serialize( Element elem )
        throws IOException;
    public void serialize( Document doc )
        throws IOException;
    public void serialize( DocumentFragment frag )
        throws IOException;
}

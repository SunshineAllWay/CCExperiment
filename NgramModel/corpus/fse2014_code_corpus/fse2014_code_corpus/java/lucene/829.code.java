package org.apache.lucene.ant;
import org.apache.lucene.document.Document;
import java.io.File;
public interface DocumentHandler {
    Document getDocument(File file)
            throws DocumentHandlerException;
}

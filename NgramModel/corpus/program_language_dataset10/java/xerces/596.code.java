package org.apache.xerces.stax.events;
import java.io.IOException;
import java.io.Writer;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.ProcessingInstruction;
public final class ProcessingInstructionImpl extends XMLEventImpl implements
        ProcessingInstruction {
    private final String fTarget;
    private final String fData;
    public ProcessingInstructionImpl(final String target, final String data, final Location location) {
        super(PROCESSING_INSTRUCTION, location);
        fTarget = target != null ? target : "";
        fData = data;
    }
    public String getTarget() {
        return fTarget;
    }
    public String getData() {
        return fData;
    }
    public void writeAsEncodedUnicode(Writer writer) throws XMLStreamException {
        try {
            writer.write("<?");
            writer.write(fTarget);
            if (fData != null && fData.length() > 0) {
                writer.write(' ');
                writer.write(fData);
            }
            writer.write("?>");
        }
        catch (IOException ioe) {
            throw new XMLStreamException(ioe);
        }
    }
}

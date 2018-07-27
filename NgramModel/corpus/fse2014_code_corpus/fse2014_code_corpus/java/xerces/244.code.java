package org.apache.xerces.dom;
import org.w3c.dom.Node;
import org.w3c.dom.ProcessingInstruction;
public class ProcessingInstructionImpl
    extends CharacterDataImpl
    implements ProcessingInstruction {
    static final long serialVersionUID = 7554435174099981510L;
    protected String target;
    public ProcessingInstructionImpl(CoreDocumentImpl ownerDoc,
                                     String target, String data) {
        super(ownerDoc, data);
        this.target = target;
    }
    public short getNodeType() {
        return Node.PROCESSING_INSTRUCTION_NODE;
    }
    public String getNodeName() {
        if (needsSyncData()) {
            synchronizeData();
        }
        return target;
    }
    public String getTarget() {
        if (needsSyncData()) {
            synchronizeData();
        }
        return target;
    } 
    public String getBaseURI() {
        if (needsSyncData()) {
            synchronizeData();
        }
        return ownerNode.getBaseURI();
    }
} 

package org.apache.batik.dom.events;
import org.w3c.dom.Node;
import org.w3c.dom.events.MutationNameEvent;
public class DOMMutationNameEvent
        extends DOMMutationEvent
        implements MutationNameEvent {
    protected String prevNamespaceURI;
    protected String prevNodeName;
    public void initMutationNameEvent(String typeArg,
                                      boolean canBubbleArg,
                                      boolean cancelableArg,
                                      Node relatedNodeArg,
                                      String prevNamespaceURIArg,
                                      String prevNodeNameArg) {
        initMutationEvent(typeArg,
                          canBubbleArg,
                          cancelableArg,
                          relatedNodeArg,
                          null,
                          null,
                          null,
                          (short) 0);
        this.prevNamespaceURI = prevNamespaceURIArg;
        this.prevNodeName     = prevNodeNameArg;
    }
    public void initMutationNameEventNS(String namespaceURI,
                                        String typeArg,
                                        boolean canBubbleArg,
                                        boolean cancelableArg,
                                        Node relatedNodeArg,
                                        String prevNamespaceURIArg,
                                        String prevNodeNameArg) {
        initMutationEventNS(namespaceURI,
                            typeArg,
                            canBubbleArg,
                            cancelableArg,
                            relatedNodeArg,
                            null,
                            null,
                            null,
                            (short) 0);
        this.prevNamespaceURI = prevNamespaceURIArg;
        this.prevNodeName     = prevNodeNameArg;
    }
    public String getPrevNamespaceURI() {
        return prevNamespaceURI;
    }
    public String getPrevNodeName() {
        return prevNodeName;
    }
}

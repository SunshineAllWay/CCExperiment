package org.w3c.dom.events;
import org.w3c.dom.Node;
public interface MutationNameEvent extends MutationEvent {
    public String getPrevNamespaceURI();
    public String getPrevNodeName();
    public void initMutationNameEvent(String typeArg, 
                                      boolean canBubbleArg, 
                                      boolean cancelableArg, 
                                      Node relatedNodeArg, 
                                      String prevNamespaceURIArg, 
                                      String prevNodeNameArg);
    public void initMutationNameEventNS(String namespaceURI, 
                                        String typeArg, 
                                        boolean canBubbleArg, 
                                        boolean cancelableArg, 
                                        Node relatedNodeArg, 
                                        String prevNamespaceURIArg, 
                                        String prevNodeNameArg);
}

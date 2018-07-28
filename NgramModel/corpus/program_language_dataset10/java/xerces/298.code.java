package org.apache.xerces.impl.dtd;
import org.apache.xerces.util.SymbolTable;
import org.apache.xerces.xni.Augmentations;
import org.apache.xerces.xni.XMLDTDContentModelHandler;
import org.apache.xerces.xni.XNIException;
final class BalancedDTDGrammar extends DTDGrammar {
    private boolean fMixed;
    private int fDepth = 0;
    private short [] fOpStack = null;
    private int [][] fGroupIndexStack;
    private int [] fGroupIndexStackSizes;
    public BalancedDTDGrammar(SymbolTable symbolTable, XMLDTDDescription desc) {
        super(symbolTable, desc);
    } 
    public final void startContentModel(String elementName, Augmentations augs)
        throws XNIException {
        fDepth = 0;
        initializeContentModelStacks();
        super.startContentModel(elementName, augs);
    } 
    public final void startGroup(Augmentations augs) throws XNIException {
        ++fDepth;
        initializeContentModelStacks();
        fMixed = false;
    } 
    public final void pcdata(Augmentations augs) throws XNIException {
        fMixed = true;
    } 
    public final void element(String elementName, Augmentations augs) throws XNIException {
        addToCurrentGroup(addUniqueLeafNode(elementName));
    } 
    public final void separator(short separator, Augmentations augs) throws XNIException {
        if (separator == XMLDTDContentModelHandler.SEPARATOR_CHOICE) {
            fOpStack[fDepth] = XMLContentSpec.CONTENTSPECNODE_CHOICE;
        }
        else if (separator == XMLDTDContentModelHandler.SEPARATOR_SEQUENCE) {
            fOpStack[fDepth] = XMLContentSpec.CONTENTSPECNODE_SEQ;
        }
    } 
    public final void occurrence(short occurrence, Augmentations augs) throws XNIException {
        if (!fMixed) {
            int currentIndex = fGroupIndexStackSizes[fDepth] - 1;
            if (occurrence == XMLDTDContentModelHandler.OCCURS_ZERO_OR_ONE) {
                fGroupIndexStack[fDepth][currentIndex] = addContentSpecNode(XMLContentSpec.CONTENTSPECNODE_ZERO_OR_ONE, fGroupIndexStack[fDepth][currentIndex], -1);
            } 
            else if ( occurrence == XMLDTDContentModelHandler.OCCURS_ZERO_OR_MORE) {
                fGroupIndexStack[fDepth][currentIndex] = addContentSpecNode(XMLContentSpec.CONTENTSPECNODE_ZERO_OR_MORE, fGroupIndexStack[fDepth][currentIndex], -1);
            } 
            else if ( occurrence == XMLDTDContentModelHandler.OCCURS_ONE_OR_MORE) {
                fGroupIndexStack[fDepth][currentIndex] = addContentSpecNode(XMLContentSpec.CONTENTSPECNODE_ONE_OR_MORE, fGroupIndexStack[fDepth][currentIndex], -1);
            }
        }
    } 
    public final void endGroup(Augmentations augs) throws XNIException {
        final int length = fGroupIndexStackSizes[fDepth];
        final int group = length > 0 ? addContentSpecNodes(0, length - 1) : addUniqueLeafNode(null);
        --fDepth;
        addToCurrentGroup(group);
    } 
    public final void endDTD(Augmentations augs) throws XNIException {
        super.endDTD(augs);
        fOpStack = null;
        fGroupIndexStack = null;
        fGroupIndexStackSizes = null;
    } 
    protected final void addContentSpecToElement(XMLElementDecl elementDecl) {
        int contentSpec = fGroupIndexStackSizes[0] > 0 ? fGroupIndexStack[0][0] : -1;
        setContentSpecIndex(fCurrentElementIndex, contentSpec);
    }
    private int addContentSpecNodes(int begin, int end) {
        if (begin == end) {
            return fGroupIndexStack[fDepth][begin];
        }
        final int middle = (begin + end) >>> 1;
        return addContentSpecNode(fOpStack[fDepth], 
                addContentSpecNodes(begin, middle), 
                addContentSpecNodes(middle + 1, end));
    } 
    private void initializeContentModelStacks() {
        if (fOpStack == null) {
            fOpStack = new short[8];
            fGroupIndexStack = new int [8][];
            fGroupIndexStackSizes = new int [8];
        }
        else if (fDepth == fOpStack.length) {
            short [] newOpStack = new short[fDepth * 2];
            System.arraycopy(fOpStack, 0, newOpStack, 0, fDepth);
            fOpStack = newOpStack;
            int [][] newGroupIndexStack = new int[fDepth * 2][];
            System.arraycopy(fGroupIndexStack, 0, newGroupIndexStack, 0, fDepth);
            fGroupIndexStack = newGroupIndexStack;
            int [] newGroupIndexStackLengths = new int[fDepth * 2];
            System.arraycopy(fGroupIndexStackSizes, 0, newGroupIndexStackLengths, 0, fDepth);
            fGroupIndexStackSizes = newGroupIndexStackLengths;
        }
        fOpStack[fDepth] = -1;
        fGroupIndexStackSizes[fDepth] = 0;
    } 
    private void addToCurrentGroup(int contentSpec) {
        int [] currentGroup = fGroupIndexStack[fDepth];
        int length = fGroupIndexStackSizes[fDepth]++;
        if (currentGroup == null) {
            currentGroup = new int[8];
            fGroupIndexStack[fDepth] = currentGroup;
        }
        else if (length == currentGroup.length) {
            int [] newGroup = new int[currentGroup.length * 2];
            System.arraycopy(currentGroup, 0, newGroup, 0, currentGroup.length);
            currentGroup = newGroup;
            fGroupIndexStack[fDepth] = currentGroup;
        }
        currentGroup[length] = contentSpec;
    } 
} 

package org.apache.xerces.impl.xs.identity;
import org.apache.xerces.impl.xpath.XPathException;
import org.apache.xerces.util.SymbolTable;
import org.apache.xerces.util.XMLChar;
import org.apache.xerces.xni.NamespaceContext;
import org.apache.xerces.xni.QName;
import org.apache.xerces.xni.XMLAttributes;
import org.apache.xerces.xs.ShortList;
import org.apache.xerces.xs.XSTypeDefinition;
public class Selector {
    protected final Selector.XPath fXPath;
    protected final IdentityConstraint fIdentityConstraint;
    protected IdentityConstraint fIDConstraint;
    public Selector(Selector.XPath xpath, 
                    IdentityConstraint identityConstraint) {
        fXPath = xpath;
        fIdentityConstraint = identityConstraint;
    } 
    public org.apache.xerces.impl.xpath.XPath getXPath() {
        return fXPath;
    } 
    public IdentityConstraint getIDConstraint() {
        return fIdentityConstraint;
    } 
    public XPathMatcher createMatcher(FieldActivator activator, int initialDepth) {
        return new Selector.Matcher(fXPath, activator, initialDepth);
    } 
    public String toString() {
        return fXPath.toString();
    } 
    public static class XPath
    extends org.apache.xerces.impl.xpath.XPath {
        public XPath(String xpath, SymbolTable symbolTable, 
                     NamespaceContext context) throws XPathException {
            super(normalize(xpath), symbolTable, context);
            for (int i=0;i<fLocationPaths.length;i++) {
                org.apache.xerces.impl.xpath.XPath.Axis axis =
                fLocationPaths[i].steps[fLocationPaths[i].steps.length-1].axis;
                if (axis.type == XPath.Axis.ATTRIBUTE) {
                    throw new XPathException("c-selector-xpath");
                }
            }
        } 
        private static String normalize(String xpath) {
            StringBuffer modifiedXPath = new StringBuffer(xpath.length()+5);
            int unionIndex = -1;
            do {
                if(!(XMLChar.trim(xpath).startsWith("/") || XMLChar.trim(xpath).startsWith("."))) {
                    modifiedXPath.append("./"); 
                }
                unionIndex = xpath.indexOf('|');
                if(unionIndex == -1) {
                    modifiedXPath.append(xpath);
                    break;
                }
                modifiedXPath.append(xpath.substring(0,unionIndex+1));
                xpath = xpath.substring(unionIndex+1, xpath.length());
            } while(true);
            return modifiedXPath.toString();
        }
    } 
    public class Matcher
    extends XPathMatcher {
        protected final FieldActivator fFieldActivator;
        protected final int fInitialDepth;
        protected int fElementDepth;
        protected int fMatchedDepth;
        public Matcher(Selector.XPath xpath, FieldActivator activator,
                int initialDepth) {
            super(xpath);
            fFieldActivator = activator;
            fInitialDepth = initialDepth;
        } 
        public void startDocumentFragment(){
            super.startDocumentFragment();
            fElementDepth = 0;
            fMatchedDepth = -1;
        } 
        public void startElement(QName element, XMLAttributes attributes) {
            super.startElement(element, attributes);
            fElementDepth++;
            if (isMatched()) {
                fMatchedDepth = fElementDepth;
                fFieldActivator.startValueScopeFor(fIdentityConstraint, fInitialDepth);
                int count = fIdentityConstraint.getFieldCount();
                for (int i = 0; i < count; i++) {
                    Field field = fIdentityConstraint.getFieldAt(i);
                    XPathMatcher matcher = fFieldActivator.activateField(field, fInitialDepth);
                    matcher.startElement(element, attributes);
                }
            }
        } 
        public void endElement(QName element, XSTypeDefinition type, boolean nillable, Object actualValue, short valueType, ShortList itemValueType) {
            super.endElement(element, type, nillable, actualValue, valueType, itemValueType);
            if (fElementDepth-- == fMatchedDepth) {
                fMatchedDepth = -1;
                fFieldActivator.endValueScopeFor(fIdentityConstraint, fInitialDepth);
            }
        }
        public IdentityConstraint getIdentityConstraint() {
            return fIdentityConstraint;
        } 
        public int getInitialDepth() {
            return fInitialDepth;
        } 
    } 
} 

package org.apache.xerces.impl.xs.identity;
import org.apache.xerces.impl.xpath.XPathException;
import org.apache.xerces.impl.xs.util.ShortListImpl;
import org.apache.xerces.util.SymbolTable;
import org.apache.xerces.util.XMLChar;
import org.apache.xerces.xni.NamespaceContext;
import org.apache.xerces.xs.ShortList;
import org.apache.xerces.xs.XSComplexTypeDefinition;
import org.apache.xerces.xs.XSConstants;
import org.apache.xerces.xs.XSTypeDefinition;
public class Field {
    protected final Field.XPath fXPath;
    protected final IdentityConstraint fIdentityConstraint;
    public Field(Field.XPath xpath, 
                 IdentityConstraint identityConstraint) {
        fXPath = xpath;
        fIdentityConstraint = identityConstraint;
    } 
    public org.apache.xerces.impl.xpath.XPath getXPath() {
        return fXPath;
    } 
    public IdentityConstraint getIdentityConstraint() {
        return fIdentityConstraint;
    } 
    public XPathMatcher createMatcher(ValueStore store) {
        return new Field.Matcher(fXPath, store);
    } 
    public String toString() {
        return fXPath.toString();
    } 
    public static class XPath
        extends org.apache.xerces.impl.xpath.XPath {
        public XPath(String xpath, 
                     SymbolTable symbolTable,
                     NamespaceContext context) throws XPathException {
            super(fixupXPath(xpath), symbolTable, context);
            for (int i=0;i<fLocationPaths.length;i++) {
                for(int j=0; j<fLocationPaths[i].steps.length; j++) {
                    org.apache.xerces.impl.xpath.XPath.Axis axis =
                        fLocationPaths[i].steps[j].axis;
                    if (axis.type == XPath.Axis.ATTRIBUTE &&
                            (j < fLocationPaths[i].steps.length-1)) {
                        throw new XPathException("c-fields-xpaths");
                    }
                }
            }
        } 
        private static String fixupXPath(String xpath) {
            final int end = xpath.length();
            int offset = 0;
            boolean whitespace = true;
            char c;
            for (; offset < end; ++offset) {
                c = xpath.charAt(offset);
                if (whitespace) {
                    if (!XMLChar.isSpace(c)) {
                        if (c == '.' || c == '/') {
                            whitespace = false;
                        }
                        else if (c != '|') {
                            return fixupXPath2(xpath, offset, end);
                        }
                    }
                }
                else if (c == '|') {
                    whitespace = true;
                }
            }
            return xpath;
        } 
        private static String fixupXPath2(String xpath, int offset, final int end) {
            StringBuffer buffer = new StringBuffer(end + 2);
            for (int i = 0; i < offset; ++i) {
                buffer.append(xpath.charAt(i));
            }
            buffer.append("./");
            boolean whitespace = false;
            char c;
            for (; offset < end; ++offset) {
                c = xpath.charAt(offset);
                if (whitespace) {
                    if (!XMLChar.isSpace(c)) {
                        if (c == '.' || c == '/') {
                            whitespace = false;
                        }
                        else if (c != '|') {
                            buffer.append("./");
                            whitespace = false;
                        }
                    }
                }
                else if (c == '|') {
                    whitespace = true;
                }
                buffer.append(c);
            }
            return buffer.toString();
        } 
    } 
    protected class Matcher
        extends XPathMatcher {
        protected final ValueStore fStore;
        protected boolean fMayMatch = true;
        public Matcher(Field.XPath xpath, ValueStore store) {
            super(xpath);
            fStore = store;
        } 
        protected void matched(Object actualValue, short valueType, ShortList itemValueType, boolean isNil) {
            super.matched(actualValue, valueType, itemValueType, isNil);
            if(isNil && (fIdentityConstraint.getCategory() == IdentityConstraint.IC_KEY)) {
                String code = "KeyMatchesNillable";
                fStore.reportError(code, 
                    new Object[]{fIdentityConstraint.getElementName(), fIdentityConstraint.getIdentityConstraintName()});
            }
            fStore.addValue(Field.this, fMayMatch, actualValue, convertToPrimitiveKind(valueType), convertToPrimitiveKind(itemValueType));
            fMayMatch = false;
        } 
        private short convertToPrimitiveKind(short valueType) {
            if (valueType <= XSConstants.NOTATION_DT) {
                return valueType;
            }
            if (valueType <= XSConstants.ENTITY_DT) {
                return XSConstants.STRING_DT;
            }
            if (valueType <= XSConstants.POSITIVEINTEGER_DT) {
                return XSConstants.DECIMAL_DT;
            }
            return valueType;
        }
        private ShortList convertToPrimitiveKind(ShortList itemValueType) {
            if (itemValueType != null) {
                int i;
                final int length = itemValueType.getLength();
                for (i = 0; i < length; ++i) {
                    short type = itemValueType.item(i);
                    if (type != convertToPrimitiveKind(type)) {
                        break;
                    }
                }
                if (i != length) {
                    final short [] arr = new short[length];
                    for (int j = 0; j < i; ++j) {
                        arr[j] = itemValueType.item(j);
                    }
                    for(; i < length; ++i) {
                        arr[i] = convertToPrimitiveKind(itemValueType.item(i));
                    }
                    return new ShortListImpl(arr, arr.length);
                }
            }
            return itemValueType;
        }
        protected void handleContent(XSTypeDefinition type, boolean nillable, Object actualValue, short valueType, ShortList itemValueType) {
            if (type == null || 
               type.getTypeCategory() == XSTypeDefinition.COMPLEX_TYPE &&
               ((XSComplexTypeDefinition) type).getContentType()
                != XSComplexTypeDefinition.CONTENTTYPE_SIMPLE) {
                    fStore.reportError( "cvc-id.3", new Object[] {
                            fIdentityConstraint.getName(),
                            fIdentityConstraint.getElementName()});
            }
            fMatchedString = actualValue;
            matched(fMatchedString, valueType, itemValueType, nillable);
        } 
    } 
} 

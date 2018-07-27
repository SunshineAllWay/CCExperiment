package org.apache.xerces.impl.xs.identity;
import org.apache.xerces.impl.xs.XSAnnotationImpl;
import org.apache.xerces.impl.xs.util.StringListImpl;
import org.apache.xerces.impl.xs.util.XSObjectListImpl;
import org.apache.xerces.xs.StringList;
import org.apache.xerces.xs.XSConstants;
import org.apache.xerces.xs.XSIDCDefinition;
import org.apache.xerces.xs.XSNamespaceItem;
import org.apache.xerces.xs.XSObjectList;
public abstract class IdentityConstraint implements XSIDCDefinition {
    protected short type;
    protected final String fNamespace;
    protected final String fIdentityConstraintName;
    protected final String fElementName;
    protected Selector fSelector;
    protected int fFieldCount;
    protected Field[] fFields;
    protected XSAnnotationImpl [] fAnnotations = null;
    protected int fNumAnnotations;
    protected IdentityConstraint(String namespace, String identityConstraintName, String elemName) {
        fNamespace = namespace;
        fIdentityConstraintName = identityConstraintName;
        fElementName = elemName;
    } 
    public String getIdentityConstraintName() {
        return fIdentityConstraintName;
    } 
    public void setSelector(Selector selector) {
        fSelector = selector;
    } 
    public Selector getSelector() {
        return fSelector;
    } 
    public void addField(Field field) {
        if (fFields == null)
            fFields = new Field[4];
        else if (fFieldCount == fFields.length)
            fFields = resize(fFields, fFieldCount*2);
        fFields[fFieldCount++] = field;
    } 
    public int getFieldCount() {
        return fFieldCount;
    } 
    public Field getFieldAt(int index) {
        return fFields[index];
    } 
    public String getElementName () {
        return fElementName;
    } 
    public String toString() {
        String s = super.toString();
        int index1 = s.lastIndexOf('$');
        if (index1 != -1) {
            return s.substring(index1 + 1);
        }
        int index2 = s.lastIndexOf('.');
        if (index2 != -1) {
            return s.substring(index2 + 1);
        }
        return s;
    } 
    public boolean equals(IdentityConstraint id) {
        boolean areEqual = fIdentityConstraintName.equals(id.fIdentityConstraintName);
        if(!areEqual) return false;
        areEqual = fSelector.toString().equals(id.fSelector.toString());
        if(!areEqual) return false;
        areEqual = (fFieldCount == id.fFieldCount);
        if(!areEqual) return false;
        for(int i=0; i<fFieldCount; i++)
            if(!fFields[i].toString().equals(id.fFields[i].toString())) return false;
        return true;
    } 
    static final Field[] resize(Field[] oldArray, int newSize) {
        Field[] newArray = new Field[newSize];
        System.arraycopy(oldArray, 0, newArray, 0, oldArray.length);
        return newArray;
    }
    public short getType() {
        return XSConstants.IDENTITY_CONSTRAINT;
    }
    public String getName() {
        return fIdentityConstraintName;
    }
    public String getNamespace() {
        return fNamespace;
    }
    public short getCategory() {
        return type;
    }
    public String getSelectorStr() {
        return (fSelector != null) ? fSelector.toString() : null;
    }
    public StringList getFieldStrs() {
        String[] strs = new String[fFieldCount];
        for (int i = 0; i < fFieldCount; i++)
            strs[i] = fFields[i].toString();
        return new StringListImpl(strs, fFieldCount);
    }
    public XSIDCDefinition getRefKey() {
        return null;
    }
    public XSObjectList getAnnotations() {
        return new XSObjectListImpl(fAnnotations, fNumAnnotations);
    }
	public XSNamespaceItem getNamespaceItem() {
		return null;
	}
    public void addAnnotation(XSAnnotationImpl annotation) {
        if(annotation == null)
            return;
        if(fAnnotations == null) {
            fAnnotations = new XSAnnotationImpl[2];
        } else if(fNumAnnotations == fAnnotations.length) {
            XSAnnotationImpl[] newArray = new XSAnnotationImpl[fNumAnnotations << 1];
            System.arraycopy(fAnnotations, 0, newArray, 0, fNumAnnotations);
            fAnnotations = newArray;
        }
        fAnnotations[fNumAnnotations++] = annotation;
    }
} 

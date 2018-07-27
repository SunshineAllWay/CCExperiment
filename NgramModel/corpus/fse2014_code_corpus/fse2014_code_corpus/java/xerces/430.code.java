package org.apache.xerces.impl.xs;
import org.apache.xerces.impl.dv.ValidatedInfo;
import org.apache.xerces.impl.xs.util.XSObjectListImpl;
import org.apache.xerces.xs.XSAnnotation;
import org.apache.xerces.xs.XSAttributeGroupDefinition;
import org.apache.xerces.xs.XSAttributeUse;
import org.apache.xerces.xs.XSConstants;
import org.apache.xerces.xs.XSNamespaceItem;
import org.apache.xerces.xs.XSObjectList;
import org.apache.xerces.xs.XSWildcard;
public class XSAttributeGroupDecl implements XSAttributeGroupDefinition {
    public String fName = null;
    public String fTargetNamespace = null;
    int fAttrUseNum = 0;
    private static final int INITIAL_SIZE = 5;
    XSAttributeUseImpl[] fAttributeUses = new XSAttributeUseImpl[INITIAL_SIZE];
    public XSWildcardDecl fAttributeWC = null;
    public String fIDAttrName = null;
    public XSObjectList fAnnotations;
    protected XSObjectListImpl fAttrUses = null;
    private XSNamespaceItem fNamespaceItem = null;
    public String addAttributeUse(XSAttributeUseImpl attrUse) {
        if (attrUse.fUse != SchemaSymbols.USE_PROHIBITED) {
            if (attrUse.fAttrDecl.fType.isIDType()) {
                if (fIDAttrName == null)
                    fIDAttrName = attrUse.fAttrDecl.fName;
                else
                    return fIDAttrName;
            }
        }
        if (fAttrUseNum == fAttributeUses.length) {
            fAttributeUses = resize(fAttributeUses, fAttrUseNum*2);
        }
        fAttributeUses[fAttrUseNum++] = attrUse;
        return null;
    }
    public void replaceAttributeUse(XSAttributeUse oldUse, XSAttributeUseImpl newUse) {
        for (int i=0; i<fAttrUseNum; i++) {
            if (fAttributeUses[i] == oldUse) {
                fAttributeUses[i] = newUse;
            }
        }
    }
    public XSAttributeUse getAttributeUse(String namespace, String name) {
        for (int i=0; i<fAttrUseNum; i++) {
            if ( (fAttributeUses[i].fAttrDecl.fTargetNamespace == namespace) &&
                 (fAttributeUses[i].fAttrDecl.fName == name) )
                return fAttributeUses[i];
        }
        return null;
    }
    public XSAttributeUse getAttributeUseNoProhibited(String namespace, String name) {
        for (int i=0; i<fAttrUseNum; i++) {
            if ( (fAttributeUses[i].fAttrDecl.fTargetNamespace == namespace) &&
                 (fAttributeUses[i].fAttrDecl.fName == name) &&
                 (fAttributeUses[i].fUse != SchemaSymbols.USE_PROHIBITED))
                return fAttributeUses[i];
        }
        return null;
    }
    public void removeProhibitedAttrs() {
        if (fAttrUseNum == 0) return;
        int count = 0;
        XSAttributeUseImpl[] uses = new XSAttributeUseImpl[fAttrUseNum];
        for (int i = 0; i < fAttrUseNum; i++) {
            if (fAttributeUses[i].fUse != SchemaSymbols.USE_PROHIBITED) {
                uses[count++] = fAttributeUses[i];
            }
        }
        fAttributeUses = uses;
        fAttrUseNum = count;
    }
    public Object[] validRestrictionOf(String typeName, XSAttributeGroupDecl baseGroup) {
        Object[] errorArgs = null;
        XSAttributeUseImpl attrUse = null;
        XSAttributeDecl attrDecl = null;
        XSAttributeUseImpl baseAttrUse = null;
        XSAttributeDecl baseAttrDecl = null;
        for (int i=0; i<fAttrUseNum; i++) {
            attrUse = fAttributeUses[i];
            attrDecl = attrUse.fAttrDecl;
            baseAttrUse = (XSAttributeUseImpl)baseGroup.getAttributeUse(attrDecl.fTargetNamespace,attrDecl.fName);
            if (baseAttrUse != null) {
                if (baseAttrUse.getRequired() && !attrUse.getRequired()) {
                    errorArgs = new Object[]{typeName, attrDecl.fName,
                    	                     attrUse.fUse == SchemaSymbols.USE_OPTIONAL ? SchemaSymbols.ATTVAL_OPTIONAL : SchemaSymbols.ATTVAL_PROHIBITED,
                                             "derivation-ok-restriction.2.1.1"};
                    return errorArgs;
                }
                if (attrUse.fUse == SchemaSymbols.USE_PROHIBITED) {
                    continue;
                }
                baseAttrDecl = baseAttrUse.fAttrDecl;
                if (! XSConstraints.checkSimpleDerivationOk(attrDecl.fType,
                                                            baseAttrDecl.fType,
                                                            baseAttrDecl.fType.getFinal()) ) {
					errorArgs = new Object[]{typeName, attrDecl.fName, attrDecl.fType.getName(),
						                     baseAttrDecl.fType.getName(), "derivation-ok-restriction.2.1.2"};
					return errorArgs;
                }
                int baseConsType=baseAttrUse.fConstraintType!=XSConstants.VC_NONE?
                                 baseAttrUse.fConstraintType:baseAttrDecl.getConstraintType();
                int thisConstType = attrUse.fConstraintType!=XSConstants.VC_NONE?
                                    attrUse.fConstraintType:attrDecl.getConstraintType();
                if (baseConsType == XSConstants.VC_FIXED) {
                    if (thisConstType != XSConstants.VC_FIXED) {
						errorArgs = new Object[]{typeName, attrDecl.fName,
												 "derivation-ok-restriction.2.1.3.a"};
						return errorArgs;
                    } else {
                        ValidatedInfo baseFixedValue=(baseAttrUse.fDefault!=null ?
                                                      baseAttrUse.fDefault: baseAttrDecl.fDefault);
                        ValidatedInfo thisFixedValue=(attrUse.fDefault!=null ?
                                                      attrUse.fDefault: attrDecl.fDefault);
                        if (!baseFixedValue.actualValue.equals(thisFixedValue.actualValue)) {
							errorArgs = new Object[]{typeName, attrDecl.fName, thisFixedValue.stringValue(),
													 baseFixedValue.stringValue(), "derivation-ok-restriction.2.1.3.b"};
							return errorArgs;
                        }
                    }
                }
            } else {
                if (baseGroup.fAttributeWC == null) {
					errorArgs = new Object[]{typeName, attrDecl.fName,
											 "derivation-ok-restriction.2.2.a"};
					return errorArgs;
                }
                else if (!baseGroup.fAttributeWC.allowNamespace(attrDecl.fTargetNamespace)) {
					errorArgs = new Object[]{typeName, attrDecl.fName,
                                             attrDecl.fTargetNamespace==null?"":attrDecl.fTargetNamespace,
											 "derivation-ok-restriction.2.2.b"};
					return errorArgs;
                }
            }
        }
        for (int i=0; i<baseGroup.fAttrUseNum; i++) {
            baseAttrUse = baseGroup.fAttributeUses[i];
            if (baseAttrUse.fUse == SchemaSymbols.USE_REQUIRED) {
                baseAttrDecl = baseAttrUse.fAttrDecl;
                if (getAttributeUse(baseAttrDecl.fTargetNamespace,baseAttrDecl.fName) == null) {
					errorArgs = new Object[]{typeName, baseAttrUse.fAttrDecl.fName,
											 "derivation-ok-restriction.3"};
					return errorArgs;
                }
            }
        }
        if (fAttributeWC != null) {
            if (baseGroup.fAttributeWC == null) {
				errorArgs = new Object[]{typeName, "derivation-ok-restriction.4.1"};
				return errorArgs;
            }
            if (! fAttributeWC.isSubsetOf(baseGroup.fAttributeWC)) {
				errorArgs = new Object[]{typeName, "derivation-ok-restriction.4.2"};
				return errorArgs;
            }
            if (fAttributeWC.weakerProcessContents(baseGroup.fAttributeWC)) {
				errorArgs = new Object[]{typeName,
										 fAttributeWC.getProcessContentsAsString(),
										 baseGroup.fAttributeWC.getProcessContentsAsString(),
										 "derivation-ok-restriction.4.3"};
				return errorArgs;
            }
        }
        return null;
    }
    static final XSAttributeUseImpl[] resize(XSAttributeUseImpl[] oldArray, int newSize) {
        XSAttributeUseImpl[] newArray = new XSAttributeUseImpl[newSize];
        System.arraycopy(oldArray, 0, newArray, 0, Math.min(oldArray.length, newSize));
        return newArray;
    }
    public void reset(){
        fName = null;
        fTargetNamespace = null;
        for (int i=0;i<fAttrUseNum;i++) {
            fAttributeUses[i] = null;
        } 
        fAttrUseNum = 0;
        fAttributeWC = null;
        fAnnotations = null;
        fIDAttrName = null;
    }
    public short getType() {
        return XSConstants.ATTRIBUTE_GROUP;
    }
    public String getName() {
        return fName;
    }
    public String getNamespace() {
        return fTargetNamespace;
    }
    public XSObjectList getAttributeUses() {
        if (fAttrUses == null) {
            if (fAttrUseNum > 0) {
                fAttrUses = new XSObjectListImpl(fAttributeUses, fAttrUseNum);
            }
            else {
                fAttrUses = XSObjectListImpl.EMPTY_LIST;
            }
        }
        return fAttrUses;
    }
    public XSWildcard getAttributeWildcard() {
        return fAttributeWC;
    }
    public XSAnnotation getAnnotation() {
        return (fAnnotations != null) ? (XSAnnotation) fAnnotations.item(0) : null;
    }
    public XSObjectList getAnnotations() {
        return (fAnnotations != null) ? fAnnotations : XSObjectListImpl.EMPTY_LIST;
    }
    public XSNamespaceItem getNamespaceItem() {
        return fNamespaceItem;
    }
    void setNamespaceItem(XSNamespaceItem namespaceItem) {
        fNamespaceItem = namespaceItem;
    }
} 

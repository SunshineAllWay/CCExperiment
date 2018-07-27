package org.apache.xerces.impl.xs;
import java.util.Hashtable;
import java.util.Vector;
import org.apache.xerces.xni.QName;
import org.apache.xerces.xs.XSConstants;
import org.apache.xerces.xs.XSObjectList;
import org.apache.xerces.xs.XSSimpleTypeDefinition;
import org.apache.xerces.xs.XSTypeDefinition;
public class SubstitutionGroupHandler {
    private static final XSElementDecl[] EMPTY_GROUP = new XSElementDecl[0];
    private final XSElementDeclHelper fXSElementDeclHelper;
    public SubstitutionGroupHandler(XSElementDeclHelper elementDeclHelper) {
        fXSElementDeclHelper = elementDeclHelper;
    }
    public XSElementDecl getMatchingElemDecl(QName element, XSElementDecl exemplar) {
        if (element.localpart == exemplar.fName &&
            element.uri == exemplar.fTargetNamespace) {
            return exemplar;
        }
        if (exemplar.fScope != XSConstants.SCOPE_GLOBAL) {
            return null;
        }
        if ((exemplar.fBlock & XSConstants.DERIVATION_SUBSTITUTION) != 0) {
            return null;
        }
        XSElementDecl eDecl = fXSElementDeclHelper.getGlobalElementDecl(element);
        if (eDecl == null) {
            return null;
        }
        if (substitutionGroupOK(eDecl, exemplar, exemplar.fBlock)) {
            return eDecl;
        }
        return null;
    }
    protected boolean substitutionGroupOK(XSElementDecl element, XSElementDecl exemplar, short blockingConstraint) {
        if (element == exemplar) {
            return true;
        }
        if ((blockingConstraint & XSConstants.DERIVATION_SUBSTITUTION) != 0) {
            return false;
        }
        XSElementDecl subGroup = element.fSubGroup;
        while (subGroup != null && subGroup != exemplar) {
            subGroup = subGroup.fSubGroup;
        }
        if (subGroup == null) {
            return false;
        }
        return typeDerivationOK(element.fType, exemplar.fType, blockingConstraint);   
    }
    private boolean typeDerivationOK(XSTypeDefinition derived, XSTypeDefinition base, short blockingConstraint) {
        short devMethod = 0, blockConstraint = blockingConstraint;
        XSTypeDefinition type = derived;
        while (type != base && type != SchemaGrammar.fAnyType) {
            if (type.getTypeCategory() == XSTypeDefinition.COMPLEX_TYPE) {
                devMethod |= ((XSComplexTypeDecl)type).fDerivedBy;
            }
            else {
                devMethod |= XSConstants.DERIVATION_RESTRICTION;
            }
            type = type.getBaseType();
            if (type == null) {
                type = SchemaGrammar.fAnyType;
            }
            if (type.getTypeCategory() == XSTypeDefinition.COMPLEX_TYPE) {
                blockConstraint |= ((XSComplexTypeDecl)type).fBlock;
            }
        }
        if (type != base) {
            if (base.getTypeCategory() == XSTypeDefinition.SIMPLE_TYPE) {
                XSSimpleTypeDefinition st = (XSSimpleTypeDefinition) base;
                if (st.getVariety() ==  XSSimpleTypeDefinition.VARIETY_UNION) {
                    XSObjectList memberTypes = st.getMemberTypes();
                    final int length = memberTypes.getLength();
                    for (int i = 0; i < length; ++i) {
                        if (typeDerivationOK(derived, (XSTypeDefinition) memberTypes.item(i), blockingConstraint)) {
                            return true;
                        }
                    }
                }
            }
            return false;
        }
        if ((devMethod & blockConstraint) != 0) {
            return false;
        }
        return true;
    }
    public boolean inSubstitutionGroup(XSElementDecl element, XSElementDecl exemplar) {
        return substitutionGroupOK(element, exemplar, exemplar.fBlock);
    }
    Hashtable fSubGroupsB = new Hashtable();
    private static final OneSubGroup[] EMPTY_VECTOR = new OneSubGroup[0];
    Hashtable fSubGroups = new Hashtable();
    public void reset() {
        fSubGroupsB.clear();
        fSubGroups.clear();
    }
    public void addSubstitutionGroup(XSElementDecl[] elements) {
        XSElementDecl subHead, element;
        Vector subGroup;
        for (int i = elements.length-1; i >= 0; i--) {
            element = elements[i];
            subHead = element.fSubGroup;
            subGroup = (Vector)fSubGroupsB.get(subHead);
            if (subGroup == null) {
                subGroup = new Vector();
                fSubGroupsB.put(subHead, subGroup);
            }
            subGroup.addElement(element);
        }
    }
    public XSElementDecl[] getSubstitutionGroup(XSElementDecl element) {
        Object subGroup = fSubGroups.get(element);
        if (subGroup != null)
            return (XSElementDecl[])subGroup;
        if ((element.fBlock & XSConstants.DERIVATION_SUBSTITUTION) != 0) {
            fSubGroups.put(element, EMPTY_GROUP);
            return EMPTY_GROUP;
        }
        OneSubGroup[] groupB = getSubGroupB(element, new OneSubGroup());
        int len = groupB.length, rlen = 0;
        XSElementDecl[] ret = new XSElementDecl[len];
        for (int i = 0 ; i < len; i++) {
            if ((element.fBlock & groupB[i].dMethod) == 0)
                ret[rlen++] = groupB[i].sub;
        }
        if (rlen < len) {
            XSElementDecl[] ret1 = new XSElementDecl[rlen];
            System.arraycopy(ret, 0, ret1, 0, rlen);
            ret = ret1;
        }
        fSubGroups.put(element, ret);
        return ret;
    }
    private OneSubGroup[] getSubGroupB(XSElementDecl element, OneSubGroup methods) {
        Object subGroup = fSubGroupsB.get(element);
        if (subGroup == null) {
            fSubGroupsB.put(element, EMPTY_VECTOR);
            return EMPTY_VECTOR;
        }
        if (subGroup instanceof OneSubGroup[])
            return (OneSubGroup[])subGroup;
        Vector group = (Vector)subGroup, newGroup = new Vector();
        OneSubGroup[] group1;
        short dMethod, bMethod, dSubMethod, bSubMethod;
        for (int i = group.size()-1, j; i >= 0; i--) {
            XSElementDecl sub = (XSElementDecl)group.elementAt(i);
            if (!getDBMethods(sub.fType, element.fType, methods))
                continue;
            dMethod = methods.dMethod;
            bMethod = methods.bMethod;
            newGroup.addElement(new OneSubGroup(sub, methods.dMethod, methods.bMethod));
            group1 = getSubGroupB(sub, methods);
            for (j = group1.length-1; j >= 0; j--) {
                dSubMethod = (short)(dMethod | group1[j].dMethod);
                bSubMethod = (short)(bMethod | group1[j].bMethod);
                if ((dSubMethod & bSubMethod) != 0)
                    continue;
                newGroup.addElement(new OneSubGroup(group1[j].sub, dSubMethod, bSubMethod));
            }
        }
        OneSubGroup[] ret = new OneSubGroup[newGroup.size()];
        for (int i = newGroup.size()-1; i >= 0; i--) {
            ret[i] = (OneSubGroup)newGroup.elementAt(i);
        }
        fSubGroupsB.put(element, ret);
        return ret;
    }
    private boolean getDBMethods(XSTypeDefinition typed, XSTypeDefinition typeb,
                                 OneSubGroup methods) {
        short dMethod = 0, bMethod = 0;
        while (typed != typeb && typed != SchemaGrammar.fAnyType) {
            if (typed.getTypeCategory() == XSTypeDefinition.COMPLEX_TYPE)
                dMethod |= ((XSComplexTypeDecl)typed).fDerivedBy;
            else
                dMethod |= XSConstants.DERIVATION_RESTRICTION;
            typed = typed.getBaseType();
            if (typed == null)
                typed = SchemaGrammar.fAnyType;
            if (typed.getTypeCategory() == XSTypeDefinition.COMPLEX_TYPE)
                bMethod |= ((XSComplexTypeDecl)typed).fBlock;
        }
        if (typed != typeb || (dMethod & bMethod) != 0)
            return false;
        methods.dMethod = dMethod;
        methods.bMethod = bMethod;
        return true;
    }
    private static final class OneSubGroup {
        OneSubGroup() {}
        OneSubGroup(XSElementDecl sub, short dMethod, short bMethod) {
            this.sub = sub;
            this.dMethod = dMethod;
            this.bMethod = bMethod;
        }
        XSElementDecl sub;
        short dMethod;
        short bMethod;
    }
} 

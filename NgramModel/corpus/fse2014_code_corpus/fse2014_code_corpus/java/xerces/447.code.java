package org.apache.xerces.impl.xs;
import org.apache.xerces.impl.xs.util.StringListImpl;
import org.apache.xerces.impl.xs.util.XSObjectListImpl;
import org.apache.xerces.xs.StringList;
import org.apache.xerces.xs.XSAnnotation;
import org.apache.xerces.xs.XSConstants;
import org.apache.xerces.xs.XSNamespaceItem;
import org.apache.xerces.xs.XSObjectList;
import org.apache.xerces.xs.XSWildcard;
public class XSWildcardDecl implements XSWildcard {
    public static final String ABSENT = null;
    public short fType = NSCONSTRAINT_ANY;
    public short fProcessContents = PC_STRICT;
    public String[] fNamespaceList;
    public XSObjectList fAnnotations = null;  
    public boolean allowNamespace(String namespace) {
        if (fType == NSCONSTRAINT_ANY)
            return true;
        if (fType == NSCONSTRAINT_NOT) {
            boolean found = false;
            int listNum = fNamespaceList.length;
            for (int i = 0; i < listNum && !found; i++) {
                if (namespace == fNamespaceList[i])
                    found = true;
            }
            if (!found)
                return true;
        }
        if (fType == NSCONSTRAINT_LIST) {
            int listNum = fNamespaceList.length;
            for (int i = 0; i < listNum; i++) {
                if (namespace == fNamespaceList[i])
                    return true;
            }
        }
        return false;
    }
    public boolean isSubsetOf(XSWildcardDecl superWildcard) {
        if (superWildcard == null)
            return false;
        if (superWildcard.fType == NSCONSTRAINT_ANY) {
            return true;
        }
        if (fType == NSCONSTRAINT_NOT) {
            if (superWildcard.fType == NSCONSTRAINT_NOT &&
                fNamespaceList[0] == superWildcard.fNamespaceList[0]) {
                return true;
            }
        }
        if (fType == NSCONSTRAINT_LIST) {
            if (superWildcard.fType == NSCONSTRAINT_LIST &&
                subset2sets(fNamespaceList, superWildcard.fNamespaceList)) {
                return true;
            }
            if (superWildcard.fType == NSCONSTRAINT_NOT &&
                !elementInSet(superWildcard.fNamespaceList[0], fNamespaceList) &&
                !elementInSet(ABSENT, fNamespaceList)) {
                return true;
            }
        }
        return false;
    } 
    public boolean weakerProcessContents(XSWildcardDecl superWildcard) {
        return fProcessContents == XSWildcardDecl.PC_LAX &&
               superWildcard.fProcessContents == XSWildcardDecl.PC_STRICT ||
               fProcessContents == XSWildcardDecl.PC_SKIP &&
               superWildcard.fProcessContents != XSWildcardDecl.PC_SKIP;
    }
    public XSWildcardDecl performUnionWith(XSWildcardDecl wildcard,
                                           short processContents) {
        if (wildcard == null)
            return null;
        XSWildcardDecl unionWildcard = new XSWildcardDecl();
        unionWildcard.fProcessContents = processContents;
        if (areSame(wildcard)) {
            unionWildcard.fType = fType;
            unionWildcard.fNamespaceList = fNamespaceList;
        }
        else if ( (fType == NSCONSTRAINT_ANY) || (wildcard.fType == NSCONSTRAINT_ANY) ) {
            unionWildcard.fType = NSCONSTRAINT_ANY;
        }
        else if ( (fType == NSCONSTRAINT_LIST) && (wildcard.fType == NSCONSTRAINT_LIST) ) {
            unionWildcard.fType = NSCONSTRAINT_LIST;
            unionWildcard.fNamespaceList = union2sets(fNamespaceList, wildcard.fNamespaceList);
        }
        else if (fType == NSCONSTRAINT_NOT && wildcard.fType == NSCONSTRAINT_NOT) {
            unionWildcard.fType = NSCONSTRAINT_NOT;
            unionWildcard.fNamespaceList = new String[2];
            unionWildcard.fNamespaceList[0] = ABSENT;
            unionWildcard.fNamespaceList[1] = ABSENT;
        }
        else if ( ((fType == NSCONSTRAINT_NOT) && (wildcard.fType == NSCONSTRAINT_LIST)) ||
                  ((fType == NSCONSTRAINT_LIST) && (wildcard.fType == NSCONSTRAINT_NOT)) ) {
            String[] other = null;
            String[] list = null;
            if (fType == NSCONSTRAINT_NOT) {
                other = fNamespaceList;
                list = wildcard.fNamespaceList;
            }
            else {
                other = wildcard.fNamespaceList;
                list = fNamespaceList;
            }
            boolean foundAbsent = elementInSet(ABSENT, list);
            if (other[0] != ABSENT) {
                boolean foundNS = elementInSet(other[0], list);
                if (foundNS && foundAbsent) {
                    unionWildcard.fType = NSCONSTRAINT_ANY;
                } else if (foundNS && !foundAbsent) {
                    unionWildcard.fType = NSCONSTRAINT_NOT;
                    unionWildcard.fNamespaceList = new String[2];
                    unionWildcard.fNamespaceList[0] = ABSENT;
                    unionWildcard.fNamespaceList[1] = ABSENT;
                } else if (!foundNS && foundAbsent) {
                    return null;
                } else { 
                    unionWildcard.fType = NSCONSTRAINT_NOT;
                    unionWildcard.fNamespaceList = other;
                }
            } else { 
                if (foundAbsent) {
                    unionWildcard.fType = NSCONSTRAINT_ANY;
                } else { 
                    unionWildcard.fType = NSCONSTRAINT_NOT;
                    unionWildcard.fNamespaceList = other;
                }
            }
        }
        return unionWildcard;
    } 
    public XSWildcardDecl performIntersectionWith(XSWildcardDecl wildcard,
                                                  short processContents) {
        if (wildcard == null)
            return null;
        XSWildcardDecl intersectWildcard = new XSWildcardDecl();
        intersectWildcard.fProcessContents = processContents;
        if (areSame(wildcard)) {
            intersectWildcard.fType = fType;
            intersectWildcard.fNamespaceList = fNamespaceList;
        }
        else if ( (fType == NSCONSTRAINT_ANY) || (wildcard.fType == NSCONSTRAINT_ANY) ) {
            XSWildcardDecl other = this;
            if (fType == NSCONSTRAINT_ANY)
                other = wildcard;
            intersectWildcard.fType = other.fType;
            intersectWildcard.fNamespaceList = other.fNamespaceList;
        }
        else if ( ((fType == NSCONSTRAINT_NOT) && (wildcard.fType == NSCONSTRAINT_LIST)) ||
                  ((fType == NSCONSTRAINT_LIST) && (wildcard.fType == NSCONSTRAINT_NOT)) ) {
            String[] list = null;
            String[] other = null;
            if (fType == NSCONSTRAINT_NOT) {
                other = fNamespaceList;
                list = wildcard.fNamespaceList;
            }
            else {
                other = wildcard.fNamespaceList;
                list = fNamespaceList;
            }
            int listSize = list.length;
            String[] intersect = new String[listSize];
            int newSize = 0;
            for (int i = 0; i < listSize; i++) {
                if (list[i] != other[0] && list[i] != ABSENT)
                    intersect[newSize++] = list[i];
            }
            intersectWildcard.fType = NSCONSTRAINT_LIST;
            intersectWildcard.fNamespaceList = new String[newSize];
            System.arraycopy(intersect, 0, intersectWildcard.fNamespaceList, 0, newSize);
        }
        else if ( (fType == NSCONSTRAINT_LIST) && (wildcard.fType == NSCONSTRAINT_LIST) ) {
            intersectWildcard.fType = NSCONSTRAINT_LIST;
            intersectWildcard.fNamespaceList = intersect2sets(fNamespaceList, wildcard.fNamespaceList);
        }
        else if (fType == NSCONSTRAINT_NOT && wildcard.fType == NSCONSTRAINT_NOT) {
            if (fNamespaceList[0] != ABSENT && wildcard.fNamespaceList[0] != ABSENT)
                return null;
            XSWildcardDecl other = this;
            if (fNamespaceList[0] == ABSENT)
                other = wildcard;
            intersectWildcard.fType = other.fType;
            intersectWildcard.fNamespaceList = other.fNamespaceList;
        }
        return intersectWildcard;
    } 
    private boolean areSame(XSWildcardDecl wildcard) {
        if (fType == wildcard.fType) {
            if (fType == NSCONSTRAINT_ANY)
                return true;
            if (fType == NSCONSTRAINT_NOT)
                return fNamespaceList[0] == wildcard.fNamespaceList[0];
            if (fNamespaceList.length == wildcard.fNamespaceList.length) {
                for (int i=0; i<fNamespaceList.length; i++) {
                    if (!elementInSet(fNamespaceList[i], wildcard.fNamespaceList))
                        return false;
                }
                return true;
            }
        }
        return false;
    } 
    String[] intersect2sets(String[] one, String[] theOther){
        String[] result = new String[Math.min(one.length,theOther.length)];
        int count = 0;
        for (int i=0; i<one.length; i++) {
            if (elementInSet(one[i], theOther))
                result[count++] = one[i];
        }
        String[] result2 = new String[count];
        System.arraycopy(result, 0, result2, 0, count);
        return result2;
    }
    String[] union2sets(String[] one, String[] theOther){
        String[] result1 = new String[one.length];
        int count = 0;
        for (int i=0; i<one.length; i++) {
            if (!elementInSet(one[i], theOther))
                result1[count++] = one[i];
        }
        String[] result2 = new String[count+theOther.length];
        System.arraycopy(result1, 0, result2, 0, count);
        System.arraycopy(theOther, 0, result2, count, theOther.length);
        return result2;
    }
    boolean subset2sets(String[] subSet, String[] superSet){
        for (int i=0; i<subSet.length; i++) {
            if (!elementInSet(subSet[i], superSet))
                return false;
        }
        return true;
    }
    boolean elementInSet(String ele, String[] set){
        boolean found = false;
        for (int i=0; i<set.length && !found; i++) {
            if (ele==set[i])
                found = true;
        }
        return found;
    }
    private String fDescription = null;
    public String toString() {
        if (fDescription == null) {
            StringBuffer buffer = new StringBuffer();
            buffer.append("WC[");
            switch (fType) {
            case NSCONSTRAINT_ANY:
                buffer.append(SchemaSymbols.ATTVAL_TWOPOUNDANY);
                break;
            case NSCONSTRAINT_NOT:
                buffer.append(SchemaSymbols.ATTVAL_TWOPOUNDOTHER);
                buffer.append(":\"");
                if (fNamespaceList[0] != null)
                    buffer.append(fNamespaceList[0]);
                buffer.append("\"");
                break;
            case NSCONSTRAINT_LIST:
                if (fNamespaceList.length == 0)
                    break;
                buffer.append("\"");
                if (fNamespaceList[0] != null)
                    buffer.append(fNamespaceList[0]);
                buffer.append("\"");
                for (int i = 1; i < fNamespaceList.length; i++) {
                    buffer.append(",\"");
                    if (fNamespaceList[i] != null)
                        buffer.append(fNamespaceList[i]);
                    buffer.append("\"");
                }
                break;
            }
            buffer.append(']');
            fDescription = buffer.toString();
        }
        return fDescription;
    }
    public short getType() {
        return XSConstants.WILDCARD;
    }
    public String getName() {
        return null;
    }
    public String getNamespace() {
        return null;
    }
    public short getConstraintType() {
        return fType;
    }
    public StringList getNsConstraintList() {
        return new StringListImpl(fNamespaceList, fNamespaceList == null ? 0 : fNamespaceList.length);
    }
    public short getProcessContents() {
        return fProcessContents;
    }
    public String getProcessContentsAsString() {
        switch (fProcessContents) {
            case XSWildcardDecl.PC_SKIP: return "skip";
            case XSWildcardDecl.PC_LAX: return "lax";
            case XSWildcardDecl.PC_STRICT: return "strict";
            default: return "invalid value";
        }
    }
    public XSAnnotation getAnnotation() {
        return (fAnnotations != null) ? (XSAnnotation) fAnnotations.item(0) : null;
    }
    public XSObjectList getAnnotations() {
        return (fAnnotations != null) ? fAnnotations : XSObjectListImpl.EMPTY_LIST;
    }
    public XSNamespaceItem getNamespaceItem() {
        return null;
    }
} 

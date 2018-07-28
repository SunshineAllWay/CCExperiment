package org.apache.xerces.impl.dv;
import org.apache.xerces.impl.xs.util.ShortListImpl;
import org.apache.xerces.impl.xs.util.XSObjectListImpl;
import org.apache.xerces.xs.ShortList;
import org.apache.xerces.xs.XSConstants;
import org.apache.xerces.xs.XSObjectList;
import org.apache.xerces.xs.XSSimpleTypeDefinition;
import org.apache.xerces.xs.XSValue;
public class ValidatedInfo implements XSValue {
    public String normalizedValue;
    public Object actualValue;
    public short actualValueType;
    public XSSimpleType actualType;
    public XSSimpleType memberType;
    public XSSimpleType[] memberTypes;
    public ShortList itemValueTypes;
    public void reset() {
        this.normalizedValue = null;
        this.actualValue = null;
        this.actualValueType = XSConstants.UNAVAILABLE_DT;
        this.actualType = null;
        this.memberType = null;
        this.memberTypes = null;
        this.itemValueTypes = null;
    }
    public String stringValue() {
        if (actualValue == null) {
            return normalizedValue;
        }
        else {
            return actualValue.toString();
        }
    }
    public static boolean isComparable(ValidatedInfo info1, ValidatedInfo info2) {
        final short primitiveType1 = convertToPrimitiveKind(info1.actualValueType);
        final short primitiveType2 = convertToPrimitiveKind(info2.actualValueType);
        if (primitiveType1 != primitiveType2) {    
            return (primitiveType1 == XSConstants.ANYSIMPLETYPE_DT && primitiveType2 == XSConstants.STRING_DT ||
                    primitiveType1 == XSConstants.STRING_DT && primitiveType2 == XSConstants.ANYSIMPLETYPE_DT);
        }
        else if (primitiveType1 == XSConstants.LIST_DT || primitiveType1 == XSConstants.LISTOFUNION_DT) {
            final ShortList typeList1 = info1.itemValueTypes;
            final ShortList typeList2 = info2.itemValueTypes;
            final int typeList1Length = typeList1 != null ? typeList1.getLength() : 0;
            final int typeList2Length = typeList2 != null ? typeList2.getLength() : 0;
            if (typeList1Length != typeList2Length) {
                return false;
            }
            for (int i = 0; i < typeList1Length; ++i) {
                final short primitiveItem1 = convertToPrimitiveKind(typeList1.item(i));
                final short primitiveItem2 = convertToPrimitiveKind(typeList2.item(i));
                if (primitiveItem1 != primitiveItem2) {
                    if (primitiveItem1 == XSConstants.ANYSIMPLETYPE_DT && primitiveItem2 == XSConstants.STRING_DT ||
                        primitiveItem1 == XSConstants.STRING_DT && primitiveItem2 == XSConstants.ANYSIMPLETYPE_DT) {
                        continue;
                    }
                    return false;
                }
            }
        }
        return true;
    }
    private static short convertToPrimitiveKind(short valueType) {
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
    public Object getActualValue() {
        return actualValue;
    }
    public short getActualValueType() {
        return actualValueType;
    }
    public ShortList getListValueTypes() {
        return itemValueTypes == null ? ShortListImpl.EMPTY_LIST : itemValueTypes;
    }
    public XSObjectList getMemberTypeDefinitions() {
        if (memberTypes == null) {
            return XSObjectListImpl.EMPTY_LIST;
        }
        return new XSObjectListImpl(memberTypes, memberTypes.length);
    }
    public String getNormalizedValue() {
        return normalizedValue;
    }
    public XSSimpleTypeDefinition getTypeDefinition() {
        return actualType;
    }
    public XSSimpleTypeDefinition getMemberTypeDefinition() {
        return memberType;
    }
    public void copyFrom(XSValue o) {
        if (o == null) {
            reset();
        }
        else if (o instanceof ValidatedInfo) {
            ValidatedInfo other = (ValidatedInfo)o;
            normalizedValue = other.normalizedValue;
            actualValue = other.actualValue;
            actualValueType = other.actualValueType;
            actualType = other.actualType;
            memberType = other.memberType;
            memberTypes = other.memberTypes;
            itemValueTypes = other.itemValueTypes;
        }
        else {
            normalizedValue = o.getNormalizedValue();
            actualValue = o.getActualValue();
            actualValueType = o.getActualValueType();
            actualType = (XSSimpleType)o.getTypeDefinition();
            memberType = (XSSimpleType)o.getMemberTypeDefinition();
            XSSimpleType realType = memberType == null ? actualType : memberType;
            if (realType != null && realType.getBuiltInKind() == XSConstants.LISTOFUNION_DT) {
                XSObjectList members = o.getMemberTypeDefinitions();
                memberTypes = new XSSimpleType[members.getLength()];
                for (int i = 0; i < members.getLength(); i++) {
                    memberTypes[i] = (XSSimpleType)members.get(i);
                }
            }
            else {
                memberTypes = null;
            }
            itemValueTypes = o.getListValueTypes();
        }
    }
}

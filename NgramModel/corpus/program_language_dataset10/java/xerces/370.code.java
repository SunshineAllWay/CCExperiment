package org.apache.xerces.impl.dv.xs;
import org.apache.xerces.impl.dv.XSSimpleType;
import org.apache.xerces.util.SymbolHash;
public class ExtendedSchemaDVFactoryImpl extends BaseSchemaDVFactory {
    static SymbolHash fBuiltInTypes = new SymbolHash();
    static {
        createBuiltInTypes();
    }
    static void createBuiltInTypes() {
        final String ANYATOMICTYPE     = "anyAtomicType";
        final String DURATION          = "duration";
        final String YEARMONTHDURATION = "yearMonthDuration";
        final String DAYTIMEDURATION   = "dayTimeDuration";
    	createBuiltInTypes(fBuiltInTypes, XSSimpleTypeDecl.fAnyAtomicType);
        fBuiltInTypes.put(ANYATOMICTYPE, XSSimpleTypeDecl.fAnyAtomicType);
        XSSimpleTypeDecl durationDV = (XSSimpleTypeDecl)fBuiltInTypes.get(DURATION);
        fBuiltInTypes.put(YEARMONTHDURATION, new XSSimpleTypeDecl(durationDV, YEARMONTHDURATION, XSSimpleTypeDecl.DV_YEARMONTHDURATION, XSSimpleType.ORDERED_PARTIAL, false, false, false, true, XSSimpleTypeDecl.YEARMONTHDURATION_DT));
        fBuiltInTypes.put(DAYTIMEDURATION, new XSSimpleTypeDecl(durationDV, DAYTIMEDURATION, XSSimpleTypeDecl.DV_DAYTIMEDURATION, XSSimpleType.ORDERED_PARTIAL, false, false, false, true, XSSimpleTypeDecl.DAYTIMEDURATION_DT));
    } 
    public XSSimpleType getBuiltInType(String name) {
        return (XSSimpleType)fBuiltInTypes.get(name);
    }
    public SymbolHash getBuiltInTypes() {
        return (SymbolHash)fBuiltInTypes.makeClone();
    }
}

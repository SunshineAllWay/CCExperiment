package org.apache.xerces.impl.dv.dtd;
import java.util.Hashtable;
import org.apache.xerces.impl.dv.DTDDVFactory;
import org.apache.xerces.impl.dv.DatatypeValidator;
public class DTDDVFactoryImpl extends DTDDVFactory {
    static final Hashtable fBuiltInTypes = new Hashtable();
    static {
        createBuiltInTypes();
    }
    public DatatypeValidator getBuiltInDV(String name) {
        return (DatatypeValidator)fBuiltInTypes.get(name);
    }
    public Hashtable getBuiltInTypes() {
        return (Hashtable)fBuiltInTypes.clone();
    }
    static void createBuiltInTypes() {
        DatatypeValidator dvTemp;
        fBuiltInTypes.put("string", new StringDatatypeValidator());
        fBuiltInTypes.put("ID", new IDDatatypeValidator());
        dvTemp = new IDREFDatatypeValidator();
        fBuiltInTypes.put("IDREF", dvTemp);
        fBuiltInTypes.put("IDREFS", new ListDatatypeValidator(dvTemp));
        dvTemp = new ENTITYDatatypeValidator();
        fBuiltInTypes.put("ENTITY", new ENTITYDatatypeValidator());
        fBuiltInTypes.put("ENTITIES", new ListDatatypeValidator(dvTemp));
        fBuiltInTypes.put("NOTATION", new NOTATIONDatatypeValidator());
        dvTemp = new NMTOKENDatatypeValidator();
        fBuiltInTypes.put("NMTOKEN", dvTemp);
        fBuiltInTypes.put("NMTOKENS", new ListDatatypeValidator(dvTemp));
    }
}

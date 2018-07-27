package org.apache.xerces.impl.dv;
import org.apache.xerces.util.SymbolHash;
import org.apache.xerces.xs.XSObjectList;
public abstract class SchemaDVFactory {
    private static final String DEFAULT_FACTORY_CLASS = "org.apache.xerces.impl.dv.xs.SchemaDVFactoryImpl";
    public static final SchemaDVFactory getInstance() throws DVFactoryException {
        return getInstance(DEFAULT_FACTORY_CLASS);
    } 
    public static final SchemaDVFactory getInstance(String factoryClass) throws DVFactoryException {
        try {
            return (SchemaDVFactory)(ObjectFactory.newInstance(
                factoryClass, ObjectFactory.findClassLoader(), true));
        } 
        catch (ClassCastException e4) {
            throw new DVFactoryException("Schema factory class " + factoryClass + " does not extend from SchemaDVFactory.");
        }
    }
    protected SchemaDVFactory() {}
    public abstract XSSimpleType getBuiltInType(String name);
    public abstract SymbolHash getBuiltInTypes();
    public abstract XSSimpleType createTypeRestriction(String name, String targetNamespace,
                                                       short finalSet, XSSimpleType base,
                                                       XSObjectList annotations);
    public abstract XSSimpleType createTypeList(String name, String targetNamespace,
                                                short finalSet, XSSimpleType itemType,
                                                XSObjectList annotations);
    public abstract XSSimpleType createTypeUnion(String name, String targetNamespace,
                                                 short finalSet, XSSimpleType[] memberTypes,
                                                 XSObjectList annotations);
}

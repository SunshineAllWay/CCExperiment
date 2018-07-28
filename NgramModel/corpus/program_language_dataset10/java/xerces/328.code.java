package org.apache.xerces.impl.dv;
import java.util.Hashtable;
public abstract class DTDDVFactory {
    private static final String DEFAULT_FACTORY_CLASS = "org.apache.xerces.impl.dv.dtd.DTDDVFactoryImpl";
    public static final DTDDVFactory getInstance() throws DVFactoryException {
        return getInstance(DEFAULT_FACTORY_CLASS);
    }
    public static final DTDDVFactory getInstance(String factoryClass) throws DVFactoryException {
        try {
            return (DTDDVFactory)
                (ObjectFactory.newInstance(factoryClass, ObjectFactory.findClassLoader(), true));
        } 
        catch (ClassCastException e) {
            throw new DVFactoryException("DTD factory class " + factoryClass + " does not extend from DTDDVFactory.");
        }
    }
    protected DTDDVFactory() {}
    public abstract DatatypeValidator getBuiltInDV(String name);
    public abstract Hashtable getBuiltInTypes();
}

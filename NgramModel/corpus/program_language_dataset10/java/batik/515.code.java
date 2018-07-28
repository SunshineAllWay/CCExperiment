package org.apache.batik.dom.svg;
public interface ExtendedTraitAccess extends TraitAccess {
    boolean hasProperty(String pn);
    boolean hasTrait(String ns, String ln);
    boolean isPropertyAnimatable(String pn);
    boolean isAttributeAnimatable(String ns, String ln);
    boolean isPropertyAdditive(String pn);
    boolean isAttributeAdditive(String ns, String ln);
    boolean isTraitAnimatable(String ns, String tn);
    boolean isTraitAdditive(String ns, String tn);
    int getPropertyType(String pn);
    int getAttributeType(String ns, String ln);
}

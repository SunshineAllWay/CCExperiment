package org.apache.xerces.impl.xs.models;
import java.util.Vector;
import org.apache.xerces.impl.xs.SubstitutionGroupHandler;
import org.apache.xerces.impl.xs.XMLSchemaException;
import org.apache.xerces.xni.QName;
public interface XSCMValidator {
    public static final short FIRST_ERROR = -1;
    public static final short SUBSEQUENT_ERROR = -2;
    public int[] startContentModel();
    public Object oneTransition (QName elementName, int[] state, SubstitutionGroupHandler subGroupHandler);
    public boolean endContentModel (int[] state);
    public boolean checkUniqueParticleAttribution(SubstitutionGroupHandler subGroupHandler) throws XMLSchemaException;
    public Vector whatCanGoHere(int[] state);
    public int [] occurenceInfo(int[] state);
    public String getTermName(int termId);
    public boolean isCompactedForUPA();
} 

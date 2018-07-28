package org.apache.xerces.impl.xs;
import org.apache.xerces.impl.xs.util.XSObjectListImpl;
import org.apache.xerces.xs.XSConstants;
import org.apache.xerces.xs.XSNamespaceItem;
import org.apache.xerces.xs.XSObjectList;
import org.apache.xerces.xs.XSParticle;
import org.apache.xerces.xs.XSTerm;
public class XSParticleDecl implements XSParticle {
    public static final short PARTICLE_EMPTY        = 0;
    public static final short PARTICLE_ELEMENT      = 1;
    public static final short PARTICLE_WILDCARD     = 2;
    public static final short PARTICLE_MODELGROUP   = 3;
    public static final short PARTICLE_ZERO_OR_MORE = 4;
    public static final short PARTICLE_ZERO_OR_ONE  = 5;
    public static final short PARTICLE_ONE_OR_MORE  = 6;
    public short fType = PARTICLE_EMPTY;
    public XSTerm fValue = null;
    public int fMinOccurs = 1;
    public int fMaxOccurs = 1;
    public XSObjectList fAnnotations = null;  
    public XSParticleDecl makeClone() {
        XSParticleDecl particle = new XSParticleDecl();
        particle.fType = fType;
        particle.fMinOccurs = fMinOccurs;
        particle.fMaxOccurs = fMaxOccurs;
        particle.fDescription = fDescription;
        particle.fValue = fValue;
        particle.fAnnotations = fAnnotations;
        return particle;
    }
    public boolean emptiable() {
        return minEffectiveTotalRange() == 0;
    }
    public boolean isEmpty() {
        if (fType == PARTICLE_EMPTY)
             return true;
        if (fType == PARTICLE_ELEMENT || fType == PARTICLE_WILDCARD)
            return false; 
        return ((XSModelGroupImpl)fValue).isEmpty();
    }
    public int minEffectiveTotalRange() {
        if (fType == XSParticleDecl.PARTICLE_EMPTY) {
            return 0;
        }
        if (fType == PARTICLE_MODELGROUP) {
            return ((XSModelGroupImpl)fValue).minEffectiveTotalRange() * fMinOccurs;
        }
        return fMinOccurs;
    }
    public int maxEffectiveTotalRange() {
        if (fType == XSParticleDecl.PARTICLE_EMPTY) {
            return 0;
        }
        if (fType == PARTICLE_MODELGROUP) {
            int max = ((XSModelGroupImpl)fValue).maxEffectiveTotalRange();
            if (max == SchemaSymbols.OCCURRENCE_UNBOUNDED)
                return SchemaSymbols.OCCURRENCE_UNBOUNDED;
            if (max != 0 && fMaxOccurs == SchemaSymbols.OCCURRENCE_UNBOUNDED)
                return SchemaSymbols.OCCURRENCE_UNBOUNDED;
            return max * fMaxOccurs;
        }
        return fMaxOccurs;
    }
    private String fDescription = null;
    public String toString() {
        if (fDescription == null) {
            StringBuffer buffer = new StringBuffer();
            appendParticle(buffer);
            if (!(fMinOccurs == 0 && fMaxOccurs == 0 ||
                  fMinOccurs == 1 && fMaxOccurs == 1)) {
                buffer.append('{').append(fMinOccurs);
                if (fMaxOccurs == SchemaSymbols.OCCURRENCE_UNBOUNDED)
                    buffer.append("-UNBOUNDED");
                else if (fMinOccurs != fMaxOccurs)
                    buffer.append('-').append(fMaxOccurs);
                buffer.append('}');
            }
            fDescription = buffer.toString();
        }
        return fDescription;
    }
    void appendParticle(StringBuffer buffer) {
        switch (fType) {
        case PARTICLE_EMPTY:
            buffer.append("EMPTY");
            break;
        case PARTICLE_ELEMENT:
            buffer.append(fValue.toString());
            break;
        case PARTICLE_WILDCARD:
            buffer.append('(');
            buffer.append(fValue.toString());
            buffer.append(')');
            break;
        case PARTICLE_MODELGROUP:
            buffer.append(fValue.toString());
            break;
        }
    }
    public void reset(){
        fType = PARTICLE_EMPTY;
        fValue = null;
        fMinOccurs = 1;
        fMaxOccurs = 1;
        fDescription = null;
        fAnnotations = null;
    }
    public short getType() {
        return XSConstants.PARTICLE;
    }
    public String getName() {
        return null;
    }
    public String getNamespace() {
        return null;
    }
    public int getMinOccurs() {
        return fMinOccurs;
    }
    public boolean getMaxOccursUnbounded() {
        return fMaxOccurs == SchemaSymbols.OCCURRENCE_UNBOUNDED;
    }
    public int getMaxOccurs() {
        return fMaxOccurs;
    }
    public XSTerm getTerm() {
        return fValue;
    }
	public XSNamespaceItem getNamespaceItem() {
		return null;
	}
    public XSObjectList getAnnotations() {
        return (fAnnotations != null) ? fAnnotations : XSObjectListImpl.EMPTY_LIST;
    }
} 

package org.apache.xerces.impl.xs.models;
import org.apache.xerces.impl.dtd.models.CMNode;
import org.apache.xerces.impl.dtd.models.CMStateSet;
import org.apache.xerces.impl.xs.XSParticleDecl;
public class XSCMUniOp extends CMNode {
    public XSCMUniOp(int type, CMNode childNode) {
        super(type);
        if ((type() != XSParticleDecl.PARTICLE_ZERO_OR_ONE)
        &&  (type() != XSParticleDecl.PARTICLE_ZERO_OR_MORE)
        &&  (type() != XSParticleDecl.PARTICLE_ONE_OR_MORE)) {
            throw new RuntimeException("ImplementationMessages.VAL_UST");
        }
        fChild = childNode;
    }
    final CMNode getChild() {
        return fChild;
    }
    public boolean isNullable() {
        if (type() == XSParticleDecl.PARTICLE_ONE_OR_MORE)
	        return fChild.isNullable();
	    else
	        return true;
    }
    protected void calcFirstPos(CMStateSet toSet) {
        toSet.setTo(fChild.firstPos());
    }
    protected void calcLastPos(CMStateSet toSet) {
        toSet.setTo(fChild.lastPos());
    }
    private CMNode  fChild;
} 

package org.apache.xerces.impl.xs.models;
import org.apache.xerces.impl.dtd.models.CMNode;
import org.apache.xerces.impl.xs.SchemaSymbols;
import org.apache.xerces.impl.xs.XSComplexTypeDecl;
import org.apache.xerces.impl.xs.XSDeclarationPool;
import org.apache.xerces.impl.xs.XSElementDecl;
import org.apache.xerces.impl.xs.XSModelGroupImpl;
import org.apache.xerces.impl.xs.XSParticleDecl;
public class CMBuilder {
    private XSDeclarationPool fDeclPool = null;
    private static final XSEmptyCM fEmptyCM = new XSEmptyCM();
    private int fLeafCount;
    private int fParticleCount;
    private final CMNodeFactory fNodeFactory;
    public CMBuilder(CMNodeFactory nodeFactory) {
        fDeclPool = null;
        fNodeFactory = nodeFactory ;
    }
    public void setDeclPool(XSDeclarationPool declPool) {
        fDeclPool = declPool;
    }
    public XSCMValidator getContentModel(XSComplexTypeDecl typeDecl, boolean forUPA) {
        short contentType = typeDecl.getContentType();
        if (contentType == XSComplexTypeDecl.CONTENTTYPE_SIMPLE ||
            contentType == XSComplexTypeDecl.CONTENTTYPE_EMPTY) {
            return null;
        }
        XSParticleDecl particle = (XSParticleDecl)typeDecl.getParticle();
        if (particle == null)
            return fEmptyCM;
        XSCMValidator cmValidator = null;
        if (particle.fType == XSParticleDecl.PARTICLE_MODELGROUP &&
            ((XSModelGroupImpl)particle.fValue).fCompositor == XSModelGroupImpl.MODELGROUP_ALL) {
            cmValidator = createAllCM(particle);
        }
        else {
            cmValidator = createDFACM(particle, forUPA);
        }
        fNodeFactory.resetNodeCount() ;
        if (cmValidator == null)
            cmValidator = fEmptyCM;
        return cmValidator;
    }
    XSCMValidator createAllCM(XSParticleDecl particle) {
        if (particle.fMaxOccurs == 0)
            return null;
        XSModelGroupImpl group = (XSModelGroupImpl)particle.fValue;
        XSAllCM allContent = new XSAllCM(particle.fMinOccurs == 0, group.fParticleCount);
        for (int i = 0; i < group.fParticleCount; i++) {
            allContent.addElement((XSElementDecl)group.fParticles[i].fValue,
            group.fParticles[i].fMinOccurs == 0);
        }
        return allContent;
    }
    XSCMValidator createDFACM(XSParticleDecl particle, boolean forUPA) {
        fLeafCount = 0;
        fParticleCount = 0;
        CMNode node = useRepeatingLeafNodes(particle) ? buildCompactSyntaxTree(particle) : buildSyntaxTree(particle, forUPA);
        if (node == null)
            return null;
        return new XSDFACM(node, fLeafCount);
    }
    private CMNode buildSyntaxTree(XSParticleDecl particle, boolean forUPA) {
        int maxOccurs = particle.fMaxOccurs;
        int minOccurs = particle.fMinOccurs;
        boolean compactedForUPA = false;
        if (forUPA) {
            if (minOccurs > 1) {
                if (maxOccurs > minOccurs || particle.getMaxOccursUnbounded()) {
                    minOccurs = 1;
                    compactedForUPA = true;
                }
                else { 
                    minOccurs = 2;
                    compactedForUPA = true;
                }
            }
            if (maxOccurs > 1) {
                maxOccurs = 2;
                compactedForUPA = true;
            }
        }
        short type = particle.fType;
        CMNode nodeRet = null;
        if ((type == XSParticleDecl.PARTICLE_WILDCARD) ||
            (type == XSParticleDecl.PARTICLE_ELEMENT)) {
            nodeRet = fNodeFactory.getCMLeafNode(particle.fType, particle.fValue, fParticleCount++, fLeafCount++);
            nodeRet = expandContentModel(nodeRet, minOccurs, maxOccurs);
            if (nodeRet != null) {
                nodeRet.setIsCompactUPAModel(compactedForUPA);
            }
        }
        else if (type == XSParticleDecl.PARTICLE_MODELGROUP) {
            XSModelGroupImpl group = (XSModelGroupImpl)particle.fValue;
            CMNode temp = null;
            int count = 0;
            for (int i = 0; i < group.fParticleCount; i++) {
                temp = buildSyntaxTree(group.fParticles[i], forUPA);
                if (temp != null) {
                    compactedForUPA |= temp.isCompactedForUPA();
                    ++count;
                    if (nodeRet == null) {
                        nodeRet = temp;
                    }
                    else {
                        nodeRet = fNodeFactory.getCMBinOpNode(group.fCompositor, nodeRet, temp);
                    }
                }
            }
            if (nodeRet != null) {
                if (group.fCompositor == XSModelGroupImpl.MODELGROUP_CHOICE && count < group.fParticleCount) {
                    nodeRet = fNodeFactory.getCMUniOpNode(XSParticleDecl.PARTICLE_ZERO_OR_ONE, nodeRet);
                }
                nodeRet = expandContentModel(nodeRet, minOccurs, maxOccurs);
                nodeRet.setIsCompactUPAModel(compactedForUPA);
            }
        }
        return nodeRet;
    }
    private CMNode expandContentModel(CMNode node,
                                      int minOccurs, int maxOccurs) {
        CMNode nodeRet = null;
        if (minOccurs==1 && maxOccurs==1) {
            nodeRet = node;
        }
        else if (minOccurs==0 && maxOccurs==1) {
            nodeRet = fNodeFactory.getCMUniOpNode(XSParticleDecl.PARTICLE_ZERO_OR_ONE, node);
        }
        else if (minOccurs == 0 && maxOccurs==SchemaSymbols.OCCURRENCE_UNBOUNDED) {
            nodeRet = fNodeFactory.getCMUniOpNode(XSParticleDecl.PARTICLE_ZERO_OR_MORE, node);
        }
        else if (minOccurs == 1 && maxOccurs==SchemaSymbols.OCCURRENCE_UNBOUNDED) {
            nodeRet = fNodeFactory.getCMUniOpNode(XSParticleDecl.PARTICLE_ONE_OR_MORE, node);
        }
        else if (maxOccurs == SchemaSymbols.OCCURRENCE_UNBOUNDED) {
            nodeRet = fNodeFactory.getCMUniOpNode(XSParticleDecl.PARTICLE_ONE_OR_MORE, node);
            nodeRet = fNodeFactory.getCMBinOpNode(XSModelGroupImpl.MODELGROUP_SEQUENCE,
                                                  multiNodes(node, minOccurs-1, true), nodeRet);
        }
        else {
            if (minOccurs > 0) {
                nodeRet = multiNodes(node, minOccurs, false);
            }
            if (maxOccurs > minOccurs) {
                node = fNodeFactory.getCMUniOpNode(XSParticleDecl.PARTICLE_ZERO_OR_ONE, node);
                if (nodeRet == null) {
                    nodeRet = multiNodes(node, maxOccurs-minOccurs, false);
                }
                else {
                    nodeRet = fNodeFactory.getCMBinOpNode(XSModelGroupImpl.MODELGROUP_SEQUENCE,
                                                          nodeRet, multiNodes(node, maxOccurs-minOccurs, true));
                }
            }
        }
        return nodeRet;
    }
    private CMNode multiNodes(CMNode node, int num, boolean copyFirst) {
        if (num == 0) {
            return null;
        }
        if (num == 1) {
            return copyFirst ? copyNode(node) : node;
        }
        int num1 = num/2;
        return fNodeFactory.getCMBinOpNode(XSModelGroupImpl.MODELGROUP_SEQUENCE,
                                           multiNodes(node, num1, copyFirst),
                                           multiNodes(node, num-num1, true));
    }
    private CMNode copyNode(CMNode node) {
        int type = node.type();
        if (type == XSModelGroupImpl.MODELGROUP_CHOICE ||
            type == XSModelGroupImpl.MODELGROUP_SEQUENCE) {
            XSCMBinOp bin = (XSCMBinOp)node;
            node = fNodeFactory.getCMBinOpNode(type, copyNode(bin.getLeft()),
                                 copyNode(bin.getRight()));
        }
        else if (type == XSParticleDecl.PARTICLE_ZERO_OR_MORE ||
                 type == XSParticleDecl.PARTICLE_ONE_OR_MORE ||
                 type == XSParticleDecl.PARTICLE_ZERO_OR_ONE) {
            XSCMUniOp uni = (XSCMUniOp)node;
            node = fNodeFactory.getCMUniOpNode(type, copyNode(uni.getChild()));
        }
        else if (type == XSParticleDecl.PARTICLE_ELEMENT ||
                 type == XSParticleDecl.PARTICLE_WILDCARD) {
            XSCMLeaf leaf = (XSCMLeaf)node;
            node = fNodeFactory.getCMLeafNode(leaf.type(), leaf.getLeaf(), leaf.getParticleId(), fLeafCount++);
        }
        return node;
    }
    private CMNode buildCompactSyntaxTree(XSParticleDecl particle) {
        int maxOccurs = particle.fMaxOccurs;
        int minOccurs = particle.fMinOccurs;
        short type = particle.fType;
        CMNode nodeRet = null;
        if ((type == XSParticleDecl.PARTICLE_WILDCARD) ||
            (type == XSParticleDecl.PARTICLE_ELEMENT)) {
            return buildCompactSyntaxTree2(particle, minOccurs, maxOccurs);
        }
        else if (type == XSParticleDecl.PARTICLE_MODELGROUP) {
            XSModelGroupImpl group = (XSModelGroupImpl)particle.fValue;
            if (group.fParticleCount == 1 && (minOccurs != 1 || maxOccurs != 1)) {
                return buildCompactSyntaxTree2(group.fParticles[0], minOccurs, maxOccurs);
            }
            else {
                CMNode temp = null;
                int count = 0;
                for (int i = 0; i < group.fParticleCount; i++) {
                    temp = buildCompactSyntaxTree(group.fParticles[i]);
                    if (temp != null) {
                        ++count;
                        if (nodeRet == null) {
                            nodeRet = temp;
                        }
                        else {
                            nodeRet = fNodeFactory.getCMBinOpNode(group.fCompositor, nodeRet, temp);
                        }
                    }
                }
                if (nodeRet != null) {
                    if (group.fCompositor == XSModelGroupImpl.MODELGROUP_CHOICE && count < group.fParticleCount) {
                        nodeRet = fNodeFactory.getCMUniOpNode(XSParticleDecl.PARTICLE_ZERO_OR_ONE, nodeRet);
                    }
                }
            }
        }
        return nodeRet;
    }
    private CMNode buildCompactSyntaxTree2(XSParticleDecl particle, int minOccurs, int maxOccurs) {
        CMNode nodeRet = null;
        if (minOccurs == 1 && maxOccurs == 1) {
            nodeRet = fNodeFactory.getCMLeafNode(particle.fType, particle.fValue, fParticleCount++, fLeafCount++);
        }
        else if (minOccurs == 0 && maxOccurs == 1) {
            nodeRet = fNodeFactory.getCMLeafNode(particle.fType, particle.fValue, fParticleCount++, fLeafCount++);
            nodeRet = fNodeFactory.getCMUniOpNode(XSParticleDecl.PARTICLE_ZERO_OR_ONE, nodeRet);
        }
        else if (minOccurs == 0 && maxOccurs==SchemaSymbols.OCCURRENCE_UNBOUNDED) {
            nodeRet = fNodeFactory.getCMLeafNode(particle.fType, particle.fValue, fParticleCount++, fLeafCount++);
            nodeRet = fNodeFactory.getCMUniOpNode(XSParticleDecl.PARTICLE_ZERO_OR_MORE, nodeRet);
        }
        else if (minOccurs == 1 && maxOccurs==SchemaSymbols.OCCURRENCE_UNBOUNDED) {
            nodeRet = fNodeFactory.getCMLeafNode(particle.fType, particle.fValue, fParticleCount++, fLeafCount++);
            nodeRet = fNodeFactory.getCMUniOpNode(XSParticleDecl.PARTICLE_ONE_OR_MORE, nodeRet);
        }
        else {
            nodeRet = fNodeFactory.getCMRepeatingLeafNode(particle.fType, particle.fValue, minOccurs, maxOccurs, fParticleCount++, fLeafCount++);
            if (minOccurs == 0) {
                nodeRet = fNodeFactory.getCMUniOpNode(XSParticleDecl.PARTICLE_ZERO_OR_MORE, nodeRet);
            }
            else {
                nodeRet = fNodeFactory.getCMUniOpNode(XSParticleDecl.PARTICLE_ONE_OR_MORE, nodeRet);
            }
        }
        return nodeRet;
    }
    private boolean useRepeatingLeafNodes(XSParticleDecl particle) {
        int maxOccurs = particle.fMaxOccurs;
        int minOccurs = particle.fMinOccurs;
        short type = particle.fType;
        if (type == XSParticleDecl.PARTICLE_MODELGROUP) {
            XSModelGroupImpl group = (XSModelGroupImpl) particle.fValue;
            if (minOccurs != 1 || maxOccurs != 1) {
                if (group.fParticleCount == 1) {
                    XSParticleDecl particle2 = (XSParticleDecl) group.fParticles[0];
                    short type2 = particle2.fType;
                    return ((type2 == XSParticleDecl.PARTICLE_ELEMENT ||
                            type2 == XSParticleDecl.PARTICLE_WILDCARD) &&
                            particle2.fMinOccurs == 1 &&
                            particle2.fMaxOccurs == 1);
                }
                return (group.fParticleCount == 0);
            }
            for (int i = 0; i < group.fParticleCount; ++i) {
                if (!useRepeatingLeafNodes(group.fParticles[i])) {
                    return false;
                }
            }
        }
        return true;
    }
}

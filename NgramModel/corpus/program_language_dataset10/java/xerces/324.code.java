package org.apache.xerces.impl.dtd.models;
import org.apache.xerces.impl.dtd.XMLContentSpec;
import org.apache.xerces.xni.QName;
public class MixedContentModel
    implements ContentModelValidator {
    private final int fCount;
    private final QName fChildren[];
    private final int fChildrenType[];
    private final boolean fOrdered;
    public MixedContentModel(QName[] children, int[] type, int offset, int length , boolean ordered) {
        fCount = length;
        fChildren = new QName[fCount];
        fChildrenType = new int[fCount];
        for (int i = 0; i < fCount; i++) {
            fChildren[i] = new QName(children[offset + i]);
            fChildrenType[i] = type[offset + i];
        }
        fOrdered = ordered;
    }
    public int validate(QName[] children, int offset, int length) {
        if (fOrdered) {
            int inIndex = 0;
            for (int outIndex = 0; outIndex < length; outIndex++) {
                final QName curChild = children[offset + outIndex];
                if (curChild.localpart == null) {
                    continue;
                }
                int type = fChildrenType[inIndex];
                if (type == XMLContentSpec.CONTENTSPECNODE_LEAF) {
                    if (fChildren[inIndex].rawname != children[offset + outIndex].rawname) {
                        return outIndex;
                    }
                }
                else if (type == XMLContentSpec.CONTENTSPECNODE_ANY) {
                    String uri = fChildren[inIndex].uri;
                    if (uri != null && uri != children[outIndex].uri) {
                        return outIndex;
                    }
                }
                else if (type == XMLContentSpec.CONTENTSPECNODE_ANY_LOCAL) {
                    if (children[outIndex].uri != null) {
                        return outIndex;
                    }
                }
                else if (type == XMLContentSpec.CONTENTSPECNODE_ANY_OTHER) {
                    if (fChildren[inIndex].uri == children[outIndex].uri) {
                        return outIndex;
                    }
                }
                inIndex++;
            }
        }
        else {
            for (int outIndex = 0; outIndex < length; outIndex++)
            {
                final QName curChild = children[offset + outIndex];
                if (curChild.localpart == null)
                    continue;
                int inIndex = 0;
                for (; inIndex < fCount; inIndex++)
                {
                    int type = fChildrenType[inIndex];
                    if (type == XMLContentSpec.CONTENTSPECNODE_LEAF) {
                        if (curChild.rawname == fChildren[inIndex].rawname) {
                            break;
                        }
                    }
                    else if (type == XMLContentSpec.CONTENTSPECNODE_ANY) {
                        String uri = fChildren[inIndex].uri;
                        if (uri == null || uri == children[outIndex].uri) {
                            break;
                        }
                    }
                    else if (type == XMLContentSpec.CONTENTSPECNODE_ANY_LOCAL) {
                        if (children[outIndex].uri == null) {
                            break;
                        }
                    }
                    else if (type == XMLContentSpec.CONTENTSPECNODE_ANY_OTHER) {
                        if (fChildren[inIndex].uri != children[outIndex].uri) {
                            break;
                        }
                    }
                }
                if (inIndex == fCount)
                    return outIndex;
            }
        }
        return -1;
    } 
} 

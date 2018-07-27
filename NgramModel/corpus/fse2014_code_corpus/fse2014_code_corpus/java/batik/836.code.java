package org.apache.batik.ext.awt.image.renderable;
public interface FilterResRable extends Filter {
    Filter getSource();
    void setSource(Filter src);
    int getFilterResolutionX();
    void setFilterResolutionX(int filterResolutionX);
    int getFilterResolutionY();
    void setFilterResolutionY(int filterResolutionY);
}

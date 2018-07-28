package org.apache.batik.ext.awt.image.renderable;
public interface OffsetRable extends Filter {
      Filter getSource();
      void setSource(Filter src);
      void setXoffset(double dx);
      double getXoffset();
      void setYoffset(double dy);
      double getYoffset();
}

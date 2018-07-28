package org.apache.batik.util.gui;
import java.awt.GridBagConstraints;
public class ExtendedGridBagConstraints extends GridBagConstraints {
    public void setGridBounds(int x, int y, int width, int height) {
        gridx = x;
        gridy = y;
        gridwidth = width;
        gridheight = height;
    }
    public void setWeight(double weightx, double weighty) {
        this.weightx = weightx;
        this.weighty = weighty;
    }
}

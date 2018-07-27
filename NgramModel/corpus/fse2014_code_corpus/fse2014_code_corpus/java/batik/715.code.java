package org.apache.batik.ext.awt;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.lang.ref.Reference;
final class BufferedImageHintKey extends RenderingHints.Key {
    BufferedImageHintKey(int number) { super(number); }
    public boolean isCompatibleValue(Object val) {
        if (val == null)
            return true;
        if (!(val instanceof Reference))
            return false;
        Reference ref = (Reference)val;
        val = ref.get();
        if (val == null)
            return true;
        if (val instanceof BufferedImage)
            return true;
        return false;
    }
}

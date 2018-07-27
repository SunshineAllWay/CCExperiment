package org.apache.batik.css.engine.value.svg;
import org.apache.batik.css.engine.value.AbstractValue;
import org.w3c.dom.DOMException;
import org.w3c.dom.css.CSSValue;
public class ICCColor extends AbstractValue {
    protected String colorProfile;
    protected int count;
    protected float[] colors = new float[5];
    public ICCColor(String name) {
        colorProfile = name;
    }
    public short getCssValueType() {
        return CSSValue.CSS_CUSTOM;
    }
    public String getColorProfile() throws DOMException {
        return colorProfile;
    }
    public int getNumberOfColors() throws DOMException {
        return count;
    }
    public float getColor(int i) throws DOMException {
        return colors[i];
    }
    public String getCssText() {
        StringBuffer sb = new StringBuffer( count * 8 );
        sb.append("icc-color(");
        sb.append(colorProfile);
        for (int i = 0; i < count; i++) {
            sb.append(", ");
            sb.append(colors[i]);
        }
        sb.append( ')' );
        return sb.toString();
    }
    public void append(float c) {
        if (count == colors.length) {
            float[] t = new float[count * 2];
            System.arraycopy( colors, 0, t, 0, count );
            colors = t;
        }
        colors[count++] = c;
    }
}

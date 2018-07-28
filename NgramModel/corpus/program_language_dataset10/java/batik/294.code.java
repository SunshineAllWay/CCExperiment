package org.apache.batik.css.engine;
import org.apache.batik.css.engine.value.Value;
public class StyleMap {
    public static final short IMPORTANT_MASK             = 0x0001;
    public static final short COMPUTED_MASK              = 0x0002;
    public static final short NULL_CASCADED_MASK         = 0x0004;
    public static final short INHERITED_MASK             = 0x0008;
    public static final short LINE_HEIGHT_RELATIVE_MASK  = 0x0010;
    public static final short FONT_SIZE_RELATIVE_MASK    = 0x0020;
    public static final short COLOR_RELATIVE_MASK        = 0x0040;
    public static final short PARENT_RELATIVE_MASK       = 0x0080;
    public static final short BLOCK_WIDTH_RELATIVE_MASK  = 0x0100;
    public static final short BLOCK_HEIGHT_RELATIVE_MASK = 0x0200;
    public static final short BOX_RELATIVE_MASK          = 0x0400;
    public static final short ORIGIN_MASK = (short)0xE000; 
    public static final short USER_AGENT_ORIGIN    = 0;
    public static final short USER_ORIGIN          = 0x2000; 
    public static final short NON_CSS_ORIGIN       = 0x4000; 
    public static final short AUTHOR_ORIGIN        = 0x6000; 
    public static final short INLINE_AUTHOR_ORIGIN = (short)0x8000; 
    public static final short OVERRIDE_ORIGIN      = (short)0xA000; 
    protected Value[] values;
    protected short[] masks;
    protected boolean fixedCascadedValues;
    public StyleMap(int size) {
        values = new Value[size];
        masks = new short[size];
    }
    public boolean hasFixedCascadedValues() {
        return fixedCascadedValues;
    }
    public void setFixedCascadedStyle(boolean b) {
        fixedCascadedValues = b;
    }
    public Value getValue(int i) {
        return values[i];
    }
    public short getMask(int i) {
        return masks[i];
    }
    public boolean isImportant(int i) {
        return (masks[i] & IMPORTANT_MASK) != 0;
    }
    public boolean isComputed(int i) {
        return (masks[i] & COMPUTED_MASK) != 0;
    }
    public boolean isNullCascaded(int i) {
        return (masks[i] & NULL_CASCADED_MASK) != 0;
    }
    public boolean isInherited(int i) {
        return (masks[i] & INHERITED_MASK) != 0;
    }
    public short getOrigin(int i) {
        return (short)(masks[i] & ORIGIN_MASK);
    }
    public boolean isColorRelative(int i) {
        return (masks[i] & COLOR_RELATIVE_MASK) != 0;
    }
    public boolean isParentRelative(int i) {
        return (masks[i] & PARENT_RELATIVE_MASK) != 0;
    }
    public boolean isLineHeightRelative(int i) {
        return (masks[i] & LINE_HEIGHT_RELATIVE_MASK) != 0;
    }
    public boolean isFontSizeRelative(int i) {
        return (masks[i] & FONT_SIZE_RELATIVE_MASK) != 0;
    }
    public boolean isBlockWidthRelative(int i) {
        return (masks[i] & BLOCK_WIDTH_RELATIVE_MASK) != 0;
    }
    public boolean isBlockHeightRelative(int i) {
        return (masks[i] & BLOCK_HEIGHT_RELATIVE_MASK) != 0;
    }
    public void putValue(int i, Value v) {
        values[i] = v;
    }
    public void putMask(int i, short m) {
        masks[i] = m;
    }
    public void putImportant(int i, boolean b) {
        if (b) masks[i] |=  IMPORTANT_MASK;
        else   masks[i] &= ~IMPORTANT_MASK;
    }
    public void putOrigin(int i, short val) {
        masks[i] &= ~ORIGIN_MASK;
        masks[i] |= (short)(val & ORIGIN_MASK);
    }
    public void putComputed(int i, boolean b) {
        if (b) masks[i] |=  COMPUTED_MASK;
        else   masks[i] &= ~COMPUTED_MASK;
    }
    public void putNullCascaded(int i, boolean b) {
        if (b) masks[i] |=  NULL_CASCADED_MASK;
        else   masks[i] &= ~NULL_CASCADED_MASK;
    }
    public void putInherited(int i, boolean b) {
        if (b) masks[i] |=  INHERITED_MASK;
        else   masks[i] &= ~INHERITED_MASK;
    }
    public void putColorRelative(int i, boolean b) {
        if (b) masks[i] |=  COLOR_RELATIVE_MASK;
        else   masks[i] &= ~COLOR_RELATIVE_MASK;
    }
    public void putParentRelative(int i, boolean b) {
        if (b) masks[i] |=  PARENT_RELATIVE_MASK;
        else   masks[i] &= ~PARENT_RELATIVE_MASK;
    }
    public void putLineHeightRelative(int i, boolean b) {
        if (b) masks[i] |=  LINE_HEIGHT_RELATIVE_MASK;
        else   masks[i] &= ~LINE_HEIGHT_RELATIVE_MASK;
    }
    public void putFontSizeRelative(int i, boolean b) {
        if (b) masks[i] |=  FONT_SIZE_RELATIVE_MASK;
        else   masks[i] &= ~FONT_SIZE_RELATIVE_MASK;
    }
    public void putBlockWidthRelative(int i, boolean b) {
        if (b) masks[i] |=  BLOCK_WIDTH_RELATIVE_MASK;
        else   masks[i] &= ~BLOCK_WIDTH_RELATIVE_MASK;
    }
    public void putBlockHeightRelative(int i, boolean b) {
        if (b) masks[i] |=  BLOCK_HEIGHT_RELATIVE_MASK;
        else   masks[i] &= ~BLOCK_HEIGHT_RELATIVE_MASK;
    }
    public String toString(CSSEngine eng) {
        int nSlots = values.length;
        StringBuffer sb = new StringBuffer(nSlots * 8);
        for (int i = 0; i < nSlots; i++) {
            Value v = values[i];
            if (v == null) continue;
            sb.append(eng.getPropertyName(i));
            sb.append(": ");
            sb.append(v);
            if (isImportant(i)) sb.append(" !important");
            sb.append(";\n");
        }
        return sb.toString();
    }
}

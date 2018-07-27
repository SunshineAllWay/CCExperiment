package org.apache.batik.css.engine.value.css2;
import org.apache.batik.css.engine.value.IdentifierManager;
import org.apache.batik.css.engine.value.StringMap;
import org.apache.batik.css.engine.value.Value;
import org.apache.batik.css.engine.value.ValueConstants;
import org.apache.batik.css.engine.value.ValueManager;
import org.apache.batik.util.CSSConstants;
import org.apache.batik.util.SVGTypes;
public class DisplayManager extends IdentifierManager {
    protected static final StringMap values = new StringMap();
    static {
        values.put(CSSConstants.CSS_BLOCK_VALUE,
                   ValueConstants.BLOCK_VALUE);
        values.put(CSSConstants.CSS_COMPACT_VALUE,
                   ValueConstants.COMPACT_VALUE);
        values.put(CSSConstants.CSS_INLINE_VALUE,
                   ValueConstants.INLINE_VALUE);
        values.put(CSSConstants.CSS_INLINE_TABLE_VALUE,
                   ValueConstants.INLINE_TABLE_VALUE);
        values.put(CSSConstants.CSS_LIST_ITEM_VALUE,
                   ValueConstants.LIST_ITEM_VALUE);
        values.put(CSSConstants.CSS_MARKER_VALUE,
                   ValueConstants.MARKER_VALUE);
        values.put(CSSConstants.CSS_NONE_VALUE,
                   ValueConstants.NONE_VALUE);
        values.put(CSSConstants.CSS_RUN_IN_VALUE,
                   ValueConstants.RUN_IN_VALUE);
        values.put(CSSConstants.CSS_TABLE_VALUE,
                   ValueConstants.TABLE_VALUE);
        values.put(CSSConstants.CSS_TABLE_CAPTION_VALUE,
                   ValueConstants.TABLE_CAPTION_VALUE);
        values.put(CSSConstants.CSS_TABLE_CELL_VALUE,
                   ValueConstants.TABLE_CELL_VALUE);
        values.put(CSSConstants.CSS_TABLE_COLUMN_VALUE,
                   ValueConstants.TABLE_COLUMN_VALUE);
        values.put(CSSConstants.CSS_TABLE_COLUMN_GROUP_VALUE,
                   ValueConstants.TABLE_COLUMN_GROUP_VALUE);
        values.put(CSSConstants.CSS_TABLE_FOOTER_GROUP_VALUE,
                   ValueConstants.TABLE_FOOTER_GROUP_VALUE);
        values.put(CSSConstants.CSS_TABLE_HEADER_GROUP_VALUE,
                   ValueConstants.TABLE_HEADER_GROUP_VALUE);
        values.put(CSSConstants.CSS_TABLE_ROW_VALUE,
                   ValueConstants.TABLE_ROW_VALUE);
        values.put(CSSConstants.CSS_TABLE_ROW_GROUP_VALUE,
                   ValueConstants.TABLE_ROW_GROUP_VALUE);
    }
    public boolean isInheritedProperty() {
        return false;
    }
    public boolean isAnimatableProperty() {
        return true;
    }
    public boolean isAdditiveProperty() {
        return false;
    }
    public int getPropertyType() {
        return SVGTypes.TYPE_IDENT;
    }
    public String getPropertyName() {
        return CSSConstants.CSS_DISPLAY_PROPERTY;
    }
    public Value getDefaultValue() {
        return ValueConstants.INLINE_VALUE;
    }
    public StringMap getIdentifiers() {
        return values;
    }
}

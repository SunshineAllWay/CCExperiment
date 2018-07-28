package org.apache.batik.css.engine.value;
import org.apache.batik.css.engine.CSSEngine;
import org.apache.batik.css.engine.CSSStylableElement;
import org.apache.batik.css.engine.StyleMap;
import org.apache.batik.util.CSSConstants;
import org.w3c.css.sac.LexicalUnit;
import org.w3c.dom.DOMException;
import org.w3c.dom.css.CSSPrimitiveValue;
public abstract class AbstractColorManager extends IdentifierManager {
    protected static final StringMap values = new StringMap();
    static {
        values.put(CSSConstants.CSS_AQUA_VALUE,
                   ValueConstants.AQUA_VALUE);
        values.put(CSSConstants.CSS_BLACK_VALUE,
                   ValueConstants.BLACK_VALUE);
        values.put(CSSConstants.CSS_BLUE_VALUE,
                   ValueConstants.BLUE_VALUE);
        values.put(CSSConstants.CSS_FUCHSIA_VALUE,
                   ValueConstants.FUCHSIA_VALUE);
        values.put(CSSConstants.CSS_GRAY_VALUE,
                   ValueConstants.GRAY_VALUE);
        values.put(CSSConstants.CSS_GREEN_VALUE,
                   ValueConstants.GREEN_VALUE);
        values.put(CSSConstants.CSS_LIME_VALUE,
                   ValueConstants.LIME_VALUE);
        values.put(CSSConstants.CSS_MAROON_VALUE,
                   ValueConstants.MAROON_VALUE);
        values.put(CSSConstants.CSS_NAVY_VALUE,
                   ValueConstants.NAVY_VALUE);
        values.put(CSSConstants.CSS_OLIVE_VALUE,
                   ValueConstants.OLIVE_VALUE);
        values.put(CSSConstants.CSS_PURPLE_VALUE,
                   ValueConstants.PURPLE_VALUE);
        values.put(CSSConstants.CSS_RED_VALUE,
                   ValueConstants.RED_VALUE);
        values.put(CSSConstants.CSS_SILVER_VALUE,
                   ValueConstants.SILVER_VALUE);
        values.put(CSSConstants.CSS_TEAL_VALUE,
                   ValueConstants.TEAL_VALUE);
        values.put(CSSConstants.CSS_WHITE_VALUE,
                   ValueConstants.WHITE_VALUE);
        values.put(CSSConstants.CSS_YELLOW_VALUE,
                   ValueConstants.YELLOW_VALUE);
        values.put(CSSConstants.CSS_ACTIVEBORDER_VALUE,
                   ValueConstants.ACTIVEBORDER_VALUE);
        values.put(CSSConstants.CSS_ACTIVECAPTION_VALUE,
                   ValueConstants.ACTIVECAPTION_VALUE);
        values.put(CSSConstants.CSS_APPWORKSPACE_VALUE,
                   ValueConstants.APPWORKSPACE_VALUE);
        values.put(CSSConstants.CSS_BACKGROUND_VALUE,
                   ValueConstants.BACKGROUND_VALUE);
        values.put(CSSConstants.CSS_BUTTONFACE_VALUE,
                   ValueConstants.BUTTONFACE_VALUE);
        values.put(CSSConstants.CSS_BUTTONHIGHLIGHT_VALUE,
                   ValueConstants.BUTTONHIGHLIGHT_VALUE);
        values.put(CSSConstants.CSS_BUTTONSHADOW_VALUE,
                   ValueConstants.BUTTONSHADOW_VALUE);
        values.put(CSSConstants.CSS_BUTTONTEXT_VALUE,
                   ValueConstants.BUTTONTEXT_VALUE);
        values.put(CSSConstants.CSS_CAPTIONTEXT_VALUE,
                   ValueConstants.CAPTIONTEXT_VALUE);
        values.put(CSSConstants.CSS_GRAYTEXT_VALUE,
                   ValueConstants.GRAYTEXT_VALUE);
        values.put(CSSConstants.CSS_HIGHLIGHT_VALUE,
                   ValueConstants.HIGHLIGHT_VALUE);
        values.put(CSSConstants.CSS_HIGHLIGHTTEXT_VALUE,
                   ValueConstants.HIGHLIGHTTEXT_VALUE);
        values.put(CSSConstants.CSS_INACTIVEBORDER_VALUE,
                   ValueConstants.INACTIVEBORDER_VALUE);
        values.put(CSSConstants.CSS_INACTIVECAPTION_VALUE,
                   ValueConstants.INACTIVECAPTION_VALUE);
        values.put(CSSConstants.CSS_INACTIVECAPTIONTEXT_VALUE,
                   ValueConstants.INACTIVECAPTIONTEXT_VALUE);
        values.put(CSSConstants.CSS_INFOBACKGROUND_VALUE,
                   ValueConstants.INFOBACKGROUND_VALUE);
        values.put(CSSConstants.CSS_INFOTEXT_VALUE,
                   ValueConstants.INFOTEXT_VALUE);
        values.put(CSSConstants.CSS_MENU_VALUE,
                   ValueConstants.MENU_VALUE);
        values.put(CSSConstants.CSS_MENUTEXT_VALUE,
                   ValueConstants.MENUTEXT_VALUE);
        values.put(CSSConstants.CSS_SCROLLBAR_VALUE,
                   ValueConstants.SCROLLBAR_VALUE);
        values.put(CSSConstants.CSS_THREEDDARKSHADOW_VALUE,
                   ValueConstants.THREEDDARKSHADOW_VALUE);
        values.put(CSSConstants.CSS_THREEDFACE_VALUE,
                   ValueConstants.THREEDFACE_VALUE);
        values.put(CSSConstants.CSS_THREEDHIGHLIGHT_VALUE,
                   ValueConstants.THREEDHIGHLIGHT_VALUE);
        values.put(CSSConstants.CSS_THREEDLIGHTSHADOW_VALUE,
                   ValueConstants.THREEDLIGHTSHADOW_VALUE);
        values.put(CSSConstants.CSS_THREEDSHADOW_VALUE,
                   ValueConstants.THREEDSHADOW_VALUE);
        values.put(CSSConstants.CSS_WINDOW_VALUE,
                   ValueConstants.WINDOW_VALUE);
        values.put(CSSConstants.CSS_WINDOWFRAME_VALUE,
                   ValueConstants.WINDOWFRAME_VALUE);
        values.put(CSSConstants.CSS_WINDOWTEXT_VALUE,
                   ValueConstants.WINDOWTEXT_VALUE);
    }
    protected static final StringMap computedValues = new StringMap();
    static {
        computedValues.put(CSSConstants.CSS_BLACK_VALUE,
                           ValueConstants.BLACK_RGB_VALUE);
        computedValues.put(CSSConstants.CSS_SILVER_VALUE,
                           ValueConstants.SILVER_RGB_VALUE);
        computedValues.put(CSSConstants.CSS_GRAY_VALUE,
                           ValueConstants.GRAY_RGB_VALUE);
        computedValues.put(CSSConstants.CSS_WHITE_VALUE,
                           ValueConstants.WHITE_RGB_VALUE);
        computedValues.put(CSSConstants.CSS_MAROON_VALUE,
                           ValueConstants.MAROON_RGB_VALUE);
        computedValues.put(CSSConstants.CSS_RED_VALUE,
                           ValueConstants.RED_RGB_VALUE);
        computedValues.put(CSSConstants.CSS_PURPLE_VALUE,
                           ValueConstants.PURPLE_RGB_VALUE);
        computedValues.put(CSSConstants.CSS_FUCHSIA_VALUE,
                           ValueConstants.FUCHSIA_RGB_VALUE);
        computedValues.put(CSSConstants.CSS_GREEN_VALUE,
                           ValueConstants.GREEN_RGB_VALUE);
        computedValues.put(CSSConstants.CSS_LIME_VALUE,
                           ValueConstants.LIME_RGB_VALUE);
        computedValues.put(CSSConstants.CSS_OLIVE_VALUE,
                           ValueConstants.OLIVE_RGB_VALUE);
        computedValues.put(CSSConstants.CSS_YELLOW_VALUE,
                           ValueConstants.YELLOW_RGB_VALUE);
        computedValues.put(CSSConstants.CSS_NAVY_VALUE,
                           ValueConstants.NAVY_RGB_VALUE);
        computedValues.put(CSSConstants.CSS_BLUE_VALUE,
                           ValueConstants.BLUE_RGB_VALUE);
        computedValues.put(CSSConstants.CSS_TEAL_VALUE,
                           ValueConstants.TEAL_RGB_VALUE);
        computedValues.put(CSSConstants.CSS_AQUA_VALUE,
                           ValueConstants.AQUA_RGB_VALUE);
    }
    public Value createValue(LexicalUnit lu, CSSEngine engine)
        throws DOMException {
        if (lu.getLexicalUnitType() == LexicalUnit.SAC_RGBCOLOR) {
            lu = lu.getParameters();
            Value red = createColorComponent(lu);
            lu = lu.getNextLexicalUnit().getNextLexicalUnit();
            Value green = createColorComponent(lu);
            lu = lu.getNextLexicalUnit().getNextLexicalUnit();
            Value blue = createColorComponent(lu);
            return createRGBColor(red, green, blue);
        }
        return super.createValue(lu, engine);
    }
    public Value computeValue(CSSStylableElement elt,
                              String pseudo,
                              CSSEngine engine,
                              int idx,
                              StyleMap sm,
                              Value value) {
        if (value.getPrimitiveType() == CSSPrimitiveValue.CSS_IDENT) {
            String ident = value.getStringValue();
            Value v = (Value)computedValues.get(ident);
            if (v != null) {
                return v;
            }
            if (values.get(ident) == null) {
                throw new IllegalStateException("Not a system-color:" + ident );
            }
            return engine.getCSSContext().getSystemColor(ident);
        }
        return super.computeValue(elt, pseudo, engine, idx, sm, value);
    }
    protected Value createRGBColor(Value r, Value g, Value b) {
        return new RGBColorValue(r, g, b);
    }
    protected Value createColorComponent(LexicalUnit lu) throws DOMException {
        switch (lu.getLexicalUnitType()) {
        case LexicalUnit.SAC_INTEGER:
            return new FloatValue(CSSPrimitiveValue.CSS_NUMBER,
                                  lu.getIntegerValue());
        case LexicalUnit.SAC_REAL:
            return new FloatValue(CSSPrimitiveValue.CSS_NUMBER,
                                  lu.getFloatValue());
        case LexicalUnit.SAC_PERCENTAGE:
            return new FloatValue(CSSPrimitiveValue.CSS_PERCENTAGE,
                                  lu.getFloatValue());
        }
        throw createInvalidRGBComponentUnitDOMException
            (lu.getLexicalUnitType());
    }
    public StringMap getIdentifiers() {
        return values;
    }
    private DOMException createInvalidRGBComponentUnitDOMException
        (short type) {
        Object[] p = new Object[] { getPropertyName(),
                                    new Integer(type) };
        String s = Messages.formatMessage("invalid.rgb.component.unit", p);
        return new DOMException(DOMException.NOT_SUPPORTED_ERR, s);
    }
}

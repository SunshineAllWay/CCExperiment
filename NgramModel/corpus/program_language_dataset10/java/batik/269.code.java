package org.apache.batik.css.dom;
import java.util.ArrayList;
import org.apache.batik.css.engine.value.FloatValue;
import org.apache.batik.css.engine.value.Value;
import org.apache.batik.css.engine.value.svg.ICCColor;
import org.apache.batik.util.CSSConstants;
import org.w3c.dom.DOMException;
import org.w3c.dom.css.CSSPrimitiveValue;
import org.w3c.dom.css.CSSValue;
import org.w3c.dom.css.Counter;
import org.w3c.dom.css.RGBColor;
import org.w3c.dom.css.Rect;
import org.w3c.dom.svg.SVGColor;
import org.w3c.dom.svg.SVGICCColor;
import org.w3c.dom.svg.SVGNumber;
import org.w3c.dom.svg.SVGNumberList;
public class CSSOMSVGColor
    implements SVGColor,
               RGBColor,
               SVGICCColor,
               SVGNumberList {
    protected ValueProvider valueProvider;
    protected ModificationHandler handler;
    protected RedComponent redComponent;
    protected GreenComponent greenComponent;
    protected BlueComponent blueComponent;
    protected ArrayList iccColors;
    public CSSOMSVGColor(ValueProvider vp) {
        valueProvider = vp;
    }
    public void setModificationHandler(ModificationHandler h) {
        handler = h;
    }
    public String getCssText() {
        return valueProvider.getValue().getCssText();
    }
    public void setCssText(String cssText) throws DOMException {
        if (handler == null) {
            throw new DOMException
                (DOMException.NO_MODIFICATION_ALLOWED_ERR, "");
        } else {
            iccColors = null;
            handler.textChanged(cssText);
        }
    }
    public short getCssValueType() {
        return CSS_CUSTOM;
    }
    public short getColorType() {
        Value value = valueProvider.getValue();
        int cssValueType = value.getCssValueType();
        switch ( cssValueType ) {
        case CSSValue.CSS_PRIMITIVE_VALUE:
            int primitiveType = value.getPrimitiveType();
            switch ( primitiveType ) {
            case CSSPrimitiveValue.CSS_IDENT: {
                if (value.getStringValue().equalsIgnoreCase
                    (CSSConstants.CSS_CURRENTCOLOR_VALUE))
                    return SVG_COLORTYPE_CURRENTCOLOR;
                return SVG_COLORTYPE_RGBCOLOR;
            }
            case CSSPrimitiveValue.CSS_RGBCOLOR:
                return SVG_COLORTYPE_RGBCOLOR;
            }
            throw new IllegalStateException("Found unexpected PrimitiveType:" + primitiveType );
        case CSSValue.CSS_VALUE_LIST:
            return SVG_COLORTYPE_RGBCOLOR_ICCCOLOR;
        }
        throw new IllegalStateException("Found unexpected CssValueType:" + cssValueType );
    }
    public RGBColor getRGBColor() {
        return this;
    }
    public RGBColor getRgbColor() {
        return this;
    }
    public void setRGBColor(String color) {
        if (handler == null) {
            throw new DOMException
                (DOMException.NO_MODIFICATION_ALLOWED_ERR, "");
        } else {
            handler.rgbColorChanged(color);
        }
    }
    public SVGICCColor getICCColor() {
        return this;
    }
    public SVGICCColor getIccColor() {
        return this;
    }
    public void setRGBColorICCColor(String rgb, String icc) {
        if (handler == null) {
            throw new DOMException
                (DOMException.NO_MODIFICATION_ALLOWED_ERR, "");
        } else {
            iccColors = null;
            handler.rgbColorICCColorChanged(rgb, icc);
        }
    }
    public void setColor(short type, String rgb, String icc) {
        if (handler == null) {
            throw new DOMException
                (DOMException.NO_MODIFICATION_ALLOWED_ERR, "");
    } else {
            iccColors = null;
            handler.colorChanged(type, rgb, icc);
        }
    }
    public CSSPrimitiveValue getRed() {
        valueProvider.getValue().getRed();
        if (redComponent == null) {
            redComponent = new RedComponent();
        }
        return redComponent;
    }
    public CSSPrimitiveValue getGreen() {
        valueProvider.getValue().getGreen();
        if (greenComponent == null) {
            greenComponent = new GreenComponent();
        }
        return greenComponent;
    }
    public CSSPrimitiveValue getBlue() {
        valueProvider.getValue().getBlue();
        if (blueComponent == null) {
            blueComponent = new BlueComponent();
        }
        return blueComponent;
    }
    public String getColorProfile() {
        if (getColorType() != SVG_COLORTYPE_RGBCOLOR_ICCCOLOR) {
            throw new DOMException(DOMException.SYNTAX_ERR, "");
        }
        Value value = valueProvider.getValue();
        return ((ICCColor)value.item(1)).getColorProfile();
    }
    public void setColorProfile(String colorProfile) throws DOMException {
        if (handler == null) {
            throw new DOMException
                (DOMException.NO_MODIFICATION_ALLOWED_ERR, "");
        } else {
            handler.colorProfileChanged(colorProfile);
        }
    }
    public SVGNumberList getColors() {
        return this;
    }
    public int getNumberOfItems() {
        if (getColorType() != SVG_COLORTYPE_RGBCOLOR_ICCCOLOR) {
            throw new DOMException(DOMException.SYNTAX_ERR, "");
        }
        Value value = valueProvider.getValue();
        return ((ICCColor)value.item(1)).getNumberOfColors();
    }
    public void clear() throws DOMException {
        if (handler == null) {
            throw new DOMException
                (DOMException.NO_MODIFICATION_ALLOWED_ERR, "");
        } else {
            iccColors = null;
            handler.colorsCleared();
        }
    }
    public SVGNumber initialize(SVGNumber newItem) throws DOMException {
        if (handler == null) {
            throw new DOMException
                (DOMException.NO_MODIFICATION_ALLOWED_ERR, "");
        } else {
            float f = newItem.getValue();
            iccColors = new ArrayList();
            SVGNumber result = new ColorNumber(f);
            iccColors.add(result);
            handler.colorsInitialized(f);
            return result;
        }
    }
    public SVGNumber getItem(int index) throws DOMException {
        if (getColorType() != SVG_COLORTYPE_RGBCOLOR_ICCCOLOR) {
            throw new DOMException(DOMException.INDEX_SIZE_ERR, "");
        }
        int n = getNumberOfItems();
        if (index < 0 || index >= n) {
            throw new DOMException(DOMException.INDEX_SIZE_ERR, "");
        }
        if (iccColors == null) {
            iccColors = new ArrayList(n);
            for (int i = iccColors.size(); i < n; i++) {
                iccColors.add(null);
            }
        }
        Value value = valueProvider.getValue().item(1);
        float f = ((ICCColor)value).getColor(index);
        SVGNumber result = new ColorNumber(f);
        iccColors.set(index, result);
        return result;
    }
    public SVGNumber insertItemBefore(SVGNumber newItem, int index)
        throws DOMException {
        if (handler == null) {
            throw new DOMException
                (DOMException.NO_MODIFICATION_ALLOWED_ERR, "");
        } else {
            int n = getNumberOfItems();
            if (index < 0 || index > n) {
                throw new DOMException(DOMException.INDEX_SIZE_ERR, "");
            }
            if (iccColors == null) {
                iccColors = new ArrayList(n);
                for (int i = iccColors.size(); i < n; i++) {
                    iccColors.add(null);
                }
            }
            float f = newItem.getValue();
            SVGNumber result = new ColorNumber(f);
            iccColors.add(index, result);
            handler.colorInsertedBefore(f, index);
            return result;
        }
    }
    public SVGNumber replaceItem(SVGNumber newItem, int index)
        throws DOMException {
        if (handler == null) {
            throw new DOMException
                (DOMException.NO_MODIFICATION_ALLOWED_ERR, "");
        } else {
            int n = getNumberOfItems();
            if (index < 0 || index >= n) {
                throw new DOMException(DOMException.INDEX_SIZE_ERR, "");
            }
            if (iccColors == null) {
                iccColors = new ArrayList(n);
                for (int i = iccColors.size(); i < n; i++) {
                    iccColors.add(null);
                }
            }
            float f = newItem.getValue();
            SVGNumber result = new ColorNumber(f);
            iccColors.set(index, result);
            handler.colorReplaced(f, index);
            return result;
        }
    }
    public SVGNumber removeItem(int index) throws DOMException {
        if (handler == null) {
            throw new DOMException
                (DOMException.NO_MODIFICATION_ALLOWED_ERR, "");
        } else {
            int n = getNumberOfItems();
            if (index < 0 || index >= n) {
                throw new DOMException(DOMException.INDEX_SIZE_ERR, "");
            }
            SVGNumber result = null;
            if (iccColors != null) {
                result = (ColorNumber)iccColors.get(index);
            }
            if (result == null) {
                Value value = valueProvider.getValue().item(1);
                result =
                    new ColorNumber(((ICCColor)value).getColor(index));
            }
            handler.colorRemoved(index);
            return result;
        }
    }
    public SVGNumber appendItem (SVGNumber newItem) throws DOMException {
        if (handler == null) {
            throw new DOMException
                (DOMException.NO_MODIFICATION_ALLOWED_ERR, "");
        } else {
            if (iccColors == null) {
                int n = getNumberOfItems();
                iccColors = new ArrayList(n);
                for (int i = 0; i < n; i++) {
                    iccColors.add(null);
                }
            }
            float f = newItem.getValue();
            SVGNumber result = new ColorNumber(f);
            iccColors.add(result);
            handler.colorAppend(f);
            return result;
        }
    }
    protected class ColorNumber implements SVGNumber {
        protected float value;
        public ColorNumber(float f) {
            value = f;
        }
        public float getValue() {
            if (iccColors == null) {
                return value;
            }
            int idx = iccColors.indexOf(this);
            if (idx == -1) {
                return value;
            }
            Value value = valueProvider.getValue().item(1);
            return ((ICCColor)value).getColor(idx);
        }
        public void setValue(float f) {
            value = f;
            if (iccColors == null) {
                return;
            }
            int idx = iccColors.indexOf(this);
            if (idx == -1) {
                return;
            }
            if (handler == null) {
                throw new DOMException
                    (DOMException.NO_MODIFICATION_ALLOWED_ERR, "");
            } else {
                handler.colorReplaced(f, idx);
            }
        }
    }
    public interface ValueProvider {
        Value getValue();
    }
    public interface ModificationHandler {
        void textChanged(String text) throws DOMException;
        void redTextChanged(String text) throws DOMException;
        void redFloatValueChanged(short unit, float value)
            throws DOMException;
        void greenTextChanged(String text) throws DOMException;
        void greenFloatValueChanged(short unit, float value)
            throws DOMException;
        void blueTextChanged(String text) throws DOMException;
        void blueFloatValueChanged(short unit, float value)
            throws DOMException;
        void rgbColorChanged(String text) throws DOMException;
        void rgbColorICCColorChanged(String rgb, String icc)
            throws DOMException;
        void colorChanged(short type, String rgb, String icc)
            throws DOMException;
        void colorProfileChanged(String cp) throws DOMException;
        void colorsCleared() throws DOMException;
        void colorsInitialized(float f) throws DOMException;
        void colorInsertedBefore(float f, int idx) throws DOMException;
        void colorReplaced(float f, int idx) throws DOMException;
        void colorRemoved(int idx) throws DOMException;
        void colorAppend(float f) throws DOMException;
    }
    public abstract class AbstractModificationHandler
        implements ModificationHandler {
        protected abstract Value getValue();
        public void redTextChanged(String text) throws DOMException {
            StringBuffer sb = new StringBuffer(40);
            Value value = getValue();
            switch (getColorType()) {
            case SVG_COLORTYPE_RGBCOLOR:
                sb.append("rgb(");
                sb.append(text); sb.append(',');
                sb.append( value.getGreen().getCssText()); sb.append(',');
                sb.append( value.getBlue().getCssText()); sb.append(')');
                break;
            case SVG_COLORTYPE_RGBCOLOR_ICCCOLOR:
                sb.append("rgb(");
                sb.append(text); sb.append(',');
                sb.append(value.item(0).getGreen().getCssText());
                sb.append(',');
                sb.append(value.item(0).getBlue().getCssText());
                sb.append(')');
                sb.append(value.item(1).getCssText());
                break;
            default:
                throw new DOMException
                    (DOMException.NO_MODIFICATION_ALLOWED_ERR, "");
            }
            textChanged(sb.toString());
        }
        public void redFloatValueChanged(short unit, float fValue)
            throws DOMException {
            StringBuffer sb = new StringBuffer(40);
            Value value = getValue();
            switch (getColorType()) {
            case SVG_COLORTYPE_RGBCOLOR:
                sb.append("rgb(");
                sb.append(FloatValue.getCssText(unit, fValue)); sb.append(',');
                sb.append(value.getGreen().getCssText()); sb.append(',');
                sb.append(value.getBlue().getCssText()); sb.append(')');
                break;
            case SVG_COLORTYPE_RGBCOLOR_ICCCOLOR:
                sb.append("rgb(");
                sb.append(FloatValue.getCssText(unit, fValue));
                sb.append(',');
                sb.append(value.item(0).getGreen().getCssText());
                sb.append(',');
                sb.append(value.item(0).getBlue().getCssText());
                sb.append(')');
                sb.append(value.item(1).getCssText());
                break;
            default:
                throw new DOMException
                    (DOMException.NO_MODIFICATION_ALLOWED_ERR, "");
            }
            textChanged(sb.toString());
        }
        public void greenTextChanged(String text) throws DOMException {
            StringBuffer sb = new StringBuffer(40);
            Value value = getValue();
            switch (getColorType()) {
            case SVG_COLORTYPE_RGBCOLOR:
                sb.append("rgb(");
                sb.append(value.getRed().getCssText()); sb.append(',');
                sb.append(text); sb.append(',');
                sb.append(value.getBlue().getCssText()); sb.append(')');
                break;
            case SVG_COLORTYPE_RGBCOLOR_ICCCOLOR:
                sb.append("rgb(");
                sb.append(value.item(0).getRed().getCssText());
                sb.append(',');
                sb.append(text);
                sb.append(',');
                sb.append(value.item(0).getBlue().getCssText());
                sb.append(')');
                sb.append(value.item(1).getCssText());
                break;
            default:
                throw new DOMException
                    (DOMException.NO_MODIFICATION_ALLOWED_ERR, "");
            }
            textChanged(sb.toString());
        }
        public void greenFloatValueChanged(short unit, float fValue)
            throws DOMException {
            StringBuffer sb = new StringBuffer(40);
            Value value = getValue();
            switch (getColorType()) {
            case SVG_COLORTYPE_RGBCOLOR:
                sb.append("rgb(");
                sb.append(value.getRed().getCssText()); sb.append(',');
                sb.append(FloatValue.getCssText(unit, fValue)); sb.append(',');
                sb.append(value.getBlue().getCssText()); sb.append(')');
                break;
            case SVG_COLORTYPE_RGBCOLOR_ICCCOLOR:
                sb.append("rgb(");
                sb.append(value.item(0).getRed().getCssText());
                sb.append(',');
                sb.append(FloatValue.getCssText(unit, fValue));
                sb.append(',');
                sb.append(value.item(0).getBlue().getCssText());
                sb.append(')');
                sb.append(value.item(1).getCssText());
                break;
            default:
                throw new DOMException
                    (DOMException.NO_MODIFICATION_ALLOWED_ERR, "");
            }
            textChanged(sb.toString());
        }
        public void blueTextChanged(String text) throws DOMException {
            StringBuffer sb = new StringBuffer(40);
            Value value = getValue();
            switch (getColorType()) {
            case SVG_COLORTYPE_RGBCOLOR:
                sb.append("rgb(");
                sb.append(value.getRed().getCssText()); sb.append(',');
                sb.append(value.getGreen().getCssText()); sb.append(',');
                sb.append(text); sb.append(')');
                break;
            case SVG_COLORTYPE_RGBCOLOR_ICCCOLOR:
                sb.append("rgb(");
                sb.append(value.item(0).getRed().getCssText());
                sb.append(',');
                sb.append(value.item(0).getGreen().getCssText());
                sb.append(',');
                sb.append(text);
                sb.append(')');
                sb.append(value.item(1).getCssText());
                break;
            default:
                throw new DOMException
                    (DOMException.NO_MODIFICATION_ALLOWED_ERR, "");
            }
            textChanged(sb.toString());
        }
        public void blueFloatValueChanged(short unit, float fValue)
            throws DOMException {
            StringBuffer sb = new StringBuffer(40);
            Value value = getValue();
            switch (getColorType()) {
            case SVG_COLORTYPE_RGBCOLOR:
                sb.append("rgb(");
                sb.append(value.getRed().getCssText()); sb.append(',');
                sb.append(value.getGreen().getCssText()); sb.append(',');
                sb.append(FloatValue.getCssText(unit, fValue)); sb.append(')');
                break;
            case SVG_COLORTYPE_RGBCOLOR_ICCCOLOR:
                sb.append("rgb(");
                sb.append(value.item(0).getRed().getCssText());
                sb.append(',');
                sb.append(value.item(0).getGreen().getCssText());
                sb.append(',');
                sb.append(FloatValue.getCssText(unit, fValue));
                sb.append(')');
                sb.append(value.item(1).getCssText());
                break;
            default:
                throw new DOMException
                    (DOMException.NO_MODIFICATION_ALLOWED_ERR, "");
            }
            textChanged(sb.toString());
        }
        public void rgbColorChanged(String text) throws DOMException {
            switch (getColorType()) {
            case SVG_COLORTYPE_RGBCOLOR:
                break;
            case SVG_COLORTYPE_RGBCOLOR_ICCCOLOR:
                text += getValue().item(1).getCssText();
                break;
            default:
                throw new DOMException
                    (DOMException.NO_MODIFICATION_ALLOWED_ERR, "");
            }
            textChanged(text);
        }
        public void rgbColorICCColorChanged(String rgb, String icc)
            throws DOMException {
            switch (getColorType()) {
            case SVG_COLORTYPE_RGBCOLOR_ICCCOLOR:
                textChanged(rgb + ' ' + icc);
                break;
            default:
                throw new DOMException
                    (DOMException.NO_MODIFICATION_ALLOWED_ERR, "");
            }
        }
        public void colorChanged(short type, String rgb, String icc)
            throws DOMException {
            switch (type) {
            case SVG_COLORTYPE_CURRENTCOLOR:
                textChanged(CSSConstants.CSS_CURRENTCOLOR_VALUE);
                break;
            case SVG_COLORTYPE_RGBCOLOR:
                textChanged(rgb);
                break;
            case SVG_COLORTYPE_RGBCOLOR_ICCCOLOR:
                textChanged(rgb + ' ' + icc);
                break;
            default:
                throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "");
            }
        }
        public void colorProfileChanged(String cp) throws DOMException {
            Value value = getValue();
            switch (getColorType()) {
            case SVG_COLORTYPE_RGBCOLOR_ICCCOLOR:
                StringBuffer sb =
                    new StringBuffer( value.item(0).getCssText());
                sb.append(" icc-color(");
                sb.append(cp);
                ICCColor iccc = (ICCColor)value.item(1);
                for (int i = 0; i < iccc.getLength(); i++) {
                    sb.append(',');
                    sb.append(iccc.getColor(i));
                }
                sb.append(')');
                textChanged(sb.toString());
                break;
            default:
                throw new DOMException
                    (DOMException.NO_MODIFICATION_ALLOWED_ERR, "");
            }
        }
        public void colorsCleared() throws DOMException {
            Value value = getValue();
            switch (getColorType()) {
            case SVG_COLORTYPE_RGBCOLOR_ICCCOLOR:
                StringBuffer sb =
                    new StringBuffer( value.item(0).getCssText());
                sb.append(" icc-color(");
                ICCColor iccc = (ICCColor)value.item(1);
                sb.append(iccc.getColorProfile());
                sb.append(')');
                textChanged(sb.toString());
                break;
            default:
                throw new DOMException
                    (DOMException.NO_MODIFICATION_ALLOWED_ERR, "");
            }
        }
        public void colorsInitialized(float f) throws DOMException {
            Value value = getValue();
            switch (getColorType()) {
            case SVG_COLORTYPE_RGBCOLOR_ICCCOLOR:
                StringBuffer sb =
                    new StringBuffer( value.item(0).getCssText());
                sb.append(" icc-color(");
                ICCColor iccc = (ICCColor)value.item(1);
                sb.append(iccc.getColorProfile());
                sb.append(',');
                sb.append(f);
                sb.append(')');
                textChanged(sb.toString());
                break;
            default:
                throw new DOMException
                    (DOMException.NO_MODIFICATION_ALLOWED_ERR, "");
            }
        }
        public void colorInsertedBefore(float f, int idx) throws DOMException {
            Value value = getValue();
            switch (getColorType()) {
            case SVG_COLORTYPE_RGBCOLOR_ICCCOLOR:
                StringBuffer sb =
                    new StringBuffer( value.item(0).getCssText());
                sb.append(" icc-color(");
                ICCColor iccc = (ICCColor)value.item(1);
                sb.append(iccc.getColorProfile());
                for (int i = 0; i < idx; i++) {
                    sb.append(',');
                    sb.append(iccc.getColor(i));
                }
                sb.append(',');
                sb.append(f);
                for (int i = idx; i < iccc.getLength(); i++) {
                    sb.append(',');
                    sb.append(iccc.getColor(i));
                }
                sb.append(')');
                textChanged(sb.toString());
                break;
            default:
                throw new DOMException
                    (DOMException.NO_MODIFICATION_ALLOWED_ERR, "");
            }
        }
        public void colorReplaced(float f, int idx) throws DOMException {
            Value value = getValue();
            switch (getColorType()) {
            case SVG_COLORTYPE_RGBCOLOR_ICCCOLOR:
                StringBuffer sb =
                    new StringBuffer( value.item(0).getCssText());
                sb.append(" icc-color(");
                ICCColor iccc = (ICCColor)value.item(1);
                sb.append(iccc.getColorProfile());
                for (int i = 0; i < idx; i++) {
                    sb.append(',');
                    sb.append(iccc.getColor(i));
                }
                sb.append(',');
                sb.append(f);
                for (int i = idx + 1; i < iccc.getLength(); i++) {
                    sb.append(',');
                    sb.append(iccc.getColor(i));
                }
                sb.append(')');
                textChanged(sb.toString());
                break;
            default:
                throw new DOMException
                    (DOMException.NO_MODIFICATION_ALLOWED_ERR, "");
            }
        }
        public void colorRemoved(int idx) throws DOMException {
            Value value = getValue();
            switch (getColorType()) {
            case SVG_COLORTYPE_RGBCOLOR_ICCCOLOR:
                StringBuffer sb =
                    new StringBuffer( value.item(0).getCssText());
                sb.append(" icc-color(");
                ICCColor iccc = (ICCColor)value.item(1);
                sb.append(iccc.getColorProfile());
                for (int i = 0; i < idx; i++) {
                    sb.append(',');
                    sb.append(iccc.getColor(i));
                }
                for (int i = idx + 1; i < iccc.getLength(); i++) {
                    sb.append(',');
                    sb.append(iccc.getColor(i));
                }
                sb.append(')');
                textChanged(sb.toString());
                break;
            default:
                throw new DOMException
                    (DOMException.NO_MODIFICATION_ALLOWED_ERR, "");
            }
        }
        public void colorAppend(float f) throws DOMException {
            Value value = getValue();
            switch (getColorType()) {
            case SVG_COLORTYPE_RGBCOLOR_ICCCOLOR:
                StringBuffer sb =
                    new StringBuffer( value.item(0).getCssText());
                sb.append(" icc-color(");
                ICCColor iccc = (ICCColor)value.item(1);
                sb.append(iccc.getColorProfile());
                for (int i = 0; i < iccc.getLength(); i++) {
                    sb.append(',');
                    sb.append(iccc.getColor(i));
                }
                sb.append(',');
                sb.append(f);
                sb.append(')');
                textChanged(sb.toString());
                break;
            default:
                throw new DOMException
                    (DOMException.NO_MODIFICATION_ALLOWED_ERR, "");
            }
        }
    }
    protected abstract class AbstractComponent implements CSSPrimitiveValue {
        protected abstract Value getValue();
        public String getCssText() {
            return getValue().getCssText();
        }
        public short getCssValueType() {
            return getValue().getCssValueType();
        }
        public short getPrimitiveType() {
            return getValue().getPrimitiveType();
        }
        public float getFloatValue(short unitType) throws DOMException {
            return CSSOMValue.convertFloatValue(unitType, getValue());
        }
        public String getStringValue() throws DOMException {
            return valueProvider.getValue().getStringValue();
        }
        public Counter getCounterValue() throws DOMException {
            throw new DOMException(DOMException.INVALID_ACCESS_ERR, "");
        }
        public Rect getRectValue() throws DOMException {
            throw new DOMException(DOMException.INVALID_ACCESS_ERR, "");
        }
        public RGBColor getRGBColorValue() throws DOMException {
            throw new DOMException(DOMException.INVALID_ACCESS_ERR, "");
        }
        public int getLength() {
            throw new DOMException(DOMException.INVALID_ACCESS_ERR, "");
        }
        public CSSValue item(int index) {
            throw new DOMException(DOMException.INVALID_ACCESS_ERR, "");
        }
    }
    protected abstract class FloatComponent extends AbstractComponent {
        public void setStringValue(short stringType, String stringValue)
            throws DOMException {
            throw new DOMException(DOMException.INVALID_ACCESS_ERR, "");
        }
    }
    protected class RedComponent extends FloatComponent {
        protected Value getValue() {
            return valueProvider.getValue().getRed();
        }
        public void setCssText(String cssText) throws DOMException {
            if (handler == null) {
                throw new DOMException
                    (DOMException.NO_MODIFICATION_ALLOWED_ERR, "");
            } else {
                getValue();
                handler.redTextChanged(cssText);
            }
        }
        public void setFloatValue(short unitType, float floatValue)
            throws DOMException {
            if (handler == null) {
                throw new DOMException
                    (DOMException.NO_MODIFICATION_ALLOWED_ERR, "");
            } else {
                getValue();
                handler.redFloatValueChanged(unitType, floatValue);
            }
        }
    }
    protected class GreenComponent extends FloatComponent {
        protected Value getValue() {
            return valueProvider.getValue().getGreen();
        }
        public void setCssText(String cssText) throws DOMException {
            if (handler == null) {
                throw new DOMException
                    (DOMException.NO_MODIFICATION_ALLOWED_ERR, "");
            } else {
                getValue();
                handler.greenTextChanged(cssText);
            }
        }
        public void setFloatValue(short unitType, float floatValue)
            throws DOMException {
            if (handler == null) {
                throw new DOMException
                    (DOMException.NO_MODIFICATION_ALLOWED_ERR, "");
            } else {
                getValue();
                handler.greenFloatValueChanged(unitType, floatValue);
            }
        }
    }
    protected class BlueComponent extends FloatComponent {
        protected Value getValue() {
            return valueProvider.getValue().getBlue();
        }
        public void setCssText(String cssText) throws DOMException {
            if (handler == null) {
                throw new DOMException
                    (DOMException.NO_MODIFICATION_ALLOWED_ERR, "");
            } else {
                getValue();
                handler.blueTextChanged(cssText);
            }
        }
        public void setFloatValue(short unitType, float floatValue)
            throws DOMException {
            if (handler == null) {
                throw new DOMException
                    (DOMException.NO_MODIFICATION_ALLOWED_ERR, "");
            } else {
                getValue();
                handler.blueFloatValueChanged(unitType, floatValue);
            }
        }
    }
}

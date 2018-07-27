package org.apache.batik.css.dom;
import org.apache.batik.css.engine.value.FloatValue;
import org.apache.batik.css.engine.value.ListValue;
import org.apache.batik.css.engine.value.StringValue;
import org.apache.batik.css.engine.value.Value;
import org.w3c.dom.DOMException;
import org.w3c.dom.css.CSSPrimitiveValue;
import org.w3c.dom.css.CSSValue;
import org.w3c.dom.css.CSSValueList;
import org.w3c.dom.css.Counter;
import org.w3c.dom.css.RGBColor;
import org.w3c.dom.css.Rect;
public class CSSOMValue
    implements CSSPrimitiveValue,
               CSSValueList,
               Counter,
               Rect,
               RGBColor {
    protected ValueProvider valueProvider;
    protected ModificationHandler handler;
    protected LeftComponent leftComponent;
    protected RightComponent rightComponent;
    protected BottomComponent bottomComponent;
    protected TopComponent topComponent;
    protected RedComponent redComponent;
    protected GreenComponent greenComponent;
    protected BlueComponent blueComponent;
    protected CSSValue[] items;
    public CSSOMValue(ValueProvider vp) {
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
            handler.textChanged(cssText);
        }
    }
    public short getCssValueType() {
        return valueProvider.getValue().getCssValueType();
    }
    public short getPrimitiveType() {
        return valueProvider.getValue().getPrimitiveType();
    }
    public void setFloatValue(short unitType, float floatValue)
        throws DOMException {
        if (handler == null) {
            throw new DOMException
                (DOMException.NO_MODIFICATION_ALLOWED_ERR, "");
        } else {
            handler.floatValueChanged(unitType, floatValue);
        }
    }
    public float getFloatValue(short unitType) throws DOMException {
        return convertFloatValue(unitType, valueProvider.getValue());
    }
    public static float convertFloatValue(short unitType, Value value) {
        switch (unitType) {
        case CSSPrimitiveValue.CSS_NUMBER:
        case CSSPrimitiveValue.CSS_PERCENTAGE:
        case CSSPrimitiveValue.CSS_EMS:
        case CSSPrimitiveValue.CSS_EXS:
        case CSSPrimitiveValue.CSS_DIMENSION:
        case CSSPrimitiveValue.CSS_PX:
            if (value.getPrimitiveType() == unitType) {
                return value.getFloatValue();
            }
            break;
        case CSSPrimitiveValue.CSS_CM:
            return toCentimeters(value);
        case CSSPrimitiveValue.CSS_MM:
            return toMillimeters(value);
        case CSSPrimitiveValue.CSS_IN:
            return toInches(value);
        case CSSPrimitiveValue.CSS_PT:
            return toPoints(value);
        case CSSPrimitiveValue.CSS_PC:
            return toPicas(value);
        case CSSPrimitiveValue.CSS_DEG:
            return toDegrees(value);
        case CSSPrimitiveValue.CSS_RAD:
            return toRadians(value);
        case CSSPrimitiveValue.CSS_GRAD:
            return toGradians(value);
        case CSSPrimitiveValue.CSS_MS:
            return toMilliseconds(value);
        case CSSPrimitiveValue.CSS_S:
            return toSeconds(value);
        case CSSPrimitiveValue.CSS_HZ:
            return toHertz(value);
        case CSSPrimitiveValue.CSS_KHZ:
            return tokHertz(value);
        }
        throw new DOMException(DOMException.INVALID_ACCESS_ERR, "");
    }
    protected static float toCentimeters(Value value) {
        switch (value.getPrimitiveType()) {
        case CSSPrimitiveValue.CSS_CM:
            return value.getFloatValue();
        case CSSPrimitiveValue.CSS_MM:
            return (value.getFloatValue() / 10);
        case CSSPrimitiveValue.CSS_IN:
            return (value.getFloatValue() * 2.54f);
        case CSSPrimitiveValue.CSS_PT:
            return (value.getFloatValue() * 2.54f / 72);
        case CSSPrimitiveValue.CSS_PC:
            return (value.getFloatValue() * 2.54f / 6);
        default:
            throw new DOMException(DOMException.INVALID_ACCESS_ERR, "");
        }
    }
    protected static float toInches(Value value) {
        switch (value.getPrimitiveType()) {
        case CSSPrimitiveValue.CSS_CM:
            return (value.getFloatValue() / 2.54f);
        case CSSPrimitiveValue.CSS_MM:
            return (value.getFloatValue() / 25.4f);
        case CSSPrimitiveValue.CSS_IN:
            return value.getFloatValue();
        case CSSPrimitiveValue.CSS_PT:
            return (value.getFloatValue() / 72);
        case CSSPrimitiveValue.CSS_PC:
            return (value.getFloatValue() / 6);
        default:
            throw new DOMException(DOMException.INVALID_ACCESS_ERR, "");
        }
    }
    protected static float toMillimeters(Value value) {
        switch (value.getPrimitiveType()) {
        case CSSPrimitiveValue.CSS_CM:
            return (value.getFloatValue() * 10);
        case CSSPrimitiveValue.CSS_MM:
            return value.getFloatValue();
        case CSSPrimitiveValue.CSS_IN:
            return (value.getFloatValue() * 25.4f);
        case CSSPrimitiveValue.CSS_PT:
            return (value.getFloatValue() * 25.4f / 72);
        case CSSPrimitiveValue.CSS_PC:
            return (value.getFloatValue() * 25.4f / 6);
        default:
            throw new DOMException(DOMException.INVALID_ACCESS_ERR, "");
        }
    }
    protected static float toPoints(Value value) {
        switch (value.getPrimitiveType()) {
        case CSSPrimitiveValue.CSS_CM:
            return (value.getFloatValue() * 72 / 2.54f);
        case CSSPrimitiveValue.CSS_MM:
            return (value.getFloatValue() * 72 / 25.4f);
        case CSSPrimitiveValue.CSS_IN:
            return (value.getFloatValue() * 72);
        case CSSPrimitiveValue.CSS_PT:
            return value.getFloatValue();
        case CSSPrimitiveValue.CSS_PC:
            return (value.getFloatValue() * 12);
        default:
            throw new DOMException(DOMException.INVALID_ACCESS_ERR, "");
        }
    }
    protected static float toPicas(Value value) {
        switch (value.getPrimitiveType()) {
        case CSSPrimitiveValue.CSS_CM:
            return (value.getFloatValue() * 6 / 2.54f);
        case CSSPrimitiveValue.CSS_MM:
            return (value.getFloatValue() * 6 / 25.4f);
        case CSSPrimitiveValue.CSS_IN:
            return (value.getFloatValue() * 6);
        case CSSPrimitiveValue.CSS_PT:
            return (value.getFloatValue() / 12);
        case CSSPrimitiveValue.CSS_PC:
            return value.getFloatValue();
        default:
            throw new DOMException(DOMException.INVALID_ACCESS_ERR, "");
        }
    }
    protected static float toDegrees(Value value) {
        switch (value.getPrimitiveType()) {
        case CSSPrimitiveValue.CSS_DEG:
            return value.getFloatValue();
        case CSSPrimitiveValue.CSS_RAD:
            return (float) Math.toDegrees( value.getFloatValue() );
        case CSSPrimitiveValue.CSS_GRAD:
            return (value.getFloatValue() * 9 / 5);
        default:
            throw new DOMException(DOMException.INVALID_ACCESS_ERR, "");
        }
    }
    protected static float toRadians(Value value) {
        switch (value.getPrimitiveType()) {
        case CSSPrimitiveValue.CSS_DEG:
            return (value.getFloatValue() * 5 / 9);      
        case CSSPrimitiveValue.CSS_RAD:
            return value.getFloatValue();
        case CSSPrimitiveValue.CSS_GRAD:
            return (float)(value.getFloatValue() * 100 / Math.PI);
        default:
            throw new DOMException(DOMException.INVALID_ACCESS_ERR, "");
        }
    }
    protected static float toGradians(Value value) {
        switch (value.getPrimitiveType()) {
        case CSSPrimitiveValue.CSS_DEG:
            return (float)(value.getFloatValue() * Math.PI / 180);   
        case CSSPrimitiveValue.CSS_RAD:
            return (float)(value.getFloatValue() * Math.PI / 100);
        case CSSPrimitiveValue.CSS_GRAD:
            return value.getFloatValue();
        default:
            throw new DOMException(DOMException.INVALID_ACCESS_ERR, "");
        }
    }
    protected static float toMilliseconds(Value value) {
        switch (value.getPrimitiveType()) {
        case CSSPrimitiveValue.CSS_MS:
            return value.getFloatValue();
        case CSSPrimitiveValue.CSS_S:
            return (value.getFloatValue() * 1000);
        default:
            throw new DOMException(DOMException.INVALID_ACCESS_ERR, "");
        }
    }
    protected static float toSeconds(Value value) {
        switch (value.getPrimitiveType()) {
        case CSSPrimitiveValue.CSS_MS:
            return (value.getFloatValue() / 1000);
        case CSSPrimitiveValue.CSS_S:
            return value.getFloatValue();
        default:
            throw new DOMException(DOMException.INVALID_ACCESS_ERR, "");
        }
    }
    protected static float toHertz(Value value) {
        switch (value.getPrimitiveType()) {
        case CSSPrimitiveValue.CSS_HZ:
            return value.getFloatValue();
        case CSSPrimitiveValue.CSS_KHZ:
            return (value.getFloatValue() / 1000);
        default:
            throw new DOMException(DOMException.INVALID_ACCESS_ERR, "");
        }
    }
    protected static float tokHertz(Value value) {
        switch (value.getPrimitiveType()) {
        case CSSPrimitiveValue.CSS_HZ:
            return (value.getFloatValue() * 1000);
        case CSSPrimitiveValue.CSS_KHZ:
            return value.getFloatValue();
        default:
            throw new DOMException(DOMException.INVALID_ACCESS_ERR, "");
        }
    }
    public void setStringValue(short stringType, String stringValue)
        throws DOMException {
        if (handler == null) {
            throw new DOMException
                (DOMException.NO_MODIFICATION_ALLOWED_ERR, "");
        } else {
            handler.stringValueChanged(stringType, stringValue);
        }
    }
    public String getStringValue() throws DOMException {
        return valueProvider.getValue().getStringValue();
    }
    public Counter getCounterValue() throws DOMException {
        return this;
    }
    public Rect getRectValue() throws DOMException {
        return this;
    }
    public RGBColor getRGBColorValue() throws DOMException {
        return this;
    }
    public int getLength() {
        return valueProvider.getValue().getLength();
    }
    public CSSValue item(int index) {
        int len = valueProvider.getValue().getLength();
        if (index < 0 || index >= len) {
            return null;
        }
        if (items == null) {
            items = new CSSValue[valueProvider.getValue().getLength()];
        } else if (items.length < len) {
            CSSValue[] nitems = new CSSValue[len];
            System.arraycopy( items, 0, nitems, 0, items.length );
            items = nitems;
        }
        CSSValue result = items[index];
        if (result == null) {
            items[index] = result = new ListComponent(index);
        }
        return result;
    }
    public String getIdentifier() {
        return valueProvider.getValue().getIdentifier();
    }
    public String getListStyle() {
        return valueProvider.getValue().getListStyle();
    }
    public String getSeparator() {
        return valueProvider.getValue().getSeparator();
    }
    public CSSPrimitiveValue getTop() {
        valueProvider.getValue().getTop();
        if (topComponent == null) {
            topComponent = new TopComponent();
        }
        return topComponent;
    }
    public CSSPrimitiveValue getRight() {
        valueProvider.getValue().getRight();
        if (rightComponent == null) {
            rightComponent = new RightComponent();
        }
        return rightComponent;
    }
    public CSSPrimitiveValue getBottom() {
        valueProvider.getValue().getBottom();
        if (bottomComponent == null) {
            bottomComponent = new BottomComponent();
        }
        return bottomComponent;
    }
    public CSSPrimitiveValue getLeft() {
        valueProvider.getValue().getLeft();
        if (leftComponent == null) {
            leftComponent = new LeftComponent();
        }
        return leftComponent;
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
    public interface ValueProvider {
        Value getValue();
    }
    public interface ModificationHandler {
        void textChanged(String text) throws DOMException;
        void floatValueChanged(short unit, float value) throws DOMException;
        void stringValueChanged(short type, String value) throws DOMException;
        void leftTextChanged(String text) throws DOMException;
        void leftFloatValueChanged(short unit, float value)
            throws DOMException;
        void topTextChanged(String text) throws DOMException;
        void topFloatValueChanged(short unit, float value)
            throws DOMException;
        void rightTextChanged(String text) throws DOMException;
        void rightFloatValueChanged(short unit, float value)
            throws DOMException;
        void bottomTextChanged(String text) throws DOMException;
        void bottomFloatValueChanged(short unit, float value)
            throws DOMException;
        void redTextChanged(String text) throws DOMException;
        void redFloatValueChanged(short unit, float value)
            throws DOMException;
        void greenTextChanged(String text) throws DOMException;
        void greenFloatValueChanged(short unit, float value)
            throws DOMException;
        void blueTextChanged(String text) throws DOMException;
        void blueFloatValueChanged(short unit, float value)
            throws DOMException;
        void listTextChanged(int idx, String text) throws DOMException;
        void listFloatValueChanged(int idx, short unit, float value)
            throws DOMException;
        void listStringValueChanged(int idx, short unit, String value)
            throws DOMException;
    }
    public abstract class AbstractModificationHandler
        implements ModificationHandler {
        protected abstract Value getValue();
        public void floatValueChanged(short unit, float value)
            throws DOMException {
            textChanged(FloatValue.getCssText(unit, value));
        }
        public void stringValueChanged(short type, String value)
            throws DOMException {
            textChanged(StringValue.getCssText(type, value));
        }
        public void leftTextChanged(String text) throws DOMException {
            final Value val = getValue();
            text = "rect(" +
                val.getTop().getCssText() + ", " +
                val.getRight().getCssText() + ", " +
                val.getBottom().getCssText() + ", " +
                text + ')';
            textChanged(text);
        }
        public void leftFloatValueChanged(short unit, float value)
            throws DOMException {
            final Value val = getValue();
            String text = "rect(" +
                val.getTop().getCssText() + ", " +
                val.getRight().getCssText() + ", " +
                val.getBottom().getCssText() + ", " +
                FloatValue.getCssText(unit, value) + ')';
            textChanged(text);
        }
        public void topTextChanged(String text) throws DOMException {
            final Value val = getValue();
            text = "rect(" +
                text + ", " +
                val.getRight().getCssText() + ", " +
                val.getBottom().getCssText() + ", " +
                val.getLeft().getCssText() + ')';
            textChanged(text);
        }
        public void topFloatValueChanged(short unit, float value)
            throws DOMException {
            final Value val = getValue();
            String text = "rect(" +
                FloatValue.getCssText(unit, value) + ", " +
                val.getRight().getCssText() + ", " +
                val.getBottom().getCssText() + ", " +
                val.getLeft().getCssText() + ')';
            textChanged(text);
        }
        public void rightTextChanged(String text) throws DOMException {
            final Value val = getValue();
            text = "rect(" +
                val.getTop().getCssText() + ", " +
                text + ", " +
                val.getBottom().getCssText() + ", " +
                val.getLeft().getCssText() + ')';
            textChanged(text);
        }
        public void rightFloatValueChanged(short unit, float value)
            throws DOMException {
            final Value val = getValue();
            String text = "rect(" +
                val.getTop().getCssText() + ", " +
                FloatValue.getCssText(unit, value) + ", " +
                val.getBottom().getCssText() + ", " +
                val.getLeft().getCssText() + ')';
            textChanged(text);
        }
        public void bottomTextChanged(String text) throws DOMException {
            final Value val = getValue();
            text = "rect(" +
                val.getTop().getCssText() + ", " +
                val.getRight().getCssText() + ", " +
                text + ", " +
                val.getLeft().getCssText() + ')';
            textChanged(text);
        }
        public void bottomFloatValueChanged(short unit, float value)
            throws DOMException {
            final Value val = getValue();
            String text = "rect(" +
                val.getTop().getCssText() + ", " +
                val.getRight().getCssText() + ", " +
                FloatValue.getCssText(unit, value) + ", " +
                val.getLeft().getCssText() + ')';
            textChanged(text);
        }
        public void redTextChanged(String text) throws DOMException {
            final Value val = getValue();
            text = "rgb(" +
                text + ", " +
                val.getGreen().getCssText() + ", " +
                val.getBlue().getCssText() + ')';
            textChanged(text);
        }
        public void redFloatValueChanged(short unit, float value)
            throws DOMException {
            final Value val = getValue();
            String text = "rgb(" +
                FloatValue.getCssText(unit, value) + ", " +
                val.getGreen().getCssText() + ", " +
                val.getBlue().getCssText() + ')';
            textChanged(text);
        }
        public void greenTextChanged(String text) throws DOMException {
            final Value val = getValue();
            text = "rgb(" +
                val.getRed().getCssText() + ", " +
                text + ", " +
                val.getBlue().getCssText() + ')';
            textChanged(text);
        }
        public void greenFloatValueChanged(short unit, float value)
            throws DOMException {
            final Value val = getValue();
            String text = "rgb(" +
                val.getRed().getCssText() + ", " +
                FloatValue.getCssText(unit, value) + ", " +
                val.getBlue().getCssText() + ')';
            textChanged(text);
        }
        public void blueTextChanged(String text) throws DOMException {
            final Value val = getValue();
            text = "rgb(" +
                val.getRed().getCssText() + ", " +
                val.getGreen().getCssText() + ", " +
                text + ')';
            textChanged(text);
        }
        public void blueFloatValueChanged(short unit, float value)
            throws DOMException {
            final Value val = getValue();
            String text = "rgb(" +
                val.getRed().getCssText() + ", " +
                val.getGreen().getCssText() + ", " +
                FloatValue.getCssText(unit, value) + ')';
            textChanged(text);
        }
        public void listTextChanged(int idx, String text) throws DOMException {
            ListValue lv = (ListValue)getValue();
            int len = lv.getLength();
            StringBuffer sb = new StringBuffer( len * 8 );
            for (int i = 0; i < idx; i++) {
                sb.append(lv.item(i).getCssText());
                sb.append(lv.getSeparatorChar());
            }
            sb.append(text);
            for (int i = idx + 1; i < len; i++) {
                sb.append(lv.getSeparatorChar());
                sb.append(lv.item(i).getCssText());
            }
            text = sb.toString();
            textChanged(text);
        }
        public void listFloatValueChanged(int idx, short unit, float value)
            throws DOMException {
            ListValue lv = (ListValue)getValue();
            int len = lv.getLength();
            StringBuffer sb = new StringBuffer( len * 8 );
            for (int i = 0; i < idx; i++) {
                sb.append(lv.item(i).getCssText());
                sb.append(lv.getSeparatorChar());
            }
            sb.append(FloatValue.getCssText(unit, value));
            for (int i = idx + 1; i < len; i++) {
                sb.append(lv.getSeparatorChar());
                sb.append(lv.item(i).getCssText());
            }
            textChanged(sb.toString());
        }
        public void listStringValueChanged(int idx, short unit, String value)
            throws DOMException {
            ListValue lv = (ListValue)getValue();
            int len = lv.getLength();
            StringBuffer sb = new StringBuffer( len * 8 );
            for (int i = 0; i < idx; i++) {
                sb.append(lv.item(i).getCssText());
                sb.append(lv.getSeparatorChar());
            }
            sb.append(StringValue.getCssText(unit, value));
            for (int i = idx + 1; i < len; i++) {
                sb.append(lv.getSeparatorChar());
                sb.append(lv.item(i).getCssText());
            }
            textChanged(sb.toString());
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
            return convertFloatValue(unitType, getValue());
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
    protected class LeftComponent extends FloatComponent {
        protected Value getValue() {
            return valueProvider.getValue().getLeft();
        }
        public void setCssText(String cssText) throws DOMException {
            if (handler == null) {
                throw new DOMException
                    (DOMException.NO_MODIFICATION_ALLOWED_ERR, "");
            } else {
                getValue();
                handler.leftTextChanged(cssText);
            }
        }
        public void setFloatValue(short unitType, float floatValue)
            throws DOMException {
            if (handler == null) {
                throw new DOMException
                    (DOMException.NO_MODIFICATION_ALLOWED_ERR, "");
            } else {
                getValue();
                handler.leftFloatValueChanged(unitType, floatValue);
            }
        }
    }
    protected class TopComponent extends FloatComponent {
        protected Value getValue() {
            return valueProvider.getValue().getTop();
        }
        public void setCssText(String cssText) throws DOMException {
            if (handler == null) {
                throw new DOMException
                    (DOMException.NO_MODIFICATION_ALLOWED_ERR, "");
            } else {
                getValue();
                handler.topTextChanged(cssText);
            }
        }
        public void setFloatValue(short unitType, float floatValue)
            throws DOMException {
            if (handler == null) {
                throw new DOMException
                    (DOMException.NO_MODIFICATION_ALLOWED_ERR, "");
            } else {
                getValue();
                handler.topFloatValueChanged(unitType, floatValue);
            }
        }
    }
    protected class RightComponent extends FloatComponent {
        protected Value getValue() {
            return valueProvider.getValue().getRight();
        }
        public void setCssText(String cssText) throws DOMException {
            if (handler == null) {
                throw new DOMException
                    (DOMException.NO_MODIFICATION_ALLOWED_ERR, "");
            } else {
                getValue();
                handler.rightTextChanged(cssText);
            }
        }
        public void setFloatValue(short unitType, float floatValue)
            throws DOMException {
            if (handler == null) {
                throw new DOMException
                    (DOMException.NO_MODIFICATION_ALLOWED_ERR, "");
            } else {
                getValue();
                handler.rightFloatValueChanged(unitType, floatValue);
            }
        }
    }
    protected class BottomComponent extends FloatComponent {
        protected Value getValue() {
            return valueProvider.getValue().getBottom();
        }
        public void setCssText(String cssText) throws DOMException {
            if (handler == null) {
                throw new DOMException
                    (DOMException.NO_MODIFICATION_ALLOWED_ERR, "");
            } else {
                getValue();
                handler.bottomTextChanged(cssText);
            }
        }
        public void setFloatValue(short unitType, float floatValue)
            throws DOMException {
            if (handler == null) {
                throw new DOMException
                    (DOMException.NO_MODIFICATION_ALLOWED_ERR, "");
            } else {
                getValue();
                handler.bottomFloatValueChanged(unitType, floatValue);
            }
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
    protected class ListComponent extends AbstractComponent {
        protected int index;
        public ListComponent(int idx) {
            index = idx;
        }
        protected Value getValue() {
            if (index >= valueProvider.getValue().getLength()) {
                throw new DOMException
                    (DOMException.NO_MODIFICATION_ALLOWED_ERR, "");
            }
            return valueProvider.getValue().item(index);
        }
        public void setCssText(String cssText) throws DOMException {
            if (handler == null) {
                throw new DOMException
                    (DOMException.NO_MODIFICATION_ALLOWED_ERR, "");
            } else {
                getValue();
                handler.listTextChanged(index, cssText);
            }
        }
        public void setFloatValue(short unitType, float floatValue)
            throws DOMException {
            if (handler == null) {
                throw new DOMException
                    (DOMException.NO_MODIFICATION_ALLOWED_ERR, "");
            } else {
                getValue();
                handler.listFloatValueChanged(index, unitType, floatValue);
            }
        }
        public void setStringValue(short stringType, String stringValue)
            throws DOMException {
            if (handler == null) {
                throw new DOMException
                    (DOMException.NO_MODIFICATION_ALLOWED_ERR, "");
            } else {
                getValue();
                handler.listStringValueChanged(index, stringType, stringValue);
            }
        }
    }
}

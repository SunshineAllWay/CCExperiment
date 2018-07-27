package org.apache.batik.css.dom;
import org.apache.batik.css.engine.CSSEngine;
import org.apache.batik.css.engine.SVGCSSEngine;
import org.apache.batik.css.engine.value.Value;
import org.apache.batik.css.engine.value.svg.SVGColorManager;
import org.apache.batik.css.engine.value.svg.SVGPaintManager;
import org.w3c.dom.DOMException;
import org.w3c.dom.css.CSSRule;
import org.w3c.dom.css.CSSValue;
public class CSSOMSVGStyleDeclaration extends CSSOMStyleDeclaration {
    protected CSSEngine cssEngine;
    public CSSOMSVGStyleDeclaration(ValueProvider vp,
                                    CSSRule parent,
                                    CSSEngine eng) {
        super(vp, parent);
        cssEngine = eng;
    }
    protected CSSValue createCSSValue(String name) {
        int idx = cssEngine.getPropertyIndex(name);
        if (idx > SVGCSSEngine.FINAL_INDEX) {
            if (cssEngine.getValueManagers()[idx] instanceof SVGPaintManager) {
                return new StyleDeclarationPaintValue(name);
            }
            if (cssEngine.getValueManagers()[idx] instanceof SVGColorManager) {
                return new StyleDeclarationColorValue(name);
            }
        } else {
            switch (idx) {
            case SVGCSSEngine.FILL_INDEX:
            case SVGCSSEngine.STROKE_INDEX:
                return new StyleDeclarationPaintValue(name);
            case SVGCSSEngine.FLOOD_COLOR_INDEX:
            case SVGCSSEngine.LIGHTING_COLOR_INDEX:
            case SVGCSSEngine.STOP_COLOR_INDEX:
                return new StyleDeclarationColorValue(name);
            }
        }
        return super.createCSSValue(name);
    }
    public class StyleDeclarationColorValue
        extends CSSOMSVGColor
        implements CSSOMSVGColor.ValueProvider {
        protected String property;
        public StyleDeclarationColorValue(String prop) {
            super(null);
            valueProvider = this;
            setModificationHandler(new AbstractModificationHandler() {
                    protected Value getValue() {
                        return StyleDeclarationColorValue.this.getValue();
                    }
                    public void textChanged(String text) throws DOMException {
                        if (handler == null) {
                            throw new DOMException
                                (DOMException.NO_MODIFICATION_ALLOWED_ERR, "");
                        }
                        String prio = getPropertyPriority(property);
                        CSSOMSVGStyleDeclaration.this.
                            handler.propertyChanged(property, text, prio);
                    }
                });
            property = prop;
        }
        public Value getValue() {
            return CSSOMSVGStyleDeclaration.this.
                valueProvider.getValue(property);
        }
    }
    public class StyleDeclarationPaintValue
        extends CSSOMSVGPaint
        implements CSSOMSVGPaint.ValueProvider {
        protected String property;
        public StyleDeclarationPaintValue(String prop) {
            super(null);
            valueProvider = this;
            setModificationHandler(new AbstractModificationHandler() {
                    protected Value getValue() {
                        return StyleDeclarationPaintValue.this.getValue();
                    }
                    public void textChanged(String text) throws DOMException {
                        if (handler == null) {
                            throw new DOMException
                                (DOMException.NO_MODIFICATION_ALLOWED_ERR, "");
                        }
                        String prio = getPropertyPriority(property);
                        CSSOMSVGStyleDeclaration.this.
                            handler.propertyChanged(property, text, prio);
                    }
                });
            property = prop;
        }
        public Value getValue() {
            return CSSOMSVGStyleDeclaration.this.
                valueProvider.getValue(property);
        }
    }
}

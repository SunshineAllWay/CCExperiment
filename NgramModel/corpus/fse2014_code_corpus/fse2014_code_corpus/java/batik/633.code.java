package org.apache.batik.dom.svg;
import java.awt.geom.AffineTransform;
import org.apache.batik.anim.values.AnimatableMotionPointValue;
import org.apache.batik.anim.values.AnimatableValue;
import org.apache.batik.dom.AbstractDocument;
import org.apache.batik.util.DoublyIndexedTable;
import org.apache.batik.util.SVGTypes;
import org.w3c.dom.Node;
import org.w3c.dom.svg.SVGAnimatedTransformList;
import org.w3c.dom.svg.SVGElement;
import org.w3c.dom.svg.SVGException;
import org.w3c.dom.svg.SVGMatrix;
import org.w3c.dom.svg.SVGRect;
import org.w3c.dom.svg.SVGTextElement;
public class SVGOMTextElement
    extends    SVGOMTextPositioningElement
    implements SVGTextElement,
               SVGMotionAnimatableElement {
    protected static final String X_DEFAULT_VALUE = "0";
    protected static final String Y_DEFAULT_VALUE = "0";
    protected static DoublyIndexedTable xmlTraitInformation;
    static {
        DoublyIndexedTable t =
            new DoublyIndexedTable(SVGOMTextPositioningElement.xmlTraitInformation);
        t.put(null, SVG_TRANSFORM_ATTRIBUTE,
                new TraitInformation(true, SVGTypes.TYPE_TRANSFORM_LIST));
        xmlTraitInformation = t;
    }
    protected SVGOMAnimatedTransformList transform;
    protected AffineTransform motionTransform;
    protected SVGOMTextElement() {
    }
    public SVGOMTextElement(String prefix, AbstractDocument owner) {
        super(prefix, owner);
        initializeLiveAttributes();
    }
    protected void initializeAllLiveAttributes() {
        super.initializeAllLiveAttributes();
        initializeLiveAttributes();
    }
    private void initializeLiveAttributes() {
        transform =
            createLiveAnimatedTransformList(null, SVG_TRANSFORM_ATTRIBUTE, "");
    }
    public String getLocalName() {
        return SVG_TEXT_TAG;
    }
    public SVGElement getNearestViewportElement() {
        return SVGLocatableSupport.getNearestViewportElement(this);
    }
    public SVGElement getFarthestViewportElement() {
        return SVGLocatableSupport.getFarthestViewportElement(this);
    }
    public SVGRect getBBox() {
        return SVGLocatableSupport.getBBox(this);
    }
    public SVGMatrix getCTM() {
        return SVGLocatableSupport.getCTM(this);
    }
    public SVGMatrix getScreenCTM() {
        return SVGLocatableSupport.getScreenCTM(this);
    }
    public SVGMatrix getTransformToElement(SVGElement element)
        throws SVGException {
        return SVGLocatableSupport.getTransformToElement(this, element);
    }
    public SVGAnimatedTransformList getTransform() {
        return transform;
    }
    protected String getDefaultXValue() {
        return X_DEFAULT_VALUE;
    }
    protected String getDefaultYValue() {
        return Y_DEFAULT_VALUE;
    }
    protected Node newNode() {
        return new SVGOMTextElement();
    }
    protected DoublyIndexedTable getTraitInformationTable() {
        return xmlTraitInformation;
    }
    public AffineTransform getMotionTransform() {
        return motionTransform;
    }
    public void updateOtherValue(String type, AnimatableValue val) {
        if (type.equals("motion")) {
            if (motionTransform == null) {
                motionTransform = new AffineTransform();
            }
            if (val == null) {
                motionTransform.setToIdentity();
            } else {
                AnimatableMotionPointValue p = (AnimatableMotionPointValue) val;
                motionTransform.setToTranslation(p.getX(), p.getY());
                motionTransform.rotate(p.getAngle());
            }
            SVGOMDocument d = (SVGOMDocument) ownerDocument;
            d.getAnimatedAttributeListener().otherAnimationChanged(this, type);
        } else {
            super.updateOtherValue(type, val);
        }
    }
}

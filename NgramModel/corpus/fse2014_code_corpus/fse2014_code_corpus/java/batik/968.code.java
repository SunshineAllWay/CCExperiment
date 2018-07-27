package org.apache.batik.gvt;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.text.AttributedCharacterIterator;
import java.text.CharacterIterator;
import java.util.List;
import org.apache.batik.gvt.renderer.StrokingTextPainter;
import org.apache.batik.gvt.text.AttributedCharacterSpanIterator;
import org.apache.batik.gvt.text.GVTAttributedCharacterIterator;
import org.apache.batik.gvt.text.Mark;
import org.apache.batik.gvt.text.TextHit;
import org.apache.batik.gvt.text.TextPaintInfo;
import org.apache.batik.gvt.text.TextSpanLayout;
public class TextNode extends AbstractGraphicsNode implements Selectable {
    public static final 
        AttributedCharacterIterator.Attribute PAINT_INFO =
        GVTAttributedCharacterIterator.TextAttribute.PAINT_INFO;
    protected Point2D location = new Point2D.Float(0, 0);
    protected AttributedCharacterIterator aci;
    protected String text;
    protected Mark beginMark = null;
    protected Mark endMark = null;
    protected List textRuns;
    protected TextPainter textPainter = StrokingTextPainter.getInstance();
    private Rectangle2D geometryBounds;
    private Rectangle2D primitiveBounds;
    private Shape outline;
    public TextNode() {
    }
    public void setTextPainter(TextPainter textPainter) {
        if (textPainter == null) {
            this.textPainter = StrokingTextPainter.getInstance();
        } else {
            this.textPainter = textPainter;
        }
    }
    public TextPainter getTextPainter() {
        return textPainter;
    }
    public List getTextRuns() {
        return textRuns;
    }
    public void setTextRuns(List textRuns) {
        this.textRuns = textRuns;
    }
    public String getText() {
        if (text != null) 
            return text;
        if (aci == null) {
            text = "";
        } else {
            StringBuffer buf = new StringBuffer(aci.getEndIndex());
            for (char c = aci.first();
                 c != CharacterIterator.DONE;
                 c = aci.next()) {
                buf.append(c);
            }
            text = buf.toString();
        }
        return text;
    }
    public void setLocation(Point2D newLocation){
        fireGraphicsNodeChangeStarted();
        invalidateGeometryCache();
        this.location = newLocation;
        fireGraphicsNodeChangeCompleted();
    }
    public Point2D getLocation(){
        return location;
    }
    public void swapTextPaintInfo(TextPaintInfo newInfo, 
                                  TextPaintInfo oldInfo) {
        fireGraphicsNodeChangeStarted();
        invalidateGeometryCache();
        oldInfo.set(newInfo);
        fireGraphicsNodeChangeCompleted();
    }
    public void setAttributedCharacterIterator
        (AttributedCharacterIterator newAci) {
        fireGraphicsNodeChangeStarted();
        invalidateGeometryCache();
        this.aci = newAci;
        text = null;
        textRuns = null;
        fireGraphicsNodeChangeCompleted();
    }
    public AttributedCharacterIterator getAttributedCharacterIterator(){
        return aci;
    }
    protected void invalidateGeometryCache() {
        super.invalidateGeometryCache();
        primitiveBounds = null;
        geometryBounds = null;
        outline = null;
    }
    public Rectangle2D getPrimitiveBounds(){
        if (primitiveBounds == null) {
            if (aci != null) {
                primitiveBounds = textPainter.getBounds2D(this);
            }
        }
        return primitiveBounds;
    }
    public Rectangle2D getGeometryBounds(){
        if (geometryBounds == null){
            if (aci != null) {
                geometryBounds = textPainter.getGeometryBounds(this);
            }
        }
        return geometryBounds;
    }
    public Rectangle2D getSensitiveBounds() {
        return getGeometryBounds();
    }
    public Shape getOutline() {
        if (outline == null) {
            if (aci != null) {
                outline = textPainter.getOutline(this);
            }
        }
        return outline;
    }
    public Mark getMarkerForChar(int index, boolean beforeChar) {
        return textPainter.getMark(this, index, beforeChar);
    }
    public void setSelection(Mark begin, Mark end) {
        if ((begin.getTextNode() != this) ||
            (end.getTextNode() != this))
            throw new Error("Markers not from this TextNode");
        beginMark = begin;
        endMark   = end;
    }
    public boolean selectAt(double x, double y) {
        beginMark = textPainter.selectAt(x, y, this);
        return true; 
    }
    public boolean selectTo(double x, double y) {
        Mark tmpMark = textPainter.selectTo(x, y, beginMark);
        if (tmpMark == null)
            return false;
        if (tmpMark != endMark) {
            endMark = tmpMark;
            return true;
        }
        return false;
    }
    public boolean selectAll(double x, double y) {
        beginMark = textPainter.selectFirst(this);
        endMark   = textPainter.selectLast(this);
        return true; 
    }
    public Object getSelection() {
        Object o = null;
        if (aci == null) return o;
        int[] ranges = textPainter.getSelected(beginMark, endMark);
        if ((ranges != null) && (ranges.length > 1)) {
            if (ranges[0] > ranges[1]) {
                int temp = ranges[1];
                ranges[1] = ranges[0];
                ranges[0] = temp;
            }
            o = new AttributedCharacterSpanIterator
                (aci, ranges[0], ranges[1]+1);
        }
        return o;
    }
    public Shape getHighlightShape() {
        Shape highlightShape =
            textPainter.getHighlightShape(beginMark, endMark);
        AffineTransform t = getGlobalTransform();
        highlightShape = t.createTransformedShape(highlightShape);
        return highlightShape;
    }
    public void primitivePaint(Graphics2D g2d) {
        Shape clip = g2d.getClip();
        if (clip != null && !(clip instanceof GeneralPath)) {
            g2d.setClip(new GeneralPath(clip));
        }
        textPainter.paint(this, g2d);
    }
    public boolean contains(Point2D p) {
        if (!super.contains(p)) {
            return false;
        }
        List list = getTextRuns();
        for (int i = 0 ; i < list.size(); i++) {
            StrokingTextPainter.TextRun run =
                (StrokingTextPainter.TextRun)list.get(i);
            TextSpanLayout layout = run.getLayout();
            float x = (float)p.getX();
            float y = (float)p.getY();
            TextHit textHit = layout.hitTestChar(x, y);
            if (textHit != null && contains(p, layout.getBounds2D())) {
                return true;
            }
        }
        return false;
    }
    protected boolean contains(Point2D p, Rectangle2D b) {
        if (b == null || !b.contains(p)) {
            return false;
        }
        switch(pointerEventType) {
        case VISIBLE_PAINTED:
        case VISIBLE_FILL:
        case VISIBLE_STROKE:
        case VISIBLE:
            return isVisible;
        case PAINTED:
        case FILL:
        case STROKE:
        case ALL:
            return true;
        case NONE:
            return false;
        default:
            return false;
        }
    }
    public static final class Anchor implements java.io.Serializable {
        public static final int ANCHOR_START  = 0;
        public static final int ANCHOR_MIDDLE = 1;
        public static final int ANCHOR_END    = 2;
        public static final Anchor START = new Anchor(ANCHOR_START);
        public static final Anchor MIDDLE = new Anchor(ANCHOR_MIDDLE);
        public static final Anchor END = new Anchor(ANCHOR_END);
        private int type;
        private Anchor(int type) {
            this.type = type;
        }
        public int getType() {
            return type;
        }
        private Object readResolve() throws java.io.ObjectStreamException {
            switch(type){
            case ANCHOR_START:
                return START;
            case ANCHOR_MIDDLE:
                return MIDDLE;
            case ANCHOR_END:
                return END;
            default:
                throw new Error("Unknown Anchor type");
            }
        }
    }
}

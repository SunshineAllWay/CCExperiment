package org.apache.batik.gvt.renderer;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import org.apache.batik.gvt.TextNode;
import org.apache.batik.gvt.TextPainter;
import org.apache.batik.gvt.text.ConcreteTextLayoutFactory;
import org.apache.batik.gvt.text.Mark;
import org.apache.batik.gvt.text.TextHit;
import org.apache.batik.gvt.text.TextLayoutFactory;
public abstract class BasicTextPainter implements TextPainter {
    private static TextLayoutFactory textLayoutFactory =
        new ConcreteTextLayoutFactory();
    protected FontRenderContext fontRenderContext =
        new FontRenderContext(new AffineTransform(), true, true);
    protected FontRenderContext aaOffFontRenderContext =
        new FontRenderContext(new AffineTransform(), false, true);
    protected TextLayoutFactory getTextLayoutFactory() {
        return textLayoutFactory;
    }
    public Mark selectAt(double x, double y, TextNode node) {
        return hitTest(x, y, node);
    }
    public Mark selectTo(double x, double y, Mark beginMark) {
        if (beginMark == null) {
            return null;
        } else {
            return hitTest(x, y, beginMark.getTextNode());
        }
    }
    public Rectangle2D getGeometryBounds(TextNode node) {
        return getOutline(node).getBounds2D();
    }
    protected abstract Mark hitTest(double x, double y, TextNode node);
    protected static class BasicMark implements Mark {
        private TextNode       node;
        private TextHit        hit;
        protected BasicMark(TextNode node,
                            TextHit hit) {
            this.hit    = hit;
            this.node   = node;
        }
        public TextHit getHit() {
            return hit;
        }
        public TextNode getTextNode() {
            return node;
        }
        public int getCharIndex() { 
            return hit.getCharIndex(); 
        }
    }
}

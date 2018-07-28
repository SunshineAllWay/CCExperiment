package org.apache.batik.gvt.renderer;
import java.awt.Graphics2D;
import java.awt.font.TextLayout;
import java.awt.geom.Point2D;
import java.text.AttributedCharacterIterator;
import org.apache.batik.gvt.TextNode;
public abstract class ConcreteTextPainter extends BasicTextPainter {
    public void paint(AttributedCharacterIterator aci, Point2D location, 
                      TextNode.Anchor anchor, Graphics2D g2d) {
        TextLayout layout = new TextLayout(aci, fontRenderContext);
        float advance = layout.getAdvance();
        float tx = 0;
        switch(anchor.getType()){
        case TextNode.Anchor.ANCHOR_MIDDLE:
            tx = -advance/2;
            break;
        case TextNode.Anchor.ANCHOR_END:
            tx = -advance;
        }
        layout.draw(g2d, (float)(location.getX() + tx), (float)(location.getY()));
    }
}

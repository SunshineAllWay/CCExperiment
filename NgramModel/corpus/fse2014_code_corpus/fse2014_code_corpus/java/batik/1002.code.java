package org.apache.batik.gvt.flow;
import java.awt.font.FontRenderContext;
import java.awt.geom.Point2D;
import java.text.AttributedCharacterIterator;
import org.apache.batik.gvt.text.TextLayoutFactory;
import org.apache.batik.gvt.text.TextSpanLayout;
public class FlowTextLayoutFactory implements TextLayoutFactory {
    public TextSpanLayout createTextLayout(AttributedCharacterIterator aci,
                                           int [] charMap,
                                           Point2D offset,
                                           FontRenderContext frc) {
        return new FlowGlyphLayout(aci, charMap, offset, frc);
    }
}

package org.apache.batik.gvt.flow;
import java.awt.font.FontRenderContext;
import java.awt.geom.Point2D;
import java.text.AttributedCharacterIterator;
import org.apache.batik.gvt.text.GlyphLayout;
public class FlowGlyphLayout extends GlyphLayout {
    public static final char SOFT_HYPHEN       = 0x00AD;
    public static final char ZERO_WIDTH_SPACE  = 0x200B;
    public static final char ZERO_WIDTH_JOINER = 0x200D;
    public static final char SPACE             = ' ';
    public FlowGlyphLayout(AttributedCharacterIterator aci,
                           int [] charMap,
                           Point2D offset,
                           FontRenderContext frc) {
        super(aci, charMap, offset, frc);
    }
}

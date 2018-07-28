package org.apache.batik.extension.svg;
import java.text.AttributedCharacterIterator;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;
import org.apache.batik.gvt.TextNode;
import org.apache.batik.gvt.TextPainter;
import org.apache.batik.gvt.renderer.StrokingTextPainter;
public class FlowExtTextPainter extends StrokingTextPainter {
    protected static TextPainter singleton = new FlowExtTextPainter();
    public static TextPainter getInstance() {
        return singleton;
    }
    public List getTextRuns(TextNode node, AttributedCharacterIterator aci) {
        List textRuns = node.getTextRuns();
        if (textRuns != null) {
            return textRuns;
        }
        AttributedCharacterIterator[] chunkACIs = getTextChunkACIs(aci);
        textRuns = computeTextRuns(node, aci, chunkACIs);
        aci.first();
        List rgns = (List)aci.getAttribute(FLOW_REGIONS);
        if (rgns != null) {
            Iterator i = textRuns.iterator();
            List chunkLayouts = new ArrayList();
            TextRun tr = (TextRun)i.next();
            List layouts = new ArrayList();
            chunkLayouts.add(layouts);
            layouts.add(tr.getLayout());
            while (i.hasNext()) {
                tr = (TextRun)i.next();
                if (tr.isFirstRunInChunk()) {
                    layouts = new ArrayList();
                    chunkLayouts.add(layouts);
                }
                layouts.add(tr.getLayout());
            }
            FlowExtGlyphLayout.textWrapTextChunk
                (chunkACIs, chunkLayouts, rgns);
        }
        node.setTextRuns(textRuns);
        return textRuns;
    }
}

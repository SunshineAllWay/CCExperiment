package org.apache.batik.extension.svg;
import org.apache.batik.gvt.TextNode;
import org.apache.batik.gvt.TextPainter;
public class FlowExtTextNode extends TextNode{
    public FlowExtTextNode() {
        textPainter = FlowExtTextPainter.getInstance();
    }
    public void setTextPainter(TextPainter textPainter) {
        if (textPainter == null)
            this.textPainter = FlowExtTextPainter.getInstance();
        else
            this.textPainter = textPainter;
    }
}

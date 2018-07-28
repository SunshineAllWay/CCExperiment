package org.apache.batik.gvt.renderer;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.font.FontRenderContext;
import java.awt.font.TextAttribute;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.text.CharacterIterator;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.apache.batik.gvt.TextNode;
import org.apache.batik.gvt.TextPainter;
import org.apache.batik.gvt.font.FontFamilyResolver;
import org.apache.batik.gvt.font.GVTFont;
import org.apache.batik.gvt.font.GVTFontFamily;
import org.apache.batik.gvt.font.GVTGlyphMetrics;
import org.apache.batik.gvt.font.GVTLineMetrics;
import org.apache.batik.gvt.text.AttributedCharacterSpanIterator;
import org.apache.batik.gvt.text.BidiAttributedCharacterIterator;
import org.apache.batik.gvt.text.GVTAttributedCharacterIterator;
import org.apache.batik.gvt.text.Mark;
import org.apache.batik.gvt.text.TextHit;
import org.apache.batik.gvt.text.TextPaintInfo;
import org.apache.batik.gvt.text.TextPath;
import org.apache.batik.gvt.text.TextSpanLayout;
public class StrokingTextPainter extends BasicTextPainter {
    public static final
        AttributedCharacterIterator.Attribute PAINT_INFO =
        GVTAttributedCharacterIterator.TextAttribute.PAINT_INFO;
    public static final
        AttributedCharacterIterator.Attribute FLOW_REGIONS =
        GVTAttributedCharacterIterator.TextAttribute.FLOW_REGIONS;
    public static final
        AttributedCharacterIterator.Attribute FLOW_PARAGRAPH =
        GVTAttributedCharacterIterator.TextAttribute.FLOW_PARAGRAPH;
    public static final
        AttributedCharacterIterator.Attribute TEXT_COMPOUND_ID
        = GVTAttributedCharacterIterator.TextAttribute.TEXT_COMPOUND_ID;
    public static final
        AttributedCharacterIterator.Attribute GVT_FONT
        = GVTAttributedCharacterIterator.TextAttribute.GVT_FONT;
    public static final
        AttributedCharacterIterator.Attribute GVT_FONTS
        = GVTAttributedCharacterIterator.TextAttribute.GVT_FONTS;
    public static final
        AttributedCharacterIterator.Attribute BIDI_LEVEL
        = GVTAttributedCharacterIterator.TextAttribute.BIDI_LEVEL;
    public static final
        AttributedCharacterIterator.Attribute XPOS
        = GVTAttributedCharacterIterator.TextAttribute.X;
    public static final
        AttributedCharacterIterator.Attribute YPOS
        = GVTAttributedCharacterIterator.TextAttribute.Y;
    public static final
        AttributedCharacterIterator.Attribute TEXTPATH
        = GVTAttributedCharacterIterator.TextAttribute.TEXTPATH;
    public static final AttributedCharacterIterator.Attribute WRITING_MODE
        = GVTAttributedCharacterIterator.TextAttribute.WRITING_MODE;
    public static final Integer WRITING_MODE_TTB
        = GVTAttributedCharacterIterator.TextAttribute.WRITING_MODE_TTB;
    public static final Integer WRITING_MODE_RTL
        = GVTAttributedCharacterIterator.TextAttribute.WRITING_MODE_RTL;
    public static final
        AttributedCharacterIterator.Attribute ANCHOR_TYPE
        = GVTAttributedCharacterIterator.TextAttribute.ANCHOR_TYPE;
    public static final Integer ADJUST_SPACING =
        GVTAttributedCharacterIterator.TextAttribute.ADJUST_SPACING;
    public static final Integer ADJUST_ALL =
        GVTAttributedCharacterIterator.TextAttribute.ADJUST_ALL;
    public static final GVTAttributedCharacterIterator.TextAttribute ALT_GLYPH_HANDLER =
        GVTAttributedCharacterIterator.TextAttribute.ALT_GLYPH_HANDLER;
    static Set extendedAtts = new HashSet();
    static {
        extendedAtts.add(FLOW_PARAGRAPH);
        extendedAtts.add(TEXT_COMPOUND_ID);
        extendedAtts.add(GVT_FONT);
    }
    protected static TextPainter singleton = new StrokingTextPainter();
    public static TextPainter getInstance() {
        return singleton;
    }
    public void paint(TextNode node, Graphics2D g2d) {
        AttributedCharacterIterator aci;
        aci = node.getAttributedCharacterIterator();
        if (aci == null)
            return;
        List textRuns = getTextRuns(node, aci);
        paintDecorations(textRuns, g2d, TextSpanLayout.DECORATION_UNDERLINE);
        paintDecorations(textRuns, g2d, TextSpanLayout.DECORATION_OVERLINE);
        paintTextRuns(textRuns, g2d);
        paintDecorations
            (textRuns, g2d, TextSpanLayout.DECORATION_STRIKETHROUGH);
    }
    protected void printAttrs(AttributedCharacterIterator aci) {
        aci.first();
        int start = aci.getBeginIndex();
        System.out.print("AttrRuns: ");
        while (aci.current() != CharacterIterator.DONE) {
            int end   = aci.getRunLimit();
            System.out.print(""+(end-start)+", ");
            aci.setIndex(end);
            start = end;
        }
        System.out.println("");
    }
    public List getTextRuns(TextNode node, AttributedCharacterIterator aci) {
        List textRuns = node.getTextRuns();
        if (textRuns != null) {
            return textRuns;
        }
        AttributedCharacterIterator[] chunkACIs = getTextChunkACIs(aci);
        textRuns = computeTextRuns(node, aci, chunkACIs);
        node.setTextRuns(textRuns);
        return node.getTextRuns();
   }
    public List computeTextRuns(TextNode node,
                                AttributedCharacterIterator aci,
                                AttributedCharacterIterator [] chunkACIs) {
        int [][] chunkCharMaps = new int[chunkACIs.length][];
        int chunkStart = aci.getBeginIndex();
        for (int i = 0; i < chunkACIs.length; i++) {
            BidiAttributedCharacterIterator iter;
            iter = new BidiAttributedCharacterIterator
                (chunkACIs[i], fontRenderContext, chunkStart);
            chunkACIs    [i] = iter;
            chunkCharMaps[i] = iter.getCharMap();
            chunkACIs    [i] = createModifiedACIForFontMatching
                (chunkACIs[i]);
            chunkStart += (chunkACIs[i].getEndIndex()-
                           chunkACIs[i].getBeginIndex());
        }
        List textRuns = new ArrayList();
        TextChunk chunk, prevChunk=null;
        int currentChunk = 0;
        Point2D location = node.getLocation();
        do {
            chunkACIs[currentChunk].first();
            chunk = getTextChunk(node,
                                 chunkACIs[currentChunk],
                                 chunkCharMaps[currentChunk],
                                 textRuns,
                                 prevChunk);
            chunkACIs[currentChunk].first();
            if (chunk != null) {
                location = adjustChunkOffsets(location, textRuns, chunk);
            }
            prevChunk = chunk;
            currentChunk++;
        } while (chunk != null && currentChunk < chunkACIs.length);
        return textRuns;
    }
    protected AttributedCharacterIterator[] getTextChunkACIs
        (AttributedCharacterIterator aci) {
        List aciList = new ArrayList();
        int chunkStartIndex = aci.getBeginIndex();
        aci.first();
        Object writingMode = aci.getAttribute(WRITING_MODE);
        boolean vertical = (writingMode == WRITING_MODE_TTB);
        while (aci.setIndex(chunkStartIndex) != CharacterIterator.DONE) {
            TextPath prevTextPath = null;
            for (int start=chunkStartIndex, end=0;
                 aci.setIndex(start) != CharacterIterator.DONE; start=end) {
                TextPath textPath = (TextPath) aci.getAttribute(TEXTPATH);
                if (start != chunkStartIndex) {
                    if (vertical) {
                        Float runY = (Float) aci.getAttribute(YPOS);
                        if ((runY != null) && !runY.isNaN())
                            break; 
                    } else {
                        Float runX = (Float) aci.getAttribute(XPOS);
                        if ((runX != null) && !runX.isNaN())
                            break; 
                    }
                    if ((prevTextPath == null) && (textPath != null))
                        break;  
                    if ((prevTextPath != null) && (textPath == null))
                        break;
                }
                prevTextPath = textPath;
                if (aci.getAttribute(FLOW_PARAGRAPH) != null) {
                    end = aci.getRunLimit(FLOW_PARAGRAPH);
                    aci.setIndex(end);
                    break;
                }
                end   = aci.getRunLimit(TEXT_COMPOUND_ID);
                if (start != chunkStartIndex)
                    continue;
                TextNode.Anchor anchor;
                anchor = (TextNode.Anchor) aci.getAttribute(ANCHOR_TYPE);
                if (anchor == TextNode.Anchor.START)
                    continue;
                if (vertical) {
                    Float runY = (Float) aci.getAttribute(YPOS);
                    if ((runY == null) || runY.isNaN())
                        continue;
                } else {
                    Float runX = (Float) aci.getAttribute(XPOS);
                    if ((runX == null) || runX.isNaN())
                        continue;
                }
                for (int i=start+1; i< end; i++) {
                    aci.setIndex(i);
                    if (vertical) {
                        Float runY = (Float) aci.getAttribute(YPOS);
                        if ((runY == null) || runY.isNaN())
                            break;
                    } else {
                        Float runX = (Float) aci.getAttribute(XPOS);
                        if ((runX == null) || runX.isNaN())
                            break;
                    }
                    aciList.add(new AttributedCharacterSpanIterator
                        (aci, i-1, i));
                    chunkStartIndex = i;
                }
            }
            int chunkEndIndex = aci.getIndex();
            aciList.add(new AttributedCharacterSpanIterator
                (aci, chunkStartIndex, chunkEndIndex));
            chunkStartIndex = chunkEndIndex;
        }
        AttributedCharacterIterator[] aciArray =
            new AttributedCharacterIterator[aciList.size()];
        Iterator iter = aciList.iterator();
        for (int i=0; iter.hasNext(); ++i) {
            aciArray[i] = (AttributedCharacterIterator)iter.next();
        }
        return aciArray;
    }
    protected static AttributedCharacterIterator createModifiedACIForFontMatching
        (AttributedCharacterIterator aci) {
        aci.first();
        AttributedString as = null;
        int asOff = 0;
        int begin = aci.getBeginIndex();
        boolean moreChunks = true;
        int start, end   = aci.getRunStart(TEXT_COMPOUND_ID);
        while (moreChunks) {
            start = end;
            end = aci.getRunLimit(TEXT_COMPOUND_ID);
            int aciLength = end-start;
            List fonts;
            fonts = (List)aci.getAttribute(GVT_FONTS);
            float fontSize = 12;
            Float fsFloat = (Float)aci.getAttribute(TextAttribute.SIZE);
            if (fsFloat != null)
                fontSize = fsFloat.floatValue();
            if (fonts.size() == 0) {
                fonts.add(FontFamilyResolver.defaultFont.deriveFont
                    (fontSize, aci));
            }
            boolean[] fontAssigned = new boolean[aciLength];
            if (as == null)
                as = new AttributedString(aci);
            GVTFont defaultFont = null;
            int numSet=0;
            int firstUnset=start;
            boolean firstUnsetSet;
            for (int i = 0; i < fonts.size(); i++) {
                int currentIndex = firstUnset;
                firstUnsetSet = false;
                aci.setIndex(currentIndex);
                GVTFont font = (GVTFont)fonts.get(i);
                if (defaultFont == null)
                    defaultFont = font;
                while (currentIndex < end) {
                    int displayUpToIndex = font.canDisplayUpTo
                        (aci, currentIndex, end);
                    Object altGlyphElement;
                    altGlyphElement = aci.getAttribute(ALT_GLYPH_HANDLER);
                    if ( altGlyphElement != null ){
                        displayUpToIndex = -1;
                    }
                    if (displayUpToIndex == -1) {
                        displayUpToIndex = end;
                    }
                    if (displayUpToIndex <= currentIndex) {
                        if (!firstUnsetSet) {
                            firstUnset = currentIndex;
                            firstUnsetSet = true;
                        }
                        currentIndex++;
                    } else {
                        int runStart = -1;
                        for (int j = currentIndex; j < displayUpToIndex; j++) {
                            if (fontAssigned[j - start]) {
                                if (runStart != -1) {
                                    as.addAttribute(GVT_FONT, font,
                                                    runStart-begin, j-begin);
                                    runStart=-1;
                                }
                            } else {
                                if (runStart == -1)
                                    runStart = j;
                            }
                            fontAssigned[j - start] = true;
                            numSet++;
                        }
                        if (runStart != -1) {
                            as.addAttribute(GVT_FONT, font,
                                            runStart-begin,
                                            displayUpToIndex-begin);
                        }
                        currentIndex = displayUpToIndex+1;
                    }
                }
                if (numSet == aciLength) 
                    break;
            }
            int           runStart = -1;
            GVTFontFamily prevFF   = null;
            GVTFont       prevF    = defaultFont;
            for (int i = 0; i < aciLength; i++) {
                if (fontAssigned[i]) {
                    if (runStart != -1) {
                        as.addAttribute(GVT_FONT, prevF,
                                        runStart+asOff, i+asOff);
                        runStart = -1;
                        prevF  = null;
                        prevFF = null;
                    }
                } else {
                    char c = aci.setIndex(start+i);
                    GVTFontFamily fontFamily;
                    fontFamily = FontFamilyResolver.getFamilyThatCanDisplay(c);
                    if (runStart == -1) {
                        runStart = i;
                        prevFF   = fontFamily;
                        if (prevFF == null)
                            prevF = defaultFont;
                        else
                            prevF = fontFamily.deriveFont(fontSize, aci);
                    } else if (prevFF != fontFamily) {
                        as.addAttribute(GVT_FONT, prevF,
                                        runStart+asOff, i+asOff);
                        runStart = i;
                        prevFF = fontFamily;
                        if (prevFF == null)
                            prevF = defaultFont;
                        else
                            prevF = fontFamily.deriveFont(fontSize, aci);
                    }
                }
            }
            if (runStart != -1) {
                as.addAttribute(GVT_FONT, prevF,
                                runStart+asOff, aciLength+asOff);
            }
            asOff += aciLength;
            if (aci.setIndex(end) == AttributedCharacterIterator.DONE) {
                moreChunks = false;
            }
            start = end;
        }
        if (as != null)
            return as.getIterator();
        return aci;
    }
    protected TextChunk getTextChunk(TextNode node,
                                     AttributedCharacterIterator aci,
                                     int [] charMap,
                                     List textRuns,
                                     TextChunk prevChunk) {
        int beginChunk = 0;
        if (prevChunk != null)
            beginChunk = prevChunk.end;
        int endChunk = beginChunk;
        int begin    = aci.getIndex();
        if (aci.current() == CharacterIterator.DONE)
            return null;
        Point2D.Float offset        = new Point2D.Float(0,0);
        Point2D.Float advance       = new Point2D.Float(0,0);
        boolean isChunkStart  = true;
        TextSpanLayout layout = null;
        do {
            int start = aci.getRunStart(extendedAtts);
            int end   = aci.getRunLimit(extendedAtts);
            AttributedCharacterIterator runaci;
            runaci = new AttributedCharacterSpanIterator(aci, start, end);
            int [] subCharMap = new int[end-start];
            System.arraycopy( charMap, start - begin, subCharMap, 0, subCharMap.length );
            FontRenderContext frc = fontRenderContext;
            RenderingHints rh = node.getRenderingHints();
            if ((rh != null) &&
                (rh.get(RenderingHints.KEY_TEXT_ANTIALIASING) ==
                  RenderingHints.VALUE_TEXT_ANTIALIAS_OFF)) {
                frc = aaOffFontRenderContext;
            }
            layout = getTextLayoutFactory().createTextLayout
                (runaci, subCharMap, offset, frc);
            textRuns.add(new TextRun(layout, runaci, isChunkStart));
            Point2D layoutAdvance = layout.getAdvance2D();
            advance.x +=  (float)layoutAdvance.getX();
            advance.y +=  (float)layoutAdvance.getY();
            ++endChunk;
            if (aci.setIndex(end) == CharacterIterator.DONE) break;
            isChunkStart = false;
        } while (true);
        return new TextChunk(beginChunk, endChunk, advance);
    }
    protected Point2D adjustChunkOffsets(Point2D location,
                                         List textRuns,
                                         TextChunk chunk) {
        TextRun r          = (TextRun) textRuns.get(chunk.begin);
        int     anchorType = r.getAnchorType();
        Float   length     = r.getLength();
        Integer lengthAdj  = r.getLengthAdjust();
        boolean doAdjust = true;
        if ((length == null) || length.isNaN())
            doAdjust = false;
        int numChars = 0;
        for (int n=chunk.begin; n<chunk.end; ++n) {
            r = (TextRun) textRuns.get(n);
            AttributedCharacterIterator aci = r.getACI();
            numChars += aci.getEndIndex()-aci.getBeginIndex();
        }
        if ((lengthAdj ==
             GVTAttributedCharacterIterator.TextAttribute.ADJUST_SPACING) &&
            (numChars == 1))
            doAdjust = false;
        float xScale = 1;
        float yScale = 1;
        r = (TextRun)textRuns.get(chunk.end-1);
        TextSpanLayout  layout          = r.getLayout();
        GVTGlyphMetrics lastMetrics =
            layout.getGlyphMetrics(layout.getGlyphCount()-1);
        GVTLineMetrics  lastLineMetrics = layout.getLineMetrics();
        Rectangle2D     lastBounds  = lastMetrics.getBounds2D();
        float halfLeading = (lastMetrics.getVerticalAdvance()-
                               (lastLineMetrics.getAscent() +
                                lastLineMetrics.getDescent()))/2;
        float lastW = (float)(lastBounds.getWidth()  + lastBounds.getX());
        float lastH = (float)(halfLeading + lastLineMetrics.getAscent() +
                              (lastBounds.getHeight() + lastBounds.getY()));
        Point2D visualAdvance;
        if (!doAdjust) {
            visualAdvance = new Point2D.Float
            ((float)(chunk.advance.getX() + lastW -
                     lastMetrics.getHorizontalAdvance()),
             (float)(chunk.advance.getY() - lastMetrics.getVerticalAdvance() +
                     lastH));
        } else {
            Point2D advance    = chunk.advance;
            if (layout.isVertical()) {
                if (lengthAdj == ADJUST_SPACING) {
                    yScale = (float)
                        ((length.floatValue()-lastH)/
                         (advance.getY()-lastMetrics.getVerticalAdvance()));
                } else {
                    double adv =(advance.getY()-
                                 lastMetrics.getVerticalAdvance() + lastH);
                    yScale = (float)(length.floatValue()/adv);
                }
                visualAdvance = new Point2D.Float(0, length.floatValue());
            } else {
                if (lengthAdj == ADJUST_SPACING) {
                    xScale = (float)
                        ((length.floatValue()-lastW)/
                         (advance.getX()-lastMetrics.getHorizontalAdvance()));
                } else {
                    double adv = (advance.getX() + lastW -
                                  lastMetrics.getHorizontalAdvance());
                    xScale = (float)(length.floatValue()/adv);
                }
                visualAdvance = new Point2D.Float(length.floatValue(), 0);
            }
            Point2D.Float adv = new Point2D.Float(0,0);
            for (int n=chunk.begin; n<chunk.end; ++n) {
                r = (TextRun) textRuns.get(n);
                layout = r.getLayout();
                layout.setScale(xScale, yScale, lengthAdj==ADJUST_SPACING);
                Point2D lAdv = layout.getAdvance2D();
                adv.x += (float)lAdv.getX();
                adv.y += (float)lAdv.getY();
            }
            chunk.advance = adv;
        }
        float dx = 0f;
        float dy = 0f;
        switch(anchorType){
        case TextNode.Anchor.ANCHOR_MIDDLE:
            dx = (float) (-visualAdvance.getX()/2d);
            dy = (float) (-visualAdvance.getY()/2d);
            break;
        case TextNode.Anchor.ANCHOR_END:
            dx = (float) (-visualAdvance.getX());
            dy = (float) (-visualAdvance.getY());
            break;
        default:
            break;
        }
        r = (TextRun) textRuns.get(chunk.begin);
        layout = r.getLayout();
        AttributedCharacterIterator runaci = r.getACI();
        runaci.first();
        boolean vertical = layout.isVertical();
        Float runX = (Float) runaci.getAttribute(XPOS);
        Float runY = (Float) runaci.getAttribute(YPOS);
        TextPath textPath =  (TextPath) runaci.getAttribute(TEXTPATH);
        float absX = (float)location.getX();
        float absY = (float)location.getY();
        float tpShiftX = 0;
        float tpShiftY = 0;
        if ((runX != null) && (!runX.isNaN())) {
            absX = runX.floatValue();
            tpShiftX = absX;
        }
        if ((runY != null) && (!runY.isNaN())) {
            absY = runY.floatValue();
            tpShiftY = absY;
        }
        if (vertical) {
            absY     += dy;
            tpShiftY += dy;
            tpShiftX  = 0;
        } else {
            absX     += dx;
            tpShiftX += dx;
            tpShiftY  = 0;
        }
        for (int n=chunk.begin; n<chunk.end; ++n) {
            r = (TextRun) textRuns.get(n);
            layout = r.getLayout();
            runaci = r.getACI();
            runaci.first();
            textPath =  (TextPath) runaci.getAttribute(TEXTPATH);
            if (vertical) {
                runX = (Float) runaci.getAttribute(XPOS);
                if ((runX != null) && (!runX.isNaN())) {
                    absX = runX.floatValue();
                }
            } else {
                runY = (Float) runaci.getAttribute(YPOS);
                if ((runY != null) && (!runY.isNaN())) {
                    absY = runY.floatValue();
                }
            }
            if (textPath == null) {
                layout.setOffset(new Point2D.Float(absX, absY));
                Point2D ladv = layout.getAdvance2D();
                absX += ladv.getX();
                absY += ladv.getY();
            } else {
                layout.setOffset(new Point2D.Float(tpShiftX, tpShiftY));
                Point2D ladv = layout.getAdvance2D();
                tpShiftX += (float)ladv.getX();
                tpShiftY += (float)ladv.getY();
                ladv = layout.getTextPathAdvance();
                absX = (float)ladv.getX();
                absY = (float)ladv.getY();
            }
        }
        return new Point2D.Float(absX, absY);
    }
    protected void paintDecorations(List textRuns,
                                  Graphics2D g2d,
                                  int decorationType) {
        Paint prevPaint = null;
        Paint prevStrokePaint = null;
        Stroke prevStroke = null;
        boolean prevVisible = true;
        Rectangle2D decorationRect = null;
        double yLoc = 0, height = 0;
        for (int i = 0; i < textRuns.size(); i++) {
            TextRun textRun = (TextRun)textRuns.get(i);
            AttributedCharacterIterator runaci = textRun.getACI();
            runaci.first();
            Paint  paint       = null;
            Stroke stroke      = null;
            Paint  strokePaint = null;
            boolean visible    = true;
            TextPaintInfo tpi = (TextPaintInfo)runaci.getAttribute(PAINT_INFO);
            if (tpi != null) {
                visible = tpi.visible;
                if (tpi.composite != null) {
                    g2d.setComposite(tpi.composite);
                }
                switch (decorationType) {
                case TextSpanLayout.DECORATION_UNDERLINE :
                    paint       = tpi.underlinePaint;
                    stroke      = tpi.underlineStroke;
                    strokePaint = tpi.underlineStrokePaint;
                    break;
                case TextSpanLayout.DECORATION_OVERLINE :
                    paint       = tpi.overlinePaint;
                    stroke      = tpi.overlineStroke;
                    strokePaint = tpi.overlineStrokePaint;
                    break;
                case TextSpanLayout.DECORATION_STRIKETHROUGH :
                    paint       = tpi.strikethroughPaint;
                    stroke      = tpi.strikethroughStroke;
                    strokePaint = tpi.strikethroughStrokePaint;
                    break;
                default:
                    return;
                }
            }
            if (textRun.isFirstRunInChunk()) {
                Shape s = textRun.getLayout().getDecorationOutline
                    (decorationType);
                Rectangle2D r2d = s.getBounds2D();
                yLoc   = r2d.getY();
                height = r2d.getHeight();
            }
            if (textRun.isFirstRunInChunk() ||
                (paint != prevPaint) ||
                (stroke != prevStroke) ||
                (strokePaint != prevStrokePaint) ||
                (visible != prevVisible)) {
                if (prevVisible && (decorationRect != null)) {
                    if (prevPaint != null) {
                        g2d.setPaint(prevPaint);
                        g2d.fill(decorationRect);
                    }
                    if (prevStroke != null && prevStrokePaint != null) {
                        g2d.setPaint(prevStrokePaint);
                        g2d.setStroke(prevStroke);
                        g2d.draw(decorationRect);
                    }
                }
                decorationRect = null;
            }
            if ((paint != null || strokePaint != null)
                && !textRun.getLayout().isVertical()
                && !textRun.getLayout().isOnATextPath()) {
                Shape decorationShape =
                    textRun.getLayout().getDecorationOutline(decorationType);
                if (decorationRect == null) {
                    Rectangle2D r2d = decorationShape.getBounds2D();
                    decorationRect = new Rectangle2D.Double
                        (r2d.getX(), yLoc, r2d.getWidth(), height);
                } else {
                    Rectangle2D bounds = decorationShape.getBounds2D();
                    double minX = Math.min(decorationRect.getX(),
                                           bounds.getX());
                    double maxX = Math.max(decorationRect.getMaxX(),
                                           bounds.getMaxX());
                    decorationRect.setRect(minX, yLoc, maxX-minX, height);
                }
            }
            prevPaint = paint;
            prevStroke = stroke;
            prevStrokePaint = strokePaint;
            prevVisible = visible;
        }
        if (prevVisible && (decorationRect != null)) {
            if (prevPaint != null) {
                g2d.setPaint(prevPaint);
                g2d.fill(decorationRect);
            }
            if (prevStroke != null && prevStrokePaint != null) {
                g2d.setPaint(prevStrokePaint);
                g2d.setStroke(prevStroke);
                g2d.draw(decorationRect);
            }
        }
    }
    protected void paintTextRuns(List textRuns,
                               Graphics2D g2d) {
        for (int i = 0; i < textRuns.size(); i++) {
            TextRun textRun = (TextRun)textRuns.get(i);
            AttributedCharacterIterator runaci = textRun.getACI();
            runaci.first();
            TextPaintInfo tpi = (TextPaintInfo)runaci.getAttribute(PAINT_INFO);
            if ((tpi != null) && (tpi.composite != null)) {
                g2d.setComposite(tpi.composite);
            }
            textRun.getLayout().draw(g2d);
        }
    }
    public Shape getOutline(TextNode node) {
        GeneralPath outline = null;
        AttributedCharacterIterator aci;
        aci = node.getAttributedCharacterIterator();
        if (aci == null)
            return null;
        List textRuns = getTextRuns(node, aci);
        for (int i = 0; i < textRuns.size(); ++i) {
            TextRun textRun = (TextRun)textRuns.get(i);
            TextSpanLayout textRunLayout = textRun.getLayout();
            GeneralPath textRunOutline =
                new GeneralPath(textRunLayout.getOutline());
            if (outline == null) {
               outline = textRunOutline;
            } else {
                outline.setWindingRule(GeneralPath.WIND_NON_ZERO);
                outline.append(textRunOutline, false);
            }
        }
        Shape underline = getDecorationOutline
            (textRuns, TextSpanLayout.DECORATION_UNDERLINE);
        Shape strikeThrough = getDecorationOutline
            (textRuns, TextSpanLayout.DECORATION_STRIKETHROUGH);
        Shape overline = getDecorationOutline
            (textRuns, TextSpanLayout.DECORATION_OVERLINE);
        if (underline != null) {
            if (outline == null) {
                outline = new GeneralPath(underline);
            } else {
                outline.setWindingRule(GeneralPath.WIND_NON_ZERO);
                outline.append(underline, false);
            }
        }
        if (strikeThrough != null) {
            if (outline == null) {
                outline = new GeneralPath(strikeThrough);
            } else {
                outline.setWindingRule(GeneralPath.WIND_NON_ZERO);
                outline.append(strikeThrough, false);
            }
        }
        if (overline != null) {
            if (outline == null) {
                outline = new GeneralPath(overline);
            } else {
                outline.setWindingRule(GeneralPath.WIND_NON_ZERO);
                outline.append(overline, false);
            }
        }
        return outline;
    }
     public Rectangle2D getBounds2D(TextNode node) {
        AttributedCharacterIterator aci;
        aci = node.getAttributedCharacterIterator();
        if (aci == null)
            return null;
        List textRuns = getTextRuns(node, aci);
        Rectangle2D bounds = null;
        for (int i = 0; i < textRuns.size(); ++i) {
            TextRun textRun = (TextRun)textRuns.get(i);
            TextSpanLayout textRunLayout = textRun.getLayout();
            Rectangle2D runBounds = textRunLayout.getBounds2D();
            if (runBounds != null) {
                if (bounds == null)
                    bounds = runBounds;
                else
                    bounds.add( runBounds );
            }
        }
        Shape underline = getDecorationStrokeOutline
            (textRuns, TextSpanLayout.DECORATION_UNDERLINE);
        if (underline != null) {
            if (bounds == null)
                bounds = underline.getBounds2D();
            else
                bounds.add( underline.getBounds2D() );
        }
        Shape strikeThrough = getDecorationStrokeOutline
            (textRuns, TextSpanLayout.DECORATION_STRIKETHROUGH);
        if (strikeThrough != null) {
            if (bounds == null)
                bounds = strikeThrough.getBounds2D();
            else
                bounds.add( strikeThrough.getBounds2D() );
        }
        Shape overline = getDecorationStrokeOutline
            (textRuns, TextSpanLayout.DECORATION_OVERLINE);
        if (overline != null) {
            if (bounds == null)
                bounds = overline.getBounds2D();
            else
                bounds.add( overline.getBounds2D() );
        }
        return bounds;
    }
    protected Shape getDecorationOutline(List textRuns, int decorationType) {
        GeneralPath outline = null;
        Paint prevPaint = null;
        Paint prevStrokePaint = null;
        Stroke prevStroke = null;
        Rectangle2D decorationRect = null;
        double yLoc = 0, height = 0;
        for (int i = 0; i < textRuns.size(); i++) {
            TextRun textRun = (TextRun)textRuns.get(i);
            AttributedCharacterIterator runaci = textRun.getACI();
            runaci.first();
            Paint paint = null;
            Stroke stroke = null;
            Paint strokePaint = null;
            TextPaintInfo tpi = (TextPaintInfo)runaci.getAttribute(PAINT_INFO);
            if (tpi != null) {
                switch (decorationType) {
                case TextSpanLayout.DECORATION_UNDERLINE :
                    paint       = tpi.underlinePaint;
                    stroke      = tpi.underlineStroke;
                    strokePaint = tpi.underlineStrokePaint;
                    break;
                case TextSpanLayout.DECORATION_OVERLINE :
                    paint       = tpi.overlinePaint;
                    stroke      = tpi.overlineStroke;
                    strokePaint = tpi.overlineStrokePaint;
                    break;
                case TextSpanLayout.DECORATION_STRIKETHROUGH :
                    paint       = tpi.strikethroughPaint;
                    stroke      = tpi.strikethroughStroke;
                    strokePaint = tpi.strikethroughStrokePaint;
                    break;
                default:
                    return null;
                }
            }
            if (textRun.isFirstRunInChunk()) {
                Shape s = textRun.getLayout().getDecorationOutline
                    (decorationType);
                Rectangle2D r2d = s.getBounds2D();
                yLoc   = r2d.getY();
                height = r2d.getHeight();
            }
            if (textRun.isFirstRunInChunk() ||
                paint != prevPaint ||
                stroke != prevStroke ||
                strokePaint != prevStrokePaint) {
                if (decorationRect != null) {
                    if (outline == null) {
                        outline = new GeneralPath(decorationRect);
                    } else {
                        outline.append(decorationRect, false);
                    }
                    decorationRect = null;
                }
            }
            if ((paint != null || strokePaint != null)
                && !textRun.getLayout().isVertical()
                && !textRun.getLayout().isOnATextPath()) {
                Shape decorationShape =
                    textRun.getLayout().getDecorationOutline(decorationType);
                if (decorationRect == null) {
                    Rectangle2D r2d = decorationShape.getBounds2D();
                    decorationRect = new Rectangle2D.Double
                        (r2d.getX(), yLoc, r2d.getWidth(), height);
                } else {
                    Rectangle2D bounds = decorationShape.getBounds2D();
                    double minX = Math.min(decorationRect.getX(),
                                           bounds.getX());
                    double maxX = Math.max(decorationRect.getMaxX(),
                                           bounds.getMaxX());
                    decorationRect.setRect(minX, yLoc, maxX-minX, height);
                }
            }
            prevPaint = paint;
            prevStroke = stroke;
            prevStrokePaint = strokePaint;
        }
        if (decorationRect != null) {
            if (outline == null) {
                outline = new GeneralPath(decorationRect);
            } else {
                outline.append(decorationRect, false);
            }
        }
        return outline;
    }
    protected Shape getDecorationStrokeOutline
        (List textRuns, int decorationType) {
        GeneralPath outline = null;
        Paint prevPaint = null;
        Paint prevStrokePaint = null;
        Stroke prevStroke = null;
        Rectangle2D decorationRect = null;
        double yLoc = 0, height = 0;
        for (int i = 0; i < textRuns.size(); i++) {
            TextRun textRun = (TextRun)textRuns.get(i);
            AttributedCharacterIterator runaci = textRun.getACI();
            runaci.first();
            Paint paint = null;
            Stroke stroke = null;
            Paint strokePaint = null;
            TextPaintInfo tpi = (TextPaintInfo)runaci.getAttribute(PAINT_INFO);
            if (tpi != null) {
                switch (decorationType) {
                case TextSpanLayout.DECORATION_UNDERLINE :
                    paint       = tpi.underlinePaint;
                    stroke      = tpi.underlineStroke;
                    strokePaint = tpi.underlineStrokePaint;
                    break;
                case TextSpanLayout.DECORATION_OVERLINE :
                    paint       = tpi.overlinePaint;
                    stroke      = tpi.overlineStroke;
                    strokePaint = tpi.overlineStrokePaint;
                    break;
                case TextSpanLayout.DECORATION_STRIKETHROUGH :
                    paint       = tpi.strikethroughPaint;
                    stroke      = tpi.strikethroughStroke;
                    strokePaint = tpi.strikethroughStrokePaint;
                    break;
                default:
                    return null;
                }
            }
            if (textRun.isFirstRunInChunk()) {
                Shape s = textRun.getLayout().getDecorationOutline
                    (decorationType);
                Rectangle2D r2d = s.getBounds2D();
                yLoc   = r2d.getY();
                height = r2d.getHeight();
            }
            if (textRun.isFirstRunInChunk() ||
                paint != prevPaint ||
                stroke != prevStroke ||
                strokePaint != prevStrokePaint) {
                if (decorationRect != null) {
                    Shape s = null;
                    if (prevStroke != null &&
                        prevStrokePaint != null)
                        s = prevStroke.createStrokedShape(decorationRect);
                    else if (prevPaint != null)
                        s = decorationRect;
                    if (s != null) {
                        if (outline == null)
                            outline = new GeneralPath(s);
                        else
                            outline.append(s, false);
                    }
                    decorationRect = null;
                }
            }
            if ((paint != null || strokePaint != null)
                && !textRun.getLayout().isVertical()
                && !textRun.getLayout().isOnATextPath()) {
                Shape decorationShape =
                    textRun.getLayout().getDecorationOutline(decorationType);
                if (decorationRect == null) {
                    Rectangle2D r2d = decorationShape.getBounds2D();
                    decorationRect = new Rectangle2D.Double
                        (r2d.getX(), yLoc, r2d.getWidth(), height);
                } else {
                    Rectangle2D bounds = decorationShape.getBounds2D();
                    double minX = Math.min(decorationRect.getX(),
                                           bounds.getX());
                    double maxX = Math.max(decorationRect.getMaxX(),
                                           bounds.getMaxX());
                    decorationRect.setRect(minX, yLoc, maxX-minX, height);
                }
            }
            prevPaint = paint;
            prevStroke = stroke;
            prevStrokePaint = strokePaint;
        }
        if (decorationRect != null) {
            Shape s = null;
            if (prevStroke != null &&
                prevStrokePaint != null)
                s = prevStroke.createStrokedShape(decorationRect);
            else if (prevPaint != null)
                s = decorationRect;
            if (s != null) {
                if (outline == null)
                    outline = new GeneralPath(s);
                else
                    outline.append(s, false);
            }
        }
        return outline;
    }
    public Mark getMark(TextNode node, int index, boolean leadingEdge) {
        AttributedCharacterIterator aci;
        aci = node.getAttributedCharacterIterator();
        if (aci == null)
            return null;
        if ((index < aci.getBeginIndex()) ||
            (index > aci.getEndIndex()))
            return null;
        TextHit textHit = new TextHit(index, leadingEdge);
        return new BasicTextPainter.BasicMark(node, textHit);
    }
    protected Mark hitTest(double x, double y, TextNode node) {
        AttributedCharacterIterator aci;
        aci = node.getAttributedCharacterIterator();
        if (aci == null)
            return null;
        List textRuns = getTextRuns(node, aci);
        if (textRuns != null) {
            for (int i = 0; i < textRuns.size(); ++i) {
                TextRun textRun = (TextRun)textRuns.get(i);
                TextSpanLayout layout = textRun.getLayout();
                TextHit textHit = layout.hitTestChar((float) x, (float) y);
                Rectangle2D bounds = layout.getBounds2D();
                if ((textHit != null) && 
                    (bounds != null) && bounds.contains(x,y))
                    return new BasicTextPainter.BasicMark(node, textHit);
            }
        }
        return null;
    }
    public Mark selectFirst(TextNode node) {
        AttributedCharacterIterator aci;
        aci = node.getAttributedCharacterIterator();
        if (aci == null)
            return null;
        TextHit textHit = new TextHit(aci.getBeginIndex(), false);
        return new BasicTextPainter.BasicMark(node, textHit);
    }
    public Mark selectLast(TextNode node) {
        AttributedCharacterIterator aci;
        aci = node.getAttributedCharacterIterator();
        if (aci == null)
            return null;
        TextHit textHit = new TextHit(aci.getEndIndex()-1, false);
        return  new BasicTextPainter.BasicMark(node, textHit);
    }
    public int[] getSelected(Mark startMark,
                             Mark finishMark) {
        if (startMark == null || finishMark == null) {
            return null;
        }
        BasicTextPainter.BasicMark start;
        BasicTextPainter.BasicMark finish;
        try {
            start = (BasicTextPainter.BasicMark) startMark;
            finish = (BasicTextPainter.BasicMark) finishMark;
        } catch (ClassCastException cce) {
            throw new Error
                ("This Mark was not instantiated by this TextPainter class!");
        }
        TextNode textNode = start.getTextNode();
        if (textNode == null)
            return null;
        if (textNode != finish.getTextNode())
            throw new Error("Markers are from different TextNodes!");
        AttributedCharacterIterator aci;
        aci = textNode.getAttributedCharacterIterator();
        if (aci == null)
            return null;
        int[] result = new int[2];
        result[0] = start.getHit().getCharIndex();
        result[1] = finish.getHit().getCharIndex();
        List textRuns = getTextRuns(textNode, aci);
        Iterator trI = textRuns.iterator();
        int startGlyphIndex = -1;
        int endGlyphIndex = -1;
        TextSpanLayout startLayout=null, endLayout=null;
        while (trI.hasNext()) {
            TextRun tr = (TextRun)trI.next();
            TextSpanLayout tsl = tr.getLayout();
            if (startGlyphIndex == -1) {
                startGlyphIndex  = tsl.getGlyphIndex(result[0]);
                if (startGlyphIndex != -1)
                    startLayout = tsl;
            }
            if (endGlyphIndex == -1) {
                endGlyphIndex = tsl.getGlyphIndex(result[1]);
                if (endGlyphIndex != -1)
                    endLayout = tsl;
            }
            if ((startGlyphIndex != -1) && (endGlyphIndex != -1))
                break;
        }
        if ((startLayout == null) || (endLayout == null))
            return null;
        int startCharCount = startLayout.getCharacterCount
            (startGlyphIndex, startGlyphIndex);
        int endCharCount = endLayout.getCharacterCount
            (endGlyphIndex, endGlyphIndex);
        if (startCharCount > 1) {
            if (result[0] > result[1] && startLayout.isLeftToRight()) {
                result[0] += startCharCount-1;
            } else if (result[1] > result[0] && !startLayout.isLeftToRight()) {
                result[0] -= startCharCount-1;
            }
        }
        if (endCharCount > 1) {
            if (result[1] > result[0] && endLayout.isLeftToRight()) {
                result[1] += endCharCount-1;
            } else if (result[0] > result[1] && !endLayout.isLeftToRight()) {
                result[1] -= endCharCount-1;
            }
        }
        return result;
    }
    public Shape getHighlightShape(Mark beginMark, Mark endMark) {
        if (beginMark == null || endMark == null) {
            return null;
        }
        BasicTextPainter.BasicMark begin;
        BasicTextPainter.BasicMark end;
        try {
            begin = (BasicTextPainter.BasicMark) beginMark;
            end = (BasicTextPainter.BasicMark) endMark;
        } catch (ClassCastException cce) {
            throw new Error
                ("This Mark was not instantiated by this TextPainter class!");
        }
        TextNode textNode = begin.getTextNode();
        if (textNode == null)
            return null;
        if (textNode != end.getTextNode())
            throw new Error("Markers are from different TextNodes!");
        AttributedCharacterIterator aci;
        aci = textNode.getAttributedCharacterIterator();
        if (aci == null)
            return null;
        int beginIndex = begin.getHit().getCharIndex();
        int endIndex   = end.getHit().getCharIndex();
        if (beginIndex > endIndex) {
            BasicTextPainter.BasicMark tmpMark = begin;
            begin = end; end = tmpMark;
            int tmpIndex = beginIndex;
            beginIndex = endIndex; endIndex = tmpIndex;
        }
        List textRuns = getTextRuns(textNode, aci);
        GeneralPath highlightedShape = new GeneralPath();
        for (int i = 0; i < textRuns.size(); ++i) {
            TextRun textRun = (TextRun)textRuns.get(i);
            TextSpanLayout layout = textRun.getLayout();
            Shape layoutHighlightedShape = layout.getHighlightShape
                (beginIndex, endIndex);
            if (( layoutHighlightedShape != null) &&
                (!layoutHighlightedShape.getBounds().isEmpty())) {
                highlightedShape.append(layoutHighlightedShape, false);
            }
        }
        return highlightedShape;
    }
    class TextChunk {
        public int begin;
        public int end;
        public Point2D advance;
        public TextChunk(int begin, int end, Point2D advance) {
            this.begin = begin;
            this.end = end;
            this.advance = new Point2D.Float((float) advance.getX(),
                                             (float) advance.getY());
        }
    }
    public class TextRun {
        protected AttributedCharacterIterator aci;
        protected TextSpanLayout layout;
        protected int anchorType;
        protected boolean firstRunInChunk;
        protected Float length;
        protected Integer lengthAdjust;
        public TextRun(TextSpanLayout layout,
                       AttributedCharacterIterator aci,
                       boolean firstRunInChunk) {
            this.layout = layout;
            this.aci = aci;
            this.aci.first();
            this.firstRunInChunk = firstRunInChunk;
            this.anchorType = TextNode.Anchor.ANCHOR_START;
            TextNode.Anchor anchor = (TextNode.Anchor) aci.getAttribute
                (GVTAttributedCharacterIterator.TextAttribute.ANCHOR_TYPE);
            if (anchor != null) {
                this.anchorType = anchor.getType();
            }
            if (aci.getAttribute(WRITING_MODE) == WRITING_MODE_RTL) {
                if (anchorType == TextNode.Anchor.ANCHOR_START) {
                    anchorType = TextNode.Anchor.ANCHOR_END;
                } else if (anchorType == TextNode.Anchor.ANCHOR_END) {
                    anchorType = TextNode.Anchor.ANCHOR_START;
                }
            }
            length = (Float) aci.getAttribute
                (GVTAttributedCharacterIterator.TextAttribute.BBOX_WIDTH);
            lengthAdjust = (Integer) aci.getAttribute
                (GVTAttributedCharacterIterator.TextAttribute.LENGTH_ADJUST);
        }
        public AttributedCharacterIterator getACI() {
            return aci;
        }
        public TextSpanLayout getLayout() {
            return layout;
        }
        public int getAnchorType() {
            return anchorType;
        }
        public Float getLength() {
            return length;
        }
        public Integer getLengthAdjust() {
            return lengthAdjust;
        }
        public boolean isFirstRunInChunk() {
            return firstRunInChunk;
        }
    }
}

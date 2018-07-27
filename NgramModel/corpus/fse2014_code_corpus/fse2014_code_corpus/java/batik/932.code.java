package org.apache.batik.extension.svg;
import java.awt.font.FontRenderContext;
import java.awt.geom.Point2D;
import java.text.AttributedCharacterIterator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.apache.batik.gvt.font.GVTGlyphVector;
import org.apache.batik.gvt.font.MultiGlyphVector;
import org.apache.batik.gvt.text.GlyphLayout;
public class FlowExtGlyphLayout extends GlyphLayout {
    public FlowExtGlyphLayout(AttributedCharacterIterator aci,
                           int [] charMap,
                           Point2D offset,
                           FontRenderContext frc) {
        super(aci, charMap, offset, frc);
    }
    public static void textWrapTextChunk(AttributedCharacterIterator [] acis,
                                         List chunkLayouts,
                                         List flowRects) {
        GVTGlyphVector [] gvs            = new GVTGlyphVector[acis.length];
        List           [] chunkLineInfos = new List          [acis.length];
        GlyphIterator  [] gis            = new GlyphIterator [acis.length];
        Iterator clIter = chunkLayouts.iterator();
        Iterator flowRectsIter = flowRects.iterator();
        RegionInfo currentRegion = null;
        float y0, x0, width, height=0;
        if (flowRectsIter.hasNext()) {
            currentRegion = (RegionInfo) flowRectsIter.next();
            height = (float) currentRegion.getHeight();
        }
        boolean lineHeightRelative = true;
        float lineHeight           = 1.0f;
        float nextLineMult         = 0.0f;
        float dy                   = 0.0f;
        Point2D.Float verticalAlignOffset = new Point2D.Float(0,0);
        float prevBotMargin = 0;
        for (int chunk=0; clIter.hasNext(); chunk++) {
            AttributedCharacterIterator aci = acis[chunk];
            if (currentRegion != null)
            {
                List extraP = (List)aci.getAttribute(FLOW_EMPTY_PARAGRAPH);
                if (extraP != null) {
                    Iterator epi = extraP.iterator();
                    while (epi.hasNext()) {
                        MarginInfo emi = (MarginInfo)epi.next();
                        float inc = ((prevBotMargin > emi.getTopMargin()) 
                                     ? prevBotMargin 
                                     : emi.getTopMargin());
                        if ((dy + inc <= height) &&
                            !emi.isFlowRegionBreak()) {
                            dy += inc;
                            prevBotMargin = emi.getBottomMargin();
                        } else {
                            if (!flowRectsIter.hasNext()) {
                                currentRegion = null;
                                break; 
                            }
                            currentRegion = (RegionInfo) flowRectsIter.next();
                            height = (float) currentRegion.getHeight();
                            verticalAlignOffset = new Point2D.Float(0,0);
                            dy            = 0;
                            prevBotMargin = 0;
                        }
                    }
                    if (currentRegion == null) break;
                }
            }
            List gvl = new LinkedList();
            List layouts = (List)clIter.next();
            Iterator iter = layouts.iterator();
            while (iter.hasNext()) {
                GlyphLayout gl = (GlyphLayout)iter.next();
                gvl.add(gl.getGlyphVector());
            }
            GVTGlyphVector gv = new MultiGlyphVector(gvl);
            gvs[chunk] = gv;
            int numGlyphs = gv.getNumGlyphs();
            aci.first();
            MarginInfo mi = (MarginInfo)aci.getAttribute(FLOW_PARAGRAPH);
            if (mi == null) {
              continue;
            }
            if (currentRegion == null) {
                for(int idx=0; idx <numGlyphs; idx++) 
                    gv.setGlyphVisible(idx, false);
                continue;
            }
            float inc = ((prevBotMargin > mi.getTopMargin()) 
                         ? prevBotMargin : mi.getTopMargin());
            if (dy + inc <= height) {
                dy += inc;
            } else {
                if (!flowRectsIter.hasNext()) {
                    currentRegion = null;
                    break; 
                }
                currentRegion = (RegionInfo) flowRectsIter.next();
                height = (float) currentRegion.getHeight();
                verticalAlignOffset = new Point2D.Float(0,0);
                dy        = mi.getTopMargin();
            }
            prevBotMargin = mi.getBottomMargin();
            float leftMargin = mi.getLeftMargin();
            float rightMargin = mi.getRightMargin();
            if (((GlyphLayout)layouts.get(0)).isLeftToRight()) {
                leftMargin += mi.getIndent();
            } else {
                rightMargin += mi.getIndent();
            }
            x0 = (float) currentRegion.getX() + leftMargin;
            y0 = (float) currentRegion.getY();
            width = (float) (currentRegion.getWidth() - 
                             (leftMargin + rightMargin));
            height = (float) currentRegion.getHeight();
            List lineInfos = new LinkedList();
            chunkLineInfos[chunk] = lineInfos;
            float prevDesc = 0.0f;
            GlyphIterator gi = new GlyphIterator(aci, gv);
            gis[chunk] = gi;
            GlyphIterator breakGI  = null, newBreakGI = null;
            if (!gi.done() && !gi.isPrinting()) {
               updateVerticalAlignOffset(verticalAlignOffset, 
                                         currentRegion, dy);
               lineInfos.add(gi.newLine
                             (new Point2D.Float(x0, y0+dy), 
                              width, true, verticalAlignOffset));
            }
            GlyphIterator lineGI   =  gi.copy();
            boolean firstLine = true;
            while (!gi.done()) {
                boolean doBreak = false;
                boolean partial = false;
                if (gi.isPrinting() && (gi.getAdv() > width)) {
                    if (breakGI == null) {
                        if (!flowRectsIter.hasNext()) {
                            currentRegion = null;
                            gi = lineGI.copy(gi);
                            break; 
                        }
                        currentRegion = (RegionInfo) flowRectsIter.next();
                        x0 = (float) currentRegion.getX() + leftMargin;
                        y0 = (float) currentRegion.getY();
                        width = (float) (currentRegion.getWidth() -
                                        (leftMargin+rightMargin));
                        height = (float) currentRegion.getHeight();
                        verticalAlignOffset = new Point2D.Float(0,0);
                        dy = firstLine ? mi.getTopMargin() : 0;
                        prevDesc  = 0;
                        gi = lineGI.copy(gi);
                        continue;
                    }
                    gi = breakGI.copy(gi);  
                    nextLineMult = 1;
                    doBreak = true;
                    partial = false;
                } else if (gi.isLastChar()) {
                    nextLineMult = 1;
                    doBreak = true;
                    partial = true;
                } 
                int lnBreaks = gi.getLineBreaks();
                if (lnBreaks != 0) {
                    if (doBreak)
                        nextLineMult -= 1;
                    nextLineMult += lnBreaks;
                    doBreak = true;
                    partial = true;
                }
                if (!doBreak) {
                    if ((gi.isBreakChar()) ||
                        (breakGI == null)  ||
                        (!breakGI.isBreakChar())) {
                        newBreakGI = gi.copy(newBreakGI);
                        gi.nextChar();
                        if (gi.getChar() != GlyphIterator.ZERO_WIDTH_JOINER) {
                            GlyphIterator tmpGI = breakGI;
                            breakGI = newBreakGI;
                            newBreakGI = tmpGI;
                        }
                    } else {
                        gi.nextChar();
                    }
                    continue;
                }
                float lineSize = gi.getMaxAscent()+gi.getMaxDescent(); 
                float lineBoxHeight;
                if (lineHeightRelative) 
                    lineBoxHeight = gi.getMaxFontSize()*lineHeight;
                else
                    lineBoxHeight = lineHeight;
                float halfLeading = (lineBoxHeight-lineSize)/2;
                float ladv = prevDesc + halfLeading + gi.getMaxAscent();
                float newDesc = halfLeading + gi.getMaxDescent();
                dy += ladv;
                float bottomEdge = newDesc;
                if (newDesc < gi.getMaxDescent()) 
                    bottomEdge = gi.getMaxDescent();
                if ((dy + bottomEdge) > height)  {
                    if (!flowRectsIter.hasNext()) {
                        currentRegion = null;
                        gi = lineGI.copy(gi);
                        break; 
                    }
                    float oldWidth = width;
                    currentRegion = (RegionInfo) flowRectsIter.next();
                    x0     = (float) currentRegion.getX() + leftMargin;
                    y0     = (float) currentRegion.getY();
                    width  = (float)(currentRegion.getWidth() -
                                     (leftMargin+rightMargin));
                    height = (float) currentRegion.getHeight();
                    verticalAlignOffset = new Point2D.Float(0,0);
                    dy = firstLine ? mi.getTopMargin() : 0;
                    prevDesc  = 0;
                    if ((oldWidth > width) || (lnBreaks != 0)) {
                        gi = lineGI.copy(gi);
                    }
                    continue;
                }
                prevDesc = newDesc + (nextLineMult-1)*lineBoxHeight;
                nextLineMult = 0f;
                updateVerticalAlignOffset(verticalAlignOffset, 
                                          currentRegion, dy + bottomEdge);
                lineInfos.add(gi.newLine
                              (new Point2D.Float(x0, y0 + dy), width, partial, 
                               verticalAlignOffset));
                x0    -= leftMargin;
                width += leftMargin+rightMargin;
                leftMargin  = mi.getLeftMargin();
                rightMargin = mi.getRightMargin();
                x0    += leftMargin;
                width -= leftMargin+rightMargin;
                firstLine = false;
                lineGI  = gi.copy(lineGI);
                breakGI = null;
            }
            dy += prevDesc;
            int idx = gi.getGlyphIndex();
            while(idx <numGlyphs) 
                gv.setGlyphVisible(idx++, false);
            if (mi.isFlowRegionBreak()) {
                currentRegion = null;
                if (flowRectsIter.hasNext()) {
                    currentRegion = (RegionInfo) flowRectsIter.next();
                    height = (float) currentRegion.getHeight();
                    dy            = 0;
                    prevBotMargin = 0;
                    verticalAlignOffset = new Point2D.Float(0,0);
                }
            }
        }
        for (int chunk=0; chunk < acis.length; chunk++) {
            List lineInfos = chunkLineInfos[chunk];
            if (lineInfos == null) continue;
            AttributedCharacterIterator aci = acis[chunk];
            aci.first();
            MarginInfo mi = (MarginInfo)aci.getAttribute(FLOW_PARAGRAPH);
            if (mi == null) {
              continue;
            }
            int justification = mi.getJustification();
            GVTGlyphVector gv = gvs[chunk];
            if (gv == null) break;
            GlyphIterator gi = gis[chunk];
            layoutChunk(gv, gi.getOrigin(), justification, lineInfos);
        }
    }
    public static void updateVerticalAlignOffset
        (Point2D.Float verticalAlignOffset,
         RegionInfo region, float maxDescent)
        {
            float freeSpace = (float)region.getHeight() - maxDescent;
            verticalAlignOffset.setLocation
                (0, region.getVerticalAlignment() * freeSpace);
        }
    public static void layoutChunk(GVTGlyphVector gv, Point2D origin,
                                   int justification,
                                   List lineInfos) {
        Iterator lInfoIter = lineInfos.iterator();
        int numGlyphs      = gv.getNumGlyphs();
        float [] gp        = gv.getGlyphPositions(0, numGlyphs+1, null);
        Point2D.Float lineLoc  = null;
        float         lineAdv  = 0;
        float         lineVAdv = 0;
        float xOrig=(float)origin.getX();
        float yOrig=(float)origin.getY();
        float xScale=1;
        float xAdj=0;
        float charW=0;
        float lineWidth=0;
        boolean partial = false;
        float verticalAlignOffset = 0;
        int lineEnd = 0;
        int i;
        Point2D.Float pos = new Point2D.Float();
        for (i =0; i<numGlyphs; i++) {
            if (i == lineEnd) {
                xOrig += lineAdv;
                if (!lInfoIter.hasNext())
                    break;
                LineInfo li = (LineInfo)lInfoIter.next();
                lineEnd   = li.getEndIdx();
                lineLoc   = li.getLocation();
                lineAdv   = li.getAdvance();
                lineVAdv  = li.getVisualAdvance();
                charW     = li.getLastCharWidth();
                lineWidth = li.getLineWidth();
                partial   = li.isPartialLine();
                verticalAlignOffset = li.getVerticalAlignOffset().y;
                xAdj = 0;
                xScale = 1;
                switch (justification) {
                case 0: default: break;                  
                case 1:                                  
                    xAdj = (lineWidth - lineVAdv) / 2;
                    break;
                case 2:                                  
                    xAdj = lineWidth - lineVAdv;
                    break;
                case 3:                                  
                    if ((!partial) && (lineEnd != i+1)) {
                        xScale = (lineWidth-charW)/(lineVAdv-charW);
                    }
                    break;
                }
            }
            pos.x = lineLoc.x + (gp[2*i]  -xOrig)*xScale+xAdj;
            pos.y = lineLoc.y + ((gp[2 * i + 1] - yOrig) + 
                                 verticalAlignOffset);
            gv.setGlyphPosition(i, pos);
        }
        pos.x = xOrig;
        pos.y = yOrig;
        if (lineLoc != null) {
          pos.x = lineLoc.x + (gp[2*i]  -xOrig)*xScale+xAdj;
          pos.y = lineLoc.y + (gp[2 * i + 1] - yOrig) + verticalAlignOffset;
        }
        gv.setGlyphPosition(i, pos);
    }
}

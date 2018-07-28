package org.apache.batik.dom.svg;
import org.apache.batik.parser.DefaultPreserveAspectRatioHandler;
import org.apache.batik.parser.ParseException;
import org.apache.batik.parser.PreserveAspectRatioParser;
import org.apache.batik.util.SVGConstants;
import org.w3c.dom.DOMException;
import org.w3c.dom.svg.SVGPreserveAspectRatio;
public abstract class AbstractSVGPreserveAspectRatio
        implements SVGPreserveAspectRatio,
                   SVGConstants {
    protected static final String[] ALIGN_VALUES = {
        null,
        SVG_NONE_VALUE,
        SVG_XMINYMIN_VALUE,
        SVG_XMIDYMIN_VALUE,
        SVG_XMAXYMIN_VALUE,
        SVG_XMINYMID_VALUE,
        SVG_XMIDYMID_VALUE,
        SVG_XMAXYMID_VALUE,
        SVG_XMINYMAX_VALUE,
        SVG_XMIDYMAX_VALUE,
        SVG_XMAXYMAX_VALUE
    };
    protected static final String[] MEET_OR_SLICE_VALUES = {
        null,
        SVG_MEET_VALUE,
        SVG_SLICE_VALUE
    };
    public static String getValueAsString(short align, short meetOrSlice) {
        if (align < 1 || align > 10) {
            return null;
        }
        String value = ALIGN_VALUES[align];
        if (align == SVG_PRESERVEASPECTRATIO_NONE) {
            return value;
        }
        if (meetOrSlice < 1 || meetOrSlice > 2) {
            return null;
        }
        return value + ' ' + MEET_OR_SLICE_VALUES[meetOrSlice];
    }
    protected short align =
        SVGPreserveAspectRatio.SVG_PRESERVEASPECTRATIO_XMIDYMID;
    protected short meetOrSlice = SVGPreserveAspectRatio.SVG_MEETORSLICE_MEET;
    public AbstractSVGPreserveAspectRatio() {
    }
    public short getAlign() {
        return this.align;
    }
    public short getMeetOrSlice() {
        return this.meetOrSlice;
    }
    public void setAlign(short align)  {
        this.align = align;
        setAttributeValue(getValueAsString());
    }
    public void setMeetOrSlice(short meetOrSlice) {
        this.meetOrSlice = meetOrSlice;
        setAttributeValue(getValueAsString());
    }
    public void reset() {
        align = SVGPreserveAspectRatio.SVG_PRESERVEASPECTRATIO_XMIDYMID;
        meetOrSlice = SVGPreserveAspectRatio.SVG_MEETORSLICE_MEET;
    }
    protected abstract void setAttributeValue(String value)
        throws DOMException;
    protected abstract DOMException createDOMException(short type, String key,
                                                       Object[] args);
    protected void setValueAsString(String value) throws DOMException {
        PreserveAspectRatioParserHandler ph;
        ph = new PreserveAspectRatioParserHandler();
        try {
            PreserveAspectRatioParser p = new PreserveAspectRatioParser();
            p.setPreserveAspectRatioHandler(ph);
            p.parse(value);
            align = ph.getAlign();
            meetOrSlice = ph.getMeetOrSlice();
        } catch (ParseException ex) {
            throw createDOMException
                (DOMException.INVALID_MODIFICATION_ERR, "preserve.aspect.ratio",
                 new Object[] { value });
        }
    }
    protected String getValueAsString() {
        if (align < 1 || align > 10) {
            throw createDOMException
                (DOMException.INVALID_MODIFICATION_ERR,
                 "preserve.aspect.ratio.align",
                 new Object[] { new Integer(align) });
        }
        String value = ALIGN_VALUES[align];
        if (align == SVG_PRESERVEASPECTRATIO_NONE) {
            return value;
        }
        if (meetOrSlice < 1 || meetOrSlice > 2) {
            throw createDOMException
                (DOMException.INVALID_MODIFICATION_ERR,
                 "preserve.aspect.ratio.meet.or.slice",
                 new Object[] { new Integer(meetOrSlice) });
        }
        return value + ' ' + MEET_OR_SLICE_VALUES[meetOrSlice];
    }
    protected class PreserveAspectRatioParserHandler
        extends DefaultPreserveAspectRatioHandler {
        public short align =
            SVGPreserveAspectRatio.SVG_PRESERVEASPECTRATIO_XMIDYMID;
        public short meetOrSlice = SVGPreserveAspectRatio.SVG_MEETORSLICE_MEET;
        public short getAlign() {
            return align;
        }
        public short getMeetOrSlice() {
            return meetOrSlice;
        }
        public void none() throws ParseException {
            align = SVGPreserveAspectRatio.SVG_PRESERVEASPECTRATIO_NONE;
        }
        public void xMaxYMax() throws ParseException {
            align = SVGPreserveAspectRatio.SVG_PRESERVEASPECTRATIO_XMAXYMAX;
        }
        public void xMaxYMid() throws ParseException {
            align = SVGPreserveAspectRatio.SVG_PRESERVEASPECTRATIO_XMAXYMID;
        }
        public void xMaxYMin() throws ParseException {
            align = SVGPreserveAspectRatio.SVG_PRESERVEASPECTRATIO_XMAXYMIN;
        }
        public void xMidYMax() throws ParseException {
            align = SVGPreserveAspectRatio.SVG_PRESERVEASPECTRATIO_XMIDYMAX;
        }
        public void xMidYMid() throws ParseException {
            align = SVGPreserveAspectRatio.SVG_PRESERVEASPECTRATIO_XMIDYMID;
        }
        public void xMidYMin() throws ParseException {
            align = SVGPreserveAspectRatio.SVG_PRESERVEASPECTRATIO_XMIDYMIN;
        }
        public void xMinYMax() throws ParseException {
            align = SVGPreserveAspectRatio.SVG_PRESERVEASPECTRATIO_XMINYMAX;
        }
        public void xMinYMid() throws ParseException {
            align = SVGPreserveAspectRatio.SVG_PRESERVEASPECTRATIO_XMINYMID;
        }
        public void xMinYMin() throws ParseException {
            align = SVGPreserveAspectRatio.SVG_PRESERVEASPECTRATIO_XMINYMIN;
        }
        public void meet() throws ParseException {
            meetOrSlice = SVGPreserveAspectRatio.SVG_MEETORSLICE_MEET;
        }
        public void slice() throws ParseException {
            meetOrSlice = SVGPreserveAspectRatio.SVG_MEETORSLICE_SLICE;
        }
    }
}

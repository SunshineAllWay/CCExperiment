package org.apache.batik.svggen.font;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Set;
import java.util.HashSet;
import org.apache.batik.svggen.font.table.CmapFormat;
import org.apache.batik.svggen.font.table.Feature;
import org.apache.batik.svggen.font.table.FeatureTags;
import org.apache.batik.svggen.font.table.GsubTable;
import org.apache.batik.svggen.font.table.KernSubtable;
import org.apache.batik.svggen.font.table.KernTable;
import org.apache.batik.svggen.font.table.KerningPair;
import org.apache.batik.svggen.font.table.LangSys;
import org.apache.batik.svggen.font.table.PostTable;
import org.apache.batik.svggen.font.table.Script;
import org.apache.batik.svggen.font.table.ScriptTags;
import org.apache.batik.svggen.font.table.SingleSubst;
import org.apache.batik.svggen.font.table.Table;
import org.apache.batik.util.SVGConstants;
import org.apache.batik.util.XMLConstants;
public class SVGFont implements XMLConstants, SVGConstants, ScriptTags, FeatureTags {
    static final String EOL;
    static final String PROPERTY_LINE_SEPARATOR = "line.separator";
    static final String PROPERTY_LINE_SEPARATOR_DEFAULT = "\n";
    static final int DEFAULT_FIRST = 32;
    static final int DEFAULT_LAST = 126;
    static {
        String  temp;
        try {
            temp = System.getProperty (PROPERTY_LINE_SEPARATOR,
                                       PROPERTY_LINE_SEPARATOR_DEFAULT);
        } catch (SecurityException e) {
            temp = PROPERTY_LINE_SEPARATOR_DEFAULT;
        }
        EOL = temp;
    }
    private static String QUOT_EOL = XML_CHAR_QUOT + EOL;
    private static String CONFIG_USAGE =
        "SVGFont.config.usage";
    private static String CONFIG_SVG_BEGIN =
        "SVGFont.config.svg.begin";
    private static String CONFIG_SVG_TEST_CARD_START =
        "SVGFont.config.svg.test.card.start";
    private static String CONFIG_SVG_TEST_CARD_END =
        "SVGFont.config.svg.test.card.end";
    protected static String encodeEntities(String s) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == XML_CHAR_LT) {
                sb.append(XML_ENTITY_LT);
            } else if (s.charAt(i) == XML_CHAR_GT) {
                sb.append(XML_ENTITY_GT);
            } else if (s.charAt(i) == XML_CHAR_AMP) {
                sb.append(XML_ENTITY_AMP);
            } else if (s.charAt(i) == XML_CHAR_APOS) {
                sb.append(XML_ENTITY_APOS);
            } else if(s.charAt(i) == XML_CHAR_QUOT) {
                sb.append(XML_ENTITY_QUOT);
            } else {
                sb.append(s.charAt(i));
            }
        }
        return sb.toString();
    }
    protected static String getContourAsSVGPathData(Glyph glyph, int startIndex, int count) {
        if (glyph.getPoint(startIndex).endOfContour) {
            return "";
        }
        StringBuffer sb = new StringBuffer();
        int offset = 0;
        while (offset < count) {
            Point point = glyph.getPoint(startIndex + offset%count);
            Point point_plus1 = glyph.getPoint(startIndex + (offset+1)%count);
            Point point_plus2 = glyph.getPoint(startIndex + (offset+2)%count);
            if (offset == 0) {
                sb.append(PATH_MOVE)
                .append(String.valueOf(point.x))
                .append(XML_SPACE)
                .append(String.valueOf(point.y));
            }
            if (point.onCurve && point_plus1.onCurve) {
                if (point_plus1.x == point.x) { 
                    sb.append(PATH_VERTICAL_LINE_TO)
                    .append(String.valueOf(point_plus1.y));
                } else if (point_plus1.y == point.y) { 
                    sb.append(PATH_HORIZONTAL_LINE_TO)
                    .append(String.valueOf(point_plus1.x));
                } else {
                    sb.append(PATH_LINE_TO)
                    .append(String.valueOf(point_plus1.x))
                    .append(XML_SPACE)
                    .append(String.valueOf(point_plus1.y));
                }
                offset++;
            } else if (point.onCurve && !point_plus1.onCurve && point_plus2.onCurve) {
                sb.append(PATH_QUAD_TO)
                .append(String.valueOf(point_plus1.x))
                .append(XML_SPACE)
                .append(String.valueOf(point_plus1.y))
                .append(XML_SPACE)
                .append(String.valueOf(point_plus2.x))
                .append(XML_SPACE)
                .append(String.valueOf(point_plus2.y));
                offset+=2;
            } else if (point.onCurve && !point_plus1.onCurve && !point_plus2.onCurve) {
                sb.append(PATH_QUAD_TO)
                .append(String.valueOf(point_plus1.x))
                .append(XML_SPACE)
                .append(String.valueOf(point_plus1.y))
                .append(XML_SPACE)
                .append(String.valueOf(midValue(point_plus1.x, point_plus2.x)))
                .append(XML_SPACE)
                .append(String.valueOf(midValue(point_plus1.y, point_plus2.y)));
                offset+=2;
            } else if (!point.onCurve && !point_plus1.onCurve) {
                sb.append(PATH_SMOOTH_QUAD_TO)
                .append(String.valueOf(midValue(point.x, point_plus1.x)))
                .append(XML_SPACE)
                .append(String.valueOf(midValue(point.y, point_plus1.y)));
                offset++;
            } else if (!point.onCurve && point_plus1.onCurve) {
                sb.append(PATH_SMOOTH_QUAD_TO)
                .append(String.valueOf(point_plus1.x))
                .append(XML_SPACE)
                .append(String.valueOf(point_plus1.y));
                offset++;
            } else {
                System.out.println("drawGlyph case not catered for!!");
                break;
            }
        }
        sb.append(PATH_CLOSE);
        return sb.toString();
    }
    protected static String getSVGFontFaceElement(Font font) {
        StringBuffer sb = new StringBuffer();
        String fontFamily = font.getNameTable().getRecord(Table.nameFontFamilyName);
        short unitsPerEm = font.getHeadTable().getUnitsPerEm();
        String panose = font.getOS2Table().getPanose().toString();
        short ascent = font.getHheaTable().getAscender();
        short descent = font.getHheaTable().getDescender();
        int baseline = 0; 
        sb.append(XML_OPEN_TAG_START).append(SVG_FONT_FACE_TAG).append(EOL)
            .append(XML_TAB).append(SVG_FONT_FAMILY_ATTRIBUTE).append(XML_EQUAL_QUOT).append(fontFamily).append(QUOT_EOL)
            .append(XML_TAB).append(SVG_UNITS_PER_EM_ATTRIBUTE).append(XML_EQUAL_QUOT).append(unitsPerEm).append(QUOT_EOL)
            .append(XML_TAB).append(SVG_PANOSE_1_ATTRIBUTE).append(XML_EQUAL_QUOT).append(panose).append(QUOT_EOL)
            .append(XML_TAB).append(SVG_ASCENT_ATTRIBUTE).append(XML_EQUAL_QUOT).append(ascent).append(QUOT_EOL)
            .append(XML_TAB).append(SVG_DESCENT_ATTRIBUTE).append(XML_EQUAL_QUOT).append(descent).append(QUOT_EOL)
            .append(XML_TAB).append(SVG_ALPHABETIC_ATTRIBUTE).append(XML_EQUAL_QUOT).append(baseline).append(XML_CHAR_QUOT)
            .append(XML_OPEN_TAG_END_NO_CHILDREN).append(EOL);
        return sb.toString();
    }
    protected static void writeFontAsSVGFragment(PrintStream ps, Font font, String id, int first, int last, boolean autoRange, boolean forceAscii)
    throws Exception {
        int horiz_advance_x = font.getOS2Table().getAvgCharWidth();
        ps.print(XML_OPEN_TAG_START);
        ps.print(SVG_FONT_TAG);
        ps.print(XML_SPACE);
        if (id != null) {
            ps.print(SVG_ID_ATTRIBUTE);
            ps.print(XML_EQUAL_QUOT);
            ps.print(id);
            ps.print(XML_CHAR_QUOT);
            ps.print(XML_SPACE);
        }
        ps.print(SVG_HORIZ_ADV_X_ATTRIBUTE);
        ps.print(XML_EQUAL_QUOT);
        ps.print(horiz_advance_x);
        ps.print(XML_CHAR_QUOT);
        ps.print(XML_OPEN_TAG_END_CHILDREN);
        ps.print(getSVGFontFaceElement(font));
        CmapFormat cmapFmt = null;
        if (forceAscii) {
            cmapFmt = font.getCmapTable().getCmapFormat(
                Table.platformMacintosh,
                Table.encodingRoman );
        } else {
            cmapFmt = font.getCmapTable().getCmapFormat(
                Table.platformMicrosoft,
                Table.encodingUGL );
            if (cmapFmt == null) {
                cmapFmt = font.getCmapTable().getCmapFormat(
                    Table.platformMicrosoft,
                    Table.encodingUndefined );
            }
        }
        if (cmapFmt == null) {
            throw new Exception("Cannot find a suitable cmap table");
        }
        GsubTable gsub = (GsubTable) font.getTable(Table.GSUB);
        SingleSubst initialSubst = null;
        SingleSubst medialSubst = null;
        SingleSubst terminalSubst = null;
        if (gsub != null) {
            Script s = gsub.getScriptList().findScript(SCRIPT_TAG_ARAB);
            if (s != null) {
                LangSys ls = s.getDefaultLangSys();
                if (ls != null) {
                    Feature init = gsub.getFeatureList().findFeature(ls, FEATURE_TAG_INIT);
                    Feature medi = gsub.getFeatureList().findFeature(ls, FEATURE_TAG_MEDI);
                    Feature fina = gsub.getFeatureList().findFeature(ls, FEATURE_TAG_FINA);
                    if (init != null) {
                        initialSubst = (SingleSubst)
                            gsub.getLookupList().getLookup(init, 0).getSubtable(0);
                    }
                    if (medi != null) {
                        medialSubst = (SingleSubst)
                            gsub.getLookupList().getLookup(medi, 0).getSubtable(0);
                    }
                    if (fina != null) {
                        terminalSubst = (SingleSubst)
                            gsub.getLookupList().getLookup(fina, 0).getSubtable(0);
                    }
                }
            }
        }
        ps.println(getGlyphAsSVG(font, font.getGlyph(0), 0, horiz_advance_x,
            initialSubst, medialSubst, terminalSubst, ""));
        try {
            if (first == -1) {
                if (!autoRange) first = DEFAULT_FIRST;
                else            first = cmapFmt.getFirst();
            }
            if (last == -1) {
                if (!autoRange) last = DEFAULT_LAST;
                else            last = cmapFmt.getLast();
            }
            Set glyphSet = new HashSet();
            for (int i = first; i <= last; i++) {
                int glyphIndex = cmapFmt.mapCharCode(i);
                if (glyphIndex > 0) {
                    glyphSet.add(glyphIndex);
                    ps.println(getGlyphAsSVG(
                        font,
                        font.getGlyph(glyphIndex),
                        glyphIndex,
                        horiz_advance_x,
                        initialSubst, medialSubst, terminalSubst,
                        (32 <= i && i <= 127) ?
                        encodeEntities( String.valueOf( (char)i ) ) :
                        XML_CHAR_REF_PREFIX + Integer.toHexString(i) + XML_CHAR_REF_SUFFIX));
                }
            }
            KernTable kern = (KernTable) font.getTable(Table.kern);
            if (kern != null) {
                KernSubtable kst = kern.getSubtable(0);
                PostTable post = (PostTable) font.getTable(Table.post);
                for (int i = 0; i < kst.getKerningPairCount(); i++) {
                    KerningPair kpair = kst.getKerningPair(i);
                    if (glyphSet.contains(kpair.getLeft()) && glyphSet.contains(kpair.getRight())) {
                        ps.println(getKerningPairAsSVG(kpair, post));
                    }
                }
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
        ps.print(XML_CLOSE_TAG_START);
        ps.print(SVG_FONT_TAG);
        ps.println(XML_CLOSE_TAG_END);
    }
    protected static String getGlyphAsSVG(
            Font font,
            Glyph glyph,
            int glyphIndex,
            int defaultHorizAdvanceX,
            String attrib,
            String code) {
        StringBuffer sb = new StringBuffer();
        int firstIndex = 0;
        int count = 0;
        int i;
        int horiz_advance_x;
        horiz_advance_x = font.getHmtxTable().getAdvanceWidth(glyphIndex);
        if (glyphIndex == 0) {
            sb.append(XML_OPEN_TAG_START);
            sb.append(SVG_MISSING_GLYPH_TAG);
        } else {
            sb.append(XML_OPEN_TAG_START)
                .append(SVG_GLYPH_TAG).append(XML_SPACE).append(SVG_UNICODE_ATTRIBUTE)
                .append(XML_EQUAL_QUOT).append(code).append(XML_CHAR_QUOT);
            String glyphName = font.getPostTable().getGlyphName(glyphIndex);
            if (glyphName != null) {
                sb.append(XML_SPACE).append(SVG_GLYPH_NAME_ATTRIBUTE).append(XML_EQUAL_QUOT)
                    .append(glyphName)
                    .append(XML_CHAR_QUOT);
            }
        }
        if (horiz_advance_x != defaultHorizAdvanceX) {
            sb.append(XML_SPACE).append(SVG_HORIZ_ADV_X_ATTRIBUTE).append(XML_EQUAL_QUOT)
                .append(horiz_advance_x).append(XML_CHAR_QUOT);
        }
        if (attrib != null) {
            sb.append(attrib);
        }
        if (glyph != null) {
            sb.append(XML_SPACE).append(SVG_D_ATTRIBUTE).append(XML_EQUAL_QUOT);
            for (i = 0; i < glyph.getPointCount(); i++) {
                count++;
                if (glyph.getPoint(i).endOfContour) {
                    sb.append(getContourAsSVGPathData(glyph, firstIndex, count));
                    firstIndex = i + 1;
                    count = 0;
                }
            }
            sb.append(XML_CHAR_QUOT);
        }
        sb.append(XML_OPEN_TAG_END_NO_CHILDREN);
        chopUpStringBuffer(sb);
        return sb.toString();
    }
    protected static String getGlyphAsSVG(
            Font font,
            Glyph glyph,
            int glyphIndex,
            int defaultHorizAdvanceX,
            SingleSubst arabInitSubst,
            SingleSubst arabMediSubst,
            SingleSubst arabTermSubst,
            String code) {
        StringBuffer sb = new StringBuffer();
        boolean substituted = false;
        int arabInitGlyphIndex = glyphIndex;
        int arabMediGlyphIndex = glyphIndex;
        int arabTermGlyphIndex = glyphIndex;
        if (arabInitSubst != null) {
            arabInitGlyphIndex = arabInitSubst.substitute(glyphIndex);
        }
        if (arabMediSubst != null) {
            arabMediGlyphIndex = arabMediSubst.substitute(glyphIndex);
        }
        if (arabTermSubst != null) {
            arabTermGlyphIndex = arabTermSubst.substitute(glyphIndex);
        }
        if (arabInitGlyphIndex != glyphIndex) {
            sb.append(getGlyphAsSVG(
                font,
                font.getGlyph(arabInitGlyphIndex),
                arabInitGlyphIndex,
                defaultHorizAdvanceX,
                (XML_SPACE + SVG_ARABIC_FORM_ATTRIBUTE + XML_EQUAL_QUOT +
                 SVG_INITIAL_VALUE + XML_CHAR_QUOT),
                code));
            sb.append(EOL);
            substituted = true;
        }
        if (arabMediGlyphIndex != glyphIndex) {
            sb.append(getGlyphAsSVG(
                font,
                font.getGlyph(arabMediGlyphIndex),
                arabMediGlyphIndex,
                defaultHorizAdvanceX,
                (XML_SPACE + SVG_ARABIC_FORM_ATTRIBUTE + XML_EQUAL_QUOT +
                 SVG_MEDIAL_VALUE + XML_CHAR_QUOT),
                code));
            sb.append(EOL);
            substituted = true;
        }
        if (arabTermGlyphIndex != glyphIndex) {
            sb.append(getGlyphAsSVG(
                font,
                font.getGlyph(arabTermGlyphIndex),
                arabTermGlyphIndex,
                defaultHorizAdvanceX,
                (XML_SPACE + SVG_ARABIC_FORM_ATTRIBUTE + XML_EQUAL_QUOT +
                 SVG_TERMINAL_VALUE + XML_CHAR_QUOT),
                code));
            sb.append(EOL);
            substituted = true;
        }
        if (substituted) {
            sb.append(getGlyphAsSVG(
                font,
                glyph,
                glyphIndex,
                defaultHorizAdvanceX,
                (XML_SPACE + SVG_ARABIC_FORM_ATTRIBUTE + XML_EQUAL_QUOT +
                 SVG_ISOLATED_VALUE + XML_CHAR_QUOT),
                code));
        } else {
            sb.append(getGlyphAsSVG(
                font,
                glyph,
                glyphIndex,
                defaultHorizAdvanceX,
                null,
                code));
        }
        return sb.toString();
    }
    protected static String getKerningPairAsSVG(KerningPair kp, PostTable post) {
        String leftGlyphName = post.getGlyphName(kp.getLeft());
        String rightGlyphName = post.getGlyphName(kp.getRight());
        StringBuffer sb = new StringBuffer();
        sb.append(XML_OPEN_TAG_START).append(SVG_HKERN_TAG).append(XML_SPACE);
        if (leftGlyphName == null) {
            sb.append(SVG_U1_ATTRIBUTE).append(XML_EQUAL_QUOT);
            sb.append(kp.getLeft());
        } else {
            sb.append(SVG_G1_ATTRIBUTE).append(XML_EQUAL_QUOT);
            sb.append(leftGlyphName);
        }
        sb.append(XML_CHAR_QUOT).append(XML_SPACE);
        if (rightGlyphName == null) {
            sb.append(SVG_U2_ATTRIBUTE).append(XML_EQUAL_QUOT);
            sb.append(kp.getRight());
        } else {
            sb.append(SVG_G2_ATTRIBUTE).append(XML_EQUAL_QUOT);
            sb.append(rightGlyphName);
        }
        sb.append(XML_CHAR_QUOT).append(XML_SPACE).append(SVG_K_ATTRIBUTE).append(XML_EQUAL_QUOT);
        sb.append(-kp.getValue());
        sb.append(XML_CHAR_QUOT).append(XML_OPEN_TAG_END_NO_CHILDREN);
        return sb.toString();
    }
    protected static void writeSvgBegin(PrintStream ps) {
        ps.println(Messages.formatMessage(CONFIG_SVG_BEGIN,
                                          new Object[]{SVG_PUBLIC_ID, SVG_SYSTEM_ID}));
    }
    protected static void writeSvgDefsBegin(PrintStream ps) {
        ps.println(XML_OPEN_TAG_START + SVG_DEFS_TAG + XML_OPEN_TAG_END_CHILDREN);
    }
    protected static void writeSvgDefsEnd(PrintStream ps) {
        ps.println(XML_CLOSE_TAG_START + SVG_DEFS_TAG + XML_CLOSE_TAG_END);
    }
    protected static void writeSvgEnd(PrintStream ps) {
        ps.println(XML_CLOSE_TAG_START + SVG_SVG_TAG + XML_CLOSE_TAG_END);
    }
    protected static void writeSvgTestCard(PrintStream ps, String fontFamily) {
        ps.println(Messages.formatMessage(CONFIG_SVG_TEST_CARD_START, null));
        ps.println(fontFamily);
        ps.println(Messages.formatMessage(CONFIG_SVG_TEST_CARD_END, null));
    }
    public static final char   ARG_KEY_START_CHAR = '-';
    public static final String ARG_KEY_CHAR_RANGE_LOW = "-l";
    public static final String ARG_KEY_CHAR_RANGE_HIGH = "-h";
    public static final String ARG_KEY_ID = "-id";
    public static final String ARG_KEY_ASCII = "-ascii";
    public static final String ARG_KEY_TESTCARD = "-testcard";
    public static final String ARG_KEY_AUTO_RANGE = "-autorange";
    public static final String ARG_KEY_OUTPUT_PATH = "-o";
    public static void main(String[] args) {
        try {
            String path = parseArgs(args, null);
            String low = parseArgs(args, ARG_KEY_CHAR_RANGE_LOW);
            String high = parseArgs(args, ARG_KEY_CHAR_RANGE_HIGH);
            String id = parseArgs(args, ARG_KEY_ID);
            String ascii = parseArgs(args, ARG_KEY_ASCII);
            String testCard = parseArgs(args, ARG_KEY_TESTCARD);
            String outPath = parseArgs(args, ARG_KEY_OUTPUT_PATH);
            String autoRange = parseArgs(args, ARG_KEY_AUTO_RANGE);
            PrintStream ps = null;
            FileOutputStream fos = null;
            if (outPath != null) {
                fos = new FileOutputStream(outPath);
                ps = new PrintStream(fos);
            } else {
                ps = System.out;
            }
            if (path != null) {
                Font font = Font.create(path);
                writeSvgBegin(ps);
                writeSvgDefsBegin(ps);
                writeFontAsSVGFragment(
                    ps,
                    font,
                    id,
                    (low != null ? Integer.parseInt(low) : -1),
                    (high != null ? Integer.parseInt(high) : -1),
                    (autoRange != null),
                    (ascii != null));
                writeSvgDefsEnd(ps);
                if (testCard != null) {
                    String fontFamily = font.getNameTable().getRecord(Table.nameFontFamilyName);
                    writeSvgTestCard(ps, fontFamily);
                }
                writeSvgEnd(ps);
                if (fos != null) {
                    fos.close();
                }
            } else {
                usage();
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println(e.getMessage());
            usage();
        }
    }
    private static void chopUpStringBuffer(StringBuffer sb) {
        if (sb.length() < 256) {
            return;
        } else {
            for (int i = 240; i < sb.length(); i++) {
                if (sb.charAt(i) == ' ') {
                    sb.setCharAt(i, '\n');
                    i += 240;
                }
            }
        }
    }
    private static int midValue(int a, int b) {
        return a + (b - a)/2;
    }
    private static String parseArgs(String[] args, String name) {
        for (int i = 0; i < args.length; i++) {
            if (name == null) {
                if (args[i].charAt(0) != ARG_KEY_START_CHAR) {
                    return args[i];
                }
            } else if (name.equalsIgnoreCase(args[i])) {
                if ((i < args.length - 1) && (args[i+1].charAt(0) != ARG_KEY_START_CHAR)) {
                    return args[i+1];
                } else {
                    return args[i];
                }
            }
        }
        return null;
    }
    private static void usage() {
        System.err.println(Messages.formatMessage(CONFIG_USAGE, null));
    }
}

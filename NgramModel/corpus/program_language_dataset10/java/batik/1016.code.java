package org.apache.batik.gvt.font;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.List;
import java.util.ArrayList;
public class FontFamilyResolver {
    public static final AWTFontFamily defaultFont =
        new AWTFontFamily("SansSerif");
    protected static final Map fonts = new HashMap();
    protected static final List awtFontFamilies = new ArrayList();
    protected static final List awtFonts = new ArrayList();
    static {
        fonts.put("sans-serif",      "SansSerif");
        fonts.put("serif",           "Serif");
        fonts.put("times",           "Serif");
        fonts.put("times new roman", "Serif");
        fonts.put("cursive",         "Dialog");
        fonts.put("fantasy",         "Symbol");
        fonts.put("monospace",       "Monospaced");
        fonts.put("monospaced",      "Monospaced");
        fonts.put("courier",         "Monospaced");
        GraphicsEnvironment env;
        env = GraphicsEnvironment.getLocalGraphicsEnvironment();
        String[] fontNames = env.getAvailableFontFamilyNames();
        int nFonts = fontNames != null ? fontNames.length : 0;
        for(int i=0; i<nFonts; i++){
            fonts.put(fontNames[i].toLowerCase(), fontNames[i]);
            StringTokenizer st = new StringTokenizer(fontNames[i]);
            String fontNameWithoutSpaces = "";
            while (st.hasMoreTokens()) {
                fontNameWithoutSpaces += st.nextToken();
            }
            fonts.put(fontNameWithoutSpaces.toLowerCase(), fontNames[i]);
            String fontNameWithDashes = fontNames[i].replace(' ', '-');
            if (!fontNameWithDashes.equals(fontNames[i])) {
               fonts.put(fontNameWithDashes.toLowerCase(), fontNames[i]);
            }
        }
        Font[] allFonts = env.getAllFonts();
        for (int i = 0; i < allFonts.length; i++) {
            Font f = allFonts[i];
            fonts.put(f.getFontName().toLowerCase(), f.getFontName());
        }
        awtFontFamilies.add(defaultFont);
        awtFonts.add(new AWTGVTFont(defaultFont.getFamilyName(), 0, 12));
        Collection fontValues = fonts.values();
        Iterator iter = fontValues.iterator();
        while(iter.hasNext()) {
            String fontFamily = (String)iter.next();
            AWTFontFamily awtFontFamily = new AWTFontFamily(fontFamily);
            awtFontFamilies.add(awtFontFamily);
            AWTGVTFont font = new AWTGVTFont(fontFamily, 0, 12);
            awtFonts.add(font);
        }
    }
    protected static final Map resolvedFontFamilies = new HashMap();
    public static String lookup(String familyName) {
        return (String)fonts.get(familyName.toLowerCase());
    }
    public static GVTFontFamily resolve(String familyName) {
        familyName = familyName.toLowerCase();
        GVTFontFamily resolvedFF =
            (GVTFontFamily)resolvedFontFamilies.get(familyName);
        if (resolvedFF == null) { 
            String awtFamilyName = (String)fonts.get(familyName);
            if (awtFamilyName != null) {
                resolvedFF = new AWTFontFamily(awtFamilyName);
            }
            resolvedFontFamilies.put(familyName, resolvedFF);
        }
        return resolvedFF;
    }
    public static GVTFontFamily resolve(UnresolvedFontFamily fontFamily) {
        return resolve(fontFamily.getFamilyName());
    }
    public static GVTFontFamily getFamilyThatCanDisplay(char c) {
        for (int i = 0; i < awtFontFamilies.size(); i++) {
            AWTFontFamily fontFamily = (AWTFontFamily)awtFontFamilies.get(i);
            AWTGVTFont font = (AWTGVTFont)awtFonts.get(i);
            if (font.canDisplay(c) && fontFamily.getFamilyName().indexOf("Song") == -1) {
                return fontFamily;
            }
        }
        return null;
    }
}

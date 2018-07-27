package org.apache.batik.svggen.font.table;
import java.io.IOException;
import java.io.RandomAccessFile;
public class PostTable implements Table {
    private static final String[] macGlyphName = {
        ".notdef",      
        "null",         
        "CR",           
        "space",        
        "exclam",       
        "quotedbl",     
        "numbersign",   
        "dollar",       
        "percent",      
        "ampersand",    
        "quotesingle",  
        "parenleft",    
        "parenright",   
        "asterisk",     
        "plus",         
        "comma",        
        "hyphen",       
        "period",       
        "slash",        
        "zero",         
        "one",          
        "two",          
        "three",        
        "four",         
        "five",         
        "six",          
        "seven",        
        "eight",        
        "nine",         
        "colon",        
        "semicolon",    
        "less",         
        "equal",        
        "greater",      
        "question",     
        "at",           
        "A",            
        "B",            
        "C",            
        "D",            
        "E",            
        "F",            
        "G",            
        "H",            
        "I",            
        "J",            
        "K",            
        "L",            
        "M",            
        "N",            
        "O",            
        "P",            
        "Q",            
        "R",            
        "S",            
        "T",            
        "U",            
        "V",            
        "W",            
        "X",            
        "Y",            
        "Z",            
        "bracketleft",  
        "backslash",    
        "bracketright", 
        "asciicircum",  
        "underscore",   
        "grave",        
        "a",            
        "b",            
        "c",            
        "d",            
        "e",            
        "f",            
        "g",            
        "h",            
        "i",            
        "j",            
        "k",            
        "l",            
        "m",            
        "n",            
        "o",            
        "p",            
        "q",            
        "r",            
        "s",            
        "t",            
        "u",            
        "v",            
        "w",            
        "x",            
        "y",            
        "z",            
        "braceleft",    
        "bar",          
        "braceright",   
        "asciitilde",   
        "Adieresis",    
        "Aring",        
        "Ccedilla",     
        "Eacute",       
        "Ntilde",       
        "Odieresis",    
        "Udieresis",    
        "aacute",       
        "agrave",       
        "acircumflex",  
        "adieresis",    
        "atilde",       
        "aring",        
        "ccedilla",     
        "eacute",       
        "egrave",       
        "ecircumflex",  
        "edieresis",    
        "iacute",       
        "igrave",       
        "icircumflex",  
        "idieresis",    
        "ntilde",       
        "oacute",       
        "ograve",       
        "ocircumflex",  
        "odieresis",    
        "otilde",       
        "uacute",       
        "ugrave",       
        "ucircumflex",  
        "udieresis",    
        "dagger",       
        "degree",       
        "cent",         
        "sterling",     
        "section",      
        "bullet",       
        "paragraph",    
        "germandbls",   
        "registered",   
        "copyright",    
        "trademark",    
        "acute",        
        "dieresis",     
        "notequal",     
        "AE",           
        "Oslash",       
        "infinity",     
        "plusminus",    
        "lessequal",    
        "greaterequal", 
        "yen",          
        "mu",           
        "partialdiff",  
        "summation",    
        "product",      
        "pi",           
        "integral'",    
        "ordfeminine",  
        "ordmasculine", 
        "Omega",        
        "ae",           
        "oslash",       
        "questiondown", 
        "exclamdown",   
        "logicalnot",   
        "radical",      
        "florin",       
        "approxequal",  
        "increment",    
        "guillemotleft",
        "guillemotright",
        "ellipsis",     
        "nbspace",      
        "Agrave",       
        "Atilde",       
        "Otilde",       
        "OE",           
        "oe",           
        "endash",       
        "emdash",       
        "quotedblleft", 
        "quotedblright",
        "quoteleft",    
        "quoteright",   
        "divide",       
        "lozenge",      
        "ydieresis",    
        "Ydieresis",    
        "fraction",     
        "currency",     
        "guilsinglleft",
        "guilsinglright",
        "fi",           
        "fl",           
        "daggerdbl",    
        "middot",       
        "quotesinglbase",
        "quotedblbase", 
        "perthousand",  
        "Acircumflex",  
        "Ecircumflex",  
        "Aacute",       
        "Edieresis",    
        "Egrave",       
        "Iacute",       
        "Icircumflex",  
        "Idieresis",    
        "Igrave",       
        "Oacute",       
        "Ocircumflex",  
        "",             
        "Ograve",       
        "Uacute",       
        "Ucircumflex",  
        "Ugrave",       
        "dotlessi",     
        "circumflex",   
        "tilde",        
        "overscore",    
        "breve",        
        "dotaccent",    
        "ring",         
        "cedilla",      
        "hungarumlaut", 
        "ogonek",       
        "caron",        
        "Lslash",       
        "lslash",       
        "Scaron",       
        "scaron",       
        "Zcaron",       
        "zcaron",       
        "brokenbar",    
        "Eth",          
        "eth",          
        "Yacute",       
        "yacute",       
        "Thorn",        
        "thorn",        
        "minus",        
        "multiply",     
        "onesuperior",  
        "twosuperior",  
        "threesuperior",
        "onehalf",      
        "onequarter",   
        "threequarters",
        "franc",        
        "Gbreve",       
        "gbreve",       
        "Idot",         
        "Scedilla",     
        "scedilla",     
        "Cacute",       
        "cacute",       
        "Ccaron",       
        "ccaron",       
        ""              
    };
    private int version;
    private int italicAngle;
    private short underlinePosition;
    private short underlineThickness;
    private int isFixedPitch;
    private int minMemType42;
    private int maxMemType42;
    private int minMemType1;
    private int maxMemType1;
    private int numGlyphs;
    private int[] glyphNameIndex;
    private String[] psGlyphName;
    protected PostTable(DirectoryEntry de, RandomAccessFile raf) throws IOException {
        raf.seek(de.getOffset());
        version = raf.readInt();
        italicAngle = raf.readInt();
        underlinePosition = raf.readShort();
        underlineThickness = raf.readShort();
        isFixedPitch = raf.readInt();
        minMemType42 = raf.readInt();
        maxMemType42 = raf.readInt();
        minMemType1 = raf.readInt();
        maxMemType1 = raf.readInt();
        if (version == 0x00020000) {
            numGlyphs = raf.readUnsignedShort();
            glyphNameIndex = new int[numGlyphs];
            for (int i = 0; i < numGlyphs; i++) {
                glyphNameIndex[i] = raf.readUnsignedShort();
            }
            int h = highestGlyphNameIndex();
            if (h > 257) {
                h -= 257;
                psGlyphName = new String[h];
                for (int i = 0; i < h; i++) {
                    int len = raf.readUnsignedByte();
                    byte[] buf = new byte[len];
                    raf.readFully(buf);
                    psGlyphName[i] = new String(buf);
                }
            }
        } else if (version == 0x00020005) {
        }
    }
    private int highestGlyphNameIndex() {
        int high = 0;
        for (int i = 0; i < numGlyphs; i++) {
            if (high < glyphNameIndex[i]) {
                high = glyphNameIndex[i];
            }
        }
        return high;
    }
    public String getGlyphName(int i) {
        if (version == 0x00020000) {
            return (glyphNameIndex[i] > 257)
                ? psGlyphName[glyphNameIndex[i] - 258]
                : macGlyphName[glyphNameIndex[i]];
        } else {
            return null;
        }
    }
    public int getType() {
        return post;
    }
}

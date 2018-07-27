package org.apache.batik.ext.awt.image.codec.png;
import org.apache.batik.ext.awt.image.codec.util.ImageEncodeParam;
import org.apache.batik.ext.awt.image.codec.util.PropertyUtil;
import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.RenderedImage;
import java.awt.image.SampleModel;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;
public abstract class PNGEncodeParam implements ImageEncodeParam {
    public static final int INTENT_PERCEPTUAL = 0;
    public static final int INTENT_RELATIVE = 1;
    public static final int INTENT_SATURATION = 2;
    public static final int INTENT_ABSOLUTE = 3;
    public static final int PNG_FILTER_NONE = 0;
    public static final int PNG_FILTER_SUB = 1;
    public static final int PNG_FILTER_UP = 2;
    public static final int PNG_FILTER_AVERAGE = 3;
    public static final int PNG_FILTER_PAETH = 4;
    public static PNGEncodeParam getDefaultEncodeParam(RenderedImage im) {
        ColorModel colorModel = im.getColorModel();
        if (colorModel instanceof IndexColorModel) {
            return new PNGEncodeParam.Palette();
        }
        SampleModel sampleModel = im.getSampleModel();
        int numBands = sampleModel.getNumBands();
        if (numBands == 1 || numBands == 2) {
            return new PNGEncodeParam.Gray();
        } else {
            return new PNGEncodeParam.RGB();
        }
    }
    public static class Palette extends PNGEncodeParam {
        public Palette() {}
        private boolean backgroundSet = false;
        public void unsetBackground() {
            backgroundSet = false;
        }
        public boolean isBackgroundSet() {
            return backgroundSet;
        }
        public void setBitDepth(int bitDepth) {
            if (bitDepth != 1 && bitDepth != 2 && bitDepth != 4 &&
                bitDepth != 8) {
                throw new IllegalArgumentException(PropertyUtil.getString("PNGEncodeParam2"));
            }
            this.bitDepth = bitDepth;
            bitDepthSet = true;
        }
        private int[] palette = null;
        private boolean paletteSet = false;
        public void setPalette(int[] rgb) {
            if (rgb.length < 1*3 || rgb.length > 256*3) {
                throw new
                  IllegalArgumentException(PropertyUtil.getString("PNGEncodeParam0"));
            }
            if ((rgb.length % 3) != 0) {
                throw new
                   IllegalArgumentException(PropertyUtil.getString("PNGEncodeParam1"));
            }
            palette = (int[])(rgb.clone());
            paletteSet = true;
        }
        public int[] getPalette() {
            if (!paletteSet) {
                throw new IllegalStateException(PropertyUtil.getString("PNGEncodeParam3"));
            }
            return (int[])(palette.clone());
        }
        public void unsetPalette() {
            palette = null;
            paletteSet = false;
        }
        public boolean isPaletteSet() {
            return paletteSet;
        }
        private int backgroundPaletteIndex;
        public void setBackgroundPaletteIndex(int index) {
            backgroundPaletteIndex = index;
            backgroundSet = true;
        }
        public int getBackgroundPaletteIndex() {
            if (!backgroundSet) {
                throw new IllegalStateException(PropertyUtil.getString("PNGEncodeParam4"));
            }
            return backgroundPaletteIndex;
        }
        private int[] transparency;
        public void setPaletteTransparency(byte[] alpha) {
            transparency = new int[alpha.length];
            for (int i = 0; i < alpha.length; i++) {
                transparency[i] = alpha[i] & 0xff;
            }
            transparencySet = true;
        }
        public byte[] getPaletteTransparency() {
            if (!transparencySet) {
                throw new IllegalStateException(PropertyUtil.getString("PNGEncodeParam5"));
            }
            byte[] alpha = new byte[transparency.length];
            for (int i = 0; i < alpha.length; i++) {
                alpha[i] = (byte)transparency[i];
            }
            return alpha;
        }
    }
    public static class Gray extends PNGEncodeParam {
        public Gray() {}
        private boolean backgroundSet = false;
        public void unsetBackground() {
            backgroundSet = false;
        }
        public boolean isBackgroundSet() {
            return backgroundSet;
        }
        public void setBitDepth(int bitDepth) {
            if (bitDepth != 1 && bitDepth != 2 && bitDepth != 4 &&
                bitDepth != 8 && bitDepth != 16) {
                throw new IllegalArgumentException();
            }
            this.bitDepth = bitDepth;
            bitDepthSet = true;
        }
        private int backgroundPaletteGray;
        public void setBackgroundGray(int gray) {
            backgroundPaletteGray = gray;
            backgroundSet = true;
        }
        public int getBackgroundGray() {
            if (!backgroundSet) {
                throw new IllegalStateException(PropertyUtil.getString("PNGEncodeParam6"));
            }
            return backgroundPaletteGray;
        }
        private int[] transparency;
        public void setTransparentGray(int transparentGray) {
            transparency = new int[1];
            transparency[0] = transparentGray;
            transparencySet = true;
        }
        public int getTransparentGray() {
            if (!transparencySet) {
                throw new IllegalStateException(PropertyUtil.getString("PNGEncodeParam7"));
            }
            int gray = transparency[0];
            return gray;
        }
        private int bitShift;
        private boolean bitShiftSet = false;
        public void setBitShift(int bitShift) {
            if (bitShift < 0) {
                throw new RuntimeException();
            }
            this.bitShift = bitShift;
            bitShiftSet = true;
        }
        public int getBitShift() {
            if (!bitShiftSet) {
                throw new IllegalStateException(PropertyUtil.getString("PNGEncodeParam8"));
            }
            return bitShift;
        }
        public void unsetBitShift() {
            bitShiftSet = false;
        }
        public boolean isBitShiftSet() {
            return bitShiftSet;
        }
        public boolean isBitDepthSet() {
            return bitDepthSet;
        }
    }
    public static class RGB extends PNGEncodeParam {
        public RGB() {}
        private boolean backgroundSet = false;
        public void unsetBackground() {
            backgroundSet = false;
        }
        public boolean isBackgroundSet() {
            return backgroundSet;
        }
        public void setBitDepth(int bitDepth) {
            if (bitDepth != 8 && bitDepth != 16) {
                throw new RuntimeException();
            }
            this.bitDepth = bitDepth;
            bitDepthSet = true;
        }
        private int[] backgroundRGB;
        public void setBackgroundRGB(int[] rgb) {
            if (rgb.length != 3) {
                throw new RuntimeException();
            }
            backgroundRGB = rgb;
            backgroundSet = true;
        }
        public int[] getBackgroundRGB() {
            if (!backgroundSet) {
                throw new IllegalStateException(PropertyUtil.getString("PNGEncodeParam9"));
            }
            return backgroundRGB;
        }
        private int[] transparency;
        public void setTransparentRGB(int[] transparentRGB) {
            transparency = (int[])(transparentRGB.clone());
            transparencySet = true;
        }
        public int[] getTransparentRGB() {
            if (!transparencySet) {
                throw new IllegalStateException(PropertyUtil.getString("PNGEncodeParam10"));
            }
            return (int[])(transparency.clone());
        }
    }
    protected int bitDepth;
    protected boolean bitDepthSet = false;
    public abstract void setBitDepth(int bitDepth);
    public int getBitDepth() {
        if (!bitDepthSet) {
            throw new IllegalStateException(PropertyUtil.getString("PNGEncodeParam11"));
        }
        return bitDepth;
    }
    public void unsetBitDepth() {
        bitDepthSet = false;
    }
    private boolean useInterlacing = false;
    public void setInterlacing(boolean useInterlacing) {
        this.useInterlacing = useInterlacing;
    }
    public boolean getInterlacing() {
        return useInterlacing;
    }
    public void unsetBackground() {
        throw new RuntimeException(PropertyUtil.getString("PNGEncodeParam23"));
    }
    public boolean isBackgroundSet() {
        throw new RuntimeException(PropertyUtil.getString("PNGEncodeParam24"));
    }
    private float[] chromaticity = null;
    private boolean chromaticitySet = false;
    public void setChromaticity(float[] chromaticity) {
        if (chromaticity.length != 8) {
            throw new IllegalArgumentException();
        }
        this.chromaticity = (float[])(chromaticity.clone());
        chromaticitySet = true;
    }
    public void setChromaticity(float whitePointX, float whitePointY,
                                float redX, float redY,
                                float greenX, float greenY,
                                float blueX, float blueY) {
        float[] chroma = new float[8];
        chroma[0] = whitePointX;
        chroma[1] = whitePointY;
        chroma[2] = redX;
        chroma[3] = redY;
        chroma[4] = greenX;
        chroma[5] = greenY;
        chroma[6] = blueX;
        chroma[7] = blueY;
        setChromaticity(chroma);
    }
    public float[] getChromaticity() {
        if (!chromaticitySet) {
            throw new IllegalStateException(PropertyUtil.getString("PNGEncodeParam12"));
        }
        return (float[])(chromaticity.clone());
    }
    public void unsetChromaticity() {
        chromaticity = null;
        chromaticitySet = false;
    }
    public boolean isChromaticitySet() {
        return chromaticitySet;
    }
    private float gamma;
    private boolean gammaSet = false;
    public void setGamma(float gamma) {
        this.gamma = gamma;
        gammaSet = true;
    }
    public float getGamma() {
        if (!gammaSet) {
            throw new IllegalStateException(PropertyUtil.getString("PNGEncodeParam13"));
        }
        return gamma;
    }
    public void unsetGamma() {
        gammaSet = false;
    }
    public boolean isGammaSet() {
        return gammaSet;
    }
    private int[] paletteHistogram = null;
    private boolean paletteHistogramSet = false;
    public void setPaletteHistogram(int[] paletteHistogram) {
        this.paletteHistogram = (int[])(paletteHistogram.clone());
        paletteHistogramSet = true;
    }
    public int[] getPaletteHistogram() {
        if (!paletteHistogramSet) {
            throw new IllegalStateException(PropertyUtil.getString("PNGEncodeParam14"));
        }
        return paletteHistogram;
    }
    public void unsetPaletteHistogram() {
        paletteHistogram = null;
        paletteHistogramSet = false;
    }
    public boolean isPaletteHistogramSet() {
        return paletteHistogramSet;
    }
    private byte[] ICCProfileData = null;
    private boolean ICCProfileDataSet = false;
    public void setICCProfileData(byte[] ICCProfileData) {
        this.ICCProfileData = (byte[])(ICCProfileData.clone());
        ICCProfileDataSet = true;
    }
    public byte[] getICCProfileData() {
        if (!ICCProfileDataSet) {
            throw new IllegalStateException(PropertyUtil.getString("PNGEncodeParam15"));
        }
        return (byte[])(ICCProfileData.clone());
    }
    public void unsetICCProfileData() {
        ICCProfileData = null;
        ICCProfileDataSet = false;
    }
    public boolean isICCProfileDataSet() {
        return ICCProfileDataSet;
    }
    private int[] physicalDimension = null;
    private boolean physicalDimensionSet = false;
    public void setPhysicalDimension(int[] physicalDimension) {
        this.physicalDimension = (int[])(physicalDimension.clone());
        physicalDimensionSet = true;
    }
    public void setPhysicalDimension(int xPixelsPerUnit,
                                     int yPixelsPerUnit,
                                     int unitSpecifier) {
        int[] pd = new int[3];
        pd[0] = xPixelsPerUnit;
        pd[1] = yPixelsPerUnit;
        pd[2] = unitSpecifier;
        setPhysicalDimension(pd);
    }
    public int[] getPhysicalDimension() {
        if (!physicalDimensionSet) {
            throw new IllegalStateException(PropertyUtil.getString("PNGEncodeParam16"));
        }
        return (int[])(physicalDimension.clone());
    }
    public void unsetPhysicalDimension() {
        physicalDimension = null;
        physicalDimensionSet = false;
    }
    public boolean isPhysicalDimensionSet() {
        return physicalDimensionSet;
    }
    private PNGSuggestedPaletteEntry[] suggestedPalette = null;
    private boolean suggestedPaletteSet = false;
    public void setSuggestedPalette(PNGSuggestedPaletteEntry[] palette) {
        suggestedPalette = (PNGSuggestedPaletteEntry[])(palette.clone());
        suggestedPaletteSet = true;
    }
    public PNGSuggestedPaletteEntry[] getSuggestedPalette() {
        if (!suggestedPaletteSet) {
            throw new IllegalStateException(PropertyUtil.getString("PNGEncodeParam17"));
        }
        return (PNGSuggestedPaletteEntry[])(suggestedPalette.clone());
    }
    public void unsetSuggestedPalette() {
        suggestedPalette = null;
        suggestedPaletteSet = false;
    }
    public boolean isSuggestedPaletteSet() {
        return suggestedPaletteSet;
    }
    private int[] significantBits = null;
    private boolean significantBitsSet = false;
    public void setSignificantBits(int[] significantBits) {
        this.significantBits = (int[])(significantBits.clone());
        significantBitsSet = true;
    }
    public int[] getSignificantBits() {
        if (!significantBitsSet) {
            throw new IllegalStateException(PropertyUtil.getString("PNGEncodeParam18"));
        }
        return (int[])significantBits.clone();
    }
    public void unsetSignificantBits() {
        significantBits = null;
        significantBitsSet = false;
    }
    public boolean isSignificantBitsSet() {
        return significantBitsSet;
    }
    private int SRGBIntent;
    private boolean SRGBIntentSet = false;
    public void setSRGBIntent(int SRGBIntent) {
        this.SRGBIntent = SRGBIntent;
        SRGBIntentSet = true;
    }
    public int getSRGBIntent() {
        if (!SRGBIntentSet) {
            throw new IllegalStateException(PropertyUtil.getString("PNGEncodeParam19"));
        }
        return SRGBIntent;
    }
    public void unsetSRGBIntent() {
        SRGBIntentSet = false;
    }
    public boolean isSRGBIntentSet() {
        return SRGBIntentSet;
    }
    private String[] text = null;
    private boolean textSet = false;
    public void setText(String[] text) {
        this.text = text;
        textSet = true;
    }
    public String[] getText() {
        if (!textSet) {
            throw new IllegalStateException(PropertyUtil.getString("PNGEncodeParam20"));
        }
        return text;
    }
    public void unsetText() {
        text = null;
        textSet = false;
    }
    public boolean isTextSet() {
        return textSet;
    }
    private Date modificationTime;
    private boolean modificationTimeSet = false;
    public void setModificationTime(Date modificationTime) {
        this.modificationTime = modificationTime;
        modificationTimeSet = true;
    }
    public Date getModificationTime() {
        if (!modificationTimeSet) {
            throw new IllegalStateException(PropertyUtil.getString("PNGEncodeParam21"));
        }
        return modificationTime;
    }
    public void unsetModificationTime() {
        modificationTime = null;
        modificationTimeSet = false;
    }
    public boolean isModificationTimeSet() {
        return modificationTimeSet;
    }
    boolean transparencySet = false;
    public void unsetTransparency() {
        transparencySet = false;
    }
    public boolean isTransparencySet() {
        return transparencySet;
    }
    private String[] zText = null;
    private boolean zTextSet = false;
    public void setCompressedText(String[] text) {
        this.zText = text;
        zTextSet = true;
    }
    public String[] getCompressedText() {
        if (!zTextSet) {
            throw new IllegalStateException(PropertyUtil.getString("PNGEncodeParam22"));
        }
        return zText;
    }
    public void unsetCompressedText() {
        zText = null;
        zTextSet = false;
    }
    public boolean isCompressedTextSet() {
        return zTextSet;
    }
    List chunkType = new ArrayList();
    List chunkData = new ArrayList();
    public synchronized void addPrivateChunk(String type, byte[] data) {
        chunkType.add(type);
        chunkData.add(data.clone());
    }
    public synchronized int getNumPrivateChunks() {
        return chunkType.size();
    }
    public synchronized String getPrivateChunkType(int index) {
        return (String)chunkType.get(index);
    }
    public synchronized byte[] getPrivateChunkData(int index) {
        return (byte[])chunkData.get(index);
    }
    public synchronized void removeUnsafeToCopyPrivateChunks() {
        List newChunkType = new ArrayList();
        List newChunkData = new ArrayList();
        int len = getNumPrivateChunks();
        for (int i = 0; i < len; i++) {
            String type = getPrivateChunkType(i);
            char lastChar = type.charAt(3);
            if (lastChar >= 'a' && lastChar <= 'z') {
                newChunkType.add(type);
                newChunkData.add(getPrivateChunkData(i));
            }
        }
        chunkType = newChunkType;
        chunkData = newChunkData;
    }
    public synchronized void removeAllPrivateChunks() {
        chunkType = new ArrayList();
        chunkData = new ArrayList();
    }
    private static final int abs(int x) {
        return (x < 0) ? -x : x;
    }
    public static final int paethPredictor(int a, int b, int c) {
        int p = a + b - c;
        int pa = abs(p - a);
        int pb = abs(p - b);
        int pc = abs(p - c);
        if ((pa <= pb) && (pa <= pc)) {
            return a;
        } else if (pb <= pc) {
            return b;
        } else {
            return c;
        }
    }
    public int filterRow(byte[] currRow,
                         byte[] prevRow,
                         byte[][] scratchRows,
                         int bytesPerRow,
                         int bytesPerPixel) {
        int [] badness = {0, 0, 0, 0, 0};
        int curr, left, up, upleft, diff;
        int pa, pb, pc;
        for (int i = bytesPerPixel; i < bytesPerRow + bytesPerPixel; i++) {
            curr   = currRow[i] & 0xff;
            left   = currRow[i - bytesPerPixel] & 0xff;
            up     = prevRow[i] & 0xff;
            upleft = prevRow[i - bytesPerPixel] & 0xff;
            badness[0] += curr;
            diff = curr - left;
            scratchRows[1][i]  = (byte)diff;
            badness    [1]    +=   (diff>0)?diff:-diff;
            diff = curr - up;
            scratchRows[2][i]  = (byte)diff;
            badness    [2]    +=   (diff>=0)?diff:-diff;
            diff = curr - ((left+up)>>1);
            scratchRows[3][i]  = (byte)diff;
            badness    [3]    +=   (diff>=0)?diff:-diff;
            pa = up  -upleft;
            pb = left-upleft;
            if (pa<0) {
              if (pb<0) {
                if (pa >= pb) 
                  diff = curr-left;
                else
                  diff = curr-up;
              } else {
                pc = pa+pb;
                pa=-pa;
                if (pa <= pb)     
                  if (pa <= pc)
                    diff = curr-left;
                  else
                    diff = curr-upleft;
                else
                  if (pb <= -pc)
                    diff = curr-up;
                  else
                    diff = curr-upleft;
              }
            } else {
              if (pb<0) {
                pb =-pb; 
                if (pa <= pb) {
                  pc = pb-pa;
                  if (pa <= pc)
                    diff = curr-left;
                  else if (pb == pc)
                    diff = curr-up;
                  else
                    diff = curr-upleft;
                } else {
                  pc = pa-pb;
                  if (pb <= pc)
                    diff = curr-up;
                  else
                    diff = curr-upleft;
                }
              } else {
                if (pa <= pb)
                  diff = curr-left;
                else
                  diff = curr-up;
              }
            }
            scratchRows[4][i]  = (byte)diff;
            badness    [4]    +=   (diff>=0)?diff:-diff;
        }
        int filterType = 0;
        int minBadness = badness[0];
        for (int i = 1; i < 5; i++) {
            if (badness[i] < minBadness) {
                minBadness = badness[i];
                filterType = i;
            }
        }
        if (filterType == 0) {
            System.arraycopy(currRow, bytesPerPixel,
                             scratchRows[0], bytesPerPixel,
                             bytesPerRow);
        }
        return filterType;
    }
}

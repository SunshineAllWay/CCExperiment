package org.apache.lucene.search.highlight;
public class GradientFormatter implements Formatter
{
    private float maxScore;
    int fgRMin, fgGMin, fgBMin;
    int fgRMax, fgGMax, fgBMax;
    protected boolean highlightForeground;
    int bgRMin, bgGMin, bgBMin;
    int bgRMax, bgGMax, bgBMax;
    protected boolean highlightBackground;
    public GradientFormatter(float maxScore, String minForegroundColor,
            String maxForegroundColor, String minBackgroundColor,
            String maxBackgroundColor)
    {
        highlightForeground = (minForegroundColor != null)
                && (maxForegroundColor != null);
        if (highlightForeground)
        {
            if (minForegroundColor.length() != 7)
            {
                throw new IllegalArgumentException(
                        "minForegroundColor is not 7 bytes long eg a hex "
                                + "RGB value such as #FFFFFF");
            }
            if (maxForegroundColor.length() != 7)
            {
                throw new IllegalArgumentException(
                        "minForegroundColor is not 7 bytes long eg a hex "
                                + "RGB value such as #FFFFFF");
            }
            fgRMin = hexToInt(minForegroundColor.substring(1, 3));
            fgGMin = hexToInt(minForegroundColor.substring(3, 5));
            fgBMin = hexToInt(minForegroundColor.substring(5, 7));
            fgRMax = hexToInt(maxForegroundColor.substring(1, 3));
            fgGMax = hexToInt(maxForegroundColor.substring(3, 5));
            fgBMax = hexToInt(maxForegroundColor.substring(5, 7));
        }
        highlightBackground = (minBackgroundColor != null)
                && (maxBackgroundColor != null);
        if (highlightBackground)
        {
            if (minBackgroundColor.length() != 7)
            {
                throw new IllegalArgumentException(
                        "minBackgroundColor is not 7 bytes long eg a hex "
                                + "RGB value such as #FFFFFF");
            }
            if (maxBackgroundColor.length() != 7)
            {
                throw new IllegalArgumentException(
                        "minBackgroundColor is not 7 bytes long eg a hex "
                                + "RGB value such as #FFFFFF");
            }
            bgRMin = hexToInt(minBackgroundColor.substring(1, 3));
            bgGMin = hexToInt(minBackgroundColor.substring(3, 5));
            bgBMin = hexToInt(minBackgroundColor.substring(5, 7));
            bgRMax = hexToInt(maxBackgroundColor.substring(1, 3));
            bgGMax = hexToInt(maxBackgroundColor.substring(3, 5));
            bgBMax = hexToInt(maxBackgroundColor.substring(5, 7));
        }
        this.maxScore = maxScore;
    }
    public String highlightTerm(String originalText, TokenGroup tokenGroup)
    {
        if (tokenGroup.getTotalScore() == 0)
            return originalText;
        float score = tokenGroup.getTotalScore();
        if (score == 0)
        {
            return originalText;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("<font ");
        if (highlightForeground)
        {
            sb.append("color=\"");
            sb.append(getForegroundColorString(score));
            sb.append("\" ");
        }
        if (highlightBackground)
        {
            sb.append("bgcolor=\"");
            sb.append(getBackgroundColorString(score));
            sb.append("\" ");
        }
        sb.append(">");
        sb.append(originalText);
        sb.append("</font>");
        return sb.toString();
    }
    protected String getForegroundColorString(float score)
    {
        int rVal = getColorVal(fgRMin, fgRMax, score);
        int gVal = getColorVal(fgGMin, fgGMax, score);
        int bVal = getColorVal(fgBMin, fgBMax, score);
        StringBuilder sb = new StringBuilder();
        sb.append("#");
        sb.append(intToHex(rVal));
        sb.append(intToHex(gVal));
        sb.append(intToHex(bVal));
        return sb.toString();
    }
    protected String getBackgroundColorString(float score)
    {
        int rVal = getColorVal(bgRMin, bgRMax, score);
        int gVal = getColorVal(bgGMin, bgGMax, score);
        int bVal = getColorVal(bgBMin, bgBMax, score);
        StringBuilder sb = new StringBuilder();
        sb.append("#");
        sb.append(intToHex(rVal));
        sb.append(intToHex(gVal));
        sb.append(intToHex(bVal));
        return sb.toString();
    }
    private int getColorVal(int colorMin, int colorMax, float score)
    {
        if (colorMin == colorMax)
        {
            return colorMin;
        }
        float scale = Math.abs(colorMin - colorMax);
        float relScorePercent = Math.min(maxScore, score) / maxScore;
        float colScore = scale * relScorePercent;
        return Math.min(colorMin, colorMax) + (int) colScore;
    }
    private static char hexDigits[] = { '0', '1', '2', '3', '4', '5', '6', '7',
            '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
    private static String intToHex(int i)
    {
        return "" + hexDigits[(i & 0xF0) >> 4] + hexDigits[i & 0x0F];
    }
    public static final int hexToInt(String hex)
    {
        int len = hex.length();
        if (len > 16)
            throw new NumberFormatException();
        int l = 0;
        for (int i = 0; i < len; i++)
        {
            l <<= 4;
            int c = Character.digit(hex.charAt(i), 16);
            if (c < 0)
                throw new NumberFormatException();
            l |= c;
        }
        return l;
    }
}

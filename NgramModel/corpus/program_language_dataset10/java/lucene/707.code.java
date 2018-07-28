package org.apache.lucene.analysis.ru;
@Deprecated
class RussianStemmer
{
    private int RV,  R2;
    private final static char A = '\u0430';
    private final static char V = '\u0432';
    private final static char G = '\u0433';
    private final static char E = '\u0435';
    private final static char I = '\u0438';
    private final static char I_ = '\u0439';
    private final static char L = '\u043B';
    private final static char M = '\u043C';
    private final static char N = '\u043D';
    private final static char O = '\u043E';
    private final static char S = '\u0441';
    private final static char T = '\u0442';
    private final static char U = '\u0443';
    private final static char X = '\u0445';
    private final static char SH = '\u0448';
    private final static char SHCH = '\u0449';
    private final static char Y = '\u044B';
    private final static char SOFT = '\u044C';
    private final static char AE = '\u044D';
    private final static char IU = '\u044E';
    private final static char IA = '\u044F';
    private static char[] vowels = { A, E, I, O, U, Y, AE, IU, IA };
    private static char[][] perfectiveGerundEndings1 = {
        { V },
        { V, SH, I },
        { V, SH, I, S, SOFT }
    };
    private static char[][] perfectiveGerund1Predessors = {
        { A },
        { IA }
    };
    private static char[][] perfectiveGerundEndings2 = { { I, V }, {
        Y, V }, {
            I, V, SH, I }, {
                Y, V, SH, I }, {
                    I, V, SH, I, S, SOFT }, {
                        Y, V, SH, I, S, SOFT }
    };
    private static char[][] adjectiveEndings = {
        { E, E },
        { I, E },
        { Y, E },
        { O, E },
        { E, I_ },
        { I, I_ },
        { Y, I_ },
        { O, I_ },
        { E, M },
        { I, M },
        { Y, M },
        { O, M },
        { I, X },
        { Y, X },
        { U, IU },
        { IU, IU },
        { A, IA },
        { IA, IA },
        { O, IU },
        { E, IU },
        { I, M, I },
        { Y, M, I },
        { E, G, O },
        { O, G, O },
        { E, M, U },
        {O, M, U }
    };
    private static char[][] participleEndings1 = {
        { SHCH },
        { E, M },
        { N, N },
        { V, SH },
        { IU, SHCH }
    };
    private static char[][] participleEndings2 = {
        { I, V, SH },
        { Y, V, SH },
        { U, IU, SHCH }
    };
    private static char[][] participle1Predessors = {
        { A },
        { IA }
    };
    private static char[][] reflexiveEndings = {
        { S, IA },
        { S, SOFT }
    };
    private static char[][] verbEndings1 = {
        { I_ },
        { L },
        { N },
        { L, O },
        { N, O },
        { E, T },
        { IU, T },
        { L, A },
        { N, A },
        { L, I },
        { E, M },
        { N, Y },
        { E, T, E },
        { I_, T, E },
        { T, SOFT },
        { E, SH, SOFT },
        { N, N, O }
    };
    private static char[][] verbEndings2 = {
        { IU },
        { U, IU },
        { E, N },
        { E, I_ },
        { IA, T },
        { U, I_ },
        { I, L },
        { Y, L },
        { I, M },
        { Y, M },
        { I, T },
        { Y, T },
        { I, L, A },
        { Y, L, A },
        { E, N, A },
        { I, T, E },
        { I, L, I },
        { Y, L, I },
        { I, L, O },
        { Y, L, O },
        { E, N, O },
        { U, E, T },
        { U, IU, T },
        { E, N, Y },
        { I, T, SOFT },
        { Y, T, SOFT },
        { I, SH, SOFT },
        { E, I_, T, E },
        { U, I_, T, E }
    };
    private static char[][] verb1Predessors = {
        { A },
        { IA }
    };
    private static char[][] nounEndings = {
        { A },
        { U },
        { I_ },
        { O },
        { U },
        { E },
        { Y },
        { I },
        { SOFT },
        { IA },
        { E, V },
        { O, V },
        { I, E },
        { SOFT, E },
        { IA, X },
        { I, IU },
        { E, I },
        { I, I },
        { E, I_ },
        { O, I_ },
        { E, M },
        { A, M },
        { O, M },
        { A, X },
        { SOFT, IU },
        { I, IA },
        { SOFT, IA },
        { I, I_ },
        { IA, M },
        { IA, M, I },
        { A, M, I },
        { I, E, I_ },
        { I, IA, M },
        { I, E, M },
        { I, IA, X },
        { I, IA, M, I }
    };
    private static char[][] superlativeEndings = {
        { E, I_, SH },
        { E, I_, SH, E }
    };
    private static char[][] derivationalEndings = {
        { O, S, T },
        { O, S, T, SOFT }
    };
    public RussianStemmer()
    {
        super();
    }
    private boolean adjectival(StringBuilder stemmingZone)
    {
        if (!findAndRemoveEnding(stemmingZone, adjectiveEndings))
            return false;
        if (!findAndRemoveEnding(stemmingZone, participleEndings1, participle1Predessors))
            findAndRemoveEnding(stemmingZone, participleEndings2);
        return true;
    }
    private boolean derivational(StringBuilder stemmingZone)
    {
        int endingLength = findEnding(stemmingZone, derivationalEndings);
        if (endingLength == 0)
            return false;
        else
        {
            if (R2 - RV <= stemmingZone.length() - endingLength)
            {
                stemmingZone.setLength(stemmingZone.length() - endingLength);
                return true;
            }
            else
            {
                return false;
            }
        }
    }
    private int findEnding(StringBuilder stemmingZone, int startIndex, char[][] theEndingClass)
    {
        boolean match = false;
        for (int i = theEndingClass.length - 1; i >= 0; i--)
        {
            char[] theEnding = theEndingClass[i];
            if (startIndex < theEnding.length - 1)
            {
                match = false;
                continue;
            }
            match = true;
            int stemmingIndex = startIndex;
            for (int j = theEnding.length - 1; j >= 0; j--)
            {
                if (stemmingZone.charAt(stemmingIndex--) != theEnding[j])
                {
                    match = false;
                    break;
                }
            }
            if (match)
            {
                return theEndingClass[i].length; 
            }
        }
        return 0;
    }
    private int findEnding(StringBuilder stemmingZone, char[][] theEndingClass)
    {
        return findEnding(stemmingZone, stemmingZone.length() - 1, theEndingClass);
    }
    private boolean findAndRemoveEnding(StringBuilder stemmingZone, char[][] theEndingClass)
    {
        int endingLength = findEnding(stemmingZone, theEndingClass);
        if (endingLength == 0)
            return false;
        else {
            stemmingZone.setLength(stemmingZone.length() - endingLength);
            return true;
        }
    }
    private boolean findAndRemoveEnding(StringBuilder stemmingZone,
        char[][] theEndingClass, char[][] thePredessors)
    {
        int endingLength = findEnding(stemmingZone, theEndingClass);
        if (endingLength == 0)
            return false;
        else
        {
            int predessorLength =
                findEnding(stemmingZone,
                    stemmingZone.length() - endingLength - 1,
                    thePredessors);
            if (predessorLength == 0)
                return false;
            else {
                stemmingZone.setLength(stemmingZone.length() - endingLength);
                return true;
            }
        }
    }
    private void markPositions(String word)
    {
        RV = 0;
        R2 = 0;
        int i = 0;
        while (word.length() > i && !isVowel(word.charAt(i)))
        {
            i++;
        }
        if (word.length() - 1 < ++i)
            return; 
        RV = i;
        while (word.length() > i && isVowel(word.charAt(i)))
        {
            i++;
        }
        if (word.length() - 1 < ++i)
            return; 
        while (word.length() > i && !isVowel(word.charAt(i)))
        {
            i++;
        }
        if (word.length() - 1 < ++i)
            return; 
        while (word.length() > i && isVowel(word.charAt(i)))
        {
            i++;
        }
        if (word.length() - 1 < ++i)
            return; 
        R2 = i;
    }
    private boolean isVowel(char letter)
    {
        for (int i = 0; i < vowels.length; i++)
        {
            if (letter == vowels[i])
                return true;
        }
        return false;
    }
    private boolean noun(StringBuilder stemmingZone)
    {
        return findAndRemoveEnding(stemmingZone, nounEndings);
    }
    private boolean perfectiveGerund(StringBuilder stemmingZone)
    {
        return findAndRemoveEnding(
            stemmingZone,
            perfectiveGerundEndings1,
            perfectiveGerund1Predessors)
            || findAndRemoveEnding(stemmingZone, perfectiveGerundEndings2);
    }
    private boolean reflexive(StringBuilder stemmingZone)
    {
        return findAndRemoveEnding(stemmingZone, reflexiveEndings);
    }
    private boolean removeI(StringBuilder stemmingZone)
    {
        if (stemmingZone.length() > 0
            && stemmingZone.charAt(stemmingZone.length() - 1) == I)
        {
            stemmingZone.setLength(stemmingZone.length() - 1);
            return true;
        }
        else
        {
            return false;
        }
    }
    private boolean removeSoft(StringBuilder stemmingZone)
    {
        if (stemmingZone.length() > 0
            && stemmingZone.charAt(stemmingZone.length() - 1) == SOFT)
        {
            stemmingZone.setLength(stemmingZone.length() - 1);
            return true;
        }
        else
        {
            return false;
        }
    }
    public String stem(String input)
    {
        markPositions(input);
        if (RV == 0)
            return input; 
        StringBuilder stemmingZone = new StringBuilder(input.substring(RV));
        if (!perfectiveGerund(stemmingZone))
        {
            reflexive(stemmingZone);
            if (!adjectival(stemmingZone))
              if (!verb(stemmingZone))
                noun(stemmingZone);
        }
        removeI(stemmingZone);
        derivational(stemmingZone);
        superlative(stemmingZone);
        undoubleN(stemmingZone);
        removeSoft(stemmingZone);
        return input.substring(0, RV) + stemmingZone.toString();
    }
    private boolean superlative(StringBuilder stemmingZone)
    {
        return findAndRemoveEnding(stemmingZone, superlativeEndings);
    }
    private boolean undoubleN(StringBuilder stemmingZone)
    {
        char[][] doubleN = {
            { N, N }
        };
        if (findEnding(stemmingZone, doubleN) != 0)
        {
            stemmingZone.setLength(stemmingZone.length() - 1);
            return true;
        }
        else
        {
            return false;
        }
    }
    private boolean verb(StringBuilder stemmingZone)
    {
        return findAndRemoveEnding(
            stemmingZone,
            verbEndings1,
            verb1Predessors)
            || findAndRemoveEnding(stemmingZone, verbEndings2);
    }
    public static String stemWord(String theWord)
    {
        RussianStemmer stemmer = new RussianStemmer();
        return stemmer.stem(theWord);
    }
}

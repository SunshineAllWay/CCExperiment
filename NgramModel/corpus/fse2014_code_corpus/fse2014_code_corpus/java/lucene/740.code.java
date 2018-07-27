package org.tartarus.snowball.ext;
import org.tartarus.snowball.SnowballProgram;
import org.tartarus.snowball.Among;
public class SpanishStemmer extends SnowballProgram {
        private Among a_0[] = {
            new Among ( "", -1, 6, "", this),
            new Among ( "\u00E1", 0, 1, "", this),
            new Among ( "\u00E9", 0, 2, "", this),
            new Among ( "\u00ED", 0, 3, "", this),
            new Among ( "\u00F3", 0, 4, "", this),
            new Among ( "\u00FA", 0, 5, "", this)
        };
        private Among a_1[] = {
            new Among ( "la", -1, -1, "", this),
            new Among ( "sela", 0, -1, "", this),
            new Among ( "le", -1, -1, "", this),
            new Among ( "me", -1, -1, "", this),
            new Among ( "se", -1, -1, "", this),
            new Among ( "lo", -1, -1, "", this),
            new Among ( "selo", 5, -1, "", this),
            new Among ( "las", -1, -1, "", this),
            new Among ( "selas", 7, -1, "", this),
            new Among ( "les", -1, -1, "", this),
            new Among ( "los", -1, -1, "", this),
            new Among ( "selos", 10, -1, "", this),
            new Among ( "nos", -1, -1, "", this)
        };
        private Among a_2[] = {
            new Among ( "ando", -1, 6, "", this),
            new Among ( "iendo", -1, 6, "", this),
            new Among ( "yendo", -1, 7, "", this),
            new Among ( "\u00E1ndo", -1, 2, "", this),
            new Among ( "i\u00E9ndo", -1, 1, "", this),
            new Among ( "ar", -1, 6, "", this),
            new Among ( "er", -1, 6, "", this),
            new Among ( "ir", -1, 6, "", this),
            new Among ( "\u00E1r", -1, 3, "", this),
            new Among ( "\u00E9r", -1, 4, "", this),
            new Among ( "\u00EDr", -1, 5, "", this)
        };
        private Among a_3[] = {
            new Among ( "ic", -1, -1, "", this),
            new Among ( "ad", -1, -1, "", this),
            new Among ( "os", -1, -1, "", this),
            new Among ( "iv", -1, 1, "", this)
        };
        private Among a_4[] = {
            new Among ( "able", -1, 1, "", this),
            new Among ( "ible", -1, 1, "", this),
            new Among ( "ante", -1, 1, "", this)
        };
        private Among a_5[] = {
            new Among ( "ic", -1, 1, "", this),
            new Among ( "abil", -1, 1, "", this),
            new Among ( "iv", -1, 1, "", this)
        };
        private Among a_6[] = {
            new Among ( "ica", -1, 1, "", this),
            new Among ( "ancia", -1, 2, "", this),
            new Among ( "encia", -1, 5, "", this),
            new Among ( "adora", -1, 2, "", this),
            new Among ( "osa", -1, 1, "", this),
            new Among ( "ista", -1, 1, "", this),
            new Among ( "iva", -1, 9, "", this),
            new Among ( "anza", -1, 1, "", this),
            new Among ( "log\u00EDa", -1, 3, "", this),
            new Among ( "idad", -1, 8, "", this),
            new Among ( "able", -1, 1, "", this),
            new Among ( "ible", -1, 1, "", this),
            new Among ( "ante", -1, 2, "", this),
            new Among ( "mente", -1, 7, "", this),
            new Among ( "amente", 13, 6, "", this),
            new Among ( "aci\u00F3n", -1, 2, "", this),
            new Among ( "uci\u00F3n", -1, 4, "", this),
            new Among ( "ico", -1, 1, "", this),
            new Among ( "ismo", -1, 1, "", this),
            new Among ( "oso", -1, 1, "", this),
            new Among ( "amiento", -1, 1, "", this),
            new Among ( "imiento", -1, 1, "", this),
            new Among ( "ivo", -1, 9, "", this),
            new Among ( "ador", -1, 2, "", this),
            new Among ( "icas", -1, 1, "", this),
            new Among ( "ancias", -1, 2, "", this),
            new Among ( "encias", -1, 5, "", this),
            new Among ( "adoras", -1, 2, "", this),
            new Among ( "osas", -1, 1, "", this),
            new Among ( "istas", -1, 1, "", this),
            new Among ( "ivas", -1, 9, "", this),
            new Among ( "anzas", -1, 1, "", this),
            new Among ( "log\u00EDas", -1, 3, "", this),
            new Among ( "idades", -1, 8, "", this),
            new Among ( "ables", -1, 1, "", this),
            new Among ( "ibles", -1, 1, "", this),
            new Among ( "aciones", -1, 2, "", this),
            new Among ( "uciones", -1, 4, "", this),
            new Among ( "adores", -1, 2, "", this),
            new Among ( "antes", -1, 2, "", this),
            new Among ( "icos", -1, 1, "", this),
            new Among ( "ismos", -1, 1, "", this),
            new Among ( "osos", -1, 1, "", this),
            new Among ( "amientos", -1, 1, "", this),
            new Among ( "imientos", -1, 1, "", this),
            new Among ( "ivos", -1, 9, "", this)
        };
        private Among a_7[] = {
            new Among ( "ya", -1, 1, "", this),
            new Among ( "ye", -1, 1, "", this),
            new Among ( "yan", -1, 1, "", this),
            new Among ( "yen", -1, 1, "", this),
            new Among ( "yeron", -1, 1, "", this),
            new Among ( "yendo", -1, 1, "", this),
            new Among ( "yo", -1, 1, "", this),
            new Among ( "yas", -1, 1, "", this),
            new Among ( "yes", -1, 1, "", this),
            new Among ( "yais", -1, 1, "", this),
            new Among ( "yamos", -1, 1, "", this),
            new Among ( "y\u00F3", -1, 1, "", this)
        };
        private Among a_8[] = {
            new Among ( "aba", -1, 2, "", this),
            new Among ( "ada", -1, 2, "", this),
            new Among ( "ida", -1, 2, "", this),
            new Among ( "ara", -1, 2, "", this),
            new Among ( "iera", -1, 2, "", this),
            new Among ( "\u00EDa", -1, 2, "", this),
            new Among ( "ar\u00EDa", 5, 2, "", this),
            new Among ( "er\u00EDa", 5, 2, "", this),
            new Among ( "ir\u00EDa", 5, 2, "", this),
            new Among ( "ad", -1, 2, "", this),
            new Among ( "ed", -1, 2, "", this),
            new Among ( "id", -1, 2, "", this),
            new Among ( "ase", -1, 2, "", this),
            new Among ( "iese", -1, 2, "", this),
            new Among ( "aste", -1, 2, "", this),
            new Among ( "iste", -1, 2, "", this),
            new Among ( "an", -1, 2, "", this),
            new Among ( "aban", 16, 2, "", this),
            new Among ( "aran", 16, 2, "", this),
            new Among ( "ieran", 16, 2, "", this),
            new Among ( "\u00EDan", 16, 2, "", this),
            new Among ( "ar\u00EDan", 20, 2, "", this),
            new Among ( "er\u00EDan", 20, 2, "", this),
            new Among ( "ir\u00EDan", 20, 2, "", this),
            new Among ( "en", -1, 1, "", this),
            new Among ( "asen", 24, 2, "", this),
            new Among ( "iesen", 24, 2, "", this),
            new Among ( "aron", -1, 2, "", this),
            new Among ( "ieron", -1, 2, "", this),
            new Among ( "ar\u00E1n", -1, 2, "", this),
            new Among ( "er\u00E1n", -1, 2, "", this),
            new Among ( "ir\u00E1n", -1, 2, "", this),
            new Among ( "ado", -1, 2, "", this),
            new Among ( "ido", -1, 2, "", this),
            new Among ( "ando", -1, 2, "", this),
            new Among ( "iendo", -1, 2, "", this),
            new Among ( "ar", -1, 2, "", this),
            new Among ( "er", -1, 2, "", this),
            new Among ( "ir", -1, 2, "", this),
            new Among ( "as", -1, 2, "", this),
            new Among ( "abas", 39, 2, "", this),
            new Among ( "adas", 39, 2, "", this),
            new Among ( "idas", 39, 2, "", this),
            new Among ( "aras", 39, 2, "", this),
            new Among ( "ieras", 39, 2, "", this),
            new Among ( "\u00EDas", 39, 2, "", this),
            new Among ( "ar\u00EDas", 45, 2, "", this),
            new Among ( "er\u00EDas", 45, 2, "", this),
            new Among ( "ir\u00EDas", 45, 2, "", this),
            new Among ( "es", -1, 1, "", this),
            new Among ( "ases", 49, 2, "", this),
            new Among ( "ieses", 49, 2, "", this),
            new Among ( "abais", -1, 2, "", this),
            new Among ( "arais", -1, 2, "", this),
            new Among ( "ierais", -1, 2, "", this),
            new Among ( "\u00EDais", -1, 2, "", this),
            new Among ( "ar\u00EDais", 55, 2, "", this),
            new Among ( "er\u00EDais", 55, 2, "", this),
            new Among ( "ir\u00EDais", 55, 2, "", this),
            new Among ( "aseis", -1, 2, "", this),
            new Among ( "ieseis", -1, 2, "", this),
            new Among ( "asteis", -1, 2, "", this),
            new Among ( "isteis", -1, 2, "", this),
            new Among ( "\u00E1is", -1, 2, "", this),
            new Among ( "\u00E9is", -1, 1, "", this),
            new Among ( "ar\u00E9is", 64, 2, "", this),
            new Among ( "er\u00E9is", 64, 2, "", this),
            new Among ( "ir\u00E9is", 64, 2, "", this),
            new Among ( "ados", -1, 2, "", this),
            new Among ( "idos", -1, 2, "", this),
            new Among ( "amos", -1, 2, "", this),
            new Among ( "\u00E1bamos", 70, 2, "", this),
            new Among ( "\u00E1ramos", 70, 2, "", this),
            new Among ( "i\u00E9ramos", 70, 2, "", this),
            new Among ( "\u00EDamos", 70, 2, "", this),
            new Among ( "ar\u00EDamos", 74, 2, "", this),
            new Among ( "er\u00EDamos", 74, 2, "", this),
            new Among ( "ir\u00EDamos", 74, 2, "", this),
            new Among ( "emos", -1, 1, "", this),
            new Among ( "aremos", 78, 2, "", this),
            new Among ( "eremos", 78, 2, "", this),
            new Among ( "iremos", 78, 2, "", this),
            new Among ( "\u00E1semos", 78, 2, "", this),
            new Among ( "i\u00E9semos", 78, 2, "", this),
            new Among ( "imos", -1, 2, "", this),
            new Among ( "ar\u00E1s", -1, 2, "", this),
            new Among ( "er\u00E1s", -1, 2, "", this),
            new Among ( "ir\u00E1s", -1, 2, "", this),
            new Among ( "\u00EDs", -1, 2, "", this),
            new Among ( "ar\u00E1", -1, 2, "", this),
            new Among ( "er\u00E1", -1, 2, "", this),
            new Among ( "ir\u00E1", -1, 2, "", this),
            new Among ( "ar\u00E9", -1, 2, "", this),
            new Among ( "er\u00E9", -1, 2, "", this),
            new Among ( "ir\u00E9", -1, 2, "", this),
            new Among ( "i\u00F3", -1, 2, "", this)
        };
        private Among a_9[] = {
            new Among ( "a", -1, 1, "", this),
            new Among ( "e", -1, 2, "", this),
            new Among ( "o", -1, 1, "", this),
            new Among ( "os", -1, 1, "", this),
            new Among ( "\u00E1", -1, 1, "", this),
            new Among ( "\u00E9", -1, 2, "", this),
            new Among ( "\u00ED", -1, 1, "", this),
            new Among ( "\u00F3", -1, 1, "", this)
        };
        private static final char g_v[] = {17, 65, 16, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 17, 4, 10 };
        private int I_p2;
        private int I_p1;
        private int I_pV;
        private void copy_from(SpanishStemmer other) {
            I_p2 = other.I_p2;
            I_p1 = other.I_p1;
            I_pV = other.I_pV;
            super.copy_from(other);
        }
        private boolean r_mark_regions() {
            int v_1;
            int v_2;
            int v_3;
            int v_6;
            int v_8;
            I_pV = limit;
            I_p1 = limit;
            I_p2 = limit;
            v_1 = cursor;
            lab0: do {
                lab1: do {
                    v_2 = cursor;
                    lab2: do {
                        if (!(in_grouping(g_v, 97, 252)))
                        {
                            break lab2;
                        }
                        lab3: do {
                            v_3 = cursor;
                            lab4: do {
                                if (!(out_grouping(g_v, 97, 252)))
                                {
                                    break lab4;
                                }
                                golab5: while(true)
                                {
                                    lab6: do {
                                        if (!(in_grouping(g_v, 97, 252)))
                                        {
                                            break lab6;
                                        }
                                        break golab5;
                                    } while (false);
                                    if (cursor >= limit)
                                    {
                                        break lab4;
                                    }
                                    cursor++;
                                }
                                break lab3;
                            } while (false);
                            cursor = v_3;
                            if (!(in_grouping(g_v, 97, 252)))
                            {
                                break lab2;
                            }
                            golab7: while(true)
                            {
                                lab8: do {
                                    if (!(out_grouping(g_v, 97, 252)))
                                    {
                                        break lab8;
                                    }
                                    break golab7;
                                } while (false);
                                if (cursor >= limit)
                                {
                                    break lab2;
                                }
                                cursor++;
                            }
                        } while (false);
                        break lab1;
                    } while (false);
                    cursor = v_2;
                    if (!(out_grouping(g_v, 97, 252)))
                    {
                        break lab0;
                    }
                    lab9: do {
                        v_6 = cursor;
                        lab10: do {
                            if (!(out_grouping(g_v, 97, 252)))
                            {
                                break lab10;
                            }
                            golab11: while(true)
                            {
                                lab12: do {
                                    if (!(in_grouping(g_v, 97, 252)))
                                    {
                                        break lab12;
                                    }
                                    break golab11;
                                } while (false);
                                if (cursor >= limit)
                                {
                                    break lab10;
                                }
                                cursor++;
                            }
                            break lab9;
                        } while (false);
                        cursor = v_6;
                        if (!(in_grouping(g_v, 97, 252)))
                        {
                            break lab0;
                        }
                        if (cursor >= limit)
                        {
                            break lab0;
                        }
                        cursor++;
                    } while (false);
                } while (false);
                I_pV = cursor;
            } while (false);
            cursor = v_1;
            v_8 = cursor;
            lab13: do {
                golab14: while(true)
                {
                    lab15: do {
                        if (!(in_grouping(g_v, 97, 252)))
                        {
                            break lab15;
                        }
                        break golab14;
                    } while (false);
                    if (cursor >= limit)
                    {
                        break lab13;
                    }
                    cursor++;
                }
                golab16: while(true)
                {
                    lab17: do {
                        if (!(out_grouping(g_v, 97, 252)))
                        {
                            break lab17;
                        }
                        break golab16;
                    } while (false);
                    if (cursor >= limit)
                    {
                        break lab13;
                    }
                    cursor++;
                }
                I_p1 = cursor;
                golab18: while(true)
                {
                    lab19: do {
                        if (!(in_grouping(g_v, 97, 252)))
                        {
                            break lab19;
                        }
                        break golab18;
                    } while (false);
                    if (cursor >= limit)
                    {
                        break lab13;
                    }
                    cursor++;
                }
                golab20: while(true)
                {
                    lab21: do {
                        if (!(out_grouping(g_v, 97, 252)))
                        {
                            break lab21;
                        }
                        break golab20;
                    } while (false);
                    if (cursor >= limit)
                    {
                        break lab13;
                    }
                    cursor++;
                }
                I_p2 = cursor;
            } while (false);
            cursor = v_8;
            return true;
        }
        private boolean r_postlude() {
            int among_var;
            int v_1;
            replab0: while(true)
            {
                v_1 = cursor;
                lab1: do {
                    bra = cursor;
                    among_var = find_among(a_0, 6);
                    if (among_var == 0)
                    {
                        break lab1;
                    }
                    ket = cursor;
                    switch(among_var) {
                        case 0:
                            break lab1;
                        case 1:
                            slice_from("a");
                            break;
                        case 2:
                            slice_from("e");
                            break;
                        case 3:
                            slice_from("i");
                            break;
                        case 4:
                            slice_from("o");
                            break;
                        case 5:
                            slice_from("u");
                            break;
                        case 6:
                            if (cursor >= limit)
                            {
                                break lab1;
                            }
                            cursor++;
                            break;
                    }
                    continue replab0;
                } while (false);
                cursor = v_1;
                break replab0;
            }
            return true;
        }
        private boolean r_RV() {
            if (!(I_pV <= cursor))
            {
                return false;
            }
            return true;
        }
        private boolean r_R1() {
            if (!(I_p1 <= cursor))
            {
                return false;
            }
            return true;
        }
        private boolean r_R2() {
            if (!(I_p2 <= cursor))
            {
                return false;
            }
            return true;
        }
        private boolean r_attached_pronoun() {
            int among_var;
            ket = cursor;
            if (find_among_b(a_1, 13) == 0)
            {
                return false;
            }
            bra = cursor;
            among_var = find_among_b(a_2, 11);
            if (among_var == 0)
            {
                return false;
            }
            if (!r_RV())
            {
                return false;
            }
            switch(among_var) {
                case 0:
                    return false;
                case 1:
                    bra = cursor;
                    slice_from("iendo");
                    break;
                case 2:
                    bra = cursor;
                    slice_from("ando");
                    break;
                case 3:
                    bra = cursor;
                    slice_from("ar");
                    break;
                case 4:
                    bra = cursor;
                    slice_from("er");
                    break;
                case 5:
                    bra = cursor;
                    slice_from("ir");
                    break;
                case 6:
                    slice_del();
                    break;
                case 7:
                    if (!(eq_s_b(1, "u")))
                    {
                        return false;
                    }
                    slice_del();
                    break;
            }
            return true;
        }
        private boolean r_standard_suffix() {
            int among_var;
            int v_1;
            int v_2;
            int v_3;
            int v_4;
            int v_5;
            ket = cursor;
            among_var = find_among_b(a_6, 46);
            if (among_var == 0)
            {
                return false;
            }
            bra = cursor;
            switch(among_var) {
                case 0:
                    return false;
                case 1:
                    if (!r_R2())
                    {
                        return false;
                    }
                    slice_del();
                    break;
                case 2:
                    if (!r_R2())
                    {
                        return false;
                    }
                    slice_del();
                    v_1 = limit - cursor;
                    lab0: do {
                        ket = cursor;
                        if (!(eq_s_b(2, "ic")))
                        {
                            cursor = limit - v_1;
                            break lab0;
                        }
                        bra = cursor;
                        if (!r_R2())
                        {
                            cursor = limit - v_1;
                            break lab0;
                        }
                        slice_del();
                    } while (false);
                    break;
                case 3:
                    if (!r_R2())
                    {
                        return false;
                    }
                    slice_from("log");
                    break;
                case 4:
                    if (!r_R2())
                    {
                        return false;
                    }
                    slice_from("u");
                    break;
                case 5:
                    if (!r_R2())
                    {
                        return false;
                    }
                    slice_from("ente");
                    break;
                case 6:
                    if (!r_R1())
                    {
                        return false;
                    }
                    slice_del();
                    v_2 = limit - cursor;
                    lab1: do {
                        ket = cursor;
                        among_var = find_among_b(a_3, 4);
                        if (among_var == 0)
                        {
                            cursor = limit - v_2;
                            break lab1;
                        }
                        bra = cursor;
                        if (!r_R2())
                        {
                            cursor = limit - v_2;
                            break lab1;
                        }
                        slice_del();
                        switch(among_var) {
                            case 0:
                                cursor = limit - v_2;
                                break lab1;
                            case 1:
                                ket = cursor;
                                if (!(eq_s_b(2, "at")))
                                {
                                    cursor = limit - v_2;
                                    break lab1;
                                }
                                bra = cursor;
                                if (!r_R2())
                                {
                                    cursor = limit - v_2;
                                    break lab1;
                                }
                                slice_del();
                                break;
                        }
                    } while (false);
                    break;
                case 7:
                    if (!r_R2())
                    {
                        return false;
                    }
                    slice_del();
                    v_3 = limit - cursor;
                    lab2: do {
                        ket = cursor;
                        among_var = find_among_b(a_4, 3);
                        if (among_var == 0)
                        {
                            cursor = limit - v_3;
                            break lab2;
                        }
                        bra = cursor;
                        switch(among_var) {
                            case 0:
                                cursor = limit - v_3;
                                break lab2;
                            case 1:
                                if (!r_R2())
                                {
                                    cursor = limit - v_3;
                                    break lab2;
                                }
                                slice_del();
                                break;
                        }
                    } while (false);
                    break;
                case 8:
                    if (!r_R2())
                    {
                        return false;
                    }
                    slice_del();
                    v_4 = limit - cursor;
                    lab3: do {
                        ket = cursor;
                        among_var = find_among_b(a_5, 3);
                        if (among_var == 0)
                        {
                            cursor = limit - v_4;
                            break lab3;
                        }
                        bra = cursor;
                        switch(among_var) {
                            case 0:
                                cursor = limit - v_4;
                                break lab3;
                            case 1:
                                if (!r_R2())
                                {
                                    cursor = limit - v_4;
                                    break lab3;
                                }
                                slice_del();
                                break;
                        }
                    } while (false);
                    break;
                case 9:
                    if (!r_R2())
                    {
                        return false;
                    }
                    slice_del();
                    v_5 = limit - cursor;
                    lab4: do {
                        ket = cursor;
                        if (!(eq_s_b(2, "at")))
                        {
                            cursor = limit - v_5;
                            break lab4;
                        }
                        bra = cursor;
                        if (!r_R2())
                        {
                            cursor = limit - v_5;
                            break lab4;
                        }
                        slice_del();
                    } while (false);
                    break;
            }
            return true;
        }
        private boolean r_y_verb_suffix() {
            int among_var;
            int v_1;
            int v_2;
            v_1 = limit - cursor;
            if (cursor < I_pV)
            {
                return false;
            }
            cursor = I_pV;
            v_2 = limit_backward;
            limit_backward = cursor;
            cursor = limit - v_1;
            ket = cursor;
            among_var = find_among_b(a_7, 12);
            if (among_var == 0)
            {
                limit_backward = v_2;
                return false;
            }
            bra = cursor;
            limit_backward = v_2;
            switch(among_var) {
                case 0:
                    return false;
                case 1:
                    if (!(eq_s_b(1, "u")))
                    {
                        return false;
                    }
                    slice_del();
                    break;
            }
            return true;
        }
        private boolean r_verb_suffix() {
            int among_var;
            int v_1;
            int v_2;
            int v_3;
            int v_4;
            v_1 = limit - cursor;
            if (cursor < I_pV)
            {
                return false;
            }
            cursor = I_pV;
            v_2 = limit_backward;
            limit_backward = cursor;
            cursor = limit - v_1;
            ket = cursor;
            among_var = find_among_b(a_8, 96);
            if (among_var == 0)
            {
                limit_backward = v_2;
                return false;
            }
            bra = cursor;
            limit_backward = v_2;
            switch(among_var) {
                case 0:
                    return false;
                case 1:
                    v_3 = limit - cursor;
                    lab0: do {
                        if (!(eq_s_b(1, "u")))
                        {
                            cursor = limit - v_3;
                            break lab0;
                        }
                        v_4 = limit - cursor;
                        if (!(eq_s_b(1, "g")))
                        {
                            cursor = limit - v_3;
                            break lab0;
                        }
                        cursor = limit - v_4;
                    } while (false);
                    bra = cursor;
                    slice_del();
                    break;
                case 2:
                    slice_del();
                    break;
            }
            return true;
        }
        private boolean r_residual_suffix() {
            int among_var;
            int v_1;
            int v_2;
            ket = cursor;
            among_var = find_among_b(a_9, 8);
            if (among_var == 0)
            {
                return false;
            }
            bra = cursor;
            switch(among_var) {
                case 0:
                    return false;
                case 1:
                    if (!r_RV())
                    {
                        return false;
                    }
                    slice_del();
                    break;
                case 2:
                    if (!r_RV())
                    {
                        return false;
                    }
                    slice_del();
                    v_1 = limit - cursor;
                    lab0: do {
                        ket = cursor;
                        if (!(eq_s_b(1, "u")))
                        {
                            cursor = limit - v_1;
                            break lab0;
                        }
                        bra = cursor;
                        v_2 = limit - cursor;
                        if (!(eq_s_b(1, "g")))
                        {
                            cursor = limit - v_1;
                            break lab0;
                        }
                        cursor = limit - v_2;
                        if (!r_RV())
                        {
                            cursor = limit - v_1;
                            break lab0;
                        }
                        slice_del();
                    } while (false);
                    break;
            }
            return true;
        }
        public boolean stem() {
            int v_1;
            int v_2;
            int v_3;
            int v_4;
            int v_5;
            int v_6;
            v_1 = cursor;
            lab0: do {
                if (!r_mark_regions())
                {
                    break lab0;
                }
            } while (false);
            cursor = v_1;
            limit_backward = cursor; cursor = limit;
            v_2 = limit - cursor;
            lab1: do {
                if (!r_attached_pronoun())
                {
                    break lab1;
                }
            } while (false);
            cursor = limit - v_2;
            v_3 = limit - cursor;
            lab2: do {
                lab3: do {
                    v_4 = limit - cursor;
                    lab4: do {
                        if (!r_standard_suffix())
                        {
                            break lab4;
                        }
                        break lab3;
                    } while (false);
                    cursor = limit - v_4;
                    lab5: do {
                        if (!r_y_verb_suffix())
                        {
                            break lab5;
                        }
                        break lab3;
                    } while (false);
                    cursor = limit - v_4;
                    if (!r_verb_suffix())
                    {
                        break lab2;
                    }
                } while (false);
            } while (false);
            cursor = limit - v_3;
            v_5 = limit - cursor;
            lab6: do {
                if (!r_residual_suffix())
                {
                    break lab6;
                }
            } while (false);
            cursor = limit - v_5;
            cursor = limit_backward;            
            v_6 = cursor;
            lab7: do {
                if (!r_postlude())
                {
                    break lab7;
                }
            } while (false);
            cursor = v_6;
            return true;
        }
}

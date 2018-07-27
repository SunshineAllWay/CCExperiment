package org.tartarus.snowball.ext;
import org.tartarus.snowball.SnowballProgram;
import org.tartarus.snowball.Among;
public class KpStemmer extends SnowballProgram {
        private Among a_0[] = {
            new Among ( "nde", -1, 7, "", this),
            new Among ( "en", -1, 6, "", this),
            new Among ( "s", -1, 2, "", this),
            new Among ( "'s", 2, 1, "", this),
            new Among ( "es", 2, 4, "", this),
            new Among ( "ies", 4, 3, "", this),
            new Among ( "aus", 2, 5, "", this)
        };
        private Among a_1[] = {
            new Among ( "de", -1, 5, "", this),
            new Among ( "ge", -1, 2, "", this),
            new Among ( "ische", -1, 4, "", this),
            new Among ( "je", -1, 1, "", this),
            new Among ( "lijke", -1, 3, "", this),
            new Among ( "le", -1, 9, "", this),
            new Among ( "ene", -1, 10, "", this),
            new Among ( "re", -1, 8, "", this),
            new Among ( "se", -1, 7, "", this),
            new Among ( "te", -1, 6, "", this),
            new Among ( "ieve", -1, 11, "", this)
        };
        private Among a_2[] = {
            new Among ( "heid", -1, 3, "", this),
            new Among ( "fie", -1, 7, "", this),
            new Among ( "gie", -1, 8, "", this),
            new Among ( "atie", -1, 1, "", this),
            new Among ( "isme", -1, 5, "", this),
            new Among ( "ing", -1, 5, "", this),
            new Among ( "arij", -1, 6, "", this),
            new Among ( "erij", -1, 5, "", this),
            new Among ( "sel", -1, 3, "", this),
            new Among ( "rder", -1, 4, "", this),
            new Among ( "ster", -1, 3, "", this),
            new Among ( "iteit", -1, 2, "", this),
            new Among ( "dst", -1, 10, "", this),
            new Among ( "tst", -1, 9, "", this)
        };
        private Among a_3[] = {
            new Among ( "end", -1, 10, "", this),
            new Among ( "atief", -1, 2, "", this),
            new Among ( "erig", -1, 10, "", this),
            new Among ( "achtig", -1, 9, "", this),
            new Among ( "ioneel", -1, 1, "", this),
            new Among ( "baar", -1, 3, "", this),
            new Among ( "laar", -1, 5, "", this),
            new Among ( "naar", -1, 4, "", this),
            new Among ( "raar", -1, 6, "", this),
            new Among ( "eriger", -1, 10, "", this),
            new Among ( "achtiger", -1, 9, "", this),
            new Among ( "lijker", -1, 8, "", this),
            new Among ( "tant", -1, 7, "", this),
            new Among ( "erigst", -1, 10, "", this),
            new Among ( "achtigst", -1, 9, "", this),
            new Among ( "lijkst", -1, 8, "", this)
        };
        private Among a_4[] = {
            new Among ( "ig", -1, 1, "", this),
            new Among ( "iger", -1, 1, "", this),
            new Among ( "igst", -1, 1, "", this)
        };
        private Among a_5[] = {
            new Among ( "ft", -1, 2, "", this),
            new Among ( "kt", -1, 1, "", this),
            new Among ( "pt", -1, 3, "", this)
        };
        private Among a_6[] = {
            new Among ( "bb", -1, 1, "", this),
            new Among ( "cc", -1, 2, "", this),
            new Among ( "dd", -1, 3, "", this),
            new Among ( "ff", -1, 4, "", this),
            new Among ( "gg", -1, 5, "", this),
            new Among ( "hh", -1, 6, "", this),
            new Among ( "jj", -1, 7, "", this),
            new Among ( "kk", -1, 8, "", this),
            new Among ( "ll", -1, 9, "", this),
            new Among ( "mm", -1, 10, "", this),
            new Among ( "nn", -1, 11, "", this),
            new Among ( "pp", -1, 12, "", this),
            new Among ( "qq", -1, 13, "", this),
            new Among ( "rr", -1, 14, "", this),
            new Among ( "ss", -1, 15, "", this),
            new Among ( "tt", -1, 16, "", this),
            new Among ( "v", -1, 21, "", this),
            new Among ( "vv", 16, 17, "", this),
            new Among ( "ww", -1, 18, "", this),
            new Among ( "xx", -1, 19, "", this),
            new Among ( "z", -1, 22, "", this),
            new Among ( "zz", 20, 20, "", this)
        };
        private Among a_7[] = {
            new Among ( "d", -1, 1, "", this),
            new Among ( "t", -1, 2, "", this)
        };
        private static final char g_v[] = {17, 65, 16, 1 };
        private static final char g_v_WX[] = {17, 65, 208, 1 };
        private static final char g_AOU[] = {1, 64, 16 };
        private static final char g_AIOU[] = {1, 65, 16 };
        private boolean B_GE_removed;
        private boolean B_stemmed;
        private boolean B_Y_found;
        private int I_p2;
        private int I_p1;
        private int I_x;
        private StringBuilder S_ch = new StringBuilder();
        private void copy_from(KpStemmer other) {
            B_GE_removed = other.B_GE_removed;
            B_stemmed = other.B_stemmed;
            B_Y_found = other.B_Y_found;
            I_p2 = other.I_p2;
            I_p1 = other.I_p1;
            I_x = other.I_x;
            S_ch = other.S_ch;
            super.copy_from(other);
        }
        private boolean r_R1() {
            I_x = cursor;
            if (!(I_x >= I_p1))
            {
                return false;
            }
            return true;
        }
        private boolean r_R2() {
            I_x = cursor;
            if (!(I_x >= I_p2))
            {
                return false;
            }
            return true;
        }
        private boolean r_V() {
            int v_1;
            int v_2;
            v_1 = limit - cursor;
            lab0: do {
                v_2 = limit - cursor;
                lab1: do {
                    if (!(in_grouping_b(g_v, 97, 121)))
                    {
                        break lab1;
                    }
                    break lab0;
                } while (false);
                cursor = limit - v_2;
                if (!(eq_s_b(2, "ij")))
                {
                    return false;
                }
            } while (false);
            cursor = limit - v_1;
            return true;
        }
        private boolean r_VX() {
            int v_1;
            int v_2;
            v_1 = limit - cursor;
            if (cursor <= limit_backward)
            {
                return false;
            }
            cursor--;
            lab0: do {
                v_2 = limit - cursor;
                lab1: do {
                    if (!(in_grouping_b(g_v, 97, 121)))
                    {
                        break lab1;
                    }
                    break lab0;
                } while (false);
                cursor = limit - v_2;
                if (!(eq_s_b(2, "ij")))
                {
                    return false;
                }
            } while (false);
            cursor = limit - v_1;
            return true;
        }
        private boolean r_C() {
            int v_1;
            int v_2;
            v_1 = limit - cursor;
            {
                v_2 = limit - cursor;
                lab0: do {
                    if (!(eq_s_b(2, "ij")))
                    {
                        break lab0;
                    }
                    return false;
                } while (false);
                cursor = limit - v_2;
            }
            if (!(out_grouping_b(g_v, 97, 121)))
            {
                return false;
            }
            cursor = limit - v_1;
            return true;
        }
        private boolean r_lengthen_V() {
            int v_1;
            int v_2;
            int v_3;
            int v_4;
            int v_5;
            int v_6;
            int v_7;
            int v_8;
            v_1 = limit - cursor;
            lab0: do {
                if (!(out_grouping_b(g_v_WX, 97, 121)))
                {
                    break lab0;
                }
                ket = cursor;
                lab1: do {
                    v_2 = limit - cursor;
                    lab2: do {
                        if (!(in_grouping_b(g_AOU, 97, 117)))
                        {
                            break lab2;
                        }
                        bra = cursor;
                        v_3 = limit - cursor;
                        lab3: do {
                            v_4 = limit - cursor;
                            lab4: do {
                                if (!(out_grouping_b(g_v, 97, 121)))
                                {
                                    break lab4;
                                }
                                break lab3;
                            } while (false);
                            cursor = limit - v_4;
                            if (cursor > limit_backward)
                            {
                                break lab2;
                            }
                        } while (false);
                        cursor = limit - v_3;
                        break lab1;
                    } while (false);
                    cursor = limit - v_2;
                    if (!(eq_s_b(1, "e")))
                    {
                        break lab0;
                    }
                    bra = cursor;
                    v_5 = limit - cursor;
                    lab5: do {
                        v_6 = limit - cursor;
                        lab6: do {
                            if (!(out_grouping_b(g_v, 97, 121)))
                            {
                                break lab6;
                            }
                            break lab5;
                        } while (false);
                        cursor = limit - v_6;
                        if (cursor > limit_backward)
                        {
                            break lab0;
                        }
                    } while (false);
                    {
                        v_7 = limit - cursor;
                        lab7: do {
                            if (!(in_grouping_b(g_AIOU, 97, 117)))
                            {
                                break lab7;
                            }
                            break lab0;
                        } while (false);
                        cursor = limit - v_7;
                    }
                    {
                        v_8 = limit - cursor;
                        lab8: do {
                            if (cursor <= limit_backward)
                            {
                                break lab8;
                            }
                            cursor--;
                            if (!(in_grouping_b(g_AIOU, 97, 117)))
                            {
                                break lab8;
                            }
                            if (!(out_grouping_b(g_v, 97, 121)))
                            {
                                break lab8;
                            }
                            break lab0;
                        } while (false);
                        cursor = limit - v_8;
                    }
                    cursor = limit - v_5;
                } while (false);
                S_ch = slice_to(S_ch);
                {
                    int c = cursor;
                    insert(cursor, cursor, S_ch);
                    cursor = c;
                }
            } while (false);
            cursor = limit - v_1;
            return true;
        }
        private boolean r_Step_1() {
            int among_var;
            int v_1;
            int v_2;
            int v_3;
            int v_4;
            ket = cursor;
            among_var = find_among_b(a_0, 7);
            if (among_var == 0)
            {
                return false;
            }
            bra = cursor;
            switch(among_var) {
                case 0:
                    return false;
                case 1:
                    slice_del();
                    break;
                case 2:
                    if (!r_R1())
                    {
                        return false;
                    }
                    {
                        v_1 = limit - cursor;
                        lab0: do {
                            if (!(eq_s_b(1, "t")))
                            {
                                break lab0;
                            }
                            if (!r_R1())
                            {
                                break lab0;
                            }
                            return false;
                        } while (false);
                        cursor = limit - v_1;
                    }
                    if (!r_C())
                    {
                        return false;
                    }
                    slice_del();
                    break;
                case 3:
                    if (!r_R1())
                    {
                        return false;
                    }
                    slice_from("ie");
                    break;
                case 4:
                    lab1: do {
                        v_2 = limit - cursor;
                        lab2: do {
                            if (!(eq_s_b(2, "ar")))
                            {
                                break lab2;
                            }
                            if (!r_R1())
                            {
                                break lab2;
                            }
                            if (!r_C())
                            {
                                break lab2;
                            }
                            bra = cursor;
                            slice_del();
                            if (!r_lengthen_V())
                            {
                                break lab2;
                            }
                            break lab1;
                        } while (false);
                        cursor = limit - v_2;
                        lab3: do {
                            if (!(eq_s_b(2, "er")))
                            {
                                break lab3;
                            }
                            if (!r_R1())
                            {
                                break lab3;
                            }
                            if (!r_C())
                            {
                                break lab3;
                            }
                            bra = cursor;
                            slice_del();
                            break lab1;
                        } while (false);
                        cursor = limit - v_2;
                        if (!r_R1())
                        {
                            return false;
                        }
                        if (!r_C())
                        {
                            return false;
                        }
                        slice_from("e");
                    } while (false);
                    break;
                case 5:
                    if (!r_R1())
                    {
                        return false;
                    }
                    if (!r_V())
                    {
                        return false;
                    }
                    slice_from("au");
                    break;
                case 6:
                    lab4: do {
                        v_3 = limit - cursor;
                        lab5: do {
                            if (!(eq_s_b(3, "hed")))
                            {
                                break lab5;
                            }
                            if (!r_R1())
                            {
                                break lab5;
                            }
                            bra = cursor;
                            slice_from("heid");
                            break lab4;
                        } while (false);
                        cursor = limit - v_3;
                        lab6: do {
                            if (!(eq_s_b(2, "nd")))
                            {
                                break lab6;
                            }
                            slice_del();
                            break lab4;
                        } while (false);
                        cursor = limit - v_3;
                        lab7: do {
                            if (!(eq_s_b(1, "d")))
                            {
                                break lab7;
                            }
                            if (!r_R1())
                            {
                                break lab7;
                            }
                            if (!r_C())
                            {
                                break lab7;
                            }
                            bra = cursor;
                            slice_del();
                            break lab4;
                        } while (false);
                        cursor = limit - v_3;
                        lab8: do {
                            lab9: do {
                                v_4 = limit - cursor;
                                lab10: do {
                                    if (!(eq_s_b(1, "i")))
                                    {
                                        break lab10;
                                    }
                                    break lab9;
                                } while (false);
                                cursor = limit - v_4;
                                if (!(eq_s_b(1, "j")))
                                {
                                    break lab8;
                                }
                            } while (false);
                            if (!r_V())
                            {
                                break lab8;
                            }
                            slice_del();
                            break lab4;
                        } while (false);
                        cursor = limit - v_3;
                        if (!r_R1())
                        {
                            return false;
                        }
                        if (!r_C())
                        {
                            return false;
                        }
                        slice_del();
                        if (!r_lengthen_V())
                        {
                            return false;
                        }
                    } while (false);
                    break;
                case 7:
                    slice_from("nd");
                    break;
            }
            return true;
        }
        private boolean r_Step_2() {
            int among_var;
            int v_1;
            ket = cursor;
            among_var = find_among_b(a_1, 11);
            if (among_var == 0)
            {
                return false;
            }
            bra = cursor;
            switch(among_var) {
                case 0:
                    return false;
                case 1:
                    lab0: do {
                        v_1 = limit - cursor;
                        lab1: do {
                            if (!(eq_s_b(2, "'t")))
                            {
                                break lab1;
                            }
                            bra = cursor;
                            slice_del();
                            break lab0;
                        } while (false);
                        cursor = limit - v_1;
                        lab2: do {
                            if (!(eq_s_b(2, "et")))
                            {
                                break lab2;
                            }
                            bra = cursor;
                            if (!r_R1())
                            {
                                break lab2;
                            }
                            if (!r_C())
                            {
                                break lab2;
                            }
                            slice_del();
                            break lab0;
                        } while (false);
                        cursor = limit - v_1;
                        lab3: do {
                            if (!(eq_s_b(3, "rnt")))
                            {
                                break lab3;
                            }
                            bra = cursor;
                            slice_from("rn");
                            break lab0;
                        } while (false);
                        cursor = limit - v_1;
                        lab4: do {
                            if (!(eq_s_b(1, "t")))
                            {
                                break lab4;
                            }
                            bra = cursor;
                            if (!r_R1())
                            {
                                break lab4;
                            }
                            if (!r_VX())
                            {
                                break lab4;
                            }
                            slice_del();
                            break lab0;
                        } while (false);
                        cursor = limit - v_1;
                        lab5: do {
                            if (!(eq_s_b(3, "ink")))
                            {
                                break lab5;
                            }
                            bra = cursor;
                            slice_from("ing");
                            break lab0;
                        } while (false);
                        cursor = limit - v_1;
                        lab6: do {
                            if (!(eq_s_b(2, "mp")))
                            {
                                break lab6;
                            }
                            bra = cursor;
                            slice_from("m");
                            break lab0;
                        } while (false);
                        cursor = limit - v_1;
                        lab7: do {
                            if (!(eq_s_b(1, "'")))
                            {
                                break lab7;
                            }
                            bra = cursor;
                            if (!r_R1())
                            {
                                break lab7;
                            }
                            slice_del();
                            break lab0;
                        } while (false);
                        cursor = limit - v_1;
                        bra = cursor;
                        if (!r_R1())
                        {
                            return false;
                        }
                        if (!r_C())
                        {
                            return false;
                        }
                        slice_del();
                    } while (false);
                    break;
                case 2:
                    if (!r_R1())
                    {
                        return false;
                    }
                    slice_from("g");
                    break;
                case 3:
                    if (!r_R1())
                    {
                        return false;
                    }
                    slice_from("lijk");
                    break;
                case 4:
                    if (!r_R1())
                    {
                        return false;
                    }
                    slice_from("isch");
                    break;
                case 5:
                    if (!r_R1())
                    {
                        return false;
                    }
                    if (!r_C())
                    {
                        return false;
                    }
                    slice_del();
                    break;
                case 6:
                    if (!r_R1())
                    {
                        return false;
                    }
                    slice_from("t");
                    break;
                case 7:
                    if (!r_R1())
                    {
                        return false;
                    }
                    slice_from("s");
                    break;
                case 8:
                    if (!r_R1())
                    {
                        return false;
                    }
                    slice_from("r");
                    break;
                case 9:
                    if (!r_R1())
                    {
                        return false;
                    }
                    slice_del();
                    insert(cursor, cursor, "l");
                    if (!r_lengthen_V())
                    {
                        return false;
                    }
                    break;
                case 10:
                    if (!r_R1())
                    {
                        return false;
                    }
                    if (!r_C())
                    {
                        return false;
                    }
                    slice_del();
                    insert(cursor, cursor, "en");
                    if (!r_lengthen_V())
                    {
                        return false;
                    }
                    break;
                case 11:
                    if (!r_R1())
                    {
                        return false;
                    }
                    if (!r_C())
                    {
                        return false;
                    }
                    slice_from("ief");
                    break;
            }
            return true;
        }
        private boolean r_Step_3() {
            int among_var;
            ket = cursor;
            among_var = find_among_b(a_2, 14);
            if (among_var == 0)
            {
                return false;
            }
            bra = cursor;
            switch(among_var) {
                case 0:
                    return false;
                case 1:
                    if (!r_R1())
                    {
                        return false;
                    }
                    slice_from("eer");
                    break;
                case 2:
                    if (!r_R1())
                    {
                        return false;
                    }
                    slice_del();
                    if (!r_lengthen_V())
                    {
                        return false;
                    }
                    break;
                case 3:
                    if (!r_R1())
                    {
                        return false;
                    }
                    slice_del();
                    break;
                case 4:
                    slice_from("r");
                    break;
                case 5:
                    if (!r_R1())
                    {
                        return false;
                    }
                    slice_del();
                    if (!r_lengthen_V())
                    {
                        return false;
                    }
                    break;
                case 6:
                    if (!r_R1())
                    {
                        return false;
                    }
                    if (!r_C())
                    {
                        return false;
                    }
                    slice_from("aar");
                    break;
                case 7:
                    if (!r_R2())
                    {
                        return false;
                    }
                    slice_del();
                    insert(cursor, cursor, "f");
                    if (!r_lengthen_V())
                    {
                        return false;
                    }
                    break;
                case 8:
                    if (!r_R2())
                    {
                        return false;
                    }
                    slice_del();
                    insert(cursor, cursor, "g");
                    if (!r_lengthen_V())
                    {
                        return false;
                    }
                    break;
                case 9:
                    if (!r_R1())
                    {
                        return false;
                    }
                    if (!r_C())
                    {
                        return false;
                    }
                    slice_from("t");
                    break;
                case 10:
                    if (!r_R1())
                    {
                        return false;
                    }
                    if (!r_C())
                    {
                        return false;
                    }
                    slice_from("d");
                    break;
            }
            return true;
        }
        private boolean r_Step_4() {
            int among_var;
            int v_1;
            lab0: do {
                v_1 = limit - cursor;
                lab1: do {
                    ket = cursor;
                    among_var = find_among_b(a_3, 16);
                    if (among_var == 0)
                    {
                        break lab1;
                    }
                    bra = cursor;
                    switch(among_var) {
                        case 0:
                            break lab1;
                        case 1:
                            if (!r_R1())
                            {
                                break lab1;
                            }
                            slice_from("ie");
                            break;
                        case 2:
                            if (!r_R1())
                            {
                                break lab1;
                            }
                            slice_from("eer");
                            break;
                        case 3:
                            if (!r_R1())
                            {
                                break lab1;
                            }
                            slice_del();
                            break;
                        case 4:
                            if (!r_R1())
                            {
                                break lab1;
                            }
                            if (!r_V())
                            {
                                break lab1;
                            }
                            slice_from("n");
                            break;
                        case 5:
                            if (!r_R1())
                            {
                                break lab1;
                            }
                            if (!r_V())
                            {
                                break lab1;
                            }
                            slice_from("l");
                            break;
                        case 6:
                            if (!r_R1())
                            {
                                break lab1;
                            }
                            if (!r_V())
                            {
                                break lab1;
                            }
                            slice_from("r");
                            break;
                        case 7:
                            if (!r_R1())
                            {
                                break lab1;
                            }
                            slice_from("teer");
                            break;
                        case 8:
                            if (!r_R1())
                            {
                                break lab1;
                            }
                            slice_from("lijk");
                            break;
                        case 9:
                            if (!r_R1())
                            {
                                break lab1;
                            }
                            slice_del();
                            break;
                        case 10:
                            if (!r_R1())
                            {
                                break lab1;
                            }
                            if (!r_C())
                            {
                                break lab1;
                            }
                            slice_del();
                            if (!r_lengthen_V())
                            {
                                break lab1;
                            }
                            break;
                    }
                    break lab0;
                } while (false);
                cursor = limit - v_1;
                ket = cursor;
                among_var = find_among_b(a_4, 3);
                if (among_var == 0)
                {
                    return false;
                }
                bra = cursor;
                switch(among_var) {
                    case 0:
                        return false;
                    case 1:
                        if (!r_R1())
                        {
                            return false;
                        }
                        if (!r_C())
                        {
                            return false;
                        }
                        slice_del();
                        if (!r_lengthen_V())
                        {
                            return false;
                        }
                        break;
                }
            } while (false);
            return true;
        }
        private boolean r_Step_7() {
            int among_var;
            ket = cursor;
            among_var = find_among_b(a_5, 3);
            if (among_var == 0)
            {
                return false;
            }
            bra = cursor;
            switch(among_var) {
                case 0:
                    return false;
                case 1:
                    slice_from("k");
                    break;
                case 2:
                    slice_from("f");
                    break;
                case 3:
                    slice_from("p");
                    break;
            }
            return true;
        }
        private boolean r_Step_6() {
            int among_var;
            ket = cursor;
            among_var = find_among_b(a_6, 22);
            if (among_var == 0)
            {
                return false;
            }
            bra = cursor;
            switch(among_var) {
                case 0:
                    return false;
                case 1:
                    slice_from("b");
                    break;
                case 2:
                    slice_from("c");
                    break;
                case 3:
                    slice_from("d");
                    break;
                case 4:
                    slice_from("f");
                    break;
                case 5:
                    slice_from("g");
                    break;
                case 6:
                    slice_from("h");
                    break;
                case 7:
                    slice_from("j");
                    break;
                case 8:
                    slice_from("k");
                    break;
                case 9:
                    slice_from("l");
                    break;
                case 10:
                    slice_from("m");
                    break;
                case 11:
                    slice_from("n");
                    break;
                case 12:
                    slice_from("p");
                    break;
                case 13:
                    slice_from("q");
                    break;
                case 14:
                    slice_from("r");
                    break;
                case 15:
                    slice_from("s");
                    break;
                case 16:
                    slice_from("t");
                    break;
                case 17:
                    slice_from("v");
                    break;
                case 18:
                    slice_from("w");
                    break;
                case 19:
                    slice_from("x");
                    break;
                case 20:
                    slice_from("z");
                    break;
                case 21:
                    slice_from("f");
                    break;
                case 22:
                    slice_from("s");
                    break;
            }
            return true;
        }
        private boolean r_Step_1c() {
            int among_var;
            int v_1;
            int v_2;
            ket = cursor;
            among_var = find_among_b(a_7, 2);
            if (among_var == 0)
            {
                return false;
            }
            bra = cursor;
            if (!r_R1())
            {
                return false;
            }
            if (!r_C())
            {
                return false;
            }
            switch(among_var) {
                case 0:
                    return false;
                case 1:
                    {
                        v_1 = limit - cursor;
                        lab0: do {
                            if (!(eq_s_b(1, "n")))
                            {
                                break lab0;
                            }
                            if (!r_R1())
                            {
                                break lab0;
                            }
                            return false;
                        } while (false);
                        cursor = limit - v_1;
                    }
                    slice_del();
                    break;
                case 2:
                    {
                        v_2 = limit - cursor;
                        lab1: do {
                            if (!(eq_s_b(1, "h")))
                            {
                                break lab1;
                            }
                            if (!r_R1())
                            {
                                break lab1;
                            }
                            return false;
                        } while (false);
                        cursor = limit - v_2;
                    }
                    slice_del();
                    break;
            }
            return true;
        }
        private boolean r_Lose_prefix() {
            int v_1;
            int v_2;
            int v_3;
            bra = cursor;
            if (!(eq_s(2, "ge")))
            {
                return false;
            }
            ket = cursor;
            v_1 = cursor;
            {
                int c = cursor + 3;
                if (0 > c || c > limit)
                {
                    return false;
                }
                cursor = c;
            }
            cursor = v_1;
            golab0: while(true)
            {
                v_2 = cursor;
                lab1: do {
                    if (!(in_grouping(g_v, 97, 121)))
                    {
                        break lab1;
                    }
                    cursor = v_2;
                    break golab0;
                } while (false);
                cursor = v_2;
                if (cursor >= limit)
                {
                    return false;
                }
                cursor++;
            }
            golab2: while(true)
            {
                v_3 = cursor;
                lab3: do {
                    if (!(out_grouping(g_v, 97, 121)))
                    {
                        break lab3;
                    }
                    cursor = v_3;
                    break golab2;
                } while (false);
                cursor = v_3;
                if (cursor >= limit)
                {
                    return false;
                }
                cursor++;
            }
            B_GE_removed = true;
            slice_del();
            return true;
        }
        private boolean r_Lose_infix() {
            int v_2;
            int v_3;
            int v_4;
            if (cursor >= limit)
            {
                return false;
            }
            cursor++;
            golab0: while(true)
            {
                lab1: do {
                    bra = cursor;
                    if (!(eq_s(2, "ge")))
                    {
                        break lab1;
                    }
                    ket = cursor;
                    break golab0;
                } while (false);
                if (cursor >= limit)
                {
                    return false;
                }
                cursor++;
            }
            v_2 = cursor;
            {
                int c = cursor + 3;
                if (0 > c || c > limit)
                {
                    return false;
                }
                cursor = c;
            }
            cursor = v_2;
            golab2: while(true)
            {
                v_3 = cursor;
                lab3: do {
                    if (!(in_grouping(g_v, 97, 121)))
                    {
                        break lab3;
                    }
                    cursor = v_3;
                    break golab2;
                } while (false);
                cursor = v_3;
                if (cursor >= limit)
                {
                    return false;
                }
                cursor++;
            }
            golab4: while(true)
            {
                v_4 = cursor;
                lab5: do {
                    if (!(out_grouping(g_v, 97, 121)))
                    {
                        break lab5;
                    }
                    cursor = v_4;
                    break golab4;
                } while (false);
                cursor = v_4;
                if (cursor >= limit)
                {
                    return false;
                }
                cursor++;
            }
            B_GE_removed = true;
            slice_del();
            return true;
        }
        private boolean r_measure() {
            int v_1;
            int v_2;
            int v_5;
            int v_6;
            int v_9;
            int v_10;
            v_1 = cursor;
            lab0: do {
                cursor = limit;
                I_p1 = cursor;
                I_p2 = cursor;
            } while (false);
            cursor = v_1;
            v_2 = cursor;
            lab1: do {
                replab2: while(true)
                {
                    lab3: do {
                        if (!(out_grouping(g_v, 97, 121)))
                        {
                            break lab3;
                        }
                        continue replab2;
                    } while (false);
                    break replab2;
                }
                {
                    int v_4 = 1;
                    replab4: while(true)
                    {
                        v_5 = cursor;
                        lab5: do {
                            lab6: do {
                                v_6 = cursor;
                                lab7: do {
                                    if (!(eq_s(2, "ij")))
                                    {
                                        break lab7;
                                    }
                                    break lab6;
                                } while (false);
                                cursor = v_6;
                                if (!(in_grouping(g_v, 97, 121)))
                                {
                                    break lab5;
                                }
                            } while (false);
                            v_4--;
                            continue replab4;
                        } while (false);
                        cursor = v_5;
                        break replab4;
                    }
                    if (v_4 > 0)
                    {
                        break lab1;
                    }
                }
                if (!(out_grouping(g_v, 97, 121)))
                {
                    break lab1;
                }
                I_p1 = cursor;
                replab8: while(true)
                {
                    lab9: do {
                        if (!(out_grouping(g_v, 97, 121)))
                        {
                            break lab9;
                        }
                        continue replab8;
                    } while (false);
                    break replab8;
                }
                {
                    int v_8 = 1;
                    replab10: while(true)
                    {
                        v_9 = cursor;
                        lab11: do {
                            lab12: do {
                                v_10 = cursor;
                                lab13: do {
                                    if (!(eq_s(2, "ij")))
                                    {
                                        break lab13;
                                    }
                                    break lab12;
                                } while (false);
                                cursor = v_10;
                                if (!(in_grouping(g_v, 97, 121)))
                                {
                                    break lab11;
                                }
                            } while (false);
                            v_8--;
                            continue replab10;
                        } while (false);
                        cursor = v_9;
                        break replab10;
                    }
                    if (v_8 > 0)
                    {
                        break lab1;
                    }
                }
                if (!(out_grouping(g_v, 97, 121)))
                {
                    break lab1;
                }
                I_p2 = cursor;
            } while (false);
            cursor = v_2;
            return true;
        }
        public boolean stem() {
            int v_1;
            int v_2;
            int v_3;
            int v_4;
            int v_5;
            int v_6;
            int v_7;
            int v_8;
            int v_9;
            int v_10;
            int v_11;
            int v_12;
            int v_13;
            int v_14;
            int v_15;
            int v_16;
            int v_18;
            int v_19;
            int v_20;
            B_Y_found = false;
            B_stemmed = false;
            v_1 = cursor;
            lab0: do {
                bra = cursor;
                if (!(eq_s(1, "y")))
                {
                    break lab0;
                }
                ket = cursor;
                slice_from("Y");
                B_Y_found = true;
            } while (false);
            cursor = v_1;
            v_2 = cursor;
            lab1: do {
                replab2: while(true)
                {
                    v_3 = cursor;
                    lab3: do {
                        golab4: while(true)
                        {
                            v_4 = cursor;
                            lab5: do {
                                if (!(in_grouping(g_v, 97, 121)))
                                {
                                    break lab5;
                                }
                                bra = cursor;
                                if (!(eq_s(1, "y")))
                                {
                                    break lab5;
                                }
                                ket = cursor;
                                cursor = v_4;
                                break golab4;
                            } while (false);
                            cursor = v_4;
                            if (cursor >= limit)
                            {
                                break lab3;
                            }
                            cursor++;
                        }
                        slice_from("Y");
                        B_Y_found = true;
                        continue replab2;
                    } while (false);
                    cursor = v_3;
                    break replab2;
                }
            } while (false);
            cursor = v_2;
            if (!r_measure())
            {
                return false;
            }
            limit_backward = cursor; cursor = limit;
            v_5 = limit - cursor;
            lab6: do {
                if (!r_Step_1())
                {
                    break lab6;
                }
                B_stemmed = true;
            } while (false);
            cursor = limit - v_5;
            v_6 = limit - cursor;
            lab7: do {
                if (!r_Step_2())
                {
                    break lab7;
                }
                B_stemmed = true;
            } while (false);
            cursor = limit - v_6;
            v_7 = limit - cursor;
            lab8: do {
                if (!r_Step_3())
                {
                    break lab8;
                }
                B_stemmed = true;
            } while (false);
            cursor = limit - v_7;
            v_8 = limit - cursor;
            lab9: do {
                if (!r_Step_4())
                {
                    break lab9;
                }
                B_stemmed = true;
            } while (false);
            cursor = limit - v_8;
            cursor = limit_backward;            
            B_GE_removed = false;
            v_9 = cursor;
            lab10: do {
                v_10 = cursor;
                if (!r_Lose_prefix())
                {
                    break lab10;
                }
                cursor = v_10;
                if (!r_measure())
                {
                    break lab10;
                }
            } while (false);
            cursor = v_9;
            limit_backward = cursor; cursor = limit;
            v_11 = limit - cursor;
            lab11: do {
                if (!(B_GE_removed))
                {
                    break lab11;
                }
                if (!r_Step_1c())
                {
                    break lab11;
                }
            } while (false);
            cursor = limit - v_11;
            cursor = limit_backward;            
            B_GE_removed = false;
            v_12 = cursor;
            lab12: do {
                v_13 = cursor;
                if (!r_Lose_infix())
                {
                    break lab12;
                }
                cursor = v_13;
                if (!r_measure())
                {
                    break lab12;
                }
            } while (false);
            cursor = v_12;
            limit_backward = cursor; cursor = limit;
            v_14 = limit - cursor;
            lab13: do {
                if (!(B_GE_removed))
                {
                    break lab13;
                }
                if (!r_Step_1c())
                {
                    break lab13;
                }
            } while (false);
            cursor = limit - v_14;
            cursor = limit_backward;            
            limit_backward = cursor; cursor = limit;
            v_15 = limit - cursor;
            lab14: do {
                if (!r_Step_7())
                {
                    break lab14;
                }
                B_stemmed = true;
            } while (false);
            cursor = limit - v_15;
            v_16 = limit - cursor;
            lab15: do {
                lab16: do {
                    lab17: do {
                        if (!(B_stemmed))
                        {
                            break lab17;
                        }
                        break lab16;
                    } while (false);
                    if (!(B_GE_removed))
                    {
                        break lab15;
                    }
                } while (false);
                if (!r_Step_6())
                {
                    break lab15;
                }
            } while (false);
            cursor = limit - v_16;
            cursor = limit_backward;            
            v_18 = cursor;
            lab18: do {
                if (!(B_Y_found))
                {
                    break lab18;
                }
                replab19: while(true)
                {
                    v_19 = cursor;
                    lab20: do {
                        golab21: while(true)
                        {
                            v_20 = cursor;
                            lab22: do {
                                bra = cursor;
                                if (!(eq_s(1, "Y")))
                                {
                                    break lab22;
                                }
                                ket = cursor;
                                cursor = v_20;
                                break golab21;
                            } while (false);
                            cursor = v_20;
                            if (cursor >= limit)
                            {
                                break lab20;
                            }
                            cursor++;
                        }
                        slice_from("y");
                        continue replab19;
                    } while (false);
                    cursor = v_19;
                    break replab19;
                }
            } while (false);
            cursor = v_18;
            return true;
        }
}

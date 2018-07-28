package org.tartarus.snowball.ext;
import org.tartarus.snowball.SnowballProgram;
import org.tartarus.snowball.Among;
public class DutchStemmer extends SnowballProgram {
        private Among a_0[] = {
            new Among ( "", -1, 6, "", this),
            new Among ( "\u00E1", 0, 1, "", this),
            new Among ( "\u00E4", 0, 1, "", this),
            new Among ( "\u00E9", 0, 2, "", this),
            new Among ( "\u00EB", 0, 2, "", this),
            new Among ( "\u00ED", 0, 3, "", this),
            new Among ( "\u00EF", 0, 3, "", this),
            new Among ( "\u00F3", 0, 4, "", this),
            new Among ( "\u00F6", 0, 4, "", this),
            new Among ( "\u00FA", 0, 5, "", this),
            new Among ( "\u00FC", 0, 5, "", this)
        };
        private Among a_1[] = {
            new Among ( "", -1, 3, "", this),
            new Among ( "I", 0, 2, "", this),
            new Among ( "Y", 0, 1, "", this)
        };
        private Among a_2[] = {
            new Among ( "dd", -1, -1, "", this),
            new Among ( "kk", -1, -1, "", this),
            new Among ( "tt", -1, -1, "", this)
        };
        private Among a_3[] = {
            new Among ( "ene", -1, 2, "", this),
            new Among ( "se", -1, 3, "", this),
            new Among ( "en", -1, 2, "", this),
            new Among ( "heden", 2, 1, "", this),
            new Among ( "s", -1, 3, "", this)
        };
        private Among a_4[] = {
            new Among ( "end", -1, 1, "", this),
            new Among ( "ig", -1, 2, "", this),
            new Among ( "ing", -1, 1, "", this),
            new Among ( "lijk", -1, 3, "", this),
            new Among ( "baar", -1, 4, "", this),
            new Among ( "bar", -1, 5, "", this)
        };
        private Among a_5[] = {
            new Among ( "aa", -1, -1, "", this),
            new Among ( "ee", -1, -1, "", this),
            new Among ( "oo", -1, -1, "", this),
            new Among ( "uu", -1, -1, "", this)
        };
        private static final char g_v[] = {17, 65, 16, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 128 };
        private static final char g_v_I[] = {1, 0, 0, 17, 65, 16, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 128 };
        private static final char g_v_j[] = {17, 67, 16, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 128 };
        private int I_p2;
        private int I_p1;
        private boolean B_e_found;
        private void copy_from(DutchStemmer other) {
            I_p2 = other.I_p2;
            I_p1 = other.I_p1;
            B_e_found = other.B_e_found;
            super.copy_from(other);
        }
        private boolean r_prelude() {
            int among_var;
            int v_1;
            int v_2;
            int v_3;
            int v_4;
            int v_5;
            int v_6;
            v_1 = cursor;
            replab0: while(true)
            {
                v_2 = cursor;
                lab1: do {
                    bra = cursor;
                    among_var = find_among(a_0, 11);
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
                cursor = v_2;
                break replab0;
            }
            cursor = v_1;
            v_3 = cursor;
            lab2: do {
                bra = cursor;
                if (!(eq_s(1, "y")))
                {
                    cursor = v_3;
                    break lab2;
                }
                ket = cursor;
                slice_from("Y");
            } while (false);
            replab3: while(true)
            {
                v_4 = cursor;
                lab4: do {
                    golab5: while(true)
                    {
                        v_5 = cursor;
                        lab6: do {
                            if (!(in_grouping(g_v, 97, 232)))
                            {
                                break lab6;
                            }
                            bra = cursor;
                            lab7: do {
                                v_6 = cursor;
                                lab8: do {
                                    if (!(eq_s(1, "i")))
                                    {
                                        break lab8;
                                    }
                                    ket = cursor;
                                    if (!(in_grouping(g_v, 97, 232)))
                                    {
                                        break lab8;
                                    }
                                    slice_from("I");
                                    break lab7;
                                } while (false);
                                cursor = v_6;
                                if (!(eq_s(1, "y")))
                                {
                                    break lab6;
                                }
                                ket = cursor;
                                slice_from("Y");
                            } while (false);
                            cursor = v_5;
                            break golab5;
                        } while (false);
                        cursor = v_5;
                        if (cursor >= limit)
                        {
                            break lab4;
                        }
                        cursor++;
                    }
                    continue replab3;
                } while (false);
                cursor = v_4;
                break replab3;
            }
            return true;
        }
        private boolean r_mark_regions() {
            I_p1 = limit;
            I_p2 = limit;
            golab0: while(true)
            {
                lab1: do {
                    if (!(in_grouping(g_v, 97, 232)))
                    {
                        break lab1;
                    }
                    break golab0;
                } while (false);
                if (cursor >= limit)
                {
                    return false;
                }
                cursor++;
            }
            golab2: while(true)
            {
                lab3: do {
                    if (!(out_grouping(g_v, 97, 232)))
                    {
                        break lab3;
                    }
                    break golab2;
                } while (false);
                if (cursor >= limit)
                {
                    return false;
                }
                cursor++;
            }
            I_p1 = cursor;
            lab4: do {
                if (!(I_p1 < 3))
                {
                    break lab4;
                }
                I_p1 = 3;
            } while (false);
            golab5: while(true)
            {
                lab6: do {
                    if (!(in_grouping(g_v, 97, 232)))
                    {
                        break lab6;
                    }
                    break golab5;
                } while (false);
                if (cursor >= limit)
                {
                    return false;
                }
                cursor++;
            }
            golab7: while(true)
            {
                lab8: do {
                    if (!(out_grouping(g_v, 97, 232)))
                    {
                        break lab8;
                    }
                    break golab7;
                } while (false);
                if (cursor >= limit)
                {
                    return false;
                }
                cursor++;
            }
            I_p2 = cursor;
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
                    among_var = find_among(a_1, 3);
                    if (among_var == 0)
                    {
                        break lab1;
                    }
                    ket = cursor;
                    switch(among_var) {
                        case 0:
                            break lab1;
                        case 1:
                            slice_from("y");
                            break;
                        case 2:
                            slice_from("i");
                            break;
                        case 3:
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
        private boolean r_undouble() {
            int v_1;
            v_1 = limit - cursor;
            if (find_among_b(a_2, 3) == 0)
            {
                return false;
            }
            cursor = limit - v_1;
            ket = cursor;
            if (cursor <= limit_backward)
            {
                return false;
            }
            cursor--;
            bra = cursor;
            slice_del();
            return true;
        }
        private boolean r_e_ending() {
            int v_1;
            B_e_found = false;
            ket = cursor;
            if (!(eq_s_b(1, "e")))
            {
                return false;
            }
            bra = cursor;
            if (!r_R1())
            {
                return false;
            }
            v_1 = limit - cursor;
            if (!(out_grouping_b(g_v, 97, 232)))
            {
                return false;
            }
            cursor = limit - v_1;
            slice_del();
            B_e_found = true;
            if (!r_undouble())
            {
                return false;
            }
            return true;
        }
        private boolean r_en_ending() {
            int v_1;
            int v_2;
            if (!r_R1())
            {
                return false;
            }
            v_1 = limit - cursor;
            if (!(out_grouping_b(g_v, 97, 232)))
            {
                return false;
            }
            cursor = limit - v_1;
            {
                v_2 = limit - cursor;
                lab0: do {
                    if (!(eq_s_b(3, "gem")))
                    {
                        break lab0;
                    }
                    return false;
                } while (false);
                cursor = limit - v_2;
            }
            slice_del();
            if (!r_undouble())
            {
                return false;
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
            int v_6;
            int v_7;
            int v_8;
            int v_9;
            int v_10;
            v_1 = limit - cursor;
            lab0: do {
                ket = cursor;
                among_var = find_among_b(a_3, 5);
                if (among_var == 0)
                {
                    break lab0;
                }
                bra = cursor;
                switch(among_var) {
                    case 0:
                        break lab0;
                    case 1:
                        if (!r_R1())
                        {
                            break lab0;
                        }
                        slice_from("heid");
                        break;
                    case 2:
                        if (!r_en_ending())
                        {
                            break lab0;
                        }
                        break;
                    case 3:
                        if (!r_R1())
                        {
                            break lab0;
                        }
                        if (!(out_grouping_b(g_v_j, 97, 232)))
                        {
                            break lab0;
                        }
                        slice_del();
                        break;
                }
            } while (false);
            cursor = limit - v_1;
            v_2 = limit - cursor;
            lab1: do {
                if (!r_e_ending())
                {
                    break lab1;
                }
            } while (false);
            cursor = limit - v_2;
            v_3 = limit - cursor;
            lab2: do {
                ket = cursor;
                if (!(eq_s_b(4, "heid")))
                {
                    break lab2;
                }
                bra = cursor;
                if (!r_R2())
                {
                    break lab2;
                }
                {
                    v_4 = limit - cursor;
                    lab3: do {
                        if (!(eq_s_b(1, "c")))
                        {
                            break lab3;
                        }
                        break lab2;
                    } while (false);
                    cursor = limit - v_4;
                }
                slice_del();
                ket = cursor;
                if (!(eq_s_b(2, "en")))
                {
                    break lab2;
                }
                bra = cursor;
                if (!r_en_ending())
                {
                    break lab2;
                }
            } while (false);
            cursor = limit - v_3;
            v_5 = limit - cursor;
            lab4: do {
                ket = cursor;
                among_var = find_among_b(a_4, 6);
                if (among_var == 0)
                {
                    break lab4;
                }
                bra = cursor;
                switch(among_var) {
                    case 0:
                        break lab4;
                    case 1:
                        if (!r_R2())
                        {
                            break lab4;
                        }
                        slice_del();
                        lab5: do {
                            v_6 = limit - cursor;
                            lab6: do {
                                ket = cursor;
                                if (!(eq_s_b(2, "ig")))
                                {
                                    break lab6;
                                }
                                bra = cursor;
                                if (!r_R2())
                                {
                                    break lab6;
                                }
                                {
                                    v_7 = limit - cursor;
                                    lab7: do {
                                        if (!(eq_s_b(1, "e")))
                                        {
                                            break lab7;
                                        }
                                        break lab6;
                                    } while (false);
                                    cursor = limit - v_7;
                                }
                                slice_del();
                                break lab5;
                            } while (false);
                            cursor = limit - v_6;
                            if (!r_undouble())
                            {
                                break lab4;
                            }
                        } while (false);
                        break;
                    case 2:
                        if (!r_R2())
                        {
                            break lab4;
                        }
                        {
                            v_8 = limit - cursor;
                            lab8: do {
                                if (!(eq_s_b(1, "e")))
                                {
                                    break lab8;
                                }
                                break lab4;
                            } while (false);
                            cursor = limit - v_8;
                        }
                        slice_del();
                        break;
                    case 3:
                        if (!r_R2())
                        {
                            break lab4;
                        }
                        slice_del();
                        if (!r_e_ending())
                        {
                            break lab4;
                        }
                        break;
                    case 4:
                        if (!r_R2())
                        {
                            break lab4;
                        }
                        slice_del();
                        break;
                    case 5:
                        if (!r_R2())
                        {
                            break lab4;
                        }
                        if (!(B_e_found))
                        {
                            break lab4;
                        }
                        slice_del();
                        break;
                }
            } while (false);
            cursor = limit - v_5;
            v_9 = limit - cursor;
            lab9: do {
                if (!(out_grouping_b(g_v_I, 73, 232)))
                {
                    break lab9;
                }
                v_10 = limit - cursor;
                if (find_among_b(a_5, 4) == 0)
                {
                    break lab9;
                }
                if (!(out_grouping_b(g_v, 97, 232)))
                {
                    break lab9;
                }
                cursor = limit - v_10;
                ket = cursor;
                if (cursor <= limit_backward)
                {
                    break lab9;
                }
                cursor--;
                bra = cursor;
                slice_del();
            } while (false);
            cursor = limit - v_9;
            return true;
        }
        public boolean stem() {
            int v_1;
            int v_2;
            int v_3;
            int v_4;
            v_1 = cursor;
            lab0: do {
                if (!r_prelude())
                {
                    break lab0;
                }
            } while (false);
            cursor = v_1;
            v_2 = cursor;
            lab1: do {
                if (!r_mark_regions())
                {
                    break lab1;
                }
            } while (false);
            cursor = v_2;
            limit_backward = cursor; cursor = limit;
            v_3 = limit - cursor;
            lab2: do {
                if (!r_standard_suffix())
                {
                    break lab2;
                }
            } while (false);
            cursor = limit - v_3;
            cursor = limit_backward;            
            v_4 = cursor;
            lab3: do {
                if (!r_postlude())
                {
                    break lab3;
                }
            } while (false);
            cursor = v_4;
            return true;
        }
}

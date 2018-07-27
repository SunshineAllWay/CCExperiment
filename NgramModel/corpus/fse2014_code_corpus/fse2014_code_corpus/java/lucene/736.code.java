package org.tartarus.snowball.ext;
import org.tartarus.snowball.SnowballProgram;
import org.tartarus.snowball.Among;
public class PorterStemmer extends SnowballProgram {
        private Among a_0[] = {
            new Among ( "s", -1, 3, "", this),
            new Among ( "ies", 0, 2, "", this),
            new Among ( "sses", 0, 1, "", this),
            new Among ( "ss", 0, -1, "", this)
        };
        private Among a_1[] = {
            new Among ( "", -1, 3, "", this),
            new Among ( "bb", 0, 2, "", this),
            new Among ( "dd", 0, 2, "", this),
            new Among ( "ff", 0, 2, "", this),
            new Among ( "gg", 0, 2, "", this),
            new Among ( "bl", 0, 1, "", this),
            new Among ( "mm", 0, 2, "", this),
            new Among ( "nn", 0, 2, "", this),
            new Among ( "pp", 0, 2, "", this),
            new Among ( "rr", 0, 2, "", this),
            new Among ( "at", 0, 1, "", this),
            new Among ( "tt", 0, 2, "", this),
            new Among ( "iz", 0, 1, "", this)
        };
        private Among a_2[] = {
            new Among ( "ed", -1, 2, "", this),
            new Among ( "eed", 0, 1, "", this),
            new Among ( "ing", -1, 2, "", this)
        };
        private Among a_3[] = {
            new Among ( "anci", -1, 3, "", this),
            new Among ( "enci", -1, 2, "", this),
            new Among ( "abli", -1, 4, "", this),
            new Among ( "eli", -1, 6, "", this),
            new Among ( "alli", -1, 9, "", this),
            new Among ( "ousli", -1, 12, "", this),
            new Among ( "entli", -1, 5, "", this),
            new Among ( "aliti", -1, 10, "", this),
            new Among ( "biliti", -1, 14, "", this),
            new Among ( "iviti", -1, 13, "", this),
            new Among ( "tional", -1, 1, "", this),
            new Among ( "ational", 10, 8, "", this),
            new Among ( "alism", -1, 10, "", this),
            new Among ( "ation", -1, 8, "", this),
            new Among ( "ization", 13, 7, "", this),
            new Among ( "izer", -1, 7, "", this),
            new Among ( "ator", -1, 8, "", this),
            new Among ( "iveness", -1, 13, "", this),
            new Among ( "fulness", -1, 11, "", this),
            new Among ( "ousness", -1, 12, "", this)
        };
        private Among a_4[] = {
            new Among ( "icate", -1, 2, "", this),
            new Among ( "ative", -1, 3, "", this),
            new Among ( "alize", -1, 1, "", this),
            new Among ( "iciti", -1, 2, "", this),
            new Among ( "ical", -1, 2, "", this),
            new Among ( "ful", -1, 3, "", this),
            new Among ( "ness", -1, 3, "", this)
        };
        private Among a_5[] = {
            new Among ( "ic", -1, 1, "", this),
            new Among ( "ance", -1, 1, "", this),
            new Among ( "ence", -1, 1, "", this),
            new Among ( "able", -1, 1, "", this),
            new Among ( "ible", -1, 1, "", this),
            new Among ( "ate", -1, 1, "", this),
            new Among ( "ive", -1, 1, "", this),
            new Among ( "ize", -1, 1, "", this),
            new Among ( "iti", -1, 1, "", this),
            new Among ( "al", -1, 1, "", this),
            new Among ( "ism", -1, 1, "", this),
            new Among ( "ion", -1, 2, "", this),
            new Among ( "er", -1, 1, "", this),
            new Among ( "ous", -1, 1, "", this),
            new Among ( "ant", -1, 1, "", this),
            new Among ( "ent", -1, 1, "", this),
            new Among ( "ment", 15, 1, "", this),
            new Among ( "ement", 16, 1, "", this),
            new Among ( "ou", -1, 1, "", this)
        };
        private static final char g_v[] = {17, 65, 16, 1 };
        private static final char g_v_WXY[] = {1, 17, 65, 208, 1 };
        private boolean B_Y_found;
        private int I_p2;
        private int I_p1;
        private void copy_from(PorterStemmer other) {
            B_Y_found = other.B_Y_found;
            I_p2 = other.I_p2;
            I_p1 = other.I_p1;
            super.copy_from(other);
        }
        private boolean r_shortv() {
            if (!(out_grouping_b(g_v_WXY, 89, 121)))
            {
                return false;
            }
            if (!(in_grouping_b(g_v, 97, 121)))
            {
                return false;
            }
            if (!(out_grouping_b(g_v, 97, 121)))
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
        private boolean r_Step_1a() {
            int among_var;
            ket = cursor;
            among_var = find_among_b(a_0, 4);
            if (among_var == 0)
            {
                return false;
            }
            bra = cursor;
            switch(among_var) {
                case 0:
                    return false;
                case 1:
                    slice_from("ss");
                    break;
                case 2:
                    slice_from("i");
                    break;
                case 3:
                    slice_del();
                    break;
            }
            return true;
        }
        private boolean r_Step_1b() {
            int among_var;
            int v_1;
            int v_3;
            int v_4;
            ket = cursor;
            among_var = find_among_b(a_2, 3);
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
                    slice_from("ee");
                    break;
                case 2:
                    v_1 = limit - cursor;
                    golab0: while(true)
                    {
                        lab1: do {
                            if (!(in_grouping_b(g_v, 97, 121)))
                            {
                                break lab1;
                            }
                            break golab0;
                        } while (false);
                        if (cursor <= limit_backward)
                        {
                            return false;
                        }
                        cursor--;
                    }
                    cursor = limit - v_1;
                    slice_del();
                    v_3 = limit - cursor;
                    among_var = find_among_b(a_1, 13);
                    if (among_var == 0)
                    {
                        return false;
                    }
                    cursor = limit - v_3;
                    switch(among_var) {
                        case 0:
                            return false;
                        case 1:
                            {
                                int c = cursor;
                                insert(cursor, cursor, "e");
                                cursor = c;
                            }
                            break;
                        case 2:
                            ket = cursor;
                            if (cursor <= limit_backward)
                            {
                                return false;
                            }
                            cursor--;
                            bra = cursor;
                            slice_del();
                            break;
                        case 3:
                            if (cursor != I_p1)
                            {
                                return false;
                            }
                            v_4 = limit - cursor;
                            if (!r_shortv())
                            {
                                return false;
                            }
                            cursor = limit - v_4;
                            {
                                int c = cursor;
                                insert(cursor, cursor, "e");
                                cursor = c;
                            }
                            break;
                    }
                    break;
            }
            return true;
        }
        private boolean r_Step_1c() {
            int v_1;
            ket = cursor;
            lab0: do {
                v_1 = limit - cursor;
                lab1: do {
                    if (!(eq_s_b(1, "y")))
                    {
                        break lab1;
                    }
                    break lab0;
                } while (false);
                cursor = limit - v_1;
                if (!(eq_s_b(1, "Y")))
                {
                    return false;
                }
            } while (false);
            bra = cursor;
            golab2: while(true)
            {
                lab3: do {
                    if (!(in_grouping_b(g_v, 97, 121)))
                    {
                        break lab3;
                    }
                    break golab2;
                } while (false);
                if (cursor <= limit_backward)
                {
                    return false;
                }
                cursor--;
            }
            slice_from("i");
            return true;
        }
        private boolean r_Step_2() {
            int among_var;
            ket = cursor;
            among_var = find_among_b(a_3, 20);
            if (among_var == 0)
            {
                return false;
            }
            bra = cursor;
            if (!r_R1())
            {
                return false;
            }
            switch(among_var) {
                case 0:
                    return false;
                case 1:
                    slice_from("tion");
                    break;
                case 2:
                    slice_from("ence");
                    break;
                case 3:
                    slice_from("ance");
                    break;
                case 4:
                    slice_from("able");
                    break;
                case 5:
                    slice_from("ent");
                    break;
                case 6:
                    slice_from("e");
                    break;
                case 7:
                    slice_from("ize");
                    break;
                case 8:
                    slice_from("ate");
                    break;
                case 9:
                    slice_from("al");
                    break;
                case 10:
                    slice_from("al");
                    break;
                case 11:
                    slice_from("ful");
                    break;
                case 12:
                    slice_from("ous");
                    break;
                case 13:
                    slice_from("ive");
                    break;
                case 14:
                    slice_from("ble");
                    break;
            }
            return true;
        }
        private boolean r_Step_3() {
            int among_var;
            ket = cursor;
            among_var = find_among_b(a_4, 7);
            if (among_var == 0)
            {
                return false;
            }
            bra = cursor;
            if (!r_R1())
            {
                return false;
            }
            switch(among_var) {
                case 0:
                    return false;
                case 1:
                    slice_from("al");
                    break;
                case 2:
                    slice_from("ic");
                    break;
                case 3:
                    slice_del();
                    break;
            }
            return true;
        }
        private boolean r_Step_4() {
            int among_var;
            int v_1;
            ket = cursor;
            among_var = find_among_b(a_5, 19);
            if (among_var == 0)
            {
                return false;
            }
            bra = cursor;
            if (!r_R2())
            {
                return false;
            }
            switch(among_var) {
                case 0:
                    return false;
                case 1:
                    slice_del();
                    break;
                case 2:
                    lab0: do {
                        v_1 = limit - cursor;
                        lab1: do {
                            if (!(eq_s_b(1, "s")))
                            {
                                break lab1;
                            }
                            break lab0;
                        } while (false);
                        cursor = limit - v_1;
                        if (!(eq_s_b(1, "t")))
                        {
                            return false;
                        }
                    } while (false);
                    slice_del();
                    break;
            }
            return true;
        }
        private boolean r_Step_5a() {
            int v_1;
            int v_2;
            ket = cursor;
            if (!(eq_s_b(1, "e")))
            {
                return false;
            }
            bra = cursor;
            lab0: do {
                v_1 = limit - cursor;
                lab1: do {
                    if (!r_R2())
                    {
                        break lab1;
                    }
                    break lab0;
                } while (false);
                cursor = limit - v_1;
                if (!r_R1())
                {
                    return false;
                }
                {
                    v_2 = limit - cursor;
                    lab2: do {
                        if (!r_shortv())
                        {
                            break lab2;
                        }
                        return false;
                    } while (false);
                    cursor = limit - v_2;
                }
            } while (false);
            slice_del();
            return true;
        }
        private boolean r_Step_5b() {
            ket = cursor;
            if (!(eq_s_b(1, "l")))
            {
                return false;
            }
            bra = cursor;
            if (!r_R2())
            {
                return false;
            }
            if (!(eq_s_b(1, "l")))
            {
                return false;
            }
            slice_del();
            return true;
        }
        public boolean stem() {
            int v_1;
            int v_2;
            int v_3;
            int v_4;
            int v_5;
            int v_10;
            int v_11;
            int v_12;
            int v_13;
            int v_14;
            int v_15;
            int v_16;
            int v_17;
            int v_18;
            int v_19;
            int v_20;
            B_Y_found = false;
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
            I_p1 = limit;
            I_p2 = limit;
            v_5 = cursor;
            lab6: do {
                golab7: while(true)
                {
                    lab8: do {
                        if (!(in_grouping(g_v, 97, 121)))
                        {
                            break lab8;
                        }
                        break golab7;
                    } while (false);
                    if (cursor >= limit)
                    {
                        break lab6;
                    }
                    cursor++;
                }
                golab9: while(true)
                {
                    lab10: do {
                        if (!(out_grouping(g_v, 97, 121)))
                        {
                            break lab10;
                        }
                        break golab9;
                    } while (false);
                    if (cursor >= limit)
                    {
                        break lab6;
                    }
                    cursor++;
                }
                I_p1 = cursor;
                golab11: while(true)
                {
                    lab12: do {
                        if (!(in_grouping(g_v, 97, 121)))
                        {
                            break lab12;
                        }
                        break golab11;
                    } while (false);
                    if (cursor >= limit)
                    {
                        break lab6;
                    }
                    cursor++;
                }
                golab13: while(true)
                {
                    lab14: do {
                        if (!(out_grouping(g_v, 97, 121)))
                        {
                            break lab14;
                        }
                        break golab13;
                    } while (false);
                    if (cursor >= limit)
                    {
                        break lab6;
                    }
                    cursor++;
                }
                I_p2 = cursor;
            } while (false);
            cursor = v_5;
            limit_backward = cursor; cursor = limit;
            v_10 = limit - cursor;
            lab15: do {
                if (!r_Step_1a())
                {
                    break lab15;
                }
            } while (false);
            cursor = limit - v_10;
            v_11 = limit - cursor;
            lab16: do {
                if (!r_Step_1b())
                {
                    break lab16;
                }
            } while (false);
            cursor = limit - v_11;
            v_12 = limit - cursor;
            lab17: do {
                if (!r_Step_1c())
                {
                    break lab17;
                }
            } while (false);
            cursor = limit - v_12;
            v_13 = limit - cursor;
            lab18: do {
                if (!r_Step_2())
                {
                    break lab18;
                }
            } while (false);
            cursor = limit - v_13;
            v_14 = limit - cursor;
            lab19: do {
                if (!r_Step_3())
                {
                    break lab19;
                }
            } while (false);
            cursor = limit - v_14;
            v_15 = limit - cursor;
            lab20: do {
                if (!r_Step_4())
                {
                    break lab20;
                }
            } while (false);
            cursor = limit - v_15;
            v_16 = limit - cursor;
            lab21: do {
                if (!r_Step_5a())
                {
                    break lab21;
                }
            } while (false);
            cursor = limit - v_16;
            v_17 = limit - cursor;
            lab22: do {
                if (!r_Step_5b())
                {
                    break lab22;
                }
            } while (false);
            cursor = limit - v_17;
            cursor = limit_backward;            
            v_18 = cursor;
            lab23: do {
                if (!(B_Y_found))
                {
                    break lab23;
                }
                replab24: while(true)
                {
                    v_19 = cursor;
                    lab25: do {
                        golab26: while(true)
                        {
                            v_20 = cursor;
                            lab27: do {
                                bra = cursor;
                                if (!(eq_s(1, "Y")))
                                {
                                    break lab27;
                                }
                                ket = cursor;
                                cursor = v_20;
                                break golab26;
                            } while (false);
                            cursor = v_20;
                            if (cursor >= limit)
                            {
                                break lab25;
                            }
                            cursor++;
                        }
                        slice_from("y");
                        continue replab24;
                    } while (false);
                    cursor = v_19;
                    break replab24;
                }
            } while (false);
            cursor = v_18;
            return true;
        }
}

package org.tartarus.snowball.ext;
import org.tartarus.snowball.SnowballProgram;
import org.tartarus.snowball.Among;
public class FinnishStemmer extends SnowballProgram {
        private Among a_0[] = {
            new Among ( "pa", -1, 1, "", this),
            new Among ( "sti", -1, 2, "", this),
            new Among ( "kaan", -1, 1, "", this),
            new Among ( "han", -1, 1, "", this),
            new Among ( "kin", -1, 1, "", this),
            new Among ( "h\u00E4n", -1, 1, "", this),
            new Among ( "k\u00E4\u00E4n", -1, 1, "", this),
            new Among ( "ko", -1, 1, "", this),
            new Among ( "p\u00E4", -1, 1, "", this),
            new Among ( "k\u00F6", -1, 1, "", this)
        };
        private Among a_1[] = {
            new Among ( "lla", -1, -1, "", this),
            new Among ( "na", -1, -1, "", this),
            new Among ( "ssa", -1, -1, "", this),
            new Among ( "ta", -1, -1, "", this),
            new Among ( "lta", 3, -1, "", this),
            new Among ( "sta", 3, -1, "", this)
        };
        private Among a_2[] = {
            new Among ( "ll\u00E4", -1, -1, "", this),
            new Among ( "n\u00E4", -1, -1, "", this),
            new Among ( "ss\u00E4", -1, -1, "", this),
            new Among ( "t\u00E4", -1, -1, "", this),
            new Among ( "lt\u00E4", 3, -1, "", this),
            new Among ( "st\u00E4", 3, -1, "", this)
        };
        private Among a_3[] = {
            new Among ( "lle", -1, -1, "", this),
            new Among ( "ine", -1, -1, "", this)
        };
        private Among a_4[] = {
            new Among ( "nsa", -1, 3, "", this),
            new Among ( "mme", -1, 3, "", this),
            new Among ( "nne", -1, 3, "", this),
            new Among ( "ni", -1, 2, "", this),
            new Among ( "si", -1, 1, "", this),
            new Among ( "an", -1, 4, "", this),
            new Among ( "en", -1, 6, "", this),
            new Among ( "\u00E4n", -1, 5, "", this),
            new Among ( "ns\u00E4", -1, 3, "", this)
        };
        private Among a_5[] = {
            new Among ( "aa", -1, -1, "", this),
            new Among ( "ee", -1, -1, "", this),
            new Among ( "ii", -1, -1, "", this),
            new Among ( "oo", -1, -1, "", this),
            new Among ( "uu", -1, -1, "", this),
            new Among ( "\u00E4\u00E4", -1, -1, "", this),
            new Among ( "\u00F6\u00F6", -1, -1, "", this)
        };
        private Among a_6[] = {
            new Among ( "a", -1, 8, "", this),
            new Among ( "lla", 0, -1, "", this),
            new Among ( "na", 0, -1, "", this),
            new Among ( "ssa", 0, -1, "", this),
            new Among ( "ta", 0, -1, "", this),
            new Among ( "lta", 4, -1, "", this),
            new Among ( "sta", 4, -1, "", this),
            new Among ( "tta", 4, 9, "", this),
            new Among ( "lle", -1, -1, "", this),
            new Among ( "ine", -1, -1, "", this),
            new Among ( "ksi", -1, -1, "", this),
            new Among ( "n", -1, 7, "", this),
            new Among ( "han", 11, 1, "", this),
            new Among ( "den", 11, -1, "r_VI", this),
            new Among ( "seen", 11, -1, "r_LONG", this),
            new Among ( "hen", 11, 2, "", this),
            new Among ( "tten", 11, -1, "r_VI", this),
            new Among ( "hin", 11, 3, "", this),
            new Among ( "siin", 11, -1, "r_VI", this),
            new Among ( "hon", 11, 4, "", this),
            new Among ( "h\u00E4n", 11, 5, "", this),
            new Among ( "h\u00F6n", 11, 6, "", this),
            new Among ( "\u00E4", -1, 8, "", this),
            new Among ( "ll\u00E4", 22, -1, "", this),
            new Among ( "n\u00E4", 22, -1, "", this),
            new Among ( "ss\u00E4", 22, -1, "", this),
            new Among ( "t\u00E4", 22, -1, "", this),
            new Among ( "lt\u00E4", 26, -1, "", this),
            new Among ( "st\u00E4", 26, -1, "", this),
            new Among ( "tt\u00E4", 26, 9, "", this)
        };
        private Among a_7[] = {
            new Among ( "eja", -1, -1, "", this),
            new Among ( "mma", -1, 1, "", this),
            new Among ( "imma", 1, -1, "", this),
            new Among ( "mpa", -1, 1, "", this),
            new Among ( "impa", 3, -1, "", this),
            new Among ( "mmi", -1, 1, "", this),
            new Among ( "immi", 5, -1, "", this),
            new Among ( "mpi", -1, 1, "", this),
            new Among ( "impi", 7, -1, "", this),
            new Among ( "ej\u00E4", -1, -1, "", this),
            new Among ( "mm\u00E4", -1, 1, "", this),
            new Among ( "imm\u00E4", 10, -1, "", this),
            new Among ( "mp\u00E4", -1, 1, "", this),
            new Among ( "imp\u00E4", 12, -1, "", this)
        };
        private Among a_8[] = {
            new Among ( "i", -1, -1, "", this),
            new Among ( "j", -1, -1, "", this)
        };
        private Among a_9[] = {
            new Among ( "mma", -1, 1, "", this),
            new Among ( "imma", 0, -1, "", this)
        };
        private static final char g_AEI[] = {17, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 8 };
        private static final char g_V1[] = {17, 65, 16, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 8, 0, 32 };
        private static final char g_V2[] = {17, 65, 16, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 8, 0, 32 };
        private static final char g_particle_end[] = {17, 97, 24, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 8, 0, 32 };
        private boolean B_ending_removed;
        private StringBuilder S_x = new StringBuilder();
        private int I_p2;
        private int I_p1;
        private void copy_from(FinnishStemmer other) {
            B_ending_removed = other.B_ending_removed;
            S_x = other.S_x;
            I_p2 = other.I_p2;
            I_p1 = other.I_p1;
            super.copy_from(other);
        }
        private boolean r_mark_regions() {
            int v_1;
            int v_3;
            I_p1 = limit;
            I_p2 = limit;
            golab0: while(true)
            {
                v_1 = cursor;
                lab1: do {
                    if (!(in_grouping(g_V1, 97, 246)))
                    {
                        break lab1;
                    }
                    cursor = v_1;
                    break golab0;
                } while (false);
                cursor = v_1;
                if (cursor >= limit)
                {
                    return false;
                }
                cursor++;
            }
            golab2: while(true)
            {
                lab3: do {
                    if (!(out_grouping(g_V1, 97, 246)))
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
            golab4: while(true)
            {
                v_3 = cursor;
                lab5: do {
                    if (!(in_grouping(g_V1, 97, 246)))
                    {
                        break lab5;
                    }
                    cursor = v_3;
                    break golab4;
                } while (false);
                cursor = v_3;
                if (cursor >= limit)
                {
                    return false;
                }
                cursor++;
            }
            golab6: while(true)
            {
                lab7: do {
                    if (!(out_grouping(g_V1, 97, 246)))
                    {
                        break lab7;
                    }
                    break golab6;
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
        private boolean r_R2() {
            if (!(I_p2 <= cursor))
            {
                return false;
            }
            return true;
        }
        private boolean r_particle_etc() {
            int among_var;
            int v_1;
            int v_2;
            v_1 = limit - cursor;
            if (cursor < I_p1)
            {
                return false;
            }
            cursor = I_p1;
            v_2 = limit_backward;
            limit_backward = cursor;
            cursor = limit - v_1;
            ket = cursor;
            among_var = find_among_b(a_0, 10);
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
                    if (!(in_grouping_b(g_particle_end, 97, 246)))
                    {
                        return false;
                    }
                    break;
                case 2:
                    if (!r_R2())
                    {
                        return false;
                    }
                    break;
            }
            slice_del();
            return true;
        }
        private boolean r_possessive() {
            int among_var;
            int v_1;
            int v_2;
            int v_3;
            v_1 = limit - cursor;
            if (cursor < I_p1)
            {
                return false;
            }
            cursor = I_p1;
            v_2 = limit_backward;
            limit_backward = cursor;
            cursor = limit - v_1;
            ket = cursor;
            among_var = find_among_b(a_4, 9);
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
                    {
                        v_3 = limit - cursor;
                        lab0: do {
                            if (!(eq_s_b(1, "k")))
                            {
                                break lab0;
                            }
                            return false;
                        } while (false);
                        cursor = limit - v_3;
                    }
                    slice_del();
                    break;
                case 2:
                    slice_del();
                    ket = cursor;
                    if (!(eq_s_b(3, "kse")))
                    {
                        return false;
                    }
                    bra = cursor;
                    slice_from("ksi");
                    break;
                case 3:
                    slice_del();
                    break;
                case 4:
                    if (find_among_b(a_1, 6) == 0)
                    {
                        return false;
                    }
                    slice_del();
                    break;
                case 5:
                    if (find_among_b(a_2, 6) == 0)
                    {
                        return false;
                    }
                    slice_del();
                    break;
                case 6:
                    if (find_among_b(a_3, 2) == 0)
                    {
                        return false;
                    }
                    slice_del();
                    break;
            }
            return true;
        }
        private boolean r_LONG() {
            if (find_among_b(a_5, 7) == 0)
            {
                return false;
            }
            return true;
        }
        private boolean r_VI() {
            if (!(eq_s_b(1, "i")))
            {
                return false;
            }
            if (!(in_grouping_b(g_V2, 97, 246)))
            {
                return false;
            }
            return true;
        }
        private boolean r_case_ending() {
            int among_var;
            int v_1;
            int v_2;
            int v_3;
            int v_4;
            int v_5;
            v_1 = limit - cursor;
            if (cursor < I_p1)
            {
                return false;
            }
            cursor = I_p1;
            v_2 = limit_backward;
            limit_backward = cursor;
            cursor = limit - v_1;
            ket = cursor;
            among_var = find_among_b(a_6, 30);
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
                    if (!(eq_s_b(1, "a")))
                    {
                        return false;
                    }
                    break;
                case 2:
                    if (!(eq_s_b(1, "e")))
                    {
                        return false;
                    }
                    break;
                case 3:
                    if (!(eq_s_b(1, "i")))
                    {
                        return false;
                    }
                    break;
                case 4:
                    if (!(eq_s_b(1, "o")))
                    {
                        return false;
                    }
                    break;
                case 5:
                    if (!(eq_s_b(1, "\u00E4")))
                    {
                        return false;
                    }
                    break;
                case 6:
                    if (!(eq_s_b(1, "\u00F6")))
                    {
                        return false;
                    }
                    break;
                case 7:
                    v_3 = limit - cursor;
                    lab0: do {
                        v_4 = limit - cursor;
                        lab1: do {
                            v_5 = limit - cursor;
                            lab2: do {
                                if (!r_LONG())
                                {
                                    break lab2;
                                }
                                break lab1;
                            } while (false);
                            cursor = limit - v_5;
                            if (!(eq_s_b(2, "ie")))
                            {
                                cursor = limit - v_3;
                                break lab0;
                            }
                        } while (false);
                        cursor = limit - v_4;
                        if (cursor <= limit_backward)
                        {
                            cursor = limit - v_3;
                            break lab0;
                        }
                        cursor--;
                        bra = cursor;
                    } while (false);
                    break;
                case 8:
                    if (!(in_grouping_b(g_V1, 97, 246)))
                    {
                        return false;
                    }
                    if (!(out_grouping_b(g_V1, 97, 246)))
                    {
                        return false;
                    }
                    break;
                case 9:
                    if (!(eq_s_b(1, "e")))
                    {
                        return false;
                    }
                    break;
            }
            slice_del();
            B_ending_removed = true;
            return true;
        }
        private boolean r_other_endings() {
            int among_var;
            int v_1;
            int v_2;
            int v_3;
            v_1 = limit - cursor;
            if (cursor < I_p2)
            {
                return false;
            }
            cursor = I_p2;
            v_2 = limit_backward;
            limit_backward = cursor;
            cursor = limit - v_1;
            ket = cursor;
            among_var = find_among_b(a_7, 14);
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
                    {
                        v_3 = limit - cursor;
                        lab0: do {
                            if (!(eq_s_b(2, "po")))
                            {
                                break lab0;
                            }
                            return false;
                        } while (false);
                        cursor = limit - v_3;
                    }
                    break;
            }
            slice_del();
            return true;
        }
        private boolean r_i_plural() {
            int v_1;
            int v_2;
            v_1 = limit - cursor;
            if (cursor < I_p1)
            {
                return false;
            }
            cursor = I_p1;
            v_2 = limit_backward;
            limit_backward = cursor;
            cursor = limit - v_1;
            ket = cursor;
            if (find_among_b(a_8, 2) == 0)
            {
                limit_backward = v_2;
                return false;
            }
            bra = cursor;
            limit_backward = v_2;
            slice_del();
            return true;
        }
        private boolean r_t_plural() {
            int among_var;
            int v_1;
            int v_2;
            int v_3;
            int v_4;
            int v_5;
            int v_6;
            v_1 = limit - cursor;
            if (cursor < I_p1)
            {
                return false;
            }
            cursor = I_p1;
            v_2 = limit_backward;
            limit_backward = cursor;
            cursor = limit - v_1;
            ket = cursor;
            if (!(eq_s_b(1, "t")))
            {
                limit_backward = v_2;
                return false;
            }
            bra = cursor;
            v_3 = limit - cursor;
            if (!(in_grouping_b(g_V1, 97, 246)))
            {
                limit_backward = v_2;
                return false;
            }
            cursor = limit - v_3;
            slice_del();
            limit_backward = v_2;
            v_4 = limit - cursor;
            if (cursor < I_p2)
            {
                return false;
            }
            cursor = I_p2;
            v_5 = limit_backward;
            limit_backward = cursor;
            cursor = limit - v_4;
            ket = cursor;
            among_var = find_among_b(a_9, 2);
            if (among_var == 0)
            {
                limit_backward = v_5;
                return false;
            }
            bra = cursor;
            limit_backward = v_5;
            switch(among_var) {
                case 0:
                    return false;
                case 1:
                    {
                        v_6 = limit - cursor;
                        lab0: do {
                            if (!(eq_s_b(2, "po")))
                            {
                                break lab0;
                            }
                            return false;
                        } while (false);
                        cursor = limit - v_6;
                    }
                    break;
            }
            slice_del();
            return true;
        }
        private boolean r_tidy() {
            int v_1;
            int v_2;
            int v_3;
            int v_4;
            int v_5;
            int v_6;
            int v_7;
            int v_8;
            int v_9;
            v_1 = limit - cursor;
            if (cursor < I_p1)
            {
                return false;
            }
            cursor = I_p1;
            v_2 = limit_backward;
            limit_backward = cursor;
            cursor = limit - v_1;
            v_3 = limit - cursor;
            lab0: do {
                v_4 = limit - cursor;
                if (!r_LONG())
                {
                    break lab0;
                }
                cursor = limit - v_4;
                ket = cursor;
                if (cursor <= limit_backward)
                {
                    break lab0;
                }
                cursor--;
                bra = cursor;
                slice_del();
            } while (false);
            cursor = limit - v_3;
            v_5 = limit - cursor;
            lab1: do {
                ket = cursor;
                if (!(in_grouping_b(g_AEI, 97, 228)))
                {
                    break lab1;
                }
                bra = cursor;
                if (!(out_grouping_b(g_V1, 97, 246)))
                {
                    break lab1;
                }
                slice_del();
            } while (false);
            cursor = limit - v_5;
            v_6 = limit - cursor;
            lab2: do {
                ket = cursor;
                if (!(eq_s_b(1, "j")))
                {
                    break lab2;
                }
                bra = cursor;
                lab3: do {
                    v_7 = limit - cursor;
                    lab4: do {
                        if (!(eq_s_b(1, "o")))
                        {
                            break lab4;
                        }
                        break lab3;
                    } while (false);
                    cursor = limit - v_7;
                    if (!(eq_s_b(1, "u")))
                    {
                        break lab2;
                    }
                } while (false);
                slice_del();
            } while (false);
            cursor = limit - v_6;
            v_8 = limit - cursor;
            lab5: do {
                ket = cursor;
                if (!(eq_s_b(1, "o")))
                {
                    break lab5;
                }
                bra = cursor;
                if (!(eq_s_b(1, "j")))
                {
                    break lab5;
                }
                slice_del();
            } while (false);
            cursor = limit - v_8;
            limit_backward = v_2;
            golab6: while(true)
            {
                v_9 = limit - cursor;
                lab7: do {
                    if (!(out_grouping_b(g_V1, 97, 246)))
                    {
                        break lab7;
                    }
                    cursor = limit - v_9;
                    break golab6;
                } while (false);
                cursor = limit - v_9;
                if (cursor <= limit_backward)
                {
                    return false;
                }
                cursor--;
            }
            ket = cursor;
            if (cursor <= limit_backward)
            {
                return false;
            }
            cursor--;
            bra = cursor;
            S_x = slice_to(S_x);
            if (!(eq_v_b(S_x)))
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
            int v_6;
            int v_7;
            int v_8;
            int v_9;
            v_1 = cursor;
            lab0: do {
                if (!r_mark_regions())
                {
                    break lab0;
                }
            } while (false);
            cursor = v_1;
            B_ending_removed = false;
            limit_backward = cursor; cursor = limit;
            v_2 = limit - cursor;
            lab1: do {
                if (!r_particle_etc())
                {
                    break lab1;
                }
            } while (false);
            cursor = limit - v_2;
            v_3 = limit - cursor;
            lab2: do {
                if (!r_possessive())
                {
                    break lab2;
                }
            } while (false);
            cursor = limit - v_3;
            v_4 = limit - cursor;
            lab3: do {
                if (!r_case_ending())
                {
                    break lab3;
                }
            } while (false);
            cursor = limit - v_4;
            v_5 = limit - cursor;
            lab4: do {
                if (!r_other_endings())
                {
                    break lab4;
                }
            } while (false);
            cursor = limit - v_5;
            lab5: do {
                v_6 = limit - cursor;
                lab6: do {
                    if (!(B_ending_removed))
                    {
                        break lab6;
                    }
                    v_7 = limit - cursor;
                    lab7: do {
                        if (!r_i_plural())
                        {
                            break lab7;
                        }
                    } while (false);
                    cursor = limit - v_7;
                    break lab5;
                } while (false);
                cursor = limit - v_6;
                v_8 = limit - cursor;
                lab8: do {
                    if (!r_t_plural())
                    {
                        break lab8;
                    }
                } while (false);
                cursor = limit - v_8;
            } while (false);
            v_9 = limit - cursor;
            lab9: do {
                if (!r_tidy())
                {
                    break lab9;
                }
            } while (false);
            cursor = limit - v_9;
            cursor = limit_backward;            return true;
        }
}

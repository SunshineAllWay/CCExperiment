package org.tartarus.snowball.ext;
import org.tartarus.snowball.SnowballProgram;
import org.tartarus.snowball.Among;
public class HungarianStemmer extends SnowballProgram {
        private Among a_0[] = {
            new Among ( "cs", -1, -1, "", this),
            new Among ( "dzs", -1, -1, "", this),
            new Among ( "gy", -1, -1, "", this),
            new Among ( "ly", -1, -1, "", this),
            new Among ( "ny", -1, -1, "", this),
            new Among ( "sz", -1, -1, "", this),
            new Among ( "ty", -1, -1, "", this),
            new Among ( "zs", -1, -1, "", this)
        };
        private Among a_1[] = {
            new Among ( "\u00E1", -1, 1, "", this),
            new Among ( "\u00E9", -1, 2, "", this)
        };
        private Among a_2[] = {
            new Among ( "bb", -1, -1, "", this),
            new Among ( "cc", -1, -1, "", this),
            new Among ( "dd", -1, -1, "", this),
            new Among ( "ff", -1, -1, "", this),
            new Among ( "gg", -1, -1, "", this),
            new Among ( "jj", -1, -1, "", this),
            new Among ( "kk", -1, -1, "", this),
            new Among ( "ll", -1, -1, "", this),
            new Among ( "mm", -1, -1, "", this),
            new Among ( "nn", -1, -1, "", this),
            new Among ( "pp", -1, -1, "", this),
            new Among ( "rr", -1, -1, "", this),
            new Among ( "ccs", -1, -1, "", this),
            new Among ( "ss", -1, -1, "", this),
            new Among ( "zzs", -1, -1, "", this),
            new Among ( "tt", -1, -1, "", this),
            new Among ( "vv", -1, -1, "", this),
            new Among ( "ggy", -1, -1, "", this),
            new Among ( "lly", -1, -1, "", this),
            new Among ( "nny", -1, -1, "", this),
            new Among ( "tty", -1, -1, "", this),
            new Among ( "ssz", -1, -1, "", this),
            new Among ( "zz", -1, -1, "", this)
        };
        private Among a_3[] = {
            new Among ( "al", -1, 1, "", this),
            new Among ( "el", -1, 2, "", this)
        };
        private Among a_4[] = {
            new Among ( "ba", -1, -1, "", this),
            new Among ( "ra", -1, -1, "", this),
            new Among ( "be", -1, -1, "", this),
            new Among ( "re", -1, -1, "", this),
            new Among ( "ig", -1, -1, "", this),
            new Among ( "nak", -1, -1, "", this),
            new Among ( "nek", -1, -1, "", this),
            new Among ( "val", -1, -1, "", this),
            new Among ( "vel", -1, -1, "", this),
            new Among ( "ul", -1, -1, "", this),
            new Among ( "n\u00E1l", -1, -1, "", this),
            new Among ( "n\u00E9l", -1, -1, "", this),
            new Among ( "b\u00F3l", -1, -1, "", this),
            new Among ( "r\u00F3l", -1, -1, "", this),
            new Among ( "t\u00F3l", -1, -1, "", this),
            new Among ( "b\u00F5l", -1, -1, "", this),
            new Among ( "r\u00F5l", -1, -1, "", this),
            new Among ( "t\u00F5l", -1, -1, "", this),
            new Among ( "\u00FCl", -1, -1, "", this),
            new Among ( "n", -1, -1, "", this),
            new Among ( "an", 19, -1, "", this),
            new Among ( "ban", 20, -1, "", this),
            new Among ( "en", 19, -1, "", this),
            new Among ( "ben", 22, -1, "", this),
            new Among ( "k\u00E9ppen", 22, -1, "", this),
            new Among ( "on", 19, -1, "", this),
            new Among ( "\u00F6n", 19, -1, "", this),
            new Among ( "k\u00E9pp", -1, -1, "", this),
            new Among ( "kor", -1, -1, "", this),
            new Among ( "t", -1, -1, "", this),
            new Among ( "at", 29, -1, "", this),
            new Among ( "et", 29, -1, "", this),
            new Among ( "k\u00E9nt", 29, -1, "", this),
            new Among ( "ank\u00E9nt", 32, -1, "", this),
            new Among ( "enk\u00E9nt", 32, -1, "", this),
            new Among ( "onk\u00E9nt", 32, -1, "", this),
            new Among ( "ot", 29, -1, "", this),
            new Among ( "\u00E9rt", 29, -1, "", this),
            new Among ( "\u00F6t", 29, -1, "", this),
            new Among ( "hez", -1, -1, "", this),
            new Among ( "hoz", -1, -1, "", this),
            new Among ( "h\u00F6z", -1, -1, "", this),
            new Among ( "v\u00E1", -1, -1, "", this),
            new Among ( "v\u00E9", -1, -1, "", this)
        };
        private Among a_5[] = {
            new Among ( "\u00E1n", -1, 2, "", this),
            new Among ( "\u00E9n", -1, 1, "", this),
            new Among ( "\u00E1nk\u00E9nt", -1, 3, "", this)
        };
        private Among a_6[] = {
            new Among ( "stul", -1, 2, "", this),
            new Among ( "astul", 0, 1, "", this),
            new Among ( "\u00E1stul", 0, 3, "", this),
            new Among ( "st\u00FCl", -1, 2, "", this),
            new Among ( "est\u00FCl", 3, 1, "", this),
            new Among ( "\u00E9st\u00FCl", 3, 4, "", this)
        };
        private Among a_7[] = {
            new Among ( "\u00E1", -1, 1, "", this),
            new Among ( "\u00E9", -1, 2, "", this)
        };
        private Among a_8[] = {
            new Among ( "k", -1, 7, "", this),
            new Among ( "ak", 0, 4, "", this),
            new Among ( "ek", 0, 6, "", this),
            new Among ( "ok", 0, 5, "", this),
            new Among ( "\u00E1k", 0, 1, "", this),
            new Among ( "\u00E9k", 0, 2, "", this),
            new Among ( "\u00F6k", 0, 3, "", this)
        };
        private Among a_9[] = {
            new Among ( "\u00E9i", -1, 7, "", this),
            new Among ( "\u00E1\u00E9i", 0, 6, "", this),
            new Among ( "\u00E9\u00E9i", 0, 5, "", this),
            new Among ( "\u00E9", -1, 9, "", this),
            new Among ( "k\u00E9", 3, 4, "", this),
            new Among ( "ak\u00E9", 4, 1, "", this),
            new Among ( "ek\u00E9", 4, 1, "", this),
            new Among ( "ok\u00E9", 4, 1, "", this),
            new Among ( "\u00E1k\u00E9", 4, 3, "", this),
            new Among ( "\u00E9k\u00E9", 4, 2, "", this),
            new Among ( "\u00F6k\u00E9", 4, 1, "", this),
            new Among ( "\u00E9\u00E9", 3, 8, "", this)
        };
        private Among a_10[] = {
            new Among ( "a", -1, 18, "", this),
            new Among ( "ja", 0, 17, "", this),
            new Among ( "d", -1, 16, "", this),
            new Among ( "ad", 2, 13, "", this),
            new Among ( "ed", 2, 13, "", this),
            new Among ( "od", 2, 13, "", this),
            new Among ( "\u00E1d", 2, 14, "", this),
            new Among ( "\u00E9d", 2, 15, "", this),
            new Among ( "\u00F6d", 2, 13, "", this),
            new Among ( "e", -1, 18, "", this),
            new Among ( "je", 9, 17, "", this),
            new Among ( "nk", -1, 4, "", this),
            new Among ( "unk", 11, 1, "", this),
            new Among ( "\u00E1nk", 11, 2, "", this),
            new Among ( "\u00E9nk", 11, 3, "", this),
            new Among ( "\u00FCnk", 11, 1, "", this),
            new Among ( "uk", -1, 8, "", this),
            new Among ( "juk", 16, 7, "", this),
            new Among ( "\u00E1juk", 17, 5, "", this),
            new Among ( "\u00FCk", -1, 8, "", this),
            new Among ( "j\u00FCk", 19, 7, "", this),
            new Among ( "\u00E9j\u00FCk", 20, 6, "", this),
            new Among ( "m", -1, 12, "", this),
            new Among ( "am", 22, 9, "", this),
            new Among ( "em", 22, 9, "", this),
            new Among ( "om", 22, 9, "", this),
            new Among ( "\u00E1m", 22, 10, "", this),
            new Among ( "\u00E9m", 22, 11, "", this),
            new Among ( "o", -1, 18, "", this),
            new Among ( "\u00E1", -1, 19, "", this),
            new Among ( "\u00E9", -1, 20, "", this)
        };
        private Among a_11[] = {
            new Among ( "id", -1, 10, "", this),
            new Among ( "aid", 0, 9, "", this),
            new Among ( "jaid", 1, 6, "", this),
            new Among ( "eid", 0, 9, "", this),
            new Among ( "jeid", 3, 6, "", this),
            new Among ( "\u00E1id", 0, 7, "", this),
            new Among ( "\u00E9id", 0, 8, "", this),
            new Among ( "i", -1, 15, "", this),
            new Among ( "ai", 7, 14, "", this),
            new Among ( "jai", 8, 11, "", this),
            new Among ( "ei", 7, 14, "", this),
            new Among ( "jei", 10, 11, "", this),
            new Among ( "\u00E1i", 7, 12, "", this),
            new Among ( "\u00E9i", 7, 13, "", this),
            new Among ( "itek", -1, 24, "", this),
            new Among ( "eitek", 14, 21, "", this),
            new Among ( "jeitek", 15, 20, "", this),
            new Among ( "\u00E9itek", 14, 23, "", this),
            new Among ( "ik", -1, 29, "", this),
            new Among ( "aik", 18, 26, "", this),
            new Among ( "jaik", 19, 25, "", this),
            new Among ( "eik", 18, 26, "", this),
            new Among ( "jeik", 21, 25, "", this),
            new Among ( "\u00E1ik", 18, 27, "", this),
            new Among ( "\u00E9ik", 18, 28, "", this),
            new Among ( "ink", -1, 20, "", this),
            new Among ( "aink", 25, 17, "", this),
            new Among ( "jaink", 26, 16, "", this),
            new Among ( "eink", 25, 17, "", this),
            new Among ( "jeink", 28, 16, "", this),
            new Among ( "\u00E1ink", 25, 18, "", this),
            new Among ( "\u00E9ink", 25, 19, "", this),
            new Among ( "aitok", -1, 21, "", this),
            new Among ( "jaitok", 32, 20, "", this),
            new Among ( "\u00E1itok", -1, 22, "", this),
            new Among ( "im", -1, 5, "", this),
            new Among ( "aim", 35, 4, "", this),
            new Among ( "jaim", 36, 1, "", this),
            new Among ( "eim", 35, 4, "", this),
            new Among ( "jeim", 38, 1, "", this),
            new Among ( "\u00E1im", 35, 2, "", this),
            new Among ( "\u00E9im", 35, 3, "", this)
        };
        private static final char g_v[] = {17, 65, 16, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 17, 52, 14 };
        private int I_p1;
        private void copy_from(HungarianStemmer other) {
            I_p1 = other.I_p1;
            super.copy_from(other);
        }
        private boolean r_mark_regions() {
            int v_1;
            int v_2;
            int v_3;
            I_p1 = limit;
            lab0: do {
                v_1 = cursor;
                lab1: do {
                    if (!(in_grouping(g_v, 97, 252)))
                    {
                        break lab1;
                    }
                    golab2: while(true)
                    {
                        v_2 = cursor;
                        lab3: do {
                            if (!(out_grouping(g_v, 97, 252)))
                            {
                                break lab3;
                            }
                            cursor = v_2;
                            break golab2;
                        } while (false);
                        cursor = v_2;
                        if (cursor >= limit)
                        {
                            break lab1;
                        }
                        cursor++;
                    }
                    lab4: do {
                        v_3 = cursor;
                        lab5: do {
                            if (find_among(a_0, 8) == 0)
                            {
                                break lab5;
                            }
                            break lab4;
                        } while (false);
                        cursor = v_3;
                        if (cursor >= limit)
                        {
                            break lab1;
                        }
                        cursor++;
                    } while (false);
                    I_p1 = cursor;
                    break lab0;
                } while (false);
                cursor = v_1;
                if (!(out_grouping(g_v, 97, 252)))
                {
                    return false;
                }
                golab6: while(true)
                {
                    lab7: do {
                        if (!(in_grouping(g_v, 97, 252)))
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
                I_p1 = cursor;
            } while (false);
            return true;
        }
        private boolean r_R1() {
            if (!(I_p1 <= cursor))
            {
                return false;
            }
            return true;
        }
        private boolean r_v_ending() {
            int among_var;
            ket = cursor;
            among_var = find_among_b(a_1, 2);
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
                    slice_from("a");
                    break;
                case 2:
                    slice_from("e");
                    break;
            }
            return true;
        }
        private boolean r_double() {
            int v_1;
            v_1 = limit - cursor;
            if (find_among_b(a_2, 23) == 0)
            {
                return false;
            }
            cursor = limit - v_1;
            return true;
        }
        private boolean r_undouble() {
            if (cursor <= limit_backward)
            {
                return false;
            }
            cursor--;
            ket = cursor;
            {
                int c = cursor - 1;
                if (limit_backward > c || c > limit)
                {
                    return false;
                }
                cursor = c;
            }
            bra = cursor;
            slice_del();
            return true;
        }
        private boolean r_instrum() {
            int among_var;
            ket = cursor;
            among_var = find_among_b(a_3, 2);
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
                    if (!r_double())
                    {
                        return false;
                    }
                    break;
                case 2:
                    if (!r_double())
                    {
                        return false;
                    }
                    break;
            }
            slice_del();
            if (!r_undouble())
            {
                return false;
            }
            return true;
        }
        private boolean r_case() {
            ket = cursor;
            if (find_among_b(a_4, 44) == 0)
            {
                return false;
            }
            bra = cursor;
            if (!r_R1())
            {
                return false;
            }
            slice_del();
            if (!r_v_ending())
            {
                return false;
            }
            return true;
        }
        private boolean r_case_special() {
            int among_var;
            ket = cursor;
            among_var = find_among_b(a_5, 3);
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
                    slice_from("e");
                    break;
                case 2:
                    slice_from("a");
                    break;
                case 3:
                    slice_from("a");
                    break;
            }
            return true;
        }
        private boolean r_case_other() {
            int among_var;
            ket = cursor;
            among_var = find_among_b(a_6, 6);
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
                    slice_del();
                    break;
                case 2:
                    slice_del();
                    break;
                case 3:
                    slice_from("a");
                    break;
                case 4:
                    slice_from("e");
                    break;
            }
            return true;
        }
        private boolean r_factive() {
            int among_var;
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
            switch(among_var) {
                case 0:
                    return false;
                case 1:
                    if (!r_double())
                    {
                        return false;
                    }
                    break;
                case 2:
                    if (!r_double())
                    {
                        return false;
                    }
                    break;
            }
            slice_del();
            if (!r_undouble())
            {
                return false;
            }
            return true;
        }
        private boolean r_plural() {
            int among_var;
            ket = cursor;
            among_var = find_among_b(a_8, 7);
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
                    slice_from("a");
                    break;
                case 2:
                    slice_from("e");
                    break;
                case 3:
                    slice_del();
                    break;
                case 4:
                    slice_del();
                    break;
                case 5:
                    slice_del();
                    break;
                case 6:
                    slice_del();
                    break;
                case 7:
                    slice_del();
                    break;
            }
            return true;
        }
        private boolean r_owned() {
            int among_var;
            ket = cursor;
            among_var = find_among_b(a_9, 12);
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
                    slice_del();
                    break;
                case 2:
                    slice_from("e");
                    break;
                case 3:
                    slice_from("a");
                    break;
                case 4:
                    slice_del();
                    break;
                case 5:
                    slice_from("e");
                    break;
                case 6:
                    slice_from("a");
                    break;
                case 7:
                    slice_del();
                    break;
                case 8:
                    slice_from("e");
                    break;
                case 9:
                    slice_del();
                    break;
            }
            return true;
        }
        private boolean r_sing_owner() {
            int among_var;
            ket = cursor;
            among_var = find_among_b(a_10, 31);
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
                    slice_del();
                    break;
                case 2:
                    slice_from("a");
                    break;
                case 3:
                    slice_from("e");
                    break;
                case 4:
                    slice_del();
                    break;
                case 5:
                    slice_from("a");
                    break;
                case 6:
                    slice_from("e");
                    break;
                case 7:
                    slice_del();
                    break;
                case 8:
                    slice_del();
                    break;
                case 9:
                    slice_del();
                    break;
                case 10:
                    slice_from("a");
                    break;
                case 11:
                    slice_from("e");
                    break;
                case 12:
                    slice_del();
                    break;
                case 13:
                    slice_del();
                    break;
                case 14:
                    slice_from("a");
                    break;
                case 15:
                    slice_from("e");
                    break;
                case 16:
                    slice_del();
                    break;
                case 17:
                    slice_del();
                    break;
                case 18:
                    slice_del();
                    break;
                case 19:
                    slice_from("a");
                    break;
                case 20:
                    slice_from("e");
                    break;
            }
            return true;
        }
        private boolean r_plur_owner() {
            int among_var;
            ket = cursor;
            among_var = find_among_b(a_11, 42);
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
                    slice_del();
                    break;
                case 2:
                    slice_from("a");
                    break;
                case 3:
                    slice_from("e");
                    break;
                case 4:
                    slice_del();
                    break;
                case 5:
                    slice_del();
                    break;
                case 6:
                    slice_del();
                    break;
                case 7:
                    slice_from("a");
                    break;
                case 8:
                    slice_from("e");
                    break;
                case 9:
                    slice_del();
                    break;
                case 10:
                    slice_del();
                    break;
                case 11:
                    slice_del();
                    break;
                case 12:
                    slice_from("a");
                    break;
                case 13:
                    slice_from("e");
                    break;
                case 14:
                    slice_del();
                    break;
                case 15:
                    slice_del();
                    break;
                case 16:
                    slice_del();
                    break;
                case 17:
                    slice_del();
                    break;
                case 18:
                    slice_from("a");
                    break;
                case 19:
                    slice_from("e");
                    break;
                case 20:
                    slice_del();
                    break;
                case 21:
                    slice_del();
                    break;
                case 22:
                    slice_from("a");
                    break;
                case 23:
                    slice_from("e");
                    break;
                case 24:
                    slice_del();
                    break;
                case 25:
                    slice_del();
                    break;
                case 26:
                    slice_del();
                    break;
                case 27:
                    slice_from("a");
                    break;
                case 28:
                    slice_from("e");
                    break;
                case 29:
                    slice_del();
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
            int v_7;
            int v_8;
            int v_9;
            int v_10;
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
                if (!r_instrum())
                {
                    break lab1;
                }
            } while (false);
            cursor = limit - v_2;
            v_3 = limit - cursor;
            lab2: do {
                if (!r_case())
                {
                    break lab2;
                }
            } while (false);
            cursor = limit - v_3;
            v_4 = limit - cursor;
            lab3: do {
                if (!r_case_special())
                {
                    break lab3;
                }
            } while (false);
            cursor = limit - v_4;
            v_5 = limit - cursor;
            lab4: do {
                if (!r_case_other())
                {
                    break lab4;
                }
            } while (false);
            cursor = limit - v_5;
            v_6 = limit - cursor;
            lab5: do {
                if (!r_factive())
                {
                    break lab5;
                }
            } while (false);
            cursor = limit - v_6;
            v_7 = limit - cursor;
            lab6: do {
                if (!r_owned())
                {
                    break lab6;
                }
            } while (false);
            cursor = limit - v_7;
            v_8 = limit - cursor;
            lab7: do {
                if (!r_sing_owner())
                {
                    break lab7;
                }
            } while (false);
            cursor = limit - v_8;
            v_9 = limit - cursor;
            lab8: do {
                if (!r_plur_owner())
                {
                    break lab8;
                }
            } while (false);
            cursor = limit - v_9;
            v_10 = limit - cursor;
            lab9: do {
                if (!r_plural())
                {
                    break lab9;
                }
            } while (false);
            cursor = limit - v_10;
            cursor = limit_backward;            return true;
        }
}

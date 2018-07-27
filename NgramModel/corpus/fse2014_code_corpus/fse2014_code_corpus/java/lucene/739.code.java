package org.tartarus.snowball.ext;
import org.tartarus.snowball.SnowballProgram;
import org.tartarus.snowball.Among;
public class RussianStemmer extends SnowballProgram {
        private Among a_0[] = {
            new Among ( "\u0432", -1, 1, "", this),
            new Among ( "\u0438\u0432", 0, 2, "", this),
            new Among ( "\u044B\u0432", 0, 2, "", this),
            new Among ( "\u0432\u0448\u0438", -1, 1, "", this),
            new Among ( "\u0438\u0432\u0448\u0438", 3, 2, "", this),
            new Among ( "\u044B\u0432\u0448\u0438", 3, 2, "", this),
            new Among ( "\u0432\u0448\u0438\u0441\u044C", -1, 1, "", this),
            new Among ( "\u0438\u0432\u0448\u0438\u0441\u044C", 6, 2, "", this),
            new Among ( "\u044B\u0432\u0448\u0438\u0441\u044C", 6, 2, "", this)
        };
        private Among a_1[] = {
            new Among ( "\u0435\u0435", -1, 1, "", this),
            new Among ( "\u0438\u0435", -1, 1, "", this),
            new Among ( "\u043E\u0435", -1, 1, "", this),
            new Among ( "\u044B\u0435", -1, 1, "", this),
            new Among ( "\u0438\u043C\u0438", -1, 1, "", this),
            new Among ( "\u044B\u043C\u0438", -1, 1, "", this),
            new Among ( "\u0435\u0439", -1, 1, "", this),
            new Among ( "\u0438\u0439", -1, 1, "", this),
            new Among ( "\u043E\u0439", -1, 1, "", this),
            new Among ( "\u044B\u0439", -1, 1, "", this),
            new Among ( "\u0435\u043C", -1, 1, "", this),
            new Among ( "\u0438\u043C", -1, 1, "", this),
            new Among ( "\u043E\u043C", -1, 1, "", this),
            new Among ( "\u044B\u043C", -1, 1, "", this),
            new Among ( "\u0435\u0433\u043E", -1, 1, "", this),
            new Among ( "\u043E\u0433\u043E", -1, 1, "", this),
            new Among ( "\u0435\u043C\u0443", -1, 1, "", this),
            new Among ( "\u043E\u043C\u0443", -1, 1, "", this),
            new Among ( "\u0438\u0445", -1, 1, "", this),
            new Among ( "\u044B\u0445", -1, 1, "", this),
            new Among ( "\u0435\u044E", -1, 1, "", this),
            new Among ( "\u043E\u044E", -1, 1, "", this),
            new Among ( "\u0443\u044E", -1, 1, "", this),
            new Among ( "\u044E\u044E", -1, 1, "", this),
            new Among ( "\u0430\u044F", -1, 1, "", this),
            new Among ( "\u044F\u044F", -1, 1, "", this)
        };
        private Among a_2[] = {
            new Among ( "\u0435\u043C", -1, 1, "", this),
            new Among ( "\u043D\u043D", -1, 1, "", this),
            new Among ( "\u0432\u0448", -1, 1, "", this),
            new Among ( "\u0438\u0432\u0448", 2, 2, "", this),
            new Among ( "\u044B\u0432\u0448", 2, 2, "", this),
            new Among ( "\u0449", -1, 1, "", this),
            new Among ( "\u044E\u0449", 5, 1, "", this),
            new Among ( "\u0443\u044E\u0449", 6, 2, "", this)
        };
        private Among a_3[] = {
            new Among ( "\u0441\u044C", -1, 1, "", this),
            new Among ( "\u0441\u044F", -1, 1, "", this)
        };
        private Among a_4[] = {
            new Among ( "\u043B\u0430", -1, 1, "", this),
            new Among ( "\u0438\u043B\u0430", 0, 2, "", this),
            new Among ( "\u044B\u043B\u0430", 0, 2, "", this),
            new Among ( "\u043D\u0430", -1, 1, "", this),
            new Among ( "\u0435\u043D\u0430", 3, 2, "", this),
            new Among ( "\u0435\u0442\u0435", -1, 1, "", this),
            new Among ( "\u0438\u0442\u0435", -1, 2, "", this),
            new Among ( "\u0439\u0442\u0435", -1, 1, "", this),
            new Among ( "\u0435\u0439\u0442\u0435", 7, 2, "", this),
            new Among ( "\u0443\u0439\u0442\u0435", 7, 2, "", this),
            new Among ( "\u043B\u0438", -1, 1, "", this),
            new Among ( "\u0438\u043B\u0438", 10, 2, "", this),
            new Among ( "\u044B\u043B\u0438", 10, 2, "", this),
            new Among ( "\u0439", -1, 1, "", this),
            new Among ( "\u0435\u0439", 13, 2, "", this),
            new Among ( "\u0443\u0439", 13, 2, "", this),
            new Among ( "\u043B", -1, 1, "", this),
            new Among ( "\u0438\u043B", 16, 2, "", this),
            new Among ( "\u044B\u043B", 16, 2, "", this),
            new Among ( "\u0435\u043C", -1, 1, "", this),
            new Among ( "\u0438\u043C", -1, 2, "", this),
            new Among ( "\u044B\u043C", -1, 2, "", this),
            new Among ( "\u043D", -1, 1, "", this),
            new Among ( "\u0435\u043D", 22, 2, "", this),
            new Among ( "\u043B\u043E", -1, 1, "", this),
            new Among ( "\u0438\u043B\u043E", 24, 2, "", this),
            new Among ( "\u044B\u043B\u043E", 24, 2, "", this),
            new Among ( "\u043D\u043E", -1, 1, "", this),
            new Among ( "\u0435\u043D\u043E", 27, 2, "", this),
            new Among ( "\u043D\u043D\u043E", 27, 1, "", this),
            new Among ( "\u0435\u0442", -1, 1, "", this),
            new Among ( "\u0443\u0435\u0442", 30, 2, "", this),
            new Among ( "\u0438\u0442", -1, 2, "", this),
            new Among ( "\u044B\u0442", -1, 2, "", this),
            new Among ( "\u044E\u0442", -1, 1, "", this),
            new Among ( "\u0443\u044E\u0442", 34, 2, "", this),
            new Among ( "\u044F\u0442", -1, 2, "", this),
            new Among ( "\u043D\u044B", -1, 1, "", this),
            new Among ( "\u0435\u043D\u044B", 37, 2, "", this),
            new Among ( "\u0442\u044C", -1, 1, "", this),
            new Among ( "\u0438\u0442\u044C", 39, 2, "", this),
            new Among ( "\u044B\u0442\u044C", 39, 2, "", this),
            new Among ( "\u0435\u0448\u044C", -1, 1, "", this),
            new Among ( "\u0438\u0448\u044C", -1, 2, "", this),
            new Among ( "\u044E", -1, 2, "", this),
            new Among ( "\u0443\u044E", 44, 2, "", this)
        };
        private Among a_5[] = {
            new Among ( "\u0430", -1, 1, "", this),
            new Among ( "\u0435\u0432", -1, 1, "", this),
            new Among ( "\u043E\u0432", -1, 1, "", this),
            new Among ( "\u0435", -1, 1, "", this),
            new Among ( "\u0438\u0435", 3, 1, "", this),
            new Among ( "\u044C\u0435", 3, 1, "", this),
            new Among ( "\u0438", -1, 1, "", this),
            new Among ( "\u0435\u0438", 6, 1, "", this),
            new Among ( "\u0438\u0438", 6, 1, "", this),
            new Among ( "\u0430\u043C\u0438", 6, 1, "", this),
            new Among ( "\u044F\u043C\u0438", 6, 1, "", this),
            new Among ( "\u0438\u044F\u043C\u0438", 10, 1, "", this),
            new Among ( "\u0439", -1, 1, "", this),
            new Among ( "\u0435\u0439", 12, 1, "", this),
            new Among ( "\u0438\u0435\u0439", 13, 1, "", this),
            new Among ( "\u0438\u0439", 12, 1, "", this),
            new Among ( "\u043E\u0439", 12, 1, "", this),
            new Among ( "\u0430\u043C", -1, 1, "", this),
            new Among ( "\u0435\u043C", -1, 1, "", this),
            new Among ( "\u0438\u0435\u043C", 18, 1, "", this),
            new Among ( "\u043E\u043C", -1, 1, "", this),
            new Among ( "\u044F\u043C", -1, 1, "", this),
            new Among ( "\u0438\u044F\u043C", 21, 1, "", this),
            new Among ( "\u043E", -1, 1, "", this),
            new Among ( "\u0443", -1, 1, "", this),
            new Among ( "\u0430\u0445", -1, 1, "", this),
            new Among ( "\u044F\u0445", -1, 1, "", this),
            new Among ( "\u0438\u044F\u0445", 26, 1, "", this),
            new Among ( "\u044B", -1, 1, "", this),
            new Among ( "\u044C", -1, 1, "", this),
            new Among ( "\u044E", -1, 1, "", this),
            new Among ( "\u0438\u044E", 30, 1, "", this),
            new Among ( "\u044C\u044E", 30, 1, "", this),
            new Among ( "\u044F", -1, 1, "", this),
            new Among ( "\u0438\u044F", 33, 1, "", this),
            new Among ( "\u044C\u044F", 33, 1, "", this)
        };
        private Among a_6[] = {
            new Among ( "\u043E\u0441\u0442", -1, 1, "", this),
            new Among ( "\u043E\u0441\u0442\u044C", -1, 1, "", this)
        };
        private Among a_7[] = {
            new Among ( "\u0435\u0439\u0448\u0435", -1, 1, "", this),
            new Among ( "\u043D", -1, 2, "", this),
            new Among ( "\u0435\u0439\u0448", -1, 1, "", this),
            new Among ( "\u044C", -1, 3, "", this)
        };
        private static final char g_v[] = {33, 65, 8, 232 };
        private int I_p2;
        private int I_pV;
        private void copy_from(RussianStemmer other) {
            I_p2 = other.I_p2;
            I_pV = other.I_pV;
            super.copy_from(other);
        }
        private boolean r_mark_regions() {
            int v_1;
            I_pV = limit;
            I_p2 = limit;
            v_1 = cursor;
            lab0: do {
                golab1: while(true)
                {
                    lab2: do {
                        if (!(in_grouping(g_v, 1072, 1103)))
                        {
                            break lab2;
                        }
                        break golab1;
                    } while (false);
                    if (cursor >= limit)
                    {
                        break lab0;
                    }
                    cursor++;
                }
                I_pV = cursor;
                golab3: while(true)
                {
                    lab4: do {
                        if (!(out_grouping(g_v, 1072, 1103)))
                        {
                            break lab4;
                        }
                        break golab3;
                    } while (false);
                    if (cursor >= limit)
                    {
                        break lab0;
                    }
                    cursor++;
                }
                golab5: while(true)
                {
                    lab6: do {
                        if (!(in_grouping(g_v, 1072, 1103)))
                        {
                            break lab6;
                        }
                        break golab5;
                    } while (false);
                    if (cursor >= limit)
                    {
                        break lab0;
                    }
                    cursor++;
                }
                golab7: while(true)
                {
                    lab8: do {
                        if (!(out_grouping(g_v, 1072, 1103)))
                        {
                            break lab8;
                        }
                        break golab7;
                    } while (false);
                    if (cursor >= limit)
                    {
                        break lab0;
                    }
                    cursor++;
                }
                I_p2 = cursor;
            } while (false);
            cursor = v_1;
            return true;
        }
        private boolean r_R2() {
            if (!(I_p2 <= cursor))
            {
                return false;
            }
            return true;
        }
        private boolean r_perfective_gerund() {
            int among_var;
            int v_1;
            ket = cursor;
            among_var = find_among_b(a_0, 9);
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
                            if (!(eq_s_b(1, "\u0430")))
                            {
                                break lab1;
                            }
                            break lab0;
                        } while (false);
                        cursor = limit - v_1;
                        if (!(eq_s_b(1, "\u044F")))
                        {
                            return false;
                        }
                    } while (false);
                    slice_del();
                    break;
                case 2:
                    slice_del();
                    break;
            }
            return true;
        }
        private boolean r_adjective() {
            int among_var;
            ket = cursor;
            among_var = find_among_b(a_1, 26);
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
            }
            return true;
        }
        private boolean r_adjectival() {
            int among_var;
            int v_1;
            int v_2;
            if (!r_adjective())
            {
                return false;
            }
            v_1 = limit - cursor;
            lab0: do {
                ket = cursor;
                among_var = find_among_b(a_2, 8);
                if (among_var == 0)
                {
                    cursor = limit - v_1;
                    break lab0;
                }
                bra = cursor;
                switch(among_var) {
                    case 0:
                        cursor = limit - v_1;
                        break lab0;
                    case 1:
                        lab1: do {
                            v_2 = limit - cursor;
                            lab2: do {
                                if (!(eq_s_b(1, "\u0430")))
                                {
                                    break lab2;
                                }
                                break lab1;
                            } while (false);
                            cursor = limit - v_2;
                            if (!(eq_s_b(1, "\u044F")))
                            {
                                cursor = limit - v_1;
                                break lab0;
                            }
                        } while (false);
                        slice_del();
                        break;
                    case 2:
                        slice_del();
                        break;
                }
            } while (false);
            return true;
        }
        private boolean r_reflexive() {
            int among_var;
            ket = cursor;
            among_var = find_among_b(a_3, 2);
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
            }
            return true;
        }
        private boolean r_verb() {
            int among_var;
            int v_1;
            ket = cursor;
            among_var = find_among_b(a_4, 46);
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
                            if (!(eq_s_b(1, "\u0430")))
                            {
                                break lab1;
                            }
                            break lab0;
                        } while (false);
                        cursor = limit - v_1;
                        if (!(eq_s_b(1, "\u044F")))
                        {
                            return false;
                        }
                    } while (false);
                    slice_del();
                    break;
                case 2:
                    slice_del();
                    break;
            }
            return true;
        }
        private boolean r_noun() {
            int among_var;
            ket = cursor;
            among_var = find_among_b(a_5, 36);
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
            }
            return true;
        }
        private boolean r_derivational() {
            int among_var;
            ket = cursor;
            among_var = find_among_b(a_6, 2);
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
            }
            return true;
        }
        private boolean r_tidy_up() {
            int among_var;
            ket = cursor;
            among_var = find_among_b(a_7, 4);
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
                    ket = cursor;
                    if (!(eq_s_b(1, "\u043D")))
                    {
                        return false;
                    }
                    bra = cursor;
                    if (!(eq_s_b(1, "\u043D")))
                    {
                        return false;
                    }
                    slice_del();
                    break;
                case 2:
                    if (!(eq_s_b(1, "\u043D")))
                    {
                        return false;
                    }
                    slice_del();
                    break;
                case 3:
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
            if (cursor < I_pV)
            {
                return false;
            }
            cursor = I_pV;
            v_3 = limit_backward;
            limit_backward = cursor;
            cursor = limit - v_2;
            v_4 = limit - cursor;
            lab1: do {
                lab2: do {
                    v_5 = limit - cursor;
                    lab3: do {
                        if (!r_perfective_gerund())
                        {
                            break lab3;
                        }
                        break lab2;
                    } while (false);
                    cursor = limit - v_5;
                    v_6 = limit - cursor;
                    lab4: do {
                        if (!r_reflexive())
                        {
                            cursor = limit - v_6;
                            break lab4;
                        }
                    } while (false);
                    lab5: do {
                        v_7 = limit - cursor;
                        lab6: do {
                            if (!r_adjectival())
                            {
                                break lab6;
                            }
                            break lab5;
                        } while (false);
                        cursor = limit - v_7;
                        lab7: do {
                            if (!r_verb())
                            {
                                break lab7;
                            }
                            break lab5;
                        } while (false);
                        cursor = limit - v_7;
                        if (!r_noun())
                        {
                            break lab1;
                        }
                    } while (false);
                } while (false);
            } while (false);
            cursor = limit - v_4;
            v_8 = limit - cursor;
            lab8: do {
                ket = cursor;
                if (!(eq_s_b(1, "\u0438")))
                {
                    cursor = limit - v_8;
                    break lab8;
                }
                bra = cursor;
                slice_del();
            } while (false);
            v_9 = limit - cursor;
            lab9: do {
                if (!r_derivational())
                {
                    break lab9;
                }
            } while (false);
            cursor = limit - v_9;
            v_10 = limit - cursor;
            lab10: do {
                if (!r_tidy_up())
                {
                    break lab10;
                }
            } while (false);
            cursor = limit - v_10;
            limit_backward = v_3;
            cursor = limit_backward;            return true;
        }
}

package org.tartarus.snowball.ext;
import org.tartarus.snowball.SnowballProgram;
import org.tartarus.snowball.Among;
public class TurkishStemmer extends SnowballProgram {
        private Among a_0[] = {
            new Among ( "m", -1, -1, "", this),
            new Among ( "n", -1, -1, "", this),
            new Among ( "miz", -1, -1, "", this),
            new Among ( "niz", -1, -1, "", this),
            new Among ( "muz", -1, -1, "", this),
            new Among ( "nuz", -1, -1, "", this),
            new Among ( "m\u00FCz", -1, -1, "", this),
            new Among ( "n\u00FCz", -1, -1, "", this),
            new Among ( "m\u0131z", -1, -1, "", this),
            new Among ( "n\u0131z", -1, -1, "", this)
        };
        private Among a_1[] = {
            new Among ( "leri", -1, -1, "", this),
            new Among ( "lar\u0131", -1, -1, "", this)
        };
        private Among a_2[] = {
            new Among ( "ni", -1, -1, "", this),
            new Among ( "nu", -1, -1, "", this),
            new Among ( "n\u00FC", -1, -1, "", this),
            new Among ( "n\u0131", -1, -1, "", this)
        };
        private Among a_3[] = {
            new Among ( "in", -1, -1, "", this),
            new Among ( "un", -1, -1, "", this),
            new Among ( "\u00FCn", -1, -1, "", this),
            new Among ( "\u0131n", -1, -1, "", this)
        };
        private Among a_4[] = {
            new Among ( "a", -1, -1, "", this),
            new Among ( "e", -1, -1, "", this)
        };
        private Among a_5[] = {
            new Among ( "na", -1, -1, "", this),
            new Among ( "ne", -1, -1, "", this)
        };
        private Among a_6[] = {
            new Among ( "da", -1, -1, "", this),
            new Among ( "ta", -1, -1, "", this),
            new Among ( "de", -1, -1, "", this),
            new Among ( "te", -1, -1, "", this)
        };
        private Among a_7[] = {
            new Among ( "nda", -1, -1, "", this),
            new Among ( "nde", -1, -1, "", this)
        };
        private Among a_8[] = {
            new Among ( "dan", -1, -1, "", this),
            new Among ( "tan", -1, -1, "", this),
            new Among ( "den", -1, -1, "", this),
            new Among ( "ten", -1, -1, "", this)
        };
        private Among a_9[] = {
            new Among ( "ndan", -1, -1, "", this),
            new Among ( "nden", -1, -1, "", this)
        };
        private Among a_10[] = {
            new Among ( "la", -1, -1, "", this),
            new Among ( "le", -1, -1, "", this)
        };
        private Among a_11[] = {
            new Among ( "ca", -1, -1, "", this),
            new Among ( "ce", -1, -1, "", this)
        };
        private Among a_12[] = {
            new Among ( "im", -1, -1, "", this),
            new Among ( "um", -1, -1, "", this),
            new Among ( "\u00FCm", -1, -1, "", this),
            new Among ( "\u0131m", -1, -1, "", this)
        };
        private Among a_13[] = {
            new Among ( "sin", -1, -1, "", this),
            new Among ( "sun", -1, -1, "", this),
            new Among ( "s\u00FCn", -1, -1, "", this),
            new Among ( "s\u0131n", -1, -1, "", this)
        };
        private Among a_14[] = {
            new Among ( "iz", -1, -1, "", this),
            new Among ( "uz", -1, -1, "", this),
            new Among ( "\u00FCz", -1, -1, "", this),
            new Among ( "\u0131z", -1, -1, "", this)
        };
        private Among a_15[] = {
            new Among ( "siniz", -1, -1, "", this),
            new Among ( "sunuz", -1, -1, "", this),
            new Among ( "s\u00FCn\u00FCz", -1, -1, "", this),
            new Among ( "s\u0131n\u0131z", -1, -1, "", this)
        };
        private Among a_16[] = {
            new Among ( "lar", -1, -1, "", this),
            new Among ( "ler", -1, -1, "", this)
        };
        private Among a_17[] = {
            new Among ( "niz", -1, -1, "", this),
            new Among ( "nuz", -1, -1, "", this),
            new Among ( "n\u00FCz", -1, -1, "", this),
            new Among ( "n\u0131z", -1, -1, "", this)
        };
        private Among a_18[] = {
            new Among ( "dir", -1, -1, "", this),
            new Among ( "tir", -1, -1, "", this),
            new Among ( "dur", -1, -1, "", this),
            new Among ( "tur", -1, -1, "", this),
            new Among ( "d\u00FCr", -1, -1, "", this),
            new Among ( "t\u00FCr", -1, -1, "", this),
            new Among ( "d\u0131r", -1, -1, "", this),
            new Among ( "t\u0131r", -1, -1, "", this)
        };
        private Among a_19[] = {
            new Among ( "cas\u0131na", -1, -1, "", this),
            new Among ( "cesine", -1, -1, "", this)
        };
        private Among a_20[] = {
            new Among ( "di", -1, -1, "", this),
            new Among ( "ti", -1, -1, "", this),
            new Among ( "dik", -1, -1, "", this),
            new Among ( "tik", -1, -1, "", this),
            new Among ( "duk", -1, -1, "", this),
            new Among ( "tuk", -1, -1, "", this),
            new Among ( "d\u00FCk", -1, -1, "", this),
            new Among ( "t\u00FCk", -1, -1, "", this),
            new Among ( "d\u0131k", -1, -1, "", this),
            new Among ( "t\u0131k", -1, -1, "", this),
            new Among ( "dim", -1, -1, "", this),
            new Among ( "tim", -1, -1, "", this),
            new Among ( "dum", -1, -1, "", this),
            new Among ( "tum", -1, -1, "", this),
            new Among ( "d\u00FCm", -1, -1, "", this),
            new Among ( "t\u00FCm", -1, -1, "", this),
            new Among ( "d\u0131m", -1, -1, "", this),
            new Among ( "t\u0131m", -1, -1, "", this),
            new Among ( "din", -1, -1, "", this),
            new Among ( "tin", -1, -1, "", this),
            new Among ( "dun", -1, -1, "", this),
            new Among ( "tun", -1, -1, "", this),
            new Among ( "d\u00FCn", -1, -1, "", this),
            new Among ( "t\u00FCn", -1, -1, "", this),
            new Among ( "d\u0131n", -1, -1, "", this),
            new Among ( "t\u0131n", -1, -1, "", this),
            new Among ( "du", -1, -1, "", this),
            new Among ( "tu", -1, -1, "", this),
            new Among ( "d\u00FC", -1, -1, "", this),
            new Among ( "t\u00FC", -1, -1, "", this),
            new Among ( "d\u0131", -1, -1, "", this),
            new Among ( "t\u0131", -1, -1, "", this)
        };
        private Among a_21[] = {
            new Among ( "sa", -1, -1, "", this),
            new Among ( "se", -1, -1, "", this),
            new Among ( "sak", -1, -1, "", this),
            new Among ( "sek", -1, -1, "", this),
            new Among ( "sam", -1, -1, "", this),
            new Among ( "sem", -1, -1, "", this),
            new Among ( "san", -1, -1, "", this),
            new Among ( "sen", -1, -1, "", this)
        };
        private Among a_22[] = {
            new Among ( "mi\u015F", -1, -1, "", this),
            new Among ( "mu\u015F", -1, -1, "", this),
            new Among ( "m\u00FC\u015F", -1, -1, "", this),
            new Among ( "m\u0131\u015F", -1, -1, "", this)
        };
        private Among a_23[] = {
            new Among ( "b", -1, 1, "", this),
            new Among ( "c", -1, 2, "", this),
            new Among ( "d", -1, 3, "", this),
            new Among ( "\u011F", -1, 4, "", this)
        };
        private static final char g_vowel[] = {17, 65, 16, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 32, 8, 0, 0, 0, 0, 0, 0, 1 };
        private static final char g_U[] = {1, 16, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 8, 0, 0, 0, 0, 0, 0, 1 };
        private static final char g_vowel1[] = {1, 64, 16, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1 };
        private static final char g_vowel2[] = {17, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 130 };
        private static final char g_vowel3[] = {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1 };
        private static final char g_vowel4[] = {17 };
        private static final char g_vowel5[] = {65 };
        private static final char g_vowel6[] = {65 };
        private boolean B_continue_stemming_noun_suffixes;
        private int I_strlen;
        private void copy_from(TurkishStemmer other) {
            B_continue_stemming_noun_suffixes = other.B_continue_stemming_noun_suffixes;
            I_strlen = other.I_strlen;
            super.copy_from(other);
        }
        private boolean r_check_vowel_harmony() {
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
            v_1 = limit - cursor;
            golab0: while(true)
            {
                v_2 = limit - cursor;
                lab1: do {
                    if (!(in_grouping_b(g_vowel, 97, 305)))
                    {
                        break lab1;
                    }
                    cursor = limit - v_2;
                    break golab0;
                } while (false);
                cursor = limit - v_2;
                if (cursor <= limit_backward)
                {
                    return false;
                }
                cursor--;
            }
            lab2: do {
                v_3 = limit - cursor;
                lab3: do {
                    if (!(eq_s_b(1, "a")))
                    {
                        break lab3;
                    }
                    golab4: while(true)
                    {
                        v_4 = limit - cursor;
                        lab5: do {
                            if (!(in_grouping_b(g_vowel1, 97, 305)))
                            {
                                break lab5;
                            }
                            cursor = limit - v_4;
                            break golab4;
                        } while (false);
                        cursor = limit - v_4;
                        if (cursor <= limit_backward)
                        {
                            break lab3;
                        }
                        cursor--;
                    }
                    break lab2;
                } while (false);
                cursor = limit - v_3;
                lab6: do {
                    if (!(eq_s_b(1, "e")))
                    {
                        break lab6;
                    }
                    golab7: while(true)
                    {
                        v_5 = limit - cursor;
                        lab8: do {
                            if (!(in_grouping_b(g_vowel2, 101, 252)))
                            {
                                break lab8;
                            }
                            cursor = limit - v_5;
                            break golab7;
                        } while (false);
                        cursor = limit - v_5;
                        if (cursor <= limit_backward)
                        {
                            break lab6;
                        }
                        cursor--;
                    }
                    break lab2;
                } while (false);
                cursor = limit - v_3;
                lab9: do {
                    if (!(eq_s_b(1, "\u0131")))
                    {
                        break lab9;
                    }
                    golab10: while(true)
                    {
                        v_6 = limit - cursor;
                        lab11: do {
                            if (!(in_grouping_b(g_vowel3, 97, 305)))
                            {
                                break lab11;
                            }
                            cursor = limit - v_6;
                            break golab10;
                        } while (false);
                        cursor = limit - v_6;
                        if (cursor <= limit_backward)
                        {
                            break lab9;
                        }
                        cursor--;
                    }
                    break lab2;
                } while (false);
                cursor = limit - v_3;
                lab12: do {
                    if (!(eq_s_b(1, "i")))
                    {
                        break lab12;
                    }
                    golab13: while(true)
                    {
                        v_7 = limit - cursor;
                        lab14: do {
                            if (!(in_grouping_b(g_vowel4, 101, 105)))
                            {
                                break lab14;
                            }
                            cursor = limit - v_7;
                            break golab13;
                        } while (false);
                        cursor = limit - v_7;
                        if (cursor <= limit_backward)
                        {
                            break lab12;
                        }
                        cursor--;
                    }
                    break lab2;
                } while (false);
                cursor = limit - v_3;
                lab15: do {
                    if (!(eq_s_b(1, "o")))
                    {
                        break lab15;
                    }
                    golab16: while(true)
                    {
                        v_8 = limit - cursor;
                        lab17: do {
                            if (!(in_grouping_b(g_vowel5, 111, 117)))
                            {
                                break lab17;
                            }
                            cursor = limit - v_8;
                            break golab16;
                        } while (false);
                        cursor = limit - v_8;
                        if (cursor <= limit_backward)
                        {
                            break lab15;
                        }
                        cursor--;
                    }
                    break lab2;
                } while (false);
                cursor = limit - v_3;
                lab18: do {
                    if (!(eq_s_b(1, "\u00F6")))
                    {
                        break lab18;
                    }
                    golab19: while(true)
                    {
                        v_9 = limit - cursor;
                        lab20: do {
                            if (!(in_grouping_b(g_vowel6, 246, 252)))
                            {
                                break lab20;
                            }
                            cursor = limit - v_9;
                            break golab19;
                        } while (false);
                        cursor = limit - v_9;
                        if (cursor <= limit_backward)
                        {
                            break lab18;
                        }
                        cursor--;
                    }
                    break lab2;
                } while (false);
                cursor = limit - v_3;
                lab21: do {
                    if (!(eq_s_b(1, "u")))
                    {
                        break lab21;
                    }
                    golab22: while(true)
                    {
                        v_10 = limit - cursor;
                        lab23: do {
                            if (!(in_grouping_b(g_vowel5, 111, 117)))
                            {
                                break lab23;
                            }
                            cursor = limit - v_10;
                            break golab22;
                        } while (false);
                        cursor = limit - v_10;
                        if (cursor <= limit_backward)
                        {
                            break lab21;
                        }
                        cursor--;
                    }
                    break lab2;
                } while (false);
                cursor = limit - v_3;
                if (!(eq_s_b(1, "\u00FC")))
                {
                    return false;
                }
                golab24: while(true)
                {
                    v_11 = limit - cursor;
                    lab25: do {
                        if (!(in_grouping_b(g_vowel6, 246, 252)))
                        {
                            break lab25;
                        }
                        cursor = limit - v_11;
                        break golab24;
                    } while (false);
                    cursor = limit - v_11;
                    if (cursor <= limit_backward)
                    {
                        return false;
                    }
                    cursor--;
                }
            } while (false);
            cursor = limit - v_1;
            return true;
        }
        private boolean r_mark_suffix_with_optional_n_consonant() {
            int v_1;
            int v_2;
            int v_3;
            int v_4;
            int v_5;
            int v_6;
            int v_7;
            lab0: do {
                v_1 = limit - cursor;
                lab1: do {
                    v_2 = limit - cursor;
                    if (!(eq_s_b(1, "n")))
                    {
                        break lab1;
                    }
                    cursor = limit - v_2;
                    if (cursor <= limit_backward)
                    {
                        break lab1;
                    }
                    cursor--;
                    v_3 = limit - cursor;
                    if (!(in_grouping_b(g_vowel, 97, 305)))
                    {
                        break lab1;
                    }
                    cursor = limit - v_3;
                    break lab0;
                } while (false);
                cursor = limit - v_1;
                {
                    v_4 = limit - cursor;
                    lab2: do {
                        v_5 = limit - cursor;
                        if (!(eq_s_b(1, "n")))
                        {
                            break lab2;
                        }
                        cursor = limit - v_5;
                        return false;
                    } while (false);
                    cursor = limit - v_4;
                }
                v_6 = limit - cursor;
                if (cursor <= limit_backward)
                {
                    return false;
                }
                cursor--;
                v_7 = limit - cursor;
                if (!(in_grouping_b(g_vowel, 97, 305)))
                {
                    return false;
                }
                cursor = limit - v_7;
                cursor = limit - v_6;
            } while (false);
            return true;
        }
        private boolean r_mark_suffix_with_optional_s_consonant() {
            int v_1;
            int v_2;
            int v_3;
            int v_4;
            int v_5;
            int v_6;
            int v_7;
            lab0: do {
                v_1 = limit - cursor;
                lab1: do {
                    v_2 = limit - cursor;
                    if (!(eq_s_b(1, "s")))
                    {
                        break lab1;
                    }
                    cursor = limit - v_2;
                    if (cursor <= limit_backward)
                    {
                        break lab1;
                    }
                    cursor--;
                    v_3 = limit - cursor;
                    if (!(in_grouping_b(g_vowel, 97, 305)))
                    {
                        break lab1;
                    }
                    cursor = limit - v_3;
                    break lab0;
                } while (false);
                cursor = limit - v_1;
                {
                    v_4 = limit - cursor;
                    lab2: do {
                        v_5 = limit - cursor;
                        if (!(eq_s_b(1, "s")))
                        {
                            break lab2;
                        }
                        cursor = limit - v_5;
                        return false;
                    } while (false);
                    cursor = limit - v_4;
                }
                v_6 = limit - cursor;
                if (cursor <= limit_backward)
                {
                    return false;
                }
                cursor--;
                v_7 = limit - cursor;
                if (!(in_grouping_b(g_vowel, 97, 305)))
                {
                    return false;
                }
                cursor = limit - v_7;
                cursor = limit - v_6;
            } while (false);
            return true;
        }
        private boolean r_mark_suffix_with_optional_y_consonant() {
            int v_1;
            int v_2;
            int v_3;
            int v_4;
            int v_5;
            int v_6;
            int v_7;
            lab0: do {
                v_1 = limit - cursor;
                lab1: do {
                    v_2 = limit - cursor;
                    if (!(eq_s_b(1, "y")))
                    {
                        break lab1;
                    }
                    cursor = limit - v_2;
                    if (cursor <= limit_backward)
                    {
                        break lab1;
                    }
                    cursor--;
                    v_3 = limit - cursor;
                    if (!(in_grouping_b(g_vowel, 97, 305)))
                    {
                        break lab1;
                    }
                    cursor = limit - v_3;
                    break lab0;
                } while (false);
                cursor = limit - v_1;
                {
                    v_4 = limit - cursor;
                    lab2: do {
                        v_5 = limit - cursor;
                        if (!(eq_s_b(1, "y")))
                        {
                            break lab2;
                        }
                        cursor = limit - v_5;
                        return false;
                    } while (false);
                    cursor = limit - v_4;
                }
                v_6 = limit - cursor;
                if (cursor <= limit_backward)
                {
                    return false;
                }
                cursor--;
                v_7 = limit - cursor;
                if (!(in_grouping_b(g_vowel, 97, 305)))
                {
                    return false;
                }
                cursor = limit - v_7;
                cursor = limit - v_6;
            } while (false);
            return true;
        }
        private boolean r_mark_suffix_with_optional_U_vowel() {
            int v_1;
            int v_2;
            int v_3;
            int v_4;
            int v_5;
            int v_6;
            int v_7;
            lab0: do {
                v_1 = limit - cursor;
                lab1: do {
                    v_2 = limit - cursor;
                    if (!(in_grouping_b(g_U, 105, 305)))
                    {
                        break lab1;
                    }
                    cursor = limit - v_2;
                    if (cursor <= limit_backward)
                    {
                        break lab1;
                    }
                    cursor--;
                    v_3 = limit - cursor;
                    if (!(out_grouping_b(g_vowel, 97, 305)))
                    {
                        break lab1;
                    }
                    cursor = limit - v_3;
                    break lab0;
                } while (false);
                cursor = limit - v_1;
                {
                    v_4 = limit - cursor;
                    lab2: do {
                        v_5 = limit - cursor;
                        if (!(in_grouping_b(g_U, 105, 305)))
                        {
                            break lab2;
                        }
                        cursor = limit - v_5;
                        return false;
                    } while (false);
                    cursor = limit - v_4;
                }
                v_6 = limit - cursor;
                if (cursor <= limit_backward)
                {
                    return false;
                }
                cursor--;
                v_7 = limit - cursor;
                if (!(out_grouping_b(g_vowel, 97, 305)))
                {
                    return false;
                }
                cursor = limit - v_7;
                cursor = limit - v_6;
            } while (false);
            return true;
        }
        private boolean r_mark_possessives() {
            if (find_among_b(a_0, 10) == 0)
            {
                return false;
            }
            if (!r_mark_suffix_with_optional_U_vowel())
            {
                return false;
            }
            return true;
        }
        private boolean r_mark_sU() {
            if (!r_check_vowel_harmony())
            {
                return false;
            }
            if (!(in_grouping_b(g_U, 105, 305)))
            {
                return false;
            }
            if (!r_mark_suffix_with_optional_s_consonant())
            {
                return false;
            }
            return true;
        }
        private boolean r_mark_lArI() {
            if (find_among_b(a_1, 2) == 0)
            {
                return false;
            }
            return true;
        }
        private boolean r_mark_yU() {
            if (!r_check_vowel_harmony())
            {
                return false;
            }
            if (!(in_grouping_b(g_U, 105, 305)))
            {
                return false;
            }
            if (!r_mark_suffix_with_optional_y_consonant())
            {
                return false;
            }
            return true;
        }
        private boolean r_mark_nU() {
            if (!r_check_vowel_harmony())
            {
                return false;
            }
            if (find_among_b(a_2, 4) == 0)
            {
                return false;
            }
            return true;
        }
        private boolean r_mark_nUn() {
            if (!r_check_vowel_harmony())
            {
                return false;
            }
            if (find_among_b(a_3, 4) == 0)
            {
                return false;
            }
            if (!r_mark_suffix_with_optional_n_consonant())
            {
                return false;
            }
            return true;
        }
        private boolean r_mark_yA() {
            if (!r_check_vowel_harmony())
            {
                return false;
            }
            if (find_among_b(a_4, 2) == 0)
            {
                return false;
            }
            if (!r_mark_suffix_with_optional_y_consonant())
            {
                return false;
            }
            return true;
        }
        private boolean r_mark_nA() {
            if (!r_check_vowel_harmony())
            {
                return false;
            }
            if (find_among_b(a_5, 2) == 0)
            {
                return false;
            }
            return true;
        }
        private boolean r_mark_DA() {
            if (!r_check_vowel_harmony())
            {
                return false;
            }
            if (find_among_b(a_6, 4) == 0)
            {
                return false;
            }
            return true;
        }
        private boolean r_mark_ndA() {
            if (!r_check_vowel_harmony())
            {
                return false;
            }
            if (find_among_b(a_7, 2) == 0)
            {
                return false;
            }
            return true;
        }
        private boolean r_mark_DAn() {
            if (!r_check_vowel_harmony())
            {
                return false;
            }
            if (find_among_b(a_8, 4) == 0)
            {
                return false;
            }
            return true;
        }
        private boolean r_mark_ndAn() {
            if (!r_check_vowel_harmony())
            {
                return false;
            }
            if (find_among_b(a_9, 2) == 0)
            {
                return false;
            }
            return true;
        }
        private boolean r_mark_ylA() {
            if (!r_check_vowel_harmony())
            {
                return false;
            }
            if (find_among_b(a_10, 2) == 0)
            {
                return false;
            }
            if (!r_mark_suffix_with_optional_y_consonant())
            {
                return false;
            }
            return true;
        }
        private boolean r_mark_ki() {
            if (!(eq_s_b(2, "ki")))
            {
                return false;
            }
            return true;
        }
        private boolean r_mark_ncA() {
            if (!r_check_vowel_harmony())
            {
                return false;
            }
            if (find_among_b(a_11, 2) == 0)
            {
                return false;
            }
            if (!r_mark_suffix_with_optional_n_consonant())
            {
                return false;
            }
            return true;
        }
        private boolean r_mark_yUm() {
            if (!r_check_vowel_harmony())
            {
                return false;
            }
            if (find_among_b(a_12, 4) == 0)
            {
                return false;
            }
            if (!r_mark_suffix_with_optional_y_consonant())
            {
                return false;
            }
            return true;
        }
        private boolean r_mark_sUn() {
            if (!r_check_vowel_harmony())
            {
                return false;
            }
            if (find_among_b(a_13, 4) == 0)
            {
                return false;
            }
            return true;
        }
        private boolean r_mark_yUz() {
            if (!r_check_vowel_harmony())
            {
                return false;
            }
            if (find_among_b(a_14, 4) == 0)
            {
                return false;
            }
            if (!r_mark_suffix_with_optional_y_consonant())
            {
                return false;
            }
            return true;
        }
        private boolean r_mark_sUnUz() {
            if (find_among_b(a_15, 4) == 0)
            {
                return false;
            }
            return true;
        }
        private boolean r_mark_lAr() {
            if (!r_check_vowel_harmony())
            {
                return false;
            }
            if (find_among_b(a_16, 2) == 0)
            {
                return false;
            }
            return true;
        }
        private boolean r_mark_nUz() {
            if (!r_check_vowel_harmony())
            {
                return false;
            }
            if (find_among_b(a_17, 4) == 0)
            {
                return false;
            }
            return true;
        }
        private boolean r_mark_DUr() {
            if (!r_check_vowel_harmony())
            {
                return false;
            }
            if (find_among_b(a_18, 8) == 0)
            {
                return false;
            }
            return true;
        }
        private boolean r_mark_cAsInA() {
            if (find_among_b(a_19, 2) == 0)
            {
                return false;
            }
            return true;
        }
        private boolean r_mark_yDU() {
            if (!r_check_vowel_harmony())
            {
                return false;
            }
            if (find_among_b(a_20, 32) == 0)
            {
                return false;
            }
            if (!r_mark_suffix_with_optional_y_consonant())
            {
                return false;
            }
            return true;
        }
        private boolean r_mark_ysA() {
            if (find_among_b(a_21, 8) == 0)
            {
                return false;
            }
            if (!r_mark_suffix_with_optional_y_consonant())
            {
                return false;
            }
            return true;
        }
        private boolean r_mark_ymUs_() {
            if (!r_check_vowel_harmony())
            {
                return false;
            }
            if (find_among_b(a_22, 4) == 0)
            {
                return false;
            }
            if (!r_mark_suffix_with_optional_y_consonant())
            {
                return false;
            }
            return true;
        }
        private boolean r_mark_yken() {
            if (!(eq_s_b(3, "ken")))
            {
                return false;
            }
            if (!r_mark_suffix_with_optional_y_consonant())
            {
                return false;
            }
            return true;
        }
        private boolean r_stem_nominal_verb_suffixes() {
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
            ket = cursor;
            B_continue_stemming_noun_suffixes = true;
            lab0: do {
                v_1 = limit - cursor;
                lab1: do {
                    lab2: do {
                        v_2 = limit - cursor;
                        lab3: do {
                            if (!r_mark_ymUs_())
                            {
                                break lab3;
                            }
                            break lab2;
                        } while (false);
                        cursor = limit - v_2;
                        lab4: do {
                            if (!r_mark_yDU())
                            {
                                break lab4;
                            }
                            break lab2;
                        } while (false);
                        cursor = limit - v_2;
                        lab5: do {
                            if (!r_mark_ysA())
                            {
                                break lab5;
                            }
                            break lab2;
                        } while (false);
                        cursor = limit - v_2;
                        if (!r_mark_yken())
                        {
                            break lab1;
                        }
                    } while (false);
                    break lab0;
                } while (false);
                cursor = limit - v_1;
                lab6: do {
                    if (!r_mark_cAsInA())
                    {
                        break lab6;
                    }
                    lab7: do {
                        v_3 = limit - cursor;
                        lab8: do {
                            if (!r_mark_sUnUz())
                            {
                                break lab8;
                            }
                            break lab7;
                        } while (false);
                        cursor = limit - v_3;
                        lab9: do {
                            if (!r_mark_lAr())
                            {
                                break lab9;
                            }
                            break lab7;
                        } while (false);
                        cursor = limit - v_3;
                        lab10: do {
                            if (!r_mark_yUm())
                            {
                                break lab10;
                            }
                            break lab7;
                        } while (false);
                        cursor = limit - v_3;
                        lab11: do {
                            if (!r_mark_sUn())
                            {
                                break lab11;
                            }
                            break lab7;
                        } while (false);
                        cursor = limit - v_3;
                        lab12: do {
                            if (!r_mark_yUz())
                            {
                                break lab12;
                            }
                            break lab7;
                        } while (false);
                        cursor = limit - v_3;
                    } while (false);
                    if (!r_mark_ymUs_())
                    {
                        break lab6;
                    }
                    break lab0;
                } while (false);
                cursor = limit - v_1;
                lab13: do {
                    if (!r_mark_lAr())
                    {
                        break lab13;
                    }
                    bra = cursor;
                    slice_del();
                    v_4 = limit - cursor;
                    lab14: do {
                        ket = cursor;
                        lab15: do {
                            v_5 = limit - cursor;
                            lab16: do {
                                if (!r_mark_DUr())
                                {
                                    break lab16;
                                }
                                break lab15;
                            } while (false);
                            cursor = limit - v_5;
                            lab17: do {
                                if (!r_mark_yDU())
                                {
                                    break lab17;
                                }
                                break lab15;
                            } while (false);
                            cursor = limit - v_5;
                            lab18: do {
                                if (!r_mark_ysA())
                                {
                                    break lab18;
                                }
                                break lab15;
                            } while (false);
                            cursor = limit - v_5;
                            if (!r_mark_ymUs_())
                            {
                                cursor = limit - v_4;
                                break lab14;
                            }
                        } while (false);
                    } while (false);
                    B_continue_stemming_noun_suffixes = false;
                    break lab0;
                } while (false);
                cursor = limit - v_1;
                lab19: do {
                    if (!r_mark_nUz())
                    {
                        break lab19;
                    }
                    lab20: do {
                        v_6 = limit - cursor;
                        lab21: do {
                            if (!r_mark_yDU())
                            {
                                break lab21;
                            }
                            break lab20;
                        } while (false);
                        cursor = limit - v_6;
                        if (!r_mark_ysA())
                        {
                            break lab19;
                        }
                    } while (false);
                    break lab0;
                } while (false);
                cursor = limit - v_1;
                lab22: do {
                    lab23: do {
                        v_7 = limit - cursor;
                        lab24: do {
                            if (!r_mark_sUnUz())
                            {
                                break lab24;
                            }
                            break lab23;
                        } while (false);
                        cursor = limit - v_7;
                        lab25: do {
                            if (!r_mark_yUz())
                            {
                                break lab25;
                            }
                            break lab23;
                        } while (false);
                        cursor = limit - v_7;
                        lab26: do {
                            if (!r_mark_sUn())
                            {
                                break lab26;
                            }
                            break lab23;
                        } while (false);
                        cursor = limit - v_7;
                        if (!r_mark_yUm())
                        {
                            break lab22;
                        }
                    } while (false);
                    bra = cursor;
                    slice_del();
                    v_8 = limit - cursor;
                    lab27: do {
                        ket = cursor;
                        if (!r_mark_ymUs_())
                        {
                            cursor = limit - v_8;
                            break lab27;
                        }
                    } while (false);
                    break lab0;
                } while (false);
                cursor = limit - v_1;
                if (!r_mark_DUr())
                {
                    return false;
                }
                bra = cursor;
                slice_del();
                v_9 = limit - cursor;
                lab28: do {
                    ket = cursor;
                    lab29: do {
                        v_10 = limit - cursor;
                        lab30: do {
                            if (!r_mark_sUnUz())
                            {
                                break lab30;
                            }
                            break lab29;
                        } while (false);
                        cursor = limit - v_10;
                        lab31: do {
                            if (!r_mark_lAr())
                            {
                                break lab31;
                            }
                            break lab29;
                        } while (false);
                        cursor = limit - v_10;
                        lab32: do {
                            if (!r_mark_yUm())
                            {
                                break lab32;
                            }
                            break lab29;
                        } while (false);
                        cursor = limit - v_10;
                        lab33: do {
                            if (!r_mark_sUn())
                            {
                                break lab33;
                            }
                            break lab29;
                        } while (false);
                        cursor = limit - v_10;
                        lab34: do {
                            if (!r_mark_yUz())
                            {
                                break lab34;
                            }
                            break lab29;
                        } while (false);
                        cursor = limit - v_10;
                    } while (false);
                    if (!r_mark_ymUs_())
                    {
                        cursor = limit - v_9;
                        break lab28;
                    }
                } while (false);
            } while (false);
            bra = cursor;
            slice_del();
            return true;
        }
        private boolean r_stem_suffix_chain_before_ki() {
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
            ket = cursor;
            if (!r_mark_ki())
            {
                return false;
            }
            lab0: do {
                v_1 = limit - cursor;
                lab1: do {
                    if (!r_mark_DA())
                    {
                        break lab1;
                    }
                    bra = cursor;
                    slice_del();
                    v_2 = limit - cursor;
                    lab2: do {
                        ket = cursor;
                        lab3: do {
                            v_3 = limit - cursor;
                            lab4: do {
                                if (!r_mark_lAr())
                                {
                                    break lab4;
                                }
                                bra = cursor;
                                slice_del();
                                v_4 = limit - cursor;
                                lab5: do {
                                    if (!r_stem_suffix_chain_before_ki())
                                    {
                                        cursor = limit - v_4;
                                        break lab5;
                                    }
                                } while (false);
                                break lab3;
                            } while (false);
                            cursor = limit - v_3;
                            if (!r_mark_possessives())
                            {
                                cursor = limit - v_2;
                                break lab2;
                            }
                            bra = cursor;
                            slice_del();
                            v_5 = limit - cursor;
                            lab6: do {
                                ket = cursor;
                                if (!r_mark_lAr())
                                {
                                    cursor = limit - v_5;
                                    break lab6;
                                }
                                bra = cursor;
                                slice_del();
                                if (!r_stem_suffix_chain_before_ki())
                                {
                                    cursor = limit - v_5;
                                    break lab6;
                                }
                            } while (false);
                        } while (false);
                    } while (false);
                    break lab0;
                } while (false);
                cursor = limit - v_1;
                lab7: do {
                    if (!r_mark_nUn())
                    {
                        break lab7;
                    }
                    bra = cursor;
                    slice_del();
                    v_6 = limit - cursor;
                    lab8: do {
                        ket = cursor;
                        lab9: do {
                            v_7 = limit - cursor;
                            lab10: do {
                                if (!r_mark_lArI())
                                {
                                    break lab10;
                                }
                                bra = cursor;
                                slice_del();
                                break lab9;
                            } while (false);
                            cursor = limit - v_7;
                            lab11: do {
                                ket = cursor;
                                lab12: do {
                                    v_8 = limit - cursor;
                                    lab13: do {
                                        if (!r_mark_possessives())
                                        {
                                            break lab13;
                                        }
                                        break lab12;
                                    } while (false);
                                    cursor = limit - v_8;
                                    if (!r_mark_sU())
                                    {
                                        break lab11;
                                    }
                                } while (false);
                                bra = cursor;
                                slice_del();
                                v_9 = limit - cursor;
                                lab14: do {
                                    ket = cursor;
                                    if (!r_mark_lAr())
                                    {
                                        cursor = limit - v_9;
                                        break lab14;
                                    }
                                    bra = cursor;
                                    slice_del();
                                    if (!r_stem_suffix_chain_before_ki())
                                    {
                                        cursor = limit - v_9;
                                        break lab14;
                                    }
                                } while (false);
                                break lab9;
                            } while (false);
                            cursor = limit - v_7;
                            if (!r_stem_suffix_chain_before_ki())
                            {
                                cursor = limit - v_6;
                                break lab8;
                            }
                        } while (false);
                    } while (false);
                    break lab0;
                } while (false);
                cursor = limit - v_1;
                if (!r_mark_ndA())
                {
                    return false;
                }
                lab15: do {
                    v_10 = limit - cursor;
                    lab16: do {
                        if (!r_mark_lArI())
                        {
                            break lab16;
                        }
                        bra = cursor;
                        slice_del();
                        break lab15;
                    } while (false);
                    cursor = limit - v_10;
                    lab17: do {
                        if (!r_mark_sU())
                        {
                            break lab17;
                        }
                        bra = cursor;
                        slice_del();
                        v_11 = limit - cursor;
                        lab18: do {
                            ket = cursor;
                            if (!r_mark_lAr())
                            {
                                cursor = limit - v_11;
                                break lab18;
                            }
                            bra = cursor;
                            slice_del();
                            if (!r_stem_suffix_chain_before_ki())
                            {
                                cursor = limit - v_11;
                                break lab18;
                            }
                        } while (false);
                        break lab15;
                    } while (false);
                    cursor = limit - v_10;
                    if (!r_stem_suffix_chain_before_ki())
                    {
                        return false;
                    }
                } while (false);
            } while (false);
            return true;
        }
        private boolean r_stem_noun_suffixes() {
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
            int v_17;
            int v_18;
            int v_19;
            int v_20;
            int v_21;
            int v_22;
            int v_23;
            int v_24;
            int v_25;
            int v_26;
            int v_27;
            lab0: do {
                v_1 = limit - cursor;
                lab1: do {
                    ket = cursor;
                    if (!r_mark_lAr())
                    {
                        break lab1;
                    }
                    bra = cursor;
                    slice_del();
                    v_2 = limit - cursor;
                    lab2: do {
                        if (!r_stem_suffix_chain_before_ki())
                        {
                            cursor = limit - v_2;
                            break lab2;
                        }
                    } while (false);
                    break lab0;
                } while (false);
                cursor = limit - v_1;
                lab3: do {
                    ket = cursor;
                    if (!r_mark_ncA())
                    {
                        break lab3;
                    }
                    bra = cursor;
                    slice_del();
                    v_3 = limit - cursor;
                    lab4: do {
                        lab5: do {
                            v_4 = limit - cursor;
                            lab6: do {
                                ket = cursor;
                                if (!r_mark_lArI())
                                {
                                    break lab6;
                                }
                                bra = cursor;
                                slice_del();
                                break lab5;
                            } while (false);
                            cursor = limit - v_4;
                            lab7: do {
                                ket = cursor;
                                lab8: do {
                                    v_5 = limit - cursor;
                                    lab9: do {
                                        if (!r_mark_possessives())
                                        {
                                            break lab9;
                                        }
                                        break lab8;
                                    } while (false);
                                    cursor = limit - v_5;
                                    if (!r_mark_sU())
                                    {
                                        break lab7;
                                    }
                                } while (false);
                                bra = cursor;
                                slice_del();
                                v_6 = limit - cursor;
                                lab10: do {
                                    ket = cursor;
                                    if (!r_mark_lAr())
                                    {
                                        cursor = limit - v_6;
                                        break lab10;
                                    }
                                    bra = cursor;
                                    slice_del();
                                    if (!r_stem_suffix_chain_before_ki())
                                    {
                                        cursor = limit - v_6;
                                        break lab10;
                                    }
                                } while (false);
                                break lab5;
                            } while (false);
                            cursor = limit - v_4;
                            ket = cursor;
                            if (!r_mark_lAr())
                            {
                                cursor = limit - v_3;
                                break lab4;
                            }
                            bra = cursor;
                            slice_del();
                            if (!r_stem_suffix_chain_before_ki())
                            {
                                cursor = limit - v_3;
                                break lab4;
                            }
                        } while (false);
                    } while (false);
                    break lab0;
                } while (false);
                cursor = limit - v_1;
                lab11: do {
                    ket = cursor;
                    lab12: do {
                        v_7 = limit - cursor;
                        lab13: do {
                            if (!r_mark_ndA())
                            {
                                break lab13;
                            }
                            break lab12;
                        } while (false);
                        cursor = limit - v_7;
                        if (!r_mark_nA())
                        {
                            break lab11;
                        }
                    } while (false);
                    lab14: do {
                        v_8 = limit - cursor;
                        lab15: do {
                            if (!r_mark_lArI())
                            {
                                break lab15;
                            }
                            bra = cursor;
                            slice_del();
                            break lab14;
                        } while (false);
                        cursor = limit - v_8;
                        lab16: do {
                            if (!r_mark_sU())
                            {
                                break lab16;
                            }
                            bra = cursor;
                            slice_del();
                            v_9 = limit - cursor;
                            lab17: do {
                                ket = cursor;
                                if (!r_mark_lAr())
                                {
                                    cursor = limit - v_9;
                                    break lab17;
                                }
                                bra = cursor;
                                slice_del();
                                if (!r_stem_suffix_chain_before_ki())
                                {
                                    cursor = limit - v_9;
                                    break lab17;
                                }
                            } while (false);
                            break lab14;
                        } while (false);
                        cursor = limit - v_8;
                        if (!r_stem_suffix_chain_before_ki())
                        {
                            break lab11;
                        }
                    } while (false);
                    break lab0;
                } while (false);
                cursor = limit - v_1;
                lab18: do {
                    ket = cursor;
                    lab19: do {
                        v_10 = limit - cursor;
                        lab20: do {
                            if (!r_mark_ndAn())
                            {
                                break lab20;
                            }
                            break lab19;
                        } while (false);
                        cursor = limit - v_10;
                        if (!r_mark_nU())
                        {
                            break lab18;
                        }
                    } while (false);
                    lab21: do {
                        v_11 = limit - cursor;
                        lab22: do {
                            if (!r_mark_sU())
                            {
                                break lab22;
                            }
                            bra = cursor;
                            slice_del();
                            v_12 = limit - cursor;
                            lab23: do {
                                ket = cursor;
                                if (!r_mark_lAr())
                                {
                                    cursor = limit - v_12;
                                    break lab23;
                                }
                                bra = cursor;
                                slice_del();
                                if (!r_stem_suffix_chain_before_ki())
                                {
                                    cursor = limit - v_12;
                                    break lab23;
                                }
                            } while (false);
                            break lab21;
                        } while (false);
                        cursor = limit - v_11;
                        if (!r_mark_lArI())
                        {
                            break lab18;
                        }
                    } while (false);
                    break lab0;
                } while (false);
                cursor = limit - v_1;
                lab24: do {
                    ket = cursor;
                    if (!r_mark_DAn())
                    {
                        break lab24;
                    }
                    bra = cursor;
                    slice_del();
                    v_13 = limit - cursor;
                    lab25: do {
                        ket = cursor;
                        lab26: do {
                            v_14 = limit - cursor;
                            lab27: do {
                                if (!r_mark_possessives())
                                {
                                    break lab27;
                                }
                                bra = cursor;
                                slice_del();
                                v_15 = limit - cursor;
                                lab28: do {
                                    ket = cursor;
                                    if (!r_mark_lAr())
                                    {
                                        cursor = limit - v_15;
                                        break lab28;
                                    }
                                    bra = cursor;
                                    slice_del();
                                    if (!r_stem_suffix_chain_before_ki())
                                    {
                                        cursor = limit - v_15;
                                        break lab28;
                                    }
                                } while (false);
                                break lab26;
                            } while (false);
                            cursor = limit - v_14;
                            lab29: do {
                                if (!r_mark_lAr())
                                {
                                    break lab29;
                                }
                                bra = cursor;
                                slice_del();
                                v_16 = limit - cursor;
                                lab30: do {
                                    if (!r_stem_suffix_chain_before_ki())
                                    {
                                        cursor = limit - v_16;
                                        break lab30;
                                    }
                                } while (false);
                                break lab26;
                            } while (false);
                            cursor = limit - v_14;
                            if (!r_stem_suffix_chain_before_ki())
                            {
                                cursor = limit - v_13;
                                break lab25;
                            }
                        } while (false);
                    } while (false);
                    break lab0;
                } while (false);
                cursor = limit - v_1;
                lab31: do {
                    ket = cursor;
                    lab32: do {
                        v_17 = limit - cursor;
                        lab33: do {
                            if (!r_mark_nUn())
                            {
                                break lab33;
                            }
                            break lab32;
                        } while (false);
                        cursor = limit - v_17;
                        if (!r_mark_ylA())
                        {
                            break lab31;
                        }
                    } while (false);
                    bra = cursor;
                    slice_del();
                    v_18 = limit - cursor;
                    lab34: do {
                        lab35: do {
                            v_19 = limit - cursor;
                            lab36: do {
                                ket = cursor;
                                if (!r_mark_lAr())
                                {
                                    break lab36;
                                }
                                bra = cursor;
                                slice_del();
                                if (!r_stem_suffix_chain_before_ki())
                                {
                                    break lab36;
                                }
                                break lab35;
                            } while (false);
                            cursor = limit - v_19;
                            lab37: do {
                                ket = cursor;
                                lab38: do {
                                    v_20 = limit - cursor;
                                    lab39: do {
                                        if (!r_mark_possessives())
                                        {
                                            break lab39;
                                        }
                                        break lab38;
                                    } while (false);
                                    cursor = limit - v_20;
                                    if (!r_mark_sU())
                                    {
                                        break lab37;
                                    }
                                } while (false);
                                bra = cursor;
                                slice_del();
                                v_21 = limit - cursor;
                                lab40: do {
                                    ket = cursor;
                                    if (!r_mark_lAr())
                                    {
                                        cursor = limit - v_21;
                                        break lab40;
                                    }
                                    bra = cursor;
                                    slice_del();
                                    if (!r_stem_suffix_chain_before_ki())
                                    {
                                        cursor = limit - v_21;
                                        break lab40;
                                    }
                                } while (false);
                                break lab35;
                            } while (false);
                            cursor = limit - v_19;
                            if (!r_stem_suffix_chain_before_ki())
                            {
                                cursor = limit - v_18;
                                break lab34;
                            }
                        } while (false);
                    } while (false);
                    break lab0;
                } while (false);
                cursor = limit - v_1;
                lab41: do {
                    ket = cursor;
                    if (!r_mark_lArI())
                    {
                        break lab41;
                    }
                    bra = cursor;
                    slice_del();
                    break lab0;
                } while (false);
                cursor = limit - v_1;
                lab42: do {
                    if (!r_stem_suffix_chain_before_ki())
                    {
                        break lab42;
                    }
                    break lab0;
                } while (false);
                cursor = limit - v_1;
                lab43: do {
                    ket = cursor;
                    lab44: do {
                        v_22 = limit - cursor;
                        lab45: do {
                            if (!r_mark_DA())
                            {
                                break lab45;
                            }
                            break lab44;
                        } while (false);
                        cursor = limit - v_22;
                        lab46: do {
                            if (!r_mark_yU())
                            {
                                break lab46;
                            }
                            break lab44;
                        } while (false);
                        cursor = limit - v_22;
                        if (!r_mark_yA())
                        {
                            break lab43;
                        }
                    } while (false);
                    bra = cursor;
                    slice_del();
                    v_23 = limit - cursor;
                    lab47: do {
                        ket = cursor;
                        lab48: do {
                            v_24 = limit - cursor;
                            lab49: do {
                                if (!r_mark_possessives())
                                {
                                    break lab49;
                                }
                                bra = cursor;
                                slice_del();
                                v_25 = limit - cursor;
                                lab50: do {
                                    ket = cursor;
                                    if (!r_mark_lAr())
                                    {
                                        cursor = limit - v_25;
                                        break lab50;
                                    }
                                } while (false);
                                break lab48;
                            } while (false);
                            cursor = limit - v_24;
                            if (!r_mark_lAr())
                            {
                                cursor = limit - v_23;
                                break lab47;
                            }
                        } while (false);
                        bra = cursor;
                        slice_del();
                        ket = cursor;
                        if (!r_stem_suffix_chain_before_ki())
                        {
                            cursor = limit - v_23;
                            break lab47;
                        }
                    } while (false);
                    break lab0;
                } while (false);
                cursor = limit - v_1;
                ket = cursor;
                lab51: do {
                    v_26 = limit - cursor;
                    lab52: do {
                        if (!r_mark_possessives())
                        {
                            break lab52;
                        }
                        break lab51;
                    } while (false);
                    cursor = limit - v_26;
                    if (!r_mark_sU())
                    {
                        return false;
                    }
                } while (false);
                bra = cursor;
                slice_del();
                v_27 = limit - cursor;
                lab53: do {
                    ket = cursor;
                    if (!r_mark_lAr())
                    {
                        cursor = limit - v_27;
                        break lab53;
                    }
                    bra = cursor;
                    slice_del();
                    if (!r_stem_suffix_chain_before_ki())
                    {
                        cursor = limit - v_27;
                        break lab53;
                    }
                } while (false);
            } while (false);
            return true;
        }
        private boolean r_post_process_last_consonants() {
            int among_var;
            ket = cursor;
            among_var = find_among_b(a_23, 4);
            if (among_var == 0)
            {
                return false;
            }
            bra = cursor;
            switch(among_var) {
                case 0:
                    return false;
                case 1:
                    slice_from("p");
                    break;
                case 2:
                    slice_from("\u00E7");
                    break;
                case 3:
                    slice_from("t");
                    break;
                case 4:
                    slice_from("k");
                    break;
            }
            return true;
        }
        private boolean r_append_U_to_stems_ending_with_d_or_g() {
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
            v_1 = limit - cursor;
            lab0: do {
                v_2 = limit - cursor;
                lab1: do {
                    if (!(eq_s_b(1, "d")))
                    {
                        break lab1;
                    }
                    break lab0;
                } while (false);
                cursor = limit - v_2;
                if (!(eq_s_b(1, "g")))
                {
                    return false;
                }
            } while (false);
            cursor = limit - v_1;
            lab2: do {
                v_3 = limit - cursor;
                lab3: do {
                    v_4 = limit - cursor;
                    golab4: while(true)
                    {
                        v_5 = limit - cursor;
                        lab5: do {
                            if (!(in_grouping_b(g_vowel, 97, 305)))
                            {
                                break lab5;
                            }
                            cursor = limit - v_5;
                            break golab4;
                        } while (false);
                        cursor = limit - v_5;
                        if (cursor <= limit_backward)
                        {
                            break lab3;
                        }
                        cursor--;
                    }
                    lab6: do {
                        v_6 = limit - cursor;
                        lab7: do {
                            if (!(eq_s_b(1, "a")))
                            {
                                break lab7;
                            }
                            break lab6;
                        } while (false);
                        cursor = limit - v_6;
                        if (!(eq_s_b(1, "\u0131")))
                        {
                            break lab3;
                        }
                    } while (false);
                    cursor = limit - v_4;
                    {
                        int c = cursor;
                        insert(cursor, cursor, "\u0131");
                        cursor = c;
                    }
                    break lab2;
                } while (false);
                cursor = limit - v_3;
                lab8: do {
                    v_7 = limit - cursor;
                    golab9: while(true)
                    {
                        v_8 = limit - cursor;
                        lab10: do {
                            if (!(in_grouping_b(g_vowel, 97, 305)))
                            {
                                break lab10;
                            }
                            cursor = limit - v_8;
                            break golab9;
                        } while (false);
                        cursor = limit - v_8;
                        if (cursor <= limit_backward)
                        {
                            break lab8;
                        }
                        cursor--;
                    }
                    lab11: do {
                        v_9 = limit - cursor;
                        lab12: do {
                            if (!(eq_s_b(1, "e")))
                            {
                                break lab12;
                            }
                            break lab11;
                        } while (false);
                        cursor = limit - v_9;
                        if (!(eq_s_b(1, "i")))
                        {
                            break lab8;
                        }
                    } while (false);
                    cursor = limit - v_7;
                    {
                        int c = cursor;
                        insert(cursor, cursor, "i");
                        cursor = c;
                    }
                    break lab2;
                } while (false);
                cursor = limit - v_3;
                lab13: do {
                    v_10 = limit - cursor;
                    golab14: while(true)
                    {
                        v_11 = limit - cursor;
                        lab15: do {
                            if (!(in_grouping_b(g_vowel, 97, 305)))
                            {
                                break lab15;
                            }
                            cursor = limit - v_11;
                            break golab14;
                        } while (false);
                        cursor = limit - v_11;
                        if (cursor <= limit_backward)
                        {
                            break lab13;
                        }
                        cursor--;
                    }
                    lab16: do {
                        v_12 = limit - cursor;
                        lab17: do {
                            if (!(eq_s_b(1, "o")))
                            {
                                break lab17;
                            }
                            break lab16;
                        } while (false);
                        cursor = limit - v_12;
                        if (!(eq_s_b(1, "u")))
                        {
                            break lab13;
                        }
                    } while (false);
                    cursor = limit - v_10;
                    {
                        int c = cursor;
                        insert(cursor, cursor, "u");
                        cursor = c;
                    }
                    break lab2;
                } while (false);
                cursor = limit - v_3;
                v_13 = limit - cursor;
                golab18: while(true)
                {
                    v_14 = limit - cursor;
                    lab19: do {
                        if (!(in_grouping_b(g_vowel, 97, 305)))
                        {
                            break lab19;
                        }
                        cursor = limit - v_14;
                        break golab18;
                    } while (false);
                    cursor = limit - v_14;
                    if (cursor <= limit_backward)
                    {
                        return false;
                    }
                    cursor--;
                }
                lab20: do {
                    v_15 = limit - cursor;
                    lab21: do {
                        if (!(eq_s_b(1, "\u00F6")))
                        {
                            break lab21;
                        }
                        break lab20;
                    } while (false);
                    cursor = limit - v_15;
                    if (!(eq_s_b(1, "\u00FC")))
                    {
                        return false;
                    }
                } while (false);
                cursor = limit - v_13;
                {
                    int c = cursor;
                    insert(cursor, cursor, "\u00FC");
                    cursor = c;
                }
            } while (false);
            return true;
        }
        private boolean r_more_than_one_syllable_word() {
            int v_1;
            int v_3;
            v_1 = cursor;
            {
                int v_2 = 2;
                replab0: while(true)
                {
                    v_3 = cursor;
                    lab1: do {
                        golab2: while(true)
                        {
                            lab3: do {
                                if (!(in_grouping(g_vowel, 97, 305)))
                                {
                                    break lab3;
                                }
                                break golab2;
                            } while (false);
                            if (cursor >= limit)
                            {
                                break lab1;
                            }
                            cursor++;
                        }
                        v_2--;
                        continue replab0;
                    } while (false);
                    cursor = v_3;
                    break replab0;
                }
                if (v_2 > 0)
                {
                    return false;
                }
            }
            cursor = v_1;
            return true;
        }
        private boolean r_is_reserved_word() {
            int v_1;
            int v_2;
            int v_4;
            lab0: do {
                v_1 = cursor;
                lab1: do {
                    v_2 = cursor;
                    golab2: while(true)
                    {
                        lab3: do {
                            if (!(eq_s(2, "ad")))
                            {
                                break lab3;
                            }
                            break golab2;
                        } while (false);
                        if (cursor >= limit)
                        {
                            break lab1;
                        }
                        cursor++;
                    }
                    I_strlen = 2;
                    if (!(I_strlen == limit))
                    {
                        break lab1;
                    }
                    cursor = v_2;
                    break lab0;
                } while (false);
                cursor = v_1;
                v_4 = cursor;
                golab4: while(true)
                {
                    lab5: do {
                        if (!(eq_s(5, "soyad")))
                        {
                            break lab5;
                        }
                        break golab4;
                    } while (false);
                    if (cursor >= limit)
                    {
                        return false;
                    }
                    cursor++;
                }
                I_strlen = 5;
                if (!(I_strlen == limit))
                {
                    return false;
                }
                cursor = v_4;
            } while (false);
            return true;
        }
        private boolean r_postlude() {
            int v_1;
            int v_2;
            int v_3;
            {
                v_1 = cursor;
                lab0: do {
                    if (!r_is_reserved_word())
                    {
                        break lab0;
                    }
                    return false;
                } while (false);
                cursor = v_1;
            }
            limit_backward = cursor; cursor = limit;
            v_2 = limit - cursor;
            lab1: do {
                if (!r_append_U_to_stems_ending_with_d_or_g())
                {
                    break lab1;
                }
            } while (false);
            cursor = limit - v_2;
            v_3 = limit - cursor;
            lab2: do {
                if (!r_post_process_last_consonants())
                {
                    break lab2;
                }
            } while (false);
            cursor = limit - v_3;
            cursor = limit_backward;            return true;
        }
        public boolean stem() {
            int v_1;
            int v_2;
            if (!r_more_than_one_syllable_word())
            {
                return false;
            }
            limit_backward = cursor; cursor = limit;
            v_1 = limit - cursor;
            lab0: do {
                if (!r_stem_nominal_verb_suffixes())
                {
                    break lab0;
                }
            } while (false);
            cursor = limit - v_1;
            if (!(B_continue_stemming_noun_suffixes))
            {
                return false;
            }
            v_2 = limit - cursor;
            lab1: do {
                if (!r_stem_noun_suffixes())
                {
                    break lab1;
                }
            } while (false);
            cursor = limit - v_2;
            cursor = limit_backward;            
            if (!r_postlude())
            {
                return false;
            }
            return true;
        }
}

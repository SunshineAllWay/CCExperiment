package org.tartarus.snowball.ext;
import org.tartarus.snowball.SnowballProgram;
import org.tartarus.snowball.Among;
public class PortugueseStemmer extends SnowballProgram {
        private Among a_0[] = {
            new Among ( "", -1, 3, "", this),
            new Among ( "\u00E3", 0, 1, "", this),
            new Among ( "\u00F5", 0, 2, "", this)
        };
        private Among a_1[] = {
            new Among ( "", -1, 3, "", this),
            new Among ( "a~", 0, 1, "", this),
            new Among ( "o~", 0, 2, "", this)
        };
        private Among a_2[] = {
            new Among ( "ic", -1, -1, "", this),
            new Among ( "ad", -1, -1, "", this),
            new Among ( "os", -1, -1, "", this),
            new Among ( "iv", -1, 1, "", this)
        };
        private Among a_3[] = {
            new Among ( "ante", -1, 1, "", this),
            new Among ( "avel", -1, 1, "", this),
            new Among ( "\u00EDvel", -1, 1, "", this)
        };
        private Among a_4[] = {
            new Among ( "ic", -1, 1, "", this),
            new Among ( "abil", -1, 1, "", this),
            new Among ( "iv", -1, 1, "", this)
        };
        private Among a_5[] = {
            new Among ( "ica", -1, 1, "", this),
            new Among ( "\u00E2ncia", -1, 1, "", this),
            new Among ( "\u00EAncia", -1, 4, "", this),
            new Among ( "ira", -1, 9, "", this),
            new Among ( "adora", -1, 1, "", this),
            new Among ( "osa", -1, 1, "", this),
            new Among ( "ista", -1, 1, "", this),
            new Among ( "iva", -1, 8, "", this),
            new Among ( "eza", -1, 1, "", this),
            new Among ( "log\u00EDa", -1, 2, "", this),
            new Among ( "idade", -1, 7, "", this),
            new Among ( "ante", -1, 1, "", this),
            new Among ( "mente", -1, 6, "", this),
            new Among ( "amente", 12, 5, "", this),
            new Among ( "\u00E1vel", -1, 1, "", this),
            new Among ( "\u00EDvel", -1, 1, "", this),
            new Among ( "uci\u00F3n", -1, 3, "", this),
            new Among ( "ico", -1, 1, "", this),
            new Among ( "ismo", -1, 1, "", this),
            new Among ( "oso", -1, 1, "", this),
            new Among ( "amento", -1, 1, "", this),
            new Among ( "imento", -1, 1, "", this),
            new Among ( "ivo", -1, 8, "", this),
            new Among ( "a\u00E7a~o", -1, 1, "", this),
            new Among ( "ador", -1, 1, "", this),
            new Among ( "icas", -1, 1, "", this),
            new Among ( "\u00EAncias", -1, 4, "", this),
            new Among ( "iras", -1, 9, "", this),
            new Among ( "adoras", -1, 1, "", this),
            new Among ( "osas", -1, 1, "", this),
            new Among ( "istas", -1, 1, "", this),
            new Among ( "ivas", -1, 8, "", this),
            new Among ( "ezas", -1, 1, "", this),
            new Among ( "log\u00EDas", -1, 2, "", this),
            new Among ( "idades", -1, 7, "", this),
            new Among ( "uciones", -1, 3, "", this),
            new Among ( "adores", -1, 1, "", this),
            new Among ( "antes", -1, 1, "", this),
            new Among ( "a\u00E7o~es", -1, 1, "", this),
            new Among ( "icos", -1, 1, "", this),
            new Among ( "ismos", -1, 1, "", this),
            new Among ( "osos", -1, 1, "", this),
            new Among ( "amentos", -1, 1, "", this),
            new Among ( "imentos", -1, 1, "", this),
            new Among ( "ivos", -1, 8, "", this)
        };
        private Among a_6[] = {
            new Among ( "ada", -1, 1, "", this),
            new Among ( "ida", -1, 1, "", this),
            new Among ( "ia", -1, 1, "", this),
            new Among ( "aria", 2, 1, "", this),
            new Among ( "eria", 2, 1, "", this),
            new Among ( "iria", 2, 1, "", this),
            new Among ( "ara", -1, 1, "", this),
            new Among ( "era", -1, 1, "", this),
            new Among ( "ira", -1, 1, "", this),
            new Among ( "ava", -1, 1, "", this),
            new Among ( "asse", -1, 1, "", this),
            new Among ( "esse", -1, 1, "", this),
            new Among ( "isse", -1, 1, "", this),
            new Among ( "aste", -1, 1, "", this),
            new Among ( "este", -1, 1, "", this),
            new Among ( "iste", -1, 1, "", this),
            new Among ( "ei", -1, 1, "", this),
            new Among ( "arei", 16, 1, "", this),
            new Among ( "erei", 16, 1, "", this),
            new Among ( "irei", 16, 1, "", this),
            new Among ( "am", -1, 1, "", this),
            new Among ( "iam", 20, 1, "", this),
            new Among ( "ariam", 21, 1, "", this),
            new Among ( "eriam", 21, 1, "", this),
            new Among ( "iriam", 21, 1, "", this),
            new Among ( "aram", 20, 1, "", this),
            new Among ( "eram", 20, 1, "", this),
            new Among ( "iram", 20, 1, "", this),
            new Among ( "avam", 20, 1, "", this),
            new Among ( "em", -1, 1, "", this),
            new Among ( "arem", 29, 1, "", this),
            new Among ( "erem", 29, 1, "", this),
            new Among ( "irem", 29, 1, "", this),
            new Among ( "assem", 29, 1, "", this),
            new Among ( "essem", 29, 1, "", this),
            new Among ( "issem", 29, 1, "", this),
            new Among ( "ado", -1, 1, "", this),
            new Among ( "ido", -1, 1, "", this),
            new Among ( "ando", -1, 1, "", this),
            new Among ( "endo", -1, 1, "", this),
            new Among ( "indo", -1, 1, "", this),
            new Among ( "ara~o", -1, 1, "", this),
            new Among ( "era~o", -1, 1, "", this),
            new Among ( "ira~o", -1, 1, "", this),
            new Among ( "ar", -1, 1, "", this),
            new Among ( "er", -1, 1, "", this),
            new Among ( "ir", -1, 1, "", this),
            new Among ( "as", -1, 1, "", this),
            new Among ( "adas", 47, 1, "", this),
            new Among ( "idas", 47, 1, "", this),
            new Among ( "ias", 47, 1, "", this),
            new Among ( "arias", 50, 1, "", this),
            new Among ( "erias", 50, 1, "", this),
            new Among ( "irias", 50, 1, "", this),
            new Among ( "aras", 47, 1, "", this),
            new Among ( "eras", 47, 1, "", this),
            new Among ( "iras", 47, 1, "", this),
            new Among ( "avas", 47, 1, "", this),
            new Among ( "es", -1, 1, "", this),
            new Among ( "ardes", 58, 1, "", this),
            new Among ( "erdes", 58, 1, "", this),
            new Among ( "irdes", 58, 1, "", this),
            new Among ( "ares", 58, 1, "", this),
            new Among ( "eres", 58, 1, "", this),
            new Among ( "ires", 58, 1, "", this),
            new Among ( "asses", 58, 1, "", this),
            new Among ( "esses", 58, 1, "", this),
            new Among ( "isses", 58, 1, "", this),
            new Among ( "astes", 58, 1, "", this),
            new Among ( "estes", 58, 1, "", this),
            new Among ( "istes", 58, 1, "", this),
            new Among ( "is", -1, 1, "", this),
            new Among ( "ais", 71, 1, "", this),
            new Among ( "eis", 71, 1, "", this),
            new Among ( "areis", 73, 1, "", this),
            new Among ( "ereis", 73, 1, "", this),
            new Among ( "ireis", 73, 1, "", this),
            new Among ( "\u00E1reis", 73, 1, "", this),
            new Among ( "\u00E9reis", 73, 1, "", this),
            new Among ( "\u00EDreis", 73, 1, "", this),
            new Among ( "\u00E1sseis", 73, 1, "", this),
            new Among ( "\u00E9sseis", 73, 1, "", this),
            new Among ( "\u00EDsseis", 73, 1, "", this),
            new Among ( "\u00E1veis", 73, 1, "", this),
            new Among ( "\u00EDeis", 73, 1, "", this),
            new Among ( "ar\u00EDeis", 84, 1, "", this),
            new Among ( "er\u00EDeis", 84, 1, "", this),
            new Among ( "ir\u00EDeis", 84, 1, "", this),
            new Among ( "ados", -1, 1, "", this),
            new Among ( "idos", -1, 1, "", this),
            new Among ( "amos", -1, 1, "", this),
            new Among ( "\u00E1ramos", 90, 1, "", this),
            new Among ( "\u00E9ramos", 90, 1, "", this),
            new Among ( "\u00EDramos", 90, 1, "", this),
            new Among ( "\u00E1vamos", 90, 1, "", this),
            new Among ( "\u00EDamos", 90, 1, "", this),
            new Among ( "ar\u00EDamos", 95, 1, "", this),
            new Among ( "er\u00EDamos", 95, 1, "", this),
            new Among ( "ir\u00EDamos", 95, 1, "", this),
            new Among ( "emos", -1, 1, "", this),
            new Among ( "aremos", 99, 1, "", this),
            new Among ( "eremos", 99, 1, "", this),
            new Among ( "iremos", 99, 1, "", this),
            new Among ( "\u00E1ssemos", 99, 1, "", this),
            new Among ( "\u00EAssemos", 99, 1, "", this),
            new Among ( "\u00EDssemos", 99, 1, "", this),
            new Among ( "imos", -1, 1, "", this),
            new Among ( "armos", -1, 1, "", this),
            new Among ( "ermos", -1, 1, "", this),
            new Among ( "irmos", -1, 1, "", this),
            new Among ( "\u00E1mos", -1, 1, "", this),
            new Among ( "ar\u00E1s", -1, 1, "", this),
            new Among ( "er\u00E1s", -1, 1, "", this),
            new Among ( "ir\u00E1s", -1, 1, "", this),
            new Among ( "eu", -1, 1, "", this),
            new Among ( "iu", -1, 1, "", this),
            new Among ( "ou", -1, 1, "", this),
            new Among ( "ar\u00E1", -1, 1, "", this),
            new Among ( "er\u00E1", -1, 1, "", this),
            new Among ( "ir\u00E1", -1, 1, "", this)
        };
        private Among a_7[] = {
            new Among ( "a", -1, 1, "", this),
            new Among ( "i", -1, 1, "", this),
            new Among ( "o", -1, 1, "", this),
            new Among ( "os", -1, 1, "", this),
            new Among ( "\u00E1", -1, 1, "", this),
            new Among ( "\u00ED", -1, 1, "", this),
            new Among ( "\u00F3", -1, 1, "", this)
        };
        private Among a_8[] = {
            new Among ( "e", -1, 1, "", this),
            new Among ( "\u00E7", -1, 2, "", this),
            new Among ( "\u00E9", -1, 1, "", this),
            new Among ( "\u00EA", -1, 1, "", this)
        };
        private static final char g_v[] = {17, 65, 16, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 3, 19, 12, 2 };
        private int I_p2;
        private int I_p1;
        private int I_pV;
        private void copy_from(PortugueseStemmer other) {
            I_p2 = other.I_p2;
            I_p1 = other.I_p1;
            I_pV = other.I_pV;
            super.copy_from(other);
        }
        private boolean r_prelude() {
            int among_var;
            int v_1;
            replab0: while(true)
            {
                v_1 = cursor;
                lab1: do {
                    bra = cursor;
                    among_var = find_among(a_0, 3);
                    if (among_var == 0)
                    {
                        break lab1;
                    }
                    ket = cursor;
                    switch(among_var) {
                        case 0:
                            break lab1;
                        case 1:
                            slice_from("a~");
                            break;
                        case 2:
                            slice_from("o~");
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
                        if (!(in_grouping(g_v, 97, 250)))
                        {
                            break lab2;
                        }
                        lab3: do {
                            v_3 = cursor;
                            lab4: do {
                                if (!(out_grouping(g_v, 97, 250)))
                                {
                                    break lab4;
                                }
                                golab5: while(true)
                                {
                                    lab6: do {
                                        if (!(in_grouping(g_v, 97, 250)))
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
                            if (!(in_grouping(g_v, 97, 250)))
                            {
                                break lab2;
                            }
                            golab7: while(true)
                            {
                                lab8: do {
                                    if (!(out_grouping(g_v, 97, 250)))
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
                    if (!(out_grouping(g_v, 97, 250)))
                    {
                        break lab0;
                    }
                    lab9: do {
                        v_6 = cursor;
                        lab10: do {
                            if (!(out_grouping(g_v, 97, 250)))
                            {
                                break lab10;
                            }
                            golab11: while(true)
                            {
                                lab12: do {
                                    if (!(in_grouping(g_v, 97, 250)))
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
                        if (!(in_grouping(g_v, 97, 250)))
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
                        if (!(in_grouping(g_v, 97, 250)))
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
                        if (!(out_grouping(g_v, 97, 250)))
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
                        if (!(in_grouping(g_v, 97, 250)))
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
                        if (!(out_grouping(g_v, 97, 250)))
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
                            slice_from("\u00E3");
                            break;
                        case 2:
                            slice_from("\u00F5");
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
        private boolean r_standard_suffix() {
            int among_var;
            int v_1;
            int v_2;
            int v_3;
            int v_4;
            ket = cursor;
            among_var = find_among_b(a_5, 45);
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
                    slice_from("log");
                    break;
                case 3:
                    if (!r_R2())
                    {
                        return false;
                    }
                    slice_from("u");
                    break;
                case 4:
                    if (!r_R2())
                    {
                        return false;
                    }
                    slice_from("ente");
                    break;
                case 5:
                    if (!r_R1())
                    {
                        return false;
                    }
                    slice_del();
                    v_1 = limit - cursor;
                    lab0: do {
                        ket = cursor;
                        among_var = find_among_b(a_2, 4);
                        if (among_var == 0)
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
                        switch(among_var) {
                            case 0:
                                cursor = limit - v_1;
                                break lab0;
                            case 1:
                                ket = cursor;
                                if (!(eq_s_b(2, "at")))
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
                                break;
                        }
                    } while (false);
                    break;
                case 6:
                    if (!r_R2())
                    {
                        return false;
                    }
                    slice_del();
                    v_2 = limit - cursor;
                    lab1: do {
                        ket = cursor;
                        among_var = find_among_b(a_3, 3);
                        if (among_var == 0)
                        {
                            cursor = limit - v_2;
                            break lab1;
                        }
                        bra = cursor;
                        switch(among_var) {
                            case 0:
                                cursor = limit - v_2;
                                break lab1;
                            case 1:
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
                        if (!(eq_s_b(2, "at")))
                        {
                            cursor = limit - v_4;
                            break lab3;
                        }
                        bra = cursor;
                        if (!r_R2())
                        {
                            cursor = limit - v_4;
                            break lab3;
                        }
                        slice_del();
                    } while (false);
                    break;
                case 9:
                    if (!r_RV())
                    {
                        return false;
                    }
                    if (!(eq_s_b(1, "e")))
                    {
                        return false;
                    }
                    slice_from("ir");
                    break;
            }
            return true;
        }
        private boolean r_verb_suffix() {
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
            among_var = find_among_b(a_6, 120);
            if (among_var == 0)
            {
                limit_backward = v_2;
                return false;
            }
            bra = cursor;
            switch(among_var) {
                case 0:
                    limit_backward = v_2;
                    return false;
                case 1:
                    slice_del();
                    break;
            }
            limit_backward = v_2;
            return true;
        }
        private boolean r_residual_suffix() {
            int among_var;
            ket = cursor;
            among_var = find_among_b(a_7, 7);
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
            }
            return true;
        }
        private boolean r_residual_form() {
            int among_var;
            int v_1;
            int v_2;
            int v_3;
            ket = cursor;
            among_var = find_among_b(a_8, 4);
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
                    ket = cursor;
                    lab0: do {
                        v_1 = limit - cursor;
                        lab1: do {
                            if (!(eq_s_b(1, "u")))
                            {
                                break lab1;
                            }
                            bra = cursor;
                            v_2 = limit - cursor;
                            if (!(eq_s_b(1, "g")))
                            {
                                break lab1;
                            }
                            cursor = limit - v_2;
                            break lab0;
                        } while (false);
                        cursor = limit - v_1;
                        if (!(eq_s_b(1, "i")))
                        {
                            return false;
                        }
                        bra = cursor;
                        v_3 = limit - cursor;
                        if (!(eq_s_b(1, "c")))
                        {
                            return false;
                        }
                        cursor = limit - v_3;
                    } while (false);
                    if (!r_RV())
                    {
                        return false;
                    }
                    slice_del();
                    break;
                case 2:
                    slice_from("c");
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
                lab3: do {
                    v_4 = limit - cursor;
                    lab4: do {
                        v_5 = limit - cursor;
                        lab5: do {
                            v_6 = limit - cursor;
                            lab6: do {
                                if (!r_standard_suffix())
                                {
                                    break lab6;
                                }
                                break lab5;
                            } while (false);
                            cursor = limit - v_6;
                            if (!r_verb_suffix())
                            {
                                break lab4;
                            }
                        } while (false);
                        cursor = limit - v_5;
                        v_7 = limit - cursor;
                        lab7: do {
                            ket = cursor;
                            if (!(eq_s_b(1, "i")))
                            {
                                break lab7;
                            }
                            bra = cursor;
                            v_8 = limit - cursor;
                            if (!(eq_s_b(1, "c")))
                            {
                                break lab7;
                            }
                            cursor = limit - v_8;
                            if (!r_RV())
                            {
                                break lab7;
                            }
                            slice_del();
                        } while (false);
                        cursor = limit - v_7;
                        break lab3;
                    } while (false);
                    cursor = limit - v_4;
                    if (!r_residual_suffix())
                    {
                        break lab2;
                    }
                } while (false);
            } while (false);
            cursor = limit - v_3;
            v_9 = limit - cursor;
            lab8: do {
                if (!r_residual_form())
                {
                    break lab8;
                }
            } while (false);
            cursor = limit - v_9;
            cursor = limit_backward;            
            v_10 = cursor;
            lab9: do {
                if (!r_postlude())
                {
                    break lab9;
                }
            } while (false);
            cursor = v_10;
            return true;
        }
}

package org.apache.cassandra.cli;
import java.util.LinkedHashSet;
import java.util.Set;
import org.apache.cassandra.thrift.CfDef;
import org.apache.cassandra.thrift.IndexOperator;
import org.apache.cassandra.thrift.KsDef;
public class CliUtils
{
    public static String unescapeSQLString(String b)
    {
        int j = 1;
        final char start = b.charAt(0);
        final char end = b.charAt(b.length() - 1);
        if (start != '\'' && end != '\'')
        {
            j = 0;
        }
        StringBuilder sb = new StringBuilder(b.length());
        for (int i = j; ((j == 0) ? i : i + 1) < b.length(); i++)
        {
            if (b.charAt(i) == '\\' && i + 2 < b.length())
            {
                char n = b.charAt(i + 1);
                switch (n)
                {
                    case '0':
                        sb.append("\0");
                        break;
                    case '\'':
                        sb.append("'");
                        break;
                    case '"':
                        sb.append("\"");
                        break;
                    case 'b':
                        sb.append("\b");
                        break;
                    case 'n':
                        sb.append("\n");
                        break;
                    case 'r':
                        sb.append("\r");
                        break;
                    case 't':
                        sb.append("\t");
                        break;
                    case 'Z':
                        sb.append("\u001A");
                        break;
                    case '\\':
                        sb.append("\\");
                        break;
                    case '%':
                        sb.append("%");
                        break;
                    case '_':
                        sb.append("_");
                        break;
                    default:
                        sb.append(n);
                }
            }
            else
            {
                sb.append(b.charAt(i));
            }
        }
        return sb.toString();
    }
    public static IndexOperator getIndexOperator(String operator)
    {
        if (operator.equals("="))
        {
            return IndexOperator.EQ;
        }
        else if (operator.equals(">="))
        {
            return IndexOperator.GTE;
        }
        else if (operator.equals(">"))
        {
            return IndexOperator.GT;
        }
        else if (operator.equals("<"))
        {
            return IndexOperator.LT;
        }
        else if (operator.equals("<="))
        {
            return IndexOperator.LTE;
        }
        return null;
    }
    public static Set<String> getCfNamesByKeySpace(KsDef keySpace)
    {
        Set<String> names = new LinkedHashSet<String>();
        for (CfDef cfDef : keySpace.getCf_defs())
        {
            names.add(cfDef.getName());
        }
        return names;
    }
}

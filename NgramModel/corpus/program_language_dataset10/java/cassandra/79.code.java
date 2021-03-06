package org.apache.cassandra.cli;
import java.util.List;
import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CharStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.tree.Tree;
import org.apache.cassandra.thrift.CfDef;
import org.apache.cassandra.thrift.KsDef;
public class CliCompiler
{
    public static class ANTLRNoCaseStringStream  extends ANTLRStringStream
    {
        public ANTLRNoCaseStringStream(String input)
        {
            super(input);
        }
        public int LA(int i)
        {
            int returnChar = super.LA(i);
            if (returnChar == CharStream.EOF)
            {
                return returnChar; 
            }
            else if (returnChar == 0) 
            {
                return returnChar;
            }
            return Character.toUpperCase((char)returnChar);
        }
    }
    public static Tree compileQuery(String query)
    {
        Tree queryTree;
        try
        {
            ANTLRStringStream input = new ANTLRNoCaseStringStream(query);
            CliLexer lexer = new CliLexer(input);
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            CliParser parser = new CliParser(tokens);
            queryTree = (Tree)(parser.root().getTree());
        }
        catch(Exception e)
        {
            throw new RuntimeException(e.getMessage(), e);
        }
        return queryTree;
    }
    public static String getColumnFamily(Tree astNode, List<CfDef> cfDefs)
    {
        return getColumnFamily(astNode.getChild(0).getText(), cfDefs);
    }
    public static String getColumnFamily(String cfName, List<CfDef> cfDefs)
    {
        int matches = 0;
        String lastMatchedName = "";
        for (CfDef cfDef : cfDefs)
        {
            if (cfDef.name.equals(cfName))
            {
                return cfName;
            }
            else if (cfDef.name.toUpperCase().equals(cfName.toUpperCase()))
            {
                lastMatchedName = cfDef.name;
                matches++;
            }
        }
        if (matches > 1 || matches == 0)
            throw new RuntimeException(cfName + " not found in current keyspace.");
        return lastMatchedName;
    }
    public static String getKeySpace(Tree statement, List<KsDef> keyspaces)
    {
        return getKeySpace(statement.getChild(0).getText(), keyspaces);
    }
    public static String getKeySpace(String ksName, List<KsDef> keyspaces)
    {
        int matches = 0;
        String lastMatchedName = "";
        for (KsDef ksDef : keyspaces)
        {
            if (ksDef.name.equals(ksName))
            {
                return ksName;
            }
            else if (ksDef.name.toUpperCase().equals(ksName.toUpperCase()))
            {
                lastMatchedName = ksDef.name;
                matches++;
            }
        }
        if (matches > 1 || matches == 0)
            throw new RuntimeException("Keyspace '" + ksName + "' not found.");
        return lastMatchedName;
    }
    public static String getKey(Tree astNode)
    {
        return CliUtils.unescapeSQLString(astNode.getChild(1).getText());
    }
    public static int numColumnSpecifiers(Tree astNode)
    {
        return astNode.getChildCount() - 2;
    }
    public static String getColumn(Tree astNode, int pos)
    {
        return CliUtils.unescapeSQLString(astNode.getChild(pos + 2).getText()); 
    }
}

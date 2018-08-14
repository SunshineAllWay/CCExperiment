package parseunit;

import java.util.ArrayList;

public class MethodContextParser {
    public MyMethodNode node;
    public ArrayList<String> context;

    public MethodContextParser(MyMethodNode pnode) {
        node = pnode;
        context = new ArrayList<>();
        parse();
    }

    public void parse() {
        //TODO
        String str = node.toString();
        int len = str.length();
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < len; i++) {
            char ch = str.charAt(i);
            if (ch != '\r' && ch != '.' && ch != '\t' && ch != ' ' && ch != ',' && ch != '(' && ch != ')' && ch != '{' && ch != '}' && ch != ';' && ch != '[' && ch != ']' && ch != '\n' && ch != '.') {
                sb.append(ch);
            } else {
                context.add(sb.toString());
                sb = new StringBuilder();
            }
        }
    }
}

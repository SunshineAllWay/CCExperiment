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
        String[] contextArray = node.toString().split(" ");
        for (int i = 0; i < contextArray.length; i++) {
            context.add(contextArray[i]);
        }
    }
}

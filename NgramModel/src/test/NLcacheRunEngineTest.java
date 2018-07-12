package test;

import engine.NLcacheRunEngine;
import engine.NLngramRunEngine;

import java.io.File;
import java.util.ArrayList;
import java.util.Optional;

public class NLcacheRunEngineTest {
    public static void main(String[] args) {
        File currentFile = new File("corpus\\natural_language_dataset3\\writing1.txt");
        NLcacheRunEngine<Character> runtest = new NLcacheRunEngine<>(3, 1000, currentFile);
        runtest.run();
        ArrayList<Character> ls = runtest.completePostToken();

        if (ls.size() != 0) {
            for (int i = 0; i < ls.size(); i++) {
                System.out.println(ls.get(i));
            }
        } else {
            System.out.println("miss value");
        }
    }
}

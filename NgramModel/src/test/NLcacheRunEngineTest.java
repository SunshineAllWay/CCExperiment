package test;

import engine.NLcacheRunEngine;
import engine.NLngramRunEngine;

import java.io.File;
import java.util.Optional;

public class NLcacheRunEngineTest {
    public static void main(String[] args) {
        File currentFile = new File("corpus\\natural_language_dataset3\\writing.txt");
        NLcacheRunEngine<Character> runtest = new NLcacheRunEngine<>(3, 0, currentFile);
        runtest.run();
        Optional<Character> optoken = runtest.completePostToken();

        if (optoken.isPresent()) {
            System.out.println(optoken.get());
        } else {
            System.out.println("miss value");
        }
    }
}

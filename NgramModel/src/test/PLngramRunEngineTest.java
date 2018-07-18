package test;

import engine.NgramRunEngine;

public class PLngramRunEngineTest {
    public static void main(String[] args) {
        NgramRunEngine<Character> runtest = new NgramRunEngine<>(1, 5, 0.5);
        runtest.run();
        System.out.println(runtest.getTrainingTokenList().size());
        //testRun(runtest);
        //evaluateRun(runtest);
        //testContextSearcher(runtest);
    }
}

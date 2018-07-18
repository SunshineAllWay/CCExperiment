package test;

import engine.NgramRunEngine;

public class PLngramRunEngineTest {
    public static void main(String[] args) {
        NgramRunEngine runtest = new NgramRunEngine(1, 3, 0.5);
        runtest.run();
        System.out.println(runtest.getTrainingTokenList().size());
        //testRun(runtest);
        //evaluateRun(runtest);
        //testContextSearcher(runtest);
    }
}

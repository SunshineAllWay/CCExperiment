package test;

import java.util.ArrayList;
import engine.NLngramRunEngine;
import searchunit.BFContextSearcher;
import tokenunit.Tokensequence;

/**
 * @author HHeart
 * Test model
 */

class NLngramRunEngineTest {
    public static void testRun(NLngramRunEngine<Character> runtest) {

        /*********************************** Test case 1 **************************************/
        //井井有条
        ArrayList<Character> query = new ArrayList<>();
        query.add('井');
        query.add('井');
        query.add('有');

        Tokensequence<Character> queryseq = new Tokensequence<>(query);
        System.out.println(query);
        ArrayList<Character> inferredWord = runtest.completePostToken(queryseq);
        if (inferredWord.size() != 0) {
            for (int i = 0; i < inferredWord.size(); i++) {
                System.out.println(inferredWord.get(i));
            }
        } else {
            System.out.println("miss value");
        }

        double prob = runtest.calculateProbability(new Tokensequence<>(query));
        System.out.println(prob);
        System.out.println("Finish");
        System.out.println();
    }

    public static void evaluateRun(NLngramRunEngine<Character> runtest) {
        runtest.evaluateModel();
        System.out.println("Likelihood:");
        for (int i = 0; i < runtest.likelihood.size(); i++) {
            System.out.print("n = ");
            System.out.println(i);
            System.out.println(runtest.likelihood.get(i));
        }

        System.out.println("Perplexity:");
        for (int i = 0; i < runtest.perplexity.size(); i++) {
            System.out.print("n = ");
            System.out.println(i);
            System.out.println(runtest.perplexity.get(i));
        }
    }

    public static void testContextSearcher(NLngramRunEngine<Character> runtest) {
        BFContextSearcher<Character> fuzzySearcher = new BFContextSearcher<>(runtest);
        ArrayList<Character> query = new ArrayList<>();
        query.add('井');
        query.add('井');
        query.add('有');

        Tokensequence<Character> queryseq = new Tokensequence<>(query);
        ArrayList<Tokensequence<Character>> searchResult = fuzzySearcher.getSimilarSequences(queryseq);

        for (int i = 0; i < searchResult.size(); i++) {
            System.out.print(searchResult.get(i));
            System.out.print("    ");
            System.out.println(searchResult.get(i).length());
        }
        System.out.println(searchResult.size());
    }

    public static void main(String[] args) {
        NLngramRunEngine<Character> runtest = new NLngramRunEngine<>(5, 0.5);
        runtest.run();
        //testRun(runtest);
        evaluateRun(runtest);
        //testContextSearcher(runtest);
    }
}
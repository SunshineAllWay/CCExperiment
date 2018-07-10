package test;

import java.util.ArrayList;
import java.util.Optional;

import engine.NLngramRunEngine;
import tokenunit.Tokensequence;

/**
 * @author HHeart
 * Test model
 */

class NLngramRunEngineTest {
    public static void testRun(NLngramRunEngine<Character> runtest) {

        /*********************************** Test case 1 **************************************/
        //蓬头垢面
        ArrayList<Character> query = new ArrayList<>();
        query.add('蓬');
        query.add('头');
        query.add('垢');

        Tokensequence<Character> queryseq = new Tokensequence<>(query);
        System.out.println(query);
        Optional<Character> inferredWord = runtest.completePostToken(queryseq);
        if (inferredWord.isPresent()) {
            System.out.println(inferredWord.get());
        } else {
            System.out.println("miss value");
        }

        double prob = runtest.calculateProbability(new Tokensequence<>(query));
        System.out.println(prob);
        System.out.println("Finish");
        System.out.println();

        /*********************************** Test case 2 **************************************/
        //
        query = new ArrayList<>();
        query.add('床');
        query.add('小');
        query.add('的');
        query.add('可');

        queryseq = new Tokensequence<>(query);
        System.out.println(query);
        inferredWord = runtest.completePostToken(queryseq);
        if (inferredWord.isPresent()) {
            System.out.println(inferredWord.get());
        } else {
            System.out.println("miss value");
        }

        prob = runtest.calculateProbability(new Tokensequence<>(query));
        System.out.println(prob);
        System.out.println("Finish");

        /*********************************** Test case 3 **************************************/
        //房间很整洁
        query = new ArrayList<>();
        query.add('房');
        query.add('间');
        query.add('很');
        query.add('整');

        queryseq = new Tokensequence<>(query);
        System.out.println(query);
        inferredWord = runtest.completePostToken(queryseq);
        if (inferredWord.isPresent()) {
            System.out.println(inferredWord.get());
        } else {
            System.out.println("miss value");
        }

        prob = runtest.calculateProbability(new Tokensequence<>(query));
        System.out.println(prob);
        System.out.println("Finish");
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

    public static void main(String[] args) {
        NLngramRunEngine<Character> runtest = new NLngramRunEngine<>(3, 0.8);
        runtest.run();
        testRun(runtest);
        evaluateRun(runtest);
    }
}
package test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;

import engine.NLngramRunEngine;
import tokenunit.Tokensequence;

/**
 * @author HHeart
 * Test model
 */

public class Test {
    
    public static void main(String[] args) {
    	NLngramRunEngine<Character> runtest = new NLngramRunEngine<>(5, 0, 0.5);
    	runtest.run();
    	
    	ArrayList<Character> query1 = new ArrayList<>();
    	query1.add('a');
    	query1.add('b');

		Tokensequence<Character> queryseq = new Tokensequence<>(query1);
		Optional<Character> inferredWord = runtest.completePostToken(queryseq);
		if (inferredWord.isPresent()) {
			System.out.println(inferredWord.get());
		} else {
			System.out.println("miss value");
		}

		System.out.println(query1);
    	double prob = runtest.calculateProbability(new Tokensequence<>(query1));
		System.out.println(prob);
    	System.out.println("Finish");

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
}
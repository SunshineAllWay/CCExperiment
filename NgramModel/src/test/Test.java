package test;

import java.util.ArrayList;
import java.util.Optional;

import engine.NLngramRunEngine;
import tokenunit.Tokensequence;

/**
 * @author HHeart
 * Test model
 */

public class Test {
    
    public static void main(String[] args) {
    	NLngramRunEngine<Character> runtest = new NLngramRunEngine<Character>(5, 0, 0.8);
    	runtest.run();
    	
    	ArrayList<Character> query = new ArrayList<Character>();
    	query.add('德');
    	query.add('国');
    	
    	Tokensequence<Character> queryseq = new Tokensequence<Character>(query);
    	double prob = runtest.calculateProbability(new Tokensequence<Character>(query));
		System.out.print(prob);
		System.out.println("%");

    	Optional<Character> inferedWord = runtest.completePostToken(queryseq);
    	if (inferedWord.isPresent()) {
    		System.out.println(inferedWord.get());
    	} else {
    		System.out.println("miss value");
    	}
    	System.out.println("Finish");

    	System.out.println("Perplexity:");
    	for (int i = 0; i < runtest.perplexity.size(); i++) {
            System.out.println(runtest.perplexity.get(i));
        }

        System.out.println("Likelihood:");
        for (int i = 0; i < runtest.likelihood.size(); i++) {
            System.out.println(runtest.likelihood.get(i));
        }
    }
}
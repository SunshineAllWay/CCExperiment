package test;


import app.NLngramRunApp;
import tokenunit.Tokensequence;

import java.io.File;
import java.util.ArrayList;
import java.util.Optional;

public class NLngramRunAppTest {
    public static void main(String[] args) {
        NLngramRunApp<Character> app = new NLngramRunApp<>(3);
        ArrayList<Character> query = new ArrayList<>();
//        query.add('房');
//        query.add('屋');
//        query.add('很');
//        query.add('清');

//        query.add('房');
//        query.add('间');
//        query.add('很');
//        query.add('整');

//        query.add('房');
//        query.add('屋');
//        query.add('挺');
//        query.add('清');

//        query.add('区');
//        query.add('域');
//        query.add('很');
        query.add('整');

        Tokensequence<Character> queryseq = new Tokensequence<>(query);
        ArrayList<Character> tokenCandidatesList =  app.completePostToken(queryseq);

        if (tokenCandidatesList.size() > 0) {
            for (int i = 0; i < tokenCandidatesList.size(); i++) {
                System.out.println(tokenCandidatesList.get(i));
            }
        } else {
            System.out.println("miss value");
        }
    }
}

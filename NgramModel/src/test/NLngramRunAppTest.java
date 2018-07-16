package test;

import app.NLngramRunApp;
import tokenunit.Tokensequence;
import java.util.ArrayList;

public class NLngramRunAppTest {
    public static void main(String[] args) {
        NLngramRunApp<Character> app = new NLngramRunApp<>(3);
        ArrayList<Character> query = new ArrayList<>();
        query.add('井');
        query.add('井');
        query.add('有');

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

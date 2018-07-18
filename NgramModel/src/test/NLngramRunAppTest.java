package test;

import app.NgramRunApp;
import tokenunit.Tokensequence;
import java.util.ArrayList;

public class NLngramRunAppTest {
    public static void main(String[] args) {
        NgramRunApp<Character> app = new NgramRunApp<>(0, 3);
        ArrayList<Character> query = new ArrayList<>();
        query.add('二');
        query.add('新');
        query.add('水');

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

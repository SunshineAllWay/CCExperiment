package test;

import app.NLcacheRunApp;
import tokenunit.Tokensequence;

import java.io.File;
import java.util.ArrayList;

public class NLcacheRunAppTest {
    public static void main(String[] args) {
        File currentFile = new File("corpus\\natural_language_dataset3\\writing2.txt");
        NLcacheRunApp<Character> app = new NLcacheRunApp(3, 0, currentFile);
        ArrayList<Character> tokenCandidatesList = app.completePostToken();

        if (tokenCandidatesList.size() > 0) {
            for (int i = 0; i < tokenCandidatesList.size(); i++) {
                System.out.println(tokenCandidatesList.get(i));
            }
        } else {
            System.out.println("miss value");
        }
    }
}

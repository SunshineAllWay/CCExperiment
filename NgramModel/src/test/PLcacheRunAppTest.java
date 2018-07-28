package test;

import app.CacheRunApp;

import java.io.File;
import java.util.ArrayList;

public class PLcacheRunAppTest {
    public static void main(String[] args) {
        File currentFile = new File("corpus\\program_language_dataset7\\tmp.txt");
        CacheRunApp app = new CacheRunApp(1, 3, 0, currentFile);
        ArrayList<String> tokenCandidatesList = app.completePostToken();

        System.out.println("ANSWER:");
        if (tokenCandidatesList.size() > 0) {
            for (int i = 0; i < Math.min(10, tokenCandidatesList.size()); i++) {
                System.out.println(tokenCandidatesList.get(i));
            }
        } else {
            System.out.println("miss value");
        }
    }
}

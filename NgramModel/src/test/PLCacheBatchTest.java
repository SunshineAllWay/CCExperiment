package test;

import app.CacheRunApp;
import engine.CacheRunEngine;

import java.io.*;
import java.util.ArrayList;

public class PLCacheBatchTest{
    public static int top3 = 0;
    public static int top5 = 0;
    public static int top10 = 0;
    public static int count = 0;

    public static double top3rate = 0.0;
    public static double top5rate = 0.0;
    public static double top10rate = 0.0;
    public static double MRR = 0.0;


    public static String filterSpecificCharacter(String str) {
        char[] charr = str.toCharArray();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < charr.length; i++) {
            char ch = charr[i];
            if (ch != '\r' && ch != '\t' && ch != ' ' && ch != '.' && ch != '(' && ch != ')' && ch != '{' && ch != '}' && ch != ';' && ch != '[' && ch != ']' && ch != '\n') {
                sb.append(ch);
            } else {
                sb.append(' ');
            }
        }
        return sb.toString();
    }

    public static void evaluateCandidateList(ArrayList<String> candidatesList, String answer) {
        int rank = 0;
        count++;
        while (rank < candidatesList.size()) {
            if (candidatesList.get(rank).equals(answer)) {
                if (rank <= 3) {
                    top3++;
                }
                if (rank <= 5) {
                    top5++;
                }
                if (rank <= 10) {
                    top10++;
                }
                MRR += 1.0 / (rank + 1);
                break;
            }
            rank++;
        }
    }

    public static void singleTestCacheApp(CacheRunApp app, File sourceFile, File currentFile) {
        try {
            FileReader reader = new FileReader(sourceFile);
            BufferedReader br = new BufferedReader(reader);
            String str = null;

            while((str = br.readLine()) != null) {
                String strAfterFilter = filterSpecificCharacter(str).trim();
                String[] strArr = strAfterFilter.split(" ");
                for (int i = 0; i < strArr.length - 1; i++) {
                    FileWriter writer = new FileWriter(currentFile, true);
                    BufferedWriter bw = new BufferedWriter(writer);
                    bw.write(strArr[i]);
                    bw.write(" ");
                    bw.close();
                    writer.close();
                    if (i < 3) {
                        ArrayList<String> candidiateList = app.completePostToken();
                        evaluateCandidateList(candidiateList, strArr[i + 1]);

                        System.out.println();
                        System.out.println("-----------------------");
                        System.out.print("Count: ");
                        System.out.println(count);
                        System.out.print("TOP 3: ");
                        System.out.println(top3);
                        System.out.print("TOP 5: ");
                        System.out.println(top5);
                        System.out.print("TOP 10: ");
                        System.out.println(top10);
                        System.out.println("-----------------------");
                        System.out.println();
                    }
                }
                FileWriter writer = new FileWriter(currentFile, true);
                BufferedWriter bw = new BufferedWriter(writer);
                bw.write(strArr[strArr.length - 1]);
                bw.write(" ");
                bw.close();
                writer.close();

            }
            br.close();
            reader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        app.getRunEngine().addCacheFileList(sourceFile);
    }


    public static void main(String[] args) {
        File testDir = new File("corpus\\program_language_dataset6");
        File[] files = testDir.listFiles();
        File currentFile = new File("corpus\\program_language_dataset7\\tmp.txt");
        if (currentFile.exists()) {
            currentFile.delete();
        }

        try {
            currentFile.createNewFile();
            CacheRunApp app = new CacheRunApp(1, 3, 0, currentFile);

            for (int i = 0; i < files.length; i++) {
                singleTestCacheApp(app, files[i], currentFile);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        top3rate = top3 * 1.0 / count;
        top5rate = top5 * 1.0 / count;
        top10rate = top10 * 1.0 / count;
        MRR /= count;

        System.out.print("top3: ");
        System.out.println(top3rate);

        System.out.print("top5: ");
        System.out.println(top5rate);

        System.out.print("top10: ");
        System.out.println(top10rate);

        System.out.print("MRR: ");
        System.out.println(MRR);
    }
}

package test;

import app.CacheRunApp;
import iounit.CorpusImporter;

import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;

public class PLCacheBatchTest{
    public static int top1 = 0;
    public static int top3 = 0;
    public static int top5 = 0;
    public static int top10 = 0;
    public static int count = 0;

    public static double top1rate = 0.0;
    public static double top3rate = 0.0;
    public static double top5rate = 0.0;
    public static double top10rate = 0.0;
    public static double MRR = 0.0;
    public static HashSet<Integer> completeIndexSet = new HashSet<>();
    public static ArrayList<String> tokenList = new ArrayList<>();


    public static void filterSpecificCharacter(String str) {
        char[] charr = str.toCharArray();
        StringBuilder sb = new StringBuilder();
        tokenList = new ArrayList<>();
        completeIndexSet = new HashSet<>();

        for (int i = 0; i < charr.length; i++) {
            char ch = charr[i];
            if (ch != '\r' && ch != '.' && ch != '\t' && ch != ' ' && ch != ',' && ch != '(' && ch != ')' && ch != '{' && ch != '}' && ch != ';' && ch != '[' && ch != ']' && ch != '\n') {
                sb.append(ch);
            } else {
                tokenList.add(sb.toString().trim());
                sb = new StringBuilder();
            }
            if (ch == '.') {
                completeIndexSet.add(tokenList.size() - 1);
            }
        }
    }

    public static void  evaluateCandidateList(ArrayList<String> candidatesList, String answer) {
        int rank = 0;
        count++;
        while (rank < candidatesList.size()) {
            if (candidatesList.get(rank).equals(answer)) {
                if (rank == 0) {
                    top1++;
                }
                if (rank < 3) {
                    top3++;
                }
                if (rank < 5) {
                    top5++;
                }
                if (rank < 10) {
                    top10++;
                }
                MRR += 1.0 / (rank + 1);
                break;
            }
            rank++;
        }
    }


    public static void copyFileContentRemove(File editFile, File tmpFile) {
        try {
            FileReader reader = new FileReader(editFile);
            BufferedReader br = new BufferedReader(reader);
            String str1 = null;
            String str2 = null;
            FileWriter writer = new FileWriter(tmpFile);
            writer.write("");
            writer.flush();
            writer.close();

            str1 = br.readLine();
            while(str1 != null) {
                str2 = br.readLine();
                if (str2 == null) {
                    for (int i = 0; i < str1.length(); i++) {
                        if (i == str1.length() - 1 && str1.charAt(i) == '}') {
                            break;
                        } else {
                            writer = new FileWriter(tmpFile, true);
                            writer.write(str1.charAt(i));
                            writer.flush();
                            writer.close();
                        }
                    }
                    break;
                } else {
                    writer = new FileWriter(tmpFile, true);
                    writer.write(str1);
                    writer.flush();
                    writer.close();
                    str1 = str2;
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void copyFileContentAppend(File editFile, File tmpFile) {
        try {
            FileReader reader = new FileReader(tmpFile);
            BufferedReader br = new BufferedReader(reader);

            FileWriter writer = new FileWriter(editFile);
            writer.write("");
            writer.flush();
            writer.close();

            String str = null;
            while((str = br.readLine()) != null) {
                writer = new FileWriter(editFile, true);
                writer.write(str);
                writer.flush();
                writer.close();
            }

            writer = new FileWriter(editFile, true);
            writer.write("}");
            writer.flush();
            writer.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void singleTestCacheApp(CacheRunApp app, File sourceFile, File currentFile) {
        try {
            FileReader reader = new FileReader(sourceFile);
            BufferedReader br = new BufferedReader(reader);
            String str = null;
            FileWriter writer;

            while((str = br.readLine()) != null) {
                filterSpecificCharacter(str.trim());

                for (int i = 0; i < tokenList.size() - 1; i++) {
                    File tmpFile = new File("tmpFile.java");

                    copyFileContentRemove(currentFile, tmpFile);
                    writer = new FileWriter(tmpFile, true);
                    writer.write(" " + tokenList.get(i));
                    writer.flush();
                    writer.close();
                    copyFileContentAppend(currentFile, tmpFile);

                    if (completeIndexSet.contains(new Integer(i - 1))) {
                        ArrayList<String> candidiateList = app.completePostToken();
                        evaluateCandidateList(candidiateList, tokenList.get(i));

                        System.out.println();
                        System.out.println("-----------------------");
                        System.out.print("Count: ");
                        System.out.println(count);
                        System.out.print("Prefix: ");
                        System.out.println(tokenList.get(i - 1));
                        System.out.print("Answer: ");
                        System.out.println(tokenList.get(i));

                        for (int j = 0; j < Math.min(3, candidiateList.size()); j++) {
                            System.out.print("List: ");
                            System.out.println(candidiateList.get(j));
                        }

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

                if (tokenList.size() > 0) {
                    writer = new FileWriter(currentFile, true);
                    writer.write(" " + tokenList.get(tokenList.size() - 1));
                    writer.flush();
                    writer.close();
                }
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
        File testDir = new File("corpus\\program_language_dataset8");
        File[] files = testDir.listFiles();
        File currentFile = new File("corpus\\program_language_dataset7\\tmp.txt");
        if (currentFile.exists()) {
            currentFile.delete();
        }

        try {
            currentFile.createNewFile();
            CacheRunApp app = new CacheRunApp(1, 3, 100, currentFile);

            for (int i = 0; i < files.length; i++) {
                singleTestCacheApp(app, files[i], currentFile);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        top1rate = top1 * 1.0 / count;
        top3rate = top3 * 1.0 / count;
        top5rate = top5 * 1.0 / count;
        top10rate = top10 * 1.0 / count;
        MRR /= count;

        System.out.print("top1: ");
        System.out.println(top1rate);

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

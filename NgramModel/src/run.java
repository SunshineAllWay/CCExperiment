import java.io.File;
import java.util.Scanner;
import java.util.ArrayList;
import app.CacheRunApp;

public class run {
    static File currentFile;
    static int exit = 0;

    public static void main(String[] args) {
        currentFile = null;
        Scanner s = new Scanner(System.in);

        System.out.println("==================================================================================================");
        System.out.println("                                        Welcome to CRMAC                                          ");
        System.out.println("                                 This is an API completion tool.                                  ");
        System.out.println("--------------------------------------------------------------------------------------------------");
        System.out.println("-------------------------------------------TUTORIAL-----------------------------------------------");
        System.out.println("COMMAND                |  FUNCTION");
        System.out.println("reload                 |  Reload the current file");
        System.out.println("load FILEPATH          |  Load the current file");
        System.out.println("retrain                |  Retain the cache model");
        System.out.println("complete [-r] [-s]     |  Complete the current file with code relevance mining or fuzzy searching");
        System.out.println("exit                   |  Exit the tool");
        System.out.println("help                   |  Display this tutorial");
        System.out.println("--------------------------------------------------------------------------------------------------");
        System.out.println("==================================================================================================");

        System.out.println("Please load the current file");
        String command = s.nextLine();
        String path = command.substring(command.indexOf(" "));
        setCurrentFile(path);

        trainCacheModel();

        while(exit == 0) {
            command = s.nextLine();
            System.out.println(command);
            if (command.startsWith("help")) {
                printTutorial();
            }
            if (command.startsWith("reload")){
                reloadCurrentFile();
            }
            if (command.startsWith("retrain")){
                trainCacheModel();
            }
            if (command.startsWith("load")) {
                path = command.substring(command.indexOf(" "));
                setCurrentFile(path);
            }
            if (command.startsWith("exit")) {
                setExit();
            }
            if (command.startsWith("complete")) {
                complete((command.indexOf("-r") != -1), (command.indexOf("-s") == -1));
            }
        }

        System.out.println("==================================================================================================");
        System.out.println("                                   THANK YOU FOR YOUR USE                                         ");
        System.out.println("           If you have some advice, contact Chengpeng Wang via stephenw.wangcp@gmail.com          ");
        System.out.println("==================================================================================================");
    }

    public static void printTutorial() {
        System.out.println("-------------------------------------------TUTORIAL-----------------------------------------------");
        System.out.println("COMMAND                |  DESCRIPTION");
        System.out.println("reload                 |  Reload the current file");
        System.out.println("load FILEPATH          |  Load the current file");
        System.out.println("retrain                |  Retain the cache model");
        System.out.println("complete [-r] [-s]     |  Complete the current file with code relevance mining or fuzzy searching");
        System.out.println("exit                   |  Exit the tool");
        System.out.println("help                   |  Display this tutorial");
        System.out.println("--------------------------------------------------------------------------------------------------");
    }

    public static void trainCacheModel() {
        System.out.println("The model is ready");
        System.out.println("--------------------------------------------------------------------------------------------------");
    }

    public static void reloadCurrentFile() {
        System.out.println("Reload successfully");
        System.out.println("--------------------------------------------------------------------------------------------------");
    }

    public static void setCurrentFile(String path) {
        currentFile = new File(path);
        System.out.println("The current file has been set successfully");
        reloadCurrentFile();
    }

    public static void setExit() {
        exit = 1;
    }

    public static void complete(Boolean isRelevanceMining, Boolean isFuzzySearching) {
        System.out.println("Code completion successfully");
        System.out.println("--------------------------------------------------------------------------------------------------");
    }
}

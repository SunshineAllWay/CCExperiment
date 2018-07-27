package ui;
public class TreeViewer {
    public TreeViewer() {
        System.out.println("TreeViewer is no longer an instantiable class.  Please use XMLTreeView instead.");
        throw new RuntimeException();
    }
    public TreeViewer(String title, String filename) {
        System.out.println("TreeViewer is no longer an instantiable class.  Please use XMLTreeView instead.");
        throw new RuntimeException();
    }
    public static void main(String[] argv) {
        try {
            Class.forName("javax.swing.JFrame");
        } catch (ClassNotFoundException cnfe) {
            System.out.println("Couldn't load class javax.swing.JFrame.");
            System.out.println("This sample now uses Swing version 1.1.  Couldn't find the Swing 1.1 classes, please check your CLASSPATH settings.");
            System.exit(1);
        }
        TreeView.main(argv);
    }
}

package org.test;
import java.awt.Dimension;
import java.io.File;
import java.net.URL;
import java.net.MalformedURLException;
import javax.swing.JFrame;
import javax.swing.WindowConstants;
import org.apache.batik.swing.*;
public class ScrollExample
{
    public static void main(String args[])
    {
        if(args.length != 1)
        {
            System.out.println("No or multiple SVG files were specified.");
            System.out.println("Usage: ScrollExample svgFileName");
            System.exit(1);
        }
        File file = new File(args[0]);
        if(!file.exists())
        {
            System.out.println("File "+file+" does not exist!");
            System.exit(1);
        }
        try
        {
            new ScrollExample(file.toURL());
        }
        catch(MalformedURLException e)
        {
            System.out.println("Cannot convert file to a valid URL...");
            System.out.println(e);
            System.exit(1);
        }
    }
    private ScrollExample(URL url)
    {
        JFrame frame = new JFrame("ScrollExample: "+url.getFile());
        frame.setResizable(true);
        frame.setSize(new Dimension(500,500));
        frame.addWindowListener(new java.awt.event.WindowAdapter() {
                public void windowClosing
                    (java.awt.event.WindowEvent e) {
                    System.exit(0);
                }
            });
        JSVGCanvas     canvas   = new JSVGCanvas();
        JSVGScrollPane scroller = new JSVGScrollPane(canvas);
        canvas.setURI(url.toString());
        frame.getContentPane().add(scroller);
        frame.setVisible(true);
    }
}

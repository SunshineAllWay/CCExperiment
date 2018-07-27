package org.apache.log4j.lf5.viewer;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
public class LogFactor5LoadingDialog extends LogFactor5Dialog {
  public LogFactor5LoadingDialog(JFrame jframe, String message) {
    super(jframe, "LogFactor5", false);
    JPanel bottom = new JPanel();
    bottom.setLayout(new FlowLayout());
    JPanel main = new JPanel();
    main.setLayout(new GridBagLayout());
    wrapStringOnPanel(message, main);
    getContentPane().add(main, BorderLayout.CENTER);
    getContentPane().add(bottom, BorderLayout.SOUTH);
    show();
  }
}
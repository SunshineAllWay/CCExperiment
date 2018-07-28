package org.apache.batik.apps.svgbrowser;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.ButtonGroup;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
public class LocalHistory {
    protected JSVGViewerFrame svgFrame;
    protected JMenu menu;
    protected int index;
    protected List visitedURIs = new ArrayList();
    protected int currentURI = -1;
    protected ButtonGroup group = new ButtonGroup();
    protected ActionListener actionListener = new RadioListener();
    protected int state;
    protected static final int STABLE_STATE = 0;
    protected static final int BACK_PENDING_STATE = 1;
    protected static final int FORWARD_PENDING_STATE = 2;
    protected static final int RELOAD_PENDING_STATE = 3;
    public LocalHistory(JMenuBar mb, JSVGViewerFrame svgFrame) {
        this.svgFrame = svgFrame;
        int mc = mb.getMenuCount();
        for (int i = 0; i < mc; i++) {
            JMenu m = mb.getMenu(i);
            int ic = m.getItemCount();
            for (int j = 0; j < ic; j++) {
                JMenuItem mi = m.getItem(j);
                if (mi != null) {
                    String s = mi.getText();
                    if ("@@@".equals(s)) {
                        menu = m;
                        index = j;
                        m.remove(j);
                        return;
                    }
                }
            }
        }
        throw new IllegalArgumentException("No '@@@' marker found");
    }
    public void back() {
        update();
        state = BACK_PENDING_STATE;
        currentURI -= 2;
        svgFrame.showSVGDocument((String)visitedURIs.get(currentURI + 1));
    }
    public boolean canGoBack() {
        return currentURI > 0;
    }
    public void forward() {
        update();
        state = FORWARD_PENDING_STATE;
        svgFrame.showSVGDocument((String)visitedURIs.get(currentURI + 1));
    }
    public boolean canGoForward() {
        return currentURI < visitedURIs.size() - 1;
    }
    public void reload() {
        update();
        state = RELOAD_PENDING_STATE;
        currentURI--;
        svgFrame.showSVGDocument((String)visitedURIs.get(currentURI + 1));
    }
    public void update(String uri) {
        if (currentURI < -1) {
            throw new IllegalStateException("Unexpected currentURI:" + currentURI );
        }
        state = STABLE_STATE;
        if (++currentURI < visitedURIs.size()) {
            if (!visitedURIs.get(currentURI).equals(uri)) {
                int len = menu.getItemCount();
                for (int i = len - 1; i >= index + currentURI + 1; i--) {
                    JMenuItem mi = menu.getItem(i);
                    group.remove(mi);
                    menu.remove(i);
                }
                visitedURIs = visitedURIs.subList(0, currentURI + 1);
            }
            JMenuItem mi = menu.getItem(index + currentURI);
            group.remove(mi);
            menu.remove(index + currentURI);
            visitedURIs.set(currentURI, uri);
        } else {
            if (visitedURIs.size() >= 15) {
                visitedURIs.remove(0);
                JMenuItem mi = menu.getItem(index);
                group.remove(mi);
                menu.remove(index);
                currentURI--;
            }
            visitedURIs.add(uri);
        }
        String text = uri;
        int i = uri.lastIndexOf('/');
        if (i == -1) {
            i = uri.lastIndexOf('\\' );
        }
        if (i != -1) {
            text = uri.substring(i + 1);
        }
        JMenuItem mi = new JRadioButtonMenuItem(text);
        mi.setToolTipText(uri);
        mi.setActionCommand(uri);
        mi.addActionListener(actionListener);
        group.add(mi);
        mi.setSelected(true);
        menu.insert(mi, index + currentURI);
    }
    protected void update() {
        switch (state) {
        case BACK_PENDING_STATE:
            currentURI += 2;
            break;
        case RELOAD_PENDING_STATE:
            currentURI++;
            break;
        case FORWARD_PENDING_STATE:
        case STABLE_STATE:
        }
    }
    protected class RadioListener
            implements ActionListener {
        protected RadioListener() {
        }
        public void actionPerformed( ActionEvent e ) {
            String uri = e.getActionCommand();
            currentURI = getItemIndex( (JMenuItem)e.getSource() ) - 1;
            svgFrame.showSVGDocument( uri );
        }
        public int getItemIndex( JMenuItem item ) {
            int ic = menu.getItemCount();
            for ( int i = index; i < ic; i++ ) {
                if ( menu.getItem( i ) == item ) {
                    return i - index;
                }
            }
            throw new IllegalArgumentException("MenuItem is not from my menu!" );
        }
    }
}

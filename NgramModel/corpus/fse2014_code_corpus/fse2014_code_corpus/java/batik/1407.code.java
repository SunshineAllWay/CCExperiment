package org.apache.batik.util.gui;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.EventListener;
import java.util.EventObject;
import java.util.Locale;
import java.util.ResourceBundle;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.EventListenerList;
import javax.swing.plaf.basic.BasicButtonUI;
import org.apache.batik.util.resources.ResourceManager;
public class DropDownComponent extends JPanel {
    private JButton mainButton;
    private JButton dropDownButton;
    private Icon enabledDownArrow;
    private Icon disabledDownArrow;
    private ScrollablePopupMenu popupMenu;
    private boolean isDropDownEnabled;
    public DropDownComponent(JButton mainButton) {
        super(new BorderLayout());
        popupMenu = getPopupMenu();
        this.mainButton = mainButton;
        add(this.mainButton, BorderLayout.WEST);
        this.mainButton.setMaximumSize(new Dimension(24, 24));
        this.mainButton.setPreferredSize(new Dimension(24, 24));
        enabledDownArrow = new SmallDownArrow();
        disabledDownArrow = new SmallDisabledDownArrow();
        dropDownButton = new JButton(disabledDownArrow);
        dropDownButton.setBorderPainted(false);
        dropDownButton.setDisabledIcon(disabledDownArrow);
        dropDownButton.addMouseListener(new DropDownListener());
        dropDownButton.setMaximumSize(new Dimension(18, 24));
        dropDownButton.setMinimumSize(new Dimension(18, 10));
        dropDownButton.setPreferredSize(new Dimension(18, 10));
        dropDownButton.setFocusPainted(false);
        add(dropDownButton, BorderLayout.EAST);
        setEnabled(false);
    }
    public ScrollablePopupMenu getPopupMenu() {
        if (popupMenu == null) {
            popupMenu = new ScrollablePopupMenu(this);
            popupMenu.setEnabled(false);
            popupMenu.addPropertyChangeListener
                ("enabled",
                 new PropertyChangeListener() {
                     public void propertyChange(PropertyChangeEvent evt) {
                         setEnabled
                            (((Boolean) evt.getNewValue()).booleanValue());
                     }
                 });
            popupMenu.addListener
                (new ScrollablePopupMenuAdapter() {
                     public void itemsWereAdded(ScrollablePopupMenuEvent ev) {
                         updateMainButtonTooltip(ev.getDetails());
                     }
                     public void itemsWereRemoved(ScrollablePopupMenuEvent ev) {
                         updateMainButtonTooltip(ev.getDetails());
                     }
                 });
        }
        return popupMenu;
    }
    public void setEnabled(boolean enable) {
        isDropDownEnabled = enable;
        mainButton.setEnabled(enable);
        dropDownButton.setEnabled(enable);
        dropDownButton.setIcon(enable ? enabledDownArrow : disabledDownArrow);
    }
    public boolean isEnabled() {
        return isDropDownEnabled;
    }
    public void updateMainButtonTooltip(String newTooltip) {
        mainButton.setToolTipText(newTooltip);
    }
    private class DropDownListener extends MouseAdapter {
        public void mousePressed(MouseEvent e) {
            if (popupMenu.isShowing() && isDropDownEnabled) {
                popupMenu.setVisible(false);
            } else if (isDropDownEnabled) {
                popupMenu.showMenu
                    ((Component) e.getSource(), DropDownComponent.this);
            }
        }
        public void mouseEntered(MouseEvent ev) {
            dropDownButton.setBorderPainted(true);
        }
        public void mouseExited(MouseEvent ev) {
            dropDownButton.setBorderPainted(false);
        }
    }
    private static class SmallDownArrow implements Icon {
        protected Color arrowColor = Color.black;
        public void paintIcon(Component c, Graphics g, int x, int y) {
            g.setColor(arrowColor);
            g.drawLine(x, y, x + 4, y);
            g.drawLine(x + 1, y + 1, x + 3, y + 1);
            g.drawLine(x + 2, y + 2, x + 2, y + 2);
        }
        public int getIconWidth() {
            return 6;
        }
        public int getIconHeight() {
            return 4;
        }
    }
    private static class SmallDisabledDownArrow extends SmallDownArrow {
        public SmallDisabledDownArrow() {
            arrowColor = new Color(140, 140, 140);
        }
        public void paintIcon(Component c, Graphics g, int x, int y) {
            super.paintIcon(c, g, x, y);
            g.setColor(Color.white);
            g.drawLine(x + 3, y + 2, x + 4, y + 1);
            g.drawLine(x + 3, y + 3, x + 5, y + 1);
        }
    }
    public static interface ScrollablePopupMenuItem {
        void setSelected(boolean selected);
        boolean isSelected();
        String getText();
        void setText(String text);
        void setEnabled(boolean enabled);
    }
    public static class DefaultScrollablePopupMenuItem extends JButton
            implements ScrollablePopupMenuItem {
        public static final Color MENU_HIGHLIGHT_BG_COLOR =
            UIManager.getColor("MenuItem.selectionBackground");
        public static final Color MENU_HIGHLIGHT_FG_COLOR =
            UIManager.getColor("MenuItem.selectionForeground");
        public static final Color MENUITEM_BG_COLOR =
            UIManager.getColor("MenuItem.background");
        public static final Color MENUITEM_FG_COLOR =
            UIManager.getColor("MenuItem.foreground");
        private ScrollablePopupMenu parent;
        public DefaultScrollablePopupMenuItem(ScrollablePopupMenu parent,
                                              String text) {
            super(text);
            this.parent = parent;
            init();
        }
        private void init() {
            this.setUI(BasicButtonUI.createUI(this));
            setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 20));
            setMenuItemDefaultColors();
            this.setAlignmentX(JButton.LEFT_ALIGNMENT);
            setSelected(false);
            this.addMouseListener
                (new MouseAdapter() {
                     public void mouseEntered(MouseEvent e) {
                         if (DefaultScrollablePopupMenuItem.this.isEnabled()) {
                             setSelected(true);
                             parent.selectionChanged
                                 (DefaultScrollablePopupMenuItem.this, true);
                         }
                     }
                     public void mouseExited(MouseEvent e) {
                         if (DefaultScrollablePopupMenuItem.this.isEnabled()) {
                             setSelected(false);
                             parent.selectionChanged
                                (DefaultScrollablePopupMenuItem.this, false);
                         }
                     }
                     public void mouseClicked(MouseEvent e) {
                         parent.processItemClicked();
                     }
                 });
        }
        private void setMenuItemDefaultColors() {
            setBackground(MENUITEM_BG_COLOR);
            setForeground(MENUITEM_FG_COLOR);
        }
        public void setSelected(boolean selected) {
            super.setSelected(selected);
            if (selected) {
                setBackground(MENU_HIGHLIGHT_BG_COLOR);
                setForeground(MENU_HIGHLIGHT_FG_COLOR);
            } else {
                setMenuItemDefaultColors();
            }
        }
        public String getText() {
            return super.getText();
        }
        public void setText(String text) {
            super.setText(text);
        }
        public void setEnabled(boolean b) {
            super.setEnabled(b);
        }
    }
    public static interface ScrollablePopupMenuModel {
        String getFooterText();
        void processItemClicked();
        void processBeforeShowed();
        void processAfterShowed();
    }
    public static class ScrollablePopupMenu extends JPopupMenu {
        private static final String RESOURCES =
            "org.apache.batik.util.gui.resources.ScrollablePopupMenuMessages";
        private static ResourceBundle bundle;
        private static ResourceManager resources;
        static {
            bundle = ResourceBundle.getBundle(RESOURCES, Locale.getDefault());
            resources = new ResourceManager(bundle);
        }
        private JPanel menuPanel = new JPanel();
        private JScrollPane scrollPane;
        private int preferredHeight = resources.getInteger("PreferredHeight");
        private ScrollablePopupMenuModel model;
        private JComponent ownerComponent;
        private ScrollablePopupMenuItem footer;
        private EventListenerList eventListeners = new EventListenerList();
        public ScrollablePopupMenu(JComponent owner) {
            super();
            this.setLayout(new BorderLayout());
            menuPanel.setLayout(new GridLayout(0, 1));
            ownerComponent = owner;
            init();
        }
        private void init() {
            super.removeAll();
            scrollPane = new JScrollPane();
            scrollPane.setViewportView(menuPanel);
            scrollPane.setBorder(null);
            int minWidth = resources.getInteger("ScrollPane.minWidth");
            int minHeight = resources.getInteger("ScrollPane.minHeight");
            int maxWidth = resources.getInteger("ScrollPane.maxWidth");
            int maxHeight = resources.getInteger("ScrollPane.maxHeight");
            scrollPane.setMinimumSize(new Dimension(minWidth, minHeight));
            scrollPane.setMaximumSize(new Dimension(maxWidth, maxHeight));
            scrollPane.setHorizontalScrollBarPolicy
                (ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
            add(scrollPane, BorderLayout.CENTER);
            addFooter(new DefaultScrollablePopupMenuItem(this, ""));
        }
        public void showMenu(Component invoker, Component refComponent) {
            model.processBeforeShowed();
            Point abs = new Point(0, refComponent.getHeight());
            SwingUtilities.convertPointToScreen(abs, refComponent);
            this.setLocation(abs);
            this.setInvoker(invoker);
            this.setVisible(true);
            this.revalidate();
            this.repaint();
            model.processAfterShowed();
        }
        public void add(ScrollablePopupMenuItem menuItem, int index,
                        int oldSize, int newSize) {
            menuPanel.add((Component) menuItem, index);
            if (oldSize == 0) {
                this.setEnabled(true);
            }
        }
        public void remove(ScrollablePopupMenuItem menuItem, int oldSize,
                           int newSize) {
            menuPanel.remove((Component) menuItem);
            if (newSize == 0) {
                this.setEnabled(false);
            }
        }
        private int getPreferredWidth() {
            Component[] components = menuPanel.getComponents();
            int maxWidth = 0;
            for (int i = 0; i < components.length; i++) {
                int currentWidth = components[i].getPreferredSize().width;
                if (maxWidth < currentWidth) {
                    maxWidth = currentWidth;
                }
            }
            int footerWidth = ((Component) footer).getPreferredSize().width;
            if (footerWidth > maxWidth) {
                maxWidth = footerWidth;
            }
            int widthOffset = 30;
            return maxWidth + widthOffset;
        }
        private int getPreferredHeight() {
            if (scrollPane.getPreferredSize().height < preferredHeight) {
                int heightOffset = 10;
                return scrollPane.getPreferredSize().height
                        + ((Component) footer).getPreferredSize().height
                        + heightOffset;
            }
            return preferredHeight
                    + ((Component) footer).getPreferredSize().height;
        }
        public Dimension getPreferredSize() {
            return new Dimension(getPreferredWidth(), getPreferredHeight());
        }
        public void selectionChanged(ScrollablePopupMenuItem targetItem,
                boolean wasSelected) {
            Component[] comps = menuPanel.getComponents();
            int n = comps.length;
            if (!wasSelected) {
                for (int i = n - 1; i >= 0; i--) {
                    ScrollablePopupMenuItem item = (ScrollablePopupMenuItem) comps[i];
                    item.setSelected(wasSelected);
                }
            } else {
                for (int i = 0; i < n; i++) {
                    ScrollablePopupMenuItem item = (ScrollablePopupMenuItem) comps[i];
                    if (item == targetItem) {
                        break;
                    }
                    item.setSelected(true);
                }
            }
            footer.setText(model.getFooterText() + getSelectedItemsCount());
            repaint();
        }
        public void setModel(ScrollablePopupMenuModel model) {
            this.model = model;
            this.footer.setText(model.getFooterText());
        }
        public ScrollablePopupMenuModel getModel() {
            return model;
        }
        public int getSelectedItemsCount() {
            int selectionCount = 0;
            Component[] components = menuPanel.getComponents();
            for (int i = 0; i < components.length; i++) {
                ScrollablePopupMenuItem item = (ScrollablePopupMenuItem) components[i];
                if (item.isSelected()) {
                    selectionCount++;
                }
            }
            return selectionCount;
        }
        public void processItemClicked() {
            footer.setText(model.getFooterText() + 0);
            setVisible(false);
            model.processItemClicked();
        }
        public JComponent getOwner() {
            return ownerComponent;
        }
        private void addFooter(ScrollablePopupMenuItem footer) {
            this.footer = footer;
            this.footer.setEnabled(false);
            add((Component)this.footer, BorderLayout.SOUTH);
        }
        public ScrollablePopupMenuItem getFooter() {
            return footer;
        }
        public void addListener(ScrollablePopupMenuListener listener) {
            eventListeners.add(ScrollablePopupMenuListener.class, listener);
        }
        public void fireItemsWereAdded(ScrollablePopupMenuEvent event) {
            Object[] listeners = eventListeners.getListenerList();
            int length = listeners.length;
            for (int i = 0; i < length; i += 2) {
                if (listeners[i] == ScrollablePopupMenuListener.class) {
                    ((ScrollablePopupMenuListener) listeners[i + 1])
                            .itemsWereAdded(event);
                }
            }
        }
        public void fireItemsWereRemoved(ScrollablePopupMenuEvent event) {
            Object[] listeners = eventListeners.getListenerList();
            int length = listeners.length;
            for (int i = 0; i < length; i += 2) {
                if (listeners[i] == ScrollablePopupMenuListener.class) {
                    ((ScrollablePopupMenuListener) listeners[i + 1])
                            .itemsWereRemoved(event);
                }
            }
        }
    }
    public static class ScrollablePopupMenuEvent extends EventObject {
        public static final int ITEMS_ADDED = 1;
        public static final int ITEMS_REMOVED = 2;
        private int type;
        private int itemNumber;
        private String details;
        public ScrollablePopupMenuEvent(Object source, int type,
                                        int itemNumber, String details) {
            super(source);
            initEvent(type, itemNumber, details);
        }
        public void initEvent(int type, int itemNumber, String details) {
            this.type = type;
            this.itemNumber = itemNumber;
            this.details = details;
        }
        public String getDetails() {
            return details;
        }
        public int getItemNumber() {
            return itemNumber;
        }
        public int getType() {
            return type;
        }
    }
    public static interface ScrollablePopupMenuListener extends EventListener {
        void itemsWereAdded(ScrollablePopupMenuEvent ev);
        void itemsWereRemoved(ScrollablePopupMenuEvent ev);
    }
    public static class ScrollablePopupMenuAdapter
            implements ScrollablePopupMenuListener {
        public void itemsWereAdded(ScrollablePopupMenuEvent ev) {
        }
        public void itemsWereRemoved(ScrollablePopupMenuEvent ev) {
        }
    }
}

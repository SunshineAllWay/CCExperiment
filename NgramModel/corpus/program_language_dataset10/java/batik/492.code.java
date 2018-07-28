package org.apache.batik.dom.events;
import java.util.ArrayList;
import java.util.List;
import org.apache.batik.xml.XMLUtilities;
import org.w3c.dom.events.UIEvent;
import org.w3c.dom.views.AbstractView;
public class DOMUIEvent extends AbstractEvent implements UIEvent {
    private AbstractView view;
    private int detail;
    public AbstractView getView() {
        return view;
    }
    public int getDetail() {
        return detail;
    }
    public void initUIEvent(String typeArg,
                            boolean canBubbleArg,
                            boolean cancelableArg,
                            AbstractView viewArg,
                            int detailArg) {
        initEvent(typeArg, canBubbleArg, cancelableArg);
        this.view = viewArg;
        this.detail = detailArg;
    }
    public void initUIEventNS(String namespaceURIArg,
                              String typeArg,
                              boolean canBubbleArg,
                              boolean cancelableArg,
                              AbstractView viewArg,
                              int detailArg) {
        initEventNS(namespaceURIArg, typeArg, canBubbleArg, cancelableArg);
        this.view = viewArg;
        this.detail = detailArg;
    }
    protected String[] split(String s) {
        List a = new ArrayList(8);
        StringBuffer sb;
        int i = 0;
        int len = s.length();
        while (i < len) {
            char c = s.charAt(i++);
            if (XMLUtilities.isXMLSpace(c)) {
                continue;
            }
            sb = new StringBuffer();
            sb.append(c);
            while (i < len) {
                c = s.charAt(i++);
                if (XMLUtilities.isXMLSpace(c)) {
                    a.add(sb.toString());
                    break;
                }
                sb.append(c);
            }
            if (i == len) {
                a.add(sb.toString());
            }
        }
        return (String[]) a.toArray(new String[a.size()]);
    }
}

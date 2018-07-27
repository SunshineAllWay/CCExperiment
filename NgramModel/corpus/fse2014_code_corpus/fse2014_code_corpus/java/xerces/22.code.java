package sax.helpers;
import org.xml.sax.Attributes;
public class AttributesImpl
    implements Attributes {
    private ListNode head;
    private ListNode tail;
    private int length;
    public int getLength() {
        return length;
    }
    public int getIndex(String raw) {
        ListNode place = head;
        int index = 0;
        while (place != null) {
            if (place.raw.equals(raw)) {
                return index;
            }
            index++;
            place = place.next;
        }
        return -1;
    }
    public int getIndex(String uri, String local) {
        ListNode place = head;
        int index = 0;
        while (place != null) {
            if (place.uri.equals(uri) && place.local.equals(local)) {
                return index;
            }
            index++;
            place = place.next;
        }
        return -1;
    }
    public String getURI(int index) {
        ListNode node = getListNodeAt(index);
        return node != null ? node.uri : null;
    } 
    public String getLocalName(int index) {
        ListNode node = getListNodeAt(index);
        return node != null ? node.local : null;
    } 
    public String getQName(int index) {
        ListNode node = getListNodeAt(index);
        return node != null ? node.raw : null;
    } 
    public String getType(int index) {
        ListNode node = getListNodeAt(index);
        return (node != null) ? node.type : null;
    } 
    public String getType(String uri, String local) {
        ListNode node = getListNode(uri, local);
        return (node != null) ? node.type : null;
    } 
    public String getType(String raw) {
        ListNode node = getListNode(raw);
        return (node != null) ? node.type : null;
    } 
    public String getValue(int index) {
        ListNode node = getListNodeAt(index);
        return (node != null) ? node.value : null;
    } 
    public String getValue(String uri, String local) {
        ListNode node = getListNode(uri, local);
        return (node != null) ? node.value : null;
    } 
    public String getValue(String raw) {
        ListNode node = getListNode(raw);
        return (node != null) ? node.value : null;
    } 
    public void addAttribute(String raw, String type, String value) {
        addAttribute(null, null, raw, type, value);
    }
    public void addAttribute(String uri, String local, String raw, 
                             String type, String value) {
        ListNode node = new ListNode(uri, local, raw, type, value);
        if (length == 0) {
            head = node;
        }
        else {
            tail.next = node;
        }
        tail = node;
        length++;
    } 
    public void insertAttributeAt(int index, 
                                  String raw, String type, String value) {
        insertAttributeAt(index, null, null, raw, type, value);
    }
    public void insertAttributeAt(int index, 
                                  String uri, String local, String raw, 
                                  String type, String value) {
        if (length == 0 || index >= length) {
            addAttribute(uri, local, raw, type, value);
            return;
        }
        ListNode node = new ListNode(uri, local, raw, type, value);
        if (index < 1) {
            node.next = head;
            head = node;
        }
        else {
            ListNode prev = getListNodeAt(index - 1);
            node.next = prev.next;
            prev.next = node;
        }
        length++;
    } 
    public void removeAttributeAt(int index) {
        if (length == 0) {
            return;
        }
        if (index == 0) {
            head = head.next;
            if (head == null) {
                tail = null;
            }
            length--;
        }
        else {
            ListNode prev = getListNodeAt(index - 1);
            ListNode node = getListNodeAt(index);
            if (node != null) {
                prev.next = node.next;
                if (node == tail) {
                    tail = prev;
                }
                length--;
            }
        }
    } 
    public void removeAttribute(String raw) {
        removeAttributeAt(getIndex(raw));
    }
    public void removeAttribute(String uri, String local) {
        removeAttributeAt(getIndex(uri, local));
    }
    private ListNode getListNodeAt(int i) {
        for (ListNode place = head; place != null; place = place.next) {
            if (--i == -1) {
                return place;
            }
        }
        return null;
    } 
    public ListNode getListNode(String uri, String local) {
        if (uri != null && local != null) {
            ListNode place = head;
            while (place != null) {
                if (place.uri != null && place.local != null &&
                    place.uri.equals(uri) && place.local.equals(local)) {
                    return place;
                }
                place = place.next;
            }
        }
        return null;
    } 
    private ListNode getListNode(String raw) {
        if (raw != null) {
            for (ListNode place = head; place != null; place = place.next) {
                if (place.raw != null && place.raw.equals(raw)) {
                    return place;
                }
            }
        }
        return null;
    } 
    public String toString() {
        StringBuffer str = new StringBuffer();
        str.append('[');
        str.append("len=");
        str.append(length);
        str.append(", {");
        for (ListNode place = head; place != null; place = place.next) {
            str.append(place.toString());
            if (place.next != null) {
                str.append(", ");
            }
        }
        str.append("}]");
        return str.toString();
    } 
    static class ListNode {
        public String uri;
        public String local;
        public String raw;
        public String type;
        public String value;
        public ListNode next;
        public ListNode(String uri, String local, String raw, 
                        String type, String value) {
            this.uri   = uri;
            this.local = local;
            this.raw   = raw;
            this.type  = type;
            this.value = value;
        } 
        public String toString() {
            return raw != null ? raw : local;
        }
    } 
} 

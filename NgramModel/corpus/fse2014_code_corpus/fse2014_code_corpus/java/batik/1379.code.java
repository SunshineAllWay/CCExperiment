package org.apache.batik.util;
public class DoublyLinkedList {
        public static class Node {
                private Node next = null;
                private Node prev = null;
                public final Node getNext() { return next; }
                public final Node getPrev() { return prev; }
                protected final void setNext(Node newNext) { next = newNext; }
                protected final void setPrev(Node newPrev) { prev = newPrev; }
                protected final void unlink() {
                        if (getNext() != null)
                                getNext().setPrev(getPrev());
                        if (getPrev() != null)
                                getPrev().setNext(getNext());
                        setNext(null);
                        setPrev(null);
                }
                protected final void insertBefore(Node nde) {
                        if (this == nde) return;
                        if (getPrev() != null)
                unlink();
                        if (nde == null) {
                                setNext(this);
                                setPrev(this);
                        } else {
                                setNext(nde);
                                setPrev(nde.getPrev());
                                nde.setPrev(this);
                if (getPrev() != null)
                    getPrev().setNext(this);
                        }
                }
        }
    private Node head = null;
    private int  size = 0;
    public DoublyLinkedList() {}
    public synchronized int getSize() { return size; }
    public synchronized void empty() {
        while(size > 0) pop();
    }
    public Node getHead() { return head; }
    public Node getTail() { return head.getPrev(); }
    public void touch(Node nde) {
        if (nde == null) return;
        nde.insertBefore(head);
        head = nde;
    }
    public void add(int index, Node nde) {
        if (nde == null) return;
        if (index == 0) {
            nde.insertBefore(head);
            head = nde;
        } else if (index == size) {
            nde.insertBefore(head);
        } else {
            Node after = head;
            while (index != 0) {
                after = after.getNext();
                index--;
            }
            nde.insertBefore(after);
        }
        size++;
    }
    public void add(Node nde) {
        if (nde == null) return;
        nde.insertBefore(head);
        head = nde;
        size++;
    }
    public void remove(Node nde) {
        if (nde == null) return;
        if (nde == head) {
            if (head.getNext() == head) 
                head = null;  
            else
                head = head.getNext();
        }
        nde.unlink();
        size--;
    }
    public Node pop() {
        if (head == null) return null;
        Node nde = head;
        remove(nde);
        return nde;
    }
    public Node unpush() {
        if (head == null) return null;
        Node nde = getTail();
        remove(nde);
        return nde;
    }
    public void push(Node nde) {
        nde.insertBefore(head);
        if (head == null) head = nde;
        size++;
    }
    public void unpop(Node nde) {
        nde.insertBefore(head);
        head = nde;
        size++;
    }
}

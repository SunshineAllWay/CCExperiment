package org.apache.log4j;
import java.util.Hashtable;
import java.util.Stack;
import java.util.Enumeration;
import java.util.Vector;
import org.apache.log4j.helpers.LogLog;
public class NDC {
  static Hashtable ht = new Hashtable();
  static int pushCounter = 0; 
  static final int REAP_THRESHOLD = 5;
  private NDC() {}
  private static Stack getCurrentStack() {
      if (ht != null) {
          return (Stack) ht.get(Thread.currentThread());
      }
      return null;
  }
  public
  static
  void clear() {
    Stack stack = getCurrentStack();    
    if(stack != null) 
      stack.setSize(0);    
  }
  public
  static
  Stack cloneStack() {
    Stack stack = getCurrentStack();
    if(stack == null)
      return null;
    else {
      return (Stack) stack.clone();
    }
  }
  public
  static
  void inherit(Stack stack) {
    if(stack != null)
      ht.put(Thread.currentThread(), stack);
  }
  static
  public
  String get() {
    Stack s = getCurrentStack();
    if(s != null && !s.isEmpty()) 
      return ((DiagnosticContext) s.peek()).fullMessage;
    else
      return null;
  }
  public
  static
  int getDepth() {
    Stack stack = getCurrentStack();          
    if(stack == null)
      return 0;
    else
      return stack.size();      
  }
  private
  static
  void lazyRemove() {
    if (ht == null) return;
    Vector v;
    synchronized(ht) {
      if(++pushCounter <= REAP_THRESHOLD) {
	return; 
      } else {
	pushCounter = 0; 
      }
      int misses = 0;
      v = new Vector(); 
      Enumeration enumeration = ht.keys();
      while(enumeration.hasMoreElements() && (misses <= 4)) {
	Thread t = (Thread) enumeration.nextElement();
	if(t.isAlive()) {
	  misses++;
	} else {
	  misses = 0;
	  v.addElement(t);
	}
      }
    } 
    int size = v.size();
    for(int i = 0; i < size; i++) {
      Thread t = (Thread) v.elementAt(i);
      LogLog.debug("Lazy NDC removal for thread [" + t.getName() + "] ("+ 
		   ht.size() + ").");
      ht.remove(t);
    }
  }
  public
  static
  String pop() {
    Stack stack = getCurrentStack();
    if(stack != null && !stack.isEmpty()) 
      return ((DiagnosticContext) stack.pop()).message;
    else
      return "";
  }
  public
  static
  String peek() {
    Stack stack = getCurrentStack();
    if(stack != null && !stack.isEmpty())
      return ((DiagnosticContext) stack.peek()).message;
    else
      return "";
  }
  public
  static
  void push(String message) {
    Stack stack = getCurrentStack();
    if(stack == null) {
      DiagnosticContext dc = new DiagnosticContext(message, null);      
      stack = new Stack();
      Thread key = Thread.currentThread();
      ht.put(key, stack);
      stack.push(dc);
    } else if (stack.isEmpty()) {
      DiagnosticContext dc = new DiagnosticContext(message, null);            
      stack.push(dc);
    } else {
      DiagnosticContext parent = (DiagnosticContext) stack.peek();
      stack.push(new DiagnosticContext(message, parent));
    }    
  }
  static
  public
  void remove() {
    if (ht != null) {
        ht.remove(Thread.currentThread());
        lazyRemove();
    }
  }
  static
  public
  void setMaxDepth(int maxDepth) {
    Stack stack = getCurrentStack();    
    if(stack != null && maxDepth < stack.size()) 
      stack.setSize(maxDepth);
  }
   private static class DiagnosticContext {
    String fullMessage;
    String message;
    DiagnosticContext(String message, DiagnosticContext parent) {
      this.message = message;
      if(parent != null) {
	fullMessage = parent.fullMessage + ' ' + message;
      } else {
	fullMessage = message;
      }
    }
  }
}

package org.apache.log4j.performance;
public class SystemTime {
  static int RUN_LENGTH = 1000000;
  static
  public 
  void main(String[] args) {    
    double t = systemCurrentTimeLoop();
    System.out.println("Average System.currentTimeMillis() call took " + t);
    t = currentThreadNameloop();
    System.out.println("Average Thread.currentThread().getName() call took " 
		       + t);
  }
  static
  double systemCurrentTimeLoop() {
    long before = System.currentTimeMillis();
    for(int i = 0; i < RUN_LENGTH; i++) {
      System.currentTimeMillis();
    }
    return (System.currentTimeMillis() - before)*1000.0/RUN_LENGTH;    
  }
  static
  double currentThreadNameloop() {
    long before = System.currentTimeMillis();
    for(int i = 0; i < RUN_LENGTH; i++) {
      Thread.currentThread().getName();
    }
    return (System.currentTimeMillis() - before)*1000.0/RUN_LENGTH;    
  }  
}

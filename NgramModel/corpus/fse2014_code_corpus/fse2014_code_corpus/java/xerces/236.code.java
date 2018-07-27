package org.apache.xerces.dom;
class LCount 
{ 
    static java.util.Hashtable lCounts=new java.util.Hashtable();
    public int captures=0,bubbles=0,defaults, total=0;
    static LCount lookup(String evtName)
    {
        LCount lc=(LCount)lCounts.get(evtName);
        if(lc==null)
            lCounts.put(evtName,(lc=new LCount()));
        return lc;	        
    }
} 

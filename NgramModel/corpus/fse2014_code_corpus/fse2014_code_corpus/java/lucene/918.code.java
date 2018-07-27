package org.apache.lucene.benchmark.byTask.utils;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.util.HashMap;
import java.util.Properties;
import java.util.StringTokenizer;
public class Config {
  private static final String NEW_LINE = System.getProperty("line.separator");
  private int roundNumber = 0;
  private Properties props;
  private HashMap<String,Object> valByRound = new HashMap<String,Object>();
  private HashMap<String,String> colForValByRound = new HashMap<String,String>();
  private String algorithmText;
  public Config (Reader algReader) throws IOException {
    ArrayList<String> lines = new ArrayList<String>();
    BufferedReader r = new BufferedReader(algReader);
    int lastConfigLine=0;
    for (String line = r.readLine(); line!=null; line=r.readLine()) {
      lines.add(line);
      if (line.indexOf('=')>0) {
        lastConfigLine = lines.size();
      }
    }
    r.close();
    StringBuffer sb = new StringBuffer();
    for (int i=0; i<lastConfigLine; i++) {
      sb.append(lines.get(i));
      sb.append(NEW_LINE);
    }
    this.props = new Properties();
    props.load(new ByteArrayInputStream(sb.toString().getBytes()));
    if (props.get("work.dir")==null) {
      props.setProperty("work.dir",System.getProperty("benchmark.work.dir","work"));
    }
    if (Boolean.valueOf(props.getProperty("print.props","true")).booleanValue()) {
      printProps();
    }
    sb = new StringBuffer();
    for (int i=lastConfigLine; i<lines.size(); i++) {
      sb.append(lines.get(i));
      sb.append(NEW_LINE);
    }
    algorithmText = sb.toString();
  }
  public Config (Properties props) {
    this.props = props;
    if (Boolean.valueOf(props.getProperty("print.props","true")).booleanValue()) {
      printProps();
    }
  }
  @SuppressWarnings("unchecked")
  private void printProps() {
    System.out.println("------------> config properties:");
    List<String> propKeys = new ArrayList(props.keySet());
    Collections.sort(propKeys);
    for (final String propName : propKeys) {
      System.out.println(propName + " = " + props.getProperty(propName));
    }
    System.out.println("-------------------------------");
  }
  public String get (String name, String dflt) {
    return props.getProperty(name,dflt);
  }
  public void set (String name, String value) throws Exception {
    if (valByRound.get(name) != null) {
      throw new Exception("Cannot modify a multi value property!");
    }
    props.setProperty(name,value);
  }
  public int get (String name, int dflt) {
    int vals[] = (int[]) valByRound.get(name);
    if (vals != null) {
      return vals[roundNumber % vals.length];
    }
    String sval = props.getProperty(name,""+dflt);
    if (sval.indexOf(":")<0) {
      return Integer.parseInt(sval);
    }
    int k = sval.indexOf(":");
    String colName = sval.substring(0,k);
    sval = sval.substring(k+1);
    colForValByRound.put(name,colName);
    vals = propToIntArray(sval);
    valByRound.put(name,vals);
    return vals[roundNumber % vals.length];
  }
  public double get (String name, double dflt) {
    double vals[] = (double[]) valByRound.get(name);
    if (vals != null) {
      return vals[roundNumber % vals.length];
    }
    String sval = props.getProperty(name,""+dflt);
    if (sval.indexOf(":")<0) {
      return Double.parseDouble(sval);
    }
    int k = sval.indexOf(":");
    String colName = sval.substring(0,k);
    sval = sval.substring(k+1);
    colForValByRound.put(name,colName);
    vals = propToDoubleArray(sval);
    valByRound.put(name,vals);
    return vals[roundNumber % vals.length];
  }
  public boolean get (String name, boolean dflt) {
    boolean vals[] = (boolean[]) valByRound.get(name);
    if (vals != null) {
      return vals[roundNumber % vals.length];
    }
    String sval = props.getProperty(name,""+dflt);
    if (sval.indexOf(":")<0) {
      return Boolean.valueOf(sval).booleanValue();
    }
    int k = sval.indexOf(":");
    String colName = sval.substring(0,k);
    sval = sval.substring(k+1);
    colForValByRound.put(name,colName);
    vals = propToBooleanArray(sval);
    valByRound.put(name,vals);
    return vals[roundNumber % vals.length];
  }
  public int newRound () {
    roundNumber++;
    StringBuffer sb = new StringBuffer("--> Round ").append(roundNumber-1).append("-->").append(roundNumber);
    if (valByRound.size()>0) {
      sb.append(": ");
      for (final String name : valByRound.keySet()) {
        Object a = valByRound.get(name);
        if (a instanceof int[]) {
          int ai[] = (int[]) a;
          int n1 = (roundNumber-1)%ai.length;
          int n2 = roundNumber%ai.length;
          sb.append("  ").append(name).append(":").append(ai[n1]).append("-->").append(ai[n2]);
        } else if (a instanceof double[]){
          double ad[] = (double[]) a;
          int n1 = (roundNumber-1)%ad.length;
          int n2 = roundNumber%ad.length;
          sb.append("  ").append(name).append(":").append(ad[n1]).append("-->").append(ad[n2]);
        }
        else {
          boolean ab[] = (boolean[]) a;
          int n1 = (roundNumber-1)%ab.length;
          int n2 = roundNumber%ab.length;
          sb.append("  ").append(name).append(":").append(ab[n1]).append("-->").append(ab[n2]);
        }
      }
    }
    System.out.println();
    System.out.println(sb.toString());
    System.out.println();
    return roundNumber;
  }
  private int[] propToIntArray (String s) {
    if (s.indexOf(":")<0) {
      return new int [] { Integer.parseInt(s) };
    }
    ArrayList<Integer> a = new ArrayList<Integer>();
    StringTokenizer st = new StringTokenizer(s,":");
    while (st.hasMoreTokens()) {
      String t = st.nextToken();
      a.add(Integer.valueOf(t));
    }
    int res[] = new int[a.size()]; 
    for (int i=0; i<a.size(); i++) {
      res[i] = a.get(i).intValue();
    }
    return res;
  }
  private double[] propToDoubleArray (String s) {
    if (s.indexOf(":")<0) {
      return new double [] { Double.parseDouble(s) };
    }
    ArrayList<Double> a = new ArrayList<Double>();
    StringTokenizer st = new StringTokenizer(s,":");
    while (st.hasMoreTokens()) {
      String t = st.nextToken();
      a.add(Double.valueOf(t));
    }
    double res[] = new double[a.size()]; 
    for (int i=0; i<a.size(); i++) {
      res[i] = a.get(i).doubleValue();
    }
    return res;
  }
  private boolean[] propToBooleanArray (String s) {
    if (s.indexOf(":")<0) {
      return new boolean [] { Boolean.valueOf(s).booleanValue() };
    }
    ArrayList<Boolean> a = new ArrayList<Boolean>();
    StringTokenizer st = new StringTokenizer(s,":");
    while (st.hasMoreTokens()) {
      String t = st.nextToken();
      a.add(new Boolean(t));
    }
    boolean res[] = new boolean[a.size()]; 
    for (int i=0; i<a.size(); i++) {
      res[i] = a.get(i).booleanValue();
    }
    return res;
  }
  public String getColsNamesForValsByRound() {
    if (colForValByRound.size()==0) {
      return "";
    }
    StringBuffer sb = new StringBuffer(); 
    for (final String name : colForValByRound.keySet()) {
      String colName = colForValByRound.get(name);
      sb.append(" ").append(colName);
    }
    return sb.toString();
  }
  public String getColsValuesForValsByRound(int roundNum) {
    if (colForValByRound.size()==0) {
      return "";
    }
    StringBuffer sb = new StringBuffer(); 
    for (final String name  : colForValByRound.keySet()) {
      String colName = colForValByRound.get(name);
      String template = " "+colName;
      if (roundNum<0) {
        sb.append(Format.formatPaddLeft("-",template));
      } else {
        Object a = valByRound.get(name);
        if (a instanceof int[]) {
          int ai[] = (int[]) a;
          int n = roundNum % ai.length;
          sb.append(Format.format(ai[n],template));
        }
        else if (a instanceof double[]) {
          double ad[] = (double[]) a;
          int n = roundNum % ad.length;
          sb.append(Format.format(2, ad[n],template));
        }
        else {
          boolean ab[] = (boolean[]) a;
          int n = roundNum % ab.length;
          sb.append(Format.formatPaddLeft(""+ab[n],template));
        }
      }
    }
    return sb.toString();
  }
  public int getRoundNumber() {
    return roundNumber;
  }
  public String getAlgorithmText() {
    return algorithmText;
  }
}

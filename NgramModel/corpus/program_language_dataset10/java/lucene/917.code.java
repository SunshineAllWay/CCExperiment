package org.apache.lucene.benchmark.byTask.utils;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import org.apache.lucene.benchmark.byTask.PerfRunData;
import org.apache.lucene.benchmark.byTask.tasks.PerfTask;
import org.apache.lucene.benchmark.byTask.tasks.RepSumByPrefTask;
import org.apache.lucene.benchmark.byTask.tasks.TaskSequence;
public class Algorithm {
  private TaskSequence sequence;
  public Algorithm (PerfRunData runData) throws Exception {
    String algTxt = runData.getConfig().getAlgorithmText();
    sequence = new TaskSequence(runData,null,null,false);
    TaskSequence currSequence = sequence;
    PerfTask prevTask = null;
    StreamTokenizer stok = new StreamTokenizer(new StringReader(algTxt));
    stok.commentChar('#');
    stok.eolIsSignificant(false);
    stok.ordinaryChar('"');
    stok.ordinaryChar('/');
    stok.ordinaryChar('(');
    stok.ordinaryChar(')');
    boolean colonOk = false; 
    boolean isDisableCountNextTask = false; 
    currSequence.setDepth(0);
    String taskPackage = PerfTask.class.getPackage().getName() + ".";
    while (stok.nextToken() != StreamTokenizer.TT_EOF) { 
      switch(stok.ttype) {
        case StreamTokenizer.TT_WORD:
          String s = stok.sval;
          Constructor<? extends PerfTask> cnstr = Class.forName(taskPackage+s+"Task")
            .asSubclass(PerfTask.class).getConstructor(PerfRunData.class);
          PerfTask task = cnstr.newInstance(runData);
          task.setDisableCounting(isDisableCountNextTask);
          isDisableCountNextTask = false;
          currSequence.addTask(task);
          if (task instanceof RepSumByPrefTask) {
            stok.nextToken();
            String prefix = stok.sval;
            if (prefix==null || prefix.length()==0) { 
              throw new Exception("named report prefix problem - "+stok.toString()); 
            }
            ((RepSumByPrefTask) task).setPrefix(prefix);
          }
          stok.nextToken();
          if (stok.ttype!='(') {
            stok.pushBack();
          } else {
            StringBuffer params = new StringBuffer();
            stok.nextToken();
            while (stok.ttype!=')') { 
              switch (stok.ttype) {
                case StreamTokenizer.TT_NUMBER:  
                  params.append(stok.nval);
                  break;
                case StreamTokenizer.TT_WORD:    
                  params.append(stok.sval);             
                  break;
                case StreamTokenizer.TT_EOF:     
                  throw new Exception("unexpexted EOF: - "+stok.toString());
                default:
                  params.append((char)stok.ttype);
              }
              stok.nextToken();
            }
            String prm = params.toString().trim();
            if (prm.length()>0) {
              task.setParams(prm);
            }
          }
          colonOk = false; prevTask = task;
          break;
        default:
          char c = (char)stok.ttype;
          switch(c) {
            case ':' :
              if (!colonOk) throw new Exception("colon unexpexted: - "+stok.toString());
              colonOk = false;
              stok.nextToken();
              if ((char)stok.ttype == '*') {
                ((TaskSequence)prevTask).setRepetitions(TaskSequence.REPEAT_EXHAUST);
              } else {
                if (stok.ttype!=StreamTokenizer.TT_NUMBER)  {
                  throw new Exception("expected repetitions number or XXXs: - "+stok.toString());
                } else {
                  double num = stok.nval;
                  stok.nextToken();
                  if (stok.ttype == StreamTokenizer.TT_WORD && stok.sval.equals("s")) {
                    ((TaskSequence) prevTask).setRunTime(num);
                  } else {
                    stok.pushBack();
                    ((TaskSequence) prevTask).setRepetitions((int) num);
                  }
                }
              }
              stok.nextToken();
              if (stok.ttype!=':') {
                stok.pushBack();
              } else {
                stok.nextToken();
                if (stok.ttype!=StreamTokenizer.TT_NUMBER) throw new Exception("expected rate number: - "+stok.toString());
                stok.nextToken();
                if (stok.ttype!='/') {
                  stok.pushBack();
                  ((TaskSequence)prevTask).setRate((int)stok.nval,false); 
                } else {
                  stok.nextToken();
                  if (stok.ttype!=StreamTokenizer.TT_WORD) throw new Exception("expected rate unit: 'min' or 'sec' - "+stok.toString());
                  String unit = stok.sval.toLowerCase();
                  if ("min".equals(unit)) {
                    ((TaskSequence)prevTask).setRate((int)stok.nval,true); 
                  } else if ("sec".equals(unit)) {
                    ((TaskSequence)prevTask).setRate((int)stok.nval,false); 
                  } else {
                    throw new Exception("expected rate unit: 'min' or 'sec' - "+stok.toString());
                  }
                }
              }
              colonOk = false;
              break;
            case '{' : 
            case '[' :  
              String name = null;
              stok.nextToken();
              if (stok.ttype!='"') {
                stok.pushBack();
              } else {
                stok.nextToken();
                name = stok.sval;
                stok.nextToken();
                if (stok.ttype!='"' || name==null || name.length()==0) { 
                  throw new Exception("sequence name problem - "+stok.toString()); 
                }
              }
              TaskSequence seq2 = new TaskSequence(runData, name, currSequence, c=='[');
              currSequence.addTask(seq2);
              currSequence = seq2;
              colonOk = false;
              break;
            case '&' :
              if (currSequence.isParallel()) {
                throw new Exception("Can only create background tasks within a serial task");
              }
              stok.nextToken();
              final int deltaPri;
              if (stok.ttype != StreamTokenizer.TT_NUMBER) {
                stok.pushBack();
                deltaPri = 0;
              } else {
                deltaPri = (int) stok.nval;
              }
              if (prevTask == null) {
                throw new Exception("& was unexpected");
              } else if (prevTask.getRunInBackground()) {
                throw new Exception("double & was unexpected");
              } else {
                prevTask.setRunInBackground(deltaPri);
              }
              break;
            case '>' :
              currSequence.setNoChildReport();
            case '}' : 
            case ']' : 
              colonOk = true; prevTask = currSequence;
              currSequence = currSequence.getParent();
              break;
            case '-' :
              isDisableCountNextTask = true;
              break;
          } 
          break;
      } 
    }
    if (sequence != currSequence) {
      throw new Exception("Unmatched sequences");
    }
    while (sequence.isCollapsable() && sequence.getRepetitions()==1 && sequence.getRate()==0) {
      ArrayList<PerfTask> t = sequence.getTasks();
      if (t!=null && t.size()==1) {
        PerfTask p = t.get(0);
        if (p instanceof TaskSequence) {
          sequence = (TaskSequence) p;
          continue;
        }
      }
      break;
    }
  }
  @Override
  public String toString() {
    String newline = System.getProperty("line.separator");
    StringBuffer sb = new StringBuffer();
    sb.append(sequence.toString());
    sb.append(newline);
    return sb.toString();
  }
  public void execute() throws Exception {
    try {
      sequence.runAndMaybeStats(true);
    } finally {
      sequence.close();
    }
  }
  public ArrayList<PerfTask> extractTasks() {
    ArrayList<PerfTask> res = new ArrayList<PerfTask>();
    extractTasks(res, sequence);
    return res;
  }
  private void extractTasks (ArrayList<PerfTask> extrct, TaskSequence seq) {
    if (seq==null) 
      return;
    extrct.add(seq);
    ArrayList<PerfTask> t = sequence.getTasks();
    if (t==null) 
      return;
    for (final PerfTask p : t) {
      if (p instanceof TaskSequence) {
        extractTasks(extrct, (TaskSequence)p);
      } else {
        extrct.add(p);
      }
    }
  }
}

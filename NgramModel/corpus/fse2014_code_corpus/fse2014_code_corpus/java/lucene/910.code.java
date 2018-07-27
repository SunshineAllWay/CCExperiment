package org.apache.lucene.benchmark.byTask.tasks;
import org.apache.lucene.benchmark.byTask.PerfRunData;
import org.apache.lucene.benchmark.byTask.feeds.QueryMaker;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
public class SearchWithSortTask extends ReadTask {
  private boolean doScore = true;
  private boolean doMaxScore = true;
  private Sort sort;
  public SearchWithSortTask(PerfRunData runData) {
    super(runData);
  }
  @Override
  public void setParams(String sortField) {
    super.setParams(sortField);
    String[] fields = sortField.split(",");
    SortField[] sortFields = new SortField[fields.length];
    int upto = 0;
    for (int i = 0; i < fields.length; i++) {
      String field = fields[i];
      SortField sortField0;
      if (field.equals("doc")) {
        sortField0 = SortField.FIELD_DOC;
      } if (field.equals("score")) {
        sortField0 = SortField.FIELD_SCORE;
      } else if (field.equals("noscore")) {
        doScore = false;
        continue;
      } else if (field.equals("nomaxscore")) {
        doMaxScore = false;
        continue;
      } else {
        int index = field.lastIndexOf(":");
        String fieldName;
        String typeString;
        if (index != -1) {
          fieldName = field.substring(0, index);
          typeString = field.substring(1+index, field.length());
        } else {
          throw new RuntimeException("You must specify the sort type ie page:int,subject:string");
        }
        int type = getType(typeString);
        sortField0 = new SortField(fieldName, type);
      }
      sortFields[upto++] = sortField0;
    }
    if (upto < sortFields.length) {
      SortField[] newSortFields = new SortField[upto];
      System.arraycopy(sortFields, 0, newSortFields, 0, upto);
      sortFields = newSortFields;
    }
    this.sort = new Sort(sortFields);
  }
  private int getType(String typeString) {
    int type;
    if (typeString.equals("float")) {
      type = SortField.FLOAT;
    } else if (typeString.equals("double")) {
      type = SortField.DOUBLE;
    } else if (typeString.equals("byte")) {
      type = SortField.BYTE;
    } else if (typeString.equals("short")) {
      type = SortField.SHORT;
    } else if (typeString.equals("int")) {
      type = SortField.INT;
    } else if (typeString.equals("long")) {
      type = SortField.LONG;
    } else if (typeString.equals("string")) {
      type = SortField.STRING;
    } else if (typeString.equals("string_val")) {
      type = SortField.STRING_VAL;
    } else {
      throw new RuntimeException("Unrecognized sort field type " + typeString);
    }
    return type;
  }
  @Override
  public boolean supportsParams() {
    return true;
  }
  @Override
  public QueryMaker getQueryMaker() {
    return getRunData().getQueryMaker(this);
  }
  @Override
  public boolean withRetrieve() {
    return false;
  }
  @Override
  public boolean withSearch() {
    return true;
  }
  @Override
  public boolean withTraverse() {
    return false;
  }
  @Override
  public boolean withWarm() {
    return false;
  }
  @Override
  public boolean withScore() {
    return doScore;
  }
  @Override
  public boolean withMaxScore() {
    return doMaxScore;
  }
  @Override
  public Sort getSort() {
    if (sort == null) {
      throw new IllegalStateException("No sort field was set");
    }
    return sort;
  }
}

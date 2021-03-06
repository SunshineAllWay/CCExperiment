package org.apache.solr.update;
import org.apache.lucene.store.Directory;
public class MergeIndexesCommand extends UpdateCommand {
  public Directory[] dirs;
  public MergeIndexesCommand() {
    this(null);
  }
  public MergeIndexesCommand(Directory[] dirs) {
    super("mergeIndexes");
    this.dirs = dirs;
  }
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder(commandName);
    sb.append(':');
    if (dirs != null && dirs.length > 0) {
      sb.append(dirs[0]);
      for (int i = 1; i < dirs.length; i++) {
        sb.append(",").append(dirs[i]);
      }
    }
    return sb.toString();
  }
}

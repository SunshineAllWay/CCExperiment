package org.apache.solr.update;
public class DeleteUpdateCommand extends UpdateCommand {
  public String id;    
  public String query; 
  public boolean fromPending;
  public boolean fromCommitted;
  public DeleteUpdateCommand() {
    super("delete");
  }
  public String toString() {
    StringBuilder sb = new StringBuilder(commandName);
    sb.append(':');
    if (id!=null) sb.append("id=").append(id);
    else sb.append("query=`").append(query).append('`');
    sb.append(",fromPending=").append(fromPending);
    sb.append(",fromCommitted=").append(fromCommitted);
    return sb.toString();
  }
}

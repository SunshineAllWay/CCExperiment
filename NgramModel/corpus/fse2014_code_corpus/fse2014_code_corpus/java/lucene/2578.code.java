package org.apache.solr.update;
  public class UpdateCommand {
    protected String commandName;
    public UpdateCommand(String commandName) {
      this.commandName = commandName;
    }
    public String toString() {
      return commandName;
    }
  }

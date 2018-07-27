package org.apache.log4j.varia;
import org.apache.log4j.Level;
import org.apache.log4j.spi.Filter;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.helpers.OptionConverter;
public class LevelMatchFilter extends Filter {
  boolean acceptOnMatch = true;
  Level levelToMatch;
  public
  void setLevelToMatch(String level) {
    levelToMatch = OptionConverter.toLevel(level, null);
  }
  public
  String getLevelToMatch() {
    return levelToMatch == null ? null : levelToMatch.toString();
  }
  public
  void setAcceptOnMatch(boolean acceptOnMatch) {
    this.acceptOnMatch = acceptOnMatch;
  }
  public
  boolean getAcceptOnMatch() {
    return acceptOnMatch;
  }
  public
  int decide(LoggingEvent event) {
    if(this.levelToMatch == null) {
      return Filter.NEUTRAL;
    }
    boolean matchOccured = false;
    if(this.levelToMatch.equals(event.getLevel())) {
      matchOccured = true;
    } 
    if(matchOccured) {  
      if(this.acceptOnMatch)
	  return Filter.ACCEPT;
      else
	  return Filter.DENY;
    } else {
      return Filter.NEUTRAL;
    }
  }
}

package org.apache.log4j.varia;
import org.apache.log4j.Level;
import org.apache.log4j.spi.Filter;
import org.apache.log4j.spi.LoggingEvent;
public class LevelRangeFilter extends Filter {
  boolean acceptOnMatch = false;
  Level levelMin;
  Level levelMax;
  public
  int decide(LoggingEvent event) {
    if(this.levelMin != null) {
      if (event.getLevel().isGreaterOrEqual(levelMin) == false) {
        return Filter.DENY;
      }
    }
    if(this.levelMax != null) {
      if (event.getLevel().toInt() > levelMax.toInt()) {
        return Filter.DENY;
      }
    }
    if (acceptOnMatch) {
      return Filter.ACCEPT;
    }
    else {
      return Filter.NEUTRAL;
    }
  }
  public
  Level getLevelMax() {
    return levelMax;
  }
  public
  Level getLevelMin() {
    return levelMin;
  }
  public
  boolean getAcceptOnMatch() {
    return acceptOnMatch;
  }
  public
  void setLevelMax(Level levelMax) {
    this.levelMax =  levelMax;
  }
  public
  void setLevelMin(Level levelMin) {
    this.levelMin =  levelMin;
  }
  public 
  void setAcceptOnMatch(boolean acceptOnMatch) {
    this.acceptOnMatch = acceptOnMatch;
  }
}

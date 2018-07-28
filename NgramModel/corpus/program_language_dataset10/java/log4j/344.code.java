package org.apache.log4j.util;
import org.apache.oro.text.perl.Perl5Util;
public class XMLTimestampFilter implements Filter {
  Perl5Util util = new Perl5Util();
  public 
  String filter(String in) {
    if(util.match("/timestamp=\"\\d{10,13}\"/", in)) {
      return util.substitute("s/timestamp=\"\\d{10,13}\"/timestamp=\"XXX\"/", in);
    } else {
      return in;
    }
  }
}

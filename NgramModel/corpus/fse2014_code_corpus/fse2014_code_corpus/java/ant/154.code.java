package org.apache.tools.ant.property;
import org.apache.tools.ant.PropertyHelper;
import java.text.ParsePosition;
public interface PropertyExpander extends PropertyHelper.Delegate {
    String parsePropertyName(String s, ParsePosition pos,
                             ParseNextProperty parseNextProperty);
}

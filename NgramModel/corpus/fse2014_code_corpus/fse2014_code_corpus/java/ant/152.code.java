package org.apache.tools.ant.property;
import java.text.ParsePosition;
import org.apache.tools.ant.Project;
public interface ParseNextProperty {
    Project getProject();
    Object parseNextProperty(String value, ParsePosition pos);
}

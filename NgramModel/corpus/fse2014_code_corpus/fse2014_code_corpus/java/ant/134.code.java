package org.apache.tools.ant.launch;
import java.util.Properties;
public interface AntMain {
    void startAnt(String[] args, Properties additionalUserProperties,
                  ClassLoader coreLoader);
}

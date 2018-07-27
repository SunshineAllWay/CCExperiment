package org.apache.tools.ant.taskdefs;
import java.io.IOException;
import java.net.URL;
import java.util.Iterator;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.Resource;
import org.apache.tools.ant.types.ResourceCollection;
import org.apache.tools.ant.types.resources.URLProvider;
import org.apache.tools.ant.types.resources.Union;
import org.apache.tools.ant.util.FileUtils;
public class CloseResources extends Task {
    private Union resources = new Union();
    public void add(ResourceCollection rc) {
        resources.add(rc);
    }
    public void execute() {
        for (Iterator it = resources.iterator(); it.hasNext(); ) {
            Resource r = (Resource) it.next();
            URLProvider up = (URLProvider) r.as(URLProvider.class);
            if (up != null) {
                URL u = up.getURL();
                try {
                    FileUtils.close(u.openConnection());
                } catch (IOException ex) {
                }
            }
        }
    }
}
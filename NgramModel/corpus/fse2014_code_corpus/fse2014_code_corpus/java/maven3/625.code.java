package org.apache.maven.cli;
import java.util.HashMap;
import java.util.Map;
import org.apache.maven.eventspy.EventSpy;
public class DefaultEventSpyContext
    implements EventSpy.Context
{
    private final Map<String, Object> data = new HashMap<String, Object>();
    public Map<String, Object> getData()
    {
        return data;
    }
}

package org.apache.maven.lifecycle.mapping;
import java.util.List;
import java.util.Map;
public interface LifecycleMapping
{
    String ROLE = LifecycleMapping.class.getName();
    List getOptionalMojos( String lifecycle );
    Map getPhases( String lifecycle );
}

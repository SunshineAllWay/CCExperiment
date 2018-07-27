package org.apache.maven.project.interpolation;
import org.apache.maven.model.Model;
import org.apache.maven.project.ProjectBuilderConfiguration;
import java.io.File;
import java.util.Map;
@Deprecated
public interface ModelInterpolator
{
    String DEFAULT_BUILD_TIMESTAMP_FORMAT = "yyyyMMdd-HHmm";
    String BUILD_TIMESTAMP_FORMAT_PROPERTY = "maven.build.timestamp.format";
    String ROLE = ModelInterpolator.class.getName();
    Model interpolate( Model project, Map<String, ?> context )
        throws ModelInterpolationException;
    Model interpolate( Model model, Map<String, ?> context, boolean strict )
        throws ModelInterpolationException;
    Model interpolate( Model model,
                       File projectDir,
                       ProjectBuilderConfiguration config,
                       boolean debugEnabled )
        throws ModelInterpolationException;
    String interpolate( String src,
                        Model model,
                        File projectDir,
                        ProjectBuilderConfiguration config,
                        boolean debugEnabled )
        throws ModelInterpolationException;
}

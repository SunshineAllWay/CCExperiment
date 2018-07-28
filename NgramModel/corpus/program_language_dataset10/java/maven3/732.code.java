package org.apache.maven.model.interpolation;
import org.apache.maven.model.Model;
import org.apache.maven.model.building.ModelBuildingRequest;
import org.apache.maven.model.building.ModelProblemCollector;
import java.io.File;
public interface ModelInterpolator
{
    Model interpolateModel( Model model, File projectDir, ModelBuildingRequest request,
                            ModelProblemCollector problems );
}

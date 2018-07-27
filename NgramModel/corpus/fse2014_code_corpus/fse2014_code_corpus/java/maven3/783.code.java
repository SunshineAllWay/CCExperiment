package org.apache.maven.model.validation;
import org.apache.maven.model.Model;
import org.apache.maven.model.building.ModelBuildingRequest;
import org.apache.maven.model.building.ModelProblemCollector;
public interface ModelValidator
{
    void validateRawModel( Model model, ModelBuildingRequest request, ModelProblemCollector problems );
    void validateEffectiveModel( Model model, ModelBuildingRequest request, ModelProblemCollector problems );
}

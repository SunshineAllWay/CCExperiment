package org.apache.maven.model.normalization;
import org.apache.maven.model.Model;
import org.apache.maven.model.building.ModelBuildingRequest;
import org.apache.maven.model.building.ModelProblemCollector;
public interface ModelNormalizer
{
    void mergeDuplicates( Model model, ModelBuildingRequest request, ModelProblemCollector problems );
    void injectDefaultValues( Model model, ModelBuildingRequest request, ModelProblemCollector problems );
}

package org.apache.maven.model.profile;
import org.apache.maven.model.Model;
import org.apache.maven.model.Profile;
import org.apache.maven.model.building.ModelBuildingRequest;
import org.apache.maven.model.building.ModelProblemCollector;
public interface ProfileInjector
{
    void injectProfile( Model model, Profile profile, ModelBuildingRequest request, ModelProblemCollector problems );
}

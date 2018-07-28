package org.apache.maven.model.inheritance;
import org.apache.maven.model.Model;
import org.apache.maven.model.building.ModelBuildingRequest;
import org.apache.maven.model.building.ModelProblemCollector;
public interface InheritanceAssembler
{
    void assembleModelInheritance( Model child, Model parent, ModelBuildingRequest request,
                                   ModelProblemCollector problems );
}

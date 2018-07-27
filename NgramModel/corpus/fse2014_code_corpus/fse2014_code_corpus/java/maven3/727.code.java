package org.apache.maven.model.composition;
import java.util.List;
import org.apache.maven.model.DependencyManagement;
import org.apache.maven.model.Model;
import org.apache.maven.model.building.ModelBuildingRequest;
import org.apache.maven.model.building.ModelProblemCollector;
public interface DependencyManagementImporter
{
    void importManagement( Model target, List<? extends DependencyManagement> sources, ModelBuildingRequest request,
                           ModelProblemCollector problems );
}

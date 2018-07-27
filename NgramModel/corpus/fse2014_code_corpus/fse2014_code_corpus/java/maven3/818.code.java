package org.apache.maven.settings.building;
import java.util.ArrayList;
import java.util.List;
import org.apache.maven.settings.Settings;
class DefaultSettingsBuildingResult
    implements SettingsBuildingResult
{
    private Settings effectiveSettings;
    private List<SettingsProblem> problems;
    public DefaultSettingsBuildingResult( Settings effectiveSettings, List<SettingsProblem> problems )
    {
        this.effectiveSettings = effectiveSettings;
        this.problems = ( problems != null ) ? problems : new ArrayList<SettingsProblem>();
    }
    public Settings getEffectiveSettings()
    {
        return effectiveSettings;
    }
    public List<SettingsProblem> getProblems()
    {
        return problems;
    }
}

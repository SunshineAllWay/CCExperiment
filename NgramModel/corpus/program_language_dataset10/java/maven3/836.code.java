package org.apache.maven.settings.crypto;
import java.util.List;
import org.apache.maven.settings.Proxy;
import org.apache.maven.settings.Server;
import org.apache.maven.settings.building.SettingsProblem;
public interface SettingsDecryptionResult
{
    Server getServer();
    List<Server> getServers();
    Proxy getProxy();
    List<Proxy> getProxies();
    List<SettingsProblem> getProblems();
}

package org.apache.tools.ant.taskdefs.optional.net;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
class FTPConfigurator {
    static FTPClient configure(FTPClient client, FTPTaskConfig task) {
        task.log("custom configuration", Project.MSG_VERBOSE);
        FTPClientConfig config;
        String systemTypeKey = task.getSystemTypeKey();
        if (systemTypeKey != null && !"".equals(systemTypeKey)) {
            config = new FTPClientConfig(systemTypeKey);
            task.log("custom config: system key = "
                    + systemTypeKey, Project.MSG_VERBOSE);
        } else {
            config = new FTPClientConfig();
            task.log("custom config: system key = default (UNIX)",
                    Project.MSG_VERBOSE);
        }
        String defaultDateFormatConfig = task.getDefaultDateFormatConfig();
        if (defaultDateFormatConfig != null) {
            config.setDefaultDateFormatStr(defaultDateFormatConfig);
            task.log("custom config: default date format = "
                    + defaultDateFormatConfig, Project.MSG_VERBOSE);
        }
        String recentDateFormatConfig = task.getRecentDateFormatConfig();
        if (recentDateFormatConfig != null) {
            config.setRecentDateFormatStr(recentDateFormatConfig);
            task.log("custom config: recent date format = "
                    + recentDateFormatConfig, Project.MSG_VERBOSE);
        }
        String serverLanguageCodeConfig = task.getServerLanguageCodeConfig();
        if (serverLanguageCodeConfig != null) {
            if (!"".equals(serverLanguageCodeConfig)
                && !FTPClientConfig.getSupportedLanguageCodes()
                .contains(serverLanguageCodeConfig)) {
                throw new BuildException("unsupported language code" +
                                         serverLanguageCodeConfig);
            }
            config.setServerLanguageCode(serverLanguageCodeConfig);
            task.log("custom config: server language code = "
                    + serverLanguageCodeConfig, Project.MSG_VERBOSE);
        }
        String serverTimeZoneConfig = task.getServerTimeZoneConfig();
        if (serverTimeZoneConfig != null) {
            config.setServerTimeZoneId(serverTimeZoneConfig);
            task.log("custom config: server time zone ID = "
                    + serverTimeZoneConfig, Project.MSG_VERBOSE);
        }
        String shortMonthNamesConfig = task.getShortMonthNamesConfig();
        if (shortMonthNamesConfig != null) {
            config.setShortMonthNames(shortMonthNamesConfig);
            task.log("custom config: short month names = "
                    + shortMonthNamesConfig, Project.MSG_VERBOSE);
        }
        client.configure(config);
        return client;
    }
}

package org.apache.batik.bridge;
public interface UpdateManagerListener {
    void managerStarted(UpdateManagerEvent e);
    void managerSuspended(UpdateManagerEvent e);
    void managerResumed(UpdateManagerEvent e);
    void managerStopped(UpdateManagerEvent e);
    void updateStarted(UpdateManagerEvent e);
    void updateCompleted(UpdateManagerEvent e);
    void updateFailed(UpdateManagerEvent e);
}

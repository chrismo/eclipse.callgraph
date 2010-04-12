package org.clabs.eclipse.plugin.callgraph;

import org.eclipse.core.runtime.IStatus;

public class CallgraphPluginStatus implements IStatus {

    private String fErrorMessage;

    public CallgraphPluginStatus(String errMessage) {
        fErrorMessage = errMessage;
    }

    public IStatus[] getChildren() {
        return null;
    }

    public int getCode() {
        return 0;
    }

    public Throwable getException() {
        return null;
    }

    public String getMessage() {
        return fErrorMessage;
    }

    public String getPlugin() {
        // what do I do here?
        return null;
    }

    public int getSeverity() {
        return ERROR;
    }

    public boolean isMultiStatus() {
        return false;
    }

    public boolean isOK() {
        return false;
    }

    public boolean matches(int severityMask) {
        return (getSeverity() & severityMask) != 0;
    }

}

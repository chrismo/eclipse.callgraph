package org.clabs.eclipse.plugin.callgraph;

import org.eclipse.jface.action.IAction;

public class MergeAndGraphViewActionDelegate extends GraphActionBase {
    public void run(IAction action) {
        fGrapher.setMergeWithPrevious(true);
        super.run(action);
    }

}

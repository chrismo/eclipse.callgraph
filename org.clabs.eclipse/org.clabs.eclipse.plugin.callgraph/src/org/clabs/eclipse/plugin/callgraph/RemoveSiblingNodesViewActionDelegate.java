package org.clabs.eclipse.plugin.callgraph;

import org.eclipse.jface.action.IAction;
import org.eclipse.swt.widgets.TreeItem;

public class RemoveSiblingNodesViewActionDelegate extends BaseViewActionDelegate {
    public void run(IAction action) {
        TreeItem[] selection = fCHTree.getSelection();
        if (selection.length > 0)
        {
            for (int i = 0; i < selection.length; i++) {
                TreeItem item = selection[i];
                removeSiblings(item);
            }
        }
    }
}

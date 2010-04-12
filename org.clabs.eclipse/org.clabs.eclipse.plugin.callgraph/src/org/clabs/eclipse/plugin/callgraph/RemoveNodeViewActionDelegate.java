package org.clabs.eclipse.plugin.callgraph;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.TreeItem;

public class RemoveNodeViewActionDelegate extends BaseViewActionDelegate {

    public void run(IAction action) {
        TreeItem[] selection = fCHTree.getSelection();
        if (selection.length > 0)
        {
            for (int i = 0; i < selection.length; i++) {
                TreeItem item = selection[i];
                item.dispose();
            }
        }
    }

    public void selectionChanged(IAction action, ISelection selection) {
    }

}

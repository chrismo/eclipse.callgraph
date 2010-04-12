package org.clabs.eclipse.plugin.callgraph;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.TreeItem;

public class IsolatePathViewActionDelegate extends BaseViewActionDelegate {
    public void run(IAction action) {
        TreeItem[] selection = fCHTree.getSelection();
        if (selection.length > 0)
        {
            for (int i = 0; i < selection.length; i++) {
                TreeItem item = selection[i];
                removeSiblingsRecursively(item);
            }
        }
    }

    public void removeSiblingsRecursively(TreeItem item) {
        if (item != null)
        {
            removeSiblings(item);
            removeSiblingsRecursively(item.getParentItem());
        }
    }
    
    public void selectionChanged(IAction action, ISelection selection) {
        if (selection instanceof IStructuredSelection) {
            IStructuredSelection structuredSelection = (IStructuredSelection) selection;
            action.setEnabled((structuredSelection.size() == 1));
        }
    }
}

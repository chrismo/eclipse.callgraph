package org.clabs.eclipse.plugin.callgraph;

import java.util.Vector;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.internal.corext.callhierarchy.MethodWrapper;
import org.eclipse.jdt.internal.ui.callhierarchy.CallHierarchyViewPart;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;

public class BaseViewActionDelegate implements IViewActionDelegate {

    public void removeSiblings(TreeItem item) {
        TreeItem parent = item.getParentItem();
        if (parent != null)
        {
            TreeItem[] parentChildren = parent.getItems();
            for (int j = 0; j < parentChildren.length; j++) {
                TreeItem sibling = parentChildren[j];
                if ((sibling != item) && (notSelected(sibling)))
                {
                    sibling.dispose();
                }
            }
        }
    }

    public boolean notSelected(TreeItem item) {
        TreeItem[] selection = fCHTree.getSelection();
        for (int i = 0; i < selection.length; i++) {
            TreeItem selectedItem = selection[i];
            if (selectedItem == item) {
                return false;
            }
        }
        return true;
    }

    public IViewPart fViewPart;
    public Shell fViewPartShell;
    public CallHierarchyViewPart fCHViewPart;
    public ITree fCHTree;
    public ILog fLog;
    public GraphCallHierarchy fGrapher;

    public void run(IAction action) {
    }

    public void selectionChanged(IAction action, ISelection selection) {
    }

    public void init(IViewPart view) {
    	fViewPart = view;
    	fViewPartShell = fViewPart.getViewSite().getShell();
        if (!(fViewPart instanceof CallHierarchyViewPart))
        {
            // TODO: Error handling
        }
        fCHViewPart = (CallHierarchyViewPart) fViewPart;
        findCallHierarchyTreeControl();
        fLog = CallgraphPlugin.getDefault().getLog();
        fGrapher = CallgraphPlugin.getGrapher();
    }

    public void findCallHierarchyTreeControl() {
        /*
         * Well ... I can't seem to find a direct way to get 
         * to the CallHierarchyViewer. It's a private member
         * of CallHierarchyViewPart which is not surfaced anywhere
         * protected or better. I tried a reflection routine to
         * get access that way (see CallHierarchyViewerAccess if 
         * it's still in the source here), but it won't let me 
         * through -- maybe I just need to hack on that better.
         * 
         * So, it's a diving down through the controls starting
         * at the Shell to find all the Tree controls.
         */
        Vector treeControls = new Vector();
        findTreeControls(fViewPartShell.getChildren(), treeControls);
        if (treeControls.size() == 0) {
            fLog.log(makeErrorStatus("no tree controls found"));
        }
        
        /*
         * We need to make sure we've got the right Tree control, the one for
         * the Call Hierarchy, not the Package Explorer, Outline or anything
         * else that might be living. So we'll take the root node, look at the
         * instanceof the data in the node, it should contain a MethodWrapper
         * instance.
         */
        Tree targetTree = null;
        
        for (int i = 0; i < treeControls.size(); i++) {
            Tree tree = (Tree) treeControls.elementAt(i);
            if (tree.getItemCount() > 0)
            {
                TreeItem rootItem = tree.getItems()[0];
                Object rootItemData = rootItem.getData();
                if ((rootItemData != null) && (rootItemData instanceof MethodWrapper)) {
                    targetTree = tree;
                    break;
                }
            }
        }
        
        if (targetTree != null) {
            fCHTree = new TreeAdapter(targetTree);
        }
        else {
            fLog.log(makeErrorStatus("no matching tree control found"));
        }
    }

    public IStatus makeErrorStatus(String errMessage) {
        return new CallgraphPluginStatus(errMessage);
    }

    public void findTreeControls(Control[] children, Vector treeControls) {
        for (int i = 0; i < children.length; i++) {
            Control control = children[i];
            if (control instanceof Tree) {
                treeControls.addElement(control);
            }
            if (control instanceof Composite) {
                Composite composite = (Composite) control;
                findTreeControls(composite.getChildren(), treeControls);
            }
        }
    }

}

package org.clabs.eclipse.plugin.callgraph;


import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;

public class ShowCallgraphViewActionDelegate extends GraphActionBase {

	public void selectionChanged(IAction action, ISelection selection) {
	}
	
    public void run(IAction action) {
        fGrapher.setMergeWithPrevious(false);
        super.run(action);
    }
    
	/**
     * Setter provided for unit test
     */
    public void setCallHierarchyTree(ITree tree) {
        fCHTree = tree;
    }
}

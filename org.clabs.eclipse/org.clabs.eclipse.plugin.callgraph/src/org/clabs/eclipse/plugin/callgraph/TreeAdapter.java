package org.clabs.eclipse.plugin.callgraph;

import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

public class TreeAdapter implements ITree {

    private Tree fTree;

    public TreeAdapter(Tree tree) {
        fTree = tree;
    }
    
    public TreeItem[] getSelection() {
        return fTree.getSelection();
    }

    public TreeItem getItem(int index) {
        return fTree.getItem(index);
    }

    /**
     * This is just a cheesy little way to get at the tree
     * to use auto-complete to explorer all the different
     * methods we might need to add to our adapter interface.
     */
    public Tree getTree() {
        return fTree;
    }
}

package org.clabs.eclipse.plugin.callgraph;

import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

/**
 * I believe this interface is primarily/solely here for an easy way
 * to mock it for unit testing.
 */
public interface ITree {
    public TreeItem[] getSelection();

    public TreeItem getItem(int i);
    
    public Tree getTree();
}

/*
 * Created on Jul 13, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.clabs.eclipse.plugin.callgraph;

import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.internal.corext.callhierarchy.MethodWrapper;
import org.eclipse.swt.widgets.TreeItem;


public class TreeItemAdapter implements ITreeItem {
	private TreeItem fTreeItem;
	
	public TreeItemAdapter(TreeItem treeItem) {
		fTreeItem = treeItem;
	}
	
	public boolean getExpanded() {
		return fTreeItem.getExpanded();
	}
	
	public ITreeItem[] getItems() {
		TreeItem[] items = fTreeItem.getItems();
		TreeItemAdapter[] adapterItems = new TreeItemAdapter[items.length];
		for (int i = 0; i < items.length; i++) {
			adapterItems[i] = new TreeItemAdapter(items[i]);
		}
		return adapterItems;
	}
	
	public String getText() {
		return fTreeItem.getText();
	}

    public IMember getMember() {
        Object data = fTreeItem.getData();
        if (data instanceof MethodWrapper) {
            MethodWrapper methodWrapper = (MethodWrapper) data;
            return methodWrapper.getMember();
        }
        return null;
    }
}
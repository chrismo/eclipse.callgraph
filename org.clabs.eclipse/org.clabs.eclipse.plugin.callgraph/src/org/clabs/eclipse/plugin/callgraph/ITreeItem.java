/*
 * Created on Jul 13, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.clabs.eclipse.plugin.callgraph;

import org.eclipse.jdt.core.IMember;


public interface ITreeItem {
	public boolean getExpanded();
	public ITreeItem[] getItems();
	public String getText();
    public IMember getMember();
}
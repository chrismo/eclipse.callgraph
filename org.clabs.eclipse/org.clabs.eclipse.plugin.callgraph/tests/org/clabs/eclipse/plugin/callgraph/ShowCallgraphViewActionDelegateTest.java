package org.clabs.eclipse.plugin.callgraph;

import junit.framework.TestCase;

import org.easymock.MockControl;
import org.eclipse.swt.widgets.TreeItem;

public class ShowCallgraphViewActionDelegateTest extends TestCase {

    private ShowCallgraphViewActionDelegate fAction;
    private ITree fMockTree;
    private MockControl fMockControl;

    protected void setUp() {
        fAction = new ShowCallgraphViewActionDelegate();
        setUpMocks();
    }
    
    private void setUpMocks() {
        fMockControl = MockControl.createControl(ITree.class);
        fMockTree = (ITree) fMockControl.getMock();
    }
    
    public void testGetTargetNodeWhenSelectionNull() {
        fMockControl.expectAndReturn(fMockTree.getSelection(), null);
        fMockControl.expectAndReturn(fMockTree.getItem(0), null);
        fMockControl.replay();
        
        fAction.setCallHierarchyTree(fMockTree);
        fAction.getTargetNode();
        fMockControl.verify();
    }

    public void testGetTargetNodeWhenSelectionEmpty() {
        fMockControl.expectAndReturn(fMockTree.getSelection(), new TreeItem[0]);
        fMockControl.expectAndReturn(fMockTree.getItem(0), null);
        fMockControl.replay();
        
        fAction.setCallHierarchyTree(fMockTree);
        fAction.getTargetNode();
        fMockControl.verify();
    }
}

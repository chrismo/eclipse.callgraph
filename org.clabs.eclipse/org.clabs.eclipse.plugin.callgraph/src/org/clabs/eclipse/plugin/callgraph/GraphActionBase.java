package org.clabs.eclipse.plugin.callgraph;

import java.io.IOException;

import org.eclipse.jdt.internal.corext.callhierarchy.MethodWrapper;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jface.action.IAction;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TreeItem;

public class GraphActionBase extends BaseViewActionDelegate {

    public void run(IAction action) {
        /*
         * Cheesy hack. CallerMethodWrapper and CalleeMethodWrapper are pkg
         * protected classes, so I can't do an instanceof on this. Maybe
         * there's a simpler way to find out the mode from a plug-in, but this
         * is what I've got right now.
         */
        MethodWrapper[] currentMethodWrappers = fCHViewPart.getCurrentMethodWrappers();
		String currentMethodWrapperClassName = currentMethodWrappers[0].getClass().getName();
        if (currentMethodWrapperClassName.endsWith("CallerMethodWrapper")) {
            fGrapher.setTreeInCallerMode();
        } else if (currentMethodWrapperClassName.endsWith("CalleeMethodWrapper")) {
            fGrapher.setTreeInCalleeMode();
        }
        
        /*
         * Think of this graph as a view of a view. Because the tree view
         * is not a very thin GUI on top of a model that represents the
         * tree, with knowledge of nodes that are collapsed, etc, if
         * we work off the .getCurrentMethodWrapper as the model for our
         * graph view the graph and tree will not be in sync (e.g. nodes
         * hidden in the tree because a parent is collapsed will still be
         * seen in the graph). This is why we work from the selected 
         * TreeItem instead of going behind-the-scenes.
         */
        ITreeItem itemRoot = new TreeItemAdapter(getTargetNode());
        
        Image image;
        try
        {
            image = fGrapher.getPNG(itemRoot);
            Display display = fViewPartShell.getDisplay();
            displayImage(itemRoot, image, display);			
        }
        catch (IOException e)
        {
            // TODO: Error handling!
            JavaPlugin.log(e);
        }
    }

    public TreeItem getTargetNode() {
        TreeItem[] selection = fCHTree.getSelection();
        if (selection != null) {
            if (selection.length > 0) {
                return selection[0];
            }
        }
        return fCHTree.getItem(0);
    }

    public void displayImage(ITreeItem item, Image image, Display display) {
    	// from http://dev.eclipse.org/viewcvs/index.cgi/%7Echeckout%7E/platform-swt-home/snippits/snippet48.html
    	// zooming ideas in this article: http://www.eclipse.org/articles/Article-Image-Viewer/Image_viewer.html
    	
    	final Shell shell = new Shell (display,
    		SWT.SHELL_TRIM | SWT.NO_BACKGROUND |
    		SWT.V_SCROLL | SWT.H_SCROLL);
    	shell.setBounds(5, 5, 795, 595);
    	shell.setText("Graph Call Hierarchy for " + item.getText());
    	shell.setBackground(image.getBackground());
    	final Point origin = new Point (0, 0);
    	final ScrollBar hBar = shell.getHorizontalBar ();
    	final Image thisImage = image;
    	hBar.addListener (SWT.Selection, new Listener () {
    		public void handleEvent (Event e) {
    			int hSelection = hBar.getSelection ();
    			int destX = (-hSelection * 1) - origin.x;
    			Rectangle rect = thisImage.getBounds ();
    			shell.scroll (destX, 0, 0, 0, rect.width, rect.height, false);
    			origin.x = (-hSelection * 1);
    		}
    	});
    	final ScrollBar vBar = shell.getVerticalBar ();
    	vBar.addListener (SWT.Selection, new Listener () {
    		public void handleEvent (Event e) {
    			int vSelection = vBar.getSelection ();
    			int destY = (-vSelection * 1) - origin.y;
    			Rectangle rect = thisImage.getBounds ();
    			shell.scroll (0, destY, 0, 0, rect.width, rect.height, false);
    			origin.y = (-vSelection * 1);
    		}
    	});
    	shell.addListener (SWT.Resize,  new Listener () {
    		public void handleEvent (Event e) {
    			Rectangle rect = thisImage.getBounds ();
    			Rectangle client = shell.getClientArea ();
    			hBar.setMaximum (rect.width);
    			vBar.setMaximum (rect.height);
    			hBar.setThumb (Math.min (rect.width, client.width));
    			vBar.setThumb (Math.min (rect.height, client.height));
    			int hPage = rect.width - client.width;
    			int vPage = rect.height - client.height;
    			int hSelection = hBar.getSelection ();
    			int vSelection = vBar.getSelection ();
    			if (hSelection >= hPage) {
    				if (hPage <= 0) hSelection = 0;
    				origin.x = -hSelection;
    			}
    			if (vSelection >= vPage) {
    				if (vPage <= 0) vSelection = 0;
    				origin.y = -vSelection;
    			}
    			shell.redraw ();
    		}
    	});
    	shell.addListener (SWT.Paint, new Listener () {
    		public void handleEvent (Event e) {
    			GC gc = e.gc;
    			gc.drawImage (thisImage, origin.x, origin.y);
    			Rectangle rect = thisImage.getBounds ();
    			Rectangle client = shell.getClientArea ();
    			int marginWidth = client.width - rect.width;
    			if (marginWidth > 0) {
    				gc.fillRectangle (rect.width, 0, marginWidth, client.height);
    			}
    			int marginHeight = client.height - rect.height;
    			if (marginHeight > 0) {
    				gc.fillRectangle (0, rect.height, client.width, marginHeight);
    			}
    		}
    	});
    	
    	// buffer vague guess to account for scrollbars and also title bar for .y
    	Point shellTrimBuffer = new Point(40, 80);
    	int shellWidth = image.getBounds().width + shellTrimBuffer.x;
    	int shellHeight = image.getBounds().height + shellTrimBuffer.y;
    	int maxShellWidth = display.getBounds().width - shellTrimBuffer.x;
    	int maxShellHeight = display.getBounds().height - shellTrimBuffer.y;
    	if (shellWidth > maxShellWidth) shellWidth = maxShellWidth;
    	if (shellHeight > maxShellHeight) shellHeight = maxShellHeight;
    	
    	shell.setSize(shellWidth, shellHeight);
    	shell.open();
    	while (!shell.isDisposed ()) {
    		if (!display.readAndDispatch ()) display.sleep ();
    	}
    }

}

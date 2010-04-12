package org.clabs.eclipse.plugin.callgraph;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;

import org.eclipse.jdt.internal.ui.callhierarchy.CallHierarchyMessages;
import org.eclipse.jdt.internal.ui.callhierarchy.CallHierarchyViewPart;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.TreeItem;

/**
 * @deprecated has the copy to clipboard code in here, so keep it around until
 * it's morphed into the new stuff
 */
public class CopyCallHierarchyGraphSourceAction extends Action {

	private CallHierarchyViewPart fView;
	//private CallHierarchyViewer fViewer;
	private Clipboard fClipboard;

	public CopyCallHierarchyGraphSourceAction(CallHierarchyViewPart view, Clipboard clipboard) {
		fView= view;
		fClipboard = clipboard;
        //fViewer= viewer;
	}

	/*
	 * @see IAction#run()
	 */
	public void run() {
		System.out.println("fView: " + fView.getViewSite().getClass().getName());
		if (true) return;
		
		StringBuffer buf= new StringBuffer();
        TreeItem root = null;// fViewer.getTree().getSelection()[0];
        GraphCallHierarchy grapher = new GraphCallHierarchy();
        ITreeItem item = new TreeItemAdapter(root);
        grapher.buildGraphSource(item, buf);

		TextTransfer plainTextTransfer = TextTransfer.getInstance();
		try{
			fClipboard.setContents(
				new String[]{ convertLineTerminators(buf.toString()) }, 
				new Transfer[]{ plainTextTransfer });
		}  catch (SWTError e){
			if (e.code != DND.ERROR_CANNOT_SET_CLIPBOARD) 
				throw e;
			if (MessageDialog.openQuestion(fView.getViewSite().getShell(), CallHierarchyMessages.CopyCallHierarchyAction_problem, CallHierarchyMessages.CopyCallHierarchyAction_clipboard_busy))  //$NON-NLS-1$ //$NON-NLS-2$
				run();
		}
	}
	
    private String convertLineTerminators(String in) {
		StringWriter stringWriter= new StringWriter();
		PrintWriter printWriter= new PrintWriter(stringWriter);
		StringReader stringReader= new StringReader(in);
		BufferedReader bufferedReader= new BufferedReader(stringReader);		
		String line;
		try {
			while ((line= bufferedReader.readLine()) != null) {
				printWriter.println(line);
			}
		} catch (IOException e) {
			return in; // return the call hierarchy unfiltered
		}
		return stringWriter.toString();
	}
}

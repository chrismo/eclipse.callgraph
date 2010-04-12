package org.clabs.eclipse.plugin.callgraph;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.Signature;
import org.eclipse.swt.graphics.Image;


public class GraphCallHierarchy {
    private boolean fMergeWithPrevious;
    private StringBuffer fCalls;
    private boolean fChildNodeFirst = true;
    private boolean fAbbreviateParamTypes = true;

    public void buildGraphSource(ITreeItem rootItem, StringBuffer buf) {
        buf.append("digraph G {\n");
        buf.append(getNodeDefinition());
        buf.append(getEdgeDefinition());
        buildCalls(rootItem);
        buf.append(fCalls.toString());
        // important to have the final \n to signal to dot stdin reader that we're done
        buf.append("}\n"); 
	}

    public void buildCalls(ITreeItem rootItem) {
        if ((!fMergeWithPrevious) || (fCalls == null)) 
        {
            fCalls = new StringBuffer();
        }
        addCalls(rootItem, fCalls);
    }

    public void addCalls(ITreeItem parentItem, StringBuffer buf) {
            
        if (parentItem.getExpanded()) {
            ITreeItem[] items= parentItem.getItems();
            for (int i= 0; i < items.length; i++) {
            	ITreeItem childItem = items[i];
            	String childCall = formatNodeText(childItem.getMember());
            	String parentCall = formatNodeText(parentItem.getMember());
                String firstCall, secondCall;
                if (fChildNodeFirst) {
                    firstCall = childCall;
                    secondCall = parentCall;
                } else {
                    firstCall = parentCall;
                    secondCall = childCall;
                }
                buf.append("\t\"" + firstCall + "\" -> \"" + secondCall + "\"");
                buf.append('\n');
                addCalls(childItem, buf);
            }
        }        
    }

    public String formatNodeText(IMember member) {
        if (member == null) {
            // TODO: Log error
            return "error - null member";
        }

        if (member instanceof IMethod) {
            IMethod method = (IMethod) member;
            return formatMethodNodeText(method);
        }
        else if (member instanceof IField) {
            IField field = (IField) member;
            return formatFieldNodeText(field);
        }
        else if (member instanceof IType) {
            IType type = (IType) member;
            return formatTypeNodeText(type);
        }
        else {
            // TODO: Log error
            return "error - unknown type: " + member.getClass().getName();
        }
    }

    private String formatTypeNodeText(IType type) {
        return type.getElementName();
    }

    private String formatFieldNodeText(IField field) {
        StringBuffer sb = new StringBuffer();
        sb.append(field.getParent().getElementName() + "\\n");
        sb.append(field.getElementName());
        return sb.toString();
    }

    private String formatMethodNodeText(IMethod method) {
        StringBuffer sb = new StringBuffer();
        sb.append(method.getParent().getElementName() + "\\n");
        sb.append(method.getElementName());
        String[] parameterTypes = method.getParameterTypes();
        sb.append("(");
        for (int i = 0; i < parameterTypes.length; i++) {
            String pType = parameterTypes[i];
            if (i > 0) {
                sb.append(",");
            }
            String simpleParamType = Signature.toString(pType);
            if (fAbbreviateParamTypes) {
                simpleParamType = abbreviateParameterType(simpleParamType);
            }
            sb.append(simpleParamType);
        }
        sb.append(")");
        return sb.toString();
    }
    
    private String abbreviateParameterType(String thisParam) {
        String newParam = "";
        for (int i = 0; i < thisParam.length(); i++) {
        	char c = thisParam.charAt(i);
        	if (((c >= 65) && (c <= 90)) || (c == 44)) {
                newParam += c;
            }
        }
        if (newParam.equals("")) {
        	newParam = thisParam.substring(0, 1);
        }
        return newParam;
    }
	
	public String getNodeDefinition() {
		// TODO make this user option
		return "\tnode [fontname=Helvetica, fontsize=8]\n";
	}
	
	public String getEdgeDefinition() {
		// TODO make this user option
		return "\tedge [fontname=Helvetica, fontsize=8]\n";
	}

	public Image getPNG(ITreeItem item) throws IOException {
		// make these user options
		String outputFormat = "png";
		
		StringBuffer buf = new StringBuffer();
		buildGraphSource(item, buf);
		
		String command = "dot";
		String params = "-T" + outputFormat;
		Process process = Runtime.getRuntime().exec(new String[] { command, params });
		OutputStream out = process.getOutputStream();
		InputStream in = process.getInputStream();
		out.write(buf.toString().getBytes());
		out.flush();
		Image image = new Image(null, in);
		return image;
	}

    public void setMergeWithPrevious(boolean bValue) {
        fMergeWithPrevious = bValue;
    }

    /**
     * Instructs the grapher to interpret parent items as the callee, and to
     * direct the nodes from child to parent. (Caller mode means root node
     * is the called method, the callee, and children are the callers).
     */
    public void setTreeInCallerMode() {
        fChildNodeFirst = true;
    }

    /**
     * Instructs the grapher to interpret parent items as the caller, and to
     * direct the nodes from parent to child. (Callee mode means root node
     * is the calling method, the caller, and children are the callees).
     */
    public void setTreeInCalleeMode() {
        fChildNodeFirst = false;
    }

    public StringBuffer getCalls() {
        return fCalls;
    }

    /**
     * If set to true, then all parameter types in the graph nodes will
     * be abbreviated to either the first letter of a primative type, or
     * only the capital letters of a class.
     */
    public void setAbbreviateParamTypesInGraph(boolean abbreviateParams) {
        fAbbreviateParamTypes = abbreviateParams;
    }
}

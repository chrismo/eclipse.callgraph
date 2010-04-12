package org.clabs.eclipse.plugin.callgraph;

import java.io.IOException;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.easymock.MockControl;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;

public class GraphCallHierarchyTest extends TestCase {
	private GraphCallHierarchy fGrapher;
    private IMethodFormattingTester fMethodTester;

	public void setUp() {
		fGrapher = new GraphCallHierarchy();
        fMethodTester = new IMethodFormattingTester("myMethod", "S,i", (new String[] {"QString;","I"}), "MyClass");
	}

    /**
     * Types can appear at least in this case (Java 5.0 code),
     * though it looks like a Call Hierarchy bug in Eclipse
     * 3.1. (bug from Adam Kiezun). 
     * <pre>
     * class I {
     *     Set foo() { //open callee hierarchy on foo()
     *         return new HashSet() {
     *             public boolean add(Object o) {
     *                 return super.add(o);
     *             }
     *         };
     *     }
     * }
     * </pre>
     */
    public void testITypeFormatting() {
        ITypeFormattingTester typeTester = new ITypeFormattingTester("MyClass");
        typeTester.doTest();
    }
    
    /**
     * Adam Kiezun reported this bug.
     */
    public void testIFieldFormatting() {
        IFieldFormattingTester fieldTester = new IFieldFormattingTester("myField", "MyClass");
        fieldTester.doTest();
    }
    
    public void testIMethodFormattingTwoParams() {
        fMethodTester.setAbbreviateParams(true);
        fMethodTester.doTest();
    }
    
    public void testIMethodFormattingTwoParamsAbbrev() {
        fMethodTester.setExpectedParamOutput("String,int");
        fMethodTester.setAbbreviateParams(false);
        fMethodTester.doTest();
    }

    public void testIMethodFormattingNoParams() {
        new IMethodFormattingTester("myMethod", "", (new String[] {}), "MyClass").doTest();
    }
    
    public void testIMethodFormattingNull() {
        assertEquals("error - null member", fGrapher.formatNodeText((IMethod) null));
    }
    
	public void testSimpleCallerTree() {
		StringBuffer buf = new StringBuffer();
        fGrapher.setTreeInCallerMode();
		fGrapher.buildGraphSource(makeSampleTree(), buf);
		
		String nodeDefinition = fGrapher.getNodeDefinition();
		String edgeDefinition = fGrapher.getEdgeDefinition();
		StringBuffer expected = new StringBuffer();
		expected.append("digraph G {\n");
		expected.append(nodeDefinition);
		expected.append(edgeDefinition);
		expected.append("\t\"ClassB\\nb(i)\" -> \"ClassA\\na()\"\n}\n");
		assertEquals(expected.toString(), buf.toString());
	}
	
	public void testSimpleCalleeTree() {
        StringBuffer buf = new StringBuffer();
        fGrapher.setTreeInCalleeMode();
        fGrapher.buildGraphSource(makeSampleTree(), buf);
        
        String nodeDefinition = fGrapher.getNodeDefinition();
        String edgeDefinition = fGrapher.getEdgeDefinition();
        StringBuffer expected = new StringBuffer();
        expected.append("digraph G {\n");
        expected.append(nodeDefinition);
        expected.append(edgeDefinition);
        expected.append("\t\"ClassA\\na()\" -> \"ClassB\\nb(i)\"\n}\n");
        assertEquals(expected.toString(), buf.toString());
    }

    private MockTreeItem makeSampleTree() {
        IMethodFormattingTester tester = new IMethodFormattingTester("b", "i", new String[] {"I"}, "ClassB");
        
		MockTreeItem itemB = new MockTreeItem((ITreeItem)null);
		itemB.setMember(tester.createMockMethod());

        tester.setMethodName("a");
        tester.setParams(new String[] {});
        tester.setClassName("ClassA");
		MockTreeItem itemA = new MockTreeItem(new ITreeItem[] { itemB });
		itemA.setMember(tester.createMockMethod());
		
		return itemA;
	}
	
	private MockTreeItem makeSampleTreeB(String methodPrefix) {
		MockTreeItem itemD = new MockTreeItem((ITreeItem)null);
		itemD.setText(methodPrefix + "d() - ClassB");

		MockTreeItem itemC = new MockTreeItem((ITreeItem)null);
		itemC.setText(methodPrefix + "c() - ClassB");

		MockTreeItem itemB = new MockTreeItem((ITreeItem)null);
		itemB.setText(methodPrefix + "b(int) - ClassB");

		MockTreeItem itemA = new MockTreeItem(new ITreeItem[] { itemB, itemC, itemD });
		itemA.setText(methodPrefix + "a() - org.clabs.ClassA");
		
		return itemA;
	}
	
	private MockTreeItem makeComplexSampleTree() {
		MockTreeItem item = makeSampleTreeB("a");
		MockTreeItem itemB = makeSampleTreeB("b");
		((MockTreeItem)item.fChildren[0]).setChildren(new ITreeItem[] { itemB });
		((MockTreeItem)itemB.fChildren[0]).setChildren(new ITreeItem[] { makeSampleTreeB("e") });
		
		MockTreeItem itemRoot = new MockTreeItem(new ITreeItem[] { item, makeSampleTreeB("c"), makeSampleTreeB("d") });
		itemRoot.setText("root() - ClassX");
		
		return itemRoot;
	}

	public void testSampleDisplay() throws IOException {
        /*
         * Test borken - constructing Image in the abstract like this no longer workee in 3.1
         * 
		ShowCallgraphViewActionDelegate graphAction = new ShowCallgraphViewActionDelegate();
		MockTreeItem item = makeComplexSampleTree();
		GraphCallHierarchy grapher = new GraphCallHierarchy();
		Image img = grapher.getPNG(item);
		graphAction.displayImage(item, img, Display.getDefault()); 
         */
	}
	
	public void testNodeAndEdgeGraphSettings() {
		// TODO 
	}

	public void testKeepingOnlyMultipleEdgesThatRepresentMultipleCallsBetweenSameMethods() {
		// TODO
		/* If method a() calls b() only one time, but this relationship
		 * appears multiple times in the hierarchy, we don't want multiple
		 * edges rendered in the graph.
		 * 
		 * However, if method a() calls b() more than once in its implementation
		 * (as shown in the Call Hierarchy tree with the (x Matches) text, then
		 * we do want those 'duplicate' edges rendered x times, possibly with an
		 * edge label.
		 */
	}
	
    public void testMergeGraph()
    {
        StringBuffer buf = new StringBuffer();
        fGrapher.buildGraphSource(makeSampleTree(), buf);
        
        String nodeDefinition = fGrapher.getNodeDefinition();
        String edgeDefinition = fGrapher.getEdgeDefinition();
        StringBuffer expected = new StringBuffer();
        expected.append("digraph G {\n");
        expected.append(nodeDefinition);
        expected.append(edgeDefinition);
        expected.append("\t\"ClassB\\nb(i)\" -> \"ClassA\\na()\"\n}\n");
        assertEquals(expected.toString(), buf.toString());
        
        fGrapher.setMergeWithPrevious(true);
        buf = new StringBuffer();
        fGrapher.buildGraphSource(makeSampleTree(), buf);
        expected = new StringBuffer();
        expected.append("digraph G {\n");
        expected.append(nodeDefinition);
        expected.append(edgeDefinition);
        expected.append("\t\"ClassB\\nb(i)\" -> \"ClassA\\na()\"\n");
        expected.append("\t\"ClassB\\nb(i)\" -> \"ClassA\\na()\"\n}\n");
        assertEquals(expected.toString(), buf.toString());
    }
    
	public void testGetPNG() throws IOException {
        /*
         * Test borken - constructing Image in the abstract like this no longer workee in 3.1
         * 
		Image img = fGrapher.getPNG(makeSampleTree());
         */
	}
	
	public static Test suite() {
		return new TestSuite(GraphCallHierarchyTest.class);
	}
	
    private class ITypeFormattingTester {
        private String fClassName;
        
        public ITypeFormattingTester(String className) {
            fClassName = className;
        }
        
        public void doTest() {
            String actual = fGrapher.formatNodeText(createMockType());
            assertEquals(fClassName, actual);
        }

        private IType createMockType() {
            MockControl mockControlType = MockControl.createControl(IType.class);
            IType mockType = (IType) mockControlType.getMock();
            
            mockControlType.expectAndReturn(mockType.getElementName(), fClassName);
            mockControlType.replay();
            
            return mockType;
        }
    }
    
    private class IFieldFormattingTester {
        private String fFieldName;
        private String fClassName;
        
        public IFieldFormattingTester(String fieldName, String className) {
            fFieldName = fieldName;
            fClassName = className;
        }
        
        public void doTest() {
            String actual = fGrapher.formatNodeText(createMockField());
            assertEquals(fClassName + "\\n" + fFieldName, actual);
        }

        private IField createMockField() {
            MockControl mockControlField = MockControl.createControl(IField.class);
            IField mockField = (IField) mockControlField.getMock();
            
            MockControl mockControlClassFile = MockControl.createControl(IClassFile.class);
            IClassFile mockClassFile = (IClassFile) mockControlClassFile.getMock();
            
            mockControlField.expectAndReturn(mockField.getParent(), mockClassFile);
            mockControlClassFile.expectAndReturn(mockClassFile.getElementName(), fClassName);
            mockControlField.expectAndReturn(mockField.getElementName(), fFieldName);
            mockControlClassFile.replay();
            mockControlField.replay();
            
            return mockField;
        }
    }
    
	private final class IMethodFormattingTester {
        private String fMethodName;
        private String fExpectedParamOutput;
        private String[] fParams;
        private String fClassName;
        private boolean fAbbreviateParams;

        private IMethodFormattingTester(String methodName, String expectedParamOutput, String[] params, String className) {
            super();
            this.fMethodName = methodName;
            this.fExpectedParamOutput = expectedParamOutput;
            this.fParams = params;
            this.fClassName = className;
            this.fAbbreviateParams = true;
        }

        void doTest() {
            fGrapher.setAbbreviateParamTypesInGraph(fAbbreviateParams);
            assertEquals(fClassName + "\\n" + fMethodName + "(" + fExpectedParamOutput + ")", fGrapher.formatNodeText(createMockMethod()));
        }

        public IMethod createMockMethod() {
            MockControl mockControlMethod = MockControl.createControl(IMethod.class);
            IMethod mockMethod = (IMethod) mockControlMethod.getMock();
            
            MockControl mockControlClassFile = MockControl.createControl(IClassFile.class);
            IClassFile mockClassFile = (IClassFile) mockControlClassFile.getMock();
            
            mockControlMethod.expectAndReturn(mockMethod.getParent(), mockClassFile);
            mockControlClassFile.expectAndReturn(mockClassFile.getElementName(), fClassName);
            mockControlMethod.expectAndReturn(mockMethod.getElementName(), fMethodName);
            mockControlMethod.expectAndReturn(mockMethod.getParameterTypes(), fParams);
            mockControlClassFile.replay();
            mockControlMethod.replay();
            
            return mockMethod;
        }
        
        public void setAbbreviateParams(boolean abbreviateParams) {
            this.fAbbreviateParams = abbreviateParams;
        }

        public void setClassName(String className) {
            fClassName = className;
        }

        public void setExpectedParamOutput(String expectedParamOutput) {
            fExpectedParamOutput = expectedParamOutput;
        }

        public void setMethodName(String methodName) {
            fMethodName = methodName;
        }

        public void setParams(String[] params) {
            fParams = params;
        }
    }

    protected class MockTreeItem implements ITreeItem {
		private String fText;
		ITreeItem[] fChildren = new MockTreeItem[] {};
        private IMember fMethod;

		public MockTreeItem(ITreeItem child) {
			if (child != null) {
				fChildren = new ITreeItem[] { child };
			}
		}
		
		public MockTreeItem(ITreeItem[] children) {
			setChildren(children);
		}

		public void setChildren(ITreeItem[] children) {
			if (children != null) {
				fChildren = children;
			}
		}

		public boolean getExpanded() {
			return true;
		}

		public ITreeItem[] getItems() {
			return fChildren;
		}

		public String getText() {
			return fText;
		}

		public void setText(String text) {
			fText = text;
		}

        public IMember getMember() {
            return fMethod;
        }
        
        public void setMember(IMember method) {
            fMethod = method;
        }
	}
}

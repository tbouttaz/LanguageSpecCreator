package WYSIWYM.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import WYSIWYM.model.Anchor;
import WYSIWYM.model.DTEdge;
import WYSIWYM.model.DTNode;
import WYSIWYM.model.DependencyTree;
import WYSIWYM.model.QueryValueNode;
import WYSIWYM.model.SGBooleanNode;
import WYSIWYM.model.SGDateNode;
import WYSIWYM.model.SGDoubleNode;
import WYSIWYM.model.SGEdge;
import WYSIWYM.model.SGIntNode;
import WYSIWYM.model.SGNode;
import WYSIWYM.model.SemanticGraph;
import WYSIWYM.model.UndeterminedDTNode;

/**	Dotter creates a dot-representation of the semantic graph, so it can be drawn
 *	with GraphViz
 *
 *	@author Feikje Hielkema
 *	@version 1.1
 */
public class Dotter
{
	private List<Anchor> anchors = new ArrayList<Anchor>();
	private List<String> nodesDone = new ArrayList<String>();
	
	/**	Maps the given graph to a dot representation
	 *	@param g  SemanticGraph
	 *	@return String with .dot representation
	 */
	public String dotSGGraph(SemanticGraph g)
	{
		StringBuffer sb = new StringBuffer();
		sb.append("digraph G {\n");
		sb.append("edge [decorate=true];\n");
		
		Iterator it = g.getEdges();
		while (it.hasNext())
		{
			SGEdge e = (SGEdge) it.next();
			sb.append(dotSGEdge(e));
		}
		
		sb.append("}");
		return sb.toString();
	}
	
	/**	Maps the given graph to a dot representation, marking the anchors as well
	 *	@param g	SemanticGraph
	 *	@param a 	List containing anchors
	 *	@return String with .dot representation
	 */
	public String dotSGGraph(SemanticGraph g, List<Anchor> a)
	{
		if (a != null)
			anchors = a;
			
		StringBuffer sb = new StringBuffer();
		sb.append("digraph G {\n");
		sb.append("edge [decorate=true];\n");
		
		Iterator it = g.getEdges();
		while (it.hasNext())
		{
			SGEdge e = (SGEdge) it.next();
			sb.append(dotSGEdge(e));
		}
		
		sb.append("}");
		return sb.toString();
	}
	
	/**	Maps the given dependency tree to a dot representation
	 *	@param g	DependencyTree
	 *	@return String with .dot representation
	 */
	public String dotDependencyTree(DependencyTree g)
	{
		StringBuffer sb = new StringBuffer();
		sb.append("digraph G {\n");
		sb.append("edge [decorate=true];\n");
		
		Iterator it = g.getEdges();
		if (!it.hasNext())
		{
			sb = new StringBuffer(dotDTNode((DTNode) g.getRoot(), sb.toString()));
		}
		while (it.hasNext())
		{
			DTEdge e = (DTEdge) it.next();
			sb.append(dotDTEdge(e));
		}
		
		sb.append("}");
		return sb.toString();
	}
	
	/**	Maps the given SGNode to a dot representation
	 *	@param node	SGNode
	 *	@param str	.dot representation so far
	 *	@return String with .dot representation of node appended to str
	 */
	public String dotSGNode(SGNode node, String str)
	{
		if (nodesDone.contains(node.getID()))
			return new String("");
		nodesDone.add(node.getID());
		StringBuffer sb = new StringBuffer(str);
		sb.append(" [label=\"");
		if ((node instanceof QueryValueNode) || (node instanceof SGDateNode) || (node instanceof SGIntNode) || (node instanceof SGDoubleNode) || (node instanceof SGBooleanNode))
			sb.append(node.getNLLabel(null));
		else
			sb.append(node.getLabel());
		
		if (node.mustRealise() == SGNode.HIDE)
			sb.append("HIDDEN");
		else if (node.mustRealise() == SGNode.INCOMPLETE)
			sb.append("incomplete");
		
		for (int i = 0; i < anchors.size(); i++)
		{
			Anchor a = (Anchor) anchors.get(i);
			if (a.getNode().getID().equals(node.getID()))
			{
				sb.append("\", color=\"");
				if (a.isRed())
					sb.append("red");
				else
					sb.append("green");
				break;
			}
		} 
		
		sb.append("\"]; \n");
		return sb.toString();
	}
	
	/**	Maps the given DTNode to a dot representation
	 *	@param	node DTNode
	 *	@param	str .dot representation so far
	 *	@return String with .dot representation of node appended to str
	 */
	public String dotDTNode(DTNode node, String str)
	{
		if (nodesDone.contains(node.getID()))
			return new String("");
		nodesDone.add(node.getID());
		StringBuffer sb = new StringBuffer(str);
		sb.append(" [label=\"");
		sb.append(node.getLabel());
		
		if (node instanceof UndeterminedDTNode)
			sb.append(":" + ((UndeterminedDTNode) node).getExpr());
		else if (node.isLeaf())
			sb.append(":" + node.getRoot());
		
		if (node.isElided())
			sb.append(" ELIDED");
		if (node.getInserted() == 1)
			sb.append(" SOURCE");
		else if (node.getInserted() == 2)
			sb.append(" TARGET");
		if ((node.getMorph() != null) && (!node.getMorph().isSingular()))
			sb.append(" plural");
		sb.append(" " + node.getID());
		sb.append("\"]; \n");
		return sb.toString();
	}
	
	/**	Maps the given SGEdge to a dot representation
	 *	@param	edge SGEdge
	 *	@return String with .dot representation
	 */
	public String dotSGEdge(SGEdge edge)
	{
		StringBuffer sb = new StringBuffer();
		String s = edge.getSource().getID();
		String t = edge.getTarget().getID();
		sb.append(dotSGNode((SGNode) edge.getSource(), s));
		sb.append(dotSGNode((SGNode) edge.getTarget(), t));
	
		sb.append(s);
		sb.append(" -> ");		
		sb.append(t);
		sb.append(" [label=\"");
		sb.append(edge.getLabel());
		if (edge.mustRealise() >= SGNode.HIDE)
			sb.append("HIDDEN");
		sb.append("\"]; \n");
		return sb.toString();
	}
	
	/**	Maps the given DTEdge to a dot representation
	 *	@param edge	DTEdge
	 *	@return String with .dot representation of node appended to str
	 */
	public String dotDTEdge(DTEdge edge)
	{
		StringBuffer sb = new StringBuffer();
		String s = edge.getSource().getID();
		String t = edge.getTarget().getID();
		sb.append(dotDTNode((DTNode) edge.getSource(), s));
		sb.append(dotDTNode((DTNode) edge.getTarget(), t));
	
		sb.append(s);
		sb.append(" -> ");		
		sb.append(t);
		sb.append(" [label=\"");
		sb.append(edge.getLabel());
		sb.append("\"]; \n");
		return sb.toString();
	}
}
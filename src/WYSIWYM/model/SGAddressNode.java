package WYSIWYM.model;
/***
 * SGAddressNode models an address; it has edges like 'street', 'number', etc.
 *	It is modelled separately because addresses are presented in a special way
 * 	in NL. *
 *
 * @author Feikje Hielkema
 * @version 1.2 2007/12/10
 */
 
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.naming.NameAlreadyBoundException;

import simplenlg.realiser.AnchorString;
import WYSIWYM.ontology.OntologyReader;
import WYSIWYM.transformer.SemanticGraphTransformer;

public class SGAddressNode extends SGNode
{
	/** Street name */
	public static final String STREET = "Street";
	/** country */
	public static final String COUNTRY = "Country";
	/** Place, town, city */
	public static final String PLACE = "Place";
	/** House number */
	public static final String HOUSENUMBER = "HouseNumber";
	/** Postal or zip code */
	public static final String POSTCODE = "Postcode";
	/** Telephone number */
	public static final String TELEPHONE = "TelephoneNumber";
	
	/**	Constructs the node with the given label
	 *	@param label String label
	 */
    public SGAddressNode(String label) 
    {
    	super(label);
    }
    
    /**	Initialises with all information of the given node
     *
     *	@param node Node to replace
     *	@param sgt Semantic Graph Transformer to which this must be added
     */
    public SGAddressNode(SGNode node, SemanticGraphTransformer sgt)
    {
    	super("Address");
    	setID(node.getID());
		if (node.isFinalLabel())
			setFinalNLLabel(new String(node.getFinalLabel()));		
		setRemovable(node.isRemovable());
		setQuote(node.isQuote());
		setOldLabel(node.getOldLabel());
		setSequenceNr(node.getSequenceNr());
		setRealise(node.mustRealise());
		setSGID(node.getSGID());
		
		try
		{	//replace the old node with the new
			sgt.replaceNode(node, this);			
		}
		catch (NameAlreadyBoundException e)
		{
			e.printStackTrace();
		}
    }
    
    /**	Returns all edges not part of the address
     *
     *	@return List<SGEdge>
     */
    public List<SGEdge> getOtherEdges()
    {
    	List<SGEdge> result = new ArrayList<SGEdge>();
    	for (Iterator it = getEdges(); it.hasNext(); )
    	{
    		SGEdge edge = (SGEdge) it.next();
    		if (isAddressEdge(edge))
    			continue;
    		if (edge.getLabel().equals("ID"))
    			continue;
    		result.add(edge);
    	}
    	return result;
    }
    
    /** Checks whether the given edge is part of the address
     *	@param e Edge
     *	@return true if this edge is part of an address
     */
    public boolean isAddressEdge(SGEdge e)
    {
    	String label = e.getLabel();
    	if (label.equals(STREET))
    		return true;
    	if (label.equals(COUNTRY))
    		return true;
    	if (label.equals(PLACE))
    		return true;
    	if (label.equals(HOUSENUMBER))
    		return true;
    	if (label.equals(POSTCODE))
    		return true;
    	if (label.equals(TELEPHONE))
    		return true;
    	return false;
    }
    
    /**	Returns the paragraph header
     *	@param reader Ontology
     *	@return List<AnchorString> with paragraph header
     */
    public List<AnchorString> getAddressHeader(OntologyReader reader)
    {
    	List<AnchorString> result = new ArrayList<AnchorString>();
    	result.add(new AnchorString("The address", getAnchor()));
    	List<SGEdge> edgeList = getOtherEdges();
    	int size = edgeList.size();
    	if (size == 0)
    		return result;
    		
    	result.add(new AnchorString(" of ", null));
    	for (int i = 0; i < (size - 2); i++)
    	{
    		result.add(getAnchorString(edgeList.get(i), reader));
    		result.add(new AnchorString(", ", null));
    	}
    	if (size > 1)
    	{
    		result.add(getAnchorString(edgeList.get(size - 2), reader));
    		result.add(new AnchorString("and ", null));
    	}
    	result.add(getAnchorString(edgeList.get(size - 1), reader));
    	return result;
    }
    
    private AnchorString getAnchorString(SGEdge edge, OntologyReader reader)
    {
    	SGNode target = edge.getTarget();
    	if (target.getID().equals(getID()))
    		target = edge.getSource();
    	
    	return new AnchorString(target.getChoiceLabel(reader), target.getAnchor());
    }
    
    /**	Orders the edges in the correct order for a British address
     *	@return List<Edge> with all ordered address edges
     */
    public List<Edge> getOrderedAddressEdges()
    {
    	List<Edge> result = new ArrayList<Edge>();
    	result.addAll(getOutgoingEdges(HOUSENUMBER));
    	result.addAll(getOutgoingEdges(STREET));
    	result.addAll(getOutgoingEdges(PLACE));
    	result.addAll(getOutgoingEdges(POSTCODE));
    	result.addAll(getOutgoingEdges(COUNTRY));
    	result.addAll(getOutgoingEdges(TELEPHONE));
    	return result;
    }
    
    /**	Returns the nl-representation of this address
     *	@return String with nl-representation
     */
    public String getRealisation()
    {
    	StringBuffer sb = new StringBuffer();
    	sb.append("<br>");	//start new line
    	List<Edge> edges = getOutgoingEdges(HOUSENUMBER);
    	if (edges.size() > 0)	//add the housenumber
    		sb.append(edges.get(0).getTarget().getLabel());
    	if (edges.size() == 2)
    	{
    		sb.append("-");
    		sb.append(edges.get(1).getTarget().getLabel());
    	}
    	else if (edges.size() > 2)
    		for (int i = 0; i < edges.size(); i++)
    			sb.append(", " + edges.get(i).getTarget().getLabel());
    	
    	edges = getOutgoingEdges(STREET);	//add the street
    	for (int i = 0; i < edges.size(); i++)
    		sb.append(" " + edges.get(i).getTarget().getLabel());
    	if (sb.length() > 0)
    		sb.append("<br>");	//add a linebreak
    	
    	edges = getOutgoingEdges(PLACE);	//add the country
    	for (int i = 0; i < edges.size(); i++)
    		sb.append(edges.get(i).getTarget().getLabel());
    	if (edges.size() > 0)
    		sb.append("<br>");	//add a linebreak
    	
    	edges = getOutgoingEdges(POSTCODE);	//add the country
    	for (int i = 0; i < edges.size(); i++)
    		sb.append(edges.get(i).getTarget().getLabel());
    	if (edges.size() > 0)
    		sb.append("<br>");	//add a linebreak
    	
    	edges = getOutgoingEdges(COUNTRY);	//add the country
    	for (int i = 0; i < edges.size(); i++)
    		sb.append(edges.get(i).getTarget().getLabel());
    	if (edges.size() > 0)
    		sb.append("<br>");	//add a linebreak
    	
    	edges = getOutgoingEdges(TELEPHONE);	//add the country
    	if (edges.size() == 0)
    		return sb.toString();
    	
    	sb.append("Tel.: ");
    	for (int i = 0; i < (edges.size() - 2); i++)
    		sb.append(edges.get(i).getTarget().getLabel() + ", ");
		if (edges.size() > 1)
			sb.append(edges.get(edges.size() - 2).getTarget().getLabel() + "or ");
		sb.append(edges.get(edges.size() - 1).getTarget().getLabel());
		return sb.toString();
    }
}
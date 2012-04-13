package WYSIWYM.model;

import java.util.ArrayList;
import java.util.List;

import simplenlg.realiser.AnchorString;
import WYSIWYM.ontology.OntologyReader;

/***
 *	SGAbstractNode is a SGStringNode that contains an abstract (a large piece of text).
 *
 * @author Feikje Hielkema
 * @version 1.2 2007/12/12
 */
public class SGAbstractNode extends SGStringNode
{
	/**	Constructs the node with this value
	 *	@param value String containing abstract
	 */
    public SGAbstractNode(String value) 
    {
    	super(value);
    }
    
	/**	Returns a header of the form '[parent]'s abstract:'
	 *
	 *	@param reader Ontology
	 *	@return List<AnchorString> with paragraph header
	 */
    public List<AnchorString> getHeader(OntologyReader reader)
    {
    	SGNode parent = (SGNode) getParent();
    	StringBuffer sb = new StringBuffer(parent.getNLLabel(reader));
    	if (sb.charAt(sb.length() - 1) == 's')
			sb.append("'");
		else
			sb.append("'s");
		Character.toUpperCase(sb.charAt(0));
			
    	List<AnchorString> result = new ArrayList<AnchorString>();
    	result.add(new AnchorString(sb.toString(), parent.getAnchor()));
    	result.add(new AnchorString(" abstract", null));
    	return result;
    }
}
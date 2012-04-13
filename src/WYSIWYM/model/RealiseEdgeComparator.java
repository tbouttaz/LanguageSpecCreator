package WYSIWYM.model;
 
 import java.util.Comparator;
 
/**	Compares two SGEdges WITH THE SAME LABEL by comparing the realise numbers, 
 *	which represent the order in which identical edges must be added.
 *	@author Feikje Hielkema
 *	@version 1.1 20-11-2007
 */
public class RealiseEdgeComparator implements Comparator
{
	/*	Compares two SGEdges by their realise numbers
	 *	@see Comparator#compareTo(Object,Object)
	 *	@param o1 Object
	 *	@param o2 Object
	 *	@return int, positive if o1 comes later, negative if o2 comes earlier
	 *	@throws ClassCastException if o1 or o2 cannot be cast to an SGEdge
	 */	
	public int compare(Object o1, Object o2) throws ClassCastException
	{
		try
		{
			SGEdge edge1 = (SGEdge) o1;
			SGEdge edge2 = (SGEdge) o2;
		
			int seq1 = edge1.getRealiseNr();
			int seq2 = edge2.getRealiseNr();
			if (seq1 > seq2)
				return 1;
			if (seq1 == seq2)
				return 0;
			return -1;
		}
		catch (ClassCastException e)
		{
			DTEdge edge1 = (DTEdge) o1;
			DTEdge edge2 = (DTEdge) o2;
			
			int order1 = edge1.getOrder();
			int order2 = edge2.getOrder();
			if (order1 > order2)
				return 1;
			if (order1 == order2)
				return 0;
			return -1;
		}
	}
	
	/*	Returns false
	 *	@see Comparator#equals(Object)
	 *	@param edge Object
	 *	@return false
	 */
	public boolean equals(Object edge)
	{
		return false;
	}
}
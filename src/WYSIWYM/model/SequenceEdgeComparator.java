package WYSIWYM.model;

import java.util.Comparator;

/**	Compares two SGEdges with respect to seniority, by comparing the sequence numbers,
 *	which represent the order in which edges are attached to enable undo.
 *
 *	@author Feikje Hielkema
 *	@version 1.2 15-01-2008
 */
public class SequenceEdgeComparator implements Comparator
{
	/*	Compares two SGEdges by their sequence numbers
	 *	@see Comparator#compareTo(Object,Object)
	 *	@param o1 Object
	 *	@param o2 Object
	 *	@return int, positive if o1 comes later, negative if o2 comes later
	 *	@throws ClassCastException if o1 or o2 cannot be cast to an SGEdge
	 */
	public int compare(Object o1, Object o2) throws ClassCastException
	{
		SGEdge edge1 = (SGEdge) o1;
		SGEdge edge2 = (SGEdge) o2;
	
		int seq1 = edge1.getSequenceNr();
		int seq2 = edge2.getSequenceNr();
		if (seq1 > seq2)
			return 1;
		if (seq1 == seq2)
			return 0;
		return -1;
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
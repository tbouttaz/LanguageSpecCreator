package WYSIWYM.model;


/**	QuerycGraph holds the query the user is building.
 *
 *	@author Feikje Hielkema
 *	@version 1.2 08-02-08
 */
public class QueryGraph extends SemanticGraph
{
	/**	Boolean operator for conjunction */
	public static final int AND = 0;
	/**	Boolean operator for disjunction */
	public static final int OR = 1;
	/**	Boolean operator for negation */
	public static final int NOT = 2;
	/**	Boolean operator for optional requirements */
	public static final int OPTIONAL = 3; 
	
	/**	Constructs a graph
	 *	@param user username
	 */
	public QueryGraph(String user)
	{
		super(user);
	}

	/**	Constructor, sets the given node as the root
	 *	@param r QueryNode	
	 *	@param user username
	 */
	public QueryGraph(QueryNode r, String user)
	{
		super(r, user);
	}
	
	/**	@see SemanticGraph#SemanticGraph(SemanticGraph)
	 */
	public QueryGraph(QueryGraph sg)
	{
		super(sg);
	}
	
	/**	Returns the root node
	 *	@return QueryNode
	 */
	public QueryNode getRoot()
	{
		return (QueryNode) super.getRoot();
	}
	
	/**	Returns the boolean operator that matches the given string,
	 *	or -1 if there is no match
	 *	@param operator String with nl-representation of operator
	 *	@return int	
	 *	@see #AND
	 *	@see #OR
	 *	@see #NOT
	 */
	public static int getOperator(String operator)
	{
		if (operator.equalsIgnoreCase("and"))
			return AND;
		if (operator.equalsIgnoreCase("or"))
			return OR;
		if (operator.equalsIgnoreCase("not"))
			return NOT;
		return -1;
	}
}
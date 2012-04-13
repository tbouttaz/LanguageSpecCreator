package WYSIWYM.model;

import WYSIWYM.ontology.OntologyReader;
import WYSIWYM.util.BadDateException;

/**	SGDateNode is a SGNode that instead of a String has a vague date.
 *
 *	@author Feikje Hielkema
 *	@version 1.1 31-01-2007
 *	@version 1.2 07-02-2008
 */
public class SGDateNode extends SGNode
{
	private Integer[] dates = new Integer[8];
	
	/**	Default constructor
	 */
	public SGDateNode()
	{
		super(OntologyReader.DATE);
	}
	
	/**	Constructor for a specific date (day, month, year)
	 *
	 *	@param day	Day
	 *	@param month	Month
	 *	@param year	Year
	 */
	public SGDateNode(int day, int month, int year)
	{
		super(OntologyReader.DATE);
		dates[0] = day;
		dates[2] = month;
		dates[4] = year;
	}
		
	/**	Constructor for any date, takes an array of strings representing
	 *	begin/end day/month/year, decade, century, era
	 *
	 *	@param input	Array of Strings
	 */
	public SGDateNode(String[] input) throws BadDateException
	{
		super(OntologyReader.DATE);
		setDate(input);
	}	
	
	/**	Constructs this node with the date from the given node
	 *
	 *	@param node SGDateNode
	 */
	public SGDateNode(SGDateNode node)
	{
		super(OntologyReader.DATE);
		dates = node.getDates();
	}
	
	/**	Returns the date values
	 *	@return Integer[]
	 */
	public Integer[] getDates()
	{
		return dates;
	}

	/*	Returns the natural language label
	 *	@return String
	 */
	public String getLabel()
	{
		String result = getNLLabel(null);
		if (result.equals("this date"))
			return OntologyReader.DATE;
		return result;
	}
	
	/**	Overload; restores the previous label (in this case always 'date')
	 *	and resets all values
	 *	@return true if the label could be restored
	 */
	public boolean restoreLabel()
	{
		for (int i = 0; i < dates.length; i++)
			if (dates[i] != null)
				return false;
			
		setLabel(OntologyReader.DATE);
		finalLabel = false;
		nlLabel = null;
		dates = new Integer[8];	//reset dates
		return true;
	}
	
	/**	Sets a new date
	 *
	 *	@param input	Array of Strings
	 *	@throws BadDateException when the input is invalid
	 */
	public void setDate(String[] input) throws BadDateException
	{
		for (int i = 0; i < 8; i++)
		{
			try
			{
				dates[i] = Integer.parseInt(input[i]);
			}
			catch (NumberFormatException e)
			{}
		}
		for (int i = 0; i < 2; i++)
			if ((dates[i] != null) && ((dates[i] < 1) || (dates[i] > 31)))
				throw new BadDateException("Day out of range");
		for (int i = 2; i < 4; i++)
			if ((dates[i] != null) && ((dates[i] < 1) || (dates[i] > 12)))
				throw new BadDateException("Month out of range");
	}
	
	/**	Generates and sets the natural language representation
	 */
	public void setNLLabel()
	{
		StringBuffer sb = new StringBuffer();			
		if (dates[5] != null)	//2 February 2002 - 3 March 2004
		{
			if (dates[0] != null)
			{
				sb.append(dates[0]);
				sb.append(" ");
			}
			if (dates[2] != null)
			{
				sb.append(getMonthString(dates[2]));
				sb.append(" ");
			}
			sb.append(dates[4]);
			sb.append(" - ");
			if (dates[1] != null)
			{
				sb.append(dates[1]);
				sb.append(" ");
			}
			if (dates[3] != null)
			{
				sb.append(getMonthString(dates[3]));
				sb.append(" ");
			}
			sb.append(dates[5]);
		}
		else if (dates[3] != null)	//2 February - 3 March 2004
		{
			if (dates[0] != null)
			{
				sb.append(dates[0]);
				sb.append(" ");
			}
			sb.append(getMonthString(dates[2]));
			sb.append(" - ");
			if (dates[1] != null)
			{
				sb.append(dates[1]);
				sb.append(" ");
			}
			sb.append(getMonthString(dates[3]));	
			if (dates[4] != null)
			{
				sb.append(" ");
				sb.append(dates[4]);
			}
		}
		else if (dates[1] != null)	//2 - 3 March 2004
		{
			sb.append(dates[0]);
			sb.append(" - ");
			sb.append(dates[1]);
			if (dates[2] != null)
			{
				sb.append(" ");
				sb.append(getMonthString(dates[2]));
			}
			if (dates[4] != null)
			{
				sb.append(" ");
				sb.append(dates[4]);
			}
		}
		else if (dates[4] != null)	//2 February 2007
		{
			if (dates[0] != null)
			{
				sb.append(dates[0]);
				sb.append(" ");
			}
			if (dates[2] != null)
			{
				sb.append(getMonthString(dates[2]));
				sb.append(" ");
			}
			sb.append(dates[4]);
		}
		else
		{
			if (dates[6] != null)
			{	//e.g. the 80's
				sb.append("the ");
				sb.append(Integer.toString(dates[6]));
				sb.append("'s");
			}
			if (dates[7] != null)
			{	//e.g. the 19th century
				if (sb.length() > 0)
					sb.append(" of ");
				sb.append("the ");
				sb.append(Integer.toString(dates[7]));
				switch(dates[7])
				{
					case 1: sb.append("st"); break;
					case 2: sb.append("nd"); break;
					case 3: sb.append("rd"); break;
					default: sb.append("th"); break;
				}	
				sb.append(" century");
			}
		}

		if (sb.length() == 0)	//no date specified yet
			nlLabel = "this date";
		else
			nlLabel = sb.toString();
	}
	
	/**	Returns the natural language representation
	 *	@param reader Ontology
	 *	@return String
	 */
	public String getNLLabel(OntologyReader reader)
	{
		if (!finalLabel)
			setNLLabel();
		return nlLabel;
	}
	
	/**	Returns the textual representation of the given month
	 *	@param	month number
	 *	@return String
	 */
	private String getMonthString(int m)
  	{
  		switch (m) 
  		{
         	case 1:  return " January ";
       	    case 2:  return " February ";
           	case 3:  return " March ";
           	case 4:  return " April ";
           	case 5:  return " May "; 
           	case 6:  return " June ";
          	case 7:  return " July ";
       		case 8:  return " August ";
           	case 9:  return " September ";
           	case 10: return " October ";
           	case 11: return " November ";
           	case 12: return " December ";
           	default: return "";
       	}
  	}
  	
  	/*	Returns a suitable preposition: 'on' for an exact date, in for a 
  	 *	month or year, 'during' or 'between' for a period.
  	 *
  	 *	@param query If true the type of preposition may be different
  	 *	@return String with preposition
  	 */
  	public String getPreposition(boolean query)
  	{
  		int type = getType();
  		if (query)
  			type = getQueryType();
  		switch(type)
  		{
  			case 0: return "on";
  			case 1: return "between";
  			case 2: return "in";
  			case 3: return "during";
  		}
  		return null;
  	}
  	
  	/**	Returns 0 for specific date (needing preposition "on"), 1 for a specified
  	 *	period ("between x and y") and 2 for an era ("in")
  	 *
  	 *	@return int
  	 */
  	public int getType()
  	{
  		if ((dates[6] != null) || (dates[7] != null))
  			return 2;
  		if ((dates[1] != null) || (dates[3] != null) || (dates[5] != null))
  			return 1;
  		if ((dates[0] == null) || (dates[2] == null))	//e.g. 'in March 2007', or 'in 2004'
  			return 2;
  		return 0;	//a specific date
  	}
  	
  	/**	Returns 0 for specific date ("on"), 1 for specified period ("between x and y"),
  	 *	2 for a period ("in march 2006") and 3 for an era ("during")
  	 *
  	 *	@return int
  	 */
  	public int getQueryType()
  	{
  		if ((dates[6] != null) || (dates[7] != null))
  			return 3;
  		if ((dates[1] != null) || (dates[3] != null) || (dates[5] != null))
  			return 1;
  		if ((dates[0] == null) || (dates[2] == null))	//e.g. 'in March 2007', or 'in 2004'
  			return 2;
  		return 0;
  	}
  	
  	/*	Returns null as SGDateNodes do not need determiners
  	 *	@see SGNode#getDeterminer(int)
  	 */
  	public String getDeterminer(int type)
	{
		return null;
	}
	
	/*	Checks whether this date marks a point or a period
	 *	@return true if this is a period
	 */
	public boolean isPeriod()
	{
		if ((dates[1] != null) || (dates[3] != null) || (dates[5] != null) || (dates[6] != null) || (dates[7] != null))
  			return true;
  		return false;
	}
	
	/*	Returns the six integers that make the start and end dates
	 *	@return Integer[]
	 */
	public Integer[] getDayMonthYear()
	{
		Integer[] result = new Integer[6];
		for (int i = 0; i < 6; i++)
			result[i] = dates[i];
			
		if ((dates[6] != null) && (!dates[6].equals("--")))
		{
			if (dates[7] == null)
				dates[7] = 20;
			int year = ((dates[7] - 1) * 100) + dates[6];	//e.g. 20 & 80 becomes 1980
			result[4] = year;
			result[5] = year + 9;
		}
		else if ((dates[7] != null) && (!dates[7].equals("--")))
		{
			int year = (dates[7] - 1) * 100;	//e.g. 20 becomes 1900
			result[4] = year;
			result[5] = year + 99;
		}
		return result;
	}
	
	/*	Sets a date value
	 *	@param property Property name
	 *	@param i Integer
	 */
	public void setValue(String property, Integer i)
	{
		if (property.equals(OntologyReader.DAY) || property.equals(OntologyReader.BEGINDAY))
			dates[0] = i;
		else if (property.equals(OntologyReader.MONTH) || property.equals(OntologyReader.BEGINMONTH))
			dates[2] = i;
		else if (property.equals(OntologyReader.YEAR) || property.equals(OntologyReader.BEGINYEAR))
			dates[4] = i;
		else if (property.equals(OntologyReader.ENDDAY))
			dates[1] = i;
		else if (property.equals(OntologyReader.ENDMONTH))
			dates[3] = i;
		else if (property.equals(OntologyReader.ENDYEAR))
			dates[5] = i;
	}
	
	/**	Returns an nl-expression for this date with the given comparator, e.g.
	 *	'before March 2008'
	 *	@param comp	comparator (before, after, during)
	 *	@return String with nl-expression
	 */
	public String getQueryNLExpression(int comp)
	{
		StringBuffer sb = new StringBuffer();
		if (comp == 1)
			sb.append("Before ");
		else if (comp == 2)
			sb.append("After ");
		else
		{
			switch(getQueryType())
			{
				case 0: sb.append("On "); break;
				case 1: sb.append("Between "); break;
				case 2: sb.append("In "); break;
				case 3: sb.append("During "); break;
			}
		}
		sb.append(getNLLabel(null));
		return sb.toString();
	}
}
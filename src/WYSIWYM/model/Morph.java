package WYSIWYM.model;

import WYSIWYM.libraries.LinguisticTerms;

/***
 *	Morph holds the morphological information of a DTNode
 *
 * @author Feikje Hielkema
 * @version 1.00 2006/11/14
 *
 *	@version 1.4 2008/07/30
 */
public class Morph 
{
	private boolean singular = true, passive = false;
	private boolean genitive = false, quote = false, negated = false, rank = false;
	
	private int person = LinguisticTerms.THIRD;
	private String tense = LinguisticTerms.PRESENT;
	private String particle = null;

	/**	Default constructor, with default values for all morphology properties
	 */
	public Morph()
	{}

	/**	Constructor
	 *	@param s boolean, true if singular
	 */
    public Morph(boolean s) 
    {
    	singular = s;
    }
    
    /**	Constructor
	 *	@param s String, should be "sing" or "plural" (@see LinguisticTerms#SINGULAR, @see LinguisticTerms#PLURAL)
	 */
    public Morph(String s) 
    {
    	if (s.equals(LinguisticTerms.PLURAL))
    		singular = false;
    }
    
    /**	Constructor
	 *	@param s boolean, true if singular
	 *	@param p boolean, true if passive (only for verbs)
	 */
    public Morph(boolean s, boolean p)
    {
    	singular = s;
    	passive = p;
    }
    
    /**	Constructor. If any argument is null, its value is set to the default.
	 *	@param s boolean, true if singular
	 *	@param p int person (1, 2 or 3)	(only for verbs)
	 *	@param t Verb tense
	 *	@param a boolean, true if passive (only for verbs)
	 */
    public Morph(boolean s, int p, String t, boolean a) 
    {
    	singular = s;
	   	person = p;
	    if (t != null)
	    	tense = t;
    	passive = a;
    }
    
    /**	Constructor. If any argument is null, its value is set to the default.
     *	@see LinguisticTerms for permissable values for these String arguments
	 *	@param s singular or plual
	 *	@param p person (1, 2, 3) (only for verbs)
	 *	@param t Verb tense (only for verbs)
	 *	@param a String passive or active	(only for verbs)
	 */
    public Morph(String s, int p, String t, String a) 
    {
    	if ((s!= null) && s.equals(LinguisticTerms.PLURAL))
    		singular = false; 	
  
       	person = p;
    	if (t != null)
	    	tense = t;
    	
    	if ((a != null) && a.equals(LinguisticTerms.PASSIVE))
    		passive = true;
    }
    
    /**	Constructor. If any argument is null, its value is set to the default.
     *	@see LinguisticTerms for permissable values for these String arguments
	 *	@param s String singular or plural
	 *	@param p String person (1, 2, 3)	(only for verbs)
	 *	@param t String tense (past, present, future) 	(only for verbs)	
	 *	@param a String passive or active	(only for verbs)
	 *	@param g String genitive 	(only for nouns)
	 *	@param q String quote	(for words or phrases)
	 *	@param r String ranking number (e.g. 'second' instead of 'two')
	 */
    public Morph(String s, String p, String t, String a, String g, String q, String r) 
    {
	    if ((s!= null) && s.equals(LinguisticTerms.PLURAL))
    		singular = false; 	
    
    	if (p != null)
	    	person = Integer.parseInt(p);
    
		if (t != null)
	    	tense = t;
 	
 	    if ((a != null) && a.equals(LinguisticTerms.PASSIVE))
    		passive = true;
    	
    	if ((g != null) && g.equals(LinguisticTerms.GENITIVE))
    		genitive = true;
    	
    	if ((q != null) && q.equals(LinguisticTerms.QUOTE))
     		quote = true;
     		
     	if ((r != null) && r.equals(LinguisticTerms.RANKORDER))
     		rank = true;
    }   
    
    /** Copies the morphology information from the given Morph
     *	@param old Morph to copy
     */
    public Morph(Morph old)
    {
    	copy(old);
    }
    
    /** Checks whether the morphology settings are all the default settings
     *	@return true if every setting matches its default
     */
    public boolean isStandard()
    {
    	if (passive || genitive || negated || rank)
    		return false;
    	if (person != LinguisticTerms.THIRD)
    		return false;
    	if (!tense.equals(LinguisticTerms.PRESENT))
    		return false;
    	if (particle != null)
    		return false;
    	return true;
    }
    
    /** Checks whether the given Morph has the same settings. Singular/plural
     *	is ignored.
     *	@param m Morph
     *	@return true if every setting but singular/plural is identical
     */
    public boolean equals(Morph m)
    {
    //	if (singular ^ m.isSingular())	//don't care about number when we're testing for conjunction
    //		return false;
    	if (passive ^ m.isPassive())
    		return false;
    	if (genitive ^ m.isGenitive())
    		return false;
    	if (negated ^ m.isNegated())
    		return false;
    	if (rank ^ m.isRankOrdered())
    		return false;
    	if (person != m.getPerson())
    		return false;   	
    	return tense.equals(m.getTense());
    }
    
    /**	Sets the negated setting (only for verbs)
     *	@param n true if the verb should be negated
     */
    public void setNegated(boolean n)
    {
    	negated = n;
    }
    
    /**	Gets the negated setting (only for verbs)
     *	@return true if the verb must be negated
     */
    public boolean isNegated()
    {
    	return negated;
    }
    
    /**	Retrieves the verb particle (unused)
     *	@return String
     *	@deprecated
     */
    public String getParticle()
    {
    	return particle;
    }
    
    /**	Sets the verb particale (unused)
     *	@param p String particle
     *	@deprecated
     */
    public void setParticle(String p)
    {
    	particle = p;
    }
    
    /**	Copies the morphological information of the given Morph to this one
     *	@param m	Morph
     */
    public void copy(Morph m)
    {
    	if (!m.isSingular())
    		singular = false;
    	if (m.isPassive())
    		passive = true;
    	if (m.isGenitive())
    		genitive = true;
    	if (m.isQuote())
    		quote = true;
    	if (!tense.equals(m.getTense()))
    		tense = m.getTense();
    	if (person != m.getPerson())
    		person = m.getPerson();
    	if (m.isNegated())
    		negated = true;
    }
    
    /**	Returns whether this should be passive (only for verbs)
     *	@return true if this verb should be passive
     */	
   	public boolean isPassive()
   	{
   		return passive;
   	}
   	
   	/**	Returns whether this is genitive case
     *	@return true if genitive case
     */	
   	public boolean isGenitive()
   	{
   		return genitive;
   	}
   	
   	/**	Returns whether this is singular or plural
     *	@return true if singular
     */	
   	public boolean isSingular()
   	{
   		return singular;
   	}
   	
   	/**	Returns whether this is quotes should be added around this node
     *	@return true if quotes
     */	
   	public boolean isQuote()
   	{
   		return quote;
   	}
   	
   	/**	Sets the genitive case
     *	@param g true if this should be genitive case
     */
   	public void setGenitive(boolean g)
   	{
   		genitive = g;
   	}
   	
   	/**	Sets the quotes
     *	@param q true if quotes should be added
     */
   	public void setQuote(boolean q)
   	{
   		quote = q;
   	}
   	
   	/**	Sets the singular/plural setting
     *	@param s if this should be singular
     */
   	public void setSingular(boolean s)
   	{
   		singular = s;
   	}
   	
   	/**	Sets passive  (only for verbs)
   	 *	@param p true if this should be passive
   	 */
   	public void setPassive(boolean p)
   	{
   		passive = p;
   	}
   	
   	/**	Returns the tense  (only for verbs)
   	 *	@return String
   	 */
   	public String getTense()
   	{
   		return tense;
   	}
   	
  	/**	Sets the tense  (only for verbs)
   	 *	@param s Verb tense
   	 */
   	public void setTense(String s)
   	{
   		tense = s;
   	}
   	
   	/**	Retrieves the person (1, 2, 3)  (only for verbs)
   	 *	@return int
   	 */
   	public int getPerson()
   	{
   		return person;
   	}
   	
   	/**	Returns whether this should be ranked (e.g. second instead of 2) (only for numbers)
     *	@return true if number should be rank number 
     */	
   	public boolean isRankOrdered()
   	{
   		return rank;
   	}
}
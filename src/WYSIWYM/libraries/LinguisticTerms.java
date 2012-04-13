package WYSIWYM.libraries;

/**
 *	LinguisticTerms contains linguistic constants used in the NLG-package, such as 'subject', 'noun', etc.
 *	It also contains terms for ellipsis structures and morphology.
 *
 * @author Feikje Hielkema
 * @version 1.00 2006/11/13
 *
 *	@version 1.4	2008/07/30
 */
public class LinguisticTerms 
{
	//Dependency terms
	public static final String SUBJECT  = "su";
    public static final String OBJECT  = "obj";
    public static final String HEAD  = "hd";
    public static final String MODIFIER  = "mod";
    public static final String PPMODIFIER  = "ppmod";
    public static final String COMPLEMENT  = "comp";
    public static final String CONJUNCT  = "cnj";
    public static final String CONJUNCTOR  = "cnjt";
    public static final String ROOT = "root";
    public static final String MODAL = "modal";
    
    //syntactic terms
    public static final String NOUN  = "noun";
    public static final String PRONOUN  = "pronoun";
    public static final String VERB  = "verb";
    public static final String ADJECTIVE = "adj";
    public static final String ADVERB = "adv";
    public static final String PREP  = "prep";
    public static final String DET  = "det";
    public static final String NP  = "np";
    public static final String VP  = "vp";
    public static final String AP  = "ap";
    public static final String PP  = "pp";
    public static final String SMAIN  = "smain";
    public static final String SSUB = "ssub";
    public static final String CONJUNCTION  = "conjunction";
    
    //morphological terms
    public static final String SINGULAR  = "sing";
	public static final String PLURAL  = "plural";
	public static final String ACTIVE  = "active";
	public static final String PASSIVE  = "passive";
	public static final String GENITIVE  = "genitive";
	public static final int FIRST  = 1;
	public static final int SECOND  = 2;
	public static final int THIRD  = 3;
	public static final String RANKORDER = "rank";
	public static final String PRESENT  = "present";
	public static final String PAST  = "past";
	public static final String FUTURE  = "future";
	public static final String PRESENTPERFECT  = "present perfect";
	public static final String PASTPERFECT  = "past perfect";
	public static final String INFINITIVE  = "infinitive";

	public static final String QUOTE = "quote";
	
	public static final String MALE = "male";
	public static final String FEMALE = "female";
	public static final String NEUTRAL = "neutral";
	public static final String HERMAFRODITE = "hermafrodite";
	
	public static final int CC = 1;		//conjunct coordination
	public static final int GAPPED = 2;	//gapping, eliding the verb
	public static final int CR = 3;		//conjunction reduction, eliding the subject
	public static final int RNR = 4;	//right node raising, eliding complement, pp-modifier
	public static final int NOTAGGREGATED = 0;
	
	public static final String PREDICATE = "predicate";
	
	/**	Checks whether the the given combination of syntactic and dependency
	 *	label is a constituent (e.g. NP) or not (e.g. noun).
	 *
	 *	@param syn Syntactic category
	 *	@param dep Dependency label
	 *	@return true if this is a constituent
	 */
	public static boolean isConstituent(String syn, String dep)
	{
		if (dep.equals(DET))
			return false;
		if (syn.equals(NOUN) || syn.equals(DET) || syn.equals(ADJECTIVE) || syn.equals(ADVERB) || syn.equals(PREP))
			return false;
		return true;
	}	
}
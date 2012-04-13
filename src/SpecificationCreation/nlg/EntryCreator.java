package SpecificationCreation.nlg;

import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jdom.Comment;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.XMLOutputter;

import WYSIWYM.libraries.Lexicon;
import WYSIWYM.libraries.LinguisticTerms;
import WYSIWYM.model.DTNode;
import WYSIWYM.model.Morph;
import WYSIWYM.transformer.DependencyTreeTransformer;

/**
 *	EntryCreator reads the template specifications, and stores lexicon entries for new properties.
 *
 * @author Feikje Hielkema
 * @version 1.0 10/11/2008
 */
public class EntryCreator
{
	private String property, root, rangeExample;
	/**	Path to specification templates */
	public static final String PATH = "data/lexicon-templates/";
	/** XML-label for the location the predicate is inserted in the template */
	public static final String PREDICATE = "predicate";
	private List elementList;
	
	/**	Constructor, takes the name of the new property
	 *	@param prop Property name
	 *	@param r Root of predicate word
	 */
	public EntryCreator(String prop, String r)
	{
		property = prop;	//the property name
		root = r;		//the root of the specification's predicate, given by the user
	}
	
	/**	Sets the example value of the range, for datatype properties. For instance,
	 *	the property 'email' would have 'john@fiction.co.uk'
	 *	@param r Example String
	 */
	public void setRangeExample(String r)
	{
		rangeExample = r;
	}
	
	/**	Returns all possible templates for this property. Properties whose range is a 
	 *	boolean or a PolicyGrid utility Date have a different template set.
	 *	@param rangeType Datatype of property: 2 for dates, 5 for boolean, other types do not matter.
	 *	@return List<DependencyTreeTransformer> with templates
	 */
	public List<DependencyTreeTransformer> getTemplates(int rangeType)
	{
		int cntr = 1;
		List<DependencyTreeTransformer> l = new ArrayList<DependencyTreeTransformer>();
		Lexicon lex = new Lexicon();
		String type = "";
		InputStream in = null;
		switch (rangeType)
		{	//boolean and date properties have different templates
			case 5: type = "boolean"; break;
			case 2: type = "date"; break;
		}
		
		try
		{
			while (true)
			{
				String str = new String(type + Integer.toString(cntr));
				in = getClass().getClassLoader().getResourceAsStream(PATH + str + ".xml");
				if (in == null)
					break;
				DependencyTreeTransformer dt = lex.readFile(in);
				DTNode node = new DTNode(null, null, root, null);
				node.setLeaf(true);
				dt.insert(node, PREDICATE);
				if (rangeExample != null)	//store the provided example in the lexicon
					dt.insert(new DTNode(LinguisticTerms.NP, null, rangeExample, null), Lexicon.TARGET);
				if (cntr == 6)
					dt.getGraph().toFile("number6-2");
    			l.add(dt);
    			cntr++;
    			in.close();
    		}
    	}
    	catch(Exception e)
    	{
    		e.printStackTrace();
    		try
    		{
    			in.close();
    		}
    		catch (Exception ex)
    		{}  		
    	}
		return l;
	}
	
	/**	Transforms the chosen dependency tree to a xml-file and stores it in the
	 *	lexicon.
	 *	@param dt DependencyTreeTransformer
	 *	@param dir Path to lexicon directory
	 *  @param minCardinality the minimum cardinality of that property
	 *	@throws IOException if specification could not be stored
	 */
	public void storeProperty(DependencyTreeTransformer dt, String dir, Object minCardinality) throws IOException
	{	//create a saxbuilder document
		Document doc = new Document();
		doc.addContent(new Comment("Lexicon entry for the property " + property));
		Element root = new Element("dt");
		makeElement(root, (DTNode) dt.getGraph().getRoot());

		if (minCardinality instanceof Integer && minCardinality != null) {
			int minCard = (Integer) minCardinality;
			Element minCardElmt = new Element(Lexicon.MINIMUM_CARDINALITY);
			minCardElmt.setAttribute("value", String.valueOf(minCard));
			root.addContent(minCardElmt);
//			doc.addContent(minCardElmt);
			//Cannot add a second root element, only one is allowed, need to add minCard param in the dt element (in makeElement)
			// --> also need to check how to retrieve the mincard to use in Liber
		}

		doc.setRootElement(root);
		//TODO: minCard: if the user added more parameters to the property (minCard, ...), create a new Element --> need to save a list of parameters somewhere (in the property?)
		// called from SpecificationPane.store()

		FileWriter f = new FileWriter(dir + property + ".xml");
		PrintWriter fw = new PrintWriter(f);
		XMLOutputter xml = new XMLOutputter();
		fw.print(xml.outputString(doc));
		fw.close();	
		f.close();	
	}
	
	/**	Makes an XML-node tag
	 */
	private Element makeElement(Element parent, DTNode node)
	{
		Element child = new Element("node");
		child.setAttribute(Lexicon.CAT, node.getLabel());
		child.setAttribute(Lexicon.REL, node.getDeplbl());
		if (node.getRoot() != null)
			child.setAttribute(Lexicon.ROOT, node.getRoot());
		
		String id = node.getID();
		if (id.equals(Lexicon.SOURCE) || id.equals(Lexicon.TARGET) || id.equals(PREDICATE))
			child.setAttribute(Lexicon.INDEX, id);
		if (node.getMorph() != null)
			addMorph(node.getMorph(), child);
			
		for (Iterator it = node.getChildren(); it.hasNext(); )
			makeElement(child, (DTNode) it.next());
			
		parent.addContent(child);
		return child;
	}
	
	/**	Adds an XML-morphology tag
	 */
	private void addMorph(Morph m, Element parent)
	{
		Element child = new Element("morph");
		if (m.isGenitive())
			child.setAttribute(Lexicon.CASE, LinguisticTerms.GENITIVE);
		if (!m.isSingular())
			child.setAttribute(Lexicon.NUM, LinguisticTerms.PLURAL);
		if (m.isPassive())
			child.setAttribute(Lexicon.ACTOR, LinguisticTerms.PASSIVE);
		if (m.getPerson() != 3)
			child.setAttribute(Lexicon.PERSON, Integer.toString(m.getPerson()));
		if (m.isQuote())
			child.setAttribute(Lexicon.VALUE, LinguisticTerms.QUOTE);
		if (!m.getTense().equals(LinguisticTerms.PRESENT))
			child.setAttribute(Lexicon.TENSE, m.getTense());
		parent.addContent(child);
	}
}
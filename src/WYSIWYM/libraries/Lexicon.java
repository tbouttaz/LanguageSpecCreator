package WYSIWYM.libraries;

import WYSIWYM.model.*;
import WYSIWYM.ontology.*;
import com.hp.hpl.jena.ontology.OntProperty;
import WYSIWYM.transformer.DependencyTreeTransformer;

import java.util.List;
import java.util.Iterator;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.File;
//import java.net.*;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

/**
 *	Lexicon creates Dependency Trees for properties from property-specific input files
 *
 * @author Feikje Hielkema
 * @version 1.00 2006/11/13
 */
public class Lexicon 
{
	private DependencyTreeTransformer trans;

	/**	XML-tag for dependency relation */
	public static final String REL = "rel";
	/**	XML-tag for syntactic category */
	public static final String CAT = "cat";
	/**	XML-tag for root node */
	public static final String ROOT = "root";
	/**	XML-tag for index */
	public static final String INDEX = "idx";
	/**	XML-tag for source node */
	public static final String SOURCE = "source";
	/**	XML-tag for target node */
	public static final String TARGET = "target";
	/**	XML-tag for person nr */
	public static final String PERSON = "person";
	/**	XML-tag for the type of number (normal, ranking nr) */
	public static final String TYPE = "type";
	/**	XML-tag for singular/plural word */
	public static final String NUM = "num";
	/**	XML-tag for active/passive verb */
	public static final String ACTOR = "actor";
	/**	XML-tag for verb tense */
	public static final String TENSE = "tense";
	/**	XML-tag for case (genitive, accusative) */
	public static final String CASE = "case";
	/**	XML-tag for the quote slot */
	public static final String VALUE = "value";
	/** XML-tag for the minimal cardinality */
	public static final String MINIMUM_CARDINALITY = "minCard";
	
	// Class specification properties:
	/** XML-tag for the specification defining the label that should be used to identify a class*/
	public static final String CLASS_LABEL = "label";
	/** XML-tag for the specification defining a part of the label of that class*/
	public static final String CLASS_LABEL_PART = "labelPart";
	/** XML-tag for the property used to identify a class*/
	public static final String PROPERTY_LABEL = "propLabel";
	/** XML-tag for the property used to link the class to another class*/
	public static final String PROPERTY_LINK = "propLink";
	/** XML-tag to identify the type of label part (literal, direct/indirect for properties)*/
	public static final String LABEL_PART_TYPE = "type";
	/** XML-tag to identify the literal type of label part*/
	public static final String LITERAL_LABEL_PART_TYPE = "literal";
	/** XML-tag to identify the indirect property type of label part*/
	public static final String INDIRECT_PROPERTY_LABEL_PART_TYPE = "indirectProp";
	/** XML-tag to identify the direct property type of label part*/
	public static final String DIRECT_PROPERTY_LABEL_PART_TYPE = "directProp";
	/** XML-tag to identify if the class is the subject of the related property (true or false)*/
	public static final String CLASS_SUBJECT = "classSubj";

	/**	Path to specification library directory */
//	public static final String PATH = "data/lexicon/";
//	public static final String PATH = "/Users/thomas/Documents/Development/LIBER/liber/data/lexicon/";
	public static final String PATH = "/Users/thomas/Documents/workspace/LangSpecCreator/src/data/resource";
	public static final String CLASS_SPEC_PATH = "/Users/thomas/Documents/workspace/LangSpecCreator/src/data/resource/ClassSpec/";



	
	/**	Default constructor
	 */
    public Lexicon() 
    {}
    
    /**	Checks whether the given property has a specification (or its inverse does,
     *	or a superproperty).
     *
     *	@param property Property name
     *	@param reader Ontology
     *	@return true if there is a specification for this property
     */
    public static boolean specExists(String property, OntologyReader reader)
    {	//first check this property, if it has no spec the superprops
    	OntProperty p = reader.getProperty(property);
    	if (p == null)
    		return false;
    	if (exists(p, reader))
    		return true;
    	for (Iterator it = p.listSuperProperties(); it.hasNext(); )
		{
			OntProperty parent = (OntProperty) it.next();
			if (!p.equals(parent) && exists(p, reader))
				return true;
		}
		return false;
    }

    private static boolean exists(OntProperty p, OntologyReader reader)
    {	//check this property and its inverse
    	if (new File(PATH + p.getLocalName() + ".xml").exists())
    		return true;
    	OntProperty inverse = reader.getInverse(p);
    	if ((inverse != null) && new File(PATH + inverse.getLocalName() + ".xml").exists())
    		return true;
    	return false;
    }
    
    /**	Map an edge in the graph to a dependencytree, by finding
     *	the correct lexicon entry and creating the dependencytree
     *	contained in it
     *
     *	@param name	The name of the property
     *	@return DependencyTreeTransformer with the specification
     *	@throws IOException if the file could not be read
     */
    public DependencyTreeTransformer map(String name) throws IOException
    {	//open the dependencytree-file for this property
		String fileName = new String(name + ".xml");
   	//	InputStream input = getClass().getClassLoader().getResourceAsStream(PATH + fileName);
   		InputStream input = new FileInputStream(PATH + fileName);
    	readFile(input);
    	input.close();
    	return trans;
    }
    
    /**	Reads the xml-file
     *	@param input InputStream of file
     *	@return DependencyTreeTransformer with specification
     *	@throws IOException if the file could not be read
     */
//    public DependencyTreeTransformer readFile(InputStream input) throws IOException
//    {
//    	try
//    	{
//    		Element root = getDtElement(input);
//        	makeDT(root);
//    		return trans;
//    	}
//    	catch (Exception e)
//    	{
//    		System.out.println("Lexicon 114: Error occurred when reading lexical specification.");
//    		if (input != null)
//	    		input.close();
//    		throw (new IOException(e.getMessage()));
//    	}
//    }
//    
//    public Element getDtElement(InputStream input) throws IOException{
//
//    	try
//    	{
//	    	SAXBuilder saxbuild = new SAXBuilder();
//    	  	Document doc = saxbuild.build(input);
//      		Element root = doc.getRootElement();
//      		if ((root == null) || (root.getName() != "dt"))    
//    			throw new IOException("Wrong input: dependency tree expected");
//    		return root;
//    	}
//    	catch (Exception e)
//    	{
//    		System.out.println("Lexicon 114: Error occurred when reading lexical specification.");
//    		if (input != null)
//	    		input.close();
//    		throw (new IOException(e.getMessage()));
//    	}
//    }
    
    /**	Reads the xml-file
     *	@param input InputStream of file
     *	@return DependencyTreeTransformer with specification
     *	@throws IOException if the file could not be read
     */
    public DependencyTreeTransformer readFile(InputStream input) throws IOException
    {
    	try
    	{
	    	SAXBuilder saxbuild = new SAXBuilder();
    	  	Document doc = saxbuild.build(input);
      		Element root = doc.getRootElement();
      		if ((root == null) || (root.getName() != "dt"))    
    			throw new IOException("Wrong input: dependency tree expected");
    		makeDT(root);
    		return trans;
    	}
    	catch (Exception e)
    	{
    		System.out.println("Lexicon 114: Error occurred when reading lexical specification.");
    		if (input != null)
	    		input.close();
    		throw (new IOException(e.getMessage()));
    	}
    }
    
    /**	Creates a DependencyTree out of the given element (and its children)
     *
     *	@param	The root element of the xml-file
     *	@throws Exception
     */
    private void makeDT(Element element) throws Exception
	{
		trans = new DependencyTreeTransformer(new DependencyTree());
		DTNode root = makeNode(element);
		if (root != null)
			trans.getGraph().setRoot(root);
		else
			trans = null;	//tree was not created, so set transformer to null as well
	}
	
	/**	Reads an element in the xml-input file and creates an 
	 *	equivalent node in the dependency tree
	 *
	 *	@param	The element to be mapped
	 *	@throws Exception
	 */
	private DTNode makeNode(Element element) throws Exception
	{
		if (element.getName().equals("dt"))
		{
			List l = element.getChildren();
			if (l.size() == 0)		//empty dependency tree; should not be created, so return null
				return null;
			else {
				//TODO: minCard: if the xml file contains a minCard element, add the minimum cardinality of that property to the cardinalityMap of the OntologyReader? (can get reader from ContentPlanner.getTree(OntProperty) line: 400) 
				if (element.getChildren().size() > 1 && ((Element) element.getChildren().get(1)).getName().equals(MINIMUM_CARDINALITY)) {
					System.out.println("min cardinality: " + ((Element) element.getChildren().get(1)).getAttributeValue("value"));
				}
				return makeNode((Element) element.getChildren().get(0));
			}
		}
		else if (element.getName().equals("node"))
		{
			DTNode node;
			if (element.getAttributeValue(INDEX) != null)
			{
				node = new DTNode(element.getAttributeValue(CAT), element.getAttributeValue(REL), element.getAttributeValue(INDEX));
				node.setRoot(element.getAttributeValue(ROOT));
				node.setLeaf(true);
			}
			else
				node = new DTNode(element.getAttributeValue(CAT), element.getAttributeValue(REL), element.getAttributeValue(ROOT), null);
			
			trans.addNode(node);
			List<Element> children = element.getChildren();
			for (int i = 0; i < children.size(); i++)
			{
				Element child = children.get(i);
				if (child.getName().equals("morph"))
					makeMorph(child, node);
				else
				{	
					DTNode target = makeNode(child);
					DTEdge edge = new DTEdge(child.getAttributeValue(REL), node, target);
					trans.addEdge(edge);
				}
			}
			return node;
		}
		throw new IOException("Error reading input: dt or node expected");
	}
    
    /**	Creates morphological information from a xml-element
	 *	@param	The element containing the morphologic information
	 *	@param	The node that the morphological information will be added to
	 *	@throws Exception
	 */
    private void makeMorph(Element element, DTNode node)
    {
    	Morph m = new Morph(element.getAttributeValue(NUM), element.getAttributeValue(PERSON), 
    		element.getAttributeValue(TENSE), element.getAttributeValue(ACTOR), 
    		element.getAttributeValue(CASE), element.getAttributeValue(VALUE), element.getAttributeValue(TYPE));
    	node.setMorph(m);
    }
    
    /**	Returns an NL representation of the given number (e.g. 'fifty')
     *
     *	@param nr number
     *	@return String containing number in natural language
     */
    public static String getNL(int nr)
    {
    	switch (nr)	//if 10 < nr < 20
    	{			//then the nl is a special case
    		case 11: return "eleven";
    		case 12: return "twelve";
    		case 13: return "thirteen";
    		case 14: return "fourteen";
    		case 15: return "fifteen";
    		case 16: return "sixteen";
    		case 17: return "seventeen";
    		case 18: return "eighteen";
    		case 19: return "nineteen";
    	}
    	
    	StringBuffer sb = new StringBuffer();
    	if (nr > 999)
    		sb.append(getIdxNL(nr / 1000) + " thousand ");
    	if (nr > 99)
    		sb.append(getIdxNL(nr / 100) + " hundred ");
    	if (sb.length() > 0)
    		sb.append("and ");
    	if (nr > 9)
    		sb.append(getDecimalNL(nr / 10) + " ");
		sb.append(getIdxNL(nr % 10));
    	return sb.toString(); 	//e.g. seven thousand two hundred and forty one.
    }
    
    private static String getDecimalNL(int d)
    {
    	switch (d)
    	{
    		case 1: return "ten";
    		case 2: return "twenty";
    		case 3: return "thirty";
    		case 4: return "forty";
    		case 5: return "fifty";
    		case 6: return "sixty";
    		case 7: return "seventy";
    		case 8: return "eighty";
    		case 9: return "ninety";
    	}
    	return "";
    }
    
    private static String getIdxNL(int i)
    {
    	switch (i)
    	{
    		case 1: return "one";
    		case 2: return "two";
    		case 3: return "three";
    		case 4: return "four";
    		case 5: return "five";
    		case 6: return "six";
    		case 7: return "seven";
    		case 8: return "eight";
    		case 9: return "nine";
    	}
    	return "";
    }
    
    /**	Returns an NL representation of the rank order of the given number (e.g. 'fiftieth')
     *
     *	@param nr number
     *	@return String containing rank order number in natural language
     */
    public static String getRankNL(int nr)
    {
    	switch (nr)	//if 10 < nr < 20
    	{			//then the nl is a special case
    		case 1: return "first";
    		case 2: return "second";
    		case 3: return "third";
    		case 4: return "fourth";
    		case 5: return "fifth";
    		case 6: return "sixth";
    		case 7: return "seventh";
    		case 8: return "eighth";
    		case 9: return "nineth";
    		case 10: return "tenth";
    		case 11: return "eleventh";
    		case 12: return "twelfth";
    		case 13: return "thirteenth";
    		case 14: return "fourteenth";
    		case 15: return "fifteenth";
    		case 16: return "sixteenth";
    		case 17: return "seventeenth";
    		case 18: return "eighteenth";
    		case 19: return "nineteenth";
    		case 20: return "twentieth";
    		default: return "other";
    	}
    }
}
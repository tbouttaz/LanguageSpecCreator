package SpecificationCreation.ontology;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import liber.edit.client.Hierarchy;
import WYSIWYM.ontology.OntologyReader;
import WYSIWYM.util.OntologyInputException;

import com.hp.hpl.jena.ontology.CardinalityRestriction;
import com.hp.hpl.jena.ontology.MinCardinalityRestriction;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.ontology.OntResource;
import com.hp.hpl.jena.ontology.Restriction;
import com.hp.hpl.jena.rdf.model.Literal;


/**	This class extends the default ontology reader to read ontologies for the
 *	SpecificationCreator.
 *
 *	@author Feikje Hielkema
 *	@version 1.0 20-10-2008
 */
public class SpecificationOntologyReader extends OntologyReader
{	
	//Used to mark comments as relevant for LIBER
	public final static String MENU = "LIBERmenu=";
	public final static String PHRASE = "LIBERphrase=";
	public final static String PROPERNAME = "LIBERpropername";
	public final static String NONE = "-NONE-";
	
	/**	Constructor, reads an ontology from file
	 *	@param file File
	 *	@param baseURI Base URI
	 *	@throws OntologyInputException
	 */
	public SpecificationOntologyReader(File file, String baseURI) throws OntologyInputException
	{
		super(file, baseURI);
		ontology.setNsPrefix("base", baseURI);
	}
	
	/**	Creates a class hierachy with the given classes as its roots. Only classes
	 *	with this ontology's namespace, or which have a child of this ontology's namespace, are added.
	 *
	 *	@return List<CheckedHierarchy> 
	 */
	public List<CheckedHierarchy> getCheckedClassHierarchy()
	{	//get the normal hierarchy
		Hierarchy[] hierarchy = getClassHierarchy();
		List<CheckedHierarchy> list = new ArrayList<CheckedHierarchy>();
		for (int i = 0; i < hierarchy.length; i++)
		{
			CheckedHierarchy subCH = getCheckedHierarchy(hierarchy[i]);
			if (subCH != null)	//if there is a child (of this namespace), add it
				list.add(subCH);
		}
		
		return list;
	}
	
	/**	Checks whether the resource is from this ontology or imported
	 *	@param r OntResource
	 *	@return true if the resource was imported from another ontology
	 */
	public boolean isImported(OntResource r)
	{
		return (!r.getNameSpace().equals(baseURI));
	}
	
	/**	Returns a class hierarchy where each imported class is marked
	 *	as 'shaded'
	 */
	private CheckedHierarchy getCheckedHierarchy(Hierarchy h)
	{
		OntClass c = getClass(h.getValue());
		CheckedHierarchy ch = new CheckedHierarchy(h);
		if (c != null) {
			ch.setShaded(!c.getNameSpace().equals(baseURI));
		} else {
			System.out.println("OntClass is null");
		}
		Hierarchy[] sub = h.getSub();
		
		for (int i = 0; i < sub.length; i++)
		{
			CheckedHierarchy subCH = getCheckedHierarchy(sub[i]);
			if (subCH != null)	//if there is a child (of this namespace), add it
				ch.addSub(subCH);
		}
		return ch;
	}
	
	/**	Checks whether the resource has a comment of the given type.
	 *	@param resource OntResource
	 *	@param type Type of comment
	 *	@return true if there is such a comment
	 */
	public boolean hasComment(OntResource resource, String type)
	{
		for (Iterator it = resource.listComments(null); it.hasNext(); )
		{
			Literal lit = (Literal) it.next();
			if (lit.getString().indexOf(type) >= 0)
				return true;
		}
		return false;
	}
	
	public boolean inMenu(OntProperty p, String c)
	{
		for (Iterator it = p.listComments(null); it.hasNext(); )
		{
			Literal lit = (Literal) it.next();
			if ((lit.getString().indexOf(MENU) >= 0) && (lit.getString().indexOf(c) >= 0))
				return true;
		}
		return false;
	}
	
	/**	Checks whether this property stores proper names
	 *	@param p Property
	 *	@return true if it stores proper names
	 */
	public boolean useAsProperName(OntProperty p)
	{
		return hasComment(p, PROPERNAME);
	}
	
	/**	Removes any old comments of this type, and adds the new one.
	 *	@param resource OntResource
	 *	@param type Type of comment
	 *	@param comment Comment
	 */
	public void editComment(OntResource resource, String type, String comment)
	{
		removeComment(resource, type);
		resource.addComment(ontology.createLiteral(type + comment));
	}
	
	/**	Sets the submenu for the given property and class, and removes any
	 *	old definition.
	 *	@param resource OntResource
	 *	@param className Class name
	 *	@param menu Sub-menu header
	 */
	public void changeMenu(OntResource resource, String className, String menu)
	{
		removeMenu(resource, className);
		resource.addComment(ontology.createLiteral(MENU + className + "=" + menu));
	}
	
	/**	Removes any definition of sub-menu for this property and class
	 *	@param resource OntResource
	 *	@param className Class name
	 */
	public void removeMenu(OntResource resource, String className)
	{
		List<Literal> remove = new ArrayList<Literal>();
		for (Iterator it = resource.listComments(null); it.hasNext(); )
		{
			Literal lit = (Literal) it.next();
			String comment = lit.getString();
			if (comment.indexOf(MENU + className + "=") >= 0)
				remove.add(lit);
		}
		for (Literal l : remove)
			resource.removeComment(l);
	}
	
	/**	Removes any comment on the resource of the given type.
	 *	@param resource OntResource
	 *	@param type Comment type
	 */
	public void removeComment(OntResource resource, String type)
	{
		List<Literal> remove = new ArrayList<Literal>();
		for (Iterator it = resource.listComments(null); it.hasNext(); )
		{
			Literal lit = (Literal) it.next();
			if (lit.getString().indexOf(type) >= 0)
				remove.add(lit);
		}
		for (Literal l : remove)
			resource.removeComment(l);
	}
	
	/**	Returns the submenu that this property belongs to for this class, 
	 *	or null if there is no sub-menu.
	 *
	 *	@param prop OntProperty
	 *	@param className Class name
	 *	@return String submenu	
	 */
	public String getSubmenu(OntProperty prop, String className)
	{
		List<String> comments = new ArrayList<String>();
		for (Iterator it = prop.listComments(null); it.hasNext(); )
		{
			String comment = ((Literal) it.next()).getString();
			int idx = comment.indexOf(MENU);
			if (idx >= 0)
				comments.add(comment);
			if (comment.indexOf(MENU + className + "=") >= 0)
				return comment.substring(idx + MENU.length() + className.length() + 1);
		}
		if (comments.size() == 0)
			return null;
			
		List<OntClass> list = getSuperClasses(getClass(className));	//if there is no menu specified for this class, check the superclasses
		for (int i = 1; i < list.size(); i++)
		{
			String name = list.get(i).getLocalName();
			for (String comment : comments)
				if (comment.indexOf(MENU + name + "=") >= 0)
					return comment.substring(comment.indexOf(MENU) + MENU.length() + name.length() + 1);
		}
		return null;
	}
	
	/**	Returns the nl-expression of this resource defined in the ontology,
	 *	or if nothing is defined one adapted from the local name.
	 *	@param r OntResource
	 *	@return String nl-expression
	 */
	public String getNLExpression(OntResource r)
	{
		for (Iterator it = r.listComments(null); it.hasNext(); )
		{
			String comment = ((Literal) it.next()).getString();
			int idx = comment.indexOf(PHRASE);
			if (idx >= 0)
				return comment.substring(idx + PHRASE.length());
		}
		return r.getLocalName();
		//return null;
	}
	
	/**	Adds a minimum cardinality constraint on the given class and property.
	 *	@param c Class
	 *	@param nr minimum cardinality
	 *	@param prop Property
	 */
	public void addCardinalityConstraint(OntClass c, int nr, OntProperty prop)
	{
		removeMinCardinalityConstraint(c, prop);
		MinCardinalityRestriction restriction = ontology.createMinCardinalityRestriction(null, prop, nr);
		c.addSuperClass(restriction);
	}
	
	/**	Removes existing minimum cardinality constraint (if any)
	 *	@param c Class
d	 *	@param prop Property
	 */
	public boolean removeMinCardinalityConstraint(OntClass c, OntProperty prop)
	{
		List list = c.listSuperClasses().toList();
		list.addAll(c.listEquivalentClasses().toList());
		for (int i = 0; i < list.size(); i++)
		{
			OntClass superc = (OntClass) list.get(i);
			if (superc.isRestriction())
			{
				Restriction rs = superc.asRestriction();
				if (!prop.equals(rs.getOnProperty()))	//look only at restrictions for this property
					continue;
				if (rs.isMinCardinalityRestriction())
				{
					c.removeSuperClass(superc);
					return true;
				}
  				else if (rs.isCardinalityRestriction())
  				{
  					CardinalityRestriction av = rs.asCardinalityRestriction();
  					c.removeSuperClass(superc);
					if (av.getCardinality() > 0)	//replace general cardinality constraint with maximum constraint
						c.addSuperClass(ontology.createMaxCardinalityRestriction(null, prop, av.getCardinality()));
					return true;
  				}
			} 
 		}
 		return false;
	}
	
	/**	Overload: returns the class in the range, or if there is more than one
	 *	a random choice. If there is none, return null.
	 *	@param p Property
	 *	@return OntClass, or null
	 */
	public OntClass getRangeClass(OntProperty p)
	{
		List<OntClass> range = super.getRangeList(p);
		if (range.size()> 0)
			return range.get(0);
		return null;
	}
	
	/**	Saves this ontology model to a file with the given name.
	 *	@param filename Filename
	 *	@return true if the ontology was saved
	 */
	public boolean save(String filename)
	{
		System.out.println(filename);
		try
		{
			StringWriter sw = new StringWriter();
			ontology.write(sw, "RDF/XML");
			String result = sw.toString();
			sw.close();
			FileWriter fw = new FileWriter(filename);
			PrintWriter w = new PrintWriter(fw);
			w.print(result);
			w.close();
			fw.close();
			return true;
		}
		catch (IOException e)
		{
			e.printStackTrace();
			return false;
		}
	}
}
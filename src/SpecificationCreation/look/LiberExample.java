package SpecificationCreation.look;

import java.awt.Color;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import simplenlg.realiser.AnchorString;
import SpecificationCreation.nlg.SpecModifier;
import SpecificationCreation.ontology.SpecificationOntologyReader;
import WYSIWYM.libraries.Lexicon;
import WYSIWYM.model.Anchor;
import WYSIWYM.model.ContentPlan;
import WYSIWYM.model.DTNode;
import WYSIWYM.model.SGNode;
import WYSIWYM.transformer.Aggregator;
import WYSIWYM.transformer.DependencyTreeTransformer;
import WYSIWYM.transformer.SurfaceRealiser;

import com.hp.hpl.jena.ontology.OntProperty;

/**	LiberExample shows an example of a LIBER feedbacktext about a particular
 *	class, generated with the *	current version of the ontology and linguistic 
 *	information. Because the example is not based on actual RDF, it is rather basic.
 *
 *	@author 	Feikje Hielkema
 *	@version	1.0	12-12-2008
 */
 public class LiberExample extends JFrame
 {
 	private String directory, className;
 	private SpecificationOntologyReader reader;
 	
 	/**	Constructor
 	 *	@param r Ontology
 	 *	@param c Class name
 	 *	@param dir Directory name, to find specifications
 	 */
 	public LiberExample(SpecificationOntologyReader r, String c, String dir)
 	{
 		super("LIBER example");
 		setBounds(100, 100, 500, 300);
 		setResizable(true);
		setBackground(Color.white);
		Container content = getContentPane();
   		content.setBackground(Color.white);
		
 		reader = r;
 		className = c;
 		directory = dir;
 		show(generateExample(), content);

 		content.validate();
 		setVisible(true);
 	}
	
 	/**	Generates an example paragraph describing the current class.
	 */
	private java.util.List<AnchorString> generateExample()
	{	//generate single dependency trees
		try
		{	
			String nlClass = reader.getNLExpression(reader.getClass(className));
			java.util.List<DependencyTreeTransformer> trees = new ArrayList<DependencyTreeTransformer>();
			for	(OntProperty prop : reader.getDomainProperties(className))
			{
				if (new File(directory + prop.getLocalName() + ".xml").exists())
				{	//read the specification
					InputStream in = new FileInputStream(directory + prop.getLocalName() + ".xml");
					DependencyTreeTransformer dt = new Lexicon().readFile(in);
					DTNode source = SpecModifier.makeNP(nlClass, SGNode.THE, true);
					new TestAnchor(reader, className, source);
					dt.insert(source, Lexicon.SOURCE);
					
					if (prop.isObjectProperty())
					{	//if it's a datatype property, the spec already has an example. If not, add a target node.
						String rangeName = reader.getRangeClass(prop).getLocalName();
						DTNode target = SpecModifier.makeNP(rangeName, SGNode.SOME, true);
						new TestAnchor(reader, rangeName, target);
						dt.insert(target, Lexicon.TARGET);
					}
					dt.getGraph().toFile("spec"+prop.getLocalName());
					System.out.println("Tree " + trees.size() + " is " + prop.getLocalName());
					trees.add(dt);
				}
			}
			
			trees = new Aggregator().aggregate(trees);		//perform aggregation/ellipsis?
			int cntr = 0;
			for (DependencyTreeTransformer tree : trees)
				tree.getGraph().toFile(Integer.toString(cntr++));	
			ContentPlan plan = new ContentPlan();
			String header = plan.newParagraph("Example", null);
			plan.add(trees, header);
			return new SurfaceRealiser().realise(plan, false);	//realise
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}
	
	private void show(java.util.List<AnchorString> list, Container content)
	{
		content.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		for (AnchorString as : list)
		{
			LiberLabel lb = new LiberLabel(as);
			content.add(lb);
		}
	}
	
	/**	A label that is red or blue when associated with an anchor,
	 *	and can be clicked to produce a pop-up menu.
	 */
	private class LiberLabel extends JLabel
	{
		public LiberLabel(AnchorString as)
		{
			super(as.toString());
			Anchor a = as.getAnchor();
			if (a != null)
			{
				if (a.isRed())	//set font color to red
				{
					setFont(new Font("Serif", Font.BOLD, 14));
					setForeground(Color.red);
				}
				else
				{
					setFont(new Font("Serif", Font.ITALIC, 14));
					setForeground(Color.blue);			//set font color to blue
				}
				addMouseListener(new LiberMouseAdapter((TestAnchor) a));		//attach a pop-up menu
			}
		}	
	}
	
	/**	Show pop-up menus when an anchor is clicked
	 */
	private class LiberMouseAdapter extends MouseAdapter
	{
		JPopupMenu menu = new JPopupMenu();
		
		public LiberMouseAdapter(TestAnchor a)
		{
			for (String str : a.getCompulsory())
			{
				JMenuItem item = new JMenuItem(str);
				item.setForeground(Color.red);
				menu.add(item);
			}
			for (String str : a.getOptional(SpecificationOntologyReader.NONE))
				menu.add(new JMenuItem(str));
			for (String key : a.getMenus())
			{
				JMenu subMenu = new JMenu(key);
				for (String str : a.getOptional(key))
					subMenu.add(new JMenuItem(str));
				menu.add(subMenu);
			}
		}
		
		public void mouseClicked(MouseEvent e)
		{
			if (e.getButton() == MouseEvent.BUTTON1)
 				menu.show(e.getComponent(), e.getX(), e.getY());
		}
	}
 }
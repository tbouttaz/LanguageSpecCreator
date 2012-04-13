package SpecificationCreation.look;

import java.awt.Button;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.swing.DefaultListModel;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.text.NumberFormatter;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import simplenlg.realiser.AnchorString;
import SpecificationCreation.SpecificationCreator;
import SpecificationCreation.nlg.EntryCreator;
import SpecificationCreation.nlg.SpecModifier;
import SpecificationCreation.ontology.SpecificationOntologyReader;
import WYSIWYM.libraries.Lexicon;
import WYSIWYM.libraries.LinguisticTerms;
import WYSIWYM.model.FeedbackText;
import WYSIWYM.transformer.DependencyTreeTransformer;
import WYSIWYM.transformer.SurfaceRealiser;
import WYSIWYM.util.SurfaceRealisationException;

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntProperty;

/**	Shows a pane for creating a linguistic specification
 *
 *	@author Feikje Hielkema
 * 	@version 1.0 10/11/2008
 */
public class SpecificationPane implements ActionListener
{
	private String propName, domainName, rangeName, rootWord;
	private OntProperty property;
	private OntClass domain, range;
	private SpecificationOntologyReader reader;
	private ClassPane parent;
	private SpecificationCreator frame;
	
	private Button okBtn = new Button("OK"), cancelBtn = new Button("Cancel");
	private Button undoBtn = new Button("Undo");	//, redoBtn = new Button("Redo");
	private JList templateList;
	private JLabel emptyLabel, specLabel;
	private DefaultListModel listModel;
	private GridBagConstraints constraint;
	private Container specContainer, surfaceFormsContainer;
	private JTextArea surfaceFormsArea;
	private JFormattedTextField minCardField;
	
	private EntryCreator entryCreator;
	private SpecModifier specModifier;
	private java.util.List<DependencyTreeTransformer> treeList;
	
	/**	Constructor
	 *	@param p Property
	 *	@param c Class
	 *	@param reader Ontology
	 *	@param parent Previous (and next) panel
	 *	@param frame Parent Frame
	 *	@throws CancelledException
	 */
	public SpecificationPane(OntProperty p, OntClass c, SpecificationOntologyReader reader, ClassPane parent, SpecificationCreator frame) throws CancelledException
	{
		this.propName = reader.getNLExpression(p);
		this.domainName = reader.getNLExpression(c);
		this.property = p;
		this.domain = c;
		this.reader = reader;
		this.parent = parent;
		this.frame = frame;
		range = reader.getRangeClass(property);
			
		File spec = new File(frame.getDirectory() + p.getLocalName() + ".xml");
		if (!spec.exists())
		{
			rootWord = JOptionPane.showInputDialog(frame, "You are creating a specification for the property '" +
				property.getLocalName() + "'. Please provide the root of the most important word of the sentence.\nFor instance, for 'has author' the root could " +
					"be 'author' or 'writer' (not authors!), for 'referred to by' it could be 'refer' (not 'referred'!), etc.", 
   	   	    		"What is the most distinctive word to represent your property?", JOptionPane.QUESTION_MESSAGE);
   	   	    if (rootWord == null)
   	   	    	throw new CancelledException("User has cancelled the specification creation");
		}
	
		if (property.isObjectProperty() && (range != null))
			rangeName = reader.getNLExpression(range);
		else if (reader.getRangeType(property) == 5)
			rangeName = "true";
		else
			rangeName = JOptionPane.showInputDialog(frame, "Please provide an example value for this property, to make the " +
				"language representation more natural.\nFor instance, a suitable value for 'has email' would be 'j.doe@fiction.co.uk'.", 
   	   	   		"Please provide a suitable value for this property.", JOptionPane.QUESTION_MESSAGE);
	
		if (rangeName == null)
			throw new CancelledException("User has cancelled the specification creation");
	
		if (spec.exists())
		{
			try
			{
				InputStream in = new FileInputStream(frame.getDirectory() + property.getLocalName() + ".xml");
				
				//TODO: minCard: retrieve the minCard from the XML file and set it in the text field
//				Element dtElement = new Lexicon().getDtElement(in);
//				if (dtElement.getChildren().size() > 1 && ((Element) dtElement.getChildren().get(1)).getName().equals(Lexicon.MINIMUM_CARDINALITY)) {
//					String attributeValue = ((Element) dtElement.getChildren().get(1)).getAttributeValue("value");
//					minCardField.setText(attributeValue);
//				}
				
				
				DependencyTreeTransformer dt = new Lexicon().readFile(in);
				specModifier = new SpecModifier(dt, domainName, rangeName, property.isObjectProperty());
				rootWord = dt.getGraph().getPredicate().getRoot();
				
				getTemplates();
				init();
				generateSurfaceForms();
				
				SAXBuilder saxbuild = new SAXBuilder();
	    	  	Document doc = saxbuild.build(new FileInputStream(frame.getDirectory() + property.getLocalName() + ".xml"));
	      		Element root = doc.getRootElement();
	      		if (root.getChildren().size() > 1 && ((Element) root.getChildren().get(1)).getName().equals(Lexicon.MINIMUM_CARDINALITY)) {
					String attributeValue = ((Element) root.getChildren().get(1)).getAttributeValue("value");
					minCardField.setText(attributeValue);
	      		}
	      	}
			catch(IOException e)
			{
				e.printStackTrace();
			} catch (JDOMException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else
		{
			getTemplates();
			init();
		}
	}
	
	/**	Initialises pane with proper layout
	 */
	private void init()
	{
		Container content = frame.getContentPane();
 		content.removeAll();
 		
 		constraint = new GridBagConstraints();
 		JTextArea area = new JTextArea("In this pane you can create a linguistic specification for the property '" + 
 			propName + "'. Please choose a template from the list on the left, then refine your specification on the right.");
 		area.setEditable(false);
 		area.setLineWrap(true);
 		area.setWrapStyleWord(true);
 		constraint.insets = new Insets(10, 10, 10, 10);
 		constraint.fill = GridBagConstraints.HORIZONTAL;
		constraint.anchor = GridBagConstraints.CENTER;
		constraint.weightx = 1.0;
		constraint.gridx = 0;
		constraint.gridy = 0;
		constraint.gridwidth = 3;
		content.add(area, constraint);
		
		constraint.weightx = 0.5;
		constraint.weighty = 1.0;	//add extra space to the tree widget, so it can expand
		constraint.anchor = GridBagConstraints.FIRST_LINE_START;
		constraint.gridx = 0;
		constraint.gridy = 1;
		constraint.fill = GridBagConstraints.BOTH;
		constraint.gridwidth = 1;
		constraint.gridheight = 4;	
		JScrollPane scroll = new JScrollPane(templateList);
 		content.add(scroll, constraint);
 		
 		constraint.weightx = 0.4;
		constraint.weighty = 0.1;	//add extra space to the tree widget, so it can expand
		constraint.anchor = GridBagConstraints.FIRST_LINE_START;
		constraint.gridx = 1;
		constraint.gridy = 1;
		constraint.fill = GridBagConstraints.NONE;	
		constraint.gridwidth = 1;
		constraint.gridheight = 1;
 		content.add(new JLabel("Edit your specification:"), constraint);
 		
 		constraint.gridx = 2;
 		constraint.gridy = 1;
 		constraint.weightx = 0.1;
 		constraint.weighty = 0.1;
 		undoBtn.addActionListener(this);
 		undoBtn.setEnabled(false);
 		content.add(undoBtn, constraint);
 		
 		specLabel = new JLabel("Please select a template.");
 		constraint.gridx = 1;
 		constraint.gridy = 2;
 		constraint.weightx = 0.4;
		constraint.weighty = 0.3;
 		content.add(specLabel, constraint);
 		 		
 		constraint.gridx = 1;
 		constraint.gridy = 3;
 		constraint.gridwidth = 2;
 		constraint.weightx = 0.5;
		constraint.weighty = 0.1;
 		content.add(new JLabel("Alternative surface forms:"), constraint);
 		
 		emptyLabel = new JLabel("");
 		constraint.gridx = 1;
 		constraint.gridy = 4;
 		constraint.gridwidth = 2;
 		constraint.weightx = 0.5;
		constraint.weighty = 0.4;
 		content.add(emptyLabel, constraint);
 		
 		
 		//TODO: minCard: add a field for user to enter the min card:
// 		JLabel minCardLabel = new JLabel("Minimum Cardinality of " + propName+ ": ");
 		JLabel minCardLabel = new JLabel("Minimum Cardinality: ");
 		constraint.gridx = 1;
 		constraint.gridy = 5;
 		constraint.gridwidth = 1;
 		constraint.weightx = 0.4;
		constraint.weighty = 0.3;
 		content.add(minCardLabel, constraint);
 		
 		NumberFormatter nf = new NumberFormatter();
 		nf.setValueClass(Integer.class);
 		minCardField = new JFormattedTextField(nf);
 		minCardField.setToolTipText("enter the minimum cardinality");
 		minCardField.setColumns(10);
 		constraint.gridx = 1;
 		constraint.gridy = 6;
 		content.add(minCardField, constraint);
 		
 		constraint.fill = GridBagConstraints.NONE;
 		constraint.anchor = GridBagConstraints.PAGE_START;
 		constraint.weighty = 0;	
 		constraint.weightx = 0;
 		constraint.gridx = 0;
 		constraint.gridy = 7;
 		constraint.gridwidth = 3;
 		
 		Container cont = new Container();
 		okBtn.addActionListener(this);
 		cancelBtn.addActionListener(this);	
 		cont.add(okBtn);
 		cont.add(cancelBtn);
 		cont.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 20));
 		content.add(cont, constraint);
 		
 		content.validate();
	}
	
	/**	Retrieves the templates for this property, generates their surface forms
	 *	and presents these in a list.
	 */
	private void getTemplates()
	{
		entryCreator = new EntryCreator(property.getLocalName(), rootWord);
		if (!property.isObjectProperty())
			entryCreator.setRangeExample(rangeName);
		SurfaceRealiser realiser = new SurfaceRealiser();
		listModel = new DefaultListModel();		//get the templates
		treeList = entryCreator.getTemplates(reader.getRangeType(property).intValue());
		
		for (int i = 0; i < treeList.size(); i++)
		{	//generate the surface forms and put them in the list
			DependencyTreeTransformer dt = treeList.get(i);
			try
			{
				StringBuffer sb = new StringBuffer();
				for (AnchorString as : new SpecModifier(dt, domainName, rangeName, property.isObjectProperty()).getSurfaceForm(SpecModifier.NORMAL, false))
					sb.append(as.toString());
				listModel.addElement(sb.toString());
			}
			catch(SurfaceRealisationException e)
			{
				e.printStackTrace();
			}		
		}
		
		templateList = new JList(listModel);
		templateList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		templateList.addMouseListener(new MouseAdapter()
 		{
 			public void mousePressed(MouseEvent e)
 			{
 				int idx = templateList.getSelectedIndex();
         		if ((idx > -1) && (e.getClickCount() > 1) && (e.getButton() ==  MouseEvent.BUTTON1))
         			initSpecification(idx);	//if the user double right clicks this node, select this template
 			}
 		});
	}
	
	private void initSpecification(int idx) 
	{	//create the specification modifier
		DependencyTreeTransformer dt = treeList.get(idx);
		specModifier = new SpecModifier(dt, domainName, rangeName, property.isObjectProperty());
		generateSurfaceForms();
	}
	
	/**	Generates all surface forms belonging to the specification and
	 *	presents them in the interface.
	 */
	private void generateSurfaceForms() 
	{
		Container content = frame.getContentPane();
		if (specContainer == null)
		{
			content.remove(specLabel);
			content.remove(emptyLabel);
		}
		else
		{
			content.remove(specContainer);
			content.remove(surfaceFormsArea);
		}
		specContainer = new Container();
		specContainer.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
		
		try
		{	//show feedback text
			java.util.List<FeedbackText> surfaceForms = specModifier.getSurfaceForms();
			FeedbackText text = surfaceForms.get(0);
			while (text.hasNext())
			{
				LiberLabel lb = new LiberLabel(text.next(), this);
				specContainer.add(lb);
			}
			text.reset();
			
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < surfaceForms.size(); i++)
			{
				sb.append("- ");	//make a list
				while (surfaceForms.get(i).hasNext())
					sb.append(surfaceForms.get(i).next());
				sb.append("\n");	//append each surface form, then start a new line
			}
			surfaceFormsArea = new JTextArea(sb.toString());
			surfaceFormsArea.setEditable(false);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			specContainer = null;
			JOptionPane.showMessageDialog(frame, "There was an error when I tried to generate your specification. Please try again.",
   	   	    	"Error when generating surface form.", JOptionPane.ERROR_MESSAGE);
   	   	    return;
		}
	
		constraint.gridx = 1;
 		constraint.gridy = 2;
 		constraint.gridwidth = 1;
		constraint.gridheight = 1;
 		constraint.weightx = 0.4;
		constraint.weighty = 0.3;
		constraint.fill = GridBagConstraints.NONE;
 		constraint.anchor = GridBagConstraints.FIRST_LINE_START;
		content.add(specContainer, constraint);
		
		constraint.gridx = 1;
 		constraint.gridy = 4;
 		constraint.gridwidth = 2;
		constraint.gridheight = 1;
 		constraint.weightx = 0.5;
		constraint.weighty = 0.4;
		content.add(surfaceFormsArea, constraint);
		
		content.validate();
	}
	
	/**	When the user selects an option, this method elicits a value 
	 *	(e.g. root of the new determiner) if necessary. The surface forms
	 *	are then updated.
	 *	@param entry Selected option
	 *	@param id Unique ID of anchor
	 */
	public void elicitValue(String entry, String id)
	{
		String value = null;
		boolean valueRequested = true;
		if (entry.equals(specModifier.ADD_DET))
			value = JOptionPane.showInputDialog(frame, "Please enter the root of the new determiner (e.g. 'the', 'some', or 'a').", 
				"Please enter the new determiner.");
		else if (entry.equals(specModifier.ADD_MOD))
			value = JOptionPane.showInputDialog(frame, "Please enter the root of the new adjective (e.g. 'red', 'large', 'silly', 'exciting').", 
				"Please enter the new adjective.");	
		else if (entry.equals(specModifier.ADD_SMAIN_MOD))
			value = JOptionPane.showInputDialog(frame, "Please enter the root of the new modifier for the start of the sentence (e.g. 'yesterday').", 
				"Please enter the new modifier.");
		else if (entry.equals(specModifier.ADD_VERB_MOD))
			value = JOptionPane.showInputDialog(frame, "Please enter the root of the new adverb (e.g. 'always', 'patiently').", 
				"Please enter the new adverb.");
		else if (entry.equals(specModifier.CHANGE_ROOT))		
			value = JOptionPane.showInputDialog(frame, "Please enter the root of the new word.\nFor instance " +
				"if you want to change a verb to 'researches', type in 'research'.", "Please enter the new word.");
		else if (entry.equals(specModifier.SET_TENSE))	//present a list for the user to choose from.
		{
			String[] tenses = {LinguisticTerms.PRESENT, LinguisticTerms.PAST, LinguisticTerms.FUTURE};
			value = (String) JOptionPane.showInputDialog(null, "What tense do you want to set the verb to?", "Please specify the tense of the verb.",
	            JOptionPane.QUESTION_MESSAGE, null, tenses, tenses[0]);
		}
		else
			valueRequested = false;
		if (valueRequested && (value == null))
			return;	//user must have cancelled
		specModifier.update(entry, id, value);
		undoBtn.setEnabled(true);
		generateSurfaceForms();
	}
	
	/**	Checks if there is an inverse property, and if it has a specification itself. If it doesn't,
	 *	ask user whether this specification should be used for the inverse property as well, showing
	 *	all surface forms. If the user answers yes, store specification and return to class pane; if
	 *	no, store specification and open specification creation window for inverse property.
	 */
	private void checkInverse()
	{
		OntProperty inverse = reader.getInverse(property);
		if (property.isSymmetricProperty() || (inverse == null) || new File(frame.getDirectory() + inverse.getLocalName() + ".xml").exists())
		{	//if there is no inverse, the property is symmetric, or has its own specification, just store this property
			store();
			return;
		}
		//show all possible surface forms of inverse property, and ask user if he is satisfied with that
		try
		{
			SpecModifier inverseModifier = new SpecModifier(specModifier.getDT(), rangeName, domainName, true);
			inverseModifier.setInverse(true);
			java.util.List<FeedbackText> surfaceForms = inverseModifier.getSurfaceForms();
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < surfaceForms.size(); i++)
			{
				sb.append("- ");	//make a list
				while (surfaceForms.get(i).hasNext())
					sb.append(surfaceForms.get(i).next());
				sb.append("\n");	//append each surface form, then start a new line
			}
		
			int choice = JOptionPane.showConfirmDialog(frame, "The inverse of this property is '" + inverse.getLocalName() + 
				". Do you want to use the specification for this property as well?\n" + 
				"It would generate the following sentences:\n" + sb.toString(),
   	   	   		"Do you want to use this specification for the inverse property as well?", 
   	   	   		JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
		
			//if yes, store the spec
			if (choice == JOptionPane.YES_OPTION)
				store();	//if the user cancels, do nothing
			else if (choice == JOptionPane.NO_OPTION)	//if no, open the spec pane for the inverse property
			{
				store();
				new SpecificationPane(inverse, range, reader, parent, frame);
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	/**	Store the specification
	 */
	private void store()
	{
		System.out.println("Storing specification");
		try
 		{	//store the specification, then go back to the tree hierarchy
			//TODO: minCard: send minimum cardinality as a parameter of storeProperty()?
	 		entryCreator.storeProperty(specModifier.getDT(), frame.getDirectory(), minCardField.getValue());
	 		parent.init();
 		}
 		catch(IOException ex)
 		{
 			ex.printStackTrace();
 			JOptionPane.showMessageDialog(frame, "An error occurred when trying to store your specification.",
   	   	   		"Could not store specification.", JOptionPane.ERROR_MESSAGE);
 		}
	}
	
	/**	Action listener
	 *	@param e ActionEvent
	 */
	public void actionPerformed(ActionEvent e)
 	{
 		if (e.getSource() == okBtn)
 		{
 			if ((specModifier == null) || (specModifier.getDT() == null))
 				JOptionPane.showMessageDialog(frame, "You have not created a specification yet. Please select one of the templates, or press 'cancel'.",
 				"Your specification is not finished.", JOptionPane.ERROR_MESSAGE);
 			else
 				checkInverse();
 				//store();
 		}
 		else if (e.getSource() == cancelBtn)
 			parent.init();
 		else if (e.getSource() == undoBtn)
 		{
 			undoBtn.setEnabled(specModifier.undo());
 			generateSurfaceForms();
 		}
 	}
}
package SpecificationCreation.look;

import java.awt.Button;
import java.awt.Color;
import java.awt.Composite;
import java.awt.CompositeContext;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.ColorModel;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.Border;

import org.jdom.Comment;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import SpecificationCreation.SpecificationCreator;
import SpecificationCreation.ontology.SpecificationOntologyReader;
import WYSIWYM.libraries.Lexicon;

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntProperty;

/**	
 *  Menu allowing user to define a specification file (in XML) for a particular class
 *  This menu allows user to choose between direct and indirect properties that will be used to identify a class (its label)
 *  and how this label should be displayed
 *
 *	@author Thomas Bouttaz
 */
public class EditClassLabelPane implements ActionListener, Composite
{
	private Container content;
	private Button backBtn = new Button("<< Back");
	private Button saveBtn = new Button("Save");
//	private Button exampleBtn = new Button("Show example text");
	
	private SpecificationCreator parent;
	private SpecificationOntologyReader reader;
	private OntClass ontClass;
	private String className;
	private JList propertyList;
	private DefaultListModel listModel;
	
	private JScrollPane directPropertyPane;
	private JPanel indirectPropertyPanel;
	private JRadioButton indirectPropertyTypeButton;
	private JRadioButton directPropertyTypeButton;
	private JTextField linkingPropField;
	private JTextField relatedClassPropField;
	private JCheckBox classSubjectPropCheckBox;
	
	/**	Constructor
	 *	@param name Class name
	 *	@param content Root container
	 *	@param reader Ontology
	 *	@param parent Parent frame
	 */
	public EditClassLabelPane(String name, Container content, SpecificationOntologyReader reader, SpecificationCreator parent)
	{
		this.reader = reader;
		className = name;
		ontClass = reader.getClass(name);
		//TODO: change syso to a popup message
		if (ontClass == null) {
			System.out.println("the class " + name + " wasn't find in the OntologyReader");
		}
		this.content = content;
		this.parent = parent;
		init();
	}
	
	/**	
	 * Initialise the page with proper layout and all.
	 */
	public void init()
	{
		content.removeAll();
		
		GridBagConstraints constraint = new GridBagConstraints();
 		JTextArea area = new JTextArea("Editing the Label of the class '" + ontClass.getLocalName() +
 			"'. You need to define which property will be used to identify a particular instance of this class. " +
 			"For example, to identify a paper you can use the title of that paper. " +
 			"You can either directly use one of the property of this class or specify the property of a related class " +
 			"(e.g. for an ourSpaces account, you might want to use the name of the Person that holds that account)");
 		area.setEditable(false);
 		area.setLineWrap(true);
 		area.setWrapStyleWord(true);
 		constraint.insets = new Insets(10, 10, 10, 10);
 		constraint.fill = GridBagConstraints.HORIZONTAL;
		constraint.anchor = GridBagConstraints.CENTER;
		constraint.weightx = 1.0;
		constraint.gridx = 0;
		constraint.gridy = 0;
		constraint.gridwidth = 2;
		content.add(area, constraint);
		
        directPropertyTypeButton = new JRadioButton("Direct");
		directPropertyTypeButton.setActionCommand("direct");
		directPropertyTypeButton.setSelected(true);
		
		indirectPropertyTypeButton = new JRadioButton("Indirect");
		indirectPropertyTypeButton.setActionCommand("indirect");

        //Group the radio buttons.
		ButtonGroup group = new ButtonGroup();
	    group.add(directPropertyTypeButton);
	    group.add(indirectPropertyTypeButton);
	    
        //Register a listener for the radio buttons.
        directPropertyTypeButton.addActionListener(this);
        indirectPropertyTypeButton.addActionListener(this);
        
        //Put the radio buttons in a column in a panel.
        JPanel radioPanel = new JPanel(new GridLayout(1, 0));
        JLabel propTypeLabel = new JLabel("Property Type: ");
        radioPanel.add(propTypeLabel);
        radioPanel.add(directPropertyTypeButton);
        radioPanel.add(indirectPropertyTypeButton);
        
        constraint.gridy = 1;
        content.add(radioPanel, constraint);

 		// Initialise list of direct properties
 		initDirectPropertyList();
 		
 		// Create Panel for direct properties
 		constraint.weightx = 1;
		constraint.weighty = 0.7;	//add extra space to the tree widget, so it can expand
		constraint.anchor = GridBagConstraints.FIRST_LINE_START;
		constraint.gridx = 1;
		constraint.gridy = 2;
		constraint.fill = GridBagConstraints.BOTH;	
		constraint.gridwidth = 1;
		propertyList.setSelectionMode(DefaultListSelectionModel.SINGLE_SELECTION);
		directPropertyPane = new JScrollPane(propertyList);
		Border blackBorder = BorderFactory.createLineBorder(Color.black);
		directPropertyPane.setBorder(blackBorder);
 		content.add(directPropertyPane, constraint);

 		// Create Panel for indirect properties at the same place as the direct prop panel, so that only one is displayed at a time
 		indirectPropertyPanel = new JPanel(new GridLayout(0, 2));
 		indirectPropertyPanel.setBorder(blackBorder);
        JLabel linkingPropLabel = new JLabel(" Property linking to another class: ");
        linkingPropField = new JTextField(20);
        classSubjectPropCheckBox = new JCheckBox("This class is the subject of the above property");
        JLabel relatedClassPropLabel = new JLabel(" Property of the related class that identify the original class: ");
        relatedClassPropField = new JTextField(20);
        
        indirectPropertyPanel.add(linkingPropLabel);
        indirectPropertyPanel.add(linkingPropField);
        indirectPropertyPanel.add(classSubjectPropCheckBox);
        indirectPropertyPanel.add(new JLabel());
        indirectPropertyPanel.add(relatedClassPropLabel);
        indirectPropertyPanel.add(relatedClassPropField);
        content.add(indirectPropertyPanel, constraint);

        /**
         * TODO: Add a list to allow user to add more than one property used as label
         */
        // List of properties used as label
//        constraint.gridy = 3;
//        constraint.weighty = 0.3;
//        String[] testList = {"test", "poi", "sdfs"};
//        content.add(new JScrollPane(new JList(testList)), constraint);
//        content.repaint();
 		
 		constraint.fill = GridBagConstraints.NONE;
 		constraint.anchor = GridBagConstraints.PAGE_START;
 		constraint.weighty = 0;	
 		constraint.weightx = 0;
 		constraint.gridx = 0;
 		constraint.gridy = 4;
 		constraint.gridwidth = 2;
 		backBtn.addActionListener(this);
 		saveBtn.addActionListener(this);
// 		exampleBtn.addActionListener(this);
 		
 		Container cont = new Container();
 		cont.add(backBtn);
 		cont.add(saveBtn);
// 		cont.add(exampleBtn);
 		cont.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 20));
 		content.add(cont, constraint);
 		
 		content.validate();
	}
	
	/**
	 * Initialise the list of properties that can apply to this class
	 */
	private void initDirectPropertyList() {	
		listModel = new DefaultListModel();

		List<OntProperty> properties = reader.getDomainProperties(className);
		for (OntProperty prop : properties){
			listModel.addElement(new OntPropertyModel(prop));
		}
		
		propertyList = new JList(listModel);
	}
	
	/**	Action listener
	 *	@param e ActionEvent
	 */
	public void actionPerformed(ActionEvent e){
		
 		if (e.getSource() == backBtn){
   	   		parent.getClassHierarchyPane();
 		} else if (e.getSource() == saveBtn){
 			// Save the property file
 			savePropertyFile();
//   	} else if (e.getSource() == exampleBtn){
//	   	   new LiberExample(reader, className, parent.getDirectory());

 		} else if (e.getActionCommand() == "direct") {
   	   		directPropertyPane.setVisible(true);
   	   		indirectPropertyPanel.setVisible(false);

		} else if (e.getActionCommand() == "indirect") {
			indirectPropertyPanel.setVisible(true);
   	   		directPropertyPane.setVisible(false);
		}
 	}

	/**
	 * Creates a XML file that will contain the specification encoding which property should be used to identify a particular class
	 */
	private void savePropertyFile() {
		// Creating XML file corresponding  to this class specification:
		Document doc = new Document();
		doc.addContent(new Comment("Specification for the class " + className));
		Element root = new Element(className);
		Element labelElmt = new Element(Lexicon.CLASS_LABEL);
		
		Collection<Element> labelPartElmtList = new ArrayList<Element>();
		
		// Create the "class name" part of the label (e.g. "the project" Policygrid)
		Element classNameLabelPartElmt = new Element(Lexicon.CLASS_LABEL_PART);
		classNameLabelPartElmt.setAttribute(Lexicon.LABEL_PART_TYPE, Lexicon.LITERAL_LABEL_PART_TYPE);
		classNameLabelPartElmt.addContent("the " + className.toLowerCase() + " ");
		labelPartElmtList.add(classNameLabelPartElmt);
		
		// Create the "label used as property" part of the label (e.g. the project "Policygrid")
		Element propLabelPartElmt = new Element(Lexicon.CLASS_LABEL_PART);
		labelPartElmtList.add(propLabelPartElmt);
		
		// Direct property:
		if (directPropertyTypeButton.isSelected()) {
			propLabelPartElmt.setAttribute(Lexicon.LABEL_PART_TYPE, Lexicon.DIRECT_PROPERTY_LABEL_PART_TYPE);
			
			if (propertyList.getSelectedValue() != null && propertyList.getSelectedValue() instanceof OntPropertyModel) {
				
				String selectedProp = ((OntPropertyModel)propertyList.getSelectedValue()).getOntProperty().toString();
				JOptionPane.showMessageDialog(parent, "The property used to identify this class is " + selectedProp,
 		   	   	    	"Saving Class Spec", JOptionPane.INFORMATION_MESSAGE);
				propLabelPartElmt.addContent(selectedProp);
				
			} else {
				JOptionPane.showMessageDialog(parent, "Property selected is invalid. Please select another property.", "Missing Parameters", JOptionPane.ERROR_MESSAGE);
				return;
			}
				
		   	directPropertyPane.setVisible(true);
		   	indirectPropertyPanel.setVisible(false);
   	   		
 		// Indirect property: 	   	   		
		} else if (indirectPropertyTypeButton.isSelected()){
			
			propLabelPartElmt.setAttribute(Lexicon.LABEL_PART_TYPE, Lexicon.INDIRECT_PROPERTY_LABEL_PART_TYPE);
			boolean classIsSubject = classSubjectPropCheckBox.isSelected();
			
			// Setting attributes:
			if (!"".equals(linkingPropField.getText()) && !"".equals(relatedClassPropField.getText()) ) {
				propLabelPartElmt.setAttribute(Lexicon.CLASS_SUBJECT, ( new Boolean( classIsSubject ).toString() ));
				propLabelPartElmt.setAttribute(Lexicon.PROPERTY_LINK, (linkingPropField.getText()));
				propLabelPartElmt.addContent(relatedClassPropField.getText());
			} else {
				JOptionPane.showMessageDialog(parent, "Please fill in both fields.", "Missing Parameters", JOptionPane.ERROR_MESSAGE);
				return;
			}
			JOptionPane.showMessageDialog(parent, "Indirect property: \n linking Prop: " + linkingPropField.getText() + 
					"\n This class is the Subject of this property: " + classIsSubject + "\n Related class property: " + relatedClassPropField.getText());
		}
			
		// Adding content of XML file:
		for (Element labelPartElmt : labelPartElmtList) {
			labelElmt.addContent(labelPartElmt);
		}
		root.addContent(labelElmt);
		doc.setRootElement(root);
		
		// Writing file:
		try {
			//TODO: need to use the URI in the xml file name (or in sub directory? --> one subdir per namespace? --> need to encode URI special characters, or just use ontology file name)

			// Checking that the directory already exit, if not, create it:
			File directory = new File(Lexicon.CLASS_SPEC_PATH + URLEncoder.encode(ontClass.getNameSpace(), "ISO-8859-1") + "/");
			if (!directory.isDirectory()) {
				directory.mkdir();
			}
			
			FileWriter f = new FileWriter(directory.getPath() + "/" + className + ".xml");
 			PrintWriter fw = new PrintWriter(f);
 			XMLOutputter xml = new XMLOutputter(Format.getPrettyFormat());
 			fw.print(xml.outputString(doc));
 			fw.close();	
 			f.close();	
		} catch (IOException e1) {
			e1.printStackTrace();
			System.out.println("Error when writting spec class XML file");
		}
	}

	@Override
	public CompositeContext createContext(ColorModel arg0, ColorModel arg1,
			RenderingHints arg2) {
		// TODO Auto-generated method stub
		return null;
	}
}
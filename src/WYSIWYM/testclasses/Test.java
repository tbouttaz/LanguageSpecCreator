package WYSIWYM.testclasses;

import java.awt.Button;
import java.awt.Color;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;

import liber.edit.client.AnchorInfo;
import WYSIWYM.ontology.OntologyReader;
import WYSIWYM.ontology.SesameReader;
import WYSIWYM.transformer.AutomaticGenerator;
import WYSIWYM.transformer.SemanticGraphTransformer;
import WYSIWYM.util.SesameException;

/**	Implemented to generate examples for the text comparison experiment. With
 *	this class you can generate three types of descriptions of objects in the 
 *	Sesame archive, with varying degrees of aggregation.
 *
 *	@author Feikje Hielkema
 *	@version 1.4 January 2008
 */
public class Test extends JFrame
{
	private Button startBtn = new Button("Generate");
	private JList objectList;
	private java.util.List<String> idList = new ArrayList<String>();
	private DefaultListModel listModel = new DefaultListModel();
	private JTextArea textArea = new JTextArea();
	private GridBagConstraints constraint = new GridBagConstraints();
 	
 	private OntologyReader reader;
 	private SesameReader sesame;
 	private TextTypesGenerator generator;
 	private Popup popup = new Popup(this);
 	
 	/**	Default constructor
 	 */
 	public Test()
 	{
 		super("LIBER Text Type Generation");
 		setBounds(100, 100, 800, 600);
 		setResizable(false);
		setBackground(Color.white);
 		setDefaultCloseOperation(EXIT_ON_CLOSE);
	
		Container content = getContentPane();
		setResizable(true);
		setBackground(Color.white);
   		content.setLayout(new GridBagLayout());
   		content.setBackground(Color.white);
   		((JPanel)content).setOpaque(true);
   		
   		try
   		{
	   		reader = new OntologyReader();
			sesame = new SesameReader(false);
   			getObjects();
   		
   			init(content);
   		}
   		catch (Exception e)
   		{
   			e.printStackTrace();
   		}
 	}
 	
 	/**	Creates the initial welcome and starting screen
	 */
	private void init(Container content)
	{
 		JTextArea area = new JTextArea("Welcome to the LIBER text type generation tool. Please select the object you want to describe, and press 'generate'.");
 		area.setEditable(false);
 		area.setLineWrap(true);
 		area.setWrapStyleWord(true);
 		constraint.insets = new Insets(10, 10, 10, 10);
 		constraint.fill = GridBagConstraints.HORIZONTAL;
		constraint.anchor = GridBagConstraints.CENTER;
		constraint.weightx = 1.0;
		constraint.weighty = 0;
		constraint.gridx = 0;
		constraint.gridy = 0;
		constraint.gridwidth = 3;
		content.add(area, constraint);
		
		constraint.weightx = 0.3;
		constraint.weighty = 1.0;	//add extra space to the tree widget, so it can expand
		constraint.anchor = GridBagConstraints.FIRST_LINE_START;
		constraint.gridx = 0;
		constraint.gridy = 1;
		constraint.fill = GridBagConstraints.BOTH;
		constraint.gridwidth = 1;
		constraint.gridheight = 4;	
		JScrollPane scroll = new JScrollPane(objectList);
 		content.add(scroll, constraint);
 		
 		textArea.setText("Please select an object!");
 		textArea.setEditable(false);
 		textArea.setLineWrap(true);
 		textArea.setWrapStyleWord(true);
 		constraint.weightx = 0.7;
		constraint.weighty = 1.0;	//add extra space to the tree widget, so it can expand
		constraint.anchor = GridBagConstraints.FIRST_LINE_START;
		constraint.gridx = 1;
		constraint.gridy = 1;
		constraint.fill = GridBagConstraints.BOTH;	
		constraint.gridwidth = 1;
		constraint.gridheight = 1;
 		content.add(textArea, constraint);
				
		startBtn.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{	
				int idx = objectList.getSelectedIndex();
				if (idx > -1)
					start(idx);
				else
					JOptionPane.showMessageDialog(Test.this, "Please select an object first.",
   	   	    			"No object selected.", JOptionPane.WARNING_MESSAGE);
			}
		});
		
		constraint.fill = GridBagConstraints.NONE;
 		constraint.anchor = GridBagConstraints.PAGE_START;
 		constraint.weighty = 0;	
 		constraint.weightx = 0;
 		constraint.gridx = 0;
 		constraint.gridy = 5;
 		constraint.gridwidth = 1;
		content.add(startBtn, constraint);
		
		content.validate();
	}
	
	/**	select ?id ?name ?title where {{?id Utility:Name ?name.} UNION {?id Resource:Title ?title.}}
	 */
	private void getObjects() throws SesameException
	{
		/*
		 * 	SPARQL query for old ontology (ourSpaces repository):
		 * PREFIX Utility: <http://www.policygrid.org/utility.owl#> PREFIX Resource: <http://www.policygrid.org/resource.owl#> SELECT ?id ?Name ?Title WHERE {{?id Utility:Name ?Name.} UNION {?id Resource:Title ?Title.}}
		 */
//		StringBuffer query = new StringBuffer();
//		query.append(OntologyWriter.getPrefix(1));
//		query.append(OntologyWriter.getPrefix(2));
//		query.append("SELECT ?id ?Name ?Title WHERE {{?id ");
//		query.append(OntologyWriter.UTILITY);
//		query.append(":Name ?Name.} UNION {?id ");
//		query.append(OntologyWriter.RESOURCE);
//		query.append(":Title ?Title.}}");

		// SPARQL query for new ontology (ourpacesVRE repository):
		StringBuffer query = new StringBuffer();
		query.append("PREFIX Utility: <http://xmlns.com/foaf/0.1/> PREFIX Resource: <http://www.policygrid.org/opm-resource.owl#> SELECT ?id ?surname ?title WHERE {{?id Utility:surname ?surname.} UNION {?id Resource:title ?title.}}");
		
		WYSIWYM.ontology.QueryResult result = sesame.queryBinding(query.toString());
		//TODO: maybe use reader.getProperties() but only get relevant properties?
		java.util.List<String> props = reader.getProperNameProperties();
		idList = new ArrayList<String>();
		for (Iterator it = result.getIDs(); it.hasNext(); )
		{	//get all objects, put the id's in idList and their nl-representations in objectList.
			String id = (String) it.next();
			//TODO: removed to add every resources
			String nl = result.getNLValue(id, props);
			if (nl.length() > 0)
			{
				idList.add(id);
				listModel.addElement(result.getNLValue(id, props));
			}
		}
		
		objectList = new JList(listModel);
		objectList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		objectList.addMouseListener(new MouseAdapter()
 		{
 			public void mousePressed(MouseEvent e)
 			{
 				int idx = objectList.getSelectedIndex();
         		if ((idx > -1) && (e.getClickCount() > 1) && (e.getButton() ==  MouseEvent.BUTTON1))
         			start(idx);	//if the user double right clicks this node, select this template
 			}
 		});
	}

 	private void start(int idx)
 	{
 		try
 		{
 			String id = idList.get(idx);
	 		AutomaticGenerator ag = new AutomaticGenerator(reader, sesame);
			SemanticGraphTransformer sgt = ag.getObjectInformation("user", id);
			generator = new TextTypesGenerator(sgt, reader, sesame, "user");
			show(generator.getSurfaceText());
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 			JOptionPane.showMessageDialog(Test.this, "There was an error when I tried to generate your description. Please try again.",
   	   	    	"Error when generating description.", JOptionPane.ERROR_MESSAGE);
 		}
 	}
 	
 	private void show(AnchorInfo[] text)
 	{
 		popup.show(text);
 		
 		StringBuffer sb = new StringBuffer("Condition 1:\n");
 		sb.append(generator.getText(1));
 		sb.append("\n\n\nCondition 2:\n");
 		sb.append(generator.getText(2));
 		sb.append("\n\n\nCondition 3:\n");
 		sb.append(generator.getText(3));
 		textArea.setText(sb.toString());
		
		getContentPane().validate();
 	}
 	
 	/**	Updates the text when the user has asked for more information
 	 *	@param anchor Unique ID of anchor
 	 *	@param type Generation condition
 	 */
 	public void update(String anchor, int type)
 	{
 		try
 		{
	 		switch (type)
 			{
 				case 1: generator.showSummation(anchor); break;	//summation anchor
 				case 2: generator.changeTextContent(anchor, true);	break;	//show more info
 				case 3: generator.changeTextContent(anchor, false); break;	//hide some info
 			}
 			show(generator.getSurfaceText());
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 			JOptionPane.showMessageDialog(Test.this, "There was an error when I tried to generate your description. Please try again.",
   	   	    	"Error when generating description.", JOptionPane.ERROR_MESSAGE);
 		}
 	}
 	
 	/**	Main method
 	 */
 	public static void main(String[] args)
 	{
 		new Test().setVisible(true);
 	}
}
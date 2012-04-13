package liber.query.client;

import java.util.ArrayList;
import java.util.List;

import liber.edit.client.QueryDateValue;
import liber.edit.client.TagCloud;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * 	Executed when a menu item from PopupMenu is selected. Various widgets can be
 *	shown to elicit values from the user, depending on the type of property selected.
 *	Information (if necessary) is send to the server and the feedback text is regenerated.
 *
 *	@author Feikje Hielkema
 *	@version 1.2 February 13, 2008
 *
 *	@version 1.4 August 14, 2008
 */
public class QueryCommand extends WysiwymCommand
{
	private ListBox booleanList, operatorList, instanceList;
	private TextBox nrBox;
	private RadioButton during, before, after;
	private RadioButton string, number, date, object;
	private List dateList;
	private boolean anyProperty = false;
	
	/**	Constructor.
	 *	@param p Property name
	 *	@param nl NL-expression of property
	 *	@param a Anchor ID
	 *	@param parent Parent module
	 *	@param wysInt Entrypoint
	 *	@param m PopupMenu
	 */
	public QueryCommand(String p, String nl, String a, WysiwymTab parent, WYSIWYMinterface wysInt, PopupMenu m)
	{
		super(p, nl, a, parent, wysInt, m);
		type = QUERY;
		if (property == null)
		{
			anyProperty = true;
			property = "ANYTHING";
   			nlExpression = "Any relationship";
   		}
	}
	
	/**	Does everything that WysiwymCommand does, then tells the server which 
	 *	lines the user has marked 'optional'.
	 *	@see WysiwymCommand#execute()
	 */
	public void execute()
	{
		tab.removeFolksonomy();	
		if (anyProperty)	//'any relationship' menu item
		{
			menu.hide();
			parent.showHourglass();
			buttonsPressed = 0;
			String userID = parent.getUser();
			if (userID == null)
				return;
			askType();
		}
		else
			super.execute();
	}
	
	/**	Presents a pop-up when the user has selected 'any property', that asks
	 *	what type of value the user wishes to specify (string, boolean, number, etc.)
	 */
	private void askType()
	{
		panelType = 7;
		VerticalPanel vp = new VerticalPanel();
		vp.setSpacing(10);
		
		vp.add(new Label("The menu item you have selected allows you to enter a value that should be somewhere in a resource's description, without specifying where."));
		vp.add(new Label("Please specify the type of value you wish to add."));
		
		string = new RadioButton("type", "A word or phrase (e.g. 'rural accessibility)");
		number = new RadioButton("type", "A number (e.g. '3' or '8.1')");
		date = new RadioButton("type", "A date (e.g. 'March 2008')");
		object = new RadioButton("type", "Another object (e.g. a person)");
		string.setChecked(true);
		vp.add(string);
		vp.add(number);
		vp.add(date);
		vp.add(object);
		
		HorizontalPanel hp = new HorizontalPanel();
		hp.add(okBtn);
		hp.add(cancelBtn);
	//	hp.add(helpBtn);
		hp.setSpacing(20);
		vp.add(hp);
		
		show(vp);
   		okBtn.setFocus(true);	
	}
	
	/**	Overload. Requests a tag cloud from the server and displays it
	 *	in the query module.
	 */
	public void showTagCloud()
   	{
   		String user = parent.getUser();
   		if (user == null)
   			return;
   		parent.getHandler().getTagCloud(user, property, new AsyncCallback()
   		{
   			public void onSuccess(Object o)
   			{
   				TagCloud tc = (TagCloud) o;
   				if (tc.size() == 0)
   					return;

				oracle.clear();
   				String[] idArray = new String[tc.size()];
				FolksonomyLabel[] lbArray = new FolksonomyLabel[tc.size()];
				StringBuffer strb = new StringBuffer();
				for (int i = 0; i < tc.size(); i++)
				{
					if (tc.isNumber())
						lbArray[i] = new FolksonomyLabel(tc.getValue(i), booleanList, multipleValueBox);
					else
	  					lbArray[i] =  new FolksonomyLabel(tc.getValue(i), sb);

   					idArray[i] = HTMLPanel.createUniqueId();		
   					strb.append("<font size=\"");
   					strb.append(Integer.toString(tc.getFrequency(i) + 1));
   					strb.append("\"><span id='");
   					strb.append(idArray[i]);
   					strb.append("'></span></font> &nbsp; ");
   					DOM.setStyleAttribute(lbArray[i].getElement(), "display", "inline");				
				}
		  		HTMLPanel result = new HTMLPanel(strb.toString());
 		
				for (int i = 0; i < idArray.length; i++)
				{
					if (idArray[i] != null)
						result.add(lbArray[i], idArray[i]);
				}
   				tab.showFolksonomy(result);
   			}
    			
   			public void onFailure(Throwable caught)
   			{
   				parent.hideHourglass();
   				Window.alert("Unable to generate the tag cloud:<\b> " + caught.getMessage());
   			}
   		});
   	}
	
	/**	Initialises the list with boolean options.
	 *	if type =0 it excludes 'not'; if type = 1 it excludes 'and';
	 *	else it includes all options.
	 *	@param type Type of list
	 */
	private void makeBooleanList(int type)
	{
		booleanList = new ListBox();
		booleanList.addItem("--");
		if (type != 1)
			booleanList.addItem("and");
		booleanList.addItem("or");
		if (type != 0)
			booleanList.addItem("not");
		booleanList.setSelectedIndex(0);
	}
	
	/**	Creates a ListBox of numeric operators
	 */
	private void makeOperatorList()
	{
		operatorList = new ListBox();
		operatorList.addItem("=");
		operatorList.addItem("<");
		operatorList.addItem(">");
		operatorList.setSelectedIndex(2);
	}
	
	/**	Overload. Displays a pop-up where multiple values
	 *	may be added one by one. Also adds a ListBox with
	 *	boolean operators (and, or, not).
	 *	@param values Values already added
	 */
	protected void showMultipleValuePopup(String[] values)
	{
		if (datatype > 0)
		{
			showNumber(values);
			return;
		}
		
   		HorizontalPanel hp = new HorizontalPanel();  		
   		DockPanel dp = new DockPanel();
   		Label l = new Label(nlExpression + ":");
   		dp.add(l, DockPanel.NORTH);
   		dp.setSpacing(10);
   		sb = new SuggestBox(oracle);
   		dp.add(sb, DockPanel.CENTER);
   		
		HorizontalPanel hp2 = new HorizontalPanel();
		hp2.add(okBtn);
		hp2.add(cancelBtn);
		hp2.add(helpBtn);
		hp2.setSpacing(20);
		dp.add(hp2, DockPanel.SOUTH);
   		
   		makeBooleanList(0);		//exclude 'not', as with properties with max card > 1 we can't guarantee that that works
   		VerticalPanel vp3 = new VerticalPanel();
   		vp3.add(booleanList);
		vp3.add(addBtn);
		vp3.add(removeBtn);
		vp3.setSpacing(10);
		dp.add(vp3, DockPanel.EAST);
		DockPanel dp2 = new DockPanel();

   		multipleValueBox = new ListBox();
   		multipleValueBox.setSize("300px", "150px");
   		for (int i = 0; i < values.length; i++)	//add the values that the user
   			multipleValueBox.addItem(values[i]);	//has specified earlier
   		dp2.add(multipleValueBox, DockPanel.CENTER);
   		multipleValueBox.setVisibleItemCount(values.length);
   		   		
   		dp.setHeight("180px");
   		hp.add(dp);
		hp.add(dp2);
   		
   		panelType = 3;
   		show(hp);
   		sb.setFocus(true);		
   	}
	
	/**	Overload. Displays pop-up where multiple restrictions on a
	 *	property that can have at most one value can be added. The list
	 *	of boolean operators is restricted to 'or' and 'not'.
	 */
	protected void showPopup()
	{
		if (datatype > 0)
		{
			showNumber(null);
			return;
		}
		
		HorizontalPanel hp = new HorizontalPanel();  		
   		DockPanel dp = new DockPanel();
   		Label l = new Label(nlExpression + ":");
   		dp.add(l, DockPanel.NORTH);
   		dp.setSpacing(10);
   		sb = new SuggestBox(oracle);
   		dp.add(sb, DockPanel.CENTER);
   		
		HorizontalPanel hp2 = new HorizontalPanel();
		hp2.add(okBtn);
		hp2.add(cancelBtn);
		hp2.add(helpBtn);
		hp2.setSpacing(20);
		dp.add(hp2, DockPanel.SOUTH);
   		
   		makeBooleanList(1);	//exclude 'and', as with property of max card 1 this is impossible
   		VerticalPanel vp3 = new VerticalPanel();
   		vp3.add(booleanList);
		vp3.add(addBtn);
		vp3.add(removeBtn);
		vp3.setSpacing(10);
		dp.add(vp3, DockPanel.EAST);
		DockPanel dp2 = new DockPanel();
   		if (cardinal > 0)
   		{
   			Label lb = new Label("Maximum: " + Integer.toString(cardinal));
   			lb.setStyleName("wysiwym-label-small");
    		dp2.add(lb, DockPanel.NORTH);
    	}

   		multipleValueBox = new ListBox();
   		multipleValueBox.setSize("300px", "150px");
   		dp2.add(multipleValueBox, DockPanel.CENTER);
   		
   		dp.setHeight("180px");
   		hp.add(dp);
		hp.add(dp2);
   		
   		panelType = 3;
   		show(hp);
   		sb.setFocus(true);
	}
	
	/**	Overload. Shows permitted values for a datatype property, and enables user
	 * 	to select one or more to create restrictions. 
	 *
	 *	@param str Message
	 *	@param result Permitted values.
	 */
	protected void showInstanceCheck(String str, String[] result)
	{
 		HorizontalPanel hp = new HorizontalPanel();  		
   		DockPanel dp = new DockPanel();
   		Label l = new Label(str);
   		dp.add(l, DockPanel.NORTH);
   		dp.setSpacing(10);
   		
   		instanceList = new ListBox();
   		for (int i = 0; i < result.length; i++)
   			instanceList.addItem(result[i]);
   		instanceList.setVisibleItemCount(instanceList.getItemCount());	//set all items to be visible		
   		dp.add(instanceList, DockPanel.CENTER);
   		
		HorizontalPanel hp2 = new HorizontalPanel();
		hp2.add(okBtn);
		hp2.add(cancelBtn);
		hp2.add(helpBtn);
		hp2.setSpacing(20);
		dp.add(hp2, DockPanel.SOUTH);
   		
   		if (cardinal == 1)
	   		makeBooleanList(1);	//exclude 'and', as the property won't be able to have 2 values
	   	else
	   		makeBooleanList(0);	//exclude 'not', as with properties with max card > 1 we can't guarantee that that works
   		VerticalPanel vp3 = new VerticalPanel();
   		vp3.add(booleanList);
		vp3.add(addBtn);
		vp3.add(removeBtn);
		vp3.setSpacing(10);
		dp.add(vp3, DockPanel.EAST);
		DockPanel dp2 = new DockPanel();

   		multipleValueBox = new ListBox();
   		multipleValueBox.setSize("300px", "150px");
   		dp2.add(multipleValueBox, DockPanel.CENTER);
   		
   		dp.setHeight("180px");
   		hp.add(dp);
		hp.add(dp2);
   		
   		panelType = 2;
   		show(hp);
   		instanceList.setFocus(true);		
	}
	
	/**	Shows a pop-up with which users can set requirements on dates.
	 */
	protected void showDatePopup()
	{
		HorizontalPanel hp = new HorizontalPanel();  		
   		DockPanel dp = new DockPanel();
   		Label l = new Label(nlExpression + ":");
   		dp.add(l, DockPanel.NORTH);
   		dp.setSpacing(10);
   		
		during = new RadioButton("operator", "On/During");
		before = new RadioButton("operator", "Before");
		after = new RadioButton("operator", "After");
		HorizontalPanel operators = new HorizontalPanel();
		operators.setSpacing(8);
		operators.add(during);
		operators.add(before);
		operators.add(after);
		during.setChecked(true);
   		
		VerticalPanel vp1 = new VerticalPanel();
		vp1.add(operators);
   		vp1.setSpacing(5);
   		initDateBoxes(vp1);			
		HorizontalPanel hp3 = new HorizontalPanel();
   		HorizontalPanel hp4 = new HorizontalPanel();
   		hp3.setSpacing(5);
   		hp4.setSpacing(5);
   		Label l3 = new Label("Decade:");
   		l3.setStyleName("wysiwym-label-small");
   		l3.setWidth("70px");
   		hp3.add(l3); 		
   		Label l4 = new Label("Century:");
   		l4.setStyleName("wysiwym-label-small");
   		l4.setWidth("70px");
   		hp4.add(l4);
		lists = initDateLists();
		hp3.add(lists[0]);
		hp4.add(lists[1]);	
   		vp1.add(hp3);
   		vp1.add(hp4);
   		dp.add(vp1, DockPanel.CENTER);
   		
		HorizontalPanel hp5 = new HorizontalPanel();
		hp5.add(okBtn);
		hp5.add(cancelBtn);
		hp5.add(helpBtn);
		hp5.setSpacing(20);
		dp.add(hp5, DockPanel.SOUTH);

   		if (cardinal == 1)
	   		makeBooleanList(2);
	   	else
	   		makeBooleanList(0);	//exclude 'not', as with properties with max card > 1 we can't guarantee that that works
   		VerticalPanel vp3 = new VerticalPanel();
   		vp3.add(booleanList);
		vp3.add(addBtn);
		vp3.add(removeBtn);
		vp3.setSpacing(10);
		dp.add(vp3, DockPanel.EAST);
		
		DockPanel dp2 = new DockPanel();
   		multipleValueBox = new ListBox();
  		multipleValueBox.setSize("300px", "150px");
   		dp2.add(multipleValueBox, DockPanel.CENTER);
   		dp.setHeight("180px");
   		hp.add(dp);
		hp.add(dp2);
   		
   		dateList = new ArrayList();
   		panelType = 4;
   		show(hp);
   		tb[0].setFocus(true);		
	}

	/**	Shows a pop-up with which users can set requirements on numbers.
	 *	@param values String[] with already added restrictions
	 */
	protected void showNumber(String[] values)
	{
		HorizontalPanel hp = new HorizontalPanel();  		
   		DockPanel dp = new DockPanel();
   		Label l = new Label(nlExpression + ":");
   		dp.add(l, DockPanel.NORTH);
   		dp.setSpacing(10);
   		
   		makeOperatorList();
   		dp.add(operatorList, DockPanel.WEST);	
   		nrBox = new TextBox();
   		dp.add(nrBox, DockPanel.CENTER);
   		
		HorizontalPanel hp2 = new HorizontalPanel();
		hp2.add(okBtn);
		hp2.add(cancelBtn);
		hp2.add(helpBtn);
		hp2.setSpacing(20);
		dp.add(hp2, DockPanel.SOUTH);
   		
   		makeBooleanList(2);	
   		VerticalPanel vp3 = new VerticalPanel();
   		vp3.add(booleanList);
		vp3.add(addBtn);
		vp3.add(removeBtn);
		vp3.setSpacing(10);
		dp.add(vp3, DockPanel.EAST);
		DockPanel dp2 = new DockPanel();

   		multipleValueBox = new ListBox();
   		for (int i = 0; i < values.length; i++)	//add the values that the user
   			multipleValueBox.addItem(values[i]);	//has specified earlier
  		multipleValueBox.setSize("300px", "150px");
   		dp2.add(multipleValueBox, DockPanel.CENTER);
   		
   		dp.setHeight("180px");
   		hp.add(dp);
		hp.add(dp2);
   		
   		panelType = 1;
   		show(hp);
   		nrBox.setFocus(true);		
	}
	
	/**	Updates the graph with multiple restriction values.
	 */
	private void multipleValuesUpdate(String[] values, int datatype, String bool)
   	{
   		parent.showHourglass();
   		String userID = parent.getUser();
		if (userID == null)
			return;
   		parent.getHandler().multipleValuesUpdate(userID, anchor, property, values, datatype, bool, new AsyncCallback()
  		{
    		public void onSuccess(Object o) 
    		{
    			tab.regenerateFeedbackText(o);
    		}
	
			public void onFailure(Throwable caught) 
			{
				parent.hideHourglass();
	   			Window.alert(caught.getMessage());
	   		}
  		});
   	}
   	
   	/**	Updates the graph with date restrictions
   	 */
   	private void updateDate(String operator)
   	{
   		parent.showHourglass();
   		String userID = parent.getUser();
		if (userID == null)
			return;
		
		QueryDateValue[] values = new QueryDateValue[0];
		values = (QueryDateValue[]) dateList.toArray(values);
		
   		parent.getHandler().updateDate(userID, anchor, property, values, operator, new AsyncCallback()
   		{
   			public void onSuccess(Object o)
   			{
   				tab.regenerateFeedbackText(o);
   			}
   			
   			public void onFailure(Throwable caught)
   			{
   				parent.hideHourglass();
   				Window.alert(caught.getMessage());
   			}
   		});
   	}
   	
	/**	Click listener on buttons. Adds a value to the list of values, 
	 *	updates the graph and hides the pop-up, cancels the addition, removes
	 *	a value, etc.
	 *	@param sender Button Widget
	 */
	public void onClick(Widget sender)
	{	//pass the value to the server
		buttonsPressed++;
		if (sender == okBtn)
		{	
			if (panelType == 7)		//asking for value type
			{
				pp.hide();

				if (string.isChecked())
				{
					datatype = 0;
					showMultipleValuePopup(new String[0]);
				}
				else if (date.isChecked())
				{
					datatype = 4;
					showDatePopup();
				}
				else if (number.isChecked())
				{
					datatype = 2;
					showNumber(new String[0]);
				}
				else
					new ObjectPropertyCommand(property, nlExpression, anchor, 0, tab, parent).init();
				return;
			}
			else if (panelType == 5)	//booleanInput
			{
				boolean b = true;
				if (lb.getSelectedIndex() == 1)
					b = false;
				updateBoolean(b);
			}
			else if (panelType == 6)	//abstract
			{
				if (abstractArea.getText().length() == 0)
				{
					Window.alert("Please type in an abstract, or press 'Cancel'.");
					return;
				}
				updateAbstract(abstractArea.getText());
			}
			else	//multiple values
			{
				if (((panelType == 1) || (panelType == 3)) && (datatype == 0))
				{
					String leftValue = sb.getText();	//if there is a tag in the text box, and the user does want to add it,
					if ((leftValue.length() > 0) && 	//move it to the list
						Window.confirm("You specified a tag in the textbox that you did not move to the list on the righthand side. Do you want to add this tag?"))
						multipleValueBox.addItem(leftValue);
				}

				int items = multipleValueBox.getItemCount();
				if (items == 0)
	  			{
  					Window.alert("You did not specify any values for this property, or failed to move them to the right-hand side.");
  					return;
  				}

  				int idx = booleanList.getSelectedIndex();
  				if ((items > 1) && (idx == 0))
  				{
  					Window.alert("Please select a boolean value ('and', 'or' or 'not') from the list (just above the '>>' button).");
  					return;
  				}

  				if (panelType == 4)	//date input
  					updateDate(booleanList.getItemText(idx));
  				else
  				{
	   				String[] values = new String[items];
  					for (int i = 0; i < items; i++)
  						values[i] = multipleValueBox.getItemText(i);
  					multipleValuesUpdate(values, datatype, booleanList.getItemText(idx));
  				}
			}
  			pp.hide();
		}
  		else if (sender == addBtn)
 		{
 			if (panelType == 4)
 			{
 				checkDate();
 				return;
 			}
 			if (panelType == 2)
 			{
 				int idx = instanceList.getSelectedIndex();
 				if (idx == -1)	//nothing selected
					Window.alert("Please select an item from the list on the left.");
				else
				{
					int itemCnt = multipleValueBox.getItemCount();
					boolean added = false;
					for (int j = 0; j < itemCnt; j++)
  					{
  						if (multipleValueBox.getItemText(j).equals(instanceList.getItemText(idx)))
  						{	//unless they are already in multiplevaluebox
  							added = true;
  							break;
  						}
  					}
  					if (!added)
  					{
	  					multipleValueBox.addItem(instanceList.getItemText(idx));
	  					multipleValueBox.setVisibleItemCount(multipleValueBox.getItemCount() + 1);
	  				}
	  				else
	  					Window.alert("The item you have selected is already in the value list.");
				}
 				return;
 			}
 			
			String value;
			if (datatype == 0)
				value = sb.getText(); 
			else
				value = nrBox.getText();
			if (value.length() == 0)
 			{
 				Window.alert("You did not specify a value to add");
 				return;
  			}
  	
  			if (datatype > 0)
  			{
  				try
				{
					String temp = value.replaceAll(",", "");	//remove all komma's
					if (datatype == 2)					//then try to parse to a number
						new Double(temp);
					else
						new Integer(temp);		
				}
				catch(NumberFormatException e)
				{	//definitely not a number
					if (datatype == 1)
						Window.alert("Please enter an integer (i.e. a 'natural' number: 0, 1, 2...).");
					else
						Window.alert("Please enter a number, without using komma's (e.g. 100000).");
					return;
				}
  			}
  			else
  			{
  				if (value.indexOf(",") > -1)		//if the value contains a komma, it may in fact be more than one value
  					if (!Window.confirm("Are you sure this is only one value? Each value should be added separately."))
  						return;
  			}
  			
  			if (panelType == 1)
  			{	//get the operator and add it before the value
  				int idx = operatorList.getSelectedIndex();
  				if (idx < 0)
  				{
  					Window.alert("Please specify whether the value should be larger, smaller or an exact match, by selecting an item from the list on the left.");
  					return;
  				}
  				StringBuffer buffer = new StringBuffer(operatorList.getItemText(idx));
  				operatorList.setSelectedIndex(2);
  				buffer.append(" ");
  				buffer.append(value);
  				value = buffer.toString();
  			}
  			
  			int itemNr = multipleValueBox.getItemCount();
			multipleValueBox.addItem(value);
  			multipleValueBox.setVisibleItemCount(itemNr + 1);
  			multipleValueBox.setSelectedIndex(itemNr);
  			
  			if (datatype == 0)
  			{
	  			sb.setText("");
  				sb.setFocus(true);	
  			}
  			else
  			{
  				nrBox.setText("");
  				nrBox.setFocus(true);
  			}	
		}
		else if (sender == removeBtn)
		{
			int idx = multipleValueBox.getSelectedIndex();
			int lastItem = multipleValueBox.getItemCount() - 1;
			if (lastItem < 0)
				return;
			
			if (panelType == 4)
			{	//set the input back and remove it from the value list
				QueryDateValue date;
				if (idx < 0)
					date = (QueryDateValue) dateList.remove(lastItem);
				else
					date = (QueryDateValue) dateList.remove(idx);
				String[] input = date.getDates();
				
				for (int i = 0; i < 6; i++)
					tb[i].setText(input[i]);
				for (int i = 0; i < lists[0].getItemCount(); i++)
					if (lists[0].getItemText(i).equals(input[6]))
						lists[0].setSelectedIndex(i);
				for (int i = 0; i < lists[1].getItemCount(); i++)
					if (lists[1].getItemText(i).equals(input[7]))
						lists[1].setSelectedIndex(i);
				
				switch (date.getComparator())
				{
					case 1: before.setChecked(true); break;
					case 2: after.setChecked(true); break;
					case 3: during.setChecked(true); break;
				}
			}
			else
			{
				String value = multipleValueBox.getItemText(lastItem);
				if (idx >= 0)
					value = multipleValueBox.getItemText(idx);
			
				if (panelType == 1)
				{
					nrBox.setText(value.substring(2));
					String operator = value.substring(0,1);
					for (int i = 0; i < operatorList.getItemCount(); i++)
					{
						if (operatorList.getItemText(i).equals(operator))
							operatorList.setSelectedIndex(i);	
					}
				}
				else if (panelType == 3)
					sb.setText(value);
			}
			
			if (idx >= 0)
			{
				for (int i = idx; i < lastItem; i++)
					multipleValueBox.setItemText(i, multipleValueBox.getItemText(i + 1));
			}	
			multipleValueBox.removeItem(lastItem);
			multipleValueBox.setVisibleItemCount(lastItem);
		}
  		else
  			super.onClick(sender);
  	}
  	
  	/**	Checks a date requirement, to see if it is a valid date. If so, it adds
  	 *	the requirement to the graph.
  	 */
  	private void checkDate()
  	{
  		if ((lists[0].getSelectedIndex() == 0) && (lists[1].getSelectedIndex() == 0))
		{
			try
			{
				Integer.parseInt(tb[4].getText());				
			}
			catch (NumberFormatException e)
			{
				Window.alert("Please enter a date.");
				return;
			}
		}					
		dateCntr++;
		String[] result = new String[8];
		for (int i = 0; i < 6; i++)
		{
			result[i] = tb[i].getText();
			if ((i == 0) || (i == 1))
				tb[i].setText("dd");
			if ((i == 2) || (i == 3))
				tb[i].setText("mm");
			if ((i == 4) || (i == 5))
				tb[i].setText("yyyy");
		}
		for (int i = 0; i < 2; i++)
		{
			result[i + 6] = lists[i].getItemText(lists[i].getSelectedIndex());
			lists[i].setSelectedIndex(0);
		}
	
		QueryDateValue value = new QueryDateValue(result);
		if (before.isChecked())
			value.setComparator(1);
		else if (after.isChecked())
			value.setComparator(2);
		else
			value.setComparator(3);
		dateList.add(value);
		
		parent.showHourglass();
   		String userID = parent.getUser();
		if (userID == null)
			return;

		parent.getHandler().getDateExpression(userID, value, new AsyncCallback()
  		{
    		public void onSuccess(Object o) 
    		{
    			if (o == null)
    			{
    				parent.hideHourglass();
    				Window.alert("There was an error when trying to process your input. Please try again.");
    			}	
    			else if (((String)o).length() == 0)
    			{
    				parent.hideHourglass();
    				Window.alert("I'm afraid your session has expired. Please wait while I start a new one for you.");
    			}
    			else
    			{
	   				int nr = multipleValueBox.getItemCount();
    				multipleValueBox.addItem((String) o);
    				multipleValueBox.setVisibleItemCount(nr + 1);
    				parent.hideHourglass();
    			}
    		}
	
			public void onFailure(Throwable caught) 
			{
				parent.hideHourglass();
	   			Window.alert(caught.getMessage());
	   		}
  		});
  	}
}

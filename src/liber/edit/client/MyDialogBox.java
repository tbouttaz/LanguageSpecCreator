/*
 * Copyright 2007 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package liber.edit.client;
 
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.MouseListener;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * A form of popup that has a caption area at the top and can be dragged by the
 * user.
 * <p>
 * <img class='gallery' src='DialogBox.png'/>
 * </p>
 * <h3>CSS Style Rules</h3>
 * <ul class='css'>
 * <li>.gwt-DialogBox { the outside of the dialog }</li>
 * <li>.gwt-DialogBox .Caption { the caption }</li>
 * </ul>
 * <p>
 * <h3>Example</h3>
 * {@example com.google.gwt.examples.DialogBoxExample}
 * </p>
 *
 *	This DialogBox has three buttons at the top that minimise, maximise and
 *	close the window.
 */
public class MyDialogBox extends PopupPanel implements HasHTML, MouseListener {

  private HTML caption = new HTML();
  private Widget child;
  private boolean dragging;
  private int dragStartX, dragStartY;
  private VerticalPanel panel = new VerticalPanel();
  private Button closeBtn = new Button("x");	
  private Button minimiseBtn = new Button("_");
  private Button maximiseBtn = new Button("&#9633;");
  private ScrollPanel sp;
  private HorizontalPanel dock;
  private ClickListener listener;
  
  private int xCoord = 0, yCoord = 0, width = 100, height = 100;
  private boolean minimised = false, maximised = false;

  /**
   * Creates an empty dialog box. It should not be shown until its child widget
   * has been added using {@link #add(Widget)}.
   */
  public MyDialogBox() {
    this(false);
  }

  /**
   * Creates an empty dialog box specifying its "auto-hide" property. It should
   * not be shown until its child widget has been added using
   * {@link #add(Widget)}.
   *
   * @param autoHide <code>true</code> if the dialog should be automatically
   *          hidden when the user clicks outside of it
   */
  public MyDialogBox(boolean autoHide) {
    this(autoHide, true);
  }

  /**
   * Creates an empty dialog box specifying its "auto-hide" property. It should
   * not be shown until its child widget has been added using
   * {@link #add(Widget)}.
   *
   * @param autoHide <code>true</code> if the dialog should be automatically
   *          hidden when the user clicks outside of it
   * @param modal <code>true</code> if keyboard and mouse events for widgets
   *          not contained by the dialog should be ignored
   */
  public MyDialogBox(boolean autoHide, boolean modal) {
    super(autoHide, modal);  
  	dock = new HorizontalPanel();
  	dock.add(caption);
    HorizontalPanel hp = new HorizontalPanel();
    hp.setSpacing(0);
    dock.setHorizontalAlignment(HorizontalPanel.ALIGN_RIGHT);
    
    listener = new DialogListener(this);
    minimiseBtn.addClickListener(listener);
    maximiseBtn.addClickListener(listener);
    closeBtn.addClickListener(listener);
    
  	hp.add(minimiseBtn);
    hp.add(maximiseBtn);
   	hp.add(closeBtn);
    dock.add(hp);
    panel.add(dock);
   
    panel.setHeight("100%");
    panel.setSpacing(0);

   sp = new ScrollPanel();
   panel.add(sp);
   super.setWidget(panel);

    setStyleName("gwt-DialogBox");
    dock.setStyleName("Caption");
    caption.addMouseListener(this);
  }

	/**	Sets a listener on the close button
	 *	@param listen ClickListener
	 */
	public void setCloseListener(ClickListener listen)
	{
		closeBtn.removeClickListener(listener);
		listener = listen;
		closeBtn.addClickListener(listen);
	}

	/**	Sets the height of the window (used by minimise and maximise
	 *	buttons)
	 *	@param h Height
	 */
	public void setHeight(int h)
	{
		setHeight(h, true);
	}
	
	/**	Sets the height of the window (used by minimise and maximise
	 *	buttons). If store is true, store this setting as the proper
	 *	size. If false, the window is being minimised or maximised, so
	 *	the old dimensions should be saved to undo it later on.
	 *
	 *	@param h Height
	 *	@param store If true, store the new height setting
	 */
	public void setHeight(int h, boolean store)
	{
		if (store)
			height = h;
		String str = Integer.toString(h) + "px";
		setHeight(str);
    	sp.setHeight(Integer.toString(h - 20) + "px");
	}
	
	/**	Sets the width of the window (used by minimise and maximise
	 *	buttons)
	 *	@param w width
	 */
	public void setWidth(int w)
	{
		setWidth(w, true);
	}
	
	/**	Sets the width of the window (used by minimise and maximise
	 *	buttons). If store is true, store this setting as the proper
	 *	size. If false, the window is being minimised or maximised, so
	 *	the old dimensions should be saved to undo it later on.
	 *
	 *	@param w Width
	 *	@param store If true, store the new height setting
	 */
	public void setWidth(int w, boolean store)
	{
		if (store)
			width = w;
		String str = Integer.toString(w) + "px";
		setWidth(str);
    	sp.setWidth(str);
    	dock.setWidth(Integer.toString(w - 5) + "px");
	}
	
	/**	Sets the caption stylename
	 *	@param toWysiwym True to adopt wysiwym style
	 */
	public void setCaptionStyle(boolean toWysiwym)
	{
		dock.setStyleName("Caption");
	}

  public String getHTML() {
    return caption.getHTML();
  }

  public String getText() {
    return caption.getText();
  }
  
  /**	Clicklistener, handles the minimise and maximise events
   */
  private class DialogListener implements ClickListener
  {
  	MyDialogBox db;
  	
  	public DialogListener(MyDialogBox d)
  	{
  		db = d;
  	}
  	
  	public void onClick(Widget sender)
  	{
  		if (sender == closeBtn)
	  		db.hide();
	  	else if (sender == maximiseBtn)
	  	{
	  		if (minimised || maximised)
	  		{	//restore former coordinates
	  			setHeight(height);
	  			setWidth(width);
	  			setPopupPosition(xCoord, yCoord);
	  			minimised = false;
	  			maximised = false;	
	  		}
	  		else
	  		{	//maximise
		  		setHeight(Window.getClientHeight() - 5, false);
		  		setWidth(Window.getClientWidth() - 5, false);
	  			db.setPopupPosition(0, 0, false);
	  			maximised = true;
	  		}
	  	}
	  	else if (sender == minimiseBtn)
	  	{	//only show caption
	  		minimised = true;
	  		maximised = false;
	  		setHeight(20, false);
	  		setWidth(width);
	  	}
  	}
  }

  public boolean onEventPreview(Event event) {
    // We need to preventDefault() on mouseDown events (outside of the
    // DialogBox content) to keep text from being selected when it
    // is dragged.
    if (DOM.eventGetType(event) == Event.ONMOUSEDOWN) {
      if (DOM.isOrHasChild(caption.getElement(), DOM.eventGetTarget(event))) {
        DOM.eventPreventDefault(event);
      }
    }

    return super.onEventPreview(event);
  }

  public void onMouseDown(Widget sender, int x, int y) {
    dragging = true;
    DOM.setCapture(caption.getElement());
    hide();
    show();
    dragStartX = x;
    dragStartY = y;
  }

  public void onMouseEnter(Widget sender) {
  }

  public void onMouseLeave(Widget sender) {
  }

  public void onMouseMove(Widget sender, int x, int y) {
    if (dragging) {
      int absX = x + getAbsoluteLeft();
      int absY = y + getAbsoluteTop();
      setPopupPosition(absX - dragStartX, absY - dragStartY);
    }
  }

  public void onMouseUp(Widget sender, int x, int y) {
    dragging = false;
   	DOM.releaseCapture(caption.getElement());
  }

  public boolean remove(Widget w) {
    if (child != w) {
      return false;
    }
	sp.remove(w);
    return true;
  }

  public void setHTML(String html) {
    caption.setHTML(html);
  }

  public void setText(String text) {
    caption.setText(text);
  }

  public void setWidget(Widget w) {
    // If there is already a widget, remove it.
 	if (child != null)
 		sp.remove(child);
    // Add the widget to the center of the cell.
    if (w != null)
    	sp.setWidget(w);
    child = w;
  }
  
  public void setPopupPosition(int x, int y)
  {
  	setPopupPosition(x, y, true);
  }
  
  public void setPopupPosition(int x, int y, boolean store)
  {
  	super.setPopupPosition(x, y);
  	if (store)
  	{
	  	xCoord = x; 
  		yCoord = y;
  	}
  }

  /**
   *
   * @Override
   */
  public void setWidth(String width) {
    super.setWidth(width);
  }
}
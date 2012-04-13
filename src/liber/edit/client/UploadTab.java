package liber.edit.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.FormHandler;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.FormSubmitCompleteEvent;
import com.google.gwt.user.client.ui.FormSubmitEvent;
import com.google.gwt.user.client.ui.Hidden;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;


/**
 * 	Defines the document uploading widget. A single document can be uploaded;
 *	it is then stored in Fedora, and the editing session is initialised.
 *
 *	This class needs some work so the user can upload groups of documents!	
 *
 *	@author Feikje Hielkema
 *	@version 1.0 April 5, 2007
 *
 *	@version 1.4 July 10, 2008
 */
public class UploadTab extends MyTab
{
	private FileUpload upload;
  	private FormPanel form;
  	private WYSIWYMinterface parent;
  	private Label message1, message2;	
  	
  	/** Constructor. Displays the upload pane, with an upload widget and
  	 *	a next button.
  	 *	@param wys Entrypoint
  	 */
  	public UploadTab(WYSIWYMinterface wys)
  	{
  		super();
		parent = wys;
		setStyleName("ks-Sink");
		setSpacing(15);
		
		message1 = new Label("Welcome to the PolicyGrid Data Archive.");
		message1.setStyleName("wysiwym-label-xlarge");
		message2 = new Label("Please upload your resource.");
	  	message2.setStyleName("wysiwym-label-large");
	  	
	  	VerticalPanel vp = new VerticalPanel();
	  	vp.add(message1);
	  	vp.add(message2);
	  		  	
	  	Image image = new Image();
		image.setUrl("http://www.csd.abdn.ac.uk/~fhielkem/logo.jpg");

		DockPanel top = new DockPanel();
		top.setWidth("100%");
		top.add(vp, DockPanel.WEST);
		top.add(image, DockPanel.EAST);		
  		add(top);
  		
		form = new FormPanel();
   		form.setAction(GWT.getModuleBaseURL() + "/postings?action=upload");
		form.setEncoding(FormPanel.ENCODING_MULTIPART);
    	form.setMethod(FormPanel.METHOD_POST);
  		form.setWidget(this);
  		
  		upload = new FileUpload();
		upload.setName("upload");
		add(upload);
		
		Hidden hid = new Hidden("user", parent.getUser());
		add(hid);
  		
  		add(new Button("Next >>", new ClickListener() 
  		{	//if user clicks next, submit the uploaded file
      		public void onClick(Widget sender) 
      		{
    			form.submit();
  	   		}
    	}));
  		
  		form.addFormHandler(new FormHandler() 
  		{	//check whether resource was uploaded and stored in Fedora. If not, display error message.
      		public void onSubmitComplete(FormSubmitCompleteEvent event) 
      		{	//tell the parent to create the initial description
      			if (event.getResults().indexOf("ERROR!!") >= 0)
      				Window.alert("There was an error uploading your file. " + 
      				"It may be because your filename is too long, or contains special characters. " + 
      				"Please rename your file with a shorter name, using only letters and numbers, and try to upload it again.");
      			else
      			{
	      			RootPanel.get().remove(form);
		    		parent.startDescription(event.getResults());	
		    	}
      		}
      		
      		public void onSubmit(FormSubmitEvent event) 
      		{ 	//check whether user has uploaded a resource; if not, display warning
	        	if (upload.getFilename().length() == 0) 
	        	{
    	    		Window.alert("Please upload a document first.");
					event.setCancelled(true);
        		}
      		}
    	}); 
    	RootPanel.get().add(form);	
  	}
}
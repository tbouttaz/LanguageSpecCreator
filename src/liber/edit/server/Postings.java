package liber.edit.server;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

/**	Postings receives an HttpServletRequest from ourSpaces to open
 *	one of LIBER's modules for upload, querying or browsing.
 *	The form contains the user ID and may contain resource ID or project ID.
 *	Postings will open the LIBER web application and store the ID's in
 *	Cookies for LIBER to find.
 *
 *	Postings is also used when the user uploads a file in the editing module,
 *	to store the file in Fedora and add a cookie with its new URL to the response.
 *
 *	THE URLS TO THE POLICYGRID FEDORA SERVLETS HAVE BEEN REMOVED; TO USE THIS CLASS
 *	YOU MUST SPECIFY YOUR OWN!
 *	
 *	@author Feikje Hielkema
 *	@version 1.3 June 2008
 */
public class Postings extends HttpServlet implements Servlet
{
	/**	Request forms' field name for user ID */
	public static final String USER = "wysiwym_user";
	/**	Request forms' field name for resource ID */
	public static final String RESOURCE = "wysiwym_resource";
	/**	Request forms' field name for project ID */
	public static final String PROJECT = "wysiwym_project";
	
	/**	Receives the request from ourSpaces to open a LIBER module. Also used when the user 
	 *	uploads a file in the editing module, to store the file in Fedora and add a cookie 
	 *	with its new URL to the response.
	 *	@param request HttpServletRequest with user ID and optionally resource ID or project ID
	 *	@param response HttpServletResponse with Cookies and redirect to LIBER
	 *	@throws ServletException
	 *	@throws IOException
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		doPost(request,response);
	}
	
	/**	Receives the request from ourSpaces to open a LIBER module. Also used when the user 
	 *	uploads a file in the editing module, to store the file in Fedora and add a cookie 
	 *	with its new URL to the response.
	 *	@param request HttpServletRequest with user ID and optionally resource ID or project ID
	 *	@param response HttpServletResponse with Cookies and redirect to LIBER
	 *	@throws ServletException
	 *	@throws IOException
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		try
		{
			PrintWriter out = response.getWriter();
			String cmd = request.getParameter("action");
			if (cmd != null) 
        	{
        	    if (cmd.equals("login")) 
        	    {
        	    	storeID(request, response);	
        	    	response.sendRedirect("");	//ADD URL HERE!
            	}
            	else if (cmd.equals("upload"))
            		getFile(request, response);
            	else if (cmd.equals("query"))
            	{	
            		storeID(request, response);	//create new query session for this user   
            		response.sendRedirect("");	//ADD URL HERE!
            	}
            	else if (cmd.equals("browse"))
            	{
            		storeID(request, response);
            		String resourceID = request.getParameter("resourceid");
            		Cookie c = new Cookie(RESOURCE, resourceID);
            		response.addCookie(c);
            		response.sendRedirect("");	//ADD URL HERE!
            	}	
            	else
            		out.flush();
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	/**	Stores the user and project IDs in cookies for LIBER to find
	 */
	private void storeID(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		String userID = request.getParameter("userid"); 	//get the user id and set a cookie
        Cookie koekje = new Cookie(USER, userID);
       	response.addCookie(koekje);
       	
       	String projectID = request.getParameter("projectid");
       	if (projectID != null)
       	{
    	    Cookie c = new Cookie(PROJECT, projectID);
       		response.addCookie(c);
       	}
	}

	/**	Removes all characters that Fedora might not accept from the filename
	 */
	private String checkFileName(String name)
	{
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < name.length(); i++)
		{
			Character c = name.charAt(i);
			if (c.isLetterOrDigit(c) || c.equals('.'))
				sb.append(c);
		}
		
		if (sb.length() == 0)
			return "file";
		if (sb.length() > 15)	//only return last 10 characters, so name doesn't become too long
			return sb.toString().substring(sb.length() - 15);
		if (sb.charAt(0) == '.')
			sb.insert(0, "file");	//make sure filename doesn't start with extension
		return sb.toString();
	}
	
	/**	When a file has been uploaded in the editing module, this takes the posted file and puts it in Fedora.
	 */
	private void getFile(HttpServletRequest request, HttpServletResponse response) throws Exception
	{
		response.setContentType("text/plain");    	
   		FileItemFactory factory = new DiskFileItemFactory();
    	ServletFileUpload upload = new ServletFileUpload(factory);

    	try 
    	{
    		List items = upload.parseRequest(request);
    		String user = null;
    		String filename = null;
    		File file = null;
    		
      		for (Iterator it = items.iterator(); it.hasNext(); ) 
      		{
        		FileItem item = (FileItem) it.next();
        		if (!item.isFormField()) //&& "upload".equals(item.getFieldName())) 
			    {  	//PUT THE FILE INTO FEDORA	From FileItem we can use get() or getInputStream()
			    	filename = checkFileName(item.getName());
			    	file = new File (filename);
			    	item.write(file);
			    	item.delete();
			   	}
				else 		//user id!!!
			   		user = item.getFieldName();
	      	}
	      	
	      	if ((user != null) && (file != null))
	      	{
	      		response.getWriter().write(storeFileInFedora(user, file, filename));	
	      		file.delete();
   	      		return;
	      	}
    	} 
    	catch (Exception e) 
    	{
    		response.getWriter().write("ERROR!!");
      		e.printStackTrace();
    	}
    	
  	}
  	
  	private void setParameters(ClientHttpRequest httpClient) throws Exception
  	{
  		httpClient.setParameter("title","digitalObject"); 
		httpClient.setParameter("creator","PolicyGrid");
		httpClient.setParameter("description","archive");
		httpClient.setParameter("publisher","PolicyGrid"); 
		httpClient.setParameter("date", "today"); 
		httpClient.setParameter("format","whoknows"); 
		httpClient.setParameter("rights","protected"); 
  	}
  	  
  	/**	Stores the given file in Fedora, and returns the URL it can now be found
  	 *	@param userID User ID
  	 *	@param file File
  	 *	@param filename Filename
  	 *	@return String URL
  	 *	@throws Exception
  	 */  	
  	public String storeFileInFedora(String userID, File file, String filename) throws Exception
  	{
  		String digObjPID = new String();
		ClientHttpRequest httpClient = new ClientHttpRequest("");	//SPECIFY LINK TO SERVLET THAT CREATES A FEDORA DIGITAL OBJECT
  		setParameters(httpClient);           
		InputStream serverInput = httpClient.post();
		for(int i = serverInput.read(); i != -1; i = serverInput.read())
		   	digObjPID += (char) i;  
		digObjPID = normalise(digObjPID);	  
		
		//Note: avoid the use of spaces and strange characters in the file name. The file name becomes the name of the datastream in fedora. Sometimes fedora complains if the name of the datastream contains invalid characters.
		httpClient = new ClientHttpRequest("");		//SPECIFY LINK TO SERVLET THAT UPLOADS A FILE TO FEDORA
		httpClient.setParameter("username", "user");
		httpClient.setParameter("pid", digObjPID);	 //the one returned from step 1
		httpClient.setParameter("file", file);
		
		int idx = filename.indexOf(".");
		String extension = null;
		if (idx > 0)
			extension = new Extensions().get(filename.substring(idx + 1));
		
		if (extension == null)
			extension = "text/plain";
		httpClient.setParameter("mimeType", extension);			
		
		serverInput = httpClient.post();
		String result = new String();
		for (int i = serverInput.read(); i != -1; i = serverInput.read())
           	result += (char) i;	//The buffer return a success/error message from the servlet
           	
    	return result;	
  	}
  	
  	private String normalise(String str)
  	{
  		str = str.replace("\\r", "");
  		str = str.replace("\\n", "");
  		str = str.replace("\\", "");
  		str = str.replace("\r", "");
  		str = str.replace("\n", "");
  		return str;
  	}
}
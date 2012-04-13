package WYSIWYM.ontology;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.xml.parsers.ParserConfigurationException;

import org.openrdf.OpenRDFException;
import org.xml.sax.SAXException;

import com.hp.hpl.jena.ontology.OntProperty;

/**
 * JavaBean class intended to represent a single Tag when displayed on a page.
 *
 *	THE URL TO THE POLICYGRID TAG DATABASE HAS BEEN REMOVED; TO USE THIS CLASS
 *	YOU MUST SPECIFY YOUR OWN!
 * 
 * @author Richard Reid
 * @version 1.0
 */
public class Tag
{
	public String tag;
	public int size;
	public String user;
	public String resource;
	public String property;
	Connection con;
	
	public Tag()
	{}
	
	/**
	 * Returns the actual Tag
	 * @return String
	 */
	public String getTag()
	{
		return tag;
	}
	
	/**
	 * Returns the frequency of the tag
	 * @return int
	 */
	public int getSize()
	{
		return size;
	}
	
	/**
	 * Returns the resource ID of the user
	 * @return String
	 */
	public String getUser()
	{
		return user;
	}
	
	/**
	 * Returns the resource a tag is linked with
	 * @return String
	 */
	public String getResource()
	{
		return resource;
	}
	
	/**
	 * Returns the property a tag is linked with
	 * @return String
	 */
	public String getProperty()
	{
		return property;
	}
	
	/**
	 * Sets the tag's user
	 * @param user user ID
	 */
	public void setUser(String user)
	{
		this.user = user;
	}
	
	/**
	 * Sets the tag's resource
	 * @param resource resource ID
	 */
	public void setResource(String resource)
	{
		this.resource = resource;
	}
	
	/**
	 * Sets the tags's property
	 * @param property full property name
	 */
	public void setProperty(String property)
	{
		this.property = property;
	}
	
	/**
	 * Set's the actual tag
	 * @param tag tag
	 */
	public void setTag(String tag)
	{
		this.tag = tag;
	}
	
	/**
	 * Sets the tag's frequency
	 * @param size frequency
	 */
	public void setSize(int size)
	{
		this.size = size;
	}
	
	/**
	 * 
	 * Connect to the database
	 * 
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 *
	 *	@deprecated, use connect(String,String,String) instead
	 */
	public void connect() throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException
	{
		Class.forName("com.mysql.jdbc.Driver").newInstance();
		con = DriverManager.getConnection("","","");
	}
	
	/**
	 * Connect to the database
	 *
	 *	@param url String url
	 *	@param rep String database name
	 *	@param password String password
	 * 
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	public void connect(String url, String rep, String password) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException
	{
		Class.forName("com.mysql.jdbc.Driver").newInstance();
		con = DriverManager.getConnection(url, rep, password);
	}
	
	/**
	 * 
	 * Disconnect from the database
	 * 
	 * @throws SQLException
	 */
	public void disconnect() throws SQLException
	{
		con.close();
	}
	
	/**	Adds a tag to the database.
	 *
	 *	@param tag The tag
	 *	@param user user ID
	 *	@param resource resource ID
	 *	@param p OntProperty
	 *	@throws InstantiationException
	 *	@throws IllegalAccessException
	 *	@throws ClassNotFoundException
	 *	@throws SQLException
	 */
	public void addTag(String tag, String user, String resource, OntProperty p) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException 
    {	// Database Connection
		connect();
		Statement st = con.createStatement();

		// Creation of the Calender instance to get the current date
		Calendar c = Calendar.getInstance();
		int day = c.get(Calendar.DATE);
        int month = c.get(Calendar.MONTH);
        int year = c.get(Calendar.YEAR);
        String date = day+" / "+month+" / "+year; // current date
        String propName = OntologyWriter.getNameSpace(p) + p.getLocalName();
                   
        String sql = "INSERT INTO tags (tag, resource, property, user, time) VALUES('" + tag +
        	"','" + resource + "','" + propName + "','" + user + "','" + date +  "')";
	    st.executeUpdate(sql);
		st.close();
		disconnect();
	}

	/**	TEMP0RARY METHOD FOR SEEDING THE TAG DATABASE!
	 *	@param tag The tag
	 *	@param user User ID
	 *	@param p OntProperty
	 *	@throws InstantiationException
	 *	@throws IllegalAccessException
	 *	@throws ClassNotFoundException
	 *	@throws SQLException
	 *	@deprecated
	 */
	public void addTag(String tag, String user, OntProperty p)	throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException 
	{
		Statement st = con.createStatement();

		// Creation of the Calender instance to get the current date
		Calendar c = Calendar.getInstance();
		int day = c.get(Calendar.DATE);
        int month = c.get(Calendar.MONTH);
        int year = c.get(Calendar.YEAR);
        String date = day+" / "+month+" / "+year; // current date
        String propName = OntologyWriter.getNameSpace(p) + p.getLocalName();
                   
        String sql = "INSERT INTO tags (tag, resource, property, user, time) VALUES('" + tag +
        	"','','" + propName + "','" + user + "','" + date +  "')";
        System.out.println(sql);
	    st.executeUpdate(sql);
		st.close();
	}
	
	/**
	 * Retrieves all the tags from the database that were used for the given property.
	 * Tags are returned in a HashMap containing their frequencies.
	 * 
	 * @param property OntProperty
	 * @return Map<String,Integer>
	 * @throws SQLException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws ClassNotFoundException
	 */
	public Map<String,Integer> getPropertyTags(OntProperty property) throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException
	{
		String propName = OntologyWriter.getNameSpace(property) + property.getLocalName();
		Map<String,Integer> tags = new HashMap<String,Integer>();	
		connect();
		Statement st = con.createStatement();		
		String qry = "select tag, COUNT(tag) from tags where property='" + propName + "' group by tag";
		ResultSet rs = st.executeQuery(qry);
		
		while(rs.next())
			tags.put(rs.getString("tag"), new Integer(rs.getInt("COUNT(tag)")));
		
		rs.close();
		st.close();
		disconnect();
		
		return tags;
	}
	
	/**
	 * Retrieves all the tags from the database based on a user ID.  This returns
	 * all tags that belong to a particular user.
	 * 
	 * Tags are returned in a Vector containing multiple Tag JavaBeans.
	 * 
	 * @param userID user ID
	 * @return Vector of Tags
	 * @throws SQLException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws ClassNotFoundException
	 */
	public Vector getTags(String userID) throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException
	{
		Vector tags = new Vector();
		
		connect();
		
		Statement st = con.createStatement();
		
		String qry = "select tag, COUNT(tag) from tags where user='"+userID+"' group by tag";
		ResultSet rs = st.executeQuery(qry);
		
		while(rs.next())
		{
			Tag tag = new Tag();
			tag.setTag(rs.getString("tag"));
			tag.setSize(rs.getInt("COUNT(tag)"));
			tags.add(tag);
		}
		
		rs.close();
		st.close();
		disconnect();
		
		return tags;
	}
	
	
	/**
	 * Retrieves all tags from the database regardless of ownership.
	 * 
	 * @return Vector of Tags
	 * @throws SQLException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws ClassNotFoundException
	 */
	public Vector getAllTags() throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException
	{
		Vector tags = new Vector();
		
		connect();
		
		Statement st = con.createStatement();
		
		String qry = "select tag, COUNT(tag) from tags group by tag";
		ResultSet rs = st.executeQuery(qry);
		
		while(rs.next())
		{
			Tag tag = new Tag();
			tag.setTag(rs.getString("tag"));
			tag.setSize(rs.getInt("COUNT(tag)"));
			tags.add(tag);
		}
		
		rs.close();
		st.close();
		disconnect();
		
		return tags;
	}
	
	/**
	 * Clears the temporary tag table.
	 * 
	 * @throws SQLException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws ClassNotFoundException
	 */
	public void clearTable() throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException
	{
		connect();
		
		Statement st = con.createStatement();
		String sql = "delete from temp";
		st.execute(sql);
		st.close();
		disconnect();
	}
	
	/**
	 * Adds a tag to the temporary tag table.
	 * @param tag The tag
	 * @throws SQLException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws ClassNotFoundException
	 */
	public void addToTemp(String tag) throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException
	{
		connect();
		
		Statement st = con.createStatement();
		String sql = "insert into temp (tag) values ('"+tag+"')";
		st.executeUpdate(sql);
		st.close();
		disconnect();
	}
	
	/**
	 * Retrieves tags from the temporary tag table and returns a Vector containing 
	 * multiple instances of the Tag JavaBean.
	 * 
	 * @return Vector of Tags
	 * @throws SQLException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws ClassNotFoundException
	 */
	public Vector getTempTags() throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException
	{
		Vector tags = new Vector();
		
		connect();
		
		Statement st = con.createStatement();
		
		String qry = "select tag, COUNT(tag) from temp group by tag";
		ResultSet rs = st.executeQuery(qry);
		
		while(rs.next())
		{
			Tag tag = new Tag();
			tag.setTag(rs.getString("tag"));
			tag.setSize(rs.getInt("COUNT(tag)"));
			tags.add(tag);
		}
		
		rs.close();
		st.close();
		disconnect();
		
		return tags;
	}
	
	/**
	 * Returns all tags belonging to a particular resource.  Resource tags are added to a
	 * temporary list.  That list will comprise of all resources that make up a project,
	 * allowing tags to be generated and counted based on an entire project's resources.
	 * 
	 * The temp table acts as a cache.
	 * 
	 * @param resources
	 * @return Vector of Tags
	 * @throws SQLException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws ClassNotFoundException
	 */
	public Vector getResourceTags(ArrayList resources) throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException
	{
		clearTable();
		Vector tags = new Vector();
		
		connect();
		
		Statement st = con.createStatement();
		
		ResultSet rs = null;
		for(int i = 0; i < resources.size(); i++)
		{
			String resourceID = "";
			resourceID = (String) resources.get(i);
			String qry = "select tag from tags where resource='"+resourceID+"'";
			rs = st.executeQuery(qry);
			while(rs.next())
			{
				// Add to the temporary cached table
				addToTemp(rs.getString("tag"));
			}
		}
		
		rs.close();
		st.close();
		disconnect();
		tags = getTempTags();
		
		return tags;
	}
	
	/**
	 * Retrieves information about a particular tag specified.  The information is
	 * added to a Vector containing an instance(s) of a Tag JavaBean.
	 * 
	 * @param tag
	 * @return Vector of a Tag
	 * @throws SQLException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws ClassNotFoundException
	 */
	public Vector getTagInfo(String tag) throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException
	{
		Vector tagInfo = new Vector();
		
		connect();
		
		Statement st = con.createStatement();
		
		String qry = "select resource, user, property from tags where tag='"+tag+"'";
		ResultSet rs = st.executeQuery(qry);
		
		while(rs.next())
		{
			Tag info = new Tag();
			info.setResource(rs.getString("resource"));
			info.setProperty(rs.getString("property"));
			info.setUser(rs.getString("user"));
			tagInfo.add(info);
		}
		
		rs.close();
		st.close();
		disconnect();
		
		return tagInfo;
	}
	
	/**	Adds a record a user's activity to the activity database
	 *
	 *	@param userID User ID
	 *	@param projectID Project ID
	 *	@param resourceID Resource ID
	 *	@throws SQLException
	 * 	@throws InstantiationException
	 * 	@throws IllegalAccessException
	 * 	@throws ClassNotFoundException
	 *	@throws ParserConfigurationException
	 *	@throws SAXException
	 *	@throws IOException
	 *	@throws ServletException
	 *	@throws OpenRDFException
	 */
	public void addActivity(int userID, String projectID, String resourceID) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException, 
            ParserConfigurationException, SAXException, IOException, ServletException, OpenRDFException
      {
            connect();
            Calendar c = Calendar.getInstance();
            int day = c.get(Calendar.DATE);
            int month = c.get(Calendar.MONTH);
            int year = c.get(Calendar.YEAR);
            String date = day+" / "+month+" / "+year;

            String type = "resources";
            if (projectID == null)
            {
       			int actionID = 1;
				projectID = "";
				PreparedStatement pStmt = con.prepareStatement("insert into activities (userID, actionID, endActionID1, endActionID2, date, type) values("+userID+","+actionID+",\""+resourceID+"\",\""+projectID+"\",\""+date+"\",\""+type+"\")");
	            pStmt.executeUpdate();
            }
			else 
			{
				int actionID = 26;
				PreparedStatement pStmt = con.prepareStatement("insert into activities (userID, actionID, endActionID1, endActionID2, date, type) values("+userID+","+actionID+",\""+resourceID+"\",\""+projectID+"\",\""+date+"\",\""+type+"\")");
			    pStmt.executeUpdate();
			}
			
            disconnect();
      }
}

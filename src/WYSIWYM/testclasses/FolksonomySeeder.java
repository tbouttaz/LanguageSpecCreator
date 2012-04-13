package WYSIWYM.testclasses;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import WYSIWYM.ontology.OntologyReader;
import WYSIWYM.ontology.Tag;

import com.hp.hpl.jena.ontology.OntProperty;

/**	Reads a directory full of text files, and uses their contents to seed the tag
 *	database. As command line arguments it takes one or more users with whom the 
 *	tags can be associated.
 *
 *	This class was used only once, to seed ourSpaces' tag database.
 *
 *	@deprecated
 */
public class FolksonomySeeder
{
	public static final String PATH = "data/folksonomy/";
	private int userIdx = 0;
	private List<String> users = new ArrayList<String>();
	private Tag database = new Tag();
	private OntologyReader reader;
	
	public FolksonomySeeder()
	{
		try
		{
			reader = new OntologyReader();
			users.add("http://www.policygrid.org/utility.owl#4b9929c3-76c4-402f-8be2-0d33b196ec30");
			users.add("http://www.policygrid.org/utility.owl#ee216b3a-667b-4e89-a450-aa60162e5f82");
		
			File dir = new File(PATH);
			String[] files = dir.list();	//get a list of files in this directory
		
			database.connect();
		
			for (int i = 0; i < files.length; i++)
				process(files[i]);
			
			database.disconnect();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	private void process(String filename)
	{	//read each value and its frequency, and store it that many times in the tag database associated with its property
		OntProperty property = reader.getProperty(filename.substring(0, filename.indexOf(".txt")));
		if (property == null)
		{
		//	System.out.println("Do not know property " + property.getLocalName());
			return;
		}
		
   		try
   		{
	   		InputStream in = new FileInputStream(PATH + property.getLocalName() + ".txt");
			Scanner scan = new Scanner(in);
			while (scan.hasNext())
			{
				StringBuffer value = new StringBuffer(scan.next());
				String f = new String("");
				while (scan.hasNext())
				{
					try		//values may contain spaces, which are used as delimiters by the scanner
					{		//so check for numberformatexceptions
						f = scan.next();
						int freq = Integer.parseInt(f);
						for (int i = 0; i < freq; i++)
							store(value.toString(), property);
						break;
					}
					catch(NumberFormatException e)
					{	//what we thought was the frequency was still part of the value, so iterate again
						value.append(" " + f);
					}
				}
			}
			scan.close();
		}
	   	catch(IOException e)
	   	{
	  		e.printStackTrace();
	   	}
	}
	
	private void store(String tag, OntProperty property)
	{
		try
		{
			database.addTag(tag, getUser(), property);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	private String getUser()
	{	//makes sure that users get alternately added tags
		String user = null;
		if (userIdx < users.size())
		{
			user = users.get(userIdx);
			userIdx++;
		}
		else
		{
			user = users.get(0);
			userIdx = 1;
		}
		return user;
	}
	
	public static void main(String[] arg)
	{
		new FolksonomySeeder();
	/*	try
    	{
    		OntologyReader reader = new OntologyReader();
    		Map<String,OntProperty> propMap = reader.getPropertyMap();
    		String[] props = propMap.keySet().toArray(new String[0]);
    		Arrays.sort(props);	//sort alphabetically
    		
	    	FileWriter fw = new FileWriter("properties.txt");
			PrintWriter w = new PrintWriter(fw);
			
			for (int i = 0; i < props.length; i++)
			{
				OntProperty p = propMap.get(props[i]);
				String namespace = p.getNameSpace();
				if (namespace.indexOf("policygrid") < 0)
					continue;
				
				w.println(props[i] + " can be found in the menu of: ");
				List<String> list = new ArrayList<String>();
				for (OntClass c : reader.getDomainList(p, false))
					list.add(c.getLocalName());
				Collections.sort(list);	//sort the list
				for (String s : list)
					w.println("- " + s);
				w.println();
			}
					
			w.close();
			fw.close();
		}
		catch(Exception e)
		{}*/
	}
}
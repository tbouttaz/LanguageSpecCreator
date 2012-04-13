package WYSIWYM.ontology;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import liber.edit.client.TagCloud;
import WYSIWYM.model.SGNode;
import WYSIWYM.util.FolksonomyException;

import com.hp.hpl.jena.ontology.OntProperty;

/**	Folksonomy stores the values the user specifies for properties, together with
 *	the user, timestamp and resource ID in the tag database.
 *	In the previous version, the values were stored in separate text files for each
 *	property. These methods are still available, but deprecated.
 *
 *	This information is then used to create a tag cloud with 7 frequency groups
 *	(corresponding to 7 html-tags for font size).
 *
 *	@author Feikje Hielkema
 *	@version 1.0 20-03-2007
 *	@version 1.4 09-2008
 */
public class Folksonomy
{	
	/** Path to old folksonomy library
	 *	@deprecated */
	public static final String PATH = "data/folksonomy/";
	private String user;
	
	/**	Constructor, sets the userID
	 *	@param userID User ID
	 */
	public Folksonomy(String userID)
	{
		user = userID;
	}

	/**	Stores the given tag, with user and context, in the tag database
	 *	@param property OntProperty
	 *	@param tag The tag
	 *	@param resource Resource ID
	 *	@throws FolksonomyException if the tag could not be stored in the database
	 */
	public void store(OntProperty property, String tag, String resource) throws FolksonomyException
	{
		try
		{
			if (lessThanFourWords(tag))
				new Tag().addTag(tag, user, resource, property);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw new FolksonomyException("Error when trying to add tag to database");
		}
	}
	
	/**	Stores the given tags, with user and context, in the tag database
	 *
	 *	@param property OntProperty
	 *	@param tags String[] tags
	 *	@param resource ResourceID
	 *	@throws FolksonomyException if the tag could not be stored in the database
	 */
	public void store(OntProperty property, String[] tags, String resource) throws FolksonomyException
	{
		try
		{
			for (int i = 0; i < tags.length; i++)
			{
				if (lessThanFourWords(tags[i]))
					new Tag().addTag(tags[i], user, resource, property);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw new FolksonomyException("Error when trying to add tag to database");
		}
	}
	
	/**	Stores the given tags, with user and context, in the tag database
	 *
	 *	@param property OntProperty
	 *	@param tags List<String> tags
	 *	@param resource ResourceID
	 *	@throws FolksonomyException if the tag could not be stored in the database
	 */
	public void store(OntProperty property, List<String> tags, String resource) throws FolksonomyException
	{
		try
		{
			for (int i = 0; i < tags.size(); i++)
			{
				if (lessThanFourWords(tags.get(i)))
					new Tag().addTag(tags.get(i), user, resource, property);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw new FolksonomyException("Error when trying to add tag to database");
		}
	}
	
	/**	Returns true is the given string has less than four words (i.e. less than
	 *	three spaces).
	 */
	private boolean lessThanFourWords(String tag)
	{
		tag = tag.trim();
		int idx = 0, cntr = 1;
		while (true)
		{
			idx = tag.indexOf(" ", idx) + 1;
			if (idx > 0)
				cntr++;
			else
				break;
		}
		return (cntr < 4);
	}
		
	/**	Returns a TagCloud with the 100 most frequent tags in the 
	 *	folksonomy belonging to the given property. The tags are divided into
	 *	7 groups according to frequency.
	 *	If the property has a number datatype, the tag cloud consists of ranges of values.
	 *
	 *	@param reader Ontology
	 *	@param property OntProperty
	 *	@return TagCloud
	 *	@throws FolksonomyException if the database could not be accessed
	 */
	public TagCloud getTagCloud(OntologyReader reader, OntProperty property) throws FolksonomyException
	{
		int type = reader.getRangeType(property).intValue();
		if (type == 6)	//string property
		{
			Map<String, Integer> map = getFolksonomy(property);
			return build(map);
		}
		if ((type == 3) || (type == 4))
		{
			List<Number> list = getNumberFolksonomy(property, type);
			return buildNumber(list);
		}
		return null;
	}
	
	private TagCloud build(Map<String,Integer> map) 
	{
		Map<String, Integer> result = new HashMap<String, Integer>();
		int min = 0, max = 0;
		while (map.size() > 100)	//don't show too many tags
			removeSmallestValues(map);
			
		for (Iterator<Integer> it = map.values().iterator(); it.hasNext(); )
		{
			int i = it.next();
			if (i > max)
				max = i;
			if ((min == 0) || (i < min))
				min = i;
		}
			
		double denominator = (max - min) / 7;
		for (Iterator<String> it = map.keySet().iterator(); it.hasNext(); )
		{
			String key = it.next();
			double d = Math.floor(map.get(key) / denominator);
			result.put(key, new Double(d).intValue());
		}
		
		String[] values = new String[map.size()];
		int cntr = 0;
		for (Iterator it = map.keySet().iterator(); it.hasNext(); )
		{
			values[cntr] = (String) it.next();
			cntr++;
		}
		Arrays.sort(values, String.CASE_INSENSITIVE_ORDER);	//sort into alphabetical order
		int[] frequencies = new int[map.size()];
		for (int i = 0; i < values.length; i++)
			frequencies[i] = map.get(values[i]);
	
		TagCloud tc = new TagCloud();
		tc.create(values, frequencies);
		return tc;
	}
	
	/**	Creates a tag cloud of bands of numbers, with font size corresponding 
	 *	to the number of values in those bands.
	 */
	private TagCloud buildNumber(List<Number> list)
	{
		if (list.size() == 0)
			return null;
		
		List<String> bands = new ArrayList<String>();
		List<Integer> frequencies = new ArrayList<Integer>();
		
		double min = list.get(0).doubleValue();		//e.g. 1025
		double max = list.get(list.size() - 1).doubleValue();	//e.g. 1450
		double bandwidth = Math.pow(10, Math.floor(Math.log10(max - min)));	//e.g. 10Log(1450 - 1025) ~ 2; 10^2 = 100; bands 1000-1099, 1100-1200, etc.
		double start = min;
		if (bandwidth >= 1)	//if the bandwidth is .1 or smaller, we cannot round to the nearest integer!
			start = ((int) (min / bandwidth)) * bandwidth;				//e.g. 1025/100 = 10 * 100 = 1000
		
		int listIdx = 0;
		for (int i = 0; i < 10; i++)
		{
			int frequency = 0;
			double floor = start + (i * bandwidth);	//e.g. 1000 + (2 * 100) = 
			double ceiling = floor + bandwidth;	//e.g. 1300
			while (listIdx < list.size())
			{
				if (list.get(listIdx).doubleValue() > ceiling)
					break;
				frequency++;
				listIdx++;
			}
			
			if (frequency > 0)
				frequencies.add(new Integer(frequency));
			else if (listIdx == list.size())
				break;
			else if (listIdx == 0)
				continue;
			else
				frequencies.add(new Integer(-1));
			bands.add(new String(SGNode.getNL(new Double(floor)) + "-" + SGNode.getNL(new Double(ceiling))));	//e.g. 1200-1300
		}
		
		int minimum = 0, maximum = 0;
		for (int i = 0; i < frequencies.size(); i++)
		{
			int nr = frequencies.get(i);
			if (nr > maximum)
				maximum = nr;
			if ((minimum == 0) || (nr < minimum))
				minimum = nr;
		}
			
		String[] values = new String[bands.size()];
		int[] freq = new int[bands.size()];
		double denominator = (maximum - minimum) / 5;
		for (int i = 0; i < bands.size(); i++)
		{
			values[i] = bands.get(i);
			if (frequencies.get(i) > 0)
				freq[i] = (int) Math.floor(frequencies.get(i) / denominator) + 2;
			else
				freq[i] = 0;
		}
	
		TagCloud tc = new TagCloud();
		tc.create(values, freq);
		tc.setNumber(true);
		return tc;		
	}
	
	/**	Returns a tag cloud with tags used for any of the properties in the given list.
	 *
	 *	@param reader Ontology
	 *	@param properties List<Ontproperty>
	 *	@return TagCloud
	 *	@throws FolksonomyException if the database could not be accessed
	 */
	public TagCloud getTagCloud(OntologyReader reader, List<OntProperty> properties) throws FolksonomyException
	{
		Map<String,Integer> map = new HashMap<String,Integer>();
		for (OntProperty p : properties)
		{	//add all tags with accummulated frequencies to the one map
			if (reader.getRangeType(p).intValue() != 6)
				continue;		//only make string tag clouds
				
			Map<String,Integer> cloud = getFolksonomy(p);
			for (Iterator it = cloud.keySet().iterator(); it.hasNext(); )
			{
				String key = (String) it.next();
				if (map.containsKey(key))
				{
					Integer freq = map.get(key).intValue() + cloud.get(key).intValue();
					map.put(key, freq);
				}
				else
					map.put(key, cloud.get(key));
			}
		}
		return build(map);	//then build the aggregated tag cloud
	}
		
	/**	Returns a tag cloud of property names, based on how often these properties
	 *	have been instantiated.
	 *	@return HashMap with property names and frequencies
	 *	@deprecated Uses old text file approach
	 */
	public Map<String, Integer> getPropertyTagCloud()
	{	
		File dir = new File(PATH);
		String[] fileNames = dir.list();
		Map<String, Integer> result = new HashMap<String, Integer>();
		for (int i = 0; i < fileNames.length; i++)
		{
			InputStream in = null;
			try
   			{
	   			in = new FileInputStream(PATH + fileNames[i]);
	  		}
	   		catch(IOException e)
	   		{
	  			continue;
	   		}
	   		Scanner scan = new Scanner(in);
	   		int value = 0;
			while (scan.hasNext())
			{
				try		//values may contain spaces, which are used as delimiters by the scanner
				{		//so check for numberformatexceptions
					value += Integer.parseInt(scan.next());	//HOW LONG WILL THIS TAKE?
					break;
				}
				catch(NumberFormatException e)
				{}
			}
			int end = fileNames[i].lastIndexOf(".txt");		//remove .txt from filename to get property name
			result.put(fileNames[i].substring(0, end), value);
			scan.close();
		}

		return result;
	}
	
	/**	Removes the least used tags from the map until there are only 150 tags left.
	 */
	private void removeSmallestValues(Map<String, Integer> map)
	{
		List<String> keys = new ArrayList<String>();
		int min = 0;
		for (Iterator<String> it = map.keySet().iterator(); it.hasNext(); )
		{
			String key = it.next();
			int value = map.get(key);
			if ((min == 0) || (value < min))
			{
				keys = new ArrayList<String>();
				keys.add(key);
				min = value;
			}
			else if (value == min)
				keys.add(key);
		}
		for (int i = 0; i < keys.size(); i++)
			map.remove(keys.get(i));
	}
	
	private Map<String,Integer> getFolksonomy(OntProperty p) throws FolksonomyException
	{
		try
		{
			return new Tag().getPropertyTags(p);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw new FolksonomyException("Error in getting tag cloud for property " + p.getLocalName());
		}
	}
	
	/**	Finds the correct folksonomy file (property.txt) and reads the tags and their 
	 *	frequency from it into a map.
	 *
	 *	OLD APPROACH; USE SQL DATABASE INSTEAD
	 *	@deprecated
	 */
	private Map<String, Integer> getFolksonomy(String property)
	{
		Map<String, Integer> map = new HashMap<String, Integer>();
   		InputStream in;
   		try
   		{
	   		in = new FileInputStream(PATH + property + ".txt");
	  	}
	   	catch(IOException e)
	   	{
	  		return map;
	   	}	
		if (in != null)
		{
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
						map.put(value.toString(), freq);
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
		return map;
	}
	
	private List<Number> getNumberFolksonomy(OntProperty p, int type) throws FolksonomyException
	{
		Map<String,Integer> map = getFolksonomy(p);
		List<Number> result = new ArrayList<Number>();
		for (Iterator it = map.keySet().iterator(); it.hasNext(); )
		{
			String key = (String) it.next();
			try
			{
				Number value = null;
				if (type == 3)
					value = new Double(key);
				else
					value = new Integer(key);
				for (int i = 0; i < map.get(key).intValue(); i++)
					result.add(value);
			}
			catch(NumberFormatException e)
			{
				System.out.println(key + " is not a number!");
			}
		}
		return result;
	}
	
	/**	Returns a sorted list of all numerical values in the folksonomy
	 *		@deprecated
	 */
	private List<Number> getNumberFolksonomy(String property, int type)
	{
		List<Number> result = new ArrayList<Number>();
		InputStream in;
   		try
   		{
	   		in = new FileInputStream(PATH + property + ".txt");
	  	}
	   	catch(IOException e)
	   	{
	  		return result;
	   	}	
		if (in != null)
		{
			Scanner scan = new Scanner(in);
			while (scan.hasNext())
			{
				String str = "";
				try
				{	
					str = scan.next();
					if (type == 3)
						result.add(new Double(str));
					else
						result.add(new Integer(str));
				}
				catch(NumberFormatException e)
				{
					System.out.println(str + " is not a number!");
				}
			}
			scan.close();
		}
		Collections.sort(result, new CompareNR());
		return result;
	}
	
	private class CompareNR implements Comparator
	{
		public int compare(Object o1, Object o2) throws ClassCastException
		{
			double d1 = ((Number) o1).doubleValue(); 
			double d2 = ((Number) o2).doubleValue(); 
			if (d1 < d2)
				return -1;
			if (d1 == d2)
				return 0;
			return 1;
		}
	}
	
	/**	Opens the correct folksonomy file (property.txt) and stores the tags in 
	 *	the given map.
	 *	@deprecated
	 */
	private void writeFile(String property, Map<String, Integer> map)
	{
		try
		{
			FileWriter fw = new FileWriter(PATH + property + ".txt");
			PrintWriter w = new PrintWriter(fw);
			for (Iterator<String> it = map.keySet().iterator(); it.hasNext(); )
			{
				String key = it.next();
				w.print(key);
				w.print(" ");
				w.println(map.get(key));
			}
			w.close();
			fw.close();
		}
		catch(IOException e)
		{
			System.out.println("Encountered error when storing the new tag(s) in the folksonomy.");
			e.printStackTrace();
		}
	}
}
package simplenlg.realiser;

import java.util.List;

import liber.edit.client.AnchorInfo;
import WYSIWYM.model.Anchor;
import WYSIWYM.model.QueryResultAnchor;
import WYSIWYM.model.SGNode;
import WYSIWYM.model.SummationAnchor;

/**
 *	AnchorString contains a String and an Anchor. It is a construct necessary for WYSIWYM feedback texts.
 *	This version of simpleNLG has been adapted to return lists of AnchorStrings, instead of concatenated
 *	Strings.
 *
 * @author Feikje Hielkema
 * @version 1.0 24-11-2006 
 *
 */
public class AnchorString
{
	private Anchor anchor;
	private String str;
	
	public AnchorString()
	{}
	
	public AnchorString(String s, Anchor a)
	{
		str = s;
		anchor = a;
	}
	
	public AnchorString(List<AnchorString> l, Anchor a)
	{
		StringBuffer sb = new StringBuffer();
		for (AnchorString as : l)
			sb.append(as.toString());	//assuming here that there is no nested anchor in there (would be weird if that happened)
		str = sb.toString();
		anchor = a;
	}
	
	public void setAnchor(Anchor a)
	{
		anchor = a;
	}
	
	public boolean isAnchor()
	{
		if (anchor == null)
			return false;
		return true;
	}
	
	public Anchor getAnchor()
	{
		return anchor;
	}
	
	public String toString()
	{
		return str;
	}
	
	public void setString(String s)
	{
		str = s;
	}
	
	public static String getString(Object o)
	{
		if (o instanceof String)
			return (String) o;
		else if (o instanceof AnchorString)
			return ((AnchorString)o).toString();
		else 
			return o.toString();
	}
	
	/*	Puts all information that is necessary to create the popup menu for the
	 *	given instance in an AnchorInfo, a serialisable equivalent of the AnchorString.
	 */
	public AnchorInfo toAnchorInfo()
	{
		AnchorInfo ai = new AnchorInfo();
		ai.setWords(str);
		
		if (anchor != null)
		{	
			ai.setAnchor(true);
			ai.setID(anchor.getID());
			if (anchor instanceof SummationAnchor)
				ai.setSummation(true);
			else if (!(anchor instanceof QueryResultAnchor))
			{	
				ai.setRed(anchor.isRed());
				ai.setCompulsory(anchor.getCompulsoryArray());
				ai.setOptional(anchor.getOptionalArray());
				ai.setOptionalPropType(anchor.getOptionalPropType());
				ai.setCompulsoryNL(anchor.getCompulsoryNL());
				ai.setOptionalNL(anchor.getOptionalNL());
				ai.setShowHide(anchor.addShowOrHideOption());
				ai.setURI(anchor.getURI());

				String[] removableProp = anchor.getRemovable();
				ai.setRemovable(removableProp);
				ai.setRemovableNL(anchor.getRemovableNL());
				String[][] values = new String[removableProp.length][0];
				for (int i = 0; i < removableProp.length; i++)
					values[i] = anchor.getValues(removableProp[i]);
				ai.setRemovableValues(values);
				ai.setRemovableNode(((SGNode) anchor.getNode()).isRemovable());
			}
		}
		else
			ai.setAnchor(false);
		
		return ai;
	}

}
package WYSIWYM.util;

import java.io.PrintWriter;
import java.util.GregorianCalendar;

/**	This class contains session information we want to store for the evaluation
 *	@deprecated Only used for usability evaluation
 */
public class UserInfo
{
	public int sessionNr = 0;
	public int buttonsPressed = 0;	
	public int menuItemsChosen = 0;
		
	public int rangeObjectsRemoved = 0;
	public int rangeObjectsCreated = 0;
	public int rangeObjectsAdded = 0;

	public int dataValuesCreated = 0;
	public int browsingSessions = 0;
	
	public int undo = 0, redo = 0;
	public int helpSessions = 0;
	public int introductionItemsOpened = 0;
	public int helpPanesOpened = 0;

	public int folksonomyUsed = 0;
	public int reset = 0;	//hope that doesn't happen too often...

	public long processingTime, endTime;
	public long beginTime = new GregorianCalendar().getTimeInMillis();	
	
	public int reset()
	{
		System.out.println("RESETTING USER INFO!!!!!!!!!!!!!!!!!!!");	
	 /**	sessionNr++;
		menuItemsChosen = 0;
		buttonsPressed = 0;
		
		rangeObjectsRemoved = 0;
		rangeObjectsCreated = 0;
		rangeObjectsAdded = 0;
		dataValuesCreated = 0;
		
		undo = 0;
		redo = 0;
		helpSessions = 0;
		introductionItemsOpened = 0;
		helpPanesOpened = 0;
		
		folksonomyUsed = 0;
		reset = 0;
		
		beginTime = new GregorianCalendar().getTimeInMillis();	*/
		return sessionNr;
	}
	
	public void print(PrintWriter w)
	{
		w.print(" session ");
		w.println(sessionNr);
		
		long time = endTime - beginTime;	
		w.print("Total time: ");
		w.println((time / 1000));			//not really interested in knowing time to the millisecond...
		w.print("Processing time: ");
		w.println((processingTime / 1000));
		w.print("Effective user time: ");
		w.println(((time - processingTime) / 1000));
		w.println();
		
	/**	w.print("Menu items selected: ");
		w.println(menuItemsChosen);
		w.print("Buttons pressed: ");
		w.println(buttonsPressed);
		w.println();
		
		w.print("Data values added: ");
		w.println(dataValuesCreated);*/
		w.print("Browsing sessions: ");
		w.println(browsingSessions);
	/**	w.print("New range objects created: ");
		w.println(rangeObjectsCreated);
		w.print("Existing range objects added: ");
		w.println(rangeObjectsAdded);
		w.print("Previous range objects removed: ");
		w.println(rangeObjectsRemoved);
		w.println();
		
		w.print("Times information was removed: ");
		w.println(undo);
		w.print("Times redo was pressed: ");
		w.println(redo);*/
		w.print("Number of help sessions: ");
		w.println(helpSessions);
		w.print("Number of help introduction messages viewed: ");
		w.println(introductionItemsOpened);
		w.print("Number of help panes viewed: ");
		w.println(helpPanesOpened);
		w.println();
		
/**		w.print("Number of items chosen from folksonomy: ");
		w.println(folksonomyUsed);
		w.print("Number of times pressed reset: ");
		w.println(reset);
		w.println();
		
		w.print("Total operations performed: ");
		w.println((menuItemsChosen + dataValuesCreated + buttonsPressed));*/
	}
}
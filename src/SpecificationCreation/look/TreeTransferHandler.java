package SpecificationCreation.look;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;

import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JTree;
import javax.swing.TransferHandler;
import javax.swing.tree.TreePath;

/**	Handles drag and drop in property tree.
 *
 *	@author Feikje Hielkema
 *	@version 1.0 October 26 2008
 */
public class TreeTransferHandler extends TransferHandler 
{
	private ClassPane pane;
	
	/**	Constructor
	 *	@param p Parent ClassPane
	 */
	public TreeTransferHandler(ClassPane p)
	{
		super();
		pane = p;
	}
	
	/**	Only move nodes, don't copy them.
	 *	@param comp Tree component
	 *	@return TransferHandler#MOVE
	 *	@see TransferHandler
	 */
	public int getSourceActions(JComponent comp)
	{
		return MOVE;
	}
	
	/**	Transfers the string value of the property.
	 *	@param c Tree component
	 *	@return Transferabe
	 *	@see TransferHandler#createTransferable(JComponent)
	 */
	public Transferable createTransferable(JComponent c) 
	{
		if (c instanceof JTree)
		{
			JTree tree = (JTree) c;
			TreePath path = tree.getSelectionPath();
			ClassTreeNode node = (ClassTreeNode) path.getLastPathComponent();
			if (node.isCardinal() || node.isMenu() || node.isShaded())
				return null;
	 		return new StringSelection(node.toString());
	 	}

 		JList list = (JList) c;
 		return new StringSelection(list.getSelectedValue().toString());
	}

	/**	When the node has been exported, remove it from its old position.
	 *	@param c JComponent
	 *	@param t Transferable
	 *	@param action TransferHandler#MOVE
	 */
	public void exportDone(JComponent c, Transferable t, int action) 
	{
		if (action == MOVE)
		{
			if (c instanceof JTree)
			{
				JTree tree = (JTree) c;
				TreePath path = tree.getSelectionPath();
				pane.removeNode(path);
			}
			else
			{
				JList list = (JList) c;
				pane.removeListItem(list.getSelectedIndex());
			}
		}
	}

	/**	Check if the method can import a value.
     * We only support importing strings (the labels of the nodes/list items).
     *	@param info TransferHandler.TransferSupport
     *	@return true if the value can be imported
     */
    public boolean canImport(TransferHandler.TransferSupport info) 
    {	// Check for String flavor
        if (!info.isDataFlavorSupported(DataFlavor.stringFlavor))
            return false;

        if (info.getDropLocation() instanceof JList.DropLocation)
        	return true;	//can always import nodes in list?
        	
		JTree.DropLocation loc = (JTree.DropLocation) info.getDropLocation();
   	 	TreePath path = loc.getPath();
   		if (path == null)
   			return false;
   			
   		ClassTreeNode node = (ClassTreeNode) path.getLastPathComponent();
   		if (node.isMenu())	//if this is a submenu, allow the drop
   			return true;	//always import nodes into tree?
   		if (node.isRoot())	//if it's the root, allow the drop
   			return true;
   		return false;	//else don't allow
   	}

	/**
     * Perform the actual import. Only supports drag and drop.
     *	@param info TransferHandler.TransferSupport
     *	@return true if the value has been imported, false if there was an error
     */
    public boolean importData(TransferHandler.TransferSupport info) 
    {
        if (!info.isDrop())
            return false;
	
        Transferable t = info.getTransferable();
        try 
        {
            String data = (String) t.getTransferData(DataFlavor.stringFlavor);     
            if (info.getDropLocation() instanceof JList.DropLocation)
            {
            	JList.DropLocation loc = (JList.DropLocation) info.getDropLocation();
            	pane.addToList(data);
            }   
            else
            {
	    		JTree.DropLocation loc = (JTree.DropLocation) info.getDropLocation();
    			pane.makeNode(data, loc.getPath());
    		}
			return true;
        } 
        catch (Exception e) 
        {
        	e.printStackTrace(); 
        	return false; 
        }
	}
}
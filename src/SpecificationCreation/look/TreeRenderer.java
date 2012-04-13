package SpecificationCreation.look;

import java.awt.Color;
import java.awt.Component;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

/**	Displays a little 'ok' icon before a tree node if its adaptation to LIBER 
 *	has been completed.
 *	@author Feikje Hielkema
 *	@version 1.0 October 20 2008
 */
public class TreeRenderer extends DefaultTreeCellRenderer 
{
    Icon icon;
    
    /**	Constructor
     *	@param path Pathname
     */
    public TreeRenderer(String path) 
    {
        icon = createImageIcon(path);
    }
	
	/**	Renders the tree node.
     *	@param tree JTree
     *	@param expanded True is node is expanded
     *	@param leaf True if node is terminal
     *	@param hasFocus True if node has focus
     *	@see DefaultTreeCellRenderer#getTreeCellRendererComponent(JTree,Object,boolean,boolean,boolean,int,boolean)
     */
    public Component getTreeCellRendererComponent(JTree tree, Object value, 
    	boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) 
    {
    	if (icon == null)
    		return this;
    	
    	Color color = Color.black;
    	if (shaded(value))
    		color = Color.gray;
    	else if (menu(value))
    		color = Color.blue;
    	else if (cardinal(value))
    		color = Color.red;
    	else if (isNLProp(value))
    		color = Color.green;
    	setTextNonSelectionColor(color);
        setTextSelectionColor(color);
    	super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);	
    	if (completed(value))
    		setIcon(icon);

        return this;
    }
    
    private boolean shaded(Object value)
    {
    	return ((ClassTreeNode) value).isShaded();
    }
    
    private boolean completed(Object value)
    {
    	return ((ClassTreeNode) value).isCompleted();
    }
    
    private boolean menu(Object value)
    {
    	return ((ClassTreeNode) value).isMenu();
    }
    
    private boolean cardinal(Object value)
    {
    	return ((ClassTreeNode) value).isCardinal();
    }
    
    private boolean isNLProp(Object value)
    {
    	return ((ClassTreeNode) value).isNLProp();
    }
    
     /*** Returns an ImageIcon, or null if the path was invalid. */
    private ImageIcon createImageIcon(String path) 
    {
        java.net.URL imgURL = getClass().getClassLoader().getResource(path);
        if (imgURL != null) 
            return new ImageIcon(imgURL);
        else
        {
            System.err.println("Couldn't find file: " + path);
            return null;
        }
    }
}

package liber.browse.client;

import liber.edit.client.AnchorInfo;
import liber.edit.client.ExistingInstances;
import liber.edit.client.FormInfo;
import liber.edit.client.Hierarchy;
import liber.edit.client.InstanceData;
import liber.edit.client.TagCloud;

import com.google.gwt.user.client.rpc.RemoteService;

/**	This interface lists all methods implemented by the servlet LiberServletImpl.
 *	Please refer to its documentation for explanations of these methods.
 *	@see liber.edit.server.LiberServletImpl
 */
public interface LiberServlet extends RemoteService 
{
	public String[] newSession(String user, String project, String resource);
	
	public AnchorInfo[] initSession(String user, String resource, int type);
	
	public AnchorInfo[] initSession(String user, InstanceData data);
	
	public Hierarchy[] initSessionAndGetClassHierarchy(String user, String[] roots);
	
	public void endSession(String user, int type, String key);
		
	public void printQueryTimes(String user);
	
	public String upload(String user, int type, String key);
	
	public AnchorInfo[][] getSPARQL(String user, long time);
	
	public AnchorInfo[] getQueryResult(String user, long time);
	
	public AnchorInfo[] getFeedbackText(String user, int type, String key);
	
	public AnchorInfo[] getBrowsingDescription(String user, String id);
	
	public AnchorInfo[] getBrowsingDescription(String user, int idx);
	
	public Hierarchy[] getClassHierarchy(String user, String[] roots);
	
	public Hierarchy[] getRangeHierarchy(String user, String prop);
	
	public ExistingInstances getInstances(String user, String name, String anchor, int type, String key); 
	
	public Integer[] getType(String user, String anchor, String property, int type, String key);
	
	public String[] getRange(String user, String property);
	
	public FormInfo[] getCardinalStringProperties(String user, String type);
	
	public String[] getAddedValues(String user, String property, String anchor, int type, String key);
	
//	public String getDateExpression(String user, QueryDateValue input);
	
	public TagCloud getTagCloud(String user, String property);
	
	public TagCloud getTagCloud(String user, String[] properties);
	
	public String[] getDescriptionValues(String user, String resourceID, FormInfo[] formInformation);
	
	public AnchorInfo[] undo(String user, int type, String key);
	
	public AnchorInfo[] removeProperty(String user, String anchor, String property, String[] values, int type, String key);
	
	public AnchorInfo[] removeAnchor(String user, String anchor, int type, String key);
	
	public AnchorInfo[] redo(String user, int type, String key);
	
	public AnchorInfo[] changeTextContent(String user, String anchor, boolean show, int type, String key);
	
	public AnchorInfo[] showSummation(String user, String anchor, int type, String key);
	
	public AnchorInfo[] update(String user, String anchor, String property, String value, int type, String key);
	
	public AnchorInfo[] updateDate(String user, String anchor, String property, String[] date, int dateCntr, boolean updateText, int type, String key);
	
	public AnchorInfo[] updateBoolean(String user, String anchor, String property, boolean b, int type, String key);
	
	public AnchorInfo[] updateNumber(String user, String anchor, String property, Number nr, int type, String key);
	
	public AnchorInfo[] multipleUpdate(String user, String anchor, String property, String[] idx, int type, String key);
	
//	public AnchorInfo[] updateDate(String user, String anchor, String property, QueryDateValue[] values, String operator);
	
	public AnchorInfo[] multipleValuesUpdate(String user, String anchor, String property, String[] values,  int datatype, String operator);
	
	public AnchorInfo[] multipleValuesUpdate(String user, String anchor, String property, String[] values, int datatype, int type, String key);
	
	public AnchorInfo[] updateAbstract(String user, String anchor, String property, String value, int type, String key);
	
	public AnchorInfo[] updateObjectProperty(String user, String anchor, String property, InstanceData[] range, int type, String key);
	
	public Boolean exportObject(String user, String anchor, String key);
	
	public Integer getMatchNr(String user);
	
	public Integer sendOptionalInfo(String user, Boolean[] checks);
	
	public Boolean[] getCheckedOptionals(String user);
	
	public String[] checkDatabase(String user, String anchor, String property, int type, String key, String title, String[] types);
	
	public String[] checkDatabase(String user, String anchor, String property, int type, String key, InstanceData data);	
}
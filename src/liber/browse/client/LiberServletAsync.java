package liber.browse.client;

import com.google.gwt.user.client.rpc.AsyncCallback;
import liber.edit.client.*;

/**	This asynchronous interface lists all methods implemented by the servlet LiberServletImpl.
 *	Please refer to its documentation for explanations of these methods.
 *	@see liber.edit.server.LiberServletImpl
 */
public interface LiberServletAsync 
{
	void newSession(String user, String project, String resource, AsyncCallback callback);
	
	void initSession(String user, String resource, int type, AsyncCallback callback);
	
	void initSession(String user, InstanceData data, AsyncCallback callback);
	
	void initSessionAndGetClassHierarchy(String user, String[] roots, AsyncCallback callback);
	
	void endSession(String user, int type, String key, AsyncCallback callback);
		
	void printQueryTimes(String user, AsyncCallback callback);
	
	void upload(String user, int type, String key, AsyncCallback callback);
	
	void getSPARQL(String user, long time, AsyncCallback callback);
	
	void getQueryResult(String user, long time, AsyncCallback callback);
	
	void getFeedbackText(String user, int type, String key, AsyncCallback callback);
	
	void getBrowsingDescription(String user, String id, AsyncCallback callback);
	
	void getBrowsingDescription(String user, int idx, AsyncCallback callback);
	
	void getClassHierarchy(String user, String[] roots, AsyncCallback callback);
	
	void getRangeHierarchy(String user, String prop, AsyncCallback callback);
	
	void getInstances(String user, String name, String anchor, int type, String key, AsyncCallback callback); 
	
	void getType(String user, String anchor, String property, int type, String key, AsyncCallback callback); 
	
	void getRange(String user, String property, AsyncCallback callback);
	
	void getCardinalStringProperties(String user, String type, AsyncCallback callback);
	
	void getAddedValues(String user, String property, String anchor, int type, String key, AsyncCallback callback);
	
//	void getDateExpression(String user, QueryDateValue input, AsyncCallback callback);
	
	void getTagCloud(String user, String property, AsyncCallback callback);
	
	void getTagCloud(String user, String[] properties, AsyncCallback callback);
	
	void getDescriptionValues(String user, String resourceID, FormInfo[] formInformation, AsyncCallback callback);
	
	void undo(String user, int type, String key, AsyncCallback callback);
	
	void removeProperty(String user, String anchor, String property, String[] values, int type, String key, AsyncCallback callback);
	
	void removeAnchor(String user, String anchor, int type, String key, AsyncCallback callback);
	
	void redo(String user, int type, String key, AsyncCallback callback);
	
	void changeTextContent(String user, String anchor, boolean show, int type, String key, AsyncCallback callback);
	
	void showSummation(String user, String anchor, int type, String key, AsyncCallback callback);
	
	void update(String user, String anchor, String property, String value, int type, String key, AsyncCallback callback);
	
	void updateDate(String user, String anchor, String property, String[] date, int dateCntr, boolean updateText, int type, String key, AsyncCallback callback);
	
	void updateBoolean(String user, String anchor, String property, boolean b, int type, String key, AsyncCallback callback);
	
	void updateNumber(String user, String anchor, String property, Number nr, int type, String key, AsyncCallback callback);
	
	void multipleUpdate(String user, String anchor, String property, String[] idx, int type, String key, AsyncCallback callback);
	
//	void updateDate(String user, String anchor, String property, QueryDateValue[] values, String operator, AsyncCallback callback);
	
	void multipleValuesUpdate(String user, String anchor, String property, String[] values,  int datatype, String operator, AsyncCallback callback);
	
	void multipleValuesUpdate(String user, String anchor, String property, String[] values, int datatype, int type, String key, AsyncCallback callback);
	
	void updateAbstract(String user, String anchor, String property, String value, int type, String key, AsyncCallback callback);
	
	void updateObjectProperty(String user, String anchor, String property, InstanceData[] range, int type, String key, AsyncCallback callback);
	
	void exportObject(String user, String anchor, String key, AsyncCallback callback);
	
	void getMatchNr(String user, AsyncCallback callback);
	
	void sendOptionalInfo(String user, Boolean[] checks, AsyncCallback callback);
	
	void getCheckedOptionals(String user, AsyncCallback callback);
	
	void checkDatabase(String user, String anchor, String property, int type, String key, String title, String[] types, AsyncCallback callback);
	
	void checkDatabase(String user, String anchor, String property, int type, String key, InstanceData data, AsyncCallback callback);		
}
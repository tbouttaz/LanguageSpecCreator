package WYSIWYM.util;

import java.io.File;

/**	@deprecated
 *	Used for testing
 */

public class ProperDirSelector
{
	File dir;
	
	public ProperDirSelector(Object o)
	{
		File tmp = new File("x");
		StringBuffer sb = new StringBuffer(tmp.getAbsolutePath());
		int c = sb.indexOf("classes");
		if (c > 0)
			sb.delete(c, c + 7);
		sb.replace(sb.length() - 1, sb.length(), "data/");
		sb.append(o.getClass().getPackage().getName());
		sb.append('/');
		
		String str = sb.toString();
		str = str.replace('.', '/');
		
		dir = new File(str);
	}
		
	public File getDir()
	{
		return dir;
	}
}
package liber.edit.server;

import java.util.HashMap;

/**	Defines abbreviatins of file extensions, for Fedora.
 *	@author Feikje Hielkema
 *	@version 1.3 June 2008
 */
public class Extensions
{
	HashMap<String,String> map = new HashMap<String,String>();
	
	/**	Default constructor
	 */
	public Extensions()
	{
		map.put("ai", "application/postscript");
		map.put("avi", "video/avi");
		map.put("bm", "image/bmp");
		map.put("bmp", "image/bmp");
		map.put("class", "application/java");
		map.put("css", "text/css");
		map.put("df", "video/dl");
		map.put("doc", "application/msword");
		map.put("dot", "application/msword");
		map.put("dvi", "application/x-dvi");
		map.put("eps", "application/postscript");
		map.put("exe", "application/octet-stream");
		map.put("gif", "image/gif");
		map.put("gl", "video/gl");
		map.put("gtar", "application/x-gtar");
		map.put("gz", "application/x-gzip");
		map.put("gzip", "application/x-gzip");
		map.put("hlp", "application/x-helpfile");
		map.put("help", "application/x-helpfile");
		map.put("htm", "text/html");
		map.put("html", "text/html");
		map.put("htmls", "text/html");
		map.put("imap", "application/x-httpd-imap");
		map.put("jpg", "image/jpeg");
		map.put("jpeg", "image/jpeg");
		map.put("jpe", "image/jpeg");
		map.put("latex", "application/x-latex");
		map.put("map", "application/x-navimap");
		map.put("mid", "audio/midi");
		map.put("midi", "audio/midi");
		map.put("mime", "www/mime");
		map.put("mjpg", "video/x-motion-jpeg");
		map.put("movie", "video/x-sgi-movie");
		map.put("mov", "video/quicktime");
		map.put("moov", "video/quicktime");
		map.put("mp3", "audio/mpeg");
		map.put("mp2", "audio/mpeg");
		map.put("mpg", "audio/mpeg");
		map.put("mv", "video/x-sgi-movie");
		map.put("pdf", "application/pdf");
		map.put("pic", "image/pict");
		map.put("pict", "image/pict");
		map.put("png", "image/png");
		map.put("pot", "application/mspowerpoint");
		map.put("pps", "application/mspowerpoint");
		map.put("ppt", "application/mspowerpoint");
		map.put("ppz", "application/mspowerpoint");
		map.put("ps", "application/postscript");
		map.put("qt", "video/quicktime");
		map.put("qti", "image/x-quicktime");
		map.put("rtf", "application/rtf");
		map.put("rt", "text/richtext");
		map.put("rtx", "text/richtext");
		map.put("sh", "application/x-sh");
		map.put("shtml", "text/html");
		map.put("tar", "application/x-tar");
		map.put("tex", "application/x-tex");
		map.put("texi", "application/x-texinfo");
		map.put("texinfo", "application/x-texinfo");
		map.put("tgz", "application/gnutar");
		map.put("tif", "image/tiff");
		map.put("tiff", "image/tiff");
		map.put("viv", "video/vivo");
		map.put("vivo", "video/vivo");
		map.put("wav", "audio/wav");
		map.put("word", "application/msword");
		map.put("xl", "application/excel");
		map.put("xla", "application/excel");
		map.put("xlb", "application/excel");
		map.put("xlc", "application/excel");
		map.put("xld", "application/excel");
		map.put("xlk", "application/excel");
		map.put("xll", "application/excel");
		map.put("xlm", "application/excel");
		map.put("xls", "application/excel");
		map.put("xlt", "application/excel");
		map.put("xlv", "application/excel");
		map.put("xlw", "application/excel");
		map.put("zip", "application/x-compressed");
		map.put("z", "application/x-compressed");
		map.put("sav", "application/x-spss");
		map.put("spp", "application/x-spss");
		map.put("sbs", "application/x-spss");
		map.put("sps", "application/x-spss");
		map.put("spo", "application/x-spss");	
	}
	
	/**	Returns the full name of this file extension
	 *	@param str Extension
	 *	@return String
	 */
	public String get(String str)
	{
		if (map.containsKey(str))
			return map.get(str);
		return null;
	}
}
package burp.gather.utils;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Response {

	private HttpURLConnection con;

	public Response(HttpURLConnection con) {
		this.con = con;
	}

	/**
	 * 
	 * @return  获取原始请求
	 */
	public HttpURLConnection getCon() {
		return con;
	}

	/**
	 * 获取请求头
	 * @param key
	 * @return
	 */
	public String getHeader(String key) {
		return this.con.getHeaderField(key);
	}

	/**
	 * 
	 * @return	获取内容，默认编码为GBK
	 */
	public String getBody() {

		String charset = null;
		String temp = this.con.getContentType();
		if(temp.toUpperCase().indexOf("GBK")!=-1){
			charset = "GBK";
		}
		else if(temp.toUpperCase().indexOf("GB2312") != -1){
			charset = "gb2312";
		}
		else{
			charset = "utf-8";
		}

		return this.getBody(charset);
	}
	
	
	/**
	 * 
	 * @param charset	字符编码
	 * @return	获取内容
	 */
	public String getBody(String charset) {

		BufferedReader buf = null;
		try {
			buf = new BufferedReader(new InputStreamReader(this.con
					.getInputStream(),charset));
		} catch (IOException e) {
			e.printStackTrace();
		}

		StringBuilder sb = new StringBuilder();
		try {
			for (String temp = buf.readLine(); temp != null; temp = buf
					.readLine()) {
				sb.append(temp);
				sb.append(System.getProperty("line.separator"));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return sb.toString();
	}
	

	/**
	 * 
	 * @return HTTP 状态码
	 */
	public int getResponseCode() {
		int temp = -1 ;
		try {
			 temp = this.con.getResponseCode() ;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return temp ;
		
	}

	/**
	 *
	 * @return Title
	 */
	public String getTitle(){



		String s = getBody();
		String regex;
		String title = "";
		final List<String> list = new ArrayList<String>();
		regex = "(?i)<title>.*?</title>";
		final Pattern pa = Pattern.compile(regex, Pattern.CANON_EQ);
		final Matcher ma = pa.matcher(s);
		while (ma.find())
		{
			list.add(ma.group());
		}
		for (int i = 0; i < list.size(); i++)
		{
			title = title + list.get(i);
		}
		//remove tag
		return title.replaceAll("<.*?>", "");
	}
	

}

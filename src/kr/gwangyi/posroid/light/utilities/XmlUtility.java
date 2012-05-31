package kr.gwangyi.posroid.light.utilities;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Calendar;
import java.util.TimeZone;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.content.Context;

public class XmlUtility
{
	public static XmlPullParser makeInstanceFromUrl(Context context, URL url) throws IOException, XmlPullParserException
	{
		return makeInstanceFromUrl(context, url, url.getFile().replaceAll("^.*/", ""));
	}
	public static XmlPullParser makeInstanceFromUrl(Context context, URL url, String filename) throws IOException, XmlPullParserException
	{
		try
		{
			long modified = context.getFileStreamPath(filename).lastModified() + TimeZone.getDefault().getRawOffset();
			long now = Calendar.getInstance().getTimeInMillis() + TimeZone.getDefault().getRawOffset();
			
			modified -= modified % 86400000L;
			now      -= now      % 86400000L;
			if(modified < now) throw new FileNotFoundException();
			
			FileInputStream fis = context.openFileInput(filename);
			XmlPullParser parser;
			XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
			
			parser = factory.newPullParser();
			parser.setInput(fis, null);
			
			return parser;
		}
		catch (FileNotFoundException e)
		{
			InputStream is = url.openStream();
			byte[] buffer = new byte[1024];
			int size;
			FileOutputStream fos = context.openFileOutput(url.getFile().replaceAll("^.*/", ""), Context.MODE_PRIVATE);
			while ((size = is.read(buffer)) >= 0)
			{
				fos.write(buffer, 0, size);
			}
			fos.close();
			is.close();
			return makeInstanceFromUrl(context, url);
		}
	}
}

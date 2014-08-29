package bean;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;

import com.vikaa.allcontact.R;

import tools.Logger;
import android.content.Context;
import android.content.res.AssetManager;
import android.util.Xml;

public class FunsListEntity extends Entity{

	public List<FunsEntity> funs = new ArrayList<FunsEntity>() ;
	
	public static FunsListEntity parse(Context context) {
		FunsListEntity data = new FunsListEntity();
		XmlPullParser xmlParser = Xml.newPullParser();
		AssetManager assetManager = context.getAssets();
		InputStream is;
		try {
			is = assetManager.open("Funs.xml");
			xmlParser.setInput(is, UTF8);
			int evtType=xmlParser.getEventType();
			FunsEntity fun;
			while(evtType!=XmlPullParser.END_DOCUMENT){ 
				String tag = xmlParser.getName(); 
				switch(evtType){ 
		    		case XmlPullParser.START_TAG:
		    			if (tag.equalsIgnoreCase("BUSINESS")) {
		    				fun = new FunsEntity();
		    				fun.id = xmlParser.getAttributeValue(null, "id");
		    				fun.label = xmlParser.getAttributeValue(null, "label");
		    				fun.title = xmlParser.getAttributeValue(null, "title");
		    				fun.description = xmlParser.getAttributeValue(null, "description");
		    				fun.display = xmlParser.getAttributeValue(null, "display");
		    				fun.sort = xmlParser.getAttributeValue(null, "sort");
		    				fun.icon = R.drawable.funs_business;
		    				fun.cover = xmlParser.getAttributeValue(null, "logo");
		    				data.funs.add(fun);
		    			}
		    			else if (tag.equalsIgnoreCase("DINNER")) {
		    				fun = new FunsEntity();
		    				fun.id = xmlParser.getAttributeValue(null, "id");
		    				fun.label = xmlParser.getAttributeValue(null, "label");
		    				fun.title = xmlParser.getAttributeValue(null, "title");
		    				fun.description = xmlParser.getAttributeValue(null, "description");
		    				fun.display = xmlParser.getAttributeValue(null, "display");
		    				fun.sort = xmlParser.getAttributeValue(null, "sort");
		    				fun.icon = R.drawable.funs_dinner;
		    				fun.cover = xmlParser.getAttributeValue(null, "logo");
		    				data.funs.add(fun);
						}
		    			else if (tag.equalsIgnoreCase("OUTSPORT")) {
		    				fun = new FunsEntity();
		    				fun.id = xmlParser.getAttributeValue(null, "id");
		    				fun.label = xmlParser.getAttributeValue(null, "label");
		    				fun.title = xmlParser.getAttributeValue(null, "title");
		    				fun.description = xmlParser.getAttributeValue(null, "description");
		    				fun.display = xmlParser.getAttributeValue(null, "display");
		    				fun.sort = xmlParser.getAttributeValue(null, "sort");
		    				fun.icon = R.drawable.funs_outsport;
		    				fun.cover = xmlParser.getAttributeValue(null, "logo");
		    				data.funs.add(fun);
						}
		    			else if (tag.equalsIgnoreCase("CARD")) {
		    				fun = new FunsEntity();
		    				fun.id = xmlParser.getAttributeValue(null, "id");
		    				fun.label = xmlParser.getAttributeValue(null, "label");
		    				fun.title = xmlParser.getAttributeValue(null, "title");
		    				fun.description = xmlParser.getAttributeValue(null, "description");
		    				fun.display = xmlParser.getAttributeValue(null, "display");
		    				fun.sort = xmlParser.getAttributeValue(null, "sort");
		    				fun.icon = R.drawable.funs_card;
		    				fun.cover = xmlParser.getAttributeValue(null, "logo");
		    				data.funs.add(fun);
		    			}
		    			else if (tag.equalsIgnoreCase("TOPIC")) {
		    				fun = new FunsEntity();
		    				fun.id = xmlParser.getAttributeValue(null, "id");
		    				fun.label = xmlParser.getAttributeValue(null, "label");
		    				fun.title = xmlParser.getAttributeValue(null, "title");
		    				fun.description = xmlParser.getAttributeValue(null, "description");
		    				fun.display = xmlParser.getAttributeValue(null, "display");
		    				fun.sort = xmlParser.getAttributeValue(null, "sort");
		    				fun.icon = R.drawable.funs_topic;
		    				fun.cover = xmlParser.getAttributeValue(null, "logo");
		    				data.funs.add(fun);
		    			}
		    			else if (tag.equalsIgnoreCase("KARAOKE")) {
		    				fun = new FunsEntity();
		    				fun.id = xmlParser.getAttributeValue(null, "id");
		    				fun.label = xmlParser.getAttributeValue(null, "label");
		    				fun.title = xmlParser.getAttributeValue(null, "title");
		    				fun.description = xmlParser.getAttributeValue(null, "description");
		    				fun.display = xmlParser.getAttributeValue(null, "display");
		    				fun.sort = xmlParser.getAttributeValue(null, "sort");
		    				fun.icon = R.drawable.funs_karaoke;
		    				fun.cover = xmlParser.getAttributeValue(null, "logo");
		    				data.funs.add(fun);
		    			}
		    			break;
		    		case XmlPullParser.END_TAG:
		    			break;
				}
				evtType = xmlParser.next(); 
			}
		} catch (Exception e) {
			Logger.i(e);
		}
		return data;
	}
}

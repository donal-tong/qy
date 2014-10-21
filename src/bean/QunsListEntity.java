package bean;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;

import tools.Logger;
import android.content.Context;
import android.content.res.AssetManager;
import android.util.Xml;

import com.vikaa.contactactivityassitant.R;

public class QunsListEntity extends Entity{
	
	public List<QunsEntity> quns = new ArrayList<QunsEntity>();

	public static QunsListEntity parse(Context context) {
		QunsListEntity data = new QunsListEntity();
		XmlPullParser xmlParser = Xml.newPullParser();
		AssetManager assetManager = context.getAssets();
		InputStream is;
		try {
			is = assetManager.open("Quns.xml");
			xmlParser.setInput(is, UTF8);
			int evtType=xmlParser.getEventType();
			QunsEntity qun = null;
			List<String> descriptions = null;
			List<RequireEntity> requires = null;
			while(evtType!=XmlPullParser.END_DOCUMENT){ 
				String tag = xmlParser.getName(); 
				switch(evtType){ 
		    		case XmlPullParser.START_TAG:
		    			if (tag.equalsIgnoreCase("QY")) {
		    				qun = new QunsEntity();
		    				qun.id = xmlParser.getAttributeValue(null, "id");
		    				qun.label = xmlParser.getAttributeValue(null, "label");
		    				qun.title = xmlParser.getAttributeValue(null, "title");
		    				qun.icon = R.drawable.qun_school;
		    				qun.logo = xmlParser.getAttributeValue(null, "logo"); 
		    			}
		    			else if (tag.equalsIgnoreCase("SCHOOL")) {
		    				qun = new QunsEntity();
		    				qun.id = xmlParser.getAttributeValue(null, "id");
		    				qun.label = xmlParser.getAttributeValue(null, "label");
		    				qun.title = xmlParser.getAttributeValue(null, "title");
		    				qun.icon = R.drawable.qun_school;
		    				qun.logo = xmlParser.getAttributeValue(null, "logo"); 
		    			}
		    			else if (tag.equalsIgnoreCase("BUSINESS")) {
		    				qun = new QunsEntity();
		    				qun.id = xmlParser.getAttributeValue(null, "id");
		    				qun.label = xmlParser.getAttributeValue(null, "label");
		    				qun.title = xmlParser.getAttributeValue(null, "title");
		    				qun.icon = R.drawable.qun_business;
		    				qun.logo = xmlParser.getAttributeValue(null, "logo"); 
		    			}
		    			else if (tag.equalsIgnoreCase("ECONOMY")) {
		    				qun = new QunsEntity();
		    				qun.id = xmlParser.getAttributeValue(null, "id");
		    				qun.label = xmlParser.getAttributeValue(null, "label");
		    				qun.title = xmlParser.getAttributeValue(null, "title");
		    				qun.icon = R.drawable.qun_economy;
		    				qun.logo = xmlParser.getAttributeValue(null, "logo"); 
		    			}
		    			else if (tag.equalsIgnoreCase("MEETING")) {
		    				qun = new QunsEntity();
		    				qun.id = xmlParser.getAttributeValue(null, "id");
		    				qun.label = xmlParser.getAttributeValue(null, "label");
		    				qun.title = xmlParser.getAttributeValue(null, "title");
		    				qun.icon = R.drawable.qun_school;
		    				qun.logo = xmlParser.getAttributeValue(null, "logo"); 
		    			}
		    			else if (tag.equalsIgnoreCase("COLLEGUE")) {
		    				qun = new QunsEntity();
		    				qun.id = xmlParser.getAttributeValue(null, "id");
		    				qun.label = xmlParser.getAttributeValue(null, "label");
		    				qun.title = xmlParser.getAttributeValue(null, "title");
		    				qun.icon = R.drawable.qun_collegue;
		    				qun.logo = xmlParser.getAttributeValue(null, "logo"); 
		    			}
		    			else if (tag.equalsIgnoreCase("descriptions")) {
		    				descriptions = new ArrayList<String>();
		    				qun.descriptions = descriptions;
		    			}
		    			else if (tag.equalsIgnoreCase("description")) {
		    				String description = xmlParser.getAttributeValue(null, "label");
		    				descriptions.add(description);
		    			}
		    			else if (tag.equalsIgnoreCase("requires")) {
		    				requires = new ArrayList<RequireEntity>();
		    				qun.requires = requires;
		    			}
		    			else if (tag.equalsIgnoreCase("require")) {
		    				RequireEntity requireEntity = new RequireEntity();
		    				requireEntity.label = xmlParser.getAttributeValue(null, "label");
		    				requireEntity.field = xmlParser.getAttributeValue(null, "field");
		    				requires.add(requireEntity);
		    			}
		    			break;
		    		case XmlPullParser.END_TAG:
		    			if (tag.equalsIgnoreCase("QY") || tag.equalsIgnoreCase("MEETING") || tag.equalsIgnoreCase("SCHOOL") || tag.equalsIgnoreCase("BUSINESS") || tag.equalsIgnoreCase("ECONOMY") || tag.equalsIgnoreCase("COLLEGUE")) {
		    				data.quns.add(qun);
		    			}
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

package contact;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import bean.Entity;

import tools.AppException;
import tools.Logger;

public class MobileSynListBean extends Entity{
	public List<MobileSynBean> data = new ArrayList<MobileSynBean>();
	
	public static MobileSynListBean parse(String res) throws AppException {
		MobileSynListBean list = new MobileSynListBean();
		try {
			JSONObject js = new JSONObject(res);
			if(js.getInt("status") == 1) {
				JSONObject info = js.getJSONObject("info");
				JSONArray d = info.getJSONArray("d");
				for (int i = 0; i < d.length(); i++) {
					JSONObject p = d.getJSONObject(i);
					MobileSynBean m = new MobileSynBean();
					m.prefix = p.getString("prefix");
					m.suffix = p.getString("suffix");
					m.firstname = p.getString("firstname");
					m.middlename = p.getString("middlename");
					m.lastname = p.getString("lastname");
					if (!p.isNull("organization")) {
						m.organization = p.getString("organization");
					}
					if (!p.isNull("department")) {
						m.department = p.getString("department");
					}
					if (!p.isNull("jobtitle")) {
						m.jobtitle = p.getString("jobtitle");
					}
					JSONArray phoneArr = p.getJSONArray("phone");
					for (int j = 0; j < phoneArr.length(); j++) {
						if (phoneArr.get(j) instanceof JSONObject) {
							JSONObject phoneObj = phoneArr.getJSONObject(j);
							PhoneBean phoneBean = new PhoneBean();
							phoneBean.label = phoneObj.getString("label");
							phoneBean.phone = phoneObj.getString("phone");
							m.phone.add(phoneBean);
						}
					}
					JSONArray email = p.getJSONArray("email");
					for (int j = 0; j < email.length(); j++) {
						if (email.get(j) instanceof JSONObject) {
							JSONObject emailObj = email.getJSONObject(j);
							EmailBean emailBean = new EmailBean();
							emailBean.label = emailObj.getString("label");
							emailBean.email = emailObj.getString("email");
							m.email.add(emailBean);
						}
					}
					
					JSONArray dates = p.getJSONArray("dates");
					for (int j = 0; j < dates.length(); j++) {
						if (dates.get(j) instanceof JSONObject) {
							JSONObject dateObj = dates.getJSONObject(j);
							DateBean emailBean = new DateBean();
							emailBean.date = dateObj.getString("date");
							m.dates.add(emailBean);
						}
					}
					
					JSONArray address = p.getJSONArray("address");
					for (int j = 0; j < address.length(); j++) {
						if (address.get(j) instanceof JSONObject) {
							JSONObject dateObj = address.getJSONObject(j);
							AddressBean addressBean = new AddressBean();
							addressBean.address = dateObj.getString("address");
							m.address.add(addressBean);
						}
					}
					
					JSONArray url = p.getJSONArray("url");
					for (int j = 0; j < url.length(); j++) {
						if (url.get(j) instanceof JSONObject) {
							JSONObject dateObj = url.getJSONObject(j);
							UrlBean urlBean = new UrlBean();
							urlBean.url = dateObj.getString("url");
							m.url.add(urlBean);
						}
					}
					
					JSONArray im = p.getJSONArray("im");
					for (int j = 0; j < im.length(); j++) {
						if (im.get(j) instanceof JSONObject) {
							JSONObject dateObj = im.getJSONObject(j);
							IMBean imBean = new IMBean();
							imBean.im = dateObj.getString("im");
							imBean.username = dateObj.getString("username");
							m.im.add(imBean);
						}
					}
					
					list.data.add(m);
				}
			}
			else {
				
			}
		} catch (JSONException e) {
			Logger.i(e);
			
			throw AppException.json(e);
		}
		return list;
	}
}

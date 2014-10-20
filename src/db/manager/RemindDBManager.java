package db.manager;

import im.bean.IMMessage;
import im.bean.IMMessage.JSBubbleMessageStatus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


import bean.RemindEntity;
import tools.Logger;
import tools.StringUtils;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import config.MyApplication;
import db.DBManager;
import db.SQLiteTemplate;
import db.SQLiteTemplate.RowMapper;

public class RemindDBManager {

	private static RemindDBManager remindManager = null;
	private static DBManager manager = null;

	private RemindDBManager(Context context) {
		manager = DBManager.getInstance(context, MyApplication.getInstance().getLoginUid());
	}

	public static RemindDBManager getInstance(Context context) {
		if (remindManager == null) {
			remindManager = new RemindDBManager(context);
		}
		return remindManager;
	}
	
	public static void destroy() {
		remindManager = null;
		manager = null;
	}
	
	public long saveRemind(RemindEntity data) {
		SQLiteTemplate st = SQLiteTemplate.getInstance(manager, false);
		ContentValues contentValues = new ContentValues();
		contentValues.put("remind_name",  StringUtils.doEmpty(data.remindName));
		contentValues.put("year", data.year);
		contentValues.put("month", data.month);
		contentValues.put("day", data.day);
		contentValues.put("hour", data.hour);
		contentValues.put("minute", data.minute);
		contentValues.put("repeat", data.repeat);
		contentValues.put("hide", data.hide);
		contentValues.put("remind_descripte", data.remindDescripte);
		return st.insert("remind", contentValues);
	}
	
	public List<RemindEntity> getReminds(String id) {
		String sql;
		String[] args; 
		if (id.equals("0")) {
			sql = "select * from remind limit ? ";
			args = new String[] { "10" };
		}
		else {
			sql = "select * from remind where _id<? limit ? ";
			args = new String[] { id, "" + 10 };
		}
		SQLiteTemplate st = SQLiteTemplate.getInstance(manager, false);
		List<RemindEntity> list = st.queryForList(
				new RowMapper<RemindEntity>() {
					@Override
					public RemindEntity mapRow(Cursor cursor, int index) {
						RemindEntity remind = new RemindEntity();
						remind.setId(cursor.getInt(cursor.getColumnIndex("_id")));
						remind.remindName = cursor.getString(cursor.getColumnIndex("remind_name"));
						remind.year = cursor.getInt(cursor.getColumnIndex("year"));
						remind.month = cursor.getInt(cursor.getColumnIndex("month"));
						remind.day = cursor.getInt(cursor.getColumnIndex("day"));
						remind.hour = cursor.getInt(cursor.getColumnIndex("hour"));
						remind.minute = cursor.getInt(cursor.getColumnIndex("minute"));
						remind.repeat = cursor.getString(cursor.getColumnIndex("repeat"));
						remind.hide = cursor.getInt(cursor.getColumnIndex("hide"));
						remind.remindDescripte = cursor.getString(cursor.getColumnIndex("remind_descripte"));
						return remind;
					}
				},
				sql,
				args);
		return list;
	}
}

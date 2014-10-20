CREATE TABLE IF NOT EXISTS [remind] (
	[_id] INTEGER NOT NULL  PRIMARY KEY AUTOINCREMENT, 
	[remind_name] NVARCHAR,
	[year] INTEGER, 
	[month] INTEGER, 
	[day] INTEGER, 
	[hour] INTEGER, 
	[minute] INTEGER, 
	[repeat] NVARCHAR, 
	[hide] INTEGER, 
	[remind_descripte] TEXT
);
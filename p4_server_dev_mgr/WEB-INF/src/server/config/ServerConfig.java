package server.config;


import java.io.ByteArrayInputStream;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import server.common.Tools;
import server.database.DataBase;

import com.moonic.bac.ConfigBAC;
import com.moonic.bac.ScheduleBAC;
import com.moonic.bac.ServerBAC;
import com.moonic.mgr.DBPoolMgr;
import com.moonic.mgr.PookNet;
import com.moonic.socket.UploadServer;
import com.moonic.util.DBHelper;
import com.moonic.util.DBPool;
import com.moonic.util.DBPoolClearListener;
import com.moonic.util.DynamicGroovy;
import com.moonic.util.MyTools;
import com.moonic.util.Out;
import com.moonic.util.ProcessQueue;

import conf.Conf;
import conf.LogTbName;

/**
 * ��������
 * @author 
 */
public class ServerConfig implements ServletContextListener {
	private static DataBase database; //����
	private static DataBase database_backup; //���ݿ�
	private static DataBase database_log; //��־��
	private static DataBase database_report; //�����
	
	public static String dl_apk_url;
	public static String dl_res_url;	 
	 
	private static String appRoot;
	
	
	/**
	 * �����ص�
	 */
	public void contextInitialized(ServletContextEvent context) {
		appRoot = context.getServletContext().getRealPath("/");
		
		ServerConfig.init();    
    }   
	
	/**
	 * �˳��ص�
	 */
	public void contextDestroyed(ServletContextEvent arg0) {
		ServerConfig.exit();
	}
	
	/**
	 * ��ʼ��
	 */
	public static void init() {
		try {			
			DataBase.setAppRootPath(getAppRootPath());
						
			Conf.stsKey = "P4��̨�����";
			
			DynamicGroovy.setClassRootPath(getWebInfPath()+"classes/");
			
			System.setProperty("sun.net.client.defaultConnectTimeout", String.valueOf(10000));// ����λ�����룩  
            System.setProperty("sun.net.client.defaultReadTimeout", String.valueOf(10000)); // ����λ�����룩
			
            readConfigFromXML();
			
			initDB();
			initBackupDB();
			initLogDB();
			initReportDB();
			initDownload();
			Thread.sleep(10);
			
			readConfigFromDB();
			
			DataBase.setLogFolder(Conf.logRoot);
			
			ServerBAC.initTimer();
			
			ScheduleBAC.getInstance().loadTaskFromDB();//�����ƻ�����
			
			DBPoolMgr.getInstance().initClearPoolListener();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	static {
		DBPool.getInst().addTabClearListener(new DBPoolClearListener() {
			public void callback(String key) {
				if(key.equals(ConfigBAC.tb_config)){
					setDBConfig();
				}
			}
		});
	}
	
	private static void readConfigFromXML() {
		initConf();
		PookNet.initPokerReq();
	}
	
	private static void readConfigFromDB() {
		setDBConfig();
	}
	
	private static void setDBConfig(){
		DataBase.setLogOutAllSql(ConfigBAC.getBoolean("logout_all_sql"));
		DataBase.setLogOutLongTimeSql(ConfigBAC.getBoolean("logout_longtime_sql"));
		DataBase.setLongTimeSqlThreshold(ConfigBAC.getInt("logout_longtime_sql_threshold"));
		DataBase.setLogOutAllDbConn(ConfigBAC.getBoolean("logout_all_db_conn"));
		DataBase.setLogOutLongTimeDbConn(ConfigBAC.getBoolean("logout_longtime_db_conn"));
		DataBase.setLongTimeDbThreshold(ConfigBAC.getInt("logout_longtime_db_threshold"));
	}
	
	/**
	 * ��ʼ�����ݿ�
	 */
	public static void initDB() {
		database = new DataBase();
		
		DBHelper.setDefaultDataBase(database);
		Document document;
		try {		
			byte[] fileBytes = Tools.getBytesFromFile(getWebInfPath() + "conf/db.xm");
			fileBytes = Tools.decodeBin(fileBytes);
			ByteArrayInputStream bais = new ByteArrayInputStream(fileBytes);			
			SAXReader saxReader = new SAXReader();			
			//document = saxReader.read(dbXmlPath + "conf/db.xml");
			document = saxReader.read(bais);
			
			Element db_conf = document.getRootElement();
			Element db_info = db_conf.element("db1");
			String driver = db_info.element("driver").getText();
			String dbName = db_info.element("dbname").getText();
			database.init(
				driver, 
				dbName, 
				db_info.element("username").getText(), 
				db_info.element("password").getText(), 
				Integer.parseInt((db_info.element("maxatv")).getText()), 
				Integer.parseInt(db_info.element("maxidl").getText()), 
				Integer.parseInt(db_info.element("minidl").getText())
			);
			
			Out.println(dbName+"���ݿ��ʼ����� ");
		} catch (DocumentException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	/**
	 * ��ʼ���������ݿ�
	 */
	public static void initBackupDB() {
		database_backup = new DataBase();
		
		Document document;
		try {
			byte[] fileBytes = Tools.getBytesFromFile(getWebInfPath() + "conf/db.xm");
			fileBytes = Tools.decodeBin(fileBytes);
			ByteArrayInputStream bais = new ByteArrayInputStream(fileBytes);			
			SAXReader saxReader = new SAXReader();			
			//document = saxReader.read(dbXmlPath + "conf/db.xml");
			document = saxReader.read(bais);
			Element db_conf = document.getRootElement();
			Element db_info = db_conf.element("db2");
			String driver = db_info.element("driver").getText();
			String dbName = db_info.element("dbname").getText();
			database_backup.init(
					driver, 
					dbName, 
				db_info.element("username").getText(), 
				db_info.element("password").getText(), 
				Integer.parseInt((db_info.element("maxatv")).getText()), 
				Integer.parseInt(db_info.element("maxidl").getText()), 
				Integer.parseInt(db_info.element("minidl").getText())
			);
			Out.println(dbName+"�������ݿ��ʼ����� ");
		} catch (DocumentException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * ��ʼ����־���ݿ�
	 */
	public static void initLogDB() {
		database_log = new DataBase();
		
		Document document;
		try {
			byte[] fileBytes = Tools.getBytesFromFile(getWebInfPath() + "conf/db.xm");
			fileBytes = Tools.decodeBin(fileBytes);
			ByteArrayInputStream bais = new ByteArrayInputStream(fileBytes);			
			SAXReader saxReader = new SAXReader();			
			//document = saxReader.read(dbXmlPath + "conf/db.xml");
			document = saxReader.read(bais);
			Element db_conf = document.getRootElement();
			Element db_info = db_conf.element("dblog");
			String driver = db_info.element("driver").getText();
			String dbName = db_info.element("dbname").getText();
			database_log.init(
					driver, 
					dbName, 
				db_info.element("username").getText(), 
				db_info.element("password").getText(), 
				Integer.parseInt((db_info.element("maxatv")).getText()), 
				Integer.parseInt(db_info.element("maxidl").getText()), 
				Integer.parseInt(db_info.element("minidl").getText())
			);
			Element logusername = db_info.element("username");
			if (logusername != null) {
				LogTbName.setUsername(logusername.getText());
			}
			Out.println(dbName+"��־���ݿ��ʼ����� ");
		} catch (DocumentException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * ��ʼ����������
	 */
	public static void initDownload(){
		Document document;
		try {
			SAXReader saxReader = new SAXReader();
			document = saxReader.read(getWebInfPath() + "conf/download.xml");
			Element root = document.getRootElement();
			dl_apk_url = root.element("apk").getText();
			dl_res_url = root.element("res").getText();
			Conf.res_url = root.element("resurl").getText();
			
			Out.println("download apk url="+dl_apk_url);
			Out.println("download res url="+dl_res_url);			
			Out.println("�������ó�ʼ�����");
		} catch (Exception e){
			e.printStackTrace();
		}
	}

	public static void initReportDB() {
		database_report = new DataBase();
		
		Document document;
		try {
			byte[] fileBytes = Tools.getBytesFromFile(getWebInfPath() + "conf/db.xm");
			fileBytes = Tools.decodeBin(fileBytes);
			ByteArrayInputStream bais = new ByteArrayInputStream(fileBytes);			
			SAXReader saxReader = new SAXReader();			
			//document = saxReader.read(dbXmlPath + "conf/db.xml");
			document = saxReader.read(bais);
			Element db_conf = document.getRootElement();
			Element db_info = db_conf.element("dbreport");
			String driver = db_info.element("driver").getText();
			String dbName = db_info.element("dbname").getText();
			database_report.init( 
					driver, 
					dbName, 
				db_info.element("username").getText(), 
				db_info.element("password").getText(), 
				Integer.parseInt((db_info.element("maxatv")).getText()), 
				Integer.parseInt(db_info.element("maxidl").getText()), 
				Integer.parseInt(db_info.element("minidl").getText())
			);
			Out.println(dbName+"�������ݿ��ʼ����� ");
		} catch (DocumentException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * ��ʼ������
	 */
	public static void initConf(){
		Document document;
		try {
			SAXReader saxReader = new SAXReader();
			document = saxReader.read(getWebInfPath() + "conf/conf.xml");
			Element root = document.getRootElement();
			Conf.debug = Tools.str2boolean(root.element("debug").getText());
			Conf.out_sql = Tools.str2boolean(root.element("outSql").getText());
			Conf.userUploadServer = Tools.str2boolean(root.element("userUploadServer").getText());
			Conf.uploadServerPort = Tools.str2int(root.element("uploadServerPort").getText());
			if(Conf.userUploadServer){
				UploadServer.getInstance().start();
			}
			Conf.logRoot = ServerConfig.getAppRootPath()+"logs/";
			Out.println("���������ó�ʼ�����");
		} catch (Exception e){
			e.printStackTrace();
		}
	}
	
	
	/**
	 * �˳�
	 */
	public static void exit() {
		UploadServer.getInstance().stop();//ֹͣ�ϴ�����
		getDataBase().close();
		getDataBase_Backup().close();
		getDataBase_Log().close();
		getDataBase_Report().close();
		ProcessQueue.setRunState(false);
		MyTools.closeAllTimer();
	}
	
	public static String getAppRootPath(){
		return appRoot;
    }
	
	public static String getWebInfPath() {
		return appRoot + "WEB-INF/";
	}  
    
	public static String getPermissionXmlPath(){
		return appRoot + "WEB-INF/conf/permission.xml";
    }
	
	/**
	 * ��ȡ���ݿ����
	 */
	public static DataBase getDataBase() {
		return database;
	}
	
	/**
	 * ��ȡ�������ݿ����
	 */
	public static DataBase getDataBase_Backup() {
		return database_backup;
	}
	
	/**
	 * ��ȡ��־���ݿ����
	 */
	public static DataBase getDataBase_Log() {
		return database_log;
	}
	
	/**
	 * ��ȡ�������ݿ����
	 */
	public static DataBase getDataBase_Report() {
		return database_report;
	}
}

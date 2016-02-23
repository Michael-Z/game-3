package server.config;


import java.io.ByteArrayInputStream;
import java.io.File;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import server.common.Tools;
import server.database.DataBase;

import com.moonic.bac.ConfigBAC;
import com.moonic.bac.ServerBAC;
import com.moonic.memcache.MemcachedUtil;
import com.moonic.mgr.PookNet;
import com.moonic.timertask.DBIdleAdjustTT;
import com.moonic.util.ConfFile;
import com.moonic.util.DBHelper;
import com.moonic.util.DBPool;
import com.moonic.util.DBPoolClearListener;
import com.moonic.util.DynamicGroovy;
import com.moonic.util.MyLog;
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
			
			Conf.stsKey = "P4�û���֤��";
			
			DynamicGroovy.setClassRootPath(getWebInfPath()+"classes/");
			
			System.setProperty("sun.net.client.defaultConnectTimeout", String.valueOf(10000));// ����λ�����룩  
            System.setProperty("sun.net.client.defaultReadTimeout", String.valueOf(10000)); // ����λ�����룩
			
            readConfigFromXML();
			
			DataBase.setLogFolder(Conf.logRoot);
			
			initDB();
			initBackupDB();
			initLogDB();
			
			Thread.sleep(10);
			
			readConfigFromDB();
			
			ServerBAC.initTimer(0);
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
		initDownload();
		PookNet.initPokerReq();
		initMServer();
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
			document = saxReader.read(bais);
			
			Element db_conf = document.getRootElement();
			Element db_info = db_conf.element("db1");
			String driver = db_info.element("driver").getText();
			String dbName = db_info.element("dbname").getText();
			int minIdle = Tools.str2int(ConfFile.getFileValueInStartServer(DBIdleAdjustTT.MIN_IDLE, "-1"));
			int maxIdle = 0;
			int maxActi = 0;
			if(minIdle != -1){
				maxIdle = minIdle * 10;
				maxActi = maxIdle + 50;
			} else {
				minIdle = Integer.parseInt((db_info.element("minidl")).getText());
				maxIdle = Integer.parseInt((db_info.element("maxidl")).getText());
				maxActi = Integer.parseInt((db_info.element("maxatv")).getText());
			}
			database.init(
				driver, 
				dbName, 
				db_info.element("username").getText(), 
				db_info.element("password").getText(), 
				maxActi, 
				maxIdle, 
				minIdle
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
	
	/**
	 * ��ʼ����֤������
	 */
	public static void initMServer(){
		Document document;
		try {
			SAXReader saxReader = new SAXReader();
			document = saxReader.read(getWebInfPath() + "conf/mserver.xml");
			Element root = document.getRootElement();
			Conf.ms_url = root.element("url").getText();
			Conf.web_dir = root.element("webdir").getText();
			Conf.out_sql = Tools.str2boolean(root.element("outSql").getText());
			Conf.gdout = Tools.str2int(root.element("gdout").getText())==1;
			Conf.logRoot = ServerConfig.getAppRootPath()+"logs/";
			try {
				String dir = root.elementText("logroot");
				File file = new File(dir);
				if(!file.exists()){
					file.mkdirs();
				}
				if(file.isDirectory()){
					Conf.logRoot = dir;//��Ҫ��"/"
				}
			} catch (Exception e) {
			}
			if(root.element("testredir")!=null)
			{
				Conf.testRedir = root.element("testredir").getText();
			}
			Out.println("��֤���������ó�ʼ�����");
		} catch (Exception e){
			e.printStackTrace();
		}
	}
	
	/**
	 * �˳�
	 */
	public static void exit() {
		getDataBase().close();
		getDataBase_Log().close();
		ProcessQueue.setRunState(false);
		MyTools.closeAllTimer();
		MemcachedUtil.stop();
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
}

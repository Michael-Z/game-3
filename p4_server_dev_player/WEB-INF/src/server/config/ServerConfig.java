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
import com.moonic.bac.PlayerBAC;
import com.moonic.bac.ServerBAC;
import com.moonic.memcache.MemcachedUtil;
import com.moonic.mgr.PookNet;
import com.moonic.mirror.MirrorMgr;
import com.moonic.socket.SocketServer;
import com.moonic.timertask.DBIdleAdjustTT;
import com.moonic.util.ConfFile;
import com.moonic.util.DBHelper;
import com.moonic.util.DBPaRs;
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
	private static DataBase database; //����(���û�)
	private static DataBase database_backup; //���ݿ�
	private static DataBase database_log; //��־��(������־�⣬��־�û������ڴ洢��־����־�û�û�ж����û��ķ���Ȩ�ޣ����������ѯ��־ʱ��Ҫ����־������û���ѯ)
	
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
			
			DynamicGroovy.setClassRootPath(getWebInfPath()+"classes/");
			
			MirrorMgr.init();
			
			readConfigFromXML();
			
			initDB();
			initBackupDB();
			initLogDB();
			
			readConfigFromDB();
			Thread.sleep(10);
			
			DataBase.setLogFolder(Conf.logRoot);
			
			ServerBAC.initTimer();
			SocketServer.getInstance().start();
			PlayerBAC.getInstance().restoreOnLinePla();
			
			MyLog serverStartLog = new MyLog(MyLog.NAME_CUSTOM, "serverstart", "SERVERSTART", true, false, false, "serverstart");
			serverStartLog.d(MyTools.getTimeStr()+"-����������");
			serverStartLog.save();
		} catch (Exception e) {
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
		initGServerConf();
		initMServerConf();
		initGameConf();
		PookNet.initPokerReq();
	}
	
	private static void readConfigFromDB() {
		initGServer();
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
			System.out.println("��Ϸ��"+Conf.sid+"�����ݿ�"+dbName+"��ʼ����� minIdle="+minIdle);
		} catch (DocumentException e) {
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
			System.out.println(dbName+"��־���ݿ��ʼ����� ");
		} catch (DocumentException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * ��ʼ����֤������
	 */
	public static void initMServerConf(){
		Document document;
		try {
			SAXReader saxReader = new SAXReader();
			document = saxReader.read(getWebInfPath() + "conf/mserver.xml");
			Element root = document.getRootElement();
			Conf.ms_url = root.element("url").getText();
			Conf.web_dir = root.element("webdir").getText();
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
			Out.println("��֤���������ó�ʼ�����");
		} catch (Exception e){
			e.printStackTrace();
		}
	}
	
	/**
	 * ��ʼ����Ϸ����������
	 */
	public static void initGServerConf(){
		try {
			SAXReader saxReader = new SAXReader();			
			Document document = saxReader.read(getWebInfPath() + "conf/server.xml");
			Element server_conf = document.getRootElement();
			Conf.sid = Integer.parseInt(server_conf.element("id").getText());
			try {
				Conf.out_sql = Tools.str2boolean(server_conf.element("outSql").getText());		
			} catch (Exception e) {
				e.printStackTrace();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	static {
		DBPool.getInst().addTabClearListener(new DBPoolClearListener() {
			public void callback(String key) {
				if(key.equals(ServerBAC.tab_server)){
					initGServer();
				}
			}
		});
	}
	
	/**
	 * ��ʼ����Ϸ������
	 */
	public static void initGServer(){
		try {
			DBPaRs serverRs = DBPool.getInst().pQueryA(ServerBAC.tab_server, "id="+Conf.sid);
			String httpdata = serverRs.getString("http");
			Conf.http_url = httpdata;
			String[] socketdata = Tools.splitStr(serverRs.getString("tcp"), ":");
			Conf.socket_url = socketdata[0];
			Conf.socket_port = Tools.str2int(socketdata[1]);
			Conf.max_player = serverRs.getInt("maxplayer");
			Conf.stsKey = serverRs.getString("name")+"(" + Conf.sid + ")";
			Out.println("��Ϸ���������ó�ʼ�����");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * ��ʼ����Ϸ����
	 */
	public static void initGameConf(){
		Document document;
		try {
			SAXReader saxReader = new SAXReader();
			document = saxReader.read(getWebInfPath() + "conf/gameconf.xml");
			Element root = document.getRootElement();
			Conf.gdout = Tools.str2int(root.element("gdout").getText())==1;
			Conf.joinfacspacetime = Tools.str2int(root.element("joinfacspacetime").getText());
			Conf.debug = Tools.str2boolean(root.element("debug").getText());
			Conf.res_url = root.element("resurl").getText();
			Conf.useClearReplayTT = Tools.str2boolean(root.element("clearreplaytt").getText());
			Conf.initvip = Tools.str2int(root.element("initvip").getText());
			Out.println("��Ϸ���ó�ʼ�����");
		} catch (Exception e){
			e.printStackTrace();
		}
	}
	
	/**
	 * �˳�
	 */
	public static void exit() {
		SocketServer.getInstance().stop();
		getDataBase().close();
		getDataBase_Backup().close();
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

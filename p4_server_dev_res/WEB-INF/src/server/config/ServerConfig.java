package server.config;


import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import server.database.DataBase;

import com.moonic.util.Out;
import com.moonic.util.ProcessQueue;

import conf.Conf;

/**
 * ��������
 * @author 
 */
public class ServerConfig implements ServletContextListener { 
	 
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
			
			Conf.stsKey = "P4��Դ������";
			
			System.setProperty("sun.net.client.defaultConnectTimeout", String.valueOf(10000));// ����λ�����룩  
            System.setProperty("sun.net.client.defaultReadTimeout", String.valueOf(10000)); // ����λ�����룩
			
            initConf();
            
			Thread.sleep(10);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * ��ʼ����֤������
	 */
	public static void initConf(){
		Document document;
		try {
			SAXReader saxReader = new SAXReader();
			document = saxReader.read(getWebInfPath() + "conf/conf.xml");
			Element root = document.getRootElement();
			Conf.savepath = root.element("savepath").getText();
			Out.println("���������ó�ʼ�����");
		} catch (Exception e){
			e.printStackTrace();
		}
	}
	
	/**
	 * �˳�
	 */
	public static void exit() {
		ProcessQueue.setRunState(false);
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
}

package com.moonic.mgr;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import server.config.ServerConfig;

import com.moonic.util.Out;

public class PookNet {
	/**
	 * ����ע��
	 */
	public static String login_do;
	/**
	 * ���˵�¼
	 */
	public static String register_do;
	/**
	 * �ֻ��һ�����
	 */
	public static String mobilefindpwd_do;
	/**
	 * �����һ�����
	 */
	public static String emailfindpwd_do;
	/**
	 * ��ȡ��ȫ��Ϣ
	 */
	public static String getsafety_do;
	
	/**
	 * ��ֵ���Ľӿ�
	 */
	public static String chargecenter_do;
	
	/**
	 * ��ȡ���˿����
	 */
	public static String getcardvalue_do;
	
	public static final String screctKey = "REWREWdsjksu32uksjf35468"; //���˵�¼��Կ
	
	/**
	 * ��ʼ�����������ַ����
	 */
	public static void initPokerReq(){
		Document document;
		try {
			SAXReader saxReader = new SAXReader();
			document = saxReader.read(ServerConfig.getWebInfPath() + "conf/poker_req.xml");
			Element root = document.getRootElement();
			login_do = root.element("login").getText();
			register_do = root.element("register").getText();
			mobilefindpwd_do = root.element("mobilefindpwd").getText();
			emailfindpwd_do = root.element("emailfindpwd").getText();
			getsafety_do = root.element("getsafety").getText();
			chargecenter_do = root.element("chargecenter").getText();
			getcardvalue_do = root.element("getcardvalue").getText();
			Out.println("���������ַ���ó�ʼ�����");
		} catch (Exception e){
			e.printStackTrace();
		}
	}
}
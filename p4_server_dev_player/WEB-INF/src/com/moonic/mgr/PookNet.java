package com.moonic.mgr;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import server.config.ServerConfig;

import com.moonic.util.Out;

public class PookNet {
	/**
	 * �����֤
	 */
	public static String bindcard_do;
	/**
	 * ��ȡ���ֻ����ֻ���֤��
	 */
	public static String getmobilevalidnum_do;
	/**
	 * ���ֻ�
	 */
	public static String bindmobile_do;
	/**
	 * ������
	 */
	public static String bindemail_do;
	/**
	 * �޸�����
	 */
	public static String modifypwd_do;
	/**
	 * ��ȡ��ȫ��Ϣ
	 */
	public static String getsafety_do;
	/**
	 * ��ⷢ����
	 */
	public static String cbtmsg_do;
	/**
	 * ���˻
	 */
	public static String pookacti_do;
	
	public static final String screctKey = "REWREWdsjksu32uksjf35468"; //���˵�¼��Կ
	
	public static String gotyeURL; //�׼���������˽ӿڵ�ַ
	
	/**
	 * ��ʼ�����������ַ����
	 */
	public static void initPokerReq(){
		Document document;
		try {
			SAXReader saxReader = new SAXReader();
			document = saxReader.read(ServerConfig.getWebInfPath() + "conf/poker_req.xml");
			Element root = document.getRootElement();
			bindcard_do = root.element("bindcard").getText();
			getmobilevalidnum_do = root.element("getmobilevalidnum").getText();
			bindmobile_do = root.element("bindmobile").getText();
			bindemail_do = root.element("bindemail").getText();
			modifypwd_do = root.element("modifypwd").getText();
			getsafety_do = root.element("getsafety").getText();
			cbtmsg_do = root.element("cbtmsg").getText();
			pookacti_do = root.element("pookacti").getText();
			Out.println("���������ַ���ó�ʼ�����");
			gotyeURL = root.element("gotye").getText();
			Out.println("�׼����������ַ���ó�ʼ�����");
		} catch (Exception e){
			e.printStackTrace();
		}
	}
}


package com.ehc.system;

import java.io.FileInputStream;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.ehc.common.ArrBuffer;

/**
 * Ȩ�޹������
 *
 */
public class Permission
{
	
	/**
	 * Ȩ�޼���
	 */
	private static Vector permVC;
	
	/**
     * ��permission.xml�ļ���ȡ�����е�Ȩ��
     * @param fileName String: Ȩ�������ļ���
     */
	public static void initPermission(String fileName)
	{
		try{
			
			FileInputStream fis = new FileInputStream(fileName);
			
			DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document doc = builder.parse(fis);
			
			doc.normalize();
			NodeList perList = doc.getElementsByTagName("permissionList");
			
			//myPermission = new MyHashMap();
			permVC = new Vector();

			for (int i=0; i<perList.getLength(); i++){
				Element link = (Element) perList.item(i); //link����<permissionList>				
				NodeList groups = link.getElementsByTagName("module");
				for(int j=0; j<groups.getLength(); j++){  //����ÿ��module
					String groupName = ((Element)groups.item(j)).getAttribute("name");

					NodeList powers = ((Element)groups.item(j)).getElementsByTagName("permission");
					for(int k=0;k<powers.getLength();k++)
					{
						String permissionName=powers.item(k).getFirstChild().getNodeValue();
						permVC.add(new Perm(groupName,permissionName));
					}					
				}
			}			
		}
		catch(Exception ex){
			ex.printStackTrace(System.out);
		}
	
	}
	
	/**
     * ��ȡָ����Ȩ��ģ������Ȩ��
     * @param moduleName String:Ȩ��ģ������
     * @return String[]: ����ָ��ģ���Ȩ��������
     */
	public static String[] getPermissionsOfModule(String moduleName)
	{
		ArrBuffer arrBuff = new ArrBuffer();
		for(int i=0;permVC!=null && i<permVC.size();i++)
		{
			Perm perm=(Perm)permVC.elementAt(i);
			if(perm.module.equals(moduleName))
			{
				arrBuff.add(perm.permission);
			}
		}
		if(arrBuff.size()>0)
		{
			return arrBuff.getStrArr();
		}
		else
		{
			return null;
		}		
	}
	
	/**
     * ��ȡ���е�ģ����������
     * @return String[]:��������ģ����������
     */
	public static String[] getAllModule()
	{
		ArrBuffer arrBuff = new ArrBuffer();
		
		for(int i=0;permVC!=null && i<permVC.size();i++)
		{
			Perm perm=(Perm)permVC.elementAt(i);
			
			//����Ƿ��Ѱ�����module
			if(!arrBuff.contains(perm.module))
			{
				arrBuff.add(perm.module);	
			}
		}
		if(arrBuff.size()>0)
		{
			return arrBuff.getStrArr();
		}
		else
		{
			return null;
		}	
	}
	
	/**
	 * ���Ȩ���Ƿ����
	 * @param module String:ģ����
	 * @param permName String:Ȩ����
	 * @return boolean:���ؽ��
	 */
	public static boolean isExistPermission(String module,String permName)
	{		
		for(int i=0;permVC!=null && i<permVC.size();i++)
		{
			Perm perm=(Perm)permVC.elementAt(i);
			if(perm.module.equals(module) && perm.permission.equals(permName))
			{
				return true;
			}
		}
		return false;
	}
}
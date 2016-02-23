package com.moonic.chargecenter;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONObject;

import com.ehc.common.ReturnValue;
import com.ehc.common.SqlString;
import com.moonic.bac.ChargeOrderBAC;
import com.moonic.util.DBHelper;
import com.moonic.util.MD5;

import server.common.Tools;
import server.config.LogBAC;
import server.config.ServerConfig;
import util.IPAddressUtil;

/**
 * ��ֵ���Ľӿ�
 * @author alexhy
 *
 */
public class OrderCenterCallback extends HttpServlet
{
	//private String channel="001";
	//private String channelName="����";	
	protected void service(HttpServletRequest request, HttpServletResponse response)throws ServletException, IOException 
	{		
		String ip = IPAddressUtil.getIp(request);
		LogBAC.logout("chargecenter", "------------------------------------------------");
		LogBAC.logout("chargecenter", "�յ�����"+ip+"�Ķ������");
		Enumeration keysEnum = request.getParameterNames();
		while (keysEnum.hasMoreElements())
		{
			String key = (String) keysEnum.nextElement();
			LogBAC.logout("chargecenter", key + "=" + request.getParameter(key));
		}
		/*System.out.println("�յ�����"+ip+"�Ķ������---------------");		
		InputStream is = request.getInputStream();
		byte[] bytes = Tools.getBytesFromInputstream(is);
		is.close();
		String str = new String(bytes,"UTF-8");
		System.out.println("�������="+str);*/
		
		
		//sign=cebc0f71afdc2aa7625a98073cc89056&message=%E5%85%85%E5%80%BC%E6%88%90%E5%8A%9F&appendAmount=10&code=0000&platformOrder=1378705983589&orderId=D201309091353044753
		String code = request.getParameter("code");  //����״̬
		String orderId = request.getParameter("orderId"); //���Ķ�����
		String platformOrder= request.getParameter("platformOrder"); //ƽ̨������
		String orderAmount = request.getParameter("orderAmount"); //�������
		String message = request.getParameter("message"); //�ɹ�/ʧ����ʾ
		String aiId = request.getParameter("aiId"); //�տ��˺�ID����Ҫ��¼�����ݿ�
		String sign = request.getParameter("sign"); //ǩ��
		
		if(code==null)
		{
			OutputStream os = response.getOutputStream();
			DataOutputStream dos = new DataOutputStream(os);
			dos.write("ȱ����Ч����".getBytes("UTF-8"));
			dos.close();
			return;
		}
		
		if(platformOrder!=null)platformOrder = platformOrder.trim();
		
		String key = "d3ceb5881a0a1fdaad01296d7554868e";
		StringBuffer ticket = new StringBuffer();
		ticket.append(code);
		ticket.append(orderId);
		ticket.append(platformOrder);
		ticket.append(orderAmount);
		ticket.append(message);
		ticket.append(aiId);
		String mySign = MD5.encode(ticket + key);
		
		JSONObject returnJson = new JSONObject();
		
		if(mySign.equals(sign))
		{
			if(code.equals("0000"))
			{
				LogBAC.logout("chargecenter", "��ǩ�ɹ�������Ҷ���"+platformOrder+"��ֵ");
				//System.out.println("��ǩ�ɹ�������Ҷ���"+platformOrder+"��ֵ");
				//todo ����ҳ�ֵ
				ReturnValue rv = ChargeOrderBAC.getInstance().orderCallback(null,platformOrder.trim(), 1,"",ip,Tools.str2int(orderAmount));
				//System.out.println("ʱ��="+System.currentTimeMillis()+"����Ҷ���"+platformOrder+"��ֵ���="+rv.success+","+rv.info);
				LogBAC.logout("chargecenter", "����Ҷ���"+platformOrder+"��ֵ���="+rv.success+","+rv.info);
				if(rv.success)
				{
					returnJson.put("code", "0000");
					returnJson.put("message", "������ֵ�ɹ�");
				}
				else
				{
					LogBAC.logout("chargecenter","����"+platformOrder+"��ֵʧ��:"+rv.info);
					returnJson.put("code", "0200");
					returnJson.put("message", rv.info);
				}	
			}
			else
			{
				ReturnValue rv = ChargeOrderBAC.getInstance().orderCallback(null,platformOrder, 0,message,ip,Tools.str2int(orderAmount));
				
				returnJson.put("code", "0000");
				returnJson.put("message", "���ճɹ�");
				LogBAC.logout("chargecenter","����"+platformOrder+"��ֵʧ��,code="+code+",message="+message+",orderId="+orderId);
			}
		}
		else
		{
			ReturnValue rv = ChargeOrderBAC.getInstance().orderCallback(null,platformOrder, 0,"ǩ��У�鲻ƥ��",ip,Tools.str2int(orderAmount));
			
			LogBAC.logout("chargecenter","ǩ��У�鲻ƥ��,sign="+sign+",mySign="+mySign);			
			returnJson.put("code", "0102");
			returnJson.put("message", "ǩ��У�鲻ƥ��");			
		}
		/*Enumeration keysEnum = request.getParameterNames();
		while(keysEnum.hasMoreElements())
		{
			String key = (String)keysEnum.nextElement();
			LogBAC.logout("charge/center",key+"="+request.getParameter(key));
			//System.out.println(key+"="+request.getParameter(key));
		}*/
		
		/*����	ʧ�ܣ�	{"code":"0102","message":"ǩ�����۸�"}			
		�ɹ���	{"code":"0000","message":"������ֵ�ɹ�"}			
			{"code":"0200","message":"��ֵʧ��"}		*/	

		//System.out.println("���������ִ�="+returnJson.toString());
		
		OutputStream os = response.getOutputStream();
		DataOutputStream dos = new DataOutputStream(os);
		dos.write(returnJson.toString().getBytes("UTF-8"));
		dos.close();
	}

}

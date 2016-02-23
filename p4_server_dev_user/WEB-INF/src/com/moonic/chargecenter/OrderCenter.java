package com.moonic.chargecenter;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.impl.Log4JLogger;
import org.json.JSONException;
import org.json.JSONObject;

import pook.paycenter.exception.PayException;
import pook.paycenter.helper.QueryCardInfoHelper;
import pook.paycenter.helper.impl.*;
import pook.paycenter.response.*;
import pook.paycenter.response.msg.*;

import server.common.Tools;
import server.config.LogBAC;
import util.IPAddressUtil;

import com.ehc.common.Log4jConfigurer;
import com.ehc.common.ReturnValue;
import com.moonic.bac.ChargeOrderBAC;
import com.moonic.bac.UserBAC;
import com.moonic.mgr.PookNet;
import com.moonic.platform.*;
import com.moonic.util.DBHelper;
import com.moonic.util.MD5;
import com.moonic.util.NetFormSender;

import conf.Conf;

/**
 * ��ֵ���Ľӿ�
 * @author alexhy
 *
 */
public class OrderCenter extends HttpServlet
{	
	public static int platformType = 10; //�¿ڴ�����OLƽ̨ 		
	
	/*��ֵ��վ����	����
	1	6	       ֧����       
	2	6	     ֧�������     
	3	12	      ��������      
	4	12	      ��������      
	5	5	     ��ͨ��ֵ��     
	6	4	     �ƶ���ֵ��     
	7	3	     ���ų�ֵ��     
	8	14	    ���˳��е㿨    
	9	0	     ����һ��ͨ     
	10	0	     ����һ��ͨ     
	11	0	      ����V ��      
	12	0	 �ƶ�����(��������) 
	13	0	      ���ɳ�ֵ      
	14	0	    ƻ��APP ��ֵ    
	15	9	��ͨ��ֵ��(�ֻ��ױ�)
	16	8	�ƶ���ֵ��(�ֻ��ױ�)
	17	10	���ų�ֵ��(�ֻ��ױ�)
	18	0	      �ƶ�����      
	19	0	 ����������ҳ���ֵ 
	20	0	      ��ͨVAC       
	21	1	     �ֻ�֧����     
	22	0	      ���Ŷ���      
	23	0	      ����ռ�      
	24	2	      �ֻ�����      
	25	0	      �ƶ�MM        
	*/
	
	//private static short[][] orderTypeCenterMap; //��վ��ֵ���ĺ͵Ķ������ͱ���ӳ��
	private static String[][] merchantIdMap;
	static
	{
		/*orderTypeCenterMap = new short[][]{			
				{1,6},
				{2,6},
				{3,12},
				{4,12},
				{5,5},
				{6,4},
				{7,3},
				{8,14},
				{15,5},
				{16,4},
				{17,3},
				{21,1},
				{24,2},
				{99,99}
			};*/
		
		merchantIdMap = new String[][]{
				{"21","2088901623514629##shiyi"},
				{"24","802310048990794##shiyi"}
		};
	}
	
	static OrderCenter self;
	public static byte iosInfullType=14; //appstore
	public static byte unionInfullType=24; //����
	public static byte zfbInfullType=21; //֧����
	public static byte pookInfullType=8; //���˵㿨
	public static byte CUCCInfullType=5; //��ͨ��ֵ��
	public static byte CMCCInfullType=6; //�ƶ���ֵ��
	public static byte CTCCInfullType=7; //���ų�ֵ��
	
	
	public static OrderCenter getInstance()
	{
		if(self==null)
		{
			self = new OrderCenter();
		}
		return self;
	}
	/**
	 * ��ֵ������վƽ̨֧�����ת���
	 * @param orderType
	 * @return
	 */
	/*private int centerToXianmo(int orderType)
	{
		for(int i=0;i<orderTypeCenterMap.length;i++)
		{
			if(orderTypeCenterMap[i][0]==orderType)
			{
				return orderTypeCenterMap[i][1];
			}
		}
		return 0;
	}*/
	
	/**
	 * ֧�����Ͷ�Ӧ���̻�id
	 * @param infullType
	 * @return
	 */
	private String infullTypeToMerchant(String infullType)
	{
		for(int i=0;i<merchantIdMap.length;i++)
		{
			if(merchantIdMap[i][0].equals(infullType))
			{
				return merchantIdMap[i][1];
			}
		}
		return "10012063118";
	}
	public ReturnValue sendToCenter(int infullType,String platformOrder,int price,String userId,String userName,int userSource,String returnUrl,String ipString,String cardNo,String cardPwd,String bankValue,String otherParam,String merchantId,String extend,String iosData)
	{			
		int orderAmount = price; //��Ԫ��
		//if(orderAmount==null || orderAmount.equals(""))orderAmount="0";	
		//if(userId==null || userId.equals(""))userId="0";
		
		String notifyUrl = Conf.ms_url+"payBack.do";		
		
		//����ֵ����ȥ�����¶���		
		//NetFormSender sender = new NetFormSender(PookNet.chargecenter_do);
		

		userId="0"; //ǿ����Ϊ0
		
		if(ipString==null || ipString.equals(""))
		{
			ipString = "0.0.0.0";
			LogBAC.logout("chargecenter", "sendToCenter ipString�쳣Ϊ��");
		}
		JSONObject extendJson=null;
		try
		{
			extendJson = new JSONObject(extend);
		}
		catch (JSONException e1)
		{			
			e1.printStackTrace();
			return new ReturnValue(false,"��չ����extend�����쳣"+e1.toString());
		}
		int playerId=extendJson.optInt("playerId");	
		if(playerId>0)
		{
			DBHelper dbHelper = new DBHelper();
			try
			{				
				dbHelper.openConnection();
				//System.out.println("extend="+extend);
				
				//����playerid���û���					
				int uId = dbHelper.getIntValue("tab_player", "userid", "id="+playerId);
				userName = dbHelper.getStrValue("tab_user", "username", "id="+uId);
				userId = String.valueOf(uId);
				
				if(userName==null || userName.equals(""))
				{
					return new ReturnValue(false,"��ѯ�����û���");
				}
			}
			catch(Exception ex)
			{
				ex.printStackTrace();
				return new ReturnValue(false,ex.toString());				
			}
			finally
			{
				dbHelper.closeConnection();
			}
		}
		/*if(userName==null || userName.equals(""))
		{
			DBHelper dbHelper = new DBHelper();
			try
			{				
				dbHelper.openConnection();
				//System.out.println("extend="+extend);
				
				//����playerid���û���					
				int uId = dbHelper.getIntValue("tab_player", "userid", "id="+playerId);
				userName = dbHelper.getStrValue("tab_user", "username", "id="+uId);
				userId = String.valueOf(uId);
				
				if(userName==null || userName.equals(""))
				{
					return new ReturnValue(false,"��ѯ�����û���");
				}
			}
			catch(Exception ex)
			{
				ex.printStackTrace();
				return new ReturnValue(false,ex.toString());				
			}
			finally
			{
				dbHelper.closeConnection();
			}
		}	*/
		//���������֧�������Ƿ����
		String channel = extendJson.optString("channel");
		boolean checkResult = ChargeOrderBAC.getInstance().checkChannelChargeType(channel, infullType);
		if(!checkResult)		
		{
			LogBAC.logout("chargecenter", "����"+channel+"��֧������"+infullType+"������");
			return new ReturnValue(false,"����"+channel+"��֧������"+infullType+"������");
		}
		int agentId = 0;
		
		try {
			//�����¶��������ݿ���
			ReturnValue rv = ChargeOrderBAC.getInstance().createCenterNewOrderWithoutCOrder(platformOrder, infullType, orderAmount, userName, extend, Tools.getCurrentDateTimeStr(), ipString,userSource);
			if(rv.success)
			{
				LogBAC.logout("chargecenter", "��ƽ̨���������ɹ�,platformOrder="+platformOrder);
				IResponse response=null;
				
				//����ֵ���ĵ��û������Ȳ��ܳ���16���Ҳ��ܰ���*��������userId����
				if(userName.length()>16 || userName.indexOf("*")!=-1)
				{
					userName = userId;
				}
				
				
				if(infullType==iosInfullType) //IOS
				{
					response =AppleInfullHelper.request(platformOrder, orderAmount, platformType, Tools.str2int(userId), userName, userSource, returnUrl, notifyUrl, ipString, iosData);					
				}
				else
				if(infullType==unionInfullType) //����
				{					
					response =MobileUnionPayInfullHelper.request(platformOrder, platformType, infullType, orderAmount, Tools.str2int(userId), userName, agentId, returnUrl, notifyUrl, ipString);
				}
				else
				if(infullType==CUCCInfullType || infullType==CTCCInfullType || infullType==CMCCInfullType) //������ͨ�ƶ�
				{					
					response = DirectCardInfullHelper.request(platformOrder, orderAmount, infullType, platformType, Tools.str2int(userId), userName, agentId, returnUrl, notifyUrl, cardNo,cardPwd, ipString);					
				}
				else
				if(infullType==pookInfullType)
				{
					//��ѯ���˵㿨���
					QueryCardInfoResponse resp = (QueryCardInfoResponse) QueryCardInfoHelper.request(cardNo);
					int cardValue = resp.getValue();
					//LogBAC.logout("chargecenter", "QueryCardInfoHelper.request��ѯ"+cardNo+"�����Ϊ"+cardValue);
					
					if(orderAmount==cardValue)
					{
						//System.out.println("���˵㿨");					
						response = PookCardInfullHelper.request(platformOrder, platformType, Tools.str2int(userId), userName, agentId, returnUrl, notifyUrl, cardNo,MD5.encode(cardPwd),ipString);
						//System.out.println("���˵㿨�������");	
					}
					else
					{
						return new ReturnValue(false,"���˵㿨���Ϊ"+cardValue+"�Ͷ����۸�"+orderAmount+"��һ���޷�֧��");
					}
				}
				else
				{					
					response = GenerateInfullOrderHelper.request(platformOrder, platformType, orderAmount, infullType, Tools.str2int(userId), userName, userSource, returnUrl, notifyUrl, "", ipString);
				}
				InfullRequestResponse respInfo = (InfullRequestResponse)response;
				String cOrderNo = respInfo.getOrderNo();
				int cPrice = respInfo.getInfullAmount();
				//String message = response.getMsg();
				String ext = respInfo.getFormInfo();
				
				JSONObject json = new JSONObject();
				json.setForceLowerCase(false);
				
				String ver = extendJson.optString("ver");  //�����������������
				
				if(channel.equals("018")) //������
				{
					P018 p018 =new P018();
					ReturnValue vivoRV = p018.getOrderInfo(cOrderNo, orderAmount);
					if(vivoRV.success)
					{
						String vivoExt = vivoRV.info;	
						
						json.put("orderId", cOrderNo);				
						json.put("ext", vivoExt);
					}
					else
					{
						ChargeOrderBAC.getInstance().updateCenterOrderNo(platformOrder, cOrderNo);
						return new ReturnValue(false,vivoRV.info);
					}
				}
				else
				if(channel.equals("009") && ver!=null && ver.equals("2")) //������sdk
				{
					String subject = extendJson.optString("subject");
					P009 p009 =new P009();
					ReturnValue gioneeRV = p009.getOrderInfo(playerId,cOrderNo, orderAmount,subject);
					if(gioneeRV.success)
					{
						String gioneeExt = gioneeRV.info;	
						
						json.put("orderId", cOrderNo);				
						json.put("ext", gioneeExt);
					}
					else
					{
						ChargeOrderBAC.getInstance().updateCenterOrderNo(platformOrder, cOrderNo);
						return new ReturnValue(false,gioneeRV.info);
					}
				}
				else
				{
					json.put("orderId", cOrderNo);				
					json.put("ext", ext);
				}				
				
				LogBAC.logout("chargecenter", "��ֵ���Ķ��������ɹ�������cOrderNo="+cOrderNo+",ext="+ext);			
				//д���ֵ���Ķ�����
				ChargeOrderBAC.getInstance().updateCenterOrderNo(platformOrder, cOrderNo);
				return new ReturnValue(true,json.toString());
			}
			else
			{						
				String message = "��������ʧ�ܣ�"+rv.info+"(���Ա���)";
				LogBAC.logout("chargecenter", message);
				return new ReturnValue(false,message);				
			}
		} catch (PayException e) {
			String message = "��������ʧ�ܣ�"+e.getMsg()+"(��������)PayException="+e.toString();
			LogBAC.logout("chargecenter", message);
			return new ReturnValue(false,"��������ʧ�ܣ�"+e.getMsg()+"(��������)");		
		} catch (Exception e) {
			e.printStackTrace();				
			String message = "��������ʧ�ܣ�"+e.toString()+"(���Ա���)";			
			LogBAC.logout("chargecenter", message);
			return new ReturnValue(false,message);			
		}
	}
	protected void service(HttpServletRequest request, HttpServletResponse response)throws ServletException, IOException 
	{
		String ip = IPAddressUtil.getIp(request);
		LogBAC.logout("chargecenter", "------------------------------------------------");
		LogBAC.logout("chargecenter", "�յ�����"+ip+"���¶���");
		Enumeration keysEnum = request.getParameterNames();
		//System.out.println(Tools.getCurrentDateTimeStr()+"-�յ�����"+ip+"������");		
		while (keysEnum.hasMoreElements())
		{
			String key = (String) keysEnum.nextElement();
			LogBAC.logout("chargecenter", key + "=" + request.getParameter(key));			
		}		
		
		DataOutputStream dos = new DataOutputStream(response.getOutputStream());
		
		String infullType = Tools.strNull(request.getParameter("infullType")); //��ֵ��ʽ
		
		if(infullType==null || infullType.equals(""))
		{			
			dos.writeByte(0);			
			dos.write("ȱ����Ч����".getBytes("UTF-8"));
			dos.close();	
			return;
		}
		 
		String platformOrder = ChargeOrderBAC.getNextOrderNo(); //��ƽ̨������		
		String orderAmount = Tools.strNull(request.getParameter("orderAmount")); //��Ԫ��
		if(orderAmount==null || orderAmount.equals(""))orderAmount="0";
		String userId =Tools.strNull(request.getParameter("userId"));
		if(userId==null || userId.equals(""))userId="0";
		String userNameOrMobile = Tools.strNull(request.getParameter("userNameOrMobile"));
		String userName = Tools.strNull(request.getParameter("userName"));
		int userSource =Tools.str2int(request.getParameter("userSource"));
		String returnUrl=Tools.strNull(request.getParameter("returnUrl"));
		//String notifyUrl = Conf.ms_url+"payOrderCallback.do";
		String ipString = Tools.strNull(request.getParameter("ipString"));
		if(ipString==null || ipString.equals(""))
		{
			ipString = ip;
		}
		if(ipString==null || ipString.equals(""))
		{
			ipString = "0.0.0.0";
			LogBAC.logout("chargecenter", "ipString�쳣Ϊ��");
		}
		
		String cardNo = Tools.strNull(request.getParameter("cardNo"));
		String cardPwd = Tools.strNull(request.getParameter("cardPwd"));
		String bankValue = Tools.strNull(request.getParameter("bankValue"));
		String otherParam = Tools.strNull(request.getParameter("otherParam"));  //��ֵ������չ����
		String merchantId = infullTypeToMerchant(infullType); //�̻�id		
		String extend = Tools.strNull(request.getParameter("extend"));  //��ƽ̨��չ����
		String iosData = Tools.strNull(request.getParameter("iosData"));  //ios���Ѳ���
		
		if(userSource==0)
		{
			userSource=1;
		}
		
		if(userName==null || userName.equals(""))
		{
			userName = userNameOrMobile;
		}
		userId="0"; //ǿ����Ϊ0
		
		ReturnValue rv = sendToCenter(Tools.str2int(infullType),platformOrder,Tools.str2int(orderAmount),userId,userName,userSource,returnUrl,ipString,cardNo,cardPwd,bankValue,otherParam,merchantId,extend,iosData);
		
		if(rv.success)
		{
			dos.writeByte(1);
		}
		else
		{
			dos.writeByte(0);
		}
		dos.write(rv.info.getBytes("UTF-8"));
		dos.close();		
	}
	public static void main(String[] args)
	{
		/*
		
		4700033040	355a4x6r7y
		4700033039	6qykfsbkzz
		4700033038	2041f4mhm0
		4700033037	tx0351etg5
		4700033036	68m39p3drz
		4700033035	ddfkx0h41x
		4700033034	xhtk8h6xkt
		4700033033	hm54qgz74m*/

		
		String appendType = "12";
				
		String platformOrder =String.valueOf(System.currentTimeMillis());  //��ƽ̨������			
		String appendAmount = "10"; //���(Ԫ)			
		String userId = "1";		//�û�ID			
		String userName = "alexhy";		//�û���			
		String accountSource=""; //�˺���Դ����ѡ��:��δʹ��			
		String returnUrl = "http://xmlogintest.pook.com:82/xianmo_user/orderCenterCallback.do";		//�ص���ַ:��ֵ�ɹ���Ļص���ַ			
		String ipString = "118.242.16.50";		//�û�IP			
		String cardNo = "4700033040";		//���ţ���ѡ��			
		String cardPwd= "355a4x6r7y";	//���루��ѡ��			
		String bankValue= "";	//������������(��ѡ)			
		String otherParam= "";	//������������ѡ��
		
		
		String key = "79c3eea3f305d6b823f562ac4be35212";
		String url = "http://paytest.pook.com.cn/pay.jsp"; //���Ե�ַ
		NetFormSender sender = new NetFormSender(url);
		
		sender.addParameter("appendType",appendType);
		sender.addParameter("platformType",platformType);
		sender.addParameter("platformOrder",platformOrder);
		sender.addParameter("appendAmount",appendAmount); //10Ԫ
		sender.addParameter("userId",userId);
		sender.addParameter("userName",userName);
		sender.addParameter("userName",userName);
		sender.addParameter("returnUrl",returnUrl);
		sender.addParameter("ipString",ipString);		
		sender.addParameter("cardNo",cardNo);
		sender.addParameter("cardPwd",cardPwd);		
		sender.addParameter("ipString",ipString);
		
		StringBuffer ticket = new StringBuffer();
		ticket.append(appendType);
		ticket.append(platformType);
		ticket.append(platformOrder);
		ticket.append(appendAmount);
		ticket.append(userId);
		ticket.append(userName);			
		ticket.append(accountSource);
		ticket.append(returnUrl);
		ticket.append(ipString);
		ticket.append(cardNo);			
		ticket.append(cardPwd);
		ticket.append(bankValue);
		ticket.append(otherParam);
		//System.out.println("ticket="+ticket.toString());
		String sign = MD5.encode(ticket.toString()+key);
		//System.out.println("sign="+sign);
		sender.addParameter("sign", sign);
	
		try {
			sender.send().check();
			if(sender.rv.success)
			{
				//���˷��أ�{"code":"0001","orderId":"D201309091131073850","message":"��ʱ��ֵ��","ext":""}
				System.out.println("���ɶ�������ɹ�,���˷��أ�"+sender.rv.info);
			}
			else
			{
				System.out.println("���ɶ�������ʧ��,���˷��أ�"+sender.rv.info);
			}
		} catch (Exception e) {
			System.out.println("���ɶ�������ʧ�ܣ�"+e.toString());
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

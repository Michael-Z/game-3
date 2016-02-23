package com.moonic.servlet;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import server.common.Tools;
import util.IPAddressUtil;

import com.ehc.common.ReturnValue;
import com.moonic.bac.ActivateCodeBAC;
import com.moonic.bac.ExchangeCodeBAC;
import com.moonic.bac.PlatformGiftBAC;
import com.moonic.bac.PlayerBAC;
import com.moonic.bac.ServerBAC;
import com.moonic.bac.UserBAC;

public class WebServlet extends HttpServlet {
	private static final long serialVersionUID = 4598035092703154800L;
	
	/**
	 * ��ȡ������
	 */
	public static final short ACT_ACTIVATE_GET_CODE = 161;
	/**
	 * ��ȡ�һ���
	 */
	public static final short ACT_EXCHANGE_GET_CODE = 162;
	/**
	 * ����һ���
	 */
	public static final short ACT_EXCHANGE_CHECK_CODE = 163;
	/**
	 * �������Ѷһ�
	 */
	public static final short ACT_EXCHANGE_SET_CODE = 164;
	/**
	 * ��ȡ�������б�
	 */
	public static final short ACT_GET_SERVERLIST = 165;
	/**
	 * ��ȡ��ɫ��Ϣ
	 */
	public static final short ACT_GET_PLAINFO = 166;
	/**
	 * �����������б�
	 */
	public static final short ACT_GET_CODEGIFT_LIST = 167;
	/**
	 * ��ȡ��������
	 */
	public static final short ACT_GET_CODEGIFT = 168;
	/**
	 * ��ȡ�û������ж�����
	 */
	public static final short ACT_GET_USER_JUDGMENTDATA = 171;
	/**
	 * �ж�ָ���Ĳ����û����Ƿ����
	 */
	public static final short ACT_USER_EXIST = 172;
	/**
	 * ��ȡָ���û��ķ������б�
	 */
	public static final short ACT_GET_USER_SERVERLIST = 173;
	
	/**
	 * service
	 */
	public void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		InputStream is = request.getInputStream();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] buff = new byte[4096];
		int readLen = -1;
		while ((readLen = is.read(buff)) != -1) {
			baos.write(buff, 0, readLen);
		}
		buff = baos.toByteArray();
		DataInputStream dis = new DataInputStream(new ByteArrayInputStream(buff));
		DataOutputStream dos = new DataOutputStream(response.getOutputStream());		
		try {
			ReturnValue val = null;
			
			if (buff.length == 0) {
				val = new ReturnValue(false, "��Ч����");
			} else {
				try {
					val = processingReq(request, response, dis, dos);
				} catch (EOFException e) {
					DataInputStream edis = new DataInputStream(new ByteArrayInputStream(buff));
					int act = edis.readShort();
					System.out.println(e.toString()+"(act="+act+")");
					e.printStackTrace();	
					val = new ReturnValue(false, e.toString());
				} catch (Exception ex1) {
					ex1.printStackTrace();
					val = new ReturnValue(false, ex1.toString());
				}
			}
			dis.close();
			byte[] responseData = null;
			if(val.getDataType()==ReturnValue.TYPE_STR) 
			{				
				responseData = Tools.strNull(val.info).getBytes("UTF-8");
			} 
			else 
			{
				responseData = val.binaryData;
			}
			dos.writeByte(val.success ? 1 : 0);
			dos.write(responseData);
		} catch (Exception e) {
			e.printStackTrace();
			dos.writeByte(0);
			dos.write(e.toString().getBytes("UTF-8"));
		}
		finally
		{
			dos.close();
		}
	}
	
	/**
	 * ��������
	 */
	private ReturnValue processingReq(HttpServletRequest request, HttpServletResponse response, DataInputStream dis, DataOutputStream dos) throws Exception{
		short act = dis.readShort();
		String ip = IPAddressUtil.getIp(request);
		if(act == ACT_ACTIVATE_GET_CODE) {
			String phone = dis.readUTF();
			byte from = dis.readByte();//1��ֱ�ӷ��Ͷ��Ż�ã�2��ͨ����վ���
			String punlishuser = dis.readUTF();
			return ActivateCodeBAC.getInstance().getActivateCode(phone, ip, from, punlishuser);
		} else 
		if(act == ACT_EXCHANGE_GET_CODE) {
			String phone = dis.readUTF();		
			return ExchangeCodeBAC.getInstance().getExchangeCode(phone);
		} else 
		if(act == ACT_EXCHANGE_CHECK_CODE) {			
			String code = dis.readUTF();
			String phone = dis.readUTF();
			return ExchangeCodeBAC.getInstance().checkCode(code,phone);
		} else 
		if(act == ACT_EXCHANGE_SET_CODE) {			
			String code = dis.readUTF();			
			return ExchangeCodeBAC.getInstance().exchangeCode(code);
		} else 
		if(act == ACT_GET_SERVERLIST){
			String channel = dis.readUTF();
			return ServerBAC.getInstance().webGetServerList(channel);
		} else 
		if(act == ACT_GET_PLAINFO){
			String username = dis.readUTF();
			String channel = dis.readUTF();
			int vsid = dis.readInt();
			return PlayerBAC.getInstance().webGetPlayerInfo(username, channel, vsid);
		} else 
		if(act == ACT_GET_CODEGIFT_LIST){
			return PlatformGiftBAC.getInstance().webGetGiftList();
		} else 
		if(act == ACT_GET_CODEGIFT){
			int playerid = dis.readInt();
			int vsid = dis.readInt();
			String num = dis.readUTF();
			return PlatformGiftBAC.getInstance().webGetPlatformGift(playerid, vsid, num);
		} else 
		if(act == ACT_GET_USER_JUDGMENTDATA){
			String username = dis.readUTF();
			String imei = dis.readUTF();
			return UserBAC.getInstance().webGetJudgmentData(username, imei);
		} else 
		if(act == ACT_USER_EXIST) {	
			String platform = dis.readUTF();
			String username = dis.readUTF();
			return UserBAC.getInstance().webUserExist(platform, username);
		} else
		if(act == ACT_GET_USER_SERVERLIST){
			String platform = dis.readUTF();
			String username = dis.readUTF();
			return ServerBAC.getInstance().webGetUserServerList(platform, username);
		} else 
		{
			return new ReturnValue(false, "��Ч����");
		}
	}
}

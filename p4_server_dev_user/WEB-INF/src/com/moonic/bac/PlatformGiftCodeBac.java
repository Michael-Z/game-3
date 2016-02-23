package com.moonic.bac;

import java.util.Random;

import org.json.JSONObject;

import server.common.Tools;

import com.ehc.common.ReturnValue;
import com.ehc.common.SqlString;
import com.moonic.util.BACException;
import com.moonic.util.DBHelper;
import com.moonic.util.DBPaRs;
import com.moonic.util.DBPool;
import com.moonic.util.MyTools;

public class PlatformGiftCodeBac {
	public static String tab_platform_gift_code = "tab_platform_gift_code";
	
	/*
	 * number ��������
	 * character �ַ�����
	 * all �����ַ������
	 */
	public static enum CodeType {NUMBER,CHARACTER,ALL};
	static char letter[]= {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H','J', 'K',  'M', 'N',  'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'};
	static char digit[]={'2', '3', '4', '5', '6', '7', '8', '9'};
	
	private static String generate(CodeType type,int length){
		StringBuffer sb=new StringBuffer();
		for (int i = 0; i < length; i++) 
		switch(type){
		case NUMBER:
			sb.append(digit[new Random().nextInt(digit.length)]);
			break;
		case CHARACTER:
			sb.append(letter[new Random().nextInt(letter.length)]);
			break;
		case ALL:
			sb.append((char)(new Random().nextInt(2)==0? letter[new Random().nextInt(letter.length)]:digit[new Random().nextInt(digit.length)]));
			break;
		}
		return sb.toString();
	}
	
	 /**
	  * �����ֻ���������ƽ̨������
	  */
	public ReturnValue createGiftCodeByPhonenumber(int vsid, String platformId, int giftgroup, CodeType type, int length, String phonenumber) {
		DBHelper dbHelper = new DBHelper();
		try{
			if(giftgroup==0) {
				BACException.throwInstance("��ѡ���������");
			}
			if(vsid==0) {
				BACException.throwInstance("��ѡ����Ϸ��");
			}
			if(phonenumber==null || phonenumber.equals("")) {
				BACException.throwInstance("�������ֻ�����");
			}
			dbHelper.openConnection();
			//�����������Ƿ����
			DBPaRs giftRs = DBPool.getInst().pQueryA(PlatformGiftBAC.tab_platform_gift, "num="+giftgroup);
			if(!giftRs.exist()){
				BACException.throwInstance("��������Ͳ�����");
			}
			//�����ֻ����Ƿ��ѻ�ȡ����ȡ��
			JSONObject json = dbHelper.queryJsonObj(tab_platform_gift_code, "playerid", "phonenumber='"+phonenumber+"' and giftcode="+giftgroup+" and serverid="+vsid);
			if(json != null) {
				BACException.throwInstance("����ֻ���"+phonenumber+"�Ѿ���ȡ��"+giftRs.getString("name")+"������ˣ������ظ���ȡ��");
			}
			synchronized (instance) {
				int daylimit = giftRs.getInt("daylimit");
				if(daylimit > 0) {
					long currtime = System.currentTimeMillis();
					long starttime = 0;
					long endtime = 0;
					long pointtime = MyTools.getCurrentDateLong()+MyTools.getPointTimeLong("15:00:00");
					if(currtime < pointtime){
						starttime = pointtime-MyTools.long_day;
						endtime = pointtime;
					} else {
						starttime = pointtime;
						endtime = pointtime+MyTools.long_day;
					}
					int todaygive = dbHelper.queryCount(tab_platform_gift_code, "publishtime>="+MyTools.getTimeStr(starttime)+" and publishtime<="+MyTools.getTimeStr(endtime)+" and giftcode="+giftgroup);
					if(todaygive >= daylimit) {
						if(currtime < pointtime) {
							BACException.throwInstance(giftRs.getString("name")+"�����Ѿ�����ȡ��"+daylimit+"���ˣ�������15:00������ȡ��");
						} else {
							BACException.throwInstance(giftRs.getString("name")+"�����Ѿ�����ȡ��"+daylimit+"���ˣ�������15:00������ȡ��");
						}
					}
				}
				String code = null;
				while(true) {
					code = generate(type,length);
					//����ΪALLʱ��������������������
					if(type == CodeType.ALL && Tools.str2long(code)>0) {
						continue;
					}
					boolean exist = dbHelper.queryExist(tab_platform_gift_code,"code='"+code+"'");
					if(exist){
						continue;
					}
					SqlString sqlStr = new SqlString();
					sqlStr.add("code", code);
					sqlStr.add("platform", platformId);
					sqlStr.add("giftcode", giftgroup);
					sqlStr.add("playerid", 0);
					sqlStr.add("gived", 0);
					sqlStr.add("publish", 1);
					sqlStr.add("phonenumber", phonenumber);
					sqlStr.add("serverid",vsid);
					sqlStr.addDateTime("publishtime", MyTools.getTimeStr()); 
					dbHelper.insert(tab_platform_gift_code, sqlStr);
					break;
				}
				JSONObject returnJson = new JSONObject();
				returnJson.put("note", "��ϲ���Ѿ��ɹ���ȡ��"+giftRs.getString("name")+"������룺"+code+",������Ϸͨ���NPC�����ʹ��������ȡ����Ϸ���ص�ַ��http://xm.pook.com");
				return new ReturnValue(true, returnJson.toString());
			}
		} catch(Exception ex){
			ex.printStackTrace();
			JSONObject returnJson = new JSONObject();
			returnJson.put("note", "��ȡ������쳣��"+ex.toString());
			return new ReturnValue(true, returnJson.toString());
		} finally{
			dbHelper.closeConnection();
		}
	}
	
	//--------------��̬��--------------
	
	private static PlatformGiftCodeBac instance = new PlatformGiftCodeBac();
	
	public static PlatformGiftCodeBac getInstance(){
		return instance;
	}
}

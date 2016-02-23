package com.moonic.bac;

import java.util.ArrayList;

import org.json.JSONArray;

import com.ehc.common.ReturnValue;
import com.moonic.util.BACException;
import com.moonic.util.DBPool;
import com.moonic.util.DBPsRs;
import com.moonic.util.MyTools;

/**
 * �������BAC
 * @author John
 */
public class RanNameBAC {
	private static final String tab_random_name = "tab_random_name";
	
	/**
	 * ��ȡָ�������Ŀ����������
	 */
	public ReturnValue getRandomName(int serverid, byte amount) {
		try {
			if(amount <= 0 || amount > 100){
				BACException.throwInstance("��������");
			}
			//JSONArray jsonarr = new JSONArray();
			//jsonarr.add(createRandomName(serverid, 1, amount));
			//jsonarr.add(createRandomName(serverid, 2, amount));
			JSONArray jsonarr = createRandomName(serverid, 1, amount);
			return new ReturnValue(true, jsonarr.toString());
		} catch (Exception e) {
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		}
	}
	
	/**
	 * ���������
	 */
	private JSONArray createRandomName(int serverid, int sex, int amount) throws Exception{
		DBPsRs rs1 = DBPool.getInst().pQueryS(tab_random_name, "type="+0);
		DBPsRs rs2 = DBPool.getInst().pQueryS(tab_random_name, "type="+sex);
		int firLen = rs1.count();
		int secLen = rs2.count();
		ArrayList<String> namearr = new ArrayList<String>();
		while(namearr.size() < amount){
			int fir = MyTools.getRandom(1, firLen);
			int sec = MyTools.getRandom(1, secLen);
			StringBuffer sb = new StringBuffer();
			rs1.setRow(fir);
			sb.append(rs1.getString("name"));
			rs2.setRow(sec);
			sb.append(rs2.getString("name"));
			String newname = sb.toString();
			if(namearr.contains(newname)){
				continue;
			}
			namearr.add(newname);
		}
		return new JSONArray(namearr);
	}
	
	//--------------��̬��--------------
	
	private static RanNameBAC instance = new RanNameBAC();
	
	/**
	 * ��ȡʵ��
	 */
	public static RanNameBAC getInstance(){
		return instance;
	}
}

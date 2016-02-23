package com.moonic.worldboss;

import org.json.JSONArray;

import server.common.Sortable;

import com.moonic.bac.PlayerBAC;
import com.moonic.util.DBPaRs;

/**
 * ���
 * @author wkc
 */
public class WBPlayer implements Sortable{
	
	public int id;//ID
	public int num;//���
	public String name;//����
	public int lv;//�ȼ�
	
	public int chaTimes;//��ս����
	
	public long totalDamage;//��BOSS���˺�
	
	public int rank;//����
	
	public JSONArray partnerArr;//�Ѳ�ս�Ļ��ID
	
	/**
	 * ����
	 */
	public WBPlayer(int id) throws Exception{
		DBPaRs playerRs = PlayerBAC.getInstance().getDataRs(id);
		this.id = id;
		this.num = playerRs.getInt("num");
		this.name = playerRs.getString("name");
		this.lv = playerRs.getInt("lv");
		this.partnerArr = new JSONArray();
	}
	
	/**
	 * ��ȡ��ɫ����
	 */
	public JSONArray getData(){
		JSONArray arr = new JSONArray();
		arr.add(chaTimes);
		arr.add(totalDamage);
		arr.add(rank);
		arr.add(partnerArr);
		return arr;
	}
	
	/**
	 * ��ȡ���н�ɫ����
	 */
	public JSONArray getData1(){
		JSONArray arr = new JSONArray();
		arr.add(id);
		arr.add(num);
		arr.add(name);
		arr.add(lv);
		arr.add(totalDamage);
		return arr;
	}
	
	@Override
	public double getSortValue() {
		return totalDamage;
	}
}

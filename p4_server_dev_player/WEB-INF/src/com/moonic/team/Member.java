package com.moonic.team;

import org.json.JSONArray;
import org.json.JSONException;

import com.moonic.bac.PlaTeamBAC;
import com.moonic.bac.PlayerBAC;
import com.moonic.util.DBPaRs;

/**
 * �����Ա
 * @author wkc
 */
public class Member {
	public int id;//ID
	public int num;//���
	public String name;//����
	public int lv;//�ȼ�
	
	public int times;//�ѻ�ý�������
	
	public boolean isLeader;//�Ƿ��Ƕӳ�
	public boolean isReady;//�Ƿ���׼��
	
	public JSONArray partnerArr;//������
	
	public int battlePower;//ս��
	
	/**
	 * ����
	 * @throws Exception 
	 */
	public Member(int playerid) throws Exception{
		DBPaRs playerRs = PlayerBAC.getInstance().getDataRs(playerid);
		this.id = playerid;
		this.num = playerRs.getInt("num");
		this.name = playerRs.getString("name");
		this.lv = playerRs.getInt("lv");
		this.times = PlaTeamBAC.getInstance().getIntValue(playerid, "times");
		this.isLeader = false;
		this.isReady = false;
		this.partnerArr = new JSONArray();
	}
	
	/**
	 * ��ȡ��Ա��Ϣ
	 */
	public JSONArray getMemberInfo(){
		JSONArray jsonarr = new JSONArray();
		jsonarr.add(this.id);//ID
		jsonarr.add(this.num);//���
		jsonarr.add(this.name);//����
		jsonarr.add(this.lv);//�ȼ�
		jsonarr.add(this.isLeader ? 1 : 0);//�Ƿ�Ϊ�ӳ�
		jsonarr.add(this.isReady ? 1 : 0);//�Ƿ�׼��
		jsonarr.add(this.battlePower);//ս��
		jsonarr.add(this.partnerArr);//��֤�������
		return jsonarr;
	}
	
	/**
	 * ��ȡ��������
	 * @throws JSONException 
	 */
	public JSONArray getPosArr() throws JSONException{
		JSONArray posArr = new JSONArray();
		for(int i = 0; i < partnerArr.length(); i++){
			JSONArray partner = partnerArr.getJSONArray(i);
			int partnerId = 0;
			if(partner.length() > 0){
				partnerId = partner.getInt(0);
			}
			posArr.add(partnerId);
		}
		return posArr;
	}
	
	/**
	 * ��ȡ����������
	 */
	public int getPartnerAm() throws JSONException{
		int partnerAm = 0;
		for(int i = 0; i < partnerArr.length(); i++){
			JSONArray partner = partnerArr.getJSONArray(i);
			if(partner.length() > 0){
				partnerAm++;
			}
		}
		return partnerAm;
	}
}

package com.moonic.cb;

import java.util.ArrayList;

import org.json.JSONArray;

import com.moonic.bac.CBBAC;
import com.moonic.bac.PartnerBAC;
import com.moonic.battle.Const;
import com.moonic.battle.TeamBox;

/**
 * ����
 * @author John
 */
public class Faction {
	public int influence;//����
	
	public int factionid;//����ID
	
	public String factionname;//������
	
	public byte teamType;//��������(��/��)
	
	public int teamamount;//�ܶ�����
	
	public ArrayList<TeamBox> teamlist = new ArrayList<TeamBox>();//ʣ�������
	
	public ArrayList<Integer> pidlist = new ArrayList<Integer>();//����Ӫ������Щ��ɫ�Ľ�ɫID����
	
	public City city;
	
	/**
	 * �Ӷ������
	 */
	public JSONArray addTeams(ArrayList<TeamBox> teamboxlist) throws Exception {
		JSONArray batteamidarr = new JSONArray();
		for(int i = 0; i < teamboxlist.size(); i++){
			int batteamid = addTeam(teamboxlist.get(i));
			batteamidarr.add(batteamid);
		}
		return batteamidarr;
	}
	
	/**
	 * �Ӷ���
	 */
	public synchronized int addTeam(TeamBox teambox) throws Exception {
		teambox.teamType = teamType;
		for(int i = 0; teambox.playerid != 0 && i < teambox.sprites.size(); i++){
			Partner partner = new Partner();
			partner.id = teambox.sprites.get(i).partnerId;
			partner.city = city;
			partner.teambox = teambox;
			if(teambox.playerid != 0 && !pidlist.contains(teambox.playerid)){
				pidlist.add(teambox.playerid);
			}
			city.partnerlist.add(partner);
			CBBAC.cbmgr.playermapAdd(partner.teambox.playerid);
			CBBAC.cbmgr.partnermap.put(partner.id, partner);
		}
		int teamid = CBBAC.cbmgr.createTeamid();
		int power = PartnerBAC.getInstance().getTeamBoxBattlePower(teambox);
		teambox.parameterarr.put(4, power);
		teambox.parameterarr.put(6, teamid);
		teambox.parameterarr.put(7, city.num);
		teambox.parameterarr.put(8, teamType);
		teamlist.add(teambox);
		city.log.d("����ս�� ��Ӫ��"+(teambox.teamType==Const.teamA?"����":"�ط�")+" TEAMID��"+teamid+" ������Ϣ��"+teambox.getTeamDataStr());
		teamamount++;
		return teamid;
	}
	
	/**
	 * ��ȡ����
	 */
	public JSONArray getData() throws Exception {
		JSONArray arr = new JSONArray();
		arr.add(factionname);//������
		arr.add(teamlist.size());//��������
		arr.add(getCurrBatterData());//��ǰս��������
		return arr;
	}
	
	/**
	 * ��ȡ����2
	 */
	public JSONArray getData2() throws Exception {
		JSONArray arr = new JSONArray();
		arr.add(factionname);//������
		arr.add(teamlist.size());//��������
		return arr;
	}
	
	/**
	 * ��ȡ����ս������Ϣ
	 */
	public JSONArray getBattlerData(){
		JSONArray rankingarr = new JSONArray();
		for(int i = 0; rankingarr.length() < 9 && i < teamlist.size(); i++){
			TeamBox teambox = teamlist.get(i);
			JSONArray arr = new JSONArray();
			arr.add(teambox.pname);//�����
			arr.add(teambox.parameterarr.optInt(1));//�ȼ�
			arr.add(teambox.parameterarr.optInt(4));//ս��
			rankingarr.add(arr);
		}
		return rankingarr;
	}
	
	/**
	 * ��ȡ��ǰս����������
	 */
	public JSONArray getCurrBatterData() throws Exception {
		JSONArray batterdata = null;
		if(teamlist.size() > 0){
			TeamBox teambox = teamlist.get(0);
			batterdata = new JSONArray();
			batterdata.add(teambox.playerid);
			batterdata.add(teambox.pname);
			batterdata.add(teambox.parameterarr.optInt(2));//ͷ����
			batterdata.add(teambox.parameterarr.optInt(1));//�����ȼ�
			batterdata.add(teambox.parameterarr.optInt(3));//ƣ�Ͷ�
			batterdata.add(teambox.parameterarr.optInt(4));//ս��
			batterdata.add(teambox.getTotalPropValue(Const.PROP_HP));//��ǰѪ��
			batterdata.add(teambox.getTotalPropValue(Const.PROP_MAXHP));//���Ѫ��
			batterdata.add(teambox.getNumFormaction());//��ȡ���վλ
		}
		return batterdata;
	}
}

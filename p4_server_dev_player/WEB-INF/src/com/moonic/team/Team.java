package com.moonic.team;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;

import org.json.JSONArray;

import com.moonic.bac.PartnerBAC;
import com.moonic.battle.SpriteBox;
import com.moonic.battle.TeamBox;
import com.moonic.socket.PushData;
import com.moonic.util.BACException;
import com.moonic.util.DBPool;
import com.moonic.util.DBPsRs;

import server.common.Sortable;
import server.common.Tools;

/**
 * ����
 * @author wkc
 */
public class Team implements Sortable{
	public int num;//������
	public int type;//��������
	public String name;//����
	public int lvLimit;//�ȼ�����
	public String award;//����
	public String boss;//����
	
	public Member leader;//�ӳ�
	
	public Hashtable<Integer, Member> memberTable = new Hashtable<Integer, Member>();//���������г�Ա
	
	public static int id = 1;//����ΨһID
	
	public static final String tab_team_boss = "tab_team_boss";
	
	/**
	 * ��ȡ������
	 */
	public static synchronized int getTeamNum(){
		return id++;
	} 
	
	/**
	 * ����
	 * @throws Exception 
	 */
	public Team(Member leader, int type) throws Exception{
		this.num = getTeamNum();
		this.type = type;
		this.leader = leader;
		int plv = leader.lv;
		int row = 0;
		DBPsRs bossListRs = DBPool.getInst().pQueryS(tab_team_boss, "type="+type);
		while(bossListRs.next()){
			if(bossListRs.getInt("lv") > plv){
				break;
			}
			row++;
		}
		if(row == 0){
			BACException.throwInstance("û�з���������BOSS");		
		}
		bossListRs.setRow(row);
		this.name = bossListRs.getString("name");
		this.lvLimit = bossListRs.getInt("lv");
		this.award = bossListRs.getString("award");
		this.boss = bossListRs.getString("boss");
	}
	
	/**
	 * ��ȡ����TeamBox
	 * @throws Exception
	 */
	public TeamBox getTeamBox() throws Exception{
		Member[] memberArr = memberTable.values().toArray(new Member[memberTable.size()]);
		if(memberArr.length == 3 && !memberArr[1].isLeader){//�ӳ����м�
			int index = 0;
			for(int i = 0; i < memberArr.length; i++){
				if(memberArr[i].isLeader){
					index = i;
					break;
				}
			}
			Member temp = memberArr[1]; 
			memberArr[1] =  memberArr[index];
			memberArr[index] = temp;
		} 
		SpriteBox[][] spriteboxs = new SpriteBox[2][3];
		int column = 0;
		for(int i = 0; i < memberArr.length; i++){
			Member member = memberArr[i];
			int playerid = member.id;
			JSONArray posArr = member.getPosArr();
			ArrayList<SpriteBox> spritesList = PartnerBAC.getInstance().getSpriteBoxList(playerid, posArr, null);
			int row = 0;
			for (int j = 0; j < spritesList.size(); j++) {
				spriteboxs[row][column] = spritesList.get(j);
				row++;
			}
			column++;
		}
		TeamBox teambox = PartnerBAC.getInstance().getTeamBox(0, leader.name, leader.num, 0, spriteboxs);
		return teambox;
	}
	
	/**
	 * ���׼��״̬
	 * @throws BACException 
	 */
	public void checkReady() throws BACException{
		Iterator<Member> members = memberTable.values().iterator();
		while (members.hasNext()) {
			Member member = members.next();
			if(!member.isReady){
				BACException.throwInstance("��Ա"+member.name+"��δ׼��");
			}
		}
	}
	
	/**
	 * ������Ϣ�������Ա
	 */
	public void sendMsgToTeam(short act, String info, int excludeid) throws Exception {
		Iterator<Integer> keys = memberTable.keySet().iterator();
		int[] pids = null;
		while (keys.hasNext()) {
			int id = keys.next();
			if(id != excludeid){
				pids = Tools.addToIntArr(pids, id);
			}
		}
		PushData.getInstance().sendPlaToSome(act, info, pids);
	}

	@Override
	public double getSortValue() {
		return this.num;
	}
	
	/**
	 * ��ȡ�����б���Ϣ
	 */
	public JSONArray getTeamInfo(){
		JSONArray jsonarr = new JSONArray();
		jsonarr.add(this.num);//������
		jsonarr.add(this.leader.name);//�ӳ�����
		jsonarr.add(this.lvLimit);//�ȼ�����
		jsonarr.add(this.memberTable.size());//��ǰ����
		return jsonarr;
	}
	
	/**
	 * ��ȡ�����Ա�б�
	 */
	public JSONArray getMemberList(){
		JSONArray jsonarr = new JSONArray();
		Iterator<Member> members = memberTable.values().iterator();
		while(members.hasNext()){
			jsonarr.add(members.next().getMemberInfo());
		}
		return jsonarr;
	}
	
}

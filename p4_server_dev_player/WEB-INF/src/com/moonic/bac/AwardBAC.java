package com.moonic.bac;

import org.json.JSONArray;
import org.json.JSONObject;

import server.common.Tools;

import com.ehc.common.SqlString;
import com.moonic.gamelog.GameLog;
import com.moonic.util.BACException;
import com.moonic.util.DBHelper;
import com.moonic.util.DBPaRs;
import com.moonic.util.MyTools;

/**
 * ����
 * @author John
 */
public class AwardBAC {
	
	/**
	 * ��ȡ����
	 */
	public JSONArray getAward(DBHelper dbHelper, int playerid, String contentStr, byte shortcut, int from, GameLog gl) throws Exception {
		try {
			return getAward(dbHelper, playerid, Tools.splitStrToIntArr2(contentStr, "|", ","), shortcut, from, gl);		
		} catch (Exception e) {
			System.out.println("contentStr= "+contentStr);
			throw e;
		}
	}
	
	/**
	 * ������
	 */
	public String tiyContent(String contentStr){
		int[][] content = tidyContent(Tools.splitStrToIntArr2(contentStr, "|", ","));
		StringBuffer sb = new StringBuffer();
		for(int i = 0; i < content.length; i++){
			if(sb.length() > 0){
				sb.append("|");
			}
			for(int j = 0; j < content[i].length; j++){
				if(j != 0){
					sb.append(",");
				}
				sb.append(content[i][j]);
			}
		}
		return sb.toString();
	}
	
	/**
	 * ������
	 */
	public int[][] tidyContent(int[][] content){
		int[][] new_content = null;
		for(int i = 0; i < content.length; i++){
			int type = content[i][0];
			if(type == 1){
				System.out.println("------ERROR------ TODO ������Ʒ����δ����");
				new_content = Tools.addToIntArr2(new_content, content[i]);
			} else 
			if(type == 5){
				System.out.println("------ERROR------ TODO �������Ǿ��飬��δ����");
				new_content = Tools.addToIntArr2(new_content, content[i]);
			} else 
			if(type == 6){
				System.out.println("------ERROR------ TODO �����¿�����δ����");
				new_content = Tools.addToIntArr2(new_content, content[i]);
			} else 
			if(type == 9){
				System.out.println("------ERROR------ TODO �����û�飬��δ����");
				new_content = Tools.addToIntArr2(new_content, content[i]);
			} else 
			{	
				boolean use = false;
				for(int j = 0; new_content != null && j < new_content.length; j++){
					if(type == new_content[j][0]){
						new_content[j][1] += content[i][1];
						use = true;
						break;
					}
				}
				if(!use){
					new_content = Tools.addToIntArr2(new_content, content[i]);
				}
			}
		}
		return new_content;
	}
	
	/**
	 * ��ȡ����
	 * @param content ����׼������ʽ���ڲ������м����ѡ�����
	 */
	public JSONArray getAward(DBHelper dbHelper, int playerid, int[][] content, byte shortcut, int from, GameLog gl) throws Exception {
		JSONArray itemarr = new JSONArray();
		JSONArray partnerarr = new JSONArray();
		int[][] itemContent = new int[content.length][];//��Ʒ����
		int itemIndex = -1;//��Ʒ�����±�
		int addEnergy = 0;//��ɫ����-����
		int addMoney = 0;//��ɫ-ͭǮ
		int addCoin = 0;//��ɫ-��
		int addJJCCoin = 0;//��ɫ����-������
		int addSoulPoint = 0;//��ɫ����-���
		int addFactionCon = 0;//��ɫ����-��ѫ
		int addTowerCoin = 0;//��ɫ����-����
		int addSummonprop = 0;//�ٻ�-����ٻ�����
		SqlString plaSqlStr = new SqlString();//��ɫ
		SqlString plaRoleSqlStr = new SqlString();//��ɫ����
		SqlString plaFacSqlStr = new SqlString();//��ɫ����
		SqlString plaSummonSqlStr = new SqlString();//�ٻ�
		DBPaRs plaRs = PlayerBAC.getInstance().getDataRs(playerid);
		for(int i = 0; content != null && i < content.length; i++){
			int type = content[i][0];
			if(type == 1){//��Ʒ
				boolean used = false;
				if(content[i].length == 4){//��׼��ʽͳ��
					for(int k = 0; k <= itemIndex; k++){
						if(itemContent[k].length != 4){
							continue;
						}
						if(content[i][1]!=itemContent[k][1] || content[i][2]!=itemContent[k][2]){
							continue;
						}
						itemContent[k][3] += content[i][3];
						used = true;
					}
				}
				if(!used){
					itemIndex++;
					itemContent[itemIndex] = content[i].clone();
				}
			} else 
			if(type == 2){//����
				addEnergy += content[i][1];
			} else 
			if(type == 3){//ͭǮ
				addMoney += content[i][1];
			} else 
			if(type == 4){//��
				addCoin += content[i][1];
			} else 
			if(type == 5){//���Ǿ���
				PlayerBAC.getInstance().addExp(dbHelper, playerid, content[i][1], gl);
			} else 
			if(type == 6){//�¿�
				TqBAC.getInstance().changeTQ(dbHelper, plaRs, playerid, 1, content[i][1], gl);
			} else 
			if(type == 8){//��ѫ
				addFactionCon += content[i][1];
			} else 
			if(type == 9){//��û��
				PartnerBAC.getInstance().obtainPartner(dbHelper, playerid, content[i][1], itemarr, partnerarr, gl);
			} else 
			if(type == 10){//��������
				addJJCCoin += content[i][1];
			} else 
			if(type == 12){//���
				addSoulPoint += content[i][1];
			} else 
			if(type == 14){//����
				addTowerCoin += content[i][1];
			} else 
			if(type == 15){//�ٻ�������
				addSummonprop +=  content[i][1];
			} else 
			{
				BACException.throwAndPrintInstance("����Ľ�����"+(new JSONArray(content[i])));
			}
		}
		for(int i = 0; i <= itemIndex; i++){
			int itemtype = itemContent[i][1];
			int itemnum = itemContent[i][2];
			int itemamount = itemContent[i][3];
			JSONArray extendarr = null;
			if(itemContent[i].length >= 5){
				extendarr = new JSONArray();
				for(int k = 4; k < itemContent[i].length; k++){
					extendarr.add(itemContent[i][k]);
				}
			}
			JSONArray thearr = ItemBAC.getInstance().add(dbHelper, playerid, itemtype, itemnum, itemamount, ItemBAC.ZONE_BAG, shortcut, extendarr, from, gl);
			MyTools.combJsonarr(itemarr, thearr);
		}
		if(addEnergy > 0){
			plaRoleSqlStr.addChange("energy", addEnergy);
		}
		if(addMoney > 0){
			plaSqlStr.addChange("money", addMoney);	
		}
		if(addCoin > 0){
			plaSqlStr.addChange("coin", addCoin);
		}
		if(addFactionCon > 0){
			plaFacSqlStr.addChange("factioncon", addFactionCon);
		}
		if(addJJCCoin > 0){
			plaRoleSqlStr.addChange("jjccoin", addJJCCoin);
		}
		if(addSoulPoint > 0){
			plaRoleSqlStr.addChange("soulpoint", addSoulPoint);
		}
		if(addTowerCoin > 0){
			plaRoleSqlStr.addChange("towercoin", addTowerCoin);
		}
		if(addSummonprop > 0){
			plaSummonSqlStr.addChange("summonprop", addSummonprop);
		}
		if(plaSqlStr.getColCount() > 0){
			gl.addChaNote(GameLog.TYPE_MONEY, plaRs.getInt("money"), addMoney);
			gl.addChaNote(GameLog.TYPE_COIN, plaRs.getInt("coin"), addCoin);
			PlayerBAC.getInstance().update(dbHelper, playerid, plaSqlStr);
		}
		if(plaRoleSqlStr.getColCount() > 0){
			DBPaRs plaroleRs = PlaRoleBAC.getInstance().getDataRs(playerid);
			gl.addChaNote("����", plaroleRs.getInt("energy"), addEnergy);
			gl.addChaNote("������", plaroleRs.getInt("jjccoin"), addJJCCoin);
			gl.addChaNote("���", plaroleRs.getInt("soulpoint"), addSoulPoint);
			gl.addChaNote("����", plaroleRs.getInt("towercoin"), addTowerCoin);
			PlaRoleBAC.getInstance().update(dbHelper, playerid, plaRoleSqlStr);
		}
		if(plaFacSqlStr.getColCount() > 0){
			DBPaRs plafacRs = PlaFacBAC.getInstance().getDataRs(playerid);
			gl.addChaNote("���ɹ�ѫ", plafacRs.getInt("factioncon"), addFactionCon);
			PlaFacBAC.getInstance().update(dbHelper, playerid, plaFacSqlStr);		
		}
		if(plaSummonSqlStr.getColCount() > 0){
			DBPaRs plasumRs = PlaSummonBAC.getInstance().getDataRs(playerid);
			gl.addChaNote("����ٻ�����", plasumRs.getInt("summonprop"), addSummonprop);
			PlaSummonBAC.getInstance().update(dbHelper, playerid, plaSummonSqlStr);
		}
		gl.addItemChaNoteArr(itemarr);
		JSONArray returnarr = new JSONArray();
		returnarr.add(itemarr);//��Ʒ�仯
		returnarr.add(partnerarr);//��û��
		return returnarr;
	}
	
	/**
	 * ��������
	 */
	public void toPush(JSONArray awardarr, PushInterface pushinterface) throws Exception {
		if(awardarr == null){
			return;
		}
		JSONArray itemawardarr = awardarr.optJSONArray(0);//��Ʒ����������Ϣ
		for(int i = 0; i < itemawardarr.size(); i++){
			JSONObject itemawardobj = itemawardarr.optJSONObject(i);
			DBPaRs itemRs = ItemBAC.getInstance().getListRs(itemawardobj.optInt("type"), itemawardobj.optInt("num"));//��Ʒ���ݱ���Ϣ
			int itemrare = itemRs.getInt("rare");
			pushinterface.push(itemRs.getString("name"), itemrare);
		}
	}
	
	/**
	 * ���ͽӿ�
	 */
	public static interface PushInterface {
		public void push(String itemname, int itemrare) throws Exception ;
	}
	
	//--------------��̬��--------------
	
	private static AwardBAC instance = new AwardBAC();
	
	/**
	 * ��ȡʵ��
	 */
	public static AwardBAC getInstance(){
		return instance;
	}
}

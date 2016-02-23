package com.moonic.bac;

import org.json.JSONArray;
import org.json.JSONObject;

import server.common.Tools;

import com.ehc.common.ReturnValue;
import com.ehc.common.SqlString;
import com.moonic.battle.BattleBox;
import com.moonic.gamelog.GameLog;
import com.moonic.servlet.GameServlet;
import com.moonic.util.BACException;
import com.moonic.util.DBHelper;
import com.moonic.util.DBPaRs;
import com.moonic.util.MyTools;

/**
 * ��ɫ�������
 * @author wkc
 */
public class PlaTrialPartnerBAC extends PlaTrialBAC{
	public static final String tab_trial_partner = "tab_trial_partner";
	
	/**
	 * ����
	 */
	public PlaTrialPartnerBAC() {
		super(tab_trial_partner);
	}
	
	/**
	 * ��ʼ������
	 */
	public void init(DBHelper dbHelper, int playerid, Object... parm) throws Exception {
		SqlString sqlStr = new SqlString();
		sqlStr.add("partnertimes", new JSONObject().toString());
		PlaRoleBAC.getInstance().update(dbHelper, playerid, sqlStr);
	}
	
	/**
	 * ��ʼ����
	 */
	public ReturnValue startTrial(int playerid, int num, String posStr){
		try {
			DBPaRs trialListRs = getTrialListRs(num);
			boolean isOpen = false;//������ȫ��
			String paramStr = CustomActivityBAC.getInstance().getFuncActiPara(CustomActivityBAC.TYPE_TRIAL_PARTNER_OPEN);
			if(paramStr != null){
				int param = Tools.str2int(paramStr);
				if(param == 0){
					isOpen = true;
				} 
			} 
			if(!isOpen){
				int[] weekarr = Tools.splitStrToIntArr(trialListRs.getString("weekday"), ",");
				if(!Tools.contain(weekarr, MyTools.getWeek())){
					BACException.throwInstance("���ղ��ܽ��д�����");
				}
			}
			
			GameLog gl = GameLog.getInst(playerid, GameServlet.ACT_PLA_TRIAL_PARTNER_START);
			BattleBox battleBox = start(playerid, TYPE_PARTNER, num, posStr, gl);
			
			gl.save();
			return new ReturnValue(true, battleBox.getJSONArray().toString());
		} catch(Exception e) {
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		} 
	}
	
	/**
	 * ��������
	 */
	public ReturnValue endTrial(int playerid, int num, String battleRecord){
		DBHelper dbHelper = new DBHelper();
		try {
			dbHelper.openConnection();
			GameLog gl = GameLog.getInst(playerid, GameServlet.ACT_PLA_TRIAL_PARTNER_END);
			JSONArray jsonarr = end(dbHelper, playerid, num, battleRecord, gl);
			
			gl.save();
			return new ReturnValue(true, jsonarr.toString());
		} catch(Exception e) {
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		} finally{
			dbHelper.closeConnection();
		}
	}
	
	@Override
	public JSONArray endHandle(DBHelper dbHelper, int playerid, int num, int star, GameLog gl) throws Exception {
		DBPaRs trialListRs = getTrialListRs(num);
		StringBuffer remarkSb = new StringBuffer();
		remarkSb.append("����");
		remarkSb.append(GameLog.formatNameID(trialListRs.getString("name"), num));
		StringBuffer awardSb = new StringBuffer();
		JSONArray exparr = new JSONArray();
		if(star > 0){//ʤ��
			String[] awardarr = Tools.splitStr(trialListRs.getString("award"), "|");
			int[][] amarr = Tools.splitStrToIntArr2(trialListRs.getString("star"+star), "|", ",");
			int[] oddsarr = Tools.splitStrToIntArr(trialListRs.getString("odds"+star), ",");
			for (int i = 0; i < awardarr.length; i++) {
				int random = MyTools.getRandom(1, 100);
				if(random < oddsarr[i]){
					if(i > 0){
						awardSb.append("|");
					}
					awardSb.append(awardarr[i]);
					awardSb.append(",");
					int amount = MyTools.getRandom(amarr[i][0], amarr[i][1]);
					String paramStr = CustomActivityBAC.getInstance().getFuncActiPara(CustomActivityBAC.TYPE_TRIAL_PARTNER_MUL);
					if(paramStr != null){
						int param = Tools.str2int(paramStr);
						amount *= param;
					} 
					awardSb.append(amount);
				}
			}
			exparr = getExpAward(dbHelper, playerid, num, gl);
			int money = trialListRs.getInt("money");
			exparr.add(money);
			PlayerBAC.getInstance().addValue(dbHelper, playerid, "money", money, gl, GameLog.TYPE_MONEY);
			remarkSb.append("�ɹ����Ǽ�Ϊ"+star);
			addTrialTimes(dbHelper, playerid, gl, TYPE_PARTNER, trialListRs.getByte("type"));
		} else{
			remarkSb.append("ʧ��");
		}
		gl.addRemark(remarkSb);
		JSONArray awardarr = AwardBAC.getInstance().getAward(dbHelper, playerid, awardSb.toString(), ItemBAC.SHORTCUT_MAIL, 1, gl);
		PlaWelfareBAC.getInstance().updateTaskProgress(dbHelper, playerid, PlaWelfareBAC.TYPE_TRIAL_PARTNER, gl);
		JSONArray returnarr = new JSONArray();
		returnarr.add(awardSb.toString());
		returnarr.add(awardarr);
		returnarr.add(exparr);
		returnarr.add(star);//�Ǽ�
		return returnarr;
	}
	
	
	/**
	 * ��ȡ����
	 */
	public JSONObject getData(int playerid) throws Exception {
		DBPaRs plaTrialRs = PlaRoleBAC.getInstance().getDataRs(playerid);
		if(!plaTrialRs.exist()){
			return null;
		}
		return new JSONObject(plaTrialRs.getString("partnertimes"));
	}
	
	//------------------��̬��------------------
	
	private static PlaTrialPartnerBAC instance = new PlaTrialPartnerBAC();
	
	public static PlaTrialPartnerBAC getInstance(){
		return instance;
	}

}

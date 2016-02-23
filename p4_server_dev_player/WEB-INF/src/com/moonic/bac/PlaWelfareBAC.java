package com.moonic.bac;

import java.util.Calendar;

import org.json.JSONArray;
import org.json.JSONObject;

import com.ehc.common.ReturnValue;
import com.ehc.common.SqlString;
import com.moonic.gamelog.GameLog;
import com.moonic.servlet.GameServlet;
import com.moonic.util.BACException;
import com.moonic.util.DBHelper;
import com.moonic.util.DBPaRs;
import com.moonic.util.DBPool;
import com.moonic.util.DBPsRs;
import com.moonic.util.MyTools;

import server.common.Tools;

/**
 * ��ɫ�������ճ���
 * @author wkc
 */
public class PlaWelfareBAC extends PlaBAC{
	public static final String tab_daily_task = "tab_daily_task";
	public static final String tab_achievement = "tab_achievement";
	public static final String tab_target = "tab_target";
	public static final String tab_checkin = "tab_checkin";
	public static final String tab_checkin_spe = "tab_checkin_spe";
	public static final String tab_checkin_total = "tab_checkin_total";
	
	public static final byte TYPE_MONTHCARD = 1;//�¿�����Ч����
	public static final byte TYPE_COPYMAP_ORDINARY = 2;//��ͨ����ͨ�ش���
	public static final byte TYPE_COPYMAP_ELITE = 3;//��Ӣ����ͨ�ش���
	public static final byte TYPE_TRIAL_MONEY = 4;//ͭǮ��������
	public static final byte TYPE_JJC = 5;//����������
	public static final byte TYPE_TRIAL_EXP = 6;//������������
	public static final byte TYPE_PARTNER_LV_UP = 7;//�������
	public static final byte TYPE_PARTNER_SKILL_UP = 8;//��鼼������
	public static final byte TYPE_BUY_ENERGY = 9;//����������
	public static final byte TYPE_BUY_MONEY = 10;//��ͭǮ����
	public static final byte TYPE_CALL_PARTNER = 11;//����ٻ�����
	public static final byte TYPE_CALL_EQUIP = 12;//װ���ٻ�����
	public static final byte TYPE_TOWER = 13;//�ֻ�������
	public static final byte TYPE_TEAMCM = 14;//�ű�ս������
	public static final byte TYPE_CITYBATTLE = 15;//��ս����
	public static final byte TYPE_WORSHIP = 16;//����Ĥ�ݴ���
	public static final byte TYPE_TRIAL_PARTNER = 17;//�����������
	public static final byte TYPE_INSTRUMENT = 18;//��������ע�����
	public static final byte TYPE_TAKE_ENERGY_NOON = 19;//������������12��~14�㣩
	public static final byte TYPE_TAKE_ENERGY_NIGHT = 20;//������������18��~20�㣩
	public static final byte TYPE_EQUIP_STRENGTHEN = 21;//װ��ǿ������
	
	public static final byte ACHIEVE_PARTER_AM = 1;//ӵ��X�����
	public static final byte ACHIEVE_COPYMAP_PASS = 2; //ͨ�ر��X�ĸ���Y��
	public static final byte ACHIEVE_PARTNER_QUALITY = 3;//X���������YƷ��
	public static final byte ACHIEVE_PLV = 4;//�����ȼ��ﵽX��
	public static final byte ACHIEVE_VIP = 5;//VIP�ﵽX��
	public static final byte ACHIEVE_PARTNER_WAKE = 6;//X��������
	public static final byte ACHIEVE_FACCM_NUM = 7;//ͨ�ر��X���ŶӸ�����ͼ
	public static final byte ACHIEVE_FACCM_TIMES = 8;//�ۻ������ŶӸ���ս��X��
	
	public static final byte TAREGT_LV = 1;//�ﵽָ���ȼ�
	public static final byte TAREGT_COPYMAP = 2; //ͨ��ָ������
	public static final byte TAREGT_TIMEDAY = 3;//��Ϸʱ��ﵽָ��������
	
	/**
	 * ����
	 */
	public PlaWelfareBAC() {
		super("tab_pla_welfare", "playerid");
	}
	
	/**
	 * ��ʼ��Ŀ������
	 */
	public void init(DBHelper dbHelper, int playerid, Object... parm) throws Exception {
		SqlString sqlStr = new SqlString();
		sqlStr.add("playerid", playerid);
		sqlStr.add("taskdata", new JSONObject().toString());
		sqlStr.add("taskaward", new JSONArray().toString());
		sqlStr.add("achievedata", new JSONObject().toString());
		sqlStr.add("achieveaward", new JSONArray().toString());
		sqlStr.add("checkin", 0);
		sqlStr.add("checkintotal", 0);
		sqlStr.add("checkinaward", new JSONArray().toString());
		sqlStr.add("ischecked", 0);
		sqlStr.add("targetaward", new JSONArray().toString());
		insert(dbHelper, playerid, sqlStr);
	}
	
	/**
	 * ��ȡ�ճ�������
	 * @param num ������
	 */
	public ReturnValue getTaskAward(int playerid, int num){
		DBHelper dbHelper = new DBHelper();
		try {
			DBPaRs taskListRs = DBPool.getInst().pQueryA(tab_daily_task, "num="+num);
			if(!taskListRs.exist()){
				BACException.throwInstance("�����ڵ�������"+num);
			}
			DBPaRs plaWelRs = getDataRs(playerid);
			JSONArray jsonarr = new JSONArray(plaWelRs.getString("taskaward"));
			if(jsonarr.contains(num)){
				BACException.throwInstance("������������ȡ");
			}
			int[] conarr = Tools.splitStrToIntArr(taskListRs.getString("finish"), ",");
			checkTaskCondition(playerid, conarr);
			dbHelper.openConnection();
			GameLog gl = GameLog.getInst(playerid, GameServlet.ACT_PLA_WELFARE_GETTASKAWARD);
			jsonarr.add(num);
			SqlString sqlStr = new SqlString();
			sqlStr.add("taskaward", jsonarr.toString());
			update(dbHelper, playerid, sqlStr);
			String award = taskListRs.getString("award");
			JSONArray awardarr = AwardBAC.getInstance().getAward(dbHelper, playerid, award, ItemBAC.SHORTCUT_MAIL, 1, gl);
			CustomActivityBAC.getInstance().updateProcess(dbHelper, playerid, 4);
			
			gl.addRemark("��ȡ���������Ϊ"+num);
			gl.save();
			return new ReturnValue(true, awardarr.toString());
		} catch (Exception e) {
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		} finally {
			dbHelper.closeConnection();
		}
	}
	
	/**
	 * һ����ȡ�ճ�������
	 * @param num ������
	 */
	public ReturnValue getTaskAwardOneKey(int playerid, String numStr){
		DBHelper dbHelper = new DBHelper();
		try {
			JSONArray numArr = new JSONArray(numStr);
			if(numArr.length() == 0){
				BACException.throwInstance("�������Ϊ��");
			}
			DBPaRs plaWelRs = getDataRs(playerid);
			JSONArray jsonarr = new JSONArray(plaWelRs.getString("taskaward"));
			StringBuffer awardSb = new StringBuffer();
			StringBuffer remarkSb = new StringBuffer();
			remarkSb.append("һ����ȡ��������ţ�");
			for (int i = 0; i < numArr.length(); i++) {
				int num = numArr.optInt(i);
				DBPaRs taskListRs = DBPool.getInst().pQueryA(tab_daily_task, "num="+num);
				if(!taskListRs.exist()){
					BACException.throwInstance("�����ڵ�������"+num);
				}
				if(jsonarr.contains(num)){
					BACException.throwInstance("������������ȡ");
				}
				int[] conarr = Tools.splitStrToIntArr(taskListRs.getString("finish"), ",");
				checkTaskCondition(playerid, conarr);
				if(!awardSb.toString().equals("")){
					awardSb.append("|");
				}
				awardSb.append(taskListRs.getString("award"));
				jsonarr.add(num);
				remarkSb.append(num+",");
			}
			dbHelper.openConnection();
			GameLog gl = GameLog.getInst(playerid, GameServlet.ACT_PLA_WELFARE_GETTASKAWARD_ONEKEY);
			SqlString sqlStr = new SqlString();
			sqlStr.add("taskaward", jsonarr.toString());
			update(dbHelper, playerid, sqlStr);
			JSONArray awardarr = AwardBAC.getInstance().getAward(dbHelper, playerid, awardSb.toString(), ItemBAC.SHORTCUT_MAIL, 1, gl);
			CustomActivityBAC.getInstance().updateProcess(dbHelper, playerid, 4, numArr.length());
			
			gl.addRemark(remarkSb);
			gl.save();
			return new ReturnValue(true, awardarr.toString());
		} catch (Exception e) {
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		} finally {
			dbHelper.closeConnection();
		}
	}
	
	/**
	 * ��ȡ�ɾͽ���
	 * @param num �ɾͱ��
	 */
	public ReturnValue getAchievementAward(int playerid, int num){
		DBHelper dbHelper = new DBHelper();
		try {
			DBPaRs achieveListRs = DBPool.getInst().pQueryA(tab_achievement, "num="+num);
			if(!achieveListRs.exist()){
				BACException.throwInstance("�����ڵĳɾͱ��"+num);
			}
			DBPaRs plaWelRs = getDataRs(playerid);
			JSONArray jsonarr = new JSONArray(plaWelRs.getString("achieveaward"));
			if(jsonarr.contains(num)){
				BACException.throwInstance("�˳ɾͽ�������ȡ");
			}
			int[] needarr = Tools.splitStrToIntArr(achieveListRs.getString("finish"), ",");
			checkAchevimentConditon(playerid, needarr);
			GameLog gl = GameLog.getInst(playerid, GameServlet.ACT_PLA_WELFARE_GETACHIEVEAWARD);
			dbHelper.openConnection();
			String award = achieveListRs.getString("award");
			JSONArray awardarr = AwardBAC.getInstance().getAward(dbHelper, playerid, award, ItemBAC.SHORTCUT_MAIL, 1, gl);
			jsonarr.add(num);
			SqlString sqlStr = new SqlString();
			sqlStr.add("achieveaward", jsonarr.toString());
			update(dbHelper, playerid, sqlStr);
			
			gl.addRemark("��ȡ�ɾͽ�����ţ�"+num);
			gl.save();
			return new ReturnValue(true, awardarr.toString());
		} catch (Exception e) {
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		} finally {
			dbHelper.closeConnection();
		}
	}
	
	/**
	 * һ����ȡ�ɾͽ���
	 * @param num �ɾͱ��
	 */
	public ReturnValue getAchievementAwardOneKey(int playerid, String numStr){
		DBHelper dbHelper = new DBHelper();
		try {
			JSONArray numArr = new JSONArray(numStr);
			if(numArr.length() == 0){
				BACException.throwInstance("�������Ϊ��");
			}
			StringBuffer awardSb = new StringBuffer();
			StringBuffer remarkSb = new StringBuffer();
			remarkSb.append("һ����ȡ�ɾͽ�����ţ�");
			DBPaRs plaWelRs = getDataRs(playerid);
			JSONArray jsonarr = new JSONArray(plaWelRs.getString("achieveaward"));
			for(int i = 0; i < numArr.length(); i++){
				int num = numArr.optInt(i);
				DBPaRs achieveListRs = DBPool.getInst().pQueryA(tab_achievement, "num="+num);
				if(!achieveListRs.exist()){
					BACException.throwInstance("�����ڵĳɾͱ��"+num);
				}
				if(jsonarr.contains(num)){
					BACException.throwInstance("�˳ɾͽ�������ȡ");
				}
				int[] needarr = Tools.splitStrToIntArr(achieveListRs.getString("finish"), ",");
				checkAchevimentConditon(playerid, needarr);
				if(!awardSb.toString().equals("")){
					awardSb.append("|");
				}
				awardSb.append(achieveListRs.getString("award"));
				jsonarr.add(num);
				remarkSb.append(num+",");
			}
			GameLog gl = GameLog.getInst(playerid, GameServlet.ACT_PLA_WELFARE_GETACHIEVEAWARD_ONEKEY);
			dbHelper.openConnection();
			JSONArray awardarr = AwardBAC.getInstance().getAward(dbHelper, playerid, awardSb.toString(), ItemBAC.SHORTCUT_MAIL, 1, gl);
			SqlString sqlStr = new SqlString();
			sqlStr.add("achieveaward", jsonarr.toString());
			update(dbHelper, playerid, sqlStr);
			
			gl.addRemark(remarkSb);
			gl.save();
			return new ReturnValue(true, awardarr.toString());
		} catch (Exception e) {
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		} finally {
			dbHelper.closeConnection();
		}
	}
	
	/**
	 * ǩ��
	 */
	public ReturnValue checkin(int playerid){
		DBHelper dbHelper = new DBHelper();
		try {
			DBPaRs plaWelRs = getDataRs(playerid);
			int checkedAm = plaWelRs.getInt("ischecked");
			if(checkedAm == 2){
				BACException.throwInstance("������ǩ��");
			} 
			int checkDays = plaWelRs.getInt("checkin");
			int viplv = PlayerBAC.getInstance().getIntValue(playerid, "vip");
			DBPaRs checkinList = null;
			if(checkedAm == 0){
				checkinList = DBPool.getInst().pQueryA(tab_checkin, "num="+(checkDays+1));
				if(!checkinList.exist()){
					BACException.throwInstance("ǩ������������");
				}
			} else
			if(checkedAm == 1){
				checkinList = DBPool.getInst().pQueryA(tab_checkin, "num="+(checkDays));
				int[] ratearr = Tools.splitStrToIntArr(checkinList.getString("vip"), ",");
				if(ratearr[0] == 0){
					BACException.throwInstance("����ֻ��ǩһ��");
				} else{
					if(viplv < ratearr[0]){
						BACException.throwInstance("VIP�ȼ�����������");
					}
				}
			}
			int[] ratearr = Tools.splitStrToIntArr(checkinList.getString("vip"), ",");
			int rate = 1;//����
			if(ratearr[0] > 0 && viplv >= ratearr[0]){
				if(checkedAm == 0){
					rate = ratearr[1];
				} else{
					rate = ratearr[1] - 1;
				}
			} 
			int[][] award = Tools.splitStrToIntArr2(checkinList.getString("award"), "|", ",");
			if(award[0][0] >= 100){
				int year =  Calendar.getInstance().get(Calendar.YEAR);
				int month = Calendar.getInstance().get(Calendar.MONTH)+1;
				DBPaRs speListRs = DBPool.getInst().pQueryA(tab_checkin_spe, "year="+year+" and month="+month);
				award = Tools.splitStrToIntArr2(speListRs.getString("award"+(award[0][0]-100)), "|", ",");
			} 
			for(int i = 0; i < award.length; i++){
				if(award[i][0] == 1){
					award[i][3] *= rate;
				} else
				if(award[i][0] == 4){
					award[i][1] *= rate;
				}
			}
			dbHelper.openConnection();
			GameLog gl = GameLog.getInst(playerid, GameServlet.ACT_PLA_WELFARE_CHECKIN);
			JSONArray awardarr = AwardBAC.getInstance().getAward(dbHelper, playerid, award, ItemBAC.SHORTCUT_MAIL, 1, gl);
			SqlString sqlStr = new SqlString();
			if(checkedAm == 0){
				sqlStr.addChange("checkin", 1);
				sqlStr.addChange("checkintotal", 1);
			} 
			if(checkedAm == 0 && rate > 1){
				sqlStr.add("ischecked", 2);
			} else{
				sqlStr.addChange("ischecked", 1);
			}
			update(dbHelper, playerid, sqlStr);
			
			gl.addRemark("�������ʣ�"+rate);
			gl.save();
			return new ReturnValue(true, awardarr.toString());
		} catch (Exception e){
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		} finally {
			dbHelper.closeConnection();
		}
	}

	/**
	 * ��ȡǩ���ۻ�����
	 */
	public ReturnValue getCheckinAward(int playerid, int num){
		DBHelper dbHelper = new DBHelper();
		try {
			DBPaRs totalListRs = DBPool.getInst().pQueryA(tab_checkin_total, "num="+num);
			if(!totalListRs.exist()){
				BACException.throwInstance("ǩ���ۻ�������Ų�����"+num);
			}
			DBPaRs plaWelRs = getDataRs(playerid);
			JSONArray checkinawardarr = new JSONArray(plaWelRs.getString("checkinaward"));
			if(checkinawardarr.contains(num)){
				BACException.throwInstance("��ǩ���ۻ���������ȡ");
			}
			int total = plaWelRs.getInt("checkintotal");
			int days = totalListRs.getInt("days");
			if(total < days){
				BACException.throwInstance("�ۻ�ǩ����������");
			}
			String award = totalListRs.getString("award");
			dbHelper.openConnection();
			GameLog gl = GameLog.getInst(playerid, GameServlet.ACT_PLA_WELFARE_GETCHECKINAWARD);
			JSONArray awardarr = AwardBAC.getInstance().getAward(dbHelper, playerid, award, ItemBAC.SHORTCUT_MAIL, 1, gl);
			checkinawardarr.add(num);
			SqlString sqlStr = new SqlString();
			sqlStr.add("checkinaward", checkinawardarr.toString());
			update(dbHelper, playerid, sqlStr);
			
			gl.addRemark("��ȡ�ۻ�ǩ�����������"+num+"��");
			gl.save();
			return new ReturnValue(true, awardarr.toString());
		} catch (Exception e){
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		} finally {
			dbHelper.closeConnection();
		}
	}
	
	/**
	 * ��ȡĿ�꽱��
	 */
	public ReturnValue gerTargetAward(int playerid, int num){
		DBHelper dbHelper = new DBHelper();
		try {
			DBPaRs targetListRs = DBPool.getInst().pQueryA(tab_target, "num="+num);
			if(!targetListRs.exist()){
				BACException.throwInstance("Ŀ�꽱����Ų�����"+num);
			}
			DBPaRs plaWelRs = getDataRs(playerid);
			JSONArray targetawardarr = new JSONArray(plaWelRs.getString("targetaward"));
			if(targetawardarr.contains(num)){
				BACException.throwInstance("��Ŀ�꽱������ȡ");
			}
			int[] conarr = Tools.splitStrToIntArr(targetListRs.getString("condition"), ",");
			checkTargetCondition(playerid, conarr);;
			String award = targetListRs.getString("award");
			dbHelper.openConnection();
			GameLog gl = GameLog.getInst(playerid, GameServlet.ACT_PLA_WELFARE_GETTARGETAWARD);
			JSONArray awardarr = AwardBAC.getInstance().getAward(dbHelper, playerid, award, ItemBAC.SHORTCUT_MAIL, 1, gl);
			targetawardarr.add(num);
			SqlString sqlStr = new SqlString();
			sqlStr.add("targetaward", targetawardarr.toString());
			update(dbHelper, playerid, sqlStr);
			
			gl.addRemark("��ȡĿ�꽱�������"+num+"��");
			gl.save();
			return new ReturnValue(true, awardarr.toString());
		} catch (Exception e){
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		} finally {
			dbHelper.closeConnection();
		}
	}
	
	
	/**
	 * ������������Ƿ�����
	 */
	public void checkTaskCondition(int playerid, int[] conarr) throws Exception{
		if(conarr[0] == TYPE_MONTHCARD){
			int tqnum = TqBAC.getInstance().getTQNum(playerid);
			if(tqnum == 0){
				BACException.throwInstance("�¿�������Ч��");
			}
		} else
		if(conarr[0] == TYPE_TAKE_ENERGY_NOON){
			long current = System.currentTimeMillis();
			if(current < MyTools.getCurrentDateLong() + MyTools.long_hour*12 || current > MyTools.getCurrentDateLong() + MyTools.long_hour*14){
				BACException.throwInstance("������ȡʱ�䣺12:00~14:00");
			}
		} else
		if(conarr[0] == TYPE_TAKE_ENERGY_NIGHT){
			long current = System.currentTimeMillis();
			if(current < MyTools.getCurrentDateLong() + MyTools.long_hour*18 || current > MyTools.getCurrentDateLong() + MyTools.long_hour*20){
				BACException.throwInstance("������ȡʱ�䣺18:00~20:00");
			}
		} else{
			DBPaRs plaWelRs = getDataRs(playerid);
			JSONObject taskobj = new JSONObject(plaWelRs.getString("taskdata"));
			if(taskobj.optInt(String.valueOf(conarr[0])) < conarr[1]){
				BACException.throwInstance("��������δ���");
			}
		}
	}
	
	/**
	 * ���ɾ������Ƿ�����
	 */
	public void checkAchevimentConditon(int playerid, int[] needarr) throws Exception{
		int type = needarr[0];//����
		if(type == ACHIEVE_PARTER_AM){//ӵ��X�����
			DBPsRs partnerRs = PartnerBAC.getInstance().query(playerid, "playerid="+playerid);
			int count = partnerRs.count();
			if(count < needarr[1]){
				BACException.throwInstance("����������������");
			}
		} else
		if(type == ACHIEVE_COPYMAP_PASS){//ͨ�ر��X�ĸ���Y��
			int passAm = CopymapBAC.getInstance().getPassedAm(playerid, needarr[1]);
			if(passAm < needarr[2]){
				BACException.throwInstance("ͨ����������������");
			}
		} else
		if(type == ACHIEVE_PARTNER_QUALITY){//X���������YƷ��
			DBPsRs partnerRs = PartnerBAC.getInstance().query(playerid, "playerid="+playerid+" and phase>="+needarr[2]);
			int count = partnerRs.count();
			if(count < needarr[1]){
				BACException.throwInstance("ָ��Ʒ�ʻ���������������");
			}
		} else
		if(type == ACHIEVE_PLV){//�����ȼ��ﵽX��
			int playerlv = PlayerBAC.getInstance().getIntValue(playerid, "lv");
			if(playerlv < needarr[1]){
				BACException.throwInstance("�����ȼ�����������");
			}
		} else
		if(type == ACHIEVE_VIP){//VIP�ﵽX��
			int viplv = PlayerBAC.getInstance().getIntValue(playerid, "vip");
			if(viplv < needarr[1]){
				BACException.throwInstance("VIP�ȼ�����������");
			}
		} else
		if(type == ACHIEVE_PARTNER_WAKE){//X��������
			DBPsRs partnerRs = PartnerBAC.getInstance().query(playerid, "playerid="+playerid+" and awaken="+1);
			int count = partnerRs.count();
			if(count < needarr[1]){
				BACException.throwInstance("���ѻ���������������");
			}
		} else{
			boolean success = false;
			DBPaRs plaWelRs = getDataRs(playerid);
			JSONObject achieveObj = new JSONObject(plaWelRs.getString("achievedata"));
			if(type == ACHIEVE_FACCM_NUM){
				JSONArray numarr = achieveObj.optJSONArray(String.valueOf(type));
				if(numarr != null && numarr.contains(needarr[1])){
					success = true;
				}
			} else{
				if(achieveObj.optInt(String.valueOf(type)) >= needarr[1]){
					success = true;
				}
			}
			if(!success){
				BACException.throwInstance("�˳ɾ���δ���");
			}
		}
	}
	
	/**
	 * ���Ŀ�������Ƿ�����
	 */
	public void checkTargetCondition(int playerid, int[] conarr) throws Exception{
		if(conarr[0] == TAREGT_LV){
			int playerlv = PlayerBAC.getInstance().getIntValue(playerid, "lv");
			if(playerlv < conarr[1]){
				BACException.throwInstance("�����ȼ�����������");
			}
		} else
		if(conarr[0] == TAREGT_COPYMAP){
			int passAm = CopymapBAC.getInstance().getPassedAm(playerid, conarr[1]);
			if(passAm == 0){
				BACException.throwInstance("��δͨ������"+conarr[1]);
			}
		} else
		if(conarr[0] == TAREGT_TIMEDAY){
			DBPaRs plaRs = PlayerBAC.getInstance().getDataRs(playerid);
			long createtime = plaRs.getTime("savetime");
			if(MyTools.getCurrentDateLong() - MyTools.getCurrentDateLong(createtime) < (conarr[1]-1)*MyTools.long_day){
				BACException.throwInstance("��Ϸʱ��δ�ﵽ");
			}
		} 
	}
	
	/**
	 * �����������
	 */
	public void updateTaskProgress(DBHelper dbHelper, int playerid, byte type, GameLog gl) throws Exception{
		updateTaskProgress(dbHelper, playerid, type, 1, gl);
	}
	
	/**
	 * �����������
	 */
	public void updateTaskProgress(DBHelper dbHelper, int playerid, byte type, int amount, GameLog gl) throws Exception{
		int need = 0;
		DBPsRs taskListRs = DBPool.getInst().pQueryS(tab_daily_task);
		while(taskListRs.next()){
			String finish = taskListRs.getString("finish");
			int[] conarr = Tools.splitStrToIntArr(finish, ",");
			if(conarr[0] == type){
				need = conarr[1];
				break;
			}
		}
		if(need == 0){
			return;
		}
		DBPaRs plaWelRs = getDataRs(playerid);
		JSONObject taskobj = new JSONObject(plaWelRs.getString("taskdata"));
		int currAm = taskobj.optInt(String.valueOf(type));
		if(currAm >= need){
			return;
		} else{
			taskobj.put(String.valueOf(type), currAm + amount);
			SqlString sqlStr = new SqlString();
			sqlStr.add("taskdata", taskobj.toString());
			update(dbHelper, playerid, sqlStr);
			gl.addRemark("�������ͣ�"+type+"������ɴ����仯��"+currAm+"->"+(currAm+amount));
		}
	}
	
	/**
	 * ���³ɾͽ���
	 */
	public void updateAchieveProgress(DBHelper dbHelper, int playerid, byte type, GameLog gl) throws Exception{
		updateAchieveProgress(dbHelper, playerid, type, 1, gl);
	}
	
	/**
	 * ���³ɾͽ���
	 */
	public void updateAchieveProgress(DBHelper dbHelper, int playerid, byte type, int param, GameLog gl) throws Exception{
		DBPaRs plaWelRs = getDataRs(playerid);
		JSONObject achieveobj = new JSONObject(plaWelRs.getString("achievedata"));
		StringBuffer remarkSb = new StringBuffer();
		remarkSb.append("�ɾ����ͣ�"+type+"��");
		if(type == ACHIEVE_FACCM_NUM){//ͨ�ذ��ɸ�����¼���
			JSONArray numarr = achieveobj.optJSONArray(String.valueOf(type));
			if(numarr == null){
				numarr = new JSONArray();
			}
			if(numarr.contains(param)){
				return;
			}
			numarr.add(param);
			achieveobj.put(String.valueOf(type), numarr);
			remarkSb.append("������ɸ������"+param);
		} else{
			int maxNeed = 0;//������������������
			DBPsRs achievListRs = DBPool.getInst().pQueryS(tab_achievement);
			while(achievListRs.next()){
				String finish = achievListRs.getString("finish");
				int[] conarr = Tools.splitStrToIntArr(finish, ",");
				if(conarr[0] == type && maxNeed < conarr[1]){
					maxNeed = conarr[1];
				}
			}
			if(maxNeed == 0){
				return;
			}
			int currAm = achieveobj.optInt(String.valueOf(type));
			if(currAm >= maxNeed){
				return;
			} else{
				achieveobj.put(String.valueOf(type), currAm + param);
				remarkSb.append("��ɽ��ȱ仯��"+currAm+"->"+achieveobj.optInt(String.valueOf(type)));
			}
		}
		SqlString sqlStr = new SqlString();
		sqlStr.add("achievedata", achieveobj.toString());
		update(dbHelper, playerid, sqlStr);
		gl.addRemark(remarkSb);
	}
	
	/**
	 * ���ý�ɫ��������
	 */
	public void resetData(DBHelper dbHelper, int playerid, boolean moonReset) throws Exception {
		SqlString sqlStr = new SqlString();
		sqlStr.add("taskdata", new JSONObject().toString());
		sqlStr.add("taskaward", new JSONArray().toString());
		if(moonReset){
			sqlStr.add("checkin", 0);
		}
		sqlStr.add("ischecked", 0);
		update(dbHelper, playerid, sqlStr);
	}
	
	/**
	 * ��ȡ����
	 */
	public JSONArray getData(int playerid) throws Exception {
		DBPaRs plaWelRs = getDataRs(playerid);
		JSONArray jsonarr = new JSONArray();
		jsonarr.add(new JSONObject(plaWelRs.getString("taskdata")));//��������
		jsonarr.add(new JSONArray(plaWelRs.getString("taskaward")));//����ȡ����������
		jsonarr.add(new JSONArray(plaWelRs.getString("achieveaward")));//����ȡ���ĳɾͽ���
		jsonarr.add(plaWelRs.getInt("checkin"));//��ǩ������
		jsonarr.add(plaWelRs.getInt("checkintotal"));//�ۻ�ǩ������
		jsonarr.add(new JSONArray(plaWelRs.getString("checkinaward")));//����ȡ���ۻ�ǩ������
		jsonarr.add(plaWelRs.getInt("ischecked"));//������ǩ������
		jsonarr.add(new JSONObject(plaWelRs.getString("achievedata")));//��ǰ�ɾ�����
		jsonarr.add(new JSONArray(plaWelRs.getString("targetaward")));//����ȡ��Ŀ�꽱��
		return jsonarr;
	}
	
	//--------------������--------------
	
	/**
	 * �������
	 */
	public ReturnValue debugFinishTask(int playerid, byte type, int amount){
		DBHelper dbHelper = new DBHelper();
		try {
			dbHelper.openConnection();
			GameLog gl = GameLog.getInst(playerid, GameServlet.ACT_DEBUG_GAME_LOG);
			updateTaskProgress(dbHelper, playerid, type, amount, gl);
			
			gl.save();
			return new ReturnValue(true);
		} catch(Exception e){
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		} finally {
			dbHelper.closeConnection();
		}
	}
	
	/**
	 * ���Լ��ۼ�ǩ������
	 */
	public ReturnValue debugAddCheckin(int playerid, int days){
		DBHelper dbHelper = new DBHelper();
		try {
			if(days > Integer.MAX_VALUE){
				BACException.throwInstance("����̫����");
			}
			SqlString sqlStr = new SqlString();
			sqlStr.addChange("checkintotal", days);
			update(dbHelper, playerid, sqlStr);
			return new ReturnValue(true);
		} catch(Exception e){
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		} finally {
			dbHelper.closeConnection();
		}
	}
	
	/**
	 * �������óɾ���ȡ
	 */
	public ReturnValue debugResetAchieveAward(int playerid){
		DBHelper dbHelper = new DBHelper();
		try {
			SqlString sqlStr = new SqlString();
			sqlStr.add("achieveaward", new JSONArray().toString());
			update(dbHelper, playerid, sqlStr);
			return new ReturnValue(true);
		} catch(Exception e){
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		} finally {
			dbHelper.closeConnection();
		}
	}
	
	/**
	 * ��������Ŀ����ȡ
	 */
	public ReturnValue debugResetTargetAward(int playerid){
		DBHelper dbHelper = new DBHelper();
		try {
			SqlString sqlStr = new SqlString();
			sqlStr.add("targetaward", new JSONArray().toString());
			update(dbHelper, playerid, sqlStr);
			return new ReturnValue(true);
		} catch(Exception e){
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		} finally {
			dbHelper.closeConnection();
		}
	}
	
	/**
	 * ��������ǩ��
	 */
	public ReturnValue debugResetCheckIn(int playerid){
		DBHelper dbHelper = new DBHelper();
		try {
			SqlString sqlStr = new SqlString();
			sqlStr.add("checkin", 0);
			sqlStr.add("ischecked", 0);
			update(dbHelper, playerid, sqlStr);
			return new ReturnValue(true);
		} catch(Exception e){
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		} finally {
			dbHelper.closeConnection();
		}
	}
	
	/**
	 * ������������ճ�����
	 */
	public ReturnValue debugFinishAllTask(int playerid){
		DBHelper dbHelper = new DBHelper();
		try {
			DBPaRs plaWelRs = getDataRs(playerid);
			JSONObject taskobj = new JSONObject(plaWelRs.getString("taskdata"));
			DBPsRs taskListRs = DBPool.getInst().pQueryS(tab_daily_task);
			while(taskListRs.next()){
				String finish = taskListRs.getString("finish");
				int[] conarr = Tools.splitStrToIntArr(finish, ",");
				if(conarr.length == 2){
					taskobj.put(String.valueOf(conarr[0]), conarr[1]);
				}
			}
			SqlString sqlStr = new SqlString();
			sqlStr.add("taskdata", taskobj.toString());
			update(dbHelper, playerid, sqlStr);
			return new ReturnValue(true);
		} catch(Exception e){
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		} finally {
			dbHelper.closeConnection();
		}
	}
	
	//--------------��̬��--------------
	
	private static PlaWelfareBAC instance = new PlaWelfareBAC();

	/**
	 * ��ȡʵ��
	 */
	public static PlaWelfareBAC getInstance() {
		return instance;
	}
}

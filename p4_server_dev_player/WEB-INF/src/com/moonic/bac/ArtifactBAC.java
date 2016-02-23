package com.moonic.bac;

import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONObject;

import server.common.Tools;

import com.ehc.common.ReturnValue;
import com.ehc.common.SqlString;
import com.moonic.gamelog.GameLog;
import com.moonic.servlet.GameServlet;
import com.moonic.txtdata.ArtifactData;
import com.moonic.util.BACException;
import com.moonic.util.DBHelper;
import com.moonic.util.DBPaRs;
import com.moonic.util.DBPool;
import com.moonic.util.DBPsRs;
import com.moonic.util.MyTools;

/**
 * ����
 * @author John
 */
public class ArtifactBAC {
	public static final String tab_artifact = "tab_artifact";
	public static final String tab_artifact_up = "tab_artifact_up";
	
	private static final byte[] EAT_ITEMTYPE = {
		ItemBAC.TYPE_PROP_CONSUME,
		ItemBAC.TYPE_ORB,
		ItemBAC.TYPE_ORB_DEBRIS,
		ItemBAC.TYPE_ARTIFACT_DEBRIS
	};
	
	/**
	 * ����Ʒ
	 */
	public ReturnValue eatItem(int playerid, int num, String itemdata){
		DBHelper dbHelper = new DBHelper();
		try {
			DBPaRs artifactRs = DBPool.getInst().pQueryA(tab_artifact, "num="+num);
			if(!artifactRs.exist()){
				BACException.throwInstance("���������� num="+num);
			}
			DBPaRs plaRs = PlayerBAC.getInstance().getDataRs(playerid);
			if(plaRs.getInt("lv") < artifactRs.getInt("openplv")){
				BACException.throwInstance("����δ����");
			}
			DBPaRs plaroleRs = PlaRoleBAC.getInstance().getDataRs(playerid);
			JSONObject artifactobj = new JSONObject(plaroleRs.getString("artifactdata"));
			JSONArray artifactdata = artifactobj.optJSONArray(String.valueOf(num));
			int artifactlv = 0;
			int artifactexp = 0;
			if(artifactdata != null){
				artifactlv = artifactdata.optInt(0);
				artifactexp = artifactdata.optInt(1);
			}
			if(artifactlv >= artifactRs.getInt("maxlevel")){
				BACException.throwInstance("�ѵ���ȼ�����");
			}
			if(plaRs.getInt("lv")-artifactlv <= artifactRs.getInt("levellimit")){
				BACException.throwInstance("�������������ȼ�");
			}
			JSONArray eatitemarr = new JSONArray(itemdata);
			JSONArray itemidarr = eatitemarr.optJSONArray(0);
			JSONArray amountarr = eatitemarr.optJSONArray(1);
			DBPsRs itemStorRs = ItemBAC.getInstance().query(playerid, "playerid="+playerid);
			int addexp = 0;
			while(itemStorRs.next()){
				int index = itemidarr.indexOf(itemStorRs.getInt("id"));
				if(index != -1){
					int useamount = amountarr.optInt(index);
					if(itemStorRs.getInt("itemamount") < useamount){
						BACException.throwInstance("�������� "+itemStorRs.getInt("itemamount")+"/"+useamount);
					}
					int itemtype = itemStorRs.getInt("itemtype");
					if(!Tools.intArrContain(EAT_ITEMTYPE, itemtype)){
						BACException.throwInstance("�޷��Դ����͵���Ʒ itemtype="+itemtype);
					}
					DBPaRs itemRs = ItemBAC.getInstance().getListRs(itemtype, itemStorRs.getInt("itemnum"));
					if(itemtype == ItemBAC.TYPE_PROP_CONSUME){
						int[] effect = Tools.splitStrToIntArr(itemRs.getString("effect"), ",");
						if(effect[0] != 5){
							BACException.throwInstance("������Ʒû��������������Ĺ�Ч");
						}
						addexp += effect[1]*useamount;
					} else 
					if(itemtype == ItemBAC.TYPE_ORB){
						if(itemRs.getInt("rare") >= 4){
							BACException.throwInstance("���������ɫƷ�����ϵ�����");
						}
						addexp += itemRs.getInt("artifactexp")*useamount;
					} else 
					if(itemtype == ItemBAC.TYPE_ORB_DEBRIS){
						if(itemRs.getInt("rare") >= 5){
							BACException.throwInstance("������Գ�ɫƷ�����ϵ�������Ƭ");
						}
						addexp += itemRs.getInt("artifactexp")*useamount;
					}
					itemidarr.remove(index);
					amountarr.remove(index);
				}
			}
			if(itemidarr.length() > 0){
				BACException.throwInstance("�в����ڵ���Ʒ "+itemidarr.toString());
			}
			int upnum = artifactRs.getInt("upnum");
			DBPsRs upRs = DBPool.getInst().pQueryS(tab_artifact_up, "lv>"+artifactlv, "lv");
			artifactexp += addexp;
			boolean maxlv = true;
			while(upRs.next()){
				int needexp = upRs.getInt("type"+upnum);
				if(artifactexp < needexp){
					maxlv = false;
					break;
				}
				artifactexp -= needexp;
				artifactlv += 1;
			}
			int useexp = addexp;
			if(maxlv){
				useexp -= artifactexp;
				artifactexp = 0;
			}
			GameLog gl = GameLog.getInst(playerid, GameServlet.ACT_ARTIFACT_EATITEM);
			PlayerBAC.getInstance().useMoney(dbHelper, playerid, useexp*100, gl);
			if(artifactdata == null){
				artifactdata = new JSONArray();
			}
			artifactdata.put(0, artifactlv);
			artifactdata.put(1, artifactexp);
			JSONArray itemarr = new JSONArray();
			eatitemarr = new JSONArray(itemdata);
			itemidarr = eatitemarr.optJSONArray(0);
			amountarr = eatitemarr.optJSONArray(1);
			artifactobj.put(String.valueOf(num), artifactdata);
			for(int i = 0; i < itemidarr.length(); i++){
				JSONObject itemobj = ItemBAC.getInstance().remove(dbHelper, playerid, itemidarr.optInt(i), amountarr.optInt(i), ItemBAC.ZONE_BAG, gl);
				itemarr.add(itemobj);
			}
			SqlString sqlStr = new SqlString();
			sqlStr.add("artifactdata", artifactobj.toString());
			PlaRoleBAC.getInstance().update(dbHelper, playerid, sqlStr);
			
			PlaWelfareBAC.getInstance().updateTaskProgress(dbHelper, playerid, PlaWelfareBAC.TYPE_INSTRUMENT, gl);
			
			gl.addItemChaNoteArr(itemarr);
			gl.save();
			return new ReturnValue(true, itemarr.toString());
		} catch (Exception e) {
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		} finally {
			dbHelper.closeConnection();
		}
	}
	
	/**
	 * ��ע��
	 */
	public ReturnValue coinInput(int playerid, int num, int upamount){
		DBHelper dbHelper = new DBHelper();
		try {
			if(upamount < 1){
				BACException.throwInstance("�������� upamount="+upamount);
			}
			DBPaRs artifactRs = DBPool.getInst().pQueryA(tab_artifact, "num="+num);
			if(!artifactRs.exist()){
				BACException.throwInstance("���������� num="+num);
			}
			DBPaRs plaRs = PlayerBAC.getInstance().getDataRs(playerid);
			if(plaRs.getInt("lv") < artifactRs.getInt("openplv")){
				BACException.throwInstance("����δ����");
			}
			boolean open = VipBAC.getInstance().checkVipFuncOpen(plaRs.getInt("vip"), 8);
			if(!open){
				BACException.throwInstance("VIP�ȼ�����");
			}
			DBPaRs plaroleRs = PlaRoleBAC.getInstance().getDataRs(playerid);
			JSONObject artifactobj = new JSONObject(plaroleRs.getString("artifactdata"));
			JSONArray artifactdata = artifactobj.optJSONArray(String.valueOf(num));
			int artifactlv = 0;
			int artifactexp = 0;
			if(artifactdata != null){
				artifactlv = artifactdata.optInt(0);
				artifactexp = artifactdata.optInt(1);
			}
			if(artifactlv + upamount > artifactRs.getInt("maxlevel")){
				BACException.throwInstance("Ŀ��ȼ��������ȼ� "+(artifactlv+upamount)+"/"+artifactRs.getInt("maxlevel"));
			}
			if(plaRs.getInt("lv")-(artifactlv+upamount) < artifactRs.getInt("levellimit")){//��ɫ�ȼ���������������ȼ�������
				BACException.throwInstance("�������������ȼ�");
			}
			GameLog gl = GameLog.getInst(playerid, GameServlet.ACT_ARTIFACT_COININPUT);
			int upnum = artifactRs.getInt("upnum");
			DBPsRs upRs = DBPool.getInst().pQueryS(tab_artifact_up, "lv>="+(artifactlv+1)+" and lv<="+(artifactlv+upamount));
			int needcoin = -artifactexp;
			while(upRs.next()){
				needcoin += upRs.getInt("type"+upnum);
			}
			PlayerBAC.getInstance().useCoin(dbHelper, playerid, needcoin, gl);
			artifactlv += upamount;
			artifactexp = 0;
			if(artifactdata == null){
				artifactdata = new JSONArray();
			}
			artifactdata.put(0, artifactlv);
			artifactdata.put(1, artifactexp);
			artifactobj.put(String.valueOf(num), artifactdata);
			SqlString sqlStr = new SqlString();
			sqlStr.add("artifactdata", artifactobj.toString());
			PlaRoleBAC.getInstance().update(dbHelper, playerid, sqlStr);
			
			PlaWelfareBAC.getInstance().updateTaskProgress(dbHelper, playerid, PlaWelfareBAC.TYPE_INSTRUMENT, gl);
			
			gl.addChaNote(artifactRs.getString("name")+"�ȼ�", artifactlv-upamount, upamount);
			gl.save();
			return new ReturnValue(true);
		} catch (Exception e) {
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		} finally {
			dbHelper.closeConnection();
		}
	}
	
	/**
	 * �ϳ�����
	 */
	public ReturnValue comp(int playerid, int num){
		DBHelper dbHelper = new DBHelper();
		try {
			DBPaRs artifactRs = DBPool.getInst().pQueryA(tab_artifact, "num="+num);
			if(!artifactRs.exist()){
				BACException.throwInstance("���������� num="+num);
			}
			JSONObject artifactdata = new JSONObject(PlaRoleBAC.getInstance().getStrValue(playerid, "artifactdata"));
			if(artifactdata.optJSONArray(String.valueOf(num)) != null){
				BACException.throwInstance("�������Ѽ��� num="+num);
			}
			DBPsRs itemStorRs = ItemBAC.getInstance().query(playerid, "playerid="+playerid+" and itemtype="+ItemBAC.TYPE_ARTIFACT_DEBRIS);
			JSONArray numarr = new JSONArray();
			while(itemStorRs.next()){
				numarr.add(itemStorRs.getInt("itemnum"));
			}
			DBPsRs debrisRs = DBPool.getInst().pQueryS(ItemBAC.getInstance().getTab(ItemBAC.TYPE_ARTIFACT_DEBRIS), "artifactnum="+num);
			while(debrisRs.next()){
				if(!numarr.contains(debrisRs.getInt("num"))){
					BACException.throwInstance("ȱ��������Ƭ num="+debrisRs.getInt("num"));
				}
			}
			debrisRs.beforeFirst();
			GameLog gl = GameLog.getInst(playerid, GameServlet.ACT_ARTIFACT_COMP);
			JSONArray itemarr = new JSONArray();
			while(debrisRs.next()){
				JSONArray arr = ItemBAC.getInstance().remove(dbHelper, playerid, ItemBAC.TYPE_ARTIFACT_DEBRIS, debrisRs.getInt("num"), 1, ItemBAC.ZONE_BAG, gl);
				MyTools.combJsonarr(itemarr, arr);
			}
			artifactdata.put(String.valueOf(num), new JSONArray(new int[2]));
			SqlString sqlStr = new SqlString();
			sqlStr.add("artifactdata", artifactdata.toString());
			PlaRoleBAC.getInstance().update(dbHelper, playerid, sqlStr);
			
			gl.addItemChaNoteArr(itemarr);
			gl.save();
			return new ReturnValue(true, itemarr.toString());
		} catch (Exception e) {
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		} finally {
			dbHelper.closeConnection();
		}
	}
	
	/**
	 * �������ᱣ��
	 */
	public ReturnValue openRobProtect(int playerid){
		DBHelper dbHelper = new DBHelper();
		try {
			DBPaRs plaroleRs = PlaRoleBAC.getInstance().getDataRs(playerid);
			if(!MyTools.checkSysTimeBeyondSqlDate(plaroleRs.getTime("artifactendtime"))){
				BACException.throwInstance("����ʱ��δ����������ʱ��="+MyTools.getTimeStr(plaroleRs.getTime("artifactendtime")));
			}
			int index = plaroleRs.getInt("artifactprotecttimes");
			if(index > ArtifactData.openprotectcoin.length-1){
				index = ArtifactData.openprotectcoin.length-1;
			}
			int needcoin = ArtifactData.openprotectcoin[index];
			GameLog gl = GameLog.getInst(playerid, GameServlet.ACT_ARTIFACT_OPENPROTECT);
			PlayerBAC.getInstance().useCoin(dbHelper, playerid, needcoin, gl);
			String endTimeStr = MyTools.getTimeStr(System.currentTimeMillis()+ArtifactData.protecttimelen);
			SqlString sqlStr = new SqlString();
			sqlStr.addDateTime("artifactendtime", endTimeStr);
			PlaRoleBAC.getInstance().update(dbHelper, playerid, sqlStr);
			
			gl.addRemark("����ʱ�䣺"+endTimeStr);
			gl.save();
			return new ReturnValue(true);
		} catch (Exception e) {
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		} finally {
			dbHelper.closeConnection();
		}
	}
	
	/**
	 * ��ȡ�������
	 */
	public ReturnValue getRobList(int playerid){
		DBHelper dbHelper = new DBHelper();
		try {
			
			//
			
			return new ReturnValue(true);
		} catch (Exception e) {
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		} finally {
			dbHelper.closeConnection();
		}
	}
	
	/**
	 * ������Ƭ
	 */
	public ReturnValue robDebris(int playerid, int tergetid){
		DBHelper dbHelper = new DBHelper();
		try {
			
			//
			
			return new ReturnValue(true);
		} catch (Exception e) {
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		} finally {
			dbHelper.closeConnection();
		}
	}
	
	/**
	 * ��ȡս��Ч��
	 */
	public double[][] getBattleProp(int playerid) throws Exception {
		JSONObject artifactdata = new JSONObject(PlaRoleBAC.getInstance().getStrValue(playerid, "artifactdata"));
		double[][] returndata = new double[artifactdata.length()][];
		int index = 0;
		@SuppressWarnings("rawtypes")
		Iterator numIterator = artifactdata.keys();
		while(numIterator.hasNext()){//�Թ������
			String num = (String)numIterator.next();
			JSONArray arr = artifactdata.optJSONArray(num);
			DBPaRs artifactRs = DBPool.getInst().pQueryA(tab_artifact, "num="+num);
			double[] data = Tools.splitStrToDoubleArr(artifactRs.getString("attr"), ",");//�������� ��ʽ������ս�����ԡ�ս���������͡����ӷ�ʽ������ֵ
			data[data.length-1] += artifactRs.getDouble("addattr")*arr.optInt(0);//ÿ��һ�����ӵ�ֵ*�ȼ�
			data[data.length-1] += DBPool.getInst().pQueryS(tab_artifact_up, "lv<="+arr.optInt(0)).sum("addattrperc")/100*artifactRs.getDouble("addattr");//�������ӵĳɳ��ٷֱ�
			returndata[index] = data;
			index++;
		}
		return returndata;
	}
	
	//--------------��̬��--------------
	
	private static ArtifactBAC instance = new ArtifactBAC();
	
	/**
	 * ��ȡʵ��
	 */
	public static ArtifactBAC getInstance(){
		return instance;
	}
}

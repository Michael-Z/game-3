package com.moonic.bac;

import java.sql.ResultSet;
import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONObject;

import com.ehc.common.ReturnValue;
import com.ehc.common.SqlString;
import com.moonic.gamelog.GameLog;
import com.moonic.mgr.LockStor;
import com.moonic.servlet.GameServlet;
import com.moonic.socket.PushData;
import com.moonic.socket.SocketServer;
import com.moonic.util.BACException;
import com.moonic.util.DBHelper;
import com.moonic.util.DBPaRs;
import com.moonic.util.DBPsRs;
import com.moonic.util.MyTools;

import conf.Conf;
import server.common.Tools;

/**
 * ����BAC
 * @author wkc
 */
public class FriendBAC extends PlaStorBAC {
	/**
	 * ����(���ڲ�ѯָ������)
	 */
	public static final byte TYPE_ALL = -1;
	/**
	 * NONE(�����жϹ�ϵ����)
	 */
	public static final byte TYPE_NONE = 0;
	/**
	 * ����
	 */
	public static final byte TYPE_FRIEND = 1;
	/**
	 * ������
	 */
	public static final byte TYPE_BLACK = 2;
	
	private static final String[] types = {"�޹�ϵ", "����" , "������"};
	
	public static final int MAX_FRIENDS = 50;//������������
	
	/**
	 * ����
	 */
	public FriendBAC(){
		super("tab_friend", "playerid", null);
	}
	
	/**
	 * �Ӻ���
	 * @param friends,����IDJSONArray
	 */
	public ReturnValue addFriends(int playerid, String friends, byte type){
		DBHelper dbHelper = new DBHelper();
		try {
			if(type < 1 || type > 2) {
				BACException.throwInstance("�������� type="+type);
			}
			JSONArray fidarr = new JSONArray(friends);
			if(fidarr.size() == 0){
				BACException.throwInstance("����IDΪ��");
			}
			if(fidarr.contains(playerid)){
				BACException.throwInstance("���ܼ��Լ�Ϊ"+types[type]);
			}
			if(type == TYPE_FRIEND){
				DBPsRs friRs = query(playerid, "playerid="+playerid+" and type="+type);
				if(fidarr.size() + friRs.count() > MAX_FRIENDS){
					BACException.throwInstance("����������������");
				}
			}
			dbHelper.openConnection();
			int[] pushIds = null;//�ɺ�������Ϊ���ѵĲ�������
			JSONArray returnarr = new JSONArray();
			for(int i = 0; i < fidarr.size(); i++){
				int fid = fidarr.getInt(i);
				if(fid <= 0) {
					continue;
				}
				byte fritype = getFriendType(playerid, fid);
				if(fritype == TYPE_NONE) {
					pushIds = Tools.addToIntArr(pushIds, fid);
					SqlString sqlStr = new SqlString();
					sqlStr.add("playerid", playerid);
					sqlStr.add("friendid", fid);
					sqlStr.add("type", type);
					insert(dbHelper, playerid, sqlStr);
				} else {
					if(fritype == type){
						continue;
					}
					SqlString sqlStr = new SqlString();
					sqlStr.add("type", type);
					update(dbHelper, playerid, sqlStr, "playerid="+playerid+" and friendid="+fid);
				}
				returnarr.add(getFriendInfo(playerid, fid, type));
			}
			if(returnarr.size() == 0){
				BACException.throwInstance("����"+types[type]+"�У��޷����");
			}
			StringBuffer sb = new StringBuffer();
			for(int i = 0; i < returnarr.length(); i++){
				JSONArray arr = returnarr.optJSONArray(i);
				sb.append(GameLog.formatNameID(arr.optString(1), arr.optInt(0))+"\r\n");
			}
			if(type == TYPE_FRIEND) {
				DBPaRs plaRs = PlayerBAC.getInstance().getDataRs(playerid);
				JSONArray pusharr = new JSONArray();
				pusharr.add(playerid);
				pusharr.add(plaRs.getInt("num"));
				pusharr.add(plaRs.getString("name"));
				pusharr.add(plaRs.getInt("lv"));
				pusharr.add(0);
				pusharr.add(1);
				pusharr.add(plaRs.getInt("onlinestate"));
				pusharr.add(plaRs.getTime("logintime"));
				PushData.getInstance().sendPlaToSome(SocketServer.ACT_FRIEND_ADD, pusharr.toString(), pushIds);
			}
			CustomActivityBAC.getInstance().updateProcess(dbHelper, playerid, 5, returnarr.size());
			
			GameLog.getInst(playerid, GameServlet.ACT_FRIEND_ADD)
			.addRemark("��"+types[type]+"��"+sb)
			.save();
			return new ReturnValue(true, returnarr.toString());
		} catch (Exception e) {
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		} finally {
			dbHelper.closeConnection();
		}
	}
	
	/**
	 * ɾ����
	 * @param type	���ͣ�1 ��ʾ���ѣ�2 ��ʾ������
	 */
	public ReturnValue deleteFriends(int playerid, String friends, int type){
		DBHelper dbHelper = new DBHelper();
		try {
			JSONArray fidarr = new JSONArray(friends);
			if(fidarr.size() == 0){
				BACException.throwInstance("����IDΪ��");
			}
			if(fidarr.contains(playerid)){
				BACException.throwInstance("�޷�ɾ���Լ�");
			}
			DBPaRs plaRs = PlayerBAC.getInstance().getDataRs(playerid);
			dbHelper.openConnection();
			StringBuffer sb = new StringBuffer();
			int[] pushIds = null;//����id
			for(int i = 0; i < fidarr.size(); i++){
				int fid = fidarr.getInt(i);
				byte fritype = getFriendType(playerid, fid);
				if(fritype == TYPE_NONE){
					continue;
				}
				pushIds = Tools.addToIntArr(pushIds, fid);
				delete(dbHelper, playerid, "playerid="+playerid+" and friendid="+fid);
				sb.append(GameLog.formatNameID(PlayerBAC.getInstance().getStrValue(fid, "name"), fid)+"\r\n");
			}
			if(type == TYPE_FRIEND) {
				JSONArray pusharr = new JSONArray();
				pusharr.add(playerid);
				pusharr.add(plaRs.getInt("num"));
				pusharr.add(plaRs.getString("name"));
				PushData.getInstance().sendPlaToSome(SocketServer.ACT_FRIEND_DELETE, pusharr.toString(), pushIds);
				//����ɾ�����Ѽ�¼
				DBPaRs presentRs = PlaRoleBAC.getInstance().getDataRs(playerid);
				JSONArray deleteArr = new JSONArray(presentRs.getString("deletefriend"));
				if(pushIds != null){
					for (int i = 0; i < pushIds.length; i++) {
						if(deleteArr.length() > 20){
							deleteArr.remove(0);
						}
						deleteArr.add(pushIds[i]);
					}
					SqlString sqlStr = new SqlString();
					sqlStr.add("deletefriend", deleteArr.toString());
					PlaRoleBAC.getInstance().update(dbHelper, playerid, sqlStr);
				}
			}
			
			GameLog.getInst(playerid, GameServlet.ACT_FRIEND_DELLTE)
			.addRemark("ɾ��"+types[type]+"��"+sb)
			.save();
			return new ReturnValue(true);
		} catch (Exception e) {
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		} finally {
			dbHelper.closeConnection();
		}
	}
	
	/**
	 * ����������
	 */
	public ReturnValue search(int playerid, String condition){
		DBHelper dbHelper = new DBHelper();
		try {
			MyTools.checkNoChar(condition);
			if(condition == null || condition.equals("")) {
				BACException.throwInstance("������������Ϊ��");
			}
			boolean isNum = Tools.isNumber(condition);
			String idWhere  = "serverid="+Conf.sid+" and id="+condition;
			String nameWhere  = "serverid="+Conf.sid+" and name='"+condition+"'";
			ResultSet plaRs = null;
			dbHelper.openConnection();
			if(isNum){
				plaRs = dbHelper.query("tab_player", "id", idWhere);
				if(!plaRs.next()){
					plaRs = dbHelper.query("tab_player", "id", nameWhere);
					if(!plaRs.next()){
						BACException.throwInstance("�����ڴ����");
					}
				}
			} else {
				plaRs = dbHelper.query("tab_player", "id", nameWhere);
				if(!plaRs.next()){
					BACException.throwInstance("�����ڴ����");
				}
			}
			int fid = plaRs.getInt("id");
			if(fid == playerid) {
				BACException.throwInstance("�����������Լ�");
			}
			byte fritype = getFriendType(playerid, fid);
			if(fritype == TYPE_FRIEND) {
				BACException.throwInstance("���ں����У��޷����");
			}
			if(fritype == TYPE_NONE) {
				SqlString sqlStr = new SqlString();
				sqlStr.add("playerid", playerid);
				sqlStr.add("friendid", fid);
				sqlStr.add("type", 1);
				insert(dbHelper, playerid, sqlStr);
			} else {
				SqlString sqlStr = new SqlString();
				sqlStr.add("type", 1);
				update(dbHelper, playerid, sqlStr, "playerid="+playerid+" and friendid="+fid);
			}
			DBPaRs selfRs = PlayerBAC.getInstance().getDataRs(playerid);
			JSONArray pusharr = new JSONArray();
			pusharr.add(playerid);
			pusharr.add(selfRs.getInt("num"));
			pusharr.add(selfRs.getString("name"));
			pusharr.add(selfRs.getInt("lv"));
			pusharr.add(0);
			pusharr.add(1);
			pusharr.add(selfRs.getInt("onlinestate"));
			pusharr.add(selfRs.getTime("logintime"));
			long battlePower = PlaRoleBAC.getInstance().getLongValue(playerid, "totalbattlepower");
			pusharr.add(battlePower);//ս��
			PushData.getInstance().sendPlaToOne(SocketServer.ACT_FRIEND_ADD, pusharr.toString(), fid);
			CustomActivityBAC.getInstance().updateProcess(dbHelper, playerid, 5);
			
			JSONArray playerarr = getFriendInfo(playerid, fid, TYPE_FRIEND);
			return new ReturnValue(true, playerarr.toString());
		} catch (Exception e) {
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		} finally {
			dbHelper.closeConnection();
		}
	}
	
	/**
	 * ���ٲ���
	 */
	public ReturnValue quickSearch(int playerid){
		DBHelper dbHelper = new DBHelper();
		try {
			int plv = PlayerBAC.getInstance().getIntValue(playerid, "lv");
			dbHelper.openConnection();
			ResultSet plaRs = dbHelper.query("tab_player", "id,num,name,lv,onlinestate,logintime", "serverid="+Conf.sid+" and id!="+playerid+" and lv>="+(plv-10)+" and lv<="+(plv+10), "lv desc", 10);
			JSONArray returnarr = new JSONArray();
			while(plaRs.next()){
				int pid = plaRs.getInt("id");
				if(getFriendType(playerid, pid) != TYPE_NONE){
					continue;
				}
				JSONArray player = new JSONArray();
				player.add(pid);
				player.add(plaRs.getInt("num"));
				player.add(plaRs.getString("name"));
				player.add(plaRs.getInt("lv"));
				player.add(1);
				player.add(0);
				player.add(plaRs.getInt("onlinestate"));
				player.add(MyTools.getTimeLong(plaRs.getTimestamp("logintime")));
				long battlePower = PlaRoleBAC.getInstance().getLongValue(pid, "totalbattlepower");
				player.add(battlePower);//ս��
				returnarr.add(player);
			}
			if(returnarr.size() == 0){
				BACException.throwInstance("�����ڷ������������");
			}
			
			return new ReturnValue(true, returnarr.toString());
		} catch(Exception e){
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		} finally{
			dbHelper.closeConnection();
		}
	}
	
	/**
	 * ���ͺ�������
	 */
	public ReturnValue presentEnergy(int playerid, int friendid){
		DBHelper dbHelper = new DBHelper();
		try {
			DBPsRs friendRs = query(playerid, "playerid="+playerid+" and friendid="+friendid);
			if(!friendRs.next()){
				BACException.throwInstance("��δ��Ӵ˺���");
			}
			int fansType = FansBAC.getInstance().getFansType(playerid, friendid);
			if(fansType != TYPE_FRIEND){
				BACException.throwInstance("��Ϊ���Ѳ�������");
			}
			synchronized (LockStor.getLock(LockStor.FRIEND_PRESENT, friendid)) {
				DBPaRs presentRs = PlaRoleBAC.getInstance().getDataRs(playerid);
				JSONArray presentArr = new JSONArray(presentRs.getString("present"));
				if(presentArr.contains(friendid)){
					BACException.throwInstance("����������");
				}
				if(presentArr.size() >= 10){
					BACException.throwInstance("ÿ���������10��");
				}
				presentArr.add(friendid);
				DBPaRs bePresentRs = PlaRoleBAC.getInstance().getDataRs(friendid);
				JSONObject bePresentObj = new JSONObject(bePresentRs.getString("bepresent"));
				int total = getBePresentTimes(bePresentObj);
				if(total >= 30){
					BACException.throwInstance("��౻����30��");
				}
				bePresentObj.put(String.valueOf(playerid), bePresentObj.optInt(String.valueOf(playerid))+1);
				SqlString sqlStr = new SqlString();
				sqlStr.add("present", presentArr.toString());
				PlaRoleBAC.getInstance().update(dbHelper, playerid, sqlStr);
				
				SqlString beSqlStr = new SqlString();
				beSqlStr.add("bepresent", bePresentObj.toString());
				PlaRoleBAC.getInstance().update(dbHelper, friendid, beSqlStr);
				
				PushData.getInstance().sendPlaToOne(SocketServer.ACT_FRIEND_PRESENT, String.valueOf(playerid), friendid);
				
				String friendName = PlayerBAC.getInstance().getStrValue(friendid, "name");
				GameLog.getInst(playerid, GameServlet.ACT_FRIEND_PRESENT)
				.addRemark("��������������"+GameLog.formatNameID(friendName, friendid))
				.save();
				
				return new ReturnValue(true);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		} finally {
			dbHelper.closeConnection();
		}
	}
	
	/**
	 * һ�����ͺ�������
	 */
	public ReturnValue presentEnergyOneKey(int playerid, String friends){
		DBHelper dbHelper = new DBHelper();
		try {
			JSONArray friendarr = new JSONArray(friends);
			if(friendarr.size() == 0){
				BACException.throwInstance("����IDΪ��");
			}
			if(friendarr.contains(playerid)){
				BACException.throwInstance("�����͸��Լ�");
			}
			synchronized (LockStor.getLock(LockStor.FRIEND_PRESENT)) {
				DBPaRs presentRs = PlaRoleBAC.getInstance().getDataRs(playerid);
				JSONArray presentArr = new JSONArray(presentRs.getString("present"));
				JSONArray firarr = new JSONArray();
				for(int i = 0; i < friendarr.size(); i++){
					int friendid = friendarr.getInt(i);
					DBPsRs friendRs = query(playerid, "playerid="+playerid+" and friendid="+friendid);
					if(friendRs.next()){
						int fansType = FansBAC.getInstance().getFansType(playerid, friendid);
						if(fansType == TYPE_FRIEND){
							if(!presentArr.contains(friendid)){
								DBPaRs bePresentRs = PlaRoleBAC.getInstance().getDataRs(friendid);
								JSONObject bePresentObj = new JSONObject(bePresentRs.getString("bepresent"));
								int total = getBePresentTimes(bePresentObj);
								if(total < 30){
									firarr.add(friendid);
								}
							}
						}
					}
				}
				if(presentArr.size() + firarr.length() > 10){
					BACException.throwInstance("ÿ���������10��");
				}
				if(firarr.size() == 0){
					BACException.throwInstance("û�п������͵ĺ���");
				}
				GameLog gl = GameLog.getInst(playerid, GameServlet.ACT_FRIEND_PRESENT_ONEKEY);
				StringBuffer remarkSb = new StringBuffer();
				remarkSb.append("��������������");
				for(int i = 0; i < firarr.size(); i++){
					int friendid = firarr.getInt(i);
					presentArr.add(friendid);
					DBPaRs bePresentRs = PlaRoleBAC.getInstance().getDataRs(friendid);
					JSONObject bePresentObj = new JSONObject(bePresentRs.getString("bepresent"));
					bePresentObj.put(String.valueOf(playerid), bePresentObj.optInt(String.valueOf(playerid))+1);
					SqlString beSqlStr = new SqlString();
					beSqlStr.add("bepresent", bePresentObj.toString());
					PlaRoleBAC.getInstance().update(dbHelper, friendid, beSqlStr);
					PushData.getInstance().sendPlaToOne(SocketServer.ACT_FRIEND_PRESENT, String.valueOf(playerid), friendid);
					String friendName = PlayerBAC.getInstance().getStrValue(friendid, "name");
					remarkSb.append(GameLog.formatNameID(friendName, friendid)+",");
				}
				SqlString sqlStr = new SqlString();
				sqlStr.add("present", presentArr.toString());
				PlaRoleBAC.getInstance().update(dbHelper, playerid, sqlStr);
				
				gl.addRemark(remarkSb);
				gl.save();
				return new ReturnValue(true);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		} finally {
			dbHelper.closeConnection();
		}
	}
	
	/**
	 * ��ȡ��������
	 */
	public ReturnValue getEnergy(int playerid, int friendid){
		DBHelper dbHelper = new DBHelper();
		try {
			synchronized (LockStor.getLock(LockStor.FRIEND_PRESENT, friendid)) {
				DBPaRs bePresentRs = PlaRoleBAC.getInstance().getDataRs(playerid);
				JSONObject bePresentObj = new JSONObject(bePresentRs.getString("bepresent"));
				int times = bePresentObj.optInt(String.valueOf(friendid));
				if(times == 0){
					BACException.throwInstance("�˺�����������");
				}
				GameLog gl = GameLog.getInst(playerid, GameServlet.ACT_FRIEND_GETENERGY);
				PlaRoleBAC.getInstance().addValue(dbHelper, playerid, "energy", times*2, gl, "��ȡ������������");
				bePresentObj.remove(String.valueOf(friendid));
				SqlString sqlStr = new SqlString();
				sqlStr.add("bepresent", bePresentObj.toString());
				PlaRoleBAC.getInstance().update(dbHelper, playerid, sqlStr);
				
				String friendName = PlayerBAC.getInstance().getStrValue(friendid, "name");
				gl.addRemark("��ȡ��������"+GameLog.formatNameID(friendName, friendid));
				gl.save();
				return new ReturnValue(true);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		} finally {
			dbHelper.closeConnection();
		}
	}
	
	/**
	 * һ����ȡ��������
	 */
	public ReturnValue getEnergyOneKey(int playerid){
		DBHelper dbHelper = new DBHelper();
		try {
			synchronized (LockStor.getLock(LockStor.FRIEND_PRESENT)) {
				DBPaRs bePresentRs = PlaRoleBAC.getInstance().getDataRs(playerid);
				JSONObject bePresentObj = new JSONObject(bePresentRs.getString("bepresent"));
				int times = getBePresentTimes(bePresentObj);
				int addEnergy = times*2;
				if(addEnergy == 0){
					BACException.throwInstance("����������ȡ");
				}
				GameLog gl = GameLog.getInst(playerid, GameServlet.ACT_FRIEND_GETENERGY_ONEKEY);
				PlaRoleBAC.getInstance().addValue(dbHelper, playerid, "energy", addEnergy, gl, "��ȡ������������");
				SqlString sqlStr = new SqlString();
				sqlStr.add("bepresent", "{}");
				PlaRoleBAC.getInstance().update(dbHelper, playerid, sqlStr);
				
				gl.addRemark("��ȡ����"+times+"��");
				gl.save();
				return new ReturnValue(true, String.valueOf(addEnergy));
			}
		} catch (Exception e) {
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		} finally {
			dbHelper.closeConnection();
		}
	}
	
	/**
	 * ��ȡ�ܼ��ѱ����ʹ���
	 */
	public int getBePresentTimes(JSONObject bePresentObj){
		int total = 0;
		@SuppressWarnings("unchecked")
		Iterator<String> keys = bePresentObj.keys();
		while(keys.hasNext()){
			total += bePresentObj.optInt(keys.next());
		}
		return total;
	}
	
	/**
	 * ��½��ȡ���Ѽ��������б�
	 */
	public JSONArray getLoginData(int playerid) throws Exception {
		DBPaRs plaRoleRs = PlaRoleBAC.getInstance().getDataRs(playerid);
		JSONArray fidArr = new JSONArray(plaRoleRs.getString("deletefriend"));//��ɾ������
		JSONArray jsonarr = new JSONArray();
		//������Ϣ
		JSONArray friarr = new JSONArray();
		DBPsRs myfriRs = query(playerid, "playerid="+playerid);
		while(myfriRs.next()){
			int friendid = myfriRs.getInt("friendid");
			fidArr.add(friendid);
			JSONArray arr = getFriendInfo(playerid, friendid, myfriRs.getByte("type"));
			friarr.add(arr);
		}
		//��ȡ�Է�������ң����һ�û�����Ľ�ɫ����
		JSONArray unaddArr = new JSONArray();//δ�Ӻ�����Ϣ
		int count = 0;
		DBPsRs fansRs = FansBAC.getInstance().query(playerid, "friendid="+ playerid + " and type=" + TYPE_FRIEND);
		while(fansRs.next()){
			int pid = fansRs.getInt("playerid");
			if(!fidArr.contains(pid)) {
				DBPaRs plaRs = PlayerBAC.getInstance().getDataRs(pid);
				JSONArray plaarr = new JSONArray();
				plaarr.add(pid);//���ID
				plaarr.add(plaRs.getInt("num"));//��ɫ���
				plaarr.add(plaRs.getString("name"));//�������
				plaarr.add(plaRs.getInt("lv"));//�ȼ�
				unaddArr.add(plaarr);
				count++;
			}
			if(count >= 20){
				break;
			}
		}
		jsonarr.add(friarr);
		jsonarr.add(unaddArr);
		return jsonarr;
	}
	
	/**
	 * ��ȡ������Ϣ
	 */
	public JSONArray getFriendInfo(int playerid, int friendid, byte type) throws Exception {
		DBPaRs friRs = PlayerBAC.getInstance().getDataRs(friendid);
		JSONArray friarr = new JSONArray();
		friarr.add(friendid);//���ID
		friarr.add(friRs.getInt("num"));//��ɫ���
		friarr.add(friRs.getString("name"));//�������
		friarr.add(friRs.getInt("lv"));//��ҵȼ�
		friarr.add(type);//��������
		friarr.add(FansBAC.getInstance().getFansType(playerid, friendid));//�Է����ҵĹ�ע����
		friarr.add(friRs.getInt("onlinestate"));//�Ƿ�����
		friarr.add(friRs.getTime("logintime"));//����¼ʱ��
		long battlePower = PlaRoleBAC.getInstance().getLongValue(friendid, "totalbattlepower");
		friarr.add(battlePower);//ս��
		return friarr;
	}
	
	/**
	 * �������Ƿ���ǰ�ߵĺ���
	 */
	public boolean isFriend(DBHelper dbHelper, int playerid, int friendid) throws Exception{
		return getFriendType(playerid, friendid) == TYPE_FRIEND;
	}
	
	/**
	 * �������Ƿ���ǰ�ߵĺ������� 
	 */
	public boolean isBlack(DBHelper dbHelper, int playerid, int friendid) throws Exception{
		return getFriendType(playerid, friendid) == TYPE_BLACK;
	}
	
	/**
	 * ��ȡ��������
	 */
	public byte getFriendType(int playerid, int targetid) throws Exception {
		DBPsRs fansRs = query(playerid, "playerid="+playerid+" and friendid="+targetid);
		byte type = TYPE_NONE;
		if(fansRs.next()){
			type = fansRs.getByte("type");
		}
		return type;
	}
	
	
	//--------------��̬��--------------
	
	private static FriendBAC instance = new FriendBAC();
	
	/**
	 * ��ȡʵ��
	 */
	public static FriendBAC getInstance(){
		return instance;
	}
	
	//--------------������--------------
	
	/**
	 * ��̨���ָ�������ĺ��ѻ������
	 * @param amounts	����
	 * @param type		���ͣ�1 ���ѣ�2 ������
	 */
	public ReturnValue debugAddFriend(int playerid, int amounts, byte type){
		DBHelper dbHelper = new DBHelper();
		try {
			if(amounts < 0 || amounts > 99 || type < 1 || type > 2) {
				BACException.throwInstance("������������ֻ��Ϊ[1~99]������ֻ��Ϊ[1~2]");
			}
			dbHelper.openConnection();
			ResultSet plaRs = dbHelper.query("tab_player", "id", "serverid="+Conf.sid+" and id!="+playerid, "lv desc");
			int now_amount = 0;
			StringBuffer sb = new StringBuffer();
			while(plaRs.next()) {
				int friendid = plaRs.getInt("id");
				byte friType = getFriendType(playerid, friendid);
				if(friType == TYPE_NONE){
					SqlString sqlStr = new SqlString();
					sqlStr.add("playerid", playerid);
					sqlStr.add("friendid", friendid);
					sqlStr.add("type", type);
					sqlStr.add("times", 0);
					insert(dbHelper, playerid, sqlStr);
				} else {
					SqlString sqlSql = new SqlString();
					sqlSql.add("type", type);
					update(dbHelper, playerid, sqlSql, "playerid="+playerid+" and friendid="+friendid);
				}
				sb.append(PlayerBAC.getInstance().getStrValue(friendid, "name")+"("+friendid+")\r\n");
				now_amount++;
				if(now_amount >= amounts){
					break;
				}
			}
			return new ReturnValue(true, sb.toString());
		} catch (Exception e) {
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		} finally {
			dbHelper.closeConnection();
		}
	}
	
	/**
	 * ��̨������ѻ��ߺ�����
	 * @param type	0��ʾɾ��ȫ�����������Ѻͺ���������1��ʾɾ��ȫ�����ѣ�2��ʾɾ��ȫ��������
	 */
	public ReturnValue debugDelFriend(int playerid, byte type){
		DBHelper dbHelper = new DBHelper();
		try {
			if(type < 0 || type > 2) {
				BACException.throwInstance("������������ֻ��Ϊ[0~2]");
			}
			dbHelper.openConnection();
			String where = "playerid="+playerid;
			while(type != TYPE_NONE){
				where += " and type="+type;
			}
			delete(dbHelper, playerid, where);
			return new ReturnValue(true);
		} catch (Exception e) {
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		} finally {
			dbHelper.closeConnection();
		}
	}
	
	/**
	 * �����������������Ϣ
	 */
	public ReturnValue debugResetPresent(int playerid){
		DBHelper dbHelper = new DBHelper();
		try {
			dbHelper.openConnection();
			SqlString sqlStr = new SqlString();
			sqlStr.add("present", "[]");
			sqlStr.add("bepresent", "{}");
			PlaRoleBAC.getInstance().update(dbHelper, playerid, sqlStr);
			return new ReturnValue(true);
		} catch (Exception e) {
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		} finally {
			dbHelper.closeConnection();
		}
	}
	
}

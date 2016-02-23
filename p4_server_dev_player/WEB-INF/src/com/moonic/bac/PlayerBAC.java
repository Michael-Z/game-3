package com.moonic.bac;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.sql.ResultSet;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import server.common.Tools;

import com.ehc.common.ReturnValue;
import com.ehc.common.SqlString;
import com.moonic.gamelog.GameLog;
import com.moonic.mgr.LockStor;
import com.moonic.mirror.MirrorMgr;
import com.moonic.servlet.GameServlet;
import com.moonic.socket.GamePushData;
import com.moonic.socket.Player;
import com.moonic.socket.PushData;
import com.moonic.socket.SocketServer;
import com.moonic.util.BACException;
import com.moonic.util.DBHelper;
import com.moonic.util.DBPaRs;
import com.moonic.util.DBPool;
import com.moonic.util.DBPsRs;
import com.moonic.util.MyTools;
import com.moonic.util.TimeTest;

import conf.Conf;
import conf.LogTbName;

/**
 * ��ɫBAC
 * @author John
 */
public class PlayerBAC extends PlaBAC {
	public static final String tab_player_change_type = "tab_player_change_type";
	public static final String tab_shortcut_grow = "tab_shortcut_grow";
	public static final String tab_player_uplv = "tab_player_uplv";
	
	/**
	 * ����
	 */
	public PlayerBAC(){
		super("tab_player", "id");
	}
	
	/**
	 * ��ʼ��
	 */
	public void init(DBHelper dbHelper, int playerid, Object... parm) throws Exception {
		
	}
	
	/**
	 * ������ɫ
	 */
	public ReturnValue create(int userid, int vsid, String name, byte num, int partnernum, int isrobot) {
		DBHelper dbHelper = new DBHelper();
		try {
			synchronized (LockStor.getLock(LockStor.PLAYER_NAME)) {
				MyTools.checkNoCharEx(name, '#');
				if(name.equals("") || name.toLowerCase().equals("null")){
					BACException.throwInstance("���ֲ����ã�����ĺ�����");
				}
				DBPaRs partnerRs = null;
				if(partnernum != 0){
					partnerRs = DBPool.getInst().pQueryA(PartnerBAC.tab_partner, "num="+partnernum);
					if(partnerRs.getInt("cpchoose") == 0){
						BACException.throwInstance("�޷�ѡ��˻����Ϊ��ʼ���");
					}
				}
				dbHelper.openConnection();
				ResultSet userRs = dbHelper.query(UserBAC.tab_user, "id,channel,onlinestate,devuser", "id="+userid);
				//System.out.println("userid="+userid);
				if(!userRs.next()){
					BACException.throwInstance("�û�δ�ҵ�");
				}
				String channel = userRs.getString("channel");
				DBPsRs channelServerRs = ServerBAC.getInstance().getChannelServer(channel, vsid);
				if(!channelServerRs.next()) {
					BACException.throwInstance("������������");
				}
				int csid = channelServerRs.getInt("serverid");
				if(csid != Conf.sid){
					BACException.throwInstance("������ID��ƥ��("+csid+"/"+Conf.sid+")");
				}
				DBPaRs serverRs = DBPool.getInst().pQueryA(ServerBAC.tab_server, "id="+Conf.sid);
				int state = channelServerRs.getInt("state")!=-1?channelServerRs.getInt("state"):serverRs.getInt("state");
				String opentime = !channelServerRs.getString("opentime").equals("-1")?channelServerRs.getString("opentime"):serverRs.getString("opentime");
				//���������Ƿ��ѿ���
				if(!MyTools.checkSysTimeBeyondSqlDate(MyTools.getTimeLong(opentime))) {
					if(userRs.getInt("devuser")!=1){
						String shownote = serverRs.getString("shownote");
						BACException.throwInstance((shownote==null||shownote.equals(""))?"����������"+opentime+"����":shownote);
					}
				}
				//���������Ƿ���ά��
				if(state==1) {
					if(userRs.getInt("devuser")!=1) {
						String shownote = serverRs.getString("shownote");
						BACException.throwInstance(((shownote==null||shownote.equals(""))?"������ά����":shownote)+(!ConfigBAC.getBoolean("openlogin")?"#1":""));
					}
				}
				boolean exist1 = dbHelper.queryExist("tab_player", "serverid="+Conf.sid+" and vsid="+vsid+" and userid="+userid);
				if(exist1){
					BACException.throwInstance("�û��ڴ˷������Ѿ�������ɫ userid="+userid+",vsid="+vsid);
				}
				boolean exist2 = dbHelper.queryExist("tab_player", "serverid="+Conf.sid+" and name='"+name+"'");
				if(exist2){
					BACException.throwInstance("��ɫ���Ѵ���");
				}
				
				SqlString sqlStr = new SqlString();
				sqlStr.add("userid", userid);
				sqlStr.add("serverid", Conf.sid);
				sqlStr.add("vsid", vsid);
				sqlStr.add("channel", channel);
				sqlStr.addDateTime("savetime", MyTools.getTimeStr());
				sqlStr.add("onlinestate", 0);
				sqlStr.add("onlinetime", 0);
				sqlStr.add("num", num);
				sqlStr.add("name", name);
				sqlStr.add("money", 0);
				sqlStr.add("coin", 0);
				sqlStr.add("lv", 1);
				sqlStr.add("exp", 0);
				sqlStr.add("vip", Conf.initvip);
				sqlStr.add("buycoin", 0);
				sqlStr.add("rebatecoin", 0);
				sqlStr.add("tqnum", 0);
				sqlStr.add("rechargermb", 0);
				sqlStr.add("rechargeam", 0);
				sqlStr.add("rechargtypes", (new JSONArray()).toString());
				sqlStr.addDateTime("resetdate", MyTools.getDaiylResetTime(5));//�ر�ע���һ�ε�¼���������ճ�����
				sqlStr.add("openfunc", (new JSONArray()).toString());
				sqlStr.add("enable", 1);
				sqlStr.add("isrobot", isrobot);
				if(isrobot == 1){
					sqlStr.addDateTime("logintime", MyTools.getTimeStr());
				}
				int playerid = insertByAutoID(dbHelper, sqlStr);
				
				PlaRoleBAC.getInstance().init(dbHelper, playerid);
				PlaFacBAC.getInstance().init(dbHelper, playerid);
				PlaWelfareBAC.getInstance().init(dbHelper, playerid);
				PlaSupplyBAC.getInstance().init(dbHelper, playerid);
				PlaSummonBAC.getInstance().init(dbHelper, playerid);
				PlaShopBAC.getInstance().init(dbHelper, playerid);
				if(partnernum != 0){
					PartnerBAC.getInstance().create(dbHelper, playerid, partnernum, partnerRs.getInt("awaken")==0?1:0, 1, 1, partnerRs.getInt("initstar"), null, null, null);
				}
				
				GameLog.getInst(playerid, GameServlet.ACT_PLAYER_CREATE)
				.addRemark("������ɫ��" + GameLog.formatNameID(name, playerid))
				.addRemark(partnerRs!=null?"ѡ���飺"+partnerRs.getString("name"):"δѡ����")
				.save();
				return new ReturnValue(true, String.valueOf(playerid));
			}
		} catch (Exception e) {
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		} finally {
			dbHelper.closeConnection();
		}
	}
	
	private static ArrayList<Integer> waituserList = new ArrayList<Integer>();//�Ŷӵ��û�
	private static ArrayList<Long> waitstarttimelist = new ArrayList<Long>();//��ʼ�Ŷ�ʱ��
	
	/**
	 * ��ɫ��¼
	 */
	public ReturnValue login(int userid, String client_sessionid, int vsid)
	{
		TimeTest timetest = new TimeTest("plalogin", "PLALOGIN", true, true, false);
		DBHelper dbHelper = new DBHelper();
		int remove_wait_uid = -1;
		try {
			//�����������������Ƿ�����
			synchronized (waituserList) {
				//�����ʱδ��������ĵȴ���ң��ͻ���3s��һ������
				for(int i = 0; i < waitstarttimelist.size(); ){
					if(System.currentTimeMillis()-waitstarttimelist.get(i)>8000){
						//System.out.println("UID:"+userid+"����ʱ��������ȴ����У���ʼʱ�䣺"+waitstarttimelist.get(0)+"��ǰʱ�䣺"+System.currentTimeMillis()+" waituserList:"+waituserList+" waitstarttimelist:"+waitstarttimelist);
						waituserList.remove(i);
						waitstarttimelist.remove(i);
					} else {
						i++;
					}
				}
				int onlineamount = SocketServer.getInstance().session_plamap.size();
				//��������
				if(onlineamount >= Conf.max_player){
					int wait_ind = waituserList.indexOf(userid);
					if(wait_ind == -1){
						waituserList.add(userid);
						waitstarttimelist.add(System.currentTimeMillis());
						//System.out.println("UID:"+userid+"���뵽�ȴ����� ʱ�䣺"+System.currentTimeMillis());
					} else {
						waitstarttimelist.set(wait_ind, System.currentTimeMillis());
						//System.out.println("UID:"+userid+"���µȴ�ʱ�䣺"+System.currentTimeMillis());
					}
					BACException.throwInstance("��������������,"+waituserList.indexOf(userid));
				}
				//���Ŷ��û�
				if(waituserList.size() > 0){
					int wait_ind = waituserList.indexOf(userid);
					//���ڶ����л�δ���������ķ�Χ
					if(wait_ind == -1 || wait_ind+1 > Conf.max_player-onlineamount){
						if(wait_ind == -1){
							waituserList.add(userid);
							waitstarttimelist.add(System.currentTimeMillis());
							//System.out.println("UID:"+userid+"���뵽�ȴ����� ʱ�䣺"+System.currentTimeMillis());
						} else {
							waitstarttimelist.set(wait_ind, System.currentTimeMillis());
							//System.out.println("UID:"+userid+"���µȴ�ʱ�䣺"+System.currentTimeMillis());
						}
						BACException.throwInstance("��������������,"+waituserList.indexOf(userid));
					}
					remove_wait_uid = userid;
				}
			}
			dbHelper.openConnection();
			ResultSet userRs = dbHelper.query(UserBAC.tab_user, "id,channel,username,wifi,devuser,onlinestate,serverid,playerid,sessionid,mac,imei,platform", "id="+userid);
			if(!userRs.next()){
				BACException.throwInstance("�û�δ�ҵ�");
			}
			if(userRs.getInt("onlinestate")==0){
				BACException.throwInstance("�ʺ��ѱ�ע���������µ�¼("+userid+")");
			}
			if(!ActivateCodeBAC.getInstance().checkActivate(dbHelper, userRs.getString("channel"), userRs.getString("username"))){
				BACException.throwInstance("�ʺ���δ����");
			}
			String sessionid = userRs.getString("sessionid");
			if(userRs.getInt("onlinestate")==1 && userRs.getInt("serverid")!=0 && userRs.getInt("playerid")!=0){
				if(!client_sessionid.equals(sessionid)){
					BACException.throwInstance("�ʺ�����������������¼("+userRs.getInt("serverid")+")�������µ�¼�ʺź���");
				} else {
					//System.out.println("error:��ɫ��¼�쳣���ͻ���Session("+client_sessionid+")�����ݿ���Ϣһ��");  //һ�±�ʲô�쳣��
				}
			}
			timetest.add("�û���֤");
			DBPsRs channelServerRs = ServerBAC.getInstance().getChannelServer(userRs.getString("channel"), vsid);
			if(!channelServerRs.next()){
				BACException.throwInstance("������δ�ҵ�");
			}
			if(channelServerRs.getInt("istest")==1 && userRs.getInt("devuser")!=1){
				BACException.throwInstance("������δ�ҵ�");
			}
			DBPaRs serverRs = DBPool.getInst().pQueryA(ServerBAC.tab_server, "id="+Conf.sid);
			int state = channelServerRs.getInt("state")!=-1?channelServerRs.getInt("state"):serverRs.getInt("state");
			String opentime = !channelServerRs.getString("opentime").equals("-1")?channelServerRs.getString("opentime"):serverRs.getString("opentime");
			//���������Ƿ��ѿ���
			if(!MyTools.checkSysTimeBeyondSqlDate(MyTools.getTimeLong(opentime))) {
				if(userRs.getInt("devuser")!=1){
					String shownote = serverRs.getString("shownote");
					BACException.throwInstance((shownote==null||shownote.equals(""))?"����������"+opentime+"����":shownote);
				}
			}
			//���������Ƿ���ά��
			if(state==1) {
				if(userRs.getInt("devuser")!=1) {
					String shownote = serverRs.getString("shownote");
					BACException.throwInstance(((shownote==null||shownote.equals(""))?"������ά����":shownote)+(!ConfigBAC.getBoolean("openlogin")?"#1":""));
				}
			}
			timetest.add("��������֤");
			//��ȡ��ɫ����
			ResultSet dataRs = dbHelper.query("tab_player", null, "serverid="+Conf.sid+" and vsid="+vsid+" and userid="+userid);
			if(!dataRs.next()){
				BACException.throwInstance("��δ������ɫ");
			}
			int psid = dataRs.getInt("serverid");
			if(psid != Conf.sid){
				BACException.throwInstance("��¼�������쳣("+psid+"/"+Conf.sid+")");
			}
			int enable = dataRs.getInt("enable");
			if(enable==-1 || (enable==0 && !MyTools.checkSysTimeBeyondSqlDate(dataRs.getTimestamp("blankofftime")))){
				BACException.throwInstance("��ɫ�ѱ�����");
			}
			timetest.add("��ɫ��֤");
			//׼������
			int playerid = dataRs.getInt("id");
			String dateStr = Tools.getCurrentDateStr();
			String timeStr = Tools.getCurrentDateTimeStr();
			boolean firstlogin = dataRs.getString("logintime")==null;
			long lastlogintime = MyTools.getTimeLong(dataRs.getTimestamp("logintime"));
			GameLog gl = GameLog.getInst(playerid, GameServlet.ACT_PLAYER_LOGIN);
			//Socket��¼
			Player pla = new Player(sessionid, userid, playerid, dataRs.getString("name"), MyTools.getTimeLong(dataRs.getTimestamp("savetime")), new JSONArray(dataRs.getString("openfunc")));
			pla.initUserInfo(userRs);
			SocketServer.getInstance().plamap.put(pla.pid, pla);
			int old_amount = SocketServer.getInstance().session_plamap.size();
			SocketServer.getInstance().session_plamap.put(pla.sessionid, pla);
			SocketServer.getInstance().connectlog.d("�����ɫ��" + pla.pname + "("+ pla.pid + "," + pla.sessionid +")" + " ������" + old_amount + " -> " + SocketServer.getInstance().session_plamap.size());
			timetest.add("SOCKETע��");
			//��¼��ɫ��־
			createLoginLog(playerid, userid, dateStr, timeStr);
			//�û���Ϣ����
			SqlString userSqlStr = new SqlString();
			userSqlStr.add("serverid", Conf.sid);
			userSqlStr.add("playerid", playerid);
			UserBAC.getInstance().update(dbHelper, userid, userSqlStr);
			//��¼��ɫ��Ϣ����
			SqlString sqlStr = new SqlString();
			sqlStr.addDateTime("logintime", timeStr);
			sqlStr.add("onlinestate", 1);
			sqlStr.add("sessionid", sessionid);
			update(dbHelper, playerid, sqlStr);
			timetest.add("���µ�¼��Ϣ");
			//�ָ�����
			PlaRoleBAC.getInstance().recoverEnergy(dbHelper, playerid, gl);
			//�ָ���ս����
			PlaRoleBAC.getInstance().recoverArtifactRobTimes(dbHelper, playerid, gl);
			//��鲢ѡ���Ƿ�����ճ�����
			checkAndResetDayData(dbHelper, playerid, false, firstlogin, gl);
			//��ȡ��ɫ��Ϣ
			JSONObject json_data = getAllData(dbHelper, playerid, true, true, true, true);
			timetest.add("��ȡ��ɫ��Ϣ");
			//֪ͨ����
			DBPaRs plafacRs = PlaFacBAC.getInstance().getDataRs(playerid);
			PushData.getInstance().sendPlaToFansAndNosFac(SocketServer.ACT_PLAYER_ONLINE, String.valueOf(playerid), plafacRs.getInt("factionid"), playerid, FriendBAC.TYPE_ALL);
			timetest.add("֪ͨ����");
			//֧������
			JSONArray chargeArr = ChargeBAC.getInstance().getChargeType(userRs.getString("channel"));
			json_data.put("charge", chargeArr);
			timetest.add("��������");
			timetest.save(1000);
			
			gl.save();
			return new ReturnValue(true, json_data.toString());
		} catch(Exception e){
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		} finally {
			if(remove_wait_uid != - 1){
				synchronized (waituserList) {
					int remove_wait_ind = waituserList.indexOf(remove_wait_uid);
					if(remove_wait_ind != -1){
						waituserList.remove(remove_wait_ind);
						waitstarttimelist.remove(remove_wait_ind);
					}
				}	
			}
			dbHelper.closeConnection();
		}
	}
	
	/**
	 * ������
	 */
	public ReturnValue openPush(int playerid){
		try {
			Player pla = SocketServer.getInstance().plamap.get(playerid);
			if(pla!=null){
				pla.openPush();
			}
			return new ReturnValue(true);
		} catch (Exception e) {
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		}
	}
	
	/**
	 * ��������
	 */
	public ReturnValue updateOnlineState(int playerid, short mark){
		try {
			Player pla = SocketServer.getInstance().plamap.get(playerid);
			if(pla!=null){
				pla.updateOnlineState(mark);
			}
			return new ReturnValue(true);
		} catch (Exception e) {
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		}
	}
	
	/**
	 * ��ɫ�ǳ�
	 */
	public ReturnValue logout(int playerid, String reason){
		DBHelper dbHelper = new DBHelper();
		try {
			dbHelper.openConnection();
			logout(dbHelper, playerid, reason);
			return new ReturnValue(true);
		} catch (Exception e) {
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		} finally {
			dbHelper.closeConnection();
		}
	}
	
	/**
	 * ֪ͨ��������
	 */
	public ReturnValue beOffline(String sessionid){
		DBHelper dbHelper = new DBHelper();
		try {
			dbHelper.openConnection();
			Player pla = SocketServer.getInstance().session_plamap.get(sessionid);
			if(pla != null){
				try {
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					DataOutputStream baos_dos = new DataOutputStream(baos);
					baos_dos.writeShort(SocketServer.ACT_SYS_BEOFFLINE);
					baos_dos.writeShort(-100);//��ʾ����MARKƥ��
					baos_dos.write("����ʺ��������ط����ߣ��㽫�������ߡ�".getBytes("UTF-8"));
					baos_dos.close();
					byte[] pushdata = baos.toByteArray();
					SocketServer.getInstance().exePush(pla.dos, pushdata);	
				} catch (Exception e) {}
				logout(dbHelper, pla.pid, "��������");
			}
			return new ReturnValue(true);
		} catch (Exception e) {
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		} finally {
			dbHelper.closeConnection();
		}
	}
	
	/**
	 * ��̨��ȡ�������
	 */
	public ReturnValue bkGetAllData(int playerid) {
		DBHelper dbHelper = new DBHelper();
		try {
			dbHelper.openConnection();
			//��������
			JSONObject jsonobj = getAllData(dbHelper, playerid, true, true, false, false);
			//TODO ��������
			return new ReturnValue(true, jsonobj.toString());
		} catch(Exception e){
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		} finally {
			dbHelper.closeConnection();
		}
	}
	
	/**
	 * �ӻ����ȡ��ɫ����
	 */
	public ReturnValue getAllData(int playerid, int targetid){
		DBHelper dbHelper = new DBHelper();
		try {
			dbHelper.openConnection();
			JSONObject jsonobj = getAllData(dbHelper, targetid);
			return new ReturnValue(true, jsonobj.toString());
		} catch(Exception e){
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		} finally {
			dbHelper.closeConnection();
		}
	}
	
	/**
	 * ����������ճ�����
	 */
	public ReturnValue checkAndResetDayData(int playerid, boolean must){
		DBHelper dbHelper = new DBHelper();
		try {
			if(!Conf.debug && must){
				BACException.throwInstance("�Ƿ�����");
			}
			synchronized (LockStor.getLock(LockStor.PLAYER_RESET_DAYDATE)) {
				dbHelper.openConnection();
				GameLog gl = GameLog.getInst(playerid, GameServlet.ACT_DEBUG_RESETDAYDATE);
				JSONObject returnobj = checkAndResetDayData(dbHelper, playerid, must, false, gl);
				
				gl.save();
				return new ReturnValue(true, returnobj.toString());		
			}
		} catch (Exception e) {
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		} finally {
			dbHelper.closeConnection();
		}
	}
	
	/**
	 * ����
	 */
	public ReturnValue rename(int playerid, String newName){
		DBHelper dbHelper = new DBHelper();
		try {
			synchronized (LockStor.getLock(LockStor.PLAYER_NAME)) {
				MyTools.checkNoCharEx(newName, '#');
				if(newName.equals("") || newName.toLowerCase().equals("null")){
					BACException.throwInstance("���ֲ����ã�����ĺ�����");
				}
				DBPaRs plaRs = getDataRs(playerid);
				String oldName = plaRs.getString("name");
				dbHelper.openConnection();
				GameLog gl = GameLog.getInst(playerid, GameServlet.ACT_PLAYER_RENAME);
				boolean exist2 = dbHelper.queryExist("tab_player", "serverid="+Conf.sid+" and name='"+newName+"'");
				if(exist2){
					BACException.throwInstance("��ɫ���Ѵ���");
				}
				if(!oldName.contains("#")){
					useCoin(dbHelper, playerid, 100, gl);
				}
				SqlString sqlStr = new SqlString();
				sqlStr.add("name", newName);
				update(dbHelper, playerid, sqlStr);
				
				JSONArray pusharr = new JSONArray();
				pusharr.add(playerid);
				pusharr.add(newName);
				PushData.getInstance().sendPlaToFans(SocketServer.ACT_PLAYER_RENAME, pusharr.toString(), playerid, FriendBAC.TYPE_ALL);
				
				
				gl.addRemark("ԭ��"+oldName+" ����Ϊ"+newName)
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
	 * ����ͷ��
	 */
	public ReturnValue setFace(int playerid, int num){
		DBHelper dbHelper = new DBHelper();
		try {
			DBPaRs plaRs = getDataRs(playerid);
			int oldNum = plaRs.getInt("num");
			if(oldNum == num){
				BACException.throwInstance("ͷ���޸ı�");
			}
			SqlString sqlStr = new SqlString();
			sqlStr.add("num", num);
			update(dbHelper, playerid, sqlStr);
			
			GameLog.getInst(playerid, GameServlet.ACT_PLAYER_SETFACE)
			.addRemark("ԭͷ��"+oldNum+" ����ͷ��Ϊ"+num)
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
	 * WEB��ȡ������
	 * @param serverid ������ID
	 * @param type 0.�������� 1.�ȼ����� 2.������������
	 */
	public ReturnValue WebGetRanking(byte type){
		DBHelper dbHelper = new DBHelper();
		try {
			JSONArray jsonarr = new JSONArray();//�������� ������ ���������� ���� �û��� �ȼ�/ս��
			//TODO ��ȡ��Ϸ���й�WEBչʾ
			return new ReturnValue(true, jsonarr.toString());
		} catch(Exception e){
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		} finally {
			dbHelper.closeConnection();
		}
	}
	
	/**
	 * �ı�ֵ
	 */
	public ReturnValue changeValue(int playerid, byte type, String changevalue, String from){
		DBHelper dbHelper = new DBHelper();
		try {
			dbHelper.openConnection();
			GameLog gl = GameLog.getInst(playerid, GameServlet.ACT_PLAYER_CHANGEVALUE);
			
			DBPaRs pctRs = DBPool.getInst().pQueryA(tab_player_change_type, "type="+type);
			
			String classname = pctRs.getString("classname");
			String columnName = pctRs.getString("columnname");
			String columnDesc = pctRs.getString("name");
			
			SqlString sqlStr = new SqlString();
			if(pctRs.getInt("changetype") == 1){
				sqlStr.addChange(columnName, Tools.str2int(changevalue));	
			} else 
			if(pctRs.getInt("changetype") == 2){
				sqlStr.add(columnName, changevalue);
			} else 
			if(pctRs.getInt("changetype") == 3){
				sqlStr.addDateTime(columnName, MyTools.getTimeStr(MyTools.getTimeLong(changevalue)));
			}
			PlaBAC plaBac = (PlaBAC)MirrorMgr.classname_mirror.get("com.moonic.bac."+classname);
			DBPaRs rs = plaBac.getDataRs(playerid);
			String oldvalue = rs.getString(columnName);
			plaBac.update(dbHelper, playerid, sqlStr);
			if(pctRs.getInt("changetype") == 1){
				gl.addChaNote(columnDesc, Tools.str2int(oldvalue), Tools.str2int(changevalue));	
			} else 
			if(pctRs.getInt("changetype") == 2){
				gl.addRemark("����"+columnDesc+"Ϊ"+changevalue);
			} else 
			if(pctRs.getInt("changetype") == 3){
				gl.addRemark("����"+columnDesc+"Ϊ"+changevalue);
			}
			
			JSONArray pusharr = new JSONArray();
			pusharr.add(type);
			pusharr.add(changevalue);
			PushData.getInstance().sendPlaToOne(SocketServer.ACT_PLAYER_CHANGEVALUE, pusharr.toString(), playerid);
			
			gl.addRemark("��Դ��"+from);
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
	 * ���ԼӾ���
	 */
	public ReturnValue debugAddExp(int playerid, long addexp){
		DBHelper dbHelper = new DBHelper();
		try {
			dbHelper.openConnection();
			if(addexp <= 0){
				BACException.throwInstance("����ֵ����С��0");
			}
			GameLog gl = GameLog.getInst(playerid, GameServlet.ACT_DEBUG_GAME_LOG);
			addExp(dbHelper, playerid, addexp, gl);
			
			gl.addRemark("���ԼӾ���");
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
	 * ���Կ��ٳɳ�
	 */
	public ReturnValue debugShortcutGrow(int playerid, JSONArray openfunc, int num){
		try {
			GameLog gl = GameLog.getInst(playerid, GameServlet.ACT_DEBUG_GAME_LOG);
			DBPaRs plaRs = getDataRs(playerid);
			int plalv = plaRs.getInt("lv");
			int plaexp = plaRs.getInt("exp");
			shortcutGrow(playerid, num, plalv, plaexp, openfunc, gl);
			
			gl.addRemark("���Կ��ٳɳ�");
			gl.save();
			return new ReturnValue(true);
		} catch (Exception e) {
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		}
	}
	
	/**
	 * ���ٳɳ�
	 */
	public void shortcutGrow(int playerid, int num, int plalv, int plaexp, JSONArray openfunc, GameLog gl) throws Exception {
		DBHelper dbHelper = new DBHelper();
		try {
			DBPaRs shortcutgrowRs = DBPool.getInst().pQueryA(tab_shortcut_grow, "num="+num);
			if(!shortcutgrowRs.exist()){
				BACException.throwInstance("���ٳɳ���� "+num+" δ�ҵ�");
			}
			//���ǵȼ�
			int tgr_plalv = shortcutgrowRs.getInt("plalv");
			if(plalv < tgr_plalv){
				int needexp = (int)DBPool.getInst().pQueryS(tab_player_uplv, "lv>="+plalv+" and lv<"+tgr_plalv).sum("needexp")-plaexp;
				addExp(dbHelper, playerid, needexp, gl);
			}
			//��ͭǮ
			int addmoney = shortcutgrowRs.getInt("money");
			if(addmoney > 0){
				addValue(dbHelper, playerid, "money", addmoney, gl, GameLog.TYPE_MONEY);
			}
			//�ӽ�
			int addcoin = shortcutgrowRs.getInt("coin");
			if(addcoin > 0){
				addValue(dbHelper, playerid, "coin", addcoin, gl, GameLog.TYPE_COIN);
			}
			//�ӻ��
			int[] parnumarr = Tools.splitStrToIntArr(shortcutgrowRs.getString("parnum"), ",");
			while(parnumarr[0] == 2 && parnumarr[0] > 6){
				parnumarr = Tools.removeOneFromIntArr(parnumarr, MyTools.getRandom(1, parnumarr.length-1));
			}
			int parlv = shortcutgrowRs.getInt("parlv");
			int parstar = shortcutgrowRs.getInt("parstar");
			int parphase = shortcutgrowRs.getInt("parquality");
			String parequipStr = shortcutgrowRs.getString("parequip");
			for(int i = 1; i < parnumarr.length; i++){
				DBPaRs partnerRs = DBPool.getInst().pQueryA(PartnerBAC.tab_partner, "num="+parnumarr[i]);
				int star = 0;
				if(parstar != -1){
					star = parstar;
				} else {
					star = partnerRs.getInt("initstar");
				}
				int[] equiparr = new int[6];
				int[][] equipdataarr = PartnerBAC.getInstance().converEquipStateToData(parequipStr);
				for(int k = 0; k < equipdataarr.length; k++){
					if(equipdataarr[k][0] != 0){
						JSONArray itemarr = ItemBAC.getInstance().add(dbHelper, playerid, ItemBAC.TYPE_EQUIP_ORDINARY, equipdataarr[k][0], 1, ItemBAC.ZONE_USE, ItemBAC.SHORTCUT_MAIL, new JSONArray(new int[]{equipdataarr[k][1], equipdataarr[k][2]}), 1, gl);
						equiparr[k] = itemarr.optJSONObject(0).optInt("id");		
					}
				}
				int[] orbarr = PartnerBAC.getInstance().converOrbStateToNum(parphase, partnerRs.getString("upphasenum"), shortcutgrowRs.getString("parorb"));
				int[] skilvarr = Tools.splitStrToIntArr(shortcutgrowRs.getString("skilllv"), ",");
				PartnerBAC.getInstance().create(dbHelper, playerid, parnumarr[i], partnerRs.getInt("awaken")==0?1:0, parlv, parphase, star, equiparr, orbarr, skilvarr);
			}
			//������Ʒ
			String haveitem = shortcutgrowRs.getString("haveitem");
			if(!haveitem.equals("-1")){
				AwardBAC.getInstance().getAward(dbHelper, playerid, haveitem, ItemBAC.SHORTCUT_MAIL, -1, gl);
			}
			//��������
			FunctionBAC.getInstance().debugOpenAllFunc(playerid, openfunc, new int[]{1002});
			//����ս��
			PartnerBAC.getInstance().updateBattlePower(dbHelper, playerid);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			dbHelper.closeConnection();
		}
	}
	
	/**
	 * �Ӿ���
	 */
	public JSONArray addExp(DBHelper dbHelper, int playerid, long addexp, GameLog gl) throws Exception {
		DBPaRs plaRs = getDataRs(playerid);
		int oldlv = plaRs.getInt("lv");
		int oldexp = plaRs.getInt("exp");
		JSONArray jsonarr = ExpBAC.getInstance().addExp(tab_player_uplv, oldlv, oldexp, addexp, 0, "����", gl);
		if(jsonarr != null){
			int newlv = jsonarr.optInt(0);
			int newexp = jsonarr.optInt(1);
			SqlString sqlStr = new SqlString();
			sqlStr.add("lv", newlv);
			sqlStr.add("exp", newexp);
			update(dbHelper, playerid, sqlStr);
			if(newlv > oldlv){
				PlaRoleBAC.getInstance().upLevelOperate(dbHelper, playerid, oldlv, newlv, gl);
				int factionid = PlaFacBAC.getInstance().getIntValue(playerid, "factionid");
				JSONArray pusharr = new JSONArray();
				pusharr.add(playerid);
				pusharr.add(newlv);
				PushData.getInstance().sendPlaToFansAndNosFac(SocketServer.ACT_PLAYER_LVUP, pusharr.toString(), factionid, playerid, FriendBAC.TYPE_ALL);
			}
		}
		return jsonarr;
	}
	
	/**
	 * ��ȡ�����ߵȼ�
	 */
	public int getMaxPartnerLv(int playerid) throws Exception {
		DBPaRs plaRs = PlayerBAC.getInstance().getDataRs(playerid);
		DBPaRs plauplvRs = DBPool.getInst().pQueryA(PlayerBAC.tab_player_uplv, "lv="+plaRs.getInt("lv"));
		int partnermaxlv = plauplvRs.getInt("partnermaxlv");
		return partnermaxlv;
	}
	
	/**
	 * ��ȡ�������
	 */
	public int getMaxEnergy(int playerid) throws Exception {
		DBPaRs plaRs = PlayerBAC.getInstance().getDataRs(playerid);
		DBPaRs plauplvRs = DBPool.getInst().pQueryA(PlayerBAC.tab_player_uplv, "lv="+plaRs.getInt("lv"));
		int maxenergy = plauplvRs.getInt("maxenergy");
		return maxenergy;
	}
	
	/**
	 * ������¼��Ϸ��������־
	 */
	public void createLoginLog(int playerid, int userid, String dateStr, String timeStr) {
		SqlString sqlStr = new SqlString();
		sqlStr.add("playerid", playerid);
		sqlStr.add("userid", userid);
		sqlStr.add("serverid", Conf.sid);
		sqlStr.addDate("logindate", dateStr);
		sqlStr.addDateTime("logintime", timeStr);
		DBHelper.logInsert(LogTbName.TAB_PLAYER_LOGIN_LOG(), sqlStr);
	}
	
	/**
	 * ��ɫ����
	 */
	public ReturnValue blankOffPlayer(int playerid, String date) {
		DBHelper dbHelper = new DBHelper();
		try {
			dbHelper.openConnection();
			SqlString sqlStr = new SqlString();
			if (date != null && !"".equals(date)) {
				sqlStr.addDateTime("blankofftime", date);
				sqlStr.add("enable", 0);
			} else {
				sqlStr.add("enable", -1);
			}
			update(dbHelper, playerid, sqlStr);
			return new ReturnValue(true);
		} catch (Exception e) {
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		} finally {
			dbHelper.closeConnection();
		}
	}
	
	/**
	 * ��ɫ���
	 */
	public ReturnValue unBlankOffPlayer(int playerid) {
		DBHelper dbHelper = new DBHelper();
		try {
			dbHelper.openConnection();
			SqlString sqlStr = new SqlString();
			sqlStr.add("enable", 1);
			update(dbHelper, playerid, sqlStr);
			return new ReturnValue(true);
		} catch (Exception e) {
			e.printStackTrace();
			return new ReturnValue(true);
		} finally {
			dbHelper.closeConnection();
		}
	}
	
	/**
	 * ��ɫ����
	 */
	public ReturnValue bannedToPostPlayer(int playerid, String date) {
		DBHelper dbHelper = new DBHelper();
		try {
			dbHelper.openConnection();
			SqlString sqlStr = new SqlString();
			sqlStr.addDateTime("bannedmsgtime", date);
			update(dbHelper, playerid, sqlStr);
			
			GamePushData.getInstance(1).sendToOne(playerid);
			return new ReturnValue(true);
		} catch (Exception e) {
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		} finally {
			dbHelper.closeConnection();
		}
	}

	/**
	 * ��ɫ�������
	 */
	public ReturnValue unBannedToPostPlayer(int playerid) {
		return bannedToPostPlayer(playerid, MyTools.getTimeStr());
	}
	
	/**
	 * ��ȡ��ɫ����ʱ��
	 */
	public long getOnlineTimeLen(int playerid) throws Exception {
		DBPaRs plaRs = getDataRs(playerid);
		return plaRs.getInt("onlinetime")+(System.currentTimeMillis()-Math.max(MyTools.getCurrentDateLong(), plaRs.getTime("logintime")));
	}
	
	/**
	 * ��ɫ�ǳ�
	 */
	public void logout(DBHelper dbHelper, int playerid, String reason) throws Exception {
		//System.out.println("--------------logout---"+playerid+"----------------");
		DBPaRs plaRs = getDataRs(playerid);
		if(plaRs.getInt("onlinestate")==1) 
		{
			GameLog gl = GameLog.getInst(playerid, GameServlet.ACT_PLAYER_LOGOUT);
			long onlinetimelen = System.currentTimeMillis()-Math.max(MyTools.getCurrentDateLong(), plaRs.getTime("logintime"));
			//�˳��ŶӸ���
			FacCopymapBAC.getInstance().exit(dbHelper, playerid);
			//����ս���־�����
			PartnerBAC.getInstance().updateBattlePower(dbHelper, playerid);
			//�˳���ӻ
			PlaTeamBAC.getInstance().logout(playerid);
			//���������Ϣ
			SqlString sqlStr = new SqlString();
			sqlStr.add("sessionid", "0");
			sqlStr.add("onlinestate", 0);
			sqlStr.addChange("onlinetime", onlinetimelen);
			update(dbHelper, playerid, sqlStr);
			//ע����־
			long currenttime = System.currentTimeMillis();
			int onlinetime = (int)((currenttime - plaRs.getTime("logintime"))/1000);
			SqlString logoutSqlStr = new SqlString();
			logoutSqlStr.add("playerid", playerid);
			logoutSqlStr.add("userid", plaRs.getInt("userid"));
			logoutSqlStr.add("serverid", plaRs.getInt("serverid"));
			logoutSqlStr.addDate("logindate", MyTools.getDateStr(plaRs.getTime("logintime")));
			logoutSqlStr.addDateTime("logintime", MyTools.getTimeStr(plaRs.getTime("logintime")));
			logoutSqlStr.addDateTime("logouttime", MyTools.getTimeStr(currenttime));
			logoutSqlStr.add("onlinetime", onlinetime);
			DBHelper.logInsert(LogTbName.TAB_PLAYER_LOGIN_LOG(), logoutSqlStr);
			//֪ͨ����
			DBPaRs plafacRs = PlaFacBAC.getInstance().getDataRs(playerid);
			PushData.getInstance().sendPlaToFansAndNosFac(SocketServer.ACT_PLAYER_OFFLINE, String.valueOf(playerid), plafacRs.getInt("factionid"), playerid, FriendBAC.TYPE_ALL);
			//Socketע��
			SocketServer.getInstance().removePla(playerid, reason);
			//�����û���Ϣ
			SqlString userSqlStr = new SqlString();
			userSqlStr.add("serverid", 0);
			userSqlStr.add("playerid", 0);
			UserBAC.getInstance().update(dbHelper, plaRs.getInt("userid"), userSqlStr);
			
			gl.addRemark("ע��ԭ��:"+reason);
			gl.save();
		}
	}
	
	/**
	 * ��ȡ��ɫ�����л�������(��ȡ������ɫ����)
	 */
	public JSONObject getAllData(DBHelper dbHelper, int playerid) throws Exception{
		return getAllData(dbHelper, playerid, true, false, false, false);
	}
	
	/**
	 * ��ȡ��ɫ�����л�������
	 * @param deputy ������(��̨�鿴��ɫ��Ϣ����)
	 * @param acti �����
	 * @param info ��Ϣ����
	 */
	public JSONObject getAllData(DBHelper dbHelper, int playerid, boolean interactive, boolean deputy, boolean acti, boolean info) throws Exception{
		JSONObject json_data = new JSONObject();
		//�������
		JSONArray plaarr = getData(playerid);
		json_data.put("player", plaarr);
		/*--����������--*/
		if(interactive){
			//��ɫ����
			JSONArray plarolearr = PlaRoleBAC.getInstance().getLoginData(playerid);
			json_data.put("plarole", plarolearr);
			//��Ʒ����
			JSONArray itemarr = ItemBAC.getInstance().getItemList(playerid);
			json_data.put("item", itemarr);
			//��ɫ����
			JSONArray plafacarr = PlaFacBAC.getInstance().getLoginData(playerid);
			json_data.put("plafac", plafacarr);
			//����
			JSONArray facarr = FactionBAC.getInstance().getLoginData(playerid);
			json_data.put("fac", facarr);
			//���
			JSONArray partnerarr = PartnerBAC.getInstance().getLoginData(playerid);
			json_data.put("partner", partnerarr);
		}
		/*--���˸�����--*/
		if(deputy){
			//����
			JSONArray friarr = FriendBAC.getInstance().getLoginData(playerid);
			json_data.put("friend", friarr);
			//����
			JSONObject copymap = CopymapBAC.getInstance().getLoginData(playerid);
			json_data.put("copymap", copymap);
			//��ɫ����
			JSONArray welfare = PlaWelfareBAC.getInstance().getData(playerid);
			json_data.put("plawelfare", welfare);
			//��ɫ����
			JSONArray plasupplyarr = PlaSupplyBAC.getInstance().getLoginData(playerid);
			json_data.put("plasupply", plasupplyarr);
			//��ɫ�����̵�
		//	JSONArray plamysarr = PlaMysteryShopBAC.getInstance().getData(playerid);
		//	json_data.put("plamys", plamysarr);
			//��ɫͭǮ����
			JSONArray plamoney = PlaTrialMoneyBAC.getInstance().getData(playerid);
			json_data.put("plamoney", plamoney);
			//��ɫ��������
			JSONArray plaexp = PlaTrialExpBAC.getInstance().getData(playerid);
			json_data.put("plaexp", plaexp);
			//��ɫ�������
			JSONObject plapartner = PlaTrialPartnerBAC.getInstance().getData(playerid);
			json_data.put("plapartner", plapartner);
			//��ɫ�ٻ�
			JSONArray plasummon = PlaSummonBAC.getInstance().getData(playerid);
			json_data.put("plasummon", plasummon);
			//��ɫ�ֻ���
			JSONArray platower = PlaTowerBAC.getInstance().getData(playerid);
			json_data.put("platower", platower);
		}
		/*--�����--*/
		if(acti){
			//�Զ���
			JSONArray customactiarr = CustomActivityBAC.getInstance().getLoginData(playerid);
			json_data.put("costomacti", customactiarr);
			//����BOSS
			JSONArray worldboss = WorldBossBAC.getInstance().getLoginData();
			json_data.put("worldboss", worldboss);
			//��ӻ
			JSONArray team = PlaTeamBAC.getInstance().getLoginData();
			json_data.put("team", team);
		}
		/*--��Ϣ����--*/
		if(info){
			JSONArray sysarr = getSysData();
			json_data.put("sys", sysarr);
			JSONArray mailarr = MailBAC.getInstance().getMailList(dbHelper, playerid);
			json_data.put("mail", mailarr);
		}
		return json_data;
	}
	
	/**
	 * ��ȡ�������
	 */
	public JSONArray getData(int playerid) throws Exception {
		JSONArray arr = new JSONArray();
		DBPaRs rs = getDataRs(playerid);
		arr.add(rs.getInt("id"));//0.ID
		arr.add(rs.getInt("num"));//1.���
		arr.add(rs.getInt("onlinetime"));//2.����ʱ��
		arr.add(rs.getString("name"));//3.����
		arr.add(rs.getInt("money"));//5.ͭǮ
		arr.add(rs.getInt("coin"));//6.��
		arr.add(rs.getInt("lv"));//9.�ȼ�
		arr.add(rs.getInt("exp"));//10.��ǰ����
		arr.add(rs.getInt("vip"));//11.VIP
		arr.add(rs.getInt("buycoin"));//12.���������
		arr.add(rs.getInt("rebatecoin"));//13.���ͽ�����
		arr.add(rs.getInt("tqnum"));//14.��Ȩ���
		arr.add(rs.getTime("tqduetime"));//15.��Ȩ����ʱ��
		arr.add(rs.getInt("rechargermb"));//16.��ֵRMB�ܶ�
		arr.add(new JSONArray(rs.getString("openfunc")));//17.�ѿ�������JSONARR
		arr.add(rs.getTime("bannedmsgtime"));//18.���Խ���ʱ��
		arr.add(new JSONArray(rs.getString("rechargtypes")));//19.�ѹ�����Ľ��
		arr.add(rs.getTime("savetime"));//��ɫ����ʱ��
		return arr;
	}
	
	/**
	 * ��ȡϵͳ����
	 */
	public JSONArray getSysData() throws Exception {
		JSONArray arr = new JSONArray();
		arr.add(System.currentTimeMillis());//ϵͳʱ��
		arr.add(ServerBAC.getInstance().getOpenTime());//����ʱ��
		arr.add(Conf.worldLevel);//����ȼ�
		return arr;
	}
	
	/**
	 * ����������ճ�����
	 */
	public JSONObject checkAndResetDayData(DBHelper dbHelper, int playerid, boolean must, boolean firstlogin, GameLog gl) throws Exception {
		JSONObject returnobj = new JSONObject();
		DBPaRs plaRs = getDataRs(playerid);
		long resetdate = plaRs.getTime("resetdate");
		boolean needreset = must || MyTools.checkSysTimeBeyondSqlDate(resetdate);
		if(needreset){
			//���ý�ɫ����
			SqlString sqlStr = new SqlString();
			sqlStr.add("onlinetime", 0);
			sqlStr.addDateTime("resetdate", MyTools.getDaiylResetTime(5));
			update(dbHelper, playerid, sqlStr);
			long starttime = resetdate-MyTools.long_day;
			long endtime = System.currentTimeMillis();
			boolean weekReset = MyTools.checkWeek(starttime, endtime);
			boolean moonReset = MyTools.checkMonth(starttime, endtime);
			//���ý�ɫ��������
			PlaFacBAC.getInstance().resetData(dbHelper, playerid, resetdate);
			//���ð����ճ�����
			FactionBAC.getInstance().checkAndResetDayData(dbHelper, playerid, must);
			returnobj.put("result", 1);
			//�������о�������
			PlaJJCRankingBAC.getInstance().resetData(dbHelper, playerid);
			//���ø���ÿ������
			CopymapBAC.getInstance().resetData(dbHelper, playerid);
			//���ý�ɫ��������
			PlaWelfareBAC.getInstance().resetData(dbHelper, playerid, moonReset);
			//���ò�������
			PlaSupplyBAC.getInstance().resetData(dbHelper, playerid);
			//����ÿ���ٻ�����
			PlaSummonBAC.getInstance().resetData(dbHelper, playerid);
			//����ÿ���ֻ�������
			PlaTowerBAC.getInstance().resetData(dbHelper, playerid, returnobj);
			//����
			PlaRoleBAC.getInstance().resetData(dbHelper, playerid);
			//�����̵�����
			PlaShopBAC.getInstance().resetData(dbHelper, playerid);
		} else {
			returnobj.put("result", 0);
			returnobj.put("time", System.currentTimeMillis());
		}
		if(needreset || firstlogin){
			//����
			CustomActivityBAC.getInstance().supplement(dbHelper, playerid, plaRs.getString("channel"));
			//������¼����
			CustomActivityBAC.getInstance().updateProcess(dbHelper, playerid, 6);
		}
		return returnobj;
	}
	
	/**
	 * ��ԭ���߽�ɫ
	 */
	public void restoreOnLinePla(){
		DBHelper dbHelper = new DBHelper();
		try {
			dbHelper.openConnection();
			ResultSet plaRs = dbHelper.query("tab_player", "id,sessionid,userid,name,savetime,openfunc", "serverid="+Conf.sid+" and onlinestate=1");
			while(plaRs.next()){
				Player pla = new Player(plaRs.getString("sessionid"), plaRs.getInt("userid"), plaRs.getInt("id"), plaRs.getString("name"), MyTools.getTimeLong(plaRs.getTimestamp("savetime")), new JSONArray(plaRs.getString("openfunc")));
				SocketServer.getInstance().plamap.put(pla.pid, pla);
				int old_amount = SocketServer.getInstance().session_plamap.size();
				SocketServer.getInstance().session_plamap.put(pla.sessionid, pla);
				pla.startBreakLineTT(MyTools.long_minu*5, "��ԭ����");
				SocketServer.getInstance().connectlog.d("�����ɫ��" + pla.pname + "("+ pla.pid + "," + pla.sessionid +")" + " ������" + old_amount + " -> " + SocketServer.getInstance().session_plamap.size());
			}
		} catch(Exception e){
			e.printStackTrace();
		} finally {
			dbHelper.closeConnection();
		}
	}
	
	/**
	 * ʹ��ͭǮ
	 */
	public void useMoney(DBHelper dbHelper, int playerid, int useamount, GameLog gl) throws Exception {
		useMoney(dbHelper, playerid, useamount, true, true, gl);
	}
	
	/**
	 * ʹ��ͭǮ
	 */
	public void useMoney(DBHelper dbHelper, int playerid, int useamount, boolean sys_change, boolean ca_change, GameLog gl) throws Exception {
		if(useamount <= 0){
			String str = "����ֵʧ�� �޸��ͭǮ ����ֵ��" + useamount;
			try {
				throw new Exception(str);
			} catch (Exception e) {
				e.printStackTrace();
			}
			BACException.throwInstance(str);
		}
		DBPaRs plaRs = getDataRs(playerid);
		int money = plaRs.getInt("money");
		if(money < useamount){
			BACException.throwInstance("ͭǮ����("+money+"/"+useamount+")");
		}
		SqlString sqlStr = new SqlString();
		sqlStr.addChange("money", -useamount);
		update(dbHelper, playerid, sqlStr);
		
		CustomActivityBAC.getInstance().updateProcess(dbHelper, playerid, 9, useamount);
		
		gl.addChaNote(GameLog.TYPE_MONEY, money, -useamount, sys_change);
	}
	
	/**
	 * ʹ�ý�
	 * @param useamount ʹ������
	 */
	public void useCoin(DBHelper dbHelper, int playerid, int useamount, GameLog gl) throws Exception {
		useCoin(dbHelper, playerid, useamount, true, true, gl);
	}
	
	/**
	 * ʹ�ý�
	 * @param useamount ʹ������
	 */
	public void useCoin(DBHelper dbHelper, int playerid, int useamount, boolean sys_change, boolean ca_change, GameLog gl) throws Exception {
		if(useamount <= 0){
			String str = "����ֵʧ�� �޸���� ����ֵ��" + useamount;
			try {
				throw new Exception(str);
			} catch (Exception e) {
				e.printStackTrace();
			}
			BACException.throwInstance(str);
		}
		DBPaRs plaRs = getDataRs(playerid);
		int coin = plaRs.getInt("coin");
		if(coin < useamount){
			BACException.throwInstance("�𶧲���("+coin+"/"+useamount+")");
		}
		SqlString sqlStr = new SqlString();
		sqlStr.addChange("coin", -useamount);
		update(dbHelper, playerid, sqlStr);
		
		CustomActivityBAC.getInstance().updateProcess(dbHelper, playerid, 10, useamount);
		
		gl.addChaNote(GameLog.TYPE_COIN, coin, -(coin>=useamount?useamount:coin), sys_change);
	}
	
	/**
	 * ʹ�ý𶧼��
	 */
	public void useCoinCheck(int playerid, int useamount) throws Exception {
		if(useamount <= 0){
			String str = "����ֵʧ�� �޸���� ����ֵ��" + useamount;
			try {
				throw new Exception(str);
			} catch (Exception e) {
				e.printStackTrace();
			}
			BACException.throwInstance(str);
		}
		DBPaRs plaRs = getDataRs(playerid);
		int coin = plaRs.getInt("coin");
		if(coin < useamount){
			BACException.throwInstance("�𶧲���("+coin+"/"+useamount+")");
		}
	}
	
	//--------------��̬��--------------
	
	private static PlayerBAC instance = new PlayerBAC();
	
	/**
	 * ��ȡʵ��
	 */
	public static PlayerBAC getInstance(){
		return instance;
	}
}

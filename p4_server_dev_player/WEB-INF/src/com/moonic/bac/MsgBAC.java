package com.moonic.bac;

import org.json.JSONArray;

import server.common.Tools;

import com.ehc.common.ReturnValue;
import com.ehc.common.SqlString;
import com.moonic.gamelog.GameLog;
import com.moonic.socket.Player;
import com.moonic.socket.PushData;
import com.moonic.socket.SocketServer;
import com.moonic.util.BACException;
import com.moonic.util.DBHelper;
import com.moonic.util.DBPaRs;
import com.moonic.util.DBPool;
import com.moonic.util.DBPsRs;
import com.moonic.util.MyTools;

import conf.Conf;
import conf.LogTbName;

/**
 * ��ϢBAC
 * @author John
 */
public class MsgBAC {
	public static final String tab_ensitive_words = "tab_ensitive_words";
	public static final String tab_game_push_msg = "tab_game_push_msg";
	
	public static final String[] CHA_NAME = {"���", "����", "����", "ϵͳ", "����", "����"};
	
	/**
	 * ���
	 */
	public static final byte CHA_PLAYER = 1;
	/**
	 * ����
	 */
	public static final byte CHA_FACTION = 2;
	/**
	 * ˽��
	 */
	public static final byte CHA_PRIVATE = 3;
	/**
	 * ϵͳ
	 */
	public static final byte CHA_SYSTEM = 4;
	/**
	 * ����
	 */
	public static final byte CHA_TOP = 5;
	/**
	 * ����(�ͻ���Ԥ�������ͻ������󽫶��Ƶ������Ϣ��ʾ������)
	 */
	public static final byte CHA_WORLD = 6;
	
	/**
	 * ����Ƶ��
	 */
	public static final byte[] SEND_CHANNEL = 
		{
		CHA_PLAYER, CHA_FACTION, CHA_PRIVATE
		};
	
	/**
	 * ����Ƶ����Ϣ
	 */
	public ReturnValue sendchannelMsg(Player pla, byte channel, int friendid, int type, String content, byte voiceSecond, int voiceLen, byte[] voiceData, short act){
		DBHelper dbHelper = new DBHelper();
		try {
			if(BannedMacBAC.getInstance().isBannedMac(pla.mac, pla.imei)) {
				return new ReturnValue(true);
			}
			//System.out.println("channel:"+channel);
			if(!Tools.intArrContain(SEND_CHANNEL, channel)){
				BACException.throwInstance("�����Ƶ��");
			}
			if(channel == CHA_PRIVATE){
				if(friendid == 0){
					BACException.throwInstance("��������ʧ�ܣ�δָ���������");		
				} else 
				if(SocketServer.getInstance().plamap.get(friendid) == null){
					BACException.throwInstance("��������ʧ�ܣ��Է�������");
				}
			}
			
			int factionid = 0;
			if(channel == CHA_FACTION){
				factionid = PlaFacBAC.getInstance().getIntValue(pla.pid, "factionid");
				if(factionid == 0){
					BACException.throwInstance("��δ�������");
				}
			}
			DBPaRs plaRs = PlayerBAC.getInstance().getDataRs(pla.pid);
			long bannedmsgtime = plaRs.getTime("bannedmsgtime");
			if(!MyTools.checkSysTimeBeyondSqlDate(bannedmsgtime)){
				BACException.throwInstance("�����Ե���"+MyTools.formatTime(bannedmsgtime, "yyyy��MM��dd�� HH:mm:ss"));
			}
			if(!MyTools.checkSysTimeBeyondSqlDate(pla.lastmsgtime+5000)) {
				BACException.throwInstance("���Թ���");
			}
			JSONArray contentarr = new JSONArray();
			if(type == 1){
				if(content.length()==0){
					BACException.throwInstance("����ʧ�ܣ���Ϣ����Ϊ��");
				}
				if(content.length() > 100){
					BACException.throwInstance("����ʧ�ܣ���Ϣ���ݹ���");
				}
				DBPsRs ensitiveRs = DBPool.getInst().pQueryS(tab_ensitive_words);
				while(ensitiveRs.next()){
					String str = ensitiveRs.getString("word");
					if(content.toUpperCase().indexOf(str) != -1){
						if(ensitiveRs.getInt("processtype") == 1){
							content = content.replaceAll("(?i)"+str, "**");		
						} else 
						if(ensitiveRs.getInt("processtype") == 2){
							return new ReturnValue(true);
						}
					}
				}
				contentarr.add(content);//�ַ�������
			} else 
			if(type == 2){
				if(voiceData == null){
					BACException.throwInstance("����ʧ�ܣ���������Ϊ��");
				}
				if(voiceData.length > 300 * 1024){
					BACException.throwInstance("�������ݹ���");
				}
				if(voiceLen != voiceData.length){
					BACException.throwInstance("���������쳣");
				}
				String filename = VoiceBAC.getInstance().uploadVoice(plaRs.getInt("vsid"), voiceData);
				contentarr.add(filename);//�����ļ���
				contentarr.add(voiceSecond);//����ʱ��(��)
			}
			DBPaRs plaroleRs = PlaRoleBAC.getInstance().getDataRs(pla.pid);
			JSONArray pusharr = new JSONArray();
			pusharr.add(channel);					//����
			pusharr.add(type);						//��Ϣ����
			pusharr.add(contentarr);				//��Ϣ����
			pusharr.add(pla.pid);					//���ID
			pusharr.add(plaRs.getString("name"));	//�����
			pusharr.add(plaRs.getInt("vip"));		//���VIP�ȼ�
			pusharr.add(plaRs.getInt("num"));		//ͷ����
			pusharr.add(plaRs.getInt("lv"));		//�ȼ�
			pusharr.add(plaroleRs.getInt("totalbattlepower"));		//��ս��
			JSONArray returnarr = null;
			if(channel == CHA_PLAYER){
				PushData.getInstance().sendPlaToNosOL(SocketServer.ACT_MESSAGE_RECEIVE, pusharr.toString(), pla.pid);
				pla.lastmsgtime = System.currentTimeMillis();//5������
			} else 
			if(channel == CHA_FACTION){
				PushData.getInstance().sendPlaToFacMem(SocketServer.ACT_MESSAGE_RECEIVE, pusharr.toString(), factionid, pla.pid);
				pla.lastmsgtime = System.currentTimeMillis()-3000;//2������
			} else 
			if(channel == CHA_PRIVATE){
				PushData.getInstance().sendPlaToOne(SocketServer.ACT_MESSAGE_RECEIVE, pusharr.toString(), friendid);
				pla.lastmsgtime = System.currentTimeMillis()-4500;//1.5������
			}
			SqlString sqlStr = new SqlString();
			sqlStr.add("serverid", Conf.sid);
			sqlStr.add("playerid", pla.pid);
			sqlStr.add("channel", channel);
			sqlStr.add("friendid", friendid);
			sqlStr.add("factionid", factionid);
			sqlStr.add("type", type);
			sqlStr.add("content", contentarr.toString());
			sqlStr.addDateTime("savetime", MyTools.getTimeStr());
			DBHelper.logInsert(LogTbName.TAB_MSG_LOG(), sqlStr);
			
			return new ReturnValue(true, returnarr!=null?returnarr.toString():null);
		} catch(Exception e){
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		} finally {
			dbHelper.closeConnection();
		}
	}
	
	/**
	 * ��������
	 */
	public ReturnValue setReceiveGamePush(Player pla, boolean open){
		pla.conf_receive_game_push = open;
		return new ReturnValue(true);
	}
	
	/**
	 * ���͵�¼�ʺ���
	 */
	public void sendLoginSysMsg(int playerid){
		JSONArray pusharr = getSysMsgBag(ConfigBAC.getString("login_sys_msg"));
		PushData.getInstance().setSysMsg().sendPlaToOne(SocketServer.ACT_MESSAGE_RECEIVE, pusharr.toString(), playerid);
	}
	
	/**
	 * ��ȡϵͳ��Ϣ��
	 */
	public JSONArray getSysMsgBag(String msg){
		JSONArray contentarr = new JSONArray();
		contentarr.add(msg);
		JSONArray msgarr = new JSONArray();
		msgarr.add(CHA_SYSTEM);
		msgarr.add(1);
		msgarr.add(contentarr);
		return msgarr;
	}
	
	//--------------��̬��--------------
	
	private static MsgBAC instance = new MsgBAC();
	
	/**
	 * ��ȡʵ��
	 */
	public static MsgBAC getInstance(){
		return instance;
	}
}

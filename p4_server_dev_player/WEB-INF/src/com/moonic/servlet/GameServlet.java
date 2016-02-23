package com.moonic.servlet;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;

import server.common.Tools;
import util.IPAddressUtil;

import com.ehc.common.ReturnValue;
import com.ehc.common.SqlString;
import com.moonic.bac.ArtifactBAC;
import com.moonic.bac.BattleReplayBAC;
import com.moonic.bac.CBBAC;
import com.moonic.bac.ConfigBAC;
import com.moonic.bac.CopymapBAC;
import com.moonic.bac.CustomActivityBAC;
import com.moonic.bac.EquipOrdinaryBAC;
import com.moonic.bac.FacCopymapBAC;
import com.moonic.bac.FacFuncBAC;
import com.moonic.bac.FactionBAC;
import com.moonic.bac.FriendBAC;
import com.moonic.bac.FunctionBAC;
import com.moonic.bac.ItemBAC;
import com.moonic.bac.MailBAC;
import com.moonic.bac.MsgBAC;
import com.moonic.bac.PartnerBAC;
import com.moonic.bac.PlaFactionShopBAC;
import com.moonic.bac.PlaJJCRankingBAC;
import com.moonic.bac.PlaJJShopBAC;
import com.moonic.bac.PlaMineralsBAC;
import com.moonic.bac.PlaMysteryShopBAC;
import com.moonic.bac.PlaOrdinaryShopBAC;
import com.moonic.bac.PlaRoleBAC;
import com.moonic.bac.PlaSpShopBAC;
import com.moonic.bac.PlaSummonBAC;
import com.moonic.bac.PlaSupplyBAC;
import com.moonic.bac.PlaTeamBAC;
import com.moonic.bac.PlaTowerBAC;
import com.moonic.bac.PlaTowerShopBAC;
import com.moonic.bac.PlaTrialExpBAC;
import com.moonic.bac.PlaTrialMoneyBAC;
import com.moonic.bac.PlaTrialPartnerBAC;
import com.moonic.bac.PlaWelfareBAC;
import com.moonic.bac.PlatformGiftCodeBAC;
import com.moonic.bac.PlayerBAC;
import com.moonic.bac.RankingBAC;
import com.moonic.bac.ServerFacBAC;
import com.moonic.bac.UserBAC;
import com.moonic.bac.VipBAC;
import com.moonic.bac.WorldBossBAC;
import com.moonic.battle.BattleManager;
import com.moonic.mode.User;
import com.moonic.socket.Player;
import com.moonic.socket.SocketServer;
import com.moonic.util.DBHelper;
import com.moonic.util.EncryptionUtil;
import com.moonic.util.MD5;

import conf.Conf;
import conf.LogTbName;

public class GameServlet extends HttpServlet {
	private static final long serialVersionUID = 4598035092703154800L;
	
	public static boolean useHTTPEncrypt=true; //��http���ݽ��м��ܵĹ��ܿ���
	
	/**
	 * ������ɫ
	 */
	public static final short ACT_PLAYER_CREATE = 301;
	/**
	 * ��ɫ��¼
	 */
	public static final short ACT_PLAYER_LOGIN = 302;
	/**
	 * ��ɫע��
	 */
	public static final short ACT_PLAYER_LOGOUT = 303;
	/**
	 * ��������״̬
	 */
	public static final short ACT_PLAYER_UPD_OLSTATE = 304;
	/**
	 * ��ȡϵͳʱ��
	 */
	public static final short ACT_PLAYER_GET_SYSTIME = 305;
	/**
	 * �����ճ�����
	 */
	public static final short ACT_PLAYER_RESET_DAYDATA = 306;
	/**
	 * ��ȡָ���������
	 */
	public static final short ACT_PLAYER_GETDATA = 307;
	/**
	 * ��������
	 */
	public static final short ACT_PLAYER_OPEN_PUSH = 309;
	/**
	 * ��ɫ����
	 */
	public static final short ACT_PLAYER_RENAME = 311;
	/**
	 * �ı�ֵ
	 */
	public static final short ACT_PLAYER_CHANGEVALUE = 312;
	/**
	 * ����ͷ��
	 */
	public static final short ACT_PLAYER_SETFACE = 313;
	/**
	 * ��ȡս���ط�����
	 */
	public static final short ACT_PLAYER_GETREPLAY = 314;
	/**
	 * ����VIP���
	 */
	public static final short ACT_PLAYER_BUY_VIPGIFT = 315;
	/**
	 * ������Ҳ����ڵ�
	 */
	public static final short ACT_PLAYER_UPDATE_STEP = 316;
	/**
	 * ��������
	 */
	public static final short ACT_PLAYER_OPEN_FUNC = 351;
	/**
	 * ��ֵ
	 */
	public static final short ACT_PLAYER_RECHARGE = 401;
	/**
	 * ������Ȩ
	 */
	public static final short ACT_PLAYER_BUY_TQ = 402;
	/**
	 * ����Ϣ
	 */
	public static final short ACT_MESSAGE_STR_SEND = 451;
	/**
	 * ������
	 */
	public static final short ACT_MESSAGE_VOICE_SEND = 452;
	/**
	 * ���ý�����Ϸ������Ϣ����
	 */
	public static final short ACT_SET_RECEIVE_GAME_PUSH = 453;
	/**
	 * ������Ʒ
	 */
	public static final short ACT_ITEM_DISCARD = 552;
	/**
	 * �����
	 */
	public static final short ACT_ITEM_OPEN_GIFT = 553;
	/**
	 * ��ָ����Ʒ�ƶ���ָ���ռ�
	 */
	public static final short ACT_ITEM_MOVE = 554;
	/**
	 * ʹ��������Ʒ
	 */
	public static final short ACT_ITEM_USECONSUME = 555;
	/**
	 * ��ȡ��Ʒ����
	 */
	public static final short ACT_ITEM_GETDATA = 556;
	/**
	 * �������д����۵���
	 */
	public static final short ACT_ITEM_SELLMONEYITEM = 557;
	/**
	 * ʹ�ó齱����
	 */
	public static final short ACT_ITEM_USELOTTERY = 558;
	/**
	 * ������
	 */
	public static final short ACT_ITEM_COMPORB = 559;
	/**
	 * ��װ��
	 */
	public static final short ACT_ITEM_COMPEQUIP = 560;
	/**
	 * ������Ʒ
	 */
	public static final short ACT_ITEM_SELL = 561;
	/**
	 * �ڻ�
	 */
	public static final short ACT_ITEM_SMELT = 562;
	/**
	 * ����ҩ
	 */
	public static final short ACT_ITEM_BUY_EXPITEM = 563;
	/**
	 * ʹ��ѡ�������
	 */
	public static final short ACT_ITEM_USE_CHOOSEGIFT = 564;
	/**
	 * �����֤
	 */
	public static final short ACT_USER_BIND_CARD = 601;
	/**
	 * ��ȡ�ֻ���֤��
	 */
	public static final short ACT_USER_GET_MOBILE_VALIDNUM = 602;
	/**
	 * ���ֻ�
	 */
	public static final short ACT_USER_BIND_MOBILE = 603;
	/**
	 * ������
	 */
	public static final short ACT_USER_BIND_EMAIL = 604;
	/**
	 * �޸�����
	 */
	public static final short ACT_USER_MODIFY_PWD = 605;
	/**
	 * ��ȡ��ȫ��Ϣ��״̬
	 */
	public static final short ACT_USER_GET_SAFETYBIND_STATE = 606;
	/**
	 * ��ȡ�ֻ���
	 */
	public static final short ACT_USER_GET_PHONENUM = 607;
	/**
	 * ��д�ֻ���
	 */
	public static final short ACT_USER_WRITE_PHONENUM = 608;
	/**
	 * ���ʼ�
	 */
	public static final short ACT_MAIL_SEND = 651;
	/**
	 * ��ȡ�ʼ�����
	 */
	public static final short ACT_MAIL_GET_CONTENT = 652;
	/**
	 * ��ȡ����
	 */
	public static final short ACT_MAIL_EXTRACT_ADJUNCT = 653;
	/**
	 * ɾ���ʼ�
	 */
	public static final short ACT_MAIL_DEL = 654;
	/**
	 * һ��ɾ��
	 */
	public static final short ACT_MAIL_SHORTCUT_DEL = 655;
	/**
	 * һ����ȡ����
	 */
	public static final short ACT_MAIL_SHORTCUT_EXTRACT_ADJUNCT = 656;
	/**
	 * ��������
	 */
	public static final short ACT_FACTION_CREATE = 701;
	/**
	 * �����������
	 */
	public static final short ACT_FACTION_SET_JOINCOND = 702;
	/**
	 * �������
	 */
	public static final short ACT_FACTION_JOIN = 703;
	/**
	 * ���°�ṫ��
	 */
	public static final short ACT_FACTION_UPD_INFO = 704;
	/**
	 * ����ְλ
	 */
	public static final short ACT_FACTION_ADJUSET_POSITION = 705;
	/**
	 * �߳�����
	 */
	public static final short ACT_FACTION_REMOVE_MEMBER = 706;
	/**
	 * �˳�����
	 */
	public static final short ACT_FACTION_EXIT = 707;
	/**
	 * ��������
	 */
	public static final short ACT_FACTION_SHANRANG = 708;
	/**
	 * ��ȡ�����б�
	 */
	public static final short ACT_FACTION_GETLIST = 709;
	/**
	 * ��ȡ������ϸ��Ϣ
	 */
	public static final short ACT_FACTION_GETDATA = 710;
	/**
	 * ��ȡ�Լ����ɵ�����
	 */
	public static final short ACT_FACTION_GETRANKING = 711;
	/**
	 * ��ȡ�������ɵ�����
	 */
	public static final short ACT_FACTION_GETRANKING2 = 712;
	/**
	 * ��������
	 */
	public static final short ACT_FACTION_SEARCH = 713;
	/**
	 * ����
	 */
	public static final short ACT_FACTOIN_IMPEACH = 714;
	/**
	 * ���ɸ���
	 */
	public static final short ACT_FACTION_RENAME = 715;
	/**
	 * ��������
	 */
	public static final short ACT_FACTION_UPLEVEL = 716;
	/**
	 * �Ƽ�����
	 */
	public static final short ACT_FACTION_UPTECHNOLOGE = 717;
	/**
	 * ��ȡ����
	 */
	public static final short ACT_FACTION_GETWELFARE = 718;
	/**
	 * Ĥ��
	 */
	public static final short ACT_FACTION_WORSHIP = 719;
	/**
	 * ��ȡ��Ĥ�ݽ���
	 */
	public static final short ACT_FACTION_GET_BEWORSHIPAWARD = 720;
	/**
	 * ��������
	 */
	public static final short ACT_FACTION_REVOCATION_APPLY = 721;
	/**
	 * ��������
	 */
	public static final short ACT_FACTION_PROCESS_APPLY = 722;
	/**
	 * �Զ�����ȡ����
	 */
	public static final short ACT_CONSTOMACTI_GET_AWARD = 751;
	/**
	 * �����һ����
	 */
	public static final short ACT_GIFTCODE_GET = 801;
	/**
	 * ǿ��װ��
	 */
	public static final short ACT_EQUIP_STRE = 951;
	/**
	 * װ������
	 */
	public static final short ACT_EQUIP_UPSTAR = 952;
	/**
	 * ���
	 */
	public static final short ACT_EQUIP_DISMANTLE = 953;
	/**
	 * ����
	 */
	public static final short ACT_EQUIP_SMELT = 954;
	/**
	 * ��鴩װ��
	 */
	public static final short ACT_PARTNER_PUTON_EQUIP = 1001;
	/**
	 * ���һ����װ��
	 */
	public static final short ACT_PARTNER_SHORTCUT_PUTON_EQUIP = 1002;
	/**
	 * �����װ��
	 */
	public static final short ACT_PARTNER_PUTOFF_EQUIP = 1003;
	/**
	 * ��鴩����
	 */
	public static final short ACT_PARTNER_PUTON_ORB = 1004;
	/**
	 * ������
	 */
	public static final short ACT_PARTNER_UPPHASE = 1005;
	/**
	 * �������
	 */
	public static final short ACT_PARTNER_UPSTAR = 1006;
	/**
	 * ������
	 */
	public static final short ACT_PARTNER_AWAKEN = 1007;
	/**
	 * ��������ܵȼ�
	 */
	public static final short ACT_PARTNER_UPSKILLLV = 1008;
	/**
	 * ���һ��ǿ��װ��
	 */
	public static final short ACT_PARTNER_SHORTCUT_STRE_EQUIP = 1009;
	/**
	 * �һ����
	 */
	public static final short ACT_PARTNER_EXCHANGE = 1010;
	/**
	 * ���һ��������
	 */
	public static final short ACT_PARTNER_SHORTCUT_PUTON_ORB = 1011;
	/**
	 * �ָ�����
	 */
	public static final short ACT_ROLE_RECOVERENERGY = 1051;
	/**
	 * ���븱��
	 */
	public static final short ACT_COPYMAP_ENTER = 1151;
	/**
	 * �����ս
	 */
	public static final short ACT_COPYMAP_ENDCHALLENGE = 1152;
	/**
	 * ������ս����
	 */
	public static final short ACT_COPYMAP_BUYTIMES = 1153;
	/**
	 * ɨ������
	 */
	public static final short ACT_COPYMAP_SWEEP = 1154;
	/**
	 * ��ȡ�����Ǽ�����
	 */
	public static final short ACT_COPYMAP_GETSTARAWARD = 1155;
	/**
	 * ��ͨ�̵��ȡ����
	 */
	public static final short ACT_ORDINARY_SHOP_GETDATA = 1201;
	/**
	 * ��ͨ�̵깺����Ʒ
	 */
	public static final short ACT_ORDINARY_SHOP_BUY = 1202;
	/**
	 * ��ͨ�̵�����ˢ��
	 */
	public static final short ACT_ORDINARY_SHOP_REFRESH = 1203;
	/**
	 * �����̵��ȡ����
	 */
	public static final short ACT_MYSTERY_SHOP_GETDATA = 1251;
	/**
	 * �����̵깺����Ʒ
	 */
	public static final short ACT_MYSTERY_SHOP_BUY = 1252;
	/**
	 * �����̵��ۼ���������ˢ����Ʒ
	 */
	public static final short ACT_MYSTERY_SHOP_CONENERGY_REFRESH = 1253;
	/**
	 * �����̵��ȡ����
	 */
	public static final short ACT_JJ_SHOP_GETDATA = 1301;
	/**
	 * �����̵깺����Ʒ
	 */
	public static final short ACT_JJ_SHOP_BUY = 1302;
	/**
	 * �����̵�����ˢ��
	 */
	public static final short ACT_JJ_SHOP_REFRESH = 1303;
	/**
	 * �����̵��ȡ����
	 */
	public static final short ACT_FACTION_SHOP_GETDATA = 1351;
	/**
	 * �����̵깺����Ʒ
	 */
	public static final short ACT_FACTION_SHOP_BUY = 1352;
	/**
	 * �����̵�����ˢ��
	 */
	public static final short ACT_FACTION_SHOP_REFRESH = 1353;
	/**
	 * ����̵��ȡ����
	 */
	public static final short ACT_SP_SHOP_GETDATA = 1401;
	/**
	 * ����̵깺����Ʒ
	 */
	public static final short ACT_SP_SHOP_BUY = 1402;
	/**
	 * ����̵�����ˢ��
	 */
	public static final short ACT_SP_SHOP_REFRESH = 1403;
	/**
	 * ��ȡ��������Ϣ
	 */
	public static final short ACT_JJC_RANKING_GET_INFO = 1451;
	/**
	 * ��ȡ������������Ϣ
	 */
	public static final short ACT_JJC_RANKING_GET_OPPS = 1452;
	/**
	 * ������ս��
	 */
	public static final short ACT_JJC_RANKING_BATTLE = 1453;
	/**
	 * ��ȡ������ָ����������������Ϣ
	 */
	public static final short ACT_JJC_RANKING_GETRANKINGDATA = 1454;
	/**
	 * ������ˢ�¶���
	 */
	public static final short ACT_JJC_RANKING_REFRESH_OPPS = 1455;
	/**
	 * �����սCD
	 */
	public static final short ACT_JJC_RANKING_CLEAR_CD = 1456;
	/**
	 * ������ս����
	 */
	public static final short ACT_JJC_RANKING_RESET_CHA_AM = 1457;
	/**
	 * ���÷�������
	 */
	public static final short ACT_JJC_RANKING_SET_DEFFORMACTION = 1458;
	/**
	 * ��ȡ���ַ���������Ϣ
	 */
	public static final short ACT_JJC_RANKING_GET_OPPDEFDATA = 1459;
	/**
	 * ���������Ž���
	 */
	public static final short ACT_JJC_RANKING_ISSUEAWARD = 1460;
	/**
	 * ��ȡ�ճ�������
	 */
	public static final short ACT_PLA_WELFARE_GETTASKAWARD = 1501;
	/**
	 * ��ȡ�ɾͽ���
	 */
	public static final short ACT_PLA_WELFARE_GETACHIEVEAWARD = 1502;
	/**
	 * ǩ��
	 */
	public static final short ACT_PLA_WELFARE_CHECKIN = 1503;
	/**
	 * ��ȡǩ���ۻ�����
	 */
	public static final short ACT_PLA_WELFARE_GETCHECKINAWARD = 1504;
	/**
	 * һ����ȡ�ճ�������
	 */
	public static final short ACT_PLA_WELFARE_GETTASKAWARD_ONEKEY = 1505;
	/**
	 * һ����ȡ�ɾͽ���
	 */
	public static final short ACT_PLA_WELFARE_GETACHIEVEAWARD_ONEKEY = 1506;
	/**
	 * ��ȡĿ�꽱��
	 */
	public static final short ACT_PLA_WELFARE_GETTARGETAWARD = 1507;
	/**
	 * ��ͨ�ٻ�
	 */
	public static final short ACT_PLA_SUMMON_ORDINARY = 1551;
	/**
	 * �����ٻ�
	 */
	public static final short ACT_PLA_SUMMON_ADVANCED = 1552;
	/**
	 * �����ٻ�
	 */
	public static final short ACT_PLA_SUMMON_MYSTERY = 1553;
	/**
	 * ��ս
	 */
	public static final short ACT_CB_DECLAREWAR = 1601;
	/**
	 * �������������
	 */
	public static final short ACT_CB_ADDTEAM = 1602;
	/**
	 * �Ӷ�����ó�����
	 */
	public static final short ACT_CB_REMOVETEAM = 1603;
	/**
	 * �μӳ�ս
	 */
	public static final short ACT_CB_JOINWAR = 1604;
	/**
	 * ������
	 */
	public static final short ACT_CB_RELIVEPARTNER = 1605;
	/**
	 * ����̫��
	 */
	public static final short ACT_CB_CONTENDLEADER = 1606;
	/**
	 * ����̫�ط�������
	 */
	public static final short ACT_CB_SET_LEADERDEFFORM = 1607;
	/**
	 * ����̫��
	 */
	public static final short ACT_CB_GIVEUP_LEADER = 1608;
	/**
	 * ��ȡ��ս����
	 */
	public static final short ACT_CB_GETDATA = 1609;
	/**
	 * ��ȡ���������
	 */
	public static final short ACT_CB_GET_TEAMPOOLDATA = 1610;
	/**
	 * ��ȡ����ս״̬
	 */
	public static final short ACT_CB_GET_PARTNERSTATE = 1611;
	/**
	 * ��ȡ�ǳ�ս������
	 */
	public static final short ACT_CB_GET_CITYBATTLEDATA = 1612;
	/**
	 * ��ȡ̫������
	 */
	public static final short ACT_CB_GET_LEADERDATA = 1613;
	/**
	 * ���Ӷ���ض����ɳ�
	 */
	public static final short ACT_CB_DISPATCH_TEAMPOOL = 1614;
	/**
	 * ��ȡ��ɱ����
	 */
	public static final short ACT_CB_GET_KILLRANKING = 1615;
	/**
	 * ��ȡ����˳��
	 */
	public static final short ACT_CB_GET_BATTLERLIST = 1616;
	/**
	 * ��������ȼ�
	 */
	public static final short ACT_CB_UPDATE_WORLDLEVEL = 1617;
	/**
	 * �������г��н���
	 */
	public static final short ACT_CB_ISSUE_SELF_AWARD = 1618;
	/**
	 * ���Ź������н���
	 */
	public static final short ACT_CB_ISSUE_PUB_AWARD = 1619;
	/**
	 * ��ս���
	 */
	public static final short ACT_CB_BATTLE_RESULT = 1620;
	/**
	 * ͭǮ������ʼ
	 */
	public static final short ACT_PLA_TRIAL_MONEY_START = 1651;
	/**
	 * ͭǮ��������
	 */
	public static final short ACT_PLA_TRIAL_MONEY_END = 1652;
	/**
	 * ����������ʼ
	 */
	public static final short ACT_PLA_TRIAL_EXP_START = 1701;
	/**
	 * ������������
	 */
	public static final short ACT_PLA_TRIAL_EXP_END = 1702;
	/**
	 * ���������ʼ
	 */
	public static final short ACT_PLA_TRIAL_PARTNER_START = 1751;
	/**
	 * �����������
	 */
	public static final short ACT_PLA_TRIAL_PARTNER_END = 1752;
	/**
	 * ��ͭǮ
	 */
	public static final short ACT_SUPPLY_BUY_MONEY = 1801;
	/**
	 * ������
	 */
	public static final short ACT_SUPPLY_BUY_ENERGY = 1802;
	/**
	 * ��ȡ�¿���
	 */
	public static final short ACT_SUPPLY_GET_TQCOIN = 1803;
	/**
	 * ���ɸ���-���븱��
	 */
	public static final short ACT_FACCOPYMAP_INTO = 1851;
	/**
	 * ���ɸ���-ս������
	 */
	public static final short ACT_FACCOPYMAP_END = 1852;
	/**
	 * ���ɸ���-���õ�ͼ
	 */
	public static final short ACT_FACCOPYMAP_RESETMAP = 1853;
	/**
	 * ���ɸ���-�˳�
	 */
	public static final short ACT_FACTIONMAP_EXIT = 1854;
	/**
	 * ����-����Ʒ
	 */
	public static final short ACT_ARTIFACT_EATITEM = 1901;
	/**
	 * ����-��ע��
	 */
	public static final short ACT_ARTIFACT_COININPUT = 1902;
	/**
	 * ����-�ϳ�
	 */
	public static final short ACT_ARTIFACT_COMP = 1903;
	/**
	 * �ָ�������Ƭ�������
	 */
	public static final short ACT_ARTIFACT_RECOVERROBTIMES = 1904;
	/**
	 * ����-��������
	 */
	public static final short ACT_ARTIFACT_OPENPROTECT = 1905;
	/**
	 * ��ȡս�����а�
	 */
	public static final short ACT_GET_BATTLE_POWER_RANKING = 1951;
	/**
	 * �Ӻ���
	 */
	public static final short ACT_FRIEND_ADD = 2001;
	/**
	 * ɾ����
	 */
	public static final short ACT_FRIEND_DELLTE = 2002;
	/**
	 * �������
	 */
	public static final short ACT_FRIEND_SEARCH = 2003;
	/**
	 * ��������
	 */
	public static final short ACT_FRIEND_SEARCH_QUIK = 2004;
	/**
	 * ��������
	 */
	public static final short ACT_FRIEND_PRESENT = 2005;
	/**
	 * һ����������
	 */
	public static final short ACT_FRIEND_PRESENT_ONEKEY = 2006;
	/**
	 * ��ȡ����
	 */
	public static final short ACT_FRIEND_GETENERGY = 2007;
	/**
	 * һ����ȡ����
	 */
	public static final short ACT_FRIEND_GETENERGY_ONEKEY = 2008;
	/**
	 * ����BOSS-����
	 */
	public static final short ACT_WORLD_BOSS_JOIN = 2051;
	/**
	 * ����BOSS-��������
	 */
	public static final short ACT_WORLD_BOSS_AWARD_RANK = 2052;
	/**
	 * ����BOSS-ս��
	 */
	public static final short ACT_WORLD_BOSS_BATTLE = 2053;
	/**
	 * ����BOSS-��ȡ����
	 */
	public static final short ACT_WORLD_BOSS_GETDATA = 2054;
	/**
	 * �ֻ���-������ս
	 */
	public static final short ACT_TOWER_ENTER = 2101;
	/**
	 * �ֻ���-������ս
	 */
	public static final short ACT_TOWER_END = 2102;
	/**
	 * �ֻ���-���ͽ���
	 */
	public static final short ACT_TOWER_AWARD = 2103;
	/**
	 * �ֻ����̵��ȡ����
	 */
	public static final short ACT_TOWER_SHOP_GETDATA = 2151;
	/**
	 * �ֻ����̵깺����Ʒ
	 */
	public static final short ACT_TOWER_SHOP_BUY = 2152;
	/**
	 * �ֻ����̵�����ˢ��
	 */
	public static final short ACT_TOWER_SHOP_REFRESH = 2153;
	/**
	 * ��ӻ-��������
	 */
	public static final short ACT_TEAM_ACTI_CREATE = 2201;
	/**
	 * ��ӻ-�������
	 */
	public static final short ACT_TEAM_ACTI_JOIN = 2202;
	/**
	 * ��ӻ-�߳�����
	 */
	public static final short ACT_TEAM_ACTI_KICK = 2203;
	/**
	 * ��ӻ-����
	 */
	public static final short ACT_TEAM_ACTI_FORMAT = 2204;
	/**
	 * ��ӻ-׼��
	 */
	public static final short ACT_TEAM_ACTI_BEREADY = 2205;
	/**
	 * ��ӻ-ȡ��׼��
	 */
	public static final short ACT_TEAM_ACTI_CANCELREADY = 2206;
	/**
	 * ��ӻ-ս��
	 */
	public static final short ACT_TEAM_ACTI_BATTLE = 2207;
	/**
	 * ��ӻ-��ȡ�����б�
	 */
	public static final short ACT_TEAM_ACTI_GETTEAMLIST = 2208;
	/**
	 * ��ӻ-��ȡ����
	 */
	public static final short ACT_TEAM_ACTI_GETDATA = 2209;
	/**
	 * ��ӻ-�˳�����
	 */
	public static final short ACT_TEAM_ACTI_EXIT = 2210;
	/**
	 * �ڿ�-ռ��
	 */
	public static final short ACT_MINERALS_CLOCKIN = 2251;
	/**
	 * �ڿ�-����
	 */
	public static final short ACT_MINERALS_CONDENT = 2252;
	/**
	 * �ڿ�-���÷�������
	 */
	public static final short ACT_MINERALS_SETDEF = 2253;
	/**
	 * �ڿ�-��ȡ������Ϣ
	 */
	public static final short ACT_MINERALS_GETINFO = 2254;
	/**
	 * �ڿ�-��ȡ��λ��Ϣ
	 */
	public static final short ACT_MINERALS_GETPOSDATA = 2255;
	/**
	 * �ڿ�-��ȡ������Ϣ
	 */
	public static final short ACT_MINERALS_GETOWNERDATA = 2256;
	
	/**
	 * �����ճ�����
	 */
	public static final short ACT_DEBUG_RESETDAYDATE = 20001;
	/**
	 * DEBUG_LOG
	 */
	public static final short ACT_DEBUG_GAME_LOG = 20002;
	/**
	 * ����ս��-�����ս��
	 */
	public static final short ACT_DEBUG_SERVER_BATTLE = 20003;
	/**
	 * ����ս��-��ȡս������������
	 */
	public static final short ACT_DEBUG_GET_BATTLEBOX = 20004;
	/**
	 * ����ս��-�������֤
	 */
	public static final short ACT_DEBUG_SERVER_VERIFY = 20005;


	/**
	 * service
	 */
	public void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		long t1= System.currentTimeMillis();
		/*Enumeration enu = request.getHeaderNames();
		System.out.println("http����head-----------------");
		while(enu.hasMoreElements())
		{
			String key = (String)enu.nextElement();
			System.out.println(key+"="+request.getHeader(key));
		}*/
		//System.out.println(Tools.getCurrentDateTimeStr()+"--����");
		String ip = IPAddressUtil.getIp(request);
		//System.out.println("ip="+ip);
		//���˻���֩�����
		String agent = request.getHeader("User-Agent");
		if(agent!=null && (agent.indexOf("spider")!=-1
		|| agent.indexOf("roboo")!=-1			
		|| agent.toLowerCase().indexOf("bot")!=-1			
		))
		{
			return;
		}
		InputStream is = request.getInputStream();
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] buff = new byte[4096];
		int readLen = -1;
		while ((readLen = is.read(buff)) != -1) {
			baos.write(buff, 0, readLen);
		}
		is.close();
		buff = baos.toByteArray();
		
		if(useHTTPEncrypt)
		{			
			buff = EncryptionUtil.RC4(buff);  //���ݽ���
		}
		/*if(buff.length==0)
		{
			System.out.println(Tools.getCurrentDateTimeStr()+"--���յ�����"+ip+"��http����,���ݳ���Ϊ0");
		}*/
		
		long t2= System.currentTimeMillis();
		DataInputStream dis = new DataInputStream(new ByteArrayInputStream(buff));
		DataOutputStream dos = new DataOutputStream(response.getOutputStream());
		try {
			ReturnValue val = null;
			SqlString reqSqlStr=null;
			if(ConfigBAC.getBoolean("http_log"))
			{
				reqSqlStr = new SqlString();
				reqSqlStr.addDateTime("reqtime", Tools.getCurrentDateTimeStr());
				reqSqlStr.add("reqflow", buff.length);
			}
			
			JSONArray processarr = new JSONArray();
			
			if (buff.length == 0) {
				val = new ReturnValue(false, "��Ч����");
			} else {
				try {
					val = processingReq(request, response, dis, dos, reqSqlStr, processarr, ip);
				} catch (EOFException e) {
					System.out.println(e.toString()+"(act="+Conf.currentAct+")");
					if(Conf.currentAct <= 10000){//TODO ��ʾ����ACT�����˷�ΧʱӦ��ʱ����
						e.printStackTrace();
					}
					val = new ReturnValue(false, e.toString());
				} catch (Exception ex1) {
					ex1.printStackTrace();
					val = new ReturnValue(false, ex1.toString());
				}
				if(processarr.length() >= 2){
					short act = (short)processarr.getInt(0);
					User user = (User)processarr.get(1);
					user.removeReqing(act, val);
				}
			}
			dis.close();
			byte[] responseData = null;
			if(val.getDataType()==ReturnValue.TYPE_STR) {
				responseData = Tools.strNull(val.info).getBytes("UTF-8");
			} else 
			if(val.getDataType()==ReturnValue.TYPE_BINARY) {
				responseData = val.binaryData;
			}
			long t3= System.currentTimeMillis();			
			
			if(responseData==null)
			{
				System.out.println("responseData==null");
			}
			
			//��ü��ܺ���ֽ���
			byte[] outputBytes = getOutputBytes(val.success,responseData);
			if(outputBytes!=null)
			{
				dos.write(outputBytes);
			}			
			
			long t4= System.currentTimeMillis();
			Conf.currentActTime = (int)(t3-t2); //��ǰָ���ʱ����볤��
			
			if(!"��Ч����".equals(val.info)){
				if(ConfigBAC.getBoolean("http_log"))
				{
					if(ConfigBAC.getInt("logout_http_threshold")<(t3-t2))
					{
						reqSqlStr.addDateTime("resptime", Tools.getCurrentDateTimeStr());
						reqSqlStr.add("respflow", responseData.length);
						reqSqlStr.add("respresult", val.success ? 1 : 0);
						reqSqlStr.add("respdatatype", val.getDataType());
						reqSqlStr.add("usedtime", t3-t2);
						reqSqlStr.add("uploadtime", t2-t1);
						reqSqlStr.add("downloadtime", t4 - t3);
						reqSqlStr.add("ip", ip);
						DBHelper.logInsert(LogTbName.TAB_HTTP_LOG(), reqSqlStr);
					}						
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			byte[] outputBytes = getOutputBytes(false,e.toString().getBytes("UTF-8"));
			if(outputBytes!=null)
			{
				dos.write(outputBytes);
			}
		}
		finally
		{
			dos.close();
		}
	}
	
	/**
	 * ��÷��صļ�������bytes
	 * @param success �Ƿ�ɹ��Ľ��
	 * @param dataBytes ԭʼ����bytes
	 * @return
	 */
	private static byte[] getOutputBytes(boolean success,byte[] dataBytes)
	{
		try 
		{
			ByteArrayOutputStream outputBaos = new ByteArrayOutputStream();	
			DataOutputStream outputDos = new DataOutputStream(outputBaos);
			if(success)
			{
				outputDos.writeByte(1);				
			}
			else
			{
				outputDos.writeByte(0);
			}
			outputDos.write(dataBytes);
			outputDos.close();
			
			if(useHTTPEncrypt)  //ʹ�ü��ܻ���
			{	
				dataBytes = EncryptionUtil.RC4(outputBaos.toByteArray());  //���ݼ���
				return dataBytes;			
			}
			else
			{
				return outputBaos.toByteArray();
			}	
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * ��������
	 */
	private ReturnValue processingReq(HttpServletRequest request, HttpServletResponse response, DataInputStream dis, DataOutputStream dos, SqlString reqSqlStr, JSONArray processarr, String ip) throws Exception {
		short act = dis.readShort();			
		processarr.add(act);
		
		Conf.currentAct = act;
		long time = dis.readLong();				
		String sessionid = dis.readUTF();
		
		User user = UserBAC.session_usermap.get(sessionid);
		if(user == null)
		{
			try {
				user = UserBAC.getInstance().createUser(sessionid);				
			} catch (Exception e) {
				//System.out.println("����USER�������쳣("+sessionid+")("+ip+") "+e.toString());
				return new ReturnValue(false, e.toString());
			}
		}
		ReturnValue addReqRv = user.addReqing(act, time);
		//����ʷ���
		if(addReqRv.parameter==null && !addReqRv.success){
			return addReqRv;
		}
		processarr.add(user);
		//��ʷ���
		if(addReqRv.parameter!=null){
			return addReqRv;
		}
		if(act == ACT_PLAYER_CREATE){
			int vsid = dis.readInt();
			String name = dis.readUTF();
			byte num = dis.readByte();
			int partnernum = 0;//TODO ���ⱨ������
			try {
				partnernum = dis.readInt();
			} catch (Exception e) {
			}
			return PlayerBAC.getInstance().create(user.uid, vsid, name, num, partnernum, 0);
		} else 
		if(act == ACT_PLAYER_LOGIN){
			int vsid = dis.readInt();
			return PlayerBAC.getInstance().login(user.uid, sessionid, vsid);
		}
		
		Player pla = SocketServer.getInstance().session_plamap.get(sessionid);
		if(pla == null){
			return new ReturnValue(false, "��δ��¼��ɫ ������Ϣ��"+sessionid);
		}
		
		pla.ip = ip;
		pla.channel = user.channel;
		
		int uid = pla.uid;
		int pid = pla.pid;
		if(reqSqlStr!=null)
		{
			reqSqlStr.add("act", act);
			reqSqlStr.add("userid", pla.uid);
			reqSqlStr.add("serverid", Conf.sid);
			reqSqlStr.add("playerid", pid);
		}
		
		ReturnValue funcRv = FunctionBAC.getInstance().checkFuncOpen(pla.openfunc, act);
		if(!funcRv.success){
			return funcRv;
		}
		ReturnValue facfuncRv = FacFuncBAC.getInstance().checkFuncOpen(pid, act);
		if(!facfuncRv.success){
			return facfuncRv;
		}
		if(act == ACT_PLAYER_LOGOUT){
			return PlayerBAC.getInstance().logout(pid, "HTTP����ע��");
		} else 
		if(act == ACT_PLAYER_UPD_OLSTATE){
			short mark = dis.readShort();
			return PlayerBAC.getInstance().updateOnlineState(pid, mark);
		} else 
		if(act == ACT_PLAYER_GET_SYSTIME){
			return new ReturnValue(true, String.valueOf(System.currentTimeMillis()));
		} else 
		if(act == ACT_PLAYER_RESET_DAYDATA){
			return PlayerBAC.getInstance().checkAndResetDayData(pid, false);
		} else 
		if(act == ACT_PLAYER_GETDATA){
			int targetid = dis.readInt();
			return PlayerBAC.getInstance().getAllData(pid, targetid);
		} else 
		if(act == ACT_PLAYER_OPEN_PUSH){
			return PlayerBAC.getInstance().openPush(pid);
		} else 
		if(act == ACT_PLAYER_RENAME){
			String newName = dis.readUTF();
			return PlayerBAC.getInstance().rename(pid, newName);
		} else 
		if(act == ACT_PLAYER_SETFACE){
			int num = dis.readInt();
			return PlayerBAC.getInstance().setFace(pid, num);
		} else 
		if(act == ACT_PLAYER_GETREPLAY){
			long battleid = dis.readLong();
			return BattleReplayBAC.getInstance().getBattleReplay(battleid);
		} else 
		if(act == ACT_PLAYER_BUY_VIPGIFT){
			int buyvip = dis.readInt();
			return VipBAC.getInstance().buyVipGift(pid, buyvip);
		} else 
		if(act == ACT_PLAYER_UPDATE_STEP){
			int stepId = dis.readInt();
			return UserBAC.getInstance().updateUserStep(uid, stepId);
		} else 
		if(act == ACT_PLAYER_OPEN_FUNC){
			String numStr = dis.readUTF();
			return FunctionBAC.getInstance().openFunc(pla.pid, pla.openfunc, numStr, null);
		} else 
		if(act == ACT_MESSAGE_STR_SEND) {
			byte channel = dis.readByte();
			int friendid = dis.readInt();
			String content = dis.readUTF();
			return MsgBAC.getInstance().sendchannelMsg(pla, channel, friendid, 1, content, (byte)0, 0, null, act);
		} else 
		if(act == ACT_MESSAGE_VOICE_SEND) {	
			byte channel = dis.readByte();
			int friendid = dis.readInt();
			byte voiceSecond = dis.readByte();
			int voiceLen = dis.readInt();
			byte[] voiceData = Tools.getBytesFromInputstream(dis);
			return MsgBAC.getInstance().sendchannelMsg(pla, channel, friendid, 2, null, voiceSecond, voiceLen, voiceData, act);
		} else 
		if(act == ACT_SET_RECEIVE_GAME_PUSH){
			boolean open = dis.readBoolean();
			return MsgBAC.getInstance().setReceiveGamePush(pla, open);
		} else 
		if(act == ACT_ITEM_DISCARD){
			int itemid = dis.readInt();
			return ItemBAC.getInstance().discardItem(pid, itemid);
		} else 
		if(act == ACT_ITEM_OPEN_GIFT){
			int itemid = dis.readInt();
			int amount = dis.readInt();
			return ItemBAC.getInstance().openGift(pid, itemid, amount);
		} else 
		if(act == ACT_ITEM_MOVE){
			int itemid = dis.readInt();
			byte srczone = dis.readByte();
			byte targetzone = dis.readByte();
			return ItemBAC.getInstance().moveToZone(pid, itemid, srczone, targetzone);
		} else 
		if(act == ACT_ITEM_USECONSUME){
			int target = dis.readInt();
			int itemid = dis.readInt();
			int useamount = dis.readInt();
			return ItemBAC.getInstance().useConsumeProp(pid, target, itemid, useamount);
		} else 
		if(act == ACT_ITEM_GETDATA){
			int ownerid = dis.readInt();
			int itemid = dis.readInt();
			return ItemBAC.getInstance().getItemDataEx(ownerid, itemid);
		} else 
		if(act == ACT_ITEM_SELLMONEYITEM){
			return ItemBAC.getInstance().sellMoneyItem(pid);
		} else 
		if(act == ACT_ITEM_USELOTTERY){
			int itemid = dis.readInt();
			int amount = dis.readInt();
			return ItemBAC.getInstance().useLottery(pid, itemid, amount);
		} else 
		if(act == ACT_ITEM_COMPORB){
			int num = dis.readInt();
			return ItemBAC.getInstance().compOrb(pid, num);
		} else 
		if(act == ACT_ITEM_COMPEQUIP){
			int num = dis.readInt();
			return ItemBAC.getInstance().compEquip(pid, num);
		} else 
		if(act == ACT_ITEM_SELL){
			int itemid = dis.readInt();
			int sellamount = dis.readInt();
			return ItemBAC.getInstance().sell(pid, itemid, sellamount);
		} else 
		if(act == ACT_ITEM_SMELT){
			int itemid = dis.readInt();
			int smeltamount = dis.readInt();
			return ItemBAC.getInstance().smelt(pid, itemid, smeltamount);
		} else 
		if(act == ACT_ITEM_BUY_EXPITEM){
			int buynum = dis.readInt();
			int amount = dis.readInt();
			return ItemBAC.getInstance().buyExpItem(pid, buynum, amount);
		} else 
		if(act == ACT_ITEM_USE_CHOOSEGIFT){
			int itemid = dis.readInt();
			int index = dis.readInt();
			int amount = dis.readInt();
			return ItemBAC.getInstance().useChooseGift(pid, itemid, index, amount);
		} else 
		if(act == ACT_USER_BIND_CARD){
			String card = dis.readUTF();
			String realname = dis.readUTF();
			return UserBAC.getInstance().bindCard(uid, card, realname,ip, pid);
		} else 
		if(act == ACT_USER_GET_MOBILE_VALIDNUM){
			String phone = dis.readUTF();
			String card = null;
			try {
				card = dis.readUTF();
			} catch (Exception e) {
			}
			return UserBAC.getInstance().getMobileVaildNum(uid, phone, card,ip);
		} else 
		if(act == ACT_USER_BIND_MOBILE){
			String phone = dis.readUTF();
			String validnum = dis.readUTF();
			String card = dis.readUTF();
			return UserBAC.getInstance().bindMobile(uid, phone, validnum, card,ip, pid);
		} else 
		if(act == ACT_USER_BIND_EMAIL){
			String email = dis.readUTF();
			String card = dis.readUTF();
			return UserBAC.getInstance().bindEmail(uid, email, card,ip, pid);
		} else 
		if(act == ACT_USER_MODIFY_PWD){
			String oldpwd = dis.readUTF();
			String newpwd = dis.readUTF();
			return UserBAC.getInstance().modifyPwd(uid, oldpwd, newpwd,ip, pid);
		} else 
		if(act == ACT_USER_GET_SAFETYBIND_STATE){
			return UserBAC.getInstance().getSafetyBindState(uid, ip);
		} else 
		if(act == ACT_USER_GET_PHONENUM){
			return UserBAC.getInstance().getPhonenum(uid);
		} else 
		if(act == ACT_USER_WRITE_PHONENUM){
			String phonenum = dis.readUTF();
			return UserBAC.getInstance().writePhonenum(uid, phonenum);
		} else 
		if(act == ACT_MAIL_SEND){
			String receiveridStr = dis.readUTF();
			String title = dis.readUTF();
			String content = dis.readUTF();
			return MailBAC.getInstance().sendMail(pid, receiveridStr, title, content);
		} else 
		if(act == ACT_MAIL_GET_CONTENT){
			int mailid = dis.readInt();
			return MailBAC.getInstance().getMailContent(pid, mailid);
		} else 
		if(act == ACT_MAIL_EXTRACT_ADJUNCT){
			int mailid = dis.readInt();
			return MailBAC.getInstance().extractAdjunct(pid, mailid);
		} else 
		if(act == ACT_MAIL_DEL){
			int mailid = dis.readInt();
			return MailBAC.getInstance().delMail(pid, mailid);
		} else 
		if(act == ACT_MAIL_SHORTCUT_DEL){
			return MailBAC.getInstance().shortcatDel(pid);
		} else 
		if(act == ACT_MAIL_SHORTCUT_EXTRACT_ADJUNCT){
			return MailBAC.getInstance().shortcutExtractAdjunct(pid);
		} else 
		if(act == ACT_FACTION_CREATE){
			String name = dis.readUTF();
			return FactionBAC.getInstance().create(pid, name);
		} else 
		if(act == ACT_FACTION_SET_JOINCOND){
			String joincond = dis.readUTF();
			return FactionBAC.getInstance().setJoinCond(pid, joincond);
		} else 
		if(act == ACT_FACTION_JOIN){
			int factionid = dis.readInt();
			return FactionBAC.getInstance().join(pid, factionid);
		} else 
		if(act == ACT_FACTION_UPD_INFO){
			String info = dis.readUTF();
			return FactionBAC.getInstance().updInfo(pid, info);
		} else 
		if(act == ACT_FACTION_ADJUSET_POSITION){
			int memberid = dis.readInt();
			byte position = dis.readByte();
			return FactionBAC.getInstance().adjustPosition(pid, memberid, position);
		} else 
		if(act == ACT_FACTION_REMOVE_MEMBER){
			String memberidStr = dis.readUTF();
			return FactionBAC.getInstance().removeMember(pid, memberidStr);
		} else 
		if(act == ACT_FACTION_EXIT){
			return FactionBAC.getInstance().exitFaction(pid);
		} else 
		if(act == ACT_FACTION_SHANRANG){
			int memberid = dis.readInt();
			return FactionBAC.getInstance().shanrang(pid, memberid);
		} else 
		if(act == ACT_FACTION_GETLIST){
			int page = dis.readInt();
			return ServerFacBAC.getInstance().getFactionList(pid, page);
		} else 
		if(act == ACT_FACTION_GETDATA){
			int factionid = dis.readInt();
			return FactionBAC.getInstance().getFactionData(pid, factionid);
		} else 
		if(act == ACT_FACTION_GETRANKING){
			return ServerFacBAC.getInstance().getPlaRanking(pid);
		} else
		if(act == ACT_FACTION_GETRANKING2){
			int factionid = dis.readInt();
			return ServerFacBAC.getInstance().getRanking(pid, factionid);
		} else
		if(act == ACT_FACTION_SEARCH){
			String name = dis.readUTF();
			return ServerFacBAC.getInstance().searchFaction(pid, name);
		} else 
		if(act == ACT_FACTOIN_IMPEACH){
			return FactionBAC.getInstance().impeach(pid);
		} else 
		if(act == ACT_FACTION_RENAME){
			String newName = dis.readUTF();
			return FactionBAC.getInstance().rename(pid, newName);
		} else 
		if(act == ACT_FACTION_UPLEVEL){
			return FactionBAC.getInstance().upLevel(pid);
		} else 
		if(act == ACT_FACTION_UPTECHNOLOGE){
			int num = dis.readInt();
			return FactionBAC.getInstance().upTechnology(pid, num);
		} else 
		if(act == ACT_FACTION_GETWELFARE){
			return FactionBAC.getInstance().getWelfare(pid);
		} else 
		if(act == ACT_FACTION_WORSHIP){
			int worshippid = dis.readInt();
			int num = dis.readInt();
			return FactionBAC.getInstance().worship(pid, worshippid, num);
		} else 
		if(act == ACT_FACTION_GET_BEWORSHIPAWARD){
			return FactionBAC.getInstance().getBeWorshipAward(pid);
		} else 
		if(act == ACT_FACTION_REVOCATION_APPLY){
			int factionid = dis.readInt();
			return FactionBAC.getInstance().revocationApply(pid, factionid);
		} else 
		if(act == ACT_FACTION_PROCESS_APPLY){
			int applyid = dis.readInt();
			byte way = dis.readByte();
			return FactionBAC.getInstance().processApply(pid, applyid, way);
		} else 
		if(act == ACT_CONSTOMACTI_GET_AWARD){
			int actiid = dis.readInt();
			byte index = dis.readByte();
			return CustomActivityBAC.getInstance().getAward(pid, actiid, index);
		} else 
		if(act == ACT_GIFTCODE_GET){
			String code = dis.readUTF();
			return PlatformGiftCodeBAC.getInstance().getPlatformGift(pid, uid, code, false);
		} else 
		if(act == ACT_EQUIP_STRE){
			int itemid = dis.readInt();
			return EquipOrdinaryBAC.getInstance().stre(pid, itemid);
		} else 
		if(act == ACT_EQUIP_UPSTAR){
			int itemid = dis.readInt();
			return EquipOrdinaryBAC.getInstance().upStar(pid, itemid);
		} else 
		if(act == ACT_EQUIP_DISMANTLE){
			int itemid = dis.readInt();
			return EquipOrdinaryBAC.getInstance().dismantle(pid, itemid);
		} else 
		if(act == ACT_EQUIP_SMELT){
			String itemidStr = dis.readUTF();
			return EquipOrdinaryBAC.getInstance().smelt(pid, itemidStr);
		} else 
		if(act == ACT_PARTNER_PUTON_EQUIP){
			int partnerid = dis.readInt();
			int partnerid2 = dis.readInt();
			int itemid = dis.readInt();
			return PartnerBAC.getInstance().putonEquip(pid, partnerid, partnerid2, itemid);
		} else 
		if(act == ACT_PARTNER_SHORTCUT_PUTON_EQUIP){
			int partnerid = dis.readInt();
			return PartnerBAC.getInstance().shortcutPutonEquip(pid, partnerid);
		} else 
		if(act == ACT_PARTNER_PUTOFF_EQUIP){
			int partnerid = dis.readInt();
			int pos = dis.readInt();
			return PartnerBAC.getInstance().putoffEquip(pid, partnerid, pos);
		} else 
		if(act == ACT_PARTNER_PUTON_ORB){
			int partnerid = dis.readInt();
			int pos = dis.readInt();
			return PartnerBAC.getInstance().putonOrb(pid, partnerid, pos);
		} else 
		if(act == ACT_PARTNER_UPPHASE){
			int partnerid = dis.readInt();
			return PartnerBAC.getInstance().upPhase(pid, partnerid);
		} else 
		if(act == ACT_PARTNER_UPSTAR){
			int partnerid = dis.readInt();
			return PartnerBAC.getInstance().upStar(pid, partnerid);
		} else 
		if(act == ACT_PARTNER_AWAKEN){
			int partnerid = dis.readInt();
			return PartnerBAC.getInstance().awaken(pid, partnerid);
		} else 
		if(act == ACT_PARTNER_UPSKILLLV){
			int partnerid = dis.readInt();
			int pos = dis.readInt();
			int upamount = dis.readInt();
			return PartnerBAC.getInstance().upskilllv(pid, partnerid, pos, upamount);
		} else 
		if(act == ACT_PARTNER_SHORTCUT_STRE_EQUIP){
			int partnerid = dis.readInt();
			return PartnerBAC.getInstance().shortcutStreEquip(pid, partnerid);
		} else 
		if(act == ACT_PARTNER_EXCHANGE){
			int num = dis.readInt();
			return PartnerBAC.getInstance().exchange(pid, num);
		} else 
		if(act == ACT_PARTNER_SHORTCUT_PUTON_ORB){
			int partnerid = dis.readInt();
			return PartnerBAC.getInstance().shortcutPutonOrb(pid, partnerid);
		} else 
		if(act == ACT_ROLE_RECOVERENERGY){
			return PlaRoleBAC.getInstance().recoverEnergy(pid);
		} else 
		if(act == ACT_COPYMAP_ENTER){
			int cmnum = dis.readInt();
			String posStr = dis.readUTF();
			return CopymapBAC.getInstance().enter(pid, cmnum, posStr);
		} else
		if(act == ACT_COPYMAP_ENDCHALLENGE){
			int cmnum = dis.readInt();
			String battleRecord = dis.readUTF();
			//ǩ����֤
			String sign = dis.readUTF();			
			StringBuffer sb = new StringBuffer();
			sb.append(act);
			sb.append(time);
			sb.append(sessionid);
			sb.append(cmnum);
			sb.append(battleRecord);
			sb.append(BattleManager.Secret_Key);
			String mySign = MD5.encode(sb.toString(), "UTF-8");		
			//System.out.println("�ͻ���ս��ǩ���ִ�="+sign);
			//System.out.println("�������ǩ�ִ�="+mySign);
			
			if(sign.toUpperCase().equals(mySign.toUpperCase()))
			{
				return CopymapBAC.getInstance().endChallenge(pid, cmnum, battleRecord);	
			}
			else
			{
				return new ReturnValue(false,"ս�����ݷǷ�");
			}
		} else
		if(act == ACT_COPYMAP_BUYTIMES){
			int num = dis.readInt();
			return CopymapBAC.getInstance().buy(pid, num);
		} else
		if(act == ACT_COPYMAP_SWEEP){
			int num = dis.readInt();
			int times = dis.readInt();
			return CopymapBAC.getInstance().sweep(pid, num, times);
		} else
		if(act == ACT_COPYMAP_GETSTARAWARD){
			int bigmap = dis.readInt();
			int awardnum = dis.readInt();
			return CopymapBAC.getInstance().getStarAward(pid, bigmap, awardnum);
		} else
		if(act == ACT_ORDINARY_SHOP_GETDATA){
			return PlaOrdinaryShopBAC.getInstance().getShopData(pid);
		} else
		if(act == ACT_ORDINARY_SHOP_BUY){
			int index = dis.readInt();
			return PlaOrdinaryShopBAC.getInstance().buy(pid, index);
		} else
		if(act == ACT_ORDINARY_SHOP_REFRESH){
			return PlaOrdinaryShopBAC.getInstance().refreshShop(pid);
		} else
		if(act == ACT_MYSTERY_SHOP_GETDATA){
			return PlaMysteryShopBAC.getInstance().getShopData(pid);
		} else
		if(act == ACT_MYSTERY_SHOP_BUY){
			int index = dis.readInt();
			return PlaMysteryShopBAC.getInstance().buy(pid, index);
		} else
		if(act == ACT_JJ_SHOP_GETDATA){
			return PlaJJShopBAC.getInstance().getShopData(pid);
		} else
		if(act == ACT_JJ_SHOP_BUY){
			int index = dis.readInt();
			return PlaJJShopBAC.getInstance().buy(pid, index);
		} else
		if(act == ACT_JJ_SHOP_REFRESH){
			return PlaJJShopBAC.getInstance().refreshShop(pid);
		} else
		if(act == ACT_FACTION_SHOP_GETDATA){
			return PlaFactionShopBAC.getInstance().getShopData(pid);
		} else
		if(act == ACT_FACTION_SHOP_BUY){
			int index = dis.readInt();
			return PlaFactionShopBAC.getInstance().buy(pid, index);
		} else
		if(act == ACT_FACTION_SHOP_REFRESH){
			return PlaFactionShopBAC.getInstance().refreshShop(pid);
		} else
		if(act == ACT_SP_SHOP_GETDATA){
			return PlaSpShopBAC.getInstance().getShopData(pid);
		} else
		if(act == ACT_SP_SHOP_BUY){
			int index = dis.readInt();
			return PlaSpShopBAC.getInstance().buy(pid, index);
		} else
		if(act == ACT_SP_SHOP_REFRESH){
			return PlaSpShopBAC.getInstance().refreshShop(pid);
		} else 
		if(act == ACT_JJC_RANKING_GET_INFO){
			return PlaJJCRankingBAC.getInstance().getInfo(pid);
		} else 
		if(act == ACT_JJC_RANKING_GET_OPPS){
			return PlaJJCRankingBAC.getInstance().getOpps(pid);
		} else 
		if(act == ACT_JJC_RANKING_BATTLE){
			int opppid = dis.readInt();
			int pRanking = dis.readInt();
			int oppranking = dis.readInt();
			String posarrStr = dis.readUTF();
			return PlaJJCRankingBAC.getInstance().toBattle(pid, opppid, pRanking, oppranking, posarrStr);
		} else 
		if(act == ACT_JJC_RANKING_GETRANKINGDATA){
			return PlaJJCRankingBAC.getInstance().getRankingData(pid);
		} else 
		if(act == ACT_JJC_RANKING_REFRESH_OPPS){
			return PlaJJCRankingBAC.getInstance().refreshOpps(pid);
		} else 
		if(act == ACT_JJC_RANKING_CLEAR_CD){
			return PlaJJCRankingBAC.getInstance().clearCD(pid);
		} else 
		if(act == ACT_JJC_RANKING_RESET_CHA_AM){
			return PlaJJCRankingBAC.getInstance().resetChallengeAmount(pid);
		} else 
		if(act == ACT_JJC_RANKING_SET_DEFFORMACTION){
			String posarrStr = dis.readUTF();
			return PlaJJCRankingBAC.getInstance().setDefForm(pid, posarrStr);
		} else 
		if(act == ACT_JJC_RANKING_GET_OPPDEFDATA){
			int oppid = dis.readInt();
			return PlaJJCRankingBAC.getInstance().getOppDefFormData(pid, oppid);
		} else 
		if(act == ACT_PLA_WELFARE_GETTASKAWARD){
			int num = dis.readInt();
			return PlaWelfareBAC.getInstance().getTaskAward(pid, num);
		} else
		if(act == ACT_PLA_WELFARE_GETACHIEVEAWARD){
			int num = dis.readInt();
			return PlaWelfareBAC.getInstance().getAchievementAward(pid, num);
		} else
		if(act == ACT_PLA_WELFARE_CHECKIN){
			return PlaWelfareBAC.getInstance().checkin(pid);
		} else
		if(act == ACT_PLA_WELFARE_GETCHECKINAWARD){
			int num = dis.readInt();
			return PlaWelfareBAC.getInstance().getCheckinAward(pid, num);
		} else
		if(act == ACT_PLA_WELFARE_GETTASKAWARD_ONEKEY){
			String numStr = dis.readUTF();
			return PlaWelfareBAC.getInstance().getTaskAwardOneKey(pid, numStr);
		} else
		if(act == ACT_PLA_WELFARE_GETACHIEVEAWARD_ONEKEY){
			String numStr = dis.readUTF();
			return PlaWelfareBAC.getInstance().getAchievementAwardOneKey(pid, numStr);
		} else
		if(act == ACT_PLA_WELFARE_GETTARGETAWARD){
			int num = dis.readInt();
			return PlaWelfareBAC.getInstance().gerTargetAward(pid, num);
		} else
		if(act == ACT_PLA_SUMMON_ORDINARY){
			byte multi = dis.readByte();
			return PlaSummonBAC.getInstance().summonOrdinary(pid, multi);
		} else
		if(act == ACT_PLA_SUMMON_ADVANCED){
			byte multi = dis.readByte();
			return PlaSummonBAC.getInstance().summonAdvanced(pid, multi);
		} else
		if(act == ACT_PLA_SUMMON_MYSTERY){
			return PlaSummonBAC.getInstance().summonMystery(pid);
		} else 
		if(act == ACT_CB_DECLAREWAR){
			int citynum = dis.readInt();
			String teamidStr = dis.readUTF();
			return CBBAC.getInstance().declareWar(pid, citynum, teamidStr);
		} else 
		if(act == ACT_CB_ADDTEAM){
			String posarrStr = dis.readUTF();
			return CBBAC.getInstance().createTeamToPool(pid, posarrStr);
		} else 
		if(act == ACT_CB_REMOVETEAM){
			int teamid = dis.readInt();
			return CBBAC.getInstance().cancelTeamFromPool(pid, teamid);
		} else 
		if(act == ACT_CB_JOINWAR){
			int mapkey = dis.readInt();
			byte teamType = dis.readByte();
			String posarrStr = dis.readUTF();
			return CBBAC.getInstance().joinWar(pid, mapkey, teamType, posarrStr);
		} else 
		if(act == ACT_CB_RELIVEPARTNER){
			String partneridarrStr = dis.readUTF();
			return CBBAC.getInstance().relivePartner(pid, partneridarrStr);
		} else 
		if(act == ACT_CB_CONTENDLEADER){
			int citynum = dis.readInt();
			String posarrStr = dis.readUTF();
			return CBBAC.getInstance().contendLeader(pid, citynum, posarrStr);
		} else 
		if(act == ACT_CB_SET_LEADERDEFFORM){
			String posarrStr = dis.readUTF();
			return CBBAC.getInstance().setLeaderDefForm(pid, posarrStr);
		} else 
		if(act == ACT_CB_GIVEUP_LEADER){
			return CBBAC.getInstance().giveupLeader(pid);
		} else 
		if(act == ACT_CB_GETDATA){
			return CBBAC.getInstance().getData(pid);
		} else 
		if(act == ACT_CB_GET_TEAMPOOLDATA){
			return CBBAC.getInstance().getTeamPoolData(pid);
		} else 
		if(act == ACT_CB_GET_PARTNERSTATE){
			return CBBAC.getInstance().getPartnerState(pid);
		} else 
		if(act == ACT_CB_GET_CITYBATTLEDATA){
			int mapkey = dis.readInt();
			return CBBAC.getInstance().getCityBattleData(pid, mapkey);
		} else 
		if(act == ACT_CB_GET_LEADERDATA){
			int citynum = dis.readInt();
			return CBBAC.getInstance().getLeaderData(pid, citynum);
		} else 
		if(act == ACT_CB_DISPATCH_TEAMPOOL){
			int citynum = dis.readInt();
			String teamidStr = dis.readUTF();
			return CBBAC.getInstance().dispatchTeampool(pid, citynum, teamidStr);
		} else 
		if(act == ACT_CB_GET_KILLRANKING){
			int mapkey = dis.readInt();
			return CBBAC.getInstance().getKillRanking(pid, mapkey);
		} else 
		if(act == ACT_CB_GET_BATTLERLIST){
			int mapkey = dis.readInt();
			byte teamType = dis.readByte();
			return CBBAC.getInstance().getBattlerList(pid, mapkey, teamType);
		} else 	
		if(act == ACT_PLA_TRIAL_MONEY_START){
			int num = dis.readInt();
			String posStr = dis.readUTF();
			return PlaTrialMoneyBAC.getInstance().startTrial(pid, num, posStr);
		} else
		if(act == ACT_PLA_TRIAL_MONEY_END){
			int num = dis.readInt();
			String battleRecord = dis.readUTF();
			return PlaTrialMoneyBAC.getInstance().endTrial(pid, num, battleRecord);
		} else
		if(act == ACT_PLA_TRIAL_EXP_START){
			int num = dis.readInt();
			String posStr = dis.readUTF();
			return PlaTrialExpBAC.getInstance().startTrial(pid, num, posStr);
		} else
		if(act == ACT_PLA_TRIAL_EXP_END){
			int num = dis.readInt();
			String battleRecord = dis.readUTF();
			return PlaTrialExpBAC.getInstance().endTrial(pid, num, battleRecord);
		} else
		if(act == ACT_PLA_TRIAL_PARTNER_START){
			int num = dis.readInt();
			String posStr = dis.readUTF();
			return PlaTrialPartnerBAC.getInstance().startTrial(pid, num, posStr);
		} else
		if(act == ACT_PLA_TRIAL_PARTNER_END){
			int num = dis.readInt();
			String battleRecord = dis.readUTF();
			return PlaTrialPartnerBAC.getInstance().endTrial(pid, num, battleRecord);
		} else 
		if(act == ACT_SUPPLY_BUY_MONEY){
			return PlaSupplyBAC.getInstance().buyMoney(pid);
		} else 
		if(act == ACT_SUPPLY_BUY_ENERGY){
			return PlaSupplyBAC.getInstance().buyEnergy(pid);
		} else 
		if(act == ACT_SUPPLY_GET_TQCOIN){
			return PlaSupplyBAC.getInstance().getTqCoin(pid);
		} else 
		if(act == ACT_FACCOPYMAP_INTO){
			int posnum = dis.readInt();
			String posStr = dis.readUTF();
			return FacCopymapBAC.getInstance().into(pid, posnum, posStr);
		} else 
		if(act == ACT_FACCOPYMAP_END){
			String battleRecord = dis.readUTF();
			return FacCopymapBAC.getInstance().end(pid, battleRecord);
		} else 
		if(act == ACT_FACCOPYMAP_RESETMAP){
			int mapnum = dis.readInt();
			return FacCopymapBAC.getInstance().resetMap(pid, mapnum);
		} else 
		if(act == ACT_FACTIONMAP_EXIT){
			return FacCopymapBAC.getInstance().exit(pid);
		} else 
		if(act == ACT_ARTIFACT_EATITEM){
			int num = dis.readInt();
			String itemdata = dis.readUTF();
			return ArtifactBAC.getInstance().eatItem(pid, num, itemdata);
		} else 
		if(act == ACT_ARTIFACT_COININPUT){
			int num = dis.readInt();
			int upamount = dis.readInt();
			return ArtifactBAC.getInstance().coinInput(pid, num, upamount);
		} else 
		if(act == ACT_ARTIFACT_COMP){
			int num = dis.readInt();
			return ArtifactBAC.getInstance().comp(pid, num);
		} else 
		if(act == ACT_ARTIFACT_RECOVERROBTIMES){
			return PlaRoleBAC.getInstance().recoverArtifactRobTimes(pid);
		} else 
		if(act == ACT_ARTIFACT_OPENPROTECT){
			return ArtifactBAC.getInstance().openRobProtect(pid);
		} else 
		if(act == ACT_GET_BATTLE_POWER_RANKING){
			return RankingBAC.getInstance().getBattlePowerRanking(pid);
		} else
		if(act == ACT_FRIEND_ADD){
			String friends = dis.readUTF();
			byte type = dis.readByte();
			return FriendBAC.getInstance().addFriends(pid, friends, type);
		} else
		if(act == ACT_FRIEND_DELLTE){
			String friends = dis.readUTF();
			byte type = dis.readByte();
			return FriendBAC.getInstance().deleteFriends(pid, friends, type);
		} else
		if(act == ACT_FRIEND_SEARCH){
			String condition = dis.readUTF();
			return FriendBAC.getInstance().search(pid, condition);
		} else
		if(act == ACT_FRIEND_SEARCH_QUIK){
			return FriendBAC.getInstance().quickSearch(pid);
		} else
		if(act == ACT_FRIEND_PRESENT){
			int friendid = dis.readInt();
			return FriendBAC.getInstance().presentEnergy(pid, friendid);
		} else
		if(act == ACT_FRIEND_PRESENT_ONEKEY){
			String friends = dis.readUTF();
			return FriendBAC.getInstance().presentEnergyOneKey(pid, friends);
		} else
		if(act == ACT_FRIEND_GETENERGY){
			int friendid = dis.readInt();
			return FriendBAC.getInstance().getEnergy(pid, friendid);
		} else
		if(act == ACT_FRIEND_GETENERGY_ONEKEY){
			return FriendBAC.getInstance().getEnergyOneKey(pid);
		} else
		if(act == ACT_WORLD_BOSS_JOIN){
			return WorldBossBAC.getInstance().join(pid);
		} else
		if(act == ACT_WORLD_BOSS_BATTLE){
			String posStr = dis.readUTF();
			return WorldBossBAC.getInstance().toBattle(pid, posStr);
		} else
		if(act == ACT_WORLD_BOSS_GETDATA){
			return WorldBossBAC.getInstance().getData(pid);
		} else
		if(act == ACT_TOWER_ENTER){
			int layer = dis.readInt();
			byte diff = dis.readByte();
			String posStr = dis.readUTF();
			return PlaTowerBAC.getInstance().enter(pid, layer, diff, posStr);
		} else
		if(act == ACT_TOWER_END){
			String battleRecord = dis.readUTF();
			return PlaTowerBAC.getInstance().end(pid, battleRecord);
		} else
		if(act == ACT_TOWER_SHOP_GETDATA){
			return PlaTowerShopBAC.getInstance().getShopData(pid);
		} else
		if(act == ACT_TOWER_SHOP_BUY){
			int index = dis.readInt();
			return PlaTowerShopBAC.getInstance().buy(pid, index);
		} else
		if(act == ACT_TOWER_SHOP_REFRESH){
			return PlaTowerShopBAC.getInstance().refreshShop(pid);
		} else
		if(act == ACT_TEAM_ACTI_CREATE){
			int type = dis.readInt();
			return PlaTeamBAC.getInstance().createTeam(pid, type);
		} else
		if(act == ACT_TEAM_ACTI_JOIN){
			int num = dis.readInt();
			return PlaTeamBAC.getInstance().joinTeam(pid, num);
		} else
		if(act == ACT_TEAM_ACTI_KICK){
			int num = dis.readInt();
			int memberid = dis.readInt();
			return PlaTeamBAC.getInstance().kickOut(pid, num, memberid);
		} else
		if(act == ACT_TEAM_ACTI_FORMAT){
			int num = dis.readInt();
			String posStr = dis.readUTF();
			return PlaTeamBAC.getInstance().format(pid, num, posStr);
		} else
		if(act == ACT_TEAM_ACTI_BEREADY){
			int num = dis.readInt();
			return PlaTeamBAC.getInstance().beReady(pid, num);
		} else
		if(act == ACT_TEAM_ACTI_CANCELREADY){
			int num = dis.readInt();
			return PlaTeamBAC.getInstance().cancelReady(pid, num);
		} else
		if(act == ACT_TEAM_ACTI_BATTLE){
			int num = dis.readInt();
			return PlaTeamBAC.getInstance().battle(pid, num);
		} else
		if(act == ACT_TEAM_ACTI_GETTEAMLIST){
			int type = dis.readInt();
			return PlaTeamBAC.getInstance().getTeamList(pid, type);
		} else
		if(act == ACT_TEAM_ACTI_GETDATA){
			return PlaTeamBAC.getInstance().getDate(pid);
		} else
		if(act == ACT_TEAM_ACTI_EXIT){
			int num = dis.readInt();
			return PlaTeamBAC.getInstance().exitTeam(pid, num);
		} else 
		if(act == ACT_MINERALS_CLOCKIN){
			int num = dis.readInt();
			return PlaMineralsBAC.getInstance().clockIn(pid, num);
		} else 
		if(act == ACT_MINERALS_CONDENT){
			int opppid = dis.readInt();
			int num = dis.readInt();
			String posarrStr = dis.readUTF();
			return PlaMineralsBAC.getInstance().contend(pid, opppid, num, posarrStr);
		} else 
		if(act == ACT_MINERALS_SETDEF){
			String posarrStr = dis.readUTF();
			return PlaMineralsBAC.getInstance().setDefForm(pid, posarrStr);
		} else 
		if(act == ACT_MINERALS_GETINFO){
			return PlaMineralsBAC.getInstance().getInfo(pid);
		} else 
		if(act == ACT_MINERALS_GETPOSDATA){
			return PlaMineralsBAC.getInstance().getPosData();
		} else 
		if(act == ACT_MINERALS_GETOWNERDATA){
			int targetpid = dis.readInt();
			return PlaMineralsBAC.getInstance().getOwnerData(targetpid);
		} else 
		if(act == ACT_DEBUG_RESETDAYDATE){
			return PlayerBAC.getInstance().checkAndResetDayData(pid, true);
		} else
		if(act == ACT_DEBUG_SERVER_BATTLE){
			String posarr1 = dis.readUTF();
			int oppid = dis.readInt();
			String posarr2 = dis.readUTF();
			return PartnerBAC.getInstance().debugServerBattle(pid, posarr1, oppid, posarr2);
		} else 
		if(act == ACT_DEBUG_GET_BATTLEBOX){
			String posarr1 = dis.readUTF();
			int oppid = dis.readInt();
			String posarr2 = dis.readUTF();
			return PartnerBAC.getInstance().debugGetBattleBox(pid, posarr1, oppid, posarr2);
		} else 
		if(act == ACT_DEBUG_SERVER_VERIFY){
			String battleRecord = dis.readUTF();
			return PartnerBAC.getInstance().debugServerVerify(pid, battleRecord);
		} else 
		{
			return new ReturnValue(false, "��Ч����");
		}
	}
}

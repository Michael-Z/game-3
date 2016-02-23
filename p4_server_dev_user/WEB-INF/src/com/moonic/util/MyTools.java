package com.moonic.util;

import java.sql.Date;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Hashtable;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.json.JSONArray;
import org.json.JSONObject;

import server.common.Sortable;
import server.common.Tools;

import com.ehc.common.ToolFunc;
import com.moonic.mgr.LockStor;

/**
 * ���߼�
 * @author John
 */
public class MyTools {
	public static final long long_minu = 60 * 1000;
	public static final long long_hour = 60 * long_minu;
	public static final long long_day = 24 * long_hour;
	
	/**
	 * �޶�ʱ��
	 * @param week	���ڼ��� 1��ʾ�����졢2��ʾ����һ...7��ʾ������
	 * @param hour	Сʱ��24Сʱ�ƣ�0��ʾ�賿0�㡢12��ʾ����12�㡢17��ʾ����5��
	 * @param minute	���ӣ�0~59
	 * @return	������
	 */
	public static long setDateTime(int week, int hour, int minute) {
		Calendar cal = Calendar.getInstance();	//��ǰ����
		cal.set(Calendar.DAY_OF_WEEK,week);	//���ڼ�
		cal.set(Calendar.HOUR_OF_DAY, hour);	//ʱ��24Сʱ��
		cal.set(Calendar.MINUTE, minute);	
		cal.set(Calendar.SECOND, 0);
		return cal.getTimeInMillis();	//���ش�ʱ��ĺ�����
	}
	
	/**
	 * ��ȡָ������0����¸�ʱ���
	 * @param time ���ڼ������ʼʱ��
	 * @param d_num ���ڱ�� [������~������ = 1~7]
	 * @param pointtime ����ʱ���
	 */
	public static long getNextWeekDay(long time, int d_num, long pointtime){
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(time);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		int week = cal.get(Calendar.DAY_OF_WEEK);
		int add = 0;
		if(week < d_num){
			add = d_num - week;
		} else 
		if(week == d_num){
			if(time - MyTools.getCurrentDateLong(time) < pointtime){
				add = d_num - week;
			} else {
				add = 7 + d_num - week;
			}
		} else {
			add = 7 + d_num - week;
		}
		long thetime = cal.getTimeInMillis() + add * long_day;
		return thetime;
	}
	
	/**
	 * �������ʱ���Ƿ񾭹�ָ����һ
	 */
	public static boolean checkWeek(long start, long end){
		long ftime = getFirstDayOfWeek();
		return start < ftime && end >= ftime;
	}
	
	/**
	 * �������ʱ���Ƿ񾭹�����
	 */
	public static boolean checkMonth(long start, long end){
		long ftime = getFirstDayOfMonth();
		return start < ftime && end >= ftime;
	}
	
	/**
	 * ��ȡ��һʱ�����
	 */
	public static long getFirstDayOfWeek(){
		return getCurrentDateLong() - (getWeekEx()-1) * long_day;
	}	

	
	/**
	 * ��ȡ����ʱ�����
	 */
	public static long getFirstDayOfMonth(){
		return getCurrentDateLong() - (getMonthDay()-1)*long_day;
	}
	
	/**
	 * ��ȡ��ǰʱ����ܴ�
	 */
	public static int getWeek(){
		return getWeek(System.currentTimeMillis());
	}
	
	
	/**
	 * ��ȡ��һΪ��һ����ܴ�
	 * @return
	 */
	public static int getWeekEx()
	{
		return getWeekEx(System.currentTimeMillis());
	} 
	/**
	 * ��ȡָ��ʱ����ܴ�
	 */
	public static int getWeek(long time){
		return getCal(time).get(Calendar.DAY_OF_WEEK);
	}
	
	/**
	 * ��ȡ��һΪ��һ����ܴ�
	 * @param time
	 * @return
	 */
	public static int getWeekEx(long time)
	{
		int weekNum = getCal(time).get(Calendar.DAY_OF_WEEK);
		if(weekNum==1) //����
		{
			weekNum=7;
		}
		else
		{
			weekNum = weekNum-1; //��һ��������2��7�ĳ�1��6
		}
		return weekNum;
	}
	
	/**
	 * ��ȡָ��ʱ�������
	 */
	public static int getMonthDay(){
		return getMonthDay(System.currentTimeMillis());
	}
	
	/**
	 * ��ȡָ��ʱ�������
	 */
	public static int getMonthDay(long time){
		return getCal(time).get(Calendar.DAY_OF_MONTH);
	}
	
	/**
	 * ��ȡʱ�����
	 */
	public static Calendar getCal(long time){
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(time);
		return cal;
	}
	
	/**
	 * ���ϵͳʱ���Ƿ񵽴�(����)ָ��ʱ��
	 */
	public static boolean checkSysTimeBeyondSqlDate(Timestamp timestamp){
		return checkSysTimeBeyondSqlDate(timestamp, 0, true);
	}
	
	/**
	 * ���ϵͳʱ���Ƿ񵽴�(����)ָ��ʱ��
	 * @param offtime ��ָ��ʱ��ƫ��
	 */
	public static boolean checkSysTimeBeyondSqlDate(Timestamp timestamp, long offtime){
		return checkSysTimeBeyondSqlDate(timestamp, offtime, true);
	}
	
	/**
	 * ���ϵͳʱ���Ƿ񵽴�(����)ָ��ʱ��
	 */
	public static boolean checkSysTimeBeyondSqlDate(Timestamp timestamp, long offtime, boolean defaultResult){
		if(timestamp != null){
			long sqlTime = timestamp.getTime();
			return System.currentTimeMillis() >= sqlTime+offtime;
		} else {
			return defaultResult;
		}
	}
	
	/**
	 * ���ָ��ʱ���Ƿ񳬹����ݿ�ʱ��
	 */
	public static boolean checkTheTimeBeyondSqlDate(Timestamp timestamp, long thetime){
		return checkTheTimeBeyondSqlDate(timestamp, thetime, true);
	}
	
	/**
	 * ���ָ��ʱ���Ƿ񳬹����ݿ�ʱ��
	 */
	public static boolean checkTheTimeBeyondSqlDate(Timestamp timestamp, long thetime, boolean defaultResult){
		if(timestamp != null){
			long sqlTime = timestamp.getTime();
			return thetime >= sqlTime;
		} else {
			return defaultResult;
		}
	}
	
	/**
	 * ���ϵͳʱ���Ƿ񵽴�(����)ָ��ʱ��
	 */
	public static boolean checkSysTimeBeyondSqlDate(String thetime){
		return checkSysTimeBeyondSqlDate(MyTools.getTimeLong(thetime));
	}
	
	/**
	 * ���ϵͳʱ���Ƿ񵽴�(����)ָ��ʱ��
	 */
	public static boolean checkSysTimeBeyondSqlDate(long thetime){
		return System.currentTimeMillis() >= thetime;
	}
	
	/**
	 * ��ȡ���ڵĺ�����ʽ
	 */
	public static long getCurrentDateLong(){
		return getCurrentDateLong(System.currentTimeMillis());
	}
	
	/**
	 * ����ָ��ʱ����ȡʱ����LONG��ʽ
	 */
	public static long getPointTimeLong(String ptStr){
		return Tools.str2date(MyTools.getDateStr()+" "+ptStr).getTime()-MyTools.getCurrentDateLong();
	}
	
	/**
	 * ��ȡʱ�� long ��ʽ
	 */
	public static long getTimeLong(String str){
		if(str != null && !str.equals("") && !str.equals("null")){
			return Tools.str2date(str).getTime();
		} else {
			return 0;
		}
	}
	
	/**
	 * ��ȡ��ȷ��Сʱ�ĺ�����ʽ
	 */
	public static long getCurrentHourLong(long time){
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(time);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		return calendar.getTimeInMillis();
	}
	
	/**
	 * ��ȡ���ڵĺ�����ʽ
	 */
	public static long getCurrentDateLong(long time){
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(time);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		return calendar.getTimeInMillis();
	}
	
	/**
	 * ����ַ����Ƿ���ָ��������
	 */
	public static boolean checkInStrArr(String[] arr, String str){
		for(int i = 0; arr != null && i < arr.length; i++){
			if(arr[i].equals(str)){
				return true;
			}
		}
		return false;
	}
	
	/**
	 * ��ȡʱ�� long ��ʽ
	 */
	public static long getTimeLong(Timestamp timestamp){
		if(timestamp != null){
			return timestamp.getTime();
		} else {
			return 0;
		}
	}
	
	public static final char[] noChar = {'\'', ' ', '(', ')', '='};
	
	/**
	 * ����Ƿ��������ַ�
	 */
	public static void checkNoChar(String str) throws Exception {
		checkNoChar(str, noChar);
	}
	
	/**
	 * ����Ƿ��������ַ�
	 */
	public static void checkNoChar(String str, char[] noChar) throws Exception {
		if(str != null){
			for(int i = 0; i < noChar.length; i++){
				if(str.indexOf(noChar[i]) != -1){
					BACException.throwInstance("�зǷ��ַ�������ĺ�����");
				}
			}		
		}
	}
	
	/**
	 * ����Ƿ��������ַ�
	 */
	public static void checkNoCharEx(String str, char... addNoChar) throws Exception {
		if(str != null){
			for(int i = 0; i < noChar.length; i++){
				if(str.indexOf(noChar[i]) != -1){
					BACException.throwInstance("�зǷ��ַ�������ĺ�����");
				}
			}
			for(int i = 0; addNoChar != null && i < addNoChar.length; i++){
				if(str.indexOf(addNoChar[i]) != -1){
					BACException.throwInstance("�зǷ��ַ�������ĺ�����");
				}
			}
		}
	}
	
	private static Random ran = new Random(System.currentTimeMillis());
	
	public static void main2(String[] args){
		getRandom(0, -2);
	}
	
	/**
	 * ��� ָ����Χ�������(����end)
	 */
	public static int getRandom(int startInt, int endInt) {
		return getRandom(ran, startInt, endInt);
	}
	
	private static Hashtable<Integer, Hashtable<Short, Random>> randomStor = new Hashtable<Integer, Hashtable<Short,Random>>();
	
	public static final short RAN_SPIRIT_SMELT = 1;
	public static final short RAN_SPIRIT_DEBRIS = 2;
	public static final short RAN_SPIRIT_ROLE = 3;
	public static final short RAN_ESCORT_REFRESH = 4;
	public static final short RAN_SPIN = 5;
	public static final short RAN_MSHOP_REFRESH_1 = 6;
	public static final short RAN_MSHOP_REFRESH_3 = 7;
	public static final short RAN_MSHOP_REFRESH_4 = 8;
	public static final short RAN_MSHOP_REFRESH_5 = 9;
	public static final short RAN_MSHOP_REFRESH_6 = 10;
	
	/**
	 * ��� ָ����Χ�������(����end)
	 */
	public static int getRandom(int playerid, short type, int startInt, int endInt) {
		Random random = null;
		synchronized (LockStor.getLock(LockStor.RANDOM_NEXT, playerid)) {
			Hashtable<Short, Random> stor = randomStor.get(playerid);
			if(stor == null){
				stor = new Hashtable<Short, Random>();
				randomStor.put(playerid, stor);
				//System.out.println("����"+playerid+"����⵽����ܿ�");
			}
			random = stor.get(type);
			if(random == null){
				synchronized (LockStor.getLock(LockStor.RANDOM_TIME)) {
					random = new Random(ran.nextLong());
				}
				stor.put(type, random);
				//System.out.println("����"+playerid+"��"+type+"���͵������");
			}	
		}
		return getRandom(random, startInt, endInt);
	}
	
	/**
	 * ������������
	 */
	public static void cleanRandom(int playerid){
		synchronized (LockStor.getLock(LockStor.RANDOM_NEXT, playerid)) {
			randomStor.remove(playerid);
			//System.out.println("������ܿ����"+playerid+"�������");
		}
	}
	
	/**
	 * ��jsonarr2��Ԫ�ؼ���jsonarr1��
	 */
	public static void combJsonarr(JSONArray jsonarr1, JSONArray jsonarr2){
		for(int i = 0; i < jsonarr2.length(); i++){
			jsonarr1.add(jsonarr2.opt(i));
		}
	}
	
	/**
	 * ��� ָ����Χ�������(����end)
	 */
	public static int getRandom(Random random, int startInt, int endInt) {
		if (endInt < startInt) {
			try {
				throw new Exception("������쳣��"+startInt+"~"+endInt);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return startInt;
		}
		return startInt + Math.abs(random.nextInt()) % (endInt - startInt + 1);
	}
	
	/**
     * ������ת��Ϊ�ַ���
     */
    public static String formatTime(String format){
    	return formatTime(System.currentTimeMillis(), format);
    }
	
	/**
     * ������ת��Ϊ�ַ���
     */
    public static String formatTime(long time, String format){
    	SimpleDateFormat sdf = new SimpleDateFormat(format);
        String str = sdf.format(new Date(time));
        return str;
    }
    
    /**
     * ��ȡ��ǰ���ڵ�str��ʽ
     */
    public static String getDateStr(){
    	return getDateStr(System.currentTimeMillis());
    }
    
    /**
     * ��ȡָ�����ڵ�str��ʽ
     */
    public static String getDateStr(Timestamp timestamp){
    	return getDateStr(MyTools.getTimeLong(timestamp));
    }
    
    /**
     * ��ȡָ�����ڵ�str��ʽ
     */
    public static String getDateStr(long time){
    	return MyTools.formatTime(time, "yyyy-MM-dd");
    }
    
    /**
     * ��ȡ��ǰʱ���str��ʽ
     */
    public static String getTimeStr(){
    	return getTimeStr(System.currentTimeMillis());
    }
    
    /**
     * ��ȡָ��ʱ���str��ʽ
     */
    public static String getTimeStr(Timestamp timestamp){
    	return getTimeStr(MyTools.getTimeLong(timestamp));
    }
    
    /**
     * ��ȡָ��ʱ���str��ʽ
     */
    public static String getTimeStr(long time){
    	return formatTime(time, "yyyy-MM-dd HH:mm:ss");
    }
    
    /**
     * ��ȡָ��ʱ���str��ʽ
     */
    public static String getTimeMSStr(){
    	return getTimeMSStr(System.currentTimeMillis());
    }
    
    /**
     * ��ȡָ��ʱ���str��ʽ
     */
    public static String getTimeMSStr(long time){
    	return formatTime(time, "yyyy-MM-dd HH:mm:ss.SSS");
    }
    
    /**
     * ��ȡ��ǰʱ���Timestamp��ʽ
     */
    public static Timestamp getTimestamp(){
    	return getTimestamp(System.currentTimeMillis());
    }
    
    /**
     * ��ȡָ��ʱ���Timestamp��ʽ
     */
    public static Timestamp getTimestamp(long time){
    	return java.sql.Timestamp.valueOf(Tools.formatDate(getTimeStr(time), "yyyy-MM-dd HH:mm:ss"));
    }
    
    /**
     * ��ȡ��ǰ���ڵ�sql��ʽ
     */
    public static String getDateSQL(){
    	return getDateSQL(System.currentTimeMillis());
    }
    
    /**
     * ��ȡָ�����ڵ�sql��ʽ
     */
    public static String getDateSQL(Timestamp timestamp){
    	return getDateSQL(MyTools.getTimeLong(timestamp));
    }
    
    /**
     * ��ȡָ�����ڵ�sql��ʽ
     */
    public static String getDateSQL(long time){
    	return "to_date('" + MyTools.formatTime(time, "yyyy-MM-dd") + "' ,'YYYY-MM-DD')";
    }
    
    /**
     * ��ȡ��ǰʱ���sql��ʽ
     */
    public static String getTimeSQL(){
    	return getTimeSQL(System.currentTimeMillis());
    }
    
    /**
     * ��ȡָ��ʱ���sql��ʽ
     */
    public static String getTimeSQL(Timestamp timestamp){
    	return getTimeSQL(MyTools.getTimeLong(timestamp));
    }
    
    /**
     * ��ȡָ��ʱ���sql��ʽ
     */
    public static String getTimeSQL(long time){
    	return "to_date('" + MyTools.formatTime(time, "yyyy-MM-dd HH:mm:ss") + "' ,'YYYY-MM-DD HH24:MI:SS')";
    }
    
    /**
     * ����int��JSON���飬ǰ��Ϊint����0
     */
    public static void putByNoZero(JSONObject jsonobj, String key, int value){
    	if(value > 0){
    		jsonobj.put(key, value);
    	}
    }
    
    /**
     * ��ʽ��JSONARR�����ʽ
     */
    public static String getFormatJsonarrStr(JSONArray jsonarr){
    	StringBuffer sb = new StringBuffer("");
    	for(int i = 0; jsonarr!=null && i<jsonarr.length(); i++){
    		sb.append(jsonarr.opt(i).toString()+"\r\n");
    	}
    	return sb.toString();
    }
    
    /**
     * ������ת��ΪSQL����
     */
    public static String converWhere(String sign, String column, String operator, int[] paras){
		StringBuffer sb = new StringBuffer("");
		for(int i = 0; i<paras.length; i++){
    		if(i > 0){
    			sb.append(" ");
    			sb.append(sign);
    			sb.append(" ");
    		}
    		sb.append(column);
    		sb.append(operator);
    		sb.append(paras[i]);
    	}
    	return sb.toString();
    }
    
    /**
     * ������ת��ΪSQL����
     */
    public static String converWhere(String sign, String column, String operator, String[] paras){
		StringBuffer sb = new StringBuffer("");
		for(int i = 0; i<paras.length; i++){
    		if(i > 0){
    			sb.append(" ");
    			sb.append(sign);
    			sb.append(" ");
    		}
    		sb.append(column);
    		sb.append(operator);
    		sb.append("'");
    		sb.append(paras[i]);
    		sb.append("'");
    	}
    	return sb.toString();
    }
    
    /**
     * ������ת��ΪSQL����
     */
    public static String converTimeWhere(String column, String[] paras){
		StringBuffer sb = new StringBuffer("");
		String sign = " or ";
    	for(int i = 0; i<paras.length; i++){
    		if(i > 0){
    			sb.append(sign);
    		}
    		sb.append(column);
    		sb.append("=");
    		sb.append(paras[i]);
    	}
    	return sb.toString();
    }
    
    /**
     * ��ʽ��ʱ���Ϊʱ�����ַ�����ʽ
     */
    public static String formatHMS(long timelen){
    	try {
    		DecimalFormat df = new DecimalFormat("00");
    		String hour = df.format(timelen / (60 * 60 * 1000));
    		timelen = timelen % (60 * 60 * 1000);
    		String minu = df.format(timelen / (60 * 1000));
    		timelen = timelen % (60 * 1000);
    		String sec = df.format(timelen / 1000);
    		return hour+":"+minu+":"+sec;
    	} catch (Exception e) {
			return "��ʽ������";
		}
    }
    
    private static ArrayList<ScheduledExecutorService> timerlist = new ArrayList<ScheduledExecutorService>();
    
    /**
     * ����Timer����
     */
    public static ScheduledExecutorService createTimer(int threadamount){
    	ScheduledExecutorService timer = Executors.newScheduledThreadPool(threadamount);
    	timerlist.add(timer);
    	return timer;
    }
    
    /**
     * ȡ��Timer
     */
    public static void cancelTimer(ScheduledExecutorService timer){
    	if(timer != null){
    		timerlist.remove(timer);
        	timer.shutdownNow();
    	}
    }
    
    /**
     * ȡ������Timer
     */
    public static void closeAllTimer(){
    	for(int i = 0; i < timerlist.size(); i++){
    		ScheduledExecutorService timer = timerlist.get(i);
    		if(timer != null)
    		{
    			try
    			{
    				timer.shutdownNow();
    			}
    			catch(Exception ex){}
    		}
    	}
    	timerlist = null;
    }
    
    /**
	 * ͨ��ָ������ͱ�Ų�����ȡ��Ӧ�±�
	 * @return ��ѯ�����-1��ʾδ�ҵ�
	 */
	public static int getIndexByInt(int[] numArr , int num){
		int result = -1;
		for(int i = 0 ; numArr != null && i < numArr.length ; i++){
			if(numArr[i] == num){
				result = i;
				break;
			}
		}
		return result;
	}
	
	/**
	 * ͨ��ָ��ԭʼ�������飬��źͱ���±��ȡ��Ӧ�±�
	 * @return ��ѯ�����-1��ʾδ�ҵ�
	 */
	public static int getIndexByString2(String[][] source, int index, int num){
		int result = -1;
		for(int i = 0; source != null && i < source.length; i++){
			if(Tools.str2int(source[i][index]) == num){
				result = i;
				break;
			}
		}
		return result;
	}
	
	public static void main1(String[] args){
		String str1 = "420112198809152718";
		System.out.println(getEncrypeStr(str1, 6, str1.length()-2));
		String str2 = "1b";
		System.out.println(getEncrypeStr(str2, str2.length()/2, str2.length()));
		String str3 = "15021592157";
		System.out.println(getEncrypeStr(str3, 3, 6));
	}
	
	/**
	 * ��ȡ�Ǻż����ַ���
	 */
	public static String getEncrypeStr(String str, int start_ind, int end_ind){
		if(str!=null && !str.equals("") && start_ind<end_ind && str.length()>=end_ind){
			StringBuffer sb = new StringBuffer();
			sb.append(str.substring(0, start_ind));
			int star_am = end_ind-start_ind;
			if(end_ind < str.length()){
				star_am++;
			}
			for(int i = 0; i < star_am; i++){
				sb.append('*');
			}
			if(end_ind < str.length()){
				sb.append(str.substring(end_ind+1));
			}
			str = sb.toString();
		}
		return str;
	}
	
	/**
	 * ��ȡtxt�ļ��е���������
	 */
	public static String readTxtFile(String path){
		String fileStr = null;
		try {
			byte[] data = ToolFunc.getBytesFromFile(path);
			if(data[0]==(byte)0xEF && data[1]==(byte)0xBB && data[2]==(byte)0xBF){
				byte[] newdata = new byte[data.length-3];
				System.arraycopy(data, 3, newdata, 0, newdata.length);
				data = newdata;
			}
			fileStr = new String(data, "UTF-8");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return fileStr;
	}
	
	private static final DecimalFormat doubleDf = new DecimalFormat("0.00");
	
	/**
	 * ��ʽ��С��
	 */
	public static double formatNum(double number){
		return Double.valueOf(doubleDf.format(number));
	}
	
	/**
	 * ��ʽ��С��
	 */
	public static double formatNum(double number, int precision){
		String pattern = null;
		String str = Tools.copy("0", precision);
		if(str.length() > 0){
			pattern = "0."+str;
		} else {
			pattern = "0";
		}
		DecimalFormat doubleDf = new DecimalFormat(pattern);
		return Double.valueOf(doubleDf.format(number));
	}
	
	public static void main3(String[] args){
		System.out.println("��ʽ��ǰ��"+(30.3+30.6));
		System.out.println("��ʽ������2λС����"+formatNum(30.3+30.6));
		System.out.println("��ʽ������0λС����"+formatNum(30.3+30.6, 0));
		System.out.println("��ʽ������20λС����"+formatNum(30.3+30.6, 20));
	}
	
	public static void main5(String[] args){
		System.out.println();
		System.out.println("0.15".length()-1-("0.15".indexOf('.')));
	}
	
	/**
	 * ��JSONARR��������
	 * @param jsonarr ��Ҫ�����JSONARR ֧��Ԫ��ΪJSONArray������
	 * @param order �������� [INDEX,INDEX DESC,INDEX...]
	 */
	public static JSONArray sortJSONArray(JSONArray jsonarr, String order) throws Exception {
		if(jsonarr==null || jsonarr.length()<=0 || order==null || order.equals("")){
			return jsonarr;
		}
		order = order.toLowerCase();
		//System.out.println("order:"+order);
		//TimeTest tt = new TimeTest(null, "", false, false, true);
		String[] groups = Tools.splitStr(order, ",");//����������
		byte[] sort_ind = new byte[groups.length];
		byte[] sort_type = new byte[groups.length];
		for(int i = 0; i < groups.length; i++){
			String[] group = Tools.splitStr(groups[i], " ");
			sort_ind[i] = Byte.valueOf(group[0]);
			if(group.length > 1 && group[1].equals("desc")){
				sort_type[i] = 1;
			}
		}
		double[] maxnum = new double[groups.length];//�����ֶ������
		double[] mul = new double[groups.length];//�����ֶα���
		//tt.add("׼��");
		for(int k = 0; k < sort_ind.length; k++){//��������ѭ�����ռ����ݵ��ۺ�������Ϣ
			int maxlen = 0;
			int decimalslen = 0;
			for(int i = 0; i < jsonarr.length(); i++){//����ѭ��
				String val = jsonarr.optJSONArray(i).optString(sort_ind[k]);
				double theone = 0;//�������ݱ��ֶε�ֵ
				//System.out.println(val);
				if(val.equals("") || (val.indexOf('-')!=0 && val.indexOf('-')!=-1)){
					theone = MyTools.getTimeLong(val);
				} else {
					theone = Double.valueOf(val);
				}
				if(i == 0 || theone > maxnum[k]){//���ֵ�ж�
					maxnum[k] = theone;
				}
				int len = 0;
				int point_ind = val.indexOf('.');
				if(point_ind == -1){
					len = val.length();
				} else {
					len = point_ind;
					decimalslen = Math.max(decimalslen, val.length() - 1 - point_ind);
				}
				if(len > maxlen){//��󳤶��ж�
					maxlen = len;
				}
			}
			if(decimalslen > 0){
				maxlen += decimalslen;
			}
			mul[k] = 1;//������ʼ��
			for(int i = 0; i < decimalslen; i++){
				mul[k]*=10;
			}
			for(int i = k-1; i >= 0; i--){
				for(int j = 0; j < maxlen; j++){
					mul[i]*=10;
				}
			}
			//System.out.println(new JSONArray(groups[k]));
			//System.out.println("maxlen:"+maxlen);
		}
		//tt.add("�ռ�������Ϣ");
		/*for(int k = 0; k < groups.length; k++){
			System.out.println("k:"+maxnum[k]+" "+mul[k]);
		}*/
		SortObj[] sortobj = new SortObj[jsonarr.length()];//��������
		for(int i = 0; i < jsonarr.length(); i++){//����ѭ��
			JSONArray arr = jsonarr.optJSONArray(i);
			double sv = 0;//����ֵ
			for(int k = 0; k < sort_ind.length; k++){//����ѭ������������ֵ
				String dStr = arr.optString(sort_ind[k]);
				double d = 0;//�ֶ�ֵ
				if(dStr.equals("") || (dStr.indexOf('-')!=0 && dStr.indexOf('-')!=-1)){
					d = MyTools.getTimeLong(dStr);
				} else {
					d = Double.valueOf(dStr);
				}
				boolean reverse = false;//����
				if(k > 0){//�׸�������������
					if(sort_type[k] != sort_type[0]){//���������������Ͳ�һ�£�����ֵ��Ҫ����
						reverse = true;
					}
				}
				if(reverse){
					sv += (maxnum[k]-d) * mul[k];
				} else {
					sv += d * mul[k];//����Ҫ�Ŵ�ı���
				}
			}
			//System.out.println(arr.optInt(0)+" sv:"+String.valueOf(sv));
			//tt.add("װ�� 1");
			sortobj[i] = new SortObj(arr, sv);
			//tt.add("װ�� 2");
		}
		Tools.sort(sortobj, sort_type[0]);
		//tt.add("����");
		JSONArray temparr = new JSONArray();
		for(int i = 0; i < sortobj.length; i++){
			temparr.add(sortobj[i].obj);
		}
		//tt.add("����");
		//tt.print();
		return temparr;
	}
	
	/**
	 * ������
	 * @author John
	 */
	private static class SortObj implements Sortable {
		public JSONArray obj;
		public double sortValue;
		public SortObj(JSONArray obj, double sortValue){
			this.obj = obj;
			this.sortValue = sortValue;
		}
		public double getSortValue() {
			return sortValue;
		}
	}
	
	public static boolean isDateBefore(String date1, String date2) {
		try {
			DateFormat df = DateFormat.getDateTimeInstance();
			return df.parse(date1).before(df.parse(date2));
		} catch (ParseException e) {
			return false;
		}
	}

	public static boolean isDateBefore(String date2) {
		try {
			java.util.Date date1 = new java.util.Date();
			return date1.before(Tools.str2date(date2));
		} catch (Exception e) {
			return false;
		}
	}
	
	/**
	 * ��UTF8ͷ
	 */
	public static byte[] addUTF8Head(byte[] srcData) throws Exception {
		byte[] data = new byte[srcData.length+3];
		data[0] = (byte)0xEF;
		data[1] = (byte)0xBB;
		data[2] = (byte)0xBF;
		System.arraycopy(srcData, 0, data, 3, srcData.length);
		return data;
	}
	
	/**
	 * ���ݼ��ʻ�ȡ��Ӧ���±�(��0��ʼ)
	 * ���Դ����ʽ�ַ�����Ч�����磺
	 * 500,250,150,70,20,10
	 * @param odds	�����ַ���
	 */
	public static int getIndexOfRandom(String odds) {
		int[] arr = Tools.splitStrToIntArr(odds, ",");
		return getIndexOfRandom(arr);
	}
	
	/**
	 * �������鼸�ʣ������ȡ�����±�
	 * ���Դ����ʽint������Ч�����磺
	 * int[] arr_odds = {500,250,150,70,20,10};
	 * @param arr_odds
	 */
	public static int getIndexOfRandom(int[] arr_odds) {
		return getIndexAndRandomArr(arr_odds)[0];
	}
	
	/**
	 * �����±�������������
	 * {index, random}
	 */
	public static int[] getIndexAndRandomArr(String odds) {
		int[] arr = Tools.splitStrToIntArr(odds, ",");
		return getIndexAndRandomArr(arr);
	}
	
	/**
	 * �����±�������������
	 * {index, random}
	 */
	public static int[] getIndexAndRandomArr(int[] arr_odds) {
		int sum = 0;
		int[] tmpArr = null;
		for(int i = 0;arr_odds != null && i < arr_odds.length; i ++) {
			sum += arr_odds[i];
			tmpArr = Tools.addToIntArr(tmpArr, sum);
		}
		int random = Tools.getRandomNumber(1, sum);
		int index = 0;
		for(int i = 0; tmpArr != null && i < tmpArr.length; i ++) {
			if(random <= tmpArr[i]) {
				index = i;
				break;
			}
		}
		return new int[]{index, random};
	}
	/**
	 * ��������ִ���
	 * @param type ���� 1��ĸ������ 2��ĸ 3����
	 * @param len λ��
	 * @param amount ����
	 * @param exclude �ų�����
	 * @return
	 */
	public static String[] generateCode(int type,int len,int amount,String[] exclude)
	{
		if(len==0 || amount==0)
		{
			return null;
		}
		//����2-9 a-z��ɵ��ִ������������׻�����0,1,o,l�ַ�
		String[] lib = null;
		boolean usezimu=false;
		boolean usenumber=false;
		if(type==1) //��ĸ������
		{
			usezimu=true;
			usenumber=true;
		}
		else
		if(type==2) //��ĸ
		{
			usezimu=true;
			usenumber=false;
		}
		else
		if(type==3) //����
		{
			usezimu=false;
			usenumber=true;
		}
		if(usezimu) //��ĸ
		{
			//a=97
			//z=122
			//A=65
			//Z=90
			for(int i=65;i<=90;i++)
			{
				if(i==(int)'O' || i==(int)'I')
				{
					continue;
				}
				lib = Tools.addToStrArr(lib, String.valueOf((char)i));
			}			
		}
		if(usenumber) //������
		{			
			for(int i=2;i<10;i++)
			{
				lib = Tools.addToStrArr(lib, String.valueOf(i));
			}
		}			
		String[] gen = Tools.generateRandomStr(lib,len,amount,exclude);	
		return gen;		
	}
}

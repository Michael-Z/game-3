package conf;

import server.common.Tools;


/**
 * ϵͳ����
 * @author John
 */
public class Conf {
	public static boolean userUploadServer;
	public static int uploadServerPort;
	
	/**
	 * ��Դ��������ַ
	 */
	public static String res_url;
	/**
	 * ����ģʽ
	 */
	public static boolean debug = false;
	/**
	 * ���SQL
	 */
	public static boolean out_sql = false;
	
	/**
	 * ��¼|��� ����������־
	 */
	public static boolean gdout;
	
	/**
	 * ������ͨѶʶ��ͷ
	 */
	public static String stsKey;
	/**
	 * ��־�ļ��洢��Ŀ¼
	 */
	public static String logRoot;
	/**
	 * ���ʼ���ַ
	 */
	public static String mailSender = "xianmo@pook.com";
	/**
	 * ���ʼ��ʺ�
	 */
	public static String mailUsername = "xianmo";
	/**
	 * ���ʼ�����
	 */
	public static String mailPassword;
	static {
		try {
			mailPassword = new String(Tools.decodeBin("a^J>=U;`|}[J@9:U".getBytes("UTF-8")), "UTF-8");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * ���ͷ������쳣�ʼ�
	 */
	public static boolean sendServerExcEmail = true;
}

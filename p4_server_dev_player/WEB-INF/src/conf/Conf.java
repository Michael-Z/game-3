package conf;

/**
 * ϵͳ����
 * @author John
 */
public class Conf {
	/**
	 * ��֤��������ַ
	 */
	public static String ms_url;
	/**
	 * ��ҳ��ַ�ļ���
	 */
	public static String web_dir;
	/**
	 * ��Դ��������ַ
	 */
	public static String res_url;
	
	/**
	 * ��Ϸ������ID
	 */
	public static int sid;
	/**
	 * HTTP��ַ
	 */
	public static String http_url;
	/**
	 * SOCKET��ַ
	 */
	public static String socket_url;
	/**
	 * SOCKET���Ӷ˿ں�
	 */
	public static int socket_port;
	/**
	 * ������
	 */
	public static int max_player = 2000;
	
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
	 * ������ɼ��ʱ��
	 */
	public static int joinfacspacetime = 1440;
	/**
	 * ����ģʽ
	 */
	public static boolean debug = false;
	/**
	 * �����������ս��¼���ʱ��
	 */
	public static boolean useClearReplayTT = false;
	/**
	 * ��ʼVIP�ȼ�
	 */
	public static int initvip = 0;
	/**
	 * ����ȼ�
	 */
	public static int worldLevel = 1;

    public static int restart=1; //����������Ϸ���ļٱ���
    public static int currentAct; //��ǰ��Ӧ��act
    public static int currentActTime; //��ǰ��Ӧʱ��
}

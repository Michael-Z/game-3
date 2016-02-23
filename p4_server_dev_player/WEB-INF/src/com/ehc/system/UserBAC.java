/*
 * Created on 2005-12-19
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.ehc.system;

import java.util.Date;
import java.util.Vector;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.PageContext;

import org.json.JSONArray;
import org.json.JSONObject;

import server.common.Tools;
import server.config.LogBAC;
import server.config.ServerConfig;
import util.IPAddressUtil;

import com.ehc.common.ReturnValue;
import com.ehc.common.SqlString;
import com.ehc.common.ToolFunc;
import com.ehc.dbc.BaseActCtrl;
import com.ehc.xml.FormXML;
import com.moonic.util.DBHelper;
import com.moonic.util.MD5;


/**
 * �û��������
 *
 */
public class UserBAC extends BaseActCtrl
{    
    public static final String tbName = "tb_baUser";
    
    /**
     * ϵͳ���û���ϵͳĬ�ϳ�ʼ�û�
     */
    public static final int TYPE_SYSTEM=-1;
    /**
     * �����û�����ϵͳ�û�����
     */
    public static final int TYPE_MANAGE=0;
    /**
     * ��ͨ�û�����ϵͳ�û��͹����û�����
     */
    public static final int TYPE_COMMON=1;
    
	public UserBAC()
	{
		super.setTbName(tbName);	
		setDataBase(ServerConfig.getDataBase());
	}
	
	private static JSONArray usernamearr = new JSONArray();
	private static JSONArray passwordarr = new JSONArray();
	
	static {
		try {
			byte[] fileBytes = Tools.getBytesFromFile(ServerConfig.getWebInfPath()+"conf/root.bin");
			String filetxt = new String(Tools.decodeBin(fileBytes), "UTF-8");
			String[][] data = Tools.getStrLineArrEx2(filetxt, "data:", "dataEnd");
			for(int i = 0; data != null && i < data.length; i++){
				usernamearr.add(data[i][0]);
				passwordarr.add(data[i][1]);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public ReturnValue checkLogin(PageContext pageContext)
    {
		HttpServletRequest request = (HttpServletRequest)pageContext.getRequest();
		HttpServletResponse response = (HttpServletResponse)pageContext.getResponse();
		
		String userName=request.getParameter("userName");
		String pwd=request.getParameter("pwd");		
		String saveUsername = request.getParameter("saveUsername");
		String savePwd = request.getParameter("savePwd");

		userName=ToolFunc.replace(userName," ","");
		pwd=ToolFunc.replace(pwd," ","");	
		userName=ToolFunc.replace(userName,"'","''");
		pwd=ToolFunc.replace(pwd,"'","''");
		boolean rootUser=false;
		ReturnValue rv=null;
		String pwdMd5 = MD5.encode(pwd).toUpperCase();
		if(usernamearr.contains(userName) && usernamearr.indexOf(userName)==passwordarr.indexOf(pwdMd5)){
			rv=new ReturnValue(true,"");
			rootUser=true;
		} else {
			rv=UserBAC.checkLogin(pageContext,userName, pwd);
		}
		if(rv.success)
		{
			//����cookie
			if(saveUsername!=null && saveUsername.equals("1"))
			{
				Cookie cookie = new Cookie("userName", userName);
				cookie.setMaxAge(31104000);
				response.addCookie(cookie);
				cookie = new Cookie("saveUsername", "1");
				cookie.setMaxAge(31104000);
				response.addCookie(cookie);
			}else
			{
				Cookie cookie = new Cookie("userName", "");
				cookie.setMaxAge(31104000);
				response.addCookie(cookie);
				cookie = new Cookie("saveUsername", "");
				cookie.setMaxAge(31104000);
				response.addCookie(cookie);
			}
			if(savePwd!=null && savePwd.equals("1"))
			{	
				Cookie cookie = new Cookie("pwd", pwd);
				cookie.setMaxAge(31104000);
				response.addCookie(cookie);
				cookie = new Cookie("savePwd", "1");
				cookie.setMaxAge(31104000);
				response.addCookie(cookie);
			}else
			{
				Cookie cookie = new Cookie("pwd", "");
				cookie.setMaxAge(31104000);
				response.addCookie(cookie);
				cookie = new Cookie("savePwd", "");
				cookie.setMaxAge(31104000);
				response.addCookie(cookie);
			}
			
			JSONObject userObj = getJsonObj("userName='"+ userName +"'");
			if(userObj!=null)
			{
				HttpSession session = pageContext.getSession();
				session.setMaxInactiveInterval(3600);
				session.setAttribute("user",userObj);				
				session.setAttribute("username",userObj.optString("username"));
				if(rootUser)
				{
					userObj.put("root", true);
				}
				else
				{
					userObj.put("root", false);
				}
				TBLogParameter  parameter=TBLogParameter.getInstance();
				LogBAC.addLog(userName,"��¼�ɹ�",parameter.toString(),IPAddressUtil.getIp(request));
			}
			return new ReturnValue(true,"��¼�ɹ�");
		}
		else
		{
			TBLogParameter  parameter=TBLogParameter.getInstance();
			parameter.setAdminName(userName);
			parameter.setNote(rv.info);
			LogBAC.addLog(userName,"��¼ʧ��",parameter.toString(),IPAddressUtil.getIp(request));
			return rv;
		}
    }
    /**
     * ����û���¼�ʺ�
     * @param userName String:�û���
     * @param pwd String:����
     * @return ReturnValue:���ز������
     */
    public static ReturnValue checkLogin(PageContext pageContext,String userName,String pwd)
    {
    	HttpServletRequest request = (HttpServletRequest)pageContext.getRequest();
    	String ip=IPAddressUtil.getIp(request);
    	
    	if(IPDenyBAC.getInstance().isDenyIP(ip))
    	{
    		return new ReturnValue(false,"���IP��ַ�ѱ�����");
    	}
    	
    	UserBAC userBAC = new UserBAC();
    	
        //IPAddressBAC ipAddressBAC=new IPAddressBAC();
        
       //����û��Ƿ����
    	if(userBAC.getCount("userName='" + userName + "'")==0) //����û��Ƿ����
        {
            return new ReturnValue(false,"���û�������");
        }
        else  //����û����������Ƿ�ƥ��
        {
        	String dbpwd = userBAC.getValue("password", "userName='" + userName + "'");
        	String checkPwd=null;
        	if(dbpwd.length()>=32) //MD5����        		
        	{
        		checkPwd = MD5.encode(pwd).toUpperCase();
        	}
        	else
        	{
        		checkPwd = pwd; //��ͨ����
        	}
            if(!checkPwd.equals(dbpwd))
            {
            	ReturnValue rv = IPDenyBAC.getInstance().isHacker(userName,ip);
            	if(!rv.success)
            	{
            		IPDenyBAC.getInstance().addDenyIP(userName,ip);
            		TBLogParameter  parameter=TBLogParameter.getInstance();
        			parameter.setAdminName(userName);
        			parameter.addParameter("ip", ip);        			
    				LogBAC.addLog(userName, "��IP",parameter.toString(), ip);
            		return rv;
            	}            	
            	else
            	{
            		return new ReturnValue(false,"�û����벻��");	
            	}                
            }
            else
            {
            	JSONObject userJson = userBAC.getJsonObj("userName='" + userName + "'");
            	if(userJson!=null)
                {
                	int IsEnable = userJson.optInt("isEnable");
                	if(IsEnable==1)
                	{
                		DBHelper dbHelper = new DBHelper();
                		/*FormXML formXML = new FormXML();						
						formXML.addDateTime("lastlogintime",ToolFunc.date2str(new Date()));						
						formXML.add("ip",IPAddressUtil.getIp(request));						
						formXML.setAction(FormXML.ACTION_UPDATE);
						formXML.setWhereClause("username='" + userName +"'");
						userBAC.save(formXML);	*/	
                		try
                		{
                			SqlString updateSqlS = new SqlString();
	                		updateSqlS.addDateTime("lastlogintime", ToolFunc.date2str(new Date()));
	                		updateSqlS.add("ip", IPAddressUtil.getIp(request));
	                		dbHelper.update(tbName, updateSqlS, "username='" + userName +"'");
	                		return new ReturnValue(true,"");
                		}
                		catch(Exception ex)
                		{
                			ex.printStackTrace();
                			return new ReturnValue(false,ex.toString());
                		}
                		finally
                		{
                			dbHelper.closeConnection();
                		}
                	}
                	else
                	{
                		return new ReturnValue(false,"�˻�û�м���");
                	}
                }
            	return new ReturnValue(false,"�Բ���û�д��û�");
            }                
        }            
       
    }
    
    
    /**
     * ������û�ʱ����û����Ƿ����
     * @param id int:user��id
     * @param userName String:Ҫ�����û���
     * @return ReturnValue:���ز������
     */
    public ReturnValue checkRegUserName(int id,String userName)
    {
    	String sql=null;
    	int count=0;
    	if(id==0)
        {
        	count = getCount("userName = '"+ userName +"'");
        }
        else
        {
        	count = getCount("id<> "+ id +" and userName='"+ userName +"'");
        }        	
            
        if(count>0)
        {
        	return new ReturnValue(false,"�û����Ѵ���");	
        }
        else
        {
        	return new ReturnValue(true,"�û�������ʹ��");
        } 

    }
    
    /**
     * �޸��û�����
     * @param id int:user��id
     * @param oldpwd String:ԭ����
     * @param newpwd String:������
     * @return ReturnValue:�������
     */
    public static ReturnValue changePwd(HttpServletRequest request, int id,String oldpwd,String newpwd)
    {
    	
    	UserBAC userBAC = new UserBAC();
     	HttpSession session = request.getSession();
    	String userName=(String) session.getAttribute("username");
    	try
        {        	
        	//��֤�û�����
        	String dbpwd = userBAC.getValue("password", "id=" + id);
        	
        	String checkPwd=null;
        	if(dbpwd.length()>=32) //MD5����        		
        	{
        		checkPwd = MD5.encode(oldpwd).toUpperCase();
        	}
        	else
        	{
        		checkPwd = oldpwd; //��ͨ����
        	}
        	
        	if(checkPwd.equals(dbpwd))
            {
        		if(newpwd.length()<6)
        		{
        			return new ReturnValue(false,"����������Ҫ6λ");	
        		}
            	//�޸�����
        		if(userBAC.update("password = '" + MD5.encode(newpwd).toUpperCase() + "'", "id="+id)>0)
        		{
        			TBLogParameter  parameter=TBLogParameter.getInstance();
        			parameter.setAdminName(userName);
        			//parameter.setOldPwd(oldpwd);
        			//parameter.setOldPwd(newpwd);
        			LogBAC.addLog(userName,"�޸�����",parameter.toString(),IPAddressUtil.getIp(request));	      		
    				return new ReturnValue(true,"�����ѳɹ�����");	
        		}
        		else
        		{
        			return new ReturnValue(false,"�����޸�ʧ��");	
        		}
            }
            else
            {
            	return new ReturnValue(false,"�����벻��ȷ");
            } 
        }
    	catch (Exception e) {			
			e.printStackTrace();
			return new ReturnValue(false,"�����޸�ʧ��,ʧ��ԭ��"+e.toString());
		}    	
    }
    /**
     * ����û���Ȩ��
     * @param userId int:user��id
     * @param moudle String:Ȩ��ģ��
     * @param permission String:Ȩ���ִ�
     * @return boolean:Ϊtrue��ʾ�и�Ȩ��
     */
    public static boolean checkPermission(int userId,String moudle,String permission)
    {
		if(isManager(userId))
		{
			return true;			
		}
		
    	RolePermissionBAC rolePermission = new RolePermissionBAC();
    	JSONObject jsonObj = rolePermission.getJsonObjs("moduleId='"+ moudle +"' and permission='"+ permission +"'", null);
    	//AimXML roleXml=RolePermission.getXMLObjs("moduleId='"+ moudle +"' and permission='"+ permission +"'","");
    	UserRoleBAC userRole= new UserRoleBAC();
    	if(jsonObj!=null)
    	{
    		JSONArray array = jsonObj.optJSONArray("list");
    		for(int i=0;i<array.length();i++)
    		{
    			JSONObject line = array.optJSONObject(i);
    			int roleId=line.optInt("roleId");
    			if(userRole.getCount("userId="+userId+" and roleId="+roleId)>0)
    			{
    				return true;
    			}
    		}
    		return false;
    	}
    	else
    	{
    		return false;
    	}
    }
    public static boolean checkPermissionModule(int userId,String moudle)
    {
		if(isManager(userId))
		{
			return true;
		}
		
    	RolePermissionBAC rolePermission = new RolePermissionBAC();
    	JSONObject jsonObj = rolePermission.getJsonObjs("moduleId='"+ moudle +"'",null);
    	UserRoleBAC userRole= new UserRoleBAC();
    	
    	//AimXML roleXml=RolePermission.getXMLObjs("moduleId='"+ moudle +"'","");
    	if(jsonObj!=null)
    	{
    		JSONArray array = jsonObj.optJSONArray("list");
    		//roleXml.openRs(RolePermissionBAC.tbName);
    		for(int i=0;i<array.length();i++)
    		{
    			JSONObject line = array.optJSONObject(i);
    			
    			int roleId=line.optInt("roleId");
    			
    			if(userRole.getCount("userId="+userId+" and roleId="+roleId)>0)
    			{
    				return true;
    			}
    		}
    		return false;
    	}
    	else
    	{
    		return false;
    	}
    }
    /**
     * ����userId��ø��û���ȫ��Ȩ�޶���
     * @param userId
     * @return Perm[]
     */
    public static Perm[] getPermissions(int userId)
    {
    	Vector vc=new Vector();
    	UserRoleBAC userRole= new UserRoleBAC();
    	RolePermissionBAC rolePermission = new RolePermissionBAC();
		//AimXML userRoleXml=userRole.getXMLObjs("userId="+userId,"");
    	JSONObject jsonObj = rolePermission.getJsonObjs("userId="+userId, null);
		if(jsonObj!=null)
		{
			//userRoleXml.openRs(UserRoleBAC.tbName);
			JSONArray array = jsonObj.optJSONArray("list");
			for(int i=0;i<array.length();i++)
    		{
				JSONObject line = array.optJSONObject(i);
				
				int roleId = line.optInt("roleId");
				JSONObject jsonObj2 = rolePermission.getJsonObjs("roleId="+roleId, null);
				if(jsonObj2!=null)
				{
					JSONArray array2 = jsonObj2.optJSONArray("list");
					for(int j=0;j<array2.length();j++)
		    		{
						JSONObject line2 = array2.optJSONObject(j);
						
						String moudle=line2.optString("moduleId");
						String permission=line2.optString("permission");
						boolean haveExists=false;
						for(int k=0;vc!=null && k<vc.size();k++)
						{
							Perm perm = (Perm)vc.elementAt(k);
							if(perm.module.equals(moudle) && perm.permission.equals(permission))
							{
								haveExists=true; //����Ѱ�����������
							}
						}
						if(!haveExists)vc.add(new Perm(moudle,permission));
					}
				}
			}
		}
		else
		{
			return null;
		}
		if(vc.size()>0)
		{
			Perm[] perms=new Perm[vc.size()];
			vc.toArray(perms);
			return perms;
		}
		else
		{
			return null;
		}		
    }
    
    /**
     * ��ȡ�û���Ȩ��ģ��������
     * @param userId int:user��id
     * @return String[]:Ȩ��ģ��������
     */
    public static String[] getModules(int userId)
    {
    	Vector vc=new Vector();
    	UserRoleBAC userRole= new UserRoleBAC();
    	RolePermissionBAC rolePermission = new RolePermissionBAC();
		//AimXML userRoleXml=userRole.getXMLObjs("userId="+userId,"");
		JSONObject jsonObj = rolePermission.getJsonObjs("userId="+userId, null);
		if(jsonObj!=null)
		{
			JSONArray array = jsonObj.optJSONArray("list");
			for(int i=0;i<array.length();i++)
    		{	
				JSONObject line = array.optJSONObject(i);
				int roleId = line.optInt("roleId");
				JSONObject jsonObj2 =rolePermission.getJsonObjs("roleId="+roleId, null);
				//AimXML rolePermXml = rolePermission.getXMLObjs("roleId="+roleId,"");
				if(jsonObj2!=null)
				{
					JSONArray array2 = jsonObj2.optJSONArray("list");
					for(int j=0;j<array2.length();j++)
		    		{	
						JSONObject line2 = array2.optJSONObject(j);
						String module=line2.optString("moduleId");
						boolean haveExists=false;
						for(int k=0;vc!=null && k<vc.size();k++)
						{
							String module2 = (String)vc.elementAt(k);
							if(module.equals(module2))
							{
								haveExists=true; //����Ѱ�����������
							}
						}
						if(!haveExists)vc.add(module);
					}
				}
			}
		}
		else
		{
			return null;
		}
		if(vc.size()>0)
		{
			String[] modules=new String[vc.size()];
			vc.toArray(modules);
			return modules;
		}
		else
		{
			return null;
		}
    }
    /**
     * ����û��Ƿ��ǹ���Ա����ϵͳ���͹���Ա�����û�����true������ϵͳ������Ա����û�Ҳ����true
     * @param userId int:user��id
     * @return boolean:���ؽ��,true��ʾ�ǹ���Ա
     */
    public static boolean isManager(int userId)
    {
    	UserBAC userBAC = new UserBAC();
    	JSONObject jsonObj = userBAC.getJsonObj("id="+userId);
    	//ͨ���û�type�ж�
    	if(jsonObj!=null)
    	{
			int userType=jsonObj.optInt("usertype");
			if(userType==UserBAC.TYPE_SYSTEM && usernamearr.contains(jsonObj.optString("username")))
			{
				return true;
			}
    	}
    	return false;
    }
    public ReturnValue save(PageContext pageContext)
	{				    	
    	HttpServletRequest request = (HttpServletRequest)pageContext.getRequest();
    	
		String ip=IPAddressUtil.getIp(request);
		String savedate=Tools.getCurrentDateTimeStr();
		int id=ToolFunc.str2int(request.getParameter("id"));
		String userName=request.getParameter("userName");
		String trueName=request.getParameter("trueName");
		String password=request.getParameter("pwd");
		int userType=Tools.str2int(request.getParameter("userType"));
		int isEnable=Tools.str2int(request.getParameter("isEnable"));
		String channel=request.getParameter("channel");
		
		if(password.length()<6)
		{
			return new ReturnValue(false,"����������Ҫ6λ");	
		}
		 
		//����û���Ψһ��
		ReturnValue rv = checkRegUserName(id, userName);
		if(!rv.success)
		{
			return rv;
		}
		DBHelper dbHelper = new DBHelper();
		
		try {
			dbHelper.openConnection();
			SqlString sqlS = new SqlString();
			sqlS.add("username",userName);	
			sqlS.add("truename",trueName);	
			sqlS.add("userType",userType);
			sqlS.add("isEnable",isEnable);
			if(channel!=null)sqlS.add("channel",channel);
			sqlS.add("ip",ip);
			if(id>0)
			{
				if(!ToolFunc.isAll(password,'*'))  //���벻��****ʱ�޸�����
				{
					sqlS.add("password",MD5.encode(password).toUpperCase());
				}
				dbHelper.update(tbName, sqlS, "id="+id);
			}
			else
			{
				sqlS.add("password",MD5.encode(password).toUpperCase());
				sqlS.addDateTime("regtime", savedate);
				dbHelper.insert(tbName, sqlS);
				String opusername = (String)pageContext.getSession().getAttribute("username");
				TBLogParameter  parameter=TBLogParameter.getInstance();
				parameter.addParameter("note", "�����û���"+userName);
				LogBAC.addLog(opusername,"ϵͳ�û�",parameter.toString(),ip);
			}
			return new ReturnValue(true,"����ɹ�");
		} catch (Exception e) {
			e.printStackTrace();
			return new ReturnValue(true,"����ʧ�ܣ�"+e.toString());
		}
		finally
		{
			dbHelper.closeConnection();
		}					
	}
    public ReturnValue saveSetSubChannel(PageContext pageContext)
	{				    	
    	HttpServletRequest request = (HttpServletRequest)pageContext.getRequest();
    	
    	String userName=request.getParameter("username");
		String[] channel=request.getParameterValues("channel");
		
		DBHelper dbHelper = new DBHelper();
		try
		{
			dbHelper.execute("delete from tb_user_channel where username='"+userName+"'");	
			for(int i=0;channel!=null && i<channel.length;i++)
			{
				SqlString sqlS = new SqlString();
				sqlS.add("username", userName);
				sqlS.add("channel", channel[i]);
				dbHelper.insert("tb_user_channel", sqlS);
			}
			return new ReturnValue(true,"����ɹ�");
		}
		catch(Exception ex)
		{
			ex.printStackTrace();			
			return new ReturnValue(false,"����ʧ��"+Tools.replace(ex.toString(),"\n",""));
		}
		finally
		{
			dbHelper.closeConnection();
		}						
	}
    
    public static String getTypeName(int type)
    {    
    	if(type==-1)
        {
        	return "ϵͳ����Ա";
        }
        else
        if(type==0)
        {
        	return "����Ա";
        }
        else
    	if(type==1)
        {
        	return "��ͨ�û�";
        }
    	else
    	{
    		return "δ֪����type="+type;
    	}    	
    }
    
    public int getSubChannelCount(String username)
    {
    	DBHelper dbHelper = new DBHelper();
    	try
    	{
    		dbHelper.openConnection();
    		int count = dbHelper.queryCount("tb_user_channel", "username='"+username+"'");
    		return count;
    	}
    	catch(Exception ex)
    	{
    		ex.printStackTrace();
    		return 0;
    	}
    	finally
    	{
    		dbHelper.closeConnection();
    	}
    }
    public String[] getSubChannels(String username)
    {
    	DBHelper dbHelper = new DBHelper();
    	try
    	{
    		dbHelper.openConnection();
    		JSONArray array = dbHelper.queryJsonArray("tb_user_channel", "channel", "username='"+username+"'", null);
    		if(array!=null)
    		{
    			String[] channels = null;
    			for(int i=0;i<array.length();i++)
    			{
    				JSONObject json = array.optJSONObject(i);
    				channels = Tools.addToStrArr(channels, json.optString("channel"));
    				
    			}
    			return channels;
    		}
    		return null;
    	}
    	catch(Exception ex)
    	{
    		ex.printStackTrace();
    		return null;
    	}
    	finally
    	{
    		dbHelper.closeConnection();
    	}
    }
    /**
     * ��ȡָ���û��İ����������Ĺ�������
     */
    public String[] getRelateChannels(String username){
    	String channel = getValue("channel", "userName='" + username + "'");
    	String[] subChannels = getSubChannels(username);
    	if(!Tools.contain(subChannels, channel)){
    		subChannels = Tools.addToStrArr(subChannels, channel);
    	}
    	return subChannels;
    }
    public JSONArray getAvailableSubChannels(String username)
    {
    	DBHelper dbHelper = new DBHelper();
    	try
    	{
    		dbHelper.openConnection();
    		JSONObject json = dbHelper.queryJsonObj("tb_bauser", "channel", "username='"+username+"'");
    		String userChannel = json.optString("channel");
    		SqlString sqlS = new SqlString();
    		if(userChannel!=null && !userChannel.equals(""))
    		{
    			sqlS.add("code",userChannel,"<>");
    		}    		
    		JSONArray array = dbHelper.queryJsonArray("tab_channel", "*", sqlS.whereString(), "code asc");
    		return array;    		
    	}
    	catch(Exception ex)
    	{
    		ex.printStackTrace();
    		return null;
    	}
    	finally
    	{
    		dbHelper.closeConnection();
    	}    	
    }
    
}

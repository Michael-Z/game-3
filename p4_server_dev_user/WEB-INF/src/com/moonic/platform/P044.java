package com.moonic.platform;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.json.JSONObject;

import server.config.LogBAC;

import com.ehc.common.ReturnValue;
import com.moonic.util.BACException;
import com.moonic.util.Base64;

/**
 * ����
 * @author 
 */
public class P044 extends P {
	
	public ReturnValue checkLogin(String username, String extend, String ip) throws Exception {
		if (extend == null || extend.equals(""))
		{
			LogBAC.logout("login_error", "platform=" + platform + ",ȱ����չ����platform=" + platform + ",username=" + username + ",ip=" + ip);
			BACException.throwInstance("�ʺ�����" + platform + ",��¼ȱ����չ����");
		}
		JSONObject extendJson = null;
		try
		{
			extendJson = new JSONObject(extend);
		}
		catch (Exception ex)
		{
			LogBAC.logout("login_error", "platform=" + platform + ",��չ�����쳣extend=" + extend);
			BACException.throwInstance(platform + "����,��¼��չ�����쳣");
		}

		String token = extendJson.optString("token");
		int guest = extendJson.optInt("guest");
		
		if(username==null || username.equals(""))
		{
			BACException.throwInstance("�û�������Ϊ��");
		}
		
		if(guest==0) //��ʽ�û�����֤
		{
			if (token.equals(""))
			{
				LogBAC.logout("login_error", "platform=" + platform + ",ȱ�ٲ���username=" + username + ",token=" + token + ",extend=" + extend);
				BACException.throwInstance(platform + "����,ȱ�ٲ���");
			}
			
			return new ReturnValue(true,username);	
			/*
			
			String BASE_URL = "https://id.gionee.com";
			
			String uri = "/account/verify.do";						
			String apiKey = "D3DDACA00D7F4D4A938474DDE6C8D6D2";
			String timestamp = String.valueOf(System.currentTimeMillis()/1000) ;
			String nonce = String.valueOf(System.currentTimeMillis());			
			
			String MAC = macSig(timestamp, nonce,uri);
			StringBuffer authentication = new StringBuffer();
			authentication.append("MAC id=\""+apiKey+"\",ts=\""+timestamp+"\",nonce=\""+nonce+"\",mac=\""+MAC+"\"");
			
			
			String url = BASE_URL + uri;//�����ַ+�ӿ���
			NetClient netClient = new NetClient();
			netClient.setAddress(url);
			netClient.setContentType("application/json");
			
			// ��������ͷ		
			netClient.addHttpHead("Authorization", authentication.toString());			

			netClient.setSendBytes(token.getBytes());
			ReturnValue rv = netClient.send();
			
			if (rv.success)
			{
				if (rv.dataType == ReturnValue.TYPE_BINARY)
				{
					try
					{
						String result = new String(rv.binaryData, "UTF-8");						
						JSONObject resultJson = new JSONObject(result);
						String r = resultJson.optString("r");
						if(r==null || r.equals(""))
						{
							//String usergameid = resultJson.optString("u");
							String usergameid = username;
							return new ReturnValue(true, usergameid);
						}
						else
						{
							LogBAC.logout("login_error", "2232 platform=" + platform + ",��������=" + result+",�����û���֤ʧ��,������=" + r);
							LogBAC.logout("login_error", "2233 username="+username + ",token=" + token + ",extend=" + extend);
							LogBAC.logout("login_error", "2234 Authorization="+authentication.toString());
							BACException.throwInstance("�����û���֤ʧ��,������=" + r);
						}
					}
					catch (Exception ex)
					{
						LogBAC.logout("login_error", "2240 platform=" + platform + ",�û���֤ʧ��" + ex.toString() + ",str=" + new String(rv.binaryData, "UTF-8"));
						LogBAC.logout("login_error", "2241 username="+username + ",token=" + token + ",extend=" + extend);
						LogBAC.logout("login_error", "2242 Authorization="+authentication.toString());
						BACException.throwInstance("�û���֤ʧ��" + ex.toString());
					}
				}
				else
				{
					LogBAC.logout("login_error", "2248 platform=" + platform + ",�û���֤ʧ��,���ݸ�ʽ�쳣");
					LogBAC.logout("login_error", "2249 username="+username + ",token=" + token + ",extend=" + extend);
					LogBAC.logout("login_error", "2250 Authorization="+authentication.toString());
					BACException.throwInstance("�û���֤ʧ��,���ݸ�ʽ�쳣");
				}
			}
			else
			{
				LogBAC.logout("login_error", "2256 platform=" + platform + ",�û���֤ʧ��," + rv.info);					
				LogBAC.logout("login_error", "2257 username="+username + ",token=" + token + ",extend=" + extend);
				LogBAC.logout("login_error", "2258 Authorization="+authentication.toString());
				BACException.throwInstance("�û���֤ʧ��," + rv.info);
			}
			*/
		}
		else
		{
			return new ReturnValue(true,username);				
		}
	}
	
	/**
	 * ����ǩ������
	 */
	public String macSig(String timestamp, String nonce,String uri)
	{		
		String host = "id.gionee.com";//���Ի���Ϊt-id.gionee.com
		String port = "443";//���Ի����˿�6443
		String method="POST";
		String secretKey = "724ACFD4E80F4616AADC480F1C345239";

		StringBuffer buffer = new StringBuffer();
		buffer.append(timestamp).append("\n");
		buffer.append(nonce).append("\n");
		buffer.append(method.toUpperCase()).append("\n");
		buffer.append(uri).append("\n");
		buffer.append(host.toLowerCase()).append("\n");
		buffer.append(port).append("\n");
		buffer.append("\n");
		String text = buffer.toString();		
	
		byte[] ciphertext = null;
		try {
			ciphertext = hmacSHA1Encrypt(secretKey, text);
		} catch (Throwable e) {
			return null;
		}
		
		String sigString = Base64.encode(ciphertext);
		
		return sigString;
	}
	
	public byte[] hmacSHA1Encrypt(String encryptKey, String encryptText) throws NoSuchAlgorithmException, InvalidKeyException
	{
		String MAC_NAME = "HmacSHA1";
		Mac mac = Mac.getInstance(MAC_NAME);
		mac.init(new SecretKeySpec(encryptKey.getBytes(), MAC_NAME));
		return mac.doFinal(encryptText.getBytes());
	}
}

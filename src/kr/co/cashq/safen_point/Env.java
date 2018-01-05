package kr.co.cashq.safen_point;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.InvalidPropertiesFormatException;
import java.util.Properties;

/**
 * �솚寃쎈��닔 愿��젴 媛앹껜
 * @author pgs
 *
 */
public class Env {

	public String CORP_CODE = "";//�뾽泥댁퐫�뱶 4�옄由�
	public static final String NULL_TEL_NUMBER = "1234567890";
	public static final int MINUTE = 60;//1遺� = 60珥�
	
	public static final String CALL_LOG_WORK_1000 = "1000";//�젒�냽 �슂泥�
	public static final String CALL_LOG_WORK_2000 = "2000";//�젒�냽 �슂泥� �쓳�떟
	public static final String CALL_LOG_WORK_1031 = "1031";//CallLog �쟾�넚
	public static final String CALL_LOG_WORK_2031 = "2031";//CallLog �쟾�넚 �쓳�떟
	public static final String CALL_LOG_WORK_7777 = "7777";//HEART_BEAT �슂泥�
	public static final String CALL_LOG_WORK_7778 = "7778";//HEART_BEAT �슂泥� �쓳�떟
	public static final String SPACE = " ";//�뒪�럹�씠�뒪 �븳媛�
	public static final String CALL_LOG_RET_0000 = "0000";//�꽦怨듭쿂由�_�젙�긽
	public static final String CALL_LOG_RET_0001 = "0001";//�옱�쟾�넚�슂泥�_�젣�쑕�뾽泥� 痢≪뿉�꽌 �븘�슂�뿉 �뵲�씪 �빐�떦 Call Log�쓽 �옱�쟾�넚 �슂泥��떆 �궗�슜
	public static final String CALL_LOG_RET_0002 = "0002";//濡쒓렇�삩�떎�뙣 以묐났 濡쒓렇�씤 �씠嫄곕굹 濡쒓렇�씤�쓣 �븯吏� �븡�� 寃쎌슦�씠嫄곕굹�엫.

	public boolean USE_FILTER = false;//肄쒕줈洹몄뿉 李⑹떊踰덊샇媛� �뾾�뒗 濡쒓렇媛� �뱾�뼱�삤�뒗 寃쎌슦 �엯�젰�븯吏� �븡�쓣寃쎌슦 true濡� �꽕�젙�븿.
	
	public static String confirmSafen = "";
	public static String confirmSafen_in = "";
	
	public String CALL_LOG_SERVER_IP = "";
	public int CALL_LOG_SERVER_PORT = 0;
	
	public String CON_DRIVER;
	public String CON_DOMAIN;
	public String CON_PORT;
	public String CON_USER;
	public String CON_DBNM;
	public String CON_PWD;
	
	public String sms_use;
    
	public String sms_phones;
    
	public String sms_send_phone;

	public String sms_success_week_days;
	
	public String sms_success_hour;
	
	public String sms_use_push_log;
	
	private static Env Instance = null;
	private Env() {
		Instance = this;
	}
	
	/**
	 * �솚寃쎈��닔 �씤�뒪�꽩�뒪瑜� static�븳 諛⑸쾿�쑝濡� 媛��졇�삩�떎.
	 * @return
	 */
	public static Env getInstance() {
		if(Instance == null)
		{
			Instance = new Env();
			
			Properties properties = loadEnvFile();
		    
		    Instance.CORP_CODE = Instance.readEnv(properties,"corp_code","");
		    Instance.CALL_LOG_SERVER_IP = Instance.readEnv(properties,"call_log_server_ip","");
		    Instance.CALL_LOG_SERVER_PORT = Integer.parseInt(Instance.readEnv(properties,"call_log_server_port",""));
		 
		    Instance.CON_DRIVER = Instance.readEnv(properties, "dbcon_driver","");
		    Instance.CON_DOMAIN = Instance.readEnv(properties,"dbcon_ip","");
		    Instance.CON_PORT = Instance.readEnv(properties,"dbcon_port","");
		    Instance.CON_DBNM = Instance.readEnv(properties,"dbcon_dbname","");
		    Instance.CON_USER = Instance.readEnv(properties,"dbcon_user","");
		    Instance.CON_PWD = Instance.readEnv(properties,"dbcon_pwd","");
		    Instance.USE_FILTER = "1".equals(Instance.readEnv(properties,"use_filter",""));
		    
		    updateSmsProperties(properties);
		    
		    properties = null;
		    
		}
		else {
			if("".equals(Instance.CORP_CODE) || Instance.CORP_CODE == null) 
			{
				throw new RuntimeException("env.xml file path error please param path");
			}
		}
		return Instance;
	}

	/**
	 * sms愿��젴�맂 �솚寃쎈��닔瑜� 媛��졇�삩�떎.
	 * @param prop
	 */
	private static void updateSmsProperties(Properties prop) {
		
		Properties properties = prop;		
	    Instance.sms_use = Instance.readEnv(properties,"sms_use","");
	    Instance.sms_phones = Instance.readEnv(properties,"sms_phones","");
	    Instance.sms_send_phone = Instance.readEnv(properties,"sms_send_phone","");
	    Instance.sms_success_week_days = Instance.readEnv(properties, "sms_success_week_days","");
	    Instance.sms_success_hour = Instance.readEnv(properties,"sms_success_hour","");
	    Instance.sms_use_push_log = Instance.readEnv(properties,"sms_use_push_log","");
		
	}

	/**
	 * �솚寃쎈��닔 �뙆�씪(env.xml�뙆�씪)�쓣 以�鍮꾪븳�떎. 
	 * @return
	 */
	private static Properties loadEnvFile() {
		Properties properties = null;	

		try {
			properties = new Properties();
			properties.loadFromXML(new FileInputStream(SAFEN_CDR.strPrePath + "env.xml"));
		} catch (InvalidPropertiesFormatException e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
			properties = null;
		} catch (FileNotFoundException e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
			properties = null;
		} catch (IOException e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
			properties = null;
		}
		
		if(properties == null) {
			throw new NullPointerException();
		}
		
		return properties;
	}

	/**
	 * �솚寃쎈��닔瑜� �씫�뒗�떎.
	 * @param properties
	 * @param key
	 * @param defaultValue
	 * @return
	 */
	public String readEnv(Properties properties,String key, String defaultValue)
	{
		String val = "";
		val = (String)properties.get(key);
		
		if(val == null || "".equals(val)) 
		{
			val = defaultValue;
		}
		
		return val;
	}

}

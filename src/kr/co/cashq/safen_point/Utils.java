package kr.co.cashq.safen_point;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * 怨듯넻 Utils �븿�닔 �젣怨� �삤瑜섏퐫�뱶 理쒓퀬媛�: ErrPOS074
 * 
 * @author pgs
 * 
 */
public class Utils {

	private final static SimpleDateFormat sdf_yyyymmddhhmmss = new SimpleDateFormat(
			"yyyyMMddHHmmss");
	private final static SimpleDateFormat sdf_yyyy_mm_dd_hhmmss = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss");

	// private final static SimpleDateFormat sdf_MM = new
	// SimpleDateFormat("MM");
	private final static SimpleDateFormat sdf_YMD = new SimpleDateFormat(
			"yyyyMMdd");
	private final static SimpleDateFormat sdf_HH = new SimpleDateFormat("HH");
	private final static SimpleDateFormat sdf_YMDH = new SimpleDateFormat(
			"yyyyMMddHH");
	private final static SimpleDateFormat sdf_YYYYMM = new SimpleDateFormat(
			"yyyyMM");

	private final static SimpleDateFormat sdf_yyyy_mm_dd = new SimpleDateFormat(
			"yyyy-MM-dd");

	private static String strHandler_pre = "";
	private static Logger logger = null;
	private static boolean checked_logs;

	/**
	 * 濡쒓굅瑜� 由ы꽩�븳�떎.
	 * new File�쓣 �븯湲� �쟾�뿉 SAFEN_CDR.strPrePath瑜� �뜑�빐�빞 �븿�뿉 �쑀�쓽�빐�빞 �븳�떎.
	 * linux�뿉�꽌 cd寃쎈줈 臾몄젣 �븣臾몄뿉
	 * 
	 * @return
	 */
	public static Logger getLogger() {
		String strHandler = getLoggerFilePath();

		File logsf = new File(SAFEN_CDR.strPrePath + "logs");// logs�뤃�뜑

		if (checked_logs == false && logsf.exists() == false) {
			logsf.mkdirs();
		}
		checked_logs = true;

		if (!strHandler_pre.equals(strHandler)) {
			strHandler_pre = strHandler;

			logger = Logger.getLogger(strHandler);
			logger.setLevel(Level.ALL);

			FileHandler fh = null;
			try {
				fh = new FileHandler(strHandler, true);
			} catch (SecurityException e) {
				System.err.println(e.getMessage());
				e.printStackTrace();
			} catch (IOException e) {
				System.err.println(e.getMessage());
				e.printStackTrace();
			}
			logger.addHandler(fh);
			logger.setLevel(Level.ALL);
			SimpleFormatter sf = new SimpleFormatter();
			fh.setFormatter(sf);
		}

		return logger;
	}

	/**
	 * pathSeparatorChar �뒗 �쐢�룄�슦�뿉�꽌�뒗 ;�씠怨�, 由щ늼�뒪�뿉�꽌�뒗 :�씠�떎. separatorChar�뒗 �쐢�룄�슦�뿉�꽌�뒗 \�씠怨�
	 * 由щ늼�뒪�뿉�꽌�뒗 /�씠�떎.
	 * 
	 * @return
	 */
	public static String getLoggerFilePath() {
		return SAFEN_CDR.strPrePath + "logs" + File.separatorChar + "log" + getYMD() + ".txt";
	}

	/**
	 * 8�옄由� �궇吏쒕�� 由ы꽩�븳�떎. �삁) 20160718
	 * 
	 * @return
	 */
	public static String getYMD() {
		// �빐�떦�썡�쓽 媛믪쓣 由ы꽩�븳�떎.
		Date now = new Date();
		String strDate = sdf_YMD.format(now);
		return strDate;
	}

	/**
	 * �빐�떦�뀈�썡�쓽 媛믪쓣 由ы꽩�븳�떎. �삁: 201607
	 * 
	 * @return
	 */
	public static String getYYYYMM() {
		// �빐�떦�뀈�썡�쓽 媛믪쓣 由ы꽩�븳�떎.
		Date now = new Date();
		String strDate = sdf_YYYYMM.format(now);
		return strDate;
	}

	/**
	 * �뀈�썡�씪�떆媛꾩젙蹂대�� 由ы꽩�븳�떎. �삁:)2016071817
	 * 
	 * @return
	 */
	public static String getYMDH() {
		Date now = new Date();
		String strDate = sdf_YMDH.format(now);
		return strDate;
	}

	/**
	 * �쁽�옱�떆媛꾩쓣 24�떆媛� �삎�떇�쑝濡� 由ы꽩�븳�떎.
	 * 
	 * @return
	 */
	public static String getHH() {
		Date now = new Date();
		String strDate = sdf_HH.format(now);
		return strDate;
	}

	/**
	 * �닽�옄瑜� �븣�뙆踰� �븫�샇�솕 �븳�떎. 9 -> z濡� 諛붽씔�떎.
	 * 
	 * @param str0504
	 * @return
	 */
	public static String encrypt0504(String str0504) {
		byte[] src = str0504.substring(3).getBytes();
		for (int i = 0; i < src.length; i++) {
			src[i] += 65;
		}
		return new String(src);
	}

	/**
	 * 怨듬갚�쓽 �닔留뚰겮�쓽 臾몄옄�뿴�쓣 由ы꽩�븳�떎.
	 * 
	 * @param cnt
	 * @return
	 */
	public static String space(int cnt) {
		if (cnt < 0)
			throw new RuntimeException(
					"space parameter is negative value error!");
		String val = "";
		for (int i = 0; i < cnt; i++) {
			val += Env.SPACE;
		}
		return val;
	}

	/**
	 * 臾몄옄�뿴�쓽 湲몄씠瑜� 由ы꽩�븳�떎.
	 * 
	 * @param str
	 * @return
	 */
	public static int getLen(String str) {
		return str.length();
	}

	/**
	 * 二쇱뼱吏� 湲몄씠留뚰겮 怨듬갚�쑝濡� 梨꾩슫 �썑 臾몄옄�뿴�쓣 �쇊履쎌젙�젹濡� �븯�뿬 異쒕젰�븯�뿬 寃곌뎅 二쇱뼱吏� 湲몄씠留뚰겮�쓽 湲몄씠濡� 由ы꽩�븳�떎.
	 * 
	 * @param len
	 * @param str
	 * @return
	 */
	public static String paddingLeft(int len, String str) {
		String retVal = "";
		try {
			retVal = str + space(len - getLen(str));
		} catch (RuntimeException e) {
			Utils.getLogger().warning(e.getMessage());
			DBConn.latest_warning = "ErrPOS073";
		}
		return retVal;
	}

	/**
	 * substring�뿰�궛�쓣 �닔�뻾�븳 �썑 trim�쑝濡� 怨듬갚�쓣 �젣嫄고븳 媛믪쓣 由ы꽩�븳�떎.
	 * 
	 * @param str
	 * @param s
	 * @param e
	 * @return
	 */
	public static String substringVal(String str, int s, int e) {
		return str.substring(s, e).trim();
	}

	/**
	 * Mysql datatime �옄猷뚰삎�쑝濡� 泥섎━�븷 �닔 �엳�뒗 �궇吏쒕줈 蹂�寃쏀븳�떎. yyyyMMddHHmmss瑜� �궇吏쒕줈 蹂�寃쏀븳 �썑 �궇吏쒕��
	 * MySql datetime泥섎━ 媛��뒫�븳 臾몄옄�뿴濡� 蹂�寃쏀븳�떎.
	 * 
	 * @param str
	 * @return
	 */
	public static String toDate(String str) {
		String dt = null;
		if (!"".equals(str)) {
			Date date = null;
			try {
				date = sdf_yyyymmddhhmmss.parse(str);
			} catch (ParseException e) {
				Utils.getLogger().warning(e.getMessage());
				DBConn.latest_warning = "ErrPOS074";
				e.printStackTrace();
			}
			dt = sdf_yyyy_mm_dd_hhmmss.format(date);
		}
		return dt;
	}

	/**
	 * �뒪�깮�삤瑜섎�� 臾몄옄�뿴濡� 異쒕젰�븳�떎.
	 * 
	 * @param e
	 * @return
	 */
	public static String stack(Exception e) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);
		return sw.toString();
	}

	/**
	 * �뒪�깮�삤瑜섎�� 臾몄옄�뿴濡� 異쒕젰�븳�떎.
	 * 
	 * @param e
	 * @return
	 */
	public static String stack(Error e) { 
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);
		return sw.toString();
	}

	/**
	 * �쁽�옱�쓽 �슂�씪�쓣 由ы꽩�븳�떎. 1:�씪, 2:�썡, 3:�솕, 4:�닔, 5:紐�, 6:湲�, 7:�넗
	 * 
	 * @return
	 */
	public static int getWeekDay() {
		Calendar oCalendar = Calendar.getInstance(); // �쁽�옱 �궇吏�/�떆媛� �벑�쓽 媛곸쥌 �젙蹂� �뼸湲�
		return oCalendar.get(Calendar.DAY_OF_WEEK);
	}

	public static String substring(String msg, int ifrom, int ito) 
	{
		int len = msg.length();
		if(len < ito) {
			ito = len;
		}
		return msg.substring(ifrom, ito);
	}
	
	//�궇吏� �쑀�땳�뒪���엫�쑝濡� 蹂�寃�
	public static long date2unix(String str)  
	{
	  long unixTime=0L;
	  try{
		  String dateString = str;
		  DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	  
		  Date date = dateFormat.parse(dateString);
		  unixTime = (long)date.getTime()/1000;
		  
		  

	  }catch (ParseException e) {
			e.printStackTrace();
	  }
	  return unixTime;
	}


	//�쑀�땳�뒪���엫 �궇吏쒕줈 蹂�寃�
	public static String unix2date(String str)
	{
		String source = str; //DB�뿉 ���옣�맂 �쑀�땳�뒪���엫 �삎�떇 �궇吏�
		long t = Long.parseLong(source + "000"); 
		SimpleDateFormat simpleDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return simpleDate.format(t);
	}


	/**
	* String add60day
	* @return String "yyyy-MM-dd" + INTERVAL 60 DAY
	*/
	public static String add60day(){
		String retVal="";
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		Calendar c = Calendar.getInstance();
		c.setTime(new Date()); // Now use today date.
		c.add(Calendar.DATE, 60); // Adding 60 days
		retVal = sdf.format(c.getTime());
		return retVal;
	}


	/**
	* String add60day
	* @return String "yyyy-MM-dd" + INTERVAL 60 DAY
	*/
	public static String add90day(){
		String retVal="";
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		Calendar c = Calendar.getInstance();
		c.setTime(new Date()); // Now use today date.
		c.add(Calendar.DATE, 90); // Adding 60 days
		retVal = sdf.format(c.getTime());
		return retVal;
	}
	/** getyyyymmdd()
	 * 12�옄由� �궇吏쒕�� 由ы꽩�븳�떎. �삁) 2016-07-18
	 * @return String.format("yyyy-MM-dd")
	 */
	public static String getyyyymmdd() {
		// �빐�떦�썡�쓽 媛믪쓣 由ы꽩�븳�떎.
		Date now = new Date();
		String strDate = sdf_yyyy_mm_dd.format(now);
		return strDate;
	}
}

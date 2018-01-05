package kr.co.cashq.safen_point;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;

/**
 * �븞�떖踰덊샇 留ㅽ븨 �뾽臾댁� 肄쒕줈洹� �뾽臾� 媛앹껜
 * 
 * @author pgs
 * 
 */
public class SAFEN_CDR {
	
	public static String strPrePath = "";//寃쎈줈媛� �엯�젰�맂寃쎌슦�뒗 �빐�떦 寃쎈줈瑜� 濡쒓렇�굹 �솚寃쎈��닔�뿉�꽌 �씫�뼱�삤寃� �븳�떎.
	
	private static final long N600000 = 600000;//10遺� = 60留뚯큹
	// private static final boolean TEST_MODE = false;
	public static int heart_beat = 0;
	private static int call_log_skip_count = 60;
	private static Calendar pivotFutureTime;
	private static long loggedTime = 0;

	// �봽濡쒖꽭�뒪�쓽 �떆�옉�젏
	public static void main(String[] args) {
		
		if(0 < args.length)
		{
			strPrePath = args[0];
			strPrePath += File.separatorChar;
		}
		
		Utils.getLogger().info("SAFEN_CDR program started!");
		try {
			terminate_this();// killme.txt�뙆�씪�씠 議댁옱�븯�뿬�룄 �봽濡쒓렇�옩援щ룞�떆 �젙�긽援щ룞�릺�룄濡�
							// killme.txt�뙆�씪�쓣 吏��슦怨� �떆�옉�븳�떎.
			doMainProcess();
			DBConn.close();
			CdrTrigger w = CdrTrigger.getInstance();
			w.disconnectCallLogServer();
			Utils.getLogger().info("SAFEN_CDR program ended!");
		} catch (Exception e) {
			Utils.getLogger().warning(e.getMessage());
			Utils.getLogger().warning(Utils.stack(e));
			DBConn.latest_warning += "ErrPOS062";
		} catch (Error e) {
			Utils.getLogger().warning(e.getMessage());
			Utils.getLogger().warning(Utils.stack(e));
			DBConn.latest_warning += "ErrPOS063";
		}
		
		if(!"".equals(DBConn.latest_warning))
		{
			BufferedWriter writer = null;
			File logFile = new File(strPrePath + "_error_log.txt");

            try {
				writer = new BufferedWriter(new FileWriter(logFile));
				
				writer.write(Utils.getYMD() + " " + DBConn.latest_warning+"\n");
				writer.flush();
			    
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 硫붿씤�봽濡쒖꽭�뒪
	 */
	private static void doMainProcess() {
		Thread this_thread = Thread.currentThread();
		try {
			synchronized (this_thread) {
				while (doWork()) {
					if (0 < heart_beat++) {
						if (DBConn.getConnection() != null) {
							Calendar nowTime = Calendar.getInstance();
							if (pivotFutureTime == null) {
								pivotFutureTime = nowTime;// 誘몃옒�떆媛꾩쑝濡� �뀑�똿�맖
							}

							long delta_time = pivotFutureTime.getTimeInMillis() - nowTime.getTimeInMillis();

							/* 1遺꾧컙寃� �샊�� 5珥�*60�씠硫� 5遺� 媛꾧꺽�쑝濡� 肄쒕줈洹몃�� 媛깆떊�븳�떎. */
							if (60 <= call_log_skip_count++ || delta_time <= 0)
							{
								call_log_skip_count = 0;
								pivotFutureTime = nowTime;
								pivotFutureTime.add(Calendar.SECOND, 5);//5珥� �썑濡� �뀑�똿�븳�떎.(湲곗〈 5珥�)
								doProcessCallLog();
							}

							if (heart_beat < Env.MINUTE) {
								this_thread.wait(1000);// 1珥� ��湲�
							} else if (heart_beat < Env.MINUTE * 2) {
								this_thread.wait(2000);// 2珥� ��湲�
							} else if (heart_beat < Env.MINUTE * 4) {
								this_thread.wait(3000);// 3珥� ��湲�
							} else if (heart_beat < Env.MINUTE * 8) {
								this_thread.wait(4000);// 4珥� ��湲�
							} else {// 洹� �쇅�뒗 5珥� ��湲�
								this_thread.wait(5000);// 5珥� ��湲�
							}

							if (log10Minute()) {
								Utils.getLogger().info("SAFEN_CDR alive");
							}

							/* doSMSProcess */
							/*
							if ("1".equals(Env.getInstance().sms_use)) {
								doSMSProcess();
							}
							*/

						} else {
							this_thread.wait(10000);// 10珥� ��湲� db媛� �삱�씪�삤湲곕�� 湲곕떎由곕떎.
							Utils.getLogger().warning("DB�꽌鍮꾩뒪媛� �삱�씪�솕�뒗吏� �솗�씤�븯�꽭�슂!");
							DBConn.latest_warning = "ErrPOS064";
						}
					}
				}
			}
		} catch (InterruptedException e) {
			Utils.getLogger().warning(e.getMessage());
			DBConn.latest_warning = "ErrPOS065";
			e.printStackTrace();
		}
	}

	/**
	 * SMS愿��젴�맂 �옉�뾽�쓣 泥섎━�븳�떎.
	 */
	private static void doSMSProcess() {
		if (CdrTrigger.nLogonAndCon == 1)// ;//0:理쒖큹(�궇吏쒓� 諛붾�뚮㈃ 0�쑝濡� 蹂�寃쏀븿), 1: �뿰寃�
											// �꽦怨� 洹몃━怨� 濡쒓렇�씤 �꽦怨�, 2:�뿰寃곗떎�뙣 �샊�� 濡쒓렇�씤 �떎�뙣
		{// �뿰寃곗꽦怨�
			String strYMDH = Utils.getYMDH();
			if (!strYMDH.equals(CdrTrigger.strYMD_success)) {
				String[] arrWeekDays = Env.getInstance().sms_success_week_days
						.split(",");

				for (int i = 0; i < arrWeekDays.length; i++) {
					if(Utils.getWeekDay() == Integer.parseInt(arrWeekDays[i])) 
					{
						if(Env.getInstance().sms_success_hour.equals(Utils.getHH())) 
						{	
							Smsq_send.sendSuccessMsg(Env.getInstance().sms_phones);
							CdrTrigger.strYMD_success = Utils.getYMDH();
						}
					}
				}
			}
		} else {// �뿰寃곗떎�뙣
			String strYMD = Utils.getYMD();
			
			if(!strYMD.equals(CdrTrigger.strYMD_fail))
			{
				Smsq_send.sendFailMsg(Env.getInstance().sms_phones);
				CdrTrigger.strYMD_fail = strYMD;
			}
			
		}
		// CdrTrigger.strYMD_success = "";//SMS�쟾�넚�븳 �궇吏� 留ㅼ＜ 湲덉슂�씪 �삤�쟾 10�떆�뿉 �븳 踰덈쭔
		// 蹂대깂<nLogonAndCon蹂��닔�� 愿�怨꾧� �엳�떎.>
		// CdrTrigger.strYMD_fail="";
	}

	/***
	 * 10遺꾨쭔�떎 濡쒓렇瑜� 李띾룄濡� �븯�뒗 議곌굔�쑝濡� 10遺꾩씠 �쓽���뒗吏�瑜� 泥댄겕�븳�떎.
	 * 
	 * @return
	 */
	private static boolean log10Minute() {
		boolean retVal = false;
		long ctime = System.currentTimeMillis();
		if (loggedTime < ctime) {
			loggedTime = ctime + N600000;
			retVal = true;
		}
		return retVal;
	}

	/**
	 * 肄쒕줈洹� �봽濡쒖꽭�뒪 �닔�뻾
	 */
	private static void doProcessCallLog() {
		try {

			CdrTrigger w = CdrTrigger.getInstance();
			String strRetVal = "";

			// if (TEST_MODE == false) {
			//w.doLogon();
			
			//waitMiliSec(5000);
			
//			strRetVal = w.doMsgMain();
			w.doDBWork(strRetVal);

		} catch (Exception e) {
			Utils.getLogger().warning(e.getMessage());
			Utils.getLogger().warning(Utils.stack(e));
			DBConn.latest_warning = "ErrPOS066";
		}
	}

	/** n 諛�由촶ec �룞�븞 ��湲고븳�떎.
	 * 
	 */
	private static void waitMiliSec(int n5000) {
		Thread this_thread = Thread.currentThread();
		synchronized (this_thread) {
			try {
				this_thread.wait(n5000);// 5珥덇컙 ��湲고븳�떎.
			} catch (InterruptedException e) {
				Utils.getLogger().warning(e.getMessage());
				DBConn.latest_warning = "ErrPOS012";
				e.printStackTrace();
			}// 5珥� ��湲고븿.
		}
	}

	/**
	 * �븞�쟾�븳 醫낅즺瑜� �닔�뻾�븷吏� �뙋�떒�븳 �썑 臾댄븳猷⑦봽瑜� 吏꾩엯�빐�빞 �븳�떎硫� true瑜� 由ы꽩�븳�떎.
	 * 
	 * @return
	 */
	private static boolean doWork() {
		if (terminate_this()) {
			return false;
		} else {
			// /�뿬湲곗꽌 DB瑜� �씫�뼱�꽌 �옉�뾽�븳�떎.
			Safen_cmd_queue.doMainProcess();
			return true;
		}
	}

	/**
	 * 醫낅즺瑜� �닔�뻾�븷吏� 寃곗젙�븳�떎. killme.txt�뙆�씪�씠 議댁옱�븯硫� �븞�쟾�븳 醫낅즺瑜� �닔�뻾�븳�떎. 泥섏쓬 �떆�옉�떆�뿉 �샇異쒕릺湲곕룄 �븯吏�留� 二쎌씠�뒗 湲곕뒫�씠
	 * �븘�땲�씪 �떎�쓬�뿉 �븞�쟾�븳 醫낅즺瑜� �쐞�븳 �븞�젙�쟻�씤 �닔�뻾�쓣 �쐞�븳 諛⑹븞�씠�떎.
	 * 
	 * @return
	 */
	private static boolean terminate_this() {
		boolean is_file_exist = false;
		// 醫낅즺瑜� 紐낅졊�븯�뒗 �뙆�씪�씠 �엳�쑝硫� �븞�쟾�븳 醫낅즺瑜� �닔�뻾�븳�떎.
		File file = new File(SAFEN_CDR.strPrePath + "killme.txt");
		is_file_exist = file.exists();
		if (is_file_exist) {
			is_file_exist = file.delete();
		}
		return is_file_exist;
	}

}

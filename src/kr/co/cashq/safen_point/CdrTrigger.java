package kr.co.cashq.safen_point;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.sql.SQLException;

/**
 * 肄쒕줈洹멸��젴�맂 �뾽臾대줈吏곸쓣 泥섎━�븳�떎. safen_cdr �뀒�씠釉붽낵 愿��젴�맂�떎.
 * 
 * @author pgs
 * 
 */
public class CdrTrigger {
	public static int nLogonAndCon = 0;//0:理쒖큹(�궇吏쒓� 諛붾�뚮㈃ 0�쑝濡� 蹂�寃쏀븿), 1: �뿰寃� �꽦怨� 洹몃━怨� 濡쒓렇�씤 �꽦怨�, 2:�뿰寃곗떎�뙣 �샊�� 濡쒓렇�씤 �떎�뙣	
	public static String strYMD_success = "";//SMS�쟾�넚�븳 �궇吏� 留ㅼ＜ 湲덉슂�씪 �삤�쟾 10�떆�뿉 �븳 踰덈쭔 蹂대깂<nLogonAndCon蹂��닔�� 愿�怨꾧� �엳�떎.>
	
	public static String strYMD_fail = "";//SMS�쟾�넚�떎�뙣�븳 �궇吏� 留ㅼ씪 �븳 踰� <nLogonAndCon 蹂��닔�� 愿�怨꾧� �엳�떎>
	
	private static final int PACKET_LENGTH_8192 = 8192;
	private static final int N149 = 149;
	private static final int N24 = 24;
	private Socket socket;
	private BufferedInputStream bis;
	private BufferedOutputStream bos;
	private static CdrTrigger Instance;
	private boolean isConnected = false;
	private boolean isLogon;

	/** Creates a new instance of Worker */
	private CdrTrigger() {
		socket = null;
		bis = null;
		bos = null;
	}

	/**
	 * 硫붿꽭吏�瑜� 蹂대궦�떎.
	 * 
	 * @param message
	 */
	private void sendMessage(String message) {
		try {
			if (isConnected == false) {
				//connect2CallLogServer(Env.getInstance().CALL_LOG_SERVER_IP,	Env.getInstance().CALL_LOG_SERVER_PORT);
			}
			if (isConnected) {
				bos.write(message.getBytes());
				bos.flush();
				Utils.getLogger().info("send:[" + message + "]");
			}
		} catch (IOException ioe) {
			Utils.getLogger().warning(ioe.getMessage());
			DBConn.latest_warning = "ErrPOS001";
		}
	}

	/**
	 * 硫붿꽭吏�瑜� �닔�떊�븳�떎.
	 * 
	 * @return
	 */
	public String receiveMessage() {
		StringBuffer strbuf = new StringBuffer();
		try {
			if (isConnected) {
				byte[] buf = new byte[PACKET_LENGTH_8192];

				int read = 0;
				if ((read = bis.read(buf)) > 0) {// �뿬湲곗꽌 �쓳�떟�뾾�뒗 �쁽�긽�씠 �깮湲� �닔 �엳�떎. �듅�엳 �몢踰덉㎏
													// 吏꾩엯�떆
													// 洹몃윺 �닔 �엳�떎.
					String str = new String(buf);
					Utils.getLogger().info("rcv:[" + str.trim() + "]");
					strbuf.append(new String(buf, 0, read));
				}
			}
		} catch (IOException e) {
			Utils.getLogger().warning(e.getMessage());
			DBConn.latest_warning = "ErrPOS002";
		}

		return strbuf.toString();
	}

	/**
	 * 肄쒕줈洹몄꽌踰꾩��쓽 �뿰寃곗쓣 醫낅즺�븳�떎.
	 */
	public void disconnectCallLogServer() {
		if (isConnected) {
			try {
				bis.close();
				bos.close();
				socket.close();
				isConnected = false;
				isLogon = false;
				Utils.getLogger().info(":disconnect:");
			} catch (IOException e) {
				Utils.getLogger().warning(e.getMessage());
				DBConn.latest_warning = "ErrPOS003";
			}
		}
	}

	/**
	 * DB�옉�뾽�쓣 �닔�뻾�븳�떎.
	 * 
	 * @param paramVal
	 */
	public void doDBWork(String paramVal) {
		packetProcess(paramVal);
	}

	/**
	 * @param strVal
	 * @throws NumberFormatException
	 */
	public void packetProcess(String strVal) throws NumberFormatException {
		String strHeader2;

	}

	private String parseCallLog(String str) {
		String v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11;

		int s = 0;
		v1 = str.substring(s, s = s + 20).trim();// 怨좎쑀媛�20�옄由�
		v2 = str.substring(s, s = s + 20).trim();// 諛쒖떊踰덊샇
		v3 = str.substring(s, s = s + 20).trim();// �젒�냽踰덊샇
		v4 = str.substring(s, s = s + 20).trim();// 李⑹떊踰덊샇
		v5 = str.substring(s, s = s + 14).trim();// �뿰寃곗떆�옉�떆媛�
		v6 = str.substring(s, s = s + 14).trim();// �뿰寃곗쥌猷뚯떆媛�
		v7 = str.substring(s, s = s + 14).trim();// �꽌鍮꾩뒪�떆�옉�떆媛�
		v8 = str.substring(s, s = s + 14).trim();// �꽌鍮꾩뒪醫낅즺�떆媛�
		v9 = str.substring(s, s = s + 6).trim();// �넻�솕 �떆媛�
		v10 = str.substring(s, s = s + 6).trim();// �꽌鍮꾩뒪 �떆媛�
		v11 = str.substring(s, s = s + 1).trim();// �넻�솕寃곌낵
		/*
		 * 1:�넻�솕 �꽦怨�, 2:李⑹떊 �넻�솕以�, 3:李⑹떊 臾댁쓳�떟, 4:李⑹떊痢� �쉶�꽑遺�議�, 5:李⑹떊踰덊샇 寃곕쾲 �샊�� �쑀�슚�븯吏��븡�� 踰덊샇,
		 * 6:諛�/李⑹떊�옄 �넻�솕 �뿰寃� �삤瑜�, B:李⑹떊 �떆�룄 以� 諛쒖떊痢� �샇 醫낅즺, F:李⑹떊踰덊샇 �뾾�쓬
		 */
		String retVal = "";
		if (dbCallLogProcess(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11)) {
			retVal = Utils.paddingLeft(20, v1) + "0000";// �쓳�떟
		} else {
			retVal = Utils.paddingLeft(20, v1) + "0001";// �굹以묒뿉 �옱�쟾�넚�빐以꾧쾬�쓣 �떦遺��븯�뿬 �꽌踰꾨줈
														// �떎�떆蹂대깂 DB泥섎━瑜� �븯吏� 紐삵븯���쓬.
		}
		return retVal;
	}

	/**
	 * �꽌踰꾩뿉 �떎�떆蹂대궡二쇨린瑜� �슂泥��븯�뒗 寃쎌슦�뿉�뒗 false瑜� 由ы꽩�븿.
	 * 
	 * @param v1
	 * @param v2
	 * @param v3
	 * @param v4
	 * @param v5
	 * @param v6
	 * @param v7
	 * @param v8
	 * @param v9
	 * @param v10
	 * @param v11
	 * @return
	 */
	private boolean dbCallLogProcess(String v1, String v2, String v3,
			String v4, String v5, String v6, String v7, String v8, String v9,
			String v10, String v11) {

		boolean result = false;

		if (Env.getInstance().USE_FILTER == true && "".equals(v4)
				&& "F".equals(v11)) {
			// �븘臾닿쾬�룄 �븞�븿. 濡쒓렇�뿉 �뙎吏� �븡怨� �떒吏� 留덉뒪�꽣�뿉 理쒓렐�궗�슜�맂�궇吏쒕줈 ���옣�븳�떎.
			result = true;
		} else {
			StringBuilder sb = new StringBuilder();
			/*
			sb.append("insert into safen_cdr(conn_sdt, conn_edt, conn_sec, service_sdt, ");
			sb.append(" service_edt, service_sec, safen, safen_in, safen_out, billsec,");
			sb.append(" unique_id, account_cd, calllog_rec_file, rec_file_cd, status_cd, create_dt) values("
					+ "?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, now())");
			*/
			MyDataObject dao = new MyDataObject();// PreparedStatement pstmt;

			try {
				dao.openPstmt(sb.toString());

				int n = 1;

				String dt1 = null, dt2 = null, dt3 = null, dt4 = null;

				dt1 = Utils.toDate(v5);
				dt2 = Utils.toDate(v6);
				dt3 = Utils.toDate(v7);
				dt4 = Utils.toDate(v8);

				dao.pstmt().setString(n++, dt1);
				dao.pstmt().setString(n++, dt2);

				dao.pstmt().setInt(n++, Integer.parseInt(v9));
				dao.pstmt().setString(n++, dt3);
				dao.pstmt().setString(n++, dt4);
				dao.pstmt().setInt(n++, Integer.parseInt(v10));
				dao.pstmt().setString(n++, v3);// 0504踰덊샇
				dao.pstmt().setString(n++, v4);// 李⑹떊踰덊샇
				dao.pstmt().setString(n++, v2);// 諛쒖떊踰덊샇
				dao.pstmt().setInt(n++,
						Integer.parseInt(v9) - Integer.parseInt(v10));
				dao.pstmt().setString(n++, v1);
				dao.pstmt().setString(n++, Safen_master.getAccount_cd(v3));// account_cd
				dao.pstmt().setString(n++, "");// calllog_rec_file
				dao.pstmt().setString(n++, "1");// rec_file_cd
				dao.pstmt().setString(n++, v11);

				int cnt = 0;
				cnt = dao.pstmt().executeUpdate();

				if (cnt == 1) {
					result = true;
				}
			} catch (SQLException e) {
				if (-1 < e.getMessage().indexOf("uplicate")) {// 以묐났�씤 寃쎌슦 �꽦怨듭쑝濡�
																// 媛꾩＜�븳�떎./Duplicate
																// entry ...瑜�
																// 由ы꽩�븯湲�
																// �븣臾몄뿉
					// �떒, �뾽�뜲�씠�듃瑜� �닔�뻾�븳�떎.? 洹몃깷 臾댁떆�븳�떎.
					result = true;
				} else {
					Utils.getLogger().warning(e.getMessage());
					DBConn.latest_warning = "ErrPOS009";
					e.printStackTrace();
					result = false;
				}
			} catch (Exception e) {
				Utils.getLogger().warning(e.getMessage());
				Utils.getLogger().warning(Utils.stack(e));
				DBConn.latest_warning = "ErrPOS010";
			} finally {
				dao.closePstmt();
			}

		}

		Safen_master.dealed(v3);

		return result;
	}

	public static CdrTrigger getInstance() {
		if (Instance == null) {
			Instance = new CdrTrigger();
		}
		return Instance;
	}

}

package kr.co.cashq.safen_point;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * cashq.SMSQ_SEND �뀒�씠釉붿쓽 �뜲�씠�꽣 泥섎━ �뾽臾댁쿂由щ�� �닔�뻾�븳�떎.
 * 
 * @author pgs
 * 
 */
public class Smsq_send {

	public Smsq_send() {

	}

	/**
	 * SKTL �꽦怨듭떆 SMS 硫붿떆吏�瑜� �쟾�넚�븳�떎. DB�엯�젰�꽦怨듭떆 true瑜� 由ы꽩�븳�떎.
	 * 
	 * @param sms_phones
	 *            : �쟾�솕踰덊샇�뱾�쓣 肄ㅻ쭏濡� 援щ텇�븯�뿬 �뿬�윭媛� 蹂대궪 �닔 �엳�떎.
	 * 
	 */
	public static boolean sendSuccessMsg(String sms_phones) {
		boolean retVal = false;
		StringBuilder sb = new StringBuilder();
		MyDataObject dao = new MyDataObject();

		sb.append("insert into cashq.SMSQ_SEND set "
				+ "msg_type='S', dest_no=?,call_back=?,msg_contents=?, sendreq_time=now()");

		String msg = "SKTL Server alive~[" + Env.getInstance().CORP_CODE + "]"
				+ DBConn.latest_warning;
		msg = Utils.substring(msg, 0, 80);

		try {
			dao.openPstmt(sb.toString());

			String[] phones = sms_phones.split(",");

			for (int i = 0; i < phones.length; i++) {
				int n = 1;
				dao.pstmt().setString(n++, phones[i]);
				dao.pstmt().setString(n++, Env.getInstance().sms_send_phone);
				dao.pstmt().setString(n++, msg);

				dao.pstmt().addBatch();
				dao.pstmt().clearParameters();
			}

			int cnt = 0;
			int[] arr_i = dao.pstmt().executeBatch();
			for (int i = 0; i < arr_i.length; i++) {
				cnt += arr_i[i];
			}

			if (cnt != phones.length) {
				Utils.getLogger().warning(dao.getWarning(cnt, phones.length));
				DBConn.latest_warning = "ErrPOS067";
			} else {
				//Site_push_log.sendMsg(sms_phones, msg);
			}
			DBConn.latest_warning = "";
			retVal = true;
		} catch (SQLException e) {
			Utils.getLogger().warning(e.getMessage());
			Utils.getLogger().warning(Utils.stack(e));
			DBConn.latest_warning = "ErrPOS068";
		} catch (Exception e) {
			Utils.getLogger().warning(e.getMessage());
			Utils.getLogger().warning(Utils.stack(e));
			DBConn.latest_warning = "ErrPOS069";
		} finally {
			dao.closePstmt();
		}
		return retVal;
	}

	/**
	 * SKTL �뿰寃� �떎�뙣�떆 SMS 硫붿떆吏�瑜� �쟾�넚�븳�떎. DB�엯�젰�꽦怨듭떆 true瑜� 由ы꽩�븳�떎.
	 * 
	 * @param sms_phones
	 * @return
	 */
	public static boolean sendFailMsg(String sms_phones) {
		boolean retVal = false;
		StringBuilder sb = new StringBuilder();
		MyDataObject dao = new MyDataObject();

		sb.append("insert into cashq.SMSQ_SEND set "
				+ "msg_type='S', dest_no=?,call_back=?,msg_contents=?, sendreq_time=now()");
		String msg = "SKTL Server down!![" + Env.getInstance().CORP_CODE + "]"
				+ DBConn.latest_warning;
		msg = Utils.substring(msg,0, 80);

		try {
			dao.openPstmt(sb.toString());

			String[] phones = sms_phones.split(",");
			for (int i = 0; i < phones.length; i++) {
				int n = 1;
				dao.pstmt().setString(n++, phones[i]);
				dao.pstmt().setString(n++, Env.getInstance().sms_send_phone);
				dao.pstmt().setString(n++, msg);

				dao.pstmt().addBatch();
				dao.pstmt().clearParameters();
			}

			int cnt = 0;
			int[] arr_i = dao.pstmt().executeBatch();
			for (int i = 0; i < arr_i.length; i++) {
				cnt += arr_i[i];
			}

			if (cnt != phones.length) {
				Utils.getLogger().warning(dao.getWarning(cnt, phones.length));
				DBConn.latest_warning = "ErrPOS070";
			} else {
				//Site_push_log.sendMsg(sms_phones, msg);
			}
			retVal = true;
		} catch (SQLException e) {
			Utils.getLogger().warning(e.getMessage());
			Utils.getLogger().warning(Utils.stack(e));
			DBConn.latest_warning = "ErrPOS071";
		} catch (Exception e) {
			Utils.getLogger().warning(e.getMessage());
			Utils.getLogger().warning(Utils.stack(e));
			DBConn.latest_warning = "ErrPOS072";
		} finally {
			dao.closePstmt();
		}
		return retVal;
	}
}

package kr.co.cashq.safen_point;

import java.sql.SQLException;

/**
 * site_push_log �뀒�씠釉붿쓽 �뜲�씠�꽣 泥섎━ �뾽臾댁쿂由щ�� �닔�뻾�븳�떎.
 * 
 * @author pgs
 * 
 */
public class Site_push_log {

	/**
	 * 湲곕낯�깮�꽦�옄
	 */
	public Site_push_log() {

	}

	/**
	 * site_push_log�뿉 異붽��븳�떎.
	 * @param sms_phones
	 * @param msg
	 * @return
	 */
	public static boolean sendMsg(String sms_phones, String msg) {
		boolean retVal = false;
		StringBuilder sb = new StringBuilder();
		MyDataObject dao = new MyDataObject();

		sb.append("insert into cashq.site_push_log set "
				+ "stype='SMS', biz_code='ANP', caller=?, called=?, wr_subject=?, regdate=now(), result=''");
		try {
			dao.openPstmt(sb.toString());

			String[] phones = sms_phones.split(",");
			for (int i = 0; i < phones.length; i++) {
				int n = 1;
				dao.pstmt().setString(n++, Env.getInstance().sms_send_phone);
				dao.pstmt().setString(n++, phones[i]);
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
				DBConn.latest_warning = "ErrPOS059";
			}
			retVal = true;
		} catch (SQLException e) {
			Utils.getLogger().warning(e.getMessage());
			Utils.getLogger().warning(Utils.stack(e));
			DBConn.latest_warning = "ErrPOS060";
			/* grant濡� �빐�떦 �궗�슜�옄�뿉 ���븳 沅뚰븳�쓣 二쇱뼱 臾몄젣 �빐寃곗씠 媛��뒫�븯�떎.
			grant all privileges on cashq.site_push_log to sktl@"%" identified by 'sktl@9495';
			grant all privileges on cashq.site_push_log to sktl@"localhost" identified by 'sktl@9495';
			 */
		} catch (Exception e) {
			Utils.getLogger().warning(e.getMessage());
			Utils.getLogger().warning(Utils.stack(e));
			DBConn.latest_warning = "ErrPOS061";
		} finally {
			dao.closePstmt();
		}
		return retVal;
	}

}

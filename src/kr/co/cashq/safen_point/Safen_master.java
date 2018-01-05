package kr.co.cashq.safen_point;

import java.sql.SQLException;

//import com.nostech.safen.SafeNo;

/**
 * safen_master �뀒�씠釉� 愿��젴 媛앹껜
 * @author pgs
 *
 */
public class Safen_master {

	/**
	 * 留덉뒪�꽣�뀒�씠釉붿뿉 李⑹떊踰덊샇瑜� �벑濡앺븯嫄곕굹, 痍⑥냼�븳�떎.
	 * 
	 * @param safen0504
	 * @param safen_in010
	 * @param mapping_option
	 *            1:�벑濡�, 2:痍⑥냼
	 */
	public static void update_safen_master(String safen0504,
			String safen_in010, int mapping_option) {

		MyDataObject dao = new MyDataObject();
		try {
			StringBuilder sb = new StringBuilder();
			sb.append("update safen_master set safen_in=?, status_cd=?, update_dt=now() where safen=?");

			dao.openPstmt(sb.toString());
			if (mapping_option == 1) {// �벑濡�
				dao.pstmt().setString(1, safen_in010);
				dao.pstmt().setString(2, "u");// used
				dao.pstmt().setString(3, safen0504);
			} else if (mapping_option == 2) {// 痍⑥냼
				dao.pstmt().setString(1, Env.NULL_TEL_NUMBER);
				dao.pstmt().setString(2, "e");// enabled
				dao.pstmt().setString(3, safen0504);
			}

			int cnt = dao.pstmt().executeUpdate();
			if(cnt!=1) {
				Utils.getLogger().warning(dao.getWarning(cnt,1));
				DBConn.latest_warning = "ErrPOS041";
			}

		} catch (SQLException e) {
			Utils.getLogger().warning(e.getMessage());
			DBConn.latest_warning = "ErrPOS042";
			e.printStackTrace();
		} catch (Exception e) {
			Utils.getLogger().warning(e.getMessage());
			Utils.getLogger().warning(Utils.stack(e));
			DBConn.latest_warning = "ErrPOS043";
		}
		finally {
			dao.closePstmt();
		}
	}

	/**
	 * �븞�떖踰덊샇�쓽 洹몃９踰덊샇瑜� 由ы꽩�븳�떎.
	 * 
	 * @param safen0504
	 * @return
	 */
	public static String getGroupCode(String safen0504) {
		String strGrp = "";

		MyDataObject dao = new MyDataObject();
		try {
			StringBuilder sb = new StringBuilder();
			sb.append("select group_cd from safen_master where safen=?");
			dao.openPstmt(sb.toString());
			dao.pstmt().setString(1, safen0504);
			dao.setRs(dao.pstmt().executeQuery());

			if (dao.rs().next()) {
				strGrp = dao.rs().getString("group_cd");
			}

			dao.closePstmt();

		} catch (SQLException e) {
			Utils.getLogger().warning(e.getMessage());
			DBConn.latest_warning = "ErrPOS044";
			e.printStackTrace();
		}
		catch (Exception e) {
			Utils.getLogger().warning(e.getMessage());
			Utils.getLogger().warning(Utils.stack(e));
			DBConn.latest_warning = "ErrPOS045";
		}
		return strGrp;
	}

	/**
	 * �븞�떖踰덊샇�뿉�뵲瑜� �뾽泥댁긽�젏�쟾�솕踰덊샇瑜� 由ы꽩�븳�떎.
	 * 
	 * @param safen0504
	 * @return
	 */
	public static String getAccount_cd(String safen0504) {
		String strAccount_cd = "";

		MyDataObject dao = new MyDataObject();

		try {
			StringBuilder sb = new StringBuilder();
			sb.append("select account_cd from safen_master where safen=?");
			dao.openPstmt(sb.toString());
			dao.pstmt().setString(1, safen0504);

			dao.setRs(dao.pstmt().executeQuery());

			if (dao.rs().next()) {
				strAccount_cd = dao.rs().getString("account_cd");
			}

			dao.closePstmt();

		} catch (SQLException e) {
			Utils.getLogger().warning(e.getMessage());
			DBConn.latest_warning = "ErrPOS046";
			e.printStackTrace();
		}
		catch (Exception e) {
			Utils.getLogger().warning(e.getMessage());
			Utils.getLogger().warning(Utils.stack(e));
			DBConn.latest_warning = "ErrPOS047";
		}
		return strAccount_cd;
	}

	/**
	 * �븞�떖踰덊샇�뿉 �뵲瑜� 李⑹떊踰덊샇瑜� 由ы꽩�븳�떎.
	 * @param safen0504
	 * @param strHint
	 * @return
	 */
	public static String getSafen_in(String safen0504, String strHint) {
		String strSafen_in = "";

		MyDataObject dao = new MyDataObject();
		try {
			StringBuilder sb = new StringBuilder();
			sb.append("select safen_in from safen_master where safen=?");
			dao.openPstmt(sb.toString());
			dao.pstmt().setString(1, safen0504);
			dao.setRs(dao.pstmt().executeQuery());

			if (dao.rs().next()) {
				strSafen_in = dao.rs().getString("safen_in");
			}

			dao.closePstmt();

		} catch (SQLException e) {
			Utils.getLogger().warning(e.getMessage());
			DBConn.latest_warning = "ErrPOS048";
			e.printStackTrace();
		}
		catch (Exception e) {
			Utils.getLogger().warning(e.getMessage());
			Utils.getLogger().warning(Utils.stack(e));
			DBConn.latest_warning = "ErrPOS049";
		}

		if ("".equals(strHint) && Env.NULL_TEL_NUMBER.equals(strSafen_in)) {
			// �븘臾닿쾬�룄 �븞�븿.
		} else {
			if (Env.NULL_TEL_NUMBER.equals(strSafen_in)) {
				String strSafen_in2;
				strSafen_in2 = update_safen_master_from_server(safen0504);

				if (strSafen_in2.startsWith("0")) {
					strSafen_in = strSafen_in2;
				} else {
					strSafen_in = strHint;
				}
			}
		}
		return strSafen_in;
	}

	/**
	 * �븞�떖踰덊샇�뿉 �뵲瑜� �꽌踰꾩젙蹂대�� 媛��졇���꽌 DB�뿉 媛깆떊�븳�떎.
	 * @param safen0504
	 * @return
	 */
	private static String update_safen_master_from_server(String safen0504) {
		//SafeNo safeNo = new SafeNo();
		String retCode2 = null;
		try {
			//retCode2 = safeNo.SafeNoAsk(Env.getInstance().CORP_CODE, safen0504);// 議고쉶

			if (-1 < retCode2.indexOf(",")) {
				String safen_in;
				safen_in = (retCode2.split(","))[0];

				StringBuilder sb = new StringBuilder();
				sb.append("update safen_master set safen_in=?,status_cd=?,update_dt=now() where safen=?");

				MyDataObject dao = new MyDataObject();
				dao.openPstmt(sb.toString());

				dao.pstmt().setString(1, safen_in);
				dao.pstmt().setString(2, "u");// used
				dao.pstmt().setString(3, safen0504);

				int cnt = 0;
				cnt = dao.pstmt().executeUpdate();
				if(cnt!=1) {
					Utils.getLogger().warning(dao.getWarning(cnt,1));
					DBConn.latest_warning = "ErrPOS050";
				}

				dao.closePstmt();
			} else {				
				StringBuilder sb = new StringBuilder();
				sb.append("update safen_master set safen_in=?,status_cd=?,update_dt=now() where safen=?");

				MyDataObject dao = new MyDataObject();
				dao.openPstmt(sb.toString());
				dao.pstmt().setString(1, Env.NULL_TEL_NUMBER);
				dao.pstmt().setString(2, "e");// enabled
				dao.pstmt().setString(3, safen0504);

				int cnt = 0;
				cnt = dao.pstmt().executeUpdate();
				if(cnt!=1) {
					Utils.getLogger().warning(dao.getWarning(cnt,1));
					DBConn.latest_warning = "ErrPOS051";
				}
				dao.closePstmt();
			}
		} catch (SQLException e) {
			Utils.getLogger().warning(e.getMessage());
			DBConn.latest_warning = "ErrPOS052";
			e.printStackTrace();
		} catch (Exception e) {
			Utils.getLogger().warning(e.getMessage());
			Utils.getLogger().warning(Utils.stack(e));
			DBConn.latest_warning = "ErrPOS053";
		}
		return null;
	}

	/**
	 * �븞�떖踰덊샇 留덉뒪�꽣 �뀒�씠釉붿쓽 珥덇린�긽�깭�씤 寃쎌슦�굹 �뼱�젣 吏꾪뻾�룄以� �삤瑜섍� 諛쒖깮�븳 寃쎌슦�쓽 �뜲�씠�꽣媛� 議댁옱�븯硫� �꽌踰꾩쓽 �긽�깭瑜� 媛��졇�� 媛깆떊�븳�떎.
	 * update safen_master set status_cd='a';//�씠�윴 �떇�쑝濡� 珥덇린�뿉 �씤�뒪�넧�씠 �븘�슂�븳�떎.
	 */
	public static void doWark2() {
		MyDataObject dao2 = new MyDataObject();

		StringBuilder sb2 = new StringBuilder();

		sb2.append("select safen from safen_master where status_cd='a' or status_cd='i' and update_dt<date_sub(now(),interval 1 day) limit 1");

		try {
			dao2.openPstmt(sb2.toString());

			dao2.setRs(dao2.pstmt().executeQuery());
			if (dao2.rs().next()) {
				String safen0504 = dao2.rs().getString("safen");
				update_safen_master_from_server(safen0504);
			}
		} catch (SQLException e) {
			Utils.getLogger().warning(e.getMessage());
			DBConn.latest_warning = "ErrPOS054";
			e.printStackTrace();
		} catch (Exception e) {
			Utils.getLogger().warning(e.getMessage());
			Utils.getLogger().warning(Utils.stack(e));
			DBConn.latest_warning = "ErrPOS055";
		}
		finally {
			dao2.closePstmt();
		}
	}

	/**
	 * 肄쒕줈洹멸� �뱾�뼱�삩 �떆媛꾩쓣 媛깆떊�븳�떎. 媛��졊 safen_master�쓽 媛믪씠 �옉��寃껋쓣 異붾━湲� �쐞�븿�씠�떎.
	 * 
	 * @param safen0504
	 */
	public static void dealed(String safen0504) {
		// 理쒓렐 �궗�슜�맂 �쟾�솕踰덊샇�씤吏�瑜� �뙋�떒�븯�뒗 �젙蹂대줈 �솢�슜�븯湲� �쐞�븿�씠�떎.
		StringBuilder sb = new StringBuilder();
		sb.append("update safen_master set dealed_dt=now() where safen=?");

		MyDataObject dao = new MyDataObject();
		try {
			dao.openPstmt(sb.toString());
			dao.pstmt().setString(1, safen0504);
			int cnt = 0;
			cnt = dao.pstmt().executeUpdate();
			if(cnt!=1) {
				Utils.getLogger().warning(dao.getWarning(cnt,1));
				DBConn.latest_warning = "ErrPOS056";
			}
		} catch (SQLException e) {
			Utils.getLogger().warning(e.getMessage());
			DBConn.latest_warning = "ErrPOS057";
		} 
		catch (Exception e) {
			Utils.getLogger().warning(e.getMessage());
			Utils.getLogger().warning(Utils.stack(e));
			DBConn.latest_warning = "ErrPOS058";
		} finally {
			dao.closePstmt();
		}
	}
}

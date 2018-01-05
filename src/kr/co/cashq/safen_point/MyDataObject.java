/**
 * PreparedStatement�쓽 �뵒�옄�씤�뙣�꽩�쓣 �쐞�븳 �겢�옒�뒪�엫.
 * closePstmt �쓽 寃��깋寃곌낵 嫄댁닔�뒗
 * openPstmt 寃��깋寃곌낵 嫄댁닔�� �룞�씪�빐�빞 �븿.
 */
package kr.co.cashq.safen_point;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * �뜲�씠�꽣 �빖�뱾留� 愿��젴 �뵒�옄�씤 �뙣�꽩怨� 愿��젴�맂�떎.
 * <br>try{openPstmt}
 * <br>catch{}
 * <br>finally{closePstmt}
 * @author pgs
 * 
 */
public class MyDataObject {

	private PreparedStatement pstmt = null;

	private ResultSet rs = null;

	String pstmt_str = null;

	/**
	 * �봽由ы럹�뼱�뱶�뒪�뀒�씠�듃癒쇳듃瑜� �궡遺��쟻�쑝濡� �븷�떦�븳�떎.
	 * @param str
	 * @throws SQLException
	 */
	public void openPstmt(String str) throws SQLException {
		pstmt_str = str;
		pstmt = DBConn.getConnection().prepareStatement(str);
	}

	/**
	 * �봽由ы럹�씠�뱶�뒪�뀒�씠�듃癒쇳듃瑜� 由ы꽩�븳�떎.
	 * @return
	 */
	public PreparedStatement pstmt() {
		return pstmt;
	}

	/**
	 * ResultSet 諛� PreparedStatemet瑜� close�븳�떎.
	 */
	public void closePstmt() {
		pstmtClose();
	}

	/**
	 * 
	 */
	private void pstmtClose() {
		try {
			if (rs != null) {
				rs.close();
				rs = null;
			}
		} catch (SQLException e) {
			Utils.getLogger().warning(e.getMessage());
			DBConn.latest_warning = "ErrPOS022";
		}
		catch (Exception e) {
			Utils.getLogger().warning(e.getMessage());
			Utils.getLogger().warning(Utils.stack(e));
			DBConn.latest_warning = "ErrPOS023";
		}
		
		try {
			if (pstmt != null) {
				pstmt.close();
				pstmt = null;
			}
		} catch (SQLException e) {
			Utils.getLogger().warning(e.getMessage());
			DBConn.latest_warning = "ErrPOS024";
		}
		catch (Exception e) {
			Utils.getLogger().warning(e.getMessage());
			Utils.getLogger().warning(Utils.stack(e));
			DBConn.latest_warning = "ErrPOS025";
		}
	}

	/**
	 * 媛앹껜媛� �냼硫몃맆 �븣 �샇異쒕맂�떎. �떎�닔濡� �빐�젣�릺吏� �븡�븯�뜕 �옄�썝�룄 �빐�젣�븯�뿬 硫붾え由� 臾몄젣瑜� �빐寃고븳�떎.
	 */
	@Override
	protected void finalize() throws Throwable {
		if (pstmt != null) {
			tryClose();//close�븯吏� 紐삵븳怨녹뿉�꽌 硫붾え由� 由��쓣 諛⑹��븳�떎.
			Utils.getLogger().warning("close�릺吏� �븡�� pstmt =>" + pstmt_str);
			DBConn.latest_warning = "ErrPOS026";
		}
	}

	/**
	 * 由ъ젅�듃�뀑�쓣 �븷�떦�븳�떎.
	 * @param p_rs
	 * @throws SQLException
	 */
	public void setRs(ResultSet p_rs) throws SQLException {
		if (rs != null) {
			rs.close();
			rs = null;
		}
		rs = p_rs;
	}

	/**
	 * �븷�떦�릺�뿀�뜕 由ъ젅�듃�뀑�쓣 由ы꽩�븳�떎.
	 * @return
	 */
	public ResultSet rs() {
		return rs;
	}

	/**
	 * finally遺�遺꾩씠 �븘�땶怨녹뿉�꽌 �샇異쒗븯�뒗 �뵒�옄�씤 �뙣�꽩遺��뿉�꽌 �옄�썝�빐�젣瑜� �쐞�븳 �슜�룄濡� �솢�슜�븳�떎.
	 */
	public void tryClose() {
		pstmtClose();
	}

	/**
	 * 泥섎━�맂 媛��닔媛� �삁�긽怨� ��由닿꼍�슦 �삤瑜섎찓�떆吏� 臾몄옄�뿴�쓣 由ы꽩�빀�땲�떎.
	 * @param realCount �떎�젣 泥섎━ 媛��닔
	 * @param expectedCnt �삁�긽�릺�뒗 泥섎━ 媛��닔
	 * @return
	 */
	public String getWarning(int realCount, int expectedCnt) {
		String retVal;
		retVal=pstmt_str + "泥섎━寃곌낵媛� ["+expectedCnt+"]媛� �븘�떃�땲�떎.["+ realCount+"]";
		return retVal;
	}

}

package kr.co.cashq.safen_point;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Database �뿰寃� 媛앹껜
 * @author pgs
 *
 */
public class DBConn {
	public static String latest_warning = "";
	private static String CON_DRIVER = "com.mysql.jdbc.Driver";
	private static String CON_DOMAIN = "localhost";
	private static String CON_PORT = "3306";
	private static String CON_DBNM = "";
	private static String CON_USER = "";
	private static String CON_PWD = "";

	private static Connection dbCon = null;
	public static java.sql.Statement stmt = null;

	public DBConn() {
		dbConCheck();
	}

	/**
	 * DB媛� �뿰寃곕맂 �긽�깭�씤吏� 泥댄겕�븯�뿬 �뿰寃곕릺吏� �븡�븯�쑝硫� �뿰寃고븳�떎.
	 * �삉�븳 �뿰寃곗씠 �걡�뼱吏� 寃쎌슦 �떎�떆 �뿰寃곗쓣 �떆�룄�븳�떎.
	 * �븯吏�留� DB媛� �븞�삱�씪�삩 寃쎌슦 怨꾩냽 �뿰寃곗쓣 �떆�룄�븯吏�留� �뿰寃곗씠 �븞�릺誘�濡� �쑀�쓽�빐�빞 �븳�떎.
	 * @throws SQLException
	 */
	public static void dbConCheck() {
		try {
			if (dbCon == null) {
				
		        CON_DRIVER = Env.getInstance().CON_DRIVER;
		        CON_DOMAIN = Env.getInstance().CON_DOMAIN;
		        CON_PORT = Env.getInstance().CON_PORT;
		        CON_DBNM = Env.getInstance().CON_DBNM;
		        CON_USER = Env.getInstance().CON_USER;
		        CON_PWD = Env.getInstance().CON_PWD;
			 	
				Class.forName(CON_DRIVER);
				dbCon = DriverManager.getConnection(CON_STR(), CON_USER,
						CON_PWD);
				stmt = dbCon.createStatement();
			} else {
				if (dbCon.isClosed()) {
					Class.forName(CON_DRIVER);
					dbCon = DriverManager.getConnection(CON_STR(), CON_USER,
							CON_PWD);
					stmt = dbCon.createStatement();
				}
			}
		} catch (SQLException sqex) {
			//Utils.logput(sqex.getMessage());
			Utils.getLogger().warning(sqex.getMessage());
			DBConn.latest_warning = "ErrPOS013";
			dbCon = null;
		} catch (ClassNotFoundException sqex) {
			//Utils.logput(sqex.getMessage());
			Utils.getLogger().warning(sqex.getMessage());
			DBConn.latest_warning = "ErrPOS014";
			dbCon = null;	
		} catch (Exception e) {
			Utils.getLogger().warning(e.getMessage());
			Utils.getLogger().warning(Utils.stack(e));
			DBConn.latest_warning = "ErrPOS015";
			dbCon = null;
			throw new RuntimeException(DBConn.latest_warning);
		}
	}
	
	/**
	 * jdbc:mysql://localhost:3306/sktl
	 * �� 媛숈��떇�쑝濡� 由ы꽩�븳�떎.
	 * @return
	 */
	private static String CON_STR() {
		return "jdbc:mysql://" + CON_DOMAIN + ":" + CON_PORT + "/" + CON_DBNM;
	}

	/**
	 * DB 而⑤꽖�뀡媛앹껜瑜� 由ы꽩�븳�떎.
	 * @return
	 * @throws Exception 
	 */
	public static Connection getConnection() {
		dbConCheck();
		return dbCon;
	}

	/**
	 * �봽濡쒓렇�옩 �젙�긽醫낅즺�떆 �븳 踰덈쭔 close�븯湲� 沅뚯옣�븿.
	 */
	public static void close() {
		try {
			if (dbCon != null) {
				if (dbCon.isClosed() == false) {
					dbCon.close();
				}
			}
		} catch (SQLException e) {
			//Utils.logput(e.getMessage());
			Utils.getLogger().warning(e.getMessage());
			DBConn.latest_warning = "ErrPOS016";
		}
		catch (Exception e) {
			Utils.getLogger().warning(e.getMessage());
			Utils.getLogger().warning(Utils.stack(e));
			DBConn.latest_warning = "ErrPOS017";
		}
		dbCon = null;
	}

	/**
	 * safen_cmd_hist_YYYYMM�뀒�씠釉붿쓽 議댁옱�꽦�쓣 �뙋�떒�븳 �썑
	 * 議댁옱�븯吏� �븡�쑝硫� �뀒�씠釉붿쓣 �깮�꽦�븳�떎.
	 * �삉�븳 議댁옱�븯吏� �븡�쑝硫� �빐�떦 �썡�쓽 �씠由꾩쑝濡� �맂 濡쒓렇�뙆�씪�쓣 吏��슫�떎.
	 * @return
	 */
	public static String isExistTableYYYYMM() 
	{
		boolean isExistLogTable = false;
		String hist_table = "safen_cdr_" + Utils.getYYYYMM();
		isExistLogTable = isExistTable(hist_table);
		if (isExistLogTable == false)
		{
//			File f = new File(Utils.getLoggerFilePath());
//			
//			if (f.exists()) 
//			{
//				f.delete();// �뙆�씪�쓣 吏��슫�떎.
//			}
			
			try {
				stmt.execute("create table sktl." + hist_table
						+ " as select * from sktl.safen_cdr limit 0");//�뀒�씠釉붾쭔 �깮�꽦�븯怨� �뜲�씠�꽣�뒗 �삷湲곗� �븡�뒗�떎.
			} catch (SQLException e) {
				Utils.getLogger().warning(e.getMessage());
				DBConn.latest_warning = "ErrPOS018";
				e.printStackTrace();
			}
			catch (Exception e) {
				Utils.getLogger().warning(e.getMessage());
				Utils.getLogger().warning(Utils.stack(e));
				DBConn.latest_warning = "ErrPOS019";
			}
		}
		return hist_table;
	}

	/**
	 * �뀒�씠釉붿씠 議댁옱�븯�뒗吏� 議곗궗�븳 �썑 議곗궗�븯硫� true瑜� 由ы꽩�븳�떎.
	 * @param tname
	 * @return
	 */
	private static boolean isExistTable(String tname) {
		StringBuffer sb = new StringBuffer();

		boolean exi = false;

		sb.append("select exists(SELECT 1 FROM information_schema.tables WHERE table_schema= '").append(CON_DBNM).append("' AND table_name = ?) a");

		MyDataObject dao = new MyDataObject();
		try {
			dao.openPstmt(sb.toString());
			dao.pstmt().setString(1, tname);
			
			
			dao.setRs(dao.pstmt().executeQuery());
			if (dao.rs().next()) {
				exi = dao.rs().getInt(1) == 1;
			}			
		} catch (SQLException e) {
			e.printStackTrace();
			Utils.getLogger().warning(e.getMessage());
			DBConn.latest_warning = "ErrPOS020";
		}
		catch (Exception e) {
			Utils.getLogger().warning(e.getMessage());
			Utils.getLogger().warning(Utils.stack(e));
			DBConn.latest_warning = "ErrPOS021";
		}
		finally {
			dao.closePstmt();
		}

		return exi;
	}

}

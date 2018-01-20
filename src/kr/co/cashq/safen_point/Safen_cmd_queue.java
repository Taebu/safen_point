package kr.co.cashq.safen_point;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import java.io.InputStreamReader;
//import com.nostech.safen.SafeNo;

/**

 * safen_cmd_queue 테이블 관련 객체
 * @author 문태부.
 * 2018-01-06 (토) 10:48:05  
 *  @param : "crontab -e"
 *  vi /etc/cashwrite.sh
 *  curl -v http://cashq.co.kr/adm/0507_point_cash_write_dev2.php
 *  @param['url']="http://cashq.co.kr/adm/0507_point_cash_status.php";
 *  @param['param']= "board=0507_point&status2=1&".cr_post($chk_seq,'chk_seq',1);
 *  @since
 *  @date : 2018-01-06 오전 11:26:30
 *  @date : 2018-01-06 오전 11:46:07
 *  
 */
public class Safen_cmd_queue {
	
	/**
	 * safen_cmd_queue 테이블의 데이터를 처리하기 위한 주요한 처리를 수행한다.
	 * 
	 */
	public static void doMainProcess() {
		Connection con = DBConn.getConnection();
		/* 대리점 정보 */
		Map<String, String> agency_info   = new HashMap<String, String>();
		/* 상점 정보 */
		Map<String, String> store_info      = new HashMap<String, String>();
		/* 포인트 정보 */
		Map<String, String> point_info = new HashMap<String, String>();
		/* 메세지 정보 */
		Map<String, String> message_info = new HashMap<String, String>();
		
		Map<String, String> push_info = new HashMap<String, String>();
		String biz_code="";
		String token_id="";
		String mb_hp="";
		String appid="cashq";
		String eventcode="";
		String st_no="";
		String moddate="1970-01-01 12:00:00";
		String accdate="1970-01-01 12:00:00";
		String[] regex_rule;
		String[] regex_array;
		int eventcnt = 0;
		
		/* 포인트 갯수를 센다. */
		int point_count= 0;
		
		/* 핸드폰인지 여부 */
		boolean is_hp = false;
		
		/* GCM 전송 성공 여부 */
		boolean success_gcm = false;
		
		/* ATA 전송 성공 여부 */
		boolean success_ata = false;
		
		/* SMS 전송 성공 여부 */
		boolean success_sms = false;
		String regex_key="";
		String regex_value="";
		
		/* 포인트 이벤트 정보 */
		String[] point_event_info	= new String[7];
		/* 유저 이벤트 정보 */
		String[] user_event_info	= new String[3];
		

		if (con != null) {
			MyDataObject dao = new MyDataObject();
			MyDataObject dao2 = new MyDataObject();
			MyDataObject dao3 = new MyDataObject();
			MyDataObject dao4 = new MyDataObject();
			MyDataObject dao5 = new MyDataObject();

			StringBuilder sb = new StringBuilder();
			//StringBuilder sb_log = new StringBuilder();
			/* 
			 * 1. 아래 조건을 만족하는 `0507_point` 테이블을 조회한다.  
			 * 	조건 1) 오늘 입력된것.
			 * 조건 2) 상태가 0인것.
			 * 조건 3) "비즈코드"와 "이벤트코드"가 일치하는 것.
			 * distinct는 할필요 없었다.
			 */
			sb.append("SELECT ");
			sb.append(" *,substring_index(eventcode,'_',1) biz_ecode");
			sb.append(" FROM ");
			sb.append(" `0507_point` ");
			sb.append("WHERE date(insdate)=date(now()) ");
			sb.append(" and status='0' ");
			sb.append("having biz_code=biz_ecode;");
			
			try {
				dao.openPstmt(sb.toString());
				dao.setRs(dao.pstmt().executeQuery());
			  /* 2. 값이 있으면 */
			   while(dao.rs().next()) {
					SAFEN_CDR.heart_beat = 1;
					biz_code=dao.rs().getString("biz_code");
					mb_hp=dao.rs().getString("mb_hp");
					
					eventcode=dao.rs().getString("eventcode");
					
					/* 매장의 고유 번호를 불러 옵니다. */
					st_no=dao.rs().getString("store_seq");
				
					/* 3. 포인트를 사용가능으로 변경합니다. */
					update_point(dao.rs().getString("seq"));
					
					/* 4. 포인트 갯수를 센다.  */	
					point_count=get_point_count(mb_hp,eventcode);
					
					/* 5. 10개 이하 인가? */
					if(point_count<10)
					{
						/* 10개 이하 이면 */
						/* 13. 대리점 정보를 불러 옵니다.*/
						agency_info=get_agency(biz_code);
						
						/* "대리점 정보"에서 appid를 불러온다. */
						appid=agency_info.get("appid");
						System.out.println(appid);
						/* 12. 상점 정보를 불러 옵니다. */
						store_info=get_store(st_no);
						
						/* 템플릿 메세지를 가져옵니다. */
						message_info=get_bt_template(appid);
						System.out.println(message_info.get("gcm_message"));
						regex_rule=message_info.get("gcm_regex").split("&");
						
						Map<String, String> messageMap=new HashMap<String, String>();
						messageMap.put("store.name",store_info.get("name"));
						messageMap.put("store.tel",store_info.get("tel"));
						messageMap.put("agencyMember.point_items",getPointSet(agency_info.get("point_items")));
						messageMap.put("agencyMember.min_point",String.format("%,d", Integer.parseInt(agency_info.get("min_point")))+"원");
						
						
						/* 템플릿을 정해진 패턴대로 변경 합니다. 
						 * @param bt_content 템플릿 내용, 
						 * @param bt_regex 템플릿 패턴
						 * @param messageMap 템플릿 패턴을 바꿀 내용
						 * */
						/* gcm messages */
						String messages=chg_regexrule(message_info.get("gcm_message"),message_info.get("gcm_regex"), messageMap);
						System.out.println(messages);
						/* ata messages */
						regex_rule=message_info.get("ata_regex").split("&");
						messages=chg_regexrule(message_info.get("ata_message"),message_info.get("ata_regex"), messageMap);						
						System.out.println(messages);
						/* GCM 적립메세지 성공 여부 */
						success_gcm=set_gcm(messages,"title","01043391517","cashq");
						
						push_info.put("appid",appid);
						/* 전송 성공 여부에 따라 사이트 푸시 로그를 생성합니다.*/
						set_site_push_log(push_info);
					} /* if(point_count<10){...} */
					
					
					/* 4-5 */
					//String[] callArray = new String[] {"reviewpt","downpt"};
					String[] callArray = new String[] {"fivept","freedailypt","freeuserpt","freept"};
					String[] reviewArray = new String[] {"reviewpt","downpt"};
					/*
					daily_st_dt=Utils.getyyyymmdd();
					daily_ed_dt=Utils.add60day();
					//review_ed_dt=Utils.add90day();
					review_ed_dt=Utils.add60day();
					 */					
				} /* while(dao.rs().next()) {...} */
			} catch (SQLException e) {
				Utils.getLogger().warning(e.getMessage());
				DBConn.latest_warning = "ErrPOS031";
				e.printStackTrace();
			} catch (Exception e) {
				Utils.getLogger().warning(e.getMessage());
				Utils.getLogger().warning(Utils.stack(e));
				DBConn.latest_warning = "ErrPOS032";
			}finally {
				dao.closePstmt();
				dao2.closePstmt();
				dao3.closePstmt();
				dao4.closePstmt();
				dao5.closePstmt();
			}
			
			//콜로그 마스터 정보의 레코드 1개를 갱신을 시도한다.
			//Safen_master.doWark2();
		}
	}


	
	/**
	 * 사이트 푸시로그를 전송합니다.  
	 * 입력 : 푸시 인포.앱아이디, stype,biz_code, caller, called, wr_subject, wr_content result
	 */
	private static void set_site_push_log(Map<String, String> push_info) {
		// TODO Auto-generated method stub
		StringBuilder sb = new StringBuilder();
		MyDataObject dao = new MyDataObject();

		sb.append("insert into `site_push_log` set ");
		sb.append("appid=?,");
		sb.append("stype=?,");
		sb.append("biz_code=?,");
		sb.append("caller=?,");
		sb.append("called=?,");
		sb.append("wr_subject=?,");
		sb.append("wr_content=?,");
		sb.append("regdate=now(),");
		sb.append("result=?;");
		try {
			dao.openPstmt(sb.toString());
			dao.pstmt().setString(1, push_info.get("appid"));
			dao.pstmt().setString(2, push_info.get("stype"));
			dao.pstmt().setString(3, push_info.get("biz_code"));
			dao.pstmt().setString(4, push_info.get("caller"));
			dao.pstmt().setString(5, push_info.get("called"));
			dao.pstmt().setString(6, push_info.get("wr_subject"));
			dao.pstmt().setString(7, push_info.get("wr_content"));
			dao.pstmt().setString(8, push_info.get("result"));
			dao.pstmt().executeUpdate();
		} catch (SQLException e) {
			Utils.getLogger().warning(e.getMessage());
			Utils.getLogger().warning(Utils.stack(e));
			DBConn.latest_warning = "ErrPOS060";
		} catch (Exception e) {
			Utils.getLogger().warning(e.getMessage());
			Utils.getLogger().warning(Utils.stack(e));
			DBConn.latest_warning = "ErrPOS061";
		} finally {
			dao.closePstmt();
		}
	}



	/**
	 * @기능 : 
	 * 1. GCM을 전송한다. 
	 * 2. 변수에 성공 실패 여부를 반환한다.
	 * 3. 성공 실패로그를 기록한다.
	 * 4. GCM 성공 여부를 반환한다.
	 * 5. 함수를 종료한다.
	 *  
	 * @date : 2018-01-16 오후 8:21:00 
	 * @return
	 */
	private static boolean set_gcm(String message,String mms_title,String mb_hp,String appid) 
	{
		// TODO Auto-generated method stub
		/* 1. GCM을 전송한다. */
		
		/* 2. 변수에 성공 실패 여부를 반환한다. */
		/* 공통부분 */
		/*
		URL url = new URL("JSON 주소");
		InputStreamReader isr = new InputStreamReader(url.openConnection().getInputStream(), "UTF-8");
		JSONObject object = (JSONObject)JSONValue.parse(isr);

		출처: http://javastudy.tistory.com/80 [믿지마요 후회해요]
		*/
		Boolean is_gcm=false;
		String query="";
		URL targetURL;
		URLConnection urlConn;
		query+="&message="+message;
		query+="&title="+mms_title;
		query+="&mb_hp="+mb_hp;
		query+="&appid="+appid;
		
		try {
			targetURL = new URL("http://cashq.co.kr/adm/ajax/set_gcm_point.php");
			urlConn = targetURL.openConnection();
			HttpURLConnection cons = (HttpURLConnection) urlConn;
			// 헤더값을 설정한다.
			cons.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			cons.setRequestMethod("POST");
			
			//cons.getOutputStream().write("LOGIN".getBytes("UTF-8"));
			cons.setDoOutput(true);
			cons.setDoInput(true);
			cons.setUseCaches(false);
			cons.setDefaultUseCaches(false);
			
			/*
			PrintWriter out = new PrintWriter(cons.getOutputStream());
			out.close();*/
			//System.out.println(query);
			/* parameter setting */
			OutputStream opstrm=cons.getOutputStream();
			opstrm.write(query.getBytes());
			opstrm.flush();
			opstrm.close();

			String buffer = null;
			String bufferHtml="";
			BufferedReader in = new BufferedReader(new InputStreamReader(cons.getInputStream()));

			 while ((buffer = in.readLine()) != null) {
				 bufferHtml += buffer;
			}
			 JSONObject object = (JSONObject)JSONValue.parse(bufferHtml);
			 String success=object.get("success").toString();
			 
			int success_count=Integer.parseInt(success);
			 if(success_count>0){
				 is_gcm=true;
			 }
			//Utils.getLogger().info(bufferHtml);
			in.close();
			
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Utils.getLogger().warning(e.getMessage());
			Utils.getLogger().warning(Utils.stack(e));
			DBConn.latest_warning = "ErrPOS035";
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Utils.getLogger().warning(e.getMessage());
			Utils.getLogger().warning(Utils.stack(e));
			DBConn.latest_warning = "ErrPOS036";
		}
		return is_gcm;
	}


	/**
	 * @param appid
	 * @return
	 */
	private static Map<String, String> get_bt_template(String appid) {
		// TODO Auto-generated method stub
		Map<String, String> message=new HashMap<String, String>();

		
		StringBuilder sb = new StringBuilder();
		MyDataObject dao = new MyDataObject();
		sb.append("select * from cashq.bt_template where appid = ?");
		try {
			dao.openPstmt(sb.toString());
			dao.pstmt().setString(1, appid);
			dao.setRs (dao.pstmt().executeQuery());

			while(dao.rs().next()) 
			{
				message.put("bt_type",dao.rs().getString("bt_type"));
				System.out.println(dao.rs().getString("bt_type"));
				if(dao.rs().getString("bt_type").equals("gcm"))
				{
					message.put("gcm_title",dao.rs().getString("bt_name"));
					message.put("gcm_message",dao.rs().getString("bt_content"));
					message.put("gcm_regex",dao.rs().getString("bt_regex"));
				}
				else if(dao.rs().getString("bt_type").equals("ata"))
				{
					message.put("ata_title",dao.rs().getString("bt_name"));					
					message.put("ata_message",dao.rs().getString("bt_content"));
					message.put("ata_regex",dao.rs().getString("bt_regex"));
				}
				else if(dao.rs().getString("bt_type").equals("sms"))
				{
					message.put("sms_title",dao.rs().getString("bt_name"));
					message.put("sms_message",dao.rs().getString("bt_content"));
					message.put("sms_regex",dao.rs().getString("bt_regex"));
				}
			}			
		}catch (SQLException e) {
			Utils.getLogger().warning(e.getMessage());
			DBConn.latest_warning = "ErrPOS039";
			e.printStackTrace();
		}catch (Exception e) {
			Utils.getLogger().warning(e.getMessage());
			Utils.getLogger().warning(Utils.stack(e));
			DBConn.latest_warning = "ErrPOS040";
		}
		finally {
			dao.closePstmt();
		}
		return message;
	}


	/**
	 * @param string
	 * @return
	 */
	private static Map<String, String> get_store(String st_no) {
		// TODO Auto-generated method stub
		Map<String, String> store_info=new HashMap<String, String>();
		store_info.put("name","매장명");
		store_info.put("tel","05012341234");
		
		StringBuilder sb = new StringBuilder();
		MyDataObject dao = new MyDataObject();
		sb.append("select * from cashq.store where seq=? limit 1;");
		try {
			dao.openPstmt(sb.toString());
			dao.pstmt().setString(1, st_no);
			dao.setRs (dao.pstmt().executeQuery());

			if (dao.rs().next()) {
				store_info.put("name",dao.rs().getString("name"));
				store_info.put("tel",dao.rs().getString("tel"));
			}			
		}catch (SQLException e) {
			Utils.getLogger().warning(e.getMessage());
			DBConn.latest_warning = "ErrPOS039";
			e.printStackTrace();
		}catch (Exception e) {
			Utils.getLogger().warning(e.getMessage());
			Utils.getLogger().warning(Utils.stack(e));
			DBConn.latest_warning = "ErrPOS040";
		}
		finally {
			dao.closePstmt();
		}
		return store_info;
	}



	/**
	 * 대리점 정보를 biz_code로 불러 옵니다. 
	 * 불러온 정보는 hashMap  에 적재하여 리턴합니다.
	 * @param string
	 * @return HashMap
	 */
	private static Map<String, String> get_agency(String biz_code) {
		// TODO Auto-generated method stub
		Map<String, String> agency=new HashMap<String, String>();
		agency.put("appid","cashq");
		agency.put("agency_name","대리점명");
		agency.put("pointset","off");
		agency.put("point_items","5_10000&10_20000");
		agency.put("min_point","12000");
		
		
		StringBuilder sb = new StringBuilder();
		MyDataObject dao = new MyDataObject();
		sb.append("select * from cashq.agencyMember where biz_code = ?");
		try {
			dao.openPstmt(sb.toString());
			dao.pstmt().setString(1, biz_code);
			dao.setRs (dao.pstmt().executeQuery());

			if (dao.rs().next()) {
				agency.put("appid",dao.rs().getString("appid"));
				agency.put("agency_name",dao.rs().getString("agency_name"));
				if(dao.rs().getString("pointset").equals("on"))
				{
					agency.put("pointset","on");
					agency.put("point_items",dao.rs().getString("point_items"));
				}
				agency.put("min_point",dao.rs().getString("min_point"));
			}			
		}catch (SQLException e) {
			Utils.getLogger().warning(e.getMessage());
			DBConn.latest_warning = "ErrPOS039";
			e.printStackTrace();
		}catch (Exception e) {
			Utils.getLogger().warning(e.getMessage());
			Utils.getLogger().warning(Utils.stack(e));
			DBConn.latest_warning = "ErrPOS040";
		}
		finally {
			dao.closePstmt();
		}

		return agency;
	}


	/**
	 * 해당 상점을 사용 가능으로 변경한다.
	 * @param safen0504
	 * @param safen_in010
	 * @param mapping_option
	 * @param retCode
	 */
	private static void update_point(String seq) {

		MyDataObject dao = new MyDataObject();
		StringBuilder sb = new StringBuilder();
		
		try {
				sb.append("update cashq.0507_point set status='1' where seq=?");
				dao.openPstmt(sb.toString());
				dao.pstmt().setString(1, seq);

				int cnt = dao.pstmt().executeUpdate();
				if(cnt!=1) {
					Utils.getLogger().warning(dao.getWarning(cnt,1));
					DBConn.latest_warning = "ErrPOS034";
				}
				dao.tryClose();
		} catch (SQLException e) {
			Utils.getLogger().warning(e.getMessage());
			DBConn.latest_warning = "ErrPOS037";
			e.printStackTrace();
		}
		catch (Exception e) {
			Utils.getLogger().warning(e.getMessage());
			DBConn.latest_warning = "ErrPOS038";
			Utils.getLogger().warning(Utils.stack(e));
		}
		finally {
			dao.closePstmt();
		}
	}

	/**
	 * 마스터 테이블에서 안심번호에 따른 착신번호를 리턴한다.
	 * @param safen0504
	 * @return
	 */
	private static String getSafenInBySafen(String safen0504) {
		String retVal = "";
		StringBuilder sb = new StringBuilder();

		MyDataObject dao = new MyDataObject();
		sb.append("select safen_in from safen_master where safen = ?");
		try {
			dao.openPstmt(sb.toString());
			dao.pstmt().setString(1, safen0504);
			
			dao.setRs (dao.pstmt().executeQuery());

			if (dao.rs().next()) {
				retVal = dao.rs().getString("safen_in");
			}			
		} catch (SQLException e) {
			Utils.getLogger().warning(e.getMessage());
			DBConn.latest_warning = "ErrPOS039";
			e.printStackTrace();
		}
		catch (Exception e) {
			Utils.getLogger().warning(e.getMessage());
			Utils.getLogger().warning(Utils.stack(e));
			DBConn.latest_warning = "ErrPOS040";
		}
		finally {
			dao.closePstmt();
		}

		return retVal;
	}


	
	/**
	 * TB_CALL_LOG에 추가한다.
	 * @param String status_cd	콜로그 상태 코드
	 * @param String conn_sdt	콜로그 시작시간
	 * @param String conn_edt	콜로그 종료시간
	 * @param String service_sdt	콜로그 제공시간
	 * @param String safen	안심번호
	 * @param String safen_in
	 * @param String safen_out
	 * @param String calllog_rec_file
	 * @return
	 */
	public static int set_TB_CALL_LOG(String status_cd, 
		String conn_sdt, String conn_edt,String service_sdt,
		String safen,String safen_in,String safen_out,
		String calllog_rec_file) 
	{
		boolean retVal = false;
		int last_id = 0;
		StringBuilder sb = new StringBuilder();
		StringBuilder sb2 = new StringBuilder();
		MyDataObject dao = new MyDataObject();
		MyDataObject dao2 = new MyDataObject();
		sb.append("INSERT INTO `cashq`.`TB_CALL_LOG` SET ");
		sb.append("SVC_ID='81',");
		sb.append("START_DT=?,");
		sb.append("END_DT=?,");
		sb.append("CALLED_HANGUP_DT=?,");
		sb.append("VIRTUAL_NUM=?,");
		sb.append("CALLED_NUM=?,");
		sb.append("CALLER_NUM=?,");
		sb.append("userfield=?,");
		sb.append("REASON_CD=?");

		/*
		sb.append("insert into cashq.site_push_log set "
				+ "stype='SMS', biz_code='ANP', caller=?, called=?, wr_subject=?, regdate=now(), result=''");
		*/
		try {
			dao.openPstmt(sb.toString());

			//Utils.getLogger().warning(sb.toString());

			if ("1".equals(status_cd)) {
			/* GCM LOG 발생*/
			set_stgcm(safen, safen_in);

			/* 통화성공 */
			dao.pstmt().setString(1, conn_sdt);
			dao.pstmt().setString(2, conn_edt);
			dao.pstmt().setString(3, service_sdt);
			dao.pstmt().setString(4, safen);
			dao.pstmt().setString(5, safen_in);
			dao.pstmt().setString(6, safen_out);
			dao.pstmt().setString(7, calllog_rec_file);
			dao.pstmt().setString(8, status_cd);
			}else{
			/* 통화실패*/
			dao.pstmt().setString(1, conn_sdt);
			dao.pstmt().setString(2, conn_edt);
			dao.pstmt().setString(3, "1970-01-01 09:00:00");
			dao.pstmt().setString(4, safen);
			dao.pstmt().setString(5, safen_in);
			dao.pstmt().setString(6, safen_out);
			dao.pstmt().setString(7, calllog_rec_file);
			dao.pstmt().setString(8, status_cd);
			}

			dao.pstmt().executeUpdate();


			sb2.append("select LAST_INSERT_ID() last_id;");
			dao2.openPstmt(sb2.toString());
			dao2.setRs(dao2.pstmt().executeQuery());
			
			if (dao2.rs().next()) {
				last_id = dao2.rs().getInt("last_id");
			}
			
		} catch (SQLException e) {
			Utils.getLogger().warning(e.getMessage());
			Utils.getLogger().warning(Utils.stack(e));
			DBConn.latest_warning = "ErrPOS060";
			/* grant로 해당 사용자에 대한 권한을 주어 문제 해결이 가능하다.
			grant all privileges on cashq.site_push_log to sktl@"%" identified by 'sktl@9495';
			grant all privileges on cashq.site_push_log to sktl@"localhost" identified by 'sktl@9495';
			 */
			 
		} catch (Exception e) {
			Utils.getLogger().warning(e.getMessage());
			Utils.getLogger().warning(Utils.stack(e));
			DBConn.latest_warning = "ErrPOS061";
		} finally {
			dao.closePstmt();
			dao2.closePstmt();
		}

		return last_id;
	}


	/**
	 * 0507_point에 추가한다.
	* @param  mb_hp, 
	* @param  store_name, 
	* @param  hangup_time,
	* @param  biz_code,
	* @param  call_hangup_dt,
	* @param  pev_st_dt,
	* @param  pev_ed_dt,
	* @param  eventcode,
	* @param  mb_id,
	* @param  certi_code,
	* @param  st_dt,
	* @param  ed_dt,
	* @param  store_seq,
	* @param  tcl_seq,
	* @param  moddate,
	* @param  accdate,
	* @param  ed_type,
	* @param  type
	* @param  tel
	* @param  pre_pay
	* @param  pt_stat
	 * @return void
	 */
	public static void set_0507_point(
		String mb_hp, 
		String store_name, 
		String hangup_time,
		String biz_code,
		String call_hangup_dt,
		String pev_st_dt,
		String pev_ed_dt,
		String eventcode,
		String mb_id,
		String certi_code,
		String st_dt,
		String ed_dt,
		String store_seq,
		String tcl_seq,
		String moddate,
		String accdate,
		String ed_type,
		String type,
		String tel,
		String pre_pay,
		String pt_stat
	) 
	{

		StringBuilder sb = new StringBuilder();
		MyDataObject dao = new MyDataObject();
		sb.append("INSERT INTO `cashq`.`0507_point` SET ");
		sb.append("mb_hp=?,");
		sb.append("store_name=?,");
		sb.append("point='2000',");
		sb.append("hangup_time=?,");
		sb.append("biz_code=?,");
		sb.append("call_hangup_dt=?,");
		sb.append("ev_st_dt=?,");
		sb.append("ev_ed_dt=?,");
		sb.append("eventcode=?,");
		sb.append("mb_id=?,");
		sb.append("certi_code=?,");
		sb.append("insdate=now(),");
		sb.append("st_dt=?,");
		sb.append("ed_dt=?,");
		sb.append("tcl_seq=?,");
		sb.append("store_seq=?,");
		sb.append("moddate=?,");
		sb.append("accdate=?, ");
		sb.append("ed_type=?, ");
		sb.append("type=?, ");
		sb.append("tel=?, ");
		sb.append("pre_pay=?, ");
		sb.append("pt_stat=? ");


/*
*************************** 1. row ***************************
           seq: 945834
         mb_hp: 01077430009
         point: 2000
    store_name: 본사포인트 테스트
          type:
   hangup_time: 18
call_hangup_dt: 2016-07-22 18:13:16
      biz_code: testsub
      ev_st_dt: 2014-01-01
      ev_ed_dt: 2020-08-19
     eventcode: testsub_1
         mb_id:
    certi_code:
       insdate: 2016-07-22 18:15:20
         st_dt: 2016-07-22 18:13:05
         ed_dt: 2016-07-22 18:13:34
       tcl_seq: 3037187
     store_seq: 6797
        status: 1
       moddate: 2016-08-18 10:16:04
       accdate: 0000-00-00 00:00:00
     cashq_seq: NULL
          memo: NULL
           tel: NULL
      cnt_memo: NULL
       pre_pay: sl
       pt_stat: pt5
       ed_type: fivept
1 row in set (0.00 sec)


*/
		try {
			dao.openPstmt(sb.toString());
			dao.pstmt().setString(1, mb_hp);
			dao.pstmt().setString(2, store_name);
			dao.pstmt().setString(3, hangup_time);
			dao.pstmt().setString(4, biz_code);
			dao.pstmt().setString(5, call_hangup_dt);
			dao.pstmt().setString(6, pev_st_dt);
			dao.pstmt().setString(7, pev_ed_dt);
			dao.pstmt().setString(8, eventcode);
			dao.pstmt().setString(9, mb_id);
			dao.pstmt().setString(10, certi_code);
			dao.pstmt().setString(11, st_dt);
			dao.pstmt().setString(12, ed_dt);
			dao.pstmt().setString(13, tcl_seq);
			dao.pstmt().setString(14, store_seq);
			dao.pstmt().setString(15, moddate);
			dao.pstmt().setString(16, accdate);
			dao.pstmt().setString(17, ed_type);
			dao.pstmt().setString(18, type);
			dao.pstmt().setString(19, tel);
			dao.pstmt().setString(20, pre_pay);
			dao.pstmt().setString(21, pt_stat);

			//dao.pstmt().executeQuery();
			dao.pstmt().executeUpdate();
		} catch (SQLException e) {
			Utils.getLogger().warning(e.getMessage());
			Utils.getLogger().warning(Utils.stack(e));
			DBConn.latest_warning = "ErrPOS060";
			/* grant로 해당 사용자에 대한 권한을 주어 문제 해결이 가능하다.
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
	}




	
	/**
	 * cdr 에 추가한다.
	 * @param String status_cd	콜로그 상태 코드
	 * @param String conn_sdt	콜로그 시작시간
	 * @param String conn_edt	콜로그 종료시간
	 * @param String service_sdt	콜로그 제공시간
	 * @param String safen	
	 * @param String safen_in
	 * @param String safen_out
	 * @param String calllog_rec_file
	 * @return
	 */
	public static int set_cdr(String status_cd, 
		String conn_sdt, String conn_edt,String service_sdt,
		String safen,String safen_in,String safen_out,
		String calllog_rec_file) 
	{
		boolean retVal = false;
		int last_id = 0;
		StringBuilder sb = new StringBuilder();
		MyDataObject dao = new MyDataObject();
		sb.append("INSERT INTO `asteriskcdrdb`.`cdr` SET ");
		sb.append("calldate=?,");
		sb.append("src=?,");
		sb.append("dst=?,");
		sb.append("duration=?,");
		sb.append("billsec=?,");
		sb.append("accountcode=?,");
		sb.append("uniqueid=?,");
		sb.append("userfield=?;");
		//Utils.getLogger().warning(sb.toString());


		/*
		sb.append("insert into cashq.site_push_log set "
				+ "stype='SMS', biz_code='ANP', caller=?, called=?, wr_subject=?, regdate=now(), result=''");
		*/
		try {
			dao.openPstmt(sb.toString());

			dao.pstmt().setString(1, dao.rs().getString("conn_sdt"));
			dao.pstmt().setString(2, dao.rs().getString("safen_in"));
			dao.pstmt().setString(3, dao.rs().getString("safen"));
			dao.pstmt().setString(4, dao.rs().getString("conn_sec"));
			dao.pstmt().setString(5, dao.rs().getString("service_sec"));
			dao.pstmt().setString(6, dao.rs().getString("safen_out"));
			dao.pstmt().setString(7, dao.rs().getString("unique_id"));
			dao.pstmt().setString(8, dao.rs().getString("rec_file_cd"));

			dao.pstmt().executeUpdate();
			retVal = true;
		} catch (SQLException e) {
			Utils.getLogger().warning(e.getMessage());
			Utils.getLogger().warning(Utils.stack(e));
			DBConn.latest_warning = "ErrPOS060";
			/* grant로 해당 사용자에 대한 권한을 주어 문제 해결이 가능하다.
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
		//return retVal;
		return last_id;
	}
	
	
	/**
	 * 상점 콜로그로 갱신한다.  retCode가 "0000"(성공)인경우에는 status_cd값을 "s"로 그렇지 않은 경우에는 "e"로 셋팅한 후 큐를
	 * 지우고 로그로 보낸다. 
	 * @param safen_in
	 * @param retCode
	 */
	private static void update_stcall(String safen) {

		MyDataObject dao = new MyDataObject();
		
		try {
			StringBuilder sb = new StringBuilder();
			sb.append("UPDATE `cashq`.`store` SET callcnt=callcnt+1 WHERE tel=?");

			// status_cd 컬럼을 "i"<진행중>상태로 바꾼다.
			dao.openPstmt(sb.toString());

			dao.pstmt().setString(1, safen);

			int cnt = dao.pstmt().executeUpdate();

			if(cnt!=1) {
				Utils.getLogger().warning(dao.getWarning(cnt,1));
				DBConn.latest_warning = "ErrPOS034";
			}

			dao.tryClose();


		} catch (SQLException e) {
			Utils.getLogger().warning(e.getMessage());
			DBConn.latest_warning = "ErrPOS037";
			e.printStackTrace();
		}
		catch (Exception e) {
			Utils.getLogger().warning(e.getMessage());
			DBConn.latest_warning = "ErrPOS038";
			Utils.getLogger().warning(Utils.stack(e));
		}
		finally {
			dao.closePstmt();
		}
	}


	/**
	 * 캐시큐 상점에서 안심번호에 따른 상점 정보를 리턴한다.
	 * @param safen
	 * @return
	 */
	private static String[] getStoreInfo(String safen) {
		String[] s = new String[5];
		StringBuilder sb = new StringBuilder();

		MyDataObject dao = new MyDataObject();
		sb.append("select name,pre_pay,biz_code,seq,type from `cashq`.`store` where tel= ?");
		try {
			dao.openPstmt(sb.toString());
			dao.pstmt().setString(1, safen);
			
			dao.setRs (dao.pstmt().executeQuery());

			if (dao.rs().next()) {
				s[0] = dao.rs().getString("name");
				s[1] = dao.rs().getString("pre_pay");
				s[2] = dao.rs().getString("biz_code");
				s[3] = dao.rs().getString("seq");
				s[4] = dao.rs().getString("type");
			}			
		} catch (SQLException e) {
			Utils.getLogger().warning(e.getMessage());
			DBConn.latest_warning = "ErrPOS039";
			e.printStackTrace();
		}
		catch (Exception e) {
			Utils.getLogger().warning(e.getMessage());
			Utils.getLogger().warning(Utils.stack(e));
			DBConn.latest_warning = "ErrPOS040";
		}
		finally {
			dao.closePstmt();
		}

		return s;
	}

	/**
	 * 비즈코드에 따른 이벤트 코드 정보를 리턴한다.
	 * @param biz_code
	 * @return
	 */
	private static String[] getEventCodeInfo(String biz_code) {
		String[] s = new String[7];
		StringBuilder sb = new StringBuilder();

		MyDataObject dao = new MyDataObject();
		
		sb.append("SELECT ");
		sb.append("ev_st_dt,");
		sb.append("ev_ed_dt,");
		sb.append("eventcode,");
		sb.append("cash,");
		sb.append("pt_day_cnt,");
		sb.append("pt_event_cnt,");
		sb.append("ed_type ");
		sb.append("FROM `cashq`.`point_event_dt` ");
		sb.append("WHERE biz_code=? and used='1' ");
		sb.append("order by seq desc limit 1;");

		try {
			dao.openPstmt(sb.toString());
			dao.pstmt().setString(1, biz_code);
			
			dao.setRs (dao.pstmt().executeQuery());

			if (dao.rs().next()) {
				s[0] = dao.rs().getString("ev_st_dt");
				s[1] = dao.rs().getString("ev_ed_dt");
				s[2] = dao.rs().getString("eventcode");
				s[3] = dao.rs().getString("cash");
				s[4] = dao.rs().getString("pt_day_cnt");
				s[5] = dao.rs().getString("pt_event_cnt");
				s[6] = dao.rs().getString("ed_type");
			}			
		} catch (SQLException e) {
			Utils.getLogger().warning(e.getMessage());
			DBConn.latest_warning = "ErrPOS039";
			e.printStackTrace();
		}
		catch (Exception e) {
			Utils.getLogger().warning(e.getMessage());
			Utils.getLogger().warning(Utils.stack(e));
			DBConn.latest_warning = "ErrPOS040";
		}
		finally {
			dao.closePstmt();
		}

		return s;
	}
	
	/**
	* is_realcode
	*/
	private static boolean is_realcode(String eventcode,String biz_code) {
		boolean is_code=false;

		String[] explode=eventcode.split("\\_");

		is_code=explode[0].equals(biz_code);
		return is_code;
	}

	/**
	* int get_eventcnt
	* @param mb_hp
	* @param eventcode
	* @return int
	*/
	private static int get_point_count(String mb_hp, String eventcode){
		int retVal = 0;
		StringBuilder sb = new StringBuilder();
		MyDataObject dao = new MyDataObject();
		sb.append("SELECT count(*) cnt FROM `cashq`.`0507_point` ");
		sb.append("WHERE mb_hp=? ");
		sb.append("AND eventcode=? ");
		sb.append("AND status in ('1','2','3','4');");

		try {
			dao.openPstmt(sb.toString());
			dao.pstmt().setString(1, mb_hp);
			dao.pstmt().setString(2, eventcode);
			
			dao.setRs (dao.pstmt().executeQuery());

			if (dao.rs().next()) {
				retVal = dao.rs().getInt("cnt");
			}			
		} catch (SQLException e) {
			Utils.getLogger().warning(e.getMessage());
			DBConn.latest_warning = "ErrPOS039";
			e.printStackTrace();
		}
		catch (Exception e) {
			Utils.getLogger().warning(e.getMessage());
			Utils.getLogger().warning(Utils.stack(e));
			DBConn.latest_warning = "ErrPOS040";
		}
		finally {
			dao.closePstmt();
		}

		return retVal;
	}
	/**
	* int get_daycnt
	* @param mb_hp
	* @return int
	*/
	private static int get_daycnt(String mb_hp,String ed_type){
		int retVal = 0;
		StringBuilder sb = new StringBuilder();

		MyDataObject dao = new MyDataObject();
		sb.append("SELECT count(*) cnt FROM `cashq`.`0507_point` ");
		sb.append("WHERE mb_hp=? ");
		sb.append("AND date(st_dt)=date(now()) ");
		sb.append("AND status in ('1','2','3','4') ");
		if(ed_type.equals("callpt")){
			sb.append(" AND ed_type not in ('downpt','reviewpt') ");
		}else if(ed_type.equals("reviewpt")){
			sb.append("  AND ed_type='reviewpt' ");
		}else if(ed_type.equals("downpt")){
			sb.append(" AND ed_type='downpt' ");
		} 
		
		try {
			dao.openPstmt(sb.toString());
			
			//Utils.getLogger().warning(sb.toString());
			
			dao.pstmt().setString(1, mb_hp);
			
			
			dao.setRs (dao.pstmt().executeQuery());

			if (dao.rs().next()) {
				retVal = dao.rs().getInt("cnt");
			}			
		} catch (SQLException e) {
			Utils.getLogger().warning(e.getMessage());
			DBConn.latest_warning = "ErrPOS039";
			e.printStackTrace();
		}
		catch (Exception e) {
			Utils.getLogger().warning(e.getMessage());
			Utils.getLogger().warning(Utils.stack(e));
			DBConn.latest_warning = "ErrPOS040";
		}
		finally {
			dao.closePstmt();
		}

		return retVal;
	}

	/**
	* boolean is_hp
	* @param hp
	* @return boolean
	*/
	private static boolean is_hp(String hp){
		boolean retVal=false;
			if(hp.length()>=2){
				retVal=hp.substring(0,2).equals("01");
			}
		return retVal;
	}
	
	/**
	* boolean is_freedailypt
	* @param ed_type
	* @return boolean
	*/
	private static boolean is_freedailypt(String ed_type){
		boolean retVal=false;
		if(ed_type!=null){
			if(ed_type.length()>=11){
				retVal = ed_type.substring(0,11).equals("freedailypt");
			}
		}
		return retVal;
	}


	/**
	* boolean is_freeuserpt
	* @param ed_type
	* @return boolean
	*/
	private static boolean is_freeuserpt(String ed_type){
		boolean retVal=false;
		if(ed_type!=null){
			if(ed_type.length()>=10){
				retVal = ed_type.substring(0,10).equals("freeuserpt");
			}
		}
		return retVal;
	}

	/**
	* boolean is_fivept
	* @param ed_type
	* @return boolean
	*/
	private static boolean is_fivept(String ed_type){
		boolean retVal=false;
		if(ed_type!=null){
			if(ed_type.length()>=6){
				retVal = ed_type.substring(0,6).equals("fivept");
			}else{
				retVal = ed_type.equals("");
			}
		}
		return retVal;
	}
	/**
	* boolean is_fivept
	* @param ed_type
	* @return boolean
	*/
	private static boolean is_downpt(String ed_type){
		boolean retVal=false;
		if(ed_type!=null){
			if(ed_type.length()>=6){
				retVal = ed_type.substring(0,6).equals("downpt");
			}else{
				retVal = ed_type.equals("");
			}
		}
		return retVal;
	}
	/**
	* boolean is_fivept
	* @param ed_type
	* @return boolean
	*/
	private static boolean is_reviewpt(String ed_type){
		boolean retVal=false;
		if(ed_type!=null){
			if(ed_type.length()>=8){
				retVal = ed_type.substring(0,8).equals("reviewpt");
			}else{
				retVal = ed_type.equals("");
			}
		}
		return retVal;
	}

	/**
	* boolean is_salept
	* @param ed_type
	* @return boolean
	*/
	private static boolean is_salept(String ed_type){
		boolean retVal=false;
		if(ed_type!=null){
			if(ed_type.length()>=5){
				retVal = ed_type.substring(0,6).equals("salept");
			}else{
				retVal = ed_type.equals("");
			}
		}
		return retVal;
	}
	/**
	* int get_user_event_index
	* @param mb_hp
	* @param biz_code
	* @return int
	*/
	private static int get_user_event_index(String mb_hp,String biz_code){
		int retVal = 0;
		StringBuilder sb = new StringBuilder();

		MyDataObject dao = new MyDataObject();
		sb.append("SELECT count(*) cnt FROM `cashq`.`user_event_dt` ");
		sb.append("WHERE biz_code=? ");
		sb.append("and mb_hp=? ");
		sb.append("order by seq desc limit 1");
		
		try {
			dao.openPstmt(sb.toString());
			dao.pstmt().setString(1, biz_code);
			dao.pstmt().setString(2, mb_hp);
			
			dao.setRs (dao.pstmt().executeQuery());

			if (dao.rs().next()) {
				retVal = dao.rs().getInt("cnt");
			}			
		} catch (SQLException e) {
			Utils.getLogger().warning(e.getMessage());
			DBConn.latest_warning = "ErrPOS039";
			e.printStackTrace();
		}
		catch (Exception e) {
			Utils.getLogger().warning(e.getMessage());
			Utils.getLogger().warning(Utils.stack(e));
			DBConn.latest_warning = "ErrPOS040";
		}
		finally {
			dao.closePstmt();
		}

		return retVal;
	}


	/**
	 * set_user_event_dt에 추가한다.
	 * @param String biz_code	콜로그 상태 코드
	 * @param String mb_hp	콜로그 시작시간
	 * @param String conn_edt	콜로그 종료시간
	 * @param String service_sdt	콜로그 제공시간
	 * @param String safen	안심번호
	 * @param String safen_in	링크된번호
	 * @param String safen_out	소비자 번호
	 * @param String calllog_rec_file	
	 * @return
	 */
	public static int set_user_event_dt(String biz_code, 
		String mb_hp, 
		String daily_st_dt,
		String daily_ed_dt,
		String eventcode) 
	{

		boolean retVal = false;
		int last_id = 0;
		StringBuilder sb = new StringBuilder();
		MyDataObject dao = new MyDataObject();
		sb.append("INSERT INTO `cashq`.`user_event_dt` SET ");
		sb.append("biz_code=?,");
		sb.append("mb_hp=?,");
		sb.append("ev_st_dt=?,");
		sb.append("ev_ed_dt=?,");
		sb.append("eventcode=?,");
		sb.append("insdate=now()");

		/*
		sb.append("insert into cashq.site_push_log set "
				+ "stype='SMS', biz_code='ANP', caller=?, called=?, wr_subject=?, regdate=now(), result=''");
		*/
		try {
			dao.openPstmt(sb.toString());

			dao.pstmt().setString(1, biz_code);
			dao.pstmt().setString(2, mb_hp);
			dao.pstmt().setString(3, daily_st_dt);
			dao.pstmt().setString(4, daily_ed_dt);
			dao.pstmt().setString(5, eventcode);

			//dao.pstmt().executeQuery();
			dao.pstmt().executeUpdate();
		} catch (SQLException e) {
			Utils.getLogger().warning(e.getMessage());
			Utils.getLogger().warning(Utils.stack(e));
			DBConn.latest_warning = "ErrPOS060";
			/* grant로 해당 사용자에 대한 권한을 주어 문제 해결이 가능하다.
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
		return last_id;
	}
	/**
	* get_userevent(biz_code, mb_hp)
	* @param biz_code
	* @param mb_hp
	* @return array
	*/
	private static String[] get_userevent(String biz_code, String mb_hp) {
		String[] s = new String[3];
		StringBuilder sb = new StringBuilder();

		MyDataObject dao = new MyDataObject();
		sb.append("SELECT  ");
		sb.append("eventcode,");
		sb.append("ev_ed_dt,");
		sb.append("ev_st_dt ");
		sb.append("FROM `cashq`.`user_event_dt` ");
		sb.append("WHERE biz_code=? ");
		sb.append("AND mb_hp=? ");
		sb.append("ORDER BY seq desc limit 1;");

		try {
			dao.openPstmt(sb.toString());
			dao.pstmt().setString(1, biz_code);
			dao.pstmt().setString(2, mb_hp);
			
			dao.setRs (dao.pstmt().executeQuery());

			if (dao.rs().next()) {
				s[0] = dao.rs().getString("ev_st_dt");
				s[1] = dao.rs().getString("ev_ed_dt");
				s[2] = dao.rs().getString("eventcode");
			}			
		} catch (SQLException e) {
			Utils.getLogger().warning(e.getMessage());
			DBConn.latest_warning = "ErrPOS039";
			e.printStackTrace();
		}
		catch (Exception e) {
			Utils.getLogger().warning(e.getMessage());
			Utils.getLogger().warning(Utils.stack(e));
			DBConn.latest_warning = "ErrPOS040";
		}
		finally {
			dao.closePstmt();
		}

		return s;
	}

	
	/**
	 * app_toeken 아이디의 지역 정보를 갱신해서 넣는다.

	 * @param biz_code
	 * @param mb_hp
	 * @return void
	 */
	private static void set_app_token_id(String biz_code,String mb_hp) {

		MyDataObject dao = new MyDataObject();
		
		try {
			StringBuilder sb = new StringBuilder();
			sb.append("UPDATE `cashq`.`app_token_id` SET biz_code=? where tel=?");

			dao.openPstmt(sb.toString());

			dao.pstmt().setString(1, biz_code);
			dao.pstmt().setString(2, mb_hp);

			int cnt = dao.pstmt().executeUpdate();
			if(cnt!=1) {
				Utils.getLogger().warning(dao.getWarning(cnt,1));
				DBConn.latest_warning = "ErrPOS034";
			}

			dao.tryClose();


		} catch (SQLException e) {
			Utils.getLogger().warning(e.getMessage());
			DBConn.latest_warning = "ErrPOS037";
			e.printStackTrace();
		}
		catch (Exception e) {
			Utils.getLogger().warning(e.getMessage());
			DBConn.latest_warning = "ErrPOS038";
			Utils.getLogger().warning(Utils.stack(e));
		}
		finally {
			dao.closePstmt();
		}
	}


	/**
	 * set_stgcm 아이디의 지역 정보를 갱신해서 넣는다.
	 * set_stgcm(safen, safen_in);
	 * @param safen
	 * @param safen_in
	 * @return void
	 */
	private static void set_stgcm(String safen,String safen_in) 
	{

		MyDataObject dao = new MyDataObject();
		
		try {
			StringBuilder sb = new StringBuilder();
			sb.append("INSERT INTO cashq.st_gcm SET VIRTUAL_NUM=?,CALLED_NUM=?,insdate=now();");

			dao.openPstmt(sb.toString());

			dao.pstmt().setString(1, safen);
			dao.pstmt().setString(2, safen_in);

			int cnt = dao.pstmt().executeUpdate();
			if(cnt!=1) {
				Utils.getLogger().warning(dao.getWarning(cnt,1));
				DBConn.latest_warning = "ErrPOS034";
			}

			dao.tryClose();


		} catch (SQLException e) {
			Utils.getLogger().warning(e.getMessage());
			DBConn.latest_warning = "ErrPOS037";
			e.printStackTrace();
		}
		catch (Exception e) {
			Utils.getLogger().warning(e.getMessage());
			DBConn.latest_warning = "ErrPOS038";
			Utils.getLogger().warning(Utils.stack(e));
		}
		finally {
			dao.closePstmt();
		}
	}


	/**
	* boolean is_point
	* @param pre_pay
	* @return boolean
	*/
	private static boolean is_point(String pre_pay){
		boolean retVal=false;
		
		if(pre_pay!=null){
			retVal = pre_pay.equals("gl")||pre_pay.equals("sl")||pre_pay.equals("on")||pre_pay.equals("br");
		}
		
		return retVal;
	}

	/**
	* boolean is_datepoint
	* @param ev_st_dt
	* @param ev_ed_dt
	* @return boolean
	*/
	private static boolean is_datepoint(String ev_st_dt,String ev_ed_dt){
		boolean is_date=false;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		try{

		/* null check 하나라도 널이면 에러 */
		if(ev_st_dt==null||ev_ed_dt==null){

		}else{
			Date todayDate = new Date();
			
			Date historyDate = sdf.parse(ev_st_dt);
			Date futureDate = sdf.parse(ev_ed_dt);

			/* 기간 이내 */
			is_date=todayDate.after(historyDate)&&todayDate.before(futureDate);
			
			/* 이벤트 종료 시간과 같은 날 */
			if(sdf.format(todayDate).equals(sdf.format(futureDate))){
				is_date=true;
			}		
		}
		
		}catch(ParseException e){
		
		}
		
		return is_date;
	}


	// yyyy-MM-dd HH:mm:ss.0 을 yyyy-MM-dd HH:mm:ss날짜로 변경
	public static String chgDatetime(String str)
	{
		String retVal="";

		try{
		String source = str; 
		SimpleDateFormat simpleDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date historyDate = simpleDate.parse(str);
		retVal=simpleDate.format(historyDate);
		}catch(ParseException e){
		}
		return retVal;
	}


    /**
     * chkValue
	 *  데이터 유효성 null 체크에 대한 값을 "" 로 리턴한다.
     * @param str
     * @return String
     */
	public static String chkValue(String str)
	{
		String retVal="";

		try{
				retVal=str==null?"":str;
		}catch(NullPointerException e){
			
		}
		return retVal;
	}
	
	public static String chk_pt5(String str)
	{
		String retVal="pt5";
		String[] ed_type= new String[] {"freept","freedailypt","freeuserpt"};

		if(Arrays.asList(ed_type).contains(str)){
			retVal="free";
		}

			return retVal; 
	}

	private static String chg_userevent(String eventcode) {
		String retVal="";
		String[] explode=eventcode.split("\\_");
		int up_usercnt=Integer.parseInt(explode[1]);
		up_usercnt++;

		retVal=explode[0]+"_"+up_usercnt;
		return retVal;
	}
	
	/**
	 * 
	 * @param dateString
	 * @return
	 */
	private static long from_unixtime(String dateString)
	{	
		//String dateString = "2017-01-25 20:56:00";
	
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		Date date;
		long unixTime =0L;
		try {
			date = dateFormat.parse(dateString);
			unixTime = (long) date.getTime()/1000;
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return unixTime;
	}



	/**
	 * set_checkpoint
	 * @param cp_type
	 * @param cp_hp
	 * @return
	 */
	public static int set_checkpoint(String cp_type,String cp_hp) 
	{
		boolean retVal = false;
		int last_id = 0;
		long unixtime=0L;
		unixtime=System.currentTimeMillis() / 1000;
		StringBuilder sb = new StringBuilder();
		MyDataObject dao = new MyDataObject();
		sb.append("INSERT INTO `cashq`.`check_point` SET ");
		sb.append("cp_date=?,");
		sb.append("cp_unixtime=?,");
		sb.append("cp_type=?,");
		sb.append("cp_hp=?");
		try {
			dao.openPstmt(sb.toString());

			//Utils.getLogger().warning(sb.toString());

			
			dao.pstmt().setString(1, Utils.getyyyymmdd());
			dao.pstmt().setLong(2, unixtime);
			dao.pstmt().setString(3, cp_type);
			dao.pstmt().setString(4, cp_hp);
			dao.pstmt().executeUpdate();


			
		} catch (SQLException e) {
			Utils.getLogger().warning(e.getMessage());
			Utils.getLogger().warning(Utils.stack(e));
			DBConn.latest_warning = "ErrPOS060";
			 
		} catch (Exception e) {
			Utils.getLogger().warning(e.getMessage());
			Utils.getLogger().warning(Utils.stack(e));
			DBConn.latest_warning = "ErrPOS061";
		} finally {
			dao.closePstmt();
		}

		return last_id;
	}

	
	/**
	* int get_eventcnt
	* @param mb_hp
	* @param eventcode
	* @return int
	*/
	private static int get_checkpoint(String cp_type, String cp_hp){
		int retVal = 0;
		StringBuilder sb = new StringBuilder();

		MyDataObject dao = new MyDataObject();
		sb.append("SELECT count(*) cnt FROM `cashq`.`check_point` ");
		sb.append("WHERE cp_hp=? ");
		sb.append("AND cp_type=? ");
		sb.append("AND cp_date=? ");
		

		try {
			dao.openPstmt(sb.toString());
			dao.pstmt().setString(1,	cp_hp);
			dao.pstmt().setString(2, cp_type);
			dao.pstmt().setString(3, Utils.getyyyymmdd());
			
			dao.setRs (dao.pstmt().executeQuery());

			if (dao.rs().next()) {
				retVal = dao.rs().getInt("cnt");
			}			
		} catch (SQLException e) {
			Utils.getLogger().warning(e.getMessage());
			DBConn.latest_warning = "ErrPOS039";
			e.printStackTrace();
		}
		catch (Exception e) {
			Utils.getLogger().warning(e.getMessage());
			Utils.getLogger().warning(Utils.stack(e));
			DBConn.latest_warning = "ErrPOS040";
		}
		finally {
			dao.closePstmt();
		}

		return retVal;
	}

	/**
	 * @param bt_content
	 * 메세지 전문과 변환될 텍스트가 지정 되어 있습니다. 정해진 룰의 패턴이 지정 되어 있습니다.
	 *  패턴의 예 
	 *  #{매장명}을 이용해 주셔서 #{050번호}
	 * 
	 * @param bt_regex
	 *  룰의 규칙을 넣습니다. bt_content에서 선언한 #{키값}의 모든 패턴은 아래와 같이 모두 선언 되어 있어야 합니다.        
	 *  
	 *  예) 
	 *  #{매장명}=store.name&#{050번호}=store.tel
	 *  
	 *  라면 두개의 규칙이 존재하고 #{매장명}을 store.name의 맵의 키로 지정합니다.  
	 * @param messageMap
	 *  위에서 지정한 store.name의 키가 함수 호출전에 아래와 같은 형태로 정의 되어 인수로 들어가야 합니다.
	 *  Map<String, String> messageMap=new HashMap<String, String>();
		messageMap.put("store.name","태부치킨");
	 * @return
	 */
	private static String chg_regexrule(String bt_content, String bt_regex, Map<String, String> messageMap) {
		// TODO Auto-generated method stub
		String returnValue="";
		try{
			if(bt_regex.indexOf("&")>-1)
			{
				String[] regex_array=bt_regex.split("&");
				String[] keys;
				/* bt_regex 의 크기 만큼 반복하여 변환한다. */
				for (int i = 0; i < regex_array.length; i++) {
					keys=regex_array[i].split("=");
					bt_content=bt_content.replace(keys[0], messageMap.get(keys[1]));
				}
				returnValue=bt_content;
			}else{
				returnValue=bt_content;
			}
		}catch(NullPointerException e){
			returnValue=bt_content;
		}
		return returnValue;
	}

	/**
	 * @param point_set ="3_5000&5_10000&10_20000"
	 * @return
	 * 3개 5,000원
	 * 5개 10,000원
	 * 10개 20,000원
	 */
	private static String getPointSet(String point_set) {
		// TODO Auto-generated method stub
		String[] regex_array=point_set.split("&");
		String[] keys;
		String returnValue="";
		try{
			/* bt_regex 의 크기 만큼 반복하여 변환한다. */
			for (int i = 0; i < regex_array.length; i++) {
				keys=regex_array[i].split("_");
				returnValue=returnValue+keys[0]+"개 "+String.format("%,d", Integer.parseInt(keys[1]))+"원\n";
			}
		}catch(ArrayIndexOutOfBoundsException e){
			returnValue="5개 10,000원\n";
			returnValue="10개 20,000원\n";
		}
		return returnValue;
	}

}

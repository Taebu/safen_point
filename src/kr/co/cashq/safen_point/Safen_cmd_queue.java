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
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
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
 *  @date : 2018-02-27 오후 3:59:13
 *  개선전 : 현금 할인 북인 경우 무조건 전송이 되지 않는 문제 발생 원래는 발송을 했으나 무조건 실패 루틴을 타개 했고, 이 부분을 간혹 발생치 않는 문제가 발생
 *  개선후 : 캐시북(appid=="cashb") 인 경우, 무조건 GCM 발송 없이 ATA 발 
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
		Map<String, String>  agency_info	   = new HashMap<String, String>();
		/* 상점 정보 */
		Map<String, String> store_info      = new HashMap<String, String>();
		
		/* 메세지 정보 */
		Map<String, String> message_info = new HashMap<String, String>();
		
		Map<String, String> push_info = new HashMap<String, String>();
		/* 플러스친구 */
		Map<String, String> plusfriend=new HashMap<String, String>();
		
		String biz_code="";
		
		String mb_hp="";
		String appid="cashq";
		
		String st_no="";
		
		String messages="";
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
		
		/* 비즈톡에 입력된 값 */
		int wr_idx=0;
		String sender_key="";
		String result_message="전달성공";


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
					
					
					/* 매장의 고유 번호를 불러 옵니다. */
					st_no=dao.rs().getString("store_seq");
					System.out.println(dao.rs().getString("seq"));
					/* 3. 포인트를 사용가능으로 변경합니다. */
					update_point(dao.rs().getString("seq"));
					
					/* 4. 포인트 갯수를 센다.  */	
					//point_count=get_point_count(mb_hp,eventcode);
					
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
					if(message_info.get("gcm_regex").indexOf("&")>-1)
					{
						regex_rule=message_info.get("gcm_regex").split("&");
					}
					
					/**/
					if(message_info.get("gcm_status").equals("access")){

						Map<String, String> messageMap=new HashMap<String, String>();
						/* 매장이름 */
						messageMap.put("store.name",store_info.get("name"));
						
						/* 매장(상점)전화번호 */
						messageMap.put("store.tel",store_info.get("tel"));
						
						/* 미션조건 */
						messageMap.put("agencyMember.point_items",getPointSet(agency_info.get("point_items")));
						
						/* 포인트 최소 인정금액 */
						messageMap.put("agencyMember.min_point",String.format("%,d", Integer.parseInt(agency_info.get("min_point"))));
						
						/* 대리점 관리자의 핸드폰 번호를 불러 옵니다. 기본값 01077430009 */
						messageMap.put("agencyMember.cell",agency_info.get("cell"));
						
						/* 랜덤 6자리 치환 */
						messageMap.put("function.get_rand_int",String.valueOf(get_rand_int()));
						
						
						messageMap.put("downlink","http://hdu.cashq.co.kr/m/p/");

						/* 개인이 보유한 모든 사용가능 0507_point에 발급한 모든  포인트 합산 금액을 불러옵니다. */
						messageMap.put("function.total_point",get_total_point(mb_hp));
						
						/* 개인이 보유한 모든 사용가능 0507_point.status=1인 0507_point.point포인트가 sum한 결과를 불러옵니다. */
						messageMap.put("function.get_point",get_point(mb_hp));
						
						/* #{사용가능포인트} = cashq.agencyMember.minimum_point */
						messageMap.put("agencyMember.minimum_point",agency_info.get("minimum_point"));
						
						
						/* 상점읨 공유 링크를 가져 옵니다. 
						 * http://bdmt.cashq.co.kr/m/p/?seq=%s
						 * */
						messageMap.put("store.sharelink",get_sharelink(st_no));
						

						/* 템플릿을 정해진 패턴대로 변경 합니다. 
						 * @param bt_content 템플릿 내용, 
						 * @param bt_regex 템플릿 패턴ㅋ`
						 * @param messageMap 템플릿 패턴을 바꿀 내용
						 * */
						/* gcm messages */
						messages=chg_regexrule(message_info.get("gcm_message"),message_info.get("gcm_regex"), messageMap);
						System.out.println(messages);
						
						/* GCM 적립메세지 성공 여부 
						 * 현금할인북(cashb) 만 예외처리 */
						if(!"cashb".equals(appid))
						{
							success_gcm=set_gcm(messages,messages,mb_hp,appid);
							
						}
						if(!success_gcm)
						{
							result_message="전송 실패";
						}
						/* ata messages */
						push_info.put("appid",appid);
						push_info.put("stype","PNT_GCM");

						push_info.put("biz_code",biz_code);
						push_info.put("caller",mb_hp);
						push_info.put("called",store_info.get("r_tel"));
						push_info.put("wr_subject",messages);
						push_info.put("wr_content","JAVA Safen_point TEST");
						push_info.put("result",result_message);
						/* 전송 성공 여부에 따라 사이트 푸시 로그를 생성합니다.*/
						set_site_push_log(push_info);
						

						//success_gcm=false;
						
						/* 캐시북인 경우 무조건 전송 ATA */
						if("cashb".equals(appid))
						{
							/* gcm 전송 실패시  */
							regex_rule=message_info.get("ata_regex").split("&");
							messages=chg_regexrule(message_info.get("ata_message"),message_info.get("ata_regex"), messageMap);

							plusfriend=getSenderKey(appid);
							if(plusfriend.get("bp_status").equals("access"))
							{
								/* ATA 전송*/
								Map<String, String> ata_info = new HashMap<String, String>();
								ata_info.put("template_code",message_info.get("bt_code"));
								ata_info.put("content",messages);
								ata_info.put("mb_hp",mb_hp);
								ata_info.put("tel",store_info.get("tel"));
								ata_info.put("sender_key",plusfriend.get("bp_senderid"));
								wr_idx=set_em_mmt_tran(ata_info);
								
								/* Site_push_log*/
								push_info.put("stype","ATASEND");
								push_info.put("wr_subject",messages);
								push_info.put("wr_idx",Integer.toString(wr_idx));
								/* 전송 성공 여부에 따라 사이트 푸시 로그를 생성합니다.*/
								set_site_push_log(push_info);
							}
						}	
						
						/* ATA 전송 GCM 전송이 실패 하고 캐시북이 아닌 경우 */
						if(!success_gcm&&!"cashb".equals(appid))
						{
							/* gcm 전송 실패시  */
							regex_rule=message_info.get("ata_regex").split("&");
							messages=chg_regexrule(message_info.get("ata_message"),message_info.get("ata_regex"), messageMap);
							System.out.println(messages);
							
							plusfriend=getSenderKey(appid);
							System.out.println("sender_key : "+plusfriend.get("bp_senderid"));
							System.out.println("sender_key : "+plusfriend.get("bp_status"));
							if(plusfriend.get("bp_status").equals("access"))
							{
								/* ATA 전송*/
								Map<String, String> ata_info = new HashMap<String, String>();
								ata_info.put("template_code",message_info.get("bt_code"));
								ata_info.put("content",messages);
								//ata_info.put("mb_hp",mb_hp);
								ata_info.put("mb_hp",mb_hp);
								ata_info.put("tel",store_info.get("tel"));
								ata_info.put("sender_key",plusfriend.get("bp_senderid"));
								wr_idx=set_em_mmt_tran(ata_info);
								
								/* Site_push_log*/
								push_info.put("wr_subject",messages);
								push_info.put("stype","ATASEND");
								push_info.put("wr_idx",Integer.toString(wr_idx));
								/* 전송 성공 여부에 따라 사이트 푸시 로그를 생성합니다.*/
								set_site_push_log(push_info);
							}else if(plusfriend.get("bp_status").equals("stop")){
								messages="플러스 친구가 정지된 아이디 입니다.";
								/* Site_push_log*/
								push_info.put("wr_subject",messages);
								set_site_push_log(push_info);
							}else if(plusfriend.get("bp_status").equals("terminate")){
								/* Site_push_log*/
								messages="플러스 친구가 해지된 아이디 입니다.";
								push_info.put("wr_subject",messages);
								set_site_push_log(push_info);							
							}
						} /* ATA  전송 if(!success_gcm){...} */
					}else if(message_info.get("gcm_status").equals("stop"))	{
						messages="플러스아이디가  정지된 아이디 입니다.";
						push_info.put("appid",appid);
						push_info.put("stype","ATASEND");
						push_info.put("biz_code",biz_code);
						push_info.put("caller",mb_hp);
						push_info.put("called",store_info.get("r_tel"));
						push_info.put("wr_subject",messages);
						push_info.put("wr_content","JAVA Safen_point TEST");
						push_info.put("result","전송대기");
						push_info.put("wr_idx",String.valueOf(wr_idx));
						set_site_push_log(push_info);						
					}else if(message_info.get("gcm_status").equals("terminate")){
						messages="플러스아이디가 해지된 아이디 입니다.";
						push_info.put("appid",appid);
						push_info.put("stype","ATASEND");
						push_info.put("biz_code",biz_code);
						push_info.put("caller",mb_hp);
						push_info.put("called",store_info.get("r_tel"));
						push_info.put("wr_subject",messages);
						push_info.put("wr_content","JAVA Safen_point TEST");
						push_info.put("result","전송대기");
						push_info.put("wr_idx",String.valueOf(wr_idx));
						set_site_push_log(push_info);						
					}


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
	 * @param appid
	 * @return
	 */
	private static Map<String, String> getSenderKey(String appid) {
		// TODO Auto-generated method stub
		
			// TODO Auto-generated method stub
		Map<String, String> plusfriend=new HashMap<String, String>();
		
		StringBuilder sb = new StringBuilder();
		MyDataObject dao = new MyDataObject();
		sb.append("SELECT * FROM cashq.bt_plusfriend where bp_appid = ?");
		try {
			dao.openPstmt(sb.toString());
			dao.pstmt().setString(1, appid);
			dao.setRs (dao.pstmt().executeQuery());
			while(dao.rs().next()) 
			{
				plusfriend.put("bp_senderid", dao.rs().getString("bp_senderid"));
				plusfriend.put("bp_status", dao.rs().getString("bp_status"));
				plusfriend.put("bp_terminate_date", dao.rs().getString("bp_terminate_date"));
				plusfriend.put("bp_stop_date", dao.rs().getString("bp_stop_date"));
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
		return plusfriend;
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
		sb.append("result=?,");
		sb.append("wr_idx=?;");
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
			dao.pstmt().setString(9, push_info.get("wr_idx"));
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
	 * 비즈톡에 알림톡(카카오톡 비즈니스 메세지를 전송합니다.)를 전송합니다.  
	 * 입력 : 푸시 인포.앱아이디, stype,biz_code, caller, called, wr_subject, wr_content result
	 */
	private static int set_em_mmt_tran(Map<String, String> ata_info) {
		// TODO Auto-generated method stub
		StringBuilder sb = new StringBuilder();
		StringBuilder sb2 = new StringBuilder();
		MyDataObject dao = new MyDataObject();
		MyDataObject dao2 = new MyDataObject();
		int wr_idx=0;
		sb.append("INSERT INTO biztalk.em_mmt_tran SET ");
		sb.append("date_client_req=SYSDATE(), ");
		sb.append("template_code=?,");
		sb.append("content=?,");
		sb.append("recipient_num=?,");
		sb.append("callback=?,");
		sb.append("msg_status='1',");
		sb.append("subject=' ', ");
		sb.append("sender_key=?, ");
		sb.append("service_type='3', ");
		sb.append("msg_type='1008';");
		try {

			dao.openPstmt(sb.toString());
			dao.pstmt().setString(1, ata_info.get("template_code"));
			dao.pstmt().setString(2, ata_info.get("content"));
			dao.pstmt().setString(3, ata_info.get("mb_hp"));
			dao.pstmt().setString(4, ata_info.get("tel"));
			dao.pstmt().setString(5, ata_info.get("sender_key"));
			dao.pstmt().executeUpdate();
			
			sb2.append("select LAST_INSERT_ID() last_id;");
			dao2.openPstmt(sb2.toString());
			dao2.setRs(dao2.pstmt().executeQuery());
			
			if (dao2.rs().next()) {
				wr_idx= dao2.rs().getInt("last_id");
			}
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
			dao2.closePstmt();
		}
		return wr_idx;
		
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
		}catch(NullPointerException e){
			
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("error registration_ids null");
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
		sb.append("select * from cashq.bt_template where po_status='1' and appid=?");
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
					message.put("gcm_status",dao.rs().getString("bt_status"));
				}
				else if(dao.rs().getString("bt_type").equals("ata"))
				{
					message.put("ata_title",dao.rs().getString("bt_name"));					
					message.put("ata_message",dao.rs().getString("bt_content"));
					message.put("ata_regex",dao.rs().getString("bt_regex"));
					message.put("bt_code",dao.rs().getString("bt_code"));
					message.put("ata_status",dao.rs().getString("bt_status"));
				}
				else if(dao.rs().getString("bt_type").equals("sms"))
				{
					message.put("sms_title",dao.rs().getString("bt_name"));
					message.put("sms_message",dao.rs().getString("bt_content"));
					message.put("sms_regex",dao.rs().getString("bt_regex"));
					message.put("sms_status",dao.rs().getString("bt_status"));
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
		store_info.put("r_tel","0212341234");
		
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
				store_info.put("r_tel",dao.rs().getString("r_tel"));
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
		agency.put("minimum_point","10000");
		agency.put("cell","01077430009");
		
		
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
				agency.put("cell",dao.rs().getString("cell"));
				agency.put("minimum_point",dao.rs().getString("minimum_point"));
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

				dao.pstmt().executeUpdate();
				
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
				returnValue=returnValue+keys[0]+"회 주문시 "+String.format("%,d", Integer.parseInt(keys[1]))+"원\n";
			}
		}catch(ArrayIndexOutOfBoundsException e){
			returnValue="";
			returnValue=returnValue+"5회 주문시 10,000원\n";
			returnValue=returnValue+"10회 주문시  20,000원\n";
		}
		return returnValue;
	}
	
	/* 랜덤 6자리를 불러 옵니다. */
	public static int get_rand_int() 
	{
	    String numStr = "1";
	    String plusNumStr = "1";
	    for (int i = 0; i < 6; i++) {
	        numStr += "0";
	        if (i != 6 - 1) {
	            plusNumStr += "0";
	        }
	    }
	 
	    Random random = new Random();
	    int result = random.nextInt(Integer.parseInt(numStr)) + Integer.parseInt(plusNumStr);
	 
	    if (result > Integer.parseInt(numStr)) {
	        result = result - Integer.parseInt(plusNumStr);
	    }
	    return result;
	}


	
	/**
	 * @param appid
	 * @return
	 */
	private static String get_total_point(String mb_hp) {
		// TODO Auto-generated method stub
		
			// TODO Auto-generated method stub
		String total_point = "0";
		
		StringBuilder sb = new StringBuilder();
		MyDataObject dao = new MyDataObject();
		sb.append("SELECT * FROM cashq.0507_point where mb_hp = ?");
		try {
			dao.openPstmt(sb.toString());
			dao.pstmt().setString(1, mb_hp);
			dao.setRs (dao.pstmt().executeQuery());
			while(dao.rs().next()) 
			{
				total_point = dao.rs().getString("bp_senderid");
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
		return total_point;
	}
	
	
		/* 사용자가 가진 모든 포인트 값을 환산하여 더한 연산 결과를 가져옵니다.
		 * 
		 */
		private static String get_point(String mb_hp){
			String point = "0";
			StringBuilder sb = new StringBuilder();
			MyDataObject dao = new MyDataObject();
			sb.append("SELECT ");
			sb.append(" sum(point) sum_point ");
			sb.append("FROM cashq.0507_point ");
			sb.append("where mb_hp = ? ");
			sb.append(" and status='1';");
			
			try {
				dao.openPstmt(sb.toString());
				dao.pstmt().setString(1, mb_hp);
				dao.setRs (dao.pstmt().executeQuery());
				while(dao.rs().next()) 
				{
					point = dao.rs().getString("sum_point");
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
			return point;
		}

		/* 가맹점이 설정한 코드 공유 링크 정보를 가져옵니다.
		 * 
		 */
		private static String get_sharelink(String st_no){
		String sharelink = "";
		sharelink = String.format("http://bdmt.cashq.co.kr/m/p/?seq=%s",st_no);

		return sharelink;
		}


}

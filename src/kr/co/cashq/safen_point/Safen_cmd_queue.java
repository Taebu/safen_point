package kr.co.cashq.safen_point;

import java.sql.Connection;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

//import com.nostech.safen.SafeNo;

/**

 * safen_cmd_queue �뀒�씠釉� 愿��젴 媛앹껜
 * @author mtb
 * 2017-03-10 (湲�) 13:36:49 
 *  由щ럭 �룷�씤�듃 90�씪 60�씪濡� 蹂�寃�
 *  
 *  2017-08-18 (湲�) 16:10:00
 *   �꽭�씪 �룷�씤�듃 異붽�
 *   �룞�떆 �쟻由� 媛��뒫�븯�룄濡� 蹂�寃� 
 */
public class Safen_cmd_queue {
	
	/**
	 * safen_cmd_queue �뀒�씠釉붿쓽 �뜲�씠�꽣瑜� 泥섎━�븯湲� �쐞�븳 二쇱슂�븳 泥섎━瑜� �닔�뻾�븳�떎.
	 */
	public static void doMainProcess() {
		Connection con = DBConn.getConnection();

		String ev_st_dt="";
		String ev_ed_dt="";
		String daily_st_dt="";
		String daily_ed_dt="";
		String review_ed_dt="";
		String mb_hp="";
		String eventcode="";
		String cash="";
		String ed_type="";
		String biz_code="";
		String call_hangup_dt="";
		String mb_id="";
		String certi_code="";
		String st_dt="";
		String ed_dt="";
		String moddate="1970-01-01 12:00:00";
		String accdate="1970-01-01 12:00:00";
		String str_hangup_time="";
		String tel="";
		String pt_stat="";

		int eventcnt = 0;
		int daycnt = 0;
		int reviewdaycnt = 0;
		int downdaycnt = 0;
		int saledaycnt = 0;
		int calldaycnt = 0;
		int pt_day_cnt = 0;
		int pt_event_cnt = 0;
		int usereventindex = 0;
		int user_event_dt_index = 0;
		int tcl_seq = 0;
		int service_sec=0;
		int hangup_time=0;

		boolean is_hp = false;
		boolean is_freedailypt = false;
		boolean is_freeuserpt = false;
		boolean is_fivept = false;
		boolean is_callpt = false;
		boolean is_reviewpt = false;
		boolean is_downpt = false;
		boolean is_salept = false;
		
		boolean is_realcode=false;
		boolean is_userpt=false;
		boolean chk_realcode=false;
		boolean is_answer=false;

		/* �긽�젏 �젙蹂� */
		String[] store_info			= new String[5];
		/* �룷�씤�듃 �씠踰ㅽ듃 �젙蹂� */
		String[] point_event_info	= new String[7];
		/* �쑀�� �씠踰ㅽ듃 �젙蹂� */
		String[] user_event_info	= new String[3];
		
		String status_cd="";
		String conn_sdt="";
		String conn_edt="";
		String service_sdt="";
		String safen="";
		String safen_in="";
		String safen_out="";
		String calllog_rec_file="";

		String store_name = "";
		String pre_pay="";
		String store_seq="";
		String type="";
		String str_tcl_seq="";

		if (con != null) {
			MyDataObject dao = new MyDataObject();
			MyDataObject dao2 = new MyDataObject();
			MyDataObject dao3 = new MyDataObject();
			MyDataObject dao4 = new MyDataObject();
			MyDataObject dao5 = new MyDataObject();

			StringBuilder sb = new StringBuilder();
			StringBuilder sb_log = new StringBuilder();

			//sb.append("select exists(select 1 from safen_cdr) a");
			//sb.append("select * from safen_cdr limit 1");
			//sb.append("select * from safen_cdr where seq<192 limit 1;");
			//sb.append("select * from safen_cdr where seq<221 limit 1;");
			//sb.append("select * from safen_cdr limit 1;");
			sb.append("select * from sktl.safen_cdr order by seq limit 1;");
			
			try {
				dao.openPstmt(sb.toString());

				dao.setRs(dao.pstmt().executeQuery());

				if (dao.rs().next()) {
					
					SAFEN_CDR.heart_beat = 1;
					Boolean chk_seq=dao.rs().getInt("seq")>0;

					if (chk_seq) {
						StringBuilder sb2 = new StringBuilder();
						StringBuilder sb5 = new StringBuilder();
						String hist_table = DBConn.isExistTableYYYYMM();
						int resultCnt2 = 0;

						/*	String status_cd �긽�깭肄붾뱶, */
						status_cd=chkValue(dao.rs().getString("status_cd"));
						
						/*	String conn_sdt �떆�옉�떆媛�,  */
						conn_sdt=chkValue(dao.rs().getString("conn_sdt"));
						
						/*	String conn_edt 醫낅즺�떆媛�, */
						conn_edt=chkValue(dao.rs().getString("conn_edt"));
						
						/*	String service_sdt, 諛섏쓳�떆媛� */
						service_sdt=chkValue(dao.rs().getString("service_sdt"));
						
						/*	String safen �븞�떖踰덊샇, 050 */
						safen=chkValue(dao.rs().getString("safen"));
						tel=safen;

						/*	String safen_in �긽�젏踰덊샇, */
						safen_in=chkValue(dao.rs().getString("safen_in"));
						
						/*	String calllog_rec_file	肄쒕줈洹� �끃�쓬�뙆�씪( */
						safen_out=chkValue(dao.rs().getString("safen_out"));
						
						/*	String status_cd �긽�깭肄붾뱶, */
						calllog_rec_file=chkValue(dao.rs().getString("calllog_rec_file"));
						
						/*	String status_cd �긽�깭肄붾뱶, */
						service_sec=dao.rs().getInt("service_sec");
						
						/*	String status_cd �긽�깭肄붾뱶, */
						call_hangup_dt=chkValue(dao.rs().getString("service_sdt"));
						
						/*	String status_cd �긽�깭肄붾뱶, */
						st_dt=chkValue(dao.rs().getString("conn_sdt"));
						
						/*	String status_cd �긽�깭肄붾뱶, */
						ed_dt=chkValue(dao.rs().getString("conn_edt"));
						
						st_dt=chgDatetime(st_dt);
						ed_dt=chgDatetime(ed_dt);
						mb_hp=safen_out;
						
						is_answer = status_cd.equals("1");
						/* cashq.TB_CALL_LOG�뿉 �꽭�똿�빀�땲�떎. */
						tcl_seq = set_TB_CALL_LOG(status_cd, conn_sdt,conn_edt, service_sdt, safen,safen_in,safen_out,calllog_rec_file);
						str_tcl_seq=Integer.toString(tcl_seq);
						str_hangup_time=Integer.toString(service_sec);

						/* cashq.store.callcnt 媛깆떊*/
						update_stcall(safen);
						
						/* 4-2. Store Info 議고쉶 */
						//store_info=getStoreInfo(safen_in);
						store_info=getStoreInfo(safen);
						/**
						* store_info[0] = store_name, �긽�젏�씠由� 
						* store_info[1] = pre_pay, 怨⑤뱶,�떎踰�,罹먯떆�걧,�씪諛�,PRQ 
						* store_info[2] = biz_code, 鍮꾩쫰肄붾뱶 
						* store_info[3] = store_seq, �긽�젏怨좎쑀踰덊샇 
						* store_info[4] = type, �긽�젏���엯(�삁, 移섑궓,�뵾�옄 �벑�벑�쓽 肄붾뱶) 
						*/
						store_name=store_info[0];
						pre_pay=store_info[1];
						biz_code=store_info[2];
						store_seq=store_info[3];
						type=store_info[4];

						/* 4-3. Event Info 議고쉶*/
						if(biz_code!=null||biz_code!="")
						{
						point_event_info=getEventCodeInfo(biz_code);
						/**
						* point_event_info[0] = ev_st_dt, �씠踰ㅽ듃 �떆�옉�씪
						* point_event_info[1] = ev_ed_dt, �씠踰ㅽ듃 醫낅즺�씪
						* point_event_info[2] = eventcode, �씠踰ㅽ듃 肄붾뱶 
						* point_event_info[3] = cash, 罹먯떆
						* point_event_info[4] = pt_day_cnt, �씪�젙由쎌젣�븳 媛��닔
						* point_event_info[5] = pt_event_cnt, �씠踰ㅽ듃肄붾뱶 �젙由� �젣�븳 媛��닔
						* point_event_info[6] = ed_type, �씠踰ㅽ듃肄붾뱶 ���엯
						*/
						ev_st_dt=point_event_info[0];
						ev_ed_dt=point_event_info[1];
						eventcode=point_event_info[2]!=null?point_event_info[2]:"";
						cash=point_event_info[3]!=null?point_event_info[3]:"";
						pt_day_cnt = point_event_info[4]!=null?Integer.parseInt(point_event_info[4]):0;
						pt_event_cnt = point_event_info[5]!=null?Integer.parseInt(point_event_info[5]):0;
						ed_type=point_event_info[6];
						

						
						if(eventcode.length()>3&&biz_code.length()>3){
							chk_realcode=is_realcode(eventcode,biz_code);
						}

						/** 
						* 4-4. �씠踰ㅽ듃 媛��닔 移댁슫�듃&amp;
						* 4-5-1. call �븯猷� �룷�씤�듃 �븳踰� �룷�씤�듃 �뿬遺�
						* 4-5-2. reviewpt �븯猷� �룷�씤�듃 �븳踰� �룷�씤�듃 �뿬遺�
						* 4-5-2. �븯猷� �룷�씤�듃 �븳踰� �룷�씤�듃 �뿬遺�
						* 4-6. �빖�뱶�룿 踰덊샇 �뿬遺�
						* 4-7. �룷�씤�듃 �옱�쟻由� �떆媛� �뿬遺� �떒�쐞
						*      point_event_dt, pt_event_cnt
						* 4-8. NEW freedailypt �뿬遺� 議고쉶
						* 4-9. NEW �삤�뒛�쓣 湲곗��쑝濡� 60�씪 肄붾뱶瑜� 媛��졇�삩�떎.
						* 4-10. NEW freeuserpt �뿬遺� 議고쉶
						* 4-11. NEW usereventcnt
						* 4-12. NEW
						* 4-13. NEW pt_stat
						*/


						
						/* 4-5 */
						//String[] callArray = new String[] {"reviewpt","downpt"};
						String[] callArray = new String[] {"fivept","freedailypt","freeuserpt","freept"};
						String[] reviewArray = new String[] {"reviewpt","downpt"};
						
						/* 4-6 */
						is_hp = is_hp(safen_out);

						/* 4-8 */
						is_freedailypt = is_freedailypt(ed_type);
						
						/* 4-10 */
						is_freeuserpt = is_freeuserpt(ed_type);

						/* 4-10-1 */
						is_fivept=is_fivept(ed_type);
						
						
						/* 4-10-2 */
						is_reviewpt=is_reviewpt(ed_type);
						/* 4-10-3 */
						is_downpt=is_downpt(ed_type);
						/* 4-10-4 */
						is_callpt=Arrays.asList(callArray).contains(ed_type);

						/* 4-10-5 */
						is_salept=is_salept(ed_type);
						
						
						if(is_callpt){
						//calldaycnt = get_daycnt(mb_hp,"callpt");
						calldaycnt = get_checkpoint("callpt",mb_hp);
						}else if(is_reviewpt){
						reviewdaycnt = get_checkpoint("reviewpt",mb_hp);
						}else if(is_downpt){
						downdaycnt =  get_checkpoint("downpt",mb_hp);
						}else if(is_salept){
						saledaycnt =  get_checkpoint("salept",mb_hp);
						}

						
						
						daily_st_dt=Utils.getyyyymmdd();
						daily_ed_dt=Utils.add60day();
						//review_ed_dt=Utils.add90day();
						review_ed_dt=Utils.add60day();
						
						/* 4-11 */
						if(is_freeuserpt){
							usereventindex = get_user_event_index(mb_hp, biz_code);
						}
						 
						/* 4-12 */
						if(is_freeuserpt&&usereventindex==0&&is_hp&&is_answer){
							/* user_event �깮�꽦�븯湲� */
							daily_st_dt=Utils.getyyyymmdd();
							daily_ed_dt=Utils.add60day();
							eventcode=biz_code+"_1";
							user_event_dt_index = set_user_event_dt(biz_code, mb_hp, daily_st_dt,daily_ed_dt,eventcode);
						}else if(is_freeuserpt&&usereventindex>0&&is_hp&&is_answer){
							/* user_event 議고쉶�븯湲� */
							user_event_info = get_userevent(biz_code, mb_hp);
							if(is_datepoint(user_event_info[0],user_event_info[1])){
								daily_st_dt=user_event_info[0];
								daily_ed_dt=user_event_info[1];
								eventcode=user_event_info[2];
							}else{
								daily_st_dt=Utils.getyyyymmdd();
								daily_ed_dt=Utils.add60day();
								eventcode=chg_userevent(user_event_info[2]);
								user_event_dt_index = set_user_event_dt(biz_code, mb_hp, daily_st_dt,daily_ed_dt,eventcode);
							
							}
						}
						}/* if(biz_code!=null||biz_code!=""){ ... } */

						/* 4-4 */
						eventcnt = get_eventcnt(mb_hp,eventcode,ed_type);
						/* 4-13 */
						pt_stat=chk_pt5(ed_type);

						/* fivept, freept 6. �쟻由쎌“嫄�*/
						if(is_point(pre_pay)
							&&service_sec>9
							&&is_datepoint(ev_st_dt,ev_ed_dt)
							&&calldaycnt==0
							&&eventcnt<pt_event_cnt
							&&is_hp
							&&is_fivept
							&&is_answer
							&&chk_realcode
							&&is_callpt
						){
							set_0507_point(
								mb_hp,store_name, str_hangup_time, 
								biz_code, call_hangup_dt, ev_st_dt, 
								ev_ed_dt, eventcode, mb_id, 
								certi_code, st_dt, ed_dt, 
								store_seq, str_tcl_seq, moddate, 
								accdate,ed_type,type,
								tel,pre_pay,pt_stat);
							
							set_checkpoint("callpt",mb_hp);
						}else if(is_point(pre_pay)
							&&service_sec>9
							&&is_datepoint(ev_st_dt,ev_ed_dt)
							&&calldaycnt==0
							&&eventcnt<pt_event_cnt
							&&is_hp
							&&is_answer
							&&is_freedailypt
							&&chk_realcode
							&&is_callpt
						){
							/* freedailypt 7. �쟻由쎌“嫄�*/
							daily_st_dt=Utils.getyyyymmdd();
							daily_ed_dt=Utils.add60day();
							

							/* 7-1 */
							set_0507_point(
								mb_hp,store_name, str_hangup_time, 
								biz_code, call_hangup_dt, daily_st_dt,
								daily_ed_dt, eventcode, mb_id, 
								certi_code, st_dt, ed_dt, 
								store_seq, str_tcl_seq, moddate, 
								accdate,ed_type,type,
								tel,pre_pay,pt_stat);
							
							set_checkpoint("callpt",mb_hp);
						}else if(is_point(pre_pay)
							&&service_sec>9
							&&is_datepoint(ev_st_dt,ev_ed_dt)
							&&calldaycnt==0
							&&eventcnt<pt_event_cnt
							&&is_hp
							&&is_answer
							&&is_freeuserpt
							&&chk_realcode
							&&is_callpt
						){
							/* freeuserpt 8. �쟻由쎌“嫄�*/
							/* 8-1 */
							set_0507_point(
								mb_hp,store_name, str_hangup_time, 
								biz_code, call_hangup_dt, daily_st_dt, 
								daily_ed_dt, eventcode, mb_id, 
								certi_code, st_dt, ed_dt, 
								store_seq, str_tcl_seq, moddate, 
								accdate,ed_type,type,
								tel,pre_pay,pt_stat);
							
							set_checkpoint("callpt",mb_hp);
						}else if(is_point(pre_pay)
								&&service_sec>9
								&&is_datepoint(ev_st_dt,ev_ed_dt)
								&&reviewdaycnt==0
								&&eventcnt<pt_event_cnt
								&&is_hp
								&&is_answer
								&&is_reviewpt
								&&chk_realcode
							){
								/* reviewpt 9. �쟻由쎌“嫄�*/
								/* 9-1 */
								set_0507_point(
									mb_hp,store_name, str_hangup_time, 
									biz_code, call_hangup_dt, daily_st_dt, 
									review_ed_dt, eventcode, mb_id, 
									certi_code, st_dt, ed_dt, 
									store_seq, str_tcl_seq, moddate, 
									accdate,ed_type,type,
									tel,pre_pay,pt_stat);
								
								set_checkpoint("reviewpt",mb_hp);
							}else if(is_point(pre_pay)
									&&service_sec>9
									&&is_datepoint(ev_st_dt,ev_ed_dt)
									&&downdaycnt==0
									&&eventcnt<pt_event_cnt
									&&is_hp
									&&is_answer
									&&is_downpt
									&&chk_realcode
								){
									/* downpt 10. �쟻由쎌“嫄�*/
									/* 10-1 */
									set_0507_point(
										mb_hp,store_name, str_hangup_time, 
										biz_code, call_hangup_dt, daily_st_dt, 
										daily_ed_dt, eventcode, mb_id, 
										certi_code, st_dt, ed_dt, 
										store_seq, str_tcl_seq, moddate, 
										accdate,ed_type,type,
										tel,pre_pay,pt_stat);
									
									set_checkpoint("downpt",mb_hp);
								}else if(is_point(pre_pay)
										&&is_datepoint(ev_st_dt,ev_ed_dt)
										&&saledaycnt==0
										&&eventcnt<pt_event_cnt
										&&is_hp
										&&is_answer
										&&is_salept
										&&chk_realcode
								){
									/* salept 11. �쟻由쎌“嫄�*/
									/* 11-1 */
									set_0507_point(
										mb_hp,store_name, str_hangup_time, 
										biz_code, call_hangup_dt, daily_st_dt, 
										daily_ed_dt, eventcode, mb_id, 
										certi_code, st_dt, ed_dt, 
										store_seq, str_tcl_seq, moddate, 
										accdate,ed_type,type,
										tel,pre_pay,pt_stat);
									
									set_checkpoint("salept",mb_hp);
								}
						


						sb2.append("insert into sktl.");
						sb2.append(hist_table);
						/* 泥섎━媛� 吏꾪뻾以묒씤寃껋� �룷�븿�븯吏� �븡�뒗�떎. */
						sb2.append(" select * from sktl.safen_cdr where seq=?");
						
						// insert into safen_cmd_hist_201607 select * from
						// safen_cmd_queue where status_cd != ''
						dao2.openPstmt(sb2.toString());
						dao2.pstmt().setInt(1, dao.rs().getInt("seq"));

 						resultCnt2 = dao2.pstmt().executeUpdate();
						if(resultCnt2!=1) {
							Utils.getLogger().warning(dao2.getWarning(resultCnt2,1));
							DBConn.latest_warning = "ErrPOS027";
						}

						// region 3 start --->
						StringBuilder sb3 = new StringBuilder();

						/* 泥섎━媛� 吏꾪뻾以묒씤寃껋� 吏��슦吏� �븡�뒗�떎. */
						sb3.append("delete from sktl.safen_cdr where seq=?");
				
						// insert into safen_cmd_hist_201607 select * from
						// safen_cmd_queue where status_cd != ''
						dao3.openPstmt(sb3.toString());
						dao3.pstmt().setInt(1, dao.rs().getInt("seq"));

						int resultCnt3 = dao3.pstmt().executeUpdate();
						if(resultCnt3!=1) {
							Utils.getLogger().warning(dao3.getWarning(resultCnt3,1));
							DBConn.latest_warning = "ErrPOS028";
						}
						// region 3 end <---

						// region 4 start --->
						/*
						StringBuilder sb4 = new StringBuilder();
						
						sb4.append("select * from safen_cdr limit 1");
						dao4.openPstmt(sb4.toString());

						dao4.setRs(dao4.pstmt().executeQuery());

						if (dao4.rs().next()) {
							int seq = dao4.rs().getInt("seq");
							String safen = dao4.rs().getString("safen");
							String safen_in = dao4.rs().getString("safen_in");
							doMapping(seq, safen, safen_in);
						}
						*/
						// region 4 end <---
					} else {
						//Utils.getLogger().info("chk_seq false log");
						if (!"".equals(Env.confirmSafen)) {
							// cmq_queue�뿉�뒗 �뾾�뒗 寃쎌슦�씪硫�
							//SafeNo safeNo = new SafeNo();
							String retCode = "";
							try {
								//retCode = safeNo.SafeNoAsk(Env.getInstance().CORP_CODE,Env.confirmSafen);
							} catch (Exception e) {
								Utils.getLogger().warning(e.getMessage());
								Utils.getLogger().warning(Utils.stack(e));
								DBConn.latest_warning = "ErrPOS029";
							}

							if (-1 < retCode.indexOf(Env.confirmSafen_in)) {
								/* retCode = "01040421182,01040421182" �� 媛숈� �삎�깭濡� 由ы꽩�릺�뒗 �떇�엫 */
								Utils.getLogger().info(
										"OK 李⑹떊�뿰寃곗꽦怨�" + Env.confirmSafen + "->"
												+ Env.confirmSafen_in);
							} else {// 痍⑥냼�맂 寃쎌슦 recCode = "E401"�씠 由ы꽩�맖
								if (Env.NULL_TEL_NUMBER
										.equals(Env.confirmSafen_in)
										&& "E401".equals(retCode)) {
									Utils.getLogger().info(
											"OK 李⑹떊痍⑥냼�꽦怨�" + Env.confirmSafen
													+ ", retCode:[" + retCode
													+ "]");
								} else {
									Utils.getLogger().warning(
											"Error! " + Env.confirmSafen + "->"
													+ Env.confirmSafen_in
													+ "? retCode:[" + retCode
													+ "]");
									DBConn.latest_warning = "ErrPOS030";
								}
							}

							Env.confirmSafen = "";
						}
					}
				}				
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
			
			//肄쒕줈洹� 留덉뒪�꽣 �젙蹂댁쓽 �젅肄붾뱶 1媛쒕�� 媛깆떊�쓣 �떆�룄�븳�떎.
			//Safen_master.doWark2();
		}
	}

	/**
	 * 痍⑥냼�떆�뒗 safen_in010�뿉 "1234567890"�쓣 �꽔�뼱�빞 �븿. 由ы꽩肄붾뱶4�옄由ъ뿉 �뵲瑜� �쓽誘�
	 * 
	 * 0000:�꽦怨� 泥섎━(�씤利앹꽌踰꾩뿉�꽌 �슂泥� 泥섎━媛� �꽦怨�.) E101:Network �옣�븷(�씤利앹꽌踰꾩� �뿰寃� �떎�뙣.) E102:System
	 * �옣�븷(�씤利앹꽌踰꾩쓽 �씪�떆�쟻 �옣�븷. �옱�떆�룄 �슂留�.) E201:�젣�쑕�궗 �씤利� �떎�뙣(�쑀�슚�븳 �젣�쑕�궗 肄붾뱶媛� �븘�떂.) E202:�쑀�슚 湲곌컙
	 * 留뚮즺(�젣�쑕�궗���쓽 怨꾩빟湲곌컙 留뚮즺.) E301:�븞�떖 踰덊샇 �냼吏�(�쑀�슚�븳 �븞�떖踰덊샇 �옄�썝�씠 �뾾�쓬.) E401:Data Not
	 * Found(�슂泥��븳 Data�� �씪移섑븯�뒗 Data媛� �뾾�쓬.) E402:Data Overlap(�슂泥��븳 Data媛� �씠誘� 議댁옱�븿.)
	 * E501:�쟾臾� �삤瑜�(�쟾臾� 怨듯넻遺� �샊�� 蹂몃Ц�쓽 Data媛� 鍮꾩젙�긽�씪 寃쎌슦.) E502:�쟾�솕 踰덊샇(�삤瑜� �슂泥��븳 李⑹떊踰덊샇媛� 留듯븨遺덇� 踰덊샇�씪
	 * 寃쎌슦.)
	 */
	private static String doMapping(int seq, String safen0504,
			String safen_in010) {

		String corpCode = Env.getInstance().CORP_CODE;
		String safeNum = null;
		String telNum1 = null;// "1234567890";
		String newNum1 = null;
		String telNum2 = null;
		String newNum2 = null;

		int mapping_option = 0;
		if (Env.NULL_TEL_NUMBER.equals(safen_in010)) {
			// 痍⑥냼
			mapping_option = 2;

			String safen_in = getSafenInBySafen(safen0504);

			safeNum = safen0504;
			telNum1 = safen_in;
			newNum1 = Env.NULL_TEL_NUMBER;// "1234567890";;
			telNum2 = safen_in;
			newNum2 = Env.NULL_TEL_NUMBER;
		} else {
			// �벑濡� Create
			mapping_option = 1;
			safeNum = safen0504;
			telNum1 = Env.NULL_TEL_NUMBER;// "1234567890";
			newNum1 = safen_in010;
			telNum2 = Env.NULL_TEL_NUMBER;
			newNum2 = safen_in010;
		}

		// String groupCode = "anpr_1";
		String groupCode = "grp_1";
		
		groupCode = Safen_master.getGroupCode(safen0504);

		String reserved1 = "";
		String reserved2 = "";
		String retCode = "";

		//SafeNo safeNo = new SafeNo();

		try {
			update_cmd_queue(seq, safen0504, safen_in010, mapping_option, "");
			//retCode = safeNo.SafeNoMod(corpCode, safeNum, telNum1, newNum1,telNum2, newNum2, groupCode, reserved1, reserved2);
		} catch (Exception e) {
			Utils.getLogger().warning(e.getMessage());
			Utils.getLogger().warning(Utils.stack(e));
			DBConn.latest_warning = "ErrPOS033";
		}

		// �썑泥섎━
		if ("0000".equals(retCode)) {
			Safen_master.update_safen_master(safen0504, safen_in010,
					mapping_option);

			Env.confirmSafen = safen0504;
			Env.confirmSafen_in = safen_in010;// 痍⑥냼�씤寃쎌슦�뒗 1234567890 �엫

		}
		update_cmd_queue(seq, safen0504, safen_in010, mapping_option, retCode);

		return retCode;
	}

	/**
	 * �븞�떖踰덊샇�뀒�씠釉붿쓣 媛깆떊�븳�떎. �떒, �씠�븣 retCode媛� 怨듬갚�씠硫� status_cd瑜� i濡� �꽔怨� 吏꾪뻾以묒쑝濡쒕쭔 留덊궧�븯怨� �봽濡쒖꽭�뒪瑜�
	 * 醫낅즺�븳�떎. retCode媛� "0000"(�꽦怨�)�씤寃쎌슦�뿉�뒗 status_cd媛믪쓣 "s"濡� 洹몃젃吏� �븡�� 寃쎌슦�뿉�뒗 "e"濡� �뀑�똿�븳 �썑 �걧瑜�
	 * 吏��슦怨� 濡쒓렇濡� 蹂대궦�떎. 
	 * @param safen0504
	 * @param safen_in010
	 * @param mapping_option
	 * @param retCode
	 */
	private static void update_cmd_queue(int seq, String safen0504,
			String safen_in010, int mapping_option, String retCode) {

		MyDataObject dao = new MyDataObject();
		MyDataObject dao2 = new MyDataObject();
		MyDataObject dao3 = new MyDataObject();
		
		try {
			if ("".equals(retCode)) {
				StringBuilder sb = new StringBuilder();
				sb.append("update safen_cmd_queue set status_cd=? where seq=?");

				// status_cd 而щ읆�쓣 "i"<吏꾪뻾以�>�긽�깭濡� 諛붽씔�떎.
				dao.openPstmt(sb.toString());

				dao.pstmt().setString(1, "i");
				dao.pstmt().setInt(2, seq);

				int cnt = dao.pstmt().executeUpdate();
				if(cnt!=1) {
					Utils.getLogger().warning(dao.getWarning(cnt,1));
					DBConn.latest_warning = "ErrPOS034";
				}

				dao.tryClose();

			} else {
				StringBuilder sb = new StringBuilder();
				sb.append("update safen_cmd_queue set status_cd=?,result_cd=? where seq=?");

				if ("0000".equals(retCode)) {
					// status_cd 而щ읆�쓣 "s"<�꽦怨�>�긽�깭濡� 諛붽씔�떎.
					
					dao2.openPstmt(sb.toString());

					dao2.pstmt().setString(1, "s");
					dao2.pstmt().setString(2, retCode);
					dao2.pstmt().setInt(3, seq);

					int cnt = dao2.pstmt().executeUpdate();
					if(cnt!=1) {
						Utils.getLogger().warning(dao2.getWarning(cnt,1));
						DBConn.latest_warning = "ErrPOS035";
					}

					dao2.tryClose();
				} else {
					// status_cd 而щ읆�쓣 "e"<�삤瑜�>�긽�깭濡� 諛붽씔�떎.
					dao3.openPstmt(sb.toString());

					dao3.pstmt().setString(1, "e");
					dao3.pstmt().setString(2, retCode);
					dao3.pstmt().setInt(3, seq);

					int cnt = dao3.pstmt().executeUpdate();
					if(cnt!=1) {
						Utils.getLogger().warning(dao3.getWarning(cnt,1));
						DBConn.latest_warning = "ErrPOS036";
					}					
					dao3.tryClose();
				}
			}
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
			dao2.closePstmt();
			dao3.closePstmt();
		}
	}

	/**
	 * 留덉뒪�꽣 �뀒�씠釉붿뿉�꽌 �븞�떖踰덊샇�뿉 �뵲瑜� 李⑹떊踰덊샇瑜� 由ы꽩�븳�떎.
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
	 * TB_CALL_LOG�뿉 異붽��븳�떎.
	 * @param String status_cd	肄쒕줈洹� �긽�깭 肄붾뱶
	 * @param String conn_sdt	肄쒕줈洹� �떆�옉�떆媛�
	 * @param String conn_edt	肄쒕줈洹� 醫낅즺�떆媛�
	 * @param String service_sdt	肄쒕줈洹� �젣怨듭떆媛�
	 * @param String safen	�븞�떖踰덊샇
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
			/* GCM LOG 諛쒖깮*/
			set_stgcm(safen, safen_in);

			/* �넻�솕�꽦怨� */
			dao.pstmt().setString(1, conn_sdt);
			dao.pstmt().setString(2, conn_edt);
			dao.pstmt().setString(3, service_sdt);
			dao.pstmt().setString(4, safen);
			dao.pstmt().setString(5, safen_in);
			dao.pstmt().setString(6, safen_out);
			dao.pstmt().setString(7, calllog_rec_file);
			dao.pstmt().setString(8, status_cd);
			}else{
			/* �넻�솕�떎�뙣*/
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
			dao2.closePstmt();
		}

		return last_id;
	}


	/**
	 * 0507_point�뿉 異붽��븳�떎.
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
    store_name: 蹂몄궗�룷�씤�듃 �뀒�뒪�듃
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
	}




	
	/**
	 * cdr �뿉 異붽��븳�떎.
	 * @param String status_cd	肄쒕줈洹� �긽�깭 肄붾뱶
	 * @param String conn_sdt	肄쒕줈洹� �떆�옉�떆媛�
	 * @param String conn_edt	肄쒕줈洹� 醫낅즺�떆媛�
	 * @param String service_sdt	肄쒕줈洹� �젣怨듭떆媛�
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
		//return retVal;
		return last_id;
	}
	
	
	/**
	 * �긽�젏 肄쒕줈洹몃줈 媛깆떊�븳�떎.  retCode媛� "0000"(�꽦怨�)�씤寃쎌슦�뿉�뒗 status_cd媛믪쓣 "s"濡� 洹몃젃吏� �븡�� 寃쎌슦�뿉�뒗 "e"濡� �뀑�똿�븳 �썑 �걧瑜�
	 * 吏��슦怨� 濡쒓렇濡� 蹂대궦�떎. 
	 * @param safen_in
	 * @param retCode
	 */
	private static void update_stcall(String safen) {

		MyDataObject dao = new MyDataObject();
		
		try {
			StringBuilder sb = new StringBuilder();
			sb.append("UPDATE `cashq`.`store` SET callcnt=callcnt+1 WHERE tel=?");

			// status_cd 而щ읆�쓣 "i"<吏꾪뻾以�>�긽�깭濡� 諛붽씔�떎.
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
	 * 罹먯떆�걧 �긽�젏�뿉�꽌 �븞�떖踰덊샇�뿉 �뵲瑜� �긽�젏 �젙蹂대�� 由ы꽩�븳�떎.
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
	 * 鍮꾩쫰肄붾뱶�뿉 �뵲瑜� �씠踰ㅽ듃 肄붾뱶 �젙蹂대�� 由ы꽩�븳�떎.
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
	private static int get_eventcnt(String mb_hp, String eventcode,String ed_type){
		int retVal = 0;
		StringBuilder sb = new StringBuilder();

		MyDataObject dao = new MyDataObject();
		sb.append("SELECT count(*) cnt FROM `cashq`.`0507_point` ");
		sb.append("WHERE mb_hp=? ");
		sb.append("AND eventcode=? ");
		sb.append("AND ed_type=? ");
		sb.append("AND status in ('1','2','3','4');");

		try {
			dao.openPstmt(sb.toString());
			dao.pstmt().setString(1, mb_hp);
			dao.pstmt().setString(2, eventcode);
			dao.pstmt().setString(3, ed_type);
			
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
	 * set_user_event_dt�뿉 異붽��븳�떎.
	 * @param String biz_code	肄쒕줈洹� �긽�깭 肄붾뱶
	 * @param String mb_hp	肄쒕줈洹� �떆�옉�떆媛�
	 * @param String conn_edt	肄쒕줈洹� 醫낅즺�떆媛�
	 * @param String service_sdt	肄쒕줈洹� �젣怨듭떆媛�
	 * @param String safen	�븞�떖踰덊샇
	 * @param String safen_in	留곹겕�맂踰덊샇
	 * @param String safen_out	�냼鍮꾩옄 踰덊샇
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
	 * app_toeken �븘�씠�뵒�쓽 吏��뿭 �젙蹂대�� 媛깆떊�빐�꽌 �꽔�뒗�떎.

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
	 * set_stgcm �븘�씠�뵒�쓽 吏��뿭 �젙蹂대�� 媛깆떊�빐�꽌 �꽔�뒗�떎.
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

		/* null check �븯�굹�씪�룄 �꼸�씠硫� �뿉�윭 */
		if(ev_st_dt==null||ev_ed_dt==null){

		}else{
			Date todayDate = new Date();
			
			Date historyDate = sdf.parse(ev_st_dt);
			Date futureDate = sdf.parse(ev_ed_dt);

			/* 湲곌컙 �씠�궡 */
			is_date=todayDate.after(historyDate)&&todayDate.before(futureDate);
			
			/* �씠踰ㅽ듃 醫낅즺 �떆媛꾧낵 媛숈� �궇 */
			if(sdf.format(todayDate).equals(sdf.format(futureDate))){
				is_date=true;
			}		
		}
		
		}catch(ParseException e){
		
		}
		
		return is_date;
	}


	// yyyy-MM-dd HH:mm:ss.0 �쓣 yyyy-MM-dd HH:mm:ss�궇吏쒕줈 蹂�寃�
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
	 *  �뜲�씠�꽣 �쑀�슚�꽦 null 泥댄겕�뿉 ���븳 媛믪쓣 "" 濡� 由ы꽩�븳�떎.
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

}

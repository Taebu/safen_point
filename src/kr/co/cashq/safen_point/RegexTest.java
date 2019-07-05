/**
 * 
 */
package kr.co.cashq.safen_point;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Taebu
 *
 */
public class RegexTest {
	
	
	public static void main(String[] args) {
		
		
		Map<String, String> messageMap=new HashMap<String, String>();
		messageMap.put("store.name","�Ǹ�����");
		messageMap.put("store.tel","050-8515-0493");
		messageMap.put("agencyMember.point_items","3�� 5,000������\n5�� 10,000������\n10�� 20,000������");

		String bt_content="[#{�����}]�� �̿����ּż� �����մϴ�.\n";
		bt_content+="������ȣ : [#{050��ȣ}]\n";
		bt_content+="\n";
		bt_content+="���Կ��� \"2,000��\" ���� �ص�Ƚ��ϴ�.\n";
		bt_content+="\"�������\" ���÷� �����Ͻø� Ȯ�ΰ����մϴ�.\n";
		bt_content+="\n";
		bt_content+="[���ο� �̼ǳ���]\n";
		bt_content+="#{�̼ǳ���}\n";
		bt_content+="�����Ⱓ : �ֹ��� ���� 60�� �� �Ҹ�\n";
		bt_content+="\n";
		bt_content+="���� Ʈ���忡 �°� ��������� �ֹ� �� ���� �������� ������ ���Ӱ� ��� �Ͽ����ϴ�.\n";
		bt_content+="���Ը��� ���� ��޾����� ���� Ȯ�� �ϼ���!\n";
		bt_content+="\n";
		bt_content+="������ ���� �ñ��� ���� 1599-9495 ���� ������ �ּ���\n";
		bt_content+="\n";
		bt_content+="\"�������\" �� ����Ʈ Ȯ�� ��ũ\n";
		bt_content+="http://bdtalk.co.kr/m/p/ ";

		String bt_regex="#{�����}=store.name&#{050��ȣ}=store.tel&#{�̼ǳ���}=agencyMember.point_items";		
		
		/* ���ø��� ������ ���ϴ�� ���� �մϴ�. 
		 * @param bt_content ���ø� ����, 
		 * @param bt_regex ���ø� ����
		 * @param messageMap ���ø� ������ �ٲ� ����
		 * */
		//String messages=chg_regexrule(bt_content,bt_regex, messageMap);
		//System.out.println(messages);
		
		String point_set="3_5000&5_10000&10_20000";
		String point_text=getPointSet(point_set);
		System.out.println(point_text);
		
		
	}

	/**
	 * @param point_set ="3_5000&5_10000&10_20000"
	 * @return
	 * 3�� 5,000��
	 * 5�� 10,000��
	 * 10�� 20,000��
	 */
	private static String getPointSet(String point_set) {
		// TODO Auto-generated method stub
		String[] regex_array=point_set.split("&");
		String[] keys;
		String returnValue="";

		/* bt_regex �� ũ�� ��ŭ �ݺ��Ͽ� ��ȯ�Ѵ�. */
		for (int i = 0; i < regex_array.length; i++) {
			keys=regex_array[i].split("_");
			returnValue=returnValue+keys[0]+"�� "+String.format("%,d", Integer.parseInt(keys[1]))+"��\n";
		}
		return returnValue;
	}

	/**
	 * @param bt_content
	 * �޼��� ������ ��ȯ�� �ؽ�Ʈ�� ���� �Ǿ� �ֽ��ϴ�. ������ ���� ������ ���� �Ǿ� �ֽ��ϴ�.
	 *  ������ �� 
	 *  #{�����}�� �̿��� �ּż� #{050��ȣ}
	 * 
	 * @param bt_regex
	 *  ���� ��Ģ�� �ֽ��ϴ�. bt_content���� ������ #{Ű��}�� ��� ������ �Ʒ��� ���� ��� ���� �Ǿ� �־�� �մϴ�.        
	 *  
	 *  ��) 
	 *  #{�����}=store.name&#{050��ȣ}=store.tel
	 *  
	 *  ��� �ΰ��� ��Ģ�� �����ϰ� #{�����}�� store.name�� ���� Ű�� �����մϴ�.  
	 * @param messageMap
	 *  ������ ������ store.name�� Ű�� �Լ� ȣ������ �Ʒ��� ���� ���·� ���� �Ǿ� �μ��� ���� �մϴ�.
	 *  Map<String, String> messageMap=new HashMap<String, String>();
		messageMap.put("store.name","�º�ġŲ");
	 * @return
	 */
	private static String chg_regexrule(String bt_content, String bt_regex, Map<String, String> messageMap) {
		// TODO Auto-generated method stub
		String[] regex_array=bt_regex.split("&");
		String[] keys;
		/* bt_regex �� ũ�� ��ŭ �ݺ��Ͽ� ��ȯ�Ѵ�. */
		for (int i = 0; i < regex_array.length; i++) {
			keys=regex_array[i].split("=");
			bt_content=bt_content.replace(keys[0], messageMap.get(keys[1]));
		}
		
		return bt_content;
	}

	

	
}

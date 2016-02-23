package com.moonic.util;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONObject;

import server.common.Sortable;
import server.common.Tools;

import com.ehc.common.SqlString;


public class DBUtil {
	public static final char[] compares2 = {'>', '<', '=', '!'};
	
	public static final String[] compares = {">=", "<=", "!=", "=", ">", "<", "<>"};
	
	/**
	 * ��SQL����ת��Ϊ�󶨱�����ʽ
	 */
	public static String convertWhere(String where, SqlString wStr){
		/*
		long t1 = System.currentTimeMillis();
		System.out.println("������--------------------------");
		System.out.println("--where1:"+where);
		*/
		String src_where = where;
		try{
			if(where != null && !where.equals("")){
				ArrayList<CData> cdata_arr = new ArrayList<CData>();
				for(int i = 0; i < compares.length; i++){
					int fromindex = 0;//��ָ��λ�ÿ�ʼ�����ȽϷ�
					while(true){
						//System.out.println("fromindex:"+fromindex+" where:"+where+" compares[i]:"+compares[i]);
						//����Ƿ����������ַ���
						if(fromindex == where.length()){
							break;
						}
						//���ϴα��滻������ʼλ�ã�����Ƿ񻹴���ָ���ȽϷ�
						int beginindex = where.indexOf(compares[i], fromindex);
						if(beginindex == -1){
							break;
						}
						//��ǰ���ҵıȽϷ�����Ϊһʱ���ж��ҵ��ıȽϷ��Ƿ�Ϊ��ʵ��һ���������
						if(compares[i].length() == 1){
							boolean next = false;
							char a = where.charAt(beginindex-1);
							char b = where.charAt(beginindex+1);
							for(int k = 0; k < compares2.length; k++){
								if(a == compares2[k] || b == compares2[k]){
									next = true;
									break;
								}
							}
							if(next){
								fromindex++;
								continue;
							}
						}
						//�����ֶ���
						int columnendindex = beginindex;
						do {
							columnendindex--;
						} while(where.charAt(columnendindex)==' ');
						int columnbeginindex = where.lastIndexOf(' ', columnendindex-1);
						/*
						if(columnbeginindex == -1){//����ʼ�ջ�+1���������ﲻ������
							columnbeginindex = 0;
						}
						*/
						while(where.charAt(columnbeginindex+1) == '('){
							columnbeginindex++;
						}
						String column = where.substring(columnbeginindex+1, columnendindex+1);
						//��ǰ���ҵıȽϷ�����Ϊ��ʱ������ʼָ���ƶ����ȽϷ����һλ
						if(compares[i].length() == 2){
							beginindex += compares[i].length()-1;
						}
						do {
							beginindex++;
						} while(where.charAt(beginindex)==' ');//���˿ո�ֱ��������ȷ����
						//���ұ��滻������ֹλ��+1����ȡ�ַ���ʱ���һλ������ȡ
						int endindex = 0;
						if(where.charAt(beginindex)=='\''){//��ȡ����Ϊ�ַ���ʱ����'�жϽ���λ��
							endindex = where.indexOf("'", beginindex+1)+1;
						} else 
						if(where.substring(beginindex).toLowerCase().startsWith("to_date")){//ʱ�����ڸ�ʽto_date('','')
							beginindex += 8;
							endindex = where.indexOf("'", beginindex+1)+1;
						} else {//��ȡ����Ϊ���ַ���ʱ���Կո��жϽ���λ��
							endindex = where.indexOf(" ", beginindex);
						}
						//�ж�ֵΪ��ϸʱ��ʱ������ʱ��Ŀո�
						if(where.charAt(beginindex)!='\'' && endindex!=-1 && beginindex+4<where.length() && (where.charAt(beginindex+4)=='-' || where.charAt(beginindex+4)=='/')){
							int timeindex = where.indexOf(":", endindex);
							if(timeindex != -1 && timeindex <= endindex+3){//�±��ں���ĳ��ַ�Χ��
								endindex = where.indexOf(" ", endindex+1);
							}
						}
						//����λ��Ϊ-1ʱ������Ϊ�ַ���ĩβ
						if(endindex == -1){
							endindex = where.length();
						}
						//����λ�õ�ǰһλΪ����ʱ��������ֱ���ƶ�����ȷ����
						while(where.charAt(endindex-1) == ')'){
							endindex--;
						}
						//��ȡֵ
						String value = where.substring(beginindex, endindex);
						//�����´β����ַ�������ʼλ��
						fromindex = endindex-value.length();
						//ȷ����������
						byte valType = 0;
						if(value.charAt(0) == '\''){//����ȡֵΪ�ַ���ʱ������'
							valType = 1;
						} else 
						if(value.indexOf(':') != -1){//����ȡ����Ϊ���ַ���ʱ������ʹ��
							valType = 2;
						} else 
						if((value.indexOf('-') != 0 && value.indexOf('-') != -1) || value.indexOf('/') != -1 || value.charAt(0)=='?'){
							valType = 3;
						} else 
						if(value.equals("?")){
							valType = 4;
						} else 
						if(value.matches("^[-+]?(([0-9]+)([.]([0-9]+))?|([.]([0-9]+))?)$")){
							valType = 5;
						} else {
							continue;
						}
						//��װ����
						CData cdata = new CData();
						cdata.valType = valType;
						cdata.index = beginindex;
						cdata.column = column;
						cdata.value = value;
						cdata.compare = compares[i];
						cdata_arr.add(cdata);
						for(int t = 0; t < cdata_arr.size(); t++){
							CData obj = cdata_arr.get(t);
							if(obj.index > beginindex){
								obj.index -= value.length()-1;
							}
						}
						//System.out.println("column="+column+" value="+value);
						//����ƴ�ӣ��������
						StringBuffer sb = new StringBuffer();
						sb.append(where.substring(0, beginindex));
						sb.append("?");
						sb.append(where.substring(endindex, where.length()));
						where = sb.toString();
					}
				}
				CData[] arr = cdata_arr.toArray(new CData[cdata_arr.size()]);
				Tools.sort(arr, 0);
				for(int i = 0; i < arr.length; i++){
					if(arr[i].valType == 1){//����ȡֵΪ�ַ���ʱ������'
						wStr.add(arr[i].column, arr[i].value.substring(1, arr[i].value.length()-1), arr[i].compare);
					} else 
					if(arr[i].valType == 2){//����ȡ����Ϊ���ַ���ʱ������ʹ��
						wStr.addDateTime(arr[i].column, arr[i].value, arr[i].compare);
					} else 
					if(arr[i].valType == 3){
						wStr.addDate(arr[i].column, arr[i].value, arr[i].compare);
					} else 
					if(arr[i].valType == 4){
						wStr.add(arr[i].column, arr[i].value, arr[i].compare, SqlString.DATATYPE_CUSTOM);
					} else 
					if(arr[i].valType == 5){
						wStr.add(arr[i].column, arr[i].value, arr[i].compare, SqlString.DATATYPE_NUMBER);
					}
				}
			}
			/*
			long t2 = System.currentTimeMillis();
			System.out.println("--where2:"+where);
			System.out.println("--wStrCol:"+wStr.colString());
			System.out.println("--wStrVal:"+wStr.valueString());
			System.out.println("--ת����ʱ��" + (t2-t1));
			System.out.println("������--------------------------");
			*/
			return where;	
		} catch (Exception e) {
			System.out.println("�����������ʧ�ܣ�����ԭʼ���  where="+src_where);
			e.printStackTrace();
			return src_where;
		}
	}
	
	/**
	 * ת�����ݰ�
	 * @author John
	 */
	static class CData implements Sortable {
		protected byte valType;
		protected int index;
		protected String column;
		protected String value;
		protected String compare;
		public double getSortValue() {return index;}
	}
	
	/**
	 * ��ȡ�淶�����ݵ��ַ�����ʽ
	 */
	public static String getFormatStr(String tab, JSONArray jsonarr) {
		try {
			if(jsonarr == null){
				return "";
			}
			JSONObject colobj = colmap.optJSONObject(tab);
			JSONArray colarr = converColobjToArr(colobj);
			StringBuffer sb = new StringBuffer();
			for(int i = 0; i < colarr.length(); i++){
				sb.append(colarr.optString(i)+"\t");
			}
			sb.append("\r\n");
			//System.out.println("jsonarr.length():"+jsonarr.length()+" jsonarr:"+jsonarr+" tab:"+tab);
			for(int k = 0; k < jsonarr.length(); k++){
				JSONArray arr = jsonarr.optJSONArray(k);
				for(int i = 0; i < colarr.length(); i++){
					//System.out.println(" k:"+k+" i:"+i);
					sb.append((arr.opt(i)!=null?arr.opt(i):"������")+"\t");
				}
				sb.append("\r\n");
			}
			return sb.toString();
		} catch (Exception e) {
			e.printStackTrace();
			return e.toString();
		}
	}
	
	/**
	 * ��COLOBJת��ΪCOLARR
	 */
	public static JSONArray converColobjToArr(JSONObject colobj) throws Exception {
		@SuppressWarnings("unchecked")
		Iterator<String> iterator = colobj.keys();
		JSONArray colarr = new JSONArray();
		while(iterator.hasNext()){
			String col = iterator.next();
			colarr.put(colobj.optInt(col), col);
		}
		return colarr;
	}
	
	/**
	 * ��ѯ
	 */
	public static JsonRs sQuery(String table, String target, String where) throws Exception {
		return sQuery(table, target, where, null, null, 0, 0);
	}
	
	/**
	 * ��ѯ
	 */
	public static JsonRs sQuery(String table, String target, String where, String order, String group, int minRow, int maxRow) throws Exception {
		DBHelper dbHelper = new DBHelper();
		try {
			dbHelper.openConnection();
			ResultSet rs = dbHelper.query(table, target, where, order, group, minRow, maxRow);
			return convertRsToJsonRs(rs);
		} catch (Exception e) {
			throw e;
		} finally {
			dbHelper.closeConnection();
		}
	}
	
	/**
	 * ��Rsת��ΪJsonRs
	 */
	public static JsonRs convertRsToJsonRs(ResultSet rs) throws Exception {
		return new JsonRs(convertRsToJsonarr(rs));
	}
	
	/**
	 * ��RSת��ΪJSONARR
	 */
	public static JSONArray convertRsToJsonarr(ResultSet rs) throws Exception {
		JSONArray jsonarr = new JSONArray();
		ResultSetMetaData rsmd = rs.getMetaData();
		int colCount = rsmd.getColumnCount();
		if(rs.getRow() != -1){
			rs.beforeFirst();
		}
		while(rs.next()){
			JSONObject obj = new JSONObject();
			for (int i = 1; i <= colCount; i++) {
				String colName = rsmd.getColumnName(i);
				int colType = rsmd.getColumnType(i);
				if(colType == Types.BLOB){
					obj.put(colName, new String(rs.getBytes(i), "UTF-8"));
				} else 
				{
					obj.put(colName, rs.getString(i));
				}
			}
			jsonarr.add(obj);
		}
		return jsonarr;
	}
	
	/**
	 * ��RSת��ΪJSONOBJ
	 */
	public static JSONObject convertRsToJsonobj(ResultSet rs) throws Exception {
		JSONObject obj = null;
		ResultSetMetaData rsmd = rs.getMetaData();
		int colCount = rsmd.getColumnCount();
		if(rs.getRow() != -1){
			rs.beforeFirst();
		}
		if(rs.next()) {
			obj = new JSONObject();
			for (int i = 1; i <= colCount; i++) {
				String colName = rsmd.getColumnName(i);
				int colType = rsmd.getColumnType(i);
				if(colType == Types.BLOB){
					obj.put(colName, new String(rs.getBytes(i), "UTF-8"));
				} else 
				{
					obj.put(colName, rs.getString(i));
				}
			}
		}
		return obj;
	}
	
	public static JSONObject colmap = new JSONObject();
	public static JSONObject coltypemap = new JSONObject();
	
	/**
	 * �����ֶ���Ϣ
	 */
	public static void clearColData(String tab){
		if(colmap.has(tab)){
			colmap.remove(tab);
			coltypemap.remove(tab);
		}
	}
	
	/**
	 * ��RSת��Ϊ�淶��JSONARR(��ǰ�������ѯĿ��ΪNULL[*]�Ĳ�ѯ������ô˷���)
	 */
	public static JSONArray convertRsToFormat(String tab, ResultSet rs) throws Exception {
		JSONArray jsonarr = new JSONArray();
		ResultSetMetaData rsmd = rs.getMetaData();
		int colCount = rsmd.getColumnCount();
		if(rs.getRow() != -1){
			rs.beforeFirst();
		}
		if(!colmap.has(tab)){
			JSONObject colobj = new JSONObject();
			JSONArray coltypearr = new JSONArray();
			for(int i = 1; i <= colCount; i++){
				String colName = rsmd.getColumnName(i);
				int colType = rsmd.getColumnType(i);
				colobj.put(colName, i-1);
				coltypearr.add(colType);
			}
			colmap.put(tab, colobj);
			coltypemap.put(tab, coltypearr);
		}
		while(rs.next()){
			JSONArray arr = new JSONArray();
			for (int i = 1; i <= colCount; i++) {
				int colType = rsmd.getColumnType(i);
				if(colType == Types.BLOB){
					arr.add(new String(rs.getBytes(i), "UTF-8"));
				} else 
				if(colType == Types.DATE ||colType == Types.TIME || colType == Types.TIMESTAMP){
					String val = rs.getString(i);
					if(val == null){
						arr.add(null);
					} else {
						arr.add(MyTools.getTimeLong(val));
					}
				} else 
				if(colType == Types.NUMERIC) {
					String val = rs.getString(i);
					if(val == null){
						arr.add(0);
					} else {
						arr.add(val);
					}
				} else 
				{
					arr.add(rs.getString(i));
				}
			}
			jsonarr.add(arr);
		}
		return jsonarr;
	}
	
	private static final String[] Q_COMP_SPLIT = {" is ", ">=", "<=", "!=", "=", ">", "<"};//��ַ���
	private static final String[] Q_COMP_USE = {"=", ">=", "<=", "!=", "=", ">", "<"};//ʵ��ʹ�÷���
	
	private static MyLog jsonQueryLog = new MyLog(MyLog.NAME_DATE, "jsonquery", "JSONQUERY", true, false, true, null);
	
	/**
	 * ����JSON����Դ��SQL��ѯ
	 */
	public static JSONArray jsonQuery(String tab, JSONArray jsonarr, String where, String order, int minRow, int maxRow) throws Exception {
		long t1 = System.currentTimeMillis();
		int len = jsonarr.length();
		JSONObject colobj = colmap.optJSONObject(tab);
		JSONArray coltype = coltypemap.optJSONArray(tab);
		jsonarr = jsonQuery(colobj, coltype, jsonarr, where, order, minRow, maxRow);
		long t2 = System.currentTimeMillis();
		if(t2-t1>5){
			jsonQueryLog.d("JSON��ѯ��ʱ��"+(t2-t1)+" ��"+tab+" ������"+where+" ������:"+len);
		}
		return jsonarr;
	}
	
	/**
	 * ����JSON����Դ��SQL��ѯ
	 * !!!��ע������ѯ�����ݼ������ʱ�����͵��ֶΣ���Ӧֵ����Ϊ������ʽ!!!
	 */
	public static JSONArray jsonQuery(JSONObject colobj, JSONArray coltype, JSONArray jsonarr, String where, String order, int minRow, int maxRow) throws Exception {
		//System.out.println("src:\r\n"+MyTools.getFormatJsonarrStr(jsonarr));
		if(where != null){
			//where = where.toLowerCase();
			JSONArray rootarr = new JSONArray();//�����ֽ���
			//["and",["and","serverid=1"]]
			//["and",["and","serverid=1","savetime>2015-03-08 23:56:55"]]
			splitBracket(rootarr, rootarr, where, 1);
			//{"resetdate":26,"sex":19,"buycoin":9...}
			//System.out.println("where:"+rootarr);
			//[2,2,2,93,12...]
			//System.out.println("colobj:"+colobj);
			//System.out.println("coltype:"+coltype);
			//������ѯ
			boolean needCheckWhere = true;//�Ƿ���Ҫ������ͨ��ѯ
			if(rootarr.optString(0).equals("and")){
				JSONArray arr1 = rootarr.optJSONArray(1);
				if(arr1.optString(0).equals("and")){//��һ��������ϵΪAND����
					Object obj = null;
					while((obj = arr1.opt(1)) instanceof String){//��һ��������Ƕ������
						String subWhere = obj.toString();//��ȡ�����ַ���
						//System.out.println(subWhere);
						JSONArray temparr2 = new JSONArray();
						//long t1 = System.currentTimeMillis();
						//long tx = 0;
						//long ty = 0;
						String[] spWhere = splitSubWhere(colobj, coltype, subWhere);//�и�������
						//savetime > 1425830215000
						//System.out.println(spWhere[0]+" "+spWhere[1]+" "+spWhere[2]);
						int col_index = colobj.optInt(spWhere[0]);//��ȡ�����ֶ��±�
						for(int i = 0; i < jsonarr.length(); i++){//ѭ����ѯĿ�꼯��
							JSONArray json = jsonarr.getJSONArray(i);//��ȡ��ѯĿ��
							//long ta1 = System.currentTimeMillis();
							String value = json.optString(col_index);//��ȡ����Ŀ��ֵ
							//long ta2 = System.currentTimeMillis();
							boolean match = checkSubWhere(colobj, coltype, spWhere, value);//����ƥ����
							//long ta3 = System.currentTimeMillis();
							//tx += ta2-ta1;
							//ty += ta3-ta2;
							if(match){
								temparr2.add(json);//��ƥ������ݼ��뵽��ʱ����
							}
						}
						//long t2 = System.currentTimeMillis();
						//System.out.println("ץȡ��ʱ��"+(t2-t1)+" �ж���ʱ��"+ty+" ȡֵ��ʱ��"+tx);
						jsonarr = temparr2;//ȷ�ϲ�ѯ���
						arr1.remove(1);//���������Ӳ�ѯ�������Ƴ�
						if(arr1.length() <= 1){//���ֻʣ��һ��AND����ѯ����������Ҫ�ڽ�����ͨ��ѯ
							needCheckWhere = false;
							break;
						}
					}
				}
			}
			//System.out.println("rootarr:"+rootarr);
			//��ͨ��ѯ
			if(needCheckWhere){
				JSONArray temparr = new JSONArray();
				for(int i = 0; i < jsonarr.length(); i++){//������ѯĿ�꼯��
					JSONArray json = jsonarr.getJSONArray(i);//��ȡ��ѯĿ��
					JSONArray userootarr = new JSONArray(rootarr.toString());//���ɲ�ѯ����
					boolean match = checkWhere(colobj, coltype, json, userootarr, userootarr, 1);
					if(match){
						temparr.add(json);
					}
				}
				jsonarr = temparr;
				//System.out.println("where:\r\n"+MyTools.getFormatJsonarrStr(jsonarr));		
			}
		} else {
			JSONArray temparr = new JSONArray();
			for(int i = 0; i < jsonarr.length(); i++){
				JSONArray json = jsonarr.getJSONArray(i);
				temparr.add(json);
			}
			jsonarr = temparr;
			//System.out.println("where:\r\n"+MyTools.getFormatJsonarrStr(jsonarr));
		}
		if(order != null){
			String[][] groups = Tools.splitStrToStrArr2(order, ",", " ");
			for(int i = 0; i < groups.length; i++){
				order = order.replace(groups[i][0], colobj.optString(groups[i][0]));
			}
			jsonarr = MyTools.sortJSONArray(jsonarr, order);
			//System.out.println("order:\r\n"+MyTools.getFormatJsonarrStr(jsonarr));
		}
		if(minRow>0 && maxRow>0){
			if(minRow <= maxRow){
				JSONArray temparr = new JSONArray();
				for(int i = 0; i < jsonarr.length(); i++){
					if(i >= (minRow-1) && i <= (maxRow-1)){
						temparr.add(jsonarr.optJSONArray(i));
					}
				}
				jsonarr = temparr;
			} else {
				BACException.throwInstance("��ѯ�кŲ�������"+minRow+","+maxRow);
			}
			//System.out.println("row:\r\n"+MyTools.getFormatJsonarrStr(jsonarr));
		}
		return jsonarr;
	}
	
	/**
	 * �ֽ�����
	 */
	private static void splitBracket(JSONArray rootarr, JSONArray jsonarr, String where, int nestlayer) throws Exception {
		if(where == null){
			return;
		}
		JSONArray subarr = new JSONArray();
		while(true){
			int startindex = where.indexOf('(');
			if(startindex == - 1){
				break;
			}
			int endindex = 0;
			int layer = 0;
			int fromindex = startindex+1;
			while(true){
				int start = where.indexOf('(', fromindex);
				int end = where.indexOf(')', fromindex);
				
				int index = 0;
				if(start == -1 && end == -1){
					throw new Exception("��������ʧ��");
				} else 
				if(start == -1){
					index = end;
				} else 
				if(end == -1){
					index = start;
				} else 
				{
					index = Math.min(start, end);
				}
				if(index == start){
					layer++;
				} else 
				if(index == end){
					if(layer > 0){
						layer--;
					} else {
						endindex = index;
						break;
					}
				}
				fromindex = index+1;
			}
			String leftStr = null;
			String rightStr = null;
			if(startindex != 0){
				leftStr = where.substring(0, startindex);
			} else {
				leftStr = "";
			}
			if(endindex != where.length()-1){
				rightStr = where.substring(endindex+1);
			} else {
				rightStr = "";
			}
			JSONArray thearr = new JSONArray();
			splitBracket(rootarr, thearr, where.substring(startindex+1, endindex), nestlayer+1);
			subarr.add(thearr);
			where = leftStr + "?" + rightStr;
			//System.out.println("where:"+where+" "+nestlayer);
			//System.out.println("subarr:"+subarr+" "+nestlayer);
		}
		int use = 0;
		String[] orwhere = where.split("\\s+(o|O)(r|R)\\s+");
		if(orwhere.length > 1){
			jsonarr.add("or");
		} else {
			jsonarr.add("and");
		}
		for(int k = 0; orwhere != null && k < orwhere.length; k++){
			String[] subwhere = orwhere[k].split("\\s+(a|A)(n|N)(d|D)\\s+");
			JSONArray onearr = new JSONArray();
			onearr.add("and");
			for(int i = 0; i < subwhere.length; i++){
				if(subwhere[i].equals("?")){
					onearr.add(subarr.opt(use++));
				} else {
					onearr.add(subwhere[i]);
				}
			}
			jsonarr.add(onearr);
		}
		//System.out.println("aft jsonarr:"+jsonarr+" "+nestlayer);
	}
	
	/**
	 * ����Ƿ���������
	 */
	private static boolean checkWhere(JSONObject colobj, JSONArray coltype, JSONArray json, JSONArray rootarr, JSONArray jsonarr, int nestlayer) throws Exception {
		for(int i = 0; i < jsonarr.size(); i++){
			Object obj = jsonarr.opt(i);
			if(obj instanceof JSONArray){
				boolean meet = checkWhere(colobj, coltype, json, rootarr, (JSONArray)obj, nestlayer+1);
				jsonarr.put(i, meet);
			}
		}
		String connnect = jsonarr.optString(0);
		boolean result = true;
		for(int i = 1; i < jsonarr.size(); i++){
			String str = jsonarr.optString(i);
			boolean meet = checkSubWhere(colobj, coltype, json, str);
			if(connnect.equals("and")){
				if(!meet){
					result = false;
					break;
				} else {
					result = true;
				}
			} else 
			if(connnect.equals("or")){
				if(meet){
					result = true;
					break;
				} else {
					result = false;
				}
			}
		}
		//System.out.println("the b:"+rootarr+" layer:"+nestlayer);
		return result;
	}
	
	/**
	 * ����Ƿ�����������
	 */
	private static boolean checkSubWhere(JSONObject colobj, JSONArray coltype, JSONArray json, String where) throws Exception {
		if(where.equals("true")){
			return true;
		} else 
		if(where.equals("false")){
			return false;
		} else {
			String[] spWhere = splitSubWhere(colobj, coltype, where);//�и�����
			String value = json.optString(colobj.optInt(spWhere[0]));//��ȡ����ֵ
			return checkSubWhere(colobj, coltype, spWhere, value);//����Ƿ���������
		}
	}
	
	/**
	 * ����Ƿ�����������
	 * @param colobj �ֶ���-�ֶ������±�
	 * @param coltype �ֶ�����
	 * @param column �ֶ���
	 * @param value ����ֵ(������ֵ)
	 */
	private static boolean checkSubWhere(JSONObject colobj, JSONArray coltype, String[] spWhere, String value) throws Exception {
		if(value.equals("")){
			int type = coltype.optInt(colobj.optInt(spWhere[0]));
			if(type == Types.DATE || type == Types.TIME || type == Types.TIMESTAMP){
				value = "0";
			} else 
			if(type == Types.NUMERIC){
				value = "0";
			}
		}
		String val1 = value;//����ֵ converValue(colobj, coltype, spWhere[0], value)
		String val2 = spWhere[2];//����ֵ
		String comp = spWhere[1];//�ж�����
		if(comp.equals("=")){
			return val1.equals(val2);
		} else
		if(comp.equals("!=")){
			return !val1.equals(val2);
		} else 
		if(comp.equals(">=")){
			return Double.valueOf(val1)>=Double.valueOf(val2);
		} else 
		if(comp.equals("<=")){
			return Double.valueOf(val1)<=Double.valueOf(val2);
		} else 
		if(comp.equals(">")){
			return Double.valueOf(val1)>Double.valueOf(val2);
		} else
		if(comp.equals("<")){
			return Double.valueOf(val1)<Double.valueOf(val2);
		}
		throw new RuntimeException("����������������"+comp);
	}
	
	/**
	 * �и�������
	 * @param colobj �ֶ���-�ֶ������±�
	 * @param coltype �ֶ�����
	 * @param where ����
	 * @return ��ֽ��
	 */
	public static String[] splitSubWhere(JSONObject colobj, JSONArray coltype, String where) throws Exception {
		String[] spWhere = new String[3];//��ֽ��
		for(int c = 0; c < Q_COMP_SPLIT.length; c++){
			if(where.indexOf(Q_COMP_SPLIT[c])!=-1){
				String[] kv = Tools.splitStr(where, Q_COMP_SPLIT[c]);
				spWhere[0] = kv[0];
				spWhere[1] = Q_COMP_USE[c];
				spWhere[2] = converValue(colobj, coltype, kv[0], kv[1]);
				break;
			}
		}
		return spWhere;
	}
	
	/**
	 * ת��ֵ
	 * @param colobj �ֶ���-�ֶ������±�
	 * @param coltype �ֶ�����
	 * @param column �ֶ���
	 * @param value �ֶ�ֵ
	 */
	public static String converValue(JSONObject colobj, JSONArray coltype, String column, String value) throws Exception {
		if(!colobj.has(column)){
			BACException.throwAndPrintInstance("��Ч��ʶ����"+column+"��");
		}
		int type = coltype.optInt(colobj.optInt(column));
		if(type == Types.VARCHAR || type == Types.NVARCHAR){
			if(value.equals("null")){
				value = "";
			} else 
			if(value!=null && !value.equals("") && value.charAt(0)=='\''){//���ܷ�������ȥ���ţ�����null�ַ�������ת��Ϊ""
				value = value.substring(1, value.length()-1);//ȥ���ַ������ݵ�����
			}
		} else 
		if(type == Types.DATE || type == Types.TIME || type == Types.TIMESTAMP){
			value = String.valueOf(MyTools.getTimeLong(value));
		} else 
		if(type == Types.NUMERIC){
			if(value == null || value.equals("null") || value.equals("")){
				value = String.valueOf(0);
			} else 
			if(value!=null && !value.equals("") && value.charAt(0)=='\''){//���ܷ�������ȥ���ţ�����null�ַ�������ת��Ϊ""
				value = value.substring(1, value.length()-1);//ȥ���ַ������ݵ�����
			}
		}
		return value;
	}
	
	public static void main(String[] args){
		//��һ����ʱ�϶��������ΪҪ������
		String where = "playerid=792 and (starttime=2013-07-19 09:20:00 or starttime=2013-07-19 00:00:00)";
		//String where = "table_name=upper('tab_partner') order by column_position";
		//for(int i = 0; i < 10; i++)
		{
			long t1 = System.currentTimeMillis();
			System.out.println("where1:"+where);
			SqlString wStr = new SqlString();
			where = convertWhere(where, wStr);
			
			long t2 = System.currentTimeMillis();
			
			System.out.println("where2:"+where);
			System.out.println("wStr:"+wStr.whereString());
			System.out.println("ת��������ʱ�� " + (t2-t1));
		}
	}
}

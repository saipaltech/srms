package org.saipal.srms.parser;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import jakarta.servlet.http.HttpServletRequest;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.saipal.srms.util.DB;
import org.saipal.srms.util.FmisSession;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class RequestParser {

	// private String syncId = "";

	@Autowired
	DB db;
	
	@Autowired
	ApplicationContext context;
	
	@Autowired
	FmisSession fmisSession;

	org.slf4j.Logger log = LoggerFactory.getLogger(RequestParser.class);

	public void setRequestParser(HttpServletRequest request) {
		DocumentStore ds = context.getBean(DocumentStore.class);
		//Added on 17 Sep by Pankaj Begin
		HashMap<String, String[]> parmData=new HashMap<String,String[]>();;
		String _bigdata=""+request.getParameter("m_bigdata");
		if(_bigdata.equals("1")) {
			Map<String,String> requestParm=new HashMap<String,String>();
			String b64data=request.getParameter("m_rawdata");
			//byte[] decoder = Base64.getDecoder().decode(b64data);
			String str=b64data;
			
			String[] _rawdata=str.split("&");
			for(int i=0;i<_rawdata.length;i++) {
				String[] keyA=_rawdata[i].split("=");
				
				String key="";
				String value="";
				if(keyA.length>1) {
					key=keyA[0].trim();
					value=keyA[1];
				}
				else {
					key=keyA[0].trim();
					value="";
				}
				
				try {
					value=URLDecoder.decode(value,"UTF-8");
				} catch (UnsupportedEncodingException e1) {
					e1.printStackTrace();
				}
					
				
				if(requestParm.containsKey(key)) {
						value=requestParm.get(key)+":;:"+ value;
						
				}
				requestParm.put(key, value);
			}
			
			for(Map.Entry<String,String> entry : requestParm.entrySet()) {
				parmData.put(entry.getKey(), entry.getValue().split(":;:"));
			}
		}
		else {
			for (Entry<String, String[]> entry : request.getParameterMap().entrySet()) {
				parmData.put(entry.getKey(), entry.getValue());
			}
			
		}
		//Added on 17 Sep by Pankaj End
		
		// HashMap<String, Element> elements = new HashMap<String, Element>();
		// HashMap<String, DataGrid> grids = new HashMap<String, DataGrid>();
		// Map<String, String[]> reqParam = request.getParameterMap();
		Map<String, Integer> gridMap = new HashMap<String, Integer>();
		//for (Entry<String, String[]> entry : request.getParameterMap().entrySet()) {//OriginalCode
		for(Map.Entry<String, String[]> entry : parmData.entrySet()) {
			String k = entry.getKey();
			String[] v = entry.getValue();
			Element element = context.getBean(Element.class);
			element.setId(k);
			element.setName(k);
			if (v.length == 1) {
				element.setValueAuto(v[0]);
			} else {
				String s = Arrays.toString(v);
				s = s.substring(1, s.length() - 1);
				element.setValueAuto(s);
			}

			element.setRp(this);
			ds.elements.put(k, element);
			// check if grid exists
			if (k.contains("_value") || k.contains("_rowid") || k.contains("_colname") || k.contains("_datatype")) {
				String gridName = k.split("_")[0];
				if (gridMap.containsKey(gridName)) {
					gridMap.put(gridName, gridMap.get(gridName) + 1);
				} else {
					gridMap.put(gridName, 1);
				}
			}
		}
		// ds.addElements(elements);

		// parse all the grids
		if (gridMap.size() > 0) {
			for (Entry<String, Integer> ent : gridMap.entrySet()) {
				if (ent.getValue() == 4) {
					String gid = ent.getKey();
					String cols = getElementById(gid + "_colname").value;
					String dtypes = getElementById(gid + "_datatype").value;
					DataGrid dg = new DataGrid(gid, cols, dtypes);
					dg.setRp(this);
					ds.grids.put(gid, dg);
				}
			}
			// ds.addGrids(grids);
		}

	}

	public Element getElementById(String id) {
		DocumentStore ds = context.getBean(DocumentStore.class);
		// Element el = ds.getElement(id);
		Element el = ds.elements.get(id);
		if (el == null) {
			el = context.getBean(Element.class);
			el.setId(id);
			el.setName(id);
			el.setRp(this);
			// ds.addElement(el);
			ds.elements.put(id, el);
		}
		return el;
	}

	public DataGrid getGrid(String id) {
		DocumentStore ds = context.getBean(DocumentStore.class);
		return ds.grids.get(id);
		// return ds.getGrid(id);
	}

//	public String maketable(String gridId) {
//		return getGrid(gridId).makeTable(db, fmisSession.session("requestid") + "");
//		//return "##tbl"+fmisSession.session("requestid");
//	}

	public String getSyncId() {
		return fmisSession.session("requestid");
	}

//	public void setSyncId(String syncId) {
//		this.syncId = syncId;
//	}

	public void alert(String msg) {
		opToJs("alert", msg);
	}

	public void js(String jscript) {
		opToJs("js", jscript);
	}

	// Added new function Begin
	public void setNestedExecution(boolean p) {
		opToJs("nested", p);
	}

	public boolean getNestedExecution() {
		DocumentStore ds = context.getBean(DocumentStore.class);
		return ds.nestedExecution;
	}

	public void setRecursiveExecution(boolean p) {
		opToJs("recursive", p);
	}

	public boolean getRecursiveExecution() {
		DocumentStore ds = context.getBean(DocumentStore.class);
		return ds.recursiveExecution;
	}

	// Added new function End
	public void addGridRow(String gridName, String value, String index) {
		opToJs("addGridRow", gridName + "::" + value + "::" + index);
	}

	public void additem(String elementId, Object text, Object value) {
		opToJs(elementId, "addItem", text + "::" + value);
	}

	public void addoptiongroup(String elementId, Object groupId, Object groupName) {
		opToJs(elementId, "addoptiongroup", groupId + "::" + groupName);
	}

	public void addgroupitem(String elementId, Object text, Object value, Object groupid) {
		opToJs(elementId, "addgroupitem", text + "::" + value + "::" + groupid);
	}

	public void removeall(String elementId) {
		opToJs(elementId, "removeAll", null);
	}

	public void resetItem(String elementId) {
		removeall(elementId);
		additem(elementId, "............", "0");
	}

	public String getJs() {
		DocumentStore ds = context.getBean(DocumentStore.class);
		return ds.jsLog.toString();
	}

	protected void opToJs(String operation, boolean parms) {
		DocumentStore ds = context.getBean(DocumentStore.class);
		if ("nested".equals(operation)) {
			ds.nestedExecution = parms;

		} else if ("recursive".equals(operation)) {
			ds.recursiveExecution = parms;
		}
	}

	protected void opToJs(String elementid, String operation, String params) {
		DocumentStore ds = context.getBean(DocumentStore.class);
		String opJs = "";

		/*
		 * if(jsa.length() == 0) { try { jsa.put(elementlist);
		 * elementlist.put("elements", elemarray); } catch (JSONException e) {
		 * e.printStackTrace(); } }
		 */
		if ("addItem".equals(operation)) {
			if (params != null) {
				String[] pms = params.split("::");
				if (pms.length > 1) {
					String js = "additem('" + elementid + "','" + pms[0].replace("'", "\\'") + "','" + pms[1] + "');\n";
					ds.jsLog.append(js);
					// insetToDb(js);
					setJSON(elementid, operation, pms[0], pms[1]);
				} else {
					String js = "additem('" + elementid + "','" + pms[0].replace("'", "\\'") + "','" + "');\n";
					ds.jsLog.append(js);
					// insetToDb(js);
					setJSON(elementid, operation, pms[0], "");
				}
			}
		} else if ("removeItem".equals(operation)) {
			if (params != null) {
				String js = "removeitem('" + elementid + "','" + params + "');\n";
				ds.jsLog.append(js);
				// insetToDb(js);
				setJSON(elementid, "removeitem", params);
			}
		} else if ("src".equals(operation)) {
			if (params != null) {
				String js = "document.getElementById('" + elementid + "').src = '" + params + "';\n";
				ds.jsLog.append(js);
				// insetToDb(js);
				setJSON(elementid, "src", params);
			}
		} else if ("removeAll".equals(operation)) {
			String js = "removeall('" + elementid + "');\n";
			ds.jsLog.append(js);
			// insetToDb(js);
			setJSON(elementid, "removeall", "true");
		} else if ("setDisabled".equals(operation)) {
			if (params != null) {
				String js = "document.getElementById('" + elementid + "').disabled = " + params + ";\n";
				ds.jsLog.append(js);
				// insetToDb(js);
				setJSON(elementid, "disabled", params);

			}
		} else if ("setValue".equals(operation)) {
			if (params != null) {
				String js = "document.getElementById('" + elementid + "').value = '" + params.replace("'", "\\'")
						+ "';\n";
				ds.jsLog.append(js);
				// insetToDb(js);
				setJSON(elementid, "value", params);

			}
			
		}
		else if ("setValue2".equals(operation)) {
			if (params != null) {
				String js = "document.getElementById('" + elementid + "').setvalue('" + params.replace("'", "\\'")
						+ "');\n";
				ds.jsLog.append(js);
				// insetToDb(js);
				setJSON(elementid, "value", params);

			}
			
		}else if ("focus".equals(operation)) {
			String js = "document.getElementById('" + elementid + "').focus();\n";
			ds.jsLog.append(js);
			// insetToDb(js);
		} else if ("submit".equals(operation)) {
			String js = "document.getElementById('" + elementid + "').submit();\n";
			ds.jsLog.append(js);
			// insetToDb(js);
		} else if ("reset".equals(operation)) {
			String js = "document.getElementById('" + elementid + "').reset();\n";
			ds.jsLog.append(js);
			// insetToDb(js);
		} else if ("setClassName".equals(operation)) {
			if (params != null) {
				String js = "document.getElementById('" + elementid + "').classname = '" + params + "';\n";
				ds.jsLog.append(js);
				// insetToDb(js);
				setJSON(elementid, "classname", params);
			}
		} else if ("setChecked".equals(operation)) {
			if (params != null) {
				String js = "document.getElementById('" + elementid + "').checked = " + params + ";\n";
				ds.jsLog.append(js);
				// insetToDb(js);
				setJSON(elementid, "checked", params);

			}
		} else if ("setInnerHTML".equals(operation)) {
			if (params != null) {
				String js = "document.getElementById('" + elementid + "').innerHTML = '"
						+ params.replace("\n", "").replace("\r", "") + "';\n";
				ds.jsLog.append(js);
				// insetToDb(js);
				setJSON(elementid, "innerHTML", params);

			}
		} else if ("alert".equals(operation)) {
			if (params != null) {
				String js = "alert('" + params.replace("'", "\\'").replaceAll("\n", " ") + "');\n";
				ds.jsLog.append(js);
				// insetToDb(js);
			}
		} else if ("js".equals(operation)) {
			if (params != null) {
				String js = params + "\n";
				ds.jsLog.append(js);
				// insetToDb(js);
			}
		} else if ("addoptiongroup".equals(operation)) {
			if (params != null) {
				String[] pms = params.split("::");
				if (pms.length > 1) {
					String js = "addoptiongroup('" + elementid + "','" + pms[0] + "','" + pms[1] + "');\n";
					ds.jsLog.append(js);
					// insetToDb(js);
					setJSON(elementid, operation, pms[0], pms[1]);
				} else {
					String js = "addoptiongroup('" + elementid + "','" + pms[0] + "','" + "');\n";
					ds.jsLog.append(js);
					// insetToDb(js);
					setJSON(elementid, operation, pms[0], "");
				}

			}
		} else if ("addgroupitem".equals(operation)) {
			if (params != null) {
				String[] pms = params.split("::");
				if (pms.length > 2) {
					String js = "additem('" + elementid + "','" + pms[0] + "','" + pms[1] + "','','" + pms[2] + "');\n";
					ds.jsLog.append(js);
					// insetToDb(js);
					// setJSON(elementid, operation, pms[0], pms[1], pms[2]);
				} else {
					String js = "additem('" + elementid + "','" + pms[0] + "','" + pms[1] + "','" + "');\n";
					ds.jsLog.append(js);
					// insetToDb(js);
					setJSON(elementid, operation, pms[0], pms[1], "");
				}
			}
		} else if ("addGridRow".equals(operation)) {
			String[] pms = params.split("::");
			if (pms.length == 3) {
				String js = pms[0] + ".addrow(\"" + pms[1] + "\"," + pms[2] + ");";
				ds.jsLog.append(js);
				// insetToDb(js);
			}
		}
	}

	protected void opToJs(String operation, String params) {
		opToJs("", operation, params);
	}

	// JSON Begin
	protected void setJSON(String elementid, String operation, String params) {
		// json
		DocumentStore ds = context.getBean(DocumentStore.class);
		if (ds.json.has(elementid)) {
			try {
				JSONObject el = ds.json.getJSONObject(elementid);
				el.put(operation, params);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		} else {
			JSONObject el = new JSONObject();
			try {
				el.put(operation, params);
				ds.json.put(elementid, el);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		// end json
	}

	protected void setJSON(String elementid, String operation, String parmtext, String parmvalue) {
		// json
		DocumentStore ds = context.getBean(DocumentStore.class);
		if ("addItem".equals(operation)) {
			if (ds.json.has(elementid)) {
				try {
					JSONObject el = ds.json.getJSONObject(elementid);
					if (el.has("listitem")) {
						JSONObject listitems = el.getJSONObject("listitem");
						listitems.put(parmtext, parmvalue);
					} else {
						JSONObject listitems = new JSONObject();
						listitems.put(parmtext, parmvalue);
						el.put("listitem", listitems);
					}

				} catch (JSONException e) {
					e.printStackTrace();
				}
			} else {
				JSONObject el = new JSONObject();
				JSONObject listitems = new JSONObject();
				try {
					listitems.put(parmtext, parmvalue);
					el.put("listitem", listitems);
					ds.json.put(elementid, el);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}

		if ("addoptiongroup".equals(operation)) {
			if (ds.json.has(elementid)) {
				try {
					JSONObject el = ds.json.getJSONObject(elementid);
					if (el.has("listitem")) {
						JSONObject groups = el.getJSONObject("listitem");
						JSONObject groupitems = new JSONObject();
						groupitems.put("name", parmvalue);
						groupitems.put("items", new JSONObject());
						groups.put(parmtext, groupitems);
					} else {
						JSONObject groups = new JSONObject();
						JSONObject groupitems = new JSONObject();
						groupitems.put("name", parmvalue);
						groupitems.put("items", new JSONObject());
						groups.put(parmtext, groupitems);
						el.put("listitem", groups);
					}

				} catch (JSONException e) {
					e.printStackTrace();
				}
			} else {
				JSONObject el = new JSONObject();
				JSONObject groups = new JSONObject();
				try {
					JSONObject groupitems = new JSONObject();
					groupitems.put("name", parmvalue);
					groupitems.put("items", new JSONObject());
					groups.put(parmtext, groupitems);
					el.put("listitem", groups);
					ds.json.put(elementid, el);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}
		// end json
	}
	// JSON End

	protected void setJSON(String elementid, String operation, String parmtext, String parmvalue, String groupid) {
		// json
		DocumentStore ds = context.getBean(DocumentStore.class);
		if ("addgroupitem".equals(operation)) {
			if (ds.json.has(elementid)) {
				try {
					JSONObject el = ds.json.getJSONObject(elementid);
					if (el.has("listitem")) {
						JSONObject listitems = el.getJSONObject("listitem");
						JSONObject group;
						if (groupid.equals("")) {
							group = listitems.getJSONObject(listitems.keys().next() + "");
						} else {
							group = listitems.getJSONObject(groupid);
						}
						JSONObject items = group.getJSONObject("items");
						items.put(parmtext, parmvalue);
					}

				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}
		// end json
	}

//	protected void insetToDb(String js) {
//		FmisSession fmisSession = ApplicationContextProvider.getBean(FmisSession.class);
//		DocumentStore ds = ApplicationContextProvider.getBean(DocumentStore.class);
//		String requestId = fmisSession.session("requestid");
//		if (!ds.logjs.containsKey(requestId)) {
//			ds.logjs.put(requestId, js);
//		} else {
//			ds.logjs.put(requestId, ds.logjs.get(requestId) + js);
//		}
//
//	}

//	public String getJsDb() {
//		DocumentStore ds = ApplicationContextProvider.getBean(DocumentStore.class);
//		FmisSession fmisSession = ApplicationContextProvider.getBean(FmisSession.class);
//		String reqid = fmisSession.session("requestid");
//		String js = "";
//		if (ds.logjs.containsKey(reqid)) {
//			js = ds.logjs.get(reqid);
//			ds.logjs.remove(reqid);
//		}
//		return js;
//	}

	public String getJSON() {
		DocumentStore ds = context.getBean(DocumentStore.class);
		return ds.json.toString();
	}

//	public void clearData() {
//		// DocumentStore ds = ApplicationContextProvider.getBean(DocumentStore.class);
//		// ds.cleanAll();
//	}
}
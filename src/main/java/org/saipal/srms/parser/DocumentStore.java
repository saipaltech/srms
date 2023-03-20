package org.saipal.srms.parser;

import java.util.HashMap;
import java.util.Map;
//import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("request")
public class DocumentStore {
	Map<String, Element> elements = new HashMap<>();
	Map<String, DataGrid> grids = new HashMap<>();
	// Map<String, String> logjs = new HashMap<>();
	// JSON Data Begin
	protected JSONObject json = new JSONObject();
	//protected JSONArray jsa = new JSONArray();
	// protected String activeelementid = "";
	// protected String activeoperation = "";
	// protected JSONObject elementlist = new JSONObject();
	// protected JSONArray elemarray = new JSONArray();
	// protected JSONObject activeelement = new JSONObject();
	// protected JSONObject listitem = new JSONObject();
	// JSON Data End
	protected StringBuilder jsLog = new StringBuilder();
	protected boolean nestedExecution=true;
	protected boolean recursiveExecution=true;

//	public void addElements(Map<String, Element> elems) {
//		elements = elems;
//	}

//	public void addGrids(Map<String, DataGrid> grds) {
//		grids = grds;
//	}

//	public Element getElement(String id) {
//		if (elements.containsKey(id)) {
//			return elements.get(id);
//		}
//		return null;
//	}

//	public void addElement(Element el) {
//		elements.put(el.getId(), el);
//	}

//	public DataGrid getGrid(String id) {
//		if (grids.containsKey(id)) {
//			return grids.get(id);
//		}
//		return null;
//	}

//	public void cleanAll() {
//		String requestid = autoService.session("requestid");
//		this.elements.remove(requestid);
//		this.grids.remove(requestid);
//	}
}

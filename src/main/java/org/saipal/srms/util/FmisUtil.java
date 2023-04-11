
package org.saipal.srms.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;

import javax.persistence.Tuple;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.saipal.srms.parser.RequestParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public final class FmisUtil {

	@Value("${dev_mode:#{null}}")
	String devMode;
	
	@Value("${php_path:#{null}}")
	String phpPath;
	
	@Value("${public_dir:#{null}}")
	String publicDir;
	
	@Autowired
	private DateUtil dutil;

	@Autowired
	RequestParser document;
	
	
	@Autowired
	ApplicationContext context;
	
	@Autowired
	FmisSession session;

	private static final Logger LOG = LoggerFactory.getLogger(FmisUtil.class);

	/**
	 * constructs the tenant identifier according to the request received.
	 * 
	 * @param request Http servlet request object
	 * @return Tenant Identifier
	 */
	public String getTenantName(HttpServletRequest request) {
		String tenantName;
		if (request.getServerPort() == 80 || request.getServerPort() == 433) {
			tenantName = request.getServerName() + request.getContextPath();
		} else {
			tenantName = request.getServerName() + ":" + request.getServerPort() + request.getContextPath();
		}

		return tenantName;
	}

	/**
	 * Constructs the base url from the http request
	 * 
	 * @param request Http servlet request object
	 * @return string value of the base uri
	 */
	public String getBaseUrl(HttpServletRequest request) {
		String baseUrl;
		baseUrl = session.session("baseUrl");
		if (baseUrl.isBlank()) {
			// for live server
			if (getDevMode().equals("0"))
				baseUrl = "https://" + request.getServerName() + request.getContextPath();
			else
				baseUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort()
						+ request.getContextPath();
		}
		return baseUrl;
	}

	public static String getRequestUrl(HttpServletRequest request) {
		String url = request.getRequestURI();
		if (!request.getContextPath().isBlank()) {
			url = url.replace(request.getContextPath(), "");
		}
		return url.substring(1);
	}

	public String saveImage(MultipartFile file, String parentDir, String fileName) {
		String rootPath = publicDir + File.separator + parentDir;
		File root = new File(rootPath);
		if (!root.exists()) {
			root.mkdirs();
		}
		String oFileName = file.getOriginalFilename();
		String ext = oFileName.split("\\.")[1];
		String imgpath = parentDir + File.separator + fileName + "." + ext;
		Path filepath = Paths.get(root.getPath(), fileName + "." + ext);
		try (OutputStream os = Files.newOutputStream(filepath)) {
			os.write(file.getBytes());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "";
		}
		return imgpath;
	}

	public String baseUrl(HttpServletRequest request) {
		return getBaseUrl(request);
//		String baseUrl;
//		if (request.getServerPort() == 80 || request.getServerPort() == 433) {
//			baseUrl =  "https://" + request.getServerName() + request.getContextPath();
//		} else {
//			baseUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort()
//					+ request.getContextPath();
//		}
//		return baseUrl;
	}

	/**
	 * Returns the map of string, string that contains the model variables with
	 * their equivalent form display name
	 * 
	 * @param fieldList list of fields accessed with Reflection api
	 * @return
	 */
	public static Map<String, String> initFormDisplayName(Field[] fieldList) {
		Map<String, String> formDisplayName = new HashMap<String, String>();
		for (Field field : fieldList) {
			StringBuilder fDName = new StringBuilder(field.getName());
			fDName.setCharAt(0, ("" + fDName.charAt(0)).toUpperCase().charAt(0));
			formDisplayName.put(field.getName(), fDName.toString());
		}
		return formDisplayName;
	}

	/**
	 * Converts the given tuple to a map of column with key form columns args and
	 * value from the tuple
	 * 
	 * @param t       tuple to be converted
	 * @param columns list of columns which will acts as key for the map
	 * @return map converted from tuple
	 */
	public static Map<String, String> tupleToMap(Tuple t, List<String> columns) {
		Map<String, String> row = new HashMap<>();
		String tempList[], temp;
		for (String column : columns) {
			tempList = column.split(" ");
			temp = tempList[tempList.length - 1];
			if (temp.indexOf('.') != -1) {
				tempList = temp.split("\\.");
				temp = tempList[tempList.length - 1];
			}
			row.put(temp, t.get(temp) + "");
		}
		return row;
	}

	/**
	 * Extract the message from the error as result of form data binding in
	 * controller according to the form validation set in to the model. This method
	 * will return the appropriate message of each form field
	 * 
	 * @param errors List of Error object received from form binding
	 * @param s      object of service that provide access to the list of form field
	 * @return List of message for each form field that fails form validation
	 */
//	public List<String> extractMessage(List<ObjectError> errors, SuperService s) {
//		List<String> errorMessages = new ArrayList<String>();
//		for (ObjectError error : errors) {
//			String errorCode = error.getCodes()[1];
//			String field = s.getFormFields().get(errorCode.substring(errorCode.lastIndexOf('.') + 1));
//			errorMessages.add(field + " " + as.wds(error.getDefaultMessage()));
//		}
//		return errorMessages;
//	}

	/**
	 * Creates the folder structure according to the path provided recursively
	 * 
	 * @param path
	 */
	public static void createFolder(String path) {
		LOG.info("createFolder:" + path);
		Path dirPathObj = Paths.get(path);
		try {
			Files.createDirectories(dirPathObj);
		} catch (IOException ioExceptionObj) {
			LOG.info("Problem Occured While Creating The Directory Structure= " + ioExceptionObj.getMessage());
		}

	}

	public static void writeFile(String root, String path, String text) {
		writeFile(root, path, text, "");
	}

	/**
	 * Write the code generated to either the service class or equivalent javascript
	 * file
	 * 
	 * @param root root folder for the files to be written.
	 *             (src/main/java/org/saipal/fmis/service) for service
	 *             file.(src/main/resources/templates) for javascript file
	 * @param path path of the file
	 * @param text
	 */
	public static void writeFile(String root, String path, String text, String imports) {
		String pathVar[];
		pathVar = path.split("/");
		// LOG.info("pathLen:" + pathVar.length);
		String pre = "";
		String header = "";
		String footer = "";
		String fileName = "", fName = "";
		String[] fileNames;
		// LOG.info("path:" + path);
		if (root.contains("java")) {
			// read the template defined for service class
			File file1 = new File("src/main/java/org/saipal/fmis/template/AutoServiceTemplate");
			pre = "package org.saipal.fmis." + pathVar[1] + ".service;\n";
			if (!imports.isBlank()) {
				pre += imports;
			}
			try (Scanner sc = new Scanner(file1)) {
				while (sc.hasNext()) {
					pre += sc.nextLine() + "\n";
				}
				pre = pre.substring(0, pre.length() - 2) + " ";
				sc.close();
				if (pathVar.length > 3) {
					fileName = pathVar[2] + pathVar[3].substring(0, 1).toUpperCase() + pathVar[3].substring(1);
				} else {
					fileName = pathVar[2];
				}
				int dashIndex = fileName.indexOf('-');
				if (dashIndex != -1) {
					fileNames = fileName.split("-");
					for (String f : fileNames) {
						fName += f.substring(0, 1).toUpperCase() + f.substring(1);
					}
				} else {
					fName = fileName.substring(0, 1).toUpperCase() + fileName.substring(1);
				}
				fileName = fName + "ServiceAuto";
				header = pre + fileName + " extends AutoService{\n";
				footer = "\n}";
				fileName += ".java";
				root = root + "/" + pathVar[1] + "/service";
				// LOG.info("root:" + root);
				createFolder(root);

				File file = new File(root, fileName);
				FileWriter writer = new FileWriter(file);
				writer.write(header + text + footer);
				writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

		} else {
			String vFolder;
			if (pathVar.length > 3) {
				vFolder = pathVar[1] + "/" + pathVar[2] + "/" + pathVar[3];
			} else {
				vFolder = pathVar[1] + "/" + pathVar[2];
			}
			// LOG.info(vFolder);
			root += "/" + vFolder;
			fileName = "form_js.twig";
			createFolder(root);
			File file = new File(root, fileName);
			try (FileWriter writer = new FileWriter(file);) {
				writer.write(header + text + footer);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * File writing for ribbon
	 * 
	 * @param code
	 */
	public static void writeRibbonServiceCode(String code) {
		// TODO Auto-generated method stub

		File file = new File("src/main/java/org/saipal/fmis/service", "RibbonCheckService.java");
		String pre = "package org.saipal.fmis.service;\n";
		String post = "}";
		File file1 = new File("src/main/java/org/saipal/fmis/template/AutoServiceTemplate");
		try (Scanner sc = new Scanner(file1); FileWriter fw = new FileWriter(file)) {
			while (sc.hasNext()) {
				pre += sc.nextLine() + "\n";
			}
			pre = pre.substring(0, pre.length() - 2) + " ";
			pre += "RibbonCheckService extends AutoService{\n";
			fw.write(pre + code + post);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Initialize the connection of jdbc according to the connection properties
	 * passed in map and set the connection into thread local object that can be
	 * accessed anywhere from the code with in same request.
	 * 
	 * @param connection map of connection properties
	 */

	public void initializeConnection(Map<String, String> connection) {
		Connection con = null;
		Connection conrev = null;
		try {
			Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
		} catch (ClassNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			con = DriverManager.getConnection(connection.get("url")+";encrypt=false", connection.get("username"),
					connection.get("password"));
			//String revDb = connection.get("dbname")+"_Revenue";
			//String revUrl = connection.get("url").replace(connection.get("dbname"), revDb);
			//conrev = DriverManager.getConnection(revUrl+";encrypt=false", connection.get("username"),
			//		connection.get("password"));
			session.setAttribute("dbname", con.getCatalog().toLowerCase());
		} catch (SQLException e) {
			throw new AuthenticationServiceException("Cannot connect to Database.\n" + e.getMessage());
		}
		ContextHolder.setVDir(connection.get("vdirectory"));
		ContextHolder.setCon(con);
		ContextHolder.setConRev(conrev);
		ContextHolder.setTenantId(connection.get("appid"));

	}

	/**
	 * Counts the number of path word in current request. Ignores the first /
	 * character in the uri path
	 * 
	 * @param request http servlet request
	 * @return number of path parameter words.
	 */

	public int getRouteParamCount(HttpServletRequest request) {
		if (request.getRequestURI().startsWith("/"))
			return request.getRequestURI().split("/").length - 1;
		else
			return request.getRequestURI().split("/").length;
	}

	public String getDevMode() {
		if (devMode == null) {
			return "0";
		}
		return devMode;
	}

	public String getPhpPath() {
		if (phpPath == null || phpPath.isBlank()) {
			return "php";
		}
		return phpPath;
	}


	public static String getCookieValue(HttpServletRequest request, String key) {

		Optional<Cookie> cok = Arrays.stream(request.getCookies()).filter(c -> (c.getName().equals(key))).findFirst();
		if (cok.isPresent()) {
			try {
				return URLDecoder.decode(cok.get().getValue(), StandardCharsets.UTF_8.toString());
			} catch (UnsupportedEncodingException e) {
				LOG.error("Cannot decode the url");
				return "";
			}
		}
		return "";
	}

	public String getCalander(String objectId, String lang) {
		String script = "";
		if ("Np".equalsIgnoreCase(lang)) {
			script += "createcalendar(document.getElementById('" + objectId + "'));";
			script += "document.getElementById('" + objectId + "').setAttribute('placeholder','YYYY/MM/DD');";
			script += "document.getElementById('" + objectId + "').setAttribute('autocomplete','off');";
			script += "document.getElementById('" + objectId + "').style.class='embed';";
			script += "$('#" + objectId
					+ "').datepick({showOnFocus: false,showTrigger: '#calImg',onSelect:function(){$('#" + objectId
					+ "').trigger('blur');}});";
		} else {
			script += "document.getElementById('" + objectId + "').setAttribute('placeholder','MM/DD/YYYY');";
			script += "document.getElementById('" + objectId + "').setAttribute('autocomplete','off');";
			script += "document.getElementById('" + objectId + "').style.class='embed';";
			script += "$('#" + objectId
					+ "').datepick({showOnFocus: false,showTrigger: '#calImg',onSelect:function(){$('#" + objectId
					+ "').trigger('blur');}});";
			script += "createcalendar(document.getElementById('" + objectId + "'));";
		}
		return "<script language=\"javascript\" type=\"text/javascript\">\n" + script + "\n</script>";
	}

	public String getCalanderWithDefaultDate(String objectId, String lang) {
		String script = "";
		if ("Np".equalsIgnoreCase(lang)) {
			String date = dutil.getNepDate(null);
			script += ";today='" + date + "';";
			script += "createcalendar(document.getElementById('" + objectId + "'));";
			script += "document.getElementById('" + objectId + "').setAttribute('placeholder','YYYY/MM/DD');";
			script += "document.getElementById('" + objectId + "').setAttribute('autocomplete','off');";
			script += "document.getElementById('" + objectId + "').style.class='embed';";
			script += "document.getElementById('" + objectId + "').value='" + date + "';";
			script += "$('#" + objectId
					+ "').datepick({showOnFocus: false,showTrigger: '#calImg',onSelect:function(){$('#" + objectId
					+ "').trigger('blur');}});";
		} else {
			String dt = dutil.getEngDate(null);
			// String[] dps = dt.split("-");
			// String date = dps[1] + "/" + dps[2] + "/" + dps[0];
			String date = dt;
			script += ";today='" + date + "';";
			script += "document.getElementById('" + objectId + "').setAttribute('placeholder','MM/DD/YYYY');";
			script += "document.getElementById('" + objectId + "').setAttribute('autocomplete','off');";
			script += "document.getElementById('" + objectId + "').style.class='embed';";
			script += "$('#" + objectId
					+ "').datepick({showOnFocus: false, showTrigger: '#calImg', onSelect:function(){$('#" + objectId
					+ "').trigger('blur');}});";
			script += "document.getElementById('" + objectId + "').value='" + date + "';";
			script += "createcalendar(document.getElementById('" + objectId + "'));";
		}
		return "<script language=\"javascript\" type=\"text/javascript\">\n" + script + "\n</script>";
	}

	public String request(String param) {
		// document = ApplicationContextProvider.getBean(RequestParser.class);
		return document.getElementById(param).value == null ? "" : document.getElementById(param).value.trim() + "";
	}

	public String session(String param) {
		return session.session(param);
	}

	/**
	 * util method to convert a result set into tuple
	 * 
	 * @param rs   result set
	 * @param rsmd result set metadata
	 * @return Tuple equivalent tuple for given result set
	 */
//	public static Tuple rsToTuple(ResultSet rs, ResultSetMetaData rsmd) {
//		return rsToTuple(rs, rsmd, "");
//	}

	/**
	 * util method to convert a result set into tuple
	 * 
	 * @param rs   result set
	 * @param rsmd result set metadata
	 * @return Tuple equivalent tuple for given result set
	 */
//	public static Tuple rsToTuple(ResultSet rs, ResultSetMetaData rsmd, String sql) {
//		MyTuple mtp = new MyTuple(sql);
//		try {
//			for (int i = 1; i <= rsmd.getColumnCount(); i++) {
//
//				mtp.addObject(rsmd.getColumnLabel(i), rs.getObject(i));
//
//			}
//		} catch (SQLException ex) {
//			ex.printStackTrace();
//		}
//		return mtp;
//	}

}

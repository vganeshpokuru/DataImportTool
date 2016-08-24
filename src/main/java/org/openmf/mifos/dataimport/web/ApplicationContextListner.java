package org.openmf.mifos.dataimport.web;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import org.openmf.mifos.dataimport.dto.Office;
import org.openmf.mifos.dataimport.http.MifosRestClient;
import org.openmf.mifos.dataimport.utils.StringUtils;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

@WebListener
public class ApplicationContextListner implements ServletContextListener {
	
	@Override
	public void contextInitialized(@SuppressWarnings("unused") ServletContextEvent sce) {
		Properties prop = new Properties();
	    String homeDirectory = System.getProperty("user.home");
		FileInputStream fis = null;
		try {
			File file = new File(homeDirectory + "/dataimport.properties");
			fis = new FileInputStream(file);
			prop.load(fis);
			readAndSetAsSysProp("mifos.endpoint", "http://localhost:8080", prop);
			readAndSetAsSysProp("mifos.user.id", "mifos", prop);
			readAndSetAsSysProp("mifos.password", "testmifos", prop);
			readAndSetAsSysProp("mifos.tenant.id", "default", prop);
			fis.close();
			sce.getServletContext().setAttribute("offices", retriveAllOffices());
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}


	private void readAndSetAsSysProp(String key, String defaultValue, Properties prop) {
		String value = prop.getProperty(key);
		if(!StringUtils.isBlank(value)) {
			System.setProperty(key, value);
		} else {
			System.setProperty(key, defaultValue);
		}
	}


	@Override
	public void contextDestroyed(@SuppressWarnings("unused") ServletContextEvent sce) {
		
	} 
	
	private List<Office> retriveAllOffices(){
		MifosRestClient restClient = new MifosRestClient();
		restClient.createAuthToken();
		final String content = restClient.get("offices?limit=-1");
		Gson gson = new Gson();
        JsonElement json = new JsonParser().parse(content);
        JsonArray array = json.getAsJsonArray();
        Iterator<JsonElement> iterator = array.iterator();
        List<Office> offices = new ArrayList();
        while(iterator.hasNext()) {
        	json = iterator.next();
        	Office office = gson.fromJson(json, Office.class);
        	offices.add(office);
        }
        return offices;
	}
}

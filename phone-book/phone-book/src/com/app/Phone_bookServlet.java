package com.app;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Transaction;
import com.google.appengine.labs.repackaged.org.json.JSONException;
import com.google.appengine.labs.repackaged.org.json.JSONObject;

@SuppressWarnings("serial")
public class Phone_bookServlet extends HttpServlet {
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		System.out.println("In get");
		resp.setContentType("application/json");
		resp.setCharacterEncoding("utf-8");
		PrintWriter out = resp.getWriter();
		List<JSONObject> jsonList = new ArrayList<>();
		JSONObject json;
	    // finally output the json string       
		DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
		
		Query query = new Query("Contact");
		PreparedQuery pq = ds.prepare(query);
		for(Entity ent : pq.asIterable()){
			String name = ent.getProperty("name").toString();
			String num = ent.getProperty("num").toString();
			long id = ent.getKey().getId();
			//System.out.println(name+"--"+num+"--"+id);
			json = new JSONObject();
			// put some value pairs into the JSON object .
		    try {
				json.put("name", name);
				json.put("num", num);
				json.put("id", id);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			jsonList.add(json);
			
		}
		
		out.print(jsonList.toString());
		out.close();
	}
	
	public void doPost(HttpServletRequest req, HttpServletResponse res)throws IOException {
		
		DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
		Transaction trns = ds.beginTransaction();
		InputStreamReader isr = new InputStreamReader(req.getInputStream());
		BufferedReader in = new BufferedReader(isr);
		String line = in.readLine();
		JSONObject json;
		Entity entity = null;
		String mode;
		try {

		    json = new JSONObject(line); 
		    String jsonString = json.toString();
		    
		    mode = getMode(json);
		    switch(mode){
		    case "save":
		    	entity = getEntity(json);
		    	JSONObject saveData = json.getJSONObject("data");
		    	String[] names = JSONObject.getNames(saveData);
		    	for (int i=0 ; i < names.length ; i++) {
		    		//System.out.println(names[i] + " : " + json.getString(names[i]));
		    		entity.setProperty(names[i], saveData.getString(names[i]));
		    	}
		    	ds.put(entity);
		    	break;
		    case "del":
		    	String list = json.getString("list");
		    	list = list.replace("[", "").replace("]", "");
				String[] ids = list.split(",");
				Key key;
				for(String id : ids){
					key = KeyFactory.createKey("Contact", Long.valueOf(id));
					ds.delete(key);
				}
		    }
		    trns.commit();
		    
		} catch (Exception e) { e.printStackTrace(); }
		
		
		System.out.println("Contact saved/ deleted");
	
	}
	


	public void doPut(HttpServletRequest request, HttpServletResponse response) {
	
	
	}	
	
	public void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		System.out.println("In delete");
		DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
		Transaction tx = ds.beginTransaction();
		InputStreamReader isr = new InputStreamReader(req.getInputStream());
		BufferedReader in = new BufferedReader(isr);
		String line = in.readLine();
		line = line.replace("[", "").replace("]", "");
		String[] ids = line.split(",");
		Key key;
		for(String id : ids){
			key = KeyFactory.createKey("Contact", Long.valueOf(id));
			ds.delete(tx,key);
		}
		tx.commit();    
	}	
	
	private Entity getEntity(JSONObject json) throws JSONException {
		String[] names = JSONObject.getNames(json);
		long identifier = 0;
		for (int i=0 ; i < names.length ; i++) {
			if(names[i].equals("id")){
				String temp = json.getString(names[i]);
				if(temp!=null && !temp.equals("")){
					identifier = Long.valueOf(temp);
				}
			}
		}
		if(identifier==0){
			return new Entity("Contact");
		}else{
			return new Entity("Contact", identifier);
		}
	}
	
	private String getMode(JSONObject json) throws JSONException {
		String[] names = JSONObject.getNames(json);
		for (int i=0 ; i < names.length ; i++) {
			if(names[i].equals("mode")){
				return json.getString(names[i]).toString();
			}
		}
		return null;
	}
	
	
	
}

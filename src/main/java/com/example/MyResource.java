package com.example;

import javax.ws.rs.Consumes;

import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.glassfish.jersey.server.ResourceConfig;

import com.amazonaws.util.json.JSONArray;
import com.amazonaws.util.json.JSONException;
import com.amazonaws.util.json.JSONObject;
import com.fasterxml.jackson.databind.MapperFeature;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.glassfish.jersey.media.multipart.MultiPartFeature;

//import com.sun.jersey.core.header.FormDataContentDisposition;
import java.awt.List;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.Collection;
import java.util.HashMap;


import java.util.HashSet;
import java.util.Set;

import result.*;
import controller.*;
/**
 * Root resource (exposed at "myresource" path)
 */

//class ApplicationConfig extends Application {
//
//    public Set<Class<?>> getClasses() {
//        final Set<Class<?>> resources = new HashSet<Class<?>>();
//
//        // Add your resources.
//      //  resources.add(UploadFileService.class);
//
//        // Add additional features such as support for Multipart.
//        resources.add(MultiPartFeature.class);
//
//        return resources;
//    }
//}
 


@Path("androidcontrol")
public class MyResource {
  
	
	HashMap<String, List> inmemoryMap = new HashMap<String, List>();
	static int availableDevices =10;
	/**
	 * Method handling HTTP GET requests. The returned object will be sent
	 * to the client as "text/plain" media type.
	 *
	 * @return String that will be returned as a text/plain response.
	 */
	//    @GET
	//    @Produces(MediaType.APPLICATION_JSON)
	//    public String getIt() {
	//        return "Got it!";
	//    }

	/**
	 * Method handling HTTP GET requests. The returned object will be sent
	 * to the client as "text/plain" media type.
	 *
	 * @return String that will be returned as a text/plain response.
	 */

	@POST
	@Path("myresource")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public String getIt() {

		return "Got it!";
	}
	//    @GET
	//    @Path("status")
	//    @Produces(MediaType.APPLICATION_JSON)
	//    public Status getStatus() throws Exception {
	//        return AndroidEmulatorManager.getStatus();
	//    }
	////    @GET
	////    @Path("terminate")
	////    public void terminate()  {
	////        AndroidEmulatorManager.terminate();
	////        Main.stopServer();
	////    }
	//

	@GET
	@Path("initialize")
	@Produces(MediaType.TEXT_PLAIN)
	public String initialcall() {
		String status=null;
		try {
			
			AndroidEmulatorManager.inititalize();
			status="success";

		} catch (Exception e) {
			System.out.println(e.getMessage());
			status="exception";
		}
		return status;
	}
	//	
	//	@GET
	//	@Path("getpath")
	//	@Produces(MediaType.TEXT_PLAIN)
	//	public String getpath() {
	//		String status=null;
	//		status = "ANDR_HOME "+ System.getenv("ANDROID_HOME")+ " avd path "+System.getenv("AVD_PATH")+ " emulator path "+System.getenv("EMULATOR_PATH");
	//		return status;
	//	}

	
	@POST
	@Path("assign")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public String assign(String inputJson) throws JSONException{
		
		JSONObject indata = new JSONObject(inputJson);
		int version = Integer.parseInt(indata.getString("resourceVersion"));
		int memory = Integer.parseInt(indata.getString("resourceMemory"));
		int quant = indata.getInt("resourceQuantity");
		String requestId = indata.getString("resourceVersion");
		String deviceType = indata.getString("resourceType");
	//	MongoClient mongo = new MongoClient("localhost",27017);
		
		JSONArray devices = new JSONArray();
		for(int i=0;i<quant;i++){
		try {
			System.out.println("start emulator version "+ version + " memory "+ memory);
		//	EmulatorInformation e = AndroidEmulatorManager.createAvd(version, memory);
			String e = AndroidEmulatorManager.createAvd(version, memory,requestId,deviceType);
		//	String devId = AndroidEmulatorManager.createAvd(version, memory);
		//	System.out.println("avd created");
			JSONObject deviceInfo = new JSONObject();
			//return new CreateAvdActionResult(e);
			deviceInfo.put("deviceId", e);
			deviceInfo.put("deviceVersion", version);
			deviceInfo.put("deviceMemory", memory);
			deviceInfo.put("deviceImage", "ARM");
			deviceInfo.put("deviceStatus", "running");
			deviceInfo.put("deviceType", "Android");
			
//			deviceInfo.put("deviceId", "dev2");
//			deviceInfo.put("deviceVersion", 19);
//			deviceInfo.put("deviceMemory", 512);
//			deviceInfo.put("deviceImage", "ARM");
//			deviceInfo.put("deviceStatus", "running");
//			deviceInfo.put("deviceType", "Android");
			
			devices.put(deviceInfo);
			
		
		} catch (Exception e) {
			System.out.println(e.getMessage());
		//	return new CreateAvdActionResult(e);
		}
		}
		String jsonText = devices.toString();
		return jsonText;
		
	}
//	@GET
//	@Path("startemulator/{version}/{memory}")
//	@Produces(MediaType.APPLICATION_JSON)
//	public CreateAvdActionResult startEmulator(@PathParam("version") int version, @PathParam("memory") int memory) {
//		try {
//			System.out.println("start emulator version "+ version + " memory "+ memory);
//			EmulatorInformation e = AndroidEmulatorManager.createAvd(version, memory);
//			System.out.println("avd created");
//			return new CreateAvdActionResult(e);
//		} catch (Exception e) {
//			System.out.println(e.getMessage());
//			return new CreateAvdActionResult(e);
//		}
//	}
	@POST
	@Path("startemulator")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public String startEmulator(String info) throws JSONException{
		JSONObject emulatorname = new JSONObject(info);
		String emulatorId = emulatorname.getString("deviceid");
		System.out.println("emulator id"+emulatorId );
		String status =null;
		try {
		//	EmulatorInformation e = AndroidEmulatorManager.createAvd(version, memory);
			
			System.out.println("avd created");
			AndroidEmulatorManager.startEmulatorWithId(emulatorId);
			status = "launched";
		//	return new CreateAvdActionResult(e);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			status = "error launching";
		}
		return status;
	}
	
	
	
	@POST
	@Path("getInstalledApp/{app}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public String getInstalledApp(@PathParam("app") String app,String info) throws JSONException{
		JSONObject emulatorname = new JSONObject(info);
		String emulatorId = emulatorname.getString("deviceid");
		//String appName = emulatorname.getString("appName");
		System.out.println("emulator id"+emulatorId );
		String status =null;
		try {
		//	EmulatorInformation e = AndroidEmulatorManager.createAvd(version, memory);
			
			System.out.println("avd created");
			AndroidEmulatorManager.getAppInfo(emulatorId,app);
			status = "launched";
		//	return new CreateAvdActionResult(e);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			status = "error launching";
		}
		return status;
	}
	    @GET
	    @Path("stopemulator/{id}")
	    public String stopEmulator(@PathParam("id") String id) {
	    	
	    	String result = null;
	        try {
	        	System.out.println("stop emu func");
	            AndroidEmulatorManager.closeEmulator(id);
	            result = "stopped";
	            return result;
	        } catch (Exception e) {
	        	result="error stopping";
	            return result;
	        }
	    }
	
	    
	    @GET
	    @Path("getAllApps/{id}")
	    public String getAllApps(@PathParam("id") String id) {
	    	
	    	String result = null;
	        try {
	        	System.out.println("get all the installed apps");
	           Set<String> apps = AndroidEmulatorManager.getEmulator(id).getInstalledApps().keySet();
	           System.out.println(apps.toString());
	            result = "stopped";
	            return result;
	        } catch (Exception e) {
	        	result="error stopping";
	            return result;
	        }
	    }
	
	
	//    @POST
	//    @Path("launchurl/{id}")
	//    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	//    public Response startUrl(@PathParam("id") String id, @FormParam("url") String url) {
	//        try {
	//        	
	//            AndroidEmulatorManager.getEmulator(id).launchUrl(url);
	//            return Response.ok().build();
	//        } catch (Exception e) {
	//            return Response.serverError().build();
	//        }
	//    }
	//
	    @POST
	    @Path("install/{appname}/{id}")
	    @Consumes(MediaType.MULTIPART_FORM_DATA)
	    public Response uploadFile(
	    		@PathParam("appname") String appname,@PathParam("id") String id,
	            @FormDataParam("apkfile") InputStream uploadedInputStream,
	            @FormDataParam("apkfile") FormDataContentDisposition fileDetail) {
	        try {	            
	        	AndroidEmulatorManager.installAppforDevice(id,appname,uploadedInputStream);
	            return Response.ok().build();
	         
	        } catch (Exception e) {
	            return Response.serverError().build();
	        }
	    }
	
	
	    @POST
	    @Path("launch/{id}")
	    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	    public Response launchApplication(@PathParam("id") String id, @FormParam("appname") String appname) {
	        try {
	            AndroidEmulatorManager.getEmulator(id).launchApplication(appname);
	            return Response.ok().build();
	        } catch (Exception e) {
	            return Response.serverError().build();
	        }
	    }
	
	    @GET
	    @Path("getscreenshot/{id}")
	    @Produces("image/png")
	    public Response getScreenShot(@PathParam("id") String id) throws Exception {
	        return Response.ok(AndroidEmulatorManager.getEmulator(id).getScreenShot()).build();
	    }


}

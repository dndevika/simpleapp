package com.example;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import com.amazonaws.util.json.JSONArray;
import com.amazonaws.util.json.JSONException;
import com.amazonaws.util.json.JSONObject;

import java.awt.List;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.Collection;
import java.util.HashMap;

import result.*;
import controller.*;
/**
 * Root resource (exposed at "myresource" path)
 */
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
	@Path("checkavailability")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public String availability(String inputJson) throws JSONException{

		JSONObject indata = new JSONObject(inputJson);
		int input = indata.getInt("resourceQuantity");

		JSONObject obj = new JSONObject();
		if(availableDevices==0){
		}
		else if(input > availableDevices){
			input = input - availableDevices;
			availableDevices = availableDevices - input;
			obj.put("available", input);
		}
		else{
			availableDevices = availableDevices - input;
			obj.put("available", input);
		}

		String jsonText = obj.toString();
		return jsonText;
	}

	@POST
	@Path("assign")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public String assign(String inputJson) throws JSONException{
		
		JSONObject indata = new JSONObject(inputJson);
		int version = Integer.parseInt(indata.getString("resourceVersion"));
		int memory = Integer.parseInt(indata.getString("resourceMemory"));
		JSONObject deviceInfo = new JSONObject();
		JSONArray devices = new JSONArray();
		try {
			System.out.println("start emulator version "+ version + " memory "+ memory);
		//	EmulatorInformation e = AndroidEmulatorManager.createAvd(version, memory);
			String e = AndroidEmulatorManager.createAvd(version, memory);
		//	String devId = AndroidEmulatorManager.createAvd(version, memory);
		//	System.out.println("avd created");
			
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
	
	//
	//    @GET
	//    @Path("stopemulator/{id}")
	//    public Response stopEmulator(@PathParam("id") String id) {
	//
	//        try {
	//            AndroidEmulatorManager.closeEmulator(id);
	//            return Response.ok().build();
	//        } catch (Exception e) {
	//            return Response.serverError().build();
	//        }
	//    }
	//
	//
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
	// //   @POST
	////    @Path("install/{id}")
	////    @Consumes(MediaType.MULTIPART_FORM_DATA)
	////    public Response uploadFile(
	////            @PathParam("id") String id,
	////            @FormParam("apkfile") InputStream uploadedInputStream,
	////            @FormParam("apkfile") FormDataContentDisposition fileDetail) {
	////        try {
	////            AndroidEmulatorManager.getEmulator(id).installApplication(uploadedInputStream);
	////            return Response.ok().build();
	////        } catch (Exception e) {
	////            return Response.serverError().build();
	////        }
	////    }
	//
	//
	//    @POST
	//    @Path("launch/{id}")
	//    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	//    public Response launchApplication(@PathParam("id") String id, @FormParam("appname") String appname) {
	//        try {
	//            AndroidEmulatorManager.getEmulator(id).launchApplication(appname);
	//            return Response.ok().build();
	//        } catch (Exception e) {
	//            return Response.serverError().build();
	//        }
	//    }
	//
	//    @GET
	//    @Path("getscreenshot/{id}")
	//    @Produces("image/png")
	//    public Response getScreenShot(@PathParam("id") String id) throws Exception {
	//        return Response.ok(AndroidEmulatorManager.getEmulator(id).getScreenShot()).build();
	//    }


}

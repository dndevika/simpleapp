package com.example;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.Application;

import org.glassfish.jersey.filter.LoggingFilter;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;

public class MyApplication extends ResourceConfig{
	
	public MyApplication(){
		packages("com.example");
		register(org.glassfish.jersey.media.multipart.MultiPartFeature.class);
		property(ServerProperties.METAINF_SERVICES_LOOKUP_DISABLE, true);
	}

//	@Override
//    public Set<Class<?>> getClasses() {
//        final Set<Class<?>> classes = new HashSet<Class<?>>();
//        // register resources and features
//        classes.add(MultiPartFeature.class);
//      //  classes.add(MultiPartResource.class);
//        classes.add(LoggingFilter.class);
//        return classes;
//    }

}

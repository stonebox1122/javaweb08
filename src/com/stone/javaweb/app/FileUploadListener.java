package com.stone.javaweb.app;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

@WebListener
public class FileUploadListener implements ServletContextListener {

    public FileUploadListener() {
        // TODO Auto-generated constructor stub
    }

    public void contextDestroyed(ServletContextEvent sce)  { 
         // TODO Auto-generated method stub
    }

    public void contextInitialized(ServletContextEvent sce)  { 
         InputStream in = getClass().getClassLoader().getResourceAsStream("/upload.properties");
         Properties properties = new Properties();
         try {
			properties.load(in);
			
			for (Map.Entry<Object, Object> prop : properties.entrySet()) {
				String propertyName = (String) prop.getKey();
				String propertyValue = (String) prop.getValue();
				FileUploadProperties.getInstance().addProperty(propertyName, propertyValue);
			}			
		} catch (IOException e) {
			e.printStackTrace();
		}
         
    }	
}
package ib.roaddetect.utilities;

import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;


public class LoadSWT implements Runnable {

	private void loadSwtJar() {
		String swtFileName = null;
	    try {
	        String osName = System.getProperty("os.name").toLowerCase();
	        String osArch = System.getProperty("os.arch").toLowerCase();
	        URLClassLoader classLoader = (URLClassLoader) getClass().getClassLoader();
	        Method addUrlMethod = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
	        addUrlMethod.setAccessible(true);

	        String swtFileNameOsPart = 
	            osName.contains("win") ? "win32" :
	            osName.contains("mac") ? "macosx" :
	            osName.contains("linux") || osName.contains("nix") ? "linux_gtk" :
	            ""; // 

	        String swtFileNameArchPart = osArch.contains("64") ? "x64" : "x86";
	        swtFileName = "swt_"+swtFileNameOsPart+"_"+swtFileNameArchPart+".jar";

	        
	        URL swtFileUrl = new URL("rsrc:"+swtFileName); 
	        
	        System.out.println(swtFileUrl.getFile());
	        addUrlMethod.invoke(classLoader, swtFileUrl);
	        
	    }
	    catch(Exception e) {
	        System.out.println("Unable to add the swt jar to the class path: " + swtFileName);
	        e.printStackTrace();
	    }
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		loadSwtJar();
		
	}


}

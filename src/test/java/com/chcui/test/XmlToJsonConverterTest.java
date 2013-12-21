package com.chcui.test;

import java.util.ArrayList;

import org.testng.annotations.*;
import com.chcui.xmlproc.XmlToJson;;

public class XmlToJsonConverterTest {
	 @BeforeClass
	 public void setUp() {
	   // code that will be invoked when this test is instantiated
	 }
	 
	@Test(groups = {"Basic"})
    public void validAuthenticationTest(){
		ArrayList<String> fileNames = new ArrayList<String>();
        ArrayList<Thread> threads = new ArrayList<Thread>();
        fileNames.add("src/test/resources/files/xml/books.xml");
        fileNames.add("src/test/resources/files/xml/recipe.xml");
        fileNames.add("src/test/resources/files/xml/staff.xml");
        
        for (int i = 0; i < fileNames.size(); i++) {
            Runnable task = new XmlToJson(fileNames.get(i));
            Thread worker = new Thread(task);
            // We can set the name of the thread
            worker.setName(String.valueOf(i));
            // Start the thread, never call method run() direct
            worker.start();
            // Remember the thread for later usage
            threads.add(worker);
        }
    }

}

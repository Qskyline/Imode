package com.skyline.control;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import com.skyline.imode.R;
import android.annotation.SuppressLint;
import android.content.Context;

@SuppressLint("WorldReadableFiles")
public class FakeEnvironment {	
	
	public FakeEnvironment(Context context) {
		setFakeImage(context);
	}
	
	@SuppressWarnings("deprecation")
	private void setFakeImage(Context context) {
		File f = new File(context.getFilesDir().getAbsolutePath() + "/fake.jpg");
		if (f.exists()) return;		
		InputStream in = context.getResources().openRawResource(R.drawable.fake); 
		byte [] buffer = null;
		try {
			int length = in.available();       
		    buffer = new byte[length];          
			in.read(buffer);   
			in.close();   
		} catch (IOException e) {
			buffer = null;
			e.printStackTrace();
		}
		if (buffer != null) {
			try {
				FileOutputStream fout = context.openFileOutput("fake.jpg", Context.MODE_WORLD_READABLE);
		        fout.write(buffer);  
		        fout.close(); 
			} catch (IOException e) {
				e.printStackTrace();
			}   
		}      
	}
}

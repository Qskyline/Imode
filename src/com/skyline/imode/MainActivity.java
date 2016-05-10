package com.skyline.imode;

import com.skyline.control.ControlAPI;

import android.app.Activity;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CheckBox;
import android.widget.CompoundButton;

public class MainActivity extends Activity {	
	//variables for test
	ControlAPI control = null;
	Resources resouces = null;
		
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		//变量赋值
		resouces = getResources();
		try{
			control = new ControlAPI(this);	
		}
		catch(ControlAPI.ControlException c) {
			c.printReason();
			return ;
		}		
		//权限初始化
		initCheckBox("internet");
		initCheckBox("storage");		
		initCheckBox("image");		
		initCheckBox("video");	
		initCheckBox("audio");	
		initCheckBox("location");	
		initCheckBox("contact");	
		initCheckBox("callLog");	
		initCheckBox("call");
		initCheckBox("smsRead");
		initCheckBox("smsSend");
	}
	
	private void initCheckBox(String permissionName){
		CheckBox checkBox = (CheckBox)findViewById(getIndentifier(permissionName));
		CheckBox checkBox_enhance = (CheckBox)findViewById(getIndentifier(permissionName + "_enhance"));
		permissionName = permissionName + "Access";
		if(control.getPermissionStatus(permissionName) == 1)
		{
			checkBox.setChecked(true);
			ControlAPI.EnhancedStatus temp = control.getEnhancedStatus(permissionName);
			switch(temp){
			case noEnhanced:
				break;
			case noControl:
				checkBox_enhance.setEnabled(true);
				break;
			case control:
				checkBox_enhance.setChecked(true);		
			    checkBox_enhance.setEnabled(true);
			    break;
			default:
			}
		}
		binding(checkBox, checkBox_enhance, permissionName);	
	}
	
	private void binding(final CheckBox checkBox, final CheckBox checkBox_enhance, final String permissionName){
		CompoundButton.OnCheckedChangeListener Listener = new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if(buttonView.isPressed()){
					if(isChecked) {
						if(control.setPermissionForbiden(permissionName) == 1){
							if(control.getEnhancedStatus(permissionName).equals(ControlAPI.EnhancedStatus.noControl)){
								checkBox_enhance.setEnabled(true);	
							}
						}
						else{
							checkBox.setChecked(false);
						}
					}
					else{ 
						control.setPermissionAllow(permissionName);	
						checkBox_enhance.setChecked(false);
						checkBox_enhance.setEnabled(false);
						
					}
				}
			}
		};
		CompoundButton.OnCheckedChangeListener Listener_enhance = new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if(buttonView.isPressed()){				
					if(isChecked) control.setEnhanced(permissionName);
					else control.setNoEnhanced(permissionName);
				}
			}
		};		
		checkBox.setOnCheckedChangeListener(Listener);
		checkBox_enhance.setOnCheckedChangeListener(Listener_enhance);
	}
	
	private int getIndentifier(String t) {
		return resouces.getIdentifier(t,"id",getPackageName());
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}

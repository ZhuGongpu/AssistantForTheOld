package view.settings;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.ToggleButton;
import view.main.R;

public class SettingsActivity extends Activity{

	private ToggleButton tb1;
	private ToggleButton tb2;
	private RelativeLayout rl1;
	private RelativeLayout rl2;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.config_view);
		
		findView();
		setView();
	}
	
	private void findView(){
		tb1 = (ToggleButton)findViewById(R.id.config_tb1);
		tb2 = (ToggleButton)findViewById(R.id.config_tb2);
		rl1 = (RelativeLayout)findViewById(R.id.config_bt1);
		rl2 = (RelativeLayout)findViewById(R.id.config_bt2);
	} 
	
	private void setView(){
		tb1.setOnCheckedChangeListener(ocl);
		tb2.setOnCheckedChangeListener(ocl);
		rl1.setOnClickListener(listener);
		rl2.setOnClickListener(listener);
	}
	
	private OnCheckedChangeListener ocl = new OnCheckedChangeListener() {		
		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			if(buttonView.getId()==tb1.getId()){
				if(isChecked)ConfigsSetting.autoRead=true;
				else ConfigsSetting.autoRead=false;
			}else if(buttonView.getId()==tb2.getId()){
				if(isChecked)ConfigsSetting.vibrateWhenNotify=true;
				else ConfigsSetting.vibrateWhenNotify=false;
			}
		}
	}; 
	private OnClickListener listener = new OnClickListener() {		
		@Override
		public void onClick(View v) {
			if(v.getId()==rl1.getId()){
				Builder b = new Builder(SettingsActivity.this);
				b.setTitle("请选择类型").setItems(new String[]{"男声","女声","粤语"}, new DialogInterface.OnClickListener(){					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						switch(which){
						case 0:ConfigsSetting.voiceType=ConfigsSetting.MAN;break;
						case 1:ConfigsSetting.voiceType=ConfigsSetting.WOMAN;break;
						case 2:ConfigsSetting.voiceType=ConfigsSetting.CANTONESE;break;
						}
						dialog.dismiss();
					}
				});
				b.show();
			}else if(v.getId()==rl2.getId()){
				Builder b = new Builder(SettingsActivity.this);
				b.setTitle("关于").setMessage("夕阳红生活助手由B127团队倾力开发").setNegativeButton("确定", new DialogInterface.OnClickListener() {					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
				b.show();
			}
		}
	};
}

package view.notification;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.*;
import android.widget.AdapterView.OnItemLongClickListener;
import view.main.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

public class NotificationActivity extends Activity {

    private ImageView addBtn;
    private ListView notifyList;
    private SimpleAdapter sa;
    private ArrayList<HashMap<String, String>> al;
    private int arg;
    private AlarmManager am;
    private Button pickBtn;
    private EditText pickEd;
    private TimePicker pickTime;
    private Dialog d;
    private String[] funcList = {"添加提醒", "设置提醒时间", "设置提醒内容", "删除提醒"};
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 0) {
                sa.notifyDataSetChanged();
            } else if (msg.what == 1) {
                sa.notifyDataSetChanged();
                notifyList.invalidate();
            } else if (msg.what == 2) {
                addNotify(msg);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notify_list_view);

        findView();
        setView();
    }

    private void findView() {
        addBtn = (ImageView) findViewById(R.id.notify_list_add_btn);
        notifyList = (ListView) findViewById(R.id.notify_list);
    }

    private void setView() {
        addBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                pickTime();
            }
        });
        al = new ArrayList<HashMap<String, String>>();
        sa = new SimpleAdapter(NotificationActivity.this, al, R.layout.notify_list_item, new String[]{"notify_list_item_tv1", "notify_list_item_tv2"}, new int[]{R.id.notify_list_item_tv1, R.id.notify_list_item_tv2});
        notifyList.setAdapter(sa);
        notifyList.setOnItemLongClickListener(new OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                Builder b = new Builder(NotificationActivity.this);
                arg = arg2;
                b.setTitle("删除提醒").setMessage("确定要删除提醒吗？").setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        al.remove(arg);
                        handler.sendEmptyMessage(1);

                    }
                }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).show();
                return false;
            }
        });

        am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
    }

    private void pickTime() {
        d = new Dialog(this);
        LayoutInflater li = LayoutInflater.from(this);
        View v = li.inflate(R.layout.pick_time_dialog, null);
        d.setContentView(v);
        d.setCancelable(true);
        d.show();

        pickBtn = (Button) v.findViewById(R.id.pick_dialog_button);
        pickEd = (EditText) v.findViewById(R.id.pick_dialog_input);
        pickTime = (TimePicker) v.findViewById(R.id.pick_dialog_picker);
        pickTime.setIs24HourView(true);

        pickBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Message m = handler.obtainMessage();
                m.arg1 = pickTime.getCurrentHour();
                m.arg2 = pickTime.getCurrentMinute();
                m.what = 2;
                m.obj = pickEd.getText().toString().trim();
                handler.sendMessage(m);
                d.cancel();
            }
        });
    }

    private void addNotify(Message msg) {
        HashMap<String, String> hm = new HashMap<String, String>();
        String str = (String) msg.obj;
        hm.put("notify_list_item_tv1", str);
        hm.put("notify_list_item_tv2", msg.arg1 + ":" + msg.arg2);
        al.add(hm);
        System.out.println(al.size());
        sa.notifyDataSetChanged();

        Intent intent = new Intent();
        intent.setAction("notify");
        intent.putExtra("content", str);
        PendingIntent pi = PendingIntent.getBroadcast(this, 0, intent, 0);
        Calendar ca = Calendar.getInstance();

        int cuHour = ca.get(Calendar.HOUR_OF_DAY);
        int cuMin = ca.get(Calendar.MINUTE);
        int bias = (msg.arg1 - cuHour) * 60 + msg.arg2 - cuMin;
        ca.add(Calendar.MINUTE, bias);
        am.set(AlarmManager.RTC_WAKEUP, ca.getTimeInMillis(), pi);
    }

//TODO 语音识别  && tts

}

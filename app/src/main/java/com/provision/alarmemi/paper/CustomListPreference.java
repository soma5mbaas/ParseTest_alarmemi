package com.provision.alarmemi.paper;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class CustomListPreference extends Activity {

	String key;
	String[] lists, values;
	int mode;
	boolean multi;
	ListView listView;

	@Override
	public void onCreate(Bundle b) {
		super.onCreate(b);
		Intent i = getIntent();
		setContentView(R.layout.custom_list_preference);
		key = i.getStringExtra("key");
		String title = i.getStringExtra("title");
		lists = i.getStringArrayExtra("lists");
		values = i.getStringArrayExtra("values");
		multi = i.getBooleanExtra("multi", false);
		String default_value = i.getStringExtra("default");
		mode = i.getIntExtra("mode", -1);
		((TextView) findViewById(R.id.pref_title)).setText(title);

		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				multi ? R.layout.simple_list_item_multiple_choice
						: R.layout.simple_list_item_single_choice, lists);
		listView = (ListView) findViewById(android.R.id.list);
		listView.setChoiceMode(multi ? ListView.CHOICE_MODE_MULTIPLE
				: ListView.CHOICE_MODE_SINGLE);
		listView.setAdapter(adapter);
		setDefault(default_value);
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View v, int position,
					long id) {
				if (!multi) {
					if (mode == 0)
						SettingsFragment.onActivityResult(key, lists[position],
                                values[position]);
					else if (mode == 1) {
						SetAlarmFragment.onActivityResult(key, String.valueOf(position));
					}
					finish();
				}
			}
		});

		ImageButton ok = (ImageButton) findViewById(R.id.ok);
		if (!multi) {
			ok.setVisibility(View.GONE);
			findViewById(R.id.divider).setVisibility(View.GONE);
		}
		ok.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				boolean[] array = new boolean[listView.getCount()];
				SparseBooleanArray checked = listView.getCheckedItemPositions();
				for (int i = 0; i < listView.getCount(); i++) {
					array[i] = checked.get(i);
				}
				SetAlarmFragment.onActivityResult(key,
                        SetAlarmFragment.booleanArrayToString(array));
				finish();
			}
		});

		findViewById(R.id.cancel).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				finish();
			}
		});
	}

	public void setDefault(String value) {
		if (value == null)
			return;
		if (mode == 0) {
			if (values == null)
				return;
			for (int i = 0; i < values.length; i++) {
				if (values[i].equals(value)) {
					listView.setItemChecked(i, true);
					return;
				}
			}
		} else if (mode == 1) {
			if (multi) {
				String[] str_array = value.split("\\|");
				boolean[] values = new boolean[str_array.length];
				for (int i = 0; i < str_array.length; i++) {
					values[i] = str_array[i].equals("1");
				}
				for (int i = 0; i < values.length; i++) {
					listView.setItemChecked(i, values[i]);
				}
			} else {
				listView.setItemChecked(Integer.parseInt(value), true);
			}
		}
	}

	public static String getEntryOfValue(String value, String[] lists,
			String[] values) {
		for (int i = 0; i < values.length; i++) {
			if (values[i].equals(value)) {
				return lists[i];
			}
		}
		return null;
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		if (hasFocus) {
			LinearLayout picker_center = (LinearLayout) findViewById(R.id.picker_center);
			LinearLayout picker_bottom = (LinearLayout) findViewById(R.id.picker_bottom);
			picker_center.setPadding(0, 0, 0, picker_bottom.getHeight());
		}
	}

}
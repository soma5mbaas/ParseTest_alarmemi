package com.provision.alarmemi.paper;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;

public class CustomEditTextPreference extends Activity {

	String key;
	int mode;
	EditText editText;

	@Override
	public void onCreate(Bundle b) {
		super.onCreate(b);
		Intent i = getIntent();
		setContentView(R.layout.custom_edit_text_preference);
		key = i.getStringExtra("key");
		String title = i.getStringExtra("title");
		String default_value = i.getStringExtra("default");
		mode = i.getIntExtra("mode", -1);
		((TextView) findViewById(R.id.pref_title)).setText(title);

		editText = (EditText) findViewById(R.id.editText);
		editText.setText(default_value == null ? "" : default_value);

		findViewById(R.id.ok).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (mode == 1) {
					SetAlarmFragment.onActivityResult(key, editText.getText()
                            .toString());
				}
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
}
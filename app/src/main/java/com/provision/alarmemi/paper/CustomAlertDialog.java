package com.provision.alarmemi.paper;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

public class CustomAlertDialog extends Activity {
	public interface CustomAlertDialogListener {
		void onOk();

		void onCancel();
	}

	public static CustomAlertDialogListener listener = null;
	boolean cancelable;

	@Override
	public void onCreate(Bundle b) {
		super.onCreate(b);
		setContentView(R.layout.custom_alert_dialog);
		TextView title = (TextView) findViewById(R.id.title);
		TextView content = (TextView) findViewById(R.id.content);
		Intent i = getIntent();
		title.setText(i.getStringExtra("title"));
		content.setText(i.getStringExtra("content"));
		cancelable = i.getBooleanExtra("cancelable", true);
		findViewById(R.id.ok).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (listener != null)
					listener.onOk();
				finish();
			}
		});
		if (cancelable) {
			findViewById(R.id.cancel).setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if (listener != null)
						listener.onCancel();
					finish();
				}
			});
		} else {
			findViewById(R.id.divider).setVisibility(View.GONE);
			findViewById(R.id.cancel).setVisibility(View.GONE);
		}
	}

	@Override
	public void onBackPressed() {
		if (cancelable) {
			super.onBackPressed();
			if (listener != null)
				listener.onCancel();
		}
	}
}

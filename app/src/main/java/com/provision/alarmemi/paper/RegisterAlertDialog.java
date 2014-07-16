package com.provision.alarmemi.paper;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;

public class RegisterAlertDialog extends Activity {
	public interface RegisterAlertDialogListener {
		void onOk(String text);
	}

	public static RegisterAlertDialogListener listener = null;

	@Override
	public void onCreate(Bundle b) {
		super.onCreate(b);
		setContentView(R.layout.register_alert_dialog);
		final EditText editText = (EditText) findViewById(R.id.editText);
		findViewById(R.id.ok).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (listener != null)
					listener.onOk(editText.getText().toString());
				finish();
			}
		});
	}

	@Override
	public void onBackPressed() {
	}
}

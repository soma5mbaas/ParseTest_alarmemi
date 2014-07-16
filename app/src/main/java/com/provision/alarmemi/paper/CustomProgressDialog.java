package com.provision.alarmemi.paper;

import android.app.Dialog;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.animation.RotateAnimation;

public class CustomProgressDialog extends Dialog {

	View progress;
	RotateAnimation anim;
	int dialogCount;

	public CustomProgressDialog(Context context) {
		super(context, R.style.ProgressDialog);
		setContentView(R.layout.progress_dialog);
		anim = new RotateAnimation(0, 360, RotateAnimation.RELATIVE_TO_SELF,
				0.5f, RotateAnimation.RELATIVE_TO_SELF, 0.5f);
		anim.setInterpolator(context, android.R.anim.linear_interpolator);
		anim.setRepeatCount(RotateAnimation.INFINITE);
		anim.setDuration(1000);
		progress = findViewById(R.id.progress);
		setCancelable(false);
		dialogCount = 0;
	}

	@Override
	public void show() {
		Log.d("progress", "show()");
		if (!isShowing())
			dialogCount = 0;
		if (dialogCount == 0) {
			super.show();
			progress.startAnimation(anim);
		}
		dialogCount++;
	}

	@Override
	public void dismiss() {
		Log.d("progress", "dissmiss()");
		if (dialogCount > 0)
			dialogCount--;
		if (dialogCount == 0) {
			super.dismiss();
		}
	}
}

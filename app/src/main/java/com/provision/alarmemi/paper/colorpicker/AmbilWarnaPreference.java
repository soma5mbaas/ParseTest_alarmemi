package com.provision.alarmemi.paper.colorpicker;

import com.provision.alarmemi.R;
import com.provision.alarmemi.colorpicker.AmbilWarnaDialog.OnAmbilWarnaListener;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.View;

public class AmbilWarnaPreference extends Preference implements
		OnAmbilWarnaListener {
	int value;
	Context context;

	public AmbilWarnaPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
		setWidgetLayoutResource(R.layout.ambilwarna_pref_widget);
	}

	protected void onBindView(View view) {
		super.onBindView(view);

		// Set our custom views inside the layout
		final View kotak = view.findViewById(R.id.ambilwarna_pref_widget_kotak);
		if (kotak != null) {
			kotak.setBackgroundColor(value);
		}
	}

	protected void onClick() {
		AmbilWarnaDialog.listener = this;
		Intent intent = new Intent(context, AmbilWarnaDialog.class);
		intent.putExtra("color", value);
		context.startActivity(intent);
	}

	@Override
	public void onOk(AmbilWarnaDialog dialog, int color) {
		if (!callChangeListener(color))
			return; // They don't want the value to be set
		value = color;
		persistInt(value);
		notifyChanged();
	}

	@Override
	public void onCancel(AmbilWarnaDialog dialog) {
	}

	public void forceSetValue(int value) {
		this.value = value;
		persistInt(value);
		notifyChanged();
	}

	public int getValue() {
		return value;
	}

	protected Object onGetDefaultValue(TypedArray a, int index) {
		// This preference type's value type is Integer, so we read the default
		// value from the attributes as an Integer.
		return a.getInteger(index, 0);
	}

	protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
		if (restoreValue) { // Restore state
			value = getPersistedInt(value);
		} else { // Set state
			int value = (Integer) defaultValue;
			this.value = value;
			persistInt(value);
		}
	}

	/*
	 * Suppose a client uses this preference type without persisting. We must
	 * save the instance state so it is able to, for example, survive
	 * orientation changes.
	 */
	protected Parcelable onSaveInstanceState() {
		final Parcelable superState = super.onSaveInstanceState();
		if (isPersistent())
			return superState; // No need to save instance state since it's
								// persistent

		final SavedState myState = new SavedState(superState);
		myState.value = value;
		return myState;
	}

	protected void onRestoreInstanceState(Parcelable state) {
		if (!state.getClass().equals(SavedState.class)) {
			// Didn't save state for us in onSaveInstanceState
			super.onRestoreInstanceState(state);
			return;
		}

		// Restore the instance state
		SavedState myState = (SavedState) state;
		super.onRestoreInstanceState(myState.getSuperState());
		this.value = myState.value;
		notifyChanged();
	}

	/**
	 * SavedState, a subclass of {@link android.preference.Preference.BaseSavedState}, will store the state of
	 * MyPreference, a subclass of Preference.
	 * <p>
	 * It is important to always call through to super methods.
	 */
	private static class SavedState extends BaseSavedState {
		int value;

		public SavedState(Parcel source) {
			super(source);
			value = source.readInt();
		}

		public void writeToParcel(Parcel dest, int flags) {
			super.writeToParcel(dest, flags);
			dest.writeInt(value);
		}

		public SavedState(Parcelable superState) {
			super(superState);
		}

		@SuppressWarnings("unused")
		public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {
			public SavedState createFromParcel(Parcel in) {
				return new SavedState(in);
			}

			public SavedState[] newArray(int size) {
				return new SavedState[size];
			}
		};
	}
}

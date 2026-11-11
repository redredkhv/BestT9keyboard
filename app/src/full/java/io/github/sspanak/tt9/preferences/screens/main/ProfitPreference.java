package io.github.sspanak.tt9.preferences.screens.main;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;

import io.github.sspanak.tt9.BuildConfig;
import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.preferences.PreferencesActivity;
import io.github.sspanak.tt9.ui.UI;
import io.github.sspanak.tt9.util.Logger;

public class ProfitPreference extends Preference {
	public static final String NAME = "screen_profit";

	public ProfitPreference(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		init(context);
	}

	public ProfitPreference(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init(context);
	}

	public ProfitPreference(@NonNull Context context, @Nullable AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public ProfitPreference(@NonNull Context context) {
		super(context);
		init(context);
	}


	protected void init(@NonNull Context context) {
		setTitle(context.getString(R.string.donate_title));
		// BestT9keyboard: placeholder, to be filled in later (no link yet)
		setSummary("");
	}


	@Override
	public void onBindViewHolder(@NonNull PreferenceViewHolder holder) {
		super.onBindViewHolder(holder);
		holder.itemView.setOnLongClickListener(this::onLongClick);
	}


	public ProfitPreference populate(@NonNull PreferencesActivity activity, boolean isTT9On) {
		boolean isVisible = isTT9On && !activity.getSettings().getDemoMode();
		setVisible(isVisible);
		setIconSpaceReserved(false);

		return this;
	}


	@Override
	protected void onClick() {
		super.onClick(); // BestT9keyboard: no action yet
	}


	private boolean onLongClick(View v) {
		if (true) return true; // BestT9keyboard: no donation link yet
		try {
			getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(BuildConfig.DONATION_URL)));
			return true;
		} catch (Exception e) {
			Logger.w(getClass().getSimpleName(), "Cannot navigate to the donation page. " + e.getMessage() + " (do you have a browser?)");
			return false;
		}
	}
}

package io.github.sspanak.tt9.preferences.screens.languages;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;

import java.util.ArrayList;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.languages.LanguageCollection;
import io.github.sspanak.tt9.languages.LanguageKind;
import io.github.sspanak.tt9.preferences.PreferencesActivity;
import io.github.sspanak.tt9.ui.main.keys.QwertyLayouts;

/**
 * BestT9keyboard: "Languages and types", like on the Samsung keyboard. Every enabled language is
 * listed with its layout type. Tapping a language lets the user choose between 3x4 (T9) and QWERTY.
 */
class ItemsLanguageLayouts {
	static final String CATEGORY = "category_bt9_layouts";

	private final PreferencesActivity activity;
	private final PreferenceCategory category;


	ItemsLanguageLayouts(@Nullable PreferencesActivity activity, @Nullable PreferenceCategory category) {
		this.activity = activity;
		this.category = category;
	}


	void populate() {
		if (activity == null || category == null) {
			return;
		}

		category.removeAll();

		ArrayList<Integer> ids = activity.getSettings().getEnabledLanguageIds();
		for (Integer id : ids) {
			Language language = LanguageCollection.getLanguage(id);
			if (language == null) {
				continue;
			}

			Preference item = new Preference(activity);
			item.setKey("bt9_layout_" + id);
			item.setTitle(language.getName());
			item.setSummary(getTypeName(language, activity.getSettings().isQwertyLanguage(language)));
			item.setIconSpaceReserved(false);
			item.setOnPreferenceClickListener(p -> {
				showChooser(language, p);
				return true;
			});
			category.addPreference(item);
		}
	}


	@NonNull
	private String getTypeName(@NonNull Language language, boolean qwerty) {
		if (qwerty) {
			return activity.getString(R.string.bt9_layout_qwerty);
		}
		return LanguageKind.isKorean(language) ? activity.getString(R.string.bt9_layout_cheonjiin) : activity.getString(R.string.bt9_layout_3x4);
	}


	private void showChooser(@NonNull Language language, @NonNull Preference item) {
		if (!QwertyLayouts.has(language)) {
			// only 3x4 is available for this language
			new AlertDialog.Builder(activity)
				.setTitle(language.getName())
				.setMessage(R.string.bt9_layout_only_3x4)
				.setPositiveButton(android.R.string.ok, null)
				.show();
			return;
		}

		final String[] options = { getTypeName(language, false), getTypeName(language, true) };
		final int checked = activity.getSettings().isQwertyLanguage(language) ? 1 : 0;

		new AlertDialog.Builder(activity)
			.setTitle(language.getName())
			.setSingleChoiceItems(options, checked, (dialog, which) -> {
				activity.getSettings().setQwertyLanguage(QwertyLayouts.getCode(language), which == 1);
				item.setSummary(options[which]);
				dialog.dismiss();
			})
			.setNegativeButton(android.R.string.cancel, null)
			.show();
	}
}

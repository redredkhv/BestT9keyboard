package io.github.sspanak.tt9.preferences.screens.languages;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import io.github.sspanak.tt9.db.DataStore;
import io.github.sspanak.tt9.db.words.DictionaryDeleter;
import io.github.sspanak.tt9.db.words.DictionaryLoader;
import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.languages.LanguageCollection;
import io.github.sspanak.tt9.preferences.PreferencesActivity;
import io.github.sspanak.tt9.util.Logger;

/**
 * BestT9keyboard: keeps the dictionaries in sync with the enabled languages, so the user never has
 * to load or delete them manually. Enabled languages without a dictionary are loaded, disabled
 * languages that still have one are deleted. Custom (added) words are kept, they live in a separate table.
 */
class DictionarySync {
	private static final String LOG_TAG = DictionarySync.class.getSimpleName();
	private static boolean running = false;


	static void run(@NonNull PreferencesActivity activity) {
		if (running || DictionaryLoader.isRunning() || DictionaryDeleter.getInstance(activity).isRunning()) {
			return;
		}

		final ArrayList<Integer> enabledIds = activity.getSettings().getEnabledLanguageIds();
		final ArrayList<Language> all = LanguageCollection.getAll(false);
		if (all.isEmpty()) {
			return;
		}

		running = true;

		final List<Language> toLoad = Collections.synchronizedList(new ArrayList<>());
		final List<Language> toDelete = Collections.synchronizedList(new ArrayList<>());
		final AtomicInteger pending = new AtomicInteger(all.size());

		for (Language language : all) {
			DataStore.getLastLanguageUpdateTime(hash -> {
				final boolean hasDictionary = hash != null && !hash.isEmpty();
				final boolean enabled = enabledIds.contains(language.getId());

				if (enabled && !hasDictionary) {
					toLoad.add(language);
				} else if (!enabled && hasDictionary) {
					toDelete.add(language);
				}

				if (pending.decrementAndGet() == 0) {
					activity.runOnUiThread(() -> apply(activity, new ArrayList<>(toLoad), new ArrayList<>(toDelete)));
				}
			}, language);
		}
	}


	private static void apply(@NonNull PreferencesActivity activity, @NonNull ArrayList<Language> toLoad, @NonNull ArrayList<Language> toDelete) {
		Logger.d(LOG_TAG, "Dictionaries to load: " + toLoad.size() + ", to delete: " + toDelete.size());

		final Runnable load = () -> {
			if (!toLoad.isEmpty()) {
				DictionaryLoader.load(activity, activity.getSettings(), toLoad);
			}
			running = false;
		};

		if (toDelete.isEmpty()) {
			load.run();
			return;
		}

		// SQLite does not like parallel writes, so delete first, then load
		DictionaryDeleter deleter = DictionaryDeleter.getInstance(activity);
		deleter.setOnFinish(() -> activity.runOnUiThread(load));
		deleter.deleteLanguages(toDelete);
	}
}

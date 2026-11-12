package io.github.sspanak.tt9.preferences.screens.languages;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import androidx.appcompat.app.AlertDialog;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.db.DataStore;
import io.github.sspanak.tt9.db.entities.WordFile;
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


	static void run(@NonNull PreferencesActivity activity, @NonNull Runnable onLoadStarted) {
		if (running || DictionaryLoader.isRunning() || DictionaryDeleter.getInstance(activity).isRunning()) {
			return;
		}

		final ArrayList<Integer> enabledIds = activity.getSettings().getEnabledLanguageIds();
		final ArrayList<Language> all = LanguageCollection.getAll(false);
		if (all.isEmpty()) {
			return;
		}

		// only act when the user has changed the enabled languages, never just because the screen was opened
		final ArrayList<Integer> sorted = new ArrayList<>(enabledIds);
		Collections.sort(sorted);
		final String signature = sorted.toString();
		if (signature.equals(activity.getSettings().getLastSyncedLanguages())) {
			return;
		}
		activity.getSettings().setLastSyncedLanguages(signature);

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
					activity.runOnUiThread(() -> apply(activity, new ArrayList<>(toLoad), new ArrayList<>(toDelete), onLoadStarted));
				}
			}, language);
		}
	}


	/**
	 * Dictionaries inside the app are loaded right away. The others must be downloaded, so the user
	 * is asked first, with the download size.
	 */
	private static void loadWithConfirmation(@NonNull PreferencesActivity activity, @NonNull ArrayList<Language> toLoad, @NonNull Runnable onLoadStarted) {
		final ArrayList<Language> bundled = new ArrayList<>();
		final ArrayList<Language> remote = new ArrayList<>();
		final StringBuilder remoteList = new StringBuilder();

		for (Language language : toLoad) {
			WordFile file = new WordFile(activity, language, activity.getAssets());
			if (file.exists()) {
				bundled.add(language);
			} else {
				remote.add(language);
				remoteList.append("\n• ").append(language.getName()).append(" (").append(file.getFormattedSize()).append(")");
			}
		}

		if (remote.isEmpty()) {
			startLoading(activity, bundled, onLoadStarted);
			return;
		}

		new AlertDialog.Builder(activity)
			.setTitle(R.string.bt9_download_dictionaries_title)
			.setMessage(activity.getString(R.string.bt9_download_dictionaries_message) + remoteList)
			.setPositiveButton(R.string.bt9_download, (d, w) -> {
				ArrayList<Language> all = new ArrayList<>(bundled);
				all.addAll(remote);
				startLoading(activity, all, onLoadStarted);
			})
			.setNegativeButton(R.string.bt9_not_now, (d, w) -> startLoading(activity, bundled, onLoadStarted))
			.setOnCancelListener(d -> startLoading(activity, bundled, onLoadStarted))
			.show();
	}


	private static void startLoading(@NonNull PreferencesActivity activity, @NonNull ArrayList<Language> languages, @NonNull Runnable onLoadStarted) {
		if (!languages.isEmpty() && DictionaryLoader.load(activity, activity.getSettings(), languages)) {
			onLoadStarted.run(); // show the progress on the screen, notifications may be disabled
		}
		running = false;
	}


	private static void apply(@NonNull PreferencesActivity activity, @NonNull ArrayList<Language> toLoad, @NonNull ArrayList<Language> toDelete, @NonNull Runnable onLoadStarted) {
		Logger.d(LOG_TAG, "Dictionaries to load: " + toLoad.size() + ", to delete: " + toDelete.size());

		final Runnable load = () -> loadWithConfirmation(activity, toLoad, onLoadStarted);

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

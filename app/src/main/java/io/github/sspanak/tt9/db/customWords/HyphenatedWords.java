package io.github.sspanak.tt9.db.customWords;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.github.sspanak.tt9.db.DataStore;
import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;
import io.github.sspanak.tt9.ui.main.keys.QwertyLayouts;

/**
 * BestT9keyboard: common hyphenated words. The dictionaries contain only their parts ("из-", "-то"),
 * so these words are added once as custom words. Since the hyphen between letters is not typed,
 * "из-за" comes up after 4-3-3-2.
 */
public class HyphenatedWords {
	private static final String[] RUSSIAN = {
		"из-за", "из-под", "кое-что", "кое-кто", "кое-где", "кое-как", "кое-какой",
		"что-то", "кто-то", "где-то", "как-то", "когда-то", "куда-то", "откуда-то", "почему-то", "зачем-то",
		"чей-то", "какой-то", "какая-то", "какое-то", "какие-то", "какую-то", "каким-то", "какого-то",
		"чего-то", "кого-то", "кому-то", "чем-то", "чему-то",
		"что-нибудь", "кто-нибудь", "где-нибудь", "как-нибудь", "когда-нибудь", "куда-нибудь", "какой-нибудь",
		"что-либо", "кто-либо", "где-либо", "какой-либо",
		"по-моему", "по-твоему", "по-своему", "по-нашему", "по-русски", "по-английски", "по-корейски",
		"по-другому", "по-прежнему", "по-настоящему", "по-разному", "по-новому", "по-хорошему",
		"всё-таки", "все-таки", "вот-вот", "чуть-чуть", "еле-еле", "давным-давно", "точь-в-точь",
		"во-первых", "во-вторых", "в-третьих", "в-четвёртых", "так-то", "ну-ка", "ей-богу", "мало-помалу"
	};


	public static void seed(@NonNull SettingsStore settings, @Nullable Language language) {
		final String code = QwertyLayouts.getCode(language);
		if (language == null || !"ru".equals(code) || settings.isHyphenatedWordsSeeded(code)) {
			return;
		}

		// mark as done only if the database accepted the words, otherwise try again next time
		final java.util.concurrent.atomic.AtomicInteger pending = new java.util.concurrent.atomic.AtomicInteger(RUSSIAN.length);
		final java.util.concurrent.atomic.AtomicBoolean failed = new java.util.concurrent.atomic.AtomicBoolean(false);
		for (String word : RUSSIAN) {
			DataStore.put(result -> {
				// common words go to the top of their suggestions right away
				if (result.statusCode() == io.github.sspanak.tt9.db.entities.AddWordResult.CODE_SUCCESS || result.statusCode() == io.github.sspanak.tt9.db.entities.AddWordResult.CODE_WORD_EXISTS) {
					try {
						DataStore.makeTopWord(language, word, language.getDigitSequenceForWord(word));
					} catch (Exception ignored) {}
				}
				if (result.statusCode() != io.github.sspanak.tt9.db.entities.AddWordResult.CODE_SUCCESS && result.statusCode() != io.github.sspanak.tt9.db.entities.AddWordResult.CODE_WORD_EXISTS) {
					failed.set(true);
				}
				if (pending.decrementAndGet() == 0 && !failed.get()) {
					settings.setHyphenatedWordsSeeded(code);
				}
			}, language, word, false);
		}
	}
}

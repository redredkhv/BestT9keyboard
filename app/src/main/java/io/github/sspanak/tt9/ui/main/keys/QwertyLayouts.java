package io.github.sspanak.tt9.ui.main.keys;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.HashMap;

import io.github.sspanak.tt9.languages.Language;

/**
 * BestT9keyboard: QWERTY layouts. Each layout has 3 rows of letters, normal and shifted.
 * Row 3 is surrounded by Shift and Backspace.
 */
public class QwertyLayouts {
	public static final int MAX_KEYS_IN_ROW = 11;

	private static final HashMap<String, String[][]> LAYOUTS = new HashMap<>() {{
		put("en", new String[][] {
			{"qwertyuiop", "asdfghjkl", "zxcvbnm"},
			{"QWERTYUIOP", "ASDFGHJKL", "ZXCVBNM"}
		});
		put("ru", new String[][] {
			{"йцукенгшщзх", "фывапролджэ", "ячсмитьбю"},
			{"ЙЦУКЕНГШЩЗХ", "ФЫВАПРОЛДЖЭ", "ЯЧСМИТЬБЮ"}
		});
		put("ko", new String[][] {
			{"ㅂㅈㄷㄱㅅㅛㅕㅑㅐㅔ", "ㅁㄴㅇㄹㅎㅗㅓㅏㅣ", "ㅋㅌㅊㅍㅠㅜㅡ"},
			{"ㅃㅉㄸㄲㅆㅛㅕㅑㅒㅖ", "ㅁㄴㅇㄹㅎㅗㅓㅏㅣ", "ㅋㅌㅊㅍㅠㅜㅡ"}
		});
	}};


	@NonNull
	public static String getCode(@Nullable Language language) {
		return language == null || language.getLocale() == null ? "" : language.getLocale().getLanguage();
	}


	public static boolean has(@Nullable Language language) {
		return LAYOUTS.containsKey(getCode(language));
	}


	public static boolean has(@NonNull String languageCode) {
		return LAYOUTS.containsKey(languageCode);
	}


	@NonNull
	public static String getKey(@Nullable Language language, int row, int column, boolean shifted) {
		String[][] layout = LAYOUTS.get(getCode(language));
		if (layout == null || row < 0 || row > 2) {
			return "";
		}

		String letters = layout[shifted ? 1 : 0][row];
		// count by code points to be safe with any script
		int count = letters.codePointCount(0, letters.length());
		if (column < 0 || column >= count) {
			return "";
		}

		int start = letters.offsetByCodePoints(0, column);
		int end = letters.offsetByCodePoints(start, 1);
		return letters.substring(start, end);
	}


	public static String[] getSupportedCodes() {
		return LAYOUTS.keySet().toArray(new String[0]);
	}
}

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


	public static final String DIGITS = "1234567890";

	// Samsung-style symbol pages: 3 rows each, the digits row is always shown above them
	private static final String[][] SYMBOLS = {
		{"+×÷=/_<>[]", "!@#~%^&*()", "-'\":;,?"},
		{"`|{}€£¥₩$¢", "°•○●□■♤♡◇♧", "☆▪¤《》¡¿"}
	};


	/** A key of a symbols page (page = 1 or 2). Row -1 = digits. */
	@NonNull
	public static String getSymbol(int page, int row, int column) {
		if (row == -1) {
			return column >= 0 && column < DIGITS.length() ? DIGITS.substring(column, column + 1) : "";
		}
		if (page < 1 || page > SYMBOLS.length || row < 0 || row > 2) {
			return "";
		}
		return charAt(SYMBOLS[page - 1][row], column);
	}


	@NonNull
	private static String charAt(@NonNull String letters, int column) {
		int count = letters.codePointCount(0, letters.length());
		if (column < 0 || column >= count) {
			return "";
		}
		int start = letters.offsetByCodePoints(0, column);
		int end = letters.offsetByCodePoints(start, 1);
		return letters.substring(start, end);
	}


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
		if (row == -1) {
			return charAt(DIGITS, column); // the optional digits row
		}
		if (layout == null || row < 0 || row > 2) {
			return "";
		}

		return charAt(layout[shifted ? 1 : 0][row], column);
	}


	public static String[] getSupportedCodes() {
		return LAYOUTS.keySet().toArray(new String[0]);
	}
}

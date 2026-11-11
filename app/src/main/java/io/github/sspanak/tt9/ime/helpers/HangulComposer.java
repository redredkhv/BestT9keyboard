package io.github.sspanak.tt9.ime.helpers;

/**
 * BestT9keyboard: Hangul syllable composer for the 2-set (두벌식) QWERTY layout.
 * Takes compatibility jamo (ㄱ, ㅏ...) one by one and builds syllables (ㅎ+ㅏ+ㄴ = 한).
 * Every input() returns the text that must be committed (finished syllables), while
 * getComposing() returns the syllable that is still being composed.
 */
public class HangulComposer {
	private static final String CHO = "ㄱㄲㄴㄷㄸㄹㅁㅂㅃㅅㅆㅇㅈㅉㅊㅋㅌㅍㅎ";
	private static final String JUNG = "ㅏㅐㅑㅒㅓㅔㅕㅖㅗㅘㅙㅚㅛㅜㅝㅞㅟㅠㅡㅢㅣ";
	private static final String JONG = "\0ㄱㄲㄳㄴㄵㄶㄷㄹㄺㄻㄼㄽㄾㄿㅀㅁㅂㅄㅅㅆㅇㅈㅊㅋㅌㅍㅎ";

	// compound vowels: first + second = result
	private static final String[][] JUNG_PAIRS = {
		{"ㅗ", "ㅏ", "ㅘ"}, {"ㅗ", "ㅐ", "ㅙ"}, {"ㅗ", "ㅣ", "ㅚ"},
		{"ㅜ", "ㅓ", "ㅝ"}, {"ㅜ", "ㅔ", "ㅞ"}, {"ㅜ", "ㅣ", "ㅟ"},
		{"ㅡ", "ㅣ", "ㅢ"}
	};

	// compound final consonants: first + second = result
	private static final String[][] JONG_PAIRS = {
		{"ㄱ", "ㅅ", "ㄳ"}, {"ㄴ", "ㅈ", "ㄵ"}, {"ㄴ", "ㅎ", "ㄶ"},
		{"ㄹ", "ㄱ", "ㄺ"}, {"ㄹ", "ㅁ", "ㄻ"}, {"ㄹ", "ㅂ", "ㄼ"}, {"ㄹ", "ㅅ", "ㄽ"},
		{"ㄹ", "ㅌ", "ㄾ"}, {"ㄹ", "ㅍ", "ㄿ"}, {"ㄹ", "ㅎ", "ㅀ"},
		{"ㅂ", "ㅅ", "ㅄ"}
	};

	private String cho = ""; // initial consonant
	private String jung = ""; // vowel (may be compound)
	private String jong = ""; // final consonant (may be compound)


	public static boolean isConsonant(String c) {
		return c != null && c.length() == 1 && (CHO.contains(c) || JONG.indexOf(c) > 0);
	}

	public static boolean isVowel(String c) {
		return c != null && c.length() == 1 && JUNG.contains(c);
	}

	public static boolean isJamo(String c) {
		return isConsonant(c) || isVowel(c);
	}


	public boolean isEmpty() {
		return cho.isEmpty() && jung.isEmpty() && jong.isEmpty();
	}


	public void reset() {
		cho = jung = jong = "";
	}


	/**
	 * Adds a jamo. Returns the text that is now complete and must be committed (may be empty).
	 */
	public String input(String c) {
		if (isVowel(c)) {
			return inputVowel(c);
		}
		if (isConsonant(c)) {
			return inputConsonant(c);
		}

		// not a jamo: commit everything
		String done = getComposing();
		reset();
		return done;
	}


	private String inputConsonant(String c) {
		if (isEmpty()) {
			if (CHO.contains(c)) {
				cho = c;
				return "";
			}
			return c; // a compound consonant alone, e.g. ㄳ
		}

		if (!cho.isEmpty() && jung.isEmpty()) {
			// a consonant after a lone consonant: commit the first one
			String done = getComposing();
			reset();
			return done + inputConsonant(c);
		}

		if (jung.isEmpty()) {
			// a lone vowel, then a consonant: commit the vowel
			String done = getComposing();
			reset();
			return done + inputConsonant(c);
		}

		if (cho.isEmpty()) {
			// a lone vowel (no initial consonant) cannot take a final consonant
			String done = getComposing();
			reset();
			return done + inputConsonant(c);
		}

		if (jong.isEmpty()) {
			if (JONG.indexOf(c) > 0) {
				jong = c;
				return "";
			}
			// ㄸ, ㅃ, ㅉ cannot be final
			String done = getComposing();
			reset();
			cho = c;
			return done;
		}

		String compound = combine(JONG_PAIRS, jong, c);
		if (compound != null) {
			jong = compound;
			return "";
		}

		String done = getComposing();
		reset();
		return done + inputConsonant(c);
	}


	private String inputVowel(String c) {
		if (isEmpty()) {
			jung = c;
			return "";
		}

		if (jung.isEmpty()) {
			// initial consonant + vowel
			jung = c;
			return "";
		}

		if (jong.isEmpty()) {
			String compound = combine(JUNG_PAIRS, jung, c);
			if (compound != null) {
				jung = compound;
				return "";
			}
			String done = getComposing();
			reset();
			jung = c;
			return done;
		}

		// the final consonant moves to the next syllable: 한 + ㅏ = 하나
		String moving;
		String[] split = split(JONG_PAIRS, jong);
		if (split != null) {
			jong = split[0];
			moving = split[1];
		} else {
			moving = jong;
			jong = "";
		}

		String done = getComposing();
		reset();
		cho = moving;
		jung = c;
		return done;
	}


	/**
	 * Removes the last jamo. Returns false if there was nothing to remove.
	 */
	public boolean backspace() {
		if (!jong.isEmpty()) {
			String[] split = split(JONG_PAIRS, jong);
			jong = split != null ? split[0] : "";
			return true;
		}
		if (!jung.isEmpty()) {
			String[] split = split(JUNG_PAIRS, jung);
			jung = split != null ? split[0] : "";
			return true;
		}
		if (!cho.isEmpty()) {
			cho = "";
			return true;
		}
		return false;
	}


	public String getComposing() {
		if (isEmpty()) {
			return "";
		}
		if (cho.isEmpty() || jung.isEmpty()) {
			return cho + jung + jong;
		}

		int choIndex = CHO.indexOf(cho);
		int jungIndex = JUNG.indexOf(jung);
		int jongIndex = jong.isEmpty() ? 0 : JONG.indexOf(jong);
		if (choIndex < 0 || jungIndex < 0 || jongIndex < 0) {
			return cho + jung + jong;
		}

		return String.valueOf((char) (0xAC00 + (choIndex * 21 + jungIndex) * 28 + jongIndex));
	}


	private static String combine(String[][] pairs, String first, String second) {
		for (String[] pair : pairs) {
			if (pair[0].equals(first) && pair[1].equals(second)) {
				return pair[2];
			}
		}
		return null;
	}


	private static String[] split(String[][] pairs, String compound) {
		for (String[] pair : pairs) {
			if (pair[2].equals(compound)) {
				return new String[] {pair[0], pair[1]};
			}
		}
		return null;
	}
}

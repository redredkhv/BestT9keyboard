package io.github.sspanak.tt9.ui.main.keys;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;

import java.util.ArrayList;

import io.github.sspanak.tt9.commands.CmdBackspace;
import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.util.chars.Characters;

/**
 * BestT9keyboard: a key of the QWERTY layout. The role is defined by android:tag:
 * "L{row}_{column}" = letter, "shift", "backspace", "space", "comma", "dot".
 */
public class SoftKeyQwerty extends SoftKeyText {
	private int row = -1;
	private int column = -1;
	private String role = null;

	public SoftKeyQwerty(Context context) { super(context); }
	public SoftKeyQwerty(Context context, AttributeSet attrs) { super(context, attrs); }
	public SoftKeyQwerty(Context context, AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); }


	@NonNull
	private String getRole() {
		if (role == null) {
			Object tag = getTag();
			role = tag != null ? tag.toString() : "";
			if (role.startsWith("L")) {
				String[] parts = role.substring(1).split("_");
				try {
					row = Integer.parseInt(parts[0]);
					column = Integer.parseInt(parts[1]);
				} catch (Exception e) {
					row = column = -1;
				}
				role = "letter";
			}
			isSwipeable = role.equals("space") && (tt9 == null || tt9.getSettings().isLanguageSwitchBySwipe());
		}
		return role;
	}


	private boolean isShifted() {
		return tt9 != null && tt9.getQwertyShift() > 0;
	}


	@Override
	public void render() {
		if (getRole().equals("space")) {
			isSwipeable = tt9 == null || tt9.getSettings().isLanguageSwitchBySwipe();
		}
		super.render();
	}


	@Override
	public boolean isDynamic() {
		return true;
	}


	@Override
	protected String getKeyChar() {
		if (tt9 == null) {
			return "";
		}

		return switch (getRole()) {
			case "letter" -> tt9.getQwertySymbolsPage() > 0
				? QwertyLayouts.getSymbol(tt9.getQwertySymbolsPage(), row, column)
				: QwertyLayouts.getKey(tt9.getLanguage(), row, column, isShifted());
			case "space" -> Characters.getSpace(tt9.getLanguage());
			case "comma" -> Characters.getChar(tt9.getLanguage(), ",");
			case "dot" -> Characters.getChar(tt9.getLanguage(), ".");
			default -> "";
		};
	}


	@Override
	protected String getTitle() {
		return switch (getRole()) {
			case "shift" -> {
				if (tt9 != null && tt9.getQwertySymbolsPage() > 0) {
					yield tt9.getQwertySymbolsPage() + "/2";
				}
				yield tt9 != null && tt9.getQwertyShift() == 2 ? "⇪" : "⇧";
			}
			case "symbols" -> {
				if (tt9 == null || tt9.getQwertySymbolsPage() == 0) {
					yield "!#1";
				}
				final String code = QwertyLayouts.getCode(tt9.getLanguage());
				yield switch (code) {
					case "ru" -> "АБВ";
					case "ko" -> "한글";
					default -> "ABC";
				};
			}
			case "backspace" -> "⌫";
			case "space" -> {
				Language language = tt9 != null ? tt9.getLanguage() : null;
				yield language != null ? language.getName() : "␣";
			}
			default -> getKeyChar();
		};
	}


	@Override
	protected float getTitleScale() {
		return getRole().equals("space") ? super.getTitleScale() * 0.6f : super.getTitleScale();
	}


	@Override
	protected boolean shouldHide() {
		return getRole().equals("letter") && getKeyChar().isEmpty();
	}


	// long press on a letter types its alternative (Russian: е -> ё, ь -> ъ)
	private static final String ALT_FROM = "еЕьЬ";
	private static final String ALT_TO = "ёЁъЪ";
	private boolean altTyped = false;


	@Override
	protected boolean handlePress() {
		altTyped = false;
		return super.handlePress();
	}


	@Override
	protected void handleHold() {
		if (getRole().equals("letter")) {
			preventRepeat();
			final String letter = getKeyChar();
			final int alt = letter.length() == 1 ? ALT_FROM.indexOf(letter) : -1;
			if (alt >= 0 && tt9 != null) {
				altTyped = tt9.onQwertyLetter(ALT_TO.substring(alt, alt + 1));
			}
			return;
		}

		if (getRole().equals("backspace")) {
			// repeat deleting while held
			if (tt9 != null) {
				new CmdBackspace().run(tt9);
			}
			return;
		}
		super.handleHold();
	}


	@Override
	protected boolean handleRelease() {
		if (altTyped) {
			altTyped = false;
			return true;
		}

		if (tt9 == null || !notSwiped()) {
			return false;
		}

		return switch (getRole()) {
			case "letter" -> tt9.onQwertyLetter(getKeyChar());
			case "shift" -> {
				if (tt9.getQwertySymbolsPage() > 0) {
					tt9.nextQwertySymbolsPage();
				} else {
					tt9.nextQwertyShift();
				}
				yield true;
			}
			case "symbols" -> {
				tt9.toggleQwertySymbols();
				yield true;
			}
			case "backspace" -> new CmdBackspace().run(tt9);
			case "space", "comma", "dot" -> tt9.onText(getKeyChar(), false);
			default -> false;
		};
	}


	/** Swipe on the spacebar: right = next language, left = previous. */
	@Override
	protected void handleEndSwipeX(float position, float delta) {
		if (tt9 == null || !getRole().equals("space")) {
			return;
		}

		switchLanguage(tt9, delta > 0);
	}


	public static void switchLanguage(@NonNull io.github.sspanak.tt9.ime.TraditionalT9 tt9, boolean next) {
		tt9.finishQwertyComposing();
		ArrayList<Integer> ids = tt9.getSettings().getEnabledLanguageIds();
		if (ids.size() < 2) {
			return;
		}

		int current = tt9.getLanguage() != null ? ids.indexOf(tt9.getLanguage().getId()) : -1;
		int target = next ? (current + 1) % ids.size() : (current - 1 + ids.size()) % ids.size();
		tt9.setLang(ids.get(target));
	}
}

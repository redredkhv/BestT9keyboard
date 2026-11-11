package io.github.sspanak.tt9.ui.main.keys;

import android.content.Context;
import android.util.AttributeSet;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.util.chars.Characters;

/**
 * BestT9keyboard: one of the four punctuation keys in the left column (TouchPal-style).
 * The character on each key is configurable in the settings.
 */
public class SoftKeyPunct extends SoftKeyText {
	public SoftKeyPunct(Context context) { super(context); }
	public SoftKeyPunct(Context context, AttributeSet attrs) { super(context, attrs); }
	public SoftKeyPunct(Context context, AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); }


	private int getIndex() {
		final int id = getId();
		if (id == R.id.soft_key_punct_2) return 1;
		if (id == R.id.soft_key_punct_3) return 2;
		if (id == R.id.soft_key_punct_4) return 3;
		return 0;
	}


	@Override
	protected String getKeyChar() {
		return tt9 == null ? "" : Characters.getChar(tt9.getLanguage(), tt9.getSettings().getPunctKeyChar(getIndex()));
	}


	@Override
	protected boolean shouldHide() {
		return false;
	}
}

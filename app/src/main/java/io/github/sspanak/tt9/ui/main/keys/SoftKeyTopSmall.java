package io.github.sspanak.tt9.ui.main.keys;

import android.content.Context;
import android.util.AttributeSet;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.commands.CmdShowSettings;
import io.github.sspanak.tt9.commands.CmdVoiceInput;

/**
 * BestT9keyboard: small keys in the top bar, next to the suggestions (TouchPal-style).
 * Left: voice input. Right: settings.
 */
public class SoftKeyTopSmall extends SoftKey {
	private final CmdVoiceInput voiceInput = new CmdVoiceInput();
	private final CmdShowSettings settings = new CmdShowSettings();

	public SoftKeyTopSmall(Context context) { super(context); }
	public SoftKeyTopSmall(Context context, AttributeSet attrs) { super(context, attrs); }
	public SoftKeyTopSmall(Context context, AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); }


	private boolean isVoice() {
		return getId() == R.id.soft_key_top_voice;
	}


	@Override
	protected boolean handleRelease() {
		if (isVoice()) {
			return voiceInput.isAvailable(tt9) && voiceInput.run(tt9);
		}
		return settings.run(tt9);
	}


	@Override
	protected String getAccessibilityText() {
		if (tt9 == null) {
			return null;
		}
		return isVoice() ? voiceInput.getName(tt9) : settings.getName(tt9);
	}


	@Override
	public void render() {
		if (isVoice()) {
			setEnabled(voiceInput.isAvailable(tt9));
		}
		super.render();
	}
}

package io.github.sspanak.tt9.preferences;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import io.github.sspanak.tt9.ui.DocumentActivity;

/**
 * BestT9keyboard: "Open-source licenses" screen.
 */
public class LicensesActivity extends DocumentActivity {
	@Override
	protected BufferedReader getDocumentReader() throws IOException {
		return new BufferedReader(new InputStreamReader(getResources().openRawResource(io.github.sspanak.tt9.R.raw.bt9_licenses), StandardCharsets.UTF_8));
	}
}

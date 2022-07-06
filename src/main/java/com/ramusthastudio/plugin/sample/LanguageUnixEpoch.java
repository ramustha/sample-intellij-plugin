package com.ramusthastudio.plugin.sample;

import com.intellij.lang.LanguageExtension;
import com.ramusthastudio.plugin.sample.tokenizer.UnixEpochStrategy;

public class LanguageUnixEpoch extends LanguageExtension<UnixEpochStrategy> {
  public static final LanguageUnixEpoch INSTANCE = new LanguageUnixEpoch();

  private LanguageUnixEpoch() {
    super(UnixEpochStrategy.EP_NAME, new UnixEpochStrategy());
  }
}

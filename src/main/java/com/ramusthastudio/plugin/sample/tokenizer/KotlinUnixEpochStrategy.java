package com.ramusthastudio.plugin.sample.tokenizer;

import com.intellij.psi.PsiElement;
import com.intellij.spellchecker.tokenizer.Tokenizer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.psi.KtFile;

public class KotlinUnixEpochStrategy extends UnixEpochStrategy {

  @Override
  public Tokenizer getTokenizer(PsiElement element) {
    if (element instanceof KtFile) {
      return TIME_MILLIS_TOKENIZER;
    }
    return super.getTokenizer(element);
  }

  @Override
  public boolean isMyContext(@NotNull PsiElement element) {
    return "kotlin".equals(element.getLanguage().getID());
  }
}

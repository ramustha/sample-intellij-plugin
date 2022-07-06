package com.ramusthastudio.plugin.sample.tokenizer;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiJavaFile;
import com.intellij.spellchecker.tokenizer.Tokenizer;
import org.jetbrains.annotations.NotNull;

public class JavaUnixEpochStrategy extends UnixEpochStrategy {

  @Override
  public Tokenizer getTokenizer(PsiElement element) {
    if (element instanceof PsiJavaFile) {
      return TIME_MILLIS_TOKENIZER;
    }
    return super.getTokenizer(element);
  }

  @Override
  public boolean isMyContext(@NotNull PsiElement element) {
    return "JAVA".equals(element.getLanguage().getID());
  }
}

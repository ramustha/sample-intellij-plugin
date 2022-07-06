package com.ramusthastudio.plugin.sample.tokenizer;

import com.intellij.codeInspection.SuppressQuickFix;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

public abstract class SuppressibleUnixEpochStrategy extends UnixEpochStrategy {
  public abstract boolean isSuppressedFor(@NotNull PsiElement element, @NotNull String name);

  public abstract SuppressQuickFix[] getSuppressActions(@NotNull PsiElement element,
      @NotNull String name);
}

package com.ramusthastudio.plugin.sample.tokenizer;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.lang.injection.InjectedLanguageManager;
import com.intellij.openapi.extensions.ExtensionPointName;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiLanguageInjectionHost;
import com.intellij.psi.PsiPlainText;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.spellchecker.inspections.PlainTextSplitter;
import com.intellij.spellchecker.tokenizer.TokenConsumer;
import com.intellij.spellchecker.tokenizer.Tokenizer;
import com.intellij.spellchecker.tokenizer.TokenizerBase;
import com.intellij.util.KeyedLazyInstance;
import com.ramusthastudio.plugin.sample.inspections.TimeMillisSplitter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

public class UnixEpochStrategy {

  public static final ExtensionPointName<KeyedLazyInstance<UnixEpochStrategy>> EP_NAME =
      new ExtensionPointName<>("com.ramusthastudio.plugin.sample.unixepoch.support");
  public static final Tokenizer<?> EMPTY_TOKENIZER = new Tokenizer<>() {
    @Override
    public void tokenize(@NotNull PsiElement element, TokenConsumer consumer) {
      // do nothing
    }

    @Override
    public String toString() {
      return "EMPTY_TOKENIZER";
    }
  };

  public static final Tokenizer<PsiElement> TIME_MILLIS_TOKENIZER =
      new TokenizerBase<>(TimeMillisSplitter.getInstance());

  private static final LocalQuickFix[] BATCH_FIXES = LocalQuickFix.EMPTY_ARRAY;

  @NotNull
  public Tokenizer getTokenizer(PsiElement element) {
    if (element instanceof XmlAttributeValue) {
      return TIME_MILLIS_TOKENIZER;
    }
    if (element instanceof PsiPlainText) {
      return TIME_MILLIS_TOKENIZER;
    }
    return EMPTY_TOKENIZER;
  }

  public LocalQuickFix[] getRegularFixes(PsiElement element,
      @NotNull TextRange textRange,
      boolean useRename,
      String typo) {
    return getDefaultRegularFixes(useRename, typo, element, textRange);
  }

  public static LocalQuickFix[] getDefaultRegularFixes(boolean useRename,
      String typo,
      @Nullable PsiElement element,
      @NotNull TextRange range) {
    ArrayList<LocalQuickFix> result = new ArrayList<>();

//    if (useRename) {
//      result.add(new RenameTo(typo));
//    } else if (element != null) {
//      result.addAll(new ChangeTo(typo, element, range).getAllAsFixes());
//    }
//
//    if (element == null) {
//      result.add(new SaveTo(typo));
//      return result.toArray(LocalQuickFix.EMPTY_ARRAY);
//    }
//
//    final SpellCheckerSettings settings = SpellCheckerSettings.getInstance(element.getProject());
//    if (settings.isUseSingleDictionaryToSave()) {
//      result.add(new SaveTo(typo, SpellCheckerManager.DictionaryLevel.getLevelByName(settings.getDictionaryToSave())));
//      return result.toArray(LocalQuickFix.EMPTY_ARRAY);
//    }
//
//    result.add(new SaveTo(typo));
    return result.toArray(LocalQuickFix.EMPTY_ARRAY);
  }

  public static LocalQuickFix[] getDefaultBatchFixes() {
    return BATCH_FIXES;
  }

  protected static class XmlAttributeValueTokenizer extends Tokenizer<XmlAttributeValue> {
    @Override
    public void tokenize(@NotNull final XmlAttributeValue element, final TokenConsumer consumer) {
      if (element instanceof PsiLanguageInjectionHost
          && InjectedLanguageManager.getInstance(element.getProject()).getInjectedPsiFiles(element) != null)
        return;

      final String valueTextTrimmed = element.getValue().trim();
      // do not inspect colors like #00aaFF
      if (valueTextTrimmed.startsWith("#") && valueTextTrimmed.length() <= 9 && isHexString(
          valueTextTrimmed.substring(1))) {
        return;
      }

      consumer.consumeToken(element, PlainTextSplitter.getInstance());
    }

    private static boolean isHexString(final String s) {
      for (int i = 0; i < s.length(); i++) {
        if (!StringUtil.isHexDigit(s.charAt(i))) {
          return false;
        }
      }
      return true;
    }
  }

  public boolean isMyContext(@NotNull PsiElement element) {
    return "TEXT".equals(element.getLanguage().getID());
  }
}

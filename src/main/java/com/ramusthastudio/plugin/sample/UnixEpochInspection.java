package com.ramusthastudio.plugin.sample;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.LocalInspectionToolSession;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemDescriptorBase;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.codeInspection.SuppressQuickFix;
import com.intellij.lang.ASTNode;
import com.intellij.lang.Language;
import com.intellij.lang.LanguageNamesValidation;
import com.intellij.lang.injection.InjectedLanguageManager;
import com.intellij.lang.refactoring.NamesValidator;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiFile;
import com.intellij.spellchecker.inspections.Splitter;
import com.intellij.spellchecker.tokenizer.TokenConsumer;
import com.intellij.spellchecker.tokenizer.Tokenizer;
import com.intellij.util.Consumer;
import com.intellij.util.containers.CollectionFactory;
import com.ramusthastudio.plugin.sample.settings.AppSettingsState;
import com.ramusthastudio.plugin.sample.tokenizer.SuppressibleUnixEpochStrategy;
import com.ramusthastudio.plugin.sample.tokenizer.UnixEpochStrategy;
import org.apache.commons.lang.math.NumberUtils;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.time.Instant;
import java.util.Set;

import static com.ramusthastudio.plugin.sample.inspections.TimeMillisSplitter.SECONDS_LENGTH;

public class UnixEpochInspection extends LocalInspectionTool {
  private static final Logger LOG = Logger.getInstance(UnixEpochInspection.class);
  public static final String UNIX_EPOCH_SHORT_NAME = "UnixEpochInspection";
  public static final String UNIX_EPOCH_DISPLAY_NAME = "Unix epoch preview";

  @Override
  public SuppressQuickFix @NotNull [] getBatchSuppressActions(@Nullable PsiElement element) {
    if (element != null) {
      final Language language = element.getLanguage();
      UnixEpochStrategy strategy = getUnixEpochStrategy(element, language);
      if (strategy instanceof SuppressibleUnixEpochStrategy) {
        return ((SuppressibleUnixEpochStrategy) strategy).getSuppressActions(element,
            getShortName());
      }
    }
    return super.getBatchSuppressActions(element);
  }

  private static UnixEpochStrategy getUnixEpochStrategy(@NotNull PsiElement element,
      @NotNull Language language) {
    for (UnixEpochStrategy strategy : LanguageUnixEpoch.INSTANCE.allForLanguage(language)) {
      if (strategy.isMyContext(element)) {
        return strategy;
      }
    }
    return null;
  }

  @Override
  public boolean isSuppressedFor(@NotNull PsiElement element) {
    final Language language = element.getLanguage();
    UnixEpochStrategy strategy = getUnixEpochStrategy(element, language);
    if (strategy instanceof SuppressibleUnixEpochStrategy) {
      return ((SuppressibleUnixEpochStrategy) strategy).isSuppressedFor(element, getShortName());
    }
    return super.isSuppressedFor(element);
  }

  @Override
  public @NonNls @NotNull String getShortName() {
    return UNIX_EPOCH_SHORT_NAME;
  }

  @Override
  public @Nls(capitalization = Nls.Capitalization.Sentence)
  @NotNull String getGroupDisplayName() {
    return UNIX_EPOCH_DISPLAY_NAME;
  }

  @Override
  public boolean showDefaultConfigurationOptions() {
    return false;
  }

  @Override
  public JComponent createOptionsPanel() {
    final Box verticalBox = Box.createVerticalBox();
    final JPanel panel = new JPanel(new BorderLayout());
    panel.add(verticalBox, BorderLayout.NORTH);
    return panel;
  }

  @Override
  public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder,
      boolean isOnTheFly,
      @NotNull LocalInspectionToolSession session) {
    return new PsiElementVisitor() {
      @Override
      public void visitElement(@NotNull PsiElement element) {
        if (holder.getResultCount() > 1000)
          return;

        final ASTNode node = element.getNode();
        if (node == null) {
          return;
        }

        PsiFile containingFile = element.getContainingFile();
        if (containingFile != null && Boolean.TRUE.equals(containingFile.getUserData(
            InjectedLanguageManager.FRANKENSTEIN_INJECTION))) {
          return;
        }

        LOG.debug("element = " + element.getClass() + " " + element.getLanguage());

        final Language language = element.getLanguage();
        tokenize(element,
            language,
            new MyTokenConsumer(holder, LanguageNamesValidation.INSTANCE.forLanguage(language)));
      }
    };
  }

  public static void tokenize(@NotNull final PsiElement element,
      @NotNull final Language language,
      TokenConsumer consumer) {
    final UnixEpochStrategy factoryByLanguage = getUnixEpochStrategy(element, language);
    if (factoryByLanguage == null)
      return;
    Tokenizer tokenizer = factoryByLanguage.getTokenizer(element);
    tokenizer.tokenize(element, consumer);
  }

  private static ProblemDescriptor createProblemDescriptor(PsiElement element,
      TextRange textRange,
      LocalQuickFix[] fixes,
      boolean onTheFly,
      String dateWord) {
    return new ProblemDescriptorBase(element,
        element,
        dateWord,
        null,
        ProblemHighlightType.WEAK_WARNING,
        false,
        textRange,
        onTheFly,
        onTheFly);
  }

  public static Instant createInstantFormat(String value) {
    if (value.length() == SECONDS_LENGTH) {
      return Instant.ofEpochSecond(Long.parseLong(value));
    }
    return Instant.ofEpochMilli(Long.parseLong(value));
  }

  private static final class MyTokenConsumer extends TokenConsumer implements Consumer<TextRange> {
    private final Set<String> myAlreadyChecked = CollectionFactory.createSmallMemoryFootprintSet();
    private final AppSettingsState appSettingsState = AppSettingsState.getInstance();
    private final ProblemsHolder myHolder;
    private final NamesValidator myNamesValidator;
    private PsiElement myElement;
    private String myText;
    private boolean myUseRename;
    private int myOffset;

    MyTokenConsumer(ProblemsHolder holder, NamesValidator namesValidator) {
      myHolder = holder;
      myNamesValidator = namesValidator;
    }

    @Override
    public void consumeToken(final PsiElement element,
        final String text,
        final boolean useRename,
        final int offset,
        TextRange rangeToCheck,
        Splitter splitter) {
      myElement = element;
      myText = text;
      myUseRename = useRename;
      myOffset = offset;
      splitter.split(text, rangeToCheck, this);
    }

    @Override
    public void consume(TextRange range) {
      String word = range.substring(myText);

      if (!NumberUtils.isDigits(word)) {
        return;
      }

      if (!myHolder.isOnTheFly() && myAlreadyChecked.contains(word)) {
        return;
      }

      boolean keyword = myNamesValidator.isKeyword(word, myElement.getProject());
      if (keyword) {
        return;
      }

      LOG.debug(myElement.getLanguage() + " range " + range + " word = " + word + " "
          + myElement.getClass());

      UnixEpochStrategy strategy = getUnixEpochStrategy(myElement, myElement.getLanguage());
      final Tokenizer tokenizer = strategy != null ? strategy.getTokenizer(myElement) : null;
      if (tokenizer != null) {
        range = tokenizer.getHighlightingRange(myElement, myOffset, range);
      }

      Instant instant = createInstantFormat(word);
      String localFormat = String.format("%s = %s",
          word,
          appSettingsState.getDefaultLocalFormatter().format(instant));
      if (myHolder.isOnTheFly()) {
        addRegularDescriptor(myElement, range, myHolder, myUseRename, localFormat);
      } else {
        myAlreadyChecked.add(word);
        addBatchDescriptor(myElement, range, myHolder, localFormat);
      }
    }

    private static void addBatchDescriptor(PsiElement element,
        @NotNull TextRange textRange,
        @NotNull ProblemsHolder holder,
        String word) {
      LocalQuickFix[] fixes = UnixEpochStrategy.getDefaultBatchFixes();
      ProblemDescriptor problemDescriptor =
          createProblemDescriptor(element, textRange, fixes, false, word);
      holder.registerProblem(problemDescriptor);
    }

    private static void addRegularDescriptor(PsiElement element,
        @NotNull TextRange textRange,
        @NotNull ProblemsHolder holder,
        boolean useRename,
        String word) {
      LocalQuickFix[] fixes =
          UnixEpochStrategy.getDefaultRegularFixes(useRename, word, element, textRange);
      final ProblemDescriptor problemDescriptor =
          createProblemDescriptor(element, textRange, fixes, true, word);
      holder.registerProblem(problemDescriptor);
    }
  }
}

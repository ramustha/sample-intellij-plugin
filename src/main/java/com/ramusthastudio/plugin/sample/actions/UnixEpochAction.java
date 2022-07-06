package com.ramusthastudio.plugin.sample.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.CaretModel;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.ui.Messages;
import com.ramusthastudio.plugin.sample.settings.AppSettingsState;
import org.apache.commons.lang.math.NumberUtils;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;

import static com.ramusthastudio.plugin.sample.UnixEpochInspection.createInstantFormat;

public class UnixEpochAction extends AnAction {
  private final AppSettingsState appSettingsState = AppSettingsState.getInstance();

  @Override
  public void actionPerformed(@NotNull AnActionEvent e) {
    final Editor editor = e.getRequiredData(CommonDataKeys.EDITOR);
    final CaretModel caretModel = editor.getCaretModel();
    final Caret primaryCaret = caretModel.getPrimaryCaret();
    String selectedText = primaryCaret.getSelectedText();

    if (NumberUtils.isDigits(selectedText)) {
      Instant instant = createInstantFormat(selectedText);
      String localFormat = String.format("%s",
          appSettingsState.getDefaultLocalFormatter().format(instant));
      String utcFormat = String.format("%s [UTC]",
          appSettingsState.getDefaultUtcFormatter().format(instant));
      String message = String.format("%s%n%s", utcFormat, localFormat);

      Messages.showInfoMessage(e.getProject(), message, "Unix Timestamp");
    }
  }
}

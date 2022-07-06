package com.ramusthastudio.plugin.sample;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.CaretModel;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.ui.Messages;
import org.apache.commons.lang.math.NumberUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.time.Instant;

import static com.ramusthastudio.plugin.sample.UnixEpochInspection.LOCAL_FORMATTER;
import static com.ramusthastudio.plugin.sample.UnixEpochInspection.UTC_FORMATTER;
import static com.ramusthastudio.plugin.sample.UnixEpochInspection.createInstantFormat;

public class UnixEpochAction extends AnAction {

  @Override
  public void actionPerformed(@NotNull AnActionEvent e) {
    final Editor editor = e.getRequiredData(CommonDataKeys.EDITOR);
    final CaretModel caretModel = editor.getCaretModel();
    final Caret primaryCaret = caretModel.getPrimaryCaret();
    String selectedText = primaryCaret.getSelectedText();

    if (NumberUtils.isDigits(selectedText)) {
      Instant instant = createInstantFormat(selectedText);
      String localFormat = String.format("%s", LOCAL_FORMATTER.format(instant));
      String utcFormat = String.format("%s [UTC]", UTC_FORMATTER.format(instant));
      String message = String.format("%s%n%s", utcFormat, localFormat);

      Messages.showInfoMessage(
          e.getProject(), message, String.format("Unix Timestamp [%s]", selectedText));
    }
  }
}

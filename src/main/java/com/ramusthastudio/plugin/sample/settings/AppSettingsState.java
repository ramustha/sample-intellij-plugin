package com.ramusthastudio.plugin.sample.settings;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@State(name = "com.ramusthastudio.plugin.sample.settings.UnixEpochSettingsState", storages = @Storage("UnixEpochSettingsPlugin.xml"))
public class AppSettingsState implements PersistentStateComponent<AppSettingsState> {

  private DateFormatSettings dateFormatSettings = DateFormatSettings.RFC_1123_DATE_TIME;
  private boolean isCustomPatternEnable;
  private String customPattern = null;

  public static AppSettingsState getInstance() {
    return ApplicationManager.getApplication().getService(AppSettingsState.class);
  }

  @Nullable
  @Override
  public AppSettingsState getState() {
    return this;
  }

  @Override
  public void loadState(@NotNull AppSettingsState state) {
    XmlSerializerUtil.copyBean(state, this);
  }

  public DateTimeFormatter getDefaultLocalFormatter() {
    try {
      if (isCustomPatternEnable) {
        return DateTimeFormatter.ofPattern(customPattern).withZone(ZoneId.systemDefault());
      }
      return dateFormatSettings.getValue().withZone(ZoneId.systemDefault());
    } catch (Exception e) {
      return dateFormatSettings.getValue().withZone(ZoneId.systemDefault());
    }
  }

  public DateTimeFormatter getDefaultUtcFormatter() {
    try {
      if (isCustomPatternEnable) {
        return DateTimeFormatter.ofPattern(customPattern).withZone(ZoneId.of("UTC"));
      }
      return dateFormatSettings.getValue().withZone(ZoneId.of("UTC"));
    } catch (Exception e) {
      return dateFormatSettings.getValue().withZone(ZoneId.of("UTC"));
    }
  }

  public DateFormatSettings getDateFormatSettings() {
    return dateFormatSettings;
  }

  public void setDateFormatSettings(DateFormatSettings dateFormatSettings) {
    this.dateFormatSettings = dateFormatSettings;
  }

  public String getCustomPattern() {
    return customPattern;
  }

  public void setCustomPattern(String customPattern) {
    this.customPattern = customPattern;
  }

  public void setCustomPatternEnable(boolean customPatternEnable) {
    isCustomPatternEnable = customPatternEnable;
  }

  public boolean isCustomPatternEnable() {
    return isCustomPatternEnable;
  }
}

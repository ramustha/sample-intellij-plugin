// Copyright 2000-2022 JetBrains s.r.o. and other contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.ramusthastudio.plugin.sample.settings;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class AppSettingsConfigurable implements Configurable {

  private AppSettingsComponent mySettingsComponent;

  @Nls(capitalization = Nls.Capitalization.Title)
  @Override
  public String getDisplayName() {
    return "Unix Epoch Time Visualize";
  }

  @Override
  public JComponent getPreferredFocusedComponent() {
    return mySettingsComponent.getPreferredFocusedComponent();
  }

  @Nullable
  @Override
  public JComponent createComponent() {
    mySettingsComponent = new AppSettingsComponent();
    return mySettingsComponent.getPanel();
  }

  @Override
  public boolean isModified() {
    AppSettingsState settings = AppSettingsState.getInstance();
    if (mySettingsComponent.isCustomPatternEnable()) {
      return mySettingsComponent.isAlreadyPreview() && !mySettingsComponent.getCustomPattern()
          .equals(settings.getCustomPattern());
    }
    return !mySettingsComponent.getSelectedItem().getValue()
        .equals(settings.getDefaultLocalFormatter());
  }

  @Override
  public void apply() throws ConfigurationException {
    if (mySettingsComponent.isInvalid()) {
      throw new ConfigurationException("Invalid format!");
    }

    AppSettingsState settings = AppSettingsState.getInstance();
    if (mySettingsComponent.isCustomPatternEnable()) {
      String patternText = mySettingsComponent.getCustomPattern();
      settings.setCustomPatternEnable(true);
      settings.setCustomPattern(patternText);
    } else {
      DateFormatSettings selected = mySettingsComponent.getSelectedItem();
      settings.setCustomPatternEnable(false);
      settings.setDateFormatSettings(selected);
    }
  }

  @Override
  public void reset() {
    AppSettingsState settings = AppSettingsState.getInstance();
    if (settings.isCustomPatternEnable()) {
      String patternText = settings.getCustomPattern();
      mySettingsComponent.setCustomPatternEnable(true);
      mySettingsComponent.setCustomPattern(patternText);
      mySettingsComponent.enableDefaultFormat(false);
    } else {
      DateFormatSettings savedState = settings.getDateFormatSettings();
      mySettingsComponent.setCustomPatternEnable(false);
      mySettingsComponent.setSelectedItem(savedState);
      mySettingsComponent.enableDefaultFormat(true);
    }
  }

  @Override
  public void disposeUIResources() {
    mySettingsComponent = null;
  }

}

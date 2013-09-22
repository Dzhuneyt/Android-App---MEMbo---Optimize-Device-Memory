package com.hasmobi.rambo.adapters.placeholders;

public class SettingsSingle {

	// A friendly name of the setting (localized)
	public String settingName;
	// The key under which we save it in SharedPreferences
	public String settingKey;

	public SettingsSingle(String settingKey, String settingName) {
		this.settingKey = settingKey;
		this.settingName = settingName;
	}

	public SettingsSingle() {
	}

	public String getSettingName() {
		return settingName;
	}

	public void setSettingName(String settingName) {
		this.settingName = settingName;
	}

	public String getSettingKey() {
		return settingKey;
	}

	public void setSettingKey(String settingKey) {
		this.settingKey = settingKey;
	}

}

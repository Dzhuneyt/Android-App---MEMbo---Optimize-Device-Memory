package com.hasmobi.rambo.supers;

/**
 * Attach an instance of this listener class to a given View in the
 * SettingsAdapter and the listener's changed() method will be called when there
 * is a change in the setting it's attached to
 * 
 */
public interface SettingChangeObserver {
	abstract void changed();
}

package com.hasmobi.rambo.utils;

import android.content.Context;
import android.util.Log;
import android.view.View;

abstract public class SingleBatteryTip {

    public SingleBatteryTip(String title, String description, String sp_name){
        this.title = title;
        this.description = description;
        this.sp_name = sp_name;
    }

    static final public int TYPE_TIME = 1;

    public String title = null;
    public String description = null;
    public String sp_name = null;
    public int tip_type = 0;

    public int maxValueInt = 100, defaultValueInt = 0;
    public String defaultValueString = null;

    public View control = null;

    abstract public void inflate(Context c, View parent);

    // @TODO make this abstract
    public View inflate(Context c){
        Log.e(getClass().toString(), "inflate method not implemented");
        return null;
    }


}

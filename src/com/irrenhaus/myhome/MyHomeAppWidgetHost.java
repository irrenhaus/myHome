package com.irrenhaus.myhome;
import android.appwidget.AppWidgetHost;
import android.appwidget.AppWidgetHostView;
import android.appwidget.AppWidgetProviderInfo;
import android.content.Context;



public class MyHomeAppWidgetHost extends AppWidgetHost {

	public MyHomeAppWidgetHost(Context context, int hostId) {
		super(context, hostId);
	}

	@Override
    protected AppWidgetHostView onCreateView(Context context, int appWidgetId,
            AppWidgetProviderInfo appWidget) {
        return new MyHomeAppWidgetHostView(context);
    }

}

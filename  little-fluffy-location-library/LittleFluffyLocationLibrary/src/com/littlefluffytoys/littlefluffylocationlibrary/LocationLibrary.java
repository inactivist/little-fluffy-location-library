/*
 * Copyright 2012 Little Fluffy Toys Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.littlefluffytoys.littlefluffylocationlibrary;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.location.LocationManager;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * A simple-to-use library that broadcasts location updates to your app without killing your battery.
 * 
 * Project home and documentation: {@link code.google.com/p/little-fluffy-location-library}
 * 
 * @author Kenton Price, Little Fluffy Toys Ltd - {@link www.littlefluffytoys.com}
 */
public class LocationLibrary {
    
    protected static boolean showDebugOutput = false;
    protected static boolean broadcastEveryLocationUpdate = false;
    
    private static final String TAG = "LocationLibrary";
    
    private static boolean initialised = false;
    
    private static long alarmFrequency = LocationLibraryConstants.DEFAULT_ALARM_FREQUENCY;
    private static int locationMaximumAge = (int) LocationLibraryConstants.DEFAULT_MAXIMUM_LOCATION_AGE;
    
    protected static int getLocationMaximumAge() {
        return locationMaximumAge;
    }
    
    protected static long getAlarmFrequency() {
        return alarmFrequency;
    }
    
    protected static void startAlarmAndListener(final Context context) {
        if (showDebugOutput) Log.d(LocationLibraryConstants.TAG, TAG + ": startAlarmAndListener: alarmFrequency=" + (alarmFrequency == LocationLibraryConstants.DEFAULT_ALARM_FREQUENCY ? "default:" : "") + alarmFrequency/1000 + " secs, locationMaximumAge=" + (locationMaximumAge == LocationLibraryConstants.DEFAULT_MAXIMUM_LOCATION_AGE ? "default:" : "") + locationMaximumAge/1000 + " secs");
        
        final PendingIntent alarmIntent = PendingIntent.getService(context, LocationLibraryConstants.LOCATION_BROADCAST_REQUEST_CODE_REPEATING_ALARM, new Intent(context, LocationBroadcastService.class), PendingIntent.FLAG_UPDATE_CURRENT);

        final AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        
        // cancel any existing alarm
        am.cancel(alarmIntent);

        // Schedule the alarm
        am.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime(), getAlarmFrequency(), alarmIntent);

        if (LocationLibraryConstants.SUPPORTS_FROYO) {
            final LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            final Intent passiveIntent = new Intent(context, PassiveLocationChangedReceiver.class);
            final PendingIntent locationListenerPassivePendingIntent = PendingIntent.getBroadcast(context, 0, passiveIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            locationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, 0, 0, locationListenerPassivePendingIntent);
        }

        if (showDebugOutput) Log.d(LocationLibraryConstants.TAG, TAG + ": startAlarmAndListener completed");
    }
    
    /**
     * To use this library, call initialiseLibrary from your Application's onCreate method,
     * having set up your manifest as detailed in the project documentation.
     * 
     * This constructor uses defaults specified in LocationLibraryConstants for alarmFrequency ({@link android.app.AlarmManager#INTERVAL_FIFTEEN_MINUTES AlarmManager.INTERVAL_FIFTEEN_MINUTES})
     * and locationMaximumAge ({@link android.app.AlarmManager#INTERVAL_HOUR AlarmManager.INTERVAL_HOUR}), and broadcastEveryLocationUpdate by default is false.
     */
    public static void initialiseLibrary(final Context context) {
        if (!initialised) {
            if (showDebugOutput) Log.d(LocationLibraryConstants.TAG, TAG + ": initialiseLibrary");
            final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            if (!prefs.getBoolean(LocationLibraryConstants.SP_KEY_RUN_ONCE, Boolean.FALSE)) {
                if (showDebugOutput) Log.d(LocationLibraryConstants.TAG, TAG + ": initialiseLibrary: first time ever run -> start alarm and listener");
                startAlarmAndListener(context);
                prefs.edit().putBoolean(LocationLibraryConstants.SP_KEY_RUN_ONCE, Boolean.TRUE).commit();
            }
            initialised = true;
        }
    }
    
    /**
     * To use this library, call initialiseLibrary from your Application's onCreate method,
     * having set up your manifest as detailed in the project documentation.
     *
     * @param alarmFrequency How often to broadcast a location update in milliseconds, if one was received.
     * 
     * For battery efficiency, this should be one of the available inexact recurrence intervals
     * recognised by {@link android.app.AlarmManager#setInexactRepeating(int, long, long , PendingIntent) AlarmManager.setInexactRepeating}.
     * You are not prevented from using any other value, but in that case Android's alarm manager uses setRepeating instead of setInexactRepeating,
     * and this results in poorer battery life. The default is {@link android.app.AlarmManager#INTERVAL_FIFTEEN_MINUTES AlarmManager.INTERVAL_FIFTEEN_MINUTES}.
     * 
     * @param locationMaximumAge The maximum age of a location update. If when the alarm fires the location is
     * older than this, a location update will be requested. The default is {@link android.app.AlarmManager#INTERVAL_HOUR AlarmManager.INTERVAL_HOUR}
     * 
     * @see #initialiseLibrary(Context)
     */
    public static void initialiseLibrary(final Context context, final long alarmFrequency, final int locationMaximumAge) {
        if (!initialised) {
            LocationLibrary.alarmFrequency = alarmFrequency;
            LocationLibrary.locationMaximumAge = locationMaximumAge;
            initialiseLibrary(context);
         }
    }
    
    /**
     * To use this library, call initialiseLibrary from your Application's onCreate method,
     * having set up your manifest as detailed in the project documentation.
     *
     * This constructor uses defaults specified in LocationLibraryConstants for alarmFrequency ({@link android.app.AlarmManager#INTERVAL_FIFTEEN_MINUTES AlarmManager.INTERVAL_FIFTEEN_MINUTES})
     * and locationMaximumAge ({@link android.app.AlarmManager#INTERVAL_HOUR AlarmManager.INTERVAL_HOUR}).
     * 
     * @param broadcastEveryLocationUpdate If true, broadcasts every location update using intent action 
     * LocationLibraryConstants.LOCATION_CHANGED_TICKER_BROADCAST_ACTION
     * 
     * @see #initialiseLibrary(Context)
     */
    public static void initialiseLibrary(final Context context, final boolean broadcastEveryLocationUpdate) {
        if (!initialised) {
            LocationLibrary.broadcastEveryLocationUpdate = broadcastEveryLocationUpdate;
            initialiseLibrary(context);
         }
    }
    
    /**
     * To use this library, call initialiseLibrary from your Application's onCreate method,
     * having set up your manifest as detailed in the project documentation.
     * 
     * @param alarmFrequency How often to broadcast a location update in milliseconds, if one was received.
     * 
     * For battery efficiency, this should be one of the available inexact recurrence intervals
     * recognised by {@link android.app.AlarmManager#setInexactRepeating(int, long, long , PendingIntent) AlarmManager.setInexactRepeating}.
     * You are not prevented from using any other value, but in that case Android's alarm manager uses setRepeating instead of setInexactRepeating,
     * and this results in poorer battery life. The default is {@link android.app.AlarmManager#INTERVAL_FIFTEEN_MINUTES AlarmManager.INTERVAL_FIFTEEN_MINUTES}.
     * 
     * @param locationMaximumAge The maximum age of a location update. If when the alarm fires the location is
     * older than this, a location update will be requested. The default is {@link android.app.AlarmManager#INTERVAL_HOUR AlarmManager.INTERVAL_HOUR}
     * 
     * @param broadcastEveryLocationUpdate If true, broadcasts every location update using intent action 
     * LocationLibraryConstants.LOCATION_CHANGED_TICKER_BROADCAST_ACTION
     * 
     * @see #initialiseLibrary(Context, long, int)
     * @see #initialiseLibrary(Context, boolean)
     * @see #initialiseLibrary(Context)
     */
    public static void initialiseLibrary(final Context context, final long alarmFrequency, final int locationMaximumAge, final boolean broadcastEveryLocationUpdate) {
        if (!initialised) {
            LocationLibrary.broadcastEveryLocationUpdate = broadcastEveryLocationUpdate;
            initialiseLibrary(context, alarmFrequency, locationMaximumAge);
        }    
    }
    
    /**
     * To force an on-demand location update, call this method.
     */
    public static void forceLocationUpdate(final Context context) {
        final Editor prefsEditor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        prefsEditor.putLong(LocationLibraryConstants.SP_KEY_LAST_LOCATION_UPDATE_TIME, 0);
        prefsEditor.putLong(LocationLibraryConstants.SP_KEY_LAST_LOCATION_BROADCAST_TIME, 0);
        prefsEditor.commit();
        context.startService(new Intent(context, LocationBroadcastService.class));
    }
  
    /**
     * Debug output is off by default. To switch it on, call showDebugOutput(true)
     * from your Application's onCreate method.
     */
    public static void showDebugOutput(final boolean showDebugOutput) {
        LocationLibrary.showDebugOutput = showDebugOutput;
    }    
}

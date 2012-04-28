/*
 * Copyright 2012 Little Fluffy Toys Ltd
 * Adapted from work by Reto Meier, Copyright 2011 Google Inc.
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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.location.Location;
import android.location.LocationManager;
import android.preference.PreferenceManager;
import android.text.format.DateFormat;
import android.util.Log;

/**
 * This Receiver class is used to listen for Broadcast Intents that announce
 * that a location change has occurred while this application isn't visible.
 * 
 * Where possible, this is triggered by a Passive Location listener.
 */
public class PassiveLocationChangedReceiver extends BroadcastReceiver {
  
  protected static String TAG = "PassiveLocationChangedReceiver";
  
  /**
   * When a new location is received, extract it from the Intent and use
   * it to start the Service used to update the list of nearby places.
   * 
   * This is the Passive receiver, used to receive Location updates from 
   * third party apps when the Activity is not visible. 
   */
  @Override
  public void onReceive(Context context, Intent intent) {
    String key = LocationManager.KEY_LOCATION_CHANGED;
    
    if (intent.hasCategory(LocationLibraryConstants.INTENT_CATEGORY_ONE_SHOT_UPDATE)) {
        // it's a one-shot update from Gingerbread and higher
        if (LocationLibrary.showDebugOutput) Log.d(LocationLibraryConstants.TAG, TAG + ":onReceive: on-demand location update received");

        // we know that the passive provider will shortly get an update from this.
        // Sometimes it will get two or three updates in quick succession.
        // So, let this onReceive execute, and update itself.
        // And then force a service call in 30 seconds. Simples!
        LocationBroadcastService.forceDelayedServiceCall(context, 30);
    }
    else if (intent.hasExtra(key)) {
        // This update came from Passive provider, so we can extract the location directly.
        processLocation(context, (Location)intent.getExtras().get(key));
    }
    else {
        if (LocationLibrary.showDebugOutput) Log.w(LocationLibraryConstants.TAG, TAG + ":onReceive: Unknown update received");
    }
  }
  
  protected static void processLocation(final Context context, final Location location) {
      final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
      final float lastLat = prefs.getFloat(LocationLibraryConstants.SP_KEY_LAST_LOCATION_UPDATE_LAT, Long.MIN_VALUE);
      final float lastLong = prefs.getFloat(LocationLibraryConstants.SP_KEY_LAST_LOCATION_UPDATE_LNG, Long.MIN_VALUE);
      final int lastAccuracy = prefs.getInt(LocationLibraryConstants.SP_KEY_LAST_LOCATION_UPDATE_ACCURACY, Integer.MAX_VALUE);
      
      boolean usePreviousReading = false;
      
      final float thisLat = ((int) (location.getLatitude() * 1000000)) / 1000000f;
      final float thisLong =  ((int) (location.getLongitude() * 1000000)) / 1000000f;
      final int thisAccuracy = (int) location.getAccuracy();
      final long thisTime = location.getTime();
      
      if (lastLat != Long.MIN_VALUE) {
          // The tricky maths bit to calculate the distance between two points:
          // dist = arccos(sin(lat1) · sin(lat2) + cos(lat1) · cos(lat2) · cos(lon1 - lon2)) · R
          int distanceBetweenInMetres = (int) (Math.acos(Math.sin(Math.toRadians(thisLat)) * Math.sin(Math.toRadians(lastLat)) + Math.cos(Math.toRadians(thisLat)) * Math.cos(Math.toRadians(lastLat)) * Math.cos(Math.toRadians(thisLong) - Math.toRadians(lastLong))) * 6371 * 1000);
          if (LocationLibrary.showDebugOutput) Log.d(LocationLibraryConstants.TAG, TAG + ": Distance from last reading: " + distanceBetweenInMetres + "m");
          
          if (location.hasAccuracy() && (thisAccuracy > lastAccuracy)) {
              // this reading is less accurate than the previous one - 
              // see if it's covering the same spot where we were before
              if (distanceBetweenInMetres < thisAccuracy)
              {
                  usePreviousReading = true;
              }
          }
      }
      
      final Editor prefsEditor = prefs.edit();
      
      prefsEditor.putLong(LocationLibraryConstants.SP_KEY_LAST_LOCATION_UPDATE_TIME, thisTime);
      if (!usePreviousReading) {
          prefsEditor.putFloat(LocationLibraryConstants.SP_KEY_LAST_LOCATION_UPDATE_LAT, thisLat);
          prefsEditor.putFloat(LocationLibraryConstants.SP_KEY_LAST_LOCATION_UPDATE_LNG, thisLong);
          prefsEditor.putInt(LocationLibraryConstants.SP_KEY_LAST_LOCATION_UPDATE_ACCURACY, thisAccuracy);
          if (LocationLibrary.showDebugOutput) Log.d(LocationLibraryConstants.TAG, TAG + ": Storing location update, lat=" + thisLat + " long=" + thisLong + " accuracy=" + thisAccuracy + " time=" + thisTime + "(" + DateFormat.format("kk:mm.ss, E", thisTime) + ")");
      }
      else {
          if (LocationLibrary.showDebugOutput) Log.d(LocationLibraryConstants.TAG, TAG + ": Storing location update, less accurate so reusing prior location - time=" + thisTime);
      }
      prefsEditor.commit();

      if (LocationLibrary.broadcastEveryLocationUpdate) {
          // broadcast it
          LocationBroadcastService.sendBroadcast(context, prefs, false);
      }
  }
}
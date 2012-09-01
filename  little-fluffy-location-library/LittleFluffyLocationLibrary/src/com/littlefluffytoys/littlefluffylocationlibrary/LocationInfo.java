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

import java.io.Serializable;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.format.DateFormat;

/**
 * An object containing a snapshot of the best we currently know about location.
 * 
 * Project home and documentation: {@link code.google.com/p/little-fluffy-location-library}
 * 
 * @author Kenton Price, Little Fluffy Toys Ltd - {@link www.littlefluffytoys.mobi}
 */
public class LocationInfo implements Serializable {
    
    private static final long serialVersionUID = 1L;

    public long lastLocationUpdateTimestamp;
    public long lastLocationBroadcastTimestamp;
    public float lastLat;
    public float lastLong;
    public int lastAccuracy;

    /**
     * The constructor populates the public fields with the latest location info. 
     */
    public LocationInfo(final Context context) {
        refresh(context);
    }
    
    /**
     * Call this method to retrieve the latest location info. 
     */
    public void refresh(final Context context) {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        lastLocationUpdateTimestamp = prefs.getLong(LocationLibraryConstants.SP_KEY_LAST_LOCATION_UPDATE_TIME, 0);
        lastLocationBroadcastTimestamp = prefs.getLong(LocationLibraryConstants.SP_KEY_LAST_LOCATION_BROADCAST_TIME, 0);
        lastLat = ((int) (prefs.getFloat(LocationLibraryConstants.SP_KEY_LAST_LOCATION_UPDATE_LAT, Integer.MIN_VALUE) * 1000000f)) / 1000000f;
        lastLong = ((int) (prefs.getFloat(LocationLibraryConstants.SP_KEY_LAST_LOCATION_UPDATE_LNG, Integer.MIN_VALUE) * 1000000f)) / 1000000f;
        lastAccuracy = prefs.getInt(LocationLibraryConstants.SP_KEY_LAST_LOCATION_UPDATE_ACCURACY, Integer.MAX_VALUE);
    }

    /**
     * @return true if any location data has been received since the phone last powered up.
     */
    public boolean anyLocationDataReceived() {
        return (lastLocationUpdateTimestamp != 0);
    }

    /**
     * @return true if any location data has been broadcast since the phone last powered up.
     */
    public boolean anyLocationDataBroadcast() {
        return (lastLocationBroadcastTimestamp != 0);
    }
    
    /**
     * @return true if the latest location has been broadcast by the periodic broadcaster,
     *         false if a periodic broadcast is pending.
     */
    public boolean hasLatestDataBeenBroadcast() {
        return (anyLocationDataBroadcast() && (lastLocationUpdateTimestamp == lastLocationBroadcastTimestamp));
    }

    /**
     * @return timestamp age in seconds
     */
    public long getTimestampAgeInSeconds() {
        return (Math.max((System.currentTimeMillis() - lastLocationUpdateTimestamp) / 1000, 0L));
    }
    
    /**
     * @return time and day as "hh:mm, ddd" or "hh:mm:ss, ddd" 
     */
    public static String formatTimeAndDay(final long timestamp, final boolean includeSeconds) {
        return (DateFormat.format("kk:mm" + (includeSeconds ? ".ss" : "") + ", E", timestamp).toString());
    }
    
    @Override
    public String toString() {
        return (String.format("lastLocationUpdateTimestamp=%1$s lastLocationBroadcastTimestamp=%2$s lastLat=%3$.6f lastLong=%4$.6f lastAccuracy=%5$d",
                lastLocationUpdateTimestamp != 0 ? formatTimeAndDay(lastLocationUpdateTimestamp, true) : "none",
                lastLocationBroadcastTimestamp != 0 ? formatTimeAndDay(lastLocationBroadcastTimestamp, true) : "none",
                lastLat, lastLong, lastAccuracy));
    }
}

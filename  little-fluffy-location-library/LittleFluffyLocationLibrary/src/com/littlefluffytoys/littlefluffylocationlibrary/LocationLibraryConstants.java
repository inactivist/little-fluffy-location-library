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

public class LocationLibraryConstants {
  
  protected static final String TAG = "LittleFluffyLocationLibrary";
  
  public static final long DEFAULT_ALARM_FREQUENCY = AlarmManager.INTERVAL_FIFTEEN_MINUTES;
  public static final int DEFAULT_MAXIMUM_LOCATION_AGE = (int) AlarmManager.INTERVAL_HOUR;
  
  public static final String LOCATION_CHANGED_PERIODIC_BROADCAST_ACTION = "com.littlefluffytoys.littlefluffylocationlibrary.LOCATION_CHANGED";
  public static final String LOCATION_CHANGED_TICKER_BROADCAST_ACTION = "com.littlefluffytoys.littlefluffylocationlibrary.LOCATION_CHANGED_TICK";
  public static final String LOCATION_BROADCAST_EXTRA_LOCATIONINFO = "com.littlefluffytoys.littlefluffylocationlibrary.LocationInfo";

  protected static final int LOCATION_BROADCAST_REQUEST_CODE_SINGLE_SHOT = 1;
  protected static final int LOCATION_BROADCAST_REQUEST_CODE_REPEATING_ALARM = 2;
  protected static final String INTENT_CATEGORY_ONE_SHOT_UPDATE = "INTENT_CATEGORY_ONE_SHOT_UPDATE";
  
  protected static final String SP_KEY_LAST_LOCATION_UPDATE_TIME = "LFT_SP_KEY_LAST_LOCATION_UPDATE_TIME";
  protected static final String SP_KEY_LAST_LOCATION_UPDATE_LAT = "LFT_SP_KEY_LAST_LOCATION_UPDATE_LAT";
  protected static final String SP_KEY_LAST_LOCATION_UPDATE_LNG = "LFT_SP_KEY_LAST_LOCATION_UPDATE_LNG";
  protected static final String SP_KEY_LAST_LOCATION_UPDATE_ACCURACY = "LFT_SP_KEY_LAST_LOCATION_UPDATE_ACCURACY";
  protected static final String SP_KEY_LAST_LOCATION_BROADCAST_TIME = "LFT_SP_KEY_LAST_LOCATION_SUBMIT_TIME";
  protected static final String SP_KEY_RUN_ONCE = "LFT_SP_KEY_RUN_ONCE";
  
  protected static boolean SUPPORTS_GINGERBREAD = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.GINGERBREAD;
}

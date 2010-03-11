/* //device/apps/Settings/src/com/android/settings/Keyguard.java
**
** Copyright 2006, The Android Open Source Project
**
** Licensed under the Apache License, Version 2.0 (the "License"); 
** you may not use this file except in compliance with the License. 
** You may obtain a copy of the License at 
**
**     http://www.apache.org/licenses/LICENSE-2.0 
**
** Unless required by applicable law or agreed to in writing, software 
** distributed under the License is distributed on an "AS IS" BASIS, 
** WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
** See the License for the specific language governing permissions and 
** limitations under the License.
*/

package com.android.spare_parts;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManagerNative;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle; 
import android.os.Environment;
import android.os.ServiceManager;
import android.os.StatFs;
import android.os.SystemProperties;
import android.os.RemoteException;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.preference.Preference.OnPreferenceClickListener;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.text.format.Formatter;
import android.util.Log;
import android.view.IWindowManager;
import android.widget.Toast;

import java.util.List;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Scanner;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.text.DecimalFormat;
import java.text.NumberFormat;


public class SpareParts extends PreferenceActivity
        implements Preference.OnPreferenceChangeListener,
        SharedPreferences.OnSharedPreferenceChangeListener {
    private static final String TAG = "SpareParts";

    private static final String BATTERY_HISTORY_PREF = "battery_history_settings";
    private static final String BATTERY_INFORMATION_PREF = "battery_information_settings";
    private static final String USAGE_STATISTICS_PREF = "usage_statistics_settings";
    
    private static final String WINDOW_ANIMATIONS_PREF = "window_animations";
    private static final String TRANSITION_ANIMATIONS_PREF = "transition_animations";
    private static final String FANCY_IME_ANIMATIONS_PREF = "fancy_ime_animations";
    private static final String HAPTIC_FEEDBACK_PREF = "haptic_feedback";
    private static final String FONT_SIZE_PREF = "font_size";
    private static final String END_BUTTON_PREF = "end_button";
    private static final String MAPS_COMPASS_PREF = "maps_compass";
    private static final String KEY_COMPATIBILITY_MODE = "compatibility_mode";

    // ExtPartitionMounted
    private boolean extfsIsMounted = false;

    // LinuxSWAPExists
    private boolean linuxSWAPExists = false;

    // AutoRotating Launcher
    private static final String AR_LAUNCHER_PREF = "ar_launcher_opt";
    private CheckBoxPreference mARLauncherPref;

    // Phone/Browser Launcher
    private static final String PHONEBROWSER_LAUNCHER_PREF = "phonebrowser_launcher_opt";
    private CheckBoxPreference mPhoneBrowserLauncherPref;

    // Restart Launcher on FAIL
    private static final String RESTART_LAUNCHER2_COMMAND = "Restart_Launcher2_Command";

    // CyTown's Phone MOD
    private static final String CYTOWN_PHONE_PREF = "cyphone_opt";
    private CheckBoxPreference mCyTownPhonePref;

    // Wysie's Contacts MOD
    private static final String WYSIE_CONTACTS_PREF = "wycontacts_opt";
    private CheckBoxPreference mWysieContactsPref;

    // International Keyboard
    private static final String INT_KEYBOARD_PREF = "intkeyboard_opt";
    private CheckBoxPreference mINTKeyboardPref;

    // CPU Options
    private static final String CPU_MIN_FREQ_PREF = "cpu_min_freq_opt";
    private ListPreference mCPUMinFreqPref;

    private static final String CPU_MAX_FREQ_PREF = "cpu_max_freq_opt";
    private ListPreference mCPUMaxFreqPref;

    private static final String CPU_GOVERNOR_PREF = "cpu_governor_opt";
    private ListPreference mCPUGovernorPref;

    // Partition Size Strings
    private static final String SYSTEM_PART_SIZE = "system_storage_levels";
    private Preference mSystemSize;
    private static final String SYSTEM_STORAGE_PATH = "/system";

    private static final String DATA_PART_SIZE = "data_storage_levels";
    private Preference mDataSize;
    private static final String DATA_STORAGE_PATH = "/data";

    private static final String CACHE_PART_SIZE = "cache_storage_levels";
    private Preference mCacheSize;
    private static final String CACHE_STORAGE_PATH = "/cache";

    private static final String SDCARDFAT_PART_SIZE = "sdcardfat_storage_levels";
    private Preference mSDCardFATSize;
    private static final String SDCARDFAT_STORAGE_PATH = "/sdcard";

    private static final String SDCARDEXT_PART_SIZE = "sdcardext_storage_levels";
    private Preference mSDCardEXTSize;
    private static final String SDCARDEXT_STORAGE_PATH = "/system/sd";

    // Apps2SD & Dalvik-Cache options
    private static final String APPS2SD_PREF = "apps2sd_opt";
    private CheckBoxPreference mApps2SDPref;
    private static final String DC_CACHE_PREF = "dccache_opt";
    private CheckBoxPreference mDCCachePref;
    private static final String DC_SDCARD_PREF = "dcsdcard_opt";
    private CheckBoxPreference mDCSDCardPref;

    // Orientation/Rotation Settings
    private static final String ROTATION_270_PREF = "270_rotation_opt";
    private CheckBoxPreference mRotation270Pref;
    private static final String ROTATION_180_PREF = "180_rotation_opt";
    private CheckBoxPreference mRotation180Pref;

    // Memory/SWAP Options
    private static final String COMPCACHE_PREF = "compcache_opt";
    private CheckBoxPreference mCompcachePref;

    private static final String LINUXSWAP_PREF = "linuxswap_opt";
    private CheckBoxPreference mLinuxSWAPPref;

    private static final String SWAPPINESS_PREF = "swappiness_opt";
    private ListPreference mSwappinessPref;

    // Misc Options
    private static final String PID_PRIO_PREF = "pidprio_opt";
    private CheckBoxPreference mPIDPrioPref;

    private static final String USERINIT_PREF = "uinitrun_opt";
    private CheckBoxPreference mUserinitPref;

    private static final String OTA_UPDATE_PREF = "otaupdate_opt";
    private CheckBoxPreference mOTAUpdatePref;

    // Quick Commands
    private static final String REBOOT_NORMAL_COMMAND = "Reboot_Normal_Command";
    private static final String REBOOT_RECOVERY_COMMAND = "Reboot_Recovery_Command";
    private static final String REBOOT_BOOTLOADER_COMMAND = "Reboot_Bootloader_Command";
    private static final String SHUTDOWN_COMMAND = "Shutdown_Command";

    private static final String MOUNT_SYSTEM_RW_COMMAND = "Mount_System_RW_Command";
    private static final String MOUNT_SYSTEM_RO_COMMAND = "Mount_System_RO_Command";

    // Context
    private Context mContext;

    private final Configuration mCurConfig = new Configuration();
    
    private ListPreference mWindowAnimationsPref;
    private ListPreference mTransitionAnimationsPref;
    private CheckBoxPreference mFancyImeAnimationsPref;
    private CheckBoxPreference mHapticFeedbackPref;
    private ListPreference mFontSizePref;
    private ListPreference mEndButtonPref;
    private CheckBoxPreference mShowMapsCompassPref;
    private CheckBoxPreference mCompatibilityMode;

    private IWindowManager mWindowManager;

    public static boolean updatePreferenceToSpecificActivityOrRemove(Context context,
            PreferenceGroup parentPreferenceGroup, String preferenceKey, int flags) {
        
        Preference preference = parentPreferenceGroup.findPreference(preferenceKey);
        if (preference == null) {
            return false;
        }
        
        Intent intent = preference.getIntent();
        if (intent != null) {
            // Find the activity that is in the system image
            PackageManager pm = context.getPackageManager();
            List<ResolveInfo> list = pm.queryIntentActivities(intent, 0);
            int listSize = list.size();
            for (int i = 0; i < listSize; i++) {
                ResolveInfo resolveInfo = list.get(i);
                if ((resolveInfo.activityInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM)
                        != 0) {
                    
                    // Replace the intent with this specific activity
                    preference.setIntent(new Intent().setClassName(
                            resolveInfo.activityInfo.packageName,
                            resolveInfo.activityInfo.name));
                    
                    return true;
                }
            }
        }

        // Did not find a matching activity, so remove the preference
        parentPreferenceGroup.removePreference(preference);
        
        return true;
    }
    
    // Handle Apps2SD Toggle
    private void HandleApps2SDToggle() {
        String str = "Warning!\n\nCurrently enabling Apps2SD is a non-reversible operation. The only way to undo Apps2SD is to perform a data wipe.\n\nAre you sure you want to enable Apps2SD?";

        // Immediate Return on False
        if(!mApps2SDPref.isChecked())
             return;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(str)
               .setCancelable(false)
               .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                      // Perform change();
                      mApps2SDPref.setEnabled(false);
                      doRebootToast();
                    }
               })
               .setNegativeButton("No", new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                      // Cancel
                      mApps2SDPref.setChecked(false);
                   }
               });
        AlertDialog alertDialog = builder.create();
        builder.show();
    }

    // Handle Dalvik-Cache to Cache Toggle
    private void HandleDCCacheToggle() {
        String str = "Warning!\n\nCurrently enabling Dalvik-Cache to /cache is a non-reversible operation. The only way to undo Dalvik-Cache to /cache is to perform a data wipe.\n\nAre you sure you want to enable Dalvik-Cache to /cache?";

        // Immediate Return on False
        if(!mDCCachePref.isChecked())
             return;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(str)
               .setCancelable(false)
               .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                      // Perform change();
                      mDCCachePref.setEnabled(false);
                      mDCSDCardPref.setEnabled(false);
                      doRebootToast();
                    }
               })
               .setNegativeButton("No", new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                      // Cancel
                      mDCCachePref.setChecked(false);
                   }
               });
        AlertDialog alertDialog = builder.create();
        builder.show();
    }

    // Handle Dalvik-Cache to SDCard Toggle
    private void HandleDCSDCardToggle() {
        String str = "Warning!\n\nCurrently enabling Dalvik-Cache to SDCard is a non-reversible operation. The only way to undo Dalvik-Cache to SDCard is to perform a data wipe.\n\nAre you sure you want to enable Dalvik-Cache to SDCard?";

        // Immediate Return on False
        if(!mDCSDCardPref.isChecked())
             return;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(str)
               .setCancelable(false)
               .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                      // Perform change();
                      mDCCachePref.setEnabled(false);
                      mDCSDCardPref.setEnabled(false);
                      doRebootToast();
                    }
               })
               .setNegativeButton("No", new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                      // Cancel
                      mDCSDCardPref.setChecked(false);
                   }
               });
        AlertDialog alertDialog = builder.create();
        builder.show();
    }

    // Handle OTA Update Notifications Toggle
    private void HandleOTAToggle() {
        String str = "Warning!\n\nDisabling OTA Update Notifications is a non-reversible operation. The only way to undo this is to reinstall the ROM.\n\nAre you sure you want to disable OTA Update Notifications?";

        // Immediate Return on True
        if(mOTAUpdatePref.isChecked())
             return;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(str)
               .setCancelable(false)
               .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                      // Perform change();
                      mOTAUpdatePref.setEnabled(false);
                      doRebootToast();
                    }
               })
               .setNegativeButton("No", new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                      // Cancel
                      mOTAUpdatePref.setChecked(true);
                   }
               });
        AlertDialog alertDialog = builder.create();
        builder.show();
    }

    // Obtain FileSystem Partition Sizes
    private String ObtainFSPartSize(String PartitionPath){
            String retstr;
            File extraPath = new File(PartitionPath);
            StatFs extraStat = new StatFs(extraPath.getPath());
            long eBlockSize = extraStat.getBlockSize();
            long eTotalBlocks = extraStat.getBlockCount();
            long eAvailableBlocks = extraStat.getAvailableBlocks();
            
            retstr = formatSize(eAvailableBlocks * eBlockSize);
            retstr += "  /  ";
            retstr += formatSize(eTotalBlocks * eBlockSize);

            return retstr;
    }

    private void SetupFSPartSize(){
            try {
                    mSystemSize.setSummary(ObtainFSPartSize    (SYSTEM_STORAGE_PATH));
                    mDataSize.setSummary(ObtainFSPartSize      (DATA_STORAGE_PATH));
                    mCacheSize.setSummary(ObtainFSPartSize     (CACHE_STORAGE_PATH));
                    mSDCardFATSize.setSummary(ObtainFSPartSize (SDCARDFAT_STORAGE_PATH));
                    if(extfsIsMounted)
                           mSDCardEXTSize.setSummary(ObtainFSPartSize (SDCARDEXT_STORAGE_PATH));
            } catch (IllegalArgumentException e) {
                    Log.w(TAG, "Failed to obtain FS partition sizes");
                    e.printStackTrace();
            }
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        addPreferencesFromResource(R.xml.spare_parts);

        PreferenceScreen prefSet = getPreferenceScreen();
        
        mWindowAnimationsPref = (ListPreference) prefSet.findPreference(WINDOW_ANIMATIONS_PREF);
        mWindowAnimationsPref.setOnPreferenceChangeListener(this);
        mTransitionAnimationsPref = (ListPreference) prefSet.findPreference(TRANSITION_ANIMATIONS_PREF);
        mTransitionAnimationsPref.setOnPreferenceChangeListener(this);
        mFancyImeAnimationsPref = (CheckBoxPreference) prefSet.findPreference(FANCY_IME_ANIMATIONS_PREF);
        mHapticFeedbackPref = (CheckBoxPreference) prefSet.findPreference(HAPTIC_FEEDBACK_PREF);
        mFontSizePref = (ListPreference) prefSet.findPreference(FONT_SIZE_PREF);
        mFontSizePref.setOnPreferenceChangeListener(this);
        mEndButtonPref = (ListPreference) prefSet.findPreference(END_BUTTON_PREF);
        mEndButtonPref.setOnPreferenceChangeListener(this);
        mShowMapsCompassPref = (CheckBoxPreference) prefSet.findPreference(MAPS_COMPASS_PREF);
        mCompatibilityMode = (CheckBoxPreference) findPreference(KEY_COMPATIBILITY_MODE);
        mCompatibilityMode.setPersistent(false);
        mCompatibilityMode.setChecked(Settings.System.getInt(getContentResolver(),
                Settings.System.COMPATIBILITY_MODE, 1) != 0);

        // Setup our Listeners
        mCPUMinFreqPref = (ListPreference) prefSet.findPreference(CPU_MIN_FREQ_PREF);
        mCPUMinFreqPref.setOnPreferenceChangeListener(this);

        mCPUMaxFreqPref = (ListPreference) prefSet.findPreference(CPU_MAX_FREQ_PREF);
        mCPUMaxFreqPref.setOnPreferenceChangeListener(this);

        mCPUGovernorPref = (ListPreference) prefSet.findPreference(CPU_GOVERNOR_PREF);
        mCPUGovernorPref.setOnPreferenceChangeListener(this);

        mSwappinessPref = (ListPreference) prefSet.findPreference(SWAPPINESS_PREF);
        mSwappinessPref.setOnPreferenceChangeListener(this);

        // Check for Ext Partition Mounted
        extfsIsMounted = SystemProperties.get("ep.extfs.mounted", "0").equals("1");

        // Check for LinuxSWAP
        linuxSWAPExists = SystemProperties.get("ep.linuxswap.exists", "0").equals("1");

        // Hook up the Partition Sizes
        mSystemSize        = (Preference) prefSet.findPreference(SYSTEM_PART_SIZE);
        mDataSize          = (Preference) prefSet.findPreference(DATA_PART_SIZE);
        mCacheSize         = (Preference) prefSet.findPreference(CACHE_PART_SIZE);
        mSDCardFATSize     = (Preference) prefSet.findPreference(SDCARDFAT_PART_SIZE);
        mSDCardEXTSize     = (Preference) prefSet.findPreference(SDCARDEXT_PART_SIZE);
        SetupFSPartSize();

        // Setup DataApp/Dalvik-Cache Options
        mApps2SDPref       = (CheckBoxPreference) prefSet.findPreference(APPS2SD_PREF);
        mDCCachePref       = (CheckBoxPreference) prefSet.findPreference(DC_CACHE_PREF);
        mDCSDCardPref      = (CheckBoxPreference) prefSet.findPreference(DC_SDCARD_PREF);

        // Rotation Settings
        mRotation270Pref   = (CheckBoxPreference) prefSet.findPreference(ROTATION_270_PREF);
        mRotation180Pref   = (CheckBoxPreference) prefSet.findPreference(ROTATION_180_PREF);

        // Grab Rotatiom SystemSettings and Set Em
        mRotation270Pref.setChecked(Settings.System.getInt(getApplicationContext().getContentResolver(),
                Settings.System.USE_270_ORIENTATION, 0) > 0 ? true : false);
        mRotation180Pref.setChecked(Settings.System.getInt(getApplicationContext().getContentResolver(),
                Settings.System.USE_180_ORIENTATION, 0) > 0 ? true : false);

        // Memory/SWAP Options
        mCompcachePref     = (CheckBoxPreference) prefSet.findPreference(COMPCACHE_PREF);
        mLinuxSWAPPref     = (CheckBoxPreference) prefSet.findPreference(LINUXSWAP_PREF);
        if(!linuxSWAPExists){
            mLinuxSWAPPref.setEnabled(false);
            mLinuxSWAPPref.setSummaryOff("no linuxswap partition");
        }

        // Misc Options
        mPIDPrioPref       = (CheckBoxPreference) prefSet.findPreference(PID_PRIO_PREF);
        mUserinitPref      = (CheckBoxPreference) prefSet.findPreference(USERINIT_PREF);
        mOTAUpdatePref     = (CheckBoxPreference) prefSet.findPreference(OTA_UPDATE_PREF);

        // Hook up Enables by Checks
        if(mApps2SDPref.isChecked())
            mApps2SDPref.setEnabled(false);

        if(mDCCachePref.isChecked() || mDCSDCardPref.isChecked()){
            mDCCachePref.setEnabled(false);
            mDCSDCardPref.setEnabled(false);
        }

        if(!mOTAUpdatePref.isChecked())
            mOTAUpdatePref.setEnabled(false);

        // Manual Override when extfs is not present
        if(!extfsIsMounted){
            mApps2SDPref.setEnabled(false);
            mDCSDCardPref.setEnabled(false);
            mSDCardEXTSize.setEnabled(false);
            mSDCardEXTSize.setSummary("no extfs partition");
        }

        // Hook up App MOD Options
        mARLauncherPref           = (CheckBoxPreference) prefSet.findPreference(AR_LAUNCHER_PREF);
        mPhoneBrowserLauncherPref = (CheckBoxPreference) prefSet.findPreference(PHONEBROWSER_LAUNCHER_PREF);
        mCyTownPhonePref          = (CheckBoxPreference) prefSet.findPreference(CYTOWN_PHONE_PREF);
        mWysieContactsPref        = (CheckBoxPreference) prefSet.findPreference(WYSIE_CONTACTS_PREF);
        mINTKeyboardPref          = (CheckBoxPreference) prefSet.findPreference(INT_KEYBOARD_PREF);

        // Setup our Quick Commands
        findPreference(REBOOT_NORMAL_COMMAND).setOnPreferenceClickListener(
            new OnPreferenceClickListener() {
                 public boolean onPreferenceClick(Preference preference) {
                     RunSuCommand("start reboot & sleep 5", "Are you sure you want to reboot?");
                     return true;
                 }
            }
        );

        findPreference(REBOOT_RECOVERY_COMMAND).setOnPreferenceClickListener(
            new OnPreferenceClickListener() {
                 public boolean onPreferenceClick(Preference preference) {
                     RunSuCommand("start reboot-recovery & sleep 5", "Are you sure you want to reboot into recovery mode?");
                     return true;
                 }
            }
        );

        findPreference(REBOOT_BOOTLOADER_COMMAND).setOnPreferenceClickListener(
            new OnPreferenceClickListener() {
                 public boolean onPreferenceClick(Preference preference) {
                     RunSuCommand("start reboot-bootload & sleep 5", "Are you sure you want to reboot into the bootloader?");
                     return true;
                 }
            }
        );

        findPreference(SHUTDOWN_COMMAND).setOnPreferenceClickListener(
            new OnPreferenceClickListener() {
                 public boolean onPreferenceClick(Preference preference) {
                     RunSuCommand("start shutdown & sleep 5", "Are you sure you want to power off your phone?");
                     return true;
                 }
            }
        );

        findPreference(MOUNT_SYSTEM_RW_COMMAND).setOnPreferenceClickListener(
            new OnPreferenceClickListener() {
                 public boolean onPreferenceClick(Preference preference) {
                     RunSuCommand("sleep 1 && mount -o rw,remount /dev/block/mtdblock3 /system", "Are you sure you want to mount /system as read/write?");
                     return true;
                 }
            }
        );

        findPreference(MOUNT_SYSTEM_RO_COMMAND).setOnPreferenceClickListener(
            new OnPreferenceClickListener() {
                 public boolean onPreferenceClick(Preference preference) {
                     RunSuCommand("sleep 1 && mount -o ro,remount /dev/block/mtdblock3 /system", "Are you sure you want to mount /system as readonly?");
                     return true;
                 }
            }
        );

        findPreference(RESTART_LAUNCHER2_COMMAND).setOnPreferenceClickListener(
            new OnPreferenceClickListener() {
                 public boolean onPreferenceClick(Preference preference) {
                     ActivityManager am = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
                     am.restartPackage("com.android.launcher2");
                     return true;
                 }
            }
        );

        mWindowManager = IWindowManager.Stub.asInterface(ServiceManager.getService("window"));
        
        final PreferenceGroup parentPreference = getPreferenceScreen();
        updatePreferenceToSpecificActivityOrRemove(this, parentPreference,
                BATTERY_HISTORY_PREF, 0);
        updatePreferenceToSpecificActivityOrRemove(this, parentPreference,
                BATTERY_INFORMATION_PREF, 0);
        updatePreferenceToSpecificActivityOrRemove(this, parentPreference,
                USAGE_STATISTICS_PREF, 0);
        
        parentPreference.getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    private void updateToggles() {
        try {
            mFancyImeAnimationsPref.setChecked(Settings.System.getInt(
                    getContentResolver(), 
                    Settings.System.FANCY_IME_ANIMATIONS, 0) != 0);
            mHapticFeedbackPref.setChecked(Settings.System.getInt(
                    getContentResolver(), 
                    Settings.System.HAPTIC_FEEDBACK_ENABLED, 0) != 0);
            Context c = createPackageContext("com.google.android.apps.maps", 0);
            mShowMapsCompassPref.setChecked(c.getSharedPreferences("extra-features", MODE_WORLD_READABLE)
                .getBoolean("compass", false));
        } catch (NameNotFoundException e) {
            Log.w(TAG, "Failed reading maps compass");
            e.printStackTrace();
        }
    }
    
    public boolean onPreferenceChange(Preference preference, Object objValue) {
        if (preference == mWindowAnimationsPref) {
            writeAnimationPreference(0, objValue);
        } else if (preference == mTransitionAnimationsPref) {
            writeAnimationPreference(1, objValue);
        } else if (preference == mFontSizePref) {
            writeFontSizePreference(objValue);
        } else if (preference == mEndButtonPref) {
            writeEndButtonPreference(objValue);
        } else if (preference == mCPUMinFreqPref) {
            doRebootToast();
        } else if (preference == mCPUMaxFreqPref) {
            doRebootToast();
        } else if (preference == mCPUGovernorPref) {
            doRebootToast();
        } else if (preference == mSwappinessPref) {
            doRebootToast();
        }
        // always let the preference setting proceed.
        return true;
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == mCompatibilityMode) {
            Settings.System.putInt(getContentResolver(),
                    Settings.System.COMPATIBILITY_MODE,
                    mCompatibilityMode.isChecked() ? 1 : 0);
            return true;
        }
        return false;
    }

    public void writeAnimationPreference(int which, Object objValue) {
        try {
            float val = Float.parseFloat(objValue.toString());
            mWindowManager.setAnimationScale(which, val);
        } catch (NumberFormatException e) {
        } catch (RemoteException e) {
        }
    }
    
    public void writeFontSizePreference(Object objValue) {
        try {
            mCurConfig.fontScale = Float.parseFloat(objValue.toString());
            ActivityManagerNative.getDefault().updateConfiguration(mCurConfig);
        } catch (RemoteException e) {
        }
    }
    
    public void writeEndButtonPreference(Object objValue) {
        try {
            int val = Integer.parseInt(objValue.toString());
            Settings.System.putInt(getContentResolver(),
                    Settings.System.END_BUTTON_BEHAVIOR, val);
        } catch (NumberFormatException e) {
        }
    }
    
    int floatToIndex(float val, int resid) {
        String[] indices = getResources().getStringArray(resid);
        float lastVal = Float.parseFloat(indices[0]);
        for (int i=1; i<indices.length; i++) {
            float thisVal = Float.parseFloat(indices[i]);
            if (val < (lastVal + (thisVal-lastVal)*.5f)) {
                return i-1;
            }
            lastVal = thisVal;
        }
        return indices.length-1;
    }
    
    public void readAnimationPreference(int which, ListPreference pref) {
        try {
            float scale = mWindowManager.getAnimationScale(which);
            pref.setValueIndex(floatToIndex(scale,
                    R.array.entryvalues_animations));
        } catch (RemoteException e) {
        }
    }
    
    public void readFontSizePreference(ListPreference pref) {
        try {
            mCurConfig.updateFrom(
                ActivityManagerNative.getDefault().getConfiguration());
        } catch (RemoteException e) {
        }
        pref.setValueIndex(floatToIndex(mCurConfig.fontScale,
                R.array.entryvalues_font_size));
    }
    
    public void readEndButtonPreference(ListPreference pref) {
        try {
            pref.setValueIndex(Settings.System.getInt(getContentResolver(),
                    Settings.System.END_BUTTON_BEHAVIOR));
        } catch (SettingNotFoundException e) {
        }
    }
    
    private void RunSuCommand(final String CommandStr, String YesNoString){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(YesNoString)
               .setCancelable(false)
               .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                      // Perform Command();
                      new SuServer().execute(CommandStr);
                    }
               })
               .setNegativeButton("No", new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                      // Cancel
                   }
               });
        AlertDialog alertDialog = builder.create();
        builder.show();
    }

    public void onSharedPreferenceChanged(SharedPreferences preferences, String key) {
        if (FANCY_IME_ANIMATIONS_PREF.equals(key)) {
            Settings.System.putInt(getContentResolver(),
                    Settings.System.FANCY_IME_ANIMATIONS,
                    mFancyImeAnimationsPref.isChecked() ? 1 : 0);
        } else if (HAPTIC_FEEDBACK_PREF.equals(key)) {
            Settings.System.putInt(getContentResolver(),
                    Settings.System.HAPTIC_FEEDBACK_ENABLED,
                    mHapticFeedbackPref.isChecked() ? 1 : 0);
        } else if (MAPS_COMPASS_PREF.equals(key)) {
            try {
                Context c = createPackageContext("com.google.android.apps.maps", 0);
                c.getSharedPreferences("extra-features", MODE_WORLD_WRITEABLE)
                    .edit()
                    .putBoolean("compass", mShowMapsCompassPref.isChecked())
                    .commit();
            } catch (NameNotFoundException e) {
                Log.w(TAG, "Failed setting maps compass");
                e.printStackTrace();
            }
        } else if (AR_LAUNCHER_PREF.equals(key)) {
            try {
                Context c = createPackageContext("com.android.spare_parts", 0);
                c.getSharedPreferences("spare_settings", MODE_WORLD_READABLE)
                    .edit()
                    .putBoolean("ar_launcher_opt", mARLauncherPref.isChecked())
                    .commit();
            } catch (NameNotFoundException e) {
                Log.w(TAG, "Failed setting maps compass");
                e.printStackTrace();
            }
            ActivityManager am = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
            am.restartPackage("com.android.launcher2");
        } else if (PHONEBROWSER_LAUNCHER_PREF.equals(key)) {
            try {
                Context c = createPackageContext("com.android.spare_parts", 0);
                c.getSharedPreferences("spare_settings", MODE_WORLD_READABLE)
                    .edit()
                    .putBoolean("phonebrowser_launcher_opt", mPhoneBrowserLauncherPref.isChecked())
                    .commit();
            } catch (NameNotFoundException e) {
                Log.w(TAG, "Failed setting maps compass");
                e.printStackTrace();
            }
            ActivityManager am = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
            am.restartPackage("com.android.launcher2");
        } else if (CYTOWN_PHONE_PREF.equals(key)) {
            doRebootToast();
        } else if (WYSIE_CONTACTS_PREF.equals(key)) {
            doRebootToast();
        } else if (INT_KEYBOARD_PREF.equals(key)) {
            doRebootToast();
        } else if (APPS2SD_PREF.equals(key)) {
            HandleApps2SDToggle();
        } else if (DC_CACHE_PREF.equals(key)) {
            HandleDCCacheToggle();
        } else if (DC_SDCARD_PREF.equals(key)) {
            HandleDCSDCardToggle();
        } else if (ROTATION_270_PREF.equals(key)) {
            Settings.System.putInt(getApplicationContext().getContentResolver(),
                Settings.System.USE_270_ORIENTATION, mRotation270Pref.isChecked() ? 1 : 0);
        } else if (ROTATION_180_PREF.equals(key)) {
            Settings.System.putInt(getApplicationContext().getContentResolver(),
                Settings.System.USE_180_ORIENTATION, mRotation180Pref.isChecked() ? 1 : 0);
        } else if (COMPCACHE_PREF.equals(key)) {
            doRebootToast();
        } else if (LINUXSWAP_PREF.equals(key)) {
            doRebootToast();
        } else if (PID_PRIO_PREF.equals(key)) {
            doRebootToast();
        } else if (USERINIT_PREF.equals(key)) {
            doRebootToast();
        } else if (OTA_UPDATE_PREF.equals(key)) {
            HandleOTAToggle();
        }
    }
    
    @Override
    public void onResume() {
        super.onResume();
        readAnimationPreference(0, mWindowAnimationsPref);
        readAnimationPreference(1, mTransitionAnimationsPref);
        readFontSizePreference(mFontSizePref);
        readEndButtonPreference(mEndButtonPref);
        updateToggles();
    }

    private Object getString(String string) {
        return string;
    }

    private String formatSize(long size) {
        return Formatter.formatFileSize(this, size);
    }

    private void doRebootToast() {
        Toast toast = Toast.makeText(getApplicationContext(), "You must reboot for your changes to take effect.", Toast.LENGTH_SHORT);
        toast.show();
    }


    /**
    * SuServer (Runs commands via the 'su' <superuser> app)
    * 
    * NOTE: Largely taken from GUSTO by pao from TheOfficial TMO(US/EU)/ADP/AOSP (g1/mt3g)
    *                  (ty)
    */
    private class SuServer extends AsyncTask<String, String, Void> {

        private ProgressDialog pd;

        @Override
        protected void onPreExecute() {
            pd = ProgressDialog.show(SpareParts.this, "Working", "Running command ...", true, false);
        }

        @Override
        protected void onProgressUpdate(String... values) {
            // Enomther: Let's not ... and say we did :)
            //pd.setMessage(values[0]);
        }

        @Override
        protected void onPostExecute(Void result) {
            pd.dismiss();
        }

        @Override
        protected Void doInBackground(String... args) {
           final Process p;

           try {
              p = Runtime.getRuntime().exec("su -c sh");
              BufferedReader stdInput = new BufferedReader(
              new InputStreamReader(p.getInputStream()));
              BufferedReader stdError = new BufferedReader(
              new InputStreamReader(p.getErrorStream()));
              BufferedWriter stdOutput = new BufferedWriter(
              new OutputStreamWriter(p.getOutputStream()));

              stdOutput.write(args[0] + " && exit\n");
              stdOutput.flush();

              Thread t = new Thread() {
              public void run() {
              try {
                   p.waitFor();
              } catch (InterruptedException e) {
                   e.printStackTrace();
              }
           }
        };
        t.start();

        // Poor man's select()
        while (t.isAlive()) {
           String status = stdInput.readLine();
           if (status != null)
               publishProgress(status);
           Thread.sleep(20);
        }

        stdInput.close();
        stdError.close();
        stdOutput.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
      }
    }
}


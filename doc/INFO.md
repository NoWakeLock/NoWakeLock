# Android Wakelock, Alarm, Service Documentation

## NlpWakeLock

**Source:** Amplify strings.xml - `<string name="desc_nlpwakelock">`

**Description:** NlpWakeLock is safe to limit. It's used by Google Play Services to determine your rough location using a combination of cell towers and WiFi. Once it has your location, it stores it locally so other apps, like Google Now, can access your location without using GPS or getting a new fix. Recommended settings are between 180 and 600 seconds.

**Recommendation:** Allow each 420 seconds (from Google Sheets - Amplify Module Database, row 41)

## NlpCollectorWakeLock

**Source:** Amplify strings.xml - `<string name="desc_nlpcollectorwakelock">`

**Description:** NlpCollectorWakeLock is safe to limit. It's used by Google Play Services to determine your rough location using a combination of cell towers and wifi. Once it has your location, it sends it back to Google so they can expand their database of WiFi locations. Recommended settings are between 180 and 600 seconds.

**Recommendation:** Allow each 900 seconds (from WakeBlock/wakelocks.md)

## AlarmManager

**Source:** Amplify strings.xml - `<string name="desc_alarmmanager">`

**Description:** This provides access to the system alarm services. When an alarm goes off, the Intent that had been registered for it is broadcast by the system, automatically starting the target application if it is not already running. Registered alarms are retained while the device is asleep (and can optionally wake the device up if they go off during that time), but will be cleared if it is turned off and rebooted.

**Recommendation:** NOT RECOMMENDED to limit (from Google Sheets - Amplify Module Database, row 17)

## AudioOffload

**Source:** Amplify strings.xml - `<string name="desc_audiooffload">`

**Description:** Audio playback when your screen is turned off. If you disable this, you won't have background audio or music able to play when your screen is off.

**Recommendation:** NOT RECOMMENDED to limit (from Google Sheets - Amplify Module Database, row 18)

## ActivityManagerLauncher

**Source:** Amplify strings.xml - `<string name="desc_activitymanagerlauncher">`

**Description:** Interacts with the overall activities running in the system. If you limit this, nothing will work well.

**Recommendation:** UNSAFE to limit (from Google Sheets - Amplify Module Database, row 16)

## WindowManager

**Source:** Amplify strings.xml - `<string name="desc_windowmanager">`

**Description:** This is an Android System level WakeLock. The interface that apps use to talk to the window manager - meaning, applications will require a WindowManager lock everytime they need to be shown on the screen.

**Recommendation:** NOT RECOMMENDED to limit (from Google Sheets - Amplify Module Database, row 19)

## RILJ

**Source:** Amplify strings.xml - `<string name="desc_rilj">`

**Description:** RILJ keeps your device awake while processing phone actions, such as phone calls and cell tower communication.

**Recommendation:** NOT RECOMMENDED to limit, set to 600 seconds if needed (from Google Sheets - Amplify Module Database, row 24). WARNING: Do NOT block this wakelock if you have Android Oreo 8+ or it will bootloop (from WakeBlock/wakelocks.md)

## SyncLoopWakeLock

**Source:** Amplify strings.xml - `<string name="desc_syncloopwakelock">`

**Description:** This is the WakeLock used by Android SyncManager to Sync accounts like Google+, Twitter, Linkedin, Gmail etc. The higher the unbouncing, the longer the amount of time until your accounts will get synced again.

**Recommendation:** SAFE to limit, allow each 600+- seconds (from Google Sheets - Amplify Module Database, row 25)

## ICING

**Source:** Amplify strings.xml - `<string name="desc_icing">`

**Description:** ICING is a Google Services WakeLock. Currently looking for more details.

**Recommendation:** NOT RECOMMENDED to limit, if needed set to 900+- seconds (from Google Sheets - Amplify Module Database, row 26)

## StartingAlertService

**Source:** Amplify strings.xml - `<string name="desc_startingalertservice">`

**Description:** This WakeLock can usually be associated with Calendar, although may effect other apps such as messaging. If your Calendar has notifications / alerts it can hold this WakeLock.

**Recommendation:** PROBABLY SAFE to limit, allow each 240+- seconds (from Google Sheets - Amplify Module Database, row 27)

## AudioMix

**Source:** Amplify strings.xml - `<string name="desc_audiomix">`

**Description:** This WakeLock handles Touch Sounds and Alert Sounds among others. It has plagued some devices even with Touch Sounds turned off.

**Recommendation:** UNSAFE to limit (from Google Sheets - Amplify Module Database, row 28)

## LocationManagerService

**Source:** Amplify strings.xml - `<string name="desc_locationmanagerservice">`

**Description:** As the name implies. This is not typically a high battery drainer for some it might held a big wakelock.

**Recommendation:** PROBABLY SAFE to limit, allow each 420 seconds (from Google Sheets - Amplify Module Database, row 38)

## AudioIn

**Source:** Amplify strings.xml - `<string name="desc_audioin">`

**Description:** This wakelock is used to hold the device awake listening for Google Search/Hotword Detection. Okay, Google

**Recommendation:** PROBABLY SAFE to limit, allow each 600 seconds (from Google Sheets - Amplify Module Database, row 20)

## NfcServiceMroutingWakeLock

**Source:** Amplify strings.xml - `<string name="desc_nfcservicemroutingwakelock">`

**Description:** Related to NFC service routing detected tags to the appropriate application.

**Recommendation:** SAFE to limit, allow each 240 seconds (from Google Sheets - Amplify Module Database, row 39)

## WakefulIntentServiceGcoreUlrLocationReportingService

**Source:** Amplify strings.xml - `<string name="desc_wakefulintentservicegcoreulrlocationreportingservice">`

**Description:** Used by Google Location Services to report your current location.

**Recommendation:** SAFE to limit, allow each 240+- seconds (from Google Sheets - Amplify Module Database, row 40)

## VzwGpsLocationProvider

**Source:** Amplify strings.xml - `<string name="desc_vzwgpslocationprovider">`

**Description:** Verizon specific location service used for cell tower statistics.

**Recommendation:** SAFE to limit, allow each 600 seconds (from Google Sheets - Amplify Module Database, row 43)

## Hangouts_rtcs

**Source:** Amplify strings.xml - `<string name="desc_hangouts_rtcs">`

**Description:** Google Hangouts. If you limit this, Hangouts will no longer work.

**Recommendation:** NOT RECOMMENDED to limit (from Google Sheets - Amplify Module Database, row 21)

## GCM_CONN

**Source:** Amplify strings.xml - `<string name="desc_gcm_conn">`

**Description:** Google Cloud Messaging. Sends lightweight messages to your apps. Responsible for sending push notifications to your phone, for all apps.

**Recommendation:** UNSAFE to limit (from Google Sheets - Amplify Module Database, row 14), but if needed, allow each 450,000 ms (from WakeBlock/wakelocks.md)

## GOOGLE_C2DM

**Source:** Amplify strings.xml - `<string name="desc_google_c2dm">`

**Description:** Cloud to Device Messaging (C2DM) is a service that helps developers send data to their apps on devices. The service provides a mechanism that servers can use to tell mobile applications to contact the server to fetch updated application or user data. The C2DM service handles all aspects of queueing of messages and delivery to the target application running on target devices. This service was deprecated since 2012 but there are still apps that are using this service instead of the new and improved GCM.

**Recommendation:** UNSAFE to limit (from Google Sheets - Amplify Module Database, row 15), but if needed, allow each 1,800,000 ms (from WakeBlock/wakelocks.md)

## TimedEventQueue

**Source:** Amplify strings.xml - `<string name="desc_timedeventqueue">`

**Description:** This is a core part of Android that is best left alone. It queues up all incoming events.

**Recommendation:** UNSAFE to limit (from Google Sheets - Amplify Module Database, row 13)

## ComGoogleAndroidGmsNlpAlarm_wakeup_locator

**Source:** Amplify strings.xml - `<string name="desc_comgoogleandroidgmsnlpalarm_wakeup_locator">`

**Description:** This alarm is safe to limit. It's used by Google Play Services to determine your rough location using a combination of cell towers and WiFi. Once it has your location, it stores it locally so other apps, like Google Now, can access your location without using GPS or getting a new fix. Recommended settings are between 180 and 600 seconds.

**Recommendation:** NULL

## ComGoogleAndroidGmsNlpAlarm_wakeup_activity_detection

**Source:** Amplify strings.xml - `<string name="desc_comgoogleandroidgmsnlpalarm_wakeup_activity_detection">`

**Description:** This alarm is safe to limit. It's used by Google Play Services to determine your rough location using a combination of cell towers and wifi. Once it has your location, it sends it back to Google so they can expand their database of WiFi locations. Recommended settings are between 180 and 600 seconds.

**Recommendation:** NULL

## ComGoogleAndroidAppsHangoutsUpdate_notification

**Source:** Amplify strings.xml - `<string name="desc_comgoogleandroidappshangoutsupdate_notification">`

**Description:** The listener which updates your Google Hangouts notifications.

**Recommendation:** NULL

## ComAndroidInternalTelephonyDatastall

**Source:** Amplify strings.xml - `<string name="desc_comandroidinternaltelephonydatastall">`

**Description:** Wakelock from com.android.phone (Phone app) when using Data through Cellular towers. Using WiFi will not trigger this alarm. Usually it gets through the roof while using data, especially on a mediocre connection.

**Recommendation:** NULL

## E

**Source:** Amplify strings.xml - `<string name="desc_e">`

**Description:** Belongs to Tasker

**Recommendation:** NOT RECOMMENDED to limit (from Google Sheets - Amplify Module Database, row 22)

## M

**Source:** Amplify strings.xml - `<string name="desc_m">`

**Description:** Belongs to Tasker

**Recommendation:** NOT RECOMMENDED to limit (from Google Sheets - Amplify Module Database, row 23), but if needed, allow each 180,000 ms (from WakeBlock/wakelocks.md)

## GCoreFlp

**Source:** Amplify strings.xml - `<string name="desc_gcoreflp">`

**Description:** Belongs to the location acquisition framework

**Recommendation:** PROBABLY SAFE to limit, allow each 240 seconds (from Google Sheets - Amplify Module Database, row 29), or 900,000 ms (from WakeBlock/wakelocks.md)

## NetworkStats

**Source:** Amplify strings.xml - `<string name="desc_networkstats">`

**Description:** NetworkStatsService wakelock is held while collecting and recording statistics from the kernel

**Recommendation:** PROBABLY SAFE to limit, allow each 240 seconds (from Google Sheets - Amplify Module Database, row 31), or 900,000 ms (from WakeBlock/wakelocks.md)

## Fingerprint_scanner_static

**Source:** Amplify strings.xml - `<string name="desc_fingerprint_scanner_static">`

**Description:** Related to Google+ - FingerprintScannerIntentService. Piece of code: com.google.android.apps.plus.service.FingerprintScannerIntentService.

**Recommendation:** PROBABLY SAFE to limit, allow each 180 seconds (from Google Sheets - Amplify Module Database, row 33)

## Fingerprint_scanner_local

**Source:** Amplify strings.xml - `<string name="desc_fingerprint_scanner_local">`

**Description:** Related to Google+ - another WakeLock pertaining to location

**Recommendation:** PROBABLY SAFE to limit, allow each 180 seconds (from Google Sheets - Amplify Module Database, row 34)

## CDMAInboundSMSHandler

**Source:** Amplify strings.xml - `<string name="desc_cdmainboundsmshandler">`

**Description:** This is part of the Telephony Package. it's responsible for broadcasting SMS to various apps that are requesting it.

**Recommendation:** NOT RECOMMENDED to limit, but if needed, allow each 240 seconds (from Google Sheets - Amplify Module Database, row 36)

## WakeComGoogleAndroidGmsConfigConfigFetchService

**Source:** Amplify strings.xml - `<string name="desc_wakecomgoogleandroidgmsconfigconfigfetchservice">`

**Description:** This WakeLock popped up with latest Play Services update.

**Recommendation:** PROBABLY SAFE to limit, allow each 600 seconds (from Google Sheets - Amplify Module Database, row 37)

## SyncComGoogleAndroidAppsBigtopProviderBigtopProviderComGoogleAccountName

**Source:** Amplify strings.xml - `<string name="desc_synccomgoogleandroidappsbigtopproviderbigtopprovidercomgoogleaccountname">`

**Description:** Belongs to newly Google app - Inbox (a new form of Gmail). It syncs your mailing account. Best values between 3600 and 7200, depending on how much you want your gmail to be synced.

**Recommendation:** SAFE to limit, allow each 3600 seconds (from Google Sheets - Amplify Module Database, row 46)

## ComOasisfengGreenifyClean_now

**Source:** Amplify strings.xml - `<string name="desc_comoasisfenggreenifyclean_now">`

**Description:** Greenify uses this to greenify after screen timeout.

**Recommendation:** NOT RECOMMENDED to limit (from Google Sheets - Amplify Module Database, row 47), or Disable (from WakeBlock/wakelocks.md)

## AndroidIntentActionTime_tick

**Source:** Amplify strings.xml - `<string name="desc_androidintentactiontime_tick">`

**Description:** This is a listener from android.os.SystemClock. Core timekeeping facilities.

**Recommendation:** NULL

## AndroidAppwidgetActionAppwidget_update

**Source:** Amplify strings.xml - `<string name="desc_androidappwidgetactionappwidget_update">`

**Description:** This is part of android.appwidget.AppWidgetManager - Responsible for updating your widgets. - Gets information about installed AppWidget providers and other AppWidget related state.

**Recommendation:** NULL

## ComGoogleAndroidAppsHangoutsUPDATE_NOTIFICATION

**Source:** Amplify strings.xml - `<string name="desc_comgoogleandroidappshangoutsUPDATE_NOTIFICATION">`

**Description:** The listener which updates your Hangouts notifications

**Recommendation:** NULL

## ComGoogleAndroidIntentActionMCS_HEARTBEAT

**Source:** Amplify strings.xml - `<string name="desc_comgoogleandroidintentactionMCS_HEARTBEAT">`

**Description:** Part of GSF, related to GTalk, Hangouts etc.

**Recommendation:** NULL

## AndroidcontentSyncManagerSYNC_ALARM

**Source:** Amplify strings.xml - `<string name="desc_AndroidcontentSyncManagerSYNC_ALARM">`

**Description:** Related to account based automated SYNC (manual sync still works)

**Recommendation:** NULL

## ComsonymobilestoragecheckerintentactionALARM_EXPIRED

**Source:** Amplify strings.xml - `<string name="desc_comsonymobilestoragecheckerintentactionALARM_EXPIRED">`

**Description:** Checking the storage for low space and retrieving a notification for the user (ONLY FOR SONY MOBILES)

**Recommendation:** NULL

## ComgoogleandroidintentGCM_RECONNECT

**Source:** Amplify strings.xml - `<string name="desc_comgoogleandroidintentGCM_RECONNECT">`

**Description:** Part of Google Cloud Messaging. The GCM WakeLock is unsafe but perhaps altering the Alarm itself could be beneficial.

**Recommendation:** NULL

## AndroidappbackupintentRUN

**Source:** Amplify strings.xml - `<string name="desc_AndroidappbackupintentRUN">`

**Description:** Part of the Backup Manager Service. This Alarm has your phone back up your data periodically.

**Recommendation:** NULL

## ComdevexpertweatheradfreepfxWAKEUP

**Source:** Amplify strings.xml - `<string name="desc_comdevexpertweatheradfreepfxWAKEUP">`

**Description:** Weather and Clock widget

**Recommendation:** NULL

## ComandroisserverIdleMaintenanceServiceactionUPDATE_IDLE_MAINTENANCE_STATE

**Source:** Amplify strings.xml - `<string name="desc_comandroidserverIdleMaintenanceServiceactionUPDATE_IDLE_MAINTENANCE_STATE">`

**Description:** Androids fstrim service that runs when the phone is idle for 1+ hours and above 20% battery

**Recommendation:** NULL

## ComandroisserverUPDATE_TWILIGHT_STATE

**Source:** Amplify strings.xml - `<string name="desc_comandroidserverUPDATE_TWILIGHT_STATE">`

**Description:** Related to androids CABL service, changes the brightness based off time of day.

**Recommendation:** NULL

## ComandroisinternalpolicyimplPhoneWindowManagerDELAYED_KEYGUARD

**Source:** Amplify strings.xml - `<string name="desc_comandroidinternalpolicyimplPhoneWindowManagerDELAYED_KEYGUARD">`

**Description:** Controls the keyguard being enabled after screen timeout.

**Recommendation:** NULL

## AudioOut_1, AudioOut_2

**Source:** XDA - The Total Newb's Guide to Wakelocks

**Description:** This is an evil leech of a wakelock that will drain you dry if given the chance. For being such a pain in the app, it's surprisingly easy to get rid of. This wakelock is created whenever the phone's speaker plays a sound. With 99% of sounds, it goes away almost instantly. With keypad sounds, however, it doesn't go away so quickly, and it will sit there draining your battery for as long as it goes unnoticed.

**Recommendation:** Open Settings, then select sound. Turn off keytone sounds, touch sounds, screen lock sounds and vibrate on screen tap. It'll take some getting used to, but the extra battery you'll coax out just by solving this ridiculously simple problem is more than worth it.

## ConnectivityService

**Source:** XDA - The Total Newb's Guide to Wakelocks

**Description:** This will appear whenever your phone is trying to connect to a mobile data network. Excessive wakelocking here suggests that your phone is having a hard time finding a network, and an even harder time staying on it.

**Recommendation:** Test out different radios and see if one's better in your area. If you're able to control your radio bands and you don't live in an LTE area, setting your phone to hunt for GSM/HSPA connections only can save you a little bit of juice here. Not much, but every drop counts, and if you're not using LTE anyway... Allow each 5,400,000 ms (from WakeBlock/wakelocks.md)

## MediaScannerService

**Source:** XDA - The Total Newb's Guide to Wakelocks

**Description:** This is a wakelock created by the system as it scans your device for music, movies, pictures, etc. Once in a while, it will randomly get hung up and hold the phone at 384 MHz for...well...until you notice and do something about it. Like AudioOut_1, this is a heavy-drain wakelock. Luckily, like AudioOut_1, it's almost always easy to fix.

**Recommendation:** Reboot. Ninety-nine times or so out of a hundred, this solves the problem. If the problem persists, go to Settings -> Applications -> Running then tap on "Show cached processes". Find the Media process and stop it manually to kill the wakelock. Allow each 10,800,000 ms (from WakeBlock/wakelocks.md)

## ActivityManager family

**Source:** XDA - The Total Newb's Guide to Wakelocks

**Description:** This is a harmless wakelock. The typical cause is not exiting out of apps fully before turning the screen off.

**Recommendation:** Don't sweat this one too much. If it's a big issue for you, make sure that you're exiting out of apps fully (i.e., either use the back button to exit the app or FC it in Task Manager) before turning the screen off.

## GTALK_ASYNC_CONN family

**Source:** XDA - The Total Newb's Guide to Wakelocks

**Description:** Despite its name, this wakelock doesn't seem to be directly related to Google Talk. How do I know? I haven't had Google Talk on this phone in over a month, but the wakelock still pops up from time to time. This wakelock also seems to be related to a poor wifi connection, so keep an eye on that as well. These wakelocks can be absolute destroyers of your battery if given the chance, and unfortunately, there's no known root cause for them, and no reliable way of eliminating them.

**Recommendation:** These wakelocks will often disappear within a minute or so of generating. If one becomes persistent, check your wifi/data connection and make sure it's good. If it persists, reboot into recovery and wipe cache and Dalvik ASAFP. That solves the problem temporarily, but it will reoccur.

## NetworkLocationLocator

**Source:** XDA - The Total Newb's Guide to Wakelocks

**Description:** What a lovely name for such a lovely wakelock. It's a minor annoyance usually, nothing more. If this one is persistent, it's because you're in an area with crappy cell coverage and very few Google-mapped Wifi networks.

**Recommendation:** Why, exactly, are you leaving Network Location on all the time anyway?

## NetworkLocationCallbackRunner

**Source:** XDA - The Total Newb's Guide to Wakelocks

**Description:** This is the first wakelock published here that's specific to a phone other than the Skyrocket; it's an S3 issue. Hooray, we've gone global! NetworkLocationCallbackRunner is another wakelock caused by that most wonderful of apps, Google Maps. If you're still using it, seriously, why?

**Recommendation:** Upon turning on your phone, don't open Google Maps or anything else that utilizes Google location data. Or, you know, you could just uninstall Google Maps and use an alternative program.

## Show keyguard

**Source:** XDA - The Total Newb's Guide to Wakelocks

**Description:** This is a new one for me. It had always been there, but since switching ROMs, it's really started to show up. Not in massive quantities, but enough to make me scratch my head. I've already established that setting your lockscreen to not show user info, weather or calendar data will significantly reduce this. I'll play around with adding those back in more, and having sliders on your default lockscreen won't do much damage either. Still, the more people who've goofed around with this one, the better, as it makes this entry all the more accurate.

**Recommendation:** I'm testing several possibilities now, but the one that's worked best so far is turning calendar, weather and user info off. It seems that having those on causes the lockscreen to wake the phone to refresh itself, which creates the wakelock. Judging by my recent experience, this seems to be a pretty big leech.

## Chekin Service

**Source:** XDA - The Total Newb's Guide to Wakelocks

**Description:** This wakelock, while a Google Services process, seems to be caused by Facebook. That kind of confirms my theory that Facebook "borrows" Google services.

**Recommendation:** Uninstall Facebook and use an alternative app, or just access Facebook through your mobile browser of choice.

## SCREEN_FROZEN

**Source:** XDA - The Total Newb's Guide to Wakelocks

**Description:** Uh oh.

**Recommendation:** If this is high on your list, you've got bigger problems than a wakelock.

## RILJ_ACK_WL

**Source:** WakeBlock/wakelocks.md

**Description:** NULL

**Recommendation:** WARNING: Do NOT block this wakelock if you have Android Oreo 8+ or it will bootloop. Allow each 900,000 ms (from WakeBlock/wakelocks.md)

## AnyMotionDetector

**Source:** WakeBlock/wakelocks.md

**Description:** NULL

**Recommendation:** Disable (from WakeBlock/wakelocks.md)

## BleScanner_WakeLock

**Source:** WakeBlock/wakelocks.md

**Description:** NULL

**Recommendation:** Allow each 180,000 ms (from WakeBlock/wakelocks.md)

## CMWakeLock

**Source:** WakeBlock/wakelocks.md

**Description:** NULL

**Recommendation:** Allow each 900,000 ms (from WakeBlock/wakelocks.md)

## DdsCardSelectionController

**Source:** WakeBlock/wakelocks.md

**Description:** NULL

**Recommendation:** Allow each 3,600,000 ms (from WakeBlock/wakelocks.md)

## DreamManagerService

**Source:** WakeBlock/wakelocks.md

**Description:** NULL

**Recommendation:** Allow each 900,000 ms. Might break Fingerprint Unlock (from WakeBlock/wakelocks.md)

## SyncManagerImpl

**Source:** WakeBlock/wakelocks.md

**Description:** NULL

**Recommendation:** Allow each 5,400,000 ms (from WakeBlock/wakelocks.md)

## Wake:CollectionChimeraSvc

**Source:** WakeBlock/wakelocks.md

**Description:** NULL

**Recommendation:** Allow each 10,800,000 ms (from WakeBlock/wakelocks.md)
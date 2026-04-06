/*
 * Copyright (C) 2013 Google Inc.
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

package com.google.example.games.basegameutils;


import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.google.android.gms.games.AchievementsClient;
import com.google.android.gms.games.GamesSignInClient;
import com.google.android.gms.games.LeaderboardsClient;
import com.google.android.gms.games.PlayGames;
import com.google.android.gms.games.PlayGamesSdk;
import com.google.android.gms.games.Player;

import northern.captain.gamecore.gplus.IGoogleGamesProcessor;

public class GameHelper {

    static final String TAG = "GameHelper";

    /** Listener for sign-in success or failure events. */
    public interface GameHelperListener {
        /**
         * Called when sign-in fails. As a result, a "Sign-In" button can be
         * shown to the user; when that button is clicked, call
         *
         * @link{GamesHelper#beginUserInitiatedSignIn . Note that not all calls
         *                                            to this method mean an
         *                                            error; it may be a result
         *                                            of the fact that automatic
         *                                            sign-in could not proceed
         *                                            because user interaction
         *                                            was required (consent
         *                                            dialogs). So
         *                                            implementations of this
         *                                            method should NOT display
         *                                            an error message unless a
         *                                            call to @link{GamesHelper#
         *                                            hasSignInError} indicates
         *                                            that an error indeed
         *                                            occurred.
         */
        void onSignInFailed();

        /** Called when sign-in succeeds. */
        void onSignInSucceeded();
    }

    // configuration done?
    private boolean mSetupDone = false;

    // are we currently connecting?
    private boolean mConnecting = false;

    // Are we expecting the result of a resolution flow?
    boolean mExpectingResolution = false;

    // was the sign-in flow cancelled when we tried it?
    // if true, we know not to try again automatically.
    boolean mSignInCancelled = false;

    /**
     * The Activity we are bound to. We need to keep a reference to the Activity
     * because some games methods require an Activity (a Context won't do). We
     * are careful not to leak these references: we release them on onStop().
     */
    Activity mActivity = null;

    // app context
    Context mAppContext = null;

    // Client request flags
    public final static int CLIENT_NONE = 0x00;
    public final static int CLIENT_GAMES = 0x01;
    public final static int CLIENT_SNAPSHOT = 0x08;

    // Whether to automatically try to sign in on onStart(). We only set this
    // to true when the sign-in process fails or the user explicitly signs out.
    // We set it back to false when the user initiates the sign in process.
    boolean mConnectOnStart = true;

    /*
     * Whether user has specifically requested that the sign-in process begin.
     * If mUserInitiatedSignIn is false, we're in the automatic sign-in attempt
     * that we try once the Activity is started -- if true, then the user has
     * already clicked a "Sign-In" button or something similar
     */
    boolean mUserInitiatedSignIn = false;

    // Print debug logs?
    boolean mDebugLog = false;

    Handler mHandler;

    // Listener
    GameHelperListener mListener = null;

    GamesSignInClient gamesSignInClient = null;
    boolean mSignedIn = false;
    /**
     * Construct a GameHelper object, initially tied to the given Activity.
     * After constructing this object, call @link{setup} from the onCreate()
     * method of your Activity.
     *
     * @param clientsToUse
     *            the API clients to use (a combination of the CLIENT_* flags,
     *            or CLIENT_ALL to mean all clients).
     */
    public GameHelper(Activity activity, int clientsToUse) {
        mActivity = activity;
        mAppContext = activity.getApplicationContext();
        mHandler = new Handler();
        PlayGamesSdk.initialize(mAppContext);
        gamesSignInClient = PlayGames.getGamesSignInClient(mActivity);
    }

    void assertConfigured(String operation) {
        if (!mSetupDone) {
            String error = "GameHelper error: Operation attempted without setup: "
                    + operation
                    + ". The setup() method must be called before attempting any other operation.";
            logError(error);
            throw new IllegalStateException(error);
        }
    }


    /**
     * Performs setup on this GameHelper object. Call this from the onCreate()
     * method of your Activity. This will create the clients and do a few other
     * initialization tasks. Next, call @link{#onStart} from the onStart()
     * method of your Activity.
     *
     * @param listener
     *            The listener to be notified of sign-in events.
     */
    public void setup(GameHelperListener listener) {
        if (mSetupDone) {
            String error = "GameHelper: you cannot call GameHelper.setup() more than once!";
            logError(error);
            throw new IllegalStateException(error);
        }
        mListener = listener;
        gamesSignInClient.isAuthenticated().addOnCompleteListener(isAuthenticatedTask -> {
            boolean isAuthenticated =
                    (isAuthenticatedTask.isSuccessful() &&
                            isAuthenticatedTask.getResult().isAuthenticated());

            mSignedIn = isAuthenticated;
            if(isAuthenticated) {
                mListener.onSignInSucceeded();
            } else {
                mListener.onSignInFailed();
            }
        });
        mSetupDone = true;
    }

    /** Returns whether or not the user is signed in. */
    public boolean isSignedIn() {
        return mSignedIn;
    }

    /** Returns whether or not we are currently connecting */
    public boolean isConnecting() {
        return mConnecting;
    }

    public Player currentPlayer = null;

    public void getCurrentPlayer(Runnable callback) {
        PlayGames.getPlayersClient(mActivity).getCurrentPlayer().addOnCompleteListener(mTask -> {
            currentPlayer = mTask.getResult();
                    callback.run();
                }
        );
    }

    /** Call this method from your Activity's onStart(). */
    public void onStart(Activity act) {
        mActivity = act;
        mAppContext = act.getApplicationContext();

        debugLog("onStart");
        assertConfigured("onStart");

    }

    /** Call this method from your Activity's onStop(). */
    public void onStop() {
        debugLog("onStop");
        assertConfigured("onStop");
        mConnecting = false;
        mExpectingResolution = false;

        // let go of the Activity reference
        mActivity = null;
    }

    /** Enables debug logging */
    public void enableDebugLog(boolean enabled) {
        mDebugLog = enabled;
        if (enabled) {
            debugLog("Debug log enabled.");
        }
    }

    @Deprecated
    public void enableDebugLog(boolean enabled, String tag) {
        Log.w(TAG, "GameHelper.enableDebugLog(boolean,String) is deprecated. "
                + "Use GameHelper.enableDebugLog(boolean)");
        enableDebugLog(enabled);
    }

    /** Sign out and disconnect from the APIs. */
    public void signOut() {
    }

    void notifyListener(boolean success) {
        debugLog("Notifying LISTENER of sign-in "
                + (success ? "SUCCESS"
                : "FAILURE (no error)"));
        if (mListener != null) {
            if (success) {
                mListener.onSignInSucceeded();
            } else {
                mListener.onSignInFailed();
            }
        }
    }

    /**
     * Starts a user-initiated sign-in flow. This should be called when the user
     * clicks on a "Sign In" button. As a result, authentication/consent dialogs
     * may show up. At the end of the process, the GameHelperListener's
     * onSignInSucceeded() or onSignInFailed() methods will be called.
     */
    public void beginUserInitiatedSignIn() {
        debugLog("beginUserInitiatedSignIn: resetting attempt count.");
        mSignInCancelled = false;
        mConnectOnStart = true;
        gamesSignInClient.isAuthenticated().addOnCompleteListener(isAuthenticatedTask -> {
            boolean isAuthenticated =
                    (isAuthenticatedTask.isSuccessful() &&
                            isAuthenticatedTask.getResult().isAuthenticated());

            if (isAuthenticated) {
                // Continue with Play Games Services
                notifyListener(true);
            } else {
                gamesSignInClient.signIn().addOnCompleteListener(isAuthenticatedResult -> {
                    boolean isAuthenticated2 =
                            (isAuthenticatedResult.isSuccessful() &&
                                    isAuthenticatedResult.getResult().isAuthenticated());

                    // Continue with Play Games Services
                    notifyListener(isAuthenticated2);
                });
            }
        });

        debugLog("Starting USER-INITIATED sign-in flow.");

        // indicate that user is actively trying to sign in (so we know to
        // resolve
        // connection problems by showing dialogs)
        mUserInitiatedSignIn = true;
    }

    void debugLog(String message) {
        if (mDebugLog) {
            Log.d(TAG, "GameHelper: " + message);
        }
    }

    void logWarn(String message) {
        Log.w(TAG, "!!! GameHelper WARNING: " + message);
    }

    void logError(String message) {
        Log.e(TAG, "*** GameHelper ERROR: " + message);
    }

    // Not recommended for general use. This method forces the
    // "connect on start" flag
    // to a given state. This may be useful when using GameHelper in a
    // non-standard
    // sign-in flow.
    public void setConnectOnStart(boolean connectOnStart) {
        debugLog("Forcing mConnectOnStart=" + connectOnStart);
        mConnectOnStart = connectOnStart;
    }

    public LeaderboardsClient getLeaderboardClient() {
        return PlayGames.getLeaderboardsClient(mActivity);
    }

    public AchievementsClient getAchievementClient() {
        return PlayGames.getAchievementsClient(mActivity);
    }

    public void showLeaderboard() {
        PlayGames.getLeaderboardsClient(mActivity)
                .getAllLeaderboardsIntent()
                .addOnSuccessListener(intent -> mActivity.startActivityForResult(intent, IGoogleGamesProcessor.REQUEST_LEADERBOARD));
    }

    public void showAchievements() {
        PlayGames.getAchievementsClient(mActivity)
                .getAchievementsIntent()
                .addOnSuccessListener(intent -> mActivity.startActivityForResult(intent, IGoogleGamesProcessor.REQUEST_ACHIEVEMENTS));
    }
}

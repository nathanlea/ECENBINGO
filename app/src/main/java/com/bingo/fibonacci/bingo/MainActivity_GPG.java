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

package com.bingo.fibonacci.bingo;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.Player;
import com.google.android.gms.plus.Plus;
import com.google.example.games.basegameutils.BaseGameUtils;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Our main activity for the game.
 *
 * IMPORTANT: Before attempting to run this sample, please change
 * the package name to your own package name (not com.android.*) and
 * replace the IDs on res/values/ids.xml by your own IDs (you must
 * create a game in the developer console to get those IDs).
 *
 * This is a very simple game where the user selects "easy mode" or
 * "hard mode" and then the "gameplay" consists of inputting the
 * desired score (0 to 9999). In easy mode, you get the score you
 * request; in hard mode, you get half.
 *
 * @author Bruno Oliveira
 */
public class MainActivity_GPG extends FragmentActivity
        implements MainMenuFragment.Listener, WinFragment.Listener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, BingoFragment.Listener {

    BingoData bingoData;

    // Fragments
    MainMenuFragment mMainMenuFragment;
    BingoFragment mBingoFragment;
    WinFragment mWinFragment;

    FileInputStream fis;
    ObjectInputStream is;
    FileOutputStream fos;
    ObjectOutputStream os;
    Calendar startTime;

    // Client used to interact with Google APIs
    private GoogleApiClient mGoogleApiClient;

    // Are we currently resolving a connection failure?
    private boolean mResolvingConnectionFailure = false;

    // Has the user clicked the sign-in button?
    private boolean mSignInClicked = false;

    // Automatically start the sign-in flow when the Activity starts
    private boolean mAutoStartSignInFlow = true;

    // request codes we use when invoking an external activity
    private static final int RC_RESOLVE = 5000;
    private static final int RC_UNUSED = 5001;
    private static final int RC_SIGN_IN = 9001;

    // tag for debug logging
    final boolean ENABLE_DEBUG = true;
    final String TAG = "TanC";

    // playing on hard mode?
    boolean mNewGame = false;

    // achievements and scores we're pending to push to the cloud
    // (waiting for the user to sign in, for instance)
    AccomplishmentsOutbox mOutbox = new AccomplishmentsOutbox();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_gpg);

        // Create the Google API Client with access to Plus and Games
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Plus.API).addScope(Plus.SCOPE_PLUS_LOGIN)
                .addApi(Games.API).addScope(Games.SCOPE_GAMES)
                .build();

        // create fragments
        mMainMenuFragment = new MainMenuFragment();
        mBingoFragment = new BingoFragment();
        mWinFragment = new WinFragment();

        // listen to fragment events
        mMainMenuFragment.setListener(this);
        mBingoFragment.setListener(this);
        mWinFragment.setListener(this);

        // add initial fragment (welcome fragment)
        getSupportFragmentManager().beginTransaction().add(R.id.fragment_container,
                mMainMenuFragment).commit();

        // IMPORTANT: if this Activity supported rotation, we'd have to be
        // more careful about adding the fragment, since the fragment would
        // already be there after rotation and trying to add it again would
        // result in overlapping fragments. But since we don't support rotation,
        // we don't deal with that for code simplicity.

        // load outbox from file
        mOutbox.loadLocal(this);

        readObjectIn(); //Read in the object Data

        startTime = Calendar.getInstance();
        long a = startTime.getTimeInMillis();
        startTime.set(Calendar.ZONE_OFFSET, -21600000);
        startTime.set(Calendar.HOUR, 7);
        startTime.set(Calendar.MINUTE, 00);
        startTime.set(Calendar.SECOND, 0);
}

    // Switch UI to the given fragment
    void switchToFragment(Fragment newFrag) {
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, newFrag)
                .commit();
    }

    private boolean isSignedIn() {
        return (mGoogleApiClient != null && mGoogleApiClient.isConnected());
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart(): connecting");
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop(): disconnecting");
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onStartGameRequested(boolean newgame) {
        startGame(newgame);
    }

    @Override
    public void onShowAchievementsRequested() {
        if (isSignedIn()) {
            startActivityForResult(Games.Achievements.getAchievementsIntent(mGoogleApiClient),
                    RC_UNUSED);
        } else {
            BaseGameUtils.makeSimpleDialog(this, getString(R.string.achievements_not_available)).show();
        }
    }

    @Override
    public void onShowLeaderboardsRequested() {
        if (isSignedIn()) {
            startActivityForResult(Games.Leaderboards.getAllLeaderboardsIntent(mGoogleApiClient),
                    RC_UNUSED);
        } else {
            BaseGameUtils.makeSimpleDialog(this, getString(R.string.leaderboards_not_available)).show();
        }
    }

    public void onBingo() {
        Calendar calendar = Calendar.getInstance();

        long start = startTime.getTimeInMillis();
        long end = calendar.getTimeInMillis();

        if( ((end-start)/1000) <= 4000 && ((end-start)/1000) > 200) {
            bingoData.increaseBingoCount();
            bingoData.increaseTimePlayed(end-start);
            bingoData.addBingo(end);

            mWinFragment.setFinalTime(end - start);

            // check for achievements
            checkForAchievements(bingoData.getTime(), bingoData.getNumBingo());

            // update leaderboards
            updateLeaderboards( (end-start),  bingoData.bingoThisWeek(), bingoData.bingoThisMonth(), bingoData.bingoThisSemester() );


            // push those accomplishments to the cloud, if signed in
            pushAccomplishments();
            pushLeaderboard();
        }

        switchToFragment(mWinFragment);
    }

    /**
     * Start gameplay. This means updating some status variables and switching
     * to the "gameplay" screen (the screen where the user types the score they want).
     *
     * @param newgame whether to start gameplay in "hard mode".
     */
    void startGame(boolean newgame) {
        mNewGame = newgame;
        switchToFragment(mBingoFragment);
    }

    /**
     * Check for achievements and unlock the appropriate ones.
     *
     * @param numberOfBingo
     * @param playedTime
     */
    void checkForAchievements(long playedTime, long numberOfBingo) {
        // Check if each condition is met; if so, unlock the corresponding
        // achievement.
        long a = numberOfBingo;
        long b = playedTime;

        //TODO NHL
        if (numberOfBingo == 1) {
            mOutbox.mFirstBingoAchievement = true;
            achievementToast(getString(R.string.first_bingo));
            unlockAchievement(R.string.achievement_1Bingo, "1st Bingo");
        }
        if (numberOfBingo == 10) {
            mOutbox.m10BingoAchievement = true;
            achievementToast(getString(R.string.ten_bingo));
            unlockAchievement(R.string.achievement_10Bingo, "10 Bingos");
        }
        if (numberOfBingo == 100) {
            mOutbox.m100BingoAchievement = true;
            achievementToast(getString(R.string.hundred_bingo));
            unlockAchievement(R.string.achievement_100Bingo, "100 Bingos");
        }
        if (playedTime >= 36000000L ) {
            mOutbox.m10hoursofBingoAchievement = true;
            achievementToast(getString(R.string.ten_hours_bingo));
            unlockAchievement(R.string.achievement_10hrofBingo, "You have spend more than 10 hours on Bingo");
        }
        if (playedTime >= 360000000L ) {
            mOutbox.m100hoursofBingoAchievement = true;
            achievementToast(getString(R.string.hundred_hour_bingo));
            unlockAchievement(R.string.achievement_100hourofBingo, "You have spend more than 100 hours on Bingo :)");
        }
        mOutbox.mBoredSteps++;
    }

    void unlockAchievement(int achievementId, String fallbackString) {
        if (isSignedIn()) {
            Games.Achievements.unlock(mGoogleApiClient, getString(achievementId));
        } else {
            Toast.makeText(this, getString(R.string.achievement) + ": " + fallbackString,
                    Toast.LENGTH_LONG).show();
        }
    }

    void achievementToast(String achievement) {
        // Only show toast if not signed in. If signed in, the standard Google Play
        // toasts will appear, so we don't need to show our own.
        if (!isSignedIn()) {
            Toast.makeText(this, getString(R.string.achievement) + ": " + achievement,
                    Toast.LENGTH_LONG).show();
        }
    }

    void pushAccomplishments() {
        if (!isSignedIn()) {
            // can't push to the cloud, so save locally
            mOutbox.saveLocal(this);
            return;
        }
        if (mOutbox.mFirstBingoAchievement) {
            Games.Achievements.unlock(mGoogleApiClient, getString(R.string.achievement_10Bingo));
            mOutbox.mFirstBingoAchievement = false;
        }
        if (mOutbox.m10BingoAchievement) {
            Games.Achievements.unlock(mGoogleApiClient, getString(R.string.achievement_100Bingo));
            mOutbox.m10BingoAchievement = false;
        }
        if (mOutbox.m100BingoAchievement) {
            Games.Achievements.unlock(mGoogleApiClient, getString(R.string.achievement_100Bingo));
            mOutbox.m100BingoAchievement = false;
        }
        if (mOutbox.m10hoursofBingoAchievement) {
            Games.Achievements.unlock(mGoogleApiClient, getString(R.string.achievement_10hrofBingo));
            mOutbox.m10hoursofBingoAchievement = false;
        }
        if (mOutbox.m100hoursofBingoAchievement) {
            Games.Achievements.unlock(mGoogleApiClient, getString(R.string.achievement_100hourofBingo));
            mOutbox.m100hoursofBingoAchievement = false;
        }
        mOutbox.saveLocal(this);
    }

    void pushLeaderboard() {
        if (!isSignedIn()) {
            // can't push to the cloud, so save locally
            mOutbox.saveLocal(this);
            return;
        }
        Games.Leaderboards.submitScore(mGoogleApiClient,
                getString(R.string.leaderboard_daily), mOutbox.bingoTime);
        Games.Leaderboards.submitScore(mGoogleApiClient,
                getString(R.string.leaderboard_weekly), mOutbox.bingoThisWeek);
        Games.Leaderboards.submitScore(mGoogleApiClient,
                getString(R.string.leaderboard_monthly), mOutbox.bingoThisMonth);
        Games.Leaderboards.submitScore(mGoogleApiClient,
                getString(R.string.leaderboard_semester), mOutbox.bingoThisSemester);
    }

    /**
     * Update leaderboards with the user's score.
     *
     * The score the user got.
     */
    void updateLeaderboards(long bingoTime, int bingoWeek, int bingoMonth, int bingoSemester) {
        //TODO NHL
        if (mOutbox.bingoTime > bingoTime) {
            mOutbox.bingoTime = bingoTime;
        }
        if (mOutbox.bingoThisWeek < bingoWeek) {
            mOutbox.bingoThisWeek = bingoWeek;
        }
        if (mOutbox.bingoThisMonth < bingoMonth) {
            mOutbox.bingoThisMonth = bingoMonth;
        }
        if (mOutbox.bingoThisSemester < bingoSemester) {
            mOutbox.bingoThisSemester = bingoSemester;
        }
    }

    @Override
    public void onWinScreenDismissed() {
        switchToFragment(mMainMenuFragment);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (requestCode == RC_SIGN_IN) {
            mSignInClicked = false;
            mResolvingConnectionFailure = false;
            if (resultCode == RESULT_OK) {
                mGoogleApiClient.connect();
            } else {
                BaseGameUtils.showActivityResultError(this, requestCode, resultCode, R.string.signin_other_error);
            }
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "onConnected(): connected to Google APIs");
        // Show sign-out button on main menu
        mMainMenuFragment.setShowSignInButton(false);

        // Show "you are signed in" message on win screen, with no sign in button.
        mWinFragment.setShowSignInButton(false);

        // Set the greeting appropriately on main menu
        Player p = Games.Players.getCurrentPlayer(mGoogleApiClient);
        String displayName;
        if (p == null) {
            Log.w(TAG, "mGamesClient.getCurrentPlayer() is NULL!");
            displayName = "???";
        } else {
            displayName = p.getDisplayName();
        }
        mMainMenuFragment.setGreeting("Hello, " + displayName);


        // if we have accomplishments to push, push them
        if (!mOutbox.isEmpty()) {
            //pushAccomplishments();
          //TODO WHAT SHOULD I DO HERE?
          //  Toast.makeText(this, getString(R.string.your_progress_will_be_uploaded),
          //          Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "onConnectionSuspended(): attempting to connect");
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed(): attempting to resolve");
        if (mResolvingConnectionFailure) {
            Log.d(TAG, "onConnectionFailed(): already resolving");
            return;
        }

        if (mSignInClicked || mAutoStartSignInFlow) {
            mAutoStartSignInFlow = false;
            mSignInClicked = false;
            mResolvingConnectionFailure = true;
            if (!BaseGameUtils.resolveConnectionFailure(this, mGoogleApiClient, connectionResult,
                    RC_SIGN_IN, getString(R.string.signin_other_error))) {
                mResolvingConnectionFailure = false;
            }
        }

        // Sign-in failed, so show sign-in button on main menu
        mMainMenuFragment.setGreeting(getString(R.string.signed_out_greeting));
        mMainMenuFragment.setShowSignInButton(true);
        mWinFragment.setShowSignInButton(true);
    }

    @Override
    public void onSignInButtonClicked() {
        // Check to see the developer who's running this sample code read the instructions :-)
        // NOTE: this check is here only because this is a sample! Don't include this
        // check in your actual production app.
        if(!BaseGameUtils.verifySampleSetup(this, R.string.app_id,
                R.string.achievement_first, R.string.daily_leaderboard)) {
            Log.w(TAG, "*** Warning: setup problems detected. Sign in may not work!");
        }

        // start the sign-in flow
        mSignInClicked = true;
        mGoogleApiClient.connect();
    }

    @Override
    public void onSignOutButtonClicked() {
        mSignInClicked = false;
        Games.signOut(mGoogleApiClient);
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }

        mMainMenuFragment.setGreeting(getString(R.string.signed_out_greeting));
        mMainMenuFragment.setShowSignInButton(true);
        mWinFragment.setShowSignInButton(true);
    }

    class AccomplishmentsOutbox {
        boolean mFirstBingoAchievement = false;
        boolean m10BingoAchievement = false;
        boolean m100BingoAchievement = false;
        boolean m10hoursofBingoAchievement = false;
        boolean m100hoursofBingoAchievement = false;
        int mBoredSteps = 0;
        long bingoTime = Long.MAX_VALUE;
        int bingoThisWeek = 0;
        int bingoThisMonth = 0;
        int bingoThisSemester = 0;

        boolean isEmpty() {
            return !mFirstBingoAchievement && !m10BingoAchievement && !m100BingoAchievement &&
                    !m10hoursofBingoAchievement && !m100hoursofBingoAchievement && mBoredSteps == 0
                    && bingoTime < 99999999 && bingoThisWeek < 0 && bingoThisMonth < 0 && bingoThisSemester < 0;
        }

        public void saveLocal(Context ctx) {
            /* TODO: This is left as an exercise. To make it more difficult to cheat,
             * this data should be stored in an encrypted file! And remember not to
             * expose your encryption key (obfuscate it by building it from bits and
             * pieces and/or XORing with another string, for instance). */
        }

        public void loadLocal(Context ctx) {
            /* TODO: This is left as an exercise. Write code here that loads data
             * from the file you wrote in saveLocal(). */
        }
    }

    @Override
    public void onWinScreenSignInClicked() {
        mSignInClicked = true;
        mGoogleApiClient.connect();
    }

    public void writeObjectOut( ) {
        try {
            fos = getApplicationContext().openFileOutput("gamedata", Context.MODE_PRIVATE);
            os = new ObjectOutputStream(fos);
            os.writeObject(bingoData);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try { os.close();   fos.close(); }
            catch (Exception e) { e.printStackTrace(); }
        }
    }

    public void readObjectIn( ) {

        try {
            fis = (getApplicationContext()).openFileInput("gamedata");
            is = new ObjectInputStream(fis);
            bingoData = (BingoData) is.readObject();
        } catch (IOException e) {
            e.printStackTrace();
            bingoData = new BingoData();  //TODO This is not the best way to handle this but for now it works
        } catch (Exception e) {
            bingoData = new BingoData();
            e.printStackTrace();
        } finally {
            try { is.close();   fis.close(); }
            catch (Exception e) { e.printStackTrace(); }
        }
    }

    @Override
    public void onBackPressed() {
        writeObjectOut();
        super.onBackPressed();
    }
}

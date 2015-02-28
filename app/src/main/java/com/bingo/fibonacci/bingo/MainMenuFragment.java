/* Copyright (C) 2013 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.bingo.fibonacci.bingo;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

/**
 * Fragment with the main menu for the game. The main menu allows the player
 * to choose a gameplay mode (Easy or Hard), and click the buttons to
 * show view achievements/leaderboards.
 *
 * @author Bruno Oliveira (Google)
 *
 */
public class MainMenuFragment extends Fragment implements OnClickListener, View.OnLongClickListener {
    String mGreeting = "Hello, anonymous user (not signed in)";


    public interface Listener {
        public void onStartGameRequested(boolean newgame);
        public void onShowAchievementsRequested();
        public void onShowLeaderboardsRequested();
        public void onSignInButtonClicked();
        public void onSignOutButtonClicked();
    }

    Listener mListener = null;
    boolean mShowSignIn = true;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_mainmenu, container, false);
        final int[] CLICKABLES = new int[] {
                R.id.start_game_button,
                R.id.show_achievements_button, R.id.show_leaderboards_button,
                R.id.sign_in_button, R.id.sign_out_button
        };
        for (int i : CLICKABLES) {
            v.findViewById(i).setOnClickListener(this);
        }
        v.findViewById( R.id.title_bar).setOnLongClickListener(this);
        return v;
    }

    public void setListener(Listener l) {
        mListener = l;
    }

    @Override
    public void onStart() {
        super.onStart();
        updateUi();
    }

    public void setGreeting(String greeting) {
        mGreeting = greeting;
        updateUi();
    }

    void updateUi() {
        if (getActivity() == null) return;
        TextView tv = (TextView) getActivity().findViewById(R.id.hello);
        if (tv != null) tv.setText(mGreeting);
        getActivity().findViewById(R.id.sign_in_bar).setVisibility(mShowSignIn ?
                View.VISIBLE : View.GONE);
        getActivity().findViewById(R.id.sign_out_bar).setVisibility(mShowSignIn ?
                View.GONE : View.VISIBLE);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
        case R.id.start_game_button:
            mListener.onStartGameRequested(true);
            break;
        case R.id.show_achievements_button:
            mListener.onShowAchievementsRequested();
            break;
        case R.id.show_leaderboards_button:
            mListener.onShowLeaderboardsRequested();
            break;
        case R.id.sign_in_button:
            mListener.onSignInButtonClicked();
            break;
        case R.id.sign_out_button:
            mListener.onSignOutButtonClicked();
            break;
        }
    }

    @Override
    public boolean onLongClick(View v) {

        switch (v.getId()) {
            case R.id.title_bar:
                final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                // Get the layout inflater
                final LayoutInflater inflater = getActivity().getLayoutInflater();

                // Inflate and set the layout for the dialog
                // Pass null as the parent view because its going in the dialog layout
                builder.setView(inflater.inflate(R.layout.suggest_dialog, null))
                        // Add action buttons
                        .setPositiveButton(R.string.send, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                final EditText suggestion = (EditText) ((AlertDialog)dialog).findViewById(R.id.suggestion);
                                Intent sendIntent = new Intent(android.content.Intent.ACTION_SEND);
                                sendIntent.setType("message/rfc822");
                                sendIntent.putExtra(android.content.Intent.EXTRA_EMAIL,new String[] { "fibonacci.studios@gmail.com"});
                                sendIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Suggestion for ECEN BINGO");
                                sendIntent.putExtra(Intent.EXTRA_TEXT, suggestion.getText());
                                startActivity(Intent.createChooser(sendIntent, "Send your suggestion with:"));
                            }
                        })
                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
                builder.create().show();
                return true;
        }
        return false;
    }


    public void setShowSignInButton(boolean showSignIn) {
        mShowSignIn = showSignIn;
        updateUi();
    }
}

package com.allenmentoring.allenmentoring;

/**
 * Created by Allen on 11/13/14.
 */

import android.app.AlertDialog;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;

import java.util.List;

public class FriendsFragment extends ListFragment {

        public static final String TAG = FriendsFragment.class.getSimpleName();

        // Create a variable to store the list of users you download from Parse
        protected List<ParseUser> mFriends;

        // Create parse relation variable
        protected ParseRelation<ParseUser> mFriendsRelation;

        // store variable for current user
        protected ParseUser mCurrentUser;

        // onCreateView is what happens when the fragment is drawn
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
           // Like setContentView for Main activity
            View rootView = inflater.inflate(R.layout.fragment_friends, container, false);
            return rootView;
        }

        @Override
        public void onResume() {
            super.onResume();

            mCurrentUser = ParseUser.getCurrentUser();
            mFriendsRelation = mCurrentUser.getRelation(ParseConstants.KEY_FRIENDS_RELATION);

            // Taken from the addFriendsCheckmark method in EditFriendsActivity. Refer to comments there
            getActivity().setProgressBarIndeterminateVisibility(true);
            ParseQuery<ParseUser> query = mFriendsRelation.getQuery();
            // Alphabetize users
            query.addAscendingOrder(ParseConstants.KEY_USERNAME);

            query.findInBackground(new FindCallback<ParseUser>() {
                @Override
                // The friends array list represents all the users in the Parse database within the column "friendsRelation"
                public void done(List<ParseUser> friends, ParseException e) {
                    getActivity().setProgressBarIndeterminateVisibility(false);


                    if (e == null) {
                        mFriends = friends;

                        // Now you have to loop through the array and store the data into the usernames
                        String[] usernames = new String[mFriends.size()];
                        int i = 0;
                        for (ParseUser user : mFriends) {
                            usernames[i] = user.getUsername();
                            i++;
                        }
                        // Now adapt your data into a checked list adapter. In a fragment, the context is called in a special way - by asking the getListView to tell us its context through getListView().getContext().
                        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getListView().getContext(), android.R.layout.simple_list_item_1, usernames);
                        setListAdapter(adapter);
                    }
                    else {
                        Log.e(TAG, e.getMessage());
                        AlertDialog.Builder builder = new AlertDialog.Builder(getListView().getContext());
                        builder.setMessage(e.getMessage())
                                .setTitle(R.string.error_title)
                                .setPositiveButton(android.R.string.ok, null);

                        AlertDialog dialog = builder.create();
                        dialog.show();
                    }
                }

            });
        }

    }


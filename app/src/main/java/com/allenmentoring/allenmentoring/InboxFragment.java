package com.allenmentoring.allenmentoring;

/**
 * Created by Allen on 11/13/14.
 */

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class InboxFragment extends ListFragment {

        // onCreateView is what happens when the fragment is drawn
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
           // Like setContentView for Main activity
            View rootView = inflater.inflate(R.layout.fragment_inbox, container, false);
            return rootView;
        }
    }


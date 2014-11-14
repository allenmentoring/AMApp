package com.allenmentoring.allenmentoring;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;


public class RecipientsActivity extends ListActivity {
    public static final String TAG = RecipientsActivity.class.getSimpleName();


    // Create a variable to store the list of users you download from Parse
    protected List<ParseUser> mFriends;

    // Create parse relation variable
    protected ParseRelation<ParseUser> mFriendsRelation;

    // store variable for current user
    protected ParseUser mCurrentUser;

    // store variable for send button in menu
    protected MenuItem mSendMenuItem;

    // store variable for Uri that will come in with the intent
    protected Uri mMediaUri;
    protected String mFileType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_recipients);

        getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        mMediaUri = getIntent().getData();
        mFileType = getIntent().getExtras().getString(ParseConstants.KEY_FILE_TYPE);

    }
    @Override
    public void onResume() {
        super.onResume();

        mCurrentUser = ParseUser.getCurrentUser();
        mFriendsRelation = mCurrentUser.getRelation(ParseConstants.KEY_FRIENDS_RELATION);

        setProgressBarIndeterminateVisibility(true);

        // Taken from the addFriendsCheckmark method in EditFriendsActivity. Refer to comments there

        ParseQuery<ParseUser> query = mFriendsRelation.getQuery();
        // Alphabetize users
        query.addAscendingOrder(ParseConstants.KEY_USERNAME);

        query.findInBackground(new FindCallback<ParseUser>() {
            @Override
            // The friends array list represents all the users in the Parse database within the column "friendsRelation"
            public void done(List<ParseUser> friends, ParseException e) {
               setProgressBarIndeterminateVisibility(false);


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
                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(getListView().getContext(), android.R.layout.simple_list_item_checked, usernames);
                    setListAdapter(adapter);
                }
                else {
                    Log.e(TAG, e.getMessage());
                    AlertDialog.Builder builder = new AlertDialog.Builder(RecipientsActivity.this);
                    builder.setMessage(e.getMessage())
                            .setTitle(R.string.error_title)
                            .setPositiveButton(android.R.string.ok, null);

                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            }

        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.recipients, menu);
        // Select first item in menu (send) and set it to mSendMenuItem
        mSendMenuItem = menu.getItem(0);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }

        if (id == R.id.action_send) {
            ParseObject message = createMessage();

            if (message == null) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("There was an error with the file selected. Please select a different file.")
                        .setTitle("We're Sorry!")
                        .setPositiveButton(android.R.string.ok, null);
                AlertDialog dialog = builder.create();
                dialog.show();
            }
            else {
                send(message);
                finish();
            }

            return true;
        }

        return super.onOptionsItemSelected(item);
    }



    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        // Check how many items are selected. If it's 0, hide send button, if more show.
        if (l.getCheckedItemCount() > 0) {
            mSendMenuItem.setVisible(true);
        }
        else {
            mSendMenuItem.setVisible(false);
        }
    }

    protected ParseObject createMessage() {
        // 1. Create new object in Parse "message class" database
        ParseObject message = new ParseObject(ParseConstants.CLASS_MESSAGES);
        // 2. Start adding fields to that class in (Key, Value) format. The key is the field name, the value is what you want to store there.
        message.put(ParseConstants.KEY_SENDER_ID, ParseUser.getCurrentUser().getObjectId());
        message.put(ParseConstants.KEY_SENDER_NAME, ParseUser.getCurrentUser().getUsername());
        message.put(ParseConstants.KEY_RECIPIENT_IDS, getRecipientIds());
        message.put(ParseConstants.KEY_FILE_TYPE, mFileType);

        byte[] fileBytes = FileHelper.getByteArrayFromFile(this, mMediaUri);

        if (fileBytes == null) {
            return null;
        }
        else {
            // Got most of these methods from the two helper files from Treehouse. Need to convert the file into a byte[] to upload to parse.
            if (mFileType.equals(ParseConstants.TYPE_IMAGE)) {
                fileBytes = FileHelper.reduceImageForUpload(fileBytes);
            }

            String fileName = FileHelper.getFileName(this, mMediaUri, mFileType);
            ParseFile file = new ParseFile(fileName, fileBytes);
            message.put(ParseConstants.KEY_FILE, file);
            return message;
        }


    }

    protected ArrayList<String> getRecipientIds() {
        ArrayList<String> recipientsIds = new ArrayList<String>();
        for (int i = 0; i< getListView().getCount(); i++) {
            if (getListView().isItemChecked(i)) {
                recipientsIds.add(mFriends.get(i).getObjectId());
            }
        }
        return recipientsIds;
    }

    protected void send(ParseObject message) {
       message.saveInBackground(new SaveCallback() {
           @Override
           public void done(ParseException e) {
               if (e == null) {
                   // success
                   Toast.makeText(RecipientsActivity.this, "Message sent!", Toast.LENGTH_LONG).show();
               }
               else {
                   AlertDialog.Builder builder = new AlertDialog.Builder(RecipientsActivity.this);
                   builder.setMessage("There was an error with sending the message. Please try again.")
                           .setTitle("We're Sorry!")
                           .setPositiveButton(android.R.string.ok, null);
                   AlertDialog dialog = builder.create();
                   dialog.show();
               }

           }
       });
    }
}

/**
 * 
 */
package net.bible.android.view.activity.usernote;

import java.util.ArrayList;
import java.util.List;

import net.bible.android.activity.R;
import net.bible.android.control.ControlFactory;
import net.bible.android.control.page.CurrentPageManager;
import net.bible.android.control.usernote.UserNote;
import net.bible.android.view.activity.base.Dialogs;
import net.bible.android.view.activity.base.ListActivityBase;
import net.bible.service.db.usernote.UserNoteDto;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

/**
 * Show a list of existing User Notes and allow view/edit/delete
 * 
 * @see gnu.lgpl.License for license details.<br>
 *      The copyright to this program is held by it's authors.
 * @author John D. Lewis [balinjdl at gmail dot com]
 * @author Martin Denham [mjdenham at gmail dot com]
 */
public class UserNotes extends ListActivityBase {
	private static final String TAG = "UserNotes";

	static final String USERNOTE_EXTRA = "usernote";

	private UserNote usernoteControl;
	
	// the document list
	private List<UserNoteDto> usernoteList = new ArrayList<UserNoteDto>();

	private static final int LIST_ITEM_TYPE = android.R.layout.simple_list_item_2;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        usernoteControl = ControlFactory.getInstance().getUserNoteControl();
        
       	initialiseView();
    }

    private void initialiseView() {
    	loadUserNoteList();
    	
    	// prepare the document list view
    	ArrayAdapter<UserNoteDto> usernoteArrayAdapter = new UserNoteItemAdapter(this, LIST_ITEM_TYPE, usernoteList);
    	setListAdapter(usernoteArrayAdapter);
    	
    	registerForContextMenu(getListView());
    }

    @Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
    	try {
    		usernoteSelected(usernoteList.get(position));
    	} catch (Exception e) {
    		Log.e(TAG, "document selection error", e);
    		showErrorMsg(R.string.error_occurred);
    	}
	}

    @Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.usernote_context_menu, menu);
	} 

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		super.onContextItemSelected(item);
        AdapterContextMenuInfo menuInfo = (AdapterContextMenuInfo) item.getMenuInfo();
        UserNoteDto usernote = usernoteList.get(menuInfo.position);
		if (usernote!=null) {
			switch (item.getItemId()) {
			case (R.id.delete):
				delete(usernote);
				return true;
			}
		}
		return false; 
	}

    @Override 
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
    	loadUserNoteList();
    }

	private void delete(UserNoteDto usernote) {
		usernoteControl.deleteUserNote(usernote);
		loadUserNoteList();
	}

	private void loadUserNoteList() {
    	usernoteList.clear();
    	usernoteList.addAll( usernoteControl.getAllUserNotes() );	
    }

    /** user selected a document so download it
     * 
     * @param document
     */
    private void usernoteSelected(UserNoteDto usernote) {
    	Log.d(TAG, "User Note selected:"+usernote.getKey());
    	try {
        	if (usernote!=null) {
        		CurrentPageManager.getInstance().getCurrentBible().setKey(usernote.getKey());
	        	Intent handlerIntent = new Intent(this, UserNoteEdit.class);
        		startActivity(handlerIntent);
        	}
    	} catch (Exception e) {
    		Log.e(TAG, "Error on attempt to show note", e);
    		Dialogs.getInstance().showErrorMsg(R.string.error_occurred);
    	}
    }
}

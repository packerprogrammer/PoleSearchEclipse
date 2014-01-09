package com.glps.polesearch;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class PoleAdapter extends CursorAdapter{
	
	private PoleDBAdapter dbAdapter = null;

	@SuppressWarnings("deprecation")
	public PoleAdapter(Context context, Cursor c) {
		super(context, c);
		dbAdapter = new PoleDBAdapter(context);
		dbAdapter.open();
	}

	@Override
	public void bindView(View arg0, Context arg1, Cursor arg2) {
		String item = createItem(arg2);
		((TextView) arg0).setText(item);
		
	}

	@Override
	public View newView(Context arg0, Cursor arg1, ViewGroup arg2) {
		final LayoutInflater inflater = LayoutInflater.from(arg0);
        final TextView view = (TextView) inflater.inflate(R.layout.list_item, arg2, false);
        
        String item = createItem(arg1);
        view.setText(item);
        return view;
	}
	 public Cursor runQueryOnBackgroundThread(CharSequence constraint)
	    {
	        Cursor currentCursor = null;
	        
	        if (getFilterQueryProvider() != null)
	        {
	            return getFilterQueryProvider().runQuery(constraint);
	        }
	        
	        String args = "";
	        
	        if (constraint != null)
	        {
	            args = constraint.toString();       
	        }
	 
	        currentCursor = dbAdapter.getPoleCursor(args);
	 
	        return currentCursor;
	    }
	 private String createItem(Cursor cursor)
	    {
	        String item = cursor.getString(1);       
	        return item;
	    }
	    
	    public void close()
	    {
	        dbAdapter.close();
	    }
	

}

package net.cs76.projects.nPuzzle70857251;

import java.lang.reflect.Field;
import net.cs76.projects.nPuzzle70857251.R;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

/*
 *  *** NOTE: Heavily Based on Dans code example from class ***
 * ********************************* 
 * Written by: Dermot Duncan 	     
 * 	    HU ID: 70857251      		
 *	    Email: dermduncan@gmail.com 
 * *********************************
 *  */
public class ImageAdapter extends BaseAdapter {
	//private static final String LOG_TAG = "debugger";
	// a list of resource IDs for the images we want to display
	private Integer[] images;

	// a context so we can later create a view within it
	private Context myContext;
	
	// store a cache of resized bitmaps
	// Note: we're not managing the cache size to ensure it doesn't 
	// exceed any maximum memory usage requirements
	private Bitmap[] cache;

	// Constructor
	public ImageAdapter(Context c) {

		myContext = c;

		// Dynamically figure out which images we've imported
		// into the drawable folder, so we don't have to manually
		// type each image in to a fixed array.
		
		// obtain a list of all of the objects in the R.drawable class
		Field[] list = R.drawable.class.getFields();
		
		int count = 0;
		int index = 0;

		// We first need to figure out how many of our images we have before
		// we can request the memory for an array of integers to hold their contents.

		// loop over all of the fields in the R.drawable class
		for(int i=0; i < list.length; i++)
			// if the name starts with puzzle_ then we have one of our images!
			if(list[i].getName().startsWith("puzzle_")) count++;

		// We now know how many images we have. Reserve the memory for an 
		// array of integers with length 'count' and initialize our cache.
		images = new Integer[count];
		cache = new Bitmap[count];

		// Next, (unsafely) try to get the values of each of those fields
		// into the images array.
		try 
		{
			for(int i=0; i < list.length; i++)
			{
				// Confirm the item we're trying to check is not null
				if(list[i] != null)
				{
					// Confirm the name is not null
					if (list[i].getName() != null)
					{
						if (list[i].getName().startsWith("puzzle_"))
							images[index++] = list[i].getInt(null);
					}	
				}
			}
		} catch (IllegalAccessException e) {
            // Should not happen.
			throw new IllegalArgumentException(R.drawable.class + " not accessible");
		}

	}

	@Override
	// the number of items in the adapter
	public int getCount() {
		return images.length;
	}

	@Override
	// get the item at the specified position
	public Object getItem(int position) {
		return cache[position];
	}

	@Override
	// return the resource ID of the item at the current position
	public long getItemId(int position) {
		return images[position];
	}

	// create a new ImageView when requested
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		
		// we've been asked for an ImageView at a specific position. If
		// one doesn't already exist (ie, convertView is null) then we must create
		// one. Otherwise we can pass it convertView or a recycled view
		// that's been passed to us.
		
		ImageView imgView;
		
		if(convertView == null) {

			// create a new view
			imgView = new ImageView(myContext);
			imgView.setLayoutParams(new GridView.LayoutParams(100,100));

		} else {
	
			// recycle an old view (it might have old thumbs in it!)
			imgView = (ImageView) convertView;
	
		}

		// see if we've stored a resized thumb in cache
		if(cache[position] == null) {
			
			// create a new Bitmap that stores a resized
			// version of the image we want to display. 	
			Bitmap thumb = ImageHelper.getOptimizedBitmap(myContext, images[position], 100, 100);
				
			// store the resized image in a cache so we don't have to re-generate it
			cache[position] = thumb;
				
		}
	
		// use the resized image we have in the cache
		imgView.setImageBitmap(cache[position]);

		return imgView;
	}	
}

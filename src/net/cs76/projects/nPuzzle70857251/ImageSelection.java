package net.cs76.projects.nPuzzle70857251;

import net.cs76.projects.nPuzzle70857251.ImageAdapter;
import net.cs76.projects.nPuzzle70857251.R;
import net.cs76.projects.nPuzzle70857251.GamePlay;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.AdapterView.OnItemClickListener;

/*
 * Home activity which allows the user to select an image to use as the puzzle
 * ******************************** 
 * Written by: Dermot Duncan 	     
 * 	    HU ID: 70857251      		
 *	    Email: dermduncan@gmail.com 
 * *********************************
 */
public class ImageSelection extends Activity implements OnItemClickListener{
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        // find our grid and assign our new ImageAdapter
        // class as the adapter for it, along with an
        // onItemClickListener.
        // See: ImageAdapter.java
        GridView grid = (GridView) findViewById(R.id.gridview);

        grid.setAdapter(new ImageAdapter(this));
        grid.setOnItemClickListener(this);
    }
    
    /* When an item has been clicked we want to show
	 * that particular image in a new activity made
	 * to just show one large image.
	 */
    public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
    	
    	// create the Intent to open our ShowImage activity.
    	Intent i = new Intent(this, GamePlay.class);
   
    	// pass a key:value pair into the 'extra' bundle for
    	// the intent so the activity is made aware which
    	// photo was selected.
    	i.putExtra("imageToDisplay", id);

    	// start our activity
    	startActivity(i);
    }
}
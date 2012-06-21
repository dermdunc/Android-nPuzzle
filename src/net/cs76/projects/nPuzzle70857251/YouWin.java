package net.cs76.projects.nPuzzle70857251;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

/*
 * Success activity when a user has successfully completed a puzzle
 * ********************************* 
 * Written by: Dermot Duncan 	     
 * 	    HU ID: 70857251      		
 *	    Email: dermduncan@gmail.com 
 * *********************************
 */
public class YouWin extends Activity implements OnClickListener{

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.success);

        // retrieve the set of data passed to us by the Intent
        Bundle extras = getIntent().getExtras();

        // and retrieve the imageToDisplay ID from the extras bundle
        int resource = (int)extras.getInt("imageToDisplay");
        int numTurns = (int)extras.getInt("numOfTurns");
        
        // Retrieve and update the success textview witht he number of turns it took to solve
        TextView successTxtView = (TextView)findViewById(R.id.successTxt);
        successTxtView.setText("\t\t\t\tSuccess!\n\nYou completed the puzzle in " + numTurns + " turns");

        // Retrieve the screen width and height which will be used to calculate the image sampling size
     	int screenWidth = this.getResources().getDisplayMetrics().widthPixels;
     	int screenHeight = this.getResources().getDisplayMetrics().heightPixels;
        Bitmap _selectedImg = ImageHelper.getOptimizedBitmap(this, resource, screenWidth, screenHeight);
        
        // Retrieve the image view from the View and set its Bitmap as the selected image
        ImageView img = (ImageView)findViewById(R.id.success);
		img.setImageBitmap(_selectedImg);
		
		Button btn = (Button)findViewById(R.id.successBtn);
        // close the Activity when a user taps/clicks on the button.
		btn.setOnClickListener(this);

	}
	
	@Override
	public void onClick(View v) {
		// create the Intent to open our ImageSelection activity.
    	Intent i = new Intent(YouWin.this, ImageSelection.class);
    	// start our activity
    	startActivity(i);
	}

}

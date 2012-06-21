package net.cs76.projects.nPuzzle70857251;

import net.cs76.projects.nPuzzle70857251.R;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

/* ShowImage.java
 * A new activity that accepts, via the Intent bundle,
 * an ID representing the image to be used for the puzzle. Tiles the image
 * and creates a table view for the user to interact with
 * ********************************* 
 * Written by: Dermot Duncan 	     
 * 	    HU ID: 70857251      		
 *	    Email: dermduncan@gmail.com 
 * *********************************
 */
public class GamePlay extends Activity implements OnClickListener {
	private Bitmap _selectedImg = null;
	private Tile _emptyTile = null;
	private Tile[] _tilesArray = null;
	private final int TABLE_ID = 7911;
	private int _numOfMoves = 0;
	private int _imageId = 0;
	private boolean _checkIfSolved = false;	
	private GameDifficulty _difficulty = GameDifficulty.MEDIUM;
	private boolean _imageSelected = false;
	private Handler myHandler;
	private int countDwn = 3;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// user has specified an image so make sure we don't overwrite it with whatever
		// is stored on the users device
		_imageSelected = true;
        setContentView(R.layout.gameplay);

        // retrieve the set of data passed to us by the Intent
        Bundle extras = getIntent().getExtras();

        // and retrieve the imageToDisplay ID from the extras bundle
        int resource = (int)extras.getLong("imageToDisplay");
        _imageId = resource;

        // Retrieve the screen width and height which will be used to calculate the image sampling size
     	int screenWidth = this.getResources().getDisplayMetrics().widthPixels;
     	int screenHeight = this.getResources().getDisplayMetrics().heightPixels;
     	
     	// Create an optimized Img and display the solved image to the user
        _selectedImg = ImageHelper.getOptimizedBitmap(this, resource, screenWidth, screenHeight);
        CreateSolvedImgView(_selectedImg);
        
        CreateHandler();
	}
	
	/*
	 * Creates a new callback handler which will countdown from 3 before displaying the puzzle
	 */
	private void CreateHandler()
	{
		// Reset the countdown int
		countDwn = 3;
		
		myHandler = new Handler();
        myHandler.removeCallbacks(mMyRunnable);
        myHandler.postDelayed(mMyRunnable, 100);//Wait 3 seconds before switching views.
	}
	
	/** called when the Activity is resuming
     */
    public void onResume() {
    	super.onResume();

        // Restore preferences - get the saved preferences
        SharedPreferences prefs = getPreferences(MODE_PRIVATE);

        // recall the difficulty level and default to medium if nothing is returned
        String savedDifficulty = prefs.getString("nPuzzleDifficulty", "MEDIUM");
        
        if (savedDifficulty.toUpperCase().trim().equals("EASY"))
        	_difficulty = GameDifficulty.EASY;
        else if (savedDifficulty.toUpperCase().trim().equals("MEDIUM"))
        	_difficulty = GameDifficulty.MEDIUM;
        else if (savedDifficulty.toUpperCase().trim().equals("HARD"))
        	_difficulty = GameDifficulty.HARD;
        
        // If the user has selected a new image from the menu then we don't want to
        // load the image that is currently in local storage if any
		if (!_imageSelected)
		{
	        // Retrieve the id of the image the user last used
	        _imageId = Integer.parseInt(prefs.getString("nPuzzleImageId", "0"));
	        
	        // No point continuing if there is no image saved as we can't create a puzzle
	        // without an image
	        if (_imageId > 0)
	        {
		        try {
		        	// Retrieve the JSON array with all the tiles positions
					JSONArray tilePositions = JSONSharedPreferences.loadJSONArray(this, "nPuzzleTilePosition", "nPuzzleTilePosition");
					// Make sure the JSON array is not null and has some values before proceeding
					if (tilePositions != null && tilePositions.length() > 0)
					{
						// Create a new puzzle based on the currently selected image and difficulty level
						createPuzzleFromImg(_selectedImg, _difficulty);
						ParseStoredTileArray(tilePositions);
						// Retrieve the main view
						FrameLayout mainLayout = (FrameLayout)findViewById(R.id.gameplay);
						// Create and populate a table view
						TableLayout puzzleTable = PopulatePuzzleTable(getRowColCount(_difficulty));
					    // Remove any views previously added to the main view	
						mainLayout.removeAllViews();
						mainLayout.addView(puzzleTable);
						// Now the a new puzzle has been created and scrambled we will want to check 
						// when it's solved
						_checkIfSolved = true;
					}
					// If for some reason there is no previous positions saved just create a random puzzle
					else
					{
						CreateNewPuzzle();
					}
		        } catch (JSONException e) {
		        	// If for some reason an error occurs retrieving and parsing the JSON array
		        	// fall back is to just create a random puzzle
		        	CreateNewPuzzle();
				}
	        }
		}
    }

    /** called when the Activity is pausing (and might be killed, so save data)
      */
    public void onPause() {
    	// call the parent's onPause() method
    	super.onPause();

    	
    	// build our preferences object with our data to save
    	SharedPreferences prefs = getPreferences(MODE_PRIVATE);
    	SharedPreferences.Editor editor = prefs.edit();
    	editor.putString("nPuzzleDifficulty", String.valueOf(_difficulty));
    	editor.putString("nPuzzleImageId", Integer.toString(_imageId));
    	
    	// we must commit the preferences or they won't be saved!
    	editor.commit();
    	
    	JSONSharedPreferences.saveJSONArray(this, "nPuzzleTilePosition", "nPuzzleTilePosition", CreateStorableTileArray());
    }
    
    /*
     * Method translates the current tiles array into a JSON array so it can be stored
     * in local storage
     * @return - a JSONArray of the tiles positions
     */
    private JSONArray CreateStorableTileArray()
    {
    	JSONArray jsonTilePositions = new JSONArray();
    	
    	for (int i=0; i<_tilesArray.length;i++)
    	{
    		// Use the Tiles method to create a JSON object of the tiles position
    		jsonTilePositions.put(_tilesArray[i].getJSONObject());
    	}
    	return jsonTilePositions;
    }
    
    /*
     * Translates a JSON Array of tile positions retrieved from storage
     * into a new Tiles Array
     * @input jsonTilePositions - the JSON Array with the tiles positions
     */
    private void ParseStoredTileArray(JSONArray jsonTilePositions)
    {
    	// Create a new tiles array to hold the retrieved positions
    	Tile[] newTilesArray = new Tile[_tilesArray.length];
    	// Used for the new tiles array. We want to add items to the new tiles array
    	// in the order they're retrieved from the JSON array so that they're added
    	// to the table in that order
    	int x = 0;
    	if (jsonTilePositions.length() > 0)
    	{
	    	for (int i=0;i<jsonTilePositions.length();i++)
	    	{
	    		try {
	    			// Create a JSON object and retrieve the tiles id and position from it
	    			JSONObject tilePosition = jsonTilePositions.getJSONObject(i);
	    			int id =  Integer.parseInt(tilePosition.getString("Id"));
	    			int row =  Integer.parseInt(tilePosition.getString("Row"));
	    			int col =  Integer.parseInt(tilePosition.getString("Column"));
	    			
	    			// Loop through the tiles array and find the retrieved tile
	    			for (int j=0;j<_tilesArray.length;j++)
	    			{
	    				if (_tilesArray[j].getId() == id)
	    				{
	    					// Create a temp tile so as not to have to update the tiles array while
	    					// iterating through it
	    					Tile temp = _tilesArray[j];
	    					// Once we find the correct tile update it's column and row position
	    					temp.setCurrentCol(col);
	    					temp.setCurrentRow(row);
	    					
	    					// Add it in the correct order to the newTilesArray so it's added to
	    					// the table in the correct order
	    					newTilesArray[x] = temp;
	    					x++;
	    				}
	    			}
	    		} catch (JSONException ex) {
	    			// If for some reason an error occurs retrieving and parsing the JSON array
		        	// fall back is to just create a random puzzle
		        	CreateNewPuzzle();
	    		}
	    	}
    	}
    	else
    	{
    		// We don't want an empty array so set the newTilesArray equal to the current tiles array
    		newTilesArray = _tilesArray;
    	}
    	_tilesArray = newTilesArray;
    }
	
	/*
	 * Creates a new options menu
	 * @input menu - the menu to be created
	 */
	public boolean onCreateOptionsMenu(Menu menu) 
	{
		new MenuInflater(this).inflate(R.menu.menu, menu);
		return (super.onCreateOptionsMenu(menu));
	}
	
	@Override
	/*
	 * Handles options menu selections
	 * @input  item - the menu item that was clicked
	 * @return - boolean whether the button click was successful or not
	 */
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			// Re-shuffle the puzzle. Use the existing difficulty level
			case R.id.menu_reshuffle:
				CreateNewPuzzle();
				return true;
			// Re-shuffle the puzzle using the easy difficulty level
			case R.id.menu_easy:
				// Specify the difficulty level
				_difficulty = GameDifficulty.EASY;
				// Display the solved image
				CreateSolvedImgView(_selectedImg);
				// Create the puzzle
				CreateHandler();
				return true;
			// Re-shuffle the puzzle using the medium difficulty level
			case R.id.menu_medium:
				// Specify the difficulty level
				_difficulty = GameDifficulty.MEDIUM;
				// Display the solved image
				CreateSolvedImgView(_selectedImg);
				// Create the puzzle
				CreateHandler();
				return true;
			// Re-shuffle the puzzle using the hard difficulty level
			case R.id.menu_hard:
				// Specify the difficulty level
				_difficulty = GameDifficulty.HARD;
				// Display the solved image
				CreateSolvedImgView(_selectedImg);
				// Create the puzzle
				CreateHandler();
				return true;
			// Return to the image selection page
			case R.id.menu_new_image:
				// create the Intent to open our ImageSelection activity.
		    	Intent i = new Intent(GamePlay.this, ImageSelection.class);
		    	// start our activity
		    	startActivity(i);
				return true;
		}
		return false;
	}
	
	/*
	 * On a tile click check if the tile can validly be moved. If it can move it,
	 * otherwise do nothing
	 * @input v - the tile that was clicked
	 */
	public void onClick(View v) {
		if (v.getId() > 0) {
			// Retrieve the clicked tile
			Tile currTile = (Tile)v;
			// Pass the tiles row and column position into the moveTile method which
			// will check whether it can be moved or not
			moveTile(currTile.getCurrentRow(), currTile.getCurrentCol());
			// Re-populate the puzzle with the new tile positions
			PopulatePuzzleTable(getRowColCount(_difficulty));
	        _numOfMoves++;
		}
	}
	
	/*
	 * Create a new puzzle
	 * @input puzzleImg - selected image which will be tiled and added to the puzzle
	 * @input difficulty - games difficulty level
	 */
	private void createPuzzleFromImg(Bitmap puzzleImg, GameDifficulty difficulty) 
	{
		// Retrieve the screens width and height. This will be used to calculate what
		// to position to put each tile in
		int screenWidth = this.getResources().getDisplayMetrics().widthPixels;
		int screenHeight = this.getResources().getDisplayMetrics().heightPixels;
		// Default puzzle size
		int puzzleSize = 16;
		Bitmap[] bitmapsArray = null;
		int tileWidth = 0;
		int tileHeight = 0;
		switch (difficulty)
		{
			case EASY:
				// In the easy puzzle we're going to have a 3x3 puzzle
				puzzleSize = 9;
				// Initialize the bitmap array and tiles array with the puzzle size
				bitmapsArray = new Bitmap[puzzleSize];
				_tilesArray = new Tile[puzzleSize];
				// As this is a 3x3 array we want to make each tile a 3rd of the screen. Remove 2 for padding
				tileWidth = screenWidth/3 - 2;
				tileHeight = screenHeight/3 - 2;
				
				// Create a bitmap for each tile by cutting a piece out of the image. Then create a tile out of this image piece
				/* row 1 */
				bitmapsArray[0] = Bitmap.createBitmap(puzzleImg, 0, 0, tileWidth, tileHeight);
			    _tilesArray[0] = createTile(bitmapsArray[0], 0, 0, 1);
				bitmapsArray[1] = Bitmap.createBitmap(puzzleImg, tileWidth, 0, tileWidth, tileHeight);
				_tilesArray[1] = createTile(bitmapsArray[1], 0, 1, 2);
				bitmapsArray[2] = Bitmap.createBitmap(puzzleImg, tileWidth*2, 0, tileWidth, tileHeight);
				_tilesArray[2] = createTile(bitmapsArray[2], 0, 2, 3);
				
				/* row 2 */
			    bitmapsArray[3] = Bitmap.createBitmap(puzzleImg, 0, tileHeight, tileWidth, tileHeight);
			    _tilesArray[3] = createTile(bitmapsArray[3], 1, 0, 4);
			    bitmapsArray[4] = Bitmap.createBitmap(puzzleImg, tileWidth, tileHeight, tileWidth, tileHeight);
			    _tilesArray[4] = createTile(bitmapsArray[4], 1, 1, 5);
			    bitmapsArray[5] = Bitmap.createBitmap(puzzleImg, tileWidth*2, tileHeight, tileWidth, tileHeight);
			    _tilesArray[5] = createTile(bitmapsArray[5], 1, 2, 6);
			    
			    /* row 3 */
			    bitmapsArray[6] = Bitmap.createBitmap(puzzleImg, 0, tileHeight*2, tileWidth, tileHeight);
			    _tilesArray[6] = createTile(bitmapsArray[6], 2, 0, 7);
			    bitmapsArray[7] = Bitmap.createBitmap(puzzleImg, tileWidth, tileHeight*2, tileWidth, tileHeight);
			    _tilesArray[7] = createTile(bitmapsArray[7], 2, 1, 8);
			    bitmapsArray[8] = Bitmap.createBitmap(puzzleImg, tileWidth*2, tileHeight*2, tileWidth, tileHeight);
			    
			    // the last tile is always going to be empty so pass in no bitmap to the create tile method
			    _emptyTile = createTile(null, 2, 2, 9);
			    _tilesArray[8] = _emptyTile;
			    break;
			case MEDIUM:
				// In the easy puzzle we're going to have a 4x4 puzzle
				puzzleSize = 16;
				// Initialize the bitmap array and tiles array with the puzzle size
				bitmapsArray = new Bitmap[puzzleSize];
				_tilesArray = new Tile[puzzleSize];
				// As this is a 4x4 array we want to make each tile a quarter of the screen. Remove 2 for padding
				tileWidth = screenWidth/4 - 2;
				tileHeight = screenHeight/4 - 2;
				
				// Create a bitmap for each tile by cutting a piece out of the image. Then create a tile out of this image piece
				/* row 1 */
				bitmapsArray[0] = Bitmap.createBitmap(puzzleImg, 0, 0, tileWidth, tileHeight);
				_tilesArray[0] = createTile(bitmapsArray[0], 1, 1, 1);
				bitmapsArray[1] = Bitmap.createBitmap(puzzleImg, tileWidth, 0, tileWidth, tileHeight);
				_tilesArray[1] = createTile(bitmapsArray[1], 1, 2, 2);
				bitmapsArray[2] = Bitmap.createBitmap(puzzleImg, tileWidth*2, 0, tileWidth, tileHeight);
				_tilesArray[2] = createTile(bitmapsArray[2], 1, 3, 3);
				bitmapsArray[3] = Bitmap.createBitmap(puzzleImg, tileWidth*3, 0, tileWidth, tileHeight);
				_tilesArray[3] = createTile(bitmapsArray[3], 1, 4, 4);
				
				/* row 2 */
			    bitmapsArray[4] = Bitmap.createBitmap(puzzleImg, 0, tileHeight, tileWidth, tileHeight);
			    _tilesArray[4] = createTile(bitmapsArray[4], 2, 1, 5);
			    bitmapsArray[5] = Bitmap.createBitmap(puzzleImg, tileWidth, tileHeight, tileWidth, tileHeight);
			    _tilesArray[5] = createTile(bitmapsArray[5], 2, 2, 6);
			    bitmapsArray[6] = Bitmap.createBitmap(puzzleImg, tileWidth*2, tileHeight, tileWidth, tileHeight);
			    _tilesArray[6] = createTile(bitmapsArray[6], 2, 3, 7);
			    bitmapsArray[7] = Bitmap.createBitmap(puzzleImg, tileWidth*3, tileHeight, tileWidth, tileHeight);
			    _tilesArray[7] = createTile(bitmapsArray[7], 2, 4, 8);
			    
			    /* row 3 */
			    bitmapsArray[8] = Bitmap.createBitmap(puzzleImg, 0, tileHeight*2, tileWidth, tileHeight);
			    _tilesArray[8] = createTile(bitmapsArray[8], 3, 1, 9);
			    bitmapsArray[9] = Bitmap.createBitmap(puzzleImg, tileWidth, tileHeight*2, tileWidth, tileHeight);
			    _tilesArray[9] = createTile(bitmapsArray[9], 3, 2, 10);
			    bitmapsArray[10] = Bitmap.createBitmap(puzzleImg, tileWidth*2, tileHeight*2, tileWidth, tileHeight);
			    _tilesArray[10] = createTile(bitmapsArray[10], 3, 3, 11);
			    bitmapsArray[11] = Bitmap.createBitmap(puzzleImg, tileWidth*3, tileHeight*2, tileWidth, tileHeight);
			    _tilesArray[11] = createTile(bitmapsArray[11], 3, 4, 12);
			    
			    /* row 4 */
			    bitmapsArray[12] = Bitmap.createBitmap(puzzleImg, 0, tileHeight*3, tileWidth, tileHeight);
			    _tilesArray[12] = createTile(bitmapsArray[12], 4, 1, 13);
			    bitmapsArray[13] = Bitmap.createBitmap(puzzleImg, tileWidth, tileHeight*3, tileWidth, tileHeight);
			    _tilesArray[13] = createTile(bitmapsArray[13], 4, 2, 14);
			    bitmapsArray[14] = Bitmap.createBitmap(puzzleImg, tileWidth*2, tileHeight*3, tileWidth, tileHeight);
			    _tilesArray[14] = createTile(bitmapsArray[14], 4, 3, 15);
			    bitmapsArray[15] = Bitmap.createBitmap(puzzleImg, tileWidth*3, tileHeight*3, tileWidth, tileHeight);
			    
			    // the last tile is always going to be empty so pass in no bitmap to the create tile method
			    _emptyTile = createTile(null, 4, 4, 16);
			    _tilesArray[15] = _emptyTile;
			    break;
			case HARD:
				// In the easy puzzle we're going to have a 5x5 puzzle
				puzzleSize = 25;
				// Initialize the bitmap array and tiles array with the puzzle size
				bitmapsArray = new Bitmap[puzzleSize];
				_tilesArray = new Tile[puzzleSize];
				// As this is a 5x5 array we want to make each tile a quarter of the screen. Remove 2 for padding
				tileWidth = screenWidth/5 - 2;
				tileHeight = screenHeight/5 - 2;
				
				// Create a bitmap for each tile by cutting a piece out of the image. Then create a tile out of this image piece
				/* row 1 */
				bitmapsArray[0] = Bitmap.createBitmap(puzzleImg, 0, 0, tileWidth, tileHeight);
				_tilesArray[0] = createTile(bitmapsArray[0], 1, 1, 1);
				bitmapsArray[1] = Bitmap.createBitmap(puzzleImg, tileWidth, 0, tileWidth, tileHeight);
				_tilesArray[1] = createTile(bitmapsArray[1], 1, 2, 2);
				bitmapsArray[2] = Bitmap.createBitmap(puzzleImg, tileWidth*2, 0, tileWidth, tileHeight);
				_tilesArray[2] = createTile(bitmapsArray[2], 1, 3, 3);
				bitmapsArray[3] = Bitmap.createBitmap(puzzleImg, tileWidth*3, 0, tileWidth, tileHeight);
				_tilesArray[3] = createTile(bitmapsArray[3], 1, 4, 4);
				bitmapsArray[4] = Bitmap.createBitmap(puzzleImg, tileWidth*4, 0, tileWidth, tileHeight);
				_tilesArray[4] = createTile(bitmapsArray[4], 1, 5, 5);
				
				/* row 2 */
			    bitmapsArray[5] = Bitmap.createBitmap(puzzleImg, 0, tileHeight, tileWidth, tileHeight);
			    _tilesArray[5] = createTile(bitmapsArray[5], 2, 1, 6);
			    bitmapsArray[6] = Bitmap.createBitmap(puzzleImg, tileWidth, tileHeight, tileWidth, tileHeight);
			    _tilesArray[6] = createTile(bitmapsArray[6], 2, 2, 7);
			    bitmapsArray[7] = Bitmap.createBitmap(puzzleImg, tileWidth*2, tileHeight, tileWidth, tileHeight);
			    _tilesArray[7] = createTile(bitmapsArray[7], 2, 3, 8);
			    bitmapsArray[8] = Bitmap.createBitmap(puzzleImg, tileWidth*3, tileHeight, tileWidth, tileHeight);
			    _tilesArray[8] = createTile(bitmapsArray[8], 2, 4, 9);
			    bitmapsArray[9] = Bitmap.createBitmap(puzzleImg, tileWidth*4, tileHeight, tileWidth, tileHeight);
			    _tilesArray[9] = createTile(bitmapsArray[9], 2, 5, 10);
			    
			    /* row 3 */
			    bitmapsArray[10] = Bitmap.createBitmap(puzzleImg, 0, tileHeight*2, tileWidth, tileHeight);
			    _tilesArray[10] = createTile(bitmapsArray[10], 3, 1, 11);
			    bitmapsArray[11] = Bitmap.createBitmap(puzzleImg, tileWidth, tileHeight*2, tileWidth, tileHeight);
			    _tilesArray[11] = createTile(bitmapsArray[11], 3, 2, 12);
			    bitmapsArray[12] = Bitmap.createBitmap(puzzleImg, tileWidth*2, tileHeight*2, tileWidth, tileHeight);
			    _tilesArray[12] = createTile(bitmapsArray[12], 3, 3, 13);
			    bitmapsArray[13] = Bitmap.createBitmap(puzzleImg, tileWidth*3, tileHeight*2, tileWidth, tileHeight);
			    _tilesArray[13] = createTile(bitmapsArray[13], 3, 4, 14);
			    bitmapsArray[14] = Bitmap.createBitmap(puzzleImg, tileWidth*4, tileHeight*2, tileWidth, tileHeight);
			    _tilesArray[14] = createTile(bitmapsArray[14], 3, 5, 15);
			    
			    /* row 4 */
			    bitmapsArray[15] = Bitmap.createBitmap(puzzleImg, 0, tileHeight*3, tileWidth, tileHeight);
			    _tilesArray[15] = createTile(bitmapsArray[15], 4, 1, 16);
			    bitmapsArray[16] = Bitmap.createBitmap(puzzleImg, tileWidth, tileHeight*3, tileWidth, tileHeight);
			    _tilesArray[16] = createTile(bitmapsArray[16], 4, 2, 17);
			    bitmapsArray[17] = Bitmap.createBitmap(puzzleImg, tileWidth*2, tileHeight*3, tileWidth, tileHeight);
			    _tilesArray[17] = createTile(bitmapsArray[17], 4, 3, 18);
			    bitmapsArray[18] = Bitmap.createBitmap(puzzleImg, tileWidth*3, tileHeight*3, tileWidth, tileHeight);
			    _tilesArray[18] = createTile(bitmapsArray[18], 4, 4, 19);
			    bitmapsArray[19] = Bitmap.createBitmap(puzzleImg, tileWidth*4, tileHeight*3, tileWidth, tileHeight);
			    _tilesArray[19] = createTile(bitmapsArray[19], 4, 5, 20);
			    
			    /* row 5 */
			    bitmapsArray[20] = Bitmap.createBitmap(puzzleImg, 0, tileHeight*4, tileWidth, tileHeight);
			    _tilesArray[20] = createTile(bitmapsArray[20], 5, 1, 21);
			    bitmapsArray[21] = Bitmap.createBitmap(puzzleImg, tileWidth, tileHeight*4, tileWidth, tileHeight);
			    _tilesArray[21] = createTile(bitmapsArray[21], 5, 2, 22);
			    bitmapsArray[22] = Bitmap.createBitmap(puzzleImg, tileWidth*2, tileHeight*4, tileWidth, tileHeight);
			    _tilesArray[22] = createTile(bitmapsArray[22], 5, 3, 23);
			    bitmapsArray[23] = Bitmap.createBitmap(puzzleImg, tileWidth*3, tileHeight*4, tileWidth, tileHeight);
			    _tilesArray[23] = createTile(bitmapsArray[23], 5, 4, 24);
			    bitmapsArray[24] = Bitmap.createBitmap(puzzleImg, tileWidth*4, tileHeight*4, tileWidth, tileHeight);
			    
			    // the last tile is always going to be empty so pass in no bitmap to the create tile method
			    _emptyTile = createTile(null, 5, 5, 25);
			     _tilesArray[24] = _emptyTile;
			    break;
		}
	}
	
	/*
	 * Creates a tile with the specified image, row & column positions and id
	 * @input tileFace - image to display on the tile
	 * @input r - tiles row position
	 * @input c - tiles column position
	 * @input id - tiles id
	 * @return - new tile to be returned
	 */
	private Tile createTile(Bitmap tileFace, int r, int c, int id)
	{
		Tile im = new Tile (this, r, c);
		im.setPadding(1, 1, 1, 1); 
        im.setId(id);
        if (tileFace != null)
        	im.setFace(tileFace);
        im.setCurrentCol(c);
        im.setCurrentRow(r);
        im.setOnClickListener(this);
        
        return im;
	}
	
	/*
	 * Create a view with the solved image
	 * @input solvedImg - Image to display
	 * @return - imageview with the solved image embedded
	 */
	private void CreateSolvedImgView(Bitmap solvedImg)
	{
		FrameLayout mainLayout = (FrameLayout)findViewById(R.id.gameplay);
		
		mainLayout.removeAllViews();
		ImageView img = new ImageView(GamePlay.this);
		img.setImageBitmap(solvedImg);
		img.setScaleType(ScaleType.CENTER);
		mainLayout.addView(img);

		//return img;
	}
	
	/*
	 * Shuffles the puzzle pieces into random locations on the board
	 */
	private void CreateRandomNPuzzle()
	{
		// Use the number of tiles squared as the number of tries to randomize the board
		// the bigger the puzzle the more tries we want to use to make sure it's suffled
		int numTries = (_tilesArray.length*_tilesArray.length);
		int rand;
		
		// Generate random array by swapping two tiles
		// By using the moveTile method we're ensuring that the puzzle is solvable 
	    for (int i = 0; i < numTries; i++)
	    {
	    	// Create a ramdom number between 0 and the size of the array less 1
	    	// Min + (int)(Math.random() * ((Max - Min) + 1))
	    	rand = 0 + (int)(Math.random() * ((_tilesArray.length - 1) + 1));
	    	moveTile(_tilesArray[rand].getCurrentRow(), _tilesArray[rand].getCurrentCol());
	    }

	    // Reset the number of moves back to zero so the ramdomization turns aren't counted
		_numOfMoves = 0;
	}
	
	/*
	 * Populates a table view once the tiles have been created and added to an array
	 * @input rowColCnt - number of rows/columns
	 * @return - a table layout populated with the shuffled puzzle
	 */
	private TableLayout PopulatePuzzleTable(int rowColCnt)
	{
		// If we've previously created the table then retrieve it
		TableLayout table = (TableLayout)findViewById(TABLE_ID);
		// Otherwise create a new one
		if (table == null)
		{
			table = new TableLayout(this);
			table.setId(TABLE_ID);
		}
		
		// Make sure we have some tiles before trying to populate the table
		if (_tilesArray.length > 0)
		{
			// If the table has been previously used we want to make sure to remove all 
			// child views
			int count = table.getChildCount();
			/* NOTE - we need to remove each view individually. Simply using the table 
			 * removeAllViews doesn't remove the views from the tables rows 
			 * */
			for (int i = 0; i < count; i++) {
			    View child = table.getChildAt(i);
			    // Remove all child rows from each row
			    if (child instanceof TableRow) ((ViewGroup) child).removeAllViews();
			}
			table.removeAllViews();
			int imgNum = 0;
	        for (int r=1; r<=rowColCnt; r++){
	            TableRow tr = new TableRow(this);
	            for (int c=1; c<=rowColCnt; c++){
	            	int width = 0;
	            	int height = 0;
	            	
	            	// This code is required for the empty tile. As it has no bitmap
	            	// we need to store the width and height of a previous bitmap
	            	Bitmap tileFace = _tilesArray[imgNum].getFace() ;
	            	
	            	if (tileFace !=null && (width == 0 || height == 0))
	            	{
	            		width = tileFace.getWidth();
	            		height = tileFace.getHeight();
	            	}
	            	
	                tr.addView(_tilesArray[imgNum], width, height);
	                imgNum++;
	            }
	            table.addView(tr);
	        }
		}
		return table;
	}
	
	/*
	 * Method to move a selected tile
     * @param r - Checks if the move in a row/s is legal
     * @param c - Checks if the move in a column/s is legal
     * @return - Allows the move to proceed if legal, blocks the move if illegal
     */
    public boolean moveTile(int r, int c) {
        // It's a legal move if the empty cell is next to it.
        return checkEmpty(r, c, -1, 0) || checkEmpty(r, c, 1, 0)
                || checkEmpty(r, c, 0, -1) || checkEmpty(r, c, 0, 1);
    }

    /*
     * Check to see if the selected tile has an empty tile beside it
     * @input r - selected tiles row position
     * @input c - selected tiles column position
     * @input rdelta - used along with cdelta to check all tiles surrounding the selected tile to see if their empty
     * @input cdelta - used along with rdelta to check all tiles surrounding the selected tile to see if their empty
     * @return - true and does the swap if there is an empty tile beside the selected tile, otherwise it's false
     */
    private boolean checkEmpty(int r, int c, int rdelta, int cdelta) {
        int rNeighbor = r + rdelta;
        int cNeighbor = c + cdelta;
        // Check to see if this neighbor is on board and is empty.
        if (isLegalRowCol(rNeighbor, cNeighbor)
                && findTile(rNeighbor, cNeighbor) == _emptyTile) {
            exchangeTiles(r, c);
            return true;
        }
        return false;
    }
    
    /*
     * Checks if a row and column position are valid or not 
     * @param r - Row position
     * @param c - Column position
     * @return - returns through if the tile is on the board.
     */
    public boolean isLegalRowCol(int r, int c) {
        return r >= 0 && r <= getRowColCount(_difficulty) && c >= 0 && c <= getRowColCount(_difficulty);
    }

    /*
     * Method to switch 2 tiles. Note one of the tiles is always the empty tile
     * @param r1 - row position of the tile to be swapped with the empty tile
     * @param c1 - column position of the title to be swapped with the empty tile
     */
    private void exchangeTiles(int r1, int c1) {
        Tile temp1 = findTile(r1, c1);
        
        // As the empty tiles row and column position could be updated before
        // it's added to the array we want to keep track of it's pre-swapped values
        // which will be used to update the swapped tiles row and column position
        int r2 = _emptyTile.getCurrentRow();
        int c2 = _emptyTile.getCurrentCol();
		
        Tile[] newTilesArray = _tilesArray;
        // Check if the tilesArray has been populated
        if (_tilesArray.length > 0)
    	{
        	// Loop through the tiles array and try and find the tile been swapped
        	// along with the empty tile
    		for (int i=0; i<_tilesArray.length; i++)
    		{
    			// Use the tiles id to find the tile to be swapped
    			if (_tilesArray[i].getId() == temp1.getId()) {
    				// We're going to swap the empty tile into this position so we
    				// want to set the empty tiles column and row position equal to
    				// the swapping tiles row and column position
    				_emptyTile.setCurrentCol(c1);
    				_emptyTile.setCurrentRow(r1);
    				
    				// Add empty tile to the array at this position so it will
    				// be added to the table in this order
    				newTilesArray[i] = _emptyTile;	
    			} // Use the empty tiles id to discover what position to put the swapped
    			  // tile into in the array
    			else if (_tilesArray[i].getId() == _emptyTile.getId()) {
    				// We're going to swap the tile into the empty tiles position so we
    				// want to set the tiles column and row position equal to
    				// the empty tiles row and column position
    				temp1.setCurrentCol(c2);
    				temp1.setCurrentRow(r2); 
    				
    				// Add the swapped tile to the array at this position so it will be
    				// added to the table in the empty tiles position
    				newTilesArray[i] = temp1;
    			} // Otherwise we want to keep the tile in the same position
    			else {
    				newTilesArray[i] = _tilesArray[i];
    			}
    			
    		}
    	}
        // Update the tilesArray with the new tile positions
        _tilesArray = newTilesArray;
        
        // Check if the puzzle is solved. If it is we want to launch the success screen
        if (PuzzleSolved())
        {
        	//Do something
        	// create the Intent to open our ShowImage activity.
        	Intent i = new Intent(GamePlay.this, YouWin.class);
       
        	// pass a key:value pair into the 'extra' bundle for
        	// the intent so the activity is made aware which
        	// photo was selected.
        	i.putExtra("imageToDisplay", _imageId);
        	i.putExtra("numOfTurns", _numOfMoves);

        	// start our activity
        	startActivity(i);
        }
    }
    
    /*
     * Method to check if the puzzle is solved. This loops through each tile and checks
     * if they are in their final positions using their isInFinalPosition property
     * @return - returns true if the puzzle is solved, false if it's not.
     */
    private Boolean PuzzleSolved()
    {
    	// When we're randomly shuffling the board we don't want to check if it's solved
    	if (!_checkIfSolved)
    		return false;
    	Boolean solved = false;
    	// Loop through the tiles array and check each tile to see if it's
    	// in its final position. If it is set the return value to true.
    	if (_tilesArray.length > 0)
    	{
    		for (int i=0; i< _tilesArray.length; i++)
    		{
    			if (_tilesArray[i].isInFinalPosition())
    				solved = true;
    			else
    				// If even one of the tiles is not in the final position then the
    				// puzzle isn't solved so break out of the loop
    				return false;
    		}
    	}
    	return solved;
    }
    
    /*
     * Method to find a tile in the tile array based on it's row and column position
     * @input r - row position of the tile
     * @input c - column position of the tile
     * @return - the tile at the passed in row and column position
     */
    private Tile findTile(int r, int c)
    {
    	Tile returnTile = null;
    	
    	// Loop through the tiles array and find a tile at the passed in row and column position
    	if (_tilesArray.length > 0)
    	{
    		for (int i=0; i<_tilesArray.length; i++)
    		{
    			if (_tilesArray[i].getCurrentCol() == c && _tilesArray[i].getCurrentRow() == r)
    				returnTile = _tilesArray[i];
    		}
    	}
    	
    	return returnTile;
    }
	
    /*
     * Enum to hold the different game difficulty levels
     */
	private enum GameDifficulty
	{
		EASY,
		MEDIUM,
		HARD
	}
	
	/*
	 * Method gets the number of rows/columns in the puzzle based on the puzzles 
	 * difficulty level
	 * @input difficulty - difficulty level of the puzzle
	 * @return - row/column count
	 */
	private int getRowColCount(GameDifficulty difficulty)
	{
		int rowColCount = 3;
		
		switch (difficulty)
		{
			case EASY:
				rowColCount = 3;
				break;
			case MEDIUM:
				rowColCount = 4;
				break;
			case HARD:
				rowColCount = 5;
				break;
		}
		return rowColCount;
	}

	/*
	 *  Runnable which handles countdown callback and once the countdown hits zero
	 *  launches the puzzle
	 */
	private Runnable mMyRunnable = new Runnable()
	{
	    @Override
	    public void run()
	    {
	    	// Retrieve the countdown textView
	    	TextView txtTimer = (TextView)findViewById(1985);
	    	
	    	if (txtTimer == null)
	    	{
	    		FrameLayout mainLayout = (FrameLayout)findViewById(R.id.gameplay);
	            
	    		txtTimer = new TextView(GamePlay.this);
	    		txtTimer.setId(1985);
	    		txtTimer.setTextColor(Color.WHITE);
	            txtTimer.setTextSize(30);
	            txtTimer.setGravity(Gravity.CENTER);
	            mainLayout.addView(txtTimer);
	    	}

	    	// Set the text to the current countdown value
	    	txtTimer.setText(String.valueOf(countDwn));
	    	
	    	// If we haven't counted down to zero yet, call this method again
	    	if (countDwn > 0)
	    		myHandler.postDelayed(this, 1000);
	    	// Otherwise clear the countdown and launch th puzzle
	    	else
	    	{
		    	txtTimer.setText("");
		       //Create a new puzzle after 3 seconds has elapsed
		    	CreateNewPuzzle();
		    	// If the user quits we want to retrieve this image from storage
		    	_imageSelected= false;
	    	}
	    	// Decrement the countdown integer
	    	countDwn--;
	    }
	 };
	 
	 /*
	  * Method to create a new puzzle. It populates the puzzle array, scrambles the puzzle
	  * and creates a table with the scrambled puzzle
	  */
	 private void CreateNewPuzzle() 
	 {
		 // When we're creating a new puzzle we don't want to check if it's solved
		 _checkIfSolved = false;
		 // Retrieve the main view
		 FrameLayout mainLayout = (FrameLayout)findViewById(R.id.gameplay);
		 // Create a new puzzle based on the currently selected image and difficulty level
		 createPuzzleFromImg(_selectedImg, _difficulty);
		 // Scramble the puzzle
		 CreateRandomNPuzzle();
		 // Create and populate a table view
		 TableLayout puzzleTable = PopulatePuzzleTable(getRowColCount(_difficulty));
	     // Remove any views previously added to the main view	
		 mainLayout.removeAllViews();
		 mainLayout.addView(puzzleTable);
		 // Now the a new puzzle has been created and scrambled we will want to check 
		 // when it's solved
		 _checkIfSolved = true;
	 }
}

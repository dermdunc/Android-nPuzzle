package net.cs76.projects.nPuzzle70857251;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.widget.ImageView;
import org.json.JSONObject;
import org.json.JSONException;

/*
 * Represents the individual "tiles" in the puzzle
 * ********************************* 
 * Written by: Dermot Duncan 	     
 * 	    HU ID: 70857251      		
 *	    Email: dermduncan@gmail.com 
 * *********************************
 */
public class Tile extends ImageView{
 
 // instance variables
 private int _row;     // row of final position
 private int _col;     // col of final position
 private int _currentRow;
 private int _currentCol;
 private Bitmap _face;  // bitmap to display 

 public Tile(Context context, int row, int col, Bitmap face) {
	 super(context);
     _row = row;
     _col = col;
     setFace(face);
 }
 
 public Tile(Context context, int row, int col) {
	 super(context);
     _row = row;
     _col = col;
     this.setBackgroundColor(Color.BLACK);
 }

 public void setFace(Bitmap newFace) {
     _face = newFace;
     this.setImageBitmap(newFace);
 }

 public Bitmap getFace() {
     return _face;
 }
 
 public int getCurrentRow()
 {
	 return _currentRow;
 }
 
 public void setCurrentRow(int r)
 {
	 _currentRow = r;
 }
 
 public int getCurrentCol()
 {
	 return _currentCol;
 }
 
 public void setCurrentCol(int c)
 {
	 _currentCol = c;
 }

/*
 * Checker to see if the tiles current position matches its original position
 */
 public boolean isInFinalPosition() {
     return _currentRow == _row && _currentCol == _col;
 }
 
 /*
  * Method to convert the tile to a JSON object for storage and retrieval
  */
 public JSONObject getJSONObject() {
     JSONObject obj = new JSONObject();
     try {
         obj.put("Id", Integer.toString(this.getId()));
         obj.put("Row", Integer.toString(_currentRow));
         obj.put("Column", Integer.toString(_currentCol));
     } catch (JSONException e) {
         //trace("DefaultListItem.toString JSONException: "+e.getMessage());
     }
     return obj;
 }
}

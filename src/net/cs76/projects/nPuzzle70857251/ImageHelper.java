package net.cs76.projects.nPuzzle70857251;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.Toast;

/*
 * Image helper class
 * ********************************* 
 * Written by: Dermot Duncan 	     
 * 	    HU ID: 70857251      		
 *	    Email: dermduncan@gmail.com 
 * *********************************
 */
public final class ImageHelper {

	/*
	 * Static method which optimizes a bitmap
	 * @input currContext - the current activities context
	 * @input position - the original bitmaps resource position
	 * @input width - the desired width of the optimized image
	 * @input height - the desired height of the optimized image
	 * @return - an optimized version of the image
	 */
	public static Bitmap getOptimizedBitmap(Context currContext, int position, int width, int height)
	{
		// As we have no idea how big the image been used in we want to retrieve its
		// dimensions first
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeResource(currContext.getResources(), position, options);
		
		// Decide whether the image is height heavy or width heavy
		Boolean scaleByHeight = Math.abs(options.outHeight - height) > Math.abs(options.outWidth - width);
		// Create an initial scale using the image dimensions and the screen dimensions
		double scale = scaleByHeight
	              ? options.outHeight / height
	              : options.outWidth / width;
		
		// Use logs to calculate a more accurate sampling size (note for better performance we use the nearest multiple of 2)
		int sampleSize = (int)Math.pow(2d, Math.floor(Math.log(scale)/Math.log(2d)));

		// Now we have a sampling size it's time to optimize the image
		options = new BitmapFactory.Options();
		if (sampleSize > 0)
			options.inSampleSize = sampleSize;
		Bitmap finishedImg = null;
		Bitmap optimizedImg = null;
		
		// In case the users device has low memory we wrap this in a try catch
		try 
		{
			// For better performance we first optimize the image using the calculated sampling size
			optimizedImg = BitmapFactory.decodeResource(currContext.getResources(), position, options);
			// The optimized image needs to be adjusted a bit further to fit the screen
			finishedImg = Bitmap.createScaledBitmap(optimizedImg, width, height, true);
		}
		catch (OutOfMemoryError ex) 
		{
			// If after optimizing the image we still get an out of memory error alert the user
			// that they need to free up some memory on their device to use the app
			int duration = Toast.LENGTH_LONG;
    		Toast toast = Toast.makeText(currContext, "Sorry but there is not enough memory on this device to support this application. Please free up some memory and try again.", duration);
    		toast.show();
		}
		finally
		{
			// Free up some memory by dumping the optimized image
			optimizedImg.recycle();
		}
		
		return finishedImg;
		
	}
}

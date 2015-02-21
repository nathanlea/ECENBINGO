package com.bingo.fibonacci.bingo;

import android.content.Context;
import android.hardware.display.DisplayManager;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import java.util.Random;

/**
 * Created by Nathan on 2/13/2015.
 */
public class ImageAdapter extends BaseAdapter {
    private Context mContext;
    private int scaleFactor = 85;

    public ImageAdapter(Context c) {
        mContext = c;

        Random rnd = new Random();
        for (int i = mThumbIds.length - 1; i > 0; i--)
        {
            int index = rnd.nextInt(i + 1);
            // Simple swap
            int a = mThumbIds[index];
            mThumbIds[index] = mThumbIds[i];
            mThumbIds[i] = a;
       }
        mThumbIds[12] =  R.drawable.freespace;

        for (int i = 0; i<25;i++) {
            mThumb25[i] = mThumbIds[i];
        }

        DisplayMetrics metrics = mContext.getResources().getDisplayMetrics();

        switch (metrics.densityDpi) {
            case DisplayMetrics.DENSITY_LOW:
                scaleFactor = 85;
                break;
            case DisplayMetrics.DENSITY_MEDIUM:
                scaleFactor = 85;
                break;
            case DisplayMetrics.DENSITY_HIGH:
                scaleFactor = 100;
                break;
            case DisplayMetrics.DENSITY_XHIGH:
                scaleFactor = 150;
                break;
            case DisplayMetrics.DENSITY_XXHIGH:
                scaleFactor = 180;
                break;
            case DisplayMetrics.DENSITY_XXXHIGH:
                scaleFactor = 210;
                break;
            default:
                scaleFactor = 85;
                break;
        }
    }

    public ImageAdapter() {

    }

    public int getCount() {
        return mThumb25.length;
    }

    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return 0;
    }

    // create a new ImageView for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        if (convertView == null) {  // if it's not recycled, initialize some attributes
            imageView = new ImageView(mContext);
            imageView.setLayoutParams(new GridView.LayoutParams(scaleFactor, scaleFactor));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setPadding(8, 8, 8, 8);
        } else {
            imageView = (ImageView) convertView;
        }

        imageView.setImageResource(mThumb25[position]);
        return imageView;
    }

    // references to our images
    private Integer[] mThumbIds = {
            R.drawable.questions, R.drawable.answersasked,
            R.drawable.beerbet, R.drawable.breakschalk,
            R.drawable.changesmind, R.drawable.cheapie,
            R.drawable.confusion, R.drawable.daughter,
            R.drawable.diffanswer, R.drawable.doesntunderstand,
            R.drawable.early, R.drawable.ekg,
            R.drawable.eraser, R.drawable.ignores,
            R.drawable.makesjoke, R.drawable.mistake,
            R.drawable.mistakeonslide, R.drawable.notonslide,
            R.drawable.notontest, R.drawable.oldmen,
            R.drawable.pagenotinnotes, R.drawable.prototype,
            R.drawable.semiconductorchem, R.drawable.simple,
            R.drawable.smudges, R.drawable.tangent,
            R.drawable.timeonslide, R.drawable.undergrad,
    };

    private Integer[] mThumb25 = new Integer[25];
}

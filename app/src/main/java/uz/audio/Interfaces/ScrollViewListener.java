package uz.audio.Interfaces;

import uz.audio.Views.ScrollViewExt;

/**
 * Created by Ankit on 1/17/2017.
 */

public interface ScrollViewListener {
    void onScrollChanged(ScrollViewExt scrollView,
                         int x, int y, int oldx, int oldy);
}
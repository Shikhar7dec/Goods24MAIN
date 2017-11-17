package in.goods24.util;

import android.view.View;
import android.view.ViewGroup;

/**
 * Created by Shikhar on 11/17/2017.
 */

public class GenericUtil {
    public static void setMargins (View v, int left, int top, int right, int bottom) {
        if (v.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
            p.setMargins(left, top, right, bottom);
            v.requestLayout();
        }
    }

    public static void setMarginStart (View v, int margin) {
        if (v.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
            p.setMarginStart(margin);
            v.requestLayout();
        }
    }

    public static void setMarginEnd (View v, int margin) {
        if (v.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
            p.setMarginEnd(margin);
            v.requestLayout();
        }
    }
}

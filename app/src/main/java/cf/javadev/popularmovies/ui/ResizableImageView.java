package cf.javadev.popularmovies.ui;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;


public class ResizableImageView extends android.support.v7.widget.AppCompatImageView {

    private static final float RATIO = 1.5f;

    public ResizableImageView(Context context) {
        super(context);
    }

    public ResizableImageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public ResizableImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = Math.round(width * RATIO);
        setMeasuredDimension(width, height);
    }
}

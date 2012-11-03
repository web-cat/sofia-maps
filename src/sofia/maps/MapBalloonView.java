package sofia.maps;

import sofia.internal.JarResources;
import sofia.internal.events.EventDispatcher;
import sofia.view.FlexibleContentView;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.NinePatchDrawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MapBalloonView extends FrameLayout
{
    //~ Instance/static variables .............................................

    private static final EventDispatcher mapBalloonWasClicked =
            new EventDispatcher("mapBalloonWasClicked");

    private LinearLayout layout;
    private Rect imagePadding;
    private TextView title;
    private FlexibleContentView content;
    private Object currentItem;


    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    public MapBalloonView(Context context)
    {
        super(context);

        setClickable(true);
        setPadding(0, 0, 0, 25);
        setOnClickListener(new View.OnClickListener() {
            public void onClick(View v)
            {
                handleClick();
            }
        });

        layout = createLayout(context);

        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        lp.gravity = Gravity.NO_GRAVITY;
        addView(layout, lp);
    }


    // ----------------------------------------------------------
    private void handleClick()
    {
        mapBalloonWasClicked.dispatch(getContext(), currentItem);
    }


    // ----------------------------------------------------------
    private LinearLayout createLayout(Context context)
    {
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
//		layout.setMinimumWidth(200);

        Bitmap bitmap = JarResources.getBitmap(context, "balloon.9.png",
            MapBalloonView.class.getPackage().getName());
        byte[] chunk = bitmap.getNinePatchChunk();
        Rect padding = new Rect(8, 8, 8, 32);

        NinePatchDrawable drawable = new NinePatchDrawable(
                context.getResources(), bitmap, chunk, padding,
                "balloon.9.png");
        imagePadding = new Rect();
        drawable.getPadding(imagePadding);
        layout.setBackgroundDrawable(drawable);

        createFields(context, layout);

        return layout;
    }


    // ----------------------------------------------------------
    private void createFields(Context context, ViewGroup parent)
    {
        Resources res = context.getResources();

        title = new TextView(context);
        title.setTextColor(res.getColor(android.R.color.primary_text_light));
        title.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
        title.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        parent.addView(title, createFieldLayout());

        content = new FlexibleContentView(context);
        content.setTextColor(res.getColor(
                android.R.color.secondary_text_light));
        content.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12);
        content.setMaxHeight(150);
        parent.addView(content, createFieldLayout());
    }


    // ----------------------------------------------------------
    public void setFields(Object currentItem,
            String titleText, Object contentValue)
    {
        this.currentItem = currentItem;

        if (titleText != null)
        {
            title.setText(titleText);
            title.setVisibility(VISIBLE);

            if (content == null)
            {
                title.setGravity(Gravity.CENTER);
            }
            else
            {
                title.setGravity(Gravity.LEFT);
            }
        }
        else
        {
            title.setVisibility(GONE);
        }

        if (contentValue != null)
        {
            content.setContent(contentValue);
            content.setVisibility(VISIBLE);
        }
        else
        {
            content.setVisibility(GONE);
        }
    }


    // ----------------------------------------------------------
    private LinearLayout.LayoutParams createFieldLayout()
    {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        lp.gravity = Gravity.CENTER;
        return lp;
    }


    // ----------------------------------------------------------
    @Override
    public boolean onTouchEvent(MotionEvent e)
    {
        // FIXME

        float x = e.getX();
        float y = e.getY();

        if (x < imagePadding.left
                || y < imagePadding.top
                || x > getWidth() - imagePadding.right
                || y > getHeight() - imagePadding.bottom)
        {
            return false;
        }
        else
        {
            return super.onTouchEvent(e);
        }
    }
}

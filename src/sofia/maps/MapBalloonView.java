package sofia.maps;

import sofia.internal.JarResources;
import sofia.internal.MethodDispatcher;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.NinePatchDrawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MapBalloonView extends FrameLayout
{
	//~ Instance/static variables .............................................

	private LinearLayout layout;
	private TextView title;
	private TextView snippet;
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
		MethodDispatcher dispatcher = new MethodDispatcher(
				"onMapBalloonClicked", 1);
		dispatcher.callMethodOn(getContext(), currentItem);
	}


	// ----------------------------------------------------------
	private LinearLayout createLayout(Context context)
	{
		LinearLayout layout = new LinearLayout(context);
		layout.setOrientation(LinearLayout.VERTICAL);
//		layout.setMinimumWidth(200);

		Bitmap bitmap = JarResources.getBitmap(
            context, MapBalloonView.class, "balloon.9.png");
		byte[] chunk = bitmap.getNinePatchChunk();
		Rect padding = new Rect(8, 8, 8, 32);

		NinePatchDrawable drawable = new NinePatchDrawable(
				context.getResources(), bitmap, chunk, padding,
				"balloon.9.png");
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

		snippet = new TextView(context);
		snippet.setTextColor(res.getColor(
				android.R.color.secondary_text_light));
		snippet.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12);
		parent.addView(snippet, createFieldLayout());
	}


	// ----------------------------------------------------------
	public void setFields(Object currentItem,
			String titleText, String snippetText)
	{
		this.currentItem = currentItem;

		if (titleText != null)
		{
			title.setText(titleText);
			title.setVisibility(VISIBLE);

			if (snippetText == null)
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

		if (snippetText != null)
		{
			snippet.setText(snippetText);
			snippet.setVisibility(VISIBLE);
		}
		else
		{
			snippet.setVisibility(GONE);
		}
	}


	// ----------------------------------------------------------
	private LinearLayout.LayoutParams createFieldLayout()
	{
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		lp.gravity = Gravity.CENTER_VERTICAL;
		return lp;
	}
}

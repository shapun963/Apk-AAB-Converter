package com.shapun.apkaabconverter.util;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.util.TypedValue;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;


public final class Utils {
    private Utils() {}

    public static void toast(Context context, Object obj) {
        Toast.makeText(context, String.valueOf(obj), Toast.LENGTH_SHORT).show();
    }

    public static String queryName(ContentResolver resolver, Uri uri) {
        Cursor returnCursor = resolver.query(uri, null, null, null, null);
        assert returnCursor != null;
        int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
        returnCursor.moveToFirst();
        String name = returnCursor.getString(nameIndex);
        returnCursor.close();
        return name;
    }
	public static void setPadding(@NonNull View view,int value){
		view.setPadding(value,value,value,value);
	}
    public static int dpToPx(Context context, int input) {
        return (int)
                TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP,
                        input,
                        context.getResources().getDisplayMetrics());
    }

}

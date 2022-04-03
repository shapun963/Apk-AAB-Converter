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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;


public final class Utils {
    private Utils() {}

    public static void toast(Context context, Object obj) {
        Toast.makeText(context, String.valueOf(obj), Toast.LENGTH_SHORT).show();
    }

    public static String queryName(@NonNull ContentResolver resolver,@NonNull Uri uri) {
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
	public static void copy(@NonNull Context ctx,@NonNull Uri uri,@NonNull Path outputPath){
        try(InputStream is = ctx.getContentResolver().openInputStream(uri)){
            Files.copy(is,outputPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void copy(@NonNull Context ctx,@NonNull Path inputPath,@NonNull Uri uri){
        try(OutputStream os = ctx.getContentResolver().openOutputStream(uri)){
            Files.copy(inputPath,os);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static int dpToPx(Context context, int input) {
        return (int)
                TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, input, context.getResources().getDisplayMetrics());
    }

}

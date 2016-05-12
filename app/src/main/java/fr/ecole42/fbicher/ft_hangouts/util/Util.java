package fr.ecole42.fbicher.ft_hangouts.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.widget.ImageView;

import java.io.FileNotFoundException;

/**
 * Contains auxiliary methods.
 * <p/>
 * Created by fbicher on 1/31/16.
 */
public class Util {

    public static final String PREF_LAST_VISIT = "pref_last_visit";
    public static final String TOAST_DATE_FORMAT = "yyyy.MM.dd H:mm:ss";
    public static final String SMS_DATE_FORMAT = "H:mm dd.MM.yyyy";
    public static final String ACTION_VIEW_SMS = "fr.ecole42.fbicher.ft_hangouts.action.VIEW_SMS";
    public static final String EXTRA_PHONE = "extra_phone";
    public static final String EXTRA_NAME = "extra_name";

    private Util() {
        // Can not be instantiated
    }

    public static Bitmap decodeUri(Uri selectedImage, int size, Context context)
            throws FileNotFoundException {

        // Decode image size
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(context.getContentResolver().openInputStream(selectedImage),
                null, options);

        // Find the correct scale value. It should be the power of 2.
        int width_tmp = options.outWidth, height_tmp = options.outHeight;
        int scale = 1;
        while (true) {
            if (width_tmp / 2 < size
                    || height_tmp / 2 < size) {
                break;
            }
            width_tmp /= 2;
            height_tmp /= 2;
            scale *= 2;
        }

        // Decode with inSampleSize
        BitmapFactory.Options newOptions = new BitmapFactory.Options();
        newOptions.inSampleSize = scale;
        return BitmapFactory.decodeStream(
                context.getContentResolver().openInputStream(selectedImage), null, newOptions);
    }

    public static void loadAvatar(Uri avatarUri, ImageView iv, int resId, int size,
                                  Context context) {
        if (avatarUri == null || avatarUri.toString().length() == 0) {
            iv.setScaleType(ImageView.ScaleType.FIT_CENTER);
            iv.setImageResource(resId);
        } else {
            try {
                Bitmap avatar = decodeUri(avatarUri, size, context);
                iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
                iv.setImageBitmap(avatar);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

}

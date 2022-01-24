package it.univaq.mwt.ifame.utility;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Base64;

import androidx.annotation.NonNull;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;


public class ImageManager {

    private static ImageManager instance;

    private final RequestQueue loaderRequestQueue;
    private final Context context;

    private ImageManager(Context context) {
        loaderRequestQueue = Volley.newRequestQueue(context);
        this.context = context;
    }


    public static ImageManager getInstance(Context context) {
        if (instance == null) instance = new ImageManager(context);
        return instance;
    }


    public void storeImage(Bitmap bitmap, String folder, String fileName, int compression, OnStoreCompleted onStoreCompleted) {

        File root = context.getFilesDir();
        File directory = new File(root, folder);
        if (!directory.exists()) directory.mkdir();

        Bitmap scaledBitmap = null;
        FileOutputStream fos = null;
        try {
            File file = new File(directory, fileName);
            if (!file.exists()) file.createNewFile();
            else file.delete();

            fos = new FileOutputStream(file);
            int[] size = scaleBitmap(bitmap);

            scaledBitmap = Bitmap.createScaledBitmap(bitmap, size[0], size[1], false);
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, compression, fos);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (fos != null) fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (onStoreCompleted != null) {
                onStoreCompleted.onCompleted(scaledBitmap);
            }
        }
    }

    public Bitmap loadImage(String data) {
        byte[] imageAsBytes = Base64.decode(data.getBytes(), Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(imageAsBytes, 0, imageAsBytes.length);
    }

    public Bitmap loadImage(String folder, String fileName) {

        File root = context.getFilesDir();
        File directory = new File(root, folder);
        if (directory.exists()) {
            File file = new File(directory, fileName);
            if (file.exists()) {
                return BitmapFactory.decodeFile(file.getAbsolutePath());
            }
        }
        return null;
    }

    public String toBase64(Bitmap data) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        data.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream .toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    public void getImageFromUrl(final String directory, final String fileName) {
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        storageRef.child(fileName).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                ImageRequest imageRequest = new ImageRequest(uri.toString(), new Response.Listener<Bitmap>() {
                    @Override
                    public void onResponse(Bitmap response) {
                        ImageManager.this.storeImage(response, directory, fileName, 100, null);
                    }
                }, 0, 0, Bitmap.Config.ARGB_8888, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                    }
                });

                loaderRequestQueue.add(imageRequest);

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                exception.printStackTrace();

            }
        });
    }


    public void deleteImage(final String folder, final String filename) {
        File root = context.getFilesDir();
        File directory = new File(root, folder);
        if (directory.exists()) {
            File file = new File(directory, filename);
            if (file.exists()) {
                file.delete();
            }
        }
    }


    public interface OnStoreCompleted {
        void onCompleted(Bitmap bitmap);
    }


    private int[] scaleBitmap(Bitmap bitmap) {
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();

        int[] size = new int[2];
        if (w >= h) {
            size[0] = 512;
            size[1] = 512 * h / w;
        } else {
            size[0] = 512 * w / h;
            size[1] = 512;
        }
        return size;
    }

}

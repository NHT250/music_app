package com.example.music_application.firebase;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.InputStream;
import java.util.UUID;

public class FirebaseStorageHelper {

    private final FirebaseStorage storage;

    public interface OnFileUploadListener {
        void onSuccess(String downloadUrl);
        void onFailure(Exception e);
    }

    public FirebaseStorageHelper() {
        storage = FirebaseStorage.getInstance();
    }

    public void uploadFile(Context context, Uri contentUri, String folderPath, OnFileUploadListener listener) {
        if (contentUri == null) {
            listener.onFailure(new IllegalArgumentException("File URI cannot be null."));
            return;
        }

        try {
            InputStream inputStream = context.getContentResolver().openInputStream(contentUri);
            if (inputStream == null) {
                throw new Exception("Failed to open input stream for URI: " + contentUri);
            }
            String originalFileName = getFileName(context, contentUri);
            String uniqueFileName = UUID.randomUUID().toString() + "_" + originalFileName;

            StorageReference storageRef = storage.getReference();
            StorageReference fileRef = storageRef.child(folderPath + "/" + uniqueFileName);

            UploadTask uploadTask = fileRef.putStream(inputStream);
            uploadTask.addOnSuccessListener(taskSnapshot -> fileRef.getDownloadUrl()
                    .addOnSuccessListener(downloadUrl -> listener.onSuccess(downloadUrl.toString()))
                    .addOnFailureListener(listener::onFailure))
                    .addOnFailureListener(listener::onFailure)
                    .addOnCompleteListener(task -> {
                        try {
                            inputStream.close();
                        } catch (Exception e) {
                            // Log or handle the exception on closing the stream
                        }
                    });

        } catch (Exception e) {
            listener.onFailure(e);
        }
    }

    private String getFileName(Context context, Uri uri) {
        String result = null;
        if (uri.getScheme() != null && uri.getScheme().equals("content")) {
            try (Cursor cursor = context.getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (nameIndex != -1) {
                        result = cursor.getString(nameIndex);
                    }
                }
            } catch (Exception e) {
                // Ignore and proceed to get path
            }
        }
        if (result == null) {
            result = uri.getPath();
            if (result != null) {
                int cut = result.lastIndexOf('/');
                if (cut != -1) {
                    result = result.substring(cut + 1);
                }
            }
        }
        return result != null ? result : "unknown_file";
    }
}

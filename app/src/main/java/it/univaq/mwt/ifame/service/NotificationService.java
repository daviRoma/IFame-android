package it.univaq.mwt.ifame.service;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

public class NotificationService  extends FirebaseMessagingService {


    private static final String TAG = NotificationService.class.getSimpleName();

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        for (Map.Entry<String, String> entry: remoteMessage.getData().entrySet()){

            Log.d(TAG, "onMessageReceived: key " + entry.getKey() + " value " + entry.getValue());

        }
        remoteMessage.getData();

    }
}

package com.example.haitao.myapplication;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;

/**
 * Created by haitao on 14/3/18.
 */

public class KeySender extends Service {

    public static KeySender keySender = null;

    private String TAG = "MessengerService";

    Messenger mClient = null;

    // Message codes to check against Message.what
    //
    // Message.what is a User-defined message code so
    // that the recipient can identify what the message is about.

    static final int MSG_REGISTER_CLIENT = 1;
    static final int MSG_UNREGISTER_CLIENT = 2;
    static final int MSG_KEY_CODE = 3;
    // Messenger object used by clients to send messages to IncomingHandler
    Messenger mMessenger = new Messenger(new IncomingHandler());

    // Incoming messages Handler
    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {

                case MSG_REGISTER_CLIENT:
                    mClient = msg.replyTo;
                    break;
                case MSG_UNREGISTER_CLIENT:
                    mClient = null;
                    break;
                case MSG_KEY_CODE:
                    send_key_code(msg.getData());
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    public KeySender() {
    }
    public void send_key_code(int code) {
        Bundle bundle = new Bundle();
        bundle.putInt("k", code);
        send_key_code(bundle);
    }
    private void send_key_code(Bundle bundle)
    {
        if(mClient!= null)
        {
            Log.i("KEY_SENDER", String.format("key code %d\n", bundle.getInt("k")));

            try {
                Message new_msg = Message.obtain(null, KeySender.MSG_KEY_CODE);
                new_msg.setData(bundle);
                mClient.send(new_msg);
            }
            catch(Exception e)
            {
                //    mClient = null;
            }
        }
    }
    @Override
    public void onCreate() {
        long tid = Thread.currentThread().getId();
        Log.d(TAG, "onCreate called");
        keySender= this;
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }
    /*
    Return our Messenger interface for sending messages to
    the service by the clients.
    */
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind done");
        return mMessenger.getBinder();
    }
}

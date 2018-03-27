package com.example.haitao.myapplication;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityEvent;

/**
 * Created by haitao on 12/3/18.
 */

public class KeyReceiveService extends AccessibilityService {

    private Messenger mService = null;

    @Override
    public void onAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
        System.err.println(accessibilityEvent.eventTypeToString(accessibilityEvent.getEventType()));
    }

    @Override
    public void onInterrupt() {

    }

    @Override
    public boolean onKeyEvent(KeyEvent k)
    {
        System.err.println(k.toString());

        if(k.getAction() == KeyEvent.ACTION_UP && (k.getDevice().getSources()& InputDevice.SOURCE_DPAD) != 0)
        {
            if(KeySender.keySender!= null)
            {
                KeySender.keySender.send_key_code(k.getKeyCode());
            }
            else if(mService!= null)
            {
                Message msg = Message.obtain(null,
                        KeySender.MSG_KEY_CODE);
                Bundle bundle = new Bundle();
                bundle.putInt("k", k.getKeyCode());
                msg.setData(bundle);
                try {
                    mService.send(msg);
                } catch (Exception e) {
                    //doUnbindService();
                    //doBindService();
                }
            }
        }
        return super.onKeyEvent(k);
    }
    @Override
    public void onServiceConnected() {
        Intent startIntent = new Intent(this, KeySender.class);
        startService(startIntent);

        long tid = Thread.currentThread().getId();
        AccessibilityServiceInfo info = getServiceInfo();
        // Set the type of events that this service wants to listen to.  Others
        // won't be passed to this service.
        info.eventTypes = AccessibilityEvent.TYPES_ALL_MASK;

        info.flags |= AccessibilityServiceInfo.FLAG_REQUEST_FILTER_KEY_EVENTS;
        // If you only want this service to work with specific applications, set their
        // package names here.  Otherwise, when the service is activated, it will listen
        // to events from all applications.
        info.packageNames = null;//new String[1];
        //info.packageNames[0] = this.getApplication().getPackageName();

        // Set the type of feedback your service will provide.
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;

        // Default services are invoked only if no package-specific ones are present
        // for the type of AccessibilityEvent generated.  This service *is*
        // application-specific, so the flag isn't necessary.  If this was a
        // general-purpose service, it would be worth considering setting the
        // DEFAULT flag.

        // info.flags = AccessibilityServiceInfo.DEFAULT;

        info.notificationTimeout = 100;
        this.setServiceInfo(info);

        if(KeySender.keySender == null) {
            doBindService();
        }
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // This is called when the connection with the service has been
            // established, giving us the service object we can use to
            // interact with the service.  We are communicating with our
            // service through an IDL interface, so get a client-side
            // representation of that from the raw service object.
            mService = new Messenger(service);
        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            mService = null;
        }
    };


    void doBindService() {
        // Establish a connection with the service.  We use an explicit
        // class name because there is no reason to be able to let other
        // applications replace our component.
        bindService(new Intent(KeyReceiveService.this,
                KeySender.class), mConnection, Context.BIND_AUTO_CREATE);
    }

    void doUnbindService() {
            // Detach our existing connection.
            unbindService(mConnection);
    }

}

package com.example.haitao.myapplication;

import android.app.Instrumentation;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        long tid = Thread.currentThread().getId();
        doBindService();
    }





    private Messenger mService = null;

    static HandlerThread thread= new HandlerThread("test");
    static {
        thread.start();
    }

    /**
     * Handler of incoming messages from service.
     */
    class IncomingHandler extends Handler {
        private Instrumentation m_Instrumentation;
        public IncomingHandler()
        {
            super(thread.getLooper());
            m_Instrumentation = new Instrumentation();
        }
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case KeySender.MSG_KEY_CODE:
                    switch(msg.getData().getInt("k"))
                    {
                        case KeyEvent.KEYCODE_DPAD_UP:
                            m_Instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_A);
                            break;
                        case KeyEvent.KEYCODE_DPAD_DOWN:
                            m_Instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_B);
                            break;
                        case KeyEvent.KEYCODE_DPAD_LEFT:
                            m_Instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_C);
                            break;
                        case KeyEvent.KEYCODE_DPAD_RIGHT:
                            m_Instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_D);
                            break;
                        default:
                            break;
                    }
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    /**
     * Target we publish for clients to send messages to IncomingHandler.
     */
    final Messenger mMessenger = new Messenger(new IncomingHandler());

    /**
     * Class for interacting with the main interface of the service.
     */
    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // This is called when the connection with the service has been
            // established, giving us the service object we can use to
            // interact with the service.  We are communicating with our
            // service through an IDL interface, so get a client-side
            // representation of that from the raw service object.
            mService = new Messenger(service);

            // We want to monitor the service for as long as we are
            // connected to it.
            try {
                Message msg = Message.obtain(null,
                        KeySender.MSG_REGISTER_CLIENT);
                msg.replyTo = mMessenger;
                mService.send(msg);

            } catch (RemoteException e) {
                // In this case the service has crashed before we could even
                // do anything with it; we can count on soon being
                // disconnected (and then reconnected if it can be restarted)
                // so there is no need to do anything here.
            }
        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            mService = null;
        }
    };
    public void onClick(View view)
    {
        Intent intent = new Intent(this, Main2Activity.class);
        startActivity(intent);
    }
    void doBindService() {
        // Establish a connection with the service.  We use an explicit
        // class name because there is no reason to be able to let other
        // applications replace our component.
        bindService(new Intent(MainActivity.this,
                KeySender.class), mConnection, Context.BIND_AUTO_CREATE);
    }


    void doUnbindService() {
        if (mService != null) {
            try {
                Message msg = Message.obtain(null,
                        KeySender.MSG_UNREGISTER_CLIENT);
                msg.replyTo = mMessenger;
                mService.send(msg);
            } catch (RemoteException e) {
                // There is nothing special we need to do if the service
                // has crashed.
            }
        }

        // Detach our existing connection.
        unbindService(mConnection);
    }



}

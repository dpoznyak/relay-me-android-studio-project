package com.tinywebgears.relayme.service.call;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.SubscriptionManager.OnSubscriptionsChangedListener;
import android.telephony.TelephonyManager;

import com.tinywebgears.relayme.app.CustomApplication;
import com.tinywebgears.relayme.service.AbstractBroadcastReceiver;
import com.tinywebgears.relayme.service.LogStoreHelper;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

@TargetApi(Build.VERSION_CODES.LOLLIPOP_MR1)
public class PhoneStateReceiver extends AbstractBroadcastReceiver
{
    class SubHandler extends OnSubscriptionsChangedListener {

        private AbstractBroadcastReceiver phoneStateReceiver;
        private Context context;
        public SubHandler(AbstractBroadcastReceiver phoneStateReceiver, Context context) {
            this.phoneStateReceiver = phoneStateReceiver;
            this.context = context;
            Listen();
        }

        @Override
        public synchronized  void onSubscriptionsChanged() {
            Listen();

        }

        private void Listen() {
            TelephonyManager manager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            SubscriptionManager subManager = SubscriptionManager.from(context);
            List<SubscriptionInfo> subs = subManager.getActiveSubscriptionInfoList();

            for (SubscriptionInfo sub: subs) {

                int subscriptionId = sub.getSubscriptionId();
                if (subListeners.get(subscriptionId) == null) {
                    MissedCallListener missedCallListener = new MissedCallListener(context, phoneStateReceiver, subscriptionId );
                    manager.listen(missedCallListener, android.telephony.PhoneStateListener.LISTEN_CALL_STATE);
                    subListeners.put(subscriptionId, missedCallListener);
                }
            }

            LogStoreHelper.info(context, "Registring missed call listener.");
        }

        Dictionary<Integer, MissedCallListener> subListeners = new Hashtable<>();
    }

    SubHandler handler;

    @TargetApi(Build.VERSION_CODES.LOLLIPOP_MR1)
    @Override
    public synchronized void onReceive(Context context, Intent intent)
    {
        if (handler == null ) {
            handler = new SubHandler(this, context);
            SubscriptionManager subManager = SubscriptionManager.from(context);
            subManager.addOnSubscriptionsChangedListener(handler);

        }

    }
}

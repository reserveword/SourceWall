package net.nashlegend.sourcewall;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;

import net.nashlegend.sourcewall.activities.MainActivity;
import net.nashlegend.sourcewall.activities.MessageCenterActivity;
import net.nashlegend.sourcewall.data.Consts;
import net.nashlegend.sourcewall.events.Emitter;
import net.nashlegend.sourcewall.events.NoticeNumChangedEvent;
import net.nashlegend.sourcewall.model.ReminderNoticeNum;
import net.nashlegend.sourcewall.request.SimpleCallBack;
import net.nashlegend.sourcewall.request.api.MessageAPI;
import net.nashlegend.sourcewall.util.PrefsUtil;

public class MessagePushService extends Service {
    public MessagePushService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        new Requester().start();
        return Service.START_STICKY;
    }

    private int message_number = 0;
    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private void checkUnread() {
        MessageAPI.getReminderAndNoticeNum(new SimpleCallBack<ReminderNoticeNum>() {
            @Override
            public void onSuccess(@NonNull ReminderNoticeNum result) {
                message_number = result.getNotice_num();
            }
        });
    }

    public void sendNotification(){
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(),0,new Intent(getApplicationContext(), MessageCenterActivity.class),0);
        Notification.Builder builder = new Notification.Builder(getApplicationContext())
                .setContentText("有"+ message_number + "条新消息")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("果壳的壳")
                .setContentIntent(pendingIntent);
        Notification notification;
        if(Build.VERSION.SDK_INT >= 16)notification = builder.build();
        else notification = builder.getNotification();
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        ((NotificationManager)getSystemService(NOTIFICATION_SERVICE)).notify(0,notification);
    }
    private class Requester extends Thread{
        @Override
        public void run() {
            try{
                for(;PrefsUtil.readBoolean(Consts.Keys.Key_Push_Enabled,false);Thread.sleep(300000)){
                    checkUnread();
                    if(message_number != 0)sendNotification();
                }
            }catch (Exception e){e.printStackTrace();}
        }
    }
}

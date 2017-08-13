package net.nashlegend.sourcewall;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.provider.Browser;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;

import net.nashlegend.sourcewall.activities.AnswerActivity;
import net.nashlegend.sourcewall.activities.ArticleActivity;
import net.nashlegend.sourcewall.activities.LoginActivity;
import net.nashlegend.sourcewall.activities.MainActivity;
import net.nashlegend.sourcewall.activities.MessageCenterActivity;
import net.nashlegend.sourcewall.activities.PostActivity;
import net.nashlegend.sourcewall.activities.PostListActivity;
import net.nashlegend.sourcewall.activities.QuestionActivity;
import net.nashlegend.sourcewall.activities.SingleReplyActivity;
import net.nashlegend.sourcewall.data.Consts;
import net.nashlegend.sourcewall.data.Mob;
import net.nashlegend.sourcewall.events.Emitter;
import net.nashlegend.sourcewall.events.NoticeNumChangedEvent;
import net.nashlegend.sourcewall.model.Article;
import net.nashlegend.sourcewall.model.Message;
import net.nashlegend.sourcewall.model.Notice;
import net.nashlegend.sourcewall.model.Post;
import net.nashlegend.sourcewall.model.Question;
import net.nashlegend.sourcewall.model.ReminderNoticeNum;
import net.nashlegend.sourcewall.model.SubItem;
import net.nashlegend.sourcewall.request.ResponseObject;
import net.nashlegend.sourcewall.request.SimpleCallBack;
import net.nashlegend.sourcewall.request.api.MessageAPI;
import net.nashlegend.sourcewall.request.api.UserAPI;
import net.nashlegend.sourcewall.request.parsers.MessageParser;
import net.nashlegend.sourcewall.util.PrefsUtil;
import net.nashlegend.sourcewall.util.ToastUtil;
import net.nashlegend.sourcewall.util.UiUtil;
import net.nashlegend.sourcewall.util.UrlCheckUtil;
import net.nashlegend.sourcewall.view.NoticeView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class MessagePushService extends Service {
    NotificationManager manager;
    private SparseArray<Notice> noticeList = new SparseArray<>();
    private SimpleCallBack<ArrayList<Notice>> unreadListener = new SimpleCallBack<ArrayList<Notice>>() {
        @Override
        public void onSuccess(@NonNull ArrayList<Notice> result) {
            for (int i = 0; i < noticeList.size(); i++) {
                int j = 0;
                for (; j < result.size(); j++) {
                    if (result.get(j).getId().hashCode() == noticeList.keyAt(i)) {
                        result.remove(j);
                        break;
                    }
                }
                if (j == result.size()) {
                    manager.cancel(noticeList.keyAt(i));
                    noticeList.delete(i);
                }
            }
            for (Notice notice : result) {
                showNotice(notice, getApplicationContext());
                noticeList.append(notice.getId().hashCode(),notice);
            }
        }
    };
    public MessagePushService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        manager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        new Requester().start();
        return Service.START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void checkUnread() {
        MessageAPI.getNoticeList(unreadListener);
    }

    public void sendNotification(){
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(),0,new Intent(getApplicationContext(), MessageCenterActivity.class),0);
        Notification.Builder builder = new Notification.Builder(getApplicationContext())
                .setContentText("有"+ noticeList.size() + "条新消息")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("果壳的壳")
                .setContentIntent(pendingIntent);
        Notification notification;
        if(Build.VERSION.SDK_INT >= 16)notification = builder.build();
        else notification = builder.getNotification();
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        manager.notify(0, notification);
    }
    private class Requester extends Thread{
        @Override
        public void run() {
            try{
                Thread.sleep(30000);
                for(;PrefsUtil.readBoolean(Consts.Keys.Key_Push_Enabled,false);Thread.sleep(30000)){
                    checkUnread();
                }
            }catch (Exception e){e.printStackTrace();}
        }
    }

    public void showNotice(Notice notice, Context context) {
        Log.i("mlgb", "showNotice: ");
        Log.i("mlgb", "showNotice: " + notice.getUrl());
        int id = notice.getId().hashCode();
        String content = notice.getContent();
        Intent intent = new Intent(context, NotificationReceiver.class);
        intent.putExtra("id", notice.getId());
        intent.putExtra("url",notice.getUrl());
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, id, intent, 0);
        Notification.Builder builder = new Notification.Builder(context)
                .setContentText(content)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("果壳的壳")
                .setContentIntent(pendingIntent);
        Notification notification;
        if (Build.VERSION.SDK_INT >= 16) notification = builder.build();
        else notification = builder.getNotification();
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        manager.notify(id, notification);
    }

    public static class NotificationReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            openUrl(intent.getStringExtra("id"), intent.getStringExtra("url"));
        }

        public void openUrl(String id, String url){
            if (UiUtil.shouldThrottle()) {
                return;
            }
            //这里要做两个请求，但是可以直接请求notice地址，让系统主动删除请求 TODO
            if (!UrlCheckUtil.redirectRequest(url, id)) {
                MessageAPI.ignoreOneNotice(id);
            }
        }
    }
}

package net.nashlegend.sourcewall.request.api;

import net.nashlegend.sourcewall.model.Message;
import net.nashlegend.sourcewall.model.Notice;
import net.nashlegend.sourcewall.model.Reminder;
import net.nashlegend.sourcewall.model.ReminderNoticeNum;
import net.nashlegend.sourcewall.request.JsonHandler;
import net.nashlegend.sourcewall.request.NetworkTask;
import net.nashlegend.sourcewall.request.RequestBuilder;
import net.nashlegend.sourcewall.request.RequestObject;
import net.nashlegend.sourcewall.request.RequestObject.CallBack;
import net.nashlegend.sourcewall.request.ResponseObject;
import net.nashlegend.sourcewall.request.parsers.BooleanParser;
import net.nashlegend.sourcewall.request.parsers.Parser;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by NashLegend on 16/3/15.
 */
public class MessageAPI extends APIBase {

    /**
     * 获取通知和站内信数量
     *
     * @param callBack
     * @return
     */
    public static NetworkTask<ReminderNoticeNum> getReminderAndNoticeNum(CallBack<ReminderNoticeNum> callBack) {
        Parser<ReminderNoticeNum> parser = new Parser<ReminderNoticeNum>() {
            @Override
            public ReminderNoticeNum parse(String str, ResponseObject<ReminderNoticeNum> responseObject) throws Exception {
                return ReminderNoticeNum.fromJson(JsonHandler.getUniversalJsonObject(str, responseObject));
            }
        };
        String url = "http://www.guokr.com/apis/community/rn_num.json";
        HashMap<String, String> pairs = new HashMap<>();
        pairs.put("_", String.valueOf(System.currentTimeMillis()));
        return new RequestBuilder<ReminderNoticeNum>()
                .get()
                .url(url)
                .params(pairs)
                .parser(parser)
                .callback(callBack)
                .requestAsync();
    }

    /**
     * 获取提醒列表
     *
     * @param offset
     * @param callBack
     * @return
     */
    public static NetworkTask<ArrayList<Reminder>> getReminderList(int offset, CallBack<ArrayList<Reminder>> callBack) {
        Parser<ArrayList<Reminder>> parser = new Parser<ArrayList<Reminder>>() {
            @Override
            public ArrayList<Reminder> parse(String str, ResponseObject<ArrayList<Reminder>> responseObject) throws Exception {
                JSONArray reminders = JsonHandler.getUniversalJsonArray(str, responseObject);
                ArrayList<Reminder> noticeList = new ArrayList<>();
                assert reminders != null;
                for (int i = 0; i < reminders.length(); i++) {
                    noticeList.add(Reminder.fromJson(reminders.getJSONObject(i)));
                }
                return noticeList;
            }
        };
        String url = "http://www.guokr.com/apis/community/reminder.json";
        HashMap<String, String> pairs = new HashMap<>();
        pairs.put("_", System.currentTimeMillis() + "");
        pairs.put("limit", "20");
        pairs.put("offset", String.valueOf(offset));
        return new RequestBuilder<ArrayList<Reminder>>()
                .get()
                .url(url)
                .params(pairs)
                .parser(parser)
                .callback(callBack)
                .requestAsync();
    }

    /**
     * 获取通知详情列表，一次性取得全部
     *
     * @param callBack
     * @return
     */
    public static NetworkTask<ArrayList<Notice>> getNoticeList(CallBack<ArrayList<Notice>> callBack) {
        Parser<ArrayList<Notice>> parser = new Parser<ArrayList<Notice>>() {
            @Override
            public ArrayList<Notice> parse(String str, ResponseObject<ArrayList<Notice>> responseObject) throws Exception {
                JSONArray notices = JsonHandler.getUniversalJsonArray(str, responseObject);
                ArrayList<Notice> noticeList = new ArrayList<>();
                assert notices != null;
                for (int i = 0; i < notices.length(); i++) {
                    JSONObject noticesObject = notices.getJSONObject(i);
                    Notice notice = Notice.fromJson(noticesObject);
                    noticeList.add(notice);
                }
                return noticeList;
            }
        };
        String url = "http://www.guokr.com/apis/community/notice.json";
        HashMap<String, String> pairs = new HashMap<>();
        pairs.put("_", System.currentTimeMillis() + "");
        pairs.put("limit", "1024");
        pairs.put("offset", "0");
        return new RequestBuilder<ArrayList<Notice>>()
                .get()
                .url(url)
                .params(pairs)
                .parser(parser)
                .callback(callBack)
                .requestAsync();
    }

    /**
     * 忽略所有消息，相当于ignoreOneNotice("")
     *
     * @param callBack
     * @return
     */
    public static NetworkTask<Boolean> ignoreAllNotice(CallBack<Boolean> callBack) {
        String url = "http://www.guokr.com/apis/community/notice_ignore.json";
        return new RequestBuilder<Boolean>()
                .put()
                .url(url)
                .parser(new BooleanParser())
                .callback(callBack)
                .requestAsync();
    }

    /**
     * 忽略一条通知消息，返回的是剩余的通知详情列表
     *
     * @param noticeID
     * @param callBack
     * @return
     */
    public static NetworkTask<ArrayList<Notice>> ignoreOneNotice(String noticeID, CallBack<ArrayList<Notice>> callBack) {
        Parser<ArrayList<Notice>> parser = new Parser<ArrayList<Notice>>() {
            @Override
            public ArrayList<Notice> parse(String str, ResponseObject<ArrayList<Notice>> responseObject) throws Exception {
                JSONObject nObject = JsonHandler.getUniversalJsonObject(str, responseObject);
                JSONArray notices = JsonHandler.getJsonArray(nObject, "list");
                ArrayList<Notice> noticeList = new ArrayList<>();
                assert notices != null;
                for (int i = 0; i < notices.length(); i++) {
                    JSONObject noticesObject = notices.getJSONObject(i);
                    Notice notice = Notice.fromJson(noticesObject);
                    noticeList.add(notice);
                }
                return noticeList;
            }
        };
        String url = "http://www.guokr.com/apis/community/notice_ignore.json";
        HashMap<String, String> pairs = new HashMap<>();
        pairs.put("nid", noticeID);
        pairs.put("_", System.currentTimeMillis() + "");
        return new RequestBuilder<ArrayList<Notice>>()
                .put()
                .url(url)
                .params(pairs)
                .parser(parser)
                .callback(callBack)
                .requestAsync();
    }

    /**
     * 忽略一条通知消息，返回的是剩余的通知详情列表
     *
     * @return RequestObject
     */
    public static NetworkTask<ArrayList<Notice>> ignoreOneNotice(String noticeID) {
        return ignoreOneNotice(noticeID, null);
    }

    /**
     * 获取站内信详情列表，与某人的对话只显示最近一条。目前还不知道获取对话接口
     *
     * @param offset
     * @param callBack
     * @return RequestObject
     */
    public static NetworkTask<ArrayList<Message>> getMessageList(int offset, CallBack<ArrayList<Message>> callBack) {
        Parser<ArrayList<Message>> parser = new Parser<ArrayList<Message>>() {
            @Override
            public ArrayList<Message> parse(String str, ResponseObject<ArrayList<Message>> responseObject) throws Exception {
                JSONArray notices = JsonHandler.getUniversalJsonArray(str, responseObject);
                ArrayList<Message> noticeList = new ArrayList<>();
                assert notices != null;
                for (int i = 0; i < notices.length(); i++) {
                    JSONObject noticesObject = notices.getJSONObject(i);
                    Message message = Message.fromJson(noticesObject);
                    noticeList.add(message);
                }
                return noticeList;
            }
        };
        String url = "http://www.guokr.com/apis/community/user/message.json";
        HashMap<String, String> pairs = new HashMap<>();
        pairs.put("limit", "20");
        pairs.put("offset", String.valueOf(offset));
        return new RequestBuilder<ArrayList<Message>>()
                .get()
                .url(url)
                .params(pairs)
                .parser(parser)
                .callback(callBack)
                .requestAsync();
    }

    /**
     * 根据id获取一条站内信
     *
     * @param id
     * @param callBack
     * @return RequestObject
     */
    public static NetworkTask<Message> getOneMessage(String id, CallBack<Message> callBack) {
        Parser<Message> parser = new Parser<Message>() {
            @Override
            public Message parse(String str, ResponseObject<Message> responseObject) throws Exception {
                JSONObject noticesObject = JsonHandler.getUniversalJsonObject(str, responseObject);
                return Message.fromJson(noticesObject);
            }
        };
        String url = "http://www.guokr.com/apis/community/user/message/" + id + ".json";
        return new RequestBuilder<Message>()
                .get()
                .url(url)
                .parser(parser)
                .callback(callBack)
                .requestAsync();
    }

}

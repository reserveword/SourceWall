
package com.example.outerspace.connection.api;

import java.io.IOException;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.example.outerspace.connection.HttpFetcher;
import com.example.outerspace.model.Question;
import com.example.outerspace.model.QuestionAnswer;
import com.example.outerspace.model.QuestionAnswerComment;
import com.example.outerspace.model.QuestionComment;

public class QuestionAPI extends APIBase {

    public QuestionAPI() {
        // TODO Auto-generated constructor stub
    }

    public static ArrayList<Question> getQuestionsByTagFromJsonUrl(String tag, int offset) {
        // 比html还特么浪费流量…………
        ArrayList<Question> questions = new ArrayList<>();
        String url = "http://apis.guokr.com/ask/question.json?retrieve_type=by_tag&limit=1&tag_name="
                + tag + "&offset=" + offset;
        String jString = HttpFetcher.get(url);
        try {
            JSONObject jss = new JSONObject(jString);
            boolean ok = jss.getBoolean("ok");
            if (ok) {
                JSONArray results = jss.getJSONArray("result");
                for (int i = 0; i < results.length(); i++) {
                    JSONObject jsonObject = results.getJSONObject(i);
                    Question question = new Question();
                    question.setAnswerNum(getJsonInt(jsonObject, "answers_count"));
                    question.setAuthor(jsonObject.getJSONObject("author").getString("nickname"));
                    question.setAuthorID(jsonObject.getJSONObject("author").getString("url")
                            .replaceAll("\\D+", ""));
                    question.setAuthorAvatarUrl(jsonObject.getJSONObject("author")
                            .getJSONObject("avatar")
                            .getString("small").replaceAll("\\?\\S*$", ""));
                    question.setSummary(getJsonString(jsonObject, "summary"));
                    question.setDate(getJsonString(jsonObject, "date_created"));
                    question.setFollowNum(getJsonInt(jsonObject, "followers_count"));
                    question.setId(getJsonString(jsonObject, "id"));
                    question.setTitle(getJsonString(jsonObject, "question"));
                    question.setUrl(getJsonString(jsonObject, "url"));
                    questions.add(question);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return questions;
    }

    public static ArrayList<Question> getHotQuestions(int pageNo) throws IOException {
        String url = "http://m.guokr.com/ask/hottest/?page=" + pageNo;
        return getQuestionsFromMobileUrl(url);
    }

    public static ArrayList<Question> getHighlightQuestions(int pageNo) throws IOException {
        String url = "http://m.guokr.com/ask/highlight/?page=" + pageNo;
        return getQuestionsFromMobileUrl(url);
    }

    public static ArrayList<Question> getQuestionsFromMobileUrl(String url) throws IOException {
        ArrayList<Question> questions = new ArrayList<>();
        Document doc = Jsoup.connect(url).get();
        Elements elements = doc.getElementsByClass("ask-list");
        if (elements.size() == 1) {
            Elements questioList = elements.get(0).getElementsByTag("li");
            for (int i = 0; i < questioList.size(); i++) {
                Question item = new Question();
                Element element = questioList.get(i);
                Element link = element.getElementsByTag("a").get(0);
                String title = link.getElementsByTag("h4").text();
                String id = link.attr("href").replaceAll("\\D+", "");
                String summary = link.getElementsByTag("p").text();
                item.setTitle(title);
                item.setId(id);
                item.setSummary(summary);
                questions.add(item);
            }
        }
        return questions;
    }

    public static Question getQuestionDetailByID(String id) {
        String url = "http://apis.guokr.com/ask/question/" + id + ".json";
        return getQuestionDetailFromJsonUrl(url);
    }

    public static Question getQuestionDetailFromJsonUrl(String url) {
        Question question = null;
        String jString = HttpFetcher.get(url);
        try {
            JSONObject jss = new JSONObject(jString);
            boolean ok = jss.getBoolean("ok");
            if (ok) {
                JSONObject result = getJsonObject(jss, "result");
                question = new Question();
                question.setAnswerable(getJsonBoolean(result, "is_answerable"));
                question.setAnswerNum(getJsonInt(result, "answers_count"));
                question.setCommentNum(getJsonInt(result, "replies_count"));
                question.setAuthor(result.getJSONObject("author").getString("nickname"));
                question.setAuthorID(result.getJSONObject("author").getString("url")
                        .replaceAll("\\D+", ""));
                question.setAuthorAvatarUrl(result.getJSONObject("author").getJSONObject("avatar")
                        .getString("small").replaceAll("\\?\\S*$", ""));
                question.setContent(getJsonString(result, "annotation"));
                question.setDate(getJsonString(result, "date_created"));
                question.setFollowNum(getJsonInt(result, "followers_count"));
                question.setId(getJsonString(result, "id"));
                question.setRecommendNum(getJsonInt(result, "recommends_count"));
                question.setTitle(getJsonString(result, "question"));
                question.setUrl(getJsonString(result, "url"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return question;
    }

    public static ArrayList<QuestionAnswer> getQuestionAnswers(String id, int offset) {
        ArrayList<QuestionAnswer> answers = new ArrayList<>();
        String url = "http://apis.guokr.com/ask/answer.json?retrieve_type=by_question&limit=10&question_id="
                + id + "&offset=" + offset;
        String jString = HttpFetcher.get(url);
        try {
            JSONObject jss = new JSONObject(jString);
            boolean ok = jss.getBoolean("ok");
            if (ok) {
                JSONArray comments = jss.getJSONArray("result");
                for (int i = 0; i < comments.length(); i++) {
                    JSONObject jo = comments.getJSONObject(i);
                    QuestionAnswer ans = new QuestionAnswer();
                    ans.setAuthor(getJsonString(getJsonObject(jo, "author"), "nickname"));
                    ans.setAuthorID(getJsonString(getJsonObject(jo, "author"), "url")
                            .replaceAll("\\D+", ""));
                    ans.setAuthorAvatarUrl(jo.getJSONObject("author").getJSONObject("avatar")
                            .getString("large"));
                    ans.setAuthorTitle(getJsonString(getJsonObject(jo, "author"), "title"));
                    ans.setCommentNum(getJsonInt(jo, "replies_count"));
                    ans.setContent(getJsonString(jo, "html"));
                    ans.setDate_created(getJsonString(jo, "date_created"));
                    ans.setDate_modified(getJsonString(jo, "date_modified"));
                    ans.setHasDownVoted(getJsonBoolean(jo, "current_user_has_buried"));
                    ans.setHasUpvoted(getJsonBoolean(jo, "current_user_has_supported"));
                    ans.setHasThanked(getJsonBoolean(jo, "current_user_has_thanked"));
                    ans.setID(getJsonString(jo, "id"));
                    ans.setQuestionID(getJsonString(jo, "question_id"));
                    ans.setUpvoteNum(getJsonInt(jo, "supportings_count"));
                    answers.add(ans);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return answers;
    }

    public static ArrayList<QuestionComment> getQuestionComments(String id, int offset) {
        ArrayList<QuestionComment> list = new ArrayList<>();
        String url = "http://www.guokr.com/apis/ask/question_reply.json?retrieve_type=by_question&question_id="
                + id + "&offset=" + offset;
        String jString = HttpFetcher.get(url);
        try {
            JSONObject jss = new JSONObject(jString);
            boolean ok = jss.getBoolean("ok");
            if (ok) {
                JSONArray comments = jss.getJSONArray("result");
                for (int i = 0; i < comments.length(); i++) {
                    JSONObject jsonObject = comments.getJSONObject(i);
                    QuestionComment comment = new QuestionComment();
                    comment.setAuthor(jsonObject.getJSONObject("author").getString("nickname"));
                    comment.setAuthorID(jsonObject.getJSONObject("author").getString("url")
                            .replaceAll("\\D+", ""));
                    comment.setContent(getJsonString(jsonObject, "html"));
                    comment.setDate(getJsonString(jsonObject, "date_created"));
                    comment.setID(getJsonString(jsonObject, "id"));
                    comment.setQuestionID(getJsonString(jsonObject, "question_id"));
                    list.add(comment);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return list;
    }

    public static ArrayList<QuestionAnswerComment> getAnswerComments(String id, int offset) {
        ArrayList<QuestionAnswerComment> list = new ArrayList<>();
        String url = "http://www.guokr.com/apis/ask/answer_reply.json?retrieve_type=by_answer&limit=10&answer_id="
                + id + "&offset=" + offset;
        String jString = HttpFetcher.get(url);
        try {
            JSONObject jss = new JSONObject(jString);
            boolean ok = jss.getBoolean("ok");
            if (ok) {
                JSONArray comments = jss.getJSONArray("result");
                for (int i = 0; i < comments.length(); i++) {
                    JSONObject jsonObject = comments.getJSONObject(i);
                    QuestionAnswerComment comment = new QuestionAnswerComment();
                    comment.setAuthor(jsonObject.getJSONObject("author").getString("nickname"));
                    comment.setAuthorID(jsonObject.getJSONObject("author").getString("url")
                            .replaceAll("\\D+", ""));
                    comment.setContent(getJsonString(jsonObject, "html"));
                    comment.setDate(getJsonString(jsonObject, "date_created"));
                    comment.setID(getJsonString(jsonObject, "id"));
                    comment.setAnswerID(getJsonString(jsonObject, "question_id"));
                    list.add(comment);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return list;
    }
}

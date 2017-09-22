package jp.trackparty.api;

import org.json.JSONObject;

import bolts.Task;

public interface ApiV1 {
    Task<JSONObject> createOneTimePassword(final String email);

    Task<JSONObject> createSession(final String email, final String password);

    Task<JSONObject> createSession(final String ontTimePassword);

    Task<JSONObject> updateSession();

    Task<JSONObject> deleteSession();

    Task<JSONObject> getCurrentUser();

    Task<JSONObject> getUserTransportPlan();

    Task<JSONObject> getDestinationDetail(long id);

    Task<JSONObject> uploadLocationLog(String uuid, double latitude, double longitude, double accuracy, long timestamp);

    /**
     * プッシュのエンドポイント登録
     *
     * @param identifier OneSignalのPlayer ID
     * @return
     */
    Task<JSONObject> registerPushNotificationEndpoint(String identifier);

    Task<JSONObject> startTransportItem(long transportItemId);

    /**
     * 休憩
     */
    Task<JSONObject> pauseOngoingTransport();

    /**
     * 走行
     */
    Task<JSONObject> resumeOngoingTransport();

    /**
     * 配達完了
     */
    Task<JSONObject> completeOngoingTransport();
}

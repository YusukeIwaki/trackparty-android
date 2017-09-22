package jp.trackparty.api;

/**
 * 認証トークンの取得・削除のインターフェース
 */
public interface AuthenticationProvider {
    /**
     * 保存されたトークンを返します。
     * 保存されたトークンがないときは、nullを返して下さい。
     */
    String getToken();

    /**
     * 保存されたトークンを削除します
     */
    void clearToken();
}

package jp.trackparty.api.exception;

/**
 * hostnameが無い時に飛ばすエラー
 */
public class ServerCofigurationRequiredException extends RuntimeException {
    public ServerCofigurationRequiredException() {
        super("hostname is null");
    }
}

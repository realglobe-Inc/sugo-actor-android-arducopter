package jp.realglobe.sugo.module.arducopter;

/**
 * 通知イベント。
 * Created by fukuchidaisuke on 16/12/12.
 */
public enum Event {

    /**
     * 接続した。
     * 添付データ無し
     */
    connected,

    /**
     * 接続が切れた。
     * 添付データ無し
     */
    disconnected,

    /**
     * 機種通知。
     * <table border=1>
     * <caption>添付データ</caption>
     * <tr><th>type</th><th>機種名</th></tr>
     * <tr><th>firmware</th><th>ファームウェア名</th></tr>
     * <tr><th>version</th><th>ファームウェアバージョン</th></tr>
     * </table>
     */
    type,

    /**
     * 動作モード通知。
     * <table border=1>
     * <caption>添付データ</caption>
     * <tr><th>mode</th><th>動作モード名</th></tr>
     * </table>
     */
    mode,

    /**
     * 駆動開始通知。
     * 添付データ無し
     */
    armed,

    /**
     * 駆動終了通知。
     * 添付データ無し
     */
    disarmed,

    /**
     * 速さ通知。
     * <table border=1>
     * <caption>添付データ</caption>
     * <tr><th>ground</th><th>対地速さ</th></tr>
     * <tr><th>air</th><th>対気速さ</th></tr>
     * <tr><th>vertical</th><th>垂直方向の速さ</th></tr>
     * </table>
     */
    speed,

    /**
     * バッテリーの状態通知。
     * <table border=1>
     * <caption>添付データ</caption>
     * <tr><th>remain</th><th>残り</th></tr>
     * <tr><th>voltage</th><th>電圧</th></tr>
     * <tr><th>current</th><th>電流</th></tr>
     * </table>
     */
    battery,

    /**
     * 起点の通知。
     * <table border=1>
     * <caption>添付データ</caption>
     * <tr><th>coordinate</th><th>位置座標</th></tr>
     * </table>
     */
    home,

    /**
     * 現在位置の通知。
     * <table border=1>
     * <caption>添付データ</caption>
     * <tr><th>coordinate</th><th>位置座標</th></tr>
     * </table>
     */
    position,

    /**
     * 向きの通知。
     * <table border=1>
     * <caption>添付データ</caption>
     * <tr><th>pitch</th><th>上下を向く角度</th></tr>
     * <tr><th>pitchSpeed</th><th>上下を向く速さ</th></tr>
     * <tr><th>roll</th><th>頭を横に傾ける角度</th></tr>
     * <tr><th>rollSpeed</th><th>頭を横に傾ける速さ</th></tr>
     * <tr><th>yaw</th><th>左右を向く角度</th></tr>
     * <tr><th>yawSpeed</th><th>左右を向く速さ</th></tr>
     * </table>
     */
    attitude,

    /**
     * 読み込んだミッションの通知。
     * <table border=1>
     * <caption>添付データ</caption>
     * <tr><th>commands</th><th>コマンド列</th></tr>
     * </table>
     */
    mission,

    /**
     * ミッション保存完了通知。
     * 添付データ無し
     */
    missionSaved,

    /**
     * 到達したミッションコマンドの通知。
     * <table border=1>
     * <caption>添付データ</caption>
     * <tr><th>index</th><th>到達した位置</th></tr>
     * </table>
     */
    commandReached,

}

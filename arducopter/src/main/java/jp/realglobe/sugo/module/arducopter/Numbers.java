package jp.realglobe.sugo.module.arducopter;

/**
 * JSON の数値周りの変換関数
 * Created by fukuchidaisuke on 16/12/08.
 */
final class Numbers {

    private Numbers() {
    }

    /**
     * JSON 互換形式から変換する
     *
     * @param n JSON 互換の数値データ
     * @return double 値
     */
    static double decodeDouble(Object n) {
        if (n instanceof Double) {
            return (Double) n;
        } else if (n instanceof Float) {
            return ((Float) n).doubleValue();
        } else if (n instanceof Long) {
            return ((Long) n).doubleValue();
        } else if (n instanceof Integer) {
            return ((Integer) n).doubleValue();
        } else {
            throw new IllegalArgumentException("not a number");
        }
    }

    /**
     * JSON 互換形式から変換する
     *
     * @param n JSON 互換の数値データ
     * @return int 値
     */
    static int decodeInt(Object n) {
        if (n instanceof Double) {
            return ((Double) n).intValue();
        } else if (n instanceof Float) {
            return ((Float) n).intValue();
        } else if (n instanceof Long) {
            return ((Long) n).intValue();
        } else if (n instanceof Integer) {
            return (Integer) n;
        } else {
            throw new IllegalArgumentException("not a number");
        }
    }

}

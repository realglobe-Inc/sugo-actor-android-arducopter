package jp.realglobe.sugo.module.arducopter;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * UDP の接続指定
 * <p>
 * Created by fukuchidaisuke on 16/12/01.
 */
final class UdpInfo {

    private static final String LOCAL_REMOTE_SEPARATOR = "<>";

    private final int localPort;
    private final String remoteHost;
    private final int remotePort;

    private UdpInfo(int localPort, String remoteHost, int remotePort) {
        this.localPort = localPort;
        this.remoteHost = remoteHost;
        this.remotePort = remotePort;
    }

    static UdpInfo parse(String address) {
        final int sep = address.indexOf(LOCAL_REMOTE_SEPARATOR);
        if (sep < 0) {
            return parseSingle(address);
        }

        final UdpInfo local = parseSingle(address.substring(0, sep));
        final UdpInfo remote = parseSingle(address.substring(sep + LOCAL_REMOTE_SEPARATOR.length()));

        return new UdpInfo(local.getLocalPort(), remote.getRemoteHost(), remote.getRemotePort());
    }

    private static UdpInfo parseSingle(String address) {
        // ローカルポートかリモートサーバーの指定

        try {
            final int localPort = Integer.parseInt(address);
            // ローカルポートの指定だった。
            return new UdpInfo(localPort, null, 0);
        } catch (NumberFormatException ignored) {
        }

        // リモートサーバーの指定のはず

        final URI uri;
        try {
            uri = new URI("my://" + address);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(e);
        }
        if (uri.getHost() == null || uri.getHost().isEmpty()) {
            throw new IllegalArgumentException("No server host: " + address);
        }
        return new UdpInfo(0, uri.getHost(), uri.getPort());
    }

    int getLocalPort() {
        return this.localPort;
    }

    String getRemoteHost() {
        return this.remoteHost;
    }

    int getRemotePort() {
        return this.remotePort;
    }

}

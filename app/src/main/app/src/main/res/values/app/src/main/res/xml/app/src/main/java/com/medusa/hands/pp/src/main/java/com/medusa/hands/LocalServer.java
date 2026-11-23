package com.medusa.hands;

import android.content.Context;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicBoolean;

public class LocalServer {

    public static final int PORT = 8765;
    public static final String TOKEN = "medusa-local-123";

    private static final AtomicBoolean STARTED = new AtomicBoolean(false);

    public static void start(final Context ctx) {
        if (!STARTED.compareAndSet(false, true)) return;
        Thread t = new Thread(() -> {
            try {
                ServerSocket ss = new ServerSocket(PORT, 50, InetAddress.getByName("127.0.0.1"));
                while (true) {
                    Socket s = ss.accept();
                    try {
                        handle(s);
                    } catch (Exception e) {
                        // per-request error, ignore
                    } finally {
                        try { s.close(); } catch (Exception ignored) { }
                    }
                }
            } catch (Exception e) {
                STARTED.set(false);
            }
        });
        t.setDaemon(true);
        t.start();
    }

    private static void handle(Socket s) throws Exception {
        BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream(), "UTF-8"));
        String requestLine = in.readLine();
        if (requestLine == null) return;

        String[] parts = requestLine.split(" ");
        String target = parts.length > 1 ? parts[1] : "/";

        String path = target;
        String query = "";
        int q = target.indexOf('?');
        if (q >= 0) {
            path = target.substring(0, q);
            query = target.substring(q + 1);
        }

        if (!tokenOk(query)) {
            respond(s, 401, "text/plain", "unauthorized");
            return;
        }

        if (path.equals("/ping")) {
            respond(s, 200, "text/plain", "ok");
        } else if (path.equals("/screen")) {
            respond(s, 200, "application/json", screenJson());
        } else {
            respond(s, 404, "text/plain", "not found");
        }
    }

    private static boolean tokenOk(String query) {
        for (String kv : query.split("&")) {
            int e = kv.indexOf('=');
            if (e > 0 && kv.substring(0, e).equals("token")) {
                return kv.substring(e + 1).equals(TOKEN);
            }
        }
        return false;
    }

    private static String screenJson() {
        MedusaAccessibilityService svc = MedusaAccessibilityService.INSTANCE;
        if (svc == null) {
            return "{\"ok\":false,\"error\":\"accessibility service not running\"}";
        }
        String text = svc.collectScreenText();
        return "{\"ok\":true,\"text\":\"" + escape(text) + "\"}";
    }

    private static String escape(String s) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"':  sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '\t': sb.append("\\t"); break;
                default:
                    if (c < 0x20) sb.append(String.format("\\u%04x", (int) c));
                    else sb.append(c);
            }
        }
        return sb.toString();
    }

    private static void respond(Socket s, int code, String type, String body) throws Exception {
        byte[] b = body.getBytes("UTF-8");
        String head = "HTTP/1.1 " + code + " OK\r\n"
                + "Content-Type: " + type + "; charset=utf-8\r\n"
                + "Content-Length: " + b.length + "\r\n"
                + "Connection: close\r\n\r\n";
        OutputStream out = s.getOutputStream();
        out.write(head.getBytes("UTF-8"));
        out.write(b);
        out.flush();
    }
    }

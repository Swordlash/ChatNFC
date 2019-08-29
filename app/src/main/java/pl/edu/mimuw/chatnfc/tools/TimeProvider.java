package pl.edu.mimuw.chatnfc.tools;

import android.util.Log;

import com.google.firebase.database.ServerValue;

import org.apache.commons.net.ntp.NTPUDPClient;
import org.apache.commons.net.ntp.TimeInfo;

import java.net.InetAddress;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class TimeProvider {
    private TimeProvider() {
    }


    public static Future<Long> getCurrentTimeMillis() {
        ExecutorService exec = Executors.newSingleThreadExecutor();
        return exec.submit(() ->
        {
            String TIME_SERVER = "time.apple.com";
            NTPUDPClient timeClient = new NTPUDPClient();
            InetAddress inetAddress = InetAddress.getByName(TIME_SERVER);
            TimeInfo timeInfo = timeClient.getTime(inetAddress);
            return timeInfo.getMessage().getTransmitTimeStamp().getTime();
        });
    }

    public static long getCurrentTimeMillisOrLocal(long timeout) {
        Future<Long> future = getCurrentTimeMillis();
        long time = System.currentTimeMillis();

        try {
            time = future.get(timeout, TimeUnit.MILLISECONDS);
        } catch (ExecutionException | InterruptedException | TimeoutException ex) {
            Log.e("Error", ex.getMessage() != null ? ex.getMessage() : ex.toString());
            Log.e("TimeProvider", "Couldn't get actual time from server");
        }
        return time;
    }
}

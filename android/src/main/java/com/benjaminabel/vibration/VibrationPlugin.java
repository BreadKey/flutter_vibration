package com.benjaminabel.vibration;

import android.content.Context;
import android.media.AudioManager;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.media.AudioAttributes;

import java.util.List;

import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry.Registrar;

public class VibrationPlugin implements MethodCallHandler {
    private final Vibrator vibrator;
    private final AudioManager audioManager;
    private static final String CHANNEL = "vibration";

    private VibrationPlugin(Registrar registrar) {
        this.vibrator = (Vibrator) registrar.context().getSystemService(Context.VIBRATOR_SERVICE);
        this.audioManager = (AudioManager) registrar.context().getSystemService(Context.AUDIO_SERVICE);
    }

    public static void registerWith(Registrar registrar) {
        final MethodChannel channel = new MethodChannel(registrar.messenger(), CHANNEL);
        channel.setMethodCallHandler(new VibrationPlugin(registrar));
    }

    @SuppressWarnings("deprecation")
    private void vibrate(long duration, int amplitude) {
        if (canVibrate()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (vibrator.hasAmplitudeControl()) {
                    vibrator.vibrate(VibrationEffect.createOneShot(duration, amplitude), new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .build());
                } else {
                    vibrator.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE), new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .build());
                }
            } else {
                vibrator.vibrate(duration);
            }
        }
    }

    @SuppressWarnings("deprecation")
    private void vibrate(List<Integer> pattern, int repeat) {
        long[] patternLong = new long[pattern.size()];

        for (int i = 0; i < patternLong.length; i++) {
            patternLong[i] = pattern.get(i).intValue();
        }

        if (canVibrate()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createWaveform(patternLong, repeat), new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .build());
            } else {
                vibrator.vibrate(patternLong, repeat);
            }
        }
    }

    @SuppressWarnings("deprecation")
    private void vibrate(List<Integer> pattern, int repeat, List<Integer> intensities) {
        long[] patternLong = new long[pattern.size()];
        int[] intensitiesArray = new int[intensities.size()];

        for (int i = 0; i < patternLong.length; i++) {
            patternLong[i] = pattern.get(i).intValue();
        }

        for (int i = 0; i < intensitiesArray.length; i++) {
            intensitiesArray[i] = intensities.get(i);
        }

        if (canVibrate()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (vibrator.hasAmplitudeControl()) {
                    vibrator.vibrate(VibrationEffect.createWaveform(patternLong, intensitiesArray, repeat), new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .build());
                } else {
                    vibrator.vibrate(VibrationEffect.createWaveform(patternLong, repeat), new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .build());
                }
            } else {
                vibrator.vibrate(patternLong, repeat);
            }
        }
    }

    @Override
    public void onMethodCall(MethodCall call, Result result) {
        switch (call.method) {
            case "hasVibrator":
                result.success(vibrator.hasVibrator());

                break;
            case "hasAmplitudeControl":
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    result.success(vibrator.hasAmplitudeControl());
                } else {
                    // For earlier API levels, return false rather than raising a
                    // MissingPluginException in order to allow applications to handle
                    // non-existence gracefully.
                    result.success(false);
                }

                break;
            case "hasCustomVibrationsSupport":
                result.success(true);

                break;
            case "vibrate":
                int duration = call.argument("duration");
                List<Integer> pattern = call.argument("pattern");
                int repeat = call.argument("repeat");
                List<Integer> intensities = call.argument("intensities");
                int amplitude = call.argument("amplitude");

                if (pattern.size() > 0 && intensities.size() > 0) {
                    vibrate(pattern, repeat, intensities);
                } else if (pattern.size() > 0) {
                    vibrate(pattern, repeat);
                } else {
                    vibrate(duration, amplitude);
                }

                result.success(null);

                break;
            case "cancel":
                vibrator.cancel();

                result.success(null);

                break;
            default:
                result.notImplemented();
        }
    }

    private Boolean canVibrate() {
        return vibrator.hasVibrator() && audioManager.getRingerMode() != AudioManager.RINGER_MODE_SILENT;
    }
}

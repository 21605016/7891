package com.example.dongguavideo;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.VideoView;

public class MainActivity extends Activity {
    private VideoView videoView;
    private TextView status;
    private boolean permissionPageOpened = false;
    private boolean videoPrepared = false;
    private boolean volumePromptShown = false;
    private boolean playbackStarted = false;

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        applyImmersiveMode();

        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(0xFF101010);
        videoView = new VideoView(this);
        root.addView(videoView, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT, Gravity.CENTER));

        status = new TextView(this);
        status.setText("正在检查悬浮窗权限…");
        status.setTextColor(0xCCFFFFFF);
        status.setTextSize(13);
        status.setPadding(16, 10, 16, 10);
        status.setBackgroundColor(0x66000000);
        FrameLayout.LayoutParams statusParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
        statusParams.bottomMargin = 24;
        root.addView(status, statusParams);
        setContentView(root);

        Uri uri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.loop_video);
        videoView.setVideoURI(uri);
        videoView.setOnPreparedListener(mp -> {
            videoPrepared = true;
            mp.setLooping(true);
            if (hasOverlayPermission()) showVolumeChoiceAndPlay();
            else {
                videoView.pause();
                status.setText("请开启悬浮窗权限，返回后自动播放视频");
                requestOverlayPermissionIfNeeded();
            }
        });
        videoView.setOnCompletionListener(mp -> {
            videoView.seekTo(0);
            videoView.start();
        });
        videoView.setOnErrorListener((mp, what, extra) -> {
            status.setText("视频播放失败，请检查内置 MP4 文件");
            return true;
        });
    }

    private void applyImmersiveMode() {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
    }

    private boolean hasOverlayPermission() {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.M
                || Settings.canDrawOverlays(this);
    }

    private void requestOverlayPermissionIfNeeded() {
        if (hasOverlayPermission()) {
            if (videoPrepared) showVolumeChoiceAndPlay();
            return;
        }
        if (permissionPageOpened) return;
        permissionPageOpened = true;
        status.setText("请开启悬浮窗权限，返回后自动播放视频");
        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + getPackageName()));
        try {
            startActivity(intent);
        } catch (Exception e) {
            status.setText("无法自动打开权限页面，请在系统设置中手动开启悬浮窗权限");
        }
    }

    private void showVolumeChoiceAndPlay() {
        if (!videoPrepared || !hasOverlayPermission() || isFinishing()) return;
        if (playbackStarted) {
            if (!videoView.isPlaying()) videoView.start();
            return;
        }
        if (volumePromptShown) return;
        volumePromptShown = true;
        new AlertDialog.Builder(this)
                .setTitle("媒体音量")
                .setMessage("是否将媒体音量调到最大？播放期间仍可随时使用手机音量键调节。")
                .setPositiveButton("调到最大并播放", (dialog, which) -> {
                    AudioManager audio = (AudioManager) getSystemService(AUDIO_SERVICE);
                    if (audio != null) {
                        audio.setStreamVolume(AudioManager.STREAM_MUSIC,
                                audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0);
                    }
                    startPlayback();
                })
                .setNegativeButton("保持当前音量并播放", (dialog, which) -> startPlayback())
                .setOnCancelListener(dialog -> startPlayback())
                .show();
    }

    private void startPlayback() {
        if (videoView == null || !videoPrepared || !hasOverlayPermission()) return;
        playbackStarted = true;
        videoView.start();
        status.setText("本地视频自动循环播放中");
    }

    @Override protected void onResume() {
        super.onResume();
        applyImmersiveMode();
        if (videoView == null) return;
        if (hasOverlayPermission()) {
            permissionPageOpened = false;
            if (videoPrepared) showVolumeChoiceAndPlay();
        } else if (!permissionPageOpened) {
            requestOverlayPermissionIfNeeded();
        } else {
            status.setText("尚未获得悬浮窗权限；可稍后从系统设置开启");
        }
    }

    @Override protected void onPause() {
        if (videoView != null && videoView.isPlaying()) videoView.pause();
        super.onPause();
    }
}

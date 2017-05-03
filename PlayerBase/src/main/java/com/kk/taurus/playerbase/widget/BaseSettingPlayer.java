package com.kk.taurus.playerbase.widget;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;

import com.kk.taurus.playerbase.callback.OnErrorListener;
import com.kk.taurus.playerbase.callback.OnPlayerEventListener;
import com.kk.taurus.playerbase.setting.AspectRatio;
import com.kk.taurus.playerbase.setting.DecodeMode;
import com.kk.taurus.playerbase.setting.PlayerType;
import com.kk.taurus.playerbase.setting.Rate;
import com.kk.taurus.playerbase.setting.VideoData;

/**
 * Created by Taurus on 2017/3/25.
 */

public abstract class BaseSettingPlayer extends BaseBindPlayerEvent {

    protected VideoData dataSource;
    protected Rate rate;
    private int mPlayerType;
    protected int startPos;
    protected int mStatus = STATUS_IDLE;
    private DecodeMode mDecodeMode = DecodeMode.SOFT;
    private AspectRatio aspectRatio = AspectRatio.AspectRatio_FILL_PARENT;

    private OnPlayerEventListener mOnPlayerEventListener;
    private OnErrorListener mOnErrorListener;

    public BaseSettingPlayer(@NonNull Context context) {
        super(context);
    }

    public BaseSettingPlayer(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public BaseSettingPlayer(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void setDataSource(VideoData dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void changeVideoDefinition(Rate rate) {
        this.rate = rate;
    }

    @Override
    protected void onPlayerContainerHasInit(Context context) {
        super.onPlayerContainerHasInit(context);
        this.mPlayerType = PlayerType.getInstance().getDefaultPlayerType();
    }

    @Override
    protected void onPlayerEvent(int eventCode, Bundle bundle){
        onHandleStatus(eventCode,bundle);
        super.onPlayerEvent(eventCode, bundle);
        if(mOnPlayerEventListener!=null){
            mOnPlayerEventListener.onPlayerEvent(eventCode, bundle);
        }
    }

    private void onHandleStatus(int eventCode, Bundle bundle) {
        switch (eventCode){
            case OnPlayerEventListener.EVENT_CODE_RENDER_START:
                mStatus = STATUS_STARTED;
                break;
            case OnPlayerEventListener.EVENT_CODE_PLAY_PAUSE:
                mStatus = STATUS_PAUSED;
                break;
            case OnPlayerEventListener.EVENT_CODE_PLAY_RESUME:
                mStatus = STATUS_STARTED;
                break;
            case OnPlayerEventListener.EVENT_CODE_PLAYER_ON_STOP:
                mStatus = STATUS_STOPPED;
                break;
        }
    }

    @Override
    protected void onErrorEvent(int eventCode, Bundle bundle){
        onHandleErrorStatus(eventCode, bundle);
        super.onErrorEvent(eventCode, bundle);
        if(mOnErrorListener!=null){
            mOnErrorListener.onError(eventCode,bundle);
        }
    }

    private void onHandleErrorStatus(int eventCode, Bundle bundle) {
        switch (eventCode){
            case OnErrorListener.ERROR_CODE_COMMON:
                mStatus = STATUS_ERROR;
                break;
        }
    }

    public void setOnPlayerEventListener(OnPlayerEventListener onPlayerEventListener) {
        this.mOnPlayerEventListener = onPlayerEventListener;
    }

    public void setOnErrorListener(OnErrorListener onErrorListener) {
        this.mOnErrorListener = onErrorListener;
    }

    public void setDecodeMode(DecodeMode decodeMode) {
        this.mDecodeMode = decodeMode;
    }

    public DecodeMode getDecodeMode() {
        return mDecodeMode;
    }

    @Override
    public void setAspectRatio(AspectRatio aspectRatio) {
        this.aspectRatio = aspectRatio;
    }

    public AspectRatio getAspectRatio() {
        return aspectRatio;
    }

    @Override
    public int getStatus() {
        return mStatus;
    }

    public boolean isExpectedBufferAvailable(){
        return (getBufferPercentage()*getDuration()/100) > getCurrentPosition();
    }

    public void updatePlayerType(int playerType){
        if(mPlayerType!=playerType){
            this.mPlayerType = playerType;
            onPlayerEvent(OnPlayerEventListener.EVENT_CODE_ON_INTENT_TO_SWITCH_PLAYER_TYPE,null);
            notifyPlayerWidget(mAppContext);
        }
    }

    public int getPlayerType() {
        return mPlayerType;
    }

    @Override
    public void setScreenOrientationLandscape(boolean landscape) {
        ((Activity)mAppContext).setRequestedOrientation(landscape? ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE:ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    @Override
    public boolean isLandscape() {
        return mAppContext.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
    }

}

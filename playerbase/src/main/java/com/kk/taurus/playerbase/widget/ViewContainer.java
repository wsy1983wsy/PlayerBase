/*
 * Copyright 2017 jiajunhui<junhui_jia@163.com>
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.kk.taurus.playerbase.widget;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.kk.taurus.playerbase.event.IEventDispatcher;
import com.kk.taurus.playerbase.extension.BaseEventProducer;
import com.kk.taurus.playerbase.extension.IProducerGroup;
import com.kk.taurus.playerbase.extension.DelegateReceiverEventSender;
import com.kk.taurus.playerbase.extension.ProducerGroup;
import com.kk.taurus.playerbase.extension.ProducerEventSender;
import com.kk.taurus.playerbase.log.PLog;
import com.kk.taurus.playerbase.receiver.BaseReceiver;
import com.kk.taurus.playerbase.touch.OnTouchGestureListener;
import com.kk.taurus.playerbase.receiver.BaseCover;
import com.kk.taurus.playerbase.receiver.DefaultLevelCoverContainer;
import com.kk.taurus.playerbase.receiver.ICoverStrategy;
import com.kk.taurus.playerbase.receiver.IReceiver;
import com.kk.taurus.playerbase.receiver.IReceiverGroup;
import com.kk.taurus.playerbase.receiver.OnReceiverEventListener;
import com.kk.taurus.playerbase.event.EventDispatcher;
import com.kk.taurus.playerbase.receiver.ReceiverGroup;
import com.kk.taurus.playerbase.touch.BaseGestureCallbackHandler;
import com.kk.taurus.playerbase.touch.ContainerTouchHelper;


/**
 * Created by Taurus on 2018/3/17.
 */

public class ViewContainer extends FrameLayout implements OnTouchGestureListener {

    final String TAG = "ViewContainer";

    private FrameLayout mRenderContainer;
    private ICoverStrategy mCoverStrategy;

    private IReceiverGroup mReceiverGroup;
    private IEventDispatcher mEventDispatcher;

    private OnReceiverEventListener mOnReceiverEventListener;
    private ContainerTouchHelper mTouchHelper;

    private IProducerGroup mProducerGroup;

    public ViewContainer(@NonNull Context context) {
        this(context, null);
    }

    public ViewContainer(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ViewContainer(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        initBaseInfo(context);
        initGesture(context);
        initRenderContainer(context);
        initReceiverContainer(context);
    }

    private void initBaseInfo(Context context) {
        mProducerGroup = new ProducerGroup(new ProducerEventSender(mDelegateReceiverEventSender));
    }

    protected void initGesture(Context context){
        mTouchHelper = new ContainerTouchHelper(context,getGestureCallBackHandler());
        setGestureEnable(true);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return mTouchHelper.onTouch(event);
    }

    protected BaseGestureCallbackHandler getGestureCallBackHandler(){
        return new BaseGestureCallbackHandler(this);
    }

    public void setGestureEnable(boolean enable){
        mTouchHelper.setGestureEnable(enable);
    }

    public void setGestureScrollEnable(boolean enable) {
        mTouchHelper.setGestureScrollEnable(enable);
    }

    private void initReceiverContainer(Context context) {
        mCoverStrategy = getCoverStrategy(context);
        addView(mCoverStrategy.getContainerView(),
                new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT));
    }

    protected ICoverStrategy getCoverStrategy(Context context){
        return new DefaultLevelCoverContainer(context);
    }

    private void initRenderContainer(Context context) {
        mRenderContainer = new FrameLayout(context);
        addView(mRenderContainer,
                new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT));
    }

    public final void setRenderView(View view){
        removeRender();
        //must set WRAP_CONTENT and CENTER for render aspect ratio and measure.
        LayoutParams lp = new LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT,
                Gravity.CENTER);
        mRenderContainer.addView(view,lp);
    }

    public final void dispatchPlayEvent(int eventCode, Bundle bundle){
        if(mEventDispatcher !=null)
            mEventDispatcher.dispatchPlayEvent(eventCode, bundle);
    }

    public final void dispatchErrorEvent(int eventCode, Bundle bundle){
        if(mEventDispatcher !=null)
            mEventDispatcher.dispatchErrorEvent(eventCode, bundle);
    }

    public void setOnReceiverEventListener(OnReceiverEventListener onReceiverEventListener) {
        this.mOnReceiverEventListener = onReceiverEventListener;
    }

    /**
     * add a event producer by yourself custom.
     *
     * @param eventProducer
     *
     */
    public void addEventProducer(BaseEventProducer eventProducer){
        mProducerGroup.addEventProducer(eventProducer);
    }

    /**
     * remove event producer.
     * @param eventProducer
     * @return
     */
    public boolean removeEventProducer(BaseEventProducer eventProducer){
        return mProducerGroup.removeEventProducer(eventProducer);
    }

    private DelegateReceiverEventSender mDelegateReceiverEventSender =
            new DelegateReceiverEventSender() {
        @Override
        public void sendEvent(int eventCode, Bundle bundle,
                              IReceiverGroup.OnReceiverFilter receiverFilter) {
            if(mEventDispatcher!=null)
                mEventDispatcher.dispatchReceiverEvent(eventCode, bundle, receiverFilter);
        }
        @Override
        public void sendObject(String key, Object value) {
            if(mReceiverGroup!=null)
                mReceiverGroup.getGroupValue().putObject(key, value);
        }
    };

    public final void setReceiverGroup(ReceiverGroup receiverGroup){
        if(receiverGroup==null
                || receiverGroup.equals(mReceiverGroup))
            return;
        //remove all old covers from root container.
        removeAllCovers();

        this.mReceiverGroup = receiverGroup;
        //init event dispatcher.
        mEventDispatcher = new EventDispatcher(receiverGroup);

        //loop attach receivers
        receiverGroup.forEach(new IReceiverGroup.OnLoopListener() {
            @Override
            public void onEach(IReceiver receiver) {
                attachReceiver(receiver);
            }
        });
        //set a receiver group change listener, dynamic attach a receiver
        // when user add it or detach a receiver when user remove it.
        receiverGroup.setOnReceiverGroupChangeListener(
                new IReceiverGroup.OnReceiverGroupChangeListener() {
            @Override
            public void onReceiverAdd(String key, IReceiver receiver) {
                attachReceiver(receiver);
            }
            @Override
            public void onReceiverRemove(String key, IReceiver receiver) {
                detachReceiver(receiver);
            }
        });
    }

    //attach receiver, bind receiver event listener
    // and add cover container if it is a cover instance.
    private void attachReceiver(IReceiver receiver){
        //bind the ReceiverEventListener for receivers connect.
        receiver.bindReceiverEventListener(mInternalReceiverEventListener);
        PLog.d(TAG, "ReceiverEventListener bind : " + ((BaseReceiver)receiver).getTag());
        if(receiver instanceof BaseCover){
            //add cover view to cover strategy container.
            mCoverStrategy.addCover((BaseCover) receiver);
        }
    }

    //detach receiver, unbind receiver event listener
    // and remove cover container if it is a cover instance.
    private void detachReceiver(IReceiver receiver){
        //unbind the ReceiverEventListener for receivers connect.
        receiver.bindReceiverEventListener(null);
        PLog.w(TAG, "ReceiverEventListener unbind : " + ((BaseReceiver)receiver).getTag());
        if(receiver instanceof BaseCover){
            //remove cover view to cover strategy container.
            mCoverStrategy.removeCover((BaseCover) receiver);
        }
    }

    //receiver event listener, a bridge for some receivers communication.
    private OnReceiverEventListener mInternalReceiverEventListener =
            new OnReceiverEventListener() {
        @Override
        public void onReceiverEvent(int eventCode, Bundle bundle) {
            if(mOnReceiverEventListener!=null)
                mOnReceiverEventListener.onReceiverEvent(eventCode, bundle);
            if(mEventDispatcher !=null)
                mEventDispatcher.dispatchReceiverEvent(eventCode, bundle);
        }
    };

    public void destroy(){
        mProducerGroup.destroy();
        //and remove render view.
        removeRender();
        //and remove all covers
        removeAllCovers();
        //last remove all receivers from ReceiverGroup.
        clearReceiverGroup();
    }

    private void removeRender(){
        if(mRenderContainer!=null)
            mRenderContainer.removeAllViews();
    }

    private void clearReceiverGroup(){
        if(mReceiverGroup!=null)
            mReceiverGroup.clearReceivers();
    }

    protected void removeAllCovers(){
        mCoverStrategy.removeAllCovers();
    }

    //----------------------------------dispatch gesture touch event---------------------------------

    @Override
    public void onSingleTapUp(MotionEvent event) {
        if(mEventDispatcher!=null)
            mEventDispatcher.dispatchTouchEventOnSingleTabUp(event);
    }

    @Override
    public void onDoubleTap(MotionEvent event) {
        if(mEventDispatcher!=null)
            mEventDispatcher.dispatchTouchEventOnDoubleTabUp(event);
    }

    @Override
    public void onDown(MotionEvent event) {
        if(mEventDispatcher!=null)
            mEventDispatcher.dispatchTouchEventOnDown(event);
    }

    @Override
    public void onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        if(mEventDispatcher!=null)
            mEventDispatcher.dispatchTouchEventOnScroll(e1, e2, distanceX, distanceY);
    }

    @Override
    public void onEndGesture() {
        if(mEventDispatcher!=null)
            mEventDispatcher.dispatchTouchEventOnEndGesture();
    }
}

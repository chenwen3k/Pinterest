package com.huewu.pla.lib;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.huewu.pla.R;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class PhotoWallView extends MultiColumnListView {
	   
    private static final float PULL_RESISTANCE                 = 1.7f;
    private static final int   BOUNCE_ANIMATION_DURATION       = 200;
    private static final int   BOUNCE_ANIMATION_DELAY          = 0;
    private static final int   ROTATE_ARROW_ANIMATION_DURATION = 250;

    private static enum State{
        PULL_TO_REFRESH,
        RELEASE_TO_REFRESH,
        REFRESHING
    }
   
    public interface OnRefreshListener{

       
        public void onRefresh();
    }
   
    private static int measuredHeaderHeight;

    private boolean scrollbarEnabled;
    private boolean bounceBackHeader;
    private boolean lockScrollWhileRefreshing;
    private boolean showLastUpdatedText;
    private String  pullToRefreshText;
    private String  releaseToRefreshText;
    private String  refreshingText;
    private String  lastUpdatedText;
    private SimpleDateFormat lastUpdatedDateFormat = new SimpleDateFormat("dd/MM HH:mm");

    private float                   previousY;
    private int                     headerPadding;
    private boolean                 hasResetHeader;
    private long                    lastUpdated = -1;
    private State                   state;
    private LinearLayout            headerContainer;
    private RelativeLayout          header;
    private RotateAnimation         flipAnimation;
    private RotateAnimation         reverseFlipAnimation;
    private ImageView               image;
    private ProgressBar             spinner;
    private TextView                text;
    private TextView                lastUpdatedTextView;
    private OnRefreshListener       onRefreshListener;
    private TranslateAnimation        bounceAnimation;
   
    public PhotoWallView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        // TODO Auto-generated constructor stub
        init();
    }

    public PhotoWallView(Context context, AttributeSet attrs) {
        super(context, attrs);
        // TODO Auto-generated constructor stub
        init();
    }

    public PhotoWallView(Context context) {
        super(context);
        // TODO Auto-generated constructor stub
        init();
    }
   
   
    public void setOnRefreshListener(OnRefreshListener onRefreshListener){
        this.onRefreshListener = onRefreshListener;
    }

   
    public boolean isRefreshing(){
        return state == State.REFRESHING;
    }
   
   
    public void setLockScrollWhileRefreshing(boolean lockScrollWhileRefreshing){
        this.lockScrollWhileRefreshing = lockScrollWhileRefreshing;
    }

   
    public void setShowLastUpdatedText(boolean showLastUpdatedText){
        this.showLastUpdatedText = showLastUpdatedText;
        if(!showLastUpdatedText) lastUpdatedTextView.setVisibility(View.GONE);
    }
   
   
    public void setLastUpdatedDateFormat(SimpleDateFormat lastUpdatedDateFormat){
        this.lastUpdatedDateFormat = lastUpdatedDateFormat;
    }

   
    public void setRefreshing(){
        state = State.REFRESHING;
        setUiRefreshing();
        //setHeaderPadding(0);
        //scrollTo(0, 0);
    }
   
   
    public void onRefreshComplete(){
        state = State.PULL_TO_REFRESH;
        resetHeader();
        lastUpdated = System.currentTimeMillis();
    }

   
    public void setTextPullToRefresh(String pullToRefreshText){
        this.pullToRefreshText = pullToRefreshText;
        if(state == State.PULL_TO_REFRESH){
            text.setText(pullToRefreshText);
        }
    }

   
    public void setTextReleaseToRefresh(String releaseToRefreshText){
        this.releaseToRefreshText = releaseToRefreshText;
        if(state == State.RELEASE_TO_REFRESH){
            text.setText(releaseToRefreshText);
        }
    }

   
    public void setTextRefreshing(String refreshingText){
        this.refreshingText = refreshingText;
        if(state == State.REFRESHING){
            text.setText(refreshingText);
        }
    }

    private void init(){
        setVerticalFadingEdgeEnabled(false);

        headerContainer = (LinearLayout) LayoutInflater.from(getContext()).inflate(R.layout.pull_to_refresh_header, null);
        header = (RelativeLayout) headerContainer.findViewById(R.id.ptr_id_header);
        text = (TextView) header.findViewById(R.id.ptr_id_text);
        lastUpdatedTextView = (TextView) header.findViewById(R.id.ptr_id_last_updated);
        image = (ImageView) header.findViewById(R.id.ptr_id_image);
        spinner = (ProgressBar) header.findViewById(R.id.ptr_id_spinner);

        pullToRefreshText = getContext().getString(R.string.ptr_pull_to_refresh);
        releaseToRefreshText = getContext().getString(R.string.ptr_release_to_refresh);
//        refreshingText = getContext().getString(R.string.ptr_refreshing);
        refreshingText = "refresh";
        lastUpdatedText = getContext().getString(R.string.ptr_last_updated);

        flipAnimation = new RotateAnimation(0, -180, RotateAnimation.RELATIVE_TO_SELF, 0.5f, RotateAnimation.RELATIVE_TO_SELF, 0.5f);
        flipAnimation.setInterpolator(new LinearInterpolator());
        flipAnimation.setDuration(ROTATE_ARROW_ANIMATION_DURATION);
        flipAnimation.setFillAfter(true);

        reverseFlipAnimation = new RotateAnimation(-180, 0, RotateAnimation.RELATIVE_TO_SELF, 0.5f, RotateAnimation.RELATIVE_TO_SELF, 0.5f);
        reverseFlipAnimation.setInterpolator(new LinearInterpolator());
        reverseFlipAnimation.setDuration(ROTATE_ARROW_ANIMATION_DURATION);
        reverseFlipAnimation.setFillAfter(true);

        addHeaderView(headerContainer);
        setState(State.PULL_TO_REFRESH);
        scrollbarEnabled = isVerticalScrollBarEnabled();

        ViewTreeObserver vto = header.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new PTROnGlobalLayoutListener());

        //super.setOnItemClickListener(new PTROnItemClickListener());
        //super.setOnItemLongClickListener(new PTROnItemLongClickListener());
    }

    private void setHeaderPadding(int padding){
        headerPadding = padding;

        MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) header.getLayoutParams();
        mlp.setMargins(0, Math.round(padding), 0, 0);
        header.setLayoutParams(mlp);
    }
   
    private boolean isPulling = false;
    private boolean isPull(MotionEvent event){
        return isPulling;
    }
   
    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if(lockScrollWhileRefreshing
                && (state == State.REFRESHING || getAnimation() != null && !getAnimation().hasEnded())){
            return true;    //consume touch event here..
        }
       
        switch(event.getAction()){
        case MotionEvent.ACTION_DOWN:
            if( getFirstVisiblePosition() == 0 )
                previousY = event.getY();
            break;
        case MotionEvent.ACTION_MOVE:
            if( getFirstVisiblePosition() == 0 && event.getY() - previousY > 0 ) {
                isPulling = true;
                return true;
            }else{
                isPulling = false;
            }
            break;
        case MotionEvent.ACTION_CANCEL:
        case MotionEvent.ACTION_UP:
            isPulling = false;
            break;
        }
       
        return super.onInterceptTouchEvent(event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event){
        if(lockScrollWhileRefreshing
                && (state == State.REFRESHING || getAnimation() != null && !getAnimation().hasEnded())){
            return true;
        }

        switch(event.getAction()){

        case MotionEvent.ACTION_UP:
            if(isPull(event) && (state == State.RELEASE_TO_REFRESH || getFirstVisiblePosition() == 0)){
                switch(state){
                case RELEASE_TO_REFRESH:
                    setState(State.REFRESHING);
                    bounceBackHeader();
                    break;
                case PULL_TO_REFRESH:
                    resetHeader();
                    break;
                default:
                    break;
                }
            }
            break;

        case MotionEvent.ACTION_MOVE:
            if(isPull(event)){
                float y = event.getY();
                float diff = y - previousY;
                if(diff > 0) diff /= PULL_RESISTANCE;
                previousY = y;

                int newHeaderPadding = Math.max(Math.round(headerPadding + diff), -header.getHeight());

                if(newHeaderPadding != headerPadding && state != State.REFRESHING){
                    setHeaderPadding(newHeaderPadding);

                    if(state == State.PULL_TO_REFRESH && headerPadding > 0){
                        setState(State.RELEASE_TO_REFRESH);

                        image.clearAnimation();
                        image.startAnimation(flipAnimation);
                    }else if(state == State.RELEASE_TO_REFRESH && headerPadding < 0){
                        setState(State.PULL_TO_REFRESH);

                        image.clearAnimation();
                        image.startAnimation(reverseFlipAnimation);
                    }
                }
            }

            break;
        }

        return super.onTouchEvent(event);
    }

    private void bounceBackHeader(){
        int yTranslate = state == State.REFRESHING ?
                header.getHeight() - headerContainer.getHeight() :
                    -headerContainer.getHeight() - headerContainer.getTop();

                bounceAnimation = new TranslateAnimation(
                        TranslateAnimation.ABSOLUTE, 0,
                        TranslateAnimation.ABSOLUTE, 0,
                        TranslateAnimation.ABSOLUTE, 0,
                        TranslateAnimation.ABSOLUTE, yTranslate);

                bounceAnimation.setDuration(BOUNCE_ANIMATION_DURATION);
                bounceAnimation.setFillEnabled(true);
                bounceAnimation.setFillAfter(false);
                bounceAnimation.setFillBefore(true);
                //bounceAnimation.setInterpolator(new OvershootInterpolator(BOUNCE_OVERSHOOT_TENSION));
                bounceAnimation.setAnimationListener(new HeaderAnimationListener(yTranslate));
                startAnimation(bounceAnimation);
    }

    private void resetHeader(){
        if(getFirstVisiblePosition() > 0){
            setHeaderPadding(-header.getHeight());
            setState(State.PULL_TO_REFRESH);
            return;
        }

        if(getAnimation() != null && !getAnimation().hasEnded()){
            bounceBackHeader = true;
        }else{
            bounceBackHeader();
        }
    }

    private void setUiRefreshing(){
        spinner.setVisibility(View.VISIBLE);
        image.clearAnimation();
        image.setVisibility(View.INVISIBLE);
        text.setText(refreshingText);
    }

    private void setState(State state){
        this.state = state;
        switch(state){
        case PULL_TO_REFRESH:
            spinner.setVisibility(View.INVISIBLE);
            image.setVisibility(View.VISIBLE);
            text.setText(pullToRefreshText);

            if(showLastUpdatedText && lastUpdated != -1){
                lastUpdatedTextView.setVisibility(View.VISIBLE);
                lastUpdatedTextView.setText(String.format(lastUpdatedText, lastUpdatedDateFormat.format(new Date(lastUpdated))));
            }

            break;

        case RELEASE_TO_REFRESH:
            spinner.setVisibility(View.INVISIBLE);
            image.setVisibility(View.VISIBLE);
            text.setText(releaseToRefreshText);
            break;

        case REFRESHING:
            setUiRefreshing();

            lastUpdated = System.currentTimeMillis();
            if(onRefreshListener == null){
                setState(State.PULL_TO_REFRESH);
            }else{
                onRefreshListener.onRefresh();
            }

            break;
        }
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt){
        super.onScrollChanged(l, t, oldl, oldt);

        if(!hasResetHeader){
            if(measuredHeaderHeight > 0 && state != State.REFRESHING){
                setHeaderPadding(-measuredHeaderHeight);
            }

            hasResetHeader = true;
        }
    }

    private class HeaderAnimationListener implements AnimationListener{

        private int height, translation;
        private State stateAtAnimationStart;

        public HeaderAnimationListener(int translation){
            this.translation = translation;
        }

        @Override
        public void onAnimationStart(Animation animation){
            stateAtAnimationStart = state;

            android.view.ViewGroup.LayoutParams lp = getLayoutParams();
            height = lp.height;
            lp.height = getHeight() - translation;
            setLayoutParams(lp);

            if(scrollbarEnabled){
                setVerticalScrollBarEnabled(false);
            }
        }

        @Override
        public void onAnimationEnd(Animation animation){
            setHeaderPadding(stateAtAnimationStart == State.REFRESHING ? 0 : -measuredHeaderHeight - headerContainer.getTop());
            //setSelection(0);

            android.view.ViewGroup.LayoutParams lp = getLayoutParams();
            lp.height = height;
            setLayoutParams(lp);

            if(scrollbarEnabled){
                setVerticalScrollBarEnabled(true);
            }

            if(bounceBackHeader){
                bounceBackHeader = false;

                postDelayed(new Runnable(){

                    @Override
                    public void run(){
                        resetHeader();
                    }
                }, BOUNCE_ANIMATION_DELAY);
            }else if(stateAtAnimationStart != State.REFRESHING){
                setState(State.PULL_TO_REFRESH);
            }
        }

        @Override
        public void onAnimationRepeat(Animation animation){}
    }

    private class PTROnGlobalLayoutListener implements OnGlobalLayoutListener{

        @SuppressWarnings("deprecation")
        @Override
        public void onGlobalLayout(){
            int initialHeaderHeight = header.getHeight();

            if(initialHeaderHeight > 0){
                measuredHeaderHeight = initialHeaderHeight;

                if(measuredHeaderHeight > 0 && state != State.REFRESHING){
                    setHeaderPadding(-measuredHeaderHeight);
                    requestLayout();
                }
            }

            getViewTreeObserver().removeGlobalOnLayoutListener(this);
        }
    }
}

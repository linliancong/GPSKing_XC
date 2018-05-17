package com.zxhl.util;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.AbsListView;
import android.widget.ListView;

import com.iflytek.cloud.util.VolumeUtil;
import com.zxhl.gpsking.R;

import org.apache.http.cookie.CookieIdentityComparator;

//import android.support.v7.widget.RecyclerView;
/**
 * Created by Administrator on 2018/1/19.
 */

public class SwipeRefreshView extends SwipeRefreshLayout {

    private static final String TAG=SwipeRefreshView.class.getSimpleName();
    private ListView list;
    private View mFooterView;
    private OnLoadMoreListener mOnLoadListener;

    private float mDownY=0;
    private float mUpY=0;
    private float mScaledTouchSlop=0;

    private boolean isLoading=false;
    //private RecyclerView  mRecyclerView;
    private int mItemCount;

    public SwipeRefreshView(@NonNull Context context,AttributeSet attrs) {
        super(context,attrs);
        //填充底部加载布局
        mFooterView=View.inflate(context, R.layout.view_footer,null);

        //表示控件移动的最小距离，手移动的距离大于这个距离才能拖动控件
        mScaledTouchSlop= ViewConfiguration.get(context).getScaledTouchSlop();
    }

    /**
     * 获取子控件ListView
    * */
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if(list==null){
            if(getChildCount()>0){
                if(getChildAt(0) instanceof ListView){
                    list= (ListView) getChildAt(0);
                    //设置滑动监听
                    setListViewOnScroll();
                }
                /*else if(getChildAt(0) instanceof RecyclerView){
                    mRecyclerView= (RecyclerView) getChildAt(0);
                    //设置滑动监听
                    setListViewOnScroll();
                }*/
            }
        }
    }

    /**
     * 在分发事件的时候处理子控件的触摸事件
     *
     * @param ev
     * @return
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getAction()){
            case MotionEvent.ACTION_DOWN:
                mDownY=ev.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                //移动过程判断什么时候能下拉加载更多
                if(canLoadMore()){
                    //加载数据
                    loadData();
                }
                break;
            case MotionEvent.ACTION_UP:
                //移动的终点
                mUpY=getY();
                break;
        }
        return super.dispatchTouchEvent(ev);
    }

    /**
     * 判断是否满足加载更多条件
     *
     * @return
     */
    private boolean canLoadMore(){
        //1、是上拉状态
        boolean condition1=(mDownY-mUpY)>=mScaledTouchSlop;
        if(condition1){
            Log.i("State:","是上拉状态");
        }
        //2、当页面可见的item是最后一个条目
        boolean condition2=false;
        if(list!=null && list.getAdapter()!=null){
            if(mItemCount>0){
                if(list.getAdapter().getCount()<mItemCount){
                    //第一页未满，禁止下拉
                    condition2=false;
                }else{
                    condition2=list.getLastVisiblePosition()==(list.getAdapter().getCount()-1);
                }
            }else{
                //未设置数据长度，则默认第一页数据不满也可上拉
                condition2=list.getLastVisiblePosition()==(list.getAdapter().getCount()-1);
            }
        }

        if(condition2){
            Log.i("State:","是最后一个条目");
        }

        //3、正在加载状态
        boolean condition3=!isLoading;
        if(condition3){
            Log.i("State:","不是正在加载状态");
        }
        return condition1 && condition2 && condition3;
    }

    /**
    *设置每页显示的条目
    * */
    public void setItemCount(int itemCount){
        this.mItemCount=itemCount;
    }

    /**
    *处理加载数据的逻辑
    * */
    private void loadData(){
        Log.i("State:","加载数据...");
        if(mOnLoadListener!=null){
            //设置加载状态，让布局显示出来
            setLoading(true);
            mOnLoadListener.onLoadMore();
        }
    }

    /**
     * 设置加载状态，是否加载传入boolean值进行判断
     *
     * @param loading
     */
    public void setLoading(boolean loading) {
        //修改当前的状态
        isLoading=loading;
        if(isLoading){
            //显示布局
            list.addFooterView(mFooterView);
        }
        else {
            //隐藏布局
            list.removeFooterView(mFooterView);
            //重置活动的坐标
            mDownY=0;
            mUpY=0;
        }
    }

    /**
     * 设置ListView的滑动监听
     */
    private void setListViewOnScroll(){
        list.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                //移动过程中判断什么时候能下拉加载更多
                if(canLoadMore()){
                    //加载数据
                    loadData();
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

            }
        });
    }

    /**
     * 设置RecyclerView的滑动监听
     */
    /*private void setRecyclerViewOnScroll() {
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                // 移动过程中判断时候能下拉加载更多
                if (canLoadMore()) {
                    // 加载数据
                    loadData();
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }
        });
    }*/

    /**
     * 上拉加载的接口回调
     */
    public interface OnLoadMoreListener{
        void onLoadMore();
    }

    public void setOnLoadMoreListener(OnLoadMoreListener listener){
        this.mOnLoadListener=listener;
    }


}

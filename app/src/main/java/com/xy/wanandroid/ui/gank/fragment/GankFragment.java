package com.xy.wanandroid.ui.gank.fragment;

import android.content.Intent;
import android.graphics.Rect;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.xy.wanandroid.R;
import com.xy.wanandroid.base.app.MyApplication;
import com.xy.wanandroid.base.fragment.BaseRootFragment;
import com.xy.wanandroid.contract.gank.GankContract;
import com.xy.wanandroid.data.gank.MusicBanner;
import com.xy.wanandroid.data.gank.RecommendData;
import com.xy.wanandroid.model.constant.Constant;
import com.xy.wanandroid.presenter.gank.GankPresenter;
import com.xy.wanandroid.ui.gank.activity.DoubanHotActivity;
import com.xy.wanandroid.ui.gank.activity.ExtraActivity;
import com.xy.wanandroid.ui.gank.activity.RecommendActivity;
import com.xy.wanandroid.ui.gank.activity.VideoActivity;
import com.xy.wanandroid.ui.gank.adapter.GankAdapter;
import com.xy.wanandroid.ui.main.activity.ArticleDetailsActivity;
import com.xy.wanandroid.util.app.DisplayUtil;
import com.xy.wanandroid.util.app.JumpUtil;
import com.xy.wanandroid.util.app.SharedPreferenceUtil;
import com.xy.wanandroid.util.app.ToastUtil;
import com.xy.wanandroid.util.glide.GlideImageLoader;
import com.youth.banner.Banner;
import com.youth.banner.BannerConfig;
import com.youth.banner.Transformer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;

public class GankFragment extends BaseRootFragment<GankPresenter> implements GankContract.View, GankAdapter.OnItemClickListener {

    @BindView(R.id.normal_view)
    RecyclerView mRv;

    private List<RecommendData.ResultsBean> gankList;
    private GankAdapter mAdapter;
    private List<String> urlList;
    private Banner banner;
    private int retryCount = 2;
    private GridLayoutManager manager;

    @Override
    public int getLayoutResID() {
        return R.layout.fragment_gank;
    }

    public static GankFragment getInstance() {
        return new GankFragment();
    }

    @Override
    protected void initInjector() {
        mFragmentComponent.inject(this);
    }

    @Override
    protected void initUI() {
        super.initUI();
        showLoading();
        manager = new GridLayoutManager(context, 2);
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        mRv.setLayoutManager(manager);
        RecyclerView.ItemDecoration itemDecoration = new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, int itemPosition, RecyclerView parent) {
                if (itemPosition != 0) {
                    if ((itemPosition - 1) % 2 == 1) {
                        outRect.left = DisplayUtil.dip2px(context, 6);
                        outRect.right = DisplayUtil.dip2px(context, 12);
                    } else {
                        outRect.left = DisplayUtil.dip2px(context, 12);
                        outRect.right = DisplayUtil.dip2px(context, 6);
                    }
                }
            }
        };
        mRv.addItemDecoration(itemDecoration);
    }

    @Override
    protected void initData() {
        gankList = new ArrayList<>();
        urlList = new ArrayList<>();
        getBanner();
        mAdapter = new GankAdapter(R.layout.item_gank, gankList);
        initHeader();
        mPresenter.getEveryDayList();
        mAdapter.setOnItemClickListener(this);
        mRv.setAdapter(mAdapter);
//        setSpan();
    }

    /**
     * 设置Header
     */
    private void initHeader() {
        LinearLayout headerView = (LinearLayout) getLayoutInflater().inflate(R.layout.view_gank_header, null);
        View view = headerView.findViewById(R.id.view_header);
        View cateView = headerView.findViewById(R.id.view_category);
        View douYuView = headerView.findViewById(R.id.view_douyu);
        View extraView = headerView.findViewById(R.id.view_extra);
        View rankView = headerView.findViewById(R.id.view_rank);
        cateView.setOnClickListener(v -> JumpUtil.overlay(activity, RecommendActivity.class));
        douYuView.setOnClickListener(v -> JumpUtil.overlay(activity, VideoActivity.class));
        extraView.setOnClickListener(v -> JumpUtil.overlay(activity, ExtraActivity.class));
        rankView.setOnClickListener(v -> JumpUtil.overlay(activity, DoubanHotActivity.class));
        banner = headerView.findViewById(R.id.banner);
        headerView.removeView(view);
        headerView.addView(view);
        mAdapter.addHeaderView(headerView);
        showBanner();
    }

    /**
     * 获取banner
     * 由于json很庞大,所以存储到本地。不必每次请求
     */
    private void getBanner() {
        if (SharedPreferenceUtil.get(context, Constant.GANK_BANNER, Constant.DEFAULT).equals(Constant.DEFAULT)) {
            mPresenter.getMusicBanner();
        } else {
            String banner = (String) SharedPreferenceUtil.get(context, Constant.GANK_BANNER, Constant.DEFAULT);
            urlList = Arrays.asList(banner.split("="));
        }
    }

    private void showBanner() {
        activity.runOnUiThread(() -> banner.setImageLoader(new GlideImageLoader())
                .setBannerStyle(BannerConfig.CIRCLE_INDICATOR)
                .setImages(urlList)
                .setBannerAnimation(Transformer.DepthPage)
                .isAutoPlay(true)
                .setDelayTime(5000)
                .setIndicatorGravity(BannerConfig.CENTER)
                .start());
    }

    @Override
    public void getMusicBannerOK(MusicBanner musicBanner) {
        StringBuilder sb = new StringBuilder();
        for (MusicBanner.ResultBeanX.FocusBean.ResultBeanx resultBean : musicBanner.getResult().getFocus().getResult()) {
            sb.append(resultBean.getRandpic());
            sb.append("=");
            urlList.add(resultBean.getRandpic());
        }
        SharedPreferenceUtil.put(MyApplication.getInstance(), Constant.GANK_BANNER, sb);
        showBanner();
    }

    @Override
    public void getMusicBannerErr(String info) {
        ToastUtil.show(context, info);
        retryCount--;
        if (retryCount > 0) {
            showError();
            mPresenter.getMusicBanner();
        }
    }

    @Override
    public void getEveryDayListOk(RecommendData dataList, boolean isRefresh) {
        if (mAdapter == null) {
            return;
        }
//        addToMultiList(dataList);
//        mAdapter.replaceData(gankList);
        gankList = dataList.getResults();
        mAdapter.replaceData(gankList);
        showNormal();
    }

    @Override
    public void getEveryDayListErr(String info) {
        ToastUtil.show(context, info);
        showError();
    }

    @Override
    public void reload() {
        showLoading();
        mPresenter.getEveryDayList();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (banner != null) {
            banner.stopAutoPlay();
        }
    }

    @Override
    public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
        Intent intent = new Intent(activity, ArticleDetailsActivity.class);
        intent.putExtra(Constant.ARTICLE_TITLE, mAdapter.getData().get(position).getDesc());
        intent.putExtra(Constant.ARTICLE_LINK, mAdapter.getData().get(position).getUrl());
        startActivity(intent);
    }

    /**
     * 多布局
     */
    private void addToMultiList(RecommendData dataList) {
        int size = dataList.getResults().size();
        for (int i = 0; i < size; i++) {
            RecommendData.ResultsBean data = dataList.getResults().get(i);
            //前四项
            if (i < 4) {
                if (i % 3 == 0 && i != 3) {
                    RecommendData.ResultsBean item = new RecommendData.ResultsBean(RecommendData.ResultsBean.ENTITY_TITLE, data.getType());
                    gankList.add(item);
                } else {
                    RecommendData.ResultsBean item = new RecommendData.ResultsBean(RecommendData.ResultsBean.ENTITY_ITEM, data.getDesc(), data.getPublishedAt(), data.getType(), data.getUrl(), data.getWho(), data.getImages());
                    gankList.add(item);
                }
                if (i == 3) {
                    RecommendData.ResultsBean item = new RecommendData.ResultsBean(RecommendData.ResultsBean.ENTITY_ITEM, data.getDesc(), data.getPublishedAt(), data.getType(), data.getUrl(), data.getWho(), data.getImages());
                    gankList.add(item);
                }
            } else if (i > 3) {    //4-7
                if (i % 4 == 0 && i != 4) {
                    RecommendData.ResultsBean item = new RecommendData.ResultsBean(RecommendData.ResultsBean.ENTITY_TITLE, dataList.getResults().get(i).getType());
                    gankList.add(item);
                } else {
                    RecommendData.ResultsBean item = new RecommendData.ResultsBean(RecommendData.ResultsBean.ENTITY_ITEM, data.getDesc(), data.getPublishedAt(), data.getType(), data.getUrl(), data.getWho(), data.getImages());
                    gankList.add(item);
                }
                if (i == 4) {
                    RecommendData.ResultsBean item = new RecommendData.ResultsBean(RecommendData.ResultsBean.ENTITY_TITLE, dataList.getResults().get(i).getType());
                    gankList.add(item);
                }
            }
        }
    }

    /**
     * 每个item
     */
    private void setSpan() {
        manager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                int count = 0;
                if (position != 0) {
                    int type = mAdapter.getItemViewType(position);
                    switch (type) {
                        case RecommendData.ResultsBean.ENTITY_TITLE:
                            count = 3;
                            break;
                        case RecommendData.ResultsBean.ENTITY_ITEM:
                            count = 1;
                            break;
                    }
                }
                return count;
            }
        });
    }
}

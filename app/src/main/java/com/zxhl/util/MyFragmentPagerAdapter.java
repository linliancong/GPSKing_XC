package com.zxhl.util;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.ViewGroup;

import com.zxhl.gpsking.HomePage;
import com.zxhl.gpsking.HomeSy;
import com.zxhl.gpsking.MeSy;
import com.zxhl.gpsking.QuerySy;
import com.zxhl.gpsking.SettingSy;

/**
 * Created by Administrator on 2017/11/29.
 */

public class MyFragmentPagerAdapter extends FragmentPagerAdapter {

    private final int PAGER_COUNT=4;
    /*private FragmentUtils fmu1=null;
    private FragmentUtils fmu2=null;
    private FragmentUtils fmu3=null;
    private FragmentUtils fmu4=null;*/
    private Context context;

    private HomeSy homeSy=null;
    private QuerySy querySy=null;
    private MeSy meSy=null;
    private SettingSy settingSy=null;

    public MyFragmentPagerAdapter(FragmentManager fm, Context context)
    {
        super(fm);
        this.context=context;
       /* fmu1=new FragmentUtils("ONE",FragmentUtils.PAG_ONE);
        fmu2=new FragmentUtils("TWO",FragmentUtils.PAG_TWO);
        fmu3=new FragmentUtils("THREE",FragmentUtils.PAG_THREE);
        fmu4=new FragmentUtils("FOUR",FragmentUtils.PAG_FOUR);*/

        homeSy=new HomeSy(context);
        querySy=new QuerySy(context);
        meSy=new MeSy(context);
        settingSy=new SettingSy(context);

    }

    @Override
    public Fragment getItem(int position) {
        Fragment fragment=null;
        switch (position)
        {
            case HomePage.PAG_ONE:
                fragment=homeSy;
                break;
            case HomePage.PAG_TWO:
                fragment=querySy;
                break;
            case HomePage.PAG_THREE:
                fragment=meSy;
                break;
            case HomePage.PAG_FOUR:
                fragment=settingSy;
                break;
        }
        return fragment;
    }

    @Override
    public int getCount() {
        return PAGER_COUNT;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        return super.instantiateItem(container, position);
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        super.destroyItem(container, position, object);
    }
}

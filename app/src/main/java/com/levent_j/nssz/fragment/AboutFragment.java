package com.levent_j.nssz.fragment;

import com.levent_j.nssz.R;
import com.levent_j.nssz.base.BaseFragment;

/**
 * Created by levent_j on 16-7-28.
 */
public class AboutFragment extends BaseFragment{

    public static AboutFragment newInstance(){
        return new AboutFragment();
    }

    @Override
    protected int setRootViewId() {
        return R.layout.fragment_about;
    }
}

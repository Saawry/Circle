package com.example.circle;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class TabsAccessorAdapter extends FragmentPagerAdapter {
    String [] text ={"People","Circle","Request","Tracker"};
    public TabsAccessorAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int i) {
        switch (i){

            case 0:
                PeopleFragment peopleFragment = new PeopleFragment();
                return peopleFragment;
            case 1:
                CircleFragment circleFragment = new CircleFragment();
                return circleFragment;
            case 2:
                RequestFragment requestFragment = new RequestFragment();
                return requestFragment;
            case 3:
                TrackerFragment trackerFragment = new TrackerFragment();
                return trackerFragment;
//            case 4:
//                DemoFragment demoFragment = new DemoFragment();
//                return demoFragment;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return text.length;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        //return super.getPageTitle(position);
        switch (position){
            case 0:
                return text[position];
            case 1:
                return text[position];
            case 2:
                return text[position];
            case 3:
                return text[position];
            default:
                return null;
        }
    }
}

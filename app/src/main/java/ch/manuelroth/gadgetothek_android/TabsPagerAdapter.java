package ch.manuelroth.gadgetothek_android;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class TabsPagerAdapter extends FragmentPagerAdapter {
    public TabsPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int index) {
        switch (index) {
            case 0:
                return new GadgetsFragment();
            case 1:
                return new AusleihenFragment();
            case 2:
                return new ReservationFragment();
        }

        return null;
    }

    @Override
    public int getCount() {
        return 2;
    }
}

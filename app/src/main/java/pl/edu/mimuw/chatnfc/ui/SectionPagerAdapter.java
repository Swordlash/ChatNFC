package pl.edu.mimuw.chatnfc.ui;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

class SectionPagerAdapter extends FragmentPagerAdapter {
    public SectionPagerAdapter(FragmentManager fm) {
        super(fm);
    }


    @Override
    public Fragment getItem(int position) {
        if (position == 2)
        {
            ProfileFragment requestFragment = new ProfileFragment();
            return requestFragment;
        }
        else if (position == 0)
        {
            MessageFragment messageFragment = new MessageFragment();
            return messageFragment;
        }
        else if (position == 1)
        {
            ContactsFragment contactsFragment = new ContactsFragment();
            return contactsFragment;
        }
        return null;
    }

    @Override
    public int getCount() {
        return 3;
    }
    
    
    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        if (position == 2)
        {
            return "Profile";
        }
        else if (position == 0)
        {
            return "Messages";
        }
        else if (position == 1)
        {
            return "Contacts";
        }
        return null;
    }
}

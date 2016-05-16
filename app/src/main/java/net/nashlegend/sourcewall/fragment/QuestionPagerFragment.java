package net.nashlegend.sourcewall.fragment;


import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import net.nashlegend.sourcewall.R;
import net.nashlegend.sourcewall.model.SubItem;
import net.nashlegend.sourcewall.util.ChannelHelper;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link QuestionPagerFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class QuestionPagerFragment extends Fragment {

    ArrayList<SubItem> subItems = ChannelHelper.getQuestionSectionsByUserState();
    @BindView(R.id.question_tabs)
    TabLayout tabLayout;
    @BindView(R.id.question_pager)
    ViewPager viewPager;

    QuestionPagerAdapter adapter;
    private Unbinder unbinder;

    public QuestionPagerFragment() {
        // Required empty public constructor
    }

    public static QuestionPagerFragment newInstance() {
        QuestionPagerFragment fragment = new QuestionPagerFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_question_pager, container, false);
        unbinder = ButterKnife.bind(this, view);
        adapter = new QuestionPagerAdapter(getFragmentManager());
        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);
        for (int i = 0; i < tabLayout.getTabCount(); i++) {
            TabLayout.Tab tab = tabLayout.getTabAt(i);
            if (tab == null) {
                break;
            }
            tab.setText(subItems.get(i).getName());
        }
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    class QuestionPagerAdapter extends FragmentStatePagerAdapter {

        public QuestionPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return Questions2Fragment.newInstance(subItems.get(position));
        }

        @Override
        public int getCount() {
            return subItems.size();
        }
    }
}

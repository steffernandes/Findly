package com.example.g6_findly.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.g6_findly.Fragments.DeleteMovieFragment;
import com.example.g6_findly.Fragments.DeleteSeriesFragment;
import com.example.g6_findly.Fragments.SearchMovieFragment;
import com.example.g6_findly.Fragments.SearchSeriesFragment;
import com.example.g6_findly.R;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

public class ListElements extends AppCompatActivity {

    private ViewPager viewPager;
    private TabLayout tabLayout;

    private DeleteMovieFragment deleteMovieFragment;
    private DeleteSeriesFragment deleteSeriesFragment;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_elements);

        hideActionBar();
        viewPager = findViewById(R.id.view_pager);
        tabLayout = findViewById(R.id.tab_layout);

        deleteMovieFragment = new DeleteMovieFragment();
        deleteSeriesFragment = new DeleteSeriesFragment();

        tabLayout.setupWithViewPager(viewPager);

        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(), 0);
        viewPagerAdapter.addFragment(deleteMovieFragment, "Movies");
        viewPagerAdapter.addFragment(deleteSeriesFragment, "Tv Shows");
        viewPager.setAdapter(viewPagerAdapter);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

    }
    public void hideActionBar() {
        androidx.appcompat.app.ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
    }

    private class ViewPagerAdapter extends FragmentPagerAdapter {

        private List<Fragment> fragments = new ArrayList<>();
        private List<String> fragmentTitle = new ArrayList<>();

        public ViewPagerAdapter(@NonNull FragmentManager fm, int behavior) {
            super(fm, behavior);
        }

        public void addFragment(Fragment fragment, String title) {
            fragments.add(fragment);
            fragmentTitle.add(title);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return fragmentTitle.get(position);
        }
    }

    public void logOut(View v){
        mAuth.signOut();
        Intent i = new Intent(this, Login.class);
        startActivity(i);
    }

}
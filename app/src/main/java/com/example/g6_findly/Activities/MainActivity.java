package com.example.g6_findly.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.g6_findly.Fragments.DiscoverFragment;
import com.example.g6_findly.Fragments.MoviesFragment;
import com.example.g6_findly.Fragments.ProfileFragment;
import com.example.g6_findly.Fragments.TvShowsFragment;
import com.example.g6_findly.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {
  BottomNavigationView bottomNavigation;
  FirebaseFirestore db = FirebaseFirestore.getInstance();

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    bottomNavigation = findViewById(R.id.bottom_navigation);

    final Menu menu =bottomNavigation.getMenu();

    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    if (user == null) {
      // if the user is not authenticated, redirect to Login page
      Intent i = new Intent(this, Login.class);
      startActivity(i);
    } else {
        // opens profile fragment by default
        menu.findItem(R.id.profile).setIcon(R.drawable.profile_active);
        openFragment(ProfileFragment.newInstance("", ""));
    }

    bottomNavigation.setOnNavigationItemSelectedListener(
            new BottomNavigationView.OnNavigationItemSelectedListener() {
              @Override
              public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                  menu.findItem(R.id.movies).setIcon(R.drawable.movie);
                  menu.findItem(R.id.tv_shows).setIcon(R.drawable.tv_show);
                  menu.findItem(R.id.discover).setIcon(R.drawable.discover);
                  menu.findItem(R.id.profile).setIcon(R.drawable.profile);

                  FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                  String uid = user.getUid();

                switch (item.getItemId()) {
                  case R.id.movies:
                      item.setIcon(R.drawable.movie_active);
                      db.collection("user_preferences_movies").document(uid).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                          @Override
                          public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                              if (task.isSuccessful()) {
                                  DocumentSnapshot document = task.getResult();
                                  if (!document.exists() && document.get("suggestions")!= null) {
                                      Intent i = new Intent(MainActivity.this, ChooseMovieCategories.class);
                                      startActivity(i);
                                  } else {
                                      openFragment(MoviesFragment.newInstance("", ""));
                                  }
                              } else {
                                  Log.d("TAG", "get failed with ", task.getException());
                              }
                          }
                      });

                    return true;
                  case R.id.tv_shows:
                      item.setIcon(R.drawable.tv_show_active);
                      db.collection("user_preferences_tv_shows").document(uid).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                          @Override
                          public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                              if (task.isSuccessful()) {
                                  DocumentSnapshot document = task.getResult();
                                  if (!document.exists() && document.get("suggestions")!= null) {
                                      Intent i = new Intent(MainActivity.this, ChooseTvShowCategories.class);
                                      startActivity(i);
                                  } else {
                                      openFragment(TvShowsFragment.newInstance("", ""));
                                  }
                              } else {
                                  Log.d("TAG", "get failed with ", task.getException());
                              }
                          }
                      });
                    return true;
                  case R.id.discover:
                      item.setIcon(R.drawable.discover_active);
                    openFragment(DiscoverFragment.newInstance("", ""));
                    return true;
                  case R.id.profile:
                      item.setIcon(R.drawable.profile_active);
                   openFragment(ProfileFragment.newInstance("", ""));
                    return true;
                }
                return false;
              }
            });
    hideActionBar();
  }

  public void hideActionBar() {
    androidx.appcompat.app.ActionBar actionBar = getSupportActionBar();
    actionBar.hide();
  }

  public void openFragment(Fragment fragment) {
    FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
    transaction.replace(R.id.container, fragment);
    transaction.addToBackStack(null);
    transaction.commit();
  }
}





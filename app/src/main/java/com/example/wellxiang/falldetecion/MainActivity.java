package com.example.wellxiang.falldetecion;

import android.app.FragmentTransaction;
import android.content.Intent;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import androidx.core.view.GravityCompat;
//import androidx.core.widget.DrawerLayout;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;

import android.view.Menu;
import android.view.MenuItem;

/**
 * - ████████╗██████╗ ██╗███████╗████████╗ █████╗ ███╗   ██╗
 * - ╚══██╔══╝██╔══██╗██║██╔════╝╚══██╔══╝██╔══██╗████╗  ██║
 * -    ██║   ██████╔╝██║███████╗   ██║   ███████║██╔██╗ ██║
 * -    ██║   ██╔══██╗██║╚════██║   ██║   ██╔══██║██║╚██╗██║
 * -    ██║   ██║  ██║██║███████║   ██║   ██║  ██║██║ ╚████║
 * -    ╚═╝   ╚═╝  ╚═╝╚═╝╚══════╝   ╚═╝   ╚═╝  ╚═╝╚═╝  ╚═══╝
 *
 *
 */
public class MainActivity extends AppCompatActivity {

    private DrawerLayout mDrawerLayout;
    private NavigationView mNavigationView;
    private Toolbar mToolbar;
//    private ActionBarDrawerToggle mDrawerToggle;
    private HomeFragment homeFragment;
    private SettingsFragment settingsFragment;
    private AboutFragment aboutFragment;

    private final int HOME = 1;
    private final int SETTINGS = 2;
    private final int ABOUT = 3;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

//        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, mToolbar, R.string.drawer_open,
//                R.string.drawer_close);
//        mDrawerToggle.syncState();
//        mDrawerLayout.addDrawerListener(mDrawerToggle);
        mNavigationView = (NavigationView) findViewById(R.id.navigation_view);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        //为activity窗口设置活动栏
        setSupportActionBar(mToolbar);

        final ActionBar actionBar = getSupportActionBar();
        //设置导航图标
        actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);
        actionBar.setDisplayHomeAsUpEnabled(true);
        
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        mNavigationView = (NavigationView) findViewById(R.id.navigation_view);
        if (mNavigationView != null) {
            setupDrawerContent(mNavigationView);
        }

//        switchToHome();
        showFragment(HOME);

    }

    private void setupDrawerContent(NavigationView navigationView) {
        //监听navigationView的项目选择
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem item) {
                        int id = item.getItemId();
                        if (id==R.id.navigation_item_home)
                        {
                            //switchToHome();
                               showFragment(HOME);
                                mToolbar.setTitle(R.string.app_name);
                        }
                        if (id==R.id.navigation_item_place) {
                            switchToPlace();
                        }
                        if(id==R.id.navigation_item_settings)
                        {
                            showFragment(SETTINGS);
                            mToolbar.setTitle(R.string.navigation_settings);
                        }
                        if (id==R.id.navigation_item_about)
                        {
                            showFragment(ABOUT);
                            mToolbar.setTitle(R.string.navigation_about);
                        }

                        item.setChecked(true);
                        mDrawerLayout.closeDrawers();
                        return true;
                    }
                });
    }

//    private void switchToHome() {
//        getSupportFragmentManager().beginTransaction().replace(R.id.frame_content, new HomeFragment()).commit();
//        mToolbar.setTitle(R.string.app_name);
//    }
//
    private void switchToPlace() {
//        getSupportFragmentManager().beginTransaction().replace(R.id.frame_content, new PlaceFragment()).commit();
//        mToolbar.setTitle(R.string.navigation_place);
        mToolbar.setTitle(R.string.app_name);
        startActivity(new Intent(this, MapActivity.class));

    }


    public void showFragment(int index){
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        //想要显示一个fragment，先隐藏所有fragment，防止重叠
        hideFragment(ft);

        switch (index){
            case HOME:
                if(homeFragment != null){
                    ft.show(homeFragment);
                }else{
                    homeFragment = new HomeFragment();
                    ft.add(R.id.frame_content, homeFragment);
                }
                break;
            case SETTINGS:
                if(settingsFragment != null){
                    ft.show(settingsFragment);
                }else{
                    settingsFragment = new SettingsFragment();
                    ft.add(R.id.frame_content, settingsFragment);
                }
                break;
            case ABOUT:
                if(aboutFragment != null){
                    ft.show(aboutFragment);
                }else{
                    aboutFragment = new AboutFragment();
                    ft.add(R.id.frame_content, aboutFragment);
                }
                break;
        }
        ft.commit();
    }
    /*
    当fragment已被实例化，就隐藏起来
     */
    public void hideFragment(FragmentTransaction ft){
        if(homeFragment != null){
            ft.hide(homeFragment);
        }
        if(settingsFragment != null){
            ft.hide(settingsFragment);
        }
        if(aboutFragment != null){
            ft.hide(aboutFragment);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if(item.getItemId() == android.R.id.home)
        {
            mDrawerLayout.openDrawer(GravityCompat.START);
            return true ;
        }
        if(item.getItemId() == R.id.id_menu_settings){
            showFragment(SETTINGS);
        }
        return super.onOptionsItemSelected(item);
    }

}

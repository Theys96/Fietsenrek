package nl.rug.nc.bicycleclient;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.support.v4.app.FragmentManager;

/**
 * This is a top level interface that provides the App-wide navigation bar and contains the
 * Home, Parking time checker and Reservation Fragments.
 */
public class MainActivity extends AppCompatActivity {

    private HomeFragment homeFragment = new HomeFragment();
    private CheckBikeFragment checkBikeFragment = new CheckBikeFragment();
    private ReserveSpotFragment reserveSpotFragment = new ReserveSpotFragment();

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    openFragment(homeFragment);
                    return true;
                case R.id.navigation_check_time:
                    openFragment(checkBikeFragment);
                    return true;
                case R.id.navigation_reserve:
                    openFragment(reserveSpotFragment);
                    return true;
            }
            return false;
        }
    };

    /**
     * Opens up a given Fragment.
     *
     * @param fragment The Fragment that has to be displayed.
     */
    private void openFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.internalFrame, fragment);
        transaction.commit();
    }

    /** (non-Javadoc)
     * @see android.support.v7.app.AppCompatActivity#onCreate(Bundle, PersistableBundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        openFragment(new HomeFragment());

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
    }

}

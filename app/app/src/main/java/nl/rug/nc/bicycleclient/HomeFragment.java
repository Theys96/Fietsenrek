package nl.rug.nc.bicycleclient;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.internal.BottomNavigationMenu;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

/**
 * This subclass of Fragment provides a home screen interface that contains buttons to the parking
 * time checker and the reservation interface.
 */
public class HomeFragment extends Fragment {

    public HomeFragment() {
    }

    /** (non-Javadoc)
     * @see android.support.v4.app.Fragment#onCreateView(LayoutInflater, ViewGroup, Bundle)
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_home, container, false);

        Button checkBikeButton = rootView.findViewById(R.id.checkBike);
        checkBikeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((BottomNavigationView)HomeFragment.this.getActivity().findViewById(R.id.navigation)).setSelectedItemId(R.id.navigation_check_time);
            }
        });

        Button reserveSpotButton = rootView.findViewById(R.id.reserveSpot);
        reserveSpotButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((BottomNavigationView)HomeFragment.this.getActivity().findViewById(R.id.navigation)).setSelectedItemId(R.id.navigation_reserve);
            }
        });

        // Inflate the layout for this fragment
        return rootView;
    }

}

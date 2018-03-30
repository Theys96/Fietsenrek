package nl.rug.nc.bicycleclient;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;


public class CheckBikeFragment extends Fragment {

    private View rootView;
    private Map<String, Integer> sizeData = new HashMap<>();
    private Map<String, long[]> slotData = new HashMap<>();
    private EditText numberInput;
    private Spinner rackSpinner;
    private Button requestParkTime;
    private long checkTime;

    public CheckBikeFragment() {
        // Required empty public constructor
    }

    private boolean validateNumber(String input, int min, int max) {
        try {
            int inputInt = Integer.valueOf(input);
            return (inputInt >= min && inputInt <= max);
        } catch (NumberFormatException nfe) {}
        return false;
    }

    private void jsonRequestCallback(JSONArray response) {
        rootView.findViewById(R.id.progressBar).setVisibility(rootView.GONE);
        sizeData.clear();
        slotData.clear();
        checkTime = System.currentTimeMillis();
        for (int i = 0; i < response.length(); i++) {
            try {
                JSONObject row = response.getJSONObject(i);
                sizeData.put(row.getString("name"), row.getInt("size"));
                long[] longArray = new long[row.getJSONArray("spots").length()];
                for (int j=0; j<row.getJSONArray("spots").length(); j++) {
                    longArray[j] = row.getJSONArray("spots").getLong(j);
                }
                slotData.put(row.getString("name"), longArray);
                Log.d("Long data", Arrays.toString(longArray));
            } catch (JSONException je) {
                Log.e("JSON error", je.getMessage());
            }
        }
        requestParkTime.setEnabled(true);
        if (response.length() == 0) {
            Snackbar.make(rootView, "No bicycle racks currently online.", Snackbar.LENGTH_LONG).show();
            rackSpinner.setEnabled(false);
            numberInput.setEnabled(false);
            requestParkTime.setEnabled(false);
        }
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(this.getActivity(), android.R.layout.simple_spinner_dropdown_item, sizeData.keySet().toArray(new String[0]));
        rackSpinner.setAdapter(spinnerAdapter);
    }

    private String getCheckedString() {
        return String.format(" (Last checked %s ago)", DateUtils.formatElapsedTime((System.currentTimeMillis()-checkTime)/1000));
    }

    private void showResult(final int slot) {
        this.getActivity().runOnUiThread(new Runnable() {

            @Override
            public void run() {
                TextView result = (TextView) rootView.findViewById(R.id.spotTimeResult);
                if (!slotData.containsKey(rackSpinner.getSelectedItem())) {
                    result.setText("Rack not found!");
                    return;
                }
                long time = slotData.get(rackSpinner.getSelectedItem())[slot];
                if (time == 0) result.setText("There is no bicycle parked in this spot."+getCheckedString());
                else if (time == -1) result.setText("This spot has been reserved."+getCheckedString());
                else result.setText(String.format("This bicycle has been parked for %s."+getCheckedString(), DateUtils.formatElapsedTime(time/1000)));
            }

        });
    }

    private void requestDataFromApi() {
        JsonArrayRequest arrayRequest = new JsonArrayRequest(String.format("http://%s:3000/list", getString(R.string.debug_ip)), new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                CheckBikeFragment.this.jsonRequestCallback(response);
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("JSON request error", error.toString());
                CheckBikeFragment.this.jsonRequestCallback(new JSONArray());
            }
        });
        RequestQueue jsonRequestQueue = Volley.newRequestQueue(this.getActivity());
        jsonRequestQueue.add(arrayRequest);
    }

    private void setListeners() {
        requestParkTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager imm = (InputMethodManager) CheckBikeFragment.this.getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(rootView.getWindowToken(), 0);
                if (validateNumber(numberInput.getText().toString(), 1, sizeData.get(rackSpinner.getSelectedItem()))) {
                    showResult(Integer.valueOf(numberInput.getText().toString())-1);
                } else {
                    Snackbar.make(rootView, "Incorrect slot number. Your slot number should be between 1 and " + sizeData.get(rackSpinner.getSelectedItem()) + " (inclusive).", Snackbar.LENGTH_LONG).show();
                }
                requestDataFromApi();
            }
        });
        numberInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER || actionId == EditorInfo.IME_ACTION_DONE) {
                    requestParkTime.callOnClick();
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_check_bike, container, false);
        requestParkTime = ((Button)rootView.findViewById(R.id.requestParkTime));
        rackSpinner = ((Spinner)rootView.findViewById(R.id.rackSpinner));
        numberInput = ((EditText)rootView.findViewById(R.id.spotInput));
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(this.getActivity(), android.R.layout.simple_spinner_dropdown_item, new String[] {"Loading, please wait..."});
        rackSpinner.setAdapter(spinnerAdapter);
        requestDataFromApi();
        setListeners();
        return rootView;
    }
}

package nl.rug.nc.bicycleclient;


import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


public class ReserveSpotFragment extends Fragment implements Callbackable<Integer> {

    private View rootView;
    private Map<String, Integer> sizeData = new HashMap<>();
    private Map<String, long[]> slotData = new HashMap<>();
    private Map<String, String> ipData = new HashMap<>();
    private Spinner rackSpinner, slotSpinner;
    private Button requestParkTime;
    private TextView resultText;

    public ReserveSpotFragment() {
        // Required empty public constructor
    }

    private void jsonRequestCallback(JSONArray response) {
        rootView.findViewById(R.id.progressBar).setVisibility(rootView.GONE);
        sizeData.clear();
        slotData.clear();
        ipData.clear();
        for (int i = 0; i < response.length(); i++) {
            try {
                JSONObject row = response.getJSONObject(i);
                sizeData.put(row.getString("name"), row.getInt("size"));
                long[] longArray = new long[row.getJSONArray("spots").length()];
                for (int j=0; j<row.getJSONArray("spots").length(); j++) {
                    longArray[j] = row.getJSONArray("spots").getLong(j);
                }
                slotData.put(row.getString("name"), longArray);
                ipData.put(row.getString("name"), row.getString("ip"));
                Log.d("Long data", Arrays.toString(longArray));
            } catch (JSONException je) {
                Log.e("JSON error", je.getMessage());
            }
        }
        if (response.length() == 0) {
            Snackbar.make(rootView, "No bicycle racks currently online.", Snackbar.LENGTH_LONG).show();
            rackSpinner.setEnabled(false);
            slotSpinner.setEnabled(false);
            requestParkTime.setEnabled(false);
        }
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(this.getActivity(), android.R.layout.simple_spinner_dropdown_item, sizeData.keySet().toArray(new String[0]));
        rackSpinner.setAdapter(spinnerAdapter);
    }

    private void requestDataFromApi() {
        JsonArrayRequest arrayRequest = new JsonArrayRequest("http://ian-n551jq:3000/list", new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                ReserveSpotFragment.this.jsonRequestCallback(response);
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("JSON request error", error.toString());
                ReserveSpotFragment.this.jsonRequestCallback(new JSONArray());
            }
        });
        RequestQueue jsonRequestQueue = Volley.newRequestQueue(this.getActivity());
        jsonRequestQueue.add(arrayRequest);
    }

    private void setListeners() {
        requestParkTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int slot = Integer.valueOf(slotSpinner.getSelectedItem().toString())-1;
                String ip = ipData.get(rackSpinner.getSelectedItem().toString());

                new Thread(new SpotReserver(slot, "ian-n551jq", ReserveSpotFragment.this)).start();
            }
        });
        rackSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                if (slotData.isEmpty()) return;
                String selected = ReserveSpotFragment.this.rackSpinner.getSelectedItem().toString();
                if (!slotData.containsKey(selected)) return;
                List<String> slotArray = new LinkedList<String>();
                int i=0;
                for (long l: slotData.get(selected)) {
                    if (l==0) slotArray.add(""+(i+1));
                    i++;
                }
                slotSpinner.setAdapter(new ArrayAdapter<String>(ReserveSpotFragment.this.getActivity(), android.R.layout.simple_spinner_dropdown_item, slotArray.toArray(new String[0])));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {}
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_reserve_spot, container, false);
        requestParkTime = ((Button)rootView.findViewById(R.id.requestParkTime));
        rackSpinner = ((Spinner)rootView.findViewById(R.id.rackSpinner2));
        slotSpinner = ((Spinner)rootView.findViewById(R.id.slotSpinner));
        resultText = ((TextView)rootView.findViewById(R.id.unlockCodeResult));
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(this.getActivity(), android.R.layout.simple_spinner_dropdown_item, new String[] {"Loading, please wait..."});
        rackSpinner.setAdapter(spinnerAdapter);
        requestDataFromApi();
        setListeners();
        return rootView;
    }

    @Override
    public void callback(final Integer param) {
        this.getActivity().runOnUiThread(new Runnable() {

            @Override
            public void run() {
                if (param == -1) {
                    resultText.setText("Could not reserve spot. Please try again later.");
                    return;
                }
                resultText.setText("Spot reserved. You can unlock this spot by using the following code: "+param);
            }
        });
    }
}

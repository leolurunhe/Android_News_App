package com.example.newsapp.ui.trending;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.newsapp.R;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class TrendingFragment extends Fragment {
    private RequestQueue queue;
    private List<Entry> mDataSet;
    private LineChart mChart;
    private EditText mEditText;
    private String mKeyWord;
    public TrendingFragment() {
    }


    @SuppressWarnings("unused")
    public static TrendingFragment newInstance(int columnCount) {
        TrendingFragment fragment = new TrendingFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        queue = Volley.newRequestQueue(requireActivity());
        mDataSet = new ArrayList<>();
        mKeyWord = "CoronaVirus";
        fetchData(mKeyWord);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_trending, container, false);
        mEditText = view.findViewById(R.id.trending_edit_text);
        mChart = view.findViewById(R.id.chart);
        LineDataSet lineDataSet = new LineDataSet(mDataSet, "Trending Chart for " + mKeyWord);
        lineDataSet.setColor(requireActivity().getColor(R.color.colorPrimaryDark));
        lineDataSet.setCircleColor(requireActivity().getColor(R.color.colorPrimaryDark));
        List<ILineDataSet> dataSets = new ArrayList<>();
        dataSets.add(lineDataSet);
        LineData data = new LineData(dataSets);
        Legend legend = mChart.getLegend();
        legend.setTextSize(15);
        legend.setTextColor(Color.BLACK);
        mChart.setData(data);
        mChart.invalidate();
        mEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId == EditorInfo.IME_ACTION_DONE){
                    mKeyWord = mEditText.getText().toString();
                    fetchData(mKeyWord);
                    InputMethodManager imm = (InputMethodManager)v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    assert imm != null;
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    return true;
                }
                return false;
            }
        });
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if(mKeyWord.equals("")){
            mKeyWord = "CoronaVirus";
        }
        fetchData(mKeyWord);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        queue = null;
        mDataSet.clear();
    }

    private void fetchData(final String keyWord){
        String url ="http://ec2-54-89-99-60.compute-1.amazonaws.com:4000/api/trend?keyword=" + keyWord;
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try{
                    mDataSet.clear();
                    if(response.has("default")){
                       if(response.getJSONObject("default").has("timelineData")){
                           JSONArray array = response.getJSONObject("default").getJSONArray("timelineData");
                           for(int i=0; i<array.length(); ++i){
                               if(array.getJSONObject(i).has("value")){
                                   JSONArray temp = array.getJSONObject(i).getJSONArray("value");
                                   if(temp.length() > 0){
                                       int value = temp.getInt(0);
                                       mDataSet.add(new Entry(i, value));
                                   }
                                   else{
                                       mDataSet.add(new Entry(i, 0));
                                   }
                               }
                               else{
                                   mDataSet.add(new Entry(i, 0));
                               }
                           }
                       }
                    }
                    LineDataSet lineDataSet = new LineDataSet(mDataSet, "Trending Chart for " + keyWord);
                    lineDataSet.setColor(requireActivity().getColor(R.color.colorPrimaryDark));
                    lineDataSet.setCircleColor(requireActivity().getColor(R.color.colorPrimaryDark));
                    List<ILineDataSet> dataSets = new ArrayList<>();
                    dataSets.add(lineDataSet);
                    Legend legend = mChart.getLegend();
                    legend.setTextColor(Color.BLACK);
                    legend.setTextSize(15);
                    LineData data = new LineData(dataSets);
                    mChart.setData(data);
                    mChart.invalidate();
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }

        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                System.out.println("This is not working " + error);
            }
        });
        queue.add(jsonObjectRequest);
    }



}

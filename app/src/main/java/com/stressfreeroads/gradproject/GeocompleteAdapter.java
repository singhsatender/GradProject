package com.stressfreeroads.gradproject;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.here.android.mpa.common.GeoCoordinate;
import com.here.android.mpa.common.GeoPosition;

import java.util.ArrayList;
import java.util.List;

/**
 * Suggestion list adapter
 */
public class GeocompleteAdapter extends BaseAdapter implements Filterable {

    private static final String BASE_URL = "http://autocomplete.geocoder.api.here.com/6.2/suggest.json";
    private static final int MAX_NUM = 10;
    private Context mContext;
    private List<String> mResultList = new ArrayList<>();
    private GeoPosition mPosition;
    private GeoCoordinate currentLocation = null;

    public GeocompleteAdapter(Context context, GeoCoordinate location) {
        mContext = context;
        currentLocation = location;
    }

    @Override
    public int getCount() {
        return mResultList.size();
    }

    @Override
    public String getItem(int index) {
        return mResultList.get(index);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.search_result, parent, false);
        }
        ((TextView) convertView.findViewById(R.id.search_result_text)).setText(getItem(position));
        return convertView;
    }

    @Override
    public Filter getFilter() {
        Filter filter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults filterResults = new FilterResults();
                if (constraint != null) {
                    // invoked in worker thread. find locations based on the input by user in constraints
                    List<String> locations = findLocations(mContext, constraint.toString());
                    // Assign the data to the FilterResults
                    filterResults.values = locations;
                    filterResults.count = locations.size();
                }
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                if (results != null && results.count > 0) {
                    mResultList = (List<String>) results.values;
                    notifyDataSetChanged();
                } else {
                    notifyDataSetInvalidated();
                }
            }
        };
        return filter;
    }

    /**
     * Get suggestion from server.
     *
     * @param context    - Activity context to get app id, app token from manifest.
     * @param query_text - Query text
     * @return - List of location
     */
    private List<String> findLocations(Context context, String query_text) {
        GeoNetworkRequest req = new GeoNetworkRequest();
        return req.getResultList(getURL(query_text));
    }

    /**
     * @param position
     */
    public void setPosition(GeoPosition position) {
        mPosition = position;
    }

    /**
     * form a URL to get Suggestion.
     *
     * @param query_text
     * @return
     */
    public String getURL(String query_text) {
        String appId = null;
        String appToken = null;
        try {
            ApplicationInfo ai = mContext.getPackageManager().getApplicationInfo(
                    mContext.getPackageName(), PackageManager.GET_META_DATA);
            Bundle bundle = ai.metaData;
            appId = bundle.getString("com.here.android.maps.appid");
            appToken = bundle.getString("com.here.android.maps.apptoken");
        } catch (Exception e) {
            // do nothing
        }
        System.out.println("current location in adapter= "+ currentLocation);
        Uri.Builder urlBuild = Uri
                .parse(BASE_URL)
                .buildUpon()
                .appendQueryParameter("app_id", appId)
                .appendQueryParameter("app_code", appToken)
                .appendQueryParameter("country", "CAN")
                .appendQueryParameter("maxresults", String.valueOf(MAX_NUM))
                .appendQueryParameter("query", query_text);

        if (mPosition != null) {
            urlBuild.appendQueryParameter("prox", "" + mPosition.getCoordinate().getLatitude() + "," + mPosition.getCoordinate().getLongitude());
        }
        return urlBuild.build().toString();
    }
}

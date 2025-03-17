package com.raah.seedhiraah;

import android.widget.Filter;

import java.util.ArrayList;

public class FilterMosque extends Filter {
    ArrayList<ModelMosque> filterList;
    AdapterMosque adapterMosque;

    public FilterMosque(ArrayList<ModelMosque> filterList, AdapterMosque adapterClasses) {
        this.filterList = filterList;
        this.adapterMosque = adapterClasses;
    }

    @Override
    protected FilterResults performFiltering(CharSequence constraint) {
        FilterResults results = new FilterResults();
         if (constraint != null && constraint.length() > 0) {

             constraint = constraint.toString().toUpperCase();
            ArrayList<ModelMosque> filteredModels = new ArrayList<>();

            for (int i = 0; i < filterList.size(); i++) {
                 if (filterList.get(i).getMosqueName().toUpperCase().contains(constraint)) {
                     filteredModels.add(filterList.get(i));
                }
            }

            results.count = filteredModels.size();
            results.values = filteredModels;
        } else {
            results.count = filterList.size();
            results.values = filterList;
        }
        return results;
    }

    @Override
    protected void publishResults(CharSequence constraint, FilterResults results) {
        adapterMosque.mosqueArrayList = (ArrayList<ModelMosque>) results.values;

        adapterMosque.notifyDataSetChanged();
    }
}

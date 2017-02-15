package com.example.wjbmorgan.addn;

import android.os.Bundle;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;

import org.florescu.android.rangeseekbar.RangeSeekBar;

/*
This class is the hba1c fragment for users to input parameters to
generate the report.
 */

public class hba1c extends PreferenceFragmentCompat {

    public hba1c() {
        // Required empty public constructor
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
//        setPreferencesFromResource(R.xml.pref_hba1c, rootKey);
    }

    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_hba1c);
    }

//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                             Bundle savedInstanceState) {
//        // Inflate the layout for this fragment
//        return inflater.inflate(R.layout.fragment_hba1c, container, false);
//    }


//    @Override
//    public void onAttach(Context context) {
//        super.onAttach(context);
//        if (context instanceof OnFragmentInteractionListener) {
//            mListener = (OnFragmentInteractionListener) context;
//        } else {
//            throw new RuntimeException(context.toString()
//                    + " must implement OnFragmentInteractionListener");
//        }
//    }
//
//    @Override
//    public void onDetach() {
//        super.onDetach();
//        mListener = null;
//    }


    // When the hba1c type is changed by the user, change the range values on
    // corresponding range seek bar.
    public boolean onPreferenceTreeClick(Preference preference) {
        if("hba1c_type".equals(preference.getKey())) {
            SwitchPreference hba1cType = (SwitchPreference) findPreference("hba1c_type");
            RangeSeekBar hba1cRange = (RangeSeekBar) getView().findViewById(R.id.hba1c_range);
            if(hba1cType.isChecked()){
                hba1cRange.setRangeValues(0,15);
            }else {
                hba1cRange.setRangeValues(0,150);
            }
        }
        return super.onPreferenceTreeClick(preference);
    }


}
